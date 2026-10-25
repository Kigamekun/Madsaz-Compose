package id.madsaz.app.data.remote

data class PolicySection(val title: String, val body: String)

/** [status] "draft" selama BE belum menerbitkan versi final. */
data class PrivacyPolicy(val title: String, val version: String, val status: String, val sections: List<PolicySection>)

/** Dokumen publik (tanpa token) dalam id, en, atau ar. */
class PolicyApi(private val client: ApiClient) {
    fun privacyPolicy(language: String): PrivacyPolicy = document("/privacy-policy?lang=$language")

    fun terms(language: String): PrivacyPolicy = document("/terms?lang=$language")

    private fun document(path: String): PrivacyPolicy {
        val data = client.requestJson("GET", path).obj("data")
        return PrivacyPolicy(
            title = data.string("title"),
            version = data.string("version"),
            status = data.string("status"),
            sections = data.array("sections").objects().map { PolicySection(it.string("title"), it.string("body")) },
        )
    }
}
