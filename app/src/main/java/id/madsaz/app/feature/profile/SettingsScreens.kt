package id.madsaz.app.feature.profile

import android.app.Activity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.madsaz.app.BuildConfig
import id.madsaz.app.data.local.AppLanguage
import id.madsaz.app.ui.components.CompactContainer
import id.madsaz.app.ui.theme.BrandBlue
import id.madsaz.app.ui.theme.Ink
import id.madsaz.app.ui.theme.MutedInk
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

@Composable
fun LanguageScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val selected = AppLanguage.selected(context)
    CompactContainer {
        Column(Modifier.fillMaxSize().statusBarsPadding().padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Back") }
            Text(if (selected == "id") "Bahasa" else "Language", color = Ink, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            listOf("en" to "English", "id" to "Bahasa Indonesia").forEach { (code, label) ->
                Surface(onClick = { AppLanguage.select(context as Activity, code) },
                    modifier = Modifier.fillMaxWidth(), color = Color(0xFFF3F8FC)) {
                    androidx.compose.foundation.layout.Row(Modifier.padding(18.dp)) {
                        Text(label, modifier = Modifier.weight(1f), color = Ink)
                        if (code == selected) Icon(Icons.Rounded.Check, null, tint = BrandBlue)
                    }
                }
            }
        }
    }
}

@Composable
fun PrivacyScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val language = AppLanguage.selected(context)
    val policy by produceState<JSONObject?>(null, language) {
        value = withContext(Dispatchers.IO) {
            runCatching {
                val connection = URL(BuildConfig.API_BASE_URL.trimEnd('/') + "/privacy-policy?lang=$language")
                    .openConnection() as HttpURLConnection
                try {
                    connection.connectTimeout = 10_000
                    connection.readTimeout = 15_000
                    if (connection.responseCode !in 200..299) error("Privacy policy unavailable")
                    JSONObject(connection.inputStream.bufferedReader().use { it.readText() }).getJSONObject("data")
                } finally {
                    connection.disconnect()
                }
            }.getOrNull()
        }
    }
    CompactContainer {
        Column(Modifier.fillMaxSize().statusBarsPadding().verticalScroll(rememberScrollState()).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)) {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Back") }
            if (policy == null) {
                Text(if (language == "id") "Kebijakan privasi belum dapat dimuat." else "Privacy policy is unavailable.", color = MutedInk)
            } else {
                Text(policy!!.getString("title"), color = Ink, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                Text("${policy!!.getString("version")} · ${policy!!.getString("status")}", color = MutedInk, fontSize = 12.sp)
                val sections = policy!!.getJSONArray("sections")
                for (index in 0 until sections.length()) {
                    val section = sections.getJSONObject(index)
                    Text(section.getString("title"), color = Ink, fontWeight = FontWeight.Bold)
                    Text(section.getString("body"), color = Ink)
                }
            }
        }
    }
}
