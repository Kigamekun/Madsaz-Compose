package id.madsaz.app.data.remote

import id.madsaz.app.model.RecordingStatus
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant
import java.time.LocalDate

/** Parser mengikuti kontrak BE secara ketat: bentuk yang menyimpang gagal keras, tidak ditebak. */
class ContractTest {
    private val server = FakeServer()

    @After
    fun tearDown() = server.close()

    private fun recordings() = RecordingApi(ApiClient(server.baseUrl))

    private fun recordingJson(
        status: String = "completed",
        size: String = "1048576",
        confidence: String = "0.68",
        scores: String = """[{"code":"Neh","label":"Lapar","confidence":0.68}]""",
        duration: String = "7",
    ) = """{"id":"f33d1075-5369-80d1-2a58-44b86b500c21","status":"$status","created_at":"2026-10-13T00:12:12.383Z",
        "duration_seconds":$duration,"size_bytes":$size,"category":"Neh","confidence":$confidence,"solution":"Periksa","source":"mock","scores":$scores}"""

    @Test
    fun `a completed recording is parsed field by field`() {
        server.on("GET", "/records/x/result") { Response(200, """{"data":${recordingJson()}}""") }

        val recording = recordings().get("x")

        assertEquals(RecordingStatus.COMPLETED, recording.status)
        assertEquals(1_048_576L, recording.sizeBytes)
        assertEquals(7_000L, recording.durationMillis)
        assertEquals(1_791_850_332_383L, recording.recordedAtEpochMillis)
        assertEquals(listOf("Neh"), recording.scores.map { it.code })
        assertEquals(0.68, recording.scores.single().confidence, 0.0)
    }

    @Test
    fun `a recording without analysis has null scores and category`() {
        server.on("GET", "/records") {
            Response(200, """{"data":[{"id":"a","status":"processing","created_at":"2026-10-13T00:12:12.383Z",
                "duration_seconds":null,"size_bytes":10,"category":null,"confidence":null,"solution":null,"source":null,"scores":null}]}""")
        }

        val recording = recordings().list().single()

        assertEquals(RecordingStatus.PROCESSING, recording.status)
        assertNull(recording.category)
        assertNull(recording.durationMillis)
        assertTrue(recording.scores.isEmpty())
    }

    @Test
    fun `numbers sent as strings are a contract violation, not silently coerced`() {
        server.on("GET", "/records/x/result") { Response(200, """{"data":${recordingJson(size = "\"1048576\"")}}""") }

        assertThrows(ContractException::class.java) { recordings().get("x") }
    }

    @Test
    fun `an unknown status is rejected`() {
        server.on("GET", "/records/x/result") { Response(200, """{"data":${recordingJson(status = "archived")}}""") }

        assertThrows(IllegalStateException::class.java) { recordings().get("x") }
    }

    @Test
    fun `a missing required field is rejected`() {
        server.on("GET", "/records/x/result") { Response(200, """{"data":{"id":"a","status":"completed"}}""") }

        assertThrows(ContractException::class.java) { recordings().get("x") }
    }

    @Test
    fun `plan status distinguishes Free with limits from Plus without`() {
        val api = PlanApi(ApiClient(server.baseUrl))
        server.on("GET", "/plan") { Response(200, """{"data":{"code":"free","used":3,"limit":5,"remaining":2}}""") }
        assertEquals(PlanStatus.Free(used = 3, limit = 5, remaining = 2), api.status())

        server.on("GET", "/plan") {
            Response(200, """{"data":{"code":"plus","used":9,"limit":null,"remaining":null,"expires_at":"2026-11-13T00:12:12.383Z","auto_renewing":true}}""")
        }
        assertEquals(PlanStatus.Plus(used = 9, expiresAt = Instant.parse("2026-11-13T00:12:12.383Z"), autoRenewing = true), api.status())

        // Plus tanpa masa aktif dari BE adalah pelanggaran kontrak, bukan "tanpa batas waktu".
        server.on("GET", "/plan") { Response(200, """{"data":{"code":"plus","used":9,"limit":null,"remaining":null,"expires_at":null,"auto_renewing":false}}""") }
        assertThrows(ContractException::class.java) { api.status() }

        server.on("GET", "/plan") { Response(200, """{"data":{"code":"free","used":3,"limit":null,"remaining":null}}""") }
        assertThrows(ContractException::class.java) { api.status() }

        server.on("GET", "/plan") { Response(200, """{"data":{"code":"gold","used":0,"limit":null,"remaining":null}}""") }
        assertThrows(ContractException::class.java) { api.status() }
    }

    @Test
    fun `baby profile keeps null birth date and weight and parses the date once`() {
        val api = ProfileApi(ApiClient(server.baseUrl))
        server.on("GET", "/profile/baby") { Response(200, """{"data":{"id":"b","name":"Pandu","birthDate":"2026-01-31","weightKg":6.8}}""") }
        assertEquals(LocalDate.of(2026, 1, 31), api.getBabyProfile().birthDate)
        assertEquals(6.8, api.getBabyProfile().weightKg ?: 0.0, 0.0)

        server.on("GET", "/profile/baby") { Response(200, """{"data":{"id":"b","name":"Pandu","birthDate":null,"weightKg":null}}""") }
        val empty = api.getBabyProfile()
        assertNull(empty.birthDate)
        assertNull(empty.weightKg)

        server.on("GET", "/profile/baby") { Response(200, """{"data":{"id":"b","name":"Pandu","birthDate":"31/01/2026","weightKg":null}}""") }
        assertThrows(java.time.format.DateTimeParseException::class.java) { api.getBabyProfile() }
    }

    @Test
    fun `updating the profile sends explicit nulls because PUT replaces the whole profile`() {
        val api = ProfileApi(ApiClient(server.baseUrl))
        server.on("PUT", "/profile/baby") { Response(200, """{"data":{"id":"b","name":"Pandu","birthDate":null,"weightKg":null}}""") }

        api.updateBabyProfile("Pandu", birthDate = null, weightKg = null)

        val body = server.requests.single().body
        assertTrue(body, body.contains(""""birthDate":null""") && body.contains(""""weightKg":null"""))
    }

    @Test
    fun `auth responses are parsed and a missing token is rejected`() {
        val api = AuthApi(ApiClient(server.baseUrl))
        server.on("POST", "/auth/login") {
            Response(200, """{"user":{"id":"u","email":"a@b.test","name":null,"role":"user"},"accessToken":"t","refreshToken":"r","tokenType":"Bearer","expiresIn":900}""")
        }
        assertEquals("u", api.login("a@b.test", "x").userId)

        server.on("POST", "/auth/login") { Response(200, """{"user":{"id":"u","email":"a@b.test","name":null,"role":"user"},"refreshToken":"r"}""") }
        assertThrows(ContractException::class.java) { api.login("a@b.test", "x") }
    }

    @Test
    fun `deleting an account posts the credentials and treats 204 as success`() {
        server.on("POST", "/auth/delete-account") { Response(204) }

        AuthApi(ApiClient(server.baseUrl)).deleteAccount("a@b.test", "password-uji-1")

        val body = org.json.JSONObject(server.requests.single().body)
        assertEquals("a@b.test", body.getString("email"))
        assertEquals("password-uji-1", body.getString("password"))
        assertEquals(2, body.length())
    }
}
