package sistempenjualan;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Logika inti persediaan: penerimaan barang, kemas ulang, barang keluar,
 * pemusnahan, dan aturan FEFO (First Expired First Out).
 */
public final class Persediaan {

    private Persediaan() {
    }

    /** Satu potongan alokasi stok dari sebuah batch. */
    public static class Alokasi {

        public int batchId;
        public String noBatch;
        public double qty;
        public Date kedaluwarsa;
    }

    /** Barang yang diminta pada transaksi barang keluar. */
    public static class Item {

        public int barangId;
        public double qty;

        public Item(int barangId, double qty) {
            this.barangId = barangId;
            this.qty = qty;
        }
    }

    // ------------------------------------------------------------ status

    public static String statusStok(double layak, double minimum) {
        if (layak <= 0) {
            return "HABIS";
        }
        if (layak <= minimum) {
            return "MENIPIS";
        }
        return "AMAN";
    }

    /** sisaHari = tgl_kedaluwarsa - hari ini. */
    public static String statusKedaluwarsa(long sisaHari) {
        int[] b = batasHari();
        if (sisaHari < 0) {
            return "KEDALUWARSA";
        }
        if (sisaHari <= b[1]) {
            return "KRITIS";
        }
        if (sisaHari <= b[0]) {
            return "PERINGATAN";
        }
        return "AMAN";
    }

    /** [batas peringatan, batas kritis] dalam hari. */
    public static int[] batasHari() {
        return new int[]{
            Integer.parseInt(pengaturan("batas_peringatan_hari", "30")),
            Integer.parseInt(pengaturan("batas_kritis_hari", "7"))
        };
    }

    public static String pengaturan(String kunci, String def) {
        try {
            Object[] r = Koneksi.one("SELECT nilai FROM pengaturan WHERE kunci = ?", kunci);
            return r == null ? def : r[0].toString();
        } catch (Exception e) {
            return def;
        }
    }

    public static void simpanPengaturan(String kunci, String nilai) throws Exception {
        Koneksi.exec("INSERT INTO pengaturan (kunci, nilai) VALUES (?, ?) ON DUPLICATE KEY UPDATE nilai = VALUES(nilai)", kunci, nilai);
    }

    // ------------------------------------------------------------ stok

    /** Stok yang masih layak (belum kedaluwarsa). */
    public static double stokLayak(int barangId) throws Exception {
        return Koneksi.number("SELECT COALESCE(SUM(qty_sisa),0) FROM batch WHERE barang_id = ? AND tgl_kedaluwarsa >= CURDATE()", barangId);
    }

    /**
     * Ambil stok dari batch yang paling cepat kedaluwarsa (FEFO). Batch yang
     * sudah kedaluwarsa tidak ikut dipakai.
     */
    public static List<Alokasi> fefo(int barangId, double qty) throws Exception {
        List<Alokasi> hasil = new ArrayList<>();
        double sisa = qty;
        for (Object[] r : Koneksi.rows(
                "SELECT id, no_batch, qty_sisa, tgl_kedaluwarsa FROM batch "
                + "WHERE barang_id = ? AND qty_sisa > 0 AND tgl_kedaluwarsa >= CURDATE() "
                + "ORDER BY tgl_kedaluwarsa, id", barangId)) {
            if (sisa <= 0.000001) {
                break;
            }
            double ada = ((Number) r[2]).doubleValue();
            Alokasi a = new Alokasi();
            a.batchId = ((Number) r[0]).intValue();
            a.noBatch = (String) r[1];
            a.qty = Math.min(ada, sisa);
            a.kedaluwarsa = (Date) r[3];
            hasil.add(a);
            sisa -= a.qty;
        }
        if (sisa > 0.000001) {
            throw new Exception("Stok tidak cukup. Tersedia " + Theme.angka(qty - sisa)
                    + ", dibutuhkan " + Theme.angka(qty) + ".");
        }
        return hasil;
    }

    /** Nomor dokumen otomatis, contoh: BK-261111-001. */
    public static String nomor(String prefix, String tabel, String kolom) throws Exception {
        String dasar = prefix + "-" + new SimpleDateFormat("yyMMdd").format(new Date()) + "-";
        double n = Koneksi.number("SELECT COUNT(*) FROM " + tabel + " WHERE " + kolom + " LIKE ?", dasar + "%");
        String no;
        int i = (int) n + 1;
        do {
            no = dasar + String.format("%03d", i++);
        } while (Koneksi.number("SELECT COUNT(*) FROM " + tabel + " WHERE " + kolom + " = ?", no) > 0);
        return no;
    }

