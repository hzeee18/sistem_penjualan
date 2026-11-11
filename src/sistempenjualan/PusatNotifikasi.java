package sistempenjualan;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.SpinnerNumberModel;
import javax.swing.table.DefaultTableModel;

/**
 * Pusat notifikasi + pengaturan batas hari dan interval pengecekan otomatis.
 */
public class PusatNotifikasi extends javax.swing.JPanel implements Halaman {

    private final Bootstrap app;
    private final List<Notifikasi.Item> tampil = new ArrayList<>();
    private Date terakhirCek;

    public PusatNotifikasi(Bootstrap app) {
        this.app = app;
        initComponents();
        gaya();
    }

    private void gaya() {
        Theme.page(this);
        Theme.transparent(pnlHeader, pnlJudul, pnlAksi, pnlIsi, pnlNotifAtas, pnlFilter);
        Theme.title(lblJudul);
        Theme.subtitle(lblSubjudul);
        Theme.secondary(btnDibaca);
        Theme.primary(btnCek, btnSimpan);
        btnCek.setIcon(new Ikon("refresh", 16, java.awt.Color.WHITE));
        Theme.card(cardNotif);
        Theme.card(cardPengaturan, 20);
        Theme.section(lblNotifJudul);
        Theme.section(lblPengaturanJudul);
        Theme.label(lblBatasPeringatan);
        Theme.label(lblBatasKritis);
        Theme.label(lblInterval);
        Theme.muted(lblStatusCek);
        chkWindows.setOpaque(false);
        Theme.table(tblNotif);
        spnPeringatan.setModel(new SpinnerNumberModel(30, 1, 365, 1));
        spnKritis.setModel(new SpinnerNumberModel(7, 0, 365, 1));
        spnInterval.setModel(new SpinnerNumberModel(5, 1, 240, 1));
        cbLevel.setModel(new javax.swing.DefaultComboBoxModel<>(new String[]{"Semua Level", "Kritis", "Peringatan"}));
        cbJenis.setModel(new javax.swing.DefaultComboBoxModel<>(new String[]{"Semua Jenis", "Sudah Kedaluwarsa", "Hampir Kedaluwarsa", "Stok Habis", "Stok Menipis"}));
    }

    @Override
    public void muatData() {
        spnPeringatan.setValue(Integer.parseInt(Persediaan.pengaturan("batas_peringatan_hari", "30")));
        spnKritis.setValue(Integer.parseInt(Persediaan.pengaturan("batas_kritis_hari", "7")));
        spnInterval.setValue(Integer.parseInt(Persediaan.pengaturan("interval_cek_menit", "5")));
        chkWindows.setSelected("1".equals(Persediaan.pengaturan("notifikasi_windows", "1")));
        boolean admin = app.isAdmin();
        for (javax.swing.JComponent c : new javax.swing.JComponent[]{spnPeringatan, spnKritis, spnInterval, chkWindows, btnSimpan}) {
            c.setEnabled(admin);
        }
        cekSekarang();
    }

    private void cekSekarang() {
        terakhirCek = new Date();
        tampilkan(Notifikasi.periksaSekarang(false));
    }

    /** Dipanggil juga oleh Bootstrap ketika timer menemukan notifikasi baru. */
    public void tampilkan(List<Notifikasi.Item> items) {
        if (cbLevel.getSelectedItem() == null || cbJenis.getSelectedItem() == null) {
            return;
        }
        String lv = cbLevel.getSelectedIndex() == 0 ? null : cbLevel.getSelectedItem().toString().toUpperCase();
        String jn = cbJenis.getSelectedIndex() == 0 ? null : cbJenis.getSelectedItem().toString();
        DefaultTableModel m = Theme.model("Level", "Jenis", "Barang", "Keterangan");
        tampil.clear();
        int kritis = 0;
        for (Notifikasi.Item i : items) {
            if (Notifikasi.KRITIS.equals(i.level)) {
                kritis++;
            }
            if (lv != null && !lv.equals(i.level)) {
                continue;
            }
            if (jn != null && !jn.equals(i.jenis)) {
                continue;
            }
            tampil.add(i);
            m.addRow(new Object[]{i.level, i.jenis, i.barang, i.pesan});
        }
        tblNotif.setModel(m);
        Theme.statusColumn(tblNotif, 0);
        Theme.widths(tblNotif, 115, 150, 170, 430);
        lblNotifJudul.setText("Daftar Notifikasi (" + items.size() + " total • " + kritis + " kritis)");
        String jam = terakhirCek == null ? "-" : new SimpleDateFormat("HH:mm:ss").format(terakhirCek);
        lblStatusCek.setText("<html>Pengecekan otomatis aktif tiap " + Persediaan.pengaturan("interval_cek_menit", "5")
                + " menit.<br>Terakhir dicek manual: " + jam + "</html>");
    }

