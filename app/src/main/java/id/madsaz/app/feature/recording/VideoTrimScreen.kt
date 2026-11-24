package id.madsaz.app.feature.recording

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.widget.VideoView
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.util.UnstableApi
import androidx.media3.effect.Presentation
import androidx.media3.transformer.Composition
import androidx.media3.transformer.EditedMediaItem
import androidx.media3.transformer.Effects
import androidx.media3.transformer.ExportException
import androidx.media3.transformer.ExportResult
import androidx.media3.transformer.Transformer
import id.madsaz.app.R
import id.madsaz.app.ui.components.AppIcons
import id.madsaz.app.ui.components.screenGutter
import java.io.File
import kotlin.math.roundToLong
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

private const val MAX_CLIP_MS = MAX_RECORDING_SECONDS * 1_000L
private const val THUMBNAIL_COUNT = 8

@OptIn(ExperimentalMaterial3Api::class)
@androidx.annotation.OptIn(UnstableApi::class)
@Composable
internal fun VideoTrimScreen(
    uri: Uri,
    onBack: () -> Unit,
    createOutputFile: () -> File?,
    onUpload: (File) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    var video by remember(uri) { mutableStateOf<TrimVideo?>(null) }
    var startMs by rememberSaveable(uri) { mutableLongStateOf(0) }
    var endMs by rememberSaveable(uri) { mutableLongStateOf(0) }
    var readError by remember(uri) { mutableStateOf(false) }
    var exportError by remember(uri) { mutableStateOf(false) }
    var export by remember(uri) { mutableStateOf<Pair<Transformer, File>?>(null) }
    var player by remember(uri) { mutableStateOf<MediaPlayer?>(null) }
    var playing by remember(uri) { mutableStateOf(false) }
    val latestUpload by rememberUpdatedState(onUpload)
    val exporting = export != null

    BackHandler { if (!exporting) onBack() }

    LaunchedEffect(uri) {
        try {
            val source = withContext(Dispatchers.IO) { readTrimVideo(context, uri) }
            video = source
            if (endMs == 0L) endMs = minOf(source.durationMs, MAX_CLIP_MS)
        } catch (error: CancellationException) {
            throw error
        } catch (_: Exception) {
            readError = true
        }
    }

    DisposableEffect(uri) {
        onDispose {
            export?.let { (transformer, file) ->
                transformer.cancel()
                file.delete()
            }
        }
    }

    LaunchedEffect(player, startMs, endMs) {
        player?.let { preview ->
            if (preview.isPlaying) preview.pause()
            preview.seekToClip(startMs)
        }
    }

    LaunchedEffect(player, playing) {
        val preview = player ?: return@LaunchedEffect
        if (!playing) {
            if (preview.isPlaying) preview.pause()
            return@LaunchedEffect
        }
        while (preview.isPlaying && preview.currentPosition < endMs) delay(20)
        if (preview.isPlaying) preview.pause()
        preview.seekToClip(startMs)
        playing = false
    }

    Scaffold(modifier = modifier, contentWindowInsets = WindowInsets.safeDrawing) { padding ->
        Box(Modifier.padding(padding).consumeWindowInsets(padding).padding(horizontal = screenGutter())) {
            Column(
                Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack, enabled = !exporting) {
                        Icon(AppIcons.ArrowBack, stringResource(R.string.common_back))
                    }
                    Text(stringResource(R.string.recording_trim_title), style = MaterialTheme.typography.titleLarge)
                }
                val source = video
                if (readError) {
                    Text(stringResource(R.string.recording_trim_read_error), color = MaterialTheme.colorScheme.error)
                } else if (source == null) {
                    LinearProgressIndicator(Modifier.fillMaxWidth())
                } else {
                    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                        RangeSlider(
                            value = startMs.toFloat()..endMs.toFloat(),
                            valueRange = 0f..source.durationMs.toFloat(),
                            enabled = !exporting,
                            modifier = Modifier.fillMaxWidth(),
                            startThumb = { TrimHandle() },
                            endThumb = { TrimHandle() },
                            track = {
                                Row(
                                    Modifier.fillMaxWidth().height(52.dp).background(Color(0xFF24313A))
                                        .drawWithContent {
                                            drawContent()
                                            val left = size.width * (startMs.toFloat() / source.durationMs)
                                            val right = size.width * (endMs.toFloat() / source.durationMs)
                                            val border = 3.dp.toPx()
                                            val shade = Color.Black.copy(alpha = 0.58f)
                                            drawRect(shade, size = Size(left, size.height))
                                            drawRect(shade, Offset(right, 0f), Size(size.width - right, size.height))
                                            drawRect(Color.White, Offset(left, 0f), Size(right - left, border))
                                            drawRect(Color.White, Offset(left, size.height - border), Size(right - left, border))
                                        },
                                ) {
                                    source.frames.forEach { frame ->
                                        if (frame != null) {
                                            Image(frame.asImageBitmap(), null, Modifier.weight(1f).fillMaxHeight(), contentScale = ContentScale.Crop)
                                        } else {
                                            Spacer(Modifier.weight(1f))
                                        }
                                    }
                                }
                            },
                            onValueChange = { selection ->
                                playing = false
                                val (start, end) = updateClip(startMs, selection.start.roundToLong(), selection.endInclusive.roundToLong(), source.durationMs)
                                startMs = start
                                endMs = end
                                exportError = false
                            },
                        )
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            stringResource(R.string.recording_trim_hint),
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        Text(
                            stringResource(R.string.recording_trim_duration, (endMs - startMs) / 1000.0),
                            style = MaterialTheme.typography.labelLarge,
                        )
                    }
                    Box(
                        Modifier.fillMaxWidth().height(280.dp)
                            .clip(RoundedCornerShape(24.dp)).background(Color.Black),
                        contentAlignment = Alignment.Center,
                    ) {
                        AndroidView(
                            factory = {
                                VideoView(it).apply {
                                    setOnPreparedListener { prepared -> player = prepared }
                                    setOnErrorListener { _, _, _ ->
                                        player = null
                                        playing = false
                                        readError = true
                                        true
                                    }
                                    setVideoURI(uri)
                                }
                            },
                            modifier = Modifier.fillMaxSize(),
                            onRelease = { view ->
                                player = null
                                playing = false
                                view.stopPlayback()
                            },
                        )
                        FilledTonalIconButton(
                            enabled = player != null && !exporting,
                            onClick = {
                                val preview = player
                                if (!playing) preview?.start()
                                playing = !playing
                            },
                        ) {
                            Icon(
                                if (playing) AppIcons.Pause else AppIcons.Play,
                                stringResource(if (playing) R.string.recording_trim_pause else R.string.recording_trim_play),
                            )
                        }
                    }

                    if (exportError) {
                        Text(stringResource(R.string.recording_trim_export_error), color = MaterialTheme.colorScheme.error)
                    }
                    if (exporting) LinearProgressIndicator(Modifier.fillMaxWidth())
                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !exporting && endMs > startMs,
                        onClick = {
                            if (export != null) return@Button
                            val file = createOutputFile() ?: return@Button
                            playing = false
                            exportError = false
                            fun fail() {
                                file.delete()
                                export = null
                                exportError = true
                            }
                            try {
                                val transformer = Transformer.Builder(context)
                                    .setVideoMimeType(MimeTypes.VIDEO_H264)
                                    .setAudioMimeType(MimeTypes.AUDIO_AAC)
                                    .addListener(object : Transformer.Listener {
                                        override fun onCompleted(composition: Composition, result: ExportResult) {
                                            if (file.length() <= 0L || result.approximateDurationMs > MAX_CLIP_MS) {
                                                fail()
                                                return
                                            }
                                            export = null
                                            latestUpload(file)
                                        }

                                        override fun onError(composition: Composition, result: ExportResult, exception: ExportException) {
                                            fail()
                                        }
                                    })
                                    .build()
                                val media = MediaItem.Builder().setUri(uri).setClippingConfiguration(
                                    MediaItem.ClippingConfiguration.Builder()
                                        .setStartPositionMs(startMs)
                                        .setEndPositionMs(endMs)
                                        .build(),
                                ).build()
                                val clip = EditedMediaItem.Builder(media)
                                    .setEffects(Effects(emptyList(), listOf(Presentation.createForHeight(720))))
                                    .build()
                                export = transformer to file
                                transformer.start(clip, file.absolutePath)
                            } catch (_: RuntimeException) {
                                export?.first?.cancel()
                                fail()
                            }
                        },
                    ) {
                        Text(stringResource(if (exporting) R.string.recording_trim_preparing else R.string.recording_trim_upload))
                    }
                }
            }
        }
    }
}

