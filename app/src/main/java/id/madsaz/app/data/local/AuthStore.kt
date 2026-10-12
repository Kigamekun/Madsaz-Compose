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
    val name: String?,
)

class AuthStore(context: Context) {
    private val preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    fun read(): AuthSession? {
        val encrypted = preferences.getString(SESSION_KEY, null) ?: return null
        return try {
            val json = JSONObject(decrypt(encrypted))
            AuthSession(
                userId = json.optString("userId"),
                accessToken = json.getString("accessToken"),
                refreshToken = json.getString("refreshToken"),
                email = json.getString("email"),
                name = json.optString("name").ifBlank { null },
            )
        } catch (_: Exception) {
            clear()
            null
        }
    }

    fun write(session: AuthSession) {
        val json = JSONObject()
            .put("userId", session.userId)
            .put("accessToken", session.accessToken)
            .put("refreshToken", session.refreshToken)
            .put("email", session.email)
            .put("name", session.name.orEmpty())
        preferences.edit().putString(SESSION_KEY, encrypt(json.toString())).apply()
    }

    fun clear() {
        preferences.edit().remove(SESSION_KEY).apply()
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
