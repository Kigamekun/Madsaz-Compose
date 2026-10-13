package id.madsaz.app.feature.result

import android.net.Uri
import android.widget.MediaController
import android.widget.VideoView
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import id.madsaz.app.R
import id.madsaz.app.model.CryScore
import id.madsaz.app.model.RecordingItem
import id.madsaz.app.model.RecordingStatus
import id.madsaz.app.model.RecordingUiState
import id.madsaz.app.ui.components.CompactContainer
import id.madsaz.app.ui.components.formatDateTime
import id.madsaz.app.ui.components.formatDuration
import id.madsaz.app.ui.theme.AppBackground
import id.madsaz.app.ui.theme.BrandBlue
import id.madsaz.app.ui.theme.Ink
import id.madsaz.app.ui.theme.MutedInk
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

/** Layar sementara saat video disimpan dan diunggah, atau saat unggahan gagal. Hasil analisis ada di [ResultScreen]. */
@Composable
fun RecordingStatusScreen(
    state: RecordingUiState,
    onRetry: () -> Unit,
    onDiscard: (() -> Unit)?,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    CompactContainer(modifier) {
        when (state) {
            RecordingUiState.Saving -> ProgressContent(stringResource(R.string.analysis_saving_title), stringResource(R.string.analysis_saving_body))
            // Completed sebentar tampil di sini sebelum rute hasil terbuka.
            RecordingUiState.Analyzing, is RecordingUiState.Completed ->
                ProgressContent(stringResource(R.string.analysis_running_title), stringResource(R.string.analysis_running_body))
            is RecordingUiState.Error -> ErrorContent(stringResource(state.messageRes), state.canRetry, onRetry, onDiscard, onBack)
            else -> error("RecordingStatusScreen hanya menerima state menyimpan, mengunggah, selesai, atau gagal")
        }
    }
}

@Composable
fun ResultScreen(
    state: ResultUiState,
    loadVideoUrl: suspend () -> String,
    onBack: () -> Unit,
    onRefresh: () -> Unit,
    onFeedback: (String) -> Unit,
    startInLog: Boolean = false,
    modifier: Modifier = Modifier,
) {
    CompactContainer(modifier) {
        when (state) {
            ResultUiState.Loading ->
                ProgressContent(stringResource(R.string.result_loading_title), stringResource(R.string.result_loading_body))
            is ResultUiState.Error -> ErrorContent(stringResource(state.messageRes), true, onRefresh, null, onBack)
            is ResultUiState.Content -> {
                val recording = state.recording
                if (recording.status == RecordingStatus.PROCESSING && !startInLog) {
                    ProgressContent(stringResource(R.string.result_processing_title), stringResource(R.string.result_processing_body))
                } else {
                    CompletedContent(recording, loadVideoUrl, onBack, onRefresh, onFeedback, startInLog)
                }
            }
        }
    }
}

@Composable
private fun ProgressContent(title: String, body: String) {
    Box(Modifier.fillMaxSize().background(AppBackground), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(14.dp)) {
            CircularProgressIndicator()
            Text(title, style = MaterialTheme.typography.headlineMedium, textAlign = TextAlign.Center)
            Text(body, style = MaterialTheme.typography.bodyMedium, color = MutedInk, textAlign = TextAlign.Center)
        }
    }
}

@Composable
private fun CompletedContent(
    recording: RecordingItem,
    loadVideoUrl: suspend () -> String,
    onBack: () -> Unit,
    onRefresh: () -> Unit,
    onFeedback: (String) -> Unit,
    startInLog: Boolean,
) {
    var showLog by remember(recording.id, startInLog) { mutableStateOf(startInLog) }
    if (showLog || recording.scores.isEmpty()) {
        RecordingLog(recording, loadVideoUrl, onBack, onRefresh, onFeedback, startInLog) { showLog = false }
    } else {
        PatternResult(recording) { onFeedback(recording.id) }
    }
}