    // ------------------------------------------------------------ transaksi

    /** Penerimaan barang (pembelian dari supplier). */
    public static void terima(int barangId, Integer supplierId, String noBatch, double qty,
            Date tglMasuk, Date tglKedaluwarsa, String ket, int userId) throws Exception {
        if (qty <= 0) {
            throw new Exception("Jumlah harus lebih dari 0.");
        }
        if (!tglKedaluwarsa.after(tglMasuk)) {
            throw new Exception("Tanggal kedaluwarsa harus setelah tanggal masuk.");
        }
        try {
            Koneksi.begin();
            long batchId = Koneksi.exec(
                    "INSERT INTO batch (no_batch, barang_id, sumber, supplier_id, qty_awal, qty_sisa, tgl_masuk, tgl_kedaluwarsa, keterangan, created_by) "
                    + "VALUES (?, ?, 'PEMBELIAN', ?, ?, ?, ?, ?, ?, ?)",
                    noBatch, barangId, supplierId, qty, qty, tglMasuk, tglKedaluwarsa, ket, userId);
            Koneksi.exec("INSERT INTO mutasi_stok (batch_id, jenis, qty, no_referensi, keterangan, created_by) VALUES (?, 'MASUK', ?, ?, ?, ?)",
                    batchId, qty, noBatch, ket, userId);
            Koneksi.commit();
        } catch (Exception e) {
            Koneksi.rollback();
            throw e;
        }
    }

    /** Info kebutuhan bahan untuk kemas ulang: [bahanId, namaBahan, satuan, kebutuhan]. */
    public static Object[] kebutuhanBahan(int produkId, double qtyHasil) throws Exception {
        Object[] p = Koneksi.one(
                "SELECT b.bahan_baku_id, bb.nama, bb.satuan, b.isi_gram FROM m_barang b "
                + "JOIN m_barang bb ON bb.id = b.bahan_baku_id WHERE b.id = ?", produkId);
        if (p == null) {
            throw new Exception("Produk ini belum diatur bahan bakunya (menu Data Barang).");
        }
        double isi = p[3] == null ? 0 : ((Number) p[3]).doubleValue();
        String satuan = (String) p[2];
        double butuh = qtyHasil * isi;
        if ("kg".equalsIgnoreCase(satuan)) {
            butuh = butuh / 1000.0;
        }
        return new Object[]{((Number) p[0]).intValue(), p[1], satuan, butuh};
    }

    /**
     * Kemas ulang: bahan baku curah diambil FEFO lalu menjadi batch produk
     * jadi. Tanggal kedaluwarsa hasil = yang lebih cepat antara bahan yang
     * dipakai dan umur simpan produk.
     */
    public static String kemasUlang(int produkId, double qtyHasil, String catatan, int userId) throws Exception {
        if (qtyHasil <= 0) {
            throw new Exception("Jumlah kemasan harus lebih dari 0.");
        }
        Object[] keb = kebutuhanBahan(produkId, qtyHasil);
        int bahanId = (Integer) keb[0];
        double butuh = (Double) keb[3];
        List<Alokasi> pakai = fefo(bahanId, butuh);
        Date expHasil = tanggalKedaluwarsaHasil(produkId, pakai);
        try {
            Koneksi.begin();
            String noDok = nomor("KM", "kemas_ulang", "no_dokumen");
            String noBatch = nomor("KU", "batch", "no_batch");
            long batchHasil = Koneksi.exec(
                    "INSERT INTO batch (no_batch, barang_id, sumber, qty_awal, qty_sisa, tgl_masuk, tgl_kedaluwarsa, keterangan, created_by) "
                    + "VALUES (?, ?, 'KEMAS_ULANG', ?, ?, CURDATE(), ?, ?, ?)",
                    noBatch, produkId, qtyHasil, qtyHasil, expHasil, "Hasil " + noDok, userId);
            long kuId = Koneksi.exec(
                    "INSERT INTO kemas_ulang (no_dokumen, produk_id, qty_hasil, batch_hasil_id, catatan, created_by) VALUES (?, ?, ?, ?, ?, ?)",
                    noDok, produkId, qtyHasil, batchHasil, catatan, userId);
            for (Alokasi a : pakai) {
                Koneksi.exec("INSERT INTO kemas_ulang_detail (kemas_ulang_id, batch_id, qty_pakai) VALUES (?, ?, ?)", kuId, a.batchId, a.qty);
                Koneksi.exec("UPDATE batch SET qty_sisa = qty_sisa - ? WHERE id = ?", a.qty, a.batchId);
                Koneksi.exec("INSERT INTO mutasi_stok (batch_id, jenis, qty, no_referensi, keterangan, created_by) VALUES (?, 'KEMAS_PAKAI', ?, ?, ?, ?)",
                        a.batchId, a.qty, noDok, "Dipakai kemas ulang", userId);
            }
            Koneksi.exec("INSERT INTO mutasi_stok (batch_id, jenis, qty, no_referensi, keterangan, created_by) VALUES (?, 'KEMAS_HASIL', ?, ?, ?, ?)",
                    batchHasil, qtyHasil, noDok, catatan, userId);
            Koneksi.commit();
            return noDok;
        } catch (Exception e) {
            Koneksi.rollback();
            throw e;
        }
    }

