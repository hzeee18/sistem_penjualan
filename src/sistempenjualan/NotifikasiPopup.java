package sistempenjualan;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.Window;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JWindow;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

/**
 * Popup notifikasi di pojok kanan bawah jendela aplikasi. Hilang sendiri
 * setelah 15 detik, atau saat tombol ditekan.
 */
public class NotifikasiPopup extends JWindow {

    private static NotifikasiPopup aktif;

    public static void tampil(List<Notifikasi.Item> items, int kritis, int warn) {
        SwingUtilities.invokeLater(() -> {
            if (aktif != null) {
                aktif.dispose();
            }
            Window owner = Bootstrap.instance();
            if (owner == null || !owner.isShowing()) {
                return;
            }
            aktif = new NotifikasiPopup(owner, items, kritis, warn);
            aktif.setVisible(true);
        });
    }

    private NotifikasiPopup(Window owner, List<Notifikasi.Item> items, int kritis, int warn) {
        super(owner);
        RoundedPanel root = new RoundedPanel();
        root.setArc(18);
        root.setAccent(kritis > 0 ? Theme.DANGER : Theme.CAUTION);
        root.setLayout(new BorderLayout(0, 10));
        root.setBorder(BorderFactory.createEmptyBorder(14, 18, 14, 14));

        JPanel head = new JPanel(new BorderLayout(10, 0));
        head.setOpaque(false);
        JLabel ikon = new JLabel(new Ikon("lonceng", 22, kritis > 0 ? Theme.DANGER : Theme.CAUTION));
        JLabel judul = new JLabel(items.size() + " peringatan persediaan");
        judul.setFont(Theme.font(Font.BOLD, 14f));
        judul.setForeground(Theme.TEXT);
        StringBuilder sb = new StringBuilder();
        if (kritis > 0) {
            sb.append(kritis).append(" kritis");
        }
        if (warn > 0) {
            sb.append(sb.length() > 0 ? " • " : "").append(warn).append(" peringatan");
        }
        JLabel sub = new JLabel(sb.toString());
        Theme.muted(sub);
        JPanel teks = new JPanel();
        teks.setOpaque(false);
        teks.setLayout(new BoxLayout(teks, BoxLayout.Y_AXIS));
        teks.add(judul);
        teks.add(sub);
        JLabel tutup = new JLabel("✕");
        tutup.setForeground(Theme.MUTED);
        tutup.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        tutup.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                dispose();
            }
        });
        head.add(ikon, BorderLayout.WEST);
        head.add(teks, BorderLayout.CENTER);
        head.add(tutup, BorderLayout.EAST);

        JPanel list = new JPanel();
        list.setOpaque(false);
        list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));
        int max = Math.min(4, items.size());
        for (int i = 0; i < max; i++) {
            Notifikasi.Item it = items.get(i);
            Color[] w = Theme.statusColor(Notifikasi.KRITIS.equals(it.level) ? "KRITIS" : "PERINGATAN");
            JLabel l = new JLabel("<html><div style='width:290px'><b>" + it.barang + "</b> &middot; <span style='color:"
                    + hex(w[0]) + "'>" + it.jenis + "</span><br><span style='color:#6B7280'>" + it.pesan + "</span></div></html>");
            l.setFont(Theme.font(Font.PLAIN, 12f));
            l.setBorder(BorderFactory.createEmptyBorder(6, 0, 6, 0));
            l.setAlignmentX(0f);
            list.add(l);
        }
        if (items.size() > max) {
            JLabel more = new JLabel("+ " + (items.size() - max) + " lainnya");
            Theme.muted(more);
            more.setAlignmentX(0f);
            list.add(more);
        }
        list.add(Box.createVerticalStrut(4));

        JPanel aksi = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 8, 0));
        aksi.setOpaque(false);
        JButton nanti = new JButton("Nanti");
        JButton lihat = new JButton("Lihat Notifikasi");
        Theme.secondary(nanti);
        Theme.primary(lihat);
        nanti.addActionListener(e -> dispose());
        lihat.addActionListener(e -> {
            dispose();
            Bootstrap.instance().buka(Bootstrap.NOTIFIKASI);
        });
        aksi.add(nanti);
        aksi.add(lihat);

        root.add(head, BorderLayout.NORTH);
        root.add(list, BorderLayout.CENTER);
        root.add(aksi, BorderLayout.SOUTH);

        setBackground(new Color(0, 0, 0, 0));
        setContentPane(root);
        setSize(new Dimension(380, getPreferredSize().height));
        pack();
        setSize(380, getHeight());
        Point p = owner.getLocationOnScreen();
        setLocation(p.x + owner.getWidth() - getWidth() - 24, p.y + owner.getHeight() - getHeight() - 24);

        Timer t = new Timer(15000, e -> dispose());
        t.setRepeats(false);
        t.start();
    }

    private static String hex(Color c) {
        return String.format("#%02X%02X%02X", c.getRed(), c.getGreen(), c.getBlue());
    }
}
