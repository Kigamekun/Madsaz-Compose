package id.madsaz.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import id.madsaz.app.R
import id.madsaz.app.ui.theme.AppBackground
import id.madsaz.app.ui.theme.BrandBlue
import id.madsaz.app.ui.theme.BrandBlueDark
import id.madsaz.app.ui.theme.MutedInk

/** Empat tujuan utama di navigasi bawah; slot tengah adalah aksi Rekam, bukan tujuan. */
enum class MainDestination {
    HOME,
    HISTORY,
    CUES,
    PROFILE,
}

// Cincin tombol rekam menonjol di atas pill; tinggi total dihitung dari sini sehingga layar tidak perlu menebak padding bawahnya.
private val RecordRingSize = 64.dp
private val RecordButtonSize = 52.dp
private val ItemVerticalPadding = 8.dp
private val FloatGap = 12.dp
private val NavShape = RoundedCornerShape(28.dp)

/** Ikon tujuan yang tidak aktif: abu-biru lembut, lebih pucat dari teks label. */
private val NavIdleTint = Color(0xFF8AA0B0)

/**
 * Kerangka tujuan utama dengan navigasi melayang: Beranda, Riwayat, [Rekam], Petunjuk, Profil (pedoman Material:
 * 3-5 tujuan, setiap tujuan berikon dan berlabel). [content] menerima padding bawah yang sudah mencakup navigasi dan
 * inset bilah navigasi sistem; konten digambar penuh di belakang navigasi dan memakai padding itu sebagai ruang akhir.
 */
@Composable
fun MadsazScaffold(
    selected: MainDestination,
    onNavigate: (MainDestination) -> Unit,
    onRecord: () -> Unit,
    content: @Composable (PaddingValues) -> Unit,
) {
    val density = LocalDensity.current
    val labelHeight = with(density) { navLabelStyle().lineHeight.toDp() }
    // Ikon (24) + jarak (4) + label + padding atas/bawah, minimal 64dp agar setiap tujuan punya target sentuh di atas 48dp.
    val pillHeight = maxOf(64.dp, 28.dp + labelHeight + ItemVerticalPadding * 2)
    // Label "Rekam" sejajar label lain; cincin duduk tepat di atas label dan menonjol keluar dari pill.
    val navHeight = maxOf(pillHeight, RecordRingSize + 4.dp + labelHeight + (pillHeight - 28.dp - labelHeight) / 2)
    val systemBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    Box(Modifier.fillMaxSize().background(AppBackground)) {
        content(PaddingValues(bottom = systemBottom + FloatGap + navHeight))
        FloatingNav(
            selected = selected,
            pillHeight = pillHeight,
            navHeight = navHeight,
            // Label tujuan berada di tengah pill secara vertikal; label Rekam diberi jarak bawah yang sama agar sebaris.
            recordBottomPadding = (pillHeight - 28.dp - labelHeight) / 2,
            onNavigate = onNavigate,
            onRecord = onRecord,
            modifier = Modifier.align(Alignment.BottomCenter).navigationBarsPadding().padding(bottom = FloatGap).zIndex(1f),
        )
    }
}

