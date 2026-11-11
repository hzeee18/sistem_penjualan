package sistempenjualan;

import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

/**
 * Monitoring stok per barang. Stok "layak" = jumlah sisa semua batch yang
 * belum kedaluwarsa. Status dihitung terhadap stok minimum barang.
 */
public class MonitorStok extends javax.swing.JPanel implements Halaman {

    private final Bootstrap app;
    private final List<Object[]> semua = new ArrayList<>();
    private final List<Integer> idTampil = new ArrayList<>();

    public MonitorStok(Bootstrap app) {
        this.app = app;
        initComponents();
        gaya();
    }

    private void gaya() {
        Theme.page(this);
        Theme.transparent(pnlHeader, pnlJudul, pnlAksi, pnlIsi, pnlKartu, pnlTabel, pnlStokAtas, pnlBatchAtas, pnlFilter);
        Theme.title(lblJudul);
        Theme.subtitle(lblSubjudul);
        Theme.secondary(btnRefresh);
        Theme.primary(btnCetak);
        btnRefresh.setIcon(new Ikon("refresh", 16, Theme.TEXT));
        btnCetak.setIcon(new Ikon("cetak", 16, java.awt.Color.WHITE));
        Theme.statCard(cardAman, lblAmanJudul, lblAman, lblAmanKet, Theme.SUCCESS);
        Theme.statCard(cardMenipis, lblMenipisJudul, lblMenipis, lblMenipisKet, Theme.WARNING);
        Theme.statCard(cardHabis, lblHabisJudul, lblHabis, lblHabisKet, Theme.DANGER);
        Theme.statCard(cardRusak, lblRusakJudul, lblRusak, lblRusakKet, Theme.ACCENT);
        Theme.card(cardStok);
        Theme.card(cardBatch);
        cardBatch.setPreferredSize(new java.awt.Dimension(100, 230));
        Theme.section(lblStokJudul);
        Theme.section(lblBatchJudul);
        Theme.table(tblStok);
        Theme.table(tblBatch);
        Theme.search(txtCari, "Cari kode / nama barang");
        cbKategori.setModel(new javax.swing.DefaultComboBoxModel<>(new String[]{"Semua Kategori", "Bahan Baku", "Produk Jadi"}));
        cbStatus.setModel(new javax.swing.DefaultComboBoxModel<>(new String[]{"Semua Status", "Aman", "Menipis", "Habis"}));
        lblBatchJudul.setText("Rincian Batch  (klik salah satu barang di atas)");
    }

    @Override
    public void muatData() {
        try {
            semua.clear();
            int aman = 0, menipis = 0, habis = 0;
            for (Object[] r : Koneksi.rows("SELECT * FROM (" + Laporan.SQL_STOK + ") s ORDER BY kategori, kode")) {
                double layak = ((Number) r[6]).doubleValue();
                String st = Persediaan.statusStok(layak, ((Number) r[5]).doubleValue());
                if (st.equals("AMAN")) {
                    aman++;
                } else if (st.equals("MENIPIS")) {
                    menipis++;
                } else {
                    habis++;
                }
                semua.add(new Object[]{r[0], r[1], r[2], Laporan.kategori((String) r[3]), r[6], r[7], r[5], r[4], r[8], st});
            }
            lblAman.setText(String.valueOf(aman));
            lblMenipis.setText(String.valueOf(menipis));
            lblHabis.setText(String.valueOf(habis));
            Object[] rusak = Koneksi.one("SELECT COUNT(*) FROM batch WHERE qty_sisa > 0 AND tgl_kedaluwarsa < CURDATE()");
            lblRusak.setText(Theme.angka(rusak[0]));
            lblRusakKet.setText("batch kedaluwarsa, belum dimusnahkan");
            filter();
            tblBatch.setModel(Theme.model("No Batch", "Sumber", "Tgl Masuk", "Kedaluwarsa", "Qty Awal", "Sisa", "Sisa Hari", "Status"));
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal memuat stok: " + e.getMessage());
        }
    }

