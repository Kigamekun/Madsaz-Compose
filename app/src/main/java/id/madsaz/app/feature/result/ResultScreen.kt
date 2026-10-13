package id.madsaz.app.feature.result

import android.media.MediaPlayer
import android.view.Surface
import android.view.TextureView
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import id.madsaz.app.R
import id.madsaz.app.data.remote.RecordingApi
import id.madsaz.app.model.CryScore
import id.madsaz.app.model.RecordingItem
import id.madsaz.app.model.RecordingUiState
import id.madsaz.app.ui.components.CompactContainer
import id.madsaz.app.ui.components.formatDuration
import id.madsaz.app.ui.components.tr
import id.madsaz.app.ui.theme.AppBackground
import id.madsaz.app.ui.theme.BrandBlue
import id.madsaz.app.ui.theme.Ink
import id.madsaz.app.ui.theme.MutedInk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ResultScreen(
    state: RecordingUiState,
    accessToken: String,
    onRecordAgain: () -> Unit,
    onBack: () -> Unit,
    onRefresh: (() -> Unit)? = null,
    onFeedback: (String) -> Unit,
    startInLog: Boolean = false,
    modifier: Modifier = Modifier,
) {
    CompactContainer(modifier) {
        when (state) {
            RecordingUiState.Saving -> ProgressContent(stringResource(R.string.analysis_saving_title), stringResource(R.string.analysis_saving_body))
            RecordingUiState.Analyzing -> ProgressContent(stringResource(R.string.analysis_running_title), stringResource(R.string.analysis_running_body))
            is RecordingUiState.Completed -> CompletedContent(state.recording, accessToken, onBack, onRefresh, onFeedback, startInLog)
            is RecordingUiState.Error -> ErrorContent(
                listOfNotNull(stringResource(state.messageRes), state.detail).joinToString("\n\n"), onRecordAgain)
            else -> error("ResultScreen hanya menerima state hasil rekaman")
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
    accessToken: String,
    onBack: () -> Unit,
    onRefresh: (() -> Unit)?,
    onFeedback: (String) -> Unit,
    startInLog: Boolean,
) {
    var showLog by remember(recording.id, startInLog) { mutableStateOf(startInLog) }
    if (showLog || recording.scores.isEmpty()) {
        RecordingLog(recording, accessToken, onBack, onRefresh, onFeedback, startInLog) { showLog = false }
    } else {
        PatternResult(recording) { onFeedback(recording.id) }
    }
}

@Composable
private fun PatternResult(recording: RecordingItem, onFeedback: () -> Unit) {
    val leading = recording.scores.first()
    Column(Modifier.fillMaxSize().background(Color.White)) {
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 24.dp)) {
            Spacer(Modifier.height(48.dp))
            Image(
                painter = painterResource(R.drawable.madsaz_result_baby_halo),
                contentDescription = stringResource(R.string.result_baby_description),
                modifier = Modifier.align(Alignment.CenterHorizontally).size(104.dp),
                contentScale = ContentScale.Fit,
            )
            Spacer(Modifier.height(20.dp))
            Text("Cry pattern result", style = MaterialTheme.typography.titleLarge, color = Ink, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(16.dp))
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                color = Color(0xFFEAF6FC),
                border = BorderStroke(1.dp, Color(0xFFC8E4F3)),
            ) {
                Row(Modifier.padding(horizontal = 16.dp, vertical = 13.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Text("MOST LIKELY", color = BrandBlue, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        Text("${leading.code} - ${leading.label}", color = Ink, fontSize = 16.sp)
                    }
                    Surface(shape = RoundedCornerShape(18.dp), color = BrandBlue) {
                        Text("${(leading.confidence * 100).toInt()}%", Modifier.padding(horizontal = 12.dp, vertical = 8.dp), color = Color.White, fontSize = 12.sp)
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
            Text("Cry cue confidence", color = Ink, fontSize = 13.sp)
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
                        Text("SUGGESTED ACTION", color = Color(0xFF387A67), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        Text("What you can try", color = Ink, fontSize = 15.sp)
                        Text(recording.solution.orEmpty(), color = MutedInk, fontSize = 11.sp, lineHeight = 14.sp)
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
        }
        Button(onClick = onFeedback, modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 20.dp).height(48.dp)) {
            Text("Feedback")
        }
    }
}

@Composable
private fun ScoreBar(score: CryScore, leading: Boolean) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = if (leading) Color(0xFFEAF6FC) else Color.White,
        border = BorderStroke(1.dp, if (leading) Color(0xFFEAF6FC) else Color(0xFFE6EEF3)),
    ) {
        Row(Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Surface(Modifier.size(36.dp), shape = CircleShape, color = Color(0xFFD6EAF5)) {
                Box(contentAlignment = Alignment.Center) {
                    Text(score.code.take(1).uppercase(), color = BrandBlue, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("${score.code} - ${score.label}", color = if (leading) Ink else MutedInk, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text("${(score.confidence * 100).toInt()}%", color = if (leading) BrandBlue else MutedInk, fontSize = 11.sp)
                }
                Box(Modifier.fillMaxWidth().height(7.dp).background(Color(0xFFE2EBF0), RoundedCornerShape(4.dp))) {
                    Box(Modifier.fillMaxWidth(score.confidence.toFloat().coerceIn(0f, 1f)).height(7.dp)
                        .background(if (leading) BrandBlue else Color(0xFFA9C7D5), RoundedCornerShape(4.dp)))
                }
            }
        }
    }
}

@Composable
private fun RecordingLog(
    recording: RecordingItem,
    accessToken: String,
    onBack: () -> Unit,
    onRefresh: (() -> Unit)?,
    onFeedback: (String) -> Unit,
    startInLog: Boolean,
    onResult: () -> Unit,
) {
    val context = LocalContext.current
    var playbackError by remember(recording.id) { mutableStateOf(false) }
    var playing by remember(recording.id) { mutableStateOf(false) }
    var loadingVideo by remember(recording.id) { mutableStateOf(false) }
    var videoUrl by remember(recording.id) { mutableStateOf<String?>(null) }
    var videoAspectRatio by remember(recording.id) {
        mutableFloatStateOf(
            if (recording.videoWidth != null && recording.videoHeight != null && recording.videoHeight > 0) {
                recording.videoWidth.toFloat() / recording.videoHeight
            } else 16f / 9f,
        )
    }
    var videoSurface by remember(recording.id) { mutableStateOf<Surface?>(null) }
    var mediaPlayer by remember(recording.id) { mutableStateOf<MediaPlayer?>(null) }
    val scope = rememberCoroutineScope()
    DisposableEffect(recording.id) {
        onDispose {
            mediaPlayer?.release()
            videoSurface?.release()
        }
    }
    DisposableEffect(videoUrl, videoSurface) {
        val url = videoUrl
        val surface = videoSurface
        if (url == null || surface == null) return@DisposableEffect onDispose { }

        val player = MediaPlayer()
        mediaPlayer = player
        player.setSurface(surface)
        player.setOnPreparedListener {
            loadingVideo = false
            playing = true
            it.start()
        }
        player.setOnVideoSizeChangedListener { _, width, height ->
            if (width > 0 && height > 0) videoAspectRatio = width.toFloat() / height
        }
        player.setOnCompletionListener { playing = false }
        player.setOnErrorListener { _, _, _ ->
            playbackError = true
            playing = false
            loadingVideo = false
            true
        }
        try {
            player.setDataSource(url)
            player.prepareAsync()
        } catch (_: Exception) {
            playbackError = true
            loadingVideo = false
        }
        onDispose {
            player.release()
            if (mediaPlayer === player) mediaPlayer = null
        }
    }
    val playRecording = {
        scope.launch {
            loadingVideo = true
            playbackError = false
            try {
                val url = withContext(Dispatchers.IO) {
                    RecordingApi().presignedVideoUrl(accessToken, recording.id)
                }
                videoUrl = url
            } catch (_: Exception) {
                loadingVideo = false
                playbackError = true
            }
        }
    }
    Column(Modifier.fillMaxSize().background(Color.White).statusBarsPadding()) {
        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 24.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(tr("Recording history", "Riwayat rekaman"), style = MaterialTheme.typography.headlineMedium, color = Ink)
            Text(
                "${SimpleDateFormat("d MMM yyyy", Locale.forLanguageTag("id-ID")).format(Date(recording.recordedAtEpochMillis))}  ·  ${SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(recording.recordedAtEpochMillis))}  ·  ${formatDuration(recording.durationMillis)}",
                color = MutedInk,
            )
            Surface(
                modifier = Modifier.fillMaxWidth().heightIn(max = 260.dp)
                    .aspectRatio(videoAspectRatio.coerceIn(0.5f, 2.5f), matchHeightConstraintsFirst = true),
                shape = RoundedCornerShape(22.dp),
                color = Color(0xFFEAF5FB),
            ) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                AndroidView(
                    factory = { viewContext ->
                        TextureView(viewContext).apply {
                            surfaceTextureListener = object : TextureView.SurfaceTextureListener {
                                override fun onSurfaceTextureAvailable(texture: android.graphics.SurfaceTexture, width: Int, height: Int) {
                                    videoSurface = Surface(texture)
                                }
                                override fun onSurfaceTextureSizeChanged(texture: android.graphics.SurfaceTexture, width: Int, height: Int) = Unit
                                override fun onSurfaceTextureUpdated(texture: android.graphics.SurfaceTexture) = Unit
                                override fun onSurfaceTextureDestroyed(texture: android.graphics.SurfaceTexture): Boolean {
                                    videoSurface?.release()
                                    videoSurface = null
                                    return true
                                }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxSize(),
                )
                if (loadingVideo) {
                    CircularProgressIndicator(color = BrandBlue)
                } else if (!playing && !playbackError) {
                    Button(onClick = { playRecording() }) { Text("Putar rekaman") }
                }
            }
            }
            if (playbackError) {
                Text("Video gagal diputar. Periksa koneksi lalu coba lagi.", color = MaterialTheme.colorScheme.error)
                OutlinedButton(onClick = { playRecording() }) { Text(stringResource(R.string.common_retry)) }
            }
            if (recording.status == "processing" && onRefresh != null) {
                OutlinedButton(onClick = onRefresh) { Text("Perbarui hasil") }
            }
            if (recording.scores.isNotEmpty()) {
                Text("Hasil analisis", style = MaterialTheme.typography.titleLarge, color = Ink, fontWeight = FontWeight.Bold)
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    color = Color.White,
                    border = BorderStroke(1.dp, Color(0xFFE2EDF3)),
                ) {
                    Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        recording.scores.forEachIndexed { index, score -> ScoreBar(score, index == 0) }
                    }
                }
                recording.solution?.let { solution ->
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        color = Color(0xFFEAF5FB),
                    ) {
                        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Saran", style = MaterialTheme.typography.titleMedium, color = Ink, fontWeight = FontWeight.Bold)
                            Text(solution, color = MutedInk)
                        }
                    }
                }
            } else {
                Text(
                    if (recording.status == "failed") "Analisis gagal. Video tetap tersimpan." else "Menunggu hasil analisis.",
                    color = Ink,
                )
            }
        }
        Button(
            onClick = if (startInLog || recording.scores.isEmpty()) onBack else onResult,
            modifier = Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = 24.dp, vertical = 12.dp).height(54.dp),
            shape = RoundedCornerShape(50),
        ) { Text("Kembali") }
    }
}

@Composable
private fun ErrorContent(message: String, onRetry: () -> Unit) {
    Box(Modifier.fillMaxSize().background(AppBackground).padding(24.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(message, style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
            OutlinedButton(onClick = onRetry) { Text(stringResource(R.string.common_retry)) }
        }
    }
}
