package id.madsaz.app.feature.crycues.preview

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import id.madsaz.app.feature.crycues.CryCuesScreen
import id.madsaz.app.ui.components.MadsazScaffold
import id.madsaz.app.ui.components.MainDestination
import id.madsaz.app.ui.theme.MadsazTheme

@Preview(name = "Cry cues · 390x844", widthDp = 390, heightDp = 844, showBackground = true)
@Composable
private fun CryCuesPreview() {
    MadsazTheme {
        MadsazScaffold(
            selected = MainDestination.HOME,
            onHome = {},
            onHistory = {},
            onProfile = {},
            onRecord = {},
        ) { padding ->
            CryCuesScreen(onSeeAll = {}, contentPadding = padding)
        }
    }
}
