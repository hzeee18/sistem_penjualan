package sistempenjualan;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.JPanel;

/**
 * JPanel dengan sudut membulat + garis tepi tipis. Dipakai sebagai "kartu".
 *
 * Di NetBeans, panel ini dipasang lewat Properties > Code > Custom Creation
 * Code: new sistempenjualan.RoundedPanel()
 */
public class RoundedPanel extends JPanel {

    private int arc = 16;
    private Color accent;
    private Color borderColor = Theme.BORDER;

    public RoundedPanel() {
        setOpaque(false);
        setBackground(Color.WHITE);
    }

    public void setArc(int arc) {
        this.arc = arc;
        repaint();
    }

    /** Warna strip tipis di sisi kiri kartu (null = tanpa strip). */
    public void setAccent(Color accent) {
        this.accent = accent;
        repaint();
    }

    public void setBorderColor(Color c) {
        this.borderColor = c;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int w = getWidth() - 1;
        int h = getHeight() - 1;
        g2.setColor(getBackground());
        g2.fillRoundRect(0, 0, w, h, arc, arc);
        if (accent != null) {
            g2.setClip(new java.awt.geom.RoundRectangle2D.Float(0, 0, w, h, arc, arc));
            g2.setColor(accent);
            g2.fillRect(0, 0, 4, h + 1);
            g2.setClip(null);
        }
        if (borderColor != null) {
            g2.setColor(borderColor);
            g2.drawRoundRect(0, 0, w, h, arc, arc);
        }
        g2.dispose();
        super.paintComponent(g);
    }
}
