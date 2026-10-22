package id.madsaz.app

import android.os.Build
import android.os.ParcelFileDescriptor
import androidx.test.platform.app.InstrumentationRegistry

/**
 * Memberi izin runtime lewat shell (`pm grant`), yang jalan di Android 6+; UiAutomation.grantRuntimePermission baru ada di Android 9.
 * POST_NOTIFICATIONS hanya ada di Android 13+, jadi dilewati di bawahnya.
 */
fun grantRuntimePermissions(vararg permissions: String) {
    val instrumentation = InstrumentationRegistry.getInstrumentation()
    val packageName = instrumentation.targetContext.packageName
    permissions.filter { it != "android.permission.POST_NOTIFICATIONS" || Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU }.forEach { permission ->
        // Dibaca sampai habis agar perintah benar-benar selesai sebelum tes lanjut.
        ParcelFileDescriptor.AutoCloseInputStream(instrumentation.uiAutomation.executeShellCommand("pm grant $packageName $permission")).use { it.readBytes() }
    }
}
