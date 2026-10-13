package id.madsaz.app.feature.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.ChildCare
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.statusBarsPadding
import id.madsaz.app.R
import id.madsaz.app.model.RecordingItem
import id.madsaz.app.feature.crycues.CryCuesSheet
import id.madsaz.app.ui.components.CompactContainer
import id.madsaz.app.ui.components.MadsazHeader
import id.madsaz.app.ui.components.tr
import id.madsaz.app.ui.theme.Border
import id.madsaz.app.ui.theme.BrandBlue
import id.madsaz.app.ui.theme.BrandBlueDark
import id.madsaz.app.ui.theme.BrandBlueSoft
import id.madsaz.app.ui.theme.Ink
import id.madsaz.app.ui.theme.MutedInk

@Composable
fun HomeScreen(
    latestRecording: RecordingItem?,
    recentRecordings: List<RecordingItem> = emptyList(),
    historyLoading: Boolean,
    historyError: Boolean,
    onRecord: () -> Unit,
    onHistory: () -> Unit,
    onProfile: () -> Unit,
    onOpenRecording: (RecordingItem) -> Unit,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
    avatarUrl: String? = null,
    accountName: String? = null,
) {
    var showCryCues by remember { mutableStateOf(false) }
    val openCryCues = { showCryCues = true }
    CompactContainer(modifier = modifier.padding(contentPadding)) {
        LazyColumn(
            modifier = Modifier.fillMaxSize().background(Color.White),
            contentPadding = PaddingValues(bottom = 110.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp),
        ) {
            item {
                HomeHero(
                    latestRecording = latestRecording,
                    onRecord = onRecord,
                    onCryCues = openCryCues,
                    onProfile = onProfile,
                    onOpenRecording = onOpenRecording,
                    avatarUrl = avatarUrl,
                    accountName = accountName,
                )
            }
            item { LearningDestinations(openCryCues) }
            item {
                RecentRecordings(
                    recordings = recentRecordings.ifEmpty { listOfNotNull(latestRecording) },
                    loading = historyLoading,
                    error = historyError,
                    onSeeAll = onHistory,
                    onOpenRecording = onOpenRecording,
                )
            }
        }
        if (showCryCues) CryCuesSheet(onDismiss = { showCryCues = false })
    }
}

@Composable
private fun LearningDestinations(onCryCues: () -> Unit) {
    LearningCard(
        title = "Cry Cues",
        description = tr("Understand your baby's cues", "Pahami petunjuk tangisan bayi"),
        icon = Icons.Rounded.ChildCare,
        background = Color(0xFFF3F8FC),
        iconBackground = Color(0xFFE7F5FA),
        accent = BrandBlueDark,
        borderColor = Color(0xFFD8E9F2),
        onClick = onCryCues,
        modifier = Modifier.fillMaxWidth().padding(start = 16.dp, top = 16.dp, end = 16.dp),
    )
}

@Composable
private fun LearningCard(
    title: String,
    description: String,
    icon: ImageVector,
    background: Color,
    iconBackground: Color,
    accent: Color,
    borderColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(onClick = onClick, modifier = modifier.heightIn(min = 76.dp), shape = RoundedCornerShape(22.dp),
        color = background, border = BorderStroke(1.dp, borderColor)) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(shape = RoundedCornerShape(12.dp), color = iconBackground) {
                Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.padding(9.dp).size(22.dp))
            }
            Column(Modifier.weight(1f).padding(start = 12.dp)) {
                Text(title, color = Ink, fontFamily = MaterialTheme.typography.headlineMedium.fontFamily,
                    fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                Text(description, color = Color(0xFF476576), fontSize = 11.sp, lineHeight = 14.sp)
            }
            Icon(Icons.AutoMirrored.Rounded.ArrowForward, contentDescription = null, tint = accent, modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
private fun HomeHero(
    latestRecording: RecordingItem?,
    onRecord: () -> Unit,
    onCryCues: () -> Unit,
    onProfile: () -> Unit,
    onOpenRecording: (RecordingItem) -> Unit,
    avatarUrl: String?,
    accountName: String?,
) {
    val latestScore = latestRecording?.scores?.firstOrNull()
    val latestConfidence = latestRecording?.confidence ?: latestScore?.confidence
    Box(Modifier.fillMaxWidth().height(330.dp).clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)).background(Color.White)) {
        Image(
            painter = painterResource(R.drawable.madsaz_home_nursery),
            contentDescription = stringResource(R.string.home_hero_description),
            modifier = Modifier.fillMaxWidth().height(342.dp).align(Alignment.TopCenter).offset(y = (-36).dp)
                .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)),
            contentScale = ContentScale.Crop,
        )
        Box(
            modifier = Modifier.align(Alignment.BottomStart).fillMaxWidth().height(62.dp)
                .background(Color.White, RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp)),
        )
        MadsazHeader(
            onProfile = onProfile,
            modifier = Modifier.align(Alignment.TopCenter).statusBarsPadding().padding(top = 6.dp),
            avatarUrl = avatarUrl,
            name = accountName,
        )
        Surface(
            onClick = {
                if (latestRecording == null) onCryCues() else onOpenRecording(latestRecording)
            },
            modifier = Modifier.align(Alignment.TopCenter).offset(y = 205.dp).fillMaxWidth().padding(horizontal = 16.dp).height(125.dp),
            shape = RoundedCornerShape(23.dp),
            color = BrandBlue,
        ) {
            Box(Modifier.fillMaxSize()) {
                Text(
                    text = tr("Latest cry cue", "Petunjuk tangisan terbaru"),
                    modifier = Modifier.offset(x = 18.dp, y = 14.dp),
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = latestRecording?.category ?: latestRecording?.status?.replaceFirstChar(Char::uppercase) ?: "—",
                    modifier = Modifier.offset(x = 18.dp, y = 39.dp),
                    color = Color.White,
                    fontFamily = MaterialTheme.typography.headlineLarge.fontFamily,
                    fontSize = 34.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 40.sp,
                )
                Text(
                    text = when {
                        latestRecording == null -> tr("No result yet", "Belum ada hasil")
                        latestScore != null && latestConfidence != null -> "${latestScore.label} · ${(latestConfidence * 100).toInt()}% ${tr("confidence", "keyakinan")}"
                        latestScore != null -> latestScore.label
                        else -> tr("Waiting for analysis", "Menunggu analisis")
                    },
                    modifier = Modifier.offset(x = 18.dp, y = 89.dp),
                    color = Color.White.copy(alpha = 0.88f),
                    fontSize = 12.sp,
                )
                Text(
                    text = latestRecording?.let { formatHomeRecordedAt(it.recordedAtEpochMillis) } ?: "",
                    modifier = Modifier.align(Alignment.TopEnd).offset(x = (-18).dp, y = 14.dp),
                    color = Color.White.copy(alpha = 0.88f),
                    fontSize = 11.sp,
                )
                Row(
                    modifier = Modifier.align(Alignment.BottomEnd).padding(end = 28.dp, bottom = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.Bottom,
                ) {
                    listOf(16, 26, 33, 42, 30, 50, 64).forEach { height ->
                        Box(Modifier.width(8.dp).height(height.dp).background(Color(0xFFD7EAF4), RoundedCornerShape(4.dp)))
                    }
                }
            }
        }
    }
}

