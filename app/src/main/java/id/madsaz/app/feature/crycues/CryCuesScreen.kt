package id.madsaz.app.feature.crycues

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.madsaz.app.R
import id.madsaz.app.ui.components.BulletLine
import id.madsaz.app.ui.components.SolutionList
import id.madsaz.app.ui.components.cueTitle
import id.madsaz.app.model.CryCue
import id.madsaz.app.ui.components.CompactContainer
import id.madsaz.app.ui.components.CryCueIcon
import id.madsaz.app.ui.components.MadsazHeader
import id.madsaz.app.ui.theme.Border
import id.madsaz.app.ui.theme.BrandBlueDark
import id.madsaz.app.ui.theme.Ink
import id.madsaz.app.ui.theme.MutedInk

/** Tujuan utama "Petunjuk": kelima pola, masing-masing membuka halaman detailnya. */
@Composable
fun CryCuesScreen(
    onOpenCue: (CryCue) -> Unit,
    onProfile: () -> Unit,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
    avatarUrl: String? = null,
    accountName: String? = null,
) {
    CompactContainer(modifier) {
        LazyColumn(
            modifier = Modifier.fillMaxSize().background(Color.White).statusBarsPadding().testTag("cues_screen"),
            contentPadding = PaddingValues(bottom = contentPadding.calculateBottomPadding() + 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item { MadsazHeader(onProfile = onProfile, avatarUrl = avatarUrl, name = accountName) }
            item {
                Column(Modifier.padding(horizontal = 20.dp).padding(top = 8.dp, bottom = 4.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        stringResource(R.string.home_cry_cues_title),
                        modifier = Modifier.semantics { heading() },
                        color = Ink,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(stringResource(R.string.cues_intro), color = MutedInk, fontSize = 14.sp, lineHeight = 20.sp)
                }
            }
            items(CryCue.entries, key = { it.code }) { cue ->
                CueRow(cue, onClick = { onOpenCue(cue) }, modifier = Modifier.padding(horizontal = 20.dp))
            }
            item {
                Text(
                    stringResource(R.string.analysis_disclaimer),
                    modifier = Modifier.padding(horizontal = 20.dp).padding(top = 4.dp),
                    color = MutedInk,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}

@Composable
private fun CueRow(cue: CryCue, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val openLabel = stringResource(R.string.cue_open_detail)
    Surface(
        modifier = modifier.fillMaxWidth().testTag("cue_${cue.code.lowercase()}"),
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Border),
    ) {
        Row(
            Modifier.clickable(role = Role.Button, onClickLabel = openLabel, onClick = onClick).heightIn(min = 88.dp).padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CryCueIcon(cue.code, Modifier.size(52.dp))
            Column(Modifier.weight(1f).padding(horizontal = 14.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    cueTitle(cue.code, stringResource(cue.label)),
                    color = Ink,
                    fontFamily = MaterialTheme.typography.headlineMedium.fontFamily,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(stringResource(cue.summary), color = MutedInk, fontSize = 13.sp, lineHeight = 18.sp)
                // Teks ajakan yang terlihat, agar pengguna tahu kartu ini membuka halaman detail.
                Text(openLabel, color = BrandBlueDark, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 4.dp))
            }
            Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, contentDescription = null, tint = BrandBlueDark)
        }
    }
}

/** Detail satu pola: arti, ciri-ciri, dan yang bisa dilakukan. */
@Composable
fun CryCueDetailScreen(cue: CryCue, onBack: () -> Unit, modifier: Modifier = Modifier) {
    CompactContainer(modifier) {
        Column(Modifier.fillMaxSize().background(Color.White).statusBarsPadding().testTag("cue_detail")) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Rounded.ArrowBack, stringResource(R.string.common_back), tint = Ink)
                }
                Text(
                    stringResource(R.string.home_cry_cues_title),
                    color = Ink,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            Column(
                Modifier.weight(1f).verticalScroll(rememberScrollState()).navigationBarsPadding().padding(horizontal = 20.dp).padding(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CryCueIcon(cue.code, Modifier.size(72.dp))
                    Column(Modifier.padding(start = 16.dp).weight(1f)) {
                        Text(
                            cue.code,
                            modifier = Modifier.semantics { heading() },
                            color = Ink,
                            fontFamily = MaterialTheme.typography.headlineMedium.fontFamily,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(stringResource(cue.label), color = BrandBlueDark, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
                Text(stringResource(cue.description), color = MutedInk, fontSize = 15.sp, lineHeight = 22.sp)
                DetailSection(
                    title = stringResource(R.string.cry_cues_signs),
                    items = cue.signs,
                    background = Color(0xFFE5F4FB),
                    tint = Color(0xFF4B9EB5),
                ) { CryCueIcon(cue.code, Modifier.size(44.dp)) }
                DetailSection(
                    title = stringResource(R.string.cry_cues_actions),
                    items = cue.solution,
                    background = Color(0xFFFFF0D2),
                    tint = Color(0xFFE6A02C),
                    firstIsHeading = true,
                ) { Icon(painterResource(R.drawable.phosphor_heartbeat), null, tint = Color(0xFFE6A02C), modifier = Modifier.padding(9.dp)) }
                Spacer(Modifier.height(4.dp))
                Text(stringResource(R.string.analysis_disclaimer), color = MutedInk, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun DetailSection(
    title: String,
    items: List<Int>,
    background: Color,
    tint: Color,
    firstIsHeading: Boolean = false,
    illustration: @Composable () -> Unit,
) {
    Surface(Modifier.fillMaxWidth(), RoundedCornerShape(18.dp), background.copy(alpha = 0.34f), border = BorderStroke(1.dp, background)) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.Top) {
            Surface(Modifier.size(48.dp), CircleShape, background) { Box(Modifier.size(48.dp), contentAlignment = Alignment.Center) { illustration() } }
            Column(Modifier.padding(start = 12.dp).weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(title, color = Ink, fontSize = 16.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 10.dp).semantics { heading() })
                if (firstIsHeading) SolutionList(items, tint)
                else items.forEach { BulletLine(stringResource(it), tint) }
            }
        }
    }
}