    private void tandaiDibaca() {
        Notifikasi.tandaiSemuaDibaca();
        JOptionPane.showMessageDialog(this, "Semua notifikasi ditandai sudah dibaca.");
    }

    private void bukaTerkait(java.awt.event.MouseEvent evt) {
        if (evt.getClickCount() < 2 || tblNotif.getSelectedRow() < 0) {
            return;
        }
        Notifikasi.Item i = tampil.get(tblNotif.convertRowIndexToModel(tblNotif.getSelectedRow()));
        app.buka(i.jenis.startsWith("Stok") ? Bootstrap.MONITOR_STOK : Bootstrap.MONITOR_EXP);
    }

    private void simpan() {
        int warn = (Integer) spnPeringatan.getValue();
        int kritis = (Integer) spnKritis.getValue();
        if (kritis >= warn) {
            JOptionPane.showMessageDialog(this, "Batas kritis harus lebih kecil dari batas peringatan.");
            return;
        }
        try {
            Persediaan.simpanPengaturan("batas_peringatan_hari", String.valueOf(warn));
            Persediaan.simpanPengaturan("batas_kritis_hari", String.valueOf(kritis));
            Persediaan.simpanPengaturan("interval_cek_menit", spnInterval.getValue().toString());
            Persediaan.simpanPengaturan("notifikasi_windows", chkWindows.isSelected() ? "1" : "0");
            Notifikasi.mulai();
            cekSekarang();
            JOptionPane.showMessageDialog(this, "Pengaturan notifikasi disimpan.");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal menyimpan: " + e.getMessage());
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
        btnDibaca = new javax.swing.JButton();
        btnCek = new javax.swing.JButton();
        pnlIsi = new javax.swing.JPanel();
        cardNotif = new sistempenjualan.RoundedPanel();
        pnlNotifAtas = new javax.swing.JPanel();
        lblNotifJudul = new javax.swing.JLabel();
        pnlFilter = new javax.swing.JPanel();
        cbLevel = new javax.swing.JComboBox<>();
        cbJenis = new javax.swing.JComboBox<>();
        spNotif = new javax.swing.JScrollPane();
        tblNotif = new javax.swing.JTable();
        cardPengaturan = new sistempenjualan.RoundedPanel();
        lblPengaturanJudul = new javax.swing.JLabel();
        lblBatasPeringatan = new javax.swing.JLabel();
        spnPeringatan = new javax.swing.JSpinner();
        lblBatasKritis = new javax.swing.JLabel();
        spnKritis = new javax.swing.JSpinner();
        lblInterval = new javax.swing.JLabel();
        spnInterval = new javax.swing.JSpinner();
        chkWindows = new javax.swing.JCheckBox();
        btnSimpan = new javax.swing.JButton();
        lblStatusCek = new javax.swing.JLabel();
        lblSpacer = new javax.swing.JLabel();

        setLayout(new java.awt.BorderLayout(0, 20));

        pnlHeader.setLayout(new java.awt.BorderLayout(16, 0));

        pnlJudul.setLayout(new javax.swing.BoxLayout(pnlJudul, javax.swing.BoxLayout.Y_AXIS));

        lblJudul.setText("Notifikasi");
        pnlJudul.add(lblJudul);

        lblSubjudul.setText("Peringatan otomatis untuk stok menipis/habis dan barang yang hampir atau sudah kedaluwarsa");
        pnlJudul.add(lblSubjudul);
        pnlHeader.add(pnlJudul, java.awt.BorderLayout.CENTER);

        pnlAksi.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 8, 0));

