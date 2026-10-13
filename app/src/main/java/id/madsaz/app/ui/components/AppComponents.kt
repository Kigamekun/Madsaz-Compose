package id.madsaz.app.ui.components

import android.text.format.Formatter
import android.graphics.BitmapFactory
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale
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
import java.text.NumberFormat
import java.util.Locale

import java.net.URL
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
fun MadsazHeader(onProfile: () -> Unit, modifier: Modifier = Modifier, avatarUrl: String? = null, name: String? = null) {
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
        ProfileAvatar(
            avatarUrl,
            name,
            Modifier.size(36.dp).clip(CircleShape).clickable(onClick = onProfile).testTag("header_profile")
                .semantics { contentDescription = profileDescription },
        )
    }
}

/** Foto profil dari URL avatar; tanpa foto (atau gagal dimuat) tampil huruf pertama nama. Dekoratif: pemanggil memberi deskripsi aksesibilitas. */
@Composable
fun ProfileAvatar(url: String?, name: String?, modifier: Modifier = Modifier) {
    val image by produceState<ImageBitmap?>(null, url) {
        value = if (url == null) null else withContext(Dispatchers.IO) {
            runCatching { URL(url).openStream().use(BitmapFactory::decodeStream)?.asImageBitmap() }.getOrNull()
        }
    }
    Box(modifier.clip(CircleShape).background(Color(0xFFE6F4FB)), contentAlignment = Alignment.Center) {
        val photo = image
        if (photo == null) {
            Text(
                name?.firstOrNull()?.uppercase() ?: stringResource(R.string.header_profile_initial),
                color = BrandBlueDark,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
            )
        } else {
            Image(photo, null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
        }
    }
}

fun formatTime(epochMillis: Long): String =
    DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT).withZone(ZoneId.systemDefault()).format(Instant.ofEpochMilli(epochMillis))

fun formatDate(epochMillis: Long): String =
    DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withZone(ZoneId.systemDefault()).format(Instant.ofEpochMilli(epochMillis))

fun formatDateTime(epochMillis: Long): String =
    DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.SHORT).withZone(ZoneId.systemDefault()).format(Instant.ofEpochMilli(epochMillis))

/** Pecahan 0..1 sebagai persen sesuai locale ("68%"). */
fun formatPercent(fraction: Double): String = NumberFormat.getPercentInstance().format(fraction)

fun formatDuration(durationMillis: Long?): String {
    if (durationMillis == null) return "–"
    val totalSeconds = durationMillis / 1_000
    return String.format(Locale.getDefault(), "%02d:%02d", totalSeconds / 60, totalSeconds % 60)
}

@Composable
fun formatStorageSize(bytes: Long): String = Formatter.formatShortFileSize(LocalContext.current, bytes)
