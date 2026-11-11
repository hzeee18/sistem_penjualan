package sistempenjualan;

import java.util.Calendar;
import java.util.Date;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

/**
 * Penerimaan barang (barang masuk) per batch + tanggal kedaluwarsa.
 */
public class BarangMasuk extends javax.swing.JPanel implements Halaman {

    private final Bootstrap app;

    public BarangMasuk(Bootstrap app) {
        this.app = app;
        initComponents();
        gaya();
    }

    private void gaya() {
        Theme.page(this);
        Theme.transparent(pnlHeader, pnlJudul, pnlAksi, pnlIsi, pnlFormTombol, pnlJumlah, pnlRiwayatAtas, pnlFilter);
        Theme.title(lblJudul);
        Theme.subtitle(lblSubjudul);
        Theme.secondary(btnRefresh, btnBersihkan);
        Theme.primary(btnSimpan);
        btnRefresh.setIcon(new Ikon("refresh", 16, Theme.TEXT));
        Theme.card(cardForm, 20);
        Theme.card(cardRiwayat);
        Theme.section(lblFormJudul);
        Theme.section(lblRiwayatJudul);
        for (javax.swing.JLabel l : new javax.swing.JLabel[]{lblKategori, lblBarang, lblSupplier, lblNoBatch, lblJumlah, lblTglMasuk, lblTglExp, lblKet}) {
            Theme.label(l);
        }
        Theme.muted(lblSatuan);
        Theme.muted(lblInfoUmur);
        Theme.dateChooser(dcMasuk);
        Theme.dateChooser(dcKedaluwarsa);
        Theme.placeholder(txtJumlah, "0");
        Theme.placeholder(txtKet, "mis. PO-0123 / pembelian rutin");
        Theme.search(txtCari, "Cari batch / barang");
        Theme.table(tblRiwayat);
        cbKategori.setModel(new javax.swing.DefaultComboBoxModel<>(new String[]{"Bahan Baku", "Produk Jadi"}));
    }

    @Override
    public void muatData() {
        try {
            Pilihan.isi(cbSupplier, "- Tanpa supplier -", "SELECT id, nama FROM m_supplier ORDER BY nama");
            muatBarang();
            bersihkan();
            muatRiwayat();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal memuat data: " + e.getMessage());
        }
    }

    private void muatBarang() {
        try {
            String kat = cbKategori.getSelectedIndex() == 1 ? "PRODUK_JADI" : "BAHAN_BAKU";
            Pilihan.isi(cbBarang, null, "SELECT id, CONCAT(kode, ' - ', nama), satuan, umur_simpan_hari, supplier_id "
                    + "FROM m_barang WHERE kategori = ? ORDER BY kode", kat);
            barangDipilih();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal memuat barang: " + e.getMessage());
        }
    }

    private void barangDipilih() {
        Pilihan p = Pilihan.dari(cbBarang);
        if (p == null) {
            lblSatuan.setText("-");
            lblInfoUmur.setText(" ");
            return;
        }
        lblSatuan.setText(String.valueOf(p.data[0]));
        int umur = ((Number) p.data[1]).intValue();
        lblInfoUmur.setText("<html>Umur simpan standar <b>" + umur + " hari</b> (sesuaikan dengan label supplier)</html>");
        if (p.data[2] != null) {
            Pilihan.pilih(cbSupplier, ((Number) p.data[2]).intValue());
        }
        Date masuk = dcMasuk.getDate() == null ? new Date() : dcMasuk.getDate();
        Calendar c = Calendar.getInstance();
        c.setTime(masuk);
        c.add(Calendar.DAY_OF_MONTH, umur);
        dcKedaluwarsa.setDate(c.getTime());
    }

    private void bersihkan() {
        try {
            txtNoBatch.setText(Persediaan.nomor("BM", "batch", "no_batch"));
        } catch (Exception e) {
            txtNoBatch.setText("");
        }
        txtJumlah.setText("");
        txtKet.setText("");
        dcMasuk.setDate(new Date());
        barangDipilih();
    }

