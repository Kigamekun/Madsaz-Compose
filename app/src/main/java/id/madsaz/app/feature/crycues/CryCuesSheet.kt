package id.madsaz.app.feature.crycues

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.madsaz.app.R
import id.madsaz.app.ui.theme.BrandBlue
import id.madsaz.app.ui.theme.Ink
import id.madsaz.app.ui.theme.MutedInk

private val CryCuesSurface = Color(0xFFF3F8FC)
private val MetricBorder = Color(0xFFD8E9F2)

private data class CryCueDetail(
    val title: String,
    val meaning: String,
    val description: String,
    val icon: Int,
    val iconTint: Color,
    val iconBackground: Color,
    val metricMeaning: String,
    val metricDetail: String,
    val features: List<String>,
    val actions: List<String>,
)

private val cryCueDetails = listOf(
    CryCueDetail("Neh", "Lapar", "Tangisan ini biasanya menandakan bayi merasa lapar dan ingin menyusu.", R.drawable.phosphor_bowl_food, Color(0xFFE38A73), Color(0xFFFFF0E8), "Hunger", "Needs milk", listOf("Suara tangisan seperti ‘neh neh neh’", "Biasanya berulang dan semakin kuat", "Mencari puting atau membuka mulut"), listOf("Segera berikan ASI atau susu formula", "Pastikan posisi menyusu nyaman", "Perhatikan tanda kenyang pada bayi")),
    CryCueDetail("Owh", "Mengantuk", "Suara ini bisa menjadi tanda bayi mulai lelah dan membutuhkan waktu untuk beristirahat.", R.drawable.phosphor_moon_stars, Color(0xFF7567D8), Color(0xFFEEEAFE), "Sleepy", "Time to rest", listOf("Suara seperti ‘owh’ dengan nada panjang", "Menguap atau menggosok mata", "Gerakan bayi mulai melambat"), listOf("Pindahkan bayi ke tempat yang tenang", "Redupkan cahaya dan kurangi suara", "Bantu bayi tidur dengan rutinitas lembut")),
    CryCueDetail("Eh", "Perlu bersendawa", "Bayi mungkin sedang memberi tahu bahwa ada udara yang perlu dikeluarkan setelah menyusu.", R.drawable.phosphor_baby, Color(0xFF4B9EB5), Color(0xFFE7F5FA), "Needs a burp", "After feeding", listOf("Suara pendek seperti ‘eh’", "Muncul setelah bayi menyusu", "Bayi terlihat menggeliat atau tidak nyaman"), listOf("Tegakkan tubuh bayi dengan lembut", "Usap atau tepuk punggung perlahan", "Berhenti jika bayi sudah terlihat nyaman")),
    CryCueDetail("Heh", "Tidak nyaman", "Bayi mungkin sedang merasa kurang nyaman dan membutuhkan pemeriksaan sederhana.", R.drawable.phosphor_heartbeat, Color(0xFFDD717D), Color(0xFFFFE8EC), "Uncomfortable", "Check comfort", listOf("Suara seperti ‘heh’ dengan jeda pendek", "Bayi banyak bergerak atau merengek", "Ekspresi wajah tampak tidak nyaman"), listOf("Periksa popok dan pakaian bayi", "Pastikan suhu ruangan nyaman", "Gendong dan tenangkan bayi perlahan")),
    CryCueDetail("Eairh", "Perut kembung", "Suara ini bisa menandakan bayi merasa tidak nyaman karena gas di perut.", R.drawable.phosphor_wind, Color(0xFF56A986), Color(0xFFE7F5EE), "Lower gas", "Tummy discomfort", listOf("Suara seperti ‘eairh’ dengan tekanan", "Bayi menekuk kaki ke arah perut", "Perut terasa penuh atau bayi sulit tenang"), listOf("Bantu bayi bersendawa", "Gerakkan kaki bayi seperti mengayuh sepeda", "Pijat perut dengan sangat lembut")),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CryCuesSheet(onDismiss: () -> Unit) {
    var selectedCue by remember { mutableStateOf<CryCueDetail?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = CryCuesSurface,
        dragHandle = {
            Box(
                Modifier.padding(top = 14.dp).width(48.dp).height(5.dp)
                    .background(Color(0xFFCAD5E1), RoundedCornerShape(8.dp)),
            )
        },
    ) {
        val cue = selectedCue
        if (cue != null) {
            CryCueDetailSheet(cue, onClose = onDismiss)
        } else {
            Column(Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(top = 12.dp, bottom = 24.dp)) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "Cry Cues",
                        modifier = Modifier.weight(1f),
                        color = Ink,
                        fontFamily = MaterialTheme.typography.headlineMedium.fontFamily,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(42.dp)) {
                        Surface(shape = CircleShape, color = Color(0xFFE6F0FC)) {
                            Icon(Icons.Rounded.Close, "Close cry cues", tint = Color(0xFF405B74), modifier = Modifier.padding(10.dp))
                        }
                    }
                }
                Spacer(Modifier.height(20.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    CueMetric(Modifier.weight(1f), cryCueDetails[0], 165, 108, false) { selectedCue = cryCueDetails[0] }
                    CueMetric(Modifier.weight(1f), cryCueDetails[1], 165, 108, false) { selectedCue = cryCueDetails[1] }
                }
                Spacer(Modifier.height(14.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    CueMetric(Modifier.weight(1f), cryCueDetails[2], 165, 108, false) { selectedCue = cryCueDetails[2] }
                    CueMetric(Modifier.weight(1f), cryCueDetails[3], 165, 108, false) { selectedCue = cryCueDetails[3] }
                }
                Spacer(Modifier.height(14.dp))
                CueMetric(Modifier.fillMaxWidth(), cryCueDetails[4], 342, 84, true) { selectedCue = cryCueDetails[4] }
            }
        }
    }
}

