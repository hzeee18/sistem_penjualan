package sistempenjualan;

import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

/**
 * Lacak batch (traceability ringan).
 * <ul>
 * <li>Mundur: batch hasil kemas ulang -> batch bahan baku yang dipakai -> supplier.</li>
 * <li>Maju: batch bahan baku -> batch produk hasil kemas ulang -> customer;
 * batch produk -> customer.</li>
 * </ul>
 * Sumber data: tabel kemas_ulang(_detail) dan barang_keluar(_detail), yang
 * otomatis tercatat saat transaksi FEFO.
 */
public class LacakBatch extends javax.swing.JPanel implements Halaman {

    private final Bootstrap app;
    private boolean memuat;
    private int batchAktif;
    /** id batch tiap baris tabel asal / tujuan (0 = bukan batch), untuk klik dua kali. */
    private final List<Integer> idAsal = new ArrayList<>();
    private final List<Integer> idTujuan = new ArrayList<>();

    public LacakBatch(Bootstrap app) {
        this.app = app;
        initComponents();
        gaya();
    }

    private void gaya() {
        Theme.page(this);
        Theme.transparent(pnlHeader, pnlJudul, pnlAksi, pnlIsi, pnlKartu, pnlTabel, pnlAsalAtas, pnlTujuanAtas, pnlMutasiAtas);
        Theme.title(lblJudul);
        Theme.subtitle(lblSubjudul);
        Theme.secondary(btnRefresh);
        btnRefresh.setIcon(new Ikon("refresh", 16, Theme.TEXT));
        Theme.statCard(cardBarang, lblBarangJudul, lblBarang, lblBarangKet, Theme.PRIMARY);
        Theme.statCard(cardSumber, lblSumberJudul, lblSumber, lblSumberKet, Theme.ACCENT);
        Theme.statCard(cardExp, lblExpJudul, lblExp, lblExpKet, Theme.CAUTION);
        Theme.statCard(cardSisa, lblSisaJudul, lblSisa, lblSisaKet, Theme.INFO);
        for (javax.swing.JLabel l : new javax.swing.JLabel[]{lblBarang, lblSumber, lblExp, lblSisa}) {
            l.setFont(Theme.font(java.awt.Font.BOLD, 19f));
        }
        for (javax.swing.JPanel c : new javax.swing.JPanel[]{cardBarang, cardSumber, cardExp, cardSisa}) {
            c.setCursor(java.awt.Cursor.getDefaultCursor());
        }
        Theme.card(cardAsal);
        Theme.card(cardTujuan);
        Theme.card(cardMutasi);
        cardMutasi.setPreferredSize(new java.awt.Dimension(100, 210));
        Theme.section(lblAsalJudul);
        Theme.section(lblTujuanJudul);
        Theme.section(lblMutasiJudul);
        Theme.table(tblAsal);
        Theme.table(tblTujuan);
        Theme.table(tblMutasi);
        cbBatch.putClientProperty(com.formdev.flatlaf.FlatClientProperties.PLACEHOLDER_TEXT, "Pilih no. batch");
        cbBatch.setMaximumRowCount(16);
        kosongkan();
    }

    @Override
    public void muatData() {
        int pilih = batchAktif;
        memuat = true;
        try {
            Pilihan.isi(cbBatch, "— Pilih no. batch —",
                    "SELECT bt.id, CONCAT(bt.no_batch, '  •  ', b.nama) FROM batch bt JOIN m_barang b ON b.id = bt.barang_id "
                    + "ORDER BY bt.tgl_masuk DESC, bt.id DESC");
            Pilihan.pilih(cbBatch, pilih);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal memuat batch: " + e.getMessage());
        } finally {
            memuat = false;
        }
        if (pilih > 0) {
            lacak(pilih);
        }
    }

    /** Dipanggil dari halaman lain (mis. Monitoring Kedaluwarsa). */
    public void lacak(int batchId) {
        batchAktif = batchId;
        memuat = true;
        Pilihan.pilih(cbBatch, batchId);
        memuat = false;
        tampilkan(batchId);
    }

