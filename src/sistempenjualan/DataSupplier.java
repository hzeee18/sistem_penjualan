package sistempenjualan;

import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

/**
 * Master data supplier.
 */
public class DataSupplier extends javax.swing.JPanel implements Halaman {

    private final Bootstrap app;
    private final List<Integer> ids = new ArrayList<>();
    private int idTerpilih = 0;

    public DataSupplier(Bootstrap app) {
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
        bersihkan();
        muatTabel();
    }

    private void muatTabel() {
        try {
            String c = "%" + txtCari.getText().trim() + "%";
            DefaultTableModel m = Theme.model("Nama Supplier", "PIC", "No. HP", "Email", "Alamat");
            ids.clear();
            for (Object[] r : Koneksi.rows("SELECT id, nama, pic, no_hp, email, alamat FROM m_supplier WHERE nama LIKE ? OR pic LIKE ? ORDER BY nama", c, c)) {
                ids.add(((Number) r[0]).intValue());
                m.addRow(new Object[]{r[1] == null ? "-" : r[1], r[2] == null ? "-" : r[2], r[3] == null ? "-" : r[3], r[4] == null ? "-" : r[4], r[5] == null ? "-" : r[5]});
            }
            tblData.setModel(m);
            Theme.widths(tblData, 200, 110, 120, 190, 260);
            lblDataJudul.setText("Daftar Supplier (" + m.getRowCount() + ")");
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
            Object[] r = Koneksi.one("SELECT nama, pic, no_hp, email, alamat FROM m_supplier WHERE id = ?", id);
            idTerpilih = id;
            txtNama.setText(r[0] == null ? "" : r[0].toString());
            txtPic.setText(r[1] == null ? "" : r[1].toString());
            txtHp.setText(r[2] == null ? "" : r[2].toString());
            txtEmail.setText(r[3] == null ? "" : r[3].toString());
            txtAlamat.setText(r[4] == null ? "" : r[4].toString());
            modeUbah(true);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage());
        }
    }

    private void bersihkan() {
        idTerpilih = 0;
        txtNama.setText("");
        txtPic.setText("");
        txtHp.setText("");
        txtEmail.setText("");
        txtAlamat.setText("");
        tblData.clearSelection();
        modeUbah(false);
    }

    private void simpan() {
        if (txtNama.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nama wajib diisi.");
            return;
        }
        try {
            if (idTerpilih == 0) {
                Koneksi.exec("INSERT INTO m_supplier (nama, pic, no_hp, email, alamat) VALUES (?, ?, ?, ?, ?)", txtNama.getText().trim(), txtPic.getText().trim(), txtHp.getText().trim(), txtEmail.getText().trim(), txtAlamat.getText().trim());
            } else {
                Koneksi.exec("UPDATE m_supplier SET nama=?, pic=?, no_hp=?, email=?, alamat=? WHERE id = ?", txtNama.getText().trim(), txtPic.getText().trim(), txtHp.getText().trim(), txtEmail.getText().trim(), txtAlamat.getText().trim(), idTerpilih);
            }
            JOptionPane.showMessageDialog(this, "Data tersimpan.");
            muatData();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal menyimpan: " + e.getMessage());
        }
    }

    private void hapus() {
        if (idTerpilih == 0) {
            return;
        }
        if (JOptionPane.showConfirmDialog(this, "Hapus data " + txtNama.getText() + "?", "Hapus", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            Koneksi.exec("DELETE FROM m_supplier WHERE id = ?", idTerpilih);
            muatData();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Data tidak bisa dihapus karena sudah dipakai di transaksi.");
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
        lblNama = new javax.swing.JLabel();
        txtNama = new javax.swing.JTextField();
        lblPic = new javax.swing.JLabel();
        txtPic = new javax.swing.JTextField();
        lblHp = new javax.swing.JLabel();
        txtHp = new javax.swing.JTextField();
        lblEmail = new javax.swing.JLabel();
        txtEmail = new javax.swing.JTextField();
        lblAlamat = new javax.swing.JLabel();
        txtAlamat = new javax.swing.JTextField();
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

        lblJudul.setText("Data Supplier");
        pnlJudul.add(lblJudul);

        lblSubjudul.setText("Pemasok bahan baku bubuk");
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

        lblNama.setText("Nama supplier");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 16);
        cardForm.add(lblNama, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 14, 16);
        cardForm.add(txtNama, gridBagConstraints);

        lblPic.setText("Nama PIC");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 16);
        cardForm.add(lblPic, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.25;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 14, 16);
        cardForm.add(txtPic, gridBagConstraints);

        lblHp.setText("No. HP / Telepon");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 0);
        cardForm.add(lblHp, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.25;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 14, 0);
        cardForm.add(txtHp, gridBagConstraints);

        lblEmail.setText("Email");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 16);
        cardForm.add(lblEmail, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 14, 16);
        cardForm.add(txtEmail, gridBagConstraints);

        lblAlamat.setText("Alamat");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 0);
        cardForm.add(lblAlamat, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 14, 0);
        cardForm.add(txtAlamat, gridBagConstraints);

        lblInfo.setText(" ");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 16);
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
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 14, 16);
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
    private javax.swing.JLabel lblAlamat;
    private javax.swing.JLabel lblDataJudul;
    private javax.swing.JLabel lblEmail;
    private javax.swing.JLabel lblFormJudul;
    private javax.swing.JLabel lblHp;
    private javax.swing.JLabel lblInfo;
    private javax.swing.JLabel lblJudul;
    private javax.swing.JLabel lblNama;
    private javax.swing.JLabel lblPic;
    private javax.swing.JLabel lblSubjudul;
    private javax.swing.JPanel pnlAksi;
    private javax.swing.JPanel pnlDataAtas;
    private javax.swing.JPanel pnlFilter;
    private javax.swing.JPanel pnlHeader;
    private javax.swing.JPanel pnlIsi;
    private javax.swing.JPanel pnlJudul;
    private javax.swing.JPanel pnlTombol;
    private javax.swing.JScrollPane spData;
    private javax.swing.JTable tblData;
    private javax.swing.JTextField txtAlamat;
    private javax.swing.JTextField txtCari;
    private javax.swing.JTextField txtEmail;
    private javax.swing.JTextField txtHp;
    private javax.swing.JTextField txtNama;
    private javax.swing.JTextField txtPic;
    // End of variables declaration//GEN-END:variables
}
