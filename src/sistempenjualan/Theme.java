package sistempenjualan;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.FlatLightLaf;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

/**
 * Semua warna, font, dan gaya tampilan dikumpulkan di sini supaya seragam.
 * Kalau mau ganti warna aplikasi, cukup ubah konstanta di kelas ini.
 */
public final class Theme {

    private Theme() {
    }

    // ---- palet warna (nuansa hijau)
    public static final Color PRIMARY = new Color(0x2E7D5B);
    public static final Color PRIMARY_DARK = new Color(0x22624A);
    public static final Color PRIMARY_SOFT = new Color(0xE3F1EA);
    public static final Color ACCENT = new Color(0x8B5E3C);
    public static final Color SIDEBAR = new Color(0x1C2B24);
    public static final Color SIDEBAR_HOVER = new Color(0x26392F);
    public static final Color SIDEBAR_TEXT = new Color(0xD3DED8);
    public static final Color BG = new Color(0xF3F5F4);
    public static final Color CARD = Color.WHITE;
    public static final Color BORDER = new Color(0xE3E7E5);
    public static final Color TEXT = new Color(0x1F2937);
    public static final Color MUTED = new Color(0x5F6B7A);

    public static final Color DANGER = new Color(0xDC2626);
    public static final Color DANGER_SOFT = new Color(0xFDE8E8);
    public static final Color WARNING = new Color(0xEA580C);
    public static final Color WARNING_SOFT = new Color(0xFFEDD5);
    public static final Color CAUTION = new Color(0xB7791F);
    public static final Color CAUTION_SOFT = new Color(0xFEF6D8);
    public static final Color SUCCESS = new Color(0x15803D);
    public static final Color SUCCESS_SOFT = new Color(0xDCFCE7);
    public static final Color INFO = new Color(0x2563EB);
    public static final Color INFO_SOFT = new Color(0xDBEAFE);

    public static final String NAMA_APP = "Monitoring Persediaan";
    public static final String NAMA_PT = "PT Mitra Tiga Sepakat";

    /** Dipanggil sekali di main() sebelum jendela dibuat. */
    public static void setup() {
        // Font Inter dibundel (lib/flatlaf-fonts-inter) supaya tampilan sama di semua laptop
        com.formdev.flatlaf.fonts.inter.FlatInterFont.install();
        com.formdev.flatlaf.fonts.inter.FlatInterFont.installSemiBold();
        com.formdev.flatlaf.FlatLaf.setPreferredFontFamily(com.formdev.flatlaf.fonts.inter.FlatInterFont.FAMILY);
        com.formdev.flatlaf.FlatLaf.setPreferredSemiboldFontFamily(com.formdev.flatlaf.fonts.inter.FlatInterFont.FAMILY_SEMIBOLD);
        FlatLightLaf.setup();
        UIManager.put("defaultFont", new javax.swing.plaf.FontUIResource(com.formdev.flatlaf.fonts.inter.FlatInterFont.FAMILY, Font.PLAIN, 13));
        UIManager.put("Component.arc", 10);
        UIManager.put("Button.arc", 10);
        UIManager.put("TextComponent.arc", 10);
        UIManager.put("Component.focusWidth", 1);
        UIManager.put("Component.focusColor", PRIMARY_SOFT);
        UIManager.put("Component.focusedBorderColor", PRIMARY);
        UIManager.put("Component.borderColor", BORDER);
        UIManager.put("Button.default.background", PRIMARY);
        UIManager.put("Button.default.foreground", Color.WHITE);
        java.awt.Insets pad = new java.awt.Insets(6, 10, 6, 10);
        UIManager.put("TextField.margin", pad);
        UIManager.put("PasswordField.margin", pad);
        UIManager.put("FormattedTextField.margin", pad);
        UIManager.put("ComboBox.padding", new java.awt.Insets(5, 8, 5, 8));
        UIManager.put("Spinner.padding", new java.awt.Insets(5, 8, 5, 8));
        UIManager.put("ScrollBar.width", 10);
        UIManager.put("ScrollBar.thumbArc", 999);
        UIManager.put("ScrollBar.thumbInsets", new java.awt.Insets(2, 2, 2, 2));
        UIManager.put("ScrollBar.track", BG);
        UIManager.put("Table.selectionBackground", PRIMARY_SOFT);
        UIManager.put("Table.selectionForeground", TEXT);
        UIManager.put("Table.alternateRowColor", new Color(0xFAFBFA));
        UIManager.put("TableHeader.background", new Color(0xF6F8F7));
        UIManager.put("TableHeader.separatorColor", BORDER);
        UIManager.put("TableHeader.bottomSeparatorColor", BORDER);
        UIManager.put("Panel.background", BG);
        UIManager.put("OptionPane.background", Color.WHITE);
        UIManager.put("TitlePane.background", Color.WHITE);
        UIManager.put("ComboBox.selectionBackground", PRIMARY_SOFT);
        UIManager.put("ComboBox.selectionForeground", TEXT);
        UIManager.put("List.selectionBackground", PRIMARY_SOFT);
        UIManager.put("List.selectionForeground", TEXT);
    }

