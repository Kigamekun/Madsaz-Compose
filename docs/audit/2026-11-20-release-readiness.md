# Audit kelayakan rilis Madsaz Android: 2026-11-20

**Keputusan: siap sebagai kandidat closed testing (rilis bertahap), BELUM dapat dinyatakan "final production".** Kode app lolos semua gerbang yang dapat saya buktikan: bug terkonfirmasi sudah diperbaiki dan diuji ulang di Infinix, alur login penuh lolos di build R8, dan E2E ke BE nyata 11/11. Sebelum production penuh masih ada syarat yang hanya pemilik yang dapat memenuhi atau memutuskan, dan area yang belum teruji (lihat "Syarat sebelum production" dan "Sisa pekerjaan").

## Kondisi pengujian
- Perangkat: Infinix X6840, Android 16 (SDK 36), XOS 16.3.0, arm64, 720×1576 @280 dpi, RAM 3,8 GB. Satu perangkat tidak membuktikan kompatibilitas perangkat lain.
- Versi Play (kunci Google) di-uninstall dengan izin pemilik agar build debug bisa dipasang. **Pasang ulang dari Play setelah audit.** Tidak ada `pm clear`, tidak ada setelan perangkat yang diubah permanen (mode gelap diubah sementara lalu dikembalikan ke "no").
- Debug build → BE lokal (Postgres + SeaweedFS Docker, `MOCK_ANALYSIS_ENABLED`), akun uji hanya di BE lokal. Produksi tidak disentuh.
- Dialog sistem "Simpan sandi" (Pengelola Sandi Google) sempat menutupi aplikasi dan menggagalkan satu run tes; ditutup dengan Back tanpa menyimpan.
- Aplikasi lain milik pemilik tampil di layar saat aplikasi Madsaz di latar belakang. Tangkapan layarnya dihapus dan alat bantu uji diberi pengaman agar hanya membaca jendela `com.madsaz`.

## Bukti validasi
| Pemeriksaan | Hasil |
|---|---|
| Unit test JVM | 45 total: 34 lulus, 0 gagal, 11 dilewati (E2E ke BE nyata). Dua test baru terbukti gagal tanpa perbaikannya (red-green). |
| Regresi akhir setelah tinjauan ulang | `scripts/e2e.sh` penuh (BE nyata, token 3 detik): E2E JVM **11/11**; tes di HP **12/12** (11 dalam run itu, `AuthFlowTest` diulang dua kali lulus; kegagalan sekali dalam run itu berasal dari pemantau dialog buatan saya yang menekan Back saat Activity diluncurkan, tanpa crash di logcat). Unit 45 (0 gagal), lint 0 error, AAB 9,91 MB. |
| Instrumentation di Infinix | **12/12 lulus** (run terakhir, kode final): `AuthFlowTest`, `RecordingFlowTest`, `RecordingStoreTest`, `UiComponentsTest`×8, `UiLayoutTest` (id/en/ar × skala font × lebar layar).
| E2E data layer ke BE nyata (`scripts/e2e.sh`) | 11/11 lulus. Satu kegagalan awal (batas tamu) berasal dari `scripts/e2e.sh` yang tidak mengatur `GUEST_DAILY_LIMIT` (default BE kini 4, test mengunci 2): diperbaiki satu baris di skrip. |
| Alur login di build R8 (varian uji non-debuggable, BE lokal, Infinix) | Login, Beranda, registrasi FCM (204), Rekam→Hasil, Riwayat, Petunjuk, Profil, sheet Paket + Back (F13), Beri Masukan (201), logout (token sesi HP dicabut di server): semuanya berhasil, 0 crash. | Run awal gagal karena lingkungan (S3 belum jalan, sesi uji tertinggal), bukan bug aplikasi. |
| Test baru di HP | Back di sheet Paket dan aksi klik TalkBack kartu Beranda gagal sebelum perbaikan dan lulus sesudahnya (merah → hijau). Polling habis, form setelah recreate, dan `RecordingStore.deleteAll` lulus. |
| Lifecycle di HP (debug → BE lokal) | Proses dimatikan (`run-as kill`): sesi pulih, tanpa crash. Izin kamera/mikrofon dicabut → layar "Izin diperlukan"; ditolak → "Buka Pengaturan"; diberi lagi → siap merekam; kamera dilepas saat keluar (klien kamera aktif kosong). BE dimatikan: pesan "Gagal terhubung ke server" dan data tetap tampil; BE hidup lagi dengan kunci JWT baru: `401 → refresh 200 → 200`, pesan hilang. 0 crash di logcat. |
| Lint (debug & release) | 0 error, 58 warning (tidak ada yang menghalangi). |
| Build rilis R8 | APK 5,63 MB (baseline 5,2), AAB 9,91 MB (baseline 6,9 MB, naik ±3 MB). Dipasang di Infinix (non-debuggable, kunci lokal), 0 crash. |
| Startup rilis (layar signed-out, `am start -W`, n=10 per jenis, tanpa baseline profile) | Cold median **669 ms** (624–826), warm **224 ms** (186–269), hot **91 ms** (78–108). Peluncuran pertama setelah pasang 1,39 dtk. |
| Jank rilis (`gfxinfo`, gulir halaman Kebijakan Privasi, 36 gulir lewat `adb input swipe`) | 2.928 frame, 1 janky (**0,03%**), p50 10 ms, p90 11 ms, p99 13 ms, 0 slow UI thread. PSS memori 87 MB, 1 Activity, 0 WebView. |
| Smoke rilis | Layar masuk, daftar, bahasa Indonesia/Inggris, dan halaman Kebijakan Privasi dimuat dari produksi (versi 2026-10-25) tampil benar. Angka kuota tamu "4x/hari" tampil setelah BE menjawab. |
| Upgrade `install -r` (versionCode 100 → 101, tanda tangan sama) | Berhasil: `firstInstallTime` tetap, pilihan bahasa bertahan, 0 crash. |

