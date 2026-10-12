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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.madsaz.app.R
import id.madsaz.app.data.remote.RecordingApi
import id.madsaz.app.ui.components.CompactContainer
import id.madsaz.app.ui.theme.BrandBlueSoft
import id.madsaz.app.ui.theme.Ink
import id.madsaz.app.ui.theme.MutedInk
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private val FeedbackBorder = Color(0xFFD8E9F2)
private val FeedbackBlue = Color(0xFF2A7598)

@Composable
fun FeedbackScreen(
    recordingId: String,
    accessToken: String,
    onSubmitted: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    val api = remember { RecordingApi() }
    var rating by rememberSaveable { mutableIntStateOf(0) }
    var improvement by rememberSaveable { mutableStateOf("") }
    var note by rememberSaveable { mutableStateOf("") }
    var submitting by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    CompactContainer {
        Column(Modifier.fillMaxSize().background(Color.White).verticalScroll(rememberScrollState())) {
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
                Text("Help shape Madsaz", color = FeedbackBlue, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Text(
                    "Share your thoughts",
                    color = Ink,
                    fontFamily = MaterialTheme.typography.headlineMedium.fontFamily,
                    fontSize = 23.sp,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.height(10.dp))
                Text("A few taps help us make listening more useful.", color = MutedInk, fontSize = 13.sp)
                Spacer(Modifier.height(34.dp))

                Surface(
                    shape = RoundedCornerShape(22.dp),
                    color = Color.White,
                    border = BorderStroke(1.dp, FeedbackBorder),
                ) {
                    Column(Modifier.fillMaxWidth().padding(20.dp)) {
                        Text("How is Madsaz feeling today?", color = Ink, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(16.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            listOf("Not great", "Okay", "Love it").forEachIndexed { index, label ->
                                FeedbackChoice(
                                    label = label,
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
                    "What can we improve?",
                    color = Ink,
                    fontFamily = MaterialTheme.typography.headlineMedium.fontFamily,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    listOf("Accuracy", "Usability", "Something else").forEach { label ->
                        FeedbackChoice(
                            label = label,
                            selected = improvement == label,
                            onClick = { improvement = label },
                            modifier = Modifier.weight(1f),
                            filled = true,
                        )
                    }
                }
                Spacer(Modifier.height(14.dp))
                Surface(shape = RoundedCornerShape(22.dp), color = BrandBlueSoft) {
                    Column(Modifier.fillMaxWidth().padding(20.dp)) {
                        Text("Tell us what would make Madsaz better", color = Ink, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(12.dp))
                        BasicTextField(
                            value = note,
                            onValueChange = { note = it.take(240) },
                            modifier = Modifier.fillMaxWidth().heightIn(min = 76.dp)
                                .semantics { contentDescription = "Tell us what would make Madsaz better" },
                            textStyle = TextStyle(color = Ink, fontSize = 13.sp, lineHeight = 18.sp),
                            maxLines = 4,
                            decorationBox = { field ->
                                Box(contentAlignment = Alignment.TopStart) {
                                    if (note.isEmpty()) Text("Your note goes here...", style = TextStyle(color = MutedInk, fontSize = 13.sp, lineHeight = 18.sp))
                                    field()
                                }
                            },
                        )
                        Text("${note.length} / 240", modifier = Modifier.fillMaxWidth(), color = MutedInk, fontSize = 10.sp, textAlign = TextAlign.End)
                    }
                }
                Spacer(Modifier.height(28.dp))
                if (error != null) {
                    Text(error.orEmpty(), color = MaterialTheme.colorScheme.error)
                    Spacer(Modifier.height(10.dp))
                }
                Button(
                    onClick = {
                        submitting = true
                        error = null
                        scope.launch {
                            try {
                                withContext(Dispatchers.IO) {
                                    api.sendFeedback(accessToken, recordingId, rating, improvement, note.trim())
                                }
                                onSubmitted()
                            } catch (failure: CancellationException) {
                                throw failure
                            } catch (failure: Exception) {
                                error = failure.message ?: "Feedback gagal dikirim. Coba lagi."
                            } finally {
                                submitting = false
                            }
                        }
                    },
                    enabled = !submitting && rating > 0 && improvement.isNotEmpty() && note.isNotBlank(),
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(26.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = FeedbackBlue),
                ) {
                    Text(if (submitting) "Sending..." else "Send feedback", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
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
