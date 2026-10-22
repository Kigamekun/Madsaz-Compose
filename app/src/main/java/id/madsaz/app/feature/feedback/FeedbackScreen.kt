package id.madsaz.app.feature.feedback

import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import id.madsaz.app.ui.components.MadsazTextField
import androidx.compose.ui.platform.testTag
import id.madsaz.app.ui.theme.BrandBlueDark
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.madsaz.app.R
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.Icons
import id.madsaz.app.ui.components.CompactContainer
import id.madsaz.app.ui.theme.BrandBlueSoft
import id.madsaz.app.ui.theme.Ink
import id.madsaz.app.ui.theme.MutedInk

/** Batas komentar yang ditegakkan BE (feedback.js). */
private const val MAX_NOTE_LENGTH = 240
private val FeedbackBorder = Color(0xFFD8E9F2)
private val FeedbackBlue = BrandBlueDark

@Composable
fun FeedbackScreen(
    state: FeedbackUiState,
    onSubmit: (rating: Int, topic: FeedbackTopic, note: String) -> Unit,
    onSubmitted: () -> Unit,
    onBack: () -> Unit,
) {
    var rating by rememberSaveable { mutableIntStateOf(0) }
    var topic by rememberSaveable { mutableStateOf<FeedbackTopic?>(null) }
    var note by rememberSaveable { mutableStateOf("") }
    val submitting = state is FeedbackUiState.Submitting
    val noteLabel = stringResource(R.string.feedback_note_prompt)
    LaunchedEffect(state) { if (state is FeedbackUiState.Sent) onSubmitted() }

    CompactContainer {
        Column(Modifier.fillMaxSize().background(Color.White).windowInsetsPadding(WindowInsets.safeDrawing).imePadding().verticalScroll(rememberScrollState())) {
            // Masukan bersifat opsional: tombol tutup yang terlihat, bukan hanya tombol kembali sistem.
            IconButton(onClick = onBack, modifier = Modifier.padding(start = 8.dp, top = 4.dp)) {
                Icon(Icons.Rounded.Close, stringResource(R.string.common_close), tint = Ink)
            }
            Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(bottom = 24.dp)) {
                Text(stringResource(R.string.feedback_eyebrow), color = FeedbackBlue, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Text(
                    stringResource(R.string.feedback_title),
                    color = Ink,
                    fontFamily = MaterialTheme.typography.headlineMedium.fontFamily,
                    fontSize = 23.sp,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.height(10.dp))
                Text(stringResource(R.string.feedback_subtitle), color = MutedInk, fontSize = 14.sp)
                Spacer(Modifier.height(24.dp))

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White,
                    border = BorderStroke(1.dp, FeedbackBorder),
                ) {
                    Column(Modifier.fillMaxWidth().padding(20.dp)) {
                        Text(stringResource(R.string.feedback_question), color = Ink, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(16.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(R.string.feedback_rating_1, R.string.feedback_rating_2, R.string.feedback_rating_3).forEachIndexed { index, labelRes ->
                                FeedbackChoice(
                                    label = stringResource(labelRes),
                                    selected = rating == index + 1,
                                    onClick = { rating = index + 1 },
                                    modifier = Modifier.fillMaxWidth(),
                                )
                            }
                        }
                    }
                }
                Spacer(Modifier.height(24.dp))
                Text(
                    stringResource(R.string.feedback_improve_title),
                    color = Ink,
                    fontFamily = MaterialTheme.typography.headlineMedium.fontFamily,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.height(10.dp))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    FeedbackTopic.entries.forEach { option ->
                        FeedbackChoice(
                            label = stringResource(option.labelRes),
                            selected = topic == option,
                            onClick = { topic = option },
                            modifier = Modifier.fillMaxWidth(),
                            filled = true,
                        )
                    }
                }
                Spacer(Modifier.height(14.dp))
                MadsazTextField(
                    value = note, onValueChange = { note = it.take(MAX_NOTE_LENGTH) }, label = noteLabel,
                    modifier = Modifier.testTag("feedback_note"), enabled = !submitting, singleLine = false, minLines = 3, maxLines = 5,
                )
                Text(stringResource(R.string.feedback_note_hint), style = MaterialTheme.typography.bodySmall, color = MutedInk, modifier = Modifier.padding(top = 6.dp))
                Text(stringResource(R.string.feedback_note_counter, note.length, MAX_NOTE_LENGTH), modifier = Modifier.fillMaxWidth(), color = MutedInk, style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.End)
                Spacer(Modifier.height(24.dp))
                val complete = rating > 0 && topic != null && note.isNotBlank()
                // BE mewajibkan penilaian, topik, dan catatan; beri tahu alasannya selama tombol kirim belum aktif.
                if (!complete) {
                    Text(stringResource(R.string.feedback_requirements), color = MutedInk, style = MaterialTheme.typography.bodySmall)
                    Spacer(Modifier.height(10.dp))
                }
                if (state is FeedbackUiState.Error) {
                    Text(stringResource(state.messageRes), color = MaterialTheme.colorScheme.error)
                    Spacer(Modifier.height(10.dp))
                }
                Button(
                    onClick = { topic?.let { onSubmit(rating, it, note) } },
                    enabled = !submitting && complete,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
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
        modifier = modifier.heightIn(min = 48.dp).semantics { this.selected = selected },
        shape = RoundedCornerShape(12.dp),
        color = if (selected) FeedbackBlue else if (filled) BrandBlueSoft else Color.White,
        border = if (selected || filled) null else BorderStroke(1.dp, FeedbackBorder),
    ) {
        Box(Modifier.padding(horizontal = 8.dp, vertical = 12.dp), contentAlignment = Alignment.Center) {
            Text(label, color = if (selected) Color.White else FeedbackBlue, fontSize = 14.sp, textAlign = TextAlign.Center)
        }
    }
}