    public static Date tanggalKedaluwarsaHasil(int produkId, List<Alokasi> pakai) throws Exception {
        int umur = (int) Koneksi.number("SELECT umur_simpan_hari FROM m_barang WHERE id = ?", produkId);
        Calendar c = Calendar.getInstance();
        c.add(Calendar.DAY_OF_MONTH, umur);
        Date exp = c.getTime();
        for (Alokasi a : pakai) {
            if (a.kedaluwarsa.before(exp)) {
                exp = a.kedaluwarsa;
            }
        }
        return exp;
    }

    /** Simpan barang keluar dengan FEFO, mengembalikan id dokumen. */
    public static long barangKeluar(int customerId, Date tanggal, String catatan, List<Item> items, int userId) throws Exception {
        if (items.isEmpty()) {
            throw new Exception("Daftar barang masih kosong.");
        }
        try {
            Koneksi.begin();
            String noDok = nomor("BK", "barang_keluar", "no_dokumen");
            long id = Koneksi.exec("INSERT INTO barang_keluar (no_dokumen, customer_id, tanggal, catatan, created_by) VALUES (?, ?, ?, ?, ?)",
                    noDok, customerId, tanggal, catatan, userId);
            for (Item it : items) {
                for (Alokasi a : fefo(it.barangId, it.qty)) {
                    Koneksi.exec("INSERT INTO barang_keluar_detail (barang_keluar_id, batch_id, qty) VALUES (?, ?, ?)", id, a.batchId, a.qty);
                    Koneksi.exec("UPDATE batch SET qty_sisa = qty_sisa - ? WHERE id = ?", a.qty, a.batchId);
                    Koneksi.exec("INSERT INTO mutasi_stok (batch_id, jenis, qty, no_referensi, keterangan, created_by) VALUES (?, 'KELUAR', ?, ?, ?, ?)",
                            a.batchId, a.qty, noDok, catatan, userId);
                }
            }
            Koneksi.commit();
            return id;
        } catch (Exception e) {
            Koneksi.rollback();
            throw e;
        }
    }

    /** Musnahkan sisa stok pada batch kedaluwarsa. */
    public static void musnahkan(int batchId, String ket, int userId) throws Exception {
        try {
            Koneksi.begin();
            Object[] b = Koneksi.one("SELECT qty_sisa, no_batch FROM batch WHERE id = ?", batchId);
            double sisa = ((Number) b[0]).doubleValue();
            if (sisa <= 0) {
                throw new Exception("Batch ini sudah tidak punya sisa stok.");
            }
            Koneksi.exec("UPDATE batch SET qty_sisa = 0 WHERE id = ?", batchId);
            Koneksi.exec("INSERT INTO mutasi_stok (batch_id, jenis, qty, no_referensi, keterangan, created_by) VALUES (?, 'PEMUSNAHAN', ?, ?, ?, ?)",
                    batchId, sisa, b[1], ket, userId);
            Koneksi.commit();
        } catch (Exception e) {
            Koneksi.rollback();
            throw e;
        }
    }
}