## Temuan terkonfirmasi dan diperbaiki
| # | Prioritas | Lokasi | Masalah → akar masalah | Perbaikan | Bukti |
|---|---|---|---|---|---|
| F1 | Tinggi | `AuthViewModel.kt` `logout()` | Refresh token ditangkap sebelum `unregisterDevice()`; bila access token kedaluwarsa, panggilan itu memicu refresh yang merotasi token, sehingga `logout(tokenLama)` tidak mencabut apa pun dan token baru hidup 30 hari. Kegagalan unregister juga melewati logout server. | Unregister dalam try terpisah; token dibaca setelahnya. | Kode BE (`auth.js` logout hanya mencabut hash yang dikirim) + HP dengan TTL 3 dtk: `refresh 200 → logout 204`, refresh token aktif akun = 0. Bug pra-perbaikan tidak direproduksi langsung. |
| F2 | Sedang | `MainActivity.kt` | Extra `destination` dibaca di tiap `onCreate`; sheet Paket terbuka lagi setelah ganti bahasa/mode gelap/proses dimatikan. | Dibaca hanya bila `savedInstanceState == null`. | Direproduksi di HP sebelum fix, hilang sesudahnya. |
| F3 | Sedang | `MadsazApp.kt`, `SignedOutApp.kt` | Store ViewModel dibuang tiap Activity dibuat ulang: riwayat/profil/paket dimuat ulang, aksi berjalan batal, form lupa sandi dan rekaman tamu hilang. | `rememberScopedViewModelStoreOwner` (ViewModel activity per kunci; dibuang saat logout/akun berganti/halaman ditutup). | HP: `/profile` tidak dimuat ulang saat recreate. Crash sempat muncul dari perubahan ini (kelas private + refleksi), tertangkap di HP dan diperbaiki. |
| F4 | Sedang | `GuestApi.kt` | `cues` kosong dari BE (`analysis.cues ?? []`) membuat `scores.first()` crash di hasil tamu. | Tolak sebagai `ContractException` di batas data. | Unit test (red-green). |
| F6 | Sedang | `AuthViewModel.deleteAccount`, `AndroidManifest.xml` | Video yang belum terkirim tetap ada setelah akun dihapus; tanpa `dataExtractionRules`, transfer perangkat menyalin video, ID tamu, dan prefs. | `RecordingStore.deleteAll()`; `data_extraction_rules.xml` (exclude semua). | Lint `DataExtractionRules` berubah; `RecordingStoreTest` belum dijalankan. |
| F9 | Sedang | `ResultViewModel.kt`, `ResultScreen.kt` | Setelah 45 poll status masih `PROCESSING`, layar berputar tanpa akhir tanpa tombol. | Flag `pollingExhausted` → layar log yang sudah ada (teks menunggu + tarik untuk muat ulang). Tidak ada komponen baru. | Test UI baru (kompilasi saja). |
| F10 | Rendah | `PlanViewModel`, `PlanSummaryViewModel` | Refresh bisa tumpang tindih (kembali dari Play + `Verified`), respons lama bisa menimpa status paket baru. | Batalkan job sebelumnya (pola History/Result). | Kode; `verify` aman terhadap pembatalan. |
| F11 | Rendah | `ApiClient.kt` | Body non-JSON dengan status 2xx (portal Wi-Fi) → `JSONException` yang pesannya memuat isi body dan tercatat di logcat rilis. | `ContractException` tanpa isi body. | Unit test (red-green). |
| F12 | Sedang | `HomeScreen.kt:222` | `onClick(openLabel) { false }` menimpa aksi klik kartu utama: pengguna TalkBack tidak bisa membuka hasil terbaru atau mulai merekam dari kartu itu. | `onClick(openLabel, null)`: label tetap, aksi klik Surface dipakai. | HP: test merah (0 panggilan) → hijau. |
| F13 | Sedang | `PlanScreen.kt` | `BackHandler` di luar `ModalBottomSheet` (jendela sendiri): Back sistem di penawaran Plus menutup seluruh sheet, bukan kembali ke tampilan Free (tombol panah di header sudah benar). | `BackHandler` dipindah ke dalam konten sheet. | HP: test merah (`onBack` terpanggil) → hijau. |
| F14 | Rendah | `PlanScreen.kt` `PlanOption`, `HomeScreen.kt` "Lihat semua" | Pilihan paket tidak memberi tahu TalkBack mana yang terpilih; "Lihat semua" tanpa role tombol. | `role = RadioButton` + `selected` (pola yang sama dengan `RatingCard`); `Role.Button` pada clickable. Tanpa perubahan visual. | HP: test baru (2 pilihan selectable, 1 terpilih). |
| F17 | Rendah | `AuthScreen.kt` | Nama, email, persetujuan hilang saat bahasa diganti dari layar masuk. | `rememberSaveable` (sandi tetap `remember`). | Test UI baru (kompilasi saja). |

