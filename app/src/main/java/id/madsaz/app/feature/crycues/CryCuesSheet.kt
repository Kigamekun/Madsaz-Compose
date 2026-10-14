package id.madsaz.app.feature.crycues

import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalDensity
import id.madsaz.app.ui.theme.BrandBlueDark
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.madsaz.app.R
import id.madsaz.app.ui.components.CryCueIcon
import id.madsaz.app.ui.theme.BrandBlue
import id.madsaz.app.ui.theme.Ink
import id.madsaz.app.ui.theme.MutedInk

private val MetricBorder = Color(0xFFD8E9F2)

/** Kode (Neh, Owh, ...) adalah nama pola dari model dan tidak diterjemahkan; teks lainnya dari resource. */
private data class CryCueDetail(
    val code: String,
    @StringRes val meaning: Int,
    @StringRes val description: Int,
    @StringRes val metricMeaning: Int,
    @StringRes val metricDetail: Int,
    val features: List<Int>,
    val actions: List<Int>,
)

private val cryCueDetails = listOf(
    CryCueDetail(
        "Neh", R.string.cue_neh_meaning, R.string.cue_neh_description,
        R.string.cue_neh_metric_meaning, R.string.cue_neh_metric_detail,
        listOf(R.string.cue_neh_feature_1, R.string.cue_neh_feature_2, R.string.cue_neh_feature_3),
        listOf(R.string.cue_neh_action_1, R.string.cue_neh_action_2, R.string.cue_neh_action_3),
    ),
    CryCueDetail(
        "Owh", R.string.cue_owh_meaning, R.string.cue_owh_description,
        R.string.cue_owh_metric_meaning, R.string.cue_owh_metric_detail,
        listOf(R.string.cue_owh_feature_1, R.string.cue_owh_feature_2, R.string.cue_owh_feature_3),
        listOf(R.string.cue_owh_action_1, R.string.cue_owh_action_2, R.string.cue_owh_action_3),
    ),
    CryCueDetail(
        "Eh", R.string.cue_eh_meaning, R.string.cue_eh_description,
        R.string.cue_eh_metric_meaning, R.string.cue_eh_metric_detail,
        listOf(R.string.cue_eh_feature_1, R.string.cue_eh_feature_2, R.string.cue_eh_feature_3),
        listOf(R.string.cue_eh_action_1, R.string.cue_eh_action_2, R.string.cue_eh_action_3),
    ),
    CryCueDetail(
        "Heh", R.string.cue_heh_meaning, R.string.cue_heh_description,
        R.string.cue_heh_metric_meaning, R.string.cue_heh_metric_detail,
        listOf(R.string.cue_heh_feature_1, R.string.cue_heh_feature_2, R.string.cue_heh_feature_3),
        listOf(R.string.cue_heh_action_1, R.string.cue_heh_action_2, R.string.cue_heh_action_3),
    ),
    CryCueDetail(
        "Eairh", R.string.cue_eairh_meaning, R.string.cue_eairh_description,
        R.string.cue_eairh_metric_meaning, R.string.cue_eairh_metric_detail,
        listOf(R.string.cue_eairh_feature_1, R.string.cue_eairh_feature_2, R.string.cue_eairh_feature_3),
        listOf(R.string.cue_eairh_action_1, R.string.cue_eairh_action_2, R.string.cue_eairh_action_3),
    ),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CryCuesSheet(onDismiss: () -> Unit) {
    var selectedCue by remember { mutableStateOf<CryCueDetail?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White,
        dragHandle = null,
    ) {
        val cue = selectedCue
        if (cue != null) {
            CryCueDetailSheet(cue, onClose = onDismiss)
        } else {
            Column(
                Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp).padding(top = 16.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Row(Modifier.fillMaxWidth().padding(bottom = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        stringResource(R.string.home_cry_cues_title),
                        modifier = Modifier.weight(1f),
                        color = Ink,
                        fontFamily = MaterialTheme.typography.headlineMedium.fontFamily,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                    CloseButton(onDismiss, stringResource(R.string.cry_cues_close))
                }
                if (LocalDensity.current.fontScale > 1.3f) {
                    cryCueDetails.forEach { cue -> CueCard(cue, Modifier.fillMaxWidth(), wide = true) { selectedCue = cue } }
                } else {
                cryCueDetails.take(4).chunked(2).forEach { pair ->
                    // IntrinsicSize.Max: dua kartu sebaris selalu sama tinggi walau teksnya beda panjang.
                    Row(Modifier.fillMaxWidth().height(IntrinsicSize.Max), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        pair.forEach { cue -> CueCard(cue, Modifier.weight(1f).fillMaxHeight()) { selectedCue = cue } }
                    }
                }
                CueCard(cryCueDetails[4], Modifier.fillMaxWidth(), wide = true) { selectedCue = cryCueDetails[4] }
                }
            }
        }
    }
}

@Composable
private fun CloseButton(onClick: () -> Unit, description: String) {
    IconButton(onClick = onClick, modifier = Modifier.size(48.dp)) {
        Surface(shape = CircleShape, color = Color(0xFFE6F0FC)) {
            Icon(Icons.Rounded.Close, description, tint = Color(0xFF405B74), modifier = Modifier.padding(8.dp).size(20.dp))
        }
    }
}

@Composable
private fun CueIcon(cue: CryCueDetail, size: Int) = CryCueIcon(cue.code, Modifier.size(size.dp))

@Composable
private fun CueCard(cue: CryCueDetail, modifier: Modifier, wide: Boolean = false, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        border = BorderStroke(if (isPressed) 2.dp else 1.dp, if (isPressed) BrandBlue else MetricBorder),
        interactionSource = interactionSource,
    ) {
        if (wide) {
            Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                CueIcon(cue, 44)
                Column(Modifier.padding(start = 14.dp)) { CueTexts(cue) }
            }
        } else {
            Column(Modifier.fillMaxWidth().padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CueIcon(cue, 40)
                    Text(
                        cue.code,
                        Modifier.padding(start = 10.dp),
                        Ink,
                        fontFamily = MaterialTheme.typography.headlineMedium.fontFamily,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
                Spacer(Modifier.height(12.dp))
                CueMeaning(cue)
            }
        }
    }
}

/** Kartu lebar: kode di atas arti, karena ikon sudah di sisi kiri. */
@Composable
private fun CueTexts(cue: CryCueDetail) {
    Text(cue.code, color = Ink, fontFamily = MaterialTheme.typography.headlineMedium.fontFamily, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
    Spacer(Modifier.height(2.dp))
    CueMeaning(cue)
}

@Composable
private fun CueMeaning(cue: CryCueDetail) {
    Text(stringResource(cue.metricMeaning), color = Ink, fontSize = 15.sp, lineHeight = 20.sp, fontWeight = FontWeight.SemiBold)
    Text(stringResource(cue.metricDetail), color = MutedInk, fontSize = 12.sp, lineHeight = 16.sp)
}

@Composable
private fun CryCueDetailSheet(cue: CryCueDetail, onClose: () -> Unit) {
    Column(
        Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp).padding(top = 20.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            CueIcon(cue, 56)
            Column(Modifier.padding(start = 14.dp).weight(1f)) {
                Text(cue.code, color = Ink, fontFamily = MaterialTheme.typography.headlineMedium.fontFamily, fontSize = 26.sp, fontWeight = FontWeight.Bold)
                Text(stringResource(cue.meaning), color = MutedInk, fontSize = 16.sp)
            }
            CloseButton(onClose, stringResource(R.string.common_close))
        }
        Text(stringResource(cue.description), color = MutedInk, fontSize = 14.sp, lineHeight = 20.sp)
        DetailSection(
            title = stringResource(R.string.cry_cues_signs),
            items = cue.features,
            background = Color(0xFFE5F4FB),
            tint = Color(0xFF4B9EB5),
        ) { CryCueIcon(cue.code, Modifier.size(44.dp)) }
        DetailSection(
            title = stringResource(R.string.cry_cues_actions),
            items = cue.actions,
            background = Color(0xFFFFF0D2),
            tint = Color(0xFFE6A02C),
        ) { Icon(painterResource(R.drawable.phosphor_heartbeat), null, tint = Color(0xFFE6A02C), modifier = Modifier.padding(9.dp)) }
        Button(
            onClick = onClose,
            modifier = Modifier.fillMaxWidth().heightIn(min = 54.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = BrandBlueDark),
        ) {
            Text(stringResource(R.string.cry_cues_got_it), fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun DetailSection(
    title: String,
    items: List<Int>,
    background: Color,
    tint: Color,
    illustration: @Composable () -> Unit,
) {
    Surface(Modifier.fillMaxWidth(), RoundedCornerShape(18.dp), background.copy(alpha = 0.34f), border = BorderStroke(1.dp, background)) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.Top) {
            Surface(Modifier.size(48.dp), CircleShape, background) { Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { illustration() } }
            Column(Modifier.padding(start = 12.dp).weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(title, color = Ink, fontSize = 15.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 9.dp))
                items.forEach { item ->
                    Row(verticalAlignment = Alignment.Top) {
                        Box(Modifier.padding(top = 7.dp).size(5.dp).background(tint, CircleShape))
                        Text(stringResource(item), Modifier.padding(start = 10.dp), MutedInk, fontSize = 13.sp, lineHeight = 18.sp)
                    }
                }
            }
        }
    }
}
