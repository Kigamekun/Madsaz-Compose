package id.madsaz.app.data.local

import android.content.Context
import java.time.LocalDate
import java.time.ZoneId
import java.util.UUID

/**
 * Identitas anonim untuk Coba Gratis: UUID acak yang dibuat sekali per instalasi (bukan ID perangkat keras) dan hitungan
 * analisis hari ini (WIB) agar app tidak membuat pengguna merekam lalu ditolak BE. BE tetap yang memutuskan batasnya.
 */
class GuestDevice(context: Context) {
    private val preferences = context.getSharedPreferences("guest", Context.MODE_PRIVATE)

    fun id(): String = preferences.getString(DEVICE_ID, null)
        ?: UUID.randomUUID().toString().also { preferences.edit().putString(DEVICE_ID, it).apply() }

    /** Jumlah analisis tamu yang selesai hari ini (WIB). */
    fun analysesToday(): Int = if (preferences.getString(DAY, null) == today()) preferences.getInt(COUNT, 0) else 0

    fun recordAnalysis() {
        preferences.edit().putString(DAY, today()).putInt(COUNT, analysesToday() + 1).apply()
    }

    private fun today(): String = LocalDate.now(WIB).toString()

    private companion object {
        const val DEVICE_ID = "device_id"
        const val DAY = "day"
        const val COUNT = "count"
        val WIB: ZoneId = ZoneId.of("Asia/Jakarta")
    }
}

/** Pengenal penyimpanan rekaman tamu; ID akun berupa UUID sehingga tidak bisa bentrok. */
const val GUEST_STORE_ID = "guest"
