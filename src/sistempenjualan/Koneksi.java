package sistempenjualan;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Koneksi database + migrasi tabel + data contoh.
 *
 * Database dibuat otomatis (db_monitoring_persediaan) saat aplikasi pertama
 * kali dijalankan. Cukup nyalakan MySQL/MariaDB (XAMPP) dengan user root tanpa
 * password, atau ubah konstanta di bawah.
 */
public class Koneksi {

    public static final String HOST = "localhost:3306";
    public static final String DB = "db_monitoring_persediaan";
    public static final String USER = "root";
    public static final String PASS = "";

    private static final String OPSI = "?useSSL=false&characterEncoding=UTF-8&useUnicode=true&zeroDateTimeBehavior=convertToNull";

    private static Connection con;

    /** Dipanggil sekali saat aplikasi mulai. */
    public static void koneksi() throws Exception {
        Class.forName("com.mysql.jdbc.Driver");
        try (Connection c = DriverManager.getConnection("jdbc:mysql://" + HOST + "/" + OPSI, USER, PASS);
                Statement s = c.createStatement()) {
            s.executeUpdate("CREATE DATABASE IF NOT EXISTS " + DB);
        }
        get();
        migrate();
        seed();
    }

    /** Koneksi bersama, otomatis tersambung ulang kalau terputus. */
    public static Connection get() throws Exception {
        if (con == null || con.isClosed() || !con.isValid(2)) {
            Class.forName("com.mysql.jdbc.Driver");
            con = DriverManager.getConnection("jdbc:mysql://" + HOST + "/" + DB + OPSI, USER, PASS);
        }
        return con;
    }

    // ------------------------------------------------------------------ helper query

    private static PreparedStatement siapkan(String sql, Object... args) throws Exception {
        PreparedStatement ps = get().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        for (int i = 0; i < args.length; i++) {
            Object a = args[i];
            if (a instanceof java.util.Date && !(a instanceof java.sql.Date) && !(a instanceof java.sql.Timestamp)) {
                a = new java.sql.Date(((java.util.Date) a).getTime());
            }
            ps.setObject(i + 1, a);
        }
        return ps;
    }

    /** SELECT banyak baris, tiap baris berupa Object[]. */
    public static List<Object[]> rows(String sql, Object... args) throws Exception {
        List<Object[]> hasil = new ArrayList<>();
        try (PreparedStatement ps = siapkan(sql, args); ResultSet rs = ps.executeQuery()) {
            ResultSetMetaData md = rs.getMetaData();
            int n = md.getColumnCount();
            while (rs.next()) {
                Object[] r = new Object[n];
                for (int i = 0; i < n; i++) {
                    r[i] = rs.getObject(i + 1);
                }
                hasil.add(r);
            }
        }
        return hasil;
    }

    /** SELECT satu baris (null kalau kosong). */
    public static Object[] one(String sql, Object... args) throws Exception {
        List<Object[]> r = rows(sql, args);
        return r.isEmpty() ? null : r.get(0);
    }

    /** SELECT satu angka. */
    public static double number(String sql, Object... args) throws Exception {
        Object[] r = one(sql, args);
        return (r == null || r[0] == null) ? 0 : ((Number) r[0]).doubleValue();
    }

    /** INSERT/UPDATE/DELETE, mengembalikan id baru (kalau INSERT) atau jumlah baris. */
    public static long exec(String sql, Object... args) throws Exception {
        try (PreparedStatement ps = siapkan(sql, args)) {
            int n = ps.executeUpdate();
            try (ResultSet k = ps.getGeneratedKeys()) {
                if (k.next()) {
                    return k.getLong(1);
                }
            }
            return n;
        }
    }

    public static void begin() throws Exception {
        get().setAutoCommit(false);
    }

    public static void commit() throws Exception {
        get().commit();
        get().setAutoCommit(true);
    }

    public static void rollback() {
        try {
            get().rollback();
            get().setAutoCommit(true);
        } catch (Exception ignored) {
        }
    }

    // ------------------------------------------------------------------ migrasi