## Tinjauan ulang code-by-code (pembaca independen + self-review diff)
Dibaca ulang: alur rekam/unggah, penyimpanan, auth, profil, notifikasi, navigasi, riwayat. Semua temuan dicocokkan dengan kode sebelum diubah. Diperbaiki (tanpa perubahan visual):
- **F15** `FcmRegistration.onPermissionResult`: bila registrasi pertama gagal (jaringan), `enabled` tidak pernah ditulis dan dialog izin tidak muncul lagi, sehingga push tidak pernah aktif. Kini `enabled` ditulis lebih dulu dan `refresh()` mencoba lagi tiap peluncuran.
- **F16** `FcmRegistration.token()`: `Tasks.await` tanpa batas waktu membuat logout bisa menggantung bila Play Services macet. Kini batas 10 detik.
- **F17b** `ProfileViewModel`: refresh yang tertunda bisa menimpa hasil simpan, atau menutup sheet seolah simpan berhasil. Refresh dibatalkan saat simpan dan diabaikan selama menyimpan.
- **F18** `ChangePasswordViewModel`: `signIn` (Keystore + disk + lock refresh token) berjalan di thread utama. Dipindah ke IO, sama dengan `AuthViewModel`.
- **F19** `RecordingViewModel`: `IOException` dari `store.delete` di dalam `catch` lolos dan membuat app crash saat batas kuota tercapai. Kini ditangkap; file tetap bisa dibuang pengguna.
- **F20** `ProfileScreen`: foto dengan rasio ekstrem atau URI dicabut melempar exception selain `PhotoException` dan membuat app crash. Kini ditampilkan sebagai "foto tidak terbaca".
- **F21** Tap ganda: `launchSingleTop` pada navigasi ke hasil, petunjuk, privasi, dan feedback (tidak pernah direproduksi, tetapi idiomatis dan aman).
Diperiksa dan tidak perlu diubah: durasi dibulatkan ke atas (BE menerima 0–3600 detik, hanya kosmetik), pesan salah sandi di dialog hapus akun (tes mengunci teks itu).
Diketahui, tidak menghalangi rilis: satu status/baris rusak menggagalkan seluruh riwayat (parser ketat disengaja, lihat R11), PNG transparan menjadi hitam pada avatar, file rekaman parsial bisa menumpuk bila proses dimatikan saat merekam, pesan galat refresh profil bertahan sampai aksi berikutnya.

