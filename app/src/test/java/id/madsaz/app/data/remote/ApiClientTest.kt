package id.madsaz.app.data.remote

import org.json.JSONObject
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.concurrent.atomic.AtomicInteger

class ApiClientTest {
    private val server = FakeServer()

    @After
    fun tearDown() = server.close()

    /** Credentials palsu: token awal "old"; refresh mengganti menjadi "new". */
    private class FakeCredentials(var token: String = "old", val failRefresh: ApiException? = null) : Credentials {
        val refreshCalls = AtomicInteger()
        override fun accessToken() = token
        override fun refreshAfterUnauthorized(failedToken: String) {
            refreshCalls.incrementAndGet()
            failRefresh?.let { throw it }
            token = "new"
        }
    }

    @Test
    fun `request without credentials sends no Authorization header and parses JSON`() {
        server.on("POST", "/auth/login") { Response(200, """{"ok":true}""") }
        val json = ApiClient(server.baseUrl).requestJson("POST", "/auth/login", JSONObject().put("email", "a@b.c"))

        assertTrue(json.getBoolean("ok"))
        assertNull(server.requests.single().authorization)
        assertEquals("""{"email":"a@b.c"}""", server.requests.single().body)
    }

    @Test
    fun `error body becomes ApiException with status and code`() {
        server.on("GET", "/plan") { Response(403, """{"error":"Monthly Free plan limit reached","code":"free_limit_reached"}""") }
        val error = assertThrows(ApiException::class.java) { ApiClient(server.baseUrl).requestJson("GET", "/plan") }

        assertEquals(403, error.status)
        assertEquals("free_limit_reached", error.code)
        assertEquals("Monthly Free plan limit reached", error.message)
    }

    @Test
    fun `non JSON error body still yields ApiException with the status`() {
        server.on("GET", "/plan") { Response(502, "<html>Bad Gateway</html>") }
        val error = assertThrows(ApiException::class.java) { ApiClient(server.baseUrl).requestJson("GET", "/plan") }

        assertEquals(502, error.status)
        assertNull(error.code)
    }

    @Test
    fun `204 returns no body and an empty body is rejected where JSON is required`() {
        server.on("DELETE", "/records/x") { Response(204) }
        server.on("GET", "/empty") { Response(200) }
        val client = ApiClient(server.baseUrl)

        client.requestNoContent("DELETE", "/records/x")
        assertThrows(ContractException::class.java) { client.requestJson("GET", "/empty") }
    }

    @Test
    fun `401 refreshes once and retries with the new token`() {
        server.on("GET", "/plan") { request ->
            if (request.authorization == "Bearer new") Response(200, """{"data":{}}""") else Response(401, """{"error":"Invalid or expired token","code":"invalid_token"}""")
        }
        val credentials = FakeCredentials()

        val json = ApiClient(server.baseUrl, credentials).requestJson("GET", "/plan")

        assertTrue(json.has("data"))
        assertEquals(1, credentials.refreshCalls.get())
        assertEquals(listOf("Bearer old", "Bearer new"), server.requests.map { it.authorization })
    }

    @Test
    fun `a 401 that persists after refreshing is surfaced instead of looping`() {
        server.on("GET", "/plan") { Response(401, """{"error":"Invalid or expired token","code":"invalid_token"}""") }
        val credentials = FakeCredentials()

        val error = assertThrows(ApiException::class.java) { ApiClient(server.baseUrl, credentials).requestJson("GET", "/plan") }

        assertEquals(401, error.status)
        assertEquals(1, credentials.refreshCalls.get())
        assertEquals(2, server.count("GET", "/plan"))
    }

    @Test
    fun `refresh failure ends the request with that failure and does not retry`() {
        server.on("GET", "/plan") { Response(401, """{"error":"Invalid or expired token"}""") }
        val credentials = FakeCredentials(failRefresh = ApiException(401, "invalid_refresh_token", "Invalid or expired refresh token"))

        val error = assertThrows(ApiException::class.java) { ApiClient(server.baseUrl, credentials).requestJson("GET", "/plan") }

        assertEquals("invalid_refresh_token", error.code)
        assertEquals(1, server.count("GET", "/plan"))
    }

    @Test
    fun `401 from an unauthenticated client is never retried`() {
        server.on("POST", "/auth/login") { Response(401, """{"error":"Invalid email or password","code":"invalid_credentials"}""") }

        val error = assertThrows(ApiException::class.java) { ApiClient(server.baseUrl).requestJson("POST", "/auth/login", JSONObject()) }

        assertEquals("invalid_credentials", error.code)
        assertEquals(1, server.count("POST", "/auth/login"))
    }
}
