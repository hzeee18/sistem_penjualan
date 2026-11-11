package sistempenjualan;

import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

/**
 * Master data barang: bahan baku dan produk jadi.
 */
public class DataBarang extends javax.swing.JPanel implements Halaman {

    private final Bootstrap app;
    private final List<Integer> ids = new ArrayList<>();
    private int idTerpilih = 0;

    public DataBarang(Bootstrap app) {
        this.app = app;
        initComponents();
        gaya();
    }

    private void gaya() {
        Theme.page(this);
        Theme.transparent(pnlHeader, pnlJudul, pnlAksi, pnlIsi, pnlTombol, pnlDataAtas, pnlFilter);
        Theme.title(lblJudul);
        Theme.subtitle(lblSubjudul);
        Theme.secondary(btnRefresh, btnBersihkan);
        Theme.danger(btnHapus);
        Theme.primary(btnSimpan);
        btnRefresh.setIcon(new Ikon("refresh", 16, Theme.TEXT));
        Theme.card(cardForm, 20);
        Theme.card(cardData);
        Theme.section(lblFormJudul);
        Theme.section(lblDataJudul);
        Theme.muted(lblInfo);
        Theme.table(tblData);
        Theme.search(txtCari, "Cari...");
        for (java.awt.Component c : cardForm.getComponents()) {
            if (c instanceof javax.swing.JLabel && c != lblFormJudul && c != lblInfo) {
                Theme.label((javax.swing.JLabel) c);
            }
        }
        Theme.placeholder(txtKode, "mis. BB-008");
        Theme.placeholder(txtIsi, "khusus produk jadi");
        cbKategori.setModel(new javax.swing.DefaultComboBoxModel<>(new String[]{"Bahan Baku", "Produk Jadi"}));
        cbSatuan.setModel(new javax.swing.DefaultComboBoxModel<>(new String[]{"kg", "gr", "pcs", "pack", "sachet"}));
        cbSatuan.setEditable(true);
    }

    private int idBaris() {
        int v = tblData.getSelectedRow();
        return v < 0 ? 0 : ids.get(tblData.convertRowIndexToModel(v));
    }

    private void modeUbah(boolean ubah) {
        btnSimpan.setText(ubah ? "Simpan Perubahan" : "Simpan");
        btnHapus.setEnabled(ubah);
        lblFormJudul.setText(ubah ? "Ubah Data" : "Tambah Data Baru");
    }

    private static double angka(javax.swing.JTextField f) throws Exception {
        try {
            return Double.parseDouble(f.getText().trim().replace(",", "."));
        } catch (NumberFormatException e) {
            throw new Exception("Isian angka tidak valid: " + f.getText());
        }
    }

    @Override
    public void muatData() {
        try {
            Pilihan.isi(cbSupplier, "- Tidak ada -", "SELECT id, nama FROM m_supplier ORDER BY nama");
            Pilihan.isi(cbBahan, "- Tidak ada -", "SELECT id, CONCAT(kode, ' - ', nama) FROM m_barang WHERE kategori = 'BAHAN_BAKU' ORDER BY kode");
            bersihkan();
            muatTabel();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal memuat data: " + e.getMessage());
        }
    }

    private void kategoriBerubah() {
        boolean produk = cbKategori.getSelectedIndex() == 1;
        cbBahan.setEnabled(produk);
        txtIsi.setEnabled(produk);
        cbSupplier.setEnabled(!produk);
        cbSatuan.setSelectedItem(produk ? "pcs" : "kg");
    }

    private void muatTabel() {
        try {
            String c = "%" + txtCari.getText().trim() + "%";
            DefaultTableModel m = Theme.model("Kode", "Nama Barang", "Kategori", "Satuan", "Isi (gr)", "Bahan Baku", "Supplier", "Stok Min", "Umur Simpan");
            ids.clear();
            for (Object[] r : Koneksi.rows(
                    "SELECT b.id, b.kode, b.nama, b.kategori, b.satuan, b.isi_gram, bb.nama, s.nama, b.stok_minimum, b.umur_simpan_hari "
                    + "FROM m_barang b LEFT JOIN m_barang bb ON bb.id = b.bahan_baku_id LEFT JOIN m_supplier s ON s.id = b.supplier_id "
                    + "WHERE b.kode LIKE ? OR b.nama LIKE ? ORDER BY b.kategori, b.kode", c, c)) {
                ids.add(((Number) r[0]).intValue());
                m.addRow(new Object[]{r[1], r[2], Laporan.kategori((String) r[3]), r[4], r[5] == null ? "-" : Theme.angka(r[5]),
                    r[6] == null ? "-" : r[6], r[7] == null ? "-" : r[7], r[8], r[9] + " hari"});
            }
            tblData.setModel(m);
            Theme.widths(tblData, 70, 190, 95, 60, 70, 160, 170, 70, 90);
            lblDataJudul.setText("Daftar Barang (" + m.getRowCount() + ")");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal memuat tabel: " + e.getMessage());
        }
    }

