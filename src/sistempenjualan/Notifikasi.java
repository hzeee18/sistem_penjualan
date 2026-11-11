package sistempenjualan;

import java.awt.AWTException;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.Timer;

/**
 * Notifikasi otomatis.
 *
 * - cek() menghitung semua peringatan dari database (stok & kedaluwarsa).
 * - mulai() menjalankan Timer yang mengecek ulang tiap N menit (Pengaturan).
 * - Peringatan baru dimunculkan sebagai popup di pojok kanan bawah aplikasi
 *   dan (kalau diaktifkan) notifikasi Windows lewat System Tray.
 */
public final class Notifikasi {

    private Notifikasi() {
    }

    public static final String KRITIS = "KRITIS";
    public static final String PERINGATAN = "PERINGATAN";

    /** Satu baris notifikasi. */
    public static class Item {

        public String level;      // KRITIS / PERINGATAN
        public String jenis;      // Kedaluwarsa, Hampir Kedaluwarsa, Stok Habis, Stok Menipis
        public String barang;
        public String pesan;
        public java.util.Date tanggal;
        public String kunci;      // unik, untuk mendeteksi notifikasi baru

        Item(String level, String jenis, String barang, String pesan, java.util.Date tanggal, String kunci) {
            this.level = level;
            this.jenis = jenis;
            this.barang = barang;
            this.pesan = pesan;
            this.tanggal = tanggal;
            this.kunci = kunci;
        }
    }

    /** Pendengar perubahan daftar notifikasi (mis. badge lonceng). */
    public interface Listener {

        void notifikasiBerubah(List<Item> semua, List<Item> baru);
    }

    private static Timer timer;
    private static final Set<String> sudahDilihat = new HashSet<>();
    private static final List<Listener> listeners = new ArrayList<>();
    private static List<Item> terakhir = new ArrayList<>();
    private static TrayIcon tray;

    // ------------------------------------------------------------ hitung

    public static List<Item> cek() throws Exception {
        List<Item> out = new ArrayList<>();
        int[] batas = Persediaan.batasHari();

        // kedaluwarsa per batch
        for (Object[] r : Koneksi.rows(
                "SELECT bt.no_batch, b.nama, bt.qty_sisa, b.satuan, bt.tgl_kedaluwarsa, DATEDIFF(bt.tgl_kedaluwarsa, CURDATE()) AS sisa "
                + "FROM batch bt JOIN m_barang b ON b.id = bt.barang_id "
                + "WHERE bt.qty_sisa > 0 AND bt.tgl_kedaluwarsa <= CURDATE() + INTERVAL ? DAY "
                + "ORDER BY bt.tgl_kedaluwarsa", batas[0])) {
            long sisa = ((Number) r[5]).longValue();
            String qty = Theme.angka(r[2]) + " " + r[3];
            if (sisa < 0) {
                out.add(new Item(KRITIS, "Sudah Kedaluwarsa", (String) r[1],
                        "Batch " + r[0] + " (" + qty + ") kedaluwarsa " + (-sisa) + " hari lalu. Segera pisahkan/musnahkan.",
                        (java.util.Date) r[4], "EXP-" + r[0]));
            } else if (sisa <= batas[1]) {
                out.add(new Item(KRITIS, "Hampir Kedaluwarsa", (String) r[1],
                        "Batch " + r[0] + " (" + qty + ") kedaluwarsa " + (sisa == 0 ? "hari ini" : "dalam " + sisa + " hari") + ".",
                        (java.util.Date) r[4], "KRT-" + r[0]));
            } else {
                out.add(new Item(PERINGATAN, "Hampir Kedaluwarsa", (String) r[1],
                        "Batch " + r[0] + " (" + qty + ") kedaluwarsa dalam " + sisa + " hari.",
                        (java.util.Date) r[4], "WRN-" + r[0]));
            }
        }

        // stok menipis / habis (hanya stok layak)
        for (Object[] r : Koneksi.rows(
                "SELECT b.kode, b.nama, b.satuan, b.stok_minimum, "
                + "COALESCE(SUM(CASE WHEN bt.tgl_kedaluwarsa >= CURDATE() THEN bt.qty_sisa END),0) AS layak "
                + "FROM m_barang b LEFT JOIN batch bt ON bt.barang_id = b.id "
                + "GROUP BY b.id HAVING layak <= b.stok_minimum ORDER BY layak")) {
            double layak = ((Number) r[4]).doubleValue();
            String sat = (String) r[2];
            if (layak <= 0) {
                out.add(new Item(KRITIS, "Stok Habis", (String) r[1],
                        "Stok layak " + r[1] + " habis. Segera lakukan pengadaan / kemas ulang.", null, "HBS-" + r[0]));
            } else {
                out.add(new Item(PERINGATAN, "Stok Menipis", (String) r[1],
                        "Sisa " + Theme.angka(layak) + " " + sat + " (minimum " + Theme.angka(r[3]) + " " + sat + ").", null,
                        "MNP-" + r[0] + "-" + (int) layak));
            }
        }
        return out;
    }

