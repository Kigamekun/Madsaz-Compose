# Rilis lewat GitHub Actions (app `com.madsaz`)

Aplikasi ini memperbarui app yang sudah terbit di Play (`com.madsaz`, versi lama 1.2.1 / versionCode 14).
Push ke branch **`release/closed-testing`** menjalankan `.github/workflows/release-closed-testing.yml`:
test JVM, lint rilis, build AAB bertanda tangan, lalu unggah ke track **closed testing** (`alpha`; Google meninjau rilis closed testing, dan daftar penguji harus sudah diisi di Play Console).
`versionCode` otomatis `1000 + nomor run`, selalu di atas versi lama. `versionName` dari `app/build.gradle.kts`.
Catatan rilis dari `distribution/whatsnew/` (maksimal 500 karakter per bahasa); ubah sebelum push.

Dari closed testing, promosikan ke **Production** dari Play Console dengan **staged rollout** (mis. 10% dulu).
Production selalu melalui review Google.

## Kunci penandatangan: wajib kunci unggah app lama
Play hanya menerima update `com.madsaz` yang ditandatangani **kunci unggah yang terdaftar** untuk app itu
(Play Console > Test and release > App integrity). Dari proyek lama, kunci itu adalah `my-release-key.jks`
(alias dan password ada di `gradle.properties` proyek lama). Pastikan sidik jari SHA-1 `D9:ED:82:74:…:56:FC`
(dari `upload_certificate.pem`) sama dengan **Upload key certificate** di Play Console. Bila beda atau kunci hilang,
minta **upload key reset** di Play Console (butuh beberapa hari).
`keystore.properties` lokal juga harus menunjuk ke kunci itu, bukan `madsaz-upload.jks` (kunci khusus `id.madsaz.app`).

## Sekali saja
1. **Firebase:** daftarkan Android app **`com.madsaz`** (Firebase console > Project settings > Add app), unduh
   `google-services.json` baru ke `app/google-services.json`. Berkas lama untuk `id.madsaz.app` sudah dipindah ke `~/madsaz-keys/`.
   Tanpa berkas yang cocok dengan `com.madsaz`, build gagal ("No matching client found").
2. **Service account Play API:** di Play Console > Users and permissions, tambahkan app **Madsaz Baby Cry Translator**
   (`com.madsaz`) ke izin service account `madsaz-play-release` (izin rilis ke jalur testing; jangan izin production).
3. **Secret** GitHub (environment `play-closed-testing`) diperbarui:

   | Secret | Isi |
   |---|---|
   | `UPLOAD_KEYSTORE_BASE64` | `base64 -i /Users/Shared/Projects/Madsaz/my-release-key.jks \| pbcopy` |
   | `UPLOAD_STORE_PASSWORD`, `UPLOAD_KEY_ALIAS`, `UPLOAD_KEY_PASSWORD` | dari `gradle.properties` proyek lama (`MYAPP_UPLOAD_*`) |
   | `GOOGLE_SERVICES_JSON_BASE64` | `base64 -i app/google-services.json \| pbcopy` (berkas `com.madsaz`) |
   | `PLAY_SERVICE_ACCOUNT_JSON`, `MADSAZ_API_BASE_URL` | tidak berubah |

4. **Backend (Render):** set `PLAY_PACKAGE_NAME=com.madsaz`. Produk langganan dibuat di app `com.madsaz`.

## Tiap rilis
```bash
git checkout -B release/closed-testing main && git push -f origin release/closed-testing
```
Setujui run di tab Actions; AAB masuk ke track closed testing. Uji dari HP penguji, lalu promosikan ke production bertahap.
