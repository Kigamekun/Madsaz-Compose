package id.madsaz.app.feature.crycues.preview

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import id.madsaz.app.feature.crycues.CryCueDetailScreen
import id.madsaz.app.feature.crycues.CryCuesScreen
import id.madsaz.app.model.CryCue
import id.madsaz.app.ui.theme.MadsazTheme

@Preview(name = "Petunjuk · daftar", widthDp = 390, heightDp = 844, showBackground = true)
@Composable
private fun CryCuesPreview() {
    MadsazTheme { CryCuesScreen(onOpenCue = {}, onProfile = {}, contentPadding = PaddingValues()) }
}

@Preview(name = "Petunjuk · detail Eh", widthDp = 390, heightDp = 844, showBackground = true)
@Composable
private fun CryCueDetailPreview() {
    MadsazTheme { CryCueDetailScreen(CryCue.EH, onBack = {}) }
}
