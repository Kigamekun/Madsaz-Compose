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
import androidx.compose.material.icons.rounded.GraphicEq
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
import id.madsaz.app.model.CryCategory
import id.madsaz.app.model.RecordingItem
import id.madsaz.app.feature.crycues.CryCuesSheet
import id.madsaz.app.ui.components.CompactContainer
import id.madsaz.app.ui.components.MadsazHeader
import id.madsaz.app.ui.components.formatDuration
import id.madsaz.app.ui.theme.AppBackground
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
) {
    var showCryCues by remember { mutableStateOf(false) }
    val openCryCues = { showCryCues = true }
    CompactContainer(modifier = modifier.padding(contentPadding)) {
        LazyColumn(
            modifier = Modifier.fillMaxSize().background(AppBackground),
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
    Column(
        Modifier.fillMaxWidth().background(Color.White).padding(start = 16.dp, end = 16.dp, top = 12.dp),
    ) {
        LearningCard(
            title = "Cry Cues",
            description = "Understand your baby's cues",
            icon = Icons.Rounded.ChildCare,
            background = Color(0xFFF3F8FC),
            iconBackground = Color(0xFFE7F5FA),
            accent = BrandBlueDark,
            borderColor = Color(0xFFD8E9F2),
            onClick = onCryCues,
            modifier = Modifier.fillMaxWidth(),
        )
    }
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
    Surface(
        onClick = onClick,
        modifier = modifier.heightIn(min = 76.dp),
        shape = RoundedCornerShape(18.dp),
        color = background,
        border = BorderStroke(1.dp, borderColor),
    ) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
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
) {
    Box(Modifier.fillMaxWidth().height(330.dp).background(Color(0xFFE6F4FB))) {
        Image(
            painter = painterResource(R.drawable.madsaz_home_photo),
            contentDescription = stringResource(R.string.home_hero_description),
            modifier = Modifier.fillMaxWidth().height(342.dp).align(Alignment.TopCenter).offset(y = (-74).dp),
            contentScale = ContentScale.Crop,
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .height(62.dp)
                .clip(RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp))
                .background(Color.White),
        )
        MadsazHeader(
            onProfile = onProfile,
            modifier = Modifier.align(Alignment.TopCenter).statusBarsPadding().padding(top = 6.dp),
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
                    text = stringResource(R.string.home_latest_cry),
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
                    text = latestRecording?.let { if (it.category == null) "Waiting for analysis" else "Analysis result" }
                        ?: stringResource(R.string.home_latest_empty),
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
                "Recent recordings",
                color = Ink,
                fontFamily = MaterialTheme.typography.titleLarge.fontFamily,
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                "See all  ›",
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
                stringResource(R.string.home_recent_empty_title),
                modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 44.dp, bottom = 60.dp),
                color = MutedInk,
                style = MaterialTheme.typography.bodySmall,
            )
            else -> {
                Box(
                    Modifier.padding(top = 12.dp, start = 20.dp, end = 16.dp)
                        .fillMaxWidth()
                        .height((recordings.take(3).size * 114 - 10).dp),
                ) {
                    Box(
                        Modifier.padding(start = 5.dp, top = 50.dp, bottom = 50.dp)
                            .width(2.dp).fillMaxSize()
                            .background(Color(0xFFD5EAF3)),
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        recordings.take(3).forEach { recording ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(Modifier.width(12.dp).height(104.dp), contentAlignment = Alignment.Center) {
                                    Box(Modifier.size(12.dp).background(BrandBlue, CircleShape))
                                }
                                Spacer(Modifier.width(4.dp))
                                Surface(
                                    onClick = { onOpenRecording(recording) },
                                    modifier = Modifier.weight(1f).height(104.dp),
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
    Row(
        modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Surface(Modifier.size(54.dp), RoundedCornerShape(12.dp), BrandBlueSoft) {
            Icon(Icons.Rounded.GraphicEq, contentDescription = null, modifier = Modifier.padding(15.dp), tint = BrandBlue)
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                recording.category ?: recording.status.replaceFirstChar(Char::uppercase),
                color = Ink,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
            )
            Text(
                "Recorded at ${formatHomeRecordedAt(recording.recordedAtEpochMillis)}",
                color = MutedInk,
                fontSize = 11.sp,
                maxLines = 1,
            )
            Text(
                "Duration ${formatDuration(recording.durationMillis)}",
                color = BrandBlueDark,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

private fun formatHomeRecordedAt(epochMillis: Long): String =
    java.text.SimpleDateFormat("h:mm a", java.util.Locale.ENGLISH).format(java.util.Date(epochMillis))
