package sistempenjualan;

import javax.swing.table.DefaultTableModel;

/**
 * Dashboard: kartu ringkasan, batch yang segera kedaluwarsa, stok yang perlu
 * diisi ulang, dan aktivitas stok terakhir.
 */
public class Dashboard extends javax.swing.JPanel implements Halaman {

    private final Bootstrap app;

    public Dashboard(Bootstrap app) {
        this.app = app;
        initComponents();
        gaya();
    }

    private void gaya() {
        Theme.page(this);
        Theme.transparent(pnlHeader, pnlJudul, pnlAksi, pnlIsi, pnlKartu, pnlTabel, pnlExpAtas, pnlStokAtas);
        Theme.title(lblJudul);
        Theme.subtitle(lblSubjudul);
        Theme.secondary(btnRefresh, btnLihatExp, btnLihatStok);
        btnRefresh.setIcon(new Ikon("refresh", 16, Theme.TEXT));
        Theme.statCard(cardTotalBarang, lblTotalBarangJudul, lblTotalBarang, lblTotalBarangKet, Theme.PRIMARY);
        Theme.statCard(cardMenipis, lblMenipisJudul, lblMenipis, lblMenipisKet, Theme.WARNING);
        Theme.statCard(cardHampir, lblHampirJudul, lblHampir, lblHampirKet, Theme.CAUTION);
        Theme.statCard(cardKedaluwarsa, lblKedaluwarsaJudul, lblKedaluwarsa, lblKedaluwarsaKet, Theme.DANGER);
        Theme.card(cardExp);
        Theme.card(cardStok);
        Theme.card(cardAktivitas);
        Theme.section(lblExpJudul);
        Theme.section(lblStokJudul);
        Theme.section(lblAktivitasJudul);
        Theme.table(tblExp);
        Theme.table(tblStok);
        Theme.table(tblAktivitas);
    }

