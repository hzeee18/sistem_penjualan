package sistempenjualan;

import com.formdev.flatlaf.FlatClientProperties;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;

/**
 * Jendela utama: halaman login + kerangka aplikasi (sidebar kiri, header atas,
 * dan area konten yang berganti sesuai menu).
 */
public class Bootstrap extends JFrame {

    // kunci halaman
    public static final String DASHBOARD = "dashboard";
    public static final String NOTIFIKASI = "notifikasi";
    public static final String MONITOR_STOK = "monitor_stok";
    public static final String MONITOR_EXP = "monitor_kedaluwarsa";
    public static final String LACAK = "lacak_batch";
    public static final String BARANG_MASUK = "barang_masuk";
    public static final String KEMAS_ULANG = "kemas_ulang";
    public static final String BARANG_KELUAR = "barang_keluar";
    public static final String DATA_BARANG = "data_barang";
    public static final String DATA_SUPPLIER = "data_supplier";
    public static final String DATA_CUSTOMER = "data_customer";
    public static final String DATA_USER = "data_user";
    public static final String LAPORAN = "laporan";

    private static Bootstrap instance;

    private final CardLayout rootLayout = new CardLayout();
    private final JPanel root = new JPanel(rootLayout);
    private final CardLayout kontenLayout = new CardLayout();
    private final JPanel konten = new JPanel(kontenLayout);
    private final Map<String, Component> halaman = new LinkedHashMap<>();
    private final Map<String, JButton> menu = new LinkedHashMap<>();
    private final Map<String, JLabel> seksi = new LinkedHashMap<>();
    private final Login login;

    private JLabel lblNama;
    private JLabel lblRole;
    private JLabel lblAvatar;
    private BellButton bell;
    private String aktif;

    // sesi login
    private int sessionUserId;
    private String sessionRole;
    private String sessionNama;

    public static Bootstrap instance() {
        return instance;
    }

