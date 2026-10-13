package id.madsaz.app.data.remote

import org.json.JSONArray
import org.json.JSONObject

/** BE mengirim bentuk yang berbeda dari kontrak (field hilang atau bertipe lain). Gagal keras, jangan ditebak. */
class ContractException(message: String) : IllegalStateException(message)

// org.json di Android mengoersi tipe (getString pada null menghasilkan "null", getLong pada string menghasilkan angka).
// Helper ini memeriksa tipe sebenarnya agar penyimpangan kontrak langsung terlihat.

fun JSONObject.string(key: String): String =
    opt(key) as? String ?: throw ContractException("Field '$key' must be a string")

fun JSONObject.stringOrNull(key: String): String? =
    if (isNull(key)) null else string(key)

fun JSONObject.int(key: String): Int =
    (opt(key) as? Number)?.takeIf { it.toDouble() == it.toInt().toDouble() }?.toInt()
        ?: throw ContractException("Field '$key' must be an integer")

fun JSONObject.intOrNull(key: String): Int? =
    if (isNull(key)) null else int(key)

fun JSONObject.long(key: String): Long =
    (opt(key) as? Number)?.toLong() ?: throw ContractException("Field '$key' must be a number")

fun JSONObject.double(key: String): Double =
    (opt(key) as? Number)?.toDouble() ?: throw ContractException("Field '$key' must be a number")

fun JSONObject.doubleOrNull(key: String): Double? =
    if (isNull(key)) null else double(key)

fun JSONObject.boolean(key: String): Boolean =
    opt(key) as? Boolean ?: throw ContractException("Field '$key' must be a boolean")

fun JSONObject.obj(key: String): JSONObject =
    opt(key) as? JSONObject ?: throw ContractException("Field '$key' must be an object")

fun JSONObject.array(key: String): JSONArray =
    opt(key) as? JSONArray ?: throw ContractException("Field '$key' must be an array")

fun JSONObject.arrayOrNull(key: String): JSONArray? =
    if (isNull(key)) null else array(key)

fun JSONArray.objects(): List<JSONObject> =
    (0 until length()).map { index -> optJSONObject(index) ?: throw ContractException("Element $index must be an object") }
