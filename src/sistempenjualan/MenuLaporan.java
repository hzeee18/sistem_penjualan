package sistempenjualan;

import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import javax.swing.JOptionPane;

/**
 * Menu laporan (JasperReports).
 */
public class MenuLaporan extends javax.swing.JPanel implements Halaman {

    private final Bootstrap app;

    public MenuLaporan(Bootstrap app) {
        this.app = app;
        initComponents();
        gaya();
    }

    private void gaya() {
        Theme.page(this);
        Theme.transparent(pnlHeader, pnlJudul, pnlAksi, pnlIsi, pnlDaftar);
        Theme.title(lblJudul);
        Theme.subtitle(lblSubjudul);
        Theme.card(cardSetting, 20);
        Theme.section(lblSettingJudul);
        Theme.label(lblDari);
        Theme.label(lblSampai);
        Theme.label(lblTempat);
        Theme.label(lblMengetahui);
        Theme.dateChooser(dcDari);
        Theme.dateChooser(dcSampai);
        Object[][] kartu = {
            {cardStok, lblStokJudul, lblStokDesc, btnStok, Theme.PRIMARY},
            {cardKedaluwarsa, lblKedaluwarsaJudul, lblKedaluwarsaDesc, btnKedaluwarsa, Theme.DANGER},
            {cardMasuk, lblMasukJudul, lblMasukDesc, btnMasuk, Theme.SUCCESS},
            {cardKemas, lblKemasJudul, lblKemasDesc, btnKemas, Theme.ACCENT},
            {cardKeluar, lblKeluarJudul, lblKeluarDesc, btnKeluar, Theme.INFO},
            {cardMusnah, lblMusnahJudul, lblMusnahDesc, btnMusnah, Theme.WARNING},
        };
        for (Object[] k : kartu) {
            RoundedPanel p = (RoundedPanel) k[0];
            Theme.card(p, 22);
            p.setAccent((java.awt.Color) k[4]);
            Theme.section((javax.swing.JLabel) k[1]);
            Theme.muted((javax.swing.JLabel) k[2]);
            javax.swing.JButton b = (javax.swing.JButton) k[3];
            Theme.secondary(b);
            b.setIcon(new Ikon("cetak", 16, Theme.TEXT));
        }
    }

