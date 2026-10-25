package id.madsaz.app.data.remote

import id.madsaz.app.data.local.AuthSession
import org.json.JSONObject

/** Endpoint tanpa bearer token; [client] harus dibuat tanpa [Credentials]. */
class AuthApi(private val client: ApiClient) {
    fun login(email: String, password: String): AuthSession =
        session(client.requestJson("POST", "/auth/login", JSONObject().put("email", email).put("password", password)))

    /** [acceptTerms] selalu true: pemanggil sudah memastikan pengguna menyetujui Syarat & Ketentuan (BE menolak tanpa itu). */
    fun register(name: String, email: String, password: String, acceptTerms: Boolean): AuthSession =
        session(
            client.requestJson(
                "POST", "/auth/register",
                JSONObject().put("name", name).put("email", email).put("password", password).put("accept_terms", acceptTerms),
            ),
        )

    /** BE menjawab 202 untuk email apa pun; 503 `email_unavailable` bila server belum punya SMTP. */
    fun forgotPassword(email: String, language: String) {
        client.requestJson("POST", "/auth/forgot-password", JSONObject().put("email", email).put("language", language))
    }

    fun refresh(refreshToken: String): AuthSession =
        session(client.requestJson("POST", "/auth/refresh", JSONObject().put("refreshToken", refreshToken)))

    fun logout(refreshToken: String) {
        client.requestNoContent("POST", "/auth/logout", JSONObject().put("refreshToken", refreshToken))
    }

    /** Menghapus akun beserta semua datanya secara permanen. 401 bila kata sandi salah; tidak ada yang dihapus. */
    fun deleteAccount(email: String, password: String) {
        client.requestNoContent("POST", "/auth/delete-account", JSONObject().put("email", email).put("password", password))
    }

    private fun session(json: JSONObject): AuthSession = parseAuthSession(json)
}

/** Bentuk sesi yang sama dipakai login, daftar, refresh, dan ganti kata sandi. */
internal fun parseAuthSession(json: JSONObject): AuthSession {
    val user = json.obj("user")
    return AuthSession(
        userId = user.string("id"),
        accessToken = json.string("accessToken"),
        refreshToken = json.string("refreshToken"),
        email = user.string("email"),
    )
}
