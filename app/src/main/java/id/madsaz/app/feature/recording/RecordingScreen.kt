package id.madsaz.app.feature.recording

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.foundation.shape.RoundedCornerShape
import id.madsaz.app.ui.theme.BrandBlueSoft
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Cameraswitch
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import id.madsaz.app.R
import id.madsaz.app.model.RecordingUiState
import id.madsaz.app.ui.components.BabyMark
import id.madsaz.app.ui.components.CompactContainer
import id.madsaz.app.ui.theme.BrandBlueDark
import id.madsaz.app.ui.theme.Ink
import id.madsaz.app.ui.theme.MutedInk

@Composable
fun RecordingScreen(
    state: RecordingUiState,
    permissionPreviouslyRequested: Boolean,
    canSwitchCamera: Boolean,
    onBack: () -> Unit,
    onRequestPermission: () -> Unit,
    onOpenSettings: () -> Unit,
    onSwitchCamera: () -> Unit,
    onStart: () -> Unit,
    onStop: () -> Unit,
    audioLevels: List<Float> = emptyList(),
    /** false sampai pratinjau kamera benar-benar berjalan; merekam sebelum itu gagal di CameraX. */
    cameraReady: Boolean = true,
    cameraPreview: @Composable (Modifier) -> Unit,
    modifier: Modifier = Modifier,
) {
    BackHandler(enabled = state == RecordingUiState.PermissionRequired || state == RecordingUiState.Ready) { onBack() }
    Scaffold(modifier = modifier, containerColor = Color.White, contentWindowInsets = WindowInsets.safeDrawing) { padding ->
        CompactContainer(Modifier.padding(padding).consumeWindowInsets(padding)) {
            when (state) {
                RecordingUiState.PermissionRequired -> Box(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        BabyMark(Modifier.size(56.dp))
                        Text(stringResource(R.string.permission_title), style = MaterialTheme.typography.headlineSmall, textAlign = TextAlign.Center)
                        Text(stringResource(R.string.permission_body), style = MaterialTheme.typography.bodyMedium, color = MutedInk, textAlign = TextAlign.Center)
                        Button(onClick = if (permissionPreviouslyRequested) onOpenSettings else onRequestPermission, modifier = Modifier.fillMaxWidth()) {
                            Text(stringResource(if (permissionPreviouslyRequested) R.string.permission_settings else R.string.permission_allow))
                        }
                        TextButton(onClick = onBack) { Text(stringResource(R.string.common_back)) }
                    }
                }
                RecordingUiState.Ready, is RecordingUiState.Recording -> {
                    val elapsedSeconds = (state as? RecordingUiState.Recording)?.elapsedSeconds
                    val recording = elapsedSeconds != null
                    Column(Modifier.fillMaxSize().testTag("recording_screen")) {
                        Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = onBack, modifier = Modifier.clip(CircleShape).background(BrandBlueSoft).testTag("recording_back")) {
                                Icon(Icons.AutoMirrored.Rounded.ArrowBack, stringResource(R.string.common_back), tint = Ink)
                            }
                            Text(stringResource(R.string.recording_keep_baby_in_view), Modifier.weight(1f).padding(horizontal = 12.dp), style = MaterialTheme.typography.titleMedium, color = Ink)
                            Column(horizontalAlignment = Alignment.End) {
                                Text(stringResource(if (recording) R.string.recording_status_rec else R.string.recording_status_ready), style = MaterialTheme.typography.labelMedium, color = if (recording) MaterialTheme.colorScheme.error else MutedInk)
                                if (elapsedSeconds != null) Text(formatTimer(elapsedSeconds), style = MaterialTheme.typography.titleMedium, color = Ink, modifier = Modifier.testTag("recording_timer"))
                            }
                        }
                        Box(Modifier.fillMaxWidth().weight(1f).padding(horizontal = 16.dp).clip(RoundedCornerShape(24.dp)).background(Color.Black).testTag("camera_preview")) {
                            cameraPreview(Modifier.fillMaxSize().clipToBounds())
                        }
                        Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Canvas(Modifier.fillMaxWidth().height(28.dp)) {
                                val levels = if (recording) audioLevels.takeLast(46) else emptyList()
                                val levelOffset = 46 - levels.size
                                val step = size.width / 46
                                repeat(46) { index ->
                                    val amplitude = levels.getOrNull(index - levelOffset)?.coerceIn(0f, 1f) ?: 0f
                                    val normalized = ((amplitude - 0.02f) / 0.28f).coerceIn(0f, 1f)
                                    val height = 2.dp.toPx() + 24.dp.toPx() * kotlin.math.sqrt(normalized)
                                    val x = step * (index + 0.5f)
                                    drawLine(BrandBlueDark, Offset(x, (size.height - height) / 2), Offset(x, (size.height + height) / 2), strokeWidth = 2.dp.toPx(), cap = StrokeCap.Round)
                                }
                            }
                            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                Surface(
                                    onClick = if (recording) onStop else onStart,
                                    enabled = recording || cameraReady,
                                    modifier = Modifier.size(76.dp).testTag(if (recording) "recording_stop" else "recording_start"),
                                    shape = CircleShape,
                                    border = BorderStroke(4.dp, BrandBlueSoft),
                                    color = when {
                                        recording -> MaterialTheme.colorScheme.error
                                        cameraReady -> BrandBlueDark
                                        else -> BrandBlueDark.copy(alpha = 0.4f)
                                    },
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(painterResource(if (recording) R.drawable.phosphor_stop_circle else R.drawable.phosphor_camera), stringResource(if (recording) R.string.recording_stop else R.string.recording_start), Modifier.size(30.dp), tint = Color.White)
                                    }
                                }
                                if (canSwitchCamera && !recording) {
                                    IconButton(onClick = onSwitchCamera, enabled = cameraReady, modifier = Modifier.align(Alignment.CenterEnd).clip(CircleShape).background(BrandBlueSoft).testTag("recording_switch")) {
                                        Icon(Icons.Rounded.Cameraswitch, stringResource(R.string.recording_switch_camera), tint = Ink)
                                    }
                                }
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(stringResource(if (recording) R.string.recording_stop_label else R.string.recording_tap_to_record), style = MaterialTheme.typography.titleMedium, color = Ink, textAlign = TextAlign.Center)
                                Text(stringResource(when {
                                    recording -> R.string.recording_stop_helper
                                    cameraReady -> R.string.recording_camera_ready
                                    else -> R.string.recording_camera_starting
                                }), style = MaterialTheme.typography.bodySmall, color = MutedInk, textAlign = TextAlign.Center)
                            }
                        }
                    }
                }
                else -> error("RecordingScreen hanya menerima state izin, siap, atau merekam")
            }
        }
    }
}

private fun formatTimer(seconds: Int): String = "%02d:%02d".format(seconds / 60, seconds % 60)