    private void pilihBatch() {
        if (memuat) {
            return;
        }
        Pilihan p = Pilihan.dari(cbBatch);
        batchAktif = p == null ? 0 : p.id;
        if (p == null) {
            kosongkan();
        } else {
            tampilkan(p.id);
        }
    }

    private void kosongkan() {
        lblBarang.setText("-");
        lblBarangKet.setText("pilih batch di kanan atas");
        lblSumber.setText("-");
        lblSumberKet.setText("-");
        lblExp.setText("-");
        lblExpKet.setText("-");
        lblSisa.setText("-");
        lblSisaKet.setText("-");
        tblAsal.setModel(Theme.model("No Batch", "Barang", "Supplier", "Qty"));
        tblTujuan.setModel(Theme.model("Tanggal", "Jenis", "Tujuan", "Qty"));
        tblMutasi.setModel(Theme.model("Waktu", "Jenis", "Qty", "Referensi", "Keterangan", "Oleh"));
        idAsal.clear();
        idTujuan.clear();
    }

    private void tampilkan(int id) {
        kosongkan();
        try {
            Object[] b = Koneksi.one(
                    "SELECT bt.no_batch, b.kode, b.nama, b.satuan, bt.sumber, s.nama AS nama_supplier, bt.tgl_masuk, "
                    + "bt.tgl_kedaluwarsa, bt.qty_awal, bt.qty_sisa, DATEDIFF(bt.tgl_kedaluwarsa, CURDATE()), b.kategori "
                    + "FROM batch bt JOIN m_barang b ON b.id = bt.barang_id LEFT JOIN m_supplier s ON s.id = bt.supplier_id "
                    + "WHERE bt.id = ?", id);
            if (b == null) {
                return;
            }
            String satuan = (String) b[3];
            boolean kemas = "KEMAS_ULANG".equals(b[4]);
            long sisaHari = ((Number) b[10]).longValue();
            double sisa = ((Number) b[9]).doubleValue();

            lblBarang.setText(String.valueOf(b[2]));
            lblBarangKet.setText(b[0] + "  •  " + b[1] + "  •  " + Laporan.kategori((String) b[11]));
            lblSumber.setText(kemas ? "Kemas Ulang" : "Pembelian");
            lblSumberKet.setText((kemas ? "diproduksi " : "dari " + b[5] + ", masuk ") + Theme.tgl(b[6]));
            lblExp.setText(Theme.tgl(b[7]));
            lblExpKet.setText(sisa <= 0 ? "stok batch sudah habis"
                    : sisaHari < 0 ? "lewat " + (-sisaHari) + " hari  •  " + Persediaan.statusKedaluwarsa(sisaHari)
                    : sisaHari + " hari lagi  •  " + Persediaan.statusKedaluwarsa(sisaHari));
            lblExp.setForeground(Theme.statusColor(sisa <= 0 ? "-" : Persediaan.statusKedaluwarsa(sisaHari))[0]);
            lblSisa.setText(Theme.angka(sisa) + " " + satuan);
            lblSisaKet.setText("dari " + Theme.angka(b[8]) + " " + satuan + " awal");

            // ---------- mundur: asal bahan
            DefaultTableModel ma = Theme.model("No Batch", "Barang", "Supplier", "Qty");
            if (kemas) {
                List<Object[]> rows = Koneksi.rows(
                        "SELECT bb.id, bb.no_batch, b.nama AS nama_barang, s.nama AS nama_supplier, bb.tgl_kedaluwarsa, "
                        + "d.qty_pakai, b.satuan, k.no_dokumen "
                        + "FROM kemas_ulang k JOIN kemas_ulang_detail d ON d.kemas_ulang_id = k.id "
                        + "JOIN batch bb ON bb.id = d.batch_id JOIN m_barang b ON b.id = bb.barang_id "
                        + "LEFT JOIN m_supplier s ON s.id = bb.supplier_id WHERE k.batch_hasil_id = ? ORDER BY bb.tgl_kedaluwarsa", id);
                for (Object[] r : rows) {
                    idAsal.add(((Number) r[0]).intValue());
                    ma.addRow(new Object[]{r[1], r[2], r[3] == null ? "-" : r[3], Theme.angka(r[5]) + " " + r[6]});
                }
                lblAsalJudul.setText("Asal Bahan" + (rows.isEmpty() ? "" : "  •  " + rows.get(0)[7]) + "  (lacak mundur)");
            } else {
                idAsal.add(0);
                ma.addRow(new Object[]{b[0], b[2], b[5] == null ? "-" : b[5], Theme.angka(b[8]) + " " + satuan});
                lblAsalJudul.setText("Asal  •  Pembelian dari supplier");
            }
            tblAsal.setModel(ma);
            Theme.widths(tblAsal, 90, 150, 165, 80);
            tblAsal.setToolTipText("Klik dua kali batch bahan untuk melacak batch itu");

            // ---------- maju: dipakai / dikirim
            DefaultTableModel mt = Theme.model("Tanggal", "Jenis", "Tujuan", "Qty");
            for (Object[] r : Koneksi.rows(
                    "SELECT bk.tanggal, bk.no_dokumen, c.nama AS nama_customer, d.qty FROM barang_keluar_detail d "
                    + "JOIN barang_keluar bk ON bk.id = d.barang_keluar_id JOIN m_customer c ON c.id = bk.customer_id "
                    + "WHERE d.batch_id = ? ORDER BY bk.tanggal, bk.id", id)) {
                idTujuan.add(0);
                mt.addRow(new Object[]{pendek(r[0]), "Dikirim", r[2] + "  •  " + r[1], Theme.angka(r[3]) + " " + satuan});
            }
            for (Object[] k : Koneksi.rows(
                    "SELECT k.created_at, k.no_dokumen, p.nama AS nama_produk, hb.no_batch, d.qty_pakai, hb.id, p.satuan "
                    + "FROM kemas_ulang_detail d JOIN kemas_ulang k ON k.id = d.kemas_ulang_id "
                    + "JOIN batch hb ON hb.id = k.batch_hasil_id JOIN m_barang p ON p.id = k.produk_id "
                    + "WHERE d.batch_id = ? ORDER BY k.created_at, k.id", id)) {
                idTujuan.add(((Number) k[5]).intValue());
                mt.addRow(new Object[]{pendek(k[0]), "Pakai Kemas", k[3] + "  •  " + k[2], Theme.angka(k[4]) + " " + satuan});
                for (Object[] r : Koneksi.rows(
                        "SELECT bk.tanggal, bk.no_dokumen, c.nama AS nama_customer, d.qty FROM barang_keluar_detail d "
                        + "JOIN barang_keluar bk ON bk.id = d.barang_keluar_id JOIN m_customer c ON c.id = bk.customer_id "
                        + "WHERE d.batch_id = ? ORDER BY bk.tanggal, bk.id", k[5])) {
                    idTujuan.add(((Number) k[5]).intValue());
                    mt.addRow(new Object[]{pendek(r[0]), "Keluar", "   ↳ " + r[2] + "  •  " + r[1], Theme.angka(r[3]) + " " + k[6]});
                }
            }
            tblTujuan.setModel(mt);
            Theme.statusColumn(tblTujuan, 1);
            Theme.widths(tblTujuan, 92, 112, 205, 76);
            tblTujuan.setToolTipText("Klik dua kali baris kemas ulang untuk melacak batch hasilnya");
            lblTujuanJudul.setText("Dipakai / Dikirim ke  (" + mt.getRowCount() + ")");

            // ---------- riwayat mutasi
            DefaultTableModel mm = Theme.model("Waktu", "Jenis", "Qty", "Referensi", "Keterangan", "Oleh");
            for (Object[] r : Koneksi.rows(
                    "SELECT m.created_at, m.jenis, m.qty, m.no_referensi, m.keterangan, u.nama_lengkap FROM mutasi_stok m "
                    + "JOIN m_user u ON u.id = m.created_by WHERE m.batch_id = ? ORDER BY m.created_at, m.id", id)) {
                mm.addRow(new Object[]{r[0], Laporan.jenisMutasi((String) r[1]), Theme.angka(r[2]) + " " + satuan,
                    r[3] == null ? "-" : r[3], r[4] == null ? "-" : r[4], r[5]});
            }
            tblMutasi.setModel(mm);
            Theme.statusColumn(tblMutasi, 1);
            Theme.widths(tblMutasi, 160, 120, 100, 120, 330, 140);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal melacak batch: " + e.getMessage());
        }
    }

