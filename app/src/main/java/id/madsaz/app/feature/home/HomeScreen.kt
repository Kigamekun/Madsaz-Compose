package id.madsaz.app.feature.home

import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.foundation.Image
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.statusBarsPadding
import id.madsaz.app.R
import id.madsaz.app.model.RecordingItem
import id.madsaz.app.model.RecordingStatus
import id.madsaz.app.ui.components.RecordingEmptyState
import id.madsaz.app.ui.components.CompactContainer
import id.madsaz.app.ui.components.CryCueIcon
import id.madsaz.app.ui.components.cryCueLabel
import id.madsaz.app.model.CryCue
import id.madsaz.app.ui.components.MadsazHeader
import id.madsaz.app.ui.components.formatPercent
import id.madsaz.app.ui.components.formatTime
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
    onCryCues: () -> Unit,
    onProfile: () -> Unit,
    onOpenRecording: (RecordingItem) -> Unit,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
    avatarUrl: String? = null,
    accountName: String? = null,
) {
    CompactContainer(modifier = modifier) {
        LazyColumn(
            modifier = Modifier.fillMaxSize().background(Color.White).testTag("home_screen"),
            contentPadding = PaddingValues(bottom = contentPadding.calculateBottomPadding()),
            verticalArrangement = Arrangement.spacedBy(0.dp),
        ) {
            item {
                HomeHero(
                    latestRecording = latestRecording,
                    onRecord = onRecord,
                    onProfile = onProfile,
                    avatarUrl = avatarUrl,
                    accountName = accountName,
                    onOpenRecording = onOpenRecording,
                )
            }
            item { LearningDestinations(onCryCues) }
            item {
                RecentRecordings(
                    recordings = recentRecordings,
                    loading = historyLoading,
                    error = historyError,
                    onSeeAll = onHistory,
                    onOpenRecording = onOpenRecording,
                    onRecord = onRecord,
                )
            }
        }
    }
}

@Composable
private fun LearningDestinations(onCryCues: () -> Unit) {
    LearningCard(
        title = stringResource(R.string.home_cry_cues_title),
        description = stringResource(R.string.home_cry_cues_description),
        illustration = { Image(painterResource(R.drawable.home_cry_cues), null, Modifier.size(64.dp)) },
        background = Color(0xFFF3F8FC),
        accent = BrandBlueDark,
        borderColor = Color(0xFFD8E9F2),
        onClick = onCryCues,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(top = 16.dp),
    )
}

