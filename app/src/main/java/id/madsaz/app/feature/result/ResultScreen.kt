package id.madsaz.app.feature.result

import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import android.net.Uri
import android.widget.MediaController
import android.widget.VideoView
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import id.madsaz.app.R
import androidx.compose.material3.OutlinedButton
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.TextButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.Icons
import id.madsaz.app.ui.components.cryCueLabel
import id.madsaz.app.model.CryCue
import id.madsaz.app.model.CryScore
import id.madsaz.app.model.RecordingItem
import id.madsaz.app.model.RecordingStatus
import id.madsaz.app.model.RecordingUiState
import id.madsaz.app.ui.components.CompactContainer
import id.madsaz.app.ui.components.CryCueIcon
import id.madsaz.app.ui.components.formatDateTime
import id.madsaz.app.ui.components.formatDuration
import id.madsaz.app.ui.components.formatPercent
import id.madsaz.app.ui.theme.AppBackground
import id.madsaz.app.ui.theme.BrandBlueDark
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
    onOpenCue: (CryCue) -> Unit,
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
                    CompletedContent(recording, loadVideoUrl, onBack, onRefresh, onFeedback, onOpenCue, startInLog)
                }
            }
        }
    }
}

@Composable
private fun ProgressContent(title: String, body: String) {
    Box(Modifier.fillMaxSize().background(AppBackground).windowInsetsPadding(WindowInsets.safeDrawing).padding(20.dp), contentAlignment = Alignment.Center) {
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
    onOpenCue: (CryCue) -> Unit,
    startInLog: Boolean,
) {
    var showLog by remember(recording.id, startInLog) { mutableStateOf(startInLog) }
    if (showLog || recording.scores.isEmpty()) {
        RecordingLog(recording, loadVideoUrl, onBack, onRefresh, onOpenCue, startInLog) { showLog = false }
    } else {
        PatternResult(recording, onDone = onBack, onOpenCue = onOpenCue) { onFeedback(recording.id) }
    }
}

@Composable
private fun PatternResult(recording: RecordingItem, onDone: () -> Unit, onOpenCue: (CryCue) -> Unit, onFeedback: () -> Unit) {
    val leading = recording.scores.first()
    Column(Modifier.fillMaxSize().background(Color.White).statusBarsPadding()) {
        // Jalan keluar yang terlihat (selain tombol kembali sistem): tutup kembali ke layar asal.
        IconButton(onClick = onDone, modifier = Modifier.padding(start = 8.dp, top = 4.dp)) {
            Icon(Icons.Rounded.Close, stringResource(R.string.common_close), tint = Ink)
        }
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp)) {
            Image(
                painter = painterResource(R.drawable.madsaz_result_baby_halo),
                contentDescription = null,
                modifier = Modifier.size(80.dp).align(Alignment.CenterHorizontally),
            )
            Spacer(Modifier.height(12.dp))
            Text(stringResource(R.string.result_title), style = MaterialTheme.typography.headlineMedium, color = Ink, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(14.dp))
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                color = Color(0xFFEAF6FC),
                border = BorderStroke(1.dp, Color(0xFFC8E4F3)),
            ) {
                Row(Modifier.padding(horizontal = 16.dp, vertical = 13.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f).padding(end = 12.dp)) {
                        Text(stringResource(R.string.result_most_likely), color = BrandBlueDark, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text("${leading.code} · ${cryCueLabel(leading.code, leading.label)}", color = Ink, fontSize = 16.sp)
                    }
                    Surface(shape = RoundedCornerShape(18.dp), color = BrandBlueDark) {
                        Text(formatPercent(leading.confidence), Modifier.padding(horizontal = 12.dp, vertical = 8.dp), color = Color.White, fontSize = 12.sp)
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
                    CryCueIcon(leading.code, Modifier.size(48.dp))
                    Column(Modifier.weight(1f)) {
                        Text(stringResource(R.string.result_suggested_action), color = Color(0xFF387A67), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text(stringResource(R.string.result_what_to_try), color = Ink, fontSize = 15.sp)
                        Text(recording.solution.orEmpty(), color = MutedInk, fontSize = 14.sp, lineHeight = 21.sp)
                    }
                }
            }
            LearnCueLink(leading.code, onOpenCue)
            Spacer(Modifier.height(16.dp))
        }
        Row(
            Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            OutlinedButton(onClick = onFeedback, modifier = Modifier.weight(1f).heightIn(min = 48.dp)) {
                Text(stringResource(R.string.result_feedback))
            }
            Button(onClick = onDone, modifier = Modifier.weight(1f).heightIn(min = 48.dp)) {
                Text(stringResource(R.string.result_done))
            }
        }
    }
}

