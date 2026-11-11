package sistempenjualan;

import javax.swing.JOptionPane;

/**
 * Titik masuk aplikasi "Sistem Monitoring Persediaan dan Kedaluwarsa Barang
 * dengan Notifikasi Otomatis" - PT Mitra Tiga Sepakat.
 *
 * Nama kelas dibiarkan SistemPenjualan supaya konfigurasi project NetBeans
 * lama tetap jalan.
 */
public class SistemPenjualan {

    public static void main(String[] args) {
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");
        Theme.setup();
        try {
            Koneksi.koneksi();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null,
                    "Tidak bisa terhubung ke database MySQL.\n\n"
                    + "Pastikan MySQL di XAMPP sudah Start, lalu jalankan ulang aplikasi.\n\n"
                    + "Detail: " + e.getMessage(),
                    "Koneksi Gagal", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
        java.awt.EventQueue.invokeLater(() -> new Bootstrap().setVisible(true));
    }
}
