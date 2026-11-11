# Sistem Monitoring Persediaan dan Kedaluwarsa Barang

**Rancang Bangun Sistem Monitoring Persediaan dan Kedaluwarsa Barang dengan Notifikasi Otomatis Menggunakan Java pada PT Mitra Tiga Sepakat**

Aplikasi desktop Java Swing (NetBeans) untuk memantau stok bahan baku food ingredient (karung 25 kg) dan produk hasil kemas ulang, melacak tanggal kedaluwarsa per batch, dan memberi notifikasi otomatis.

## Cara menjalankan

1. Nyalakan **MySQL** di XAMPP (user `root`, password kosong).
2. Buka folder ini di **NetBeans** (File > Open Project).
3. Klik **Run** (F6). Database `db_monitoring_persediaan` + tabel + data contoh dibuat otomatis saat pertama kali jalan.
4. Login:
   - Admin: `admin` / `admin123`
   - Staff gudang: `gudang` / `gudang123`

Semua library sudah ada di folder `lib/` (FlatLaf, MySQL Connector, JasperReports, JCalendar), jadi tidak perlu download apa-apa. Kalau user/password MySQL beda, ubah di `Koneksi.java`.

Mau reset data contoh? Hapus database `db_monitoring_persediaan` di phpMyAdmin lalu jalankan ulang aplikasi.

## Fitur

| Menu | Isi |
|---|---|
| Dashboard | Kartu ringkasan (total barang, stok menipis/habis, hampir kedaluwarsa, sudah kedaluwarsa), tabel batch segera kedaluwarsa, stok yang perlu diisi, aktivitas stok terakhir |
| Notifikasi | Daftar peringatan otomatis + pengaturan batas hari peringatan/kritis dan interval pengecekan |
| Monitoring Stok | Stok layak per barang vs stok minimum (Aman / Menipis / Habis) + rincian batch |
| Monitoring Kedaluwarsa | Status per batch (Kedaluwarsa / Kritis / Peringatan / Aman) + pemusnahan batch kedaluwarsa |
| Barang Masuk | Penerimaan dari supplier: no. batch, jumlah, tanggal masuk, tanggal kedaluwarsa |
| Kemas Ulang | Karung bahan baku dipecah jadi kemasan kecil; bahan diambil FEFO, kedaluwarsa hasil dihitung otomatis |
| Barang Keluar | Pengiriman ke customer dengan FEFO + cetak Bukti Barang Keluar / Surat Jalan |
| Master Data | Barang, Supplier, Customer, Pengguna (khusus admin) |
| Laporan | Stok, Kedaluwarsa, Barang Masuk, Kemas Ulang, Barang Keluar, Pemusnahan (JasperReports) |

### Notifikasi otomatis
- Saat login muncul popup ringkasan di pojok kanan bawah.
- `javax.swing.Timer` mengecek ulang database tiap N menit (default 5). Kalau ada peringatan baru, muncul popup + notifikasi Windows (System Tray).
- Ikon lonceng di header menampilkan jumlah notifikasi yang belum dibaca.
- Jenis: Sudah Kedaluwarsa, Hampir Kedaluwarsa (kritis ≤ 7 hari, peringatan ≤ 30 hari), Stok Habis, Stok Menipis.

### FEFO (First Expired First Out)
Barang keluar dan kemas ulang selalu mengambil batch dengan tanggal kedaluwarsa paling dekat. Batch yang sudah kedaluwarsa tidak bisa dipakai.

## Struktur kode (`src/sistempenjualan`)

| File | Fungsi |
|---|---|
| `SistemPenjualan.java` | `main()`; setup tema & koneksi |
| `Bootstrap.java` | Jendela utama: sidebar, header, lonceng, navigasi halaman |
| `Koneksi.java` | Koneksi MySQL, pembuatan tabel, data contoh, helper query |
| `Persediaan.java` | Logika stok: FEFO, barang masuk, kemas ulang, barang keluar, pemusnahan |
| `Notifikasi.java`, `NotifikasiPopup.java` | Pengecekan otomatis, popup, notifikasi Windows |
| `Theme.java`, `RoundedPanel.java`, `Ikon.java` | Warna, font, gaya tombol/tabel, kartu, ikon |
| `Laporan.java`, `reports/*.jrxml` | Laporan JasperReports |
| `*.form` + `*.java` | Halaman (bisa dibuka di GUI Builder NetBeans) |

Catatan GUI Builder: panel kartu memakai `RoundedPanel` lewat *Properties > Code > Custom Creation Code*. Warna & font akhir diatur di method `gaya()` tiap halaman (lewat `Theme`), jadi tampilan di Design view NetBeans lebih polos daripada saat aplikasi dijalankan.

## Tabel database

`m_user`, `m_supplier`, `m_customer`, `m_barang` (kategori bahan baku/produk jadi, stok minimum, umur simpan, isi per kemasan), `batch` (no batch, qty awal/sisa, tgl masuk, tgl kedaluwarsa), `mutasi_stok`, `kemas_ulang`, `kemas_ulang_detail`, `barang_keluar`, `barang_keluar_detail`, `pengaturan`.
