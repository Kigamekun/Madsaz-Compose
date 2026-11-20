# Matriks cakupan audit Madsaz (2026-11-20)

Status: **D** diuji langsung di Infinix X6840 (Android 16/XOS 16.3.0, debug build ke BE lokal) · **K** diperiksa lewat kode · **B** bermasalah · **F** diperbaiki · **T** terhambat/belum diuji.
Pemeriksaan kode (K) tidak dianggap bukti alur berjalan. HP dicabut pemilik setelah sesi pertama; baris bertanda "uji ulang" menunggu HP tersambung lagi.

## Layar dan route
| Item | Status | Bukti / catatan |
|---|---|---|
| `home` | D, B→F | Tampil di HP, 3 bahasa × skala font lolos `UiLayoutTest`. B: aksi klik TalkBack kartu utama mati (F12), diperbaiki dan diuji di HP. |
| `history` | D, K | Baris rekaman terbuka ke hasil; tap ganda cepat tidak menumpuk layar (tidak direproduksi lewat adb). |
| `cues`, `cue/{code}` | D (layout), K | `UiLayoutTest`. |
| `profile` | D, K | Logout, dump semantics. Edit nama/avatar: K saja. |
| `privacy` / Terms | K, D sebagian | Dokumen dimuat saat `AuthFlowTest`; tampilan layar sendiri belum dilihat. |
| `recording` | D | `RecordingFlowTest` lolos. Izin dicabut/ditolak/diberi lagi dan pelepasan kamera: diuji langsung. |
| `result/{id}` | D, F | Alur lolos. Perbaikan F9 (polling habis): uji UI baru, uji ulang di HP. |
| `feedback/{id}` | D (layout), K | Kirim feedback tidak dijalankan: **T**. |

## Sheet, dialog
| Item | Status | Catatan |
|---|---|---|
| Sheet Paket (Free/Plus) | D, B→F | Dibuka lewat intent notifikasi. B: muncul lagi saat recreate (F2) dan Back menutup seluruh sheet (F13); keduanya diperbaiki dan diuji di HP. Pembelian Play: **T** (BE lokal `billing_configured=false`). |
| Notifications, Language | K | Belum dijalankan di HP. |
| ChangePassword, Delete akun | D | `AuthFlowTest` (salah/benar sandi, akun terhapus). |
| AccountEdit, UpgradeDialog | K | |
| Auth (masuk/daftar) | D, B→F | Form hilang saat recreate (F17), diperbaiki; uji ulang di HP. |
| Forgot, Terms, Privacy (signed-out) | K | Email BE lokal tidak dikonfigurasi: **T**. |
| Tamu (rekam, hasil) | K, F | Crash skor kosong (F4) diperbaiki + unit test; alur penuh: **T**. |

## ViewModel dan state
| VM | Status | Catatan |
|---|---|---|
| Auth | D, F | Logout (F1) terbukti di HP; delete akun membersihkan video lokal (F6, uji ulang). |
| History, Profile, PlanSummary, Plan | K, F | Store bertahan lewat recreate (F3, D). Race refresh paket (F10): K + kompilasi. |
| Recording, Result, Feedback | D, K | Dipegang NavBackStackEntry. |
| ChangePassword, Notifications, Privacy, ForgotPassword | K, D (ChangePassword) | |

## Penyimpanan lokal dan integrasi
| Item | Status | Catatan |
|---|---|---|
| AuthStore (AES-GCM + Keystore) | K, D | Sesi bertahan lewat update app (`install -r`). |
| RecordingStore | K, F | `deleteAll()` baru + `RecordingStoreTest` (belum dijalankan di HP). |
| GuestDevice, AppLanguage, FcmRegistration | K | |
| PlayBilling | K | Tidak dapat diuji tanpa produk Play; risiko R5. |
| API auth/profile/plan/records/guest/feedback/notifications/policy | K (vs BE), D sebagian | 45 test JVM (34 dijalankan, 11 E2E ke BE nyata dilewati; `scripts/e2e.sh` belum dijalankan). |
| 401/refresh | D | BE TTL 3 detik: refresh lalu logout berhasil. |
| 403/404/429/5xx, offline, respons terlambat | K, unit | Mode pesawat dan BE mati: **T**. |

## Lifecycle dan platform
| Skenario | Status |
|---|---|
| Recreate (mode gelap) | D |
| Proses dimatikan (`run-as kill`), HOME lalu kembali | D (sesi pulih, kembali ke Beranda) |
| Kunci layar | **T** |
| Izin kamera/mikrofon dicabut, ditolak, diberi lagi | D · notifikasi: **T** |
| BE mati lalu hidup lagi (401 → refresh) | D |
| Tablet / jendela ≥600dp (portrait diabaikan di Android 16) | **T** (butuh emulator) |
| TalkBack langsung, font 2.0×, display size | **T** |
| FCM di XOS (app standby) | **T** |
| Rilis R8: instal, startup cold/warm/hot, jank, memori, upgrade | D (signed-out; angka di laporan) · login di R8: D (varian uji ke BE lokal) |
