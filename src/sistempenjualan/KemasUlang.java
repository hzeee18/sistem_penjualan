package sistempenjualan;

import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

/**
 * Kemas ulang (repacking): bahan baku curah -> produk kemasan.
 */
public class KemasUlang extends javax.swing.JPanel implements Halaman {

    private final Bootstrap app;
    private boolean bisaProses;

    public KemasUlang(Bootstrap app) {
        this.app = app;
        initComponents();
        gaya();
    }

    private void gaya() {
        Theme.page(this);
        Theme.transparent(pnlHeader, pnlJudul, pnlAksi, pnlIsi, pnlFormTombol, pnlJumlah, pnlRiwayatAtas, pnlFilter, pnlFefo, pnlInput, pnlCatatan);
        Theme.title(lblJudul);
        Theme.subtitle(lblSubjudul);
        Theme.secondary(btnRefresh, btnBersihkan);
        Theme.primary(btnProses);
        btnRefresh.setIcon(new Ikon("refresh", 16, Theme.TEXT));
        Theme.card(cardForm, 20);
        Theme.card(cardRiwayat);
        Theme.card(cardInfo, 14);
        cardInfo.setBackground(Theme.PRIMARY_SOFT);
        ((RoundedPanel) cardInfo).setBorderColor(null);
        Theme.section(lblFormJudul);
        Theme.section(lblRiwayatJudul);
        Theme.label(lblProduk);
        Theme.label(lblJumlah);
        Theme.label(lblCatatan);
                Theme.muted(lblSatuan);
        for (javax.swing.JLabel l : new javax.swing.JLabel[]{lblInfoBahan, lblInfoKebutuhan, lblInfoTersedia, lblInfoExp}) {
            l.setFont(Theme.font(java.awt.Font.PLAIN, 12.5f));
            l.setForeground(Theme.PRIMARY_DARK);
        }
        Theme.placeholder(txtJumlah, "0");
        Theme.placeholder(txtCatatan, "opsional");
        Theme.search(txtCari, "Cari dokumen / produk");
        Theme.table(tblRiwayat);
        Theme.table(tblFefo);
        tblFefo.setRowHeight(28);
        spFefo.setPreferredSize(new java.awt.Dimension(100, 120));
    }

    @Override
    public void muatData() {
        try {
            Pilihan.isi(cbProduk, null,
                    "SELECT p.id, CONCAT(p.kode, ' - ', p.nama), p.satuan FROM m_barang p "
                    + "WHERE p.kategori = 'PRODUK_JADI' AND p.bahan_baku_id IS NOT NULL ORDER BY p.kode");
            bersihkan();
            muatRiwayat();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal memuat data: " + e.getMessage());
        }
    }

    private void bersihkan() {
        txtJumlah.setText("");
        txtCatatan.setText("");
        hitung();
    }

