package id.madsaz.app

import androidx.test.platform.app.InstrumentationRegistry
import id.madsaz.app.data.local.RecordingStore
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.util.UUID

/** Pengguna uji acak: tidak menyentuh rekaman milik akun lain di perangkat. */
class RecordingStoreTest {
    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun deleteAll_removes_pending_video_and_directory_for_that_user_only() {
        val user = RecordingStore(context, "audit-${UUID.randomUUID()}")
        val other = RecordingStore(context, "audit-${UUID.randomUUID()}")
        val mine = user.createOutputFile().apply { writeBytes(ByteArray(8)) }
        val theirs = other.createOutputFile().apply { writeBytes(ByteArray(8)) }
        user.markReady(mine)
        other.markReady(theirs)
        assertNotNull(user.pendingUpload())

        user.deleteAll()

        assertNull(user.pendingUpload())
        assertFalse(mine.exists())
        assertFalse(mine.parentFile!!.exists())
        assertTrue(theirs.exists())
        other.delete(theirs)
        assertFalse(File(theirs.parentFile!!, theirs.name).exists())
    }
}