    public Bootstrap() {
        instance = this;
        setTitle(Theme.NAMA_APP + " & Kedaluwarsa Barang - " + Theme.NAMA_PT);
        setIconImage(Notifikasi.ikonTray());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1180, 720));
        setSize(1366, 800);
        setLocationRelativeTo(null);

        login = new Login(this);
        root.add(login, "login");
        root.add(buatShell(), "app");
        setContentPane(root);
        rootLayout.show(root, "login");

        Notifikasi.addListener((semua, baru) -> {
            bell.setJumlah(Notifikasi.jumlahBelumDibaca());
            Component c = halaman.get(NOTIFIKASI);
            if (c instanceof PusatNotifikasi && NOTIFIKASI.equals(aktif)) {
                ((PusatNotifikasi) c).tampilkan(semua);
            }
        });
    }

    // ------------------------------------------------------------------ sesi

    public int getSessionUserId() {
        return sessionUserId;
    }

    public String getSessionRole() {
        return sessionRole;
    }

    public String getSessionNama() {
        return sessionNama;
    }

    public boolean isAdmin() {
        return "admin".equalsIgnoreCase(sessionRole);
    }

    /** Dipanggil dari Login setelah username & password benar. */
    public void masuk(int userId, String nama, String role) {
        sessionUserId = userId;
        sessionNama = nama;
        sessionRole = role;
        lblNama.setText(nama);
        lblRole.setText("admin".equalsIgnoreCase(role) ? "Administrator" : "Staff Gudang");
        lblAvatar.setText(inisial(nama));
        boolean admin = isAdmin();
        menu.get(DATA_USER).setVisible(admin);
        rootLayout.show(root, "app");
        buka(DASHBOARD);
        Notifikasi.ringkasanLogin();
        Notifikasi.mulai();
    }

    public void logout() {
        int ok = JOptionPane.showConfirmDialog(this, "Keluar dari aplikasi?", "Logout", JOptionPane.YES_NO_OPTION);
        if (ok != JOptionPane.YES_OPTION) {
            return;
        }
        Notifikasi.berhenti();
        Notifikasi.hapusTray();
        sessionUserId = 0;
        sessionRole = null;
        sessionNama = null;
        login.reset();
        rootLayout.show(root, "login");
    }

    // ------------------------------------------------------------------ navigasi

    /** Buka halaman berdasarkan kunci (lihat konstanta di atas). */
    public void buka(String kunci) {
        Component c = halaman.get(kunci);
        if (c == null) {
            c = buatHalaman(kunci);
            halaman.put(kunci, c);
            konten.add(c, kunci);
        }
        aktif = kunci;
        for (Map.Entry<String, JButton> e : menu.entrySet()) {
            gayaMenu(e.getValue(), e.getKey().equals(kunci));
        }
        if (c instanceof Halaman) {
            ((Halaman) c).muatData();
        }
        kontenLayout.show(konten, kunci);
    }

    /** Buka halaman Lacak Batch langsung untuk batch tertentu. */
    public void lacakBatch(int batchId) {
        buka(LACAK);
        ((LacakBatch) halaman.get(LACAK)).lacak(batchId);
    }

    private Component buatHalaman(String kunci) {
        switch (kunci) {
            case DASHBOARD:
                return new Dashboard(this);
            case NOTIFIKASI:
                return new PusatNotifikasi(this);
            case MONITOR_STOK:
                return new MonitorStok(this);
            case MONITOR_EXP:
                return new MonitorKedaluwarsa(this);
            case LACAK:
                return new LacakBatch(this);
            case BARANG_MASUK:
                return new BarangMasuk(this);
            case KEMAS_ULANG:
                return new KemasUlang(this);
            case BARANG_KELUAR:
                return new BarangKeluar(this);
            case DATA_BARANG:
                return new DataBarang(this);
            case DATA_SUPPLIER:
                return new DataSupplier(this);
            case DATA_CUSTOMER:
                return new DataCustomer(this);
            case DATA_USER:
                return new DataUser(this);
            case LAPORAN:
                return new MenuLaporan(this);
            default:
                throw new IllegalArgumentException(kunci);
        }
    }

    // ------------------------------------------------------------------ kerangka

    private JPanel buatShell() {
        JPanel shell = new JPanel(new BorderLayout());
        shell.add(buatSidebar(), BorderLayout.WEST);
        JPanel kanan = new JPanel(new BorderLayout());
        kanan.add(buatHeader(), BorderLayout.NORTH);
        konten.setBackground(Theme.BG);
        kanan.add(konten, BorderLayout.CENTER);
        shell.add(kanan, BorderLayout.CENTER);
        return shell;
    }

    private JPanel buatSidebar() {
        JPanel side = new JPanel(new BorderLayout());
        side.setBackground(Theme.SIDEBAR);
        side.setPreferredSize(new Dimension(256, 0));

        // logo
        JPanel brand = new JPanel(new BorderLayout(10, 0));
        brand.setOpaque(false);
        brand.setBorder(BorderFactory.createEmptyBorder(22, 20, 18, 16));
        JLabel logo = new JLabel(new javax.swing.ImageIcon(Notifikasi.ikonTray()));
        JLabel nama = new JLabel("<html><b>" + Theme.NAMA_APP + "</b><br><span style='font-size:9px;color:#8FA89A'>"
                + Theme.NAMA_PT + "</span></html>");
        nama.setForeground(Color.WHITE);
        nama.setFont(Theme.font(Font.PLAIN, 14f));
        brand.add(logo, BorderLayout.WEST);
        brand.add(nama, BorderLayout.CENTER);
        side.add(brand, BorderLayout.NORTH);

        JPanel list = new JPanel();
        list.setOpaque(false);
        list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));
        list.setBorder(BorderFactory.createEmptyBorder(4, 12, 12, 12));

        seksi(list, "UTAMA");
        item(list, DASHBOARD, "Dashboard", "dashboard");
        item(list, NOTIFIKASI, "Notifikasi", "lonceng");
        seksi(list, "MONITORING");
        item(list, MONITOR_STOK, "Monitoring Stok", "stok");
        item(list, MONITOR_EXP, "Monitoring Kedaluwarsa", "jam");
        item(list, LACAK, "Lacak Batch", "lacak");
        seksi(list, "TRANSAKSI");
        item(list, BARANG_MASUK, "Barang Masuk", "masuk");
        item(list, KEMAS_ULANG, "Kemas Ulang", "kemas");
        item(list, BARANG_KELUAR, "Barang Keluar", "keluar");
        seksi(list, "MASTER DATA");
        item(list, DATA_BARANG, "Data Barang", "barang");
        item(list, DATA_SUPPLIER, "Data Supplier", "supplier");
        item(list, DATA_CUSTOMER, "Data Customer", "customer");
        item(list, DATA_USER, "Data Pengguna", "user");
        seksi(list, "LAPORAN");
        item(list, LAPORAN, "Laporan", "laporan");
        list.add(Box.createVerticalGlue());

        JScrollPane sp = new JScrollPane(list);
        sp.setBorder(null);
        sp.setOpaque(false);
        sp.getViewport().setOpaque(false);
        sp.getVerticalScrollBar().setUnitIncrement(16);
        sp.getVerticalScrollBar().putClientProperty(FlatClientProperties.STYLE, "track:#1C2B24; thumb:#33493D; width:6");
        side.add(sp, BorderLayout.CENTER);

        JLabel versi = new JLabel("v2.0 • Java Swing");
        versi.setForeground(new Color(0x5E7568));
        versi.setFont(Theme.font(Font.PLAIN, 12f));
        versi.setBorder(BorderFactory.createEmptyBorder(10, 24, 16, 10));
        side.add(versi, BorderLayout.SOUTH);
        return side;
    }

    private void seksi(JPanel list, String judul) {
        JLabel l = new JLabel(judul);
        l.setForeground(new Color(0x8FA89A));
        l.setFont(Theme.font(Font.BOLD, 11.5f));
        l.setBorder(BorderFactory.createEmptyBorder(10, 12, 4, 0));
        l.setAlignmentX(0f);
        list.add(l);
        seksi.put(judul, l);
    }

    private void item(JPanel list, String kunci, String teks, String ikon) {
        JButton b = new JButton(teks, new Ikon(ikon, 18, Theme.SIDEBAR_TEXT));
        b.setHorizontalAlignment(SwingConstants.LEFT);
        b.setIconTextGap(12);
        b.setAlignmentX(0f);
        b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        b.setPreferredSize(new Dimension(200, 36));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setFocusPainted(false);
        b.addActionListener(e -> buka(kunci));
        gayaMenu(b, false);
        menu.put(kunci, b);
        list.add(b);
        list.add(Box.createVerticalStrut(2));
    }

    private void gayaMenu(JButton b, boolean aktif) {
        Ikon ik = (Ikon) b.getIcon();
        if (aktif) {
            b.putClientProperty(FlatClientProperties.STYLE,
                    "background:#2E7D5B; foreground:#FFFFFF; borderWidth:0; focusWidth:0; innerFocusWidth:0; arc:10;"
                    + "hoverBackground:#2E7D5B; pressedBackground:#22624A; margin:6,14,6,14");
            b.setFont(Theme.font(Font.BOLD, 14f));
            ik.setColor(Color.WHITE);
        } else {
            b.putClientProperty(FlatClientProperties.STYLE,
                    "background:#1C2B24; foreground:#D3DED8; borderWidth:0; focusWidth:0; innerFocusWidth:0; arc:10;"
                    + "hoverBackground:#26392F; pressedBackground:#2C4337; margin:6,14,6,14");
            b.setFont(Theme.font(Font.PLAIN, 14f));
            ik.setColor(Theme.SIDEBAR_TEXT);
        }
        b.repaint();
    }

    private JPanel buatHeader() {
        JPanel h = new JPanel(new BorderLayout());
        h.setBackground(Color.WHITE);
        h.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.BORDER),
                BorderFactory.createEmptyBorder(10, 28, 10, 20)));

        JLabel kiri = new JLabel(Theme.NAMA_PT + "   •   " + Theme.hariIni());
        Theme.muted(kiri);
        kiri.setFont(Theme.font(Font.PLAIN, 14f));
        h.add(kiri, BorderLayout.WEST);

        JPanel kanan = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        kanan.setOpaque(false);

        bell = new BellButton();
        bell.setToolTipText("Notifikasi");
        bell.addActionListener(e -> buka(NOTIFIKASI));

        lblAvatar = new JLabel("A", SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Theme.PRIMARY_SOFT);
                g2.fillOval(0, 0, getWidth() - 1, getHeight() - 1);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        lblAvatar.setPreferredSize(new Dimension(36, 36));
        lblAvatar.setForeground(Theme.PRIMARY_DARK);
        lblAvatar.setFont(Theme.font(Font.BOLD, 13f));

        lblNama = new JLabel("-");
        lblNama.setFont(Theme.font(Font.BOLD, 14f));
        lblNama.setForeground(Theme.TEXT);
        lblRole = new JLabel("-");
        Theme.muted(lblRole);
        JPanel user = new JPanel();
        user.setOpaque(false);
        user.setLayout(new BoxLayout(user, BoxLayout.Y_AXIS));
        user.add(lblNama);
        user.add(lblRole);

        JButton out = new JButton(new Ikon("keluar_app", 18, Theme.MUTED));
        out.setToolTipText("Logout");
        out.putClientProperty(FlatClientProperties.BUTTON_TYPE, FlatClientProperties.BUTTON_TYPE_TOOLBAR_BUTTON);
        out.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        out.addActionListener(e -> logout());

        JPanel sep = new JPanel();
        sep.setBackground(Theme.BORDER);
        sep.setPreferredSize(new Dimension(1, 28));

        kanan.add(bell);
        kanan.add(sep);
        kanan.add(lblAvatar);
        kanan.add(user);
        kanan.add(out);
        h.add(kanan, BorderLayout.EAST);
        return h;
    }

    private static String inisial(String nama) {
        String[] p = nama.trim().split("\\s+");
        String s = p[0].substring(0, 1);
        if (p.length > 1) {
            s += p[1].substring(0, 1);
        }
        return s.toUpperCase();
    }

    /** Tombol lonceng dengan badge jumlah notifikasi. */
    static class BellButton extends JButton {

        private int jumlah;

        BellButton() {
            super(new Ikon("lonceng", 22, Theme.TEXT));
            putClientProperty(FlatClientProperties.BUTTON_TYPE, FlatClientProperties.BUTTON_TYPE_TOOLBAR_BUTTON);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setPreferredSize(new Dimension(42, 40));
        }

        void setJumlah(int n) {
            jumlah = n;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (jumlah <= 0) {
                return;
            }
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            String t = jumlah > 99 ? "99+" : String.valueOf(jumlah);
            g2.setFont(Theme.font(Font.BOLD, 10f));
            int w = Math.max(17, g2.getFontMetrics().stringWidth(t) + 8);
            int x = getWidth() - w - 2;
            g2.setColor(Theme.DANGER);
            g2.fillRoundRect(x, 2, w, 17, 17, 17);
            g2.setColor(Color.WHITE);
            g2.drawString(t, x + (w - g2.getFontMetrics().stringWidth(t)) / 2, 15);
            g2.dispose();
        }
    }

    /** Untuk halaman yang perlu daftar notifikasi terkini. */
    public List<Notifikasi.Item> notifikasiTerakhir() {
        return Notifikasi.terakhir();
    }
}
