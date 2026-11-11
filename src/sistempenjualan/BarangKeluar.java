package sistempenjualan;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

/**
 * Barang keluar ke customer. Setiap item dialokasikan ke batch dengan FEFO
 * dan dicetak sebagai Bukti Barang Keluar (Surat Jalan).
 */
public class BarangKeluar extends javax.swing.JPanel implements Halaman {

    private final Bootstrap app;
    /** barangId -> qty diminta (urutan input dipertahankan). */
    private final Map<Integer, Double> daftar = new LinkedHashMap<>();
    private final Map<Integer, Pilihan> info = new LinkedHashMap<>();
    private final List<Long> idRiwayat = new ArrayList<>();

    public BarangKeluar(Bootstrap app) {
        this.app = app;
        initComponents();
        gaya();
    }

    private void gaya() {
        Theme.page(this);
        Theme.transparent(pnlHeader, pnlJudul, pnlAksi, pnlIsi, pnlInputAtas, pnlForm, pnlBawah, pnlSimpan, pnlRiwayatAtas);
        Theme.title(lblJudul);
        Theme.subtitle(lblSubjudul);
        Theme.secondary(btnRefresh, btnHapusItem, btnKosongkan, btnCetakUlang);
        Theme.primary(btnTambah, btnSimpan);
        btnRefresh.setIcon(new Ikon("refresh", 16, Theme.TEXT));
        btnSimpan.setIcon(new Ikon("cetak", 16, java.awt.Color.WHITE));
        btnCetakUlang.setIcon(new Ikon("cetak", 16, Theme.TEXT));
        Theme.card(cardInput, 22);
        Theme.card(cardRiwayat);
        Theme.section(lblInputJudul);
        Theme.section(lblRiwayatJudul);
        for (javax.swing.JLabel l : new javax.swing.JLabel[]{lblCustomer, lblTanggal, lblCatatan, lblProduk, lblQty}) {
            Theme.label(l);
        }
        Theme.muted(lblStokInfo);
        lblTotal.setFont(Theme.font(java.awt.Font.BOLD, 13f));
        lblTotal.setForeground(Theme.TEXT);
        Theme.dateChooser(dcTanggal);
        Theme.placeholder(txtQty, "0");
        Theme.placeholder(txtCatatan, "mis. no. PO customer");
        Theme.search(txtCari, "Cari dokumen / customer");
        Theme.table(tblItem);
        Theme.table(tblRiwayat);
    }

    @Override
    public void muatData() {
        try {
            Pilihan.isi(cbCustomer, null, "SELECT id, nama FROM m_customer ORDER BY nama");
            Pilihan.isi(cbProduk, null, "SELECT id, CONCAT(kode, ' - ', nama), satuan, kode, nama FROM m_barang ORDER BY kategori DESC, kode");
            dcTanggal.setDate(new Date());
            produkDipilih();
            tampilkanDaftar();
            muatRiwayat();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal memuat data: " + e.getMessage());
        }
    }

    private void produkDipilih() {
        Pilihan p = Pilihan.dari(cbProduk);
        if (p == null) {
            lblStokInfo.setText(" ");
            return;
        }
        try {
            double layak = Persediaan.stokLayak(p.id);
            Double sudah = daftar.get(p.id);
            lblStokInfo.setText("<html>Stok layak: <b>" + Theme.angka(layak) + " " + p.data[0] + "</b>"
                    + (sudah != null ? " &nbsp;(sudah di daftar: " + Theme.angka(sudah) + ")" : "") + "</html>");
        } catch (Exception e) {
            lblStokInfo.setText(e.getMessage());
        }
    }