    @Override
    public void muatData() {
        lblSubjudul.setText("Ringkasan persediaan dan kedaluwarsa barang per " + Theme.hariIni());
        try {
            int[] batas = Persediaan.batasHari();
            Object[] b = Koneksi.one("SELECT COUNT(*), SUM(kategori='BAHAN_BAKU'), SUM(kategori='PRODUK_JADI') FROM m_barang");
            lblTotalBarang.setText(Theme.angka(b[0]));
            lblTotalBarangKet.setText(Theme.angka(b[1]) + " bahan baku • " + Theme.angka(b[2]) + " produk jadi");

            int habis = 0;
            int menipis = 0;
            DefaultTableModel ms = Theme.model("Barang", "Stok Layak", "Minimum", "Status");
            for (Object[] r : Koneksi.rows("SELECT * FROM (" + Laporan.SQL_STOK + ") s ORDER BY layak / GREATEST(stok_minimum,1)")) {
                double layak = ((Number) r[6]).doubleValue();
                double min = ((Number) r[5]).doubleValue();
                String st = Persediaan.statusStok(layak, min);
                if (st.equals("AMAN")) {
                    continue;
                }
                if (st.equals("HABIS")) {
                    habis++;
                } else {
                    menipis++;
                }
                ms.addRow(new Object[]{r[2], Theme.angka(layak) + " " + r[4], Theme.angka(min) + " " + r[4], st});
            }
            lblMenipis.setText(String.valueOf(habis + menipis));
            lblMenipisKet.setText(menipis + " menipis • " + habis + " habis");
            tblStok.setModel(ms);
            Theme.statusColumn(tblStok, 3);
            Theme.widths(tblStok, 190, 80, 80, 90);

            Object[] h = Koneksi.one("SELECT COUNT(*), SUM(tgl_kedaluwarsa <= CURDATE() + INTERVAL ? DAY) FROM batch "
                    + "WHERE qty_sisa > 0 AND tgl_kedaluwarsa >= CURDATE() AND tgl_kedaluwarsa <= CURDATE() + INTERVAL ? DAY", batas[1], batas[0]);
            lblHampir.setText(Theme.angka(h[0]));
            lblHampirKet.setText("batch ≤ " + batas[0] + " hari • " + Theme.angka(h[1]) + " kritis ≤ " + batas[1] + " hari");

            Object[] k = Koneksi.one("SELECT COUNT(*), COUNT(DISTINCT barang_id) FROM batch WHERE qty_sisa > 0 AND tgl_kedaluwarsa < CURDATE()");
            lblKedaluwarsa.setText(Theme.angka(k[0]));
            lblKedaluwarsaKet.setText("batch dari " + Theme.angka(k[1]) + " barang, perlu dimusnahkan");

            DefaultTableModel me = Theme.model("No Batch", "Barang", "Sisa", "Kedaluwarsa", "Status");
            for (Object[] r : Koneksi.rows(
                    "SELECT bt.no_batch, b.nama, bt.qty_sisa, b.satuan, bt.tgl_kedaluwarsa, DATEDIFF(bt.tgl_kedaluwarsa, CURDATE()) "
                    + "FROM batch bt JOIN m_barang b ON b.id = bt.barang_id "
                    + "WHERE bt.qty_sisa > 0 AND bt.tgl_kedaluwarsa <= CURDATE() + INTERVAL ? DAY "
                    + "ORDER BY bt.tgl_kedaluwarsa LIMIT 20", batas[0])) {
                long sisa = ((Number) r[5]).longValue();
                me.addRow(new Object[]{r[0], r[1], Theme.angka(r[2]) + " " + r[3], r[4], Persediaan.statusKedaluwarsa(sisa)});
            }
            tblExp.setModel(me);
            Theme.statusColumn(tblExp, 4);
            Theme.widths(tblExp, 75, 150, 55, 85, 110);

            DefaultTableModel ma = Theme.model("Waktu", "Jenis", "No Batch", "Barang", "Qty", "Referensi", "Oleh");
            for (Object[] r : Koneksi.rows(
                    "SELECT m.created_at, m.jenis, bt.no_batch, b.nama, m.qty, b.satuan, m.no_referensi, u.nama_lengkap "
                    + "FROM mutasi_stok m JOIN batch bt ON bt.id = m.batch_id JOIN m_barang b ON b.id = bt.barang_id "
                    + "JOIN m_user u ON u.id = m.created_by ORDER BY m.created_at DESC, m.id DESC LIMIT 15")) {
                ma.addRow(new Object[]{r[0], Laporan.jenisMutasi((String) r[1]), r[2], r[3], Theme.angka(r[4]) + " " + r[5], r[6], r[7]});
            }
            tblAktivitas.setModel(ma);
            Theme.statusColumn(tblAktivitas, 1);
            Theme.widths(tblAktivitas, 140, 120, 120, 200, 90, 130, 120);
        } catch (Exception e) {
            javax.swing.JOptionPane.showMessageDialog(this, "Gagal memuat dashboard: " + e.getMessage());
        }
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
        pnlIsi = new javax.swing.JPanel();
        pnlKartu = new javax.swing.JPanel();
        cardTotalBarang = new sistempenjualan.RoundedPanel();
        lblTotalBarangJudul = new javax.swing.JLabel();
        lblTotalBarang = new javax.swing.JLabel();
        lblTotalBarangKet = new javax.swing.JLabel();
        cardMenipis = new sistempenjualan.RoundedPanel();
        lblMenipisJudul = new javax.swing.JLabel();
        lblMenipis = new javax.swing.JLabel();
        lblMenipisKet = new javax.swing.JLabel();
        cardHampir = new sistempenjualan.RoundedPanel();
        lblHampirJudul = new javax.swing.JLabel();
        lblHampir = new javax.swing.JLabel();
        lblHampirKet = new javax.swing.JLabel();
        cardKedaluwarsa = new sistempenjualan.RoundedPanel();
        lblKedaluwarsaJudul = new javax.swing.JLabel();
        lblKedaluwarsa = new javax.swing.JLabel();
        lblKedaluwarsaKet = new javax.swing.JLabel();
        pnlTabel = new javax.swing.JPanel();
        cardExp = new sistempenjualan.RoundedPanel();
        pnlExpAtas = new javax.swing.JPanel();
        lblExpJudul = new javax.swing.JLabel();
        btnLihatExp = new javax.swing.JButton();
        spExp = new javax.swing.JScrollPane();
        tblExp = new javax.swing.JTable();
        cardStok = new sistempenjualan.RoundedPanel();
        pnlStokAtas = new javax.swing.JPanel();
        lblStokJudul = new javax.swing.JLabel();
        btnLihatStok = new javax.swing.JButton();
        spStok = new javax.swing.JScrollPane();
        tblStok = new javax.swing.JTable();
        cardAktivitas = new sistempenjualan.RoundedPanel();
        lblAktivitasJudul = new javax.swing.JLabel();
        spAktivitas = new javax.swing.JScrollPane();
        tblAktivitas = new javax.swing.JTable();

        setLayout(new java.awt.BorderLayout(0, 20));

        pnlHeader.setLayout(new java.awt.BorderLayout(16, 0));

        pnlJudul.setLayout(new javax.swing.BoxLayout(pnlJudul, javax.swing.BoxLayout.Y_AXIS));

        lblJudul.setText("Dashboard");
        pnlJudul.add(lblJudul);

        lblSubjudul.setText("Ringkasan persediaan dan kedaluwarsa barang hari ini");
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

        pnlKartu.setLayout(new java.awt.GridLayout(1, 4, 16, 0));

        cardTotalBarang.addMouseListener(new java.awt.event.MouseAdapter() {
                    public void mouseClicked(java.awt.event.MouseEvent evt) {
                        cardTotalBarangMouseClicked(evt);
                    }
                });
        cardTotalBarang.setLayout(new javax.swing.BoxLayout(cardTotalBarang, javax.swing.BoxLayout.Y_AXIS));

        lblTotalBarangJudul.setText("TOTAL BARANG");
        cardTotalBarang.add(lblTotalBarangJudul);

        lblTotalBarang.setText("0");
        cardTotalBarang.add(lblTotalBarang);

        lblTotalBarangKet.setText("-");
        cardTotalBarang.add(lblTotalBarangKet);
        pnlKartu.add(cardTotalBarang);

        cardMenipis.addMouseListener(new java.awt.event.MouseAdapter() {
                    public void mouseClicked(java.awt.event.MouseEvent evt) {
                        cardMenipisMouseClicked(evt);
                    }
                });
        cardMenipis.setLayout(new javax.swing.BoxLayout(cardMenipis, javax.swing.BoxLayout.Y_AXIS));

        lblMenipisJudul.setText("STOK MENIPIS / HABIS");
        cardMenipis.add(lblMenipisJudul);

        lblMenipis.setText("0");
        cardMenipis.add(lblMenipis);

        lblMenipisKet.setText("-");
        cardMenipis.add(lblMenipisKet);
        pnlKartu.add(cardMenipis);

        cardHampir.addMouseListener(new java.awt.event.MouseAdapter() {
                    public void mouseClicked(java.awt.event.MouseEvent evt) {
                        cardHampirMouseClicked(evt);
                    }
                });
        cardHampir.setLayout(new javax.swing.BoxLayout(cardHampir, javax.swing.BoxLayout.Y_AXIS));

        lblHampirJudul.setText("HAMPIR KEDALUWARSA");
        cardHampir.add(lblHampirJudul);

        lblHampir.setText("0");
        cardHampir.add(lblHampir);

        lblHampirKet.setText("-");
        cardHampir.add(lblHampirKet);
        pnlKartu.add(cardHampir);

        cardKedaluwarsa.addMouseListener(new java.awt.event.MouseAdapter() {
                    public void mouseClicked(java.awt.event.MouseEvent evt) {
                        cardKedaluwarsaMouseClicked(evt);
                    }
                });
        cardKedaluwarsa.setLayout(new javax.swing.BoxLayout(cardKedaluwarsa, javax.swing.BoxLayout.Y_AXIS));

        lblKedaluwarsaJudul.setText("SUDAH KEDALUWARSA");
        cardKedaluwarsa.add(lblKedaluwarsaJudul);

        lblKedaluwarsa.setText("0");
        cardKedaluwarsa.add(lblKedaluwarsa);

        lblKedaluwarsaKet.setText("-");
        cardKedaluwarsa.add(lblKedaluwarsaKet);
        pnlKartu.add(cardKedaluwarsa);
        pnlIsi.add(pnlKartu, java.awt.BorderLayout.NORTH);

        pnlTabel.setLayout(new java.awt.GridLayout(1, 2, 20, 0));

        cardExp.setLayout(new java.awt.BorderLayout(0, 12));

        pnlExpAtas.setLayout(new java.awt.BorderLayout(12, 0));

        lblExpJudul.setText("Segera Kedaluwarsa");
        pnlExpAtas.add(lblExpJudul, java.awt.BorderLayout.WEST);

        btnLihatExp.setText("Lihat semua");
        btnLihatExp.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        btnLihatExpActionPerformed(evt);
                    }
                });
        pnlExpAtas.add(btnLihatExp, java.awt.BorderLayout.EAST);
        cardExp.add(pnlExpAtas, java.awt.BorderLayout.NORTH);

        spExp.setViewportView(tblExp);
        cardExp.add(spExp, java.awt.BorderLayout.CENTER);
        pnlTabel.add(cardExp);

        cardStok.setLayout(new java.awt.BorderLayout(0, 12));

        pnlStokAtas.setLayout(new java.awt.BorderLayout(12, 0));

        lblStokJudul.setText("Stok Perlu Diisi Ulang");
        pnlStokAtas.add(lblStokJudul, java.awt.BorderLayout.WEST);

        btnLihatStok.setText("Lihat semua");
        btnLihatStok.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        btnLihatStokActionPerformed(evt);
                    }
                });
        pnlStokAtas.add(btnLihatStok, java.awt.BorderLayout.EAST);
        cardStok.add(pnlStokAtas, java.awt.BorderLayout.NORTH);

        spStok.setViewportView(tblStok);
        cardStok.add(spStok, java.awt.BorderLayout.CENTER);
        pnlTabel.add(cardStok);
        pnlIsi.add(pnlTabel, java.awt.BorderLayout.CENTER);

        cardAktivitas.setPreferredSize(new java.awt.Dimension(100, 220));
        cardAktivitas.setLayout(new java.awt.BorderLayout(0, 10));

        lblAktivitasJudul.setText("Aktivitas Stok Terakhir");
        cardAktivitas.add(lblAktivitasJudul, java.awt.BorderLayout.NORTH);

        spAktivitas.setViewportView(tblAktivitas);
        cardAktivitas.add(spAktivitas, java.awt.BorderLayout.CENTER);
        pnlIsi.add(cardAktivitas, java.awt.BorderLayout.SOUTH);
        add(pnlIsi, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    private void btnRefreshActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRefreshActionPerformed
        muatData();
    }//GEN-LAST:event_btnRefreshActionPerformed

    private void cardTotalBarangMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_cardTotalBarangMouseClicked
        app.buka(Bootstrap.DATA_BARANG);
    }//GEN-LAST:event_cardTotalBarangMouseClicked

    private void cardMenipisMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_cardMenipisMouseClicked
        app.buka(Bootstrap.MONITOR_STOK);
    }//GEN-LAST:event_cardMenipisMouseClicked

    private void cardHampirMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_cardHampirMouseClicked
        app.buka(Bootstrap.MONITOR_EXP);
    }//GEN-LAST:event_cardHampirMouseClicked

    private void cardKedaluwarsaMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_cardKedaluwarsaMouseClicked
        app.buka(Bootstrap.MONITOR_EXP);
    }//GEN-LAST:event_cardKedaluwarsaMouseClicked

    private void btnLihatExpActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLihatExpActionPerformed
        app.buka(Bootstrap.MONITOR_EXP);
    }//GEN-LAST:event_btnLihatExpActionPerformed

    private void btnLihatStokActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLihatStokActionPerformed
        app.buka(Bootstrap.MONITOR_STOK);
    }//GEN-LAST:event_btnLihatStokActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnLihatExp;
    private javax.swing.JButton btnLihatStok;
    private javax.swing.JButton btnRefresh;
    private javax.swing.JPanel cardAktivitas;
    private javax.swing.JPanel cardExp;
    private javax.swing.JPanel cardHampir;
    private javax.swing.JPanel cardKedaluwarsa;
    private javax.swing.JPanel cardMenipis;
    private javax.swing.JPanel cardStok;
    private javax.swing.JPanel cardTotalBarang;
    private javax.swing.JLabel lblAktivitasJudul;
    private javax.swing.JLabel lblExpJudul;
    private javax.swing.JLabel lblHampir;
    private javax.swing.JLabel lblHampirJudul;
    private javax.swing.JLabel lblHampirKet;
    private javax.swing.JLabel lblJudul;
    private javax.swing.JLabel lblKedaluwarsa;
    private javax.swing.JLabel lblKedaluwarsaJudul;
    private javax.swing.JLabel lblKedaluwarsaKet;
    private javax.swing.JLabel lblMenipis;
    private javax.swing.JLabel lblMenipisJudul;
    private javax.swing.JLabel lblMenipisKet;
    private javax.swing.JLabel lblStokJudul;
    private javax.swing.JLabel lblSubjudul;
    private javax.swing.JLabel lblTotalBarang;
    private javax.swing.JLabel lblTotalBarangJudul;
    private javax.swing.JLabel lblTotalBarangKet;
    private javax.swing.JPanel pnlAksi;
    private javax.swing.JPanel pnlExpAtas;
    private javax.swing.JPanel pnlHeader;
    private javax.swing.JPanel pnlIsi;
    private javax.swing.JPanel pnlJudul;
    private javax.swing.JPanel pnlKartu;
    private javax.swing.JPanel pnlStokAtas;
    private javax.swing.JPanel pnlTabel;
    private javax.swing.JScrollPane spAktivitas;
    private javax.swing.JScrollPane spExp;
    private javax.swing.JScrollPane spStok;
    private javax.swing.JTable tblAktivitas;
    private javax.swing.JTable tblExp;
    private javax.swing.JTable tblStok;
    // End of variables declaration//GEN-END:variables
}
