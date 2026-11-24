# Activity Diagram — Madsaz (Android)

Diagram diturunkan dari kode di `app/src/main/java/id/madsaz/app/` (bukan dari asumsi). Format Mermaid; GitHub dan
Android Studio (plugin Mermaid) merendernya langsung.

Notasi: `(( ))` = mulai/selesai, `[ ]` = aktivitas, `{ }` = keputusan, `[/ /]` = aktivitas di sistem lain (BE / Google Play).

## 1. Ringkasan: buka aplikasi sampai fitur utama

Sumber: `MainActivity.kt`, `MadsazApp.kt`, `feature/auth/AuthViewModel.kt`.

```mermaid
flowchart TD
    start((Mulai)) --> splash[Splash screen, set bahasa default id bila belum dipilih,<br/>buat kanal notifikasi]
    splash --> sesi{Sesi tersimpan<br/>di SessionManager?}
    sesi -- Tidak --> signedOut[Halaman masuk / daftar]
    signedOut --> pilihan{Pilihan pengguna}
    pilihan -- Masuk / Daftar --> auth[[Alur 2: Autentikasi]]
    pilihan -- Coba Gratis --> guest[[Alur 3: Coba Gratis]]
    pilihan -- Lupa kata sandi --> forgot[Isi email, kirim tautan reset] --> signedOut
    pilihan -- S&K / Privasi / Bahasa --> dok[Lihat dokumen / ganti bahasa] --> signedOut
    guest --> signedOut
    auth -- Gagal --> signedOut
    auth -- Berhasil --> signedIn
    sesi -- Ya --> signedIn[Masuk ke Beranda]

    signedIn --> bg[Latar belakang: daftar perangkat FCM,<br/>minta izin notifikasi sekali, sync pembelian Play tiap app aktif]
    bg --> push{Dibuka dari push<br/>destination = plan?}
    push -- Ya --> plan[[Alur 6: Paket & Langganan]]
    push -- Tidak --> home[Beranda: 3 rekaman terbaru + sisa kuota]
    home --> nav{Navigasi}
    nav -- Rekam --> rekam[[Alur 4: Rekam & Analisis]]
    nav -- Riwayat --> riwayat[Daftar rekaman] --> hasil[[Alur 5: Hasil & Feedback]]
    nav -- Pola Tangis --> cues[Daftar 5 pola tangis] --> cueDetail[Detail pola]
    nav -- Profil --> profil[[Alur 7: Profil & Akun]]
    nav -- Upgrade --> plan
    rekam --> hasil
    profil -- Keluar / Hapus akun / Sesi kedaluwarsa --> signedOut
```

## 2. Autentikasi (masuk / daftar)

Sumber: `AuthViewModel.authenticate`.

```mermaid
flowchart TD
    start((Mulai)) --> isi[Isi formulir]
    isi --> mode{Mode?}
    mode -- Masuk --> vMasuk{Email & kata sandi<br/>terisi?}
    vMasuk -- Tidak --> err1[Error: data belum lengkap] --> isi
    vMasuk -- Ya --> kirim
    mode -- Daftar --> vDaftar{Nama & email terisi,<br/>kata sandi >= 8 karakter?}
    vDaftar -- Tidak --> err2[Error: input tidak valid] --> isi
    vDaftar -- Ya --> vSama{Konfirmasi kata sandi<br/>sama?}
    vSama -- Tidak --> err3[Error: kata sandi tidak cocok] --> isi
    vSama -- Ya --> vSk{S&K disetujui?}
    vSk -- Tidak --> err4[Error: wajib setujui S&K] --> isi
    vSk -- Ya --> kirim[Status Submitting]
    kirim --> api[/BE: login atau register/]
    api --> ok{Berhasil?}
    ok -- Tidak --> err5[Tampilkan pesan error] --> isi
    ok -- Ya --> simpan[SessionManager.signIn: simpan sesi] --> selesai((Masuk Beranda))
```

## 3. Coba Gratis (tamu)

Sumber: `SignedOutApp.kt`, `GuestApi`, `GuestDevice`.

