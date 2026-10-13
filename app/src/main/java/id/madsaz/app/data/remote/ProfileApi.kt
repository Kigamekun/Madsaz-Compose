package id.madsaz.app.data.remote

import org.json.JSONObject
import java.time.LocalDate

data class BabyProfile(
    val name: String,
    val birthDate: LocalDate?,
    val weightKg: Double?,
)

class ProfileApi(private val client: ApiClient) {
    fun getBabyProfile(): BabyProfile =
        parseBabyProfile(client.requestJson("GET", "/profile/baby").obj("data"))

    /** PUT mengganti seluruh profil: `null` menghapus tanggal lahir atau berat yang tersimpan. */
    fun updateBabyProfile(name: String, birthDate: LocalDate?, weightKg: Double?): BabyProfile {
        val body = JSONObject()
            .put("name", name)
            .put("birthDate", birthDate?.toString() ?: JSONObject.NULL)
            .put("weightKg", weightKg ?: JSONObject.NULL)
        return parseBabyProfile(client.requestJson("PUT", "/profile/baby", body).obj("data"))
    }

    // BE mengirim birthDate selalu sebagai YYYY-MM-DD (to_char) atau null.
    private fun parseBabyProfile(json: JSONObject) = BabyProfile(
        name = json.string("name"),
        birthDate = json.stringOrNull("birthDate")?.let(LocalDate::parse),
        weightKg = json.doubleOrNull("weightKg"),
    )
}
