package id.madsaz.app.feature.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowForward
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.madsaz.app.R
import id.madsaz.app.model.CryCategory
import id.madsaz.app.model.RecordingItem
import id.madsaz.app.ui.components.CompactContainer
import id.madsaz.app.ui.theme.AppBackground
import id.madsaz.app.ui.theme.Border
import id.madsaz.app.ui.theme.BrandBlue
import id.madsaz.app.ui.theme.BrandBlueDark
import id.madsaz.app.ui.theme.BrandBlueSoft
import id.madsaz.app.ui.theme.Ink
import id.madsaz.app.ui.theme.MutedInk

private val CueMeaning = mapOf(
    CryCategory.NEH to "Hunger",
    CryCategory.OWH to "Sleepy",
    CryCategory.EH to "Needs to burp",
    CryCategory.EAIRH to "Tummy discomfort",
    CryCategory.HEH to "Uncomfortable",
)

@Composable
fun HomeScreen(
    latestRecording: RecordingItem?,
    recentRecordings: List<RecordingItem> = emptyList(),
    historyLoading: Boolean,
    historyError: Boolean,
    onRecord: () -> Unit,
    onGuide: () -> Unit,
    onProfile: () -> Unit,
    onHistory: () -> Unit,
    onOpenRecording: (RecordingItem) -> Unit,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
) {
    CompactContainer(modifier = modifier.padding(contentPadding)) {
        Box(Modifier.fillMaxSize().background(AppBackground)) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(0.dp),
            ) {
                item {
                    HomeHero(
                        latestRecording = latestRecording,
                        onRecord = onRecord,
                        onGuide = onGuide,
                        onProfile = onProfile,
                    )
                }
                item {
                    RecentRecordings(
                        recordings = recentRecordings.ifEmpty { listOfNotNull(latestRecording) },
                        loading = historyLoading,
                        error = historyError,
                        onSeeAll = onHistory,
                        onOpenRecording = onOpenRecording,
                    )
                }
                item { Spacer(Modifier.height(12.dp)) }
            }
        }
    }
}

@Composable
private fun HomeHero(
    latestRecording: RecordingItem?,
    onRecord: () -> Unit,
    onGuide: () -> Unit,
    onProfile: () -> Unit,
) {
    Box(Modifier.fillMaxWidth().height(413.dp)) {
        Image(
            painter = painterResource(R.drawable.madsaz_home_photo),
            contentDescription = stringResource(R.string.home_hero_description),
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds,
        )

        // The fade starts over the artwork and finishes on the same surface as the feed.
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .height(188.dp)
                .background(
                    Brush.verticalGradient(
                        colorStops = arrayOf(
                            0f to Color.Transparent,
                            0.45f to AppBackground.copy(alpha = 0.08f),
                            0.67f to AppBackground,
                            1f to AppBackground,
                        ),
                    ),
                ),
        )

        Text(
            text = "9:41",
            modifier = Modifier.align(Alignment.TopStart).offset(x = 20.dp, y = 17.dp),
            color = Ink,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
        )
        Column(
            modifier = Modifier.align(Alignment.TopStart).offset(x = 20.dp, y = 68.dp),
            verticalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            Text(
                text = stringResource(R.string.home_greeting),
                color = Ink,
                fontFamily = MaterialTheme.typography.headlineMedium.fontFamily,
                fontSize = 21.sp,
                fontWeight = FontWeight.SemiBold,
                lineHeight = 24.sp,
            )
            Text(
                text = stringResource(R.string.home_greeting_subtitle),
                color = MutedInk,
                style = MaterialTheme.typography.bodySmall,
                fontSize = 13.sp,
            )
        }
        Surface(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = (-23).dp, y = 68.dp)
                .size(34.dp)
                .clickable(onClick = onProfile),
            shape = CircleShape,
            color = Color.White,
            border = androidx.compose.foundation.BorderStroke(1.dp, Border),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text("P", color = BrandBlue, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }

        Surface(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = 288.dp)
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .height(125.dp)
                .clickable(onClick = onGuide),
            shape = RoundedCornerShape(23.dp),
            color = BrandBlueDark,
        ) {
            Box(Modifier.fillMaxSize()) {
                Text(
                    text = stringResource(R.string.home_latest_cry),
                    modifier = Modifier.offset(x = 18.dp, y = 14.dp),
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = latestRecording?.category?.code ?: "—",
                    modifier = Modifier.offset(x = 18.dp, y = 39.dp),
                    color = Color(0xFFE8D7FF),
                    fontFamily = MaterialTheme.typography.headlineLarge.fontFamily,
                    fontSize = 34.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 40.sp,
                )
                Text(
                    text = latestRecording?.category?.let { "${CueMeaning.getValue(it)} · ${confidenceFor(it)} confidence" }
                        ?: stringResource(R.string.home_latest_empty),
                    modifier = Modifier.offset(x = 18.dp, y = 89.dp),
                    color = Color.White,
                    fontSize = 12.sp,
                )
                Text(
                    text = stringResource(R.string.home_just_now),
                    modifier = Modifier.align(Alignment.TopEnd).offset(x = (-18).dp, y = 14.dp),
                    color = Color(0xFFF0EEFF),
                    fontSize = 11.sp,
                )
                Row(
                    modifier = Modifier.align(Alignment.BottomEnd).offset(x = (-36).dp, y = (-16).dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.Bottom,
                ) {
                    listOf(16, 26, 33, 42, 30, 50, 64).forEachIndexed { index, height ->
                        Box(
                            Modifier
                                .width(8.dp)
                                .height(height.dp)
                                .background(
                                    if (index == 6) Color(0xFFE6E1FF) else Color(0xC7E6E1FF),
                                    RoundedCornerShape(4.dp),
                                ),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RecentRecordings(
    recordings: List<RecordingItem>,
    loading: Boolean,
    error: Boolean,
    onSeeAll: () -> Unit,
    onOpenRecording: (RecordingItem) -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(452.dp)
            .background(AppBackground),
    ) {
        Text(
            text = stringResource(R.string.home_recent_title),
            modifier = Modifier.offset(x = 20.dp, y = 14.dp),
            color = Ink,
            fontFamily = MaterialTheme.typography.titleLarge.fontFamily,
            fontSize = 19.sp,
            fontWeight = FontWeight.SemiBold,
        )
        Row(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = (-24).dp, y = 14.dp)
                .clickable(onClick = onSeeAll),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.home_see_all),
                color = BrandBlue,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
            )
            Text("›", color = BrandBlue, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }

        when {
            loading -> CircularProgressIndicator(
                modifier = Modifier.align(Alignment.TopCenter).offset(y = 90.dp).size(24.dp),
                color = BrandBlue,
                strokeWidth = 2.dp,
            )
            error -> Text(
                text = stringResource(R.string.home_recent_error_title),
                modifier = Modifier.align(Alignment.TopCenter).offset(y = 84.dp),
                color = MutedInk,
                style = MaterialTheme.typography.bodySmall,
            )
            recordings.isEmpty() -> Text(
                text = stringResource(R.string.home_recent_empty_title),
                modifier = Modifier.align(Alignment.TopCenter).offset(y = 84.dp),
                color = MutedInk,
                style = MaterialTheme.typography.bodySmall,
            )
            else -> {
                Box(
                    modifier = Modifier
                        .offset(x = 20.dp, y = 92.dp)
                        .width(2.dp)
                        .height(235.dp)
                        .background(Color(0xFFD5E2F4)),
                )
                recordings.take(3).forEachIndexed { index, recording ->
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .fillMaxWidth()
                            .padding(start = 34.dp, end = 14.dp)
                            .offset(y = (46 + index * 114).dp),
                    ) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(104.dp)
                                .clickable { onOpenRecording(recording) },
                            shape = RoundedCornerShape(22.dp),
                            color = Color.White,
                            border = androidx.compose.foundation.BorderStroke(1.dp, Border),
                        ) {
                            ActivityContent(recording)
                        }
                    }
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .offset(x = 15.dp, y = (93 + index * 114).dp)
                            .size(12.dp)
                            .background(BrandBlue, CircleShape),
                    )
                }
            }
        }
    }
}

