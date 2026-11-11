package sistempenjualan;

import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

/**
 * Monitoring kedaluwarsa per batch.
 *
 * Status: KEDALUWARSA (lewat), KRITIS (&lt;= batas kritis), PERINGATAN
 * (&lt;= batas peringatan), AMAN. Batas hari diatur di menu Notifikasi.
 */
public class MonitorKedaluwarsa extends javax.swing.JPanel implements Halaman {

    private final Bootstrap app;
    private final List<Object[]> semua = new ArrayList<>();
    private final List<Integer> idTampil = new ArrayList<>();

    public MonitorKedaluwarsa(Bootstrap app) {
        this.app = app;
        initComponents();
        gaya();
    }

    private void gaya() {
        Theme.page(this);
        Theme.transparent(pnlHeader, pnlJudul, pnlAksi, pnlIsi, pnlKartu, pnlExpAtas, pnlFilter);
        Theme.title(lblJudul);
        Theme.subtitle(lblSubjudul);
        Theme.secondary(btnRefresh);
        Theme.danger(btnMusnahkan);
        Theme.primary(btnCetak);
        btnRefresh.setIcon(new Ikon("refresh", 16, Theme.TEXT));
        btnCetak.setIcon(new Ikon("cetak", 16, java.awt.Color.WHITE));
        Theme.statCard(cardLewat, lblLewatJudul, lblLewat, lblLewatKet, Theme.DANGER);
        Theme.statCard(cardKritis, lblKritisJudul, lblKritis, lblKritisKet, Theme.WARNING);
        Theme.statCard(cardPeringatan, lblPeringatanJudul, lblPeringatan, lblPeringatanKet, Theme.CAUTION);
        Theme.statCard(cardAman, lblAmanJudul, lblAman, lblAmanKet, Theme.SUCCESS);
        Theme.card(cardExp);
        Theme.section(lblExpJudul);
        Theme.muted(lblKeterangan);
        Theme.table(tblExp);
        Theme.search(txtCari, "Cari batch / barang");
        cbKategori.setModel(new javax.swing.DefaultComboBoxModel<>(new String[]{"Semua Kategori", "Bahan Baku", "Produk Jadi"}));
        cbStatus.setModel(new javax.swing.DefaultComboBoxModel<>(new String[]{"Semua Status", "Kedaluwarsa", "Kritis", "Peringatan", "Aman"}));
    }

    @Override
    public void muatData() {
        try {
            int[] batas = Persediaan.batasHari();
            semua.clear();
            int lewat = 0, kritis = 0, warn = 0, aman = 0;
            double qLewat = 0;
            for (Object[] r : Koneksi.rows(
                    "SELECT bt.id, bt.no_batch, b.nama, b.kategori, bt.qty_sisa, b.satuan, bt.tgl_masuk, bt.tgl_kedaluwarsa, "
                    + "DATEDIFF(bt.tgl_kedaluwarsa, CURDATE()) FROM batch bt JOIN m_barang b ON b.id = bt.barang_id "
                    + "WHERE bt.qty_sisa > 0 ORDER BY bt.tgl_kedaluwarsa")) {
                long sisa = ((Number) r[8]).longValue();
                String st = Persediaan.statusKedaluwarsa(sisa);
                switch (st) {
                    case "KEDALUWARSA":
                        lewat++;
                        break;
                    case "KRITIS":
                        kritis++;
                        break;
                    case "PERINGATAN":
                        warn++;
                        break;
                    default:
                        aman++;
                }
                semua.add(new Object[]{r[0], r[1], r[2], Laporan.kategori((String) r[3]), r[4], r[5], r[6], r[7],
                    sisa < 0 ? "lewat " + (-sisa) + " hari" : (sisa == 0 ? "hari ini" : sisa + " hari"), st});
            }
            lblLewat.setText(String.valueOf(lewat));
            lblLewatKet.setText("batch, wajib dipisahkan & dimusnahkan");
            lblKritis.setText(String.valueOf(kritis));
            lblKritisKet.setText("kedaluwarsa ≤ " + batas[1] + " hari lagi");
            lblPeringatan.setText(String.valueOf(warn));
            lblPeringatanKet.setText("kedaluwarsa ≤ " + batas[0] + " hari lagi");
            lblAman.setText(String.valueOf(aman));
            lblAmanKet.setText("kedaluwarsa > " + batas[0] + " hari lagi");
            lblKeterangan.setText("Barang keluar & kemas ulang otomatis mengambil batch yang paling cepat kedaluwarsa (FEFO). "
                    + "Batch yang sudah kedaluwarsa tidak bisa dipakai dan sebaiknya dimusnahkan.");
            filter();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal memuat data: " + e.getMessage());
        }
    }

