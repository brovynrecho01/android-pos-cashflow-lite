# Toko Pak Kadi Cash System — Offline-First Android POS & Cashflow Lite

![Kotlin](https://img.shields.io/badge/Kotlin-2.0+-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)
![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-Material%203-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white)
![Room SQLite](https://img.shields.io/badge/Room-SQLite%20Offline--First-003B57?style=for-the-badge&logo=sqlite&logoColor=white)
![Google Apps Script](https://img.shields.io/badge/Cloud%20Sync-Google%20Sheets%20%26%20Drive-34A853?style=for-the-badge&logo=googlesheets&logoColor=white)

**Toko Pak Kadi Cash System** adalah aplikasi Android native pencatat arus kas harian (*cashflow tracker*) dan kasir cepat (*Point of Sale Lite*) yang dirancang khusus untuk operasional ritel toko kelontong. 

Aplikasi ini menyelesaikan masalah klasik UMKM ritel: mencatat transaksi penjualan multi-item dan biaya operasional harian secara **100% offline** dengan cepat **tanpa beban *stock opname* atau database master barang**, sekaligus menyediakan pencadangan otomatis ke **Google Sheets** dan **Google Drive** saat tutup toko.

---

## Latar Belakang & Solusi Arsitektur

Banyak aplikasi kasir modern terlalu berat untuk toko kelontong karena mewajibkan input ribuan *master SKU* dan manajemen stok sebelum bisa digunakan. Proyek ini mengambil pendekatan **Transaction-Log Only**:

1. **Zero Stock-Opname Overhead:** Kasir langsung mengetik nama barang, jumlah (*qty*), dan harga satuan secara bebas (*free-text*) dengan fitur *auto-suggest* dari riwayat transaksi.
2. **Real-Time Cashflow Equation:** Menghitung posisi uang tunai di laci kasir secara *real-time* menggunakan rumus:
   $$\text{Total Akhir Kas} = \text{Modal Awal} + \text{Total Pemasukan} - \text{Total Pengeluaran}$$
3. **Serverless Cloud Reporting:** Menggunakan jembatan HTTP POST JSON ke *Google Apps Script Webhook* untuk mencatat rekapitulasi kas harian dan rincian barang terjual langsung ke Google Sheets & Google Drive tanpa biaya sewa *cloud server* bulanan.

---

## Fitur Utama

### 1. Dashboard Beranda & Modal Awal
* Pengaturan **Modal Awal** harian (uang kembalian di laci saat buka toko).
* Kartu indikator *real-time* untuk **Modal Awal**, **Total Pemasukan**, **Total Pengeluaran**, dan **Total Akhir Kas Sebelum Tutup Toko**.

### 2. Kasir Cepat (POS Lite — Pemasukan)
* Input keranjang belanja multi-item tanpa perlu database stok barang.
* Kalkulasi otomatis **Subtotal** ($\text{Qty} \times \text{Harga Satuan}$) dan **Grand Total** struk.
* Pilihan metode pembayaran (**Tunai / QRIS / Transfer**) beserta kalkulator uang kembalian otomatis.
* Cetak/tampilkan struk digital (*Receipt Dialog*) setelah transaksi disimpan.

### 3. Manajemen Kas Keluar (Pengeluaran)
* Pencatatan cepat pengeluaran operasional harian dari laci kasir (contoh: beli kresek, bayar listrik, makan siang, atau bayar *sales* barang harian).
* Riwayat pengeluaran harian dengan fitur hapus transaksi jika terjadi salah input.

### 4. Laporan, Disaster Recovery & Cloud Sync
* **Pencarian & Filter Riwayat:** Cari transaksi berdasarkan nama barang, nomor struk, metode bayar, dan rentang tanggal (*Hari Ini*, *7 Hari*, *30 Hari*, *Semua*).
* **Tutup Toko & Sync ke Google Sheets:** Kirim rekapitulasi harian dan detail item terjual ke Google Sheets & Google Drive dalam satu ketukan.
* **Auto-Backup 24 Jam (WorkManager):** Pencadangan database lokal otomatis ke penyimpanan internal setiap 24 jam di latar belakang.
* **Export CSV & JSON Backup/Restore:** Bagikan laporan `.csv` atau cadangkan/pulihkan seluruh database `.json` melalui *Android Share Intent*.

---

## Tech Stack & Arsitektur

| Komponen | Teknologi yang Digunakan |
| :--- | :--- |
| **Bahasa Pemrograman** | Kotlin |
| **UI Framework** | Jetpack Compose (Material Design 3 — Dark Emerald Theme) |
| **Arsitektur Aplikasi** | MVVM (*Model-View-ViewModel*) + *Single Repository Pattern* |
| **Database Lokal** | Room Persistence Library (SQLite) & `SharedPreferences` |
| **Concurrency & State** | Kotlin Coroutines & `StateFlow` |
| **Background Task** | Android `WorkManager` (`PeriodicWorkRequest` 24 Jam) |
| **Cloud Integration** | Native `HttpURLConnection` JSON POST + Google Apps Script Webhook |
| **Unit Testing** | JUnit 4, Kotlinx Coroutines Test, & Robolectric |

---

## Skema Database Lokal (Room SQLite)

Aplikasi menyimpan seluruh data secara persisten di perangkat menggunakan 3 tabel relasional utama:

1. **`daily_sessions`**: Menyimpan sesi kas harian (`date`, `initialCapital`, `isClosed`, `lastSyncedAt`).
2. **`transactions`**: Menyimpan header transaksi (`id`, `date`, `timestamp`, `type` [`INCOME`/`EXPENSE`], `description`, `grandTotal`, `paymentMethod`, `amountPaid`, `changeAmount`).
3. **`transaction_items`**: Menyimpan rincian barang terjual per struk (`id`, `transactionId` [Foreign Key Cascade], `itemName`, `qty`, `unitPrice`, `subtotal`).

---

## Struktur Direktori Proyek

```text
app/src/main/java/com/example/
├── data/
│   ├── local/
│   │   ├── AppDatabase.kt               # Konfigurasi Room SQLite Database
│   │   ├── PosDao.kt                    # Query SQL & Flow Observables
│   │   └── PosRepository.kt             # Single Source of Truth & Kalkulasi Kas
│   ├── model/
│   │   └── Entities.kt                  # Entity Tabel, Enum, & Relasi Struk
│   └── remote/
│       ├── WebhookSyncManager.kt        # Klien HTTP POST JSON ke Google Sheets
│       └── GoogleAppsScriptTemplate.kt  # Template Script Backend Google Sheets
├── ui/
│   ├── components/                      # AppTopBar, AppBottomBar, ReceiptDialog
│   ├── screens/                         # Beranda, Kasir, Pengeluaran, & Laporan Screen
│   ├── theme/                           # Konfigurasi Warna & Tipografi Material 3
│   └── MainViewModel.kt                 # State Management & UI Event Handler
├── util/
│   ├── CurrencyFormatter.kt             # Format Rupiah (Rp) & Tanggal Indonesia
│   ├── CsvExporter.kt                   # Generator Laporan CSV Harian
│   ├── DatabaseBackupManager.kt         # Engine Export/Import Full Database JSON
│   └── FileSharer.kt                    # Integrasi FileProvider & Android Share Intent
├── work/
│   ├── AutoBackupWorker.kt              # Worker Pencadangan Latar Belakang
│   └── AutoBackupScheduler.kt           # Penjadwal Otomatis 24 Jam
├── MainActivity.kt
└── PosApplication.kt
```

---

## Panduan Integrasi Google Sheets & Google Drive

Aplikasi ini menggunakan pendekatan *Serverless Webhook* agar dapat terhubung ke Google Sheets tanpa memerlukan konfigurasi OAuth/SHA-1 yang rumit:

1. Buat spreadsheet baru di **Google Sheets** (otomatis tersimpan di Google Drive Anda).
2. Klik menu **Extensions (Ekstensi) > Apps Script**.
3. Salin kode `doPost(e)` yang tersedia di dalam file [`GoogleAppsScriptTemplate.kt`](app/src/main/java/com/example/data/remote/GoogleAppsScriptTemplate.kt) atau langsung dari tombol **"Salin Script Google Sheets"** di tab **Laporan** dalam aplikasi.
4. Klik **Deploy > New deployment**, pilih jenis **Web app**, atur *Execute as* menjadi **Me**, dan *Who has access* menjadi **Anyone**.
5. Salin **Web App URL** (`https://script.google.com/macros/s/.../exec`) lalu tempelkan ke kolom **Webhook URL** pada tab **Laporan** di aplikasi Android.

### Contoh Payload JSON yang Dikirim Saat Tutup Toko

```json
{
  "storeName": "Toko Pak Kadi",
  "date": "2026-09-26",
  "initialCapital": 200000,
  "totalIncome": 1450000,
  "totalExpense": 85000,
  "finalBalance": 1565000,
  "transactions": [
    {
      "id": 1,
      "time": "08:15:22",
      "type": "PEMASUKAN",
      "description": "Penjualan Kasir",
      "paymentMethod": "TUNAI",
      "grandTotal": 78000,
      "items": [
        {
          "itemName": "Beras 5kg",
          "qty": 1,
          "unitPrice": 65000,
          "subtotal": 65000
        },
        {
          "itemName": "Minyak Goreng 1L",
          "qty": 1,
          "unitPrice": 13000,
          "subtotal": 13000
        }
      ]
    }
  ]
}
```

---

## Cara Menjalankan Proyek (Build & Run)

1. **Clone repositori ini:**
   ```bash
 git clone [https://github.com/brovynrecho01/android-pos-cashflow-lite.git].
  cd android-pos-cashflow-lite.
   ```
2. **Buka di Android Studio:**
   Buka folder proyek menggunakan **Android Studio** (Ladybug atau versi lebih baru) dengan dukungan JDK 17.
3. **Jalankan Aplikasi:**
   Tunggu sinkronisasi Gradle selesai, hubungkan perangkat Android (dengan *USB Debugging* aktif) atau gunakan Emulator, lalu klik **Run (`Shift + F10`)**.
4. **Menjalankan Unit Test:**
   ```bash
   ./gradlew testDebugUnitTest
   ```

---

## Lisensi

Proyek ini dilisensikan di bawah **MIT License** — bebas digunakan dan dikembangkan untuk membantu digitalisasi UMKM dan toko kelontong.
