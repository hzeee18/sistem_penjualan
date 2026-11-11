package sistempenjualan;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Path2D;
import javax.swing.Icon;

/**
 * Ikon garis sederhana yang digambar langsung (tanpa file gambar), supaya
 * project tidak butuh library ikon tambahan.
 */
public class Ikon implements Icon {

    private final String jenis;
    private final int size;
    private Color color;

    public Ikon(String jenis, int size, Color color) {
        this.jenis = jenis;
        this.size = size;
        this.color = color;
    }

    public void setColor(Color c) {
        this.color = c;
    }

    @Override
    public int getIconWidth() {
        return size;
    }

    @Override
    public int getIconHeight() {
        return size;
    }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        g2.translate(x, y);
        float s = size / 20f;
        g2.scale(s, s);
        g2.setColor(color);
        g2.setStroke(new BasicStroke(1.7f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        switch (jenis) {
            case "dashboard":
                g2.drawRoundRect(2, 2, 7, 7, 2, 2);
                g2.drawRoundRect(11, 2, 7, 7, 2, 2);
                g2.drawRoundRect(2, 11, 7, 7, 2, 2);
                g2.drawRoundRect(11, 11, 7, 7, 2, 2);
                break;
            case "stok": {
                Path2D p = new Path2D.Float();
                p.moveTo(10, 2);
                p.lineTo(18, 6);
                p.lineTo(18, 14);
                p.lineTo(10, 18);
                p.lineTo(2, 14);
                p.lineTo(2, 6);
                p.closePath();
                g2.draw(p);
                g2.drawLine(2, 6, 10, 10);
                g2.drawLine(18, 6, 10, 10);
                g2.drawLine(10, 10, 10, 18);
                break;
            }
            case "jam":
                g2.drawOval(2, 2, 16, 16);
                g2.drawLine(10, 5, 10, 10);
                g2.drawLine(10, 10, 14, 12);
                break;
            case "masuk":
                g2.drawRoundRect(2, 11, 16, 7, 3, 3);
                g2.drawLine(10, 2, 10, 12);
                g2.drawLine(6, 8, 10, 12);
                g2.drawLine(14, 8, 10, 12);
                break;
            case "kemas":
                g2.drawRoundRect(4, 7, 12, 11, 3, 3);
                g2.drawLine(7, 7, 7, 4);
                g2.drawLine(13, 7, 13, 4);
                g2.drawLine(7, 4, 13, 4);
                g2.drawLine(8, 12, 12, 12);
                break;
            case "keluar":
                g2.drawRoundRect(2, 11, 16, 7, 3, 3);
                g2.drawLine(10, 12, 10, 2);
                g2.drawLine(6, 6, 10, 2);
                g2.drawLine(14, 6, 10, 2);
                break;
            case "barang":
                g2.drawRoundRect(2, 3, 16, 14, 3, 3);
                g2.drawLine(2, 8, 18, 8);
                g2.drawLine(8, 12, 12, 12);
                break;
            case "supplier":
                g2.drawRect(2, 7, 10, 8);
                g2.drawLine(12, 10, 16, 10);
                g2.drawLine(16, 10, 18, 13);
                g2.drawLine(18, 13, 18, 15);
                g2.drawLine(12, 15, 18, 15);
                g2.drawOval(4, 14, 3, 3);
                g2.drawOval(13, 14, 3, 3);
                break;
            case "customer":
                g2.drawOval(6, 2, 8, 8);
                g2.drawArc(2, 11, 16, 14, 0, 180);
                break;
            case "user":
                g2.drawOval(3, 3, 6, 6);
                g2.drawArc(0, 11, 12, 12, 0, 180);
                g2.drawOval(12, 5, 5, 5);
                g2.drawArc(10, 12, 9, 10, 0, 180);
                break;
            case "laporan":
                g2.drawRoundRect(4, 2, 12, 16, 3, 3);
                g2.drawLine(7, 7, 13, 7);
                g2.drawLine(7, 10, 13, 10);
                g2.drawLine(7, 13, 11, 13);
                break;
            case "lonceng": {
                Path2D p = new Path2D.Float();
                p.moveTo(4, 14);
                p.curveTo(5, 12, 5, 11, 5, 8);
                p.curveTo(5, 4.5, 7.5, 3, 10, 3);
                p.curveTo(12.5, 3, 15, 4.5, 15, 8);
                p.curveTo(15, 11, 15, 12, 16, 14);
                p.closePath();
                g2.draw(p);
                g2.drawArc(8, 14, 4, 4, 180, 180);
                break;
            }
            case "keluar_app":
                g2.drawLine(8, 3, 3, 3);
                g2.drawLine(3, 3, 3, 17);
                g2.drawLine(3, 17, 8, 17);
                g2.drawLine(8, 10, 18, 10);
                g2.drawLine(14, 6, 18, 10);
                g2.drawLine(14, 14, 18, 10);
                break;
            case "lacak":
                g2.drawOval(2, 2, 5, 5);
                g2.drawOval(13, 8, 5, 5);
                g2.drawOval(2, 13, 5, 5);
                g2.drawLine(7, 5, 13, 9);
                g2.drawLine(7, 15, 13, 12);
                break;
            case "refresh":
                g2.drawArc(3, 3, 14, 14, 30, 270);
                g2.drawLine(17, 3, 17, 8);
                g2.drawLine(17, 8, 12, 8);
                break;
            case "kalender":
                g2.drawRoundRect(2, 4, 16, 14, 3, 3);
                g2.drawLine(2, 8, 18, 8);
                g2.drawLine(6, 2, 6, 5);
                g2.drawLine(14, 2, 14, 5);
                break;
            case "cetak":
                g2.drawRect(5, 2, 10, 5);
                g2.drawRoundRect(2, 7, 16, 7, 3, 3);
                g2.drawRect(5, 12, 10, 6);
                break;
            default:
                g2.drawOval(4, 4, 12, 12);
        }
        g2.dispose();
    }
}
