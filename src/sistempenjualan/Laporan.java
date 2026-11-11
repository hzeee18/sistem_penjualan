package sistempenjualan;

import java.awt.Frame;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JOptionPane;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.view.JasperViewer;

/**
 * Query bersama dan helper untuk menampilkan laporan JasperReports.
 * File .jrxml ada di paket sistempenjualan/reports dan dikompilasi saat
 * dibuka, jadi tidak perlu file .jasper.
 */
public final class Laporan {

    private Laporan() {
    }

    /**
     * Ringkasan stok per barang. Kolom: 0 id, 1 kode, 2 nama, 3 kategori,
     * 4 satuan, 5 stok_minimum, 6 layak, 7 kedaluwarsa, 8 kedaluwarsa
     * terdekat.
     */
    public static final String SQL_STOK
            = "SELECT b.id, b.kode, b.nama, b.kategori, b.satuan, b.stok_minimum, "
            + "COALESCE(SUM(CASE WHEN bt.tgl_kedaluwarsa >= CURDATE() THEN bt.qty_sisa END),0) AS layak, "
            + "COALESCE(SUM(CASE WHEN bt.tgl_kedaluwarsa < CURDATE() THEN bt.qty_sisa END),0) AS kedaluwarsa, "
            + "MIN(CASE WHEN bt.qty_sisa > 0 AND bt.tgl_kedaluwarsa >= CURDATE() THEN bt.tgl_kedaluwarsa END) AS terdekat "
            + "FROM m_barang b LEFT JOIN batch bt ON bt.barang_id = b.id GROUP BY b.id";

    public static String kategori(String k) {
        return "BAHAN_BAKU".equals(k) ? "Bahan Baku" : "Produk Jadi";
    }

    public static String jenisMutasi(String j) {
        switch (j) {
            case "MASUK":
                return "Masuk";
            case "KELUAR":
                return "Keluar";
            case "KEMAS_PAKAI":
                return "Pakai Kemas";
            case "KEMAS_HASIL":
                return "Hasil Kemas";
            case "PEMUSNAHAN":
                return "Dimusnahkan";
            default:
                return j;
        }
    }

    /** Parameter standar untuk semua laporan. */
    public static Map<String, Object> param() {
        Map<String, Object> p = new HashMap<>();
        p.put("NAMA_PT", Theme.NAMA_PT);
        p.put("DICETAK_OLEH", Bootstrap.instance() == null ? "-" : Bootstrap.instance().getSessionNama());
        p.put("TEMPAT", Persediaan.pengaturan("laporan_tempat", "Jakarta"));
        p.put("MENGETAHUI", Persediaan.pengaturan("laporan_mengetahui", "Kepala Gudang"));
        p.put("BATAS_PERINGATAN", Persediaan.batasHari()[0]);
        p.put("BATAS_KRITIS", Persediaan.batasHari()[1]);
        return p;
    }

    /** Kompilasi + isi + tampilkan laporan di JasperViewer. */
    public static void tampilkan(String nama, Map<String, Object> param, String judul) {
        try {
            JasperPrint print = isi(nama, param);
            if (print.getPages().isEmpty()) {
                JOptionPane.showMessageDialog(Bootstrap.instance(), "Tidak ada data untuk laporan ini.", judul, JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            JasperViewer v = new JasperViewer(print, false);
            v.setTitle(judul + " - " + Theme.NAMA_PT);
            v.setExtendedState(Frame.MAXIMIZED_BOTH);
            v.setVisible(true);
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(Bootstrap.instance(), "Gagal membuat laporan: " + e.getMessage(), judul, JOptionPane.ERROR_MESSAGE);
        }
    }

    public static JasperPrint isi(String nama, Map<String, Object> param) throws Exception {
        try (InputStream in = Laporan.class.getResourceAsStream("/sistempenjualan/reports/" + nama + ".jrxml")) {
            if (in == null) {
                throw new Exception("File laporan " + nama + ".jrxml tidak ditemukan.");
            }
            JasperReport rpt = JasperCompileManager.compileReport(in);
            return JasperFillManager.fillReport(rpt, param, Koneksi.get());
        }
    }
}
