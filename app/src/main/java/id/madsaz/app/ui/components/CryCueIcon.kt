package id.madsaz.app.ui.components

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import id.madsaz.app.R
import java.util.Locale

@Composable
fun CryCueIcon(code: String?, modifier: Modifier = Modifier) {
    val icon = when (code?.substringBefore(" - ")?.lowercase(Locale.ROOT)) {
        "neh" -> R.drawable.cue_neh
        "owh" -> R.drawable.cue_owh
        "eh" -> R.drawable.cue_eh
        "heh" -> R.drawable.cue_heh
        "eairh" -> R.drawable.cue_eairh
        else -> R.drawable.activity_clock
    }
    Image(painterResource(icon), contentDescription = null, modifier = modifier, contentScale = ContentScale.Fit)
}