@Composable
private fun PatternResult(recording: RecordingItem, onFeedback: () -> Unit) {
    val leading = recording.scores.first()
    Column(Modifier.fillMaxSize().background(Color.White)) {
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).statusBarsPadding().padding(horizontal = 24.dp)) {
            Spacer(Modifier.height(24.dp))
            Image(
                painter = painterResource(R.drawable.madsaz_result_baby_halo),
                contentDescription = stringResource(R.string.result_baby_description),
                modifier = Modifier.align(Alignment.CenterHorizontally).size(104.dp),
                contentScale = ContentScale.Fit,
            )
            Spacer(Modifier.height(20.dp))
            Text(stringResource(R.string.result_title), style = MaterialTheme.typography.titleLarge, color = Ink, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(16.dp))
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                color = Color(0xFFEAF6FC),
                border = BorderStroke(1.dp, Color(0xFFC8E4F3)),
            ) {
                Row(Modifier.padding(horizontal = 16.dp, vertical = 13.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Text(stringResource(R.string.result_most_likely), color = BrandBlue, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        Text("${leading.code} - ${leading.label}", color = Ink, fontSize = 16.sp)
                    }
                    Surface(shape = RoundedCornerShape(18.dp), color = BrandBlue) {
                        Text("${(leading.confidence * 100).toInt()}%", Modifier.padding(horizontal = 12.dp, vertical = 8.dp), color = Color.White, fontSize = 12.sp)
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
            Text(stringResource(R.string.result_confidence_title), color = Ink, fontSize = 13.sp)
            Spacer(Modifier.height(10.dp))
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                recording.scores.forEachIndexed { index, score -> ScoreBar(score, index == 0) }
            }
            Spacer(Modifier.height(18.dp))
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                color = Color.White,
                border = BorderStroke(1.dp, Color(0xFFD5E6EE)),
            ) {
                Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(Modifier.size(44.dp).background(Color(0xFFE8F5EE), RoundedCornerShape(22.dp)))
                    Column {
                        Text(stringResource(R.string.result_suggested_action), color = Color(0xFF387A67), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        Text(stringResource(R.string.result_what_to_try), color = Ink, fontSize = 15.sp)
                        Text(recording.solution.orEmpty(), color = MutedInk, fontSize = 11.sp, lineHeight = 14.sp)
                    }
                }
            }
            if (recording.source == "mock") {
                Spacer(Modifier.height(10.dp))
                Text(stringResource(R.string.result_mock_note), color = MutedInk, fontSize = 10.sp)
            }
            Spacer(Modifier.height(16.dp))
        }
        Button(onClick = onFeedback, modifier = Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = 24.dp, vertical = 20.dp).height(48.dp)) {
            Text(stringResource(R.string.result_feedback))
        }
    }
}

@Composable
private fun ScoreBar(score: CryScore, leading: Boolean) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("${score.code} - ${score.label}", color = if (leading) Ink else MutedInk, fontSize = 11.sp)
            Text("${(score.confidence * 100).toInt()}%", color = if (leading) BrandBlue else MutedInk, fontSize = 11.sp)
        }
        Box(Modifier.fillMaxWidth().height(7.dp).background(Color(0xFFE8EEF1), RoundedCornerShape(4.dp))) {
            Box(Modifier.fillMaxWidth(score.confidence.toFloat().coerceIn(0f, 1f)).height(7.dp)
                .background(if (leading) BrandBlue else Color(0xFFA9C7D5), RoundedCornerShape(4.dp)))
        }
    }
}

