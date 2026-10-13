# Rilis ke Closed testing (Alpha) lewat GitHub Actions

Push ke branch **`release/closed-testing`** menjalankan `.github/workflows/release-closed-testing.yml`:
test JVM, lint rilis, build AAB bertanda tangan, lalu unggah ke track **alpha** (Closed testing) di Play Console.
`versionCode` otomatis `1000 + nomor run`, jadi selalu naik. `versionName` diambil dari `app/build.gradle.kts`.
Catatan rilis diambil dari `distribution/whatsnew/` (maksimal 500 karakter per bahasa); ubah sebelum push.

Rilis pertama aplikasi harus manual (sudah dilakukan). Unggahan lewat API tetap melalui review Google seperti biasa.

## Sekali saja

1. **Service account untuk Play API.** Google Cloud: aktifkan *Google Play Android Developer API*, buat service account dan kunci JSON.
   Play Console > *Users and permissions* > undang email service account itu dengan izin **Release manager** (atau minimal
   *Release apps to testing tracks* untuk aplikasi ini).
2. **Environment GitHub** `play-closed-testing` (Settings > Environments). Aktifkan **Required reviewers** agar tiap rilis
   butuh persetujuan Anda; batasi *Deployment branches* ke `release/closed-testing`.
3. **Secret** di environment itu (Settings > Environments > play-closed-testing > Secrets):

   | Secret | Isi |
   |---|---|
   | `PLAY_SERVICE_ACCOUNT_JSON` | isi berkas JSON service account (teks apa adanya) |
   | `UPLOAD_KEYSTORE_BASE64` | `base64 -i madsaz-upload.jks \| pbcopy` |
   | `UPLOAD_STORE_PASSWORD`, `UPLOAD_KEY_ALIAS`, `UPLOAD_KEY_PASSWORD` | sama dengan `keystore.properties` lokal |
   | `GOOGLE_SERVICES_JSON_BASE64` | `base64 -i app/google-services.json \| pbcopy` |
   | `MADSAZ_API_BASE_URL` | `https://madsaz-be.onrender.com/api/v1` |

4. Lindungi branch `release/closed-testing` (hanya Anda yang boleh push).

## Tiap rilis

```bash
git checkout release/closed-testing && git merge --ff-only main && git push origin release/closed-testing
```

Setujui run di tab Actions. Release masuk sebagai **draft** di Closed testing: buka Play Console > Closed testing > Releases,
periksa, lalu kirim review (workflow memakai `status: draft` selama aplikasi belum pernah dipublikasikan ke production).

Kunci unggah dan secret tidak pernah masuk repo; workflow menulisnya ke berkas sementara dan menghapusnya di akhir.