    private void tambah() {
        Pilihan p = Pilihan.dari(cbProduk);
        if (p == null) {
            return;
        }
        double qty;
        try {
            qty = Double.parseDouble(txtQty.getText().trim().replace(",", "."));
            if (qty <= 0) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Jumlah harus angka lebih dari 0.");
            return;
        }
        double total = qty + daftar.getOrDefault(p.id, 0.0);
        try {
            Persediaan.fefo(p.id, total); // validasi stok cukup
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, p.teks + "\n" + e.getMessage());
            return;
        }
        daftar.put(p.id, total);
        info.put(p.id, p);
        txtQty.setText("");
        tampilkanDaftar();
        produkDipilih();
    }

    private void tampilkanDaftar() {
        DefaultTableModel m = Theme.model("Kode", "Nama Barang", "Jumlah", "Satuan", "Batch Dipakai (FEFO)", "Kedaluwarsa");
        for (Map.Entry<Integer, Double> e : daftar.entrySet()) {
            Pilihan p = info.get(e.getKey());
            StringBuilder b = new StringBuilder();
            Date exp = null;
            try {
                for (Persediaan.Alokasi a : Persediaan.fefo(e.getKey(), e.getValue())) {
                    b.append(b.length() > 0 ? ", " : "").append(a.noBatch).append(" (").append(Theme.angka(a.qty)).append(")");
                    if (exp == null) {
                        exp = a.kedaluwarsa;
                    }
                }
            } catch (Exception ex) {
                b.append(ex.getMessage());
            }
            m.addRow(new Object[]{p.data[1], p.data[2], e.getValue(), p.data[0], b.toString(), exp});
        }
        tblItem.setModel(m);
        Theme.widths(tblItem, 80, 200, 70, 60, 260, 100);
        lblTotal.setText(daftar.size() + " barang di daftar");
    }

    private void hapusItem() {
        int v = tblItem.getSelectedRow();
        if (v < 0) {
            JOptionPane.showMessageDialog(this, "Pilih item di tabel yang mau dihapus.");
            return;
        }
        int idx = tblItem.convertRowIndexToModel(v);
        Integer key = new ArrayList<>(daftar.keySet()).get(idx);
        daftar.remove(key);
        info.remove(key);
        tampilkanDaftar();
        produkDipilih();
    }

    private void kosongkan() {
        daftar.clear();
        info.clear();
        txtCatatan.setText("");
        tampilkanDaftar();
        produkDipilih();
    }

    private void simpan() {
        Pilihan c = Pilihan.dari(cbCustomer);
        if (c == null || dcTanggal.getDate() == null) {
            JOptionPane.showMessageDialog(this, "Pilih customer dan tanggal.");
            return;
        }
        if (daftar.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Tambahkan minimal satu barang ke daftar.");
            return;
        }
        List<Persediaan.Item> items = new ArrayList<>();
        for (Map.Entry<Integer, Double> e : daftar.entrySet()) {
            items.add(new Persediaan.Item(e.getKey(), e.getValue()));
        }
        try {
            long id = Persediaan.barangKeluar(c.id, dcTanggal.getDate(), txtCatatan.getText().trim(), items, app.getSessionUserId());
            kosongkan();
            muatRiwayat();
            Notifikasi.periksaSekarang(false);
            cetak(id);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal menyimpan: " + e.getMessage());
        }
    }

    private void cetak(long id) {
        java.util.Map<String, Object> p = Laporan.param();
        p.put("ID", (int) id);
        Laporan.tampilkan("bukti_barang_keluar", p, "Bukti Barang Keluar");
    }

    private void cetakUlang() {
        int v = tblRiwayat.getSelectedRow();
        if (v < 0) {
            JOptionPane.showMessageDialog(this, "Pilih dokumen di tabel riwayat.");
            return;
        }
        cetak(idRiwayat.get(tblRiwayat.convertRowIndexToModel(v)));
    }

    private void muatRiwayat() {
        try {
            String cari = "%" + txtCari.getText().trim() + "%";
            DefaultTableModel m = Theme.model("No Dokumen", "Tanggal", "Customer", "Item");
            idRiwayat.clear();
            for (Object[] r : Koneksi.rows(
                    "SELECT k.id, k.no_dokumen, k.tanggal, c.nama, (SELECT COUNT(DISTINCT bt.barang_id) FROM barang_keluar_detail d "
                    + "JOIN batch bt ON bt.id = d.batch_id WHERE d.barang_keluar_id = k.id) "
                    + "FROM barang_keluar k JOIN m_customer c ON c.id = k.customer_id "
                    + "WHERE k.no_dokumen LIKE ? OR c.nama LIKE ? ORDER BY k.tanggal DESC, k.id DESC", cari, cari)) {
                idRiwayat.add(((Number) r[0]).longValue());
                m.addRow(new Object[]{r[1], r[2], r[3], r[4]});
            }
            tblRiwayat.setModel(m);
            Theme.widths(tblRiwayat, 120, 95, 150, 40);
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
        cardInput = new sistempenjualan.RoundedPanel();
        pnlInputAtas = new javax.swing.JPanel();
        lblInputJudul = new javax.swing.JLabel();
        pnlForm = new javax.swing.JPanel();
        lblCustomer = new javax.swing.JLabel();
        lblTanggal = new javax.swing.JLabel();
        lblCatatan = new javax.swing.JLabel();
        cbCustomer = new javax.swing.JComboBox<>();
        dcTanggal = new com.toedter.calendar.JDateChooser();
        txtCatatan = new javax.swing.JTextField();
        lblProduk = new javax.swing.JLabel();
        lblQty = new javax.swing.JLabel();
        lblStokInfo = new javax.swing.JLabel();
        cbProduk = new javax.swing.JComboBox<>();
        txtQty = new javax.swing.JTextField();
        btnTambah = new javax.swing.JButton();
        spItem = new javax.swing.JScrollPane();
        tblItem = new javax.swing.JTable();
        pnlBawah = new javax.swing.JPanel();
        lblTotal = new javax.swing.JLabel();
        pnlSimpan = new javax.swing.JPanel();
        btnHapusItem = new javax.swing.JButton();
        btnKosongkan = new javax.swing.JButton();
        btnSimpan = new javax.swing.JButton();
        cardRiwayat = new sistempenjualan.RoundedPanel();
        pnlRiwayatAtas = new javax.swing.JPanel();
        lblRiwayatJudul = new javax.swing.JLabel();
        txtCari = new javax.swing.JTextField();
        spRiwayat = new javax.swing.JScrollPane();
        tblRiwayat = new javax.swing.JTable();
        btnCetakUlang = new javax.swing.JButton();

        setLayout(new java.awt.BorderLayout(0, 20));

        pnlHeader.setLayout(new java.awt.BorderLayout(16, 0));

        pnlJudul.setLayout(new javax.swing.BoxLayout(pnlJudul, javax.swing.BoxLayout.Y_AXIS));

        lblJudul.setText("Barang Keluar");
        pnlJudul.add(lblJudul);

        lblSubjudul.setText("Pengiriman barang ke customer. Batch dipilih otomatis dengan metode FEFO (First Expired First Out)");
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

        pnlIsi.setLayout(new java.awt.BorderLayout(20, 0));

        cardInput.setLayout(new java.awt.BorderLayout(0, 14));

        pnlInputAtas.setLayout(new java.awt.BorderLayout(0, 16));

        lblInputJudul.setText("Input Barang Keluar");
        pnlInputAtas.add(lblInputJudul, java.awt.BorderLayout.NORTH);

        pnlForm.setLayout(new java.awt.GridBagLayout());

        lblCustomer.setText("Customer / tujuan");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 12);
        pnlForm.add(lblCustomer, gridBagConstraints);

        lblTanggal.setText("Tanggal");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 12);
        pnlForm.add(lblTanggal, gridBagConstraints);

        lblCatatan.setText("Catatan");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 0);
        pnlForm.add(lblCatatan, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.4;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 14, 12);
        pnlForm.add(cbCustomer, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 70;
        gridBagConstraints.weightx = 0.2;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 14, 12);
        pnlForm.add(dcTanggal, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.4;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 14, 0);
        pnlForm.add(txtCatatan, gridBagConstraints);

        lblProduk.setText("Barang");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 12);
        pnlForm.add(lblProduk, gridBagConstraints);

        lblQty.setText("Jumlah");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 12);
        pnlForm.add(lblQty, gridBagConstraints);

        lblStokInfo.setText(" ");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 0);
        pnlForm.add(lblStokInfo, gridBagConstraints);

        cbProduk.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        cbProdukActionPerformed(evt);
                    }
                });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 12);
        pnlForm.add(cbProduk, gridBagConstraints);

        txtQty.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        txtQtyActionPerformed(evt);
                    }
                });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 12);
        pnlForm.add(txtQty, gridBagConstraints);

        btnTambah.setText("+ Tambah ke Daftar");
        btnTambah.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        btnTambahActionPerformed(evt);
                    }
                });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        pnlForm.add(btnTambah, gridBagConstraints);
        pnlInputAtas.add(pnlForm, java.awt.BorderLayout.NORTH);
        cardInput.add(pnlInputAtas, java.awt.BorderLayout.NORTH);

        spItem.setViewportView(tblItem);
        cardInput.add(spItem, java.awt.BorderLayout.CENTER);

        pnlBawah.setLayout(new java.awt.BorderLayout(12, 0));

        lblTotal.setText("0 item");
        pnlBawah.add(lblTotal, java.awt.BorderLayout.CENTER);

        pnlSimpan.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 8, 0));

        btnHapusItem.setText("Hapus Item");
        btnHapusItem.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        btnHapusItemActionPerformed(evt);
                    }
                });
        pnlSimpan.add(btnHapusItem);

        btnKosongkan.setText("Kosongkan");
        btnKosongkan.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        btnKosongkanActionPerformed(evt);
                    }
                });
        pnlSimpan.add(btnKosongkan);

        btnSimpan.setText("Simpan & Cetak Bukti");
        btnSimpan.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        btnSimpanActionPerformed(evt);
                    }
                });
        pnlSimpan.add(btnSimpan);
        pnlBawah.add(pnlSimpan, java.awt.BorderLayout.EAST);
        cardInput.add(pnlBawah, java.awt.BorderLayout.SOUTH);
        pnlIsi.add(cardInput, java.awt.BorderLayout.CENTER);

        cardRiwayat.setPreferredSize(new java.awt.Dimension(380, 100));
        cardRiwayat.setLayout(new java.awt.BorderLayout(0, 12));

        pnlRiwayatAtas.setLayout(new java.awt.BorderLayout(0, 10));

        lblRiwayatJudul.setText("Riwayat Barang Keluar");
        pnlRiwayatAtas.add(lblRiwayatJudul, java.awt.BorderLayout.NORTH);

        txtCari.addKeyListener(new java.awt.event.KeyAdapter() {
                    public void keyReleased(java.awt.event.KeyEvent evt) {
                        txtCariKeyReleased(evt);
                    }
                });
        pnlRiwayatAtas.add(txtCari, java.awt.BorderLayout.CENTER);
        cardRiwayat.add(pnlRiwayatAtas, java.awt.BorderLayout.NORTH);

        spRiwayat.setViewportView(tblRiwayat);
        cardRiwayat.add(spRiwayat, java.awt.BorderLayout.CENTER);

        btnCetakUlang.setText("Cetak Ulang Bukti");
        btnCetakUlang.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        btnCetakUlangActionPerformed(evt);
                    }
                });
        cardRiwayat.add(btnCetakUlang, java.awt.BorderLayout.SOUTH);
        pnlIsi.add(cardRiwayat, java.awt.BorderLayout.EAST);
        add(pnlIsi, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    private void btnRefreshActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRefreshActionPerformed
        muatData();
    }//GEN-LAST:event_btnRefreshActionPerformed

    private void cbProdukActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbProdukActionPerformed
        produkDipilih();
    }//GEN-LAST:event_cbProdukActionPerformed

    private void txtQtyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtQtyActionPerformed
        tambah();
    }//GEN-LAST:event_txtQtyActionPerformed

    private void btnTambahActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnTambahActionPerformed
        tambah();
    }//GEN-LAST:event_btnTambahActionPerformed

    private void btnHapusItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnHapusItemActionPerformed
        hapusItem();
    }//GEN-LAST:event_btnHapusItemActionPerformed

    private void btnKosongkanActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnKosongkanActionPerformed
        kosongkan();
    }//GEN-LAST:event_btnKosongkanActionPerformed

    private void btnSimpanActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSimpanActionPerformed
        simpan();
    }//GEN-LAST:event_btnSimpanActionPerformed

    private void txtCariKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtCariKeyReleased
        muatRiwayat();
    }//GEN-LAST:event_txtCariKeyReleased

    private void btnCetakUlangActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCetakUlangActionPerformed
        cetakUlang();
    }//GEN-LAST:event_btnCetakUlangActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCetakUlang;
    private javax.swing.JButton btnHapusItem;
    private javax.swing.JButton btnKosongkan;
    private javax.swing.JButton btnRefresh;
    private javax.swing.JButton btnSimpan;
    private javax.swing.JButton btnTambah;
    private javax.swing.JPanel cardInput;
    private javax.swing.JPanel cardRiwayat;
    private javax.swing.JComboBox<Object> cbCustomer;
    private javax.swing.JComboBox<Object> cbProduk;
    private com.toedter.calendar.JDateChooser dcTanggal;
    private javax.swing.JLabel lblCatatan;
    private javax.swing.JLabel lblCustomer;
    private javax.swing.JLabel lblInputJudul;
    private javax.swing.JLabel lblJudul;
    private javax.swing.JLabel lblProduk;
    private javax.swing.JLabel lblQty;
    private javax.swing.JLabel lblRiwayatJudul;
    private javax.swing.JLabel lblStokInfo;
    private javax.swing.JLabel lblSubjudul;
    private javax.swing.JLabel lblTanggal;
    private javax.swing.JLabel lblTotal;
    private javax.swing.JPanel pnlAksi;
    private javax.swing.JPanel pnlBawah;
    private javax.swing.JPanel pnlForm;
    private javax.swing.JPanel pnlHeader;
    private javax.swing.JPanel pnlInputAtas;
    private javax.swing.JPanel pnlIsi;
    private javax.swing.JPanel pnlJudul;
    private javax.swing.JPanel pnlRiwayatAtas;
    private javax.swing.JPanel pnlSimpan;
    private javax.swing.JScrollPane spItem;
    private javax.swing.JScrollPane spRiwayat;
    private javax.swing.JTable tblItem;
    private javax.swing.JTable tblRiwayat;
    private javax.swing.JTextField txtCari;
    private javax.swing.JTextField txtCatatan;
    private javax.swing.JTextField txtQty;
    // End of variables declaration//GEN-END:variables
}
