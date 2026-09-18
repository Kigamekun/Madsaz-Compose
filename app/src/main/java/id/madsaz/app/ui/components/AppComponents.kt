package id.madsaz.app.ui.components

import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.os.Build
import android.text.format.Formatter
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChildCare
import androidx.compose.material.icons.rounded.VideoFile
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import id.madsaz.app.R
import id.madsaz.app.model.CryCategory
import id.madsaz.app.ui.theme.BrandBlue
import id.madsaz.app.ui.theme.BrandBlueSoft
import id.madsaz.app.ui.theme.BrandCoralSoft
import id.madsaz.app.ui.theme.Cream
import id.madsaz.app.ui.theme.MutedInk
import java.text.DateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun CompactContainer(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter,
    ) {
        Box(Modifier.widthIn(max = 420.dp).fillMaxSize()) {
            content()
        }
    }
}

@Composable
fun CategoryBadge(category: CryCategory, modifier: Modifier = Modifier) {
    val containerColor = when (category) {
        CryCategory.NEH -> Cream
        CryCategory.OWH -> BrandBlueSoft
        CryCategory.EH -> Color(0xFFE6F3EC)
        CryCategory.EAIRH -> Color(0xFFEDE6FA)
        CryCategory.HEH -> BrandCoralSoft
    }
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        color = containerColor,
        contentColor = BrandBlue,
    ) {
        Text(
            text = category.code,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
fun RecordingThumbnail(filePath: String, modifier: Modifier = Modifier) {
    val bitmap = produceState<Bitmap?>(initialValue = null, filePath) {
        value = withContext(Dispatchers.IO) { loadThumbnail(filePath) }
    }.value

    Box(
        modifier = modifier.background(BrandBlueSoft),
        contentAlignment = Alignment.Center,
    ) {
        if (bitmap != null) {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = stringResource(R.string.video_thumbnail),
                modifier = Modifier.matchParentSize(),
                contentScale = ContentScale.Crop,
            )
        } else {
            Icon(
                imageVector = Icons.Rounded.VideoFile,
                contentDescription = null,
                tint = BrandBlue,
                modifier = Modifier.size(28.dp),
            )
        }
    }
}

@Composable
fun BabyMark(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.extraLarge,
        color = Cream,
        contentColor = BrandBlue,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = Icons.Rounded.ChildCare,
                contentDescription = null,
                modifier = Modifier.size(30.dp),
            )
        }
    }
}

fun formatDuration(durationMillis: Long?): String {
    if (durationMillis == null) return "–"
    val totalSeconds = durationMillis / 1_000
    return String.format(Locale.getDefault(), "%02d:%02d", totalSeconds / 60, totalSeconds % 60)
}

fun formatRecordedAt(epochMillis: Long): String =
    DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(Date(epochMillis))

@Composable
fun formatStorageSize(bytes: Long): String = Formatter.formatShortFileSize(LocalContext.current, bytes)

private fun loadThumbnail(filePath: String): Bitmap? = runCatching {
    val retriever = MediaMetadataRetriever()
    try {
        retriever.setDataSource(filePath)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            retriever.getScaledFrameAtTime(
                0L,
                MediaMetadataRetriever.OPTION_CLOSEST_SYNC,
                320,
                240,
            )
        } else {
            retriever.getFrameAtTime(0L, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)?.let { source ->
                Bitmap.createScaledBitmap(source, 320, 240, true).also { scaled ->
                    if (scaled !== source) source.recycle()
                }
            }
        }
    } finally {
        retriever.release()
    }
}.getOrNull()