    private void pilihBaris() {
        int id = idBaris();
        if (id == 0) {
            return;
        }
        try {
            Object[] r = Koneksi.one("SELECT kode, nama, kategori, satuan, isi_gram, bahan_baku_id, supplier_id, stok_minimum, umur_simpan_hari FROM m_barang WHERE id = ?", id);
            idTerpilih = id;
            txtKode.setText((String) r[0]);
            txtNama.setText((String) r[1]);
            cbKategori.setSelectedIndex("PRODUK_JADI".equals(r[2]) ? 1 : 0);
            cbSatuan.setSelectedItem(r[3]);
            txtIsi.setText(r[4] == null ? "" : Theme.angka(r[4]).replace(".", ""));
            Pilihan.pilih(cbBahan, r[5] == null ? 0 : ((Number) r[5]).intValue());
            Pilihan.pilih(cbSupplier, r[6] == null ? 0 : ((Number) r[6]).intValue());
            txtMinimum.setText(Theme.angka(r[7]).replace(".", ""));
            txtUmur.setText(String.valueOf(r[8]));
            modeUbah(true);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage());
        }
    }

    private void bersihkan() {
        idTerpilih = 0;
        txtKode.setText("");
        txtNama.setText("");
        cbKategori.setSelectedIndex(0);
        kategoriBerubah();
        txtIsi.setText("");
        txtMinimum.setText("0");
        txtUmur.setText("365");
        Pilihan.pilih(cbBahan, 0);
        Pilihan.pilih(cbSupplier, 0);
        tblData.clearSelection();
        modeUbah(false);
    }

    private void simpan() {
        try {
            String kode = txtKode.getText().trim();
            String nama = txtNama.getText().trim();
            if (kode.isEmpty() || nama.isEmpty()) {
                throw new Exception("Kode dan nama barang wajib diisi.");
            }
            boolean produk = cbKategori.getSelectedIndex() == 1;
            Pilihan bahan = Pilihan.dari(cbBahan);
            Pilihan sup = Pilihan.dari(cbSupplier);
            Double isi = null;
            if (produk) {
                if (bahan == null) {
                    throw new Exception("Produk jadi wajib memilih bahan baku yang dikemas.");
                }
                isi = angka(txtIsi);
            }
            Object[] v = {kode, nama, produk ? "PRODUK_JADI" : "BAHAN_BAKU", String.valueOf(cbSatuan.getSelectedItem()), isi,
                produk ? bahan.id : null, (!produk && sup != null) ? sup.id : null, angka(txtMinimum), (int) angka(txtUmur)};
            if (idTerpilih == 0) {
                Koneksi.exec("INSERT INTO m_barang (kode, nama, kategori, satuan, isi_gram, bahan_baku_id, supplier_id, stok_minimum, umur_simpan_hari) "
                        + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)", v);
            } else {
                Object[] w = java.util.Arrays.copyOf(v, 10);
                w[9] = idTerpilih;
                Koneksi.exec("UPDATE m_barang SET kode=?, nama=?, kategori=?, satuan=?, isi_gram=?, bahan_baku_id=?, supplier_id=?, "
                        + "stok_minimum=?, umur_simpan_hari=? WHERE id=?", w);
            }
            JOptionPane.showMessageDialog(this, "Data barang tersimpan.");
            muatData();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal menyimpan: " + e.getMessage());
        }
    }

    private void hapus() {
        if (idTerpilih == 0) {
            return;
        }
        if (JOptionPane.showConfirmDialog(this, "Hapus barang " + txtNama.getText() + "?", "Hapus", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            Koneksi.exec("DELETE FROM m_barang WHERE id = ?", idTerpilih);
            muatData();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Barang tidak bisa dihapus karena sudah punya riwayat stok / dipakai produk lain.");
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
        lblKode = new javax.swing.JLabel();
        txtKode = new javax.swing.JTextField();
        lblNama = new javax.swing.JLabel();
        txtNama = new javax.swing.JTextField();
        lblKategori = new javax.swing.JLabel();
        cbKategori = new javax.swing.JComboBox<>();
        lblSatuan = new javax.swing.JLabel();
        cbSatuan = new javax.swing.JComboBox<>();
        lblMinimum = new javax.swing.JLabel();
        txtMinimum = new javax.swing.JTextField();
        lblUmur = new javax.swing.JLabel();
        txtUmur = new javax.swing.JTextField();
        lblSupplier = new javax.swing.JLabel();
        cbSupplier = new javax.swing.JComboBox<>();
        lblBahan = new javax.swing.JLabel();
        cbBahan = new javax.swing.JComboBox<>();
        lblIsi = new javax.swing.JLabel();
        txtIsi = new javax.swing.JTextField();
        lblInfo = new javax.swing.JLabel();
        pnlTombol = new javax.swing.JPanel();
        btnBersihkan = new javax.swing.JButton();
        btnHapus = new javax.swing.JButton();
        btnSimpan = new javax.swing.JButton();
        cardData = new sistempenjualan.RoundedPanel();
        pnlDataAtas = new javax.swing.JPanel();
        lblDataJudul = new javax.swing.JLabel();
        pnlFilter = new javax.swing.JPanel();
        txtCari = new javax.swing.JTextField();
        spData = new javax.swing.JScrollPane();
        tblData = new javax.swing.JTable();

        setLayout(new java.awt.BorderLayout(0, 20));

        pnlHeader.setLayout(new java.awt.BorderLayout(16, 0));

        pnlJudul.setLayout(new javax.swing.BoxLayout(pnlJudul, javax.swing.BoxLayout.Y_AXIS));

        lblJudul.setText("Data Barang");
        pnlJudul.add(lblJudul);

        lblSubjudul.setText("Bahan baku curah dan produk jadi hasil kemas ulang");
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

        lblFormJudul.setText("Tambah / Ubah Data");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 16, 0);
        cardForm.add(lblFormJudul, gridBagConstraints);

        lblKode.setText("Kode");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 16);
        cardForm.add(lblKode, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.25;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 14, 16);
        cardForm.add(txtKode, gridBagConstraints);

        lblNama.setText("Nama barang");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 16);
        cardForm.add(lblNama, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 14, 16);
        cardForm.add(txtNama, gridBagConstraints);

        lblKategori.setText("Kategori");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 0);
        cardForm.add(lblKategori, gridBagConstraints);

        cbKategori.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        cbKategoriActionPerformed(evt);
                    }
                });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.25;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 14, 0);
        cardForm.add(cbKategori, gridBagConstraints);

        lblSatuan.setText("Satuan");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 16);
        cardForm.add(lblSatuan, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.25;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 14, 16);
        cardForm.add(cbSatuan, gridBagConstraints);

        lblMinimum.setText("Stok minimum");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 16);
        cardForm.add(lblMinimum, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.25;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 14, 16);
        cardForm.add(txtMinimum, gridBagConstraints);

        lblUmur.setText("Umur simpan (hari)");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 16);
        cardForm.add(lblUmur, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.25;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 14, 16);
        cardForm.add(txtUmur, gridBagConstraints);

        lblSupplier.setText("Supplier utama (bahan baku)");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 0);
        cardForm.add(lblSupplier, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.25;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 14, 0);
        cardForm.add(cbSupplier, gridBagConstraints);

        lblBahan.setText("Bahan baku (produk jadi)");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 16);
        cardForm.add(lblBahan, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.25;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 14, 16);
        cardForm.add(cbBahan, gridBagConstraints);

        lblIsi.setText("Isi per kemasan (gram)");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 16);
        cardForm.add(lblIsi, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.25;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 14, 16);
        cardForm.add(txtIsi, gridBagConstraints);

        lblInfo.setText(" ");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 0);
        cardForm.add(lblInfo, gridBagConstraints);

        pnlTombol.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 8, 0));

        btnBersihkan.setText("Bersihkan");
        btnBersihkan.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        btnBersihkanActionPerformed(evt);
                    }
                });
        pnlTombol.add(btnBersihkan);

        btnHapus.setText("Hapus");
        btnHapus.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        btnHapusActionPerformed(evt);
                    }
                });
        pnlTombol.add(btnHapus);

        btnSimpan.setText("Simpan");
        btnSimpan.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        btnSimpanActionPerformed(evt);
                    }
                });
        pnlTombol.add(btnSimpan);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 14, 0);
        cardForm.add(pnlTombol, gridBagConstraints);
        pnlIsi.add(cardForm, java.awt.BorderLayout.NORTH);

        cardData.setLayout(new java.awt.BorderLayout(0, 12));

        pnlDataAtas.setLayout(new java.awt.BorderLayout(12, 0));

        lblDataJudul.setText("Daftar Data");
        pnlDataAtas.add(lblDataJudul, java.awt.BorderLayout.WEST);

        pnlFilter.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 8, 0));

        txtCari.setColumns(18);
        txtCari.addKeyListener(new java.awt.event.KeyAdapter() {
                    public void keyReleased(java.awt.event.KeyEvent evt) {
                        txtCariKeyReleased(evt);
                    }
                });
        pnlFilter.add(txtCari);
        pnlDataAtas.add(pnlFilter, java.awt.BorderLayout.EAST);
        cardData.add(pnlDataAtas, java.awt.BorderLayout.NORTH);

        tblData.addMouseListener(new java.awt.event.MouseAdapter() {
                    public void mouseClicked(java.awt.event.MouseEvent evt) {
                        tblDataMouseClicked(evt);
                    }
                });
        spData.setViewportView(tblData);
        cardData.add(spData, java.awt.BorderLayout.CENTER);
        pnlIsi.add(cardData, java.awt.BorderLayout.CENTER);
        add(pnlIsi, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    private void btnRefreshActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRefreshActionPerformed
        muatData();
    }//GEN-LAST:event_btnRefreshActionPerformed

    private void cbKategoriActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbKategoriActionPerformed
        kategoriBerubah();
    }//GEN-LAST:event_cbKategoriActionPerformed

    private void btnBersihkanActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBersihkanActionPerformed
        bersihkan();
    }//GEN-LAST:event_btnBersihkanActionPerformed

    private void btnHapusActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnHapusActionPerformed
        hapus();
    }//GEN-LAST:event_btnHapusActionPerformed

    private void btnSimpanActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSimpanActionPerformed
        simpan();
    }//GEN-LAST:event_btnSimpanActionPerformed

    private void txtCariKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtCariKeyReleased
        muatTabel();
    }//GEN-LAST:event_txtCariKeyReleased

    private void tblDataMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tblDataMouseClicked
        pilihBaris();
    }//GEN-LAST:event_tblDataMouseClicked

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnBersihkan;
    private javax.swing.JButton btnHapus;
    private javax.swing.JButton btnRefresh;
    private javax.swing.JButton btnSimpan;
    private javax.swing.JPanel cardData;
    private javax.swing.JPanel cardForm;
    private javax.swing.JComboBox<Object> cbBahan;
    private javax.swing.JComboBox<String> cbKategori;
    private javax.swing.JComboBox<String> cbSatuan;
    private javax.swing.JComboBox<Object> cbSupplier;
    private javax.swing.JLabel lblBahan;
    private javax.swing.JLabel lblDataJudul;
    private javax.swing.JLabel lblFormJudul;
    private javax.swing.JLabel lblInfo;
    private javax.swing.JLabel lblIsi;
    private javax.swing.JLabel lblJudul;
    private javax.swing.JLabel lblKategori;
    private javax.swing.JLabel lblKode;
    private javax.swing.JLabel lblMinimum;
    private javax.swing.JLabel lblNama;
    private javax.swing.JLabel lblSatuan;
    private javax.swing.JLabel lblSubjudul;
    private javax.swing.JLabel lblSupplier;
    private javax.swing.JLabel lblUmur;
    private javax.swing.JPanel pnlAksi;
    private javax.swing.JPanel pnlDataAtas;
    private javax.swing.JPanel pnlFilter;
    private javax.swing.JPanel pnlHeader;
    private javax.swing.JPanel pnlIsi;
    private javax.swing.JPanel pnlJudul;
    private javax.swing.JPanel pnlTombol;
    private javax.swing.JScrollPane spData;
    private javax.swing.JTable tblData;
    private javax.swing.JTextField txtCari;
    private javax.swing.JTextField txtIsi;
    private javax.swing.JTextField txtKode;
    private javax.swing.JTextField txtMinimum;
    private javax.swing.JTextField txtNama;
    private javax.swing.JTextField txtUmur;
    // End of variables declaration//GEN-END:variables
}