        btnDibaca.setText("Tandai Sudah Dibaca");
        btnDibaca.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        btnDibacaActionPerformed(evt);
                    }
                });
        pnlAksi.add(btnDibaca);

        btnCek.setText("Cek Sekarang");
        btnCek.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        btnCekActionPerformed(evt);
                    }
                });
        pnlAksi.add(btnCek);
        pnlHeader.add(pnlAksi, java.awt.BorderLayout.EAST);
        add(pnlHeader, java.awt.BorderLayout.NORTH);

        pnlIsi.setLayout(new java.awt.BorderLayout(20, 0));

        cardNotif.setLayout(new java.awt.BorderLayout(0, 12));

        pnlNotifAtas.setLayout(new java.awt.BorderLayout(12, 0));

        lblNotifJudul.setText("Daftar Notifikasi");
        pnlNotifAtas.add(lblNotifJudul, java.awt.BorderLayout.WEST);

        pnlFilter.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 8, 0));

        cbLevel.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        cbLevelActionPerformed(evt);
                    }
                });
        pnlFilter.add(cbLevel);

        cbJenis.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        cbJenisActionPerformed(evt);
                    }
                });
        pnlFilter.add(cbJenis);
        pnlNotifAtas.add(pnlFilter, java.awt.BorderLayout.EAST);
        cardNotif.add(pnlNotifAtas, java.awt.BorderLayout.NORTH);

        tblNotif.addMouseListener(new java.awt.event.MouseAdapter() {
                    public void mouseClicked(java.awt.event.MouseEvent evt) {
                        tblNotifMouseClicked(evt);
                    }
                });
        spNotif.setViewportView(tblNotif);
        cardNotif.add(spNotif, java.awt.BorderLayout.CENTER);
        pnlIsi.add(cardNotif, java.awt.BorderLayout.CENTER);

        cardPengaturan.setPreferredSize(new java.awt.Dimension(290, 100));
        cardPengaturan.setLayout(new java.awt.GridBagLayout());

        lblPengaturanJudul.setText("Pengaturan Notifikasi");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 18, 0);
        cardPengaturan.add(lblPengaturanJudul, gridBagConstraints);

        lblBatasPeringatan.setText("Batas peringatan (hari sebelum exp.)");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 0);
        cardPengaturan.add(lblBatasPeringatan, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 14, 0);
        cardPengaturan.add(spnPeringatan, gridBagConstraints);

        lblBatasKritis.setText("Batas kritis (hari sebelum exp.)");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 0);
        cardPengaturan.add(lblBatasKritis, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 14, 0);
        cardPengaturan.add(spnKritis, gridBagConstraints);

        lblInterval.setText("Cek otomatis setiap (menit)");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 0);
        cardPengaturan.add(lblInterval, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 14, 0);
        cardPengaturan.add(spnInterval, gridBagConstraints);

        chkWindows.setText("Tampilkan juga notifikasi Windows");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 18, 0);
        cardPengaturan.add(chkWindows, gridBagConstraints);

        btnSimpan.setText("Simpan Pengaturan");
        btnSimpan.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        btnSimpanActionPerformed(evt);
                    }
                });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        cardPengaturan.add(btnSimpan, gridBagConstraints);

        lblStatusCek.setText("-");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(16, 0, 0, 0);
        cardPengaturan.add(lblStatusCek, gridBagConstraints);

        lblSpacer.setText(" ");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.weighty = 1.0;
        cardPengaturan.add(lblSpacer, gridBagConstraints);
        pnlIsi.add(cardPengaturan, java.awt.BorderLayout.EAST);
        add(pnlIsi, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    private void btnDibacaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDibacaActionPerformed
        tandaiDibaca();
    }//GEN-LAST:event_btnDibacaActionPerformed

    private void btnCekActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCekActionPerformed
        cekSekarang();
    }//GEN-LAST:event_btnCekActionPerformed

    private void cbLevelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbLevelActionPerformed
        tampilkan(Notifikasi.terakhir());
    }//GEN-LAST:event_cbLevelActionPerformed

    private void cbJenisActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbJenisActionPerformed
        tampilkan(Notifikasi.terakhir());
    }//GEN-LAST:event_cbJenisActionPerformed

    private void tblNotifMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tblNotifMouseClicked
        bukaTerkait(evt);
    }//GEN-LAST:event_tblNotifMouseClicked

    private void btnSimpanActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSimpanActionPerformed
        simpan();
    }//GEN-LAST:event_btnSimpanActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCek;
    private javax.swing.JButton btnDibaca;
    private javax.swing.JButton btnSimpan;
    private javax.swing.JPanel cardNotif;
    private javax.swing.JPanel cardPengaturan;
    private javax.swing.JComboBox<String> cbJenis;
    private javax.swing.JComboBox<String> cbLevel;
    private javax.swing.JCheckBox chkWindows;
    private javax.swing.JLabel lblBatasKritis;
    private javax.swing.JLabel lblBatasPeringatan;
    private javax.swing.JLabel lblInterval;
    private javax.swing.JLabel lblJudul;
    private javax.swing.JLabel lblNotifJudul;
    private javax.swing.JLabel lblPengaturanJudul;
    private javax.swing.JLabel lblSpacer;
    private javax.swing.JLabel lblStatusCek;
    private javax.swing.JLabel lblSubjudul;
    private javax.swing.JPanel pnlAksi;
    private javax.swing.JPanel pnlFilter;
    private javax.swing.JPanel pnlHeader;
    private javax.swing.JPanel pnlIsi;
    private javax.swing.JPanel pnlJudul;
    private javax.swing.JPanel pnlNotifAtas;
    private javax.swing.JScrollPane spNotif;
    private javax.swing.JSpinner spnInterval;
    private javax.swing.JSpinner spnKritis;
    private javax.swing.JSpinner spnPeringatan;
    private javax.swing.JTable tblNotif;
    // End of variables declaration//GEN-END:variables
}