    // ---- font
    public static Font font(int style, float size) {
        // teks tebal ukuran kecil/sedang pakai Inter SemiBold supaya tidak terlalu "berat" di mata
        if (style == Font.BOLD && size < 20f) {
            return new Font(com.formdev.flatlaf.fonts.inter.FlatInterFont.FAMILY_SEMIBOLD, Font.PLAIN, 1).deriveFont(size);
        }
        Font f = UIManager.getFont("defaultFont");
        if (f == null) {
            f = new Font("Dialog", Font.PLAIN, 14);
        }
        return f.deriveFont(style, size);
    }

    // ---- komponen halaman
    public static void page(JPanel p) {
        p.setBackground(BG);
        p.setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));
    }

    public static void title(JLabel l) {
        l.setFont(font(Font.BOLD, 21f));
        l.setForeground(TEXT);
    }

    public static void subtitle(JLabel l) {
        l.setFont(font(Font.PLAIN, 12f));
        l.setForeground(MUTED);
    }

    public static void section(JLabel l) {
        l.setFont(font(Font.BOLD, 14.5f));
        l.setForeground(TEXT);
    }

    public static void label(JLabel l) {
        l.setFont(font(Font.BOLD, 11f));
        l.setForeground(new Color(0x374151));
    }

    public static void muted(JLabel l) {
        l.setFont(font(Font.PLAIN, 12f));
        l.setForeground(MUTED);
    }

    /** Panel kartu putih dengan sudut membulat. */
    public static void card(JPanel p) {
        card(p, 18);
    }

    public static void card(JPanel p, int pad) {
        p.setBackground(CARD);
        p.setOpaque(false);
        p.setBorder(BorderFactory.createEmptyBorder(pad, pad, pad, pad));
    }

    public static void transparent(JComponent... cs) {
        for (JComponent c : cs) {
            c.setOpaque(false);
        }
    }

    /**
     * Kartu ringkasan (angka besar). accent = warna strip & angka.
     */
    public static void statCard(JPanel card, JLabel judul, JLabel nilai, JLabel ket, Color accent) {
        card(card, 16);
        if (card instanceof RoundedPanel) {
            ((RoundedPanel) card).setAccent(accent);
        }
        judul.setFont(font(Font.BOLD, 11.5f));
        judul.setForeground(MUTED);
        nilai.setFont(font(Font.BOLD, 25f));
        nilai.setForeground(accent);
        ket.setFont(font(Font.PLAIN, 12f));
        ket.setForeground(MUTED);
        card.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
    }

    // ---- tombol
    private static void btn(JButton b, String style) {
        b.putClientProperty(FlatClientProperties.STYLE, style);
        b.setFont(font(Font.BOLD, 13f));
        b.setFocusPainted(false);
        b.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
        b.setMargin(new java.awt.Insets(7, 16, 7, 16));
    }

    public static void primary(JButton... bs) {
        for (JButton b : bs) {
            btn(b, "background:#2E7D5B; foreground:#FFFFFF; borderWidth:0; focusWidth:0; innerFocusWidth:0;"
                    + "hoverBackground:#22624A; pressedBackground:#1B4F3B; disabledText:#E5E7EB");
        }
    }

    public static void secondary(JButton... bs) {
        for (JButton b : bs) {
            btn(b, "background:#FFFFFF; foreground:#1F2937; borderColor:#D6DCD9; focusWidth:0; innerFocusWidth:0;"
                    + "hoverBackground:#F3F5F4; pressedBackground:#E6EAE8");
        }
    }

    public static void danger(JButton... bs) {
        for (JButton b : bs) {
            btn(b, "background:#FFFFFF; foreground:#DC2626; borderColor:#F5C2C2; focusWidth:0; innerFocusWidth:0;"
                    + "hoverBackground:#FDE8E8; pressedBackground:#FBD5D5");
        }
    }

    public static void placeholder(javax.swing.JTextField f, String text) {
        f.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, text);
    }

    public static void search(javax.swing.JTextField f, String text) {
        placeholder(f, text);
        f.putClientProperty(FlatClientProperties.TEXT_FIELD_LEADING_ICON, new SearchIcon());
        f.putClientProperty(FlatClientProperties.TEXT_FIELD_SHOW_CLEAR_BUTTON, true);
    }

    public static void dateChooser(com.toedter.calendar.JDateChooser d) {
        d.setDateFormatString("dd MMM yyyy");
        d.setLocale(ID);
        JButton b = d.getCalendarButton();
        b.setIcon(new Ikon("kalender", 16, MUTED));
        b.setText(null);
        b.putClientProperty(FlatClientProperties.BUTTON_TYPE, FlatClientProperties.BUTTON_TYPE_TOOLBAR_BUTTON);
        b.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
    }

    // ---- tabel
    public static DefaultTableModel model(String... kolom) {
        return new DefaultTableModel(kolom, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }

            @Override
            public Class<?> getColumnClass(int c) {
                for (int r = 0; r < getRowCount(); r++) {
                    Object v = getValueAt(r, c);
                    if (v != null) {
                        return v.getClass();
                    }
                }
                return Object.class;
            }
        };
    }

    public static void table(JTable t) {
        t.setRowHeight(32);
        t.setShowVerticalLines(false);
        t.setShowHorizontalLines(true);
        t.setGridColor(new Color(0xEEF1EF));
        t.setIntercellSpacing(new java.awt.Dimension(0, 1));
        t.setFillsViewportHeight(true);
        t.setAutoCreateRowSorter(true);
        t.setFont(font(Font.PLAIN, 13f));
        t.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        JTableHeader h = t.getTableHeader();
        h.setFont(font(Font.BOLD, 12f));
        h.setForeground(new Color(0x374151));
        h.setPreferredSize(new java.awt.Dimension(0, 34));
        h.setReorderingAllowed(false);
        t.setDefaultRenderer(Object.class, new CellRenderer());
        t.setDefaultRenderer(Number.class, new CellRenderer());
        t.setDefaultRenderer(Double.class, new CellRenderer());
        t.setDefaultRenderer(Integer.class, new CellRenderer());
        t.setDefaultRenderer(Long.class, new CellRenderer());
        t.setDefaultRenderer(java.math.BigDecimal.class, new CellRenderer());
        t.setDefaultRenderer(java.sql.Date.class, new CellRenderer());
        t.setDefaultRenderer(java.util.Date.class, new CellRenderer());
        t.setDefaultRenderer(java.sql.Timestamp.class, new CellRenderer());
        if (t.getParent() != null && t.getParent().getParent() instanceof JScrollPane) {
            JScrollPane sp = (JScrollPane) t.getParent().getParent();
            sp.setBorder(BorderFactory.createLineBorder(BORDER));
            sp.getViewport().setBackground(Color.WHITE);
        }
    }

    /** Kolom dengan tampilan "pil" berwarna sesuai status. */
    public static void statusColumn(JTable t, int col) {
        t.getColumnModel().getColumn(col).setCellRenderer(new StatusRenderer());
    }

    public static void widths(JTable t, int... w) {
        for (int i = 0; i < w.length && i < t.getColumnCount(); i++) {
            if (w[i] > 0) {
                t.getColumnModel().getColumn(i).setPreferredWidth(w[i]);
            }
        }
    }

    /** Pilih warna untuk teks status. */
    public static Color[] statusColor(String s) {
        if (s == null) {
            return new Color[]{MUTED, BG};
        }
        String u = s.toUpperCase(Locale.ROOT);
        if (u.contains("SUDAH") || u.contains("HABIS") || u.equals("KEDALUWARSA") || u.contains("KRITIS") || u.contains("DIMUSNAHKAN")) {
            return new Color[]{DANGER, DANGER_SOFT};
        }
        if (u.contains("MENIPIS") || u.contains("SEGERA")) {
            return new Color[]{WARNING, WARNING_SOFT};
        }
        if (u.contains("PERINGATAN") || u.contains("MENDEKATI")) {
            return new Color[]{CAUTION, CAUTION_SOFT};
        }
        if (u.contains("AMAN") || u.contains("MASUK") || u.contains("HASIL")) {
            return new Color[]{SUCCESS, SUCCESS_SOFT};
        }
        if (u.contains("KELUAR") || u.contains("PAKAI") || u.contains("ADMIN")) {
            return new Color[]{INFO, INFO_SOFT};
        }
        return new Color[]{new Color(0x4B5563), new Color(0xEEF1EF)};
    }

    // ---- format angka & tanggal (bahasa Indonesia)
    public static final Locale ID = new Locale("id", "ID");
    private static final DecimalFormat ANGKA = new DecimalFormat("#,##0.##", DecimalFormatSymbols.getInstance(ID));

    public static String angka(Object n) {
        if (n == null) {
            return "0";
        }
        return ANGKA.format(((Number) n).doubleValue());
    }

    public static String tgl(Object d) {
        if (d == null) {
            return "-";
        }
        return new SimpleDateFormat("dd MMM yyyy", ID).format((Date) d);
    }

    public static String tglJam(Object d) {
        if (d == null) {
            return "-";
        }
        return new SimpleDateFormat("dd MMM yyyy HH:mm", ID).format((Date) d);
    }

    public static String hariIni() {
        return new SimpleDateFormat("EEEE, dd MMMM yyyy", ID).format(new Date());
    }

    // ---- renderer
    static class CellRenderer extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r, int c) {
            Object tampil = v;
            int align = SwingConstants.LEFT;
            if (v instanceof Number) {
                tampil = angka(v);
                align = SwingConstants.RIGHT;
            } else if (v instanceof java.sql.Timestamp) {
                tampil = tglJam(v);
            } else if (v instanceof Date) {
                tampil = tgl(v);
            }
            JLabel l = (JLabel) super.getTableCellRendererComponent(t, tampil, sel, false, r, c);
            l.setHorizontalAlignment(align);
            l.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
            return l;
        }
    }

    static class StatusRenderer extends DefaultTableCellRenderer {

        private Color fg = MUTED;
        private Color bg = BG;

        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r, int c) {
            JLabel l = (JLabel) super.getTableCellRendererComponent(t, v, sel, false, r, c);
            Color[] w = statusColor(v == null ? null : v.toString());
            fg = w[0];
            bg = w[1];
            l.setForeground(fg);
            l.setFont(font(Font.BOLD, 11f));
            l.setHorizontalAlignment(SwingConstants.CENTER);
            l.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
            return l;
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getBackground());
            g2.fillRect(0, 0, getWidth(), getHeight());
            int tw = getFontMetrics(getFont()).stringWidth(getText()) + 22;
            int w = Math.min(getWidth() - 8, tw);
            int h = 24;
            int x = (getWidth() - w) / 2;
            int y = (getHeight() - h) / 2;
            g2.setColor(bg);
            g2.fillRoundRect(x, y, w, h, h, h);
            g2.dispose();
            setOpaque(false);
            super.paintComponent(g);
            setOpaque(true);
        }
    }

    /** Ikon kaca pembesar kecil untuk kolom pencarian. */
    static class SearchIcon implements javax.swing.Icon {

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(MUTED);
            g2.setStroke(new java.awt.BasicStroke(1.6f));
            g2.drawOval(x + 1, y + 1, 10, 10);
            g2.drawLine(x + 10, y + 10, x + 14, y + 14);
            g2.dispose();
        }

        @Override
        public int getIconWidth() {
            return 16;
        }

        @Override
        public int getIconHeight() {
            return 16;
        }
    }
}
