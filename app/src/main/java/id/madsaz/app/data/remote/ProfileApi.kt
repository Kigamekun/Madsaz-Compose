package id.madsaz.app.data.remote

import org.json.JSONObject

data class AccountProfile(val id: String, val name: String, val email: String, val avatarUrl: String?)

class ProfileApi(private val client: ApiClient) {
    fun get(): AccountProfile = parse(client.requestJson("GET", "/profile").obj("data"))

    fun updateName(name: String): AccountProfile =
        parse(client.requestJson("PUT", "/profile", JSONObject().put("name", name)).obj("data"))

    /** upload-url -> PUT ke penyimpanan -> baca ulang profil untuk mendapat URL avatar yang baru. */
    fun uploadAvatar(jpeg: ByteArray): AccountProfile {
        val target = client.requestJson("POST", "/profile/avatar-upload-url", JSONObject().put("size_bytes", jpeg.size)).obj("data")
        putToStorage(target.string("uploadUrl"), "image/jpeg", jpeg.size.toLong(), AVATAR_READ_TIMEOUT_MS) { it.write(jpeg) }
        return get()
    }

    private fun parse(json: JSONObject) = AccountProfile(
        id = json.string("id"),
        name = json.string("name"),
        email = json.string("email"),
        avatarUrl = json.stringOrNull("avatarUrl"),
    )

    private companion object {
        const val AVATAR_READ_TIMEOUT_MS = 60_000
    }
}