/** Tautan ke halaman Petunjuk untuk pola yang dikenal; pola di luar lima pola tidak punya halaman. */
@Composable
private fun LearnCueLink(code: String, onOpenCue: (CryCue) -> Unit) {
    val cue = CryCue.fromCode(code) ?: return
    TextButton(onClick = { onOpenCue(cue) }, modifier = Modifier.padding(top = 4.dp).heightIn(min = 48.dp)) {
        Text(stringResource(R.string.result_learn_cue, cue.code), color = BrandBlueDark, fontWeight = FontWeight.SemiBold)
        Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, contentDescription = null, tint = BrandBlueDark)
    }
}

@Composable
private fun ScoreBar(score: CryScore, leading: Boolean) {
    Column(Modifier.fillMaxWidth().padding(vertical = 3.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            CryCueIcon(score.code, Modifier.size(30.dp))
            Text("${score.code} · ${cryCueLabel(score.code, score.label)}", color = if (leading) Ink else MutedInk, fontSize = 14.sp, modifier = Modifier.weight(1f))
            Text(formatPercent(score.confidence), color = if (leading) BrandBlueDark else MutedInk, fontSize = 14.sp)
        }
        Box(Modifier.fillMaxWidth().height(7.dp).background(Color(0xFFE2EBF0), RoundedCornerShape(4.dp))) {
            Box(Modifier.fillMaxWidth(score.confidence.toFloat().coerceIn(0f, 1f)).height(7.dp)
                .background(if (leading) BrandBlueDark else Color(0xFFA9C7D5), RoundedCornerShape(4.dp)))
        }
    }
}

@Composable
private fun RecordingLog(
    recording: RecordingItem,
    loadVideoUrl: suspend () -> String,
    onBack: () -> Unit,
    onRefresh: () -> Unit,
    onOpenCue: (CryCue) -> Unit,
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
    // Rasio kotak video dari ukuran tampilan yang disimpan BE; video potret tidak membuat kotak lebih tinggi dari persegi.
    val videoRatio = recording.videoWidth?.let { width -> recording.videoHeight?.let { height -> width.toFloat() / height } } ?: DEFAULT_VIDEO_RATIO
    Column(Modifier.fillMaxSize().background(Color.White).statusBarsPadding()) {
        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(stringResource(R.string.result_log_title), style = MaterialTheme.typography.headlineMedium, color = Ink)
            Text(
                "${formatDateTime(recording.recordedAtEpochMillis)}  ·  ${formatDuration(recording.durationMillis)}",
                color = MutedInk,
            )
            Surface(
                modifier = Modifier.fillMaxWidth().aspectRatio(videoRatio.coerceIn(1f, 2.5f)),
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFFEAF5FB),
            ) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    AndroidView(factory = { player }, modifier = Modifier.fillMaxSize())
                    if (loadingVideo) {
                        CircularProgressIndicator(color = BrandBlueDark)
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
            if (recording.scores.isNotEmpty()) {
                Text(stringResource(R.string.result_analysis_title), style = MaterialTheme.typography.titleLarge, color = Ink, fontWeight = FontWeight.Bold)
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White,
                    border = BorderStroke(1.dp, Color(0xFFE2EDF3)),
                ) {
                    Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        recording.scores.forEachIndexed { index, score -> ScoreBar(score, index == 0) }
                    }
                }
                LearnCueLink(recording.scores.first().code, onOpenCue)
                recording.solution?.let { solution ->
                    Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), color = Color(0xFFEAF5FB)) {
                        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(stringResource(R.string.result_advice_title), style = MaterialTheme.typography.titleMedium, color = Ink, fontWeight = FontWeight.Bold)
                            Text(solution, color = MutedInk)
                        }
                    }
                }
            } else {
                Text(stringResource(if (recording.status == RecordingStatus.FAILED) R.string.result_log_failed else R.string.result_log_pending), color = Ink)
            }
            Text(stringResource(R.string.analysis_disclaimer), color = MutedInk, style = MaterialTheme.typography.bodySmall)
        }
        Button(
            onClick = if (startInLog || recording.scores.isEmpty()) onBack else onResult,
            modifier = Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = 20.dp, vertical = 12.dp).heightIn(min = 52.dp),
            shape = RoundedCornerShape(50),
        ) { Text(stringResource(R.string.common_back)) }
    }
}

private const val DEFAULT_VIDEO_RATIO = 16f / 9f

@Composable
private fun ErrorContent(message: String, canRetry: Boolean, onRetry: () -> Unit, onDiscard: (() -> Unit)?, onBack: () -> Unit) {
    Box(Modifier.fillMaxSize().background(AppBackground).windowInsetsPadding(WindowInsets.safeDrawing).padding(20.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(message, style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
            if (canRetry) OutlinedButton(onClick = onRetry) { Text(stringResource(R.string.common_retry)) }
            if (onDiscard != null) TextButton(onClick = onDiscard) { Text(stringResource(R.string.recording_discard)) }
            if (!canRetry) OutlinedButton(onClick = onBack) { Text(stringResource(R.string.common_back)) }
        }
    }
}