    public static List<Item> terakhir() {
        return terakhir;
    }

    public static int jumlahBelumDibaca() {
        int n = 0;
        for (Item i : terakhir) {
            if (!sudahDilihat.contains(i.kunci)) {
                n++;
            }
        }
        return n;
    }

    public static void tandaiSemuaDibaca() {
        for (Item i : terakhir) {
            sudahDilihat.add(i.kunci);
        }
        kabari(new ArrayList<Item>());
    }

    // ------------------------------------------------------------ timer

    public static void addListener(Listener l) {
        listeners.add(l);
    }

    /** Mulai pengecekan otomatis. Dipanggil setelah login. */
    public static void mulai() {
        berhenti();
        int menit = Math.max(1, Integer.parseInt(Persediaan.pengaturan("interval_cek_menit", "5")));
        timer = new Timer(menit * 60 * 1000, e -> periksaSekarang(true));
        timer.setInitialDelay(menit * 60 * 1000);
        timer.start();
    }

    public static void berhenti() {
        if (timer != null) {
            timer.stop();
            timer = null;
        }
    }

    /**
     * Hitung ulang notifikasi. Kalau ada yang baru dan tampilkanPopup = true,
     * munculkan popup & notifikasi Windows.
     */
    public static List<Item> periksaSekarang(boolean tampilkanPopup) {
        try {
            List<Item> semua = cek();
            Set<String> lama = new HashSet<>();
            for (Item i : terakhir) {
                lama.add(i.kunci);
            }
            List<Item> baru = new ArrayList<>();
            for (Item i : semua) {
                if (!lama.contains(i.kunci) && !sudahDilihat.contains(i.kunci)) {
                    baru.add(i);
                }
            }
            terakhir = semua;
            kabari(baru);
            if (tampilkanPopup && !baru.isEmpty()) {
                tampilkan(baru);
            }
            return semua;
        } catch (Exception ex) {
            System.err.println("Cek notifikasi gagal: " + ex.getMessage());
            return terakhir;
        }
    }

    /** Ringkasan saat login: selalu tampil kalau ada peringatan. */
    public static void ringkasanLogin() {
        terakhir = new ArrayList<>();
        sudahDilihat.clear();
        List<Item> semua = periksaSekarang(false);
        if (!semua.isEmpty()) {
            tampilkan(semua);
        }
    }

    private static void kabari(List<Item> baru) {
        for (Listener l : listeners) {
            l.notifikasiBerubah(terakhir, baru);
        }
    }

    private static void tampilkan(List<Item> items) {
        int kritis = 0;
        int warn = 0;
        for (Item i : items) {
            if (KRITIS.equals(i.level)) {
                kritis++;
            } else {
                warn++;
            }
        }
        NotifikasiPopup.tampil(items, kritis, warn);
        if ("1".equals(Persediaan.pengaturan("notifikasi_windows", "1"))) {
            notifWindows(items, kritis, warn);
        }
    }

    // ------------------------------------------------------------ system tray

    private static void notifWindows(List<Item> items, int kritis, int warn) {
        try {
            if (!SystemTray.isSupported()) {
                return;
            }
            if (tray == null) {
                tray = new TrayIcon(ikonTray(), Theme.NAMA_APP + " - " + Theme.NAMA_PT);
                tray.setImageAutoSize(true);
                SystemTray.getSystemTray().add(tray);
            }
            String judul = items.size() + " peringatan persediaan";
            StringBuilder isi = new StringBuilder();
            if (kritis > 0) {
                isi.append(kritis).append(" kritis");
            }
            if (warn > 0) {
                isi.append(isi.length() > 0 ? ", " : "").append(warn).append(" peringatan");
            }
            isi.append("\n").append(items.get(0).barang).append(": ").append(items.get(0).jenis);
            tray.displayMessage(judul, isi.toString(), kritis > 0 ? TrayIcon.MessageType.WARNING : TrayIcon.MessageType.INFO);
        } catch (AWTException | RuntimeException e) {
            System.err.println("Notifikasi Windows tidak tersedia: " + e.getMessage());
        }
    }

    public static void hapusTray() {
        if (tray != null) {
            SystemTray.getSystemTray().remove(tray);
            tray = null;
        }
    }

    static BufferedImage ikonTray() {
        BufferedImage img = new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(Theme.PRIMARY);
        g.fillRoundRect(0, 0, 32, 32, 10, 10);
        g.setColor(Color.WHITE);
        g.setFont(new Font("Segoe UI", Font.BOLD, 15));
        g.drawString("MP", 5, 21);
        g.dispose();
        return img;
    }
}
