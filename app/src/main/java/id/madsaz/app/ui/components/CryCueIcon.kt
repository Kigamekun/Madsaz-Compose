package id.madsaz.app.ui.components

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import id.madsaz.app.R
import id.madsaz.app.model.CryCue

@Composable
fun CryCueIcon(code: String?, modifier: Modifier = Modifier) {
    val icon = CryCue.fromCode(code)?.icon ?: R.drawable.activity_clock
    Image(painterResource(icon), contentDescription = null, modifier = modifier, contentScale = ContentScale.Fit)
}

/**
 * Arti pola dalam bahasa aplikasi. [labelFromServer] hanya dipakai untuk kode di luar lima pola yang dikenal app,
 * sebagai teks tampilan apa adanya.
 */
@Composable
fun cryCueLabel(code: String, labelFromServer: String): String =
    CryCue.fromCode(code)?.let { stringResource(it.label) } ?: labelFromServer
