package id.madsaz.app.feature.history

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Videocam
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import id.madsaz.app.R
import id.madsaz.app.model.RecordingItem
import id.madsaz.app.ui.components.CategoryBadge
import id.madsaz.app.ui.components.CompactContainer
import id.madsaz.app.ui.components.RecordingThumbnail
import id.madsaz.app.ui.components.formatDuration
import id.madsaz.app.ui.components.formatRecordedAt
import id.madsaz.app.ui.theme.BrandBlue
import id.madsaz.app.ui.theme.BrandBlueSoft
import id.madsaz.app.ui.theme.MutedInk

@Composable
fun HistoryScreen(
    state: HistoryUiState,
    onRetry: () -> Unit,
    onRecord: () -> Unit,
    onOpenRecording: (RecordingItem) -> Unit,
    onDelete: (RecordingItem) -> Unit,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
) {
    var pendingDelete by remember { mutableStateOf<RecordingItem?>(null) }

    CompactContainer(modifier = modifier.padding(contentPadding)) {
        when (state) {
            HistoryUiState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(modifier = Modifier.size(28.dp), strokeWidth = 2.5.dp)
            }
            is HistoryUiState.Error -> ErrorState(state.messageRes, onRetry)
            is HistoryUiState.Content -> HistoryContent(
                recordings = state.recordings,
                onRecord = onRecord,
                onOpenRecording = onOpenRecording,
                onDelete = { pendingDelete = it },
            )
        }
    }

    pendingDelete?.let { recording ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text(stringResource(R.string.history_delete_recording)) },
            text = { Text(stringResource(R.string.history_delete_confirmation, recording.category.code)) },
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
    onRecord: () -> Unit,
    onOpenRecording: (RecordingItem) -> Unit,
    onDelete: (RecordingItem) -> Unit,
) {
    LazyColumn(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text(stringResource(R.string.history_title), style = MaterialTheme.typography.headlineLarge)
                Text(
                    stringResource(R.string.history_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MutedInk,
                )
                if (recordings.isNotEmpty()) {
                    Text(
                        stringResource(R.string.history_count, recordings.size),
                        style = MaterialTheme.typography.labelMedium,
                        color = BrandBlue,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
            }
        }
        if (recordings.isEmpty()) {
            item { EmptyHistory(onRecord) }
        } else {
            items(recordings, key = RecordingItem::id) { recording ->
                HistoryRecordingCard(
                    recording = recording,
                    onOpen = { onOpenRecording(recording) },
                    onDelete = { onDelete(recording) },
                )
            }
        }
        item { Spacer(Modifier.height(12.dp)) }
    }
}

@Composable
private fun HistoryRecordingCard(
    recording: RecordingItem,
    onOpen: () -> Unit,
    onDelete: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onOpen),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surface,
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            RecordingThumbnail(
                filePath = recording.filePath,
                modifier = Modifier.size(width = 92.dp, height = 82.dp).clip(RoundedCornerShape(14.dp)),
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(5.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CategoryBadge(recording.category)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        formatDuration(recording.durationMillis),
                        style = MaterialTheme.typography.labelMedium,
                        color = MutedInk,
                    )
                }
                Text(stringResource(recording.category.meaningRes), style = MaterialTheme.typography.titleMedium)
                Text(
                    formatRecordedAt(recording.recordedAtEpochMillis),
                    style = MaterialTheme.typography.bodySmall,
                    color = MutedInk,
                )
            }
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Rounded.DeleteOutline,
                    contentDescription = stringResource(R.string.history_delete_recording),
                    tint = MaterialTheme.colorScheme.error,
                )
            }
        }
    }
}

@Composable
private fun EmptyHistory(onRecord: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().padding(top = 20.dp),
        shape = MaterialTheme.shapes.large,
        color = BrandBlueSoft,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Icon(Icons.Rounded.History, contentDescription = null, tint = BrandBlue, modifier = Modifier.size(34.dp))
            Text(stringResource(R.string.history_empty_title), style = MaterialTheme.typography.titleLarge)
            Text(
                stringResource(R.string.history_empty_body),
                style = MaterialTheme.typography.bodyMedium,
                color = MutedInk,
            )
            Button(onClick = onRecord, modifier = Modifier.padding(top = 6.dp)) {
                Icon(Icons.Rounded.Videocam, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.home_start_recording))
            }
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
