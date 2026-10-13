package id.madsaz.app.feature.history

import android.widget.Toast
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.madsaz.app.R
import id.madsaz.app.model.RecordingItem
import id.madsaz.app.model.RecordingStatus
import id.madsaz.app.ui.components.CompactContainer
import id.madsaz.app.ui.components.MadsazHeader
import id.madsaz.app.ui.components.formatDuration
import id.madsaz.app.ui.theme.Border
import id.madsaz.app.ui.theme.BrandBlue
import id.madsaz.app.ui.theme.BrandBlueSoft
import id.madsaz.app.ui.theme.Ink
import id.madsaz.app.ui.theme.MutedInk
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun HistoryScreen(
    state: HistoryUiState,
    onRetry: () -> Unit,
    onProfile: () -> Unit,
    onOpenRecording: (RecordingItem) -> Unit,
    onNoticeShown: () -> Unit,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
    avatarUrl: String? = null,
    accountName: String? = null,
) {
    CompactContainer(modifier = modifier) {
        when (state) {
            HistoryUiState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(modifier = Modifier.size(28.dp), strokeWidth = 2.5.dp, color = BrandBlue)
            }
            is HistoryUiState.Error -> ErrorState(state.messageRes, onRetry)
            is HistoryUiState.Content -> {
                val context = LocalContext.current
                LaunchedEffect(state.noticeRes) {
                    state.noticeRes?.let {
                        Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                        onNoticeShown()
                    }
                }
                HistoryContent(
                    recordings = state.recordings,
                    bottomPadding = contentPadding.calculateBottomPadding(),
                    onProfile = onProfile,
                    onOpenRecording = onOpenRecording,
                    avatarUrl = avatarUrl,
                    accountName = accountName,
                )
            }
        }
    }
}

@Composable
private fun HistoryContent(
    recordings: List<RecordingItem>,
    bottomPadding: androidx.compose.ui.unit.Dp,
    onProfile: () -> Unit,
    onOpenRecording: (RecordingItem) -> Unit,
    avatarUrl: String?,
    accountName: String?,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().statusBarsPadding(),
        contentPadding = PaddingValues(bottom = bottomPadding),
        verticalArrangement = Arrangement.spacedBy(0.dp),
    ) {
        item { MadsazHeader(onProfile = onProfile, avatarUrl = avatarUrl, name = accountName) }
        item { Spacer(Modifier.height(40.dp)) }
        item {
            Row(
                modifier = Modifier.padding(horizontal = 18.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        stringResource(R.string.history_title),
                        color = Color(0xFF0B2A2D),
                        fontFamily = MaterialTheme.typography.headlineMedium.fontFamily,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(stringResource(R.string.history_subtitle), color = MutedInk, fontSize = 10.sp)
                }
                Surface(shape = CircleShape, color = Color(0xFFF4F9FD)) {
                    Text(
                        pluralStringResource(R.plurals.history_count, recordings.size, recordings.size),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        color = Color(0xFF0B2A2D),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }
        item { Spacer(Modifier.height(14.dp)) }
        if (recordings.isEmpty()) {
            item { EmptyHistory(Modifier.padding(horizontal = 18.dp)) }
        } else {
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp),
                    shape = RoundedCornerShape(20.dp),
                    color = Color.White,
                    border = BorderStroke(1.dp, Border),
                ) {
                    Column(Modifier.padding(horizontal = 14.dp)) {
                        recordings.forEachIndexed { index, recording ->
                            HistoryRecordingRow(
                                recording = recording,
                                onOpen = { onOpenRecording(recording) },
                            )
                            if (index != recordings.lastIndex) {
                                Box(Modifier.fillMaxWidth().padding(start = 54.dp).height(1.dp).background(Border))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HistoryRecordingRow(
    recording: RecordingItem,
    onOpen: () -> Unit,
) {
    val code = recording.category?.substringBefore(" - ")
    val badgeColor = when (code?.lowercase(Locale.ROOT)) {
        "neh" -> Color(0xFFFFF0E9)
        "heh" -> Color(0xFFFCE7EC)
        else -> BrandBlueSoft
    }
    val openLabel = stringResource(R.string.history_open_recording)
    Row(
        modifier = Modifier.fillMaxWidth().height(63.dp).clickable(role = Role.Button, onClickLabel = openLabel, onClick = onOpen),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Surface(shape = RoundedCornerShape(13.dp), color = badgeColor) {
            Box(Modifier.widthIn(min = 40.dp, max = 88.dp).height(40.dp).padding(horizontal = 7.dp), contentAlignment = Alignment.Center) {
                Text(
                    code ?: stringResource(recording.status.labelRes),
                    color = Ink,
                    style = MaterialTheme.typography.labelLarge.copy(fontSize = 10.sp, lineHeight = 13.sp, fontWeight = FontWeight.Bold),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        Column(Modifier.padding(start = 14.dp).weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                recording.scores.firstOrNull()?.label
                    ?: stringResource(if (recording.status == RecordingStatus.PROCESSING) R.string.history_processing else R.string.history_analysis_unavailable),
                color = Color(0xFF17343B),
                style = MaterialTheme.typography.labelLarge.copy(fontSize = 12.sp, lineHeight = 15.sp, fontWeight = FontWeight.Bold),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                "${formatHistoryTime(recording.recordedAtEpochMillis)} · ${formatDuration(recording.durationMillis)}",
                color = Color(0xFF465E68),
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 9.sp, lineHeight = 12.sp),
            )
        }
        // Dekoratif: seluruh baris adalah satu tombol, sehingga pembaca layar tidak menemukan dua target untuk aksi yang sama.
        Surface(modifier = Modifier.size(32.dp), shape = CircleShape, color = BrandBlueSoft) {
            Box(contentAlignment = Alignment.Center) {
                Icon(Icons.Rounded.PlayArrow, contentDescription = null, tint = BrandBlue, modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Composable
private fun EmptyHistory(modifier: Modifier = Modifier) {
    Surface(modifier = modifier, shape = RoundedCornerShape(20.dp), color = BrandBlueSoft) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Icon(Icons.Rounded.History, contentDescription = null, tint = BrandBlue, modifier = Modifier.size(34.dp))
            Text(stringResource(R.string.history_empty_title), style = MaterialTheme.typography.titleLarge)
            Text(stringResource(R.string.history_empty_body), style = MaterialTheme.typography.bodyMedium, color = MutedInk, textAlign = TextAlign.Center)
        }
    }
}

@Composable
private fun ErrorState(messageRes: Int, onRetry: () -> Unit) {
    Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(stringResource(messageRes), style = MaterialTheme.typography.titleMedium)
            OutlinedButton(onClick = onRetry) { Text(stringResource(R.string.common_retry)) }
        }
    }
}

@Composable
private fun formatHistoryTime(epochMillis: Long): String {
    // Locale dibaca dari Configuration agar tampilan ikut berubah saat bahasa aplikasi diganti.
    val locale = LocalConfiguration.current.locales[0]
    val zone = ZoneId.systemDefault()
    val recorded = Instant.ofEpochMilli(epochMillis).atZone(zone)
    val today = LocalDate.now(zone)
    val day = when (recorded.toLocalDate()) {
        today -> stringResource(R.string.history_today)
        today.minusDays(1) -> stringResource(R.string.history_yesterday)
        else -> DateTimeFormatter.ofPattern("d MMM", locale).format(recorded)
    }
    return "$day, ${DateTimeFormatter.ofPattern("HH:mm", locale).format(recorded)}"
}