```mermaid
flowchart TD
    start((Tekan Coba Gratis)) --> limit{Analisis hari ini di perangkat<br/>>= batas harian dari BE?}
    limit -- Ya --> dialog[Dialog batas tamu]
    dialog --> pilih{Pilihan}
    pilih -- Daftar --> daftar((Form daftar))
    pilih -- Masuk --> masuk((Form masuk))
    limit -- Tidak / batas belum termuat --> rekam[Rekam video 5 detik<br/>sama seperti Alur 4]
    rekam --> analisa[/BE: analisis tamu dengan id perangkat/]
    analisa --> ok{Berhasil?}
    ok -- Tidak, guest_limit_reached --> errStop[Error tanpa tombol ulang, file dibuang] --> kembali
    ok -- Tidak, error lain --> errRetry[Error: ulangi kirim / buang rekaman]
    errRetry -- Ulangi --> analisa
    errRetry -- Buang --> kembali
    ok -- Ya --> catat[Catat 1 analisis hari ini di perangkat]
    catat --> hasil[Tampilkan skor pola tangis]
    hasil --> kembali((Kembali ke halaman masuk))
```

## 4. Rekam & Analisis (pengguna masuk)

Sumber: `MadsazApp.openRecorder`, `RecordingRoute.kt`, `RecordingViewModel.kt`.

```mermaid
flowchart TD
    start((Tekan Rekam)) --> cekPlan[/BE: status paket/]
    cekPlan --> planOk{Berhasil?}
    planOk -- Tidak --> toast[Toast error] --> stop((Tetap di layar asal))
    planOk -- Ya --> kuota{Paket Free<br/>dan sisa kuota = 0?}
    kuota -- Ya --> upgrade[Dialog Upgrade] --> up{Pilihan}
    up -- Upgrade --> plan((Alur 6: Paket))
    up -- Tutup --> stop
    kuota -- Tidak --> pending{Ada rekaman lama<br/>yang belum terkirim?}
    pending -- Ya --> errKirim
    pending -- Tidak --> izin{Izin kamera &<br/>mikrofon ada?}
    izin -- Tidak --> minta[Minta izin / buka Pengaturan] --> izin
    izin -- Ya --> siap[Pratinjau kamera, bisa ganti kamera depan/belakang]
    siap --> mulai[Mulai rekam, maks 5 detik]
    mulai --> henti{Berhenti karena<br/>5 detik / tombol stop / back}
    henti --> final{Hasil file<br/>bisa dipakai?}
    final -- Tidak --> errSimpan[Error simpan, file parsial dihapus] --> siap
    final -- Ya --> tandai[Tandai file siap kirim]
    tandai --> upload[Status Menganalisis<br/>upload video + durasi]
    upload --> be[/BE: simpan video, analisis asinkron/]
    be --> hasilUp{Upload berhasil?}
    hasilUp -- Ya --> hapus[Hapus salinan lokal] --> result((Alur 5: Hasil))
    hasilUp -- free_limit_reached --> errBatas[Error tanpa ulang, file dibuang] --> stop
    hasilUp -- Error lain --> errKirim[Error: ulangi kirim / buang rekaman]
    errKirim -- Ulangi --> upload
    errKirim -- Buang --> siap
```

## 5. Hasil & Feedback

Sumber: `ResultViewModel.kt` (poll tiap 2 detik, maks 45x), `FeedbackViewModel.kt`.

```mermaid
flowchart TD
    start((Buka hasil rekaman)) --> ambil[/BE: ambil rekaman/]
    ambil --> ok{Berhasil?}
    ok -- Tidak --> err[Error + tombol muat ulang] -- Muat ulang --> ambil
    ok -- Ya --> status{Status rekaman}
    status -- processing --> batas{Sudah 45 kali poll?}
    batas -- Belum --> tunggu[Tunggu 2 detik] --> ambil
    batas -- Ya --> habis[Berhenti menunggu, tawarkan Perbarui hasil] -- Perbarui --> ambil
    status -- failed --> gagal[Tampilkan analisis gagal]
    status -- completed --> tampil[Tampilkan skor pola tangis]
    tampil --> aksi{Aksi pengguna}
    aksi -- Putar video --> video[/BE: URL video berlaku 5 menit/] --> tampil
    aksi -- Buka pola --> cue[Detail pola tangis]
    aksi -- Kembali --> selesai((Layar sebelumnya))
    aksi -- Beri feedback --> fb[Isi rating, topik, catatan]
    fb --> kirim[/BE: kirim feedback/]
    kirim --> fbOk{Berhasil?}
    fbOk -- Tidak --> fbErr[Tampilkan error] --> fb
    fbOk -- Ya --> beranda((Kembali ke Beranda))
```