    private void filter() {
        if (cbKategori.getSelectedItem() == null || cbStatus.getSelectedItem() == null) {
            return;
        }
        String cari = txtCari.getText().trim().toLowerCase();
        String kat = cbKategori.getSelectedIndex() == 0 ? null : cbKategori.getSelectedItem().toString();
        String st = cbStatus.getSelectedIndex() == 0 ? null : cbStatus.getSelectedItem().toString().toUpperCase();
        DefaultTableModel m = Theme.model("Kode", "Nama Barang", "Kategori", "Stok Layak", "Kedaluwarsa", "Minimum", "Satuan", "Exp. Terdekat", "Status");
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
        tblStok.setModel(m);
        Theme.statusColumn(tblStok, 8);
        Theme.widths(tblStok, 70, 200, 100, 90, 90, 80, 60, 110, 100);
        lblStokJudul.setText("Daftar Stok Barang (" + m.getRowCount() + ")");
    }

    private void tampilkanBatch() {
        int v = tblStok.getSelectedRow();
        if (v < 0) {
            return;
        }
        int id = idTampil.get(tblStok.convertRowIndexToModel(v));
        try {
            DefaultTableModel m = Theme.model("No Batch", "Sumber", "Tgl Masuk", "Kedaluwarsa", "Qty Awal", "Sisa", "Sisa Hari", "Status");
            for (Object[] r : Koneksi.rows(
                    "SELECT no_batch, sumber, tgl_masuk, tgl_kedaluwarsa, qty_awal, qty_sisa, DATEDIFF(tgl_kedaluwarsa, CURDATE()) "
                    + "FROM batch WHERE barang_id = ? ORDER BY qty_sisa = 0, tgl_kedaluwarsa", id)) {
                long sisa = ((Number) r[6]).longValue();
                double qty = ((Number) r[5]).doubleValue();
                String st = qty <= 0 ? "HABIS TERPAKAI" : Persediaan.statusKedaluwarsa(sisa);
                m.addRow(new Object[]{r[0], "PEMBELIAN".equals(r[1]) ? "Pembelian" : "Kemas Ulang", r[2], r[3], r[4], r[5],
                    sisa < 0 ? "lewat " + (-sisa) + " hari" : sisa + " hari", st});
            }
            tblBatch.setModel(m);
            Theme.statusColumn(tblBatch, 7);
            lblBatchJudul.setText("Rincian Batch — " + tblStok.getValueAt(v, 1));
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal memuat batch: " + e.getMessage());
        }
    }

