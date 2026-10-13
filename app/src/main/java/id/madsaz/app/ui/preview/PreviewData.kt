package id.madsaz.app.ui.preview

import id.madsaz.app.model.CryScore
import id.madsaz.app.model.RecordingItem
import id.madsaz.app.model.RecordingStatus

object PreviewData {
    val latestRecording = RecordingItem(
        id = "f33d1075-5369-80d1-2a58-44b86b500c21",
        recordedAtEpochMillis = 1_789_351_200_000L,
        durationMillis = 12_400L,
        sizeBytes = 8_400_000L,
        status = RecordingStatus.COMPLETED,
        category = "Neh",
        solution = "Periksa tanda lapar dan tawarkan ASI atau susu sesuai kebutuhan bayi.",
        source = "mock",
        scores = listOf(
            CryScore("Neh", "Lapar", 0.68),
            CryScore("Owh", "Mengantuk", 0.15),
            CryScore("Eh", "Perlu bersendawa", 0.09),
            CryScore("Heh", "Tidak nyaman", 0.05),
            CryScore("Eairh", "Perut tidak nyaman", 0.03),
        ),
        videoWidth = 1080,
        videoHeight = 1920,
    )

    val recordings = listOf(
        latestRecording,
        RecordingItem(
            id = "f33d1075-5369-80d1-2a58-44b86b500c22",
            recordedAtEpochMillis = 1_789_263_000_000L,
            durationMillis = 18_100L,
            sizeBytes = 11_700_000L,
            status = RecordingStatus.COMPLETED,
            category = "Owh",
            solution = null,
            source = null,
            scores = emptyList(),
            videoWidth = null,
            videoHeight = null,
        ),
        RecordingItem(
            id = "f33d1075-5369-80d1-2a58-44b86b500c23",
            recordedAtEpochMillis = 1_789_175_700_000L,
            durationMillis = 9_700L,
            sizeBytes = 6_200_000L,
            status = RecordingStatus.COMPLETED,
            category = "Heh",
            solution = null,
            source = null,
            scores = emptyList(),
            videoWidth = null,
            videoHeight = null,
        ),
    )
}