    /** Hitung kebutuhan bahan dan tampilkan batch FEFO yang akan dipakai. */
    private void hitung() {
        bisaProses = false;
        DefaultTableModel fm = Theme.model("No Batch", "Dipakai", "Kedaluwarsa");
        Pilihan p = Pilihan.dari(cbProduk);
        if (p == null) {
            tblFefo.setModel(fm);
            return;
        }
        lblSatuan.setText(String.valueOf(p.data[0]));
        double qty = 0;
        try {
            qty = Double.parseDouble(txtJumlah.getText().trim().replace(",", "."));
        } catch (NumberFormatException ignored) {
        }
        try {
            Object[] keb = Persediaan.kebutuhanBahan(p.id, Math.max(qty, 0));
            int bahanId = (Integer) keb[0];
            String sat = (String) keb[2];
            double tersedia = Persediaan.stokLayak(bahanId);
            Object[] isi = Koneksi.one("SELECT isi_gram FROM m_barang WHERE id = ?", p.id);
            lblInfoBahan.setText("<html>Bahan baku: <b>" + keb[1] + "</b> &nbsp;•&nbsp; isi " + Theme.angka(isi[0]) + " gr / kemasan</html>");
            lblInfoTersedia.setText("<html>Stok bahan layak pakai: <b>" + Theme.angka(tersedia) + " " + sat + "</b></html>");
            if (qty <= 0) {
                lblInfoKebutuhan.setText("Kebutuhan bahan: isi jumlah kemasan dulu");
                lblInfoExp.setText("Kedaluwarsa hasil: -");
                tblFefo.setModel(fm);
                return;
            }
            double butuh = (Double) keb[3];
            lblInfoKebutuhan.setText("<html>Kebutuhan bahan: <b>" + Theme.angka(butuh) + " " + sat + "</b></html>");
            try {
                List<Persediaan.Alokasi> alok = Persediaan.fefo(bahanId, butuh);
                for (Persediaan.Alokasi a : alok) {
                    fm.addRow(new Object[]{a.noBatch, Theme.angka(a.qty) + " " + sat, a.kedaluwarsa});
                }
                lblInfoExp.setText("<html>Kedaluwarsa hasil: <b>" + Theme.tgl(Persediaan.tanggalKedaluwarsaHasil(p.id, alok)) + "</b></html>");
                bisaProses = true;
            } catch (Exception kurang) {
                lblInfoExp.setText("<html><span style='color:#DC2626'>" + kurang.getMessage() + "</span></html>");
            }
            tblFefo.setModel(fm);
        } catch (Exception e) {
            lblInfoBahan.setText(e.getMessage());
        }
    }

    private void proses() {
        Pilihan p = Pilihan.dari(cbProduk);
        hitung();
        if (p == null || !bisaProses) {
            JOptionPane.showMessageDialog(this, "Lengkapi produk dan jumlah kemasan, dan pastikan stok bahan cukup.");
            return;
        }
        double qty = Double.parseDouble(txtJumlah.getText().trim().replace(",", "."));
        int ok = JOptionPane.showConfirmDialog(this, "Proses kemas ulang " + Theme.angka(qty) + " " + lblSatuan.getText() + " " + p.teks + "?",
                "Konfirmasi", JOptionPane.YES_NO_OPTION);
        if (ok != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            String no = Persediaan.kemasUlang(p.id, qty, txtCatatan.getText().trim(), app.getSessionUserId());
            JOptionPane.showMessageDialog(this, "Kemas ulang berhasil. No. dokumen: " + no);
            bersihkan();
            muatRiwayat();
            Notifikasi.periksaSekarang(false);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal: " + e.getMessage());
        }
    }