## 6. Paket & Langganan (Google Play Billing)

Sumber: `PlanViewModel.kt`, `data/billing/PlayBilling.kt`.

```mermaid
flowchart TD
    start((Buka lembar Paket)) --> muat[Paralel: katalog paket dari BE +<br/>refresh status langganan lalu status paket]
    muat --> ok{Berhasil?}
    ok -- Tidak --> err[Error + coba lagi] -- Coba lagi --> muat
    ok -- Ya --> harga[Muat harga bulanan/tahunan dari Google Play]
    harga --> sync[Sync pembelian Play yang belum diketahui BE]
    sync --> tampil[Tampilkan Free / Plus + harga]
    tampil --> beli{Pilih paket<br/>bulanan / tahunan}
    beli --> lain{Akun Google ini sudah<br/>bayar untuk akun Madsaz lain?}
    lain -- Ya --> jelas[Penjelasan: 1 akun Google = 1 Plus]
    jelas -- Tetap coba --> tersedia
    jelas -- Tutup --> tampil
    lain -- Tidak --> tersedia{Pembelian aktif dan<br/>penawaran tersedia?}
    tersedia -- Tidak --> errNA[Error: paket tidak tersedia] --> tampil
    tersedia -- Ya --> play[/Google Play: jendela pembelian/]
    play --> hasil{Hasil pembelian}
    hasil -- Dibatalkan --> tampil
    hasil -- Tertunda --> pend[Info: pembayaran tertunda] --> tampil
    hasil -- Sudah dimiliki --> own[Sync paksa: milik akun ini atau akun lain?] --> tampil
    hasil -- Gagal --> errBill[Error billing] --> tampil
    hasil -- Dibeli --> verify[/BE: verifikasi purchase token/]
    verify --> vOk{Terverifikasi?}
    vOk -- Tidak --> errBill
    vOk -- Ya --> muat
    tampil -- Tutup --> tutup[Perbarui sisa kuota di Beranda] --> selesai((Selesai))
```

## 7. Profil & Akun

Sumber: `ProfileScreen.kt`, `AuthViewModel.logout` / `deleteAccount`, `ChangePasswordViewModel.kt`.

```mermaid
flowchart TD
    start((Buka Profil)) --> profil[Muat profil, jumlah rekaman, penyimpanan]
    profil --> aksi{Aksi pengguna}
    aksi -- Ubah nama / avatar --> ubah[/BE: perbarui profil/] --> profil
    aksi -- Ganti kata sandi --> pw[/BE: ganti kata sandi/] --> profil
    aksi -- Notifikasi --> notif[Atur notifikasi pengingat] --> profil
    aksi -- Bahasa --> lang[Pilih id / en / ar, Activity dibuat ulang] --> profil
    aksi -- Privasi --> priv[/BE: kebijakan privasi sesuai bahasa/] --> profil
    aksi -- Paket --> plan((Alur 6: Paket))
    aksi -- Keluar --> unreg[Lepas token FCM, best-effort]
    unreg --> logoutBe[/BE: cabut refresh token, best-effort/]
    logoutBe --> signOut[Hapus sesi lokal]
    aksi -- Hapus akun --> konfirm[Masukkan kata sandi]
    konfirm --> del[/BE: hapus akun/]
    del --> delOk{Berhasil?}
    delOk -- Tidak --> delErr[Tampilkan error] --> profil
    delOk -- Ya --> bersih[Hapus rekaman lokal & token FCM] --> signOut
    signOut --> keluar((Halaman masuk))
```
