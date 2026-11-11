package sistempenjualan;

import java.util.List;
import javax.swing.JComboBox;

/**
 * Item untuk JComboBox: menyimpan id database + teks yang tampil.
 */
public class Pilihan {

    public final int id;
    public final String teks;
    public final Object[] data;

    public Pilihan(int id, String teks, Object... data) {
        this.id = id;
        this.teks = teks;
        this.data = data;
    }

    @Override
    public String toString() {
        return teks;
    }

    /** Isi combo dari query "SELECT id, teks, ... ". */
    public static void isi(JComboBox<Object> cb, String kosong, String sql, Object... args) throws Exception {
        cb.removeAllItems();
        if (kosong != null) {
            cb.addItem(new Pilihan(0, kosong));
        }
        List<Object[]> rows = Koneksi.rows(sql, args);
        for (Object[] r : rows) {
            Object[] sisa = new Object[r.length - 2];
            System.arraycopy(r, 2, sisa, 0, sisa.length);
            cb.addItem(new Pilihan(((Number) r[0]).intValue(), String.valueOf(r[1]), sisa));
        }
    }

    public static Pilihan dari(JComboBox<Object> cb) {
        Object o = cb.getSelectedItem();
        return (o instanceof Pilihan && ((Pilihan) o).id != 0) ? (Pilihan) o : null;
    }

    public static void pilih(JComboBox<Object> cb, int id) {
        for (int i = 0; i < cb.getItemCount(); i++) {
            Object o = cb.getItemAt(i);
            if (o instanceof Pilihan && ((Pilihan) o).id == id) {
                cb.setSelectedIndex(i);
                return;
            }
        }
        if (cb.getItemCount() > 0) {
            cb.setSelectedIndex(0);
        }
    }
}