    private void muatRiwayat() {
        try {
            String cari = "%" + txtCari.getText().trim() + "%";
            DefaultTableModel m = Theme.model("No Dokumen", "Tanggal", "Produk", "Hasil", "Batch Hasil", "Kedaluwarsa", "Bahan Dipakai", "Oleh");
            for (Object[] r : Koneksi.rows(
                    "SELECT k.no_dokumen, k.created_at, p.nama, k.qty_hasil, p.satuan, bt.no_batch, bt.tgl_kedaluwarsa, "
                    + "(SELECT GROUP_CONCAT(CONCAT(b2.no_batch, ' (', TRIM(TRAILING '.' FROM TRIM(TRAILING '0' FROM d.qty_pakai)), ')') SEPARATOR ', ') "
                    + " FROM kemas_ulang_detail d JOIN batch b2 ON b2.id = d.batch_id WHERE d.kemas_ulang_id = k.id), u.nama_lengkap "
                    + "FROM kemas_ulang k JOIN m_barang p ON p.id = k.produk_id JOIN batch bt ON bt.id = k.batch_hasil_id "
                    + "JOIN m_user u ON u.id = k.created_by WHERE k.no_dokumen LIKE ? OR p.nama LIKE ? "
                    + "ORDER BY k.created_at DESC, k.id DESC", cari, cari)) {
                m.addRow(new Object[]{r[0], new java.sql.Date(((java.util.Date) r[1]).getTime()), r[2], Theme.angka(r[3]) + " " + r[4], r[5], r[6], r[7], r[8]});
            }
            tblRiwayat.setModel(m);
            Theme.widths(tblRiwayat, 130, 95, 160, 70, 120, 100, 150, 100);
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
        pnlInput = new javax.swing.JPanel();
        lblFormJudul = new javax.swing.JLabel();
        lblProduk = new javax.swing.JLabel();
        cbProduk = new javax.swing.JComboBox<>();
        lblJumlah = new javax.swing.JLabel();
        pnlJumlah = new javax.swing.JPanel();
        txtJumlah = new javax.swing.JTextField();
        lblSatuan = new javax.swing.JLabel();
        pnlFormTombol = new javax.swing.JPanel();
        btnBersihkan = new javax.swing.JButton();
        btnProses = new javax.swing.JButton();
        pnlFefo = new javax.swing.JPanel();
        cardInfo = new sistempenjualan.RoundedPanel();
        lblInfoBahan = new javax.swing.JLabel();
        lblInfoTersedia = new javax.swing.JLabel();
        lblInfoKebutuhan = new javax.swing.JLabel();
        lblInfoExp = new javax.swing.JLabel();
        spFefo = new javax.swing.JScrollPane();
        tblFefo = new javax.swing.JTable();
        pnlCatatan = new javax.swing.JPanel();
        lblCatatan = new javax.swing.JLabel();
        txtCatatan = new javax.swing.JTextField();
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

        lblJudul.setText("Kemas Ulang");
        pnlJudul.add(lblJudul);

        lblSubjudul.setText("Ubah bahan baku curah menjadi produk kemasan. Bahan diambil otomatis dari batch yang paling cepat kedaluwarsa");
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

        cardForm.setLayout(new java.awt.BorderLayout(24, 0));

        pnlInput.setPreferredSize(new java.awt.Dimension(360, 250));
        pnlInput.setLayout(new java.awt.GridBagLayout());

        lblFormJudul.setText("Proses Kemas Ulang");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 16, 0);
        pnlInput.add(lblFormJudul, gridBagConstraints);

        lblProduk.setText("Produk jadi (hasil kemasan)");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 0);
        pnlInput.add(lblProduk, gridBagConstraints);