@Composable
private fun RecentRecordings(
    recordings: List<RecordingItem>,
    loading: Boolean,
    error: Boolean,
    onSeeAll: () -> Unit,
    onOpenRecording: (RecordingItem) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth().background(Color.White).padding(top = 8.dp, bottom = 20.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                tr("Recent recordings", "Rekaman terbaru"),
                color = Ink,
                fontFamily = MaterialTheme.typography.titleLarge.fontFamily,
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                tr("See all  ›", "Lihat semua  ›"),
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable(onClick = onSeeAll)
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                color = BrandBlue,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
            )
        }

        when {
            loading -> CircularProgressIndicator(
                modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 28.dp).size(24.dp),
                color = BrandBlue,
                strokeWidth = 2.dp,
            )
            error -> Text(
                stringResource(R.string.home_recent_error_title),
                modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 28.dp),
                color = MutedInk,
                style = MaterialTheme.typography.bodySmall,
            )
            recordings.isEmpty() -> Text(
                tr("No recordings yet", "Belum ada rekaman"),
                modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 44.dp, bottom = 60.dp),
                color = MutedInk,
                style = MaterialTheme.typography.bodySmall,
            )
            else -> {
                Box(
                    Modifier.padding(top = 12.dp, start = 20.dp, end = 16.dp)
                        .fillMaxWidth()
                        .height((recordings.take(3).size * 98 - 10).dp),
                ) {
                    Box(
                        Modifier.padding(start = 5.dp, top = 43.dp, bottom = 43.dp)
                            .width(2.dp).fillMaxSize()
                            .background(Color(0xFFD5EAF3)),
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        recordings.take(3).forEach { recording ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(Modifier.width(12.dp).height(88.dp), contentAlignment = Alignment.Center) {
                                    Box(Modifier.size(12.dp).background(BrandBlue, CircleShape))
                                }
                                Spacer(Modifier.width(4.dp))
                                Surface(
                                    onClick = { onOpenRecording(recording) },
                                    modifier = Modifier.weight(1f).height(88.dp),
                                    shape = RoundedCornerShape(20.dp),
                                    color = Color.White,
                                    border = BorderStroke(1.dp, Border),
                                ) {
                                    ActivityContent(recording)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ActivityContent(recording: RecordingItem) {
    val score = recording.scores.firstOrNull()
    val code = recording.category?.substringBefore(" - ") ?: score?.code
    val confidence = recording.confidence ?: score?.confidence
    Row(
        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Surface(Modifier.size(44.dp), CircleShape, BrandBlueSoft) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    code ?: "•",
                    color = BrandBlueDark,
                    style = MaterialTheme.typography.labelLarge.copy(fontSize = 12.sp, lineHeight = 16.sp, fontWeight = FontWeight.Bold),
                )
            }
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                if (code != null && score != null) "$code - ${score.label}" else code ?: recording.status.replaceFirstChar(Char::uppercase),
                color = Ink,
                style = MaterialTheme.typography.labelLarge.copy(fontSize = 14.sp, lineHeight = 18.sp, fontWeight = FontWeight.Bold),
                maxLines = 1,
            )
            Text(
                tr("Recorded at", "Direkam pukul") + " ${formatHomeRecordedAt(recording.recordedAtEpochMillis)}",
                color = MutedInk,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, lineHeight = 14.sp),
                maxLines = 1,
            )
            if (confidence != null) {
                Surface(shape = CircleShape, color = BrandBlueSoft) {
                    Text(
                        "${(confidence * 100).toInt()}% ${tr("confidence", "keyakinan")}",
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                        color = BrandBlueDark,
                        style = MaterialTheme.typography.labelMedium.copy(fontSize = 9.sp, lineHeight = 12.sp),
                    )
                }
            }
        }
    }
}

private fun formatHomeRecordedAt(epochMillis: Long): String =
    java.text.SimpleDateFormat("h:mm a", java.util.Locale.ENGLISH).format(java.util.Date(epochMillis))
