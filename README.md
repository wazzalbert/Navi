# NAVI — Tahap 1

Asisten pribadi Android bergaya Serial Experiments Lain. Tahap 1 berisi kerangka proyek,
widget home screen, tema retro (CRT scanlines, cyan/hijau terminal), animasi boot di
aplikasi, dan layar percakapan kosong yang bisa dibuka dari widget.

**Status verifikasi:** XML valid dan semua referensi resource (`R.*`, manifest) sudah
dicek konsisten. Proyek ini **belum dikompilasi** dan **belum dijalankan di perangkat**
(tidak ada Android SDK di lingkungan pembuatnya). Anggap build pertama sebagai
pengujian pertama; kirim error-nya jika ada.

## Cara build

### A. Android Studio (paling mudah)
1. Pasang Android Studio (gratis), pilih *Open* → folder `navi`.
2. Tunggu Gradle sync (otomatis mengunduh SDK 36 jika diminta).
3. *Build → Build APK(s)*, atau colok HP (USB debugging aktif) dan tekan *Run*.
4. APK: `app/build/outputs/apk/debug/app-debug.apk`

### B. Command line
Butuh JDK 17+ dan Android SDK (platform 36, build-tools 36.0.0).
```
echo "sdk.dir=/path/ke/Android/Sdk" > local.properties
./gradlew assembleDebug
```

### C. Tanpa PC (GitHub Actions, gratis)
Unggah folder ini ke repo GitHub → tab *Actions* → *Build NAVI APK* → *Run workflow*.
Unduh `navi-debug-apk` dari hasil run, ekstrak, lalu pasang `app-debug.apk` di HP
(izinkan "pasang aplikasi dari sumber tidak dikenal").

APK debug sudah ditandatangani kunci debug sehingga bisa dipasang langsung.
Build `release` tidak bertanda tangan dan belum bisa dipasang tanpa keystore.

## Memasang widget
Buka aplikasi NAVI → **[ PASANG WIDGET ]**, atau tekan lama home screen → Widget → NAVI.
Isi nama di **IDENTITAS PENGGUNA** agar sapaan widget personal.

## Perilaku widget
- Status ONLINE/OFFLINE, sapaan, dan pesan terakhir dibaca dari data kecil lokal;
  tidak ada proses AI di widget.
- Diperbarui otomatis tiap ±30 menit (batas sistem Android), setiap aplikasi dibuka,
  dan saat bagian widget selain tombol diketuk.
- Setelah reboot, launcher memulihkan widget dan sistem memicu pembaruan; tidak ada
  service permanen dan tidak ada izin boot.
- Jam memakai `TextClock`, diperbarui oleh launcher tanpa menjalankan kode NAVI.

## Izin
Hanya `ACCESS_NETWORK_STATE`. `INTERNET` dan `RECORD_AUDIO` ditambahkan pada tahap
yang memakainya.

## Struktur
```
app/src/main/java/com/navi/assistant/
  data/NaviState.kt         status ringan (nama, pesan terakhir, AI core)
  network/NetworkManager.kt cek konektivitas (diperluas di Tahap 5)
  widget/NaviWidget.kt      AppWidgetProvider
  widget/WidgetRenderer.kt  membangun RemoteViews
  ui/MainActivity.kt        boot terminal, nama, pasang widget
  ui/ChatActivity.kt        kerangka percakapan (Tahap 2)
  ui/Insets.kt              penanganan edge-to-edge (target SDK 36)
```
