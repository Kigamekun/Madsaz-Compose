package id.madsaz.app.feature.plan

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import id.madsaz.app.R
import id.madsaz.app.ui.theme.BrandBlueDark
import id.madsaz.app.ui.theme.Ink
import id.madsaz.app.ui.theme.MutedInk

/** Muncul saat jatah rekaman harian akun gratis habis: jelaskan alasannya dan tawarkan Plus tanpa memaksa. */
@Composable
fun UpgradeDialog(onUpgrade: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag("upgrade_dialog"),
        containerColor = Color.White,
        shape = RoundedCornerShape(28.dp),
        icon = { Image(painterResource(R.drawable.madsaz_result_baby_halo), null, Modifier.size(72.dp)) },
        title = { Text(stringResource(R.string.upgrade_dialog_title), color = Ink, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()) },
        text = { Text(stringResource(R.string.upgrade_dialog_body), color = MutedInk, style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center) },
        confirmButton = {
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Button(
                    onClick = onUpgrade,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp).testTag("upgrade_confirm"),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BrandBlueDark),
                ) { Text(stringResource(R.string.upgrade_dialog_cta)) }
                TextButton(onClick = onDismiss, modifier = Modifier.heightIn(min = 48.dp).testTag("upgrade_later")) {
                    Text(stringResource(R.string.upgrade_dialog_later), color = MutedInk)
                }
            }
        },
    )
}
