package id.madsaz.app.feature.feedback

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.madsaz.app.R
import id.madsaz.app.ui.components.CompactContainer
import id.madsaz.app.ui.theme.BrandBlueSoft
import id.madsaz.app.ui.theme.Ink
import id.madsaz.app.ui.theme.MutedInk

/** Batas komentar yang ditegakkan BE (feedback.js). */
private const val MAX_NOTE_LENGTH = 240
private val FeedbackBorder = Color(0xFFD8E9F2)
private val FeedbackBlue = Color(0xFF2A7598)

@Composable
fun FeedbackScreen(
    state: FeedbackUiState,
    onSubmit: (rating: Int, topic: FeedbackTopic, note: String) -> Unit,
    onSubmitted: () -> Unit,
) {
    var rating by rememberSaveable { mutableIntStateOf(0) }
    var topic by rememberSaveable { mutableStateOf<FeedbackTopic?>(null) }
    var note by rememberSaveable { mutableStateOf("") }
    val submitting = state is FeedbackUiState.Submitting
    val noteLabel = stringResource(R.string.feedback_note_prompt)
    LaunchedEffect(state) { if (state is FeedbackUiState.Sent) onSubmitted() }

    CompactContainer {
        Column(Modifier.fillMaxSize().background(Color.White).imePadding().verticalScroll(rememberScrollState())) {
            Image(
                painter = painterResource(R.drawable.education_hero),
                contentDescription = null,
                modifier = Modifier.fillMaxWidth().height(210.dp),
                contentScale = ContentScale.Crop,
            )
            Column(
                Modifier.fillMaxWidth().offset(y = (-30).dp)
                    .clip(RoundedCornerShape(topStart = 26.dp, topEnd = 26.dp))
                    .background(Color.White)
                    .padding(horizontal = 20.dp)
                    .padding(top = 28.dp, bottom = 24.dp),
            ) {
                Text(stringResource(R.string.feedback_eyebrow), color = FeedbackBlue, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Text(
                    stringResource(R.string.feedback_title),
                    color = Ink,
                    fontFamily = MaterialTheme.typography.headlineMedium.fontFamily,
                    fontSize = 23.sp,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.height(10.dp))
                Text(stringResource(R.string.feedback_subtitle), color = MutedInk, fontSize = 13.sp)
                Spacer(Modifier.height(34.dp))

                Surface(
                    shape = RoundedCornerShape(22.dp),
                    color = Color.White,
                    border = BorderStroke(1.dp, FeedbackBorder),
                ) {
                    Column(Modifier.fillMaxWidth().padding(20.dp)) {
                        Text(stringResource(R.string.feedback_question), color = Ink, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(16.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            listOf(R.string.feedback_rating_1, R.string.feedback_rating_2, R.string.feedback_rating_3).forEachIndexed { index, labelRes ->
                                FeedbackChoice(
                                    label = stringResource(labelRes),
                                    selected = rating == index + 1,
                                    onClick = { rating = index + 1 },
                                    modifier = Modifier.weight(1f),
                                )
                            }
                        }
                    }
                }
                Spacer(Modifier.height(32.dp))
                Text(
                    stringResource(R.string.feedback_improve_title),
                    color = Ink,
                    fontFamily = MaterialTheme.typography.headlineMedium.fontFamily,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    FeedbackTopic.entries.forEach { option ->
                        FeedbackChoice(
                            label = stringResource(option.labelRes),
                            selected = topic == option,
                            onClick = { topic = option },
                            modifier = Modifier.weight(1f),
                            filled = true,
                        )
                    }
                }
                Spacer(Modifier.height(14.dp))
                Surface(shape = RoundedCornerShape(22.dp), color = BrandBlueSoft) {
                    Column(Modifier.fillMaxWidth().padding(20.dp)) {
                        Text(noteLabel, color = Ink, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(12.dp))
                        BasicTextField(
                            value = note,
                            onValueChange = { note = it.take(MAX_NOTE_LENGTH) },
                            modifier = Modifier.fillMaxWidth().heightIn(min = 76.dp)
                                .semantics { contentDescription = noteLabel },
                            textStyle = TextStyle(color = Ink, fontSize = 13.sp, lineHeight = 18.sp),
                            maxLines = 4,
                            decorationBox = { field ->
                                Box(contentAlignment = Alignment.TopStart) {
                                    if (note.isEmpty()) Text(stringResource(R.string.feedback_note_hint), style = TextStyle(color = MutedInk, fontSize = 13.sp, lineHeight = 18.sp))
                                    field()
                                }
                            },
                        )
                        Text(stringResource(R.string.feedback_note_counter, note.length, MAX_NOTE_LENGTH), modifier = Modifier.fillMaxWidth(), color = MutedInk, fontSize = 10.sp, textAlign = TextAlign.End)
                    }
                }
                Spacer(Modifier.height(28.dp))
                if (state is FeedbackUiState.Error) {
                    Text(stringResource(state.messageRes), color = MaterialTheme.colorScheme.error)
                    Spacer(Modifier.height(10.dp))
                }
                Button(
                    onClick = { topic?.let { onSubmit(rating, it, note) } },
                    enabled = !submitting && rating > 0 && topic != null && note.isNotBlank(),
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(26.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = FeedbackBlue),
                ) {
                    Text(stringResource(if (submitting) R.string.feedback_sending else R.string.feedback_send), fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun FeedbackChoice(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    filled: Boolean = false,
) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(44.dp).semantics { this.selected = selected },
        shape = RoundedCornerShape(18.dp),
        color = if (selected) FeedbackBlue else if (filled) BrandBlueSoft else Color.White,
        border = if (selected || filled) null else BorderStroke(1.dp, FeedbackBorder),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(label, color = if (selected) Color.White else FeedbackBlue, fontSize = 11.sp, textAlign = TextAlign.Center)
        }
    }
}