    @Override
    public void muatData() {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.DAY_OF_MONTH, 1);
        if (dcDari.getDate() == null) {
            dcDari.setDate(c.getTime());
            dcSampai.setDate(new Date());
        }
        txtTempat.setText(Persediaan.pengaturan("laporan_tempat", "Jakarta"));
        txtMengetahui.setText(Persediaan.pengaturan("laporan_mengetahui", "Kepala Gudang"));
    }

    private void tampilkan(String jenis) {
        try {
            Persediaan.simpanPengaturan("laporan_tempat", txtTempat.getText().trim().isEmpty() ? "Jakarta" : txtTempat.getText().trim());
            Persediaan.simpanPengaturan("laporan_mengetahui", txtMengetahui.getText().trim().isEmpty() ? "Kepala Gudang" : txtMengetahui.getText().trim());
        } catch (Exception ignored) {
        }
        Map<String, Object> p = Laporan.param();
        Date dari = dcDari.getDate();
        Date sampai = dcSampai.getDate();
        boolean periode = !jenis.equals("Stok") && !jenis.equals("Kedaluwarsa");
        if (periode) {
            if (dari == null || sampai == null || dari.after(sampai)) {
                JOptionPane.showMessageDialog(this, "Periode tidak valid. Isi tanggal dari & sampai dengan benar.");
                return;
            }
            p.put("TGL_DARI", new java.sql.Date(dari.getTime()));
            p.put("TGL_SAMPAI", new java.sql.Date(sampai.getTime()));
            p.put("PERIODE", Theme.tgl(dari) + " s/d " + Theme.tgl(sampai));
        }
        switch (jenis) {
            case "Stok":
                Laporan.tampilkan("laporan_stok", p, "Laporan Stok Persediaan");
                break;
            case "Kedaluwarsa":
                Laporan.tampilkan("laporan_kedaluwarsa", p, "Laporan Kedaluwarsa Barang");
                break;
            case "Masuk":
                Laporan.tampilkan("laporan_barang_masuk", p, "Laporan Barang Masuk");
                break;
            case "Kemas":
                Laporan.tampilkan("laporan_kemas_ulang", p, "Laporan Kemas Ulang");
                break;
            case "Keluar":
                Laporan.tampilkan("laporan_barang_keluar", p, "Laporan Barang Keluar");
                break;
            default:
                Laporan.tampilkan("laporan_pemusnahan", p, "Laporan Pemusnahan Barang");
        }
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        java.awt.GridBagConstraints gridBagConstraints;

        pnlHeader = new javax.swing.JPanel();
        pnlJudul = new javax.swing.JPanel();
        lblJudul = new javax.swing.JLabel();
        lblSubjudul = new javax.swing.JLabel();
        pnlAksi = new javax.swing.JPanel();
        pnlIsi = new javax.swing.JPanel();
        cardSetting = new sistempenjualan.RoundedPanel();
        lblSettingJudul = new javax.swing.JLabel();
        lblDari = new javax.swing.JLabel();
        dcDari = new com.toedter.calendar.JDateChooser();
        lblSampai = new javax.swing.JLabel();
        dcSampai = new com.toedter.calendar.JDateChooser();
        lblTempat = new javax.swing.JLabel();
        txtTempat = new javax.swing.JTextField();
        lblMengetahui = new javax.swing.JLabel();
        txtMengetahui = new javax.swing.JTextField();
        pnlDaftar = new javax.swing.JPanel();
        cardStok = new sistempenjualan.RoundedPanel();
        lblStokJudul = new javax.swing.JLabel();
        lblStokDesc = new javax.swing.JLabel();
        btnStok = new javax.swing.JButton();
        cardKedaluwarsa = new sistempenjualan.RoundedPanel();
        lblKedaluwarsaJudul = new javax.swing.JLabel();
        lblKedaluwarsaDesc = new javax.swing.JLabel();
        btnKedaluwarsa = new javax.swing.JButton();
        cardMasuk = new sistempenjualan.RoundedPanel();
        lblMasukJudul = new javax.swing.JLabel();
        lblMasukDesc = new javax.swing.JLabel();
        btnMasuk = new javax.swing.JButton();
        cardKemas = new sistempenjualan.RoundedPanel();
        lblKemasJudul = new javax.swing.JLabel();
        lblKemasDesc = new javax.swing.JLabel();
        btnKemas = new javax.swing.JButton();
        cardKeluar = new sistempenjualan.RoundedPanel();
        lblKeluarJudul = new javax.swing.JLabel();
        lblKeluarDesc = new javax.swing.JLabel();
        btnKeluar = new javax.swing.JButton();
        cardMusnah = new sistempenjualan.RoundedPanel();
        lblMusnahJudul = new javax.swing.JLabel();
        lblMusnahDesc = new javax.swing.JLabel();
        btnMusnah = new javax.swing.JButton();

        setLayout(new java.awt.BorderLayout(0, 20));

        pnlHeader.setLayout(new java.awt.BorderLayout(16, 0));

        pnlJudul.setLayout(new javax.swing.BoxLayout(pnlJudul, javax.swing.BoxLayout.Y_AXIS));

        lblJudul.setText("Laporan");
        pnlJudul.add(lblJudul);

        lblSubjudul.setText("Cetak laporan persediaan, kedaluwarsa, dan transaksi (bisa disimpan ke PDF/Excel dari jendela laporan)");
        pnlJudul.add(lblSubjudul);
        pnlHeader.add(pnlJudul, java.awt.BorderLayout.CENTER);

        pnlAksi.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 8, 0));
        pnlHeader.add(pnlAksi, java.awt.BorderLayout.EAST);
        add(pnlHeader, java.awt.BorderLayout.NORTH);

        pnlIsi.setLayout(new java.awt.BorderLayout(0, 20));

        cardSetting.setLayout(new java.awt.GridBagLayout());

        lblSettingJudul.setText("Pengaturan Cetak");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 16, 0);
        cardSetting.add(lblSettingJudul, gridBagConstraints);

        lblDari.setText("Periode dari");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 16);
        cardSetting.add(lblDari, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.25;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 14, 16);
        cardSetting.add(dcDari, gridBagConstraints);

        lblSampai.setText("Sampai");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 16);
        cardSetting.add(lblSampai, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.25;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 14, 16);
        cardSetting.add(dcSampai, gridBagConstraints);

        lblTempat.setText("Tempat (tanda tangan)");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 16);
        cardSetting.add(lblTempat, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.25;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 14, 16);
        cardSetting.add(txtTempat, gridBagConstraints);

        lblMengetahui.setText("Mengetahui (jabatan)");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 0);
        cardSetting.add(lblMengetahui, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.25;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 14, 0);
        cardSetting.add(txtMengetahui, gridBagConstraints);
        pnlIsi.add(cardSetting, java.awt.BorderLayout.NORTH);

        pnlDaftar.setLayout(new java.awt.GridLayout(3, 2, 16, 16));

        cardStok.setLayout(new java.awt.GridBagLayout());

        lblStokJudul.setText("Laporan Stok Persediaan");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        cardStok.add(lblStokJudul, gridBagConstraints);

        lblStokDesc.setText("Stok layak, stok kedaluwarsa, dan status setiap barang");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(4, 0, 0, 0);
        cardStok.add(lblStokDesc, gridBagConstraints);

        btnStok.setText("Tampilkan");
        btnStok.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        btnStokActionPerformed(evt);
                    }
                });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 16, 0, 0);
        cardStok.add(btnStok, gridBagConstraints);
        pnlDaftar.add(cardStok);

        cardKedaluwarsa.setLayout(new java.awt.GridBagLayout());

        lblKedaluwarsaJudul.setText("Laporan Kedaluwarsa");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        cardKedaluwarsa.add(lblKedaluwarsaJudul, gridBagConstraints);

        lblKedaluwarsaDesc.setText("Batch sudah & hampir kedaluwarsa (sesuai batas)");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(4, 0, 0, 0);
        cardKedaluwarsa.add(lblKedaluwarsaDesc, gridBagConstraints);

        btnKedaluwarsa.setText("Tampilkan");
        btnKedaluwarsa.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        btnKedaluwarsaActionPerformed(evt);
                    }
                });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 16, 0, 0);
        cardKedaluwarsa.add(btnKedaluwarsa, gridBagConstraints);
        pnlDaftar.add(cardKedaluwarsa);

        cardMasuk.setLayout(new java.awt.GridBagLayout());

        lblMasukJudul.setText("Laporan Barang Masuk");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        cardMasuk.add(lblMasukJudul, gridBagConstraints);

        lblMasukDesc.setText("Penerimaan dari supplier pada periode terpilih");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(4, 0, 0, 0);
        cardMasuk.add(lblMasukDesc, gridBagConstraints);

        btnMasuk.setText("Tampilkan");
        btnMasuk.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        btnMasukActionPerformed(evt);
                    }
                });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 16, 0, 0);
        cardMasuk.add(btnMasuk, gridBagConstraints);
        pnlDaftar.add(cardMasuk);

        cardKemas.setLayout(new java.awt.GridBagLayout());

        lblKemasJudul.setText("Laporan Kemas Ulang");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        cardKemas.add(lblKemasJudul, gridBagConstraints);

        lblKemasDesc.setText("Hasil kemas ulang & bahan yang dipakai");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(4, 0, 0, 0);
        cardKemas.add(lblKemasDesc, gridBagConstraints);

        btnKemas.setText("Tampilkan");
        btnKemas.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        btnKemasActionPerformed(evt);
                    }
                });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 16, 0, 0);
        cardKemas.add(btnKemas, gridBagConstraints);
        pnlDaftar.add(cardKemas);

        cardKeluar.setLayout(new java.awt.GridBagLayout());

        lblKeluarJudul.setText("Laporan Barang Keluar");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        cardKeluar.add(lblKeluarJudul, gridBagConstraints);

        lblKeluarDesc.setText("Pengiriman ke customer per batch pada periode terpilih");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(4, 0, 0, 0);
        cardKeluar.add(lblKeluarDesc, gridBagConstraints);

        btnKeluar.setText("Tampilkan");
        btnKeluar.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        btnKeluarActionPerformed(evt);
                    }
                });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 16, 0, 0);
        cardKeluar.add(btnKeluar, gridBagConstraints);
        pnlDaftar.add(cardKeluar);

        cardMusnah.setLayout(new java.awt.GridBagLayout());

        lblMusnahJudul.setText("Laporan Pemusnahan");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        cardMusnah.add(lblMusnahJudul, gridBagConstraints);

        lblMusnahDesc.setText("Batch kedaluwarsa yang sudah dimusnahkan per periode");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(4, 0, 0, 0);
        cardMusnah.add(lblMusnahDesc, gridBagConstraints);

        btnMusnah.setText("Tampilkan");
        btnMusnah.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        btnMusnahActionPerformed(evt);
                    }
                });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 16, 0, 0);
        cardMusnah.add(btnMusnah, gridBagConstraints);
        pnlDaftar.add(cardMusnah);
        pnlIsi.add(pnlDaftar, java.awt.BorderLayout.CENTER);
        add(pnlIsi, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    private void btnStokActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnStokActionPerformed
        tampilkan("Stok");
    }//GEN-LAST:event_btnStokActionPerformed

    private void btnKedaluwarsaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnKedaluwarsaActionPerformed
        tampilkan("Kedaluwarsa");
    }//GEN-LAST:event_btnKedaluwarsaActionPerformed

    private void btnMasukActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnMasukActionPerformed
        tampilkan("Masuk");
    }//GEN-LAST:event_btnMasukActionPerformed

    private void btnKemasActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnKemasActionPerformed
        tampilkan("Kemas");
    }//GEN-LAST:event_btnKemasActionPerformed

    private void btnKeluarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnKeluarActionPerformed
        tampilkan("Keluar");
    }//GEN-LAST:event_btnKeluarActionPerformed

    private void btnMusnahActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnMusnahActionPerformed
        tampilkan("Musnah");
    }//GEN-LAST:event_btnMusnahActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnKedaluwarsa;
    private javax.swing.JButton btnKeluar;
    private javax.swing.JButton btnKemas;
    private javax.swing.JButton btnMasuk;
    private javax.swing.JButton btnMusnah;
    private javax.swing.JButton btnStok;
    private javax.swing.JPanel cardKedaluwarsa;
    private javax.swing.JPanel cardKeluar;
    private javax.swing.JPanel cardKemas;
    private javax.swing.JPanel cardMasuk;
    private javax.swing.JPanel cardMusnah;
    private javax.swing.JPanel cardSetting;
    private javax.swing.JPanel cardStok;
    private com.toedter.calendar.JDateChooser dcDari;
    private com.toedter.calendar.JDateChooser dcSampai;
    private javax.swing.JLabel lblDari;
    private javax.swing.JLabel lblJudul;
    private javax.swing.JLabel lblKedaluwarsaDesc;
    private javax.swing.JLabel lblKedaluwarsaJudul;
    private javax.swing.JLabel lblKeluarDesc;
    private javax.swing.JLabel lblKeluarJudul;
    private javax.swing.JLabel lblKemasDesc;
    private javax.swing.JLabel lblKemasJudul;
    private javax.swing.JLabel lblMasukDesc;
    private javax.swing.JLabel lblMasukJudul;
    private javax.swing.JLabel lblMengetahui;
    private javax.swing.JLabel lblMusnahDesc;
    private javax.swing.JLabel lblMusnahJudul;
    private javax.swing.JLabel lblSampai;
    private javax.swing.JLabel lblSettingJudul;
    private javax.swing.JLabel lblStokDesc;
    private javax.swing.JLabel lblStokJudul;
    private javax.swing.JLabel lblSubjudul;
    private javax.swing.JLabel lblTempat;
    private javax.swing.JPanel pnlAksi;
    private javax.swing.JPanel pnlDaftar;
    private javax.swing.JPanel pnlHeader;
    private javax.swing.JPanel pnlIsi;
    private javax.swing.JPanel pnlJudul;
    private javax.swing.JTextField txtMengetahui;
    private javax.swing.JTextField txtTempat;
    // End of variables declaration//GEN-END:variables
}