    private static void migrate() throws Exception {
        String[] ddl = {
            "CREATE TABLE IF NOT EXISTS m_user ("
            + " id INT AUTO_INCREMENT PRIMARY KEY,"
            + " username VARCHAR(50) NOT NULL UNIQUE,"
            + " password VARCHAR(255) NOT NULL,"
            + " nama_lengkap VARCHAR(100) NOT NULL,"
            + " role ENUM('admin','gudang') NOT NULL DEFAULT 'gudang')",
            "CREATE TABLE IF NOT EXISTS m_supplier ("
            + " id INT AUTO_INCREMENT PRIMARY KEY,"
            + " nama VARCHAR(100) NOT NULL,"
            + " pic VARCHAR(100),"
            + " no_hp VARCHAR(20),"
            + " email VARCHAR(100),"
            + " alamat TEXT)",
            "CREATE TABLE IF NOT EXISTS m_customer ("
            + " id INT AUTO_INCREMENT PRIMARY KEY,"
            + " nama VARCHAR(100) NOT NULL,"
            + " no_hp VARCHAR(20),"
            + " alamat TEXT)",
            "CREATE TABLE IF NOT EXISTS m_barang ("
            + " id INT AUTO_INCREMENT PRIMARY KEY,"
            + " kode VARCHAR(20) NOT NULL UNIQUE,"
            + " nama VARCHAR(100) NOT NULL,"
            + " kategori ENUM('BAHAN_BAKU','PRODUK_JADI') NOT NULL,"
            + " satuan VARCHAR(20) NOT NULL,"
            + " isi_gram DECIMAL(10,2) NULL COMMENT 'isi per kemasan (produk jadi)',"
            + " bahan_baku_id INT NULL COMMENT 'bahan baku yang dikemas ulang',"
            + " supplier_id INT NULL,"
            + " stok_minimum DECIMAL(12,2) NOT NULL DEFAULT 0,"
            + " umur_simpan_hari INT NOT NULL DEFAULT 365,"
            + " FOREIGN KEY (bahan_baku_id) REFERENCES m_barang(id),"
            + " FOREIGN KEY (supplier_id) REFERENCES m_supplier(id))",
            "CREATE TABLE IF NOT EXISTS batch ("
            + " id INT AUTO_INCREMENT PRIMARY KEY,"
            + " no_batch VARCHAR(30) NOT NULL UNIQUE,"
            + " barang_id INT NOT NULL,"
            + " sumber ENUM('PEMBELIAN','KEMAS_ULANG') NOT NULL,"
            + " supplier_id INT NULL,"
            + " qty_awal DECIMAL(12,2) NOT NULL,"
            + " qty_sisa DECIMAL(12,2) NOT NULL,"
            + " tgl_masuk DATE NOT NULL,"
            + " tgl_kedaluwarsa DATE NOT NULL,"
            + " keterangan VARCHAR(255),"
            + " created_by INT NOT NULL,"
            + " created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,"
            + " FOREIGN KEY (barang_id) REFERENCES m_barang(id),"
            + " FOREIGN KEY (supplier_id) REFERENCES m_supplier(id),"
            + " FOREIGN KEY (created_by) REFERENCES m_user(id))",
            "CREATE TABLE IF NOT EXISTS mutasi_stok ("
            + " id INT AUTO_INCREMENT PRIMARY KEY,"
            + " batch_id INT NOT NULL,"
            + " jenis ENUM('MASUK','KELUAR','KEMAS_PAKAI','KEMAS_HASIL','PEMUSNAHAN') NOT NULL,"
            + " qty DECIMAL(12,2) NOT NULL,"
            + " no_referensi VARCHAR(30),"
            + " keterangan VARCHAR(255),"
            + " created_by INT NOT NULL,"
            + " created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,"
            + " FOREIGN KEY (batch_id) REFERENCES batch(id),"
            + " FOREIGN KEY (created_by) REFERENCES m_user(id))",
            "CREATE TABLE IF NOT EXISTS kemas_ulang ("
            + " id INT AUTO_INCREMENT PRIMARY KEY,"
            + " no_dokumen VARCHAR(30) NOT NULL UNIQUE,"
            + " produk_id INT NOT NULL,"
            + " qty_hasil DECIMAL(12,2) NOT NULL,"
            + " batch_hasil_id INT NOT NULL,"
            + " catatan VARCHAR(255),"
            + " created_by INT NOT NULL,"
            + " created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,"
            + " FOREIGN KEY (produk_id) REFERENCES m_barang(id),"
            + " FOREIGN KEY (batch_hasil_id) REFERENCES batch(id),"
            + " FOREIGN KEY (created_by) REFERENCES m_user(id))",
            "CREATE TABLE IF NOT EXISTS kemas_ulang_detail ("
            + " id INT AUTO_INCREMENT PRIMARY KEY,"
            + " kemas_ulang_id INT NOT NULL,"
            + " batch_id INT NOT NULL,"
            + " qty_pakai DECIMAL(12,2) NOT NULL,"
            + " FOREIGN KEY (kemas_ulang_id) REFERENCES kemas_ulang(id),"
            + " FOREIGN KEY (batch_id) REFERENCES batch(id))",
            "CREATE TABLE IF NOT EXISTS barang_keluar ("
            + " id INT AUTO_INCREMENT PRIMARY KEY,"
            + " no_dokumen VARCHAR(30) NOT NULL UNIQUE,"
            + " customer_id INT NOT NULL,"
            + " tanggal DATE NOT NULL,"
            + " catatan VARCHAR(255),"
            + " created_by INT NOT NULL,"
            + " created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,"
            + " FOREIGN KEY (customer_id) REFERENCES m_customer(id),"
            + " FOREIGN KEY (created_by) REFERENCES m_user(id))",
            "CREATE TABLE IF NOT EXISTS barang_keluar_detail ("
            + " id INT AUTO_INCREMENT PRIMARY KEY,"
            + " barang_keluar_id INT NOT NULL,"
            + " batch_id INT NOT NULL,"
            + " qty DECIMAL(12,2) NOT NULL,"
            + " FOREIGN KEY (barang_keluar_id) REFERENCES barang_keluar(id),"
            + " FOREIGN KEY (batch_id) REFERENCES batch(id))",
            "CREATE TABLE IF NOT EXISTS pengaturan ("
            + " kunci VARCHAR(50) PRIMARY KEY,"
            + " nilai VARCHAR(100) NOT NULL)"
        };
        try (Statement s = get().createStatement()) {
            for (String q : ddl) {
                s.executeUpdate(q);
            }
            s.executeUpdate("INSERT IGNORE INTO pengaturan VALUES "
                    + "('batas_peringatan_hari','30'),('batas_kritis_hari','7'),"
                    + "('interval_cek_menit','5'),('notifikasi_windows','1')");
        }
    }