    private static String pendek(Object d) {
        return d == null ? "-" : new java.text.SimpleDateFormat("dd/MM/yy").format((java.util.Date) d);
    }

    private void klikAsal(MouseEvent evt) {
        loncat(evt, tblAsal, idAsal);
    }

    private void klikTujuan(MouseEvent evt) {
        loncat(evt, tblTujuan, idTujuan);
    }

    /** Klik dua kali baris yang punya batch lain -> lacak batch itu. */
    private void loncat(MouseEvent evt, JTable t, List<Integer> ids) {
        if (evt.getClickCount() < 2 || t.getSelectedRow() < 0) {
            return;
        }
        int r = t.convertRowIndexToModel(t.getSelectedRow());
        if (r < ids.size() && ids.get(r) > 0 && ids.get(r) != batchAktif) {
            lacak(ids.get(r));
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
        cbBatch = new javax.swing.JComboBox<>();
        btnRefresh = new javax.swing.JButton();
        pnlIsi = new javax.swing.JPanel();
        pnlKartu = new javax.swing.JPanel();
        cardBarang = new sistempenjualan.RoundedPanel();
        lblBarangJudul = new javax.swing.JLabel();
        lblBarang = new javax.swing.JLabel();
        lblBarangKet = new javax.swing.JLabel();
        cardSumber = new sistempenjualan.RoundedPanel();
        lblSumberJudul = new javax.swing.JLabel();
        lblSumber = new javax.swing.JLabel();
        lblSumberKet = new javax.swing.JLabel();
        cardExp = new sistempenjualan.RoundedPanel();
        lblExpJudul = new javax.swing.JLabel();
        lblExp = new javax.swing.JLabel();
        lblExpKet = new javax.swing.JLabel();
        cardSisa = new sistempenjualan.RoundedPanel();
        lblSisaJudul = new javax.swing.JLabel();
        lblSisa = new javax.swing.JLabel();
        lblSisaKet = new javax.swing.JLabel();
        pnlTabel = new javax.swing.JPanel();
        cardAsal = new sistempenjualan.RoundedPanel();
        pnlAsalAtas = new javax.swing.JPanel();
        lblAsalJudul = new javax.swing.JLabel();
        spAsal = new javax.swing.JScrollPane();
        tblAsal = new javax.swing.JTable();
        cardTujuan = new sistempenjualan.RoundedPanel();
        pnlTujuanAtas = new javax.swing.JPanel();
        lblTujuanJudul = new javax.swing.JLabel();
        spTujuan = new javax.swing.JScrollPane();
        tblTujuan = new javax.swing.JTable();
        cardMutasi = new sistempenjualan.RoundedPanel();
        pnlMutasiAtas = new javax.swing.JPanel();
        lblMutasiJudul = new javax.swing.JLabel();
        spMutasi = new javax.swing.JScrollPane();
        tblMutasi = new javax.swing.JTable();

        setLayout(new java.awt.BorderLayout(0, 20));

        pnlHeader.setLayout(new java.awt.BorderLayout(16, 0));

        pnlJudul.setLayout(new javax.swing.BoxLayout(pnlJudul, javax.swing.BoxLayout.Y_AXIS));

        lblJudul.setText("Lacak Batch");
        pnlJudul.add(lblJudul);

        lblSubjudul.setText("Telusuri asal bahan dan ke mana saja sebuah batch dipakai / dikirim");
        pnlJudul.add(lblSubjudul);
        pnlHeader.add(pnlJudul, java.awt.BorderLayout.CENTER);

        pnlAksi.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 8, 0));

