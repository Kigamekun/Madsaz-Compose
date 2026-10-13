package id.madsaz.app.data.remote

import id.madsaz.app.data.local.AuthSession
import id.madsaz.app.data.local.SessionStorage
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

class SessionManagerTest {
    private val server = FakeServer()

    @After
    fun tearDown() = server.close()

    private class MemoryStorage(initial: AuthSession?) : SessionStorage {
        var stored: AuthSession? = initial
        override fun read() = stored
        override fun write(session: AuthSession) { stored = session }
        override fun clear() { stored = null }
    }

    private fun session(access: String, refresh: String) = AuthSession("user-1", access, refresh, "a@b.test")

    private fun authJson(access: String, refresh: String) =
        """{"user":{"id":"user-1","email":"a@b.test","name":"Bayi","role":"user"},"accessToken":"$access","refreshToken":"$refresh","tokenType":"Bearer","expiresIn":900}"""

    private fun manager(storage: MemoryStorage) = SessionManager(storage, AuthApi(ApiClient(server.baseUrl)))

    /** Refresh token BE sekali pakai: hanya "refresh-1" yang diterima, dan hanya sekali. */
    private fun serveSingleUseRefresh(refreshCalls: AtomicInteger) {
        server.on("POST", "/auth/refresh") { request ->
            refreshCalls.incrementAndGet()
            if (request.body.contains("refresh-1") && refreshCalls.get() == 1) Response(200, authJson("access-2", "refresh-2"))
            else Response(401, """{"error":"Invalid or expired refresh token","code":"invalid_refresh_token"}""")
        }
    }

    @Test
    fun `refresh stores the rotated tokens`() {
        serveSingleUseRefresh(AtomicInteger())
        val storage = MemoryStorage(session("access-1", "refresh-1"))
        val manager = manager(storage)

        manager.refreshAfterUnauthorized("access-1")

        assertEquals(session("access-2", "refresh-2"), manager.session.value)
        assertEquals(session("access-2", "refresh-2"), storage.stored)
    }

    @Test
    fun `parallel 401s trigger a single refresh because the refresh token is single use`() {
        val refreshCalls = AtomicInteger()
        serveSingleUseRefresh(refreshCalls)
        val manager = manager(MemoryStorage(session("access-1", "refresh-1")))
        val pool = Executors.newFixedThreadPool(8)
        val start = CountDownLatch(1)

        val results = (1..8).map {
            pool.submit<Throwable?> {
                start.await()
                runCatching { manager.refreshAfterUnauthorized("access-1") }.exceptionOrNull()
            }
        }
        start.countDown()
        val failures = results.map { it.get(10, TimeUnit.SECONDS) }
        pool.shutdownNow()

        assertEquals("tepat satu refresh ke BE", 1, refreshCalls.get())
        assertTrue("tak satu pun pemanggil boleh gagal: $failures", failures.all { it == null })
        assertNotNull(manager.session.value)
        assertEquals("access-2", manager.accessToken())
    }

    @Test
    fun `a refresh token rejected by the server ends the session`() {
        server.on("POST", "/auth/refresh") { Response(401, """{"error":"Invalid or expired refresh token","code":"invalid_refresh_token"}""") }
        val storage = MemoryStorage(session("access-1", "refresh-1"))
        val manager = manager(storage)

        val error = assertThrows(ApiException::class.java) { manager.refreshAfterUnauthorized("access-1") }

        assertEquals(401, error.status)
        assertNull(manager.session.value)
        assertNull(storage.stored)
        assertTrue(manager.consumeEndedByServer())
        assertFalse("flag hanya berlaku sekali", manager.consumeEndedByServer())
    }

    @Test
    fun `a network failure during refresh keeps the session for a later retry`() {
        val storage = MemoryStorage(session("access-1", "refresh-1"))
        val unreachable = SessionManager(storage, AuthApi(ApiClient("http://127.0.0.1:1")))

        assertThrows(IOException::class.java) { unreachable.refreshAfterUnauthorized("access-1") }

        assertEquals(session("access-1", "refresh-1"), unreachable.session.value)
        assertEquals(session("access-1", "refresh-1"), storage.stored)
        assertFalse(unreachable.consumeEndedByServer())
    }

    @Test
    fun `logout clears the session without flagging it as ended by the server`() {
        val storage = MemoryStorage(session("access-1", "refresh-1"))
        val manager = manager(storage)

        manager.signOut()

        assertNull(manager.session.value)
        assertNull(storage.stored)
        assertFalse(manager.consumeEndedByServer())
        assertEquals(401, assertThrows(ApiException::class.java) { manager.accessToken() }.status)
    }

    @Test
    fun `an expired access token is refreshed transparently through ApiClient`() {
        server.on("GET", "/plan") { request ->
            if (request.authorization == "Bearer access-2") Response(200, """{"data":{"code":"free"}}""")
            else Response(401, """{"error":"Invalid or expired token","code":"invalid_token"}""")
        }
        serveSingleUseRefresh(AtomicInteger())
        val manager = manager(MemoryStorage(session("access-1", "refresh-1")))

        val json = ApiClient(server.baseUrl, manager).requestJson("GET", "/plan")

        assertEquals("free", json.getJSONObject("data").getString("code"))
        assertEquals(1, server.count("POST", "/auth/refresh"))
        // Panggilan berikutnya langsung memakai token baru tanpa refresh lagi.
        ApiClient(server.baseUrl, manager).requestJson("GET", "/plan")
        assertEquals(1, server.count("POST", "/auth/refresh"))
    }
}
