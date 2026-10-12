package id.madsaz.app.feature.recording

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.activity.compose.BackHandler
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.madsaz.app.R
import id.madsaz.app.model.RecordingUiState
import id.madsaz.app.ui.components.BabyMark
import id.madsaz.app.ui.components.CompactContainer
import id.madsaz.app.ui.theme.AppBackground
import id.madsaz.app.ui.theme.BrandBlue
import id.madsaz.app.ui.theme.Ink
import id.madsaz.app.ui.theme.MutedInk

private val CameraPanel = Color(0xFFE6F0FC)

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
    cameraPreview: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
    BackHandler(enabled = state == RecordingUiState.PermissionRequired || state == RecordingUiState.Ready) {
        onBack()
    }
    when (state) {
        RecordingUiState.PermissionRequired -> PermissionContent(
            permissionPreviouslyRequested = permissionPreviouslyRequested,
            onBack = onBack,
            onRequestPermission = onRequestPermission,
            onOpenSettings = onOpenSettings,
            modifier = modifier,
        )
        RecordingUiState.Ready,
        is RecordingUiState.Recording -> ListenCameraContent(
            canSwitchCamera = canSwitchCamera,
            onBack = onBack,
            onSwitchCamera = onSwitchCamera,
            onStart = onStart,
            onStop = onStop,
            elapsedSeconds = (state as? RecordingUiState.Recording)?.elapsedSeconds,
            cameraPreview = cameraPreview,
            modifier = modifier,
        )
        else -> error("RecordingScreen hanya menerima state izin, siap, atau merekam")
    }
}

@Composable
private fun PermissionContent(
    permissionPreviouslyRequested: Boolean,
    onBack: () -> Unit,
    onRequestPermission: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier,
) {
    Scaffold(containerColor = AppBackground, modifier = modifier) { padding ->
        CompactContainer(Modifier.padding(padding)) {
            Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    BabyMark(Modifier.size(82.dp))
                    Text(
                        stringResource(R.string.permission_title),
                        style = MaterialTheme.typography.headlineMedium,
                        textAlign = TextAlign.Center,
                    )
                    Text(
                        stringResource(R.string.permission_body),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MutedInk,
                        textAlign = TextAlign.Center,
                    )
                    Button(
                        onClick = if (permissionPreviouslyRequested) onOpenSettings else onRequestPermission,
                        modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                    ) {
                        Text(
                            stringResource(
                                if (permissionPreviouslyRequested) R.string.permission_settings
                                else R.string.permission_allow,
                            ),
                        )
                    }
                    TextButton(onClick = onBack) { Text(stringResource(R.string.common_back)) }
                }
            }
        }
    }
}

@Composable
private fun ListenCameraContent(
    canSwitchCamera: Boolean,
    onBack: () -> Unit,
    onSwitchCamera: () -> Unit,
    onStart: () -> Unit,
    onStop: () -> Unit,
    elapsedSeconds: Int?,
    cameraPreview: @Composable () -> Unit,
    modifier: Modifier,
) {
    Box(modifier.fillMaxSize().background(CameraPanel)) {
        Column(Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(542.dp)
                    .clip(RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp)),
            ) {
                // This slot is the CameraX PreviewView supplied by RecordingRoute.
                cameraPreview()
                Surface(
                    onClick = onBack,
                    modifier = Modifier.align(Alignment.TopStart).offset(x = 14.dp, y = 20.dp).size(40.dp),
                    shape = CircleShape,
                    color = Color(0x7A1A254A),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = stringResource(R.string.common_back), tint = Color.White)
                    }
                }
                Box(
                    Modifier
                        .align(Alignment.BottomStart)
                        .fillMaxWidth()
                        .height(42.dp)
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.Transparent, CameraPanel),
                            ),
                        ),
                )
                Box(
                    Modifier
                        .align(Alignment.TopStart)
                        .fillMaxWidth()
                        .height(126.dp)
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color(0x6B0A1226), Color.Transparent),
                            ),
                        ),
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .offset(x = 28.dp, y = 138.dp)
                        .size(width = 334.dp, height = 284.dp)
                        .border(1.dp, Color.White.copy(alpha = 0.46f), RoundedCornerShape(22.dp)),
                )
                Text(
                    text = if (elapsedSeconds == null) stringResource(R.string.recording_keep_baby_in_view) else formatTimer(elapsedSeconds),
                    modifier = Modifier.align(Alignment.TopCenter).offset(y = 30.dp),
                    color = Color.White,
                    fontFamily = MaterialTheme.typography.headlineMedium.fontFamily,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.SemiBold,
                )
                Surface(
                    modifier = Modifier.align(Alignment.TopEnd).offset(x = (-14).dp, y = 20.dp),
                    shape = CircleShape,
                    color = Color(0x7A1A254A),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(Modifier.size(6.dp).background(Color(0xFFE56E78), CircleShape))
                        Text(if (elapsedSeconds == null) "READY" else "REC", color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(CameraPanel),
            ) {
                Surface(
                    modifier = Modifier.align(Alignment.TopCenter).offset(y = 50.dp).size(136.dp),
                    shape = CircleShape,
                    color = Color.White,
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFDCE5F4)),
                ) { }
                Surface(
                    modifier = Modifier.align(Alignment.TopCenter).offset(y = 64.dp).size(108.dp),
                    shape = CircleShape,
                    color = BrandBlue,
                ) {
                    Box(
                        modifier = Modifier.clickable(
                            role = Role.Button,
                            onClickLabel = stringResource(if (elapsedSeconds == null) R.string.recording_start else R.string.recording_stop),
                            onClick = if (elapsedSeconds == null) onStart else onStop,
                        ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            painter = painterResource(if (elapsedSeconds == null) R.drawable.phosphor_camera else R.drawable.phosphor_stop_circle),
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(38.dp),
                        )
                    }
                }
                Text(
                    text = stringResource(if (elapsedSeconds == null) R.string.recording_tap_to_record else R.string.recording_stop_label),
                    modifier = Modifier.align(Alignment.TopCenter).offset(y = 201.dp),
                    color = Ink,
                    fontFamily = MaterialTheme.typography.headlineMedium.fontFamily,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = stringResource(if (elapsedSeconds == null) R.string.recording_camera_ready else R.string.recording_stop_helper),
                    modifier = Modifier.align(Alignment.TopCenter).offset(y = 234.dp),
                    color = MutedInk,
                    fontSize = 10.sp,
                )
            }
        }
    }
}

private fun formatTimer(seconds: Int): String = "%02d:%02d".format(seconds / 60, seconds % 60)