@Composable
private fun FloatingNav(
    selected: MainDestination,
    pillHeight: Dp,
    navHeight: Dp,
    recordBottomPadding: Dp,
    onNavigate: (MainDestination) -> Unit,
    onRecord: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier.fillMaxWidth().padding(horizontal = 12.dp).widthIn(max = 420.dp).height(navHeight),
        contentAlignment = Alignment.BottomCenter,
    ) {
        // Lembut: bayangan lebar dan pucat berwarna biru-abu (bukan bayangan gelap Material), tepi tipis nyaris tak terlihat.
        Surface(
            modifier = Modifier.fillMaxWidth().height(pillHeight)
                .shadow(14.dp, NavShape, ambientColor = Color(0x143C6E8C), spotColor = Color(0x1F3C6E8C)),
            shape = NavShape,
            color = Color.White,
            border = BorderStroke(1.dp, Color(0xFFEEF3F8)),
        ) {
            // Lima kolom sama lebar; kolom tengah dikosongkan untuk tombol Rekam. Row ikut mencerminkan di RTL.
            Row(Modifier.fillMaxSize().selectableGroup(), verticalAlignment = Alignment.CenterVertically) {
                NavItem(MainDestination.HOME, selected, Icons.Outlined.Home, R.string.nav_home, "nav_home", onNavigate)
                NavItem(MainDestination.HISTORY, selected, Icons.Outlined.History, R.string.nav_history, "nav_history", onNavigate)
                Spacer(Modifier.weight(1f))
                NavItem(MainDestination.CUES, selected, Icons.AutoMirrored.Outlined.MenuBook, R.string.nav_cues, "nav_cues", onNavigate)
                NavItem(MainDestination.PROFILE, selected, Icons.Outlined.Person, R.string.nav_profile, "nav_profile", onNavigate)
            }
        }
        RecordAction(onRecord, Modifier.align(Alignment.BottomCenter).padding(bottom = recordBottomPadding))
    }
}

@Composable
private fun RowScope.NavItem(
    destination: MainDestination,
    selected: MainDestination,
    icon: ImageVector,
    labelRes: Int,
    tag: String,
    onNavigate: (MainDestination) -> Unit,
) {
    val isSelected = destination == selected
    Column(
        modifier = Modifier
            .weight(1f)
            .fillMaxHeight()
            .clip(RoundedCornerShape(20.dp))
            .selectable(selected = isSelected, onClick = { onNavigate(destination) }, role = Role.Tab)
            .padding(vertical = ItemVerticalPadding)
            .testTag(tag),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterVertically),
    ) {
        // Tujuan aktif hanya ditandai warna ikon (tanpa latar atau bentuk lain); label tetap netral dan dibaca TalkBack sebagai teks tab.
        Icon(icon, contentDescription = null, tint = if (isSelected) BrandBlueDark else NavIdleTint, modifier = Modifier.size(24.dp))
        Text(
            text = stringResource(labelRes),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 2.dp),
            color = MutedInk,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = navLabelStyle(),
        )
    }
}

/** Tombol Rekam: cincin menonjol plus label yang terlihat; keduanya satu target sentuh dan satu tombol bagi TalkBack. */
@Composable
private fun RecordAction(onRecord: () -> Unit, modifier: Modifier = Modifier) {
    val description = stringResource(R.string.nav_record)
    Column(
        modifier = modifier
            .width(RecordRingSize)
            .clip(RoundedCornerShape(24.dp))
            .clickable(role = Role.Button, onClickLabel = description, onClick = onRecord)
            .testTag("nav_record"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Surface(
            modifier = Modifier.size(RecordRingSize),
            shape = CircleShape,
            color = Color.White,
            border = BorderStroke(1.dp, Color(0xFFDCE8F6)),
        ) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Surface(modifier = Modifier.size(RecordButtonSize), shape = CircleShape, color = BrandBlue) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(painterResource(R.drawable.phosphor_camera), contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                    }
                }
            }
        }
        Text(
            stringResource(R.string.nav_record_label),
            color = MutedInk,
            textAlign = TextAlign.Center,
            maxLines = 1,
            style = navLabelStyle(),
        )
    }
}

/**
 * Label navigasi ikut ukuran huruf sistem sampai 130%, lalu berhenti membesar: lima slot di layar 360dp tidak muat
 * untuk satu kata pada 200% ("Beranda", "Petunjuk"), dan label terpotong lebih buruk daripada label yang sedikit lebih kecil.
 * Teks lain di layar tetap mengikuti ukuran huruf penuh.
 */
@Composable
private fun navLabelStyle(): TextStyle {
    val fontScale = LocalDensity.current.fontScale
    val factor = minOf(fontScale, MaxNavLabelScale) / fontScale
    return MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp * factor, lineHeight = 16.sp * factor)
}

private const val MaxNavLabelScale = 1.3f
