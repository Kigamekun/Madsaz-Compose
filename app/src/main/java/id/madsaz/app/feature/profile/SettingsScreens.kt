package id.madsaz.app.feature.profile

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.madsaz.app.R
import id.madsaz.app.data.local.AppLanguage
import id.madsaz.app.ui.components.CompactContainer
import id.madsaz.app.ui.theme.BrandBlue
import id.madsaz.app.ui.theme.Ink
import id.madsaz.app.ui.theme.MutedInk

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguageScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val selected = AppLanguage.current(context)
    ModalBottomSheet(onDismissRequest = onBack, sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true), containerColor = Color.White, dragHandle = null) {
        Column(
            Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp).padding(top = 16.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(R.string.language_title), modifier = Modifier.weight(1f), color = Ink, style = MaterialTheme.typography.headlineMedium)
                IconButton(onClick = onBack) { Icon(Icons.Rounded.Close, stringResource(R.string.common_close), tint = MutedInk) }
            }
            AppLanguage.supported.forEach { (tag, name) ->
                val flag = AppLanguage.flag(tag)
                Surface(
                    onClick = { AppLanguage.select(tag) },
                    modifier = Modifier.fillMaxWidth().semantics { role = Role.RadioButton; this.selected = tag == selected },
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White,
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFDEEAF2)),
                ) {
                    Row(Modifier.padding(horizontal = 14.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Surface(Modifier.size(44.dp, 34.dp), RoundedCornerShape(8.dp), Color(0xFFF6F9FB)) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(flag, fontSize = 22.sp, lineHeight = 26.sp)
                            }
                        }
                        Spacer(Modifier.width(12.dp))
                        Text(name, modifier = Modifier.weight(1f), color = Ink, style = MaterialTheme.typography.bodyLarge)
                        if (tag == selected) Icon(Icons.Rounded.Check, null, tint = BrandBlue)
                    }
                }
            }
        }
    }
}

@Composable
fun PrivacyScreen(state: PrivacyUiState, onRetry: () -> Unit, onBack: () -> Unit, @StringRes titleRes: Int = R.string.profile_privacy_title) {
    CompactContainer {
        Column(
            Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().verticalScroll(rememberScrollState()).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            BackIcon(onBack)
            when (state) {
                PrivacyUiState.Loading -> {
                    Text(
                        stringResource(titleRes),
                        color = Ink,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                    )
                    Box(Modifier.fillMaxWidth().padding(top = 34.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = BrandBlue)
                    }
                }
                is PrivacyUiState.Error -> {
                    Text(
                        stringResource(titleRes),
                        color = Ink,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(stringResource(state.messageRes), color = MaterialTheme.colorScheme.error)
                    OutlinedButton(onClick = onRetry) { Text(stringResource(R.string.common_retry)) }
                }
                is PrivacyUiState.Content -> {
                    val policy = state.policy
                    Text(policy.title, color = Ink, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    Text(stringResource(R.string.privacy_version, policy.version), color = MutedInk, fontSize = 12.sp)
                    // Kebijakan masih draf di BE: pengguna berhak tahu, dan catatan ini hilang sendiri saat BE menerbitkan versi final.
                    if (policy.status == "draft") Text(stringResource(R.string.privacy_draft_notice), color = MutedInk, fontSize = 12.sp)
                    policy.sections.forEach { section ->
                        Text(section.title, color = Ink, fontWeight = FontWeight.Bold)
                        Text(section.body, color = Ink)
                    }
                }
            }
        }
    }
}

@Composable
private fun BackIcon(onBack: () -> Unit) {
    IconButton(onClick = onBack) {
        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = stringResource(R.string.common_back), tint = Ink)
    }
}
