package id.madsaz.app.feature.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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

@Composable
fun LanguageScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val selected = AppLanguage.current(context)
    CompactContainer {
        Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            BackIcon(onBack)
            Text(stringResource(R.string.language_title), color = Ink, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            AppLanguage.supported.forEach { (tag, name) ->
                Surface(
                    onClick = { AppLanguage.select(context, tag) },
                    modifier = Modifier.fillMaxWidth().semantics { role = Role.RadioButton; this.selected = tag == selected },
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFFF3F8FC),
                ) {
                    Row(Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(name, modifier = Modifier.weight(1f), color = Ink)
                        if (tag == selected) Icon(Icons.Rounded.Check, null, tint = BrandBlue)
                    }
                }
            }
        }
    }
}

@Composable
fun PrivacyScreen(state: PrivacyUiState, onRetry: () -> Unit, onBack: () -> Unit) {
    CompactContainer {
        Column(
            Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().verticalScroll(rememberScrollState()).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            BackIcon(onBack)
            when (state) {
                PrivacyUiState.Loading -> Box(Modifier.fillMaxWidth().padding(top = 48.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = BrandBlue)
                }
                is PrivacyUiState.Error -> {
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
