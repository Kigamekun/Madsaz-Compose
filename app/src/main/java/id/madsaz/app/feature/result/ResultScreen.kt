package id.madsaz.app.feature.result

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.madsaz.app.R
import id.madsaz.app.model.CryCategory
import id.madsaz.app.model.RecordingItem
import id.madsaz.app.model.RecordingUiState
import id.madsaz.app.ui.components.CompactContainer
import id.madsaz.app.ui.theme.AppBackground
import id.madsaz.app.ui.theme.BrandBlue
import id.madsaz.app.ui.theme.BrandBlueSoft
import id.madsaz.app.ui.theme.Ink
import id.madsaz.app.ui.theme.MutedInk

private val ResultBorder = Color(0xFFE8EAF4)
private val ResultGreen = Color(0xFFD1F1E3)
private val ResultGreenText = Color(0xFF2E6E54)

@Composable
fun ResultScreen(
    state: RecordingUiState,
    onBack: () -> Unit,
    onRecordAgain: () -> Unit,
    onHistory: () -> Unit,
    modifier: Modifier = Modifier,
) {
    CompactContainer(modifier) {
        when (state) {
            RecordingUiState.Saving -> ProgressContent(
                title = stringResource(R.string.analysis_saving_title),
                body = stringResource(R.string.analysis_saving_body),
            )
            RecordingUiState.Analyzing -> ProgressContent(
                title = stringResource(R.string.analysis_running_title),
                body = stringResource(R.string.analysis_running_body),
            )
            is RecordingUiState.Completed -> CompletedContent(
                recording = state.recording,
                onBack = onBack,
                onHistory = onHistory,
            )
            is RecordingUiState.Error -> ErrorContent(
                message = stringResource(state.messageRes),
                onRetry = onRecordAgain,
            )
            else -> error("ResultScreen hanya menerima state hasil rekaman")
        }
    }
}

@Composable
private fun ProgressContent(title: String, body: String) {
    Box(Modifier.fillMaxSize().background(AppBackground), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.padding(24.dp),
        ) {
            Surface(modifier = Modifier.size(82.dp), shape = CircleShape, color = BrandBlueSoft) {
                Box(contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(modifier = Modifier.size(38.dp), strokeWidth = 3.dp)
                }
            }
            Text(title, style = MaterialTheme.typography.headlineMedium, textAlign = TextAlign.Center)
            Text(body, style = MaterialTheme.typography.bodyMedium, color = MutedInk, textAlign = TextAlign.Center)
        }
    }
}