    private void simpan() {
        Pilihan barang = Pilihan.dari(cbBarang);
        Pilihan sup = Pilihan.dari(cbSupplier);
        if (barang == null) {
            JOptionPane.showMessageDialog(this, "Pilih barang terlebih dahulu.");
            return;
        }
        if (txtNoBatch.getText().trim().isEmpty() || dcMasuk.getDate() == null || dcKedaluwarsa.getDate() == null) {
            JOptionPane.showMessageDialog(this, "No. batch, tanggal masuk, dan tanggal kedaluwarsa wajib diisi.");
            return;
        }
        double qty;
        try {
            qty = Double.parseDouble(txtJumlah.getText().trim().replace(",", "."));
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Jumlah harus berupa angka.");
            return;
        }
        try {
            Persediaan.terima(barang.id, sup == null ? null : sup.id, txtNoBatch.getText().trim(), qty,
                    dcMasuk.getDate(), dcKedaluwarsa.getDate(), txtKet.getText().trim(), app.getSessionUserId());
            JOptionPane.showMessageDialog(this, "Barang masuk tersimpan.\nBatch " + txtNoBatch.getText().trim() + " • "
                    + Theme.angka(qty) + " " + lblSatuan.getText() + " • kedaluwarsa " + Theme.tgl(dcKedaluwarsa.getDate()));
            bersihkan();
            muatRiwayat();
            Notifikasi.periksaSekarang(false);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal menyimpan: " + e.getMessage());
        }
    }

    private void muatRiwayat() {
        try {
            String cari = "%" + txtCari.getText().trim() + "%";
            DefaultTableModel m = Theme.model("Tgl Masuk", "No Batch", "Barang", "Jumlah", "Kedaluwarsa", "Supplier", "Dicatat Oleh");
            for (Object[] r : Koneksi.rows(
                    "SELECT bt.tgl_masuk, bt.no_batch, b.nama, bt.qty_awal, b.satuan, bt.tgl_kedaluwarsa, s.nama, u.nama_lengkap "
                    + "FROM batch bt JOIN m_barang b ON b.id = bt.barang_id LEFT JOIN m_supplier s ON s.id = bt.supplier_id "
                    + "JOIN m_user u ON u.id = bt.created_by WHERE bt.sumber = 'PEMBELIAN' AND (bt.no_batch LIKE ? OR b.nama LIKE ?) "
                    + "ORDER BY bt.tgl_masuk DESC, bt.id DESC", cari, cari)) {
                m.addRow(new Object[]{r[0], r[1], r[2], Theme.angka(r[3]) + " " + r[4], r[5], r[6] == null ? "-" : r[6], r[7]});
            }
            tblRiwayat.setModel(m);
            Theme.widths(tblRiwayat, 100, 120, 170, 80, 100, 170, 110);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal memuat riwayat: " + e.getMessage());
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
        btnRefresh = new javax.swing.JButton();
        pnlIsi = new javax.swing.JPanel();
        cardForm = new sistempenjualan.RoundedPanel();
        lblFormJudul = new javax.swing.JLabel();
        lblKategori = new javax.swing.JLabel();
        cbKategori = new javax.swing.JComboBox<>();
        lblBarang = new javax.swing.JLabel();
        cbBarang = new javax.swing.JComboBox<>();
        lblSupplier = new javax.swing.JLabel();
        cbSupplier = new javax.swing.JComboBox<>();
        lblNoBatch = new javax.swing.JLabel();
        txtNoBatch = new javax.swing.JTextField();
        lblJumlah = new javax.swing.JLabel();
        pnlJumlah = new javax.swing.JPanel();
        txtJumlah = new javax.swing.JTextField();
        lblSatuan = new javax.swing.JLabel();
        lblTglMasuk = new javax.swing.JLabel();
        dcMasuk = new com.toedter.calendar.JDateChooser();
        lblTglExp = new javax.swing.JLabel();
        dcKedaluwarsa = new com.toedter.calendar.JDateChooser();
        lblKet = new javax.swing.JLabel();
        txtKet = new javax.swing.JTextField();
        lblInfoUmur = new javax.swing.JLabel();
        pnlFormTombol = new javax.swing.JPanel();
        btnBersihkan = new javax.swing.JButton();
        btnSimpan = new javax.swing.JButton();
        cardRiwayat = new sistempenjualan.RoundedPanel();
        pnlRiwayatAtas = new javax.swing.JPanel();
        lblRiwayatJudul = new javax.swing.JLabel();
        pnlFilter = new javax.swing.JPanel();
        txtCari = new javax.swing.JTextField();
        spRiwayat = new javax.swing.JScrollPane();
        tblRiwayat = new javax.swing.JTable();

        setLayout(new java.awt.BorderLayout(0, 20));

        pnlHeader.setLayout(new java.awt.BorderLayout(16, 0));

        pnlJudul.setLayout(new javax.swing.BoxLayout(pnlJudul, javax.swing.BoxLayout.Y_AXIS));

        lblJudul.setText("Barang Masuk");
        pnlJudul.add(lblJudul);

        lblSubjudul.setText("Catat penerimaan barang dari supplier lengkap dengan nomor batch dan tanggal kedaluwarsa");
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
        pnlHeader.add(pnlAksi, java.awt.BorderLayout.EAST);
        add(pnlHeader, java.awt.BorderLayout.NORTH);

        pnlIsi.setLayout(new java.awt.BorderLayout(0, 20));

        cardForm.setLayout(new java.awt.GridBagLayout());

        lblFormJudul.setText("Input Barang Masuk");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 16, 0);
        cardForm.add(lblFormJudul, gridBagConstraints);

        lblKategori.setText("Jenis barang");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 16);
        cardForm.add(lblKategori, gridBagConstraints);