@Composable
private fun TrimHandle() {
    Box(Modifier.size(width = 24.dp, height = 52.dp), contentAlignment = Alignment.Center) {
        Box(Modifier.size(width = 12.dp, height = 52.dp).background(Color.White), contentAlignment = Alignment.Center) {
            Box(Modifier.size(width = 2.dp, height = 16.dp).background(Color(0xFF465660)))
        }
    }
}

internal fun updateClip(previousStart: Long, requestedStart: Long, requestedEnd: Long, duration: Long): Pair<Long, Long> {
    val minimum = minOf(250L, duration)
    return if (requestedStart != previousStart) {
        val start = requestedStart.coerceIn(0L, requestedEnd - minimum)
        start to minOf(requestedEnd, start + MAX_CLIP_MS)
    } else {
        val end = requestedEnd.coerceIn(previousStart + minimum, duration)
        maxOf(previousStart, end - MAX_CLIP_MS) to end
    }
}

private fun MediaPlayer.seekToClip(position: Long) {
    if (Build.VERSION.SDK_INT >= 26) seekTo(position, MediaPlayer.SEEK_CLOSEST)
    else seekTo(position.toInt())
}

private data class TrimVideo(val durationMs: Long, val frames: List<Bitmap?>)

private fun readTrimVideo(context: Context, uri: Uri): TrimVideo {
    val retriever = MediaMetadataRetriever()
    try {
        retriever.setDataSource(context, uri)
        require(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_HAS_VIDEO) == "yes")
        val duration = requireNotNull(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull())
        require(duration in 1..Int.MAX_VALUE.toLong())
        val frames = List(THUMBNAIL_COUNT) { index ->
            val timestamp = duration * 1_000L * index / THUMBNAIL_COUNT
            runCatching {
                if (Build.VERSION.SDK_INT >= 27) {
                    retriever.getScaledFrameAtTime(timestamp, MediaMetadataRetriever.OPTION_CLOSEST_SYNC, 120, 90)
                } else {
                    retriever.getFrameAtTime(timestamp, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)?.let {
                        Bitmap.createScaledBitmap(it, 120, 90, true)
                    }
                }
            }.getOrNull()
        }
        return TrimVideo(duration, frames)
    } finally {
        retriever.release()
    }
}
