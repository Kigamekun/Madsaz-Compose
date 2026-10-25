package id.madsaz.app.ui.components

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.madsaz.app.ui.theme.Ink
import id.madsaz.app.ui.theme.MutedInk
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

/**
 * Judul pola di daftar dan hasil: "Owh (lelah)". Arti ditulis huruf kecil karena berada di dalam tanda kurung; ejaan
 * arti mengikuti Madsaz lama (lapar, lelah, ingin bersendawa, masuk angin, tidak nyaman).
 */
fun cueTitle(code: String, label: String): String = "$code (${label.replaceFirstChar { it.lowercase() }})"

/** Satu butir daftar: titik kecil berwarna lalu teks; teks yang membungkus sejajar dengan awal teks, bukan dengan titik. */
@Composable
fun BulletLine(text: String, tint: Color, modifier: Modifier = Modifier) {
    Row(modifier, verticalAlignment = Alignment.Top) {
        Box(Modifier.padding(top = 8.dp).size(5.dp).background(tint, CircleShape))
        Text(text, Modifier.padding(start = 10.dp), MutedInk, fontSize = 14.sp, lineHeight = 20.sp)
    }
}

/** Saran Madsaz lama: baris pertama adalah judul saran, baris berikutnya butir daftarnya (di Madsaz lama diawali "~"). */
@Composable
fun SolutionList(lines: List<Int>, tint: Color, modifier: Modifier = Modifier) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(stringResource(lines.first()), color = Ink, fontSize = 14.sp, lineHeight = 21.sp, fontWeight = FontWeight.SemiBold)
        lines.drop(1).forEach { BulletLine(stringResource(it), tint) }
    }
}