@Composable
private fun CompletedContent(
    recording: RecordingItem,
    onBack: () -> Unit,
    onHistory: () -> Unit,
) {
    val copy = resultCopy(recording.category)

    Box(Modifier.fillMaxSize().background(Color.White)) {
        Text(
            text = stringResource(R.string.result_status_time),
            modifier = Modifier.offset(x = 20.dp, y = 28.dp),
            color = Ink,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
        )
        Image(
            painter = painterResource(R.drawable.madsaz_result_baby_halo),
            contentDescription = stringResource(R.string.result_baby_description),
            modifier = Modifier.offset(x = 143.dp, y = 112.dp).size(104.dp),
            contentScale = ContentScale.FillBounds,
        )
        Text(
            text = copy.heading,
            modifier = Modifier
                .fillMaxWidth()
                .offset(y = 247.dp)
                .padding(horizontal = 24.dp),
            color = Ink,
            fontFamily = MaterialTheme.typography.headlineMedium.fontFamily,
            fontSize = 23.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
        )
        Surface(
            modifier = Modifier.align(Alignment.TopCenter).offset(y = 285.dp).size(width = 122.dp, height = 24.dp),
            shape = RoundedCornerShape(12.dp),
            color = ResultGreen,
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = stringResource(R.string.result_confidence),
                    color = ResultGreenText,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
        Text(
            text = stringResource(R.string.result_explanation),
            modifier = Modifier
                .fillMaxWidth()
                .offset(y = 327.dp)
                .padding(horizontal = 38.dp),
            color = MutedInk,
            fontSize = 11.sp,
            lineHeight = 15.sp,
            textAlign = TextAlign.Center,
        )
        ResultSuggestionCard(
            modifier = Modifier.offset(x = 24.dp, y = 384.dp),
            icon = R.drawable.madsaz_activity_bottle,
            title = copy.suggestionTitle,
            body = copy.suggestionBody,
            height = 104,
        )
        ResultSuggestionCard(
            modifier = Modifier.offset(x = 24.dp, y = 504.dp),
            icon = R.drawable.madsaz_activity_clock,
            title = copy.careTitle,
            body = copy.careBody,
            helper = copy.careHelper,
            height = 112,
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .height(104.dp)
                .background(Color.White),
        ) {
            Surface(
                modifier = Modifier
                    .offset(x = 24.dp, y = 10.dp)
                    .size(width = 155.dp, height = 48.dp)
                    .clickable(onClick = onBack),
                shape = RoundedCornerShape(24.dp),
                color = Color.White,
                border = BorderStroke(1.dp, Color(0xFFD5E2F4)),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = stringResource(R.string.result_skip),
                        color = BrandBlue,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
            Surface(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = 204.dp, y = 10.dp)
                    .size(width = 155.dp, height = 48.dp)
                    .clickable(onClick = onHistory),
                shape = RoundedCornerShape(24.dp),
                color = BrandBlue,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = stringResource(R.string.result_add_log),
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }
}

@Composable
private fun ResultSuggestionCard(
    modifier: Modifier,
    icon: Int,
    title: String,
    body: String,
    helper: String? = null,
    height: Int,
) {
    Surface(
        modifier = modifier.size(width = 342.dp, height = height.dp),
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        border = BorderStroke(1.dp, ResultBorder),
    ) {
        Box(Modifier.fillMaxSize()) {
            Image(
                painter = painterResource(icon),
                contentDescription = null,
                modifier = Modifier.offset(x = 9.dp, y = if (height == 104) 19.dp else 24.dp).size(65.dp),
                contentScale = ContentScale.Fit,
            )
            Text(
                text = title,
                modifier = Modifier.offset(x = 84.dp, y = 14.dp),
                color = Ink,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = body,
                modifier = Modifier.offset(x = 84.dp, y = 42.dp).width(230.dp),
                color = MutedInk,
                fontSize = 11.sp,
                lineHeight = 14.sp,
            )
            if (helper != null) {
                Text(
                    text = helper,
                    modifier = Modifier.offset(x = 84.dp, y = 80.dp),
                    color = MutedInk,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

@Composable
private fun ErrorContent(message: String, onRetry: () -> Unit) {
    Box(Modifier.fillMaxSize().background(AppBackground).padding(24.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Surface(shape = CircleShape, color = MaterialTheme.colorScheme.errorContainer) {
                Icon(
                    Icons.Rounded.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(18.dp).size(28.dp),
                )
            }
            Text(message, style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
            OutlinedButton(onClick = onRetry) { Text(stringResource(R.string.common_retry)) }
        }
    }
}

private data class ResultCopy(
    val heading: String,
    val suggestionTitle: String,
    val suggestionBody: String,
    val careTitle: String,
    val careBody: String,
    val careHelper: String,
)

private fun resultCopy(category: CryCategory): ResultCopy = when (category) {
    CryCategory.NEH -> ResultCopy(
        heading = "Pandu might be... Hungry",
        suggestionTitle = "Try feeding",
        suggestionBody = "Neh often means baby may be hungry.",
        careTitle = "Look for rooting",
        careBody = "Lip-smacking and rooting can support this result.",
        careHelper = "Observe before feeding.",
    )
    CryCategory.OWH -> ResultCopy(
        heading = "Pandu might be... Sleepy",
        suggestionTitle = "Try settling",
        suggestionBody = "Owh can appear when baby is ready to rest.",
        careTitle = "Watch for sleepy cues",
        careBody = "A calm, dim room can support this result.",
        careHelper = "Keep the routine gentle.",
    )
    CryCategory.EH -> ResultCopy(
        heading = "Pandu might need... A burp",
        suggestionTitle = "Try burping",
        suggestionBody = "Eh can appear after feeding when baby needs a burp.",
        careTitle = "Keep baby upright",
        careBody = "A gentle upright hold can support comfort.",
        careHelper = "Stop if baby seems uncomfortable.",
    )
    CryCategory.EAIRH -> ResultCopy(
        heading = "Pandu might feel... Gassy",
        suggestionTitle = "Try gentle comfort",
        suggestionBody = "Eairh can point to tummy discomfort.",
        careTitle = "Check the tummy",
        careBody = "Slow, gentle movement may help baby settle.",
        careHelper = "Contact a clinician if concerned.",
    )
    CryCategory.HEH -> ResultCopy(
        heading = "Pandu might be... Uncomfortable",
        suggestionTitle = "Check comfort",
        suggestionBody = "Heh can appear when something feels uncomfortable.",
        careTitle = "Look around baby",
        careBody = "Check clothing, diaper, temperature, and position.",
        careHelper = "Make one small change at a time.",
    )
}
