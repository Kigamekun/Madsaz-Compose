package id.madsaz.app.data.remote

import org.json.JSONObject

class NotificationApi(private val client: ApiClient) {
    fun register(fcmToken: String) {
        client.requestNoContent("POST", "/notifications/device", JSONObject().put("token", fcmToken))
    }

    fun unregister(fcmToken: String) {
        client.requestNoContent("DELETE", "/notifications/device", JSONObject().put("token", fcmToken))
    }
}
