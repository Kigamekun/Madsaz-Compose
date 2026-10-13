package id.madsaz.app.data.remote

import com.sun.net.httpserver.HttpServer
import java.net.InetAddress
import java.net.InetSocketAddress
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.Executors

data class Request(val method: String, val path: String, val authorization: String?, val body: String)
data class Response(val status: Int, val body: String? = null)

/** Server HTTP lokal di JVM (JDK com.sun.net.httpserver) untuk menguji ApiClient dan SessionManager tanpa emulator. */
class FakeServer : AutoCloseable {
    private val server = HttpServer.create(InetSocketAddress(InetAddress.getLoopbackAddress(), 0), 0)
    private val handlers = mutableMapOf<String, (Request) -> Response>()

    val requests = CopyOnWriteArrayList<Request>()
    val baseUrl: String get() = "http://127.0.0.1:${server.address.port}"

    init {
        server.executor = Executors.newFixedThreadPool(8)
        server.createContext("/") { exchange ->
            val request = Request(
                method = exchange.requestMethod,
                path = exchange.requestURI.path,
                authorization = exchange.requestHeaders.getFirst("Authorization"),
                body = exchange.requestBody.readBytes().toString(Charsets.UTF_8),
            )
            requests += request
            val response = handlers["${request.method} ${request.path}"]?.invoke(request) ?: Response(404, """{"error":"Not Found"}""")
            val bytes = response.body?.toByteArray(Charsets.UTF_8)
            exchange.responseHeaders.add("Content-Type", "application/json")
            exchange.sendResponseHeaders(response.status, if (bytes == null) -1 else bytes.size.toLong())
            if (bytes != null) exchange.responseBody.use { it.write(bytes) }
            exchange.close()
        }
        server.start()
    }

    fun on(method: String, path: String, handler: (Request) -> Response) {
        handlers["$method $path"] = handler
    }

    fun count(method: String, path: String): Int = requests.count { it.method == method && it.path == path }

    override fun close() {
        server.stop(0)
        (server.executor as java.util.concurrent.ExecutorService).shutdownNow()
    }
}