## Risiko yang belum terbukti (butuh HP/emulator)
- (R1 dan R2 dari draf awal terbukti dan sudah diperbaiki sebagai F13 dan F12.)
- **R3** Tablet/foldable ≥600dp di Android 16 mengabaikan `screenOrientation=portrait`; rekaman dihentikan oleh recreate (butuh emulator).
- **R4** Tap ganda menumpuk layar (`navigate` tanpa `launchSingleTop`): tidak direproduksi lewat adb. Tidak diubah.
- **R5** `PlayBilling`: bila pembelian selesai saat belum login, token tercatat "terverifikasi" dan baru dicoba ulang setelah restore paksa/restart app.
- **R6** Sesi yang diakhiri server tidak melepas token FCM di BE; pengingat paket tetap dikirim ke perangkat sampai akun lain masuk.
- **R7** Permintaan yang sedang berjalan bisa memakai token akun lain bila akun berganti dalam hitungan detik (upload multi-langkah).
- **R8** Avatar diunduh ulang tiap header tampil (tanpa cache/timeout) dan tampil sebagai inisial sesaat; perbaikan butuh keputusan desain karena URL presign berubah.
- **R9** Aksesibilitas (dibaca dari kode, TalkBack belum dijalankan): checkbox S&K tanpa nama sendiri; `PlanOption`/`FeedbackChoice` tanpa role pilihan; "Lihat semua" tanpa role dan ±40 dp tinggi; label field terbaca ganda di dump UI.
- **R10** Rekaman yang baru dimulai lalu Back dalam jendela sempit bisa meninggalkan file parsial.
- **R11** Satu `scores` rusak dari ML menggagalkan seluruh riwayat (parser ketat). Butuh keputusan kontrak dengan BE.

## Butuh keputusan pemilik karena mengubah tampilan
- Teks "Hemat" putih di kartu tahunan saat tidak terpilih (`PlanScreen.kt:518`) tak terlihat di latar putih.
- Ikon status bar putih pada halaman Lupa/S&K/Privasi saat signed-out (`AuthScreen.kt:125`).

## Catatan BE (tidak diubah)
- Saat penyimpanan objek tidak terjangkau, `GET /profile` dan `POST /auth/delete-account` menjawab 500 (teramati langsung): profil dan hapus akun gagal total hanya karena S3.
- Error `PUT /profile`, `/profile/avatar-upload-url`, `/notifications/device` tidak memuat `code`.
- Tidak ada pembatasan laju untuk login/refresh/hapus akun.

## BE produksi (probe baca-saja, tanpa membuat akun)
- `https://api.madsaz.my.id/health`: 200, database terhubung, `billing_configured: true`. `/payments/plans` memuat `guest.daily_analysis_limit` dan produk `madsaz_plus` (`monthly`, `annual`), sesuai kontrak app.
- POST ke host lama (`madsaz-be.onrender.com`) dijawab **307** dan tidak diikuti; POST ke host baru menjawab normal (`401 invalid_credentials` untuk login kosong, sama dengan kode repo).
- **Syarat & Ketentuan di produksi berstatus `draft`** (`src/terms.js:5`); kebijakan privasi `final` (versi 2026-10-25). Teks S&K draft dengan kewajiban persetujuan saat daftar adalah keputusan konten/hukum pemilik sebelum rilis publik.
- Belum diverifikasi: endpoint berautentikasi (login, plan, records, profil, guest/analyze) terhadap versi BE yang terdeploy. Kode repo cocok dengan app, tetapi kesamaan versi terdeploy hanya terbukti untuk endpoint publik di atas.

