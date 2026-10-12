package id.madsaz.app.feature.history

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.madsaz.app.R
import id.madsaz.app.model.CryCategory
import id.madsaz.app.model.RecordingItem
import id.madsaz.app.ui.components.CompactContainer
import id.madsaz.app.ui.components.MadsazHeader
import id.madsaz.app.ui.components.formatDuration
import id.madsaz.app.ui.theme.AppBackground
import id.madsaz.app.ui.theme.Border
import id.madsaz.app.ui.theme.BrandBlue
import id.madsaz.app.ui.theme.BrandBlueSoft
import id.madsaz.app.ui.theme.Ink
import id.madsaz.app.ui.theme.MutedInk

@Composable
fun HistoryScreen(
    state: HistoryUiState,
    onRetry: () -> Unit,
    onProfile: () -> Unit,
    onOpenRecording: (RecordingItem) -> Unit,
    onDelete: (RecordingItem) -> Unit,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
) {
    var pendingDelete by remember { mutableStateOf<RecordingItem?>(null) }

    CompactContainer(modifier = modifier.padding(contentPadding)) {
        when (state) {
            HistoryUiState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(modifier = Modifier.size(28.dp), strokeWidth = 2.5.dp, color = BrandBlue)
            }
            is HistoryUiState.Error -> ErrorState(state.messageRes, onRetry)
            is HistoryUiState.Content -> HistoryContent(
                recordings = state.recordings,
                onProfile = onProfile,
                onOpenRecording = onOpenRecording,
                onDelete = { pendingDelete = it },
            )
        }
    }

    pendingDelete?.let { recording ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text(stringResource(R.string.history_delete_recording)) },
            text = { Text(stringResource(R.string.history_delete_confirmation, recording.category ?: recording.id.take(8))) },
            confirmButton = {
                TextButton(onClick = {
                    pendingDelete = null
                    onDelete(recording)
                }) { Text(stringResource(R.string.common_delete)) }
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete = null }) { Text(stringResource(R.string.common_cancel)) }
            },
        )
    }
}

@Composable
private fun HistoryContent(
    recordings: List<RecordingItem>,
    onProfile: () -> Unit,
    onOpenRecording: (RecordingItem) -> Unit,
    onDelete: (RecordingItem) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().statusBarsPadding(),
        contentPadding = PaddingValues(start = 0.dp, end = 0.dp, top = 0.dp, bottom = 110.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp),
    ) {
        item { MadsazHeader(onProfile = onProfile) }
        item { Spacer(Modifier.height(40.dp)) }
        item {
            Row(
                modifier = Modifier.padding(horizontal = 18.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        "Recording history",
                        color = Color(0xFF0B2A2D),
                        fontFamily = MaterialTheme.typography.headlineMedium.fontFamily,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text("Semua rekaman", color = MutedInk, fontSize = 10.sp)
                }
                Surface(shape = CircleShape, color = Color(0xFFF4F9FD)) {
                    Text(
                        "${recordings.size} recordings",
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
                                onDelete = { onDelete(recording) },
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

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
private fun HistoryRecordingRow(
    recording: RecordingItem,
    onOpen: () -> Unit,
    onDelete: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().height(63.dp)
            .combinedClickable(role = Role.Button, onClick = onOpen, onLongClick = onDelete),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Surface(Modifier.size(40.dp), RoundedCornerShape(13.dp), BrandBlueSoft) {
            Box(contentAlignment = Alignment.Center) {
                Icon(Icons.Rounded.PlayArrow, contentDescription = null, tint = BrandBlue, modifier = Modifier.size(22.dp))
            }
        }
        Column(Modifier.padding(start = 14.dp).weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(recording.category ?: recording.status.replaceFirstChar { it.uppercase() }, color = Color(0xFF0B2A2D), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            Text(
                "${formatHistoryTime(recording.recordedAtEpochMillis)} · ${formatDuration(recording.durationMillis)}",
                color = Color(0xFF465E68),
                fontSize = 9.sp,
            )
        }
        Surface(
            onClick = onOpen,
            modifier = Modifier.size(32.dp),
            shape = CircleShape,
            color = BrandBlueSoft,
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(Icons.Rounded.PlayArrow, contentDescription = "Open recording", tint = BrandBlue, modifier = Modifier.size(18.dp))
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

private fun formatHistoryTime(epochMillis: Long): String =
    java.text.SimpleDateFormat("d MMM, HH:mm", java.util.Locale.ENGLISH).format(java.util.Date(epochMillis))