        cbProduk.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        cbProdukActionPerformed(evt);
                    }
                });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 14, 0);
        pnlInput.add(cbProduk, gridBagConstraints);

        lblJumlah.setText("Jumlah kemasan");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 0);
        pnlInput.add(lblJumlah, gridBagConstraints);

        pnlJumlah.setLayout(new java.awt.BorderLayout(8, 0));

        txtJumlah.addKeyListener(new java.awt.event.KeyAdapter() {
                    public void keyReleased(java.awt.event.KeyEvent evt) {
                        txtJumlahKeyReleased(evt);
                    }
                });
        pnlJumlah.add(txtJumlah, java.awt.BorderLayout.CENTER);

        lblSatuan.setText("-");
        pnlJumlah.add(lblSatuan, java.awt.BorderLayout.EAST);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 14, 0);
        pnlInput.add(pnlJumlah, gridBagConstraints);

        pnlFormTombol.setLayout(new java.awt.GridLayout(1, 0, 8, 0));

        btnBersihkan.setText("Bersihkan");
        btnBersihkan.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        btnBersihkanActionPerformed(evt);
                    }
                });
        pnlFormTombol.add(btnBersihkan);

        btnProses.setText("Proses");
        btnProses.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        btnProsesActionPerformed(evt);
                    }
                });
        pnlFormTombol.add(btnProses);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.weighty = 1.0;
        pnlInput.add(pnlFormTombol, gridBagConstraints);
        cardForm.add(pnlInput, java.awt.BorderLayout.WEST);

        pnlFefo.setLayout(new java.awt.BorderLayout(0, 10));

        cardInfo.setLayout(new java.awt.GridBagLayout());

        lblInfoBahan.setText("Bahan baku: -");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        cardInfo.add(lblInfoBahan, gridBagConstraints);

        lblInfoTersedia.setText("Tersedia: -");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        cardInfo.add(lblInfoTersedia, gridBagConstraints);

        lblInfoKebutuhan.setText("Kebutuhan: -");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 0);
        cardInfo.add(lblInfoKebutuhan, gridBagConstraints);

        lblInfoExp.setText("Kedaluwarsa hasil: -");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 0);
        cardInfo.add(lblInfoExp, gridBagConstraints);
        pnlFefo.add(cardInfo, java.awt.BorderLayout.NORTH);

        spFefo.setViewportView(tblFefo);
        pnlFefo.add(spFefo, java.awt.BorderLayout.CENTER);

        pnlCatatan.setLayout(new java.awt.BorderLayout(10, 0));

        lblCatatan.setText("Catatan");
        pnlCatatan.add(lblCatatan, java.awt.BorderLayout.WEST);

        pnlCatatan.add(txtCatatan, java.awt.BorderLayout.CENTER);
        pnlFefo.add(pnlCatatan, java.awt.BorderLayout.SOUTH);
        cardForm.add(pnlFefo, java.awt.BorderLayout.CENTER);
        pnlIsi.add(cardForm, java.awt.BorderLayout.NORTH);

        cardRiwayat.setLayout(new java.awt.BorderLayout(0, 12));

        pnlRiwayatAtas.setLayout(new java.awt.BorderLayout(12, 0));

        lblRiwayatJudul.setText("Riwayat Kemas Ulang");
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

    private void cbProdukActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbProdukActionPerformed
        hitung();
    }//GEN-LAST:event_cbProdukActionPerformed

    private void txtJumlahKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtJumlahKeyReleased
        hitung();
    }//GEN-LAST:event_txtJumlahKeyReleased

    private void btnBersihkanActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBersihkanActionPerformed
        bersihkan();
    }//GEN-LAST:event_btnBersihkanActionPerformed

    private void btnProsesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnProsesActionPerformed
        proses();
    }//GEN-LAST:event_btnProsesActionPerformed

    private void txtCariKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtCariKeyReleased
        muatRiwayat();
    }//GEN-LAST:event_txtCariKeyReleased

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnBersihkan;
    private javax.swing.JButton btnProses;
    private javax.swing.JButton btnRefresh;
    private javax.swing.JPanel cardForm;
    private javax.swing.JPanel cardInfo;
    private javax.swing.JPanel cardRiwayat;
    private javax.swing.JComboBox<Object> cbProduk;
    private javax.swing.JLabel lblCatatan;
    private javax.swing.JLabel lblFormJudul;
    private javax.swing.JLabel lblInfoBahan;
    private javax.swing.JLabel lblInfoExp;
    private javax.swing.JLabel lblInfoKebutuhan;
    private javax.swing.JLabel lblInfoTersedia;
    private javax.swing.JLabel lblJudul;
    private javax.swing.JLabel lblJumlah;
    private javax.swing.JLabel lblProduk;
    private javax.swing.JLabel lblRiwayatJudul;
    private javax.swing.JLabel lblSatuan;
    private javax.swing.JLabel lblSubjudul;
    private javax.swing.JPanel pnlAksi;
    private javax.swing.JPanel pnlCatatan;
    private javax.swing.JPanel pnlFefo;
    private javax.swing.JPanel pnlFilter;
    private javax.swing.JPanel pnlFormTombol;
    private javax.swing.JPanel pnlHeader;
    private javax.swing.JPanel pnlInput;
    private javax.swing.JPanel pnlIsi;
    private javax.swing.JPanel pnlJudul;
    private javax.swing.JPanel pnlJumlah;
    private javax.swing.JPanel pnlRiwayatAtas;
    private javax.swing.JScrollPane spFefo;
    private javax.swing.JScrollPane spRiwayat;
    private javax.swing.JTable tblFefo;
    private javax.swing.JTable tblRiwayat;
    private javax.swing.JTextField txtCari;
    private javax.swing.JTextField txtCatatan;
    private javax.swing.JTextField txtJumlah;
    // End of variables declaration//GEN-END:variables
}
