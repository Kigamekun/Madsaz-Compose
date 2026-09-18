package id.madsaz.app.feature.guide.preview

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import id.madsaz.app.feature.guide.GuideScreen
import id.madsaz.app.ui.theme.MadsazTheme

@Preview(name = "Panduan", widthDp = 412, heightDp = 915, showBackground = true)
@Composable
private fun GuidePreview() {
    MadsazTheme { GuideScreen(onBack = {}) }
}