@Composable
private fun CueMetric(modifier: Modifier, cue: CryCueDetail, width: Int, height: Int, compact: Boolean, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    Surface(
        onClick = onClick,
        modifier = modifier.width(width.dp).height(height.dp),
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(if (isPressed) 2.dp else 1.dp, if (isPressed) BrandBlue else MetricBorder),
        interactionSource = interactionSource,
    ) {
        Box(Modifier.fillMaxSize()) {
            Surface(Modifier.offset(x = 14.dp, y = if (compact) 24.dp else 14.dp).size(36.dp), CircleShape, cue.iconBackground) {
                Icon(painterResource(cue.icon), null, tint = cue.iconTint, modifier = Modifier.padding(6.dp))
            }
            Text(cue.title, Modifier.offset(x = if (compact) 58.dp else 60.dp, y = if (compact) 10.dp else 22.dp), Ink, fontFamily = MaterialTheme.typography.headlineMedium.fontFamily, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
            Text(cue.metricMeaning, if (compact) Modifier.offset(x = 58.dp, y = 36.dp) else Modifier.align(Alignment.TopCenter).offset(y = 57.dp), Ink, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            Text(cue.metricDetail, if (compact) Modifier.offset(x = 58.dp, y = 60.dp) else Modifier.align(Alignment.TopCenter).offset(y = 82.dp), MutedInk, fontSize = 10.sp)
        }
    }
}

@Composable
private fun CryCueDetailSheet(cue: CryCueDetail, onClose: () -> Unit) {
    Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 24.dp, vertical = 20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Surface(Modifier.size(58.dp), CircleShape, cue.iconBackground) { Icon(painterResource(cue.icon), null, tint = cue.iconTint, modifier = Modifier.padding(13.dp)) }
            Column(Modifier.padding(start = 14.dp).weight(1f)) {
                Text(cue.title, color = Ink, fontSize = 27.sp, fontWeight = FontWeight.Bold)
                Text(cue.meaning, color = MutedInk, fontSize = 17.sp)
            }
            IconButton(onClick = onClose) {
                Surface(shape = CircleShape, color = Color(0xFFEAF2FB)) { Icon(Icons.Rounded.Close, "Close", tint = Color(0xFF3F5877)) }
            }
        }
        Text(cue.description, Modifier.padding(top = 14.dp), MutedInk, fontSize = 13.sp, lineHeight = 18.sp)
        DetailSection("Ciri-ciri", cue.features, R.drawable.phosphor_baby, Color(0xFFE5F4FB), Color(0xFF4B9EB5))
        DetailSection("Yang bisa dilakukan", cue.actions, R.drawable.phosphor_heartbeat, Color(0xFFFFF0D2), Color(0xFFE6A02C))
        Spacer(Modifier.height(16.dp))
        Button(onClick = onClose, modifier = Modifier.fillMaxWidth().height(54.dp).padding(bottom = 4.dp), shape = RoundedCornerShape(28.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4EA0C3))) {
            Text("Mengerti", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun DetailSection(title: String, items: List<String>, icon: Int, background: Color, tint: Color) {
    Surface(Modifier.fillMaxWidth().padding(top = 16.dp), RoundedCornerShape(18.dp), background.copy(alpha = 0.34f), border = androidx.compose.foundation.BorderStroke(1.dp, background)) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.Top) {
            Surface(Modifier.size(40.dp), CircleShape, background) { Icon(painterResource(icon), null, tint = tint, modifier = Modifier.padding(9.dp)) }
            Column(Modifier.padding(start = 12.dp)) {
                Text(title, color = Ink, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                items.forEach { Text("• $it", color = MutedInk, fontSize = 11.sp, lineHeight = 16.sp) }
            }
        }
    }
}