        cbKategori.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        cbKategoriActionPerformed(evt);
                    }
                });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.25;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 14, 16);
        cardForm.add(cbKategori, gridBagConstraints);

        lblBarang.setText("Barang");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 16);
        cardForm.add(lblBarang, gridBagConstraints);

        cbBarang.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        cbBarangActionPerformed(evt);
                    }
                });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 14, 16);
        cardForm.add(cbBarang, gridBagConstraints);

        lblSupplier.setText("Supplier");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 0);
        cardForm.add(lblSupplier, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.25;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 14, 0);
        cardForm.add(cbSupplier, gridBagConstraints);

        lblNoBatch.setText("No. batch");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 16);
        cardForm.add(lblNoBatch, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.25;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 14, 16);
        cardForm.add(txtNoBatch, gridBagConstraints);

        lblJumlah.setText("Jumlah");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 16);
        cardForm.add(lblJumlah, gridBagConstraints);

        pnlJumlah.setLayout(new java.awt.BorderLayout(8, 0));

        pnlJumlah.add(txtJumlah, java.awt.BorderLayout.CENTER);

        lblSatuan.setText("-");
        pnlJumlah.add(lblSatuan, java.awt.BorderLayout.EAST);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.25;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 14, 16);
        cardForm.add(pnlJumlah, gridBagConstraints);

        lblTglMasuk.setText("Tanggal masuk");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 16);
        cardForm.add(lblTglMasuk, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.25;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 14, 16);
        cardForm.add(dcMasuk, gridBagConstraints);

        lblTglExp.setText("Tanggal kedaluwarsa");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 0);
        cardForm.add(lblTglExp, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.25;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 14, 0);
        cardForm.add(dcKedaluwarsa, gridBagConstraints);

        lblKet.setText("Keterangan");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 16);
        cardForm.add(lblKet, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 14, 16);
        cardForm.add(txtKet, gridBagConstraints);

        lblInfoUmur.setText(" ");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 0);
        cardForm.add(lblInfoUmur, gridBagConstraints);

        pnlFormTombol.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 8, 0));

        btnBersihkan.setText("Bersihkan");
        btnBersihkan.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        btnBersihkanActionPerformed(evt);
                    }
                });
        pnlFormTombol.add(btnBersihkan);

        btnSimpan.setText("Simpan Barang Masuk");
        btnSimpan.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        btnSimpanActionPerformed(evt);
                    }
                });
        pnlFormTombol.add(btnSimpan);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 14, 0);
        cardForm.add(pnlFormTombol, gridBagConstraints);
        pnlIsi.add(cardForm, java.awt.BorderLayout.NORTH);

        cardRiwayat.setLayout(new java.awt.BorderLayout(0, 12));

        pnlRiwayatAtas.setLayout(new java.awt.BorderLayout(12, 0));

        lblRiwayatJudul.setText("Riwayat Barang Masuk");
        pnlRiwayatAtas.add(lblRiwayatJudul, java.awt.BorderLayout.WEST);

        pnlFilter.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 8, 0));

        txtCari.setColumns(16);
        txtCari.addKeyListener(new java.awt.event.KeyAdapter() {
                    public void keyReleased(java.awt.event.KeyEvent evt) {
                        txtCariKeyReleased(evt);
                    }
                });
        pnlFilter.add(txtCari);
        pnlRiwayatAtas.add(pnlFilter, java.awt.BorderLayout.EAST);
        cardRiwayat.add(pnlRiwayatAtas, java.awt.BorderLayout.NORTH);

        spRiwayat.setViewportView(tblRiwayat);
        cardRiwayat.add(spRiwayat, java.awt.BorderLayout.CENTER);
        pnlIsi.add(cardRiwayat, java.awt.BorderLayout.CENTER);
        add(pnlIsi, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    private void btnRefreshActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRefreshActionPerformed
        muatData();
    }//GEN-LAST:event_btnRefreshActionPerformed

    private void cbKategoriActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbKategoriActionPerformed
        muatBarang();
    }//GEN-LAST:event_cbKategoriActionPerformed

    private void cbBarangActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbBarangActionPerformed
        barangDipilih();
    }//GEN-LAST:event_cbBarangActionPerformed

    private void btnBersihkanActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBersihkanActionPerformed
        bersihkan();
    }//GEN-LAST:event_btnBersihkanActionPerformed

    private void btnSimpanActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSimpanActionPerformed
        simpan();
    }//GEN-LAST:event_btnSimpanActionPerformed

    private void txtCariKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtCariKeyReleased
        muatRiwayat();
    }//GEN-LAST:event_txtCariKeyReleased

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnBersihkan;
    private javax.swing.JButton btnRefresh;
    private javax.swing.JButton btnSimpan;
    private javax.swing.JPanel cardForm;
    private javax.swing.JPanel cardRiwayat;
    private javax.swing.JComboBox<Object> cbBarang;
    private javax.swing.JComboBox<String> cbKategori;
    private javax.swing.JComboBox<Object> cbSupplier;
    private com.toedter.calendar.JDateChooser dcKedaluwarsa;
    private com.toedter.calendar.JDateChooser dcMasuk;
    private javax.swing.JLabel lblBarang;
    private javax.swing.JLabel lblFormJudul;
    private javax.swing.JLabel lblInfoUmur;
    private javax.swing.JLabel lblJudul;
    private javax.swing.JLabel lblJumlah;
    private javax.swing.JLabel lblKategori;
    private javax.swing.JLabel lblKet;
    private javax.swing.JLabel lblNoBatch;
    private javax.swing.JLabel lblRiwayatJudul;
    private javax.swing.JLabel lblSatuan;
    private javax.swing.JLabel lblSubjudul;
    private javax.swing.JLabel lblSupplier;
    private javax.swing.JLabel lblTglExp;
    private javax.swing.JLabel lblTglMasuk;
    private javax.swing.JPanel pnlAksi;
    private javax.swing.JPanel pnlFilter;
    private javax.swing.JPanel pnlFormTombol;
    private javax.swing.JPanel pnlHeader;
    private javax.swing.JPanel pnlIsi;
    private javax.swing.JPanel pnlJudul;
    private javax.swing.JPanel pnlJumlah;
    private javax.swing.JPanel pnlRiwayatAtas;
    private javax.swing.JScrollPane spRiwayat;
    private javax.swing.JTable tblRiwayat;
    private javax.swing.JTextField txtCari;
    private javax.swing.JTextField txtJumlah;
    private javax.swing.JTextField txtKet;
    private javax.swing.JTextField txtNoBatch;
    // End of variables declaration//GEN-END:variables
}