    private void cetak() {
        Laporan.tampilkan("laporan_stok", Laporan.param(), "Laporan Stok Persediaan");
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
        btnCetak = new javax.swing.JButton();
        pnlIsi = new javax.swing.JPanel();
        pnlKartu = new javax.swing.JPanel();
        cardAman = new sistempenjualan.RoundedPanel();
        lblAmanJudul = new javax.swing.JLabel();
        lblAman = new javax.swing.JLabel();
        lblAmanKet = new javax.swing.JLabel();
        cardMenipis = new sistempenjualan.RoundedPanel();
        lblMenipisJudul = new javax.swing.JLabel();
        lblMenipis = new javax.swing.JLabel();
        lblMenipisKet = new javax.swing.JLabel();
        cardHabis = new sistempenjualan.RoundedPanel();
        lblHabisJudul = new javax.swing.JLabel();
        lblHabis = new javax.swing.JLabel();
        lblHabisKet = new javax.swing.JLabel();
        cardRusak = new sistempenjualan.RoundedPanel();
        lblRusakJudul = new javax.swing.JLabel();
        lblRusak = new javax.swing.JLabel();
        lblRusakKet = new javax.swing.JLabel();
        pnlTabel = new javax.swing.JPanel();
        cardStok = new sistempenjualan.RoundedPanel();
        pnlStokAtas = new javax.swing.JPanel();
        lblStokJudul = new javax.swing.JLabel();
        pnlFilter = new javax.swing.JPanel();
        txtCari = new javax.swing.JTextField();
        cbKategori = new javax.swing.JComboBox<>();
        cbStatus = new javax.swing.JComboBox<>();
        spStok = new javax.swing.JScrollPane();
        tblStok = new javax.swing.JTable();
        cardBatch = new sistempenjualan.RoundedPanel();
        pnlBatchAtas = new javax.swing.JPanel();
        lblBatchJudul = new javax.swing.JLabel();
        spBatch = new javax.swing.JScrollPane();
        tblBatch = new javax.swing.JTable();

        setLayout(new java.awt.BorderLayout(0, 20));

        pnlHeader.setLayout(new java.awt.BorderLayout(16, 0));

        pnlJudul.setLayout(new javax.swing.BoxLayout(pnlJudul, javax.swing.BoxLayout.Y_AXIS));

        lblJudul.setText("Monitoring Stok");
        pnlJudul.add(lblJudul);

        lblSubjudul.setText("Stok layak pakai per barang dibandingkan dengan stok minimum");
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

        cardAman.addMouseListener(new java.awt.event.MouseAdapter() {
                    public void mouseClicked(java.awt.event.MouseEvent evt) {
                        cardAmanMouseClicked(evt);
                    }
                });
        cardAman.setLayout(new javax.swing.BoxLayout(cardAman, javax.swing.BoxLayout.Y_AXIS));

        lblAmanJudul.setText("STOK AMAN");
        cardAman.add(lblAmanJudul);

        lblAman.setText("0");
        cardAman.add(lblAman);

        lblAmanKet.setText("barang di atas stok minimum");
        cardAman.add(lblAmanKet);
        pnlKartu.add(cardAman);

        cardMenipis.addMouseListener(new java.awt.event.MouseAdapter() {
                    public void mouseClicked(java.awt.event.MouseEvent evt) {
                        cardMenipisMouseClicked(evt);
                    }
                });
        cardMenipis.setLayout(new javax.swing.BoxLayout(cardMenipis, javax.swing.BoxLayout.Y_AXIS));

        lblMenipisJudul.setText("STOK MENIPIS");
        cardMenipis.add(lblMenipisJudul);

        lblMenipis.setText("0");
        cardMenipis.add(lblMenipis);

        lblMenipisKet.setText("di bawah / sama dengan minimum");
        cardMenipis.add(lblMenipisKet);
        pnlKartu.add(cardMenipis);

        cardHabis.addMouseListener(new java.awt.event.MouseAdapter() {
                    public void mouseClicked(java.awt.event.MouseEvent evt) {
                        cardHabisMouseClicked(evt);
                    }
                });
        cardHabis.setLayout(new javax.swing.BoxLayout(cardHabis, javax.swing.BoxLayout.Y_AXIS));

        lblHabisJudul.setText("STOK HABIS");
        cardHabis.add(lblHabisJudul);

        lblHabis.setText("0");
        cardHabis.add(lblHabis);

        lblHabisKet.setText("tidak ada stok layak");
        cardHabis.add(lblHabisKet);
        pnlKartu.add(cardHabis);

        cardRusak.addMouseListener(new java.awt.event.MouseAdapter() {
                    public void mouseClicked(java.awt.event.MouseEvent evt) {
                        cardRusakMouseClicked(evt);
                    }
                });
        cardRusak.setLayout(new javax.swing.BoxLayout(cardRusak, javax.swing.BoxLayout.Y_AXIS));

        lblRusakJudul.setText("STOK KEDALUWARSA");
        cardRusak.add(lblRusakJudul);

        lblRusak.setText("0");
        cardRusak.add(lblRusak);

        lblRusakKet.setText("belum dimusnahkan");
        cardRusak.add(lblRusakKet);
        pnlKartu.add(cardRusak);
        pnlIsi.add(pnlKartu, java.awt.BorderLayout.NORTH);

        pnlTabel.setLayout(new java.awt.BorderLayout(0, 16));

        cardStok.setLayout(new java.awt.BorderLayout(0, 12));

        pnlStokAtas.setLayout(new java.awt.BorderLayout(12, 0));

        lblStokJudul.setText("Daftar Stok Barang");
        pnlStokAtas.add(lblStokJudul, java.awt.BorderLayout.WEST);

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
        pnlStokAtas.add(pnlFilter, java.awt.BorderLayout.EAST);
        cardStok.add(pnlStokAtas, java.awt.BorderLayout.NORTH);

        tblStok.addMouseListener(new java.awt.event.MouseAdapter() {
                    public void mouseClicked(java.awt.event.MouseEvent evt) {
                        tblStokMouseClicked(evt);
                    }
                });
        spStok.setViewportView(tblStok);
        cardStok.add(spStok, java.awt.BorderLayout.CENTER);
        pnlTabel.add(cardStok, java.awt.BorderLayout.CENTER);

        cardBatch.setLayout(new java.awt.BorderLayout(0, 12));

        pnlBatchAtas.setLayout(new java.awt.BorderLayout(12, 0));

        lblBatchJudul.setText("Rincian Batch");
        pnlBatchAtas.add(lblBatchJudul, java.awt.BorderLayout.WEST);
        cardBatch.add(pnlBatchAtas, java.awt.BorderLayout.NORTH);

        spBatch.setViewportView(tblBatch);
        cardBatch.add(spBatch, java.awt.BorderLayout.CENTER);
        pnlTabel.add(cardBatch, java.awt.BorderLayout.SOUTH);
        pnlIsi.add(pnlTabel, java.awt.BorderLayout.CENTER);
        add(pnlIsi, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    private void btnRefreshActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRefreshActionPerformed
        muatData();
    }//GEN-LAST:event_btnRefreshActionPerformed

    private void btnCetakActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCetakActionPerformed
        cetak();
    }//GEN-LAST:event_btnCetakActionPerformed

