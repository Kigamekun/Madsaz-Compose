package id.madsaz.app.ui.components

import android.text.format.Formatter
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.dp
import id.madsaz.app.R
import id.madsaz.app.ui.theme.BrandBlue
import id.madsaz.app.ui.theme.BrandBlueDark
import id.madsaz.app.ui.theme.Cream
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

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
fun BabyMark(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.extraLarge,
        color = Cream,
        contentColor = BrandBlue,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(painterResource(R.drawable.phosphor_baby), contentDescription = null, modifier = Modifier.size(24.dp))
        }
    }
}

@Composable
fun MadsazHeader(onProfile: () -> Unit, modifier: Modifier = Modifier) {
    val profileDescription = stringResource(R.string.header_profile_description)
    Row(
        modifier = modifier.fillMaxWidth().padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Surface(
            modifier = Modifier.size(40.dp),
            shape = RoundedCornerShape(13.dp),
            color = Color.White,
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFDEEAF2)),
            shadowElevation = 4.dp,
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(painterResource(R.drawable.phosphor_baby), null, tint = BrandBlueDark, modifier = Modifier.size(24.dp))
            }
        }
        Column(Modifier.padding(start = 10.dp).weight(1f)) {
            Text(
                stringResource(R.string.app_name),
                color = Color(0xFF0B2A2D),
                fontFamily = MaterialTheme.typography.titleLarge.fontFamily,
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold,
                lineHeight = 19.sp,
            )
            Text(stringResource(R.string.header_tagline), color = Color(0xFF0B2A2D), fontSize = 10.sp, fontWeight = FontWeight.Bold, lineHeight = 13.sp)
        }
        Surface(
            modifier = Modifier.size(36.dp).clip(CircleShape).clickable(onClick = onProfile).testTag("header_profile")
                .semantics { contentDescription = profileDescription },
            shape = androidx.compose.foundation.shape.CircleShape,
            color = Color.White,
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFDEEAF2)),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(stringResource(R.string.header_profile_initial), color = Color(0xFF0B2A2D), fontSize = 10.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            }
        }
    }
}

fun formatTime(epochMillis: Long): String =
    DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT).withZone(ZoneId.systemDefault()).format(Instant.ofEpochMilli(epochMillis))

fun formatDate(epochMillis: Long): String =
    DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withZone(ZoneId.systemDefault()).format(Instant.ofEpochMilli(epochMillis))

fun formatDateTime(epochMillis: Long): String =
    DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.SHORT).withZone(ZoneId.systemDefault()).format(Instant.ofEpochMilli(epochMillis))

fun formatDuration(durationMillis: Long?): String {
    if (durationMillis == null) return "–"
    val totalSeconds = durationMillis / 1_000
    return String.format(Locale.getDefault(), "%02d:%02d", totalSeconds / 60, totalSeconds % 60)
}

@Composable
fun formatStorageSize(bytes: Long): String = Formatter.formatShortFileSize(LocalContext.current, bytes)