    private void filter() {
        if (cbKategori.getSelectedItem() == null || cbStatus.getSelectedItem() == null) {
            return;
        }
        String cari = txtCari.getText().trim().toLowerCase();
        String kat = cbKategori.getSelectedIndex() == 0 ? null : cbKategori.getSelectedItem().toString();
        String st = cbStatus.getSelectedIndex() == 0 ? null : cbStatus.getSelectedItem().toString().toUpperCase();
        DefaultTableModel m = Theme.model("No Batch", "Nama Barang", "Kategori", "Sisa", "Satuan", "Tgl Masuk", "Kedaluwarsa", "Sisa Waktu", "Status");
        idTampil.clear();
        for (Object[] r : semua) {
            if (!cari.isEmpty() && !(r[1] + " " + r[2]).toLowerCase().contains(cari)) {
                continue;
            }
            if (kat != null && !kat.equals(r[3])) {
                continue;
            }
            if (st != null && !st.equals(r[9])) {
                continue;
            }
            idTampil.add(((Number) r[0]).intValue());
            m.addRow(new Object[]{r[1], r[2], r[3], r[4], r[5], r[6], r[7], r[8], r[9]});
        }
        tblExp.setModel(m);
        Theme.statusColumn(tblExp, 8);
        Theme.widths(tblExp, 110, 200, 100, 70, 60, 100, 100, 100, 110);
        lblExpJudul.setText("Daftar Batch (" + m.getRowCount() + ")");
    }

    private void musnahkan() {
        int v = tblExp.getSelectedRow();
        if (v < 0) {
            JOptionPane.showMessageDialog(this, "Pilih dulu batch yang sudah kedaluwarsa di tabel.");
            return;
        }
        int mr = tblExp.convertRowIndexToModel(v);
        if (!"KEDALUWARSA".equals(tblExp.getModel().getValueAt(mr, 8))) {
            JOptionPane.showMessageDialog(this, "Hanya batch berstatus KEDALUWARSA yang bisa dimusnahkan.");
            return;
        }
        String batch = (String) tblExp.getModel().getValueAt(mr, 0);
        String ket = JOptionPane.showInputDialog(this,
                "Musnahkan sisa stok batch " + batch + " (" + tblExp.getModel().getValueAt(mr, 1) + ")?\nKeterangan / berita acara:",
                "Pemusnahan barang kedaluwarsa");
        if (ket == null) {
            return;
        }
        try {
            Persediaan.musnahkan(idTampil.get(mr), ket.trim().isEmpty() ? "Pemusnahan barang kedaluwarsa" : ket.trim(), app.getSessionUserId());
            JOptionPane.showMessageDialog(this, "Batch " + batch + " berhasil dimusnahkan.");
            muatData();
            Notifikasi.periksaSekarang(false);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal: " + e.getMessage());
        }
    }