@Composable
private fun RecordingLog(
    recording: RecordingItem,
    loadVideoUrl: suspend () -> String,
    onBack: () -> Unit,
    onRefresh: () -> Unit,
    onFeedback: (String) -> Unit,
    startInLog: Boolean,
    onResult: () -> Unit,
) {
    val context = LocalContext.current
    var playbackError by remember(recording.id) { mutableStateOf(false) }
    var playing by remember(recording.id) { mutableStateOf(false) }
    var loadingVideo by remember(recording.id) { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val player = remember(context, recording.id) {
        VideoView(context).apply {
            val controls = MediaController(context)
            controls.setAnchorView(this)
            setMediaController(controls)
            setOnCompletionListener { playing = false }
            setOnErrorListener { _, _, _ -> playbackError = true; playing = false; loadingVideo = false; true }
            setBackgroundColor(android.graphics.Color.BLACK)
        }
    }
    DisposableEffect(player) { onDispose { player.stopPlayback() } }
    val playRecording = {
        scope.launch {
            loadingVideo = true
            playbackError = false
            try {
                val url = loadVideoUrl()
                player.setOnPreparedListener { mediaPlayer ->
                    loadingVideo = false
                    playing = true
                    mediaPlayer.start()
                }
                player.setVideoURI(Uri.parse(url))
            } catch (error: CancellationException) {
                throw error
            } catch (_: Exception) {
                // Gagal mengambil URL atau memutar: pengguna melihat pesan "video gagal diputar" dengan tombol coba lagi.
                loadingVideo = false
                playbackError = true
            }
        }
    }
    Column(
        Modifier.fillMaxSize().background(Color.White).verticalScroll(rememberScrollState()).statusBarsPadding().navigationBarsPadding().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        OutlinedButton(onClick = if (startInLog || recording.scores.isEmpty()) onBack else onResult) { Text(stringResource(R.string.common_back)) }
        Text(stringResource(R.string.result_log_title), style = MaterialTheme.typography.headlineMedium, color = Ink)
        Text(
            "${formatDateTime(recording.recordedAtEpochMillis)}  ·  ${formatDuration(recording.durationMillis)}",
            color = MutedInk,
        )
        if (recording.scores.isNotEmpty()) {
            Text("${recording.scores.first().code} - ${recording.scores.first().label}", style = MaterialTheme.typography.titleMedium, color = Ink)
        } else {
            Text(stringResource(if (recording.status == RecordingStatus.FAILED) R.string.result_log_failed else R.string.result_log_pending), color = Ink)
        }
        Surface(shape = RoundedCornerShape(18.dp), color = Color(0xFFF1F7FA)) {
            Box(Modifier.fillMaxWidth().height(260.dp), contentAlignment = Alignment.Center) {
                AndroidView(factory = { player }, modifier = Modifier.fillMaxSize())
                if (loadingVideo) {
                    CircularProgressIndicator(color = BrandBlue)
                } else if (!playing && !playbackError) {
                    Button(onClick = { playRecording() }) { Text(stringResource(R.string.result_play)) }
                }
            }
        }
        if (playbackError) {
            Text(stringResource(R.string.result_play_error), color = MaterialTheme.colorScheme.error)
            OutlinedButton(onClick = { playRecording() }) { Text(stringResource(R.string.common_retry)) }
        }
        if (recording.status == RecordingStatus.PROCESSING) {
            OutlinedButton(onClick = onRefresh) { Text(stringResource(R.string.result_refresh)) }
        }
        if (recording.source == "mock") {
            Text(stringResource(R.string.result_mock_note), color = MutedInk, fontSize = 10.sp)
        }
        Text(stringResource(R.string.analysis_disclaimer), color = MutedInk, style = MaterialTheme.typography.bodySmall)
        Button(onClick = { onFeedback(recording.id) }, modifier = Modifier.fillMaxWidth().height(48.dp)) { Text(stringResource(R.string.result_feedback)) }
    }
}

@Composable
private fun ErrorContent(message: String, canRetry: Boolean, onRetry: () -> Unit, onDiscard: (() -> Unit)?, onBack: () -> Unit) {
    Box(Modifier.fillMaxSize().background(AppBackground).padding(24.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(message, style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
            if (canRetry) OutlinedButton(onClick = onRetry) { Text(stringResource(R.string.common_retry)) }
            if (onDiscard != null) TextButton(onClick = onDiscard) { Text(stringResource(R.string.recording_discard)) }
            if (!canRetry) OutlinedButton(onClick = onBack) { Text(stringResource(R.string.common_back)) }
        }
    }
}
