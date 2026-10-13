package id.madsaz.app.data.local

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import org.json.JSONObject
import java.nio.charset.StandardCharsets
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

data class AuthSession(
    val userId: String,
    val accessToken: String,
    val refreshToken: String,
    val email: String,
)

/** Penyimpanan sesi; dipisah dari Android agar logika refresh di SessionManager bisa diuji di JVM. */
interface SessionStorage {
    fun read(): AuthSession?
    fun write(session: AuthSession)
    fun clear()
}

/** Sesi terenkripsi AES-GCM dengan kunci AndroidKeyStore. [write] dan [clear] sinkron (commit): refresh token lama sudah dicabut BE. */
class AuthStore(context: Context) : SessionStorage {
    private val preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    override fun read(): AuthSession? {
        val encrypted = preferences.getString(SESSION_KEY, null) ?: return null
        return try {
            val json = JSONObject(decrypt(encrypted))
            AuthSession(
                userId = json.getString("userId"),
                accessToken = json.getString("accessToken"),
                refreshToken = json.getString("refreshToken"),
                email = json.getString("email"),
            )
        } catch (_: Exception) {
            // Tak terbaca (kunci hilang, data rusak): sesi tidak bisa dipulihkan, jadi keluar.
            clear()
            null
        }
    }

    override fun write(session: AuthSession) {
        val json = JSONObject()
            .put("userId", session.userId)
            .put("accessToken", session.accessToken)
            .put("refreshToken", session.refreshToken)
            .put("email", session.email)
        check(preferences.edit().putString(SESSION_KEY, encrypt(json.toString())).commit()) { "Gagal menyimpan sesi" }
    }

    override fun clear() {
        preferences.edit().remove(SESSION_KEY).commit()
    }

    private fun encrypt(value: String): String {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey())
        val iv = Base64.encodeToString(cipher.iv, Base64.NO_WRAP)
        val data = Base64.encodeToString(cipher.doFinal(value.toByteArray(StandardCharsets.UTF_8)), Base64.NO_WRAP)
        return "$iv:$data"
    }

    private fun decrypt(value: String): String {
        val (encodedIv, encodedData) = value.split(':', limit = 2)
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(
            Cipher.DECRYPT_MODE,
            secretKey(),
            GCMParameterSpec(128, Base64.decode(encodedIv, Base64.NO_WRAP)),
        )
        return String(cipher.doFinal(Base64.decode(encodedData, Base64.NO_WRAP)), StandardCharsets.UTF_8)
    }

    private fun secretKey(): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEY_STORE).apply { load(null) }
        val existingKey = keyStore.getKey(KEY_ALIAS, null) as? SecretKey
        if (existingKey != null) return existingKey

        return KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEY_STORE).run {
            init(
                KeyGenParameterSpec.Builder(
                    KEY_ALIAS,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT,
                )
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setUserAuthenticationRequired(false)
                    .build(),
            )
            generateKey()
        }
    }

    private companion object {
        const val ANDROID_KEY_STORE = "AndroidKeyStore"
        const val KEY_ALIAS = "madsaz_auth_key"
        const val PREFERENCES_NAME = "madsaz_auth"
        const val SESSION_KEY = "session"
        const val TRANSFORMATION = "AES/GCM/NoPadding"
    }
}