## Konfigurasi rilis
- **Host API (wajib diperiksa):** `madsazApiBaseUrl` di `secrets.properties` lokal (`madsaz-be.onrender.com/api/v1`) menjawab **HTTP 307** ke `https://api.madsaz.my.id/api/v1`. `HttpURLConnection` mengikuti redirect untuk GET tetapi tidak untuk POST, jadi login/daftar dari build berhost lama akan gagal. Pastikan secret CI `MADSAZ_API_BASE_URL` memakai `https://api.madsaz.my.id/api/v1`. Pengukuran rilis di atas memakai host baru.
- `keystore.properties` lokal menandatangani dengan sertifikat SHA-1 `1b57…8738`, bukan kunci unggah `D9:ED:82:74…` yang diminta `RELEASE.md`. Tidak berpengaruh pada CI (memakai secret), tetapi AAB dari mesin ini akan ditolak Play.
- `proguard-rules.pro` tidak ada dan tidak diperlukan (JSON manual, tanpa refleksi). Tidak ada modul baselineprofile/Macrobenchmark; startup dan jank belum diukur.
- Aman: rahasia tidak ada di git, token terenkripsi Keystore, cleartext dimatikan di rilis, komponen tidak dapat dikendalikan aplikasi lain, `PendingIntent` immutable, tanpa WebView.

## Syarat sebelum production (hanya pemilik yang dapat memenuhi)
1. Secret CI `MADSAZ_API_BASE_URL` = `https://api.madsaz.my.id/api/v1` (host lama dialihkan 307 dan POST gagal).
2. Putuskan teks Syarat & Ketentuan yang masih `draft` di BE produksi.
3. Rilis lewat CI dengan kunci unggah Play yang benar (kunci lokal salah) dan staged rollout, mulai closed testing.
4. Setelah closed testing berjalan: smoke login dengan akun uji produksi pada build dari CI, dan uji pembelian dengan license tester.

## Sisa pekerjaan sebelum keputusan final (butuh HP tersambung)
1. (Selesai) androidTest penuh 11/11 dan lifecycle dasar di HP.
2. (Selesai) Build rilis di HP: smoke signed-out, startup, jank, memori, upgrade.
3. (Selesai) Smoke alur login di build R8 lewat varian uji sementara `r8check` (`initWith(release)`, tanda tangan debug, cleartext ke BE lokal); sumbernya dikembalikan agar tidak ikut ter-commit.
4. Belum diuji: kunci layar, notifikasi/FCM di XOS, upload saat koneksi putus di tengah jalan, font 2.0× dan display size, jank di layar daftar panjang (Riwayat/Petunjuk) dan Macrobenchmark + baseline profile.
5. Emulator tablet untuk R3; TalkBack langsung untuk R9; `scripts/e2e.sh` penuh.
6. Pemilik: pasang ulang versi Play (HP berisi build rilis lokal versionCode 101 bertanda tangan kunci lokal, jadi uninstall dulu), putuskan R11, dua item tampilan, kunci penandatangan lokal, dan host API.

## Validation run / Not run / Recommended
- **Validation run:** `testDebugUnitTest` (45; 34 lulus + 11 E2E yang kini dijalankan lewat `scripts/e2e.sh`, semuanya lulus), `lintRelease`, `assembleRelease`, `bundleRelease`, `connectedDebugAndroidTest` (12/12 pada Infinix), alur login penuh di build R8, uji manual F1/F2/F3, lifecycle, startup, jank, memori, dan upgrade di Infinix, probe baca-saja BE produksi.
- **Not run:** alur login di build rilis, uji pembelian Play, Macrobenchmark, pemindaian kerentanan dependensi, serta "Sisa pekerjaan" 4-5.
- **Recommended:** `scripts/e2e.sh` dengan `E2E_DEVICE=1`, Macrobenchmark + baseline profile, audit TalkBack, dependency scan (OSV/Gradle).