    // ------------------------------------------------------------------ data contoh

    private static void seed() throws Exception {
        if (number("SELECT COUNT(*) FROM m_user") > 0) {
            return;
        }
        String[] data = {
            "INSERT INTO m_user (username, password, nama_lengkap, role) VALUES "
            + "('admin','admin123','Administrator','admin'),"
            + "('gudang','gudang123','Rina Kusuma','gudang'),"
            + "('doni','gudang123','Doni Pratama','gudang')",
            "INSERT INTO m_supplier (nama, pic, no_hp, email, alamat) VALUES "
            + "('PT Dairy Ingredients Nusantara','Hendra','021-5551001','hendra@dairyingredients.co.id','Kawasan Industri Pulogadung, Jakarta Timur'),"
            + "('PT Sumber Bahan Pangan','Dewi','021-5552002','dewi@sumberbahanpangan.co.id','Jl. Industri Raya No. 45, Bekasi'),"
            + "('CV Kimia Pangan Lestari','Roni','021-5553003','roni@kimiapangan.id','Jl. Gatot Subroto No. 9, Tangerang'),"
            + "('PT Kelapa Prima Indonesia','Linda','0813-2211-7788','linda@kelapaprima.co.id','Jl. Ahmad Yani No. 22, Bandung')",
            "INSERT INTO m_customer (nama, no_hp, alamat) VALUES "
            + "('CV Roti Mekar Sari','0812-1111-2222','Jl. Melati No. 5, Bandung'),"
            + "('PT Snack Gurih Nusantara','0813-3333-4444','Kawasan Industri Cikarang, Bekasi'),"
            + "('Kedai Minuman Segar','0815-5555-6666','Jl. Anggrek No. 3, Bekasi'),"
            + "('UD Kue Basah Ibu Ani','0817-7777-8888','Jl. Dahlia No. 17, Depok'),"
            + "('PT Minuman Serbuk Jaya','0819-9999-0000','Jl. Raya Serang KM 12, Tangerang')",
            // bahan baku curah (karung 25 kg, satuan kg)
            "INSERT INTO m_barang (kode, nama, kategori, satuan, supplier_id, stok_minimum, umur_simpan_hari) VALUES "
            + "('BB-001','Skim Milk Powder','BAHAN_BAKU','kg',1,50,540),"
            + "('BB-002','Full Cream Milk Powder','BAHAN_BAKU','kg',1,50,540),"
            + "('BB-003','Non Dairy Creamer','BAHAN_BAKU','kg',2,50,450),"
            + "('BB-004','Whey Powder','BAHAN_BAKU','kg',1,25,540),"
            + "('BB-005','Coconut Cream Powder','BAHAN_BAKU','kg',4,25,365),"
            + "('BB-006','Sucralose','BAHAN_BAKU','kg',2,5,730),"
            + "('BB-007','Trisodium Citrate','BAHAN_BAKU','kg',3,25,1095)",
            // produk jadi hasil kemas ulang (satuan pcs/pack)
            "INSERT INTO m_barang (kode, nama, kategori, satuan, isi_gram, bahan_baku_id, stok_minimum, umur_simpan_hari) VALUES "
            + "('PJ-001','Skim Milk Powder 1 kg','PRODUK_JADI','pcs',1000,1,40,365),"
            + "('PJ-002','Full Cream Milk Powder 1 kg','PRODUK_JADI','pcs',1000,2,30,365),"
            + "('PJ-003','Non Dairy Creamer 1 kg','PRODUK_JADI','pcs',1000,3,30,365),"
            + "('PJ-004','Whey Powder 500 gr','PRODUK_JADI','pcs',500,4,30,365),"
            + "('PJ-005','Coconut Cream Powder 500 gr','PRODUK_JADI','pcs',500,5,25,270),"
            + "('PJ-006','Sucralose 100 gr','PRODUK_JADI','pcs',100,6,20,540),"
            + "('PJ-007','Trisodium Citrate 1 kg','PRODUK_JADI','pcs',1000,7,20,730)",
            // batch bahan baku: tanggal dibuat relatif dari hari ini supaya notifikasi selalu terlihat saat demo
            "INSERT INTO batch (no_batch, barang_id, sumber, supplier_id, qty_awal, qty_sisa, tgl_masuk, tgl_kedaluwarsa, keterangan, created_by) VALUES "
            + "('BM-0001',1,'PEMBELIAN',1,250,160,CURDATE()-INTERVAL 60 DAY,CURDATE()+INTERVAL 480 DAY,'10 karung @25 kg',1),"
            + "('BM-0002',2,'PEMBELIAN',1,150,40,CURDATE()-INTERVAL 520 DAY,CURDATE()+INTERVAL 20 DAY,'6 karung @25 kg',1),"
            + "('BM-0003',3,'PEMBELIAN',2,200,100,CURDATE()-INTERVAL 100 DAY,CURDATE()+INTERVAL 350 DAY,'8 karung @25 kg',1),"
            + "('BM-0004',3,'PEMBELIAN',2,75,15,CURDATE()-INTERVAL 445 DAY,CURDATE()+INTERVAL 5 DAY,'Sisa batch lama',1),"
            + "('BM-0005',4,'PEMBELIAN',1,125,85,CURDATE()-INTERVAL 30 DAY,CURDATE()+INTERVAL 510 DAY,'5 karung @25 kg',1),"
            + "('BM-0006',5,'PEMBELIAN',4,50,20,CURDATE()-INTERVAL 368 DAY,CURDATE()-INTERVAL 3 DAY,'Belum terpakai',1),"
            + "('BM-0007',5,'PEMBELIAN',4,40,20,CURDATE()-INTERVAL 40 DAY,CURDATE()+INTERVAL 325 DAY,'2 karung @25 kg',1),"
            + "('BM-0008',6,'PEMBELIAN',2,10,4,CURDATE()-INTERVAL 705 DAY,CURDATE()+INTERVAL 25 DAY,'1 karton',1),"
            + "('BM-0009',7,'PEMBELIAN',3,60,0,CURDATE()-INTERVAL 220 DAY,CURDATE()+INTERVAL 875 DAY,'Habis dipakai',1)",
            // batch produk jadi
            "INSERT INTO batch (no_batch, barang_id, sumber, qty_awal, qty_sisa, tgl_masuk, tgl_kedaluwarsa, keterangan, created_by) VALUES "
            + "('KU-0001',8,'KEMAS_ULANG',90,60,CURDATE()-INTERVAL 50 DAY,CURDATE()+INTERVAL 315 DAY,'Hasil kemas ulang',2),"
            + "('KU-0002',9,'KEMAS_ULANG',100,22,CURDATE()-INTERVAL 345 DAY,CURDATE()+INTERVAL 20 DAY,'Hasil kemas ulang',2),"
            + "('KU-0003',10,'KEMAS_ULANG',100,45,CURDATE()-INTERVAL 90 DAY,CURDATE()+INTERVAL 275 DAY,'Hasil kemas ulang',2),"
            + "('KU-0004',10,'KEMAS_ULANG',60,12,CURDATE()-INTERVAL 361 DAY,CURDATE()+INTERVAL 4 DAY,'Hasil kemas ulang',2),"
            + "('KU-0005',11,'KEMAS_ULANG',80,70,CURDATE()-INTERVAL 20 DAY,CURDATE()+INTERVAL 345 DAY,'Hasil kemas ulang',2),"
            + "('KU-0006',12,'KEMAS_ULANG',50,9,CURDATE()-INTERVAL 275 DAY,CURDATE()-INTERVAL 5 DAY,'Hasil kemas ulang',2),"
            + "('KU-0007',12,'KEMAS_ULANG',40,15,CURDATE()-INTERVAL 30 DAY,CURDATE()+INTERVAL 240 DAY,'Hasil kemas ulang',2),"
            + "('KU-0008',13,'KEMAS_ULANG',50,18,CURDATE()-INTERVAL 60 DAY,CURDATE()+INTERVAL 480 DAY,'Hasil kemas ulang',2),"
            + "('KU-0009',14,'KEMAS_ULANG',60,0,CURDATE()-INTERVAL 160 DAY,CURDATE()+INTERVAL 570 DAY,'Habis terjual',2)",
            "INSERT INTO mutasi_stok (batch_id, jenis, qty, no_referensi, keterangan, created_by, created_at) "
            + "SELECT id, IF(sumber='PEMBELIAN','MASUK','KEMAS_HASIL'), qty_awal, no_batch, keterangan, created_by, tgl_masuk FROM batch",
            "INSERT INTO kemas_ulang (no_dokumen, produk_id, qty_hasil, batch_hasil_id, catatan, created_by, created_at) "
            + "SELECT REPLACE(no_batch,'KU-','KM-'), barang_id, qty_awal, id, 'Kemas ulang rutin', created_by, tgl_masuk "
            + "FROM batch WHERE sumber='KEMAS_ULANG' ORDER BY id",
            "INSERT INTO kemas_ulang_detail (kemas_ulang_id, batch_id, qty_pakai) "
            + "SELECT k.id, m.bahan_batch, k.qty_hasil * b.isi_gram / 1000 FROM kemas_ulang k "
            + "JOIN m_barang b ON b.id = k.produk_id "
            + "JOIN (SELECT 10 hasil, 1 bahan_batch UNION ALL SELECT 11,2 UNION ALL SELECT 12,3 UNION ALL SELECT 13,4 "
            + "UNION ALL SELECT 14,5 UNION ALL SELECT 15,6 UNION ALL SELECT 16,7 UNION ALL SELECT 17,8 UNION ALL SELECT 18,9) m "
            + "ON m.hasil = k.batch_hasil_id",
            "INSERT INTO mutasi_stok (batch_id, jenis, qty, no_referensi, keterangan, created_by, created_at) "
            + "SELECT d.batch_id, 'KEMAS_PAKAI', d.qty_pakai, k.no_dokumen, 'Dipakai kemas ulang', k.created_by, k.created_at "
            + "FROM kemas_ulang_detail d JOIN kemas_ulang k ON k.id = d.kemas_ulang_id",
            "INSERT INTO barang_keluar (no_dokumen, customer_id, tanggal, catatan, created_by, created_at) VALUES "
            + "('BK-0001',1,CURDATE()-INTERVAL 14 DAY,'Order mingguan',2,CURDATE()-INTERVAL 14 DAY),"
            + "('BK-0002',2,CURDATE()-INTERVAL 9 DAY,'Order mingguan',2,CURDATE()-INTERVAL 9 DAY),"
            + "('BK-0003',3,CURDATE()-INTERVAL 4 DAY,'Order bulanan',3,CURDATE()-INTERVAL 4 DAY),"
            + "('BK-0004',4,CURDATE()-INTERVAL 1 DAY,'Order mingguan',2,CURDATE()-INTERVAL 1 DAY)",
            "INSERT INTO barang_keluar_detail (barang_keluar_id, batch_id, qty) VALUES "
            + "(1,10,30),(1,11,40),(2,12,30),(2,13,20),(3,14,10),(3,16,25),(4,17,25),(4,11,38),(4,18,32)",
            "INSERT INTO mutasi_stok (batch_id, jenis, qty, no_referensi, keterangan, created_by, created_at) "
            + "SELECT d.batch_id, 'KELUAR', d.qty, k.no_dokumen, 'Barang keluar', k.created_by, k.created_at "
            + "FROM barang_keluar_detail d JOIN barang_keluar k ON k.id = d.barang_keluar_id"
        };
        try (Statement s = get().createStatement()) {
            for (String q : data) {
                s.executeUpdate(q);
            }
        }
    }
}
