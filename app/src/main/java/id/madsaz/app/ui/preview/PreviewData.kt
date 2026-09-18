package id.madsaz.app.ui.preview

import id.madsaz.app.model.CryCategory
import id.madsaz.app.model.RecordingItem

object PreviewData {
    val latestRecording = RecordingItem(
        id = "madsaz_1789351200000",
        filePath = "/preview/latest.mp4",
        recordedAtEpochMillis = 1_789_351_200_000L,
        durationMillis = 12_400L,
        sizeBytes = 8_400_000L,
        category = CryCategory.NEH,
    )

    val recordings = listOf(
        latestRecording,
        RecordingItem(
            id = "madsaz_1789263000000",
            filePath = "/preview/second.mp4",
            recordedAtEpochMillis = 1_789_263_000_000L,
            durationMillis = 18_100L,
            sizeBytes = 11_700_000L,
            category = CryCategory.OWH,
        ),
        RecordingItem(
            id = "madsaz_1789175700000",
            filePath = "/preview/third.mp4",
            recordedAtEpochMillis = 1_789_175_700_000L,
            durationMillis = 9_700L,
            sizeBytes = 6_200_000L,
            category = CryCategory.HEH,
        ),
    )
}
