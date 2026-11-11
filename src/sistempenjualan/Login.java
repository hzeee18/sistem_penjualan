package sistempenjualan;

import java.awt.Color;
import java.awt.Font;
import javax.swing.JOptionPane;

/**
 * Halaman login.
 */
public class Login extends javax.swing.JPanel {

    private final Bootstrap app;

    public Login(Bootstrap app) {
        this.app = app;
        initComponents();
        gaya();
    }

    private void gaya() {
        setBackground(Theme.BG);
        pnlKanan.setBackground(Theme.BG);
        pnlBrand.setBackground(Theme.SIDEBAR);
        pnlBrandIsi.setOpaque(false);

        lblLogo.setIcon(new javax.swing.ImageIcon(Notifikasi.ikonTray().getScaledInstance(56, 56, java.awt.Image.SCALE_SMOOTH)));
        lblLogo.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 28, 0));
        lblApp.setFont(Theme.font(Font.BOLD, 28f));
        lblApp.setForeground(Color.WHITE);
        lblPt.setFont(Theme.font(Font.PLAIN, 16f));
        lblPt.setForeground(new Color(0x9FC3AF));
        lblPt.setBorder(javax.swing.BorderFactory.createEmptyBorder(8, 0, 36, 0));
        for (javax.swing.JLabel l : new javax.swing.JLabel[]{lblFitur1, lblFitur2, lblFitur3}) {
            l.setFont(Theme.font(Font.PLAIN, 13f));
            l.setForeground(new Color(0xD5E3DB));
            l.setIcon(new Ikon("default", 10, new Color(0x6BBF93)));
            l.setIconTextGap(12);
            l.setBorder(javax.swing.BorderFactory.createEmptyBorder(6, 0, 6, 0));
        }

        Theme.card(cardLogin, 36);
        lblMasuk.setFont(Theme.font(Font.BOLD, 22f));
        lblMasuk.setForeground(Theme.TEXT);
        Theme.subtitle(lblMasukSub);
        Theme.label(lblUsername);
        Theme.label(lblPassword);
        Theme.placeholder(txtUsername, "Masukkan username");
        Theme.placeholder(txtPassword, "Masukkan password");
        txtPassword.putClientProperty(com.formdev.flatlaf.FlatClientProperties.STYLE, "showRevealButton:true");
        txtUsername.setPreferredSize(new java.awt.Dimension(0, 40));
        txtPassword.setPreferredSize(new java.awt.Dimension(0, 40));
        Theme.primary(btnMasuk);
        btnMasuk.setPreferredSize(new java.awt.Dimension(0, 42));
        Theme.muted(lblInfo);
        lblInfo.setFont(Theme.font(Font.PLAIN, 12f));
    }

    public void reset() {
        txtUsername.setText("");
        txtPassword.setText("");
        txtUsername.requestFocus();
    }

    private void masuk() {
        String user = txtUsername.getText().trim();
        String pass = new String(txtPassword.getPassword());
        if (user.isEmpty() || pass.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Username dan password wajib diisi.", "Login", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            Object[] r = Koneksi.one("SELECT id, nama_lengkap, role FROM m_user WHERE username = ? AND password = ?", user, pass);
            if (r == null) {
                JOptionPane.showMessageDialog(this, "Username atau password salah.", "Login", JOptionPane.ERROR_MESSAGE);
                txtPassword.setText("");
                return;
            }
            app.masuk(((Number) r[0]).intValue(), (String) r[1], (String) r[2]);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal login: " + e.getMessage(), "Login", JOptionPane.ERROR_MESSAGE);
        }
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        java.awt.GridBagConstraints gridBagConstraints;

        pnlBrand = new javax.swing.JPanel();
        pnlBrandIsi = new javax.swing.JPanel();
        lblLogo = new javax.swing.JLabel();
        lblApp = new javax.swing.JLabel();
        lblPt = new javax.swing.JLabel();
        lblFitur1 = new javax.swing.JLabel();
        lblFitur2 = new javax.swing.JLabel();
        lblFitur3 = new javax.swing.JLabel();
        pnlKanan = new javax.swing.JPanel();
        cardLogin = new sistempenjualan.RoundedPanel();
        lblMasuk = new javax.swing.JLabel();
        lblMasukSub = new javax.swing.JLabel();
        lblUsername = new javax.swing.JLabel();
        txtUsername = new javax.swing.JTextField();
        lblPassword = new javax.swing.JLabel();
        txtPassword = new javax.swing.JPasswordField();
        btnMasuk = new javax.swing.JButton();
        lblInfo = new javax.swing.JLabel();

        setLayout(new java.awt.BorderLayout());

        pnlBrand.setPreferredSize(new java.awt.Dimension(520, 600));
        pnlBrand.setLayout(new java.awt.GridBagLayout());

        pnlBrandIsi.setLayout(new javax.swing.BoxLayout(pnlBrandIsi, javax.swing.BoxLayout.Y_AXIS));

        lblLogo.setText("");
        pnlBrandIsi.add(lblLogo);

        lblApp.setText("<html>Sistem Monitoring<br>Persediaan &amp; Kedaluwarsa</html>");
        pnlBrandIsi.add(lblApp);

        lblPt.setText("PT Mitra Tiga Sepakat");
        pnlBrandIsi.add(lblPt);

        lblFitur1.setText("Pantau stok bahan baku & produk kemasan");
        pnlBrandIsi.add(lblFitur1);

        lblFitur2.setText("Lacak tanggal kedaluwarsa per batch (FEFO)");
        pnlBrandIsi.add(lblFitur2);

        lblFitur3.setText("Notifikasi otomatis stok menipis & hampir kedaluwarsa");
        pnlBrandIsi.add(lblFitur3);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 56, 0, 40);
        pnlBrand.add(pnlBrandIsi, gridBagConstraints);
        add(pnlBrand, java.awt.BorderLayout.WEST);

        pnlKanan.setLayout(new java.awt.GridBagLayout());

        cardLogin.setPreferredSize(new java.awt.Dimension(400, 440));
        cardLogin.setLayout(new java.awt.GridBagLayout());

        lblMasuk.setText("Selamat datang");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        cardLogin.add(lblMasuk, gridBagConstraints);

        lblMasukSub.setText("Masuk untuk melanjutkan ke aplikasi");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(4, 0, 26, 0);
        cardLogin.add(lblMasukSub, gridBagConstraints);

        lblUsername.setText("Username");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 0);
        cardLogin.add(lblUsername, gridBagConstraints);

        txtUsername.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        txtUsernameActionPerformed(evt);
                    }
                });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 16, 0);
        cardLogin.add(txtUsername, gridBagConstraints);

        lblPassword.setText("Password");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 0);
        cardLogin.add(lblPassword, gridBagConstraints);

        txtPassword.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        txtPasswordActionPerformed(evt);
                    }
                });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 24, 0);
        cardLogin.add(txtPassword, gridBagConstraints);

        btnMasuk.setText("Masuk");
        btnMasuk.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        btnMasukActionPerformed(evt);
                    }
                });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        cardLogin.add(btnMasuk, gridBagConstraints);

        lblInfo.setText("Akun contoh: admin / admin123  •  gudang / gudang123");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.insets = new java.awt.Insets(22, 0, 0, 0);
        cardLogin.add(lblInfo, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        pnlKanan.add(cardLogin, gridBagConstraints);
        add(pnlKanan, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    private void txtUsernameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtUsernameActionPerformed
        txtPassword.requestFocus();
    }//GEN-LAST:event_txtUsernameActionPerformed

    private void txtPasswordActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtPasswordActionPerformed
        masuk();
    }//GEN-LAST:event_txtPasswordActionPerformed

    private void btnMasukActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnMasukActionPerformed
        masuk();
    }//GEN-LAST:event_btnMasukActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnMasuk;
    private javax.swing.JPanel cardLogin;
    private javax.swing.JLabel lblApp;
    private javax.swing.JLabel lblFitur1;
    private javax.swing.JLabel lblFitur2;
    private javax.swing.JLabel lblFitur3;
    private javax.swing.JLabel lblInfo;
    private javax.swing.JLabel lblLogo;
    private javax.swing.JLabel lblMasuk;
    private javax.swing.JLabel lblMasukSub;
    private javax.swing.JLabel lblPassword;
    private javax.swing.JLabel lblPt;
    private javax.swing.JLabel lblUsername;
    private javax.swing.JPanel pnlBrand;
    private javax.swing.JPanel pnlBrandIsi;
    private javax.swing.JPanel pnlKanan;
    private javax.swing.JPasswordField txtPassword;
    private javax.swing.JTextField txtUsername;
    // End of variables declaration//GEN-END:variables
}
