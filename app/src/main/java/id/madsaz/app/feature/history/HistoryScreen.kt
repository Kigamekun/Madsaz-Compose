package id.madsaz.app.feature.history

import android.widget.Toast
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.HorizontalDivider
import id.madsaz.app.ui.components.RecordingEmptyState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.madsaz.app.R
import id.madsaz.app.model.CryCue
import id.madsaz.app.ui.components.cryCueLabel
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import id.madsaz.app.model.RecordingItem
import id.madsaz.app.model.RecordingStatus
import id.madsaz.app.ui.components.CompactContainer
import id.madsaz.app.ui.components.CryCueIcon
import id.madsaz.app.ui.components.MadsazHeader
import id.madsaz.app.ui.components.formatDuration
import id.madsaz.app.ui.theme.Border
import id.madsaz.app.ui.theme.BrandBlue
import id.madsaz.app.ui.theme.BrandBlueSoft
import id.madsaz.app.ui.theme.MutedInk
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun HistoryScreen(
    state: HistoryUiState,
    onRetry: () -> Unit,
    onRecord: () -> Unit,
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
                    onRecord = onRecord,
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
    onRecord: () -> Unit,
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
        item { Spacer(Modifier.height(16.dp)) }
        item {
            Row(
                modifier = Modifier.padding(horizontal = 20.dp),
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
                    Text(stringResource(R.string.history_subtitle), color = MutedInk, fontSize = 12.sp)
                }
                Surface(shape = CircleShape, color = Color(0xFFF4F9FD)) {
                    Text(
                        pluralStringResource(R.plurals.history_count, recordings.size, recordings.size),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        color = Color(0xFF0B2A2D),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }
        item { Spacer(Modifier.height(14.dp)) }
        if (recordings.isEmpty()) {
            item {
                RecordingEmptyState(stringResource(R.string.history_empty_title), stringResource(R.string.history_empty_body), onRecord, Modifier.padding(horizontal = 20.dp))
            }
        } else {
            items(recordings, key = { it.id }) { recording ->
                Column(Modifier.padding(horizontal = 20.dp)) {
                    HistoryRecordingRow(recording = recording, onOpen = { onOpenRecording(recording) })
                    HorizontalDivider(color = Border)
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
    val score = recording.scores.firstOrNull()
    val code = (recording.category ?: score?.code)?.let { CryCue.fromCode(it)?.code ?: it.substringBefore(" - ") }
    val openLabel = stringResource(R.string.history_open_recording)
    Row(
        modifier = Modifier.fillMaxWidth().heightIn(min = 72.dp).padding(vertical = 12.dp).clickable(role = Role.Button, onClickLabel = openLabel, onClick = onOpen),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CryCueIcon(code, Modifier.size(48.dp))
        Column(Modifier.padding(start = 14.dp).weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                score?.let { "${code ?: it.code} · ${cryCueLabel(it.code, it.label)}" }
                    ?: stringResource(if (recording.status == RecordingStatus.PROCESSING) R.string.history_processing else R.string.history_analysis_unavailable),
                color = Color(0xFF17343B),
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                "${formatHistoryTime(recording.recordedAtEpochMillis)} · ${formatDuration(recording.durationMillis)}",
                color = Color(0xFF465E68),
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp, lineHeight = 18.sp),
            )
        }
        // Penanda bahwa baris membuka detail (video dan hasil). Dekoratif: seluruh baris adalah satu tombol.
        Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, contentDescription = null, tint = MutedInk)
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