        cbBatch.setPreferredSize(new java.awt.Dimension(360, 36));
        cbBatch.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        cbBatchActionPerformed(evt);
                    }
                });
        pnlAksi.add(cbBatch);

        btnRefresh.setText("Refresh");
        btnRefresh.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        btnRefreshActionPerformed(evt);
                    }
                });
        pnlAksi.add(btnRefresh);
        pnlHeader.add(pnlAksi, java.awt.BorderLayout.EAST);
        add(pnlHeader, java.awt.BorderLayout.NORTH);

        pnlIsi.setLayout(new java.awt.BorderLayout(0, 16));

        pnlKartu.setLayout(new java.awt.GridLayout(1, 4, 16, 0));

        cardBarang.setLayout(new javax.swing.BoxLayout(cardBarang, javax.swing.BoxLayout.Y_AXIS));

        lblBarangJudul.setText("BARANG");
        cardBarang.add(lblBarangJudul);

        lblBarang.setText("0");
        cardBarang.add(lblBarang);

        lblBarangKet.setText("-");
        cardBarang.add(lblBarangKet);
        pnlKartu.add(cardBarang);

        cardSumber.setLayout(new javax.swing.BoxLayout(cardSumber, javax.swing.BoxLayout.Y_AXIS));

        lblSumberJudul.setText("ASAL BATCH");
        cardSumber.add(lblSumberJudul);

        lblSumber.setText("0");
        cardSumber.add(lblSumber);

        lblSumberKet.setText("-");
        cardSumber.add(lblSumberKet);
        pnlKartu.add(cardSumber);

        cardExp.setLayout(new javax.swing.BoxLayout(cardExp, javax.swing.BoxLayout.Y_AXIS));

        lblExpJudul.setText("KEDALUWARSA");
        cardExp.add(lblExpJudul);

        lblExp.setText("0");
        cardExp.add(lblExp);

        lblExpKet.setText("-");
        cardExp.add(lblExpKet);
        pnlKartu.add(cardExp);

        cardSisa.setLayout(new javax.swing.BoxLayout(cardSisa, javax.swing.BoxLayout.Y_AXIS));

        lblSisaJudul.setText("SISA STOK");
        cardSisa.add(lblSisaJudul);

        lblSisa.setText("0");
        cardSisa.add(lblSisa);

        lblSisaKet.setText("-");
        cardSisa.add(lblSisaKet);
        pnlKartu.add(cardSisa);
        pnlIsi.add(pnlKartu, java.awt.BorderLayout.NORTH);

        pnlTabel.setLayout(new java.awt.GridLayout(1, 2, 16, 0));

        cardAsal.setLayout(new java.awt.BorderLayout(0, 12));

        pnlAsalAtas.setLayout(new java.awt.BorderLayout(12, 0));

        lblAsalJudul.setText("Asal Bahan  (lacak mundur)");
        pnlAsalAtas.add(lblAsalJudul, java.awt.BorderLayout.WEST);
        cardAsal.add(pnlAsalAtas, java.awt.BorderLayout.NORTH);

        tblAsal.addMouseListener(new java.awt.event.MouseAdapter() {
                    public void mouseClicked(java.awt.event.MouseEvent evt) {
                        tblAsalMouseClicked(evt);
                    }
                });
        spAsal.setViewportView(tblAsal);
        cardAsal.add(spAsal, java.awt.BorderLayout.CENTER);
        pnlTabel.add(cardAsal);

        cardTujuan.setLayout(new java.awt.BorderLayout(0, 12));

        pnlTujuanAtas.setLayout(new java.awt.BorderLayout(12, 0));

        lblTujuanJudul.setText("Dipakai / Dikirim ke  (lacak maju)");
        pnlTujuanAtas.add(lblTujuanJudul, java.awt.BorderLayout.WEST);
        cardTujuan.add(pnlTujuanAtas, java.awt.BorderLayout.NORTH);

        tblTujuan.addMouseListener(new java.awt.event.MouseAdapter() {
                    public void mouseClicked(java.awt.event.MouseEvent evt) {
                        tblTujuanMouseClicked(evt);
                    }
                });
        spTujuan.setViewportView(tblTujuan);
        cardTujuan.add(spTujuan, java.awt.BorderLayout.CENTER);
        pnlTabel.add(cardTujuan);
        pnlIsi.add(pnlTabel, java.awt.BorderLayout.CENTER);

        cardMutasi.setLayout(new java.awt.BorderLayout(0, 12));

        pnlMutasiAtas.setLayout(new java.awt.BorderLayout(12, 0));

        lblMutasiJudul.setText("Riwayat Mutasi Batch");
        pnlMutasiAtas.add(lblMutasiJudul, java.awt.BorderLayout.WEST);
        cardMutasi.add(pnlMutasiAtas, java.awt.BorderLayout.NORTH);

        spMutasi.setViewportView(tblMutasi);
        cardMutasi.add(spMutasi, java.awt.BorderLayout.CENTER);
        pnlIsi.add(cardMutasi, java.awt.BorderLayout.SOUTH);
        add(pnlIsi, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    private void cbBatchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbBatchActionPerformed
        pilihBatch();
    }//GEN-LAST:event_cbBatchActionPerformed

    private void btnRefreshActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRefreshActionPerformed
        muatData();
    }//GEN-LAST:event_btnRefreshActionPerformed

    private void tblAsalMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tblAsalMouseClicked
        klikAsal(evt);
    }//GEN-LAST:event_tblAsalMouseClicked

    private void tblTujuanMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tblTujuanMouseClicked
        klikTujuan(evt);
    }//GEN-LAST:event_tblTujuanMouseClicked

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnRefresh;
    private javax.swing.JPanel cardAsal;
    private javax.swing.JPanel cardBarang;
    private javax.swing.JPanel cardExp;
    private javax.swing.JPanel cardMutasi;
    private javax.swing.JPanel cardSisa;
    private javax.swing.JPanel cardSumber;
    private javax.swing.JPanel cardTujuan;
    private javax.swing.JComboBox<Object> cbBatch;
    private javax.swing.JLabel lblAsalJudul;
    private javax.swing.JLabel lblBarang;
    private javax.swing.JLabel lblBarangJudul;
    private javax.swing.JLabel lblBarangKet;
    private javax.swing.JLabel lblExp;
    private javax.swing.JLabel lblExpJudul;
    private javax.swing.JLabel lblExpKet;
    private javax.swing.JLabel lblJudul;
    private javax.swing.JLabel lblMutasiJudul;
    private javax.swing.JLabel lblSisa;
    private javax.swing.JLabel lblSisaJudul;
    private javax.swing.JLabel lblSisaKet;
    private javax.swing.JLabel lblSubjudul;
    private javax.swing.JLabel lblSumber;
    private javax.swing.JLabel lblSumberJudul;
    private javax.swing.JLabel lblSumberKet;
    private javax.swing.JLabel lblTujuanJudul;
    private javax.swing.JPanel pnlAksi;
    private javax.swing.JPanel pnlAsalAtas;
    private javax.swing.JPanel pnlHeader;
    private javax.swing.JPanel pnlIsi;
    private javax.swing.JPanel pnlJudul;
    private javax.swing.JPanel pnlKartu;
    private javax.swing.JPanel pnlMutasiAtas;
    private javax.swing.JPanel pnlTabel;
    private javax.swing.JPanel pnlTujuanAtas;
    private javax.swing.JScrollPane spAsal;
    private javax.swing.JScrollPane spMutasi;
    private javax.swing.JScrollPane spTujuan;
    private javax.swing.JTable tblAsal;
    private javax.swing.JTable tblMutasi;
    private javax.swing.JTable tblTujuan;
    // End of variables declaration//GEN-END:variables
}