    private void cardAmanMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_cardAmanMouseClicked
        cbStatus.setSelectedItem("Aman");
    }//GEN-LAST:event_cardAmanMouseClicked

    private void cardMenipisMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_cardMenipisMouseClicked
        cbStatus.setSelectedItem("Menipis");
    }//GEN-LAST:event_cardMenipisMouseClicked

    private void cardHabisMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_cardHabisMouseClicked
        cbStatus.setSelectedItem("Habis");
    }//GEN-LAST:event_cardHabisMouseClicked

    private void cardRusakMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_cardRusakMouseClicked
        app.buka(Bootstrap.MONITOR_EXP);
    }//GEN-LAST:event_cardRusakMouseClicked

    private void txtCariKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtCariKeyReleased
        filter();
    }//GEN-LAST:event_txtCariKeyReleased

    private void cbKategoriActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbKategoriActionPerformed
        filter();
    }//GEN-LAST:event_cbKategoriActionPerformed

    private void cbStatusActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbStatusActionPerformed
        filter();
    }//GEN-LAST:event_cbStatusActionPerformed

    private void tblStokMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tblStokMouseClicked
        tampilkanBatch();
    }//GEN-LAST:event_tblStokMouseClicked

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCetak;
    private javax.swing.JButton btnRefresh;
    private javax.swing.JPanel cardAman;
    private javax.swing.JPanel cardBatch;
    private javax.swing.JPanel cardHabis;
    private javax.swing.JPanel cardMenipis;
    private javax.swing.JPanel cardRusak;
    private javax.swing.JPanel cardStok;
    private javax.swing.JComboBox<String> cbKategori;
    private javax.swing.JComboBox<String> cbStatus;
    private javax.swing.JLabel lblAman;
    private javax.swing.JLabel lblAmanJudul;
    private javax.swing.JLabel lblAmanKet;
    private javax.swing.JLabel lblBatchJudul;
    private javax.swing.JLabel lblHabis;
    private javax.swing.JLabel lblHabisJudul;
    private javax.swing.JLabel lblHabisKet;
    private javax.swing.JLabel lblJudul;
    private javax.swing.JLabel lblMenipis;
    private javax.swing.JLabel lblMenipisJudul;
    private javax.swing.JLabel lblMenipisKet;
    private javax.swing.JLabel lblRusak;
    private javax.swing.JLabel lblRusakJudul;
    private javax.swing.JLabel lblRusakKet;
    private javax.swing.JLabel lblStokJudul;
    private javax.swing.JLabel lblSubjudul;
    private javax.swing.JPanel pnlAksi;
    private javax.swing.JPanel pnlBatchAtas;
    private javax.swing.JPanel pnlFilter;
    private javax.swing.JPanel pnlHeader;
    private javax.swing.JPanel pnlIsi;
    private javax.swing.JPanel pnlJudul;
    private javax.swing.JPanel pnlKartu;
    private javax.swing.JPanel pnlStokAtas;
    private javax.swing.JPanel pnlTabel;
    private javax.swing.JScrollPane spBatch;
    private javax.swing.JScrollPane spStok;
    private javax.swing.JTable tblBatch;
    private javax.swing.JTable tblStok;
    private javax.swing.JTextField txtCari;
    // End of variables declaration//GEN-END:variables
}
