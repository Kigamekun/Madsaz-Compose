package id.madsaz.app.feature.recording

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.video.FileOutputOptions
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.camera.video.Recording
import androidx.camera.video.VideoRecordEvent
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.camera.view.video.AudioConfig
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import id.madsaz.app.feature.result.RecordingStatusScreen
import id.madsaz.app.model.RecordingUiState
import java.io.File

@SuppressLint("MissingPermission")
@Composable
fun RecordingRoute(
    viewModel: RecordingViewModel,
    onBack: () -> Unit,
    onRecordingSaved: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var permissionsGranted by remember { mutableStateOf(context.hasRecordingPermissions()) }
    var permissionPreviouslyRequested by rememberSaveable { mutableStateOf(false) }
    var activeRecording by remember { mutableStateOf<Recording?>(null) }
    var currentFile by remember { mutableStateOf<File?>(null) }
    var audioLevels by remember { mutableStateOf(emptyList<Float>()) }

    val packageManager = context.packageManager
    val hasBackCamera = packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA)
    val hasFrontCamera = packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_FRONT)
    var usingFrontCamera by rememberSaveable { mutableStateOf(!hasBackCamera && hasFrontCamera) }

    val cameraController = remember(context) {
        LifecycleCameraController(context.applicationContext).apply {
            setEnabledUseCases(CameraController.VIDEO_CAPTURE)
            setVideoCaptureQualitySelector(
                QualitySelector.fromOrderedList(listOf(Quality.HD, Quality.SD, Quality.LOWEST)),
            )
            setCameraSelector(
                if (usingFrontCamera) CameraSelector.DEFAULT_FRONT_CAMERA
                else CameraSelector.DEFAULT_BACK_CAMERA,
            )
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) {
        permissionPreviouslyRequested = true
        permissionsGranted = context.hasRecordingPermissions()
    }

    val latestActiveRecording by rememberUpdatedState(activeRecording)

    LaunchedEffect(permissionsGranted) {
        viewModel.onPermissionState(permissionsGranted)
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                permissionsGranted = context.hasRecordingPermissions()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    DisposableEffect(cameraController) {
        onDispose {
            latestActiveRecording?.close()
            cameraController.unbind()
        }
    }

    val state = viewModel.uiState
    val completedId = (state as? RecordingUiState.Completed)?.recordingId
    LaunchedEffect(completedId) {
        if (completedId != null) onRecordingSaved(completedId)
    }

    fun stopRecording() {
        val recording = activeRecording ?: return
        recording.stop()
    }

    BackHandler(enabled = state is RecordingUiState.Recording) { stopRecording() }
    BackHandler(enabled = state is RecordingUiState.Saving || state is RecordingUiState.Analyzing) { }

    when (state) {
        RecordingUiState.PermissionRequired,
        RecordingUiState.Ready,
        is RecordingUiState.Recording,
        -> RecordingScreen(
            state = state,
            permissionPreviouslyRequested = permissionPreviouslyRequested,
            canSwitchCamera = hasBackCamera && hasFrontCamera,
            onBack = { if (state is RecordingUiState.Recording) stopRecording() else onBack() },
            onRequestPermission = { permissionLauncher.launch(REQUIRED_PERMISSIONS) },
            onOpenSettings = { context.openAppSettings() },
            onSwitchCamera = {
                usingFrontCamera = !usingFrontCamera
                try {
                    cameraController.setCameraSelector(
                        if (usingFrontCamera) CameraSelector.DEFAULT_FRONT_CAMERA
                        else CameraSelector.DEFAULT_BACK_CAMERA,
                    )
                } catch (_: IllegalStateException) {
                    viewModel.onCameraError()
                }
            },
            onStart = {
                if (permissionsGranted && activeRecording == null) {
                    audioLevels = emptyList()
                    val outputFile = viewModel.createOutputFile()
                    if (outputFile != null) {
                        currentFile = outputFile
                        try {
                            activeRecording = cameraController.startRecording(
                                FileOutputOptions.Builder(outputFile)
                                    .setDurationLimitMillis(MAX_DURATION_MILLIS)
                                    .build(),
                                AudioConfig.create(true),
                                ContextCompat.getMainExecutor(context),
                            ) { event ->
                                when (event) {
                                    is VideoRecordEvent.Start -> viewModel.onRecordingStarted()
                                    is VideoRecordEvent.Status -> {
                                        viewModel.onRecordingProgress(event.recordingStats.recordedDurationNanos)
                                        val amplitude = event.recordingStats.audioStats.audioAmplitude.toFloat()
                                        audioLevels = (audioLevels + amplitude).takeLast(48)
                                    }
                                    is VideoRecordEvent.Finalize -> {
                                        activeRecording = null
                                        val finalizedFile = currentFile
                                        currentFile = null
                                        cameraController.unbind()
                                        viewModel.onSaving()
                                        if (finalizedFile != null && event.isUsableResult()) {
                                            viewModel.onRecordingFinalized(finalizedFile)
                                        } else if (finalizedFile != null) {
                                            viewModel.onRecordingFailed(finalizedFile)
                                        } else {
                                            viewModel.onCameraError()
                                        }
                                    }
                                    else -> Unit
                                }
                            }
                        } catch (_: RuntimeException) {
                            activeRecording = null
                            currentFile = null
                            cameraController.unbind()
                            viewModel.onRecordingFailed(outputFile)
                        }
                    }
                }
            },
            onStop = ::stopRecording,
            audioLevels = audioLevels,
            cameraPreview = { previewModifier ->
                CameraPreview(
                    controller = cameraController,
                    onError = viewModel::onCameraError,
                    modifier = previewModifier,
                )
            },
            modifier = modifier,
        )
        RecordingUiState.Saving,
        RecordingUiState.Analyzing,
        is RecordingUiState.Completed,
        is RecordingUiState.Error,
        -> RecordingStatusScreen(
            state = state,
            onRetry = {
                if (viewModel.hasPendingUpload) viewModel.retryUpload()
                else viewModel.reset(permissionsGranted)
            },
            onDiscard = if (viewModel.hasPendingUpload) ({ viewModel.discardPending(permissionsGranted) }) else null,
            onBack = onBack,
            modifier = modifier,
        )
    }
}

@Composable
private fun CameraPreview(
    controller: LifecycleCameraController,
    onError: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(controller, lifecycleOwner) {
        try {
            controller.bindToLifecycle(lifecycleOwner)
        } catch (_: RuntimeException) {
            onError()
        }
        onDispose { controller.unbind() }
    }

    AndroidView(
        factory = { context ->
            PreviewView(context).apply {
                implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                scaleType = PreviewView.ScaleType.FILL_CENTER
                this.controller = controller
            }
        },
        modifier = modifier,
    )
}

internal fun Context.hasRecordingPermissions(): Boolean = REQUIRED_PERMISSIONS.all { permission ->
    ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
}

private fun Context.openAppSettings() {
    startActivity(
        Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.fromParts("package", packageName, null),
        ),
    )
}

private fun VideoRecordEvent.Finalize.isUsableResult(): Boolean =
    error == VideoRecordEvent.Finalize.ERROR_NONE ||
        error == VideoRecordEvent.Finalize.ERROR_DURATION_LIMIT_REACHED

private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)
private const val MAX_DURATION_MILLIS = MAX_RECORDING_SECONDS * 1_000L
