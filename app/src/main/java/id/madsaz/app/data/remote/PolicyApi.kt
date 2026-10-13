package id.madsaz.app.data.remote

data class PolicySection(val title: String, val body: String)

/** [status] "draft" selama BE belum menerbitkan versi final. */
data class PrivacyPolicy(val title: String, val version: String, val status: String, val sections: List<PolicySection>)

class PolicyApi(private val client: ApiClient) {
    /** BE punya teks Indonesia ("id") dan Inggris ("en"). */
    fun privacyPolicy(language: String): PrivacyPolicy {
        val data = client.requestJson("GET", "/privacy-policy?lang=$language").obj("data")
        return PrivacyPolicy(
            title = data.string("title"),
            version = data.string("version"),
            status = data.string("status"),
            sections = data.array("sections").objects().map { PolicySection(it.string("title"), it.string("body")) },
        )
    }
}