    private void cetak() {
        Laporan.tampilkan("laporan_kedaluwarsa", Laporan.param(), "Laporan Kedaluwarsa Barang");
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        pnlHeader = new javax.swing.JPanel();
        pnlJudul = new javax.swing.JPanel();
        lblJudul = new javax.swing.JLabel();
        lblSubjudul = new javax.swing.JLabel();
        pnlAksi = new javax.swing.JPanel();
        btnRefresh = new javax.swing.JButton();
        btnMusnahkan = new javax.swing.JButton();
        btnCetak = new javax.swing.JButton();
        pnlIsi = new javax.swing.JPanel();
        pnlKartu = new javax.swing.JPanel();
        cardLewat = new sistempenjualan.RoundedPanel();
        lblLewatJudul = new javax.swing.JLabel();
        lblLewat = new javax.swing.JLabel();
        lblLewatKet = new javax.swing.JLabel();
        cardKritis = new sistempenjualan.RoundedPanel();
        lblKritisJudul = new javax.swing.JLabel();
        lblKritis = new javax.swing.JLabel();
        lblKritisKet = new javax.swing.JLabel();
        cardPeringatan = new sistempenjualan.RoundedPanel();
        lblPeringatanJudul = new javax.swing.JLabel();
        lblPeringatan = new javax.swing.JLabel();
        lblPeringatanKet = new javax.swing.JLabel();
        cardAman = new sistempenjualan.RoundedPanel();
        lblAmanJudul = new javax.swing.JLabel();
        lblAman = new javax.swing.JLabel();
        lblAmanKet = new javax.swing.JLabel();
        cardExp = new sistempenjualan.RoundedPanel();
        pnlExpAtas = new javax.swing.JPanel();
        lblExpJudul = new javax.swing.JLabel();
        pnlFilter = new javax.swing.JPanel();
        txtCari = new javax.swing.JTextField();
        cbKategori = new javax.swing.JComboBox<>();
        cbStatus = new javax.swing.JComboBox<>();
        spExp = new javax.swing.JScrollPane();
        tblExp = new javax.swing.JTable();
        lblKeterangan = new javax.swing.JLabel();

        setLayout(new java.awt.BorderLayout(0, 20));

        pnlHeader.setLayout(new java.awt.BorderLayout(16, 0));

        pnlJudul.setLayout(new javax.swing.BoxLayout(pnlJudul, javax.swing.BoxLayout.Y_AXIS));

        lblJudul.setText("Monitoring Kedaluwarsa");
        pnlJudul.add(lblJudul);

        lblSubjudul.setText("Pantau tanggal kedaluwarsa setiap batch bahan baku dan produk jadi");
        pnlJudul.add(lblSubjudul);
        pnlHeader.add(pnlJudul, java.awt.BorderLayout.CENTER);

        pnlAksi.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 8, 0));

        btnRefresh.setText("Refresh");
        btnRefresh.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        btnRefreshActionPerformed(evt);
                    }
                });
        pnlAksi.add(btnRefresh);

        btnMusnahkan.setText("Musnahkan Batch");
        btnMusnahkan.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        btnMusnahkanActionPerformed(evt);
                    }
                });
        pnlAksi.add(btnMusnahkan);

        btnCetak.setText("Cetak Laporan");
        btnCetak.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        btnCetakActionPerformed(evt);
                    }
                });
        pnlAksi.add(btnCetak);
        pnlHeader.add(pnlAksi, java.awt.BorderLayout.EAST);
        add(pnlHeader, java.awt.BorderLayout.NORTH);

        pnlIsi.setLayout(new java.awt.BorderLayout(0, 18));

        pnlKartu.setLayout(new java.awt.GridLayout(1, 4, 16, 0));

        cardLewat.addMouseListener(new java.awt.event.MouseAdapter() {
                    public void mouseClicked(java.awt.event.MouseEvent evt) {
                        cardLewatMouseClicked(evt);
                    }
                });
        cardLewat.setLayout(new javax.swing.BoxLayout(cardLewat, javax.swing.BoxLayout.Y_AXIS));

        lblLewatJudul.setText("SUDAH KEDALUWARSA");
        cardLewat.add(lblLewatJudul);

        lblLewat.setText("0");
        cardLewat.add(lblLewat);

        lblLewatKet.setText("-");
        cardLewat.add(lblLewatKet);
        pnlKartu.add(cardLewat);

        cardKritis.addMouseListener(new java.awt.event.MouseAdapter() {
                    public void mouseClicked(java.awt.event.MouseEvent evt) {
                        cardKritisMouseClicked(evt);
                    }
                });
        cardKritis.setLayout(new javax.swing.BoxLayout(cardKritis, javax.swing.BoxLayout.Y_AXIS));

        lblKritisJudul.setText("KRITIS");
        cardKritis.add(lblKritisJudul);

        lblKritis.setText("0");
        cardKritis.add(lblKritis);

        lblKritisKet.setText("-");
        cardKritis.add(lblKritisKet);
        pnlKartu.add(cardKritis);

        cardPeringatan.addMouseListener(new java.awt.event.MouseAdapter() {
                    public void mouseClicked(java.awt.event.MouseEvent evt) {
                        cardPeringatanMouseClicked(evt);
                    }
                });
        cardPeringatan.setLayout(new javax.swing.BoxLayout(cardPeringatan, javax.swing.BoxLayout.Y_AXIS));

        lblPeringatanJudul.setText("PERINGATAN");
        cardPeringatan.add(lblPeringatanJudul);

        lblPeringatan.setText("0");
        cardPeringatan.add(lblPeringatan);

        lblPeringatanKet.setText("-");
        cardPeringatan.add(lblPeringatanKet);
        pnlKartu.add(cardPeringatan);

        cardAman.addMouseListener(new java.awt.event.MouseAdapter() {
                    public void mouseClicked(java.awt.event.MouseEvent evt) {
                        cardAmanMouseClicked(evt);
                    }
                });
        cardAman.setLayout(new javax.swing.BoxLayout(cardAman, javax.swing.BoxLayout.Y_AXIS));

        lblAmanJudul.setText("AMAN");
        cardAman.add(lblAmanJudul);

        lblAman.setText("0");
        cardAman.add(lblAman);

        lblAmanKet.setText("-");
        cardAman.add(lblAmanKet);
        pnlKartu.add(cardAman);
        pnlIsi.add(pnlKartu, java.awt.BorderLayout.NORTH);

        cardExp.setLayout(new java.awt.BorderLayout(0, 12));

        pnlExpAtas.setLayout(new java.awt.BorderLayout(12, 0));

        lblExpJudul.setText("Daftar Batch");
        pnlExpAtas.add(lblExpJudul, java.awt.BorderLayout.WEST);

        pnlFilter.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 8, 0));

        txtCari.setColumns(16);
        txtCari.addKeyListener(new java.awt.event.KeyAdapter() {
                    public void keyReleased(java.awt.event.KeyEvent evt) {
                        txtCariKeyReleased(evt);
                    }
                });
        pnlFilter.add(txtCari);

        cbKategori.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        cbKategoriActionPerformed(evt);
                    }
                });
        pnlFilter.add(cbKategori);

        cbStatus.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        cbStatusActionPerformed(evt);
                    }
                });
        pnlFilter.add(cbStatus);
        pnlExpAtas.add(pnlFilter, java.awt.BorderLayout.EAST);
        cardExp.add(pnlExpAtas, java.awt.BorderLayout.NORTH);

        spExp.setViewportView(tblExp);
        cardExp.add(spExp, java.awt.BorderLayout.CENTER);

        lblKeterangan.setText("-");
        cardExp.add(lblKeterangan, java.awt.BorderLayout.SOUTH);
        pnlIsi.add(cardExp, java.awt.BorderLayout.CENTER);
        add(pnlIsi, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    private void btnRefreshActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRefreshActionPerformed
        muatData();
    }//GEN-LAST:event_btnRefreshActionPerformed

    private void btnMusnahkanActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnMusnahkanActionPerformed
        musnahkan();
    }//GEN-LAST:event_btnMusnahkanActionPerformed

    private void btnCetakActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCetakActionPerformed
        cetak();
    }//GEN-LAST:event_btnCetakActionPerformed

    private void cardLewatMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_cardLewatMouseClicked
        cbStatus.setSelectedItem("Kedaluwarsa");
    }//GEN-LAST:event_cardLewatMouseClicked

    private void cardKritisMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_cardKritisMouseClicked
        cbStatus.setSelectedItem("Kritis");
    }//GEN-LAST:event_cardKritisMouseClicked

    private void cardPeringatanMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_cardPeringatanMouseClicked
        cbStatus.setSelectedItem("Peringatan");
    }//GEN-LAST:event_cardPeringatanMouseClicked

    private void cardAmanMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_cardAmanMouseClicked
        cbStatus.setSelectedItem("Aman");
    }//GEN-LAST:event_cardAmanMouseClicked

    private void txtCariKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtCariKeyReleased
        filter();
    }//GEN-LAST:event_txtCariKeyReleased

    private void cbKategoriActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbKategoriActionPerformed
        filter();
    }//GEN-LAST:event_cbKategoriActionPerformed

    private void cbStatusActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbStatusActionPerformed
        filter();
    }//GEN-LAST:event_cbStatusActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCetak;
    private javax.swing.JButton btnMusnahkan;
    private javax.swing.JButton btnRefresh;
    private javax.swing.JPanel cardAman;
    private javax.swing.JPanel cardExp;
    private javax.swing.JPanel cardKritis;
    private javax.swing.JPanel cardLewat;
    private javax.swing.JPanel cardPeringatan;
    private javax.swing.JComboBox<String> cbKategori;
    private javax.swing.JComboBox<String> cbStatus;
    private javax.swing.JLabel lblAman;
    private javax.swing.JLabel lblAmanJudul;
    private javax.swing.JLabel lblAmanKet;
    private javax.swing.JLabel lblExpJudul;
    private javax.swing.JLabel lblJudul;
    private javax.swing.JLabel lblKeterangan;
    private javax.swing.JLabel lblKritis;
    private javax.swing.JLabel lblKritisJudul;
    private javax.swing.JLabel lblKritisKet;
    private javax.swing.JLabel lblLewat;
    private javax.swing.JLabel lblLewatJudul;
    private javax.swing.JLabel lblLewatKet;
    private javax.swing.JLabel lblPeringatan;
    private javax.swing.JLabel lblPeringatanJudul;
    private javax.swing.JLabel lblPeringatanKet;
    private javax.swing.JLabel lblSubjudul;
    private javax.swing.JPanel pnlAksi;
    private javax.swing.JPanel pnlExpAtas;
    private javax.swing.JPanel pnlFilter;
    private javax.swing.JPanel pnlHeader;
    private javax.swing.JPanel pnlIsi;
    private javax.swing.JPanel pnlJudul;
    private javax.swing.JPanel pnlKartu;
    private javax.swing.JScrollPane spExp;
    private javax.swing.JTable tblExp;
    private javax.swing.JTextField txtCari;
    // End of variables declaration//GEN-END:variables
}
