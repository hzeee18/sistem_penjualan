package sistempenjualan;

import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

/**
 * Master data pengguna (khusus admin).
 */
public class DataUser extends javax.swing.JPanel implements Halaman {

    private final Bootstrap app;
    private final List<Integer> ids = new ArrayList<>();
    private int idTerpilih = 0;

    public DataUser(Bootstrap app) {
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
        cbRole.setModel(new javax.swing.DefaultComboBoxModel<>(new String[]{"gudang", "admin"}));
        Theme.placeholder(txtPassword, "kosongkan jika tidak diubah");
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
            DefaultTableModel m = Theme.model("Username", "Nama Lengkap", "Hak Akses");
            ids.clear();
            for (Object[] r : Koneksi.rows("SELECT id, username, nama_lengkap, role FROM m_user WHERE username LIKE ? OR nama_lengkap LIKE ? ORDER BY role, username", c, c)) {
                ids.add(((Number) r[0]).intValue());
                m.addRow(new Object[]{r[1], r[2], "admin".equals(r[3]) ? "ADMIN" : "STAFF GUDANG"});
            }
            tblData.setModel(m);
            Theme.statusColumn(tblData, 2);
            Theme.widths(tblData, 200, 300, 140);
            lblDataJudul.setText("Daftar Pengguna (" + m.getRowCount() + ")");
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
            Object[] r = Koneksi.one("SELECT username, nama_lengkap, role FROM m_user WHERE id = ?", id);
            idTerpilih = id;
            txtUsername.setText((String) r[0]);
            txtNamaLengkap.setText((String) r[1]);
            txtPassword.setText("");
            cbRole.setSelectedItem(r[2]);
            modeUbah(true);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage());
        }
    }

    private void bersihkan() {
        idTerpilih = 0;
        txtUsername.setText("");
        txtNamaLengkap.setText("");
        txtPassword.setText("");
        cbRole.setSelectedIndex(0);
        tblData.clearSelection();
        modeUbah(false);
    }

    private void simpan() {
        String u = txtUsername.getText().trim();
        String n = txtNamaLengkap.getText().trim();
        String p = new String(txtPassword.getPassword());
        if (u.isEmpty() || n.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Username dan nama lengkap wajib diisi.");
            return;
        }
        try {
            if (idTerpilih == 0) {
                if (p.isEmpty()) {
                    throw new Exception("Password wajib diisi untuk pengguna baru.");
                }
                Koneksi.exec("INSERT INTO m_user (username, password, nama_lengkap, role) VALUES (?, ?, ?, ?)", u, p, n, cbRole.getSelectedItem());
            } else if (p.isEmpty()) {
                Koneksi.exec("UPDATE m_user SET username=?, nama_lengkap=?, role=? WHERE id=?", u, n, cbRole.getSelectedItem(), idTerpilih);
            } else {
                Koneksi.exec("UPDATE m_user SET username=?, nama_lengkap=?, role=?, password=? WHERE id=?", u, n, cbRole.getSelectedItem(), p, idTerpilih);
            }
            JOptionPane.showMessageDialog(this, "Data pengguna tersimpan.");
            muatData();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal menyimpan: " + e.getMessage());
        }
    }

    private void hapus() {
        if (idTerpilih == 0) {
            return;
        }
        if (idTerpilih == app.getSessionUserId()) {
            JOptionPane.showMessageDialog(this, "Tidak bisa menghapus akun yang sedang dipakai.");
            return;
        }
        if (JOptionPane.showConfirmDialog(this, "Hapus pengguna " + txtUsername.getText() + "?", "Hapus", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            Koneksi.exec("DELETE FROM m_user WHERE id = ?", idTerpilih);
            muatData();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Pengguna tidak bisa dihapus karena sudah punya riwayat transaksi.");
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
        lblUsername = new javax.swing.JLabel();
        txtUsername = new javax.swing.JTextField();
        lblNamaLengkap = new javax.swing.JLabel();
        txtNamaLengkap = new javax.swing.JTextField();
        lblPassword = new javax.swing.JLabel();
        txtPassword = new javax.swing.JPasswordField();
        lblRole = new javax.swing.JLabel();
        cbRole = new javax.swing.JComboBox<>();
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

        lblJudul.setText("Data Pengguna");
        pnlJudul.add(lblJudul);

        lblSubjudul.setText("Akun yang bisa masuk ke aplikasi (khusus admin)");
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

        lblUsername.setText("Username");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 16);
        cardForm.add(lblUsername, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.25;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 14, 16);
        cardForm.add(txtUsername, gridBagConstraints);

        lblNamaLengkap.setText("Nama lengkap");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 16);
        cardForm.add(lblNamaLengkap, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.25;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 14, 16);
        cardForm.add(txtNamaLengkap, gridBagConstraints);

        lblPassword.setText("Password");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 16);
        cardForm.add(lblPassword, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.25;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 14, 16);
        cardForm.add(txtPassword, gridBagConstraints);

        lblRole.setText("Hak akses");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 0);
        cardForm.add(lblRole, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.25;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 14, 0);
        cardForm.add(cbRole, gridBagConstraints);

        lblInfo.setText(" ");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
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
        gridBagConstraints.gridy = 4;
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
    private javax.swing.JComboBox<String> cbRole;
    private javax.swing.JLabel lblDataJudul;
    private javax.swing.JLabel lblFormJudul;
    private javax.swing.JLabel lblInfo;
    private javax.swing.JLabel lblJudul;
    private javax.swing.JLabel lblNamaLengkap;
    private javax.swing.JLabel lblPassword;
    private javax.swing.JLabel lblRole;
    private javax.swing.JLabel lblSubjudul;
    private javax.swing.JLabel lblUsername;
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
    private javax.swing.JTextField txtNamaLengkap;
    private javax.swing.JPasswordField txtPassword;
    private javax.swing.JTextField txtUsername;
    // End of variables declaration//GEN-END:variables
}