@Composable
private fun ActivityContent(recording: RecordingItem) {
    Row(
        modifier = Modifier.padding(start = 14.dp, top = 17.dp, end = 12.dp, bottom = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top,
    ) {
        val imageRes = when (recording.category) {
            CryCategory.NEH -> R.drawable.madsaz_activity_bottle
            CryCategory.OWH -> R.drawable.madsaz_activity_sleep
            CryCategory.HEH -> R.drawable.madsaz_activity_clock
            else -> null
        }
        if (imageRes != null) {
            Image(
                painter = painterResource(imageRes),
                contentDescription = null,
                modifier = Modifier.size(54.dp),
                contentScale = ContentScale.Fit,
            )
        } else {
            Surface(
                modifier = Modifier.size(54.dp),
                shape = CircleShape,
                color = BrandBlueSoft,
            ) {
                Icon(
                    imageVector = Icons.Rounded.GraphicEq,
                    contentDescription = null,
                    modifier = Modifier.padding(15.dp),
                    tint = BrandBlue,
                )
            }
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = "${recording.category.code} · ${CueMeaning.getValue(recording.category)}",
                color = Ink,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
            )
            Text(
                text = "Recorded at ${formatHomeRecordedAt(recording.recordedAtEpochMillis)}",
                color = MutedInk,
                fontSize = 12.sp,
                maxLines = 1,
            )
            Surface(shape = CircleShape, color = BrandBlueSoft) {
                Text(
                    text = "${confidenceFor(recording.category)}% confidence",
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                    color = BrandBlue,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
        Icon(
            imageVector = Icons.Rounded.ArrowForward,
            contentDescription = null,
            tint = MutedInk,
            modifier = Modifier.padding(top = 18.dp).size(16.dp),
        )
    }
}

// ponytail: design sample confidence until the real classifier exposes confidence in RecordingItem.
private fun confidenceFor(category: CryCategory): Int = when (category) {
    CryCategory.NEH -> 87
    CryCategory.OWH -> 72
    CryCategory.EH -> 68
    CryCategory.EAIRH -> 64
    CryCategory.HEH -> 61
}

private fun formatHomeRecordedAt(epochMillis: Long): String =
    java.text.SimpleDateFormat("h:mm a", java.util.Locale.ENGLISH).format(java.util.Date(epochMillis))