@Composable
private fun LearningCard(
    title: String,
    description: String,
    illustration: @Composable () -> Unit,
    background: Color,
    accent: Color,
    borderColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(onClick = onClick, modifier = modifier, shape = RoundedCornerShape(22.dp),
        color = background, border = BorderStroke(1.dp, borderColor)) {
        Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(64.dp), contentAlignment = Alignment.Center) { illustration() }
            Column(Modifier.weight(1f).padding(horizontal = 14.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(title, color = Ink, fontFamily = MaterialTheme.typography.headlineMedium.fontFamily,
                    fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                Text(description, color = Color(0xFF476576), fontSize = 12.sp, lineHeight = 16.sp)
            }
            Icon(Icons.AutoMirrored.Rounded.ArrowForward, contentDescription = null, tint = accent, modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
private fun HomeHero(
    latestRecording: RecordingItem?,
    onRecord: () -> Unit,
    onProfile: () -> Unit,
    onOpenRecording: (RecordingItem) -> Unit,
    avatarUrl: String?,
    accountName: String?,
) {
    val latestScore = latestRecording?.scores?.firstOrNull()
    val latestCode = latestRecording?.let { CryCue.fromCode(it.category ?: latestScore?.code)?.code ?: it.category ?: latestScore?.code }
    val openLabel = stringResource(if (latestRecording == null) R.string.home_latest_start else R.string.home_latest_open)
    Box(Modifier.fillMaxWidth().height(366.dp).clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)).background(Color.White)) {
        Image(
            painter = painterResource(R.drawable.madsaz_home_nursery),
            contentDescription = null,
            modifier = Modifier.fillMaxWidth().height(342.dp).align(Alignment.TopCenter).offset(y = (-20).dp)
                .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)),
            contentScale = ContentScale.Crop,
        )
        Box(
            modifier = Modifier.align(Alignment.BottomStart).fillMaxWidth().height(63.dp)
                .background(Color.White, RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp)),
        )
        MadsazHeader(
            onProfile = onProfile,
            modifier = Modifier.align(Alignment.TopCenter).statusBarsPadding(),
            avatarUrl = avatarUrl,
            name = accountName,
        )
        Surface(
            // Tanpa rekaman kartu ini mengajak merekam; dengan rekaman ia membuka hasilnya.
            onClick = { if (latestRecording == null) onRecord() else onOpenRecording(latestRecording) },
            modifier = Modifier.align(Alignment.TopCenter).offset(y = 241.dp).fillMaxWidth().padding(horizontal = 16.dp).height(125.dp)
                .semantics { onClick(openLabel) { false } },
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
                    text = latestCode ?: latestRecording?.let { stringResource(it.status.labelRes) } ?: "—",
                    modifier = Modifier.offset(x = 18.dp, y = 39.dp),
                    color = Color.White,
                    fontFamily = MaterialTheme.typography.headlineLarge.fontFamily,
                    fontSize = 34.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 40.sp,
                )
                Text(
                    text = when {
                        latestRecording == null -> stringResource(R.string.home_latest_empty)
                        latestScore != null -> stringResource(
                            R.string.home_result_with_confidence,
                            cryCueLabel(latestScore.code, latestScore.label),
                            formatPercent(latestScore.confidence),
                        )
                        else -> stringResource(
                            when (latestRecording.status) {
                                RecordingStatus.PROCESSING -> R.string.home_status_waiting
                                RecordingStatus.FAILED -> R.string.home_status_failed
                                RecordingStatus.COMPLETED -> R.string.home_status_result
                            },
                        )
                    },
                    modifier = Modifier.offset(x = 18.dp, y = 89.dp),
                    color = Color.White.copy(alpha = 0.88f),
                    fontSize = 12.sp,
                )
                Text(
                    text = latestRecording?.let { formatTime(it.recordedAtEpochMillis) } ?: "",
                    modifier = Modifier.align(Alignment.TopEnd).offset(x = (-18).dp, y = 14.dp),
                    color = Color.White.copy(alpha = 0.88f),
                    fontSize = 11.sp,
                )
                // Penanda bahwa kartu bisa diketuk (tata letak lama tidak berubah); dekoratif, aksinya dibaca lewat semantics kartu.
                Icon(
                    Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.align(Alignment.TopEnd).offset(x = (-14).dp, y = 30.dp).size(22.dp),
                )
                if (latestRecording != null) CryCueIcon(
                    latestCode,
                    Modifier.align(Alignment.BottomEnd).padding(end = 16.dp, bottom = 10.dp).size(64.dp),
                )
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
    onRecord: () -> Unit,
) {
    Column(Modifier.fillMaxWidth().background(Color.White).padding(top = 12.dp, bottom = 20.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().heightIn(min = 44.dp).padding(start = 20.dp, end = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                stringResource(R.string.home_recent_title),
                modifier = Modifier.weight(1f),
                color = Ink,
                fontFamily = MaterialTheme.typography.headlineMedium.fontFamily,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
            )
            if (recordings.isNotEmpty()) Text(
                stringResource(R.string.home_see_all),
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable(onClick = onSeeAll)
                    .padding(horizontal = 10.dp, vertical = 12.dp),
                color = BrandBlueDark,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
            )
        }

        when {
            loading -> CircularProgressIndicator(
                modifier = Modifier.align(Alignment.CenterHorizontally).padding(vertical = 32.dp).size(24.dp),
                color = BrandBlue,
                strokeWidth = 2.dp,
            )
            error -> Text(
                stringResource(R.string.home_recent_error_title),
                modifier = Modifier.align(Alignment.CenterHorizontally).padding(vertical = 32.dp),
                color = MutedInk,
                style = MaterialTheme.typography.bodySmall,
            )
            recordings.isEmpty() -> EmptyRecordings(onRecord, Modifier.padding(horizontal = 20.dp).padding(top = 12.dp))
            else -> Column(Modifier.padding(horizontal = 20.dp).padding(top = 12.dp)) {
                val shown = recordings.take(3)
                shown.forEachIndexed { index, recording ->
                    // IntrinsicSize.Min: rel timeline setinggi kartu, jadi tidak perlu menebak tinggi per item.
                    Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
                        TimelineRail(first = index == 0, last = index == shown.lastIndex, gap = if (index == shown.lastIndex) 0.dp else TimelineGap)
                        Surface(
                            onClick = { onOpenRecording(recording) },
                            modifier = Modifier.weight(1f).padding(bottom = if (index == shown.lastIndex) 0.dp else TimelineGap).heightIn(min = 88.dp),
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

private val TimelineGap = 12.dp

/** Titik per item dan garis penghubung; garis berhenti di titik pertama/terakhir. [gap] = jarak bawah kartu yang bukan bagian dari kartu. */
@Composable
private fun TimelineRail(first: Boolean, last: Boolean, gap: Dp) {
    val dot = BrandBlue
    val line = Color(0xFFD5EAF3)
    Box(
        Modifier.width(24.dp).fillMaxHeight().drawBehind {
            val cardHeight = size.height - gap.toPx()
            val centerX = if (layoutDirection == LayoutDirection.Rtl) size.width - 6.dp.toPx() else 6.dp.toPx()
            val centerY = cardHeight / 2
            drawLine(line, Offset(centerX, if (first) centerY else 0f), Offset(centerX, if (last) centerY else size.height), strokeWidth = 2.dp.toPx())
            drawCircle(dot, radius = 6.dp.toPx(), center = Offset(centerX, centerY))
        },
    )
}

@Composable
private fun EmptyRecordings(onRecord: () -> Unit, modifier: Modifier = Modifier) {
    RecordingEmptyState(
        title = stringResource(R.string.home_recent_empty_title),
        body = stringResource(R.string.home_recent_empty_message),
        onRecord = onRecord,
        modifier = modifier,
    )
}

@Composable
private fun ActivityContent(recording: RecordingItem) {
    val score = recording.scores.firstOrNull()
    val code = (recording.category ?: score?.code)?.let { CryCue.fromCode(it)?.code ?: it.substringBefore(" - ") }
    val confidence = score?.confidence
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CryCueIcon(code, Modifier.size(48.dp))
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                if (code != null && score != null) "$code · ${cryCueLabel(score.code, score.label)}" else code ?: stringResource(recording.status.labelRes),
                color = Ink,
                style = MaterialTheme.typography.labelLarge.copy(fontSize = 14.sp, lineHeight = 18.sp, fontWeight = FontWeight.Bold),
            )
            Text(
                stringResource(R.string.home_recorded_at, formatTime(recording.recordedAtEpochMillis)),
                color = MutedInk,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp, lineHeight = 18.sp),
            )
            if (confidence != null) {
                Surface(shape = CircleShape, color = BrandBlueSoft) {
                    Text(
                        stringResource(R.string.home_confidence_chip, formatPercent(confidence)),
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                        color = BrandBlueDark,
                        style = MaterialTheme.typography.labelMedium.copy(fontSize = 12.sp, lineHeight = 18.sp),
                    )
                }
            }
        }
        // Penanda bahwa kartu membuka detail rekaman; dekoratif karena seluruh kartu adalah satu tombol.
        Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, contentDescription = null, tint = MutedInk)
    }
}

