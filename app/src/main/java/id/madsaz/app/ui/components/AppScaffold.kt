package id.madsaz.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import id.madsaz.app.R
import id.madsaz.app.ui.theme.AppBackground
import id.madsaz.app.ui.theme.BrandBlue
import id.madsaz.app.ui.theme.BrandBlueDark
import id.madsaz.app.ui.theme.MutedInk

enum class MainDestination {
    HOME,
    HISTORY,
    PROFILE,
}

// Ukuran desain navigasi melayang. Tinggi total dihitung dari sini sehingga layar tidak perlu menebak padding bawahnya.
private val PillWidth = 280.dp
private val RecordRingSize = 88.dp
private val RecordButtonSize = 64.dp
private val RecordRingLift = 4.dp

/** Ruang kosong di tengah pill untuk cincin tombol rekam (88dp) ditambah jarak napas 6dp di kiri dan kanan. */
private val RecordGap = RecordRingSize + 12.dp
private val FloatGap = 12.dp

/** Tinggi dari dasar pill sampai puncak cincin tombol rekam. */

/**
 * Kerangka tiga tujuan utama dengan navigasi melayang. [content] menerima padding bawah yang sudah mencakup
 * navigasi melayang dan inset bilah navigasi sistem; konten digambar penuh di belakang navigasi dan memakai
 * padding itu sebagai ruang akhir daftarnya.
 */
@Composable
fun MadsazScaffold(
    selected: MainDestination,
    onHome: () -> Unit,
    onHistory: () -> Unit,
    onRecord: () -> Unit,
    content: @Composable (PaddingValues) -> Unit,
) {
    val density = LocalDensity.current
    val labelLines = if (density.fontScale > 1.3f) 2f else 1f
    val largeTextPadding = if (density.fontScale > 1.3f) 8.dp else 0.dp
    val pillHeight = maxOf(
        68.dp,
        44.dp + with(density) { MaterialTheme.typography.labelSmall.lineHeight.toDp() } * labelLines + largeTextPadding,
    )
    val navHeight = maxOf(pillHeight, RecordRingSize + RecordRingLift)
    val systemBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    Box(Modifier.fillMaxSize().background(AppBackground)) {
        content(PaddingValues(bottom = systemBottom + FloatGap + navHeight))
        FloatingNav(
            selected = selected,
            pillHeight = pillHeight,
            navHeight = navHeight,
            onHome = onHome,
            onHistory = onHistory,
            onRecord = onRecord,
            modifier = Modifier.align(Alignment.BottomCenter).navigationBarsPadding().padding(bottom = FloatGap).zIndex(1f),
        )
    }
}

@Composable
private fun FloatingNav(
    selected: MainDestination,
    pillHeight: androidx.compose.ui.unit.Dp,
    navHeight: androidx.compose.ui.unit.Dp,
    onHome: () -> Unit,
    onHistory: () -> Unit,
    onRecord: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier.fillMaxWidth().height(navHeight), contentAlignment = Alignment.BottomCenter) {
        Surface(
            modifier = Modifier.padding(horizontal = 20.dp).width(if (LocalDensity.current.fontScale > 1.3f) 360.dp else PillWidth).height(pillHeight),
            shape = RoundedCornerShape(27.dp),
            color = Color.White,
            border = BorderStroke(1.dp, Color(0xFFDFE3F2)),
            shadowElevation = 2.dp,
        ) {
            // Tujuan kiri, ruang tetap di bawah tombol rekam, tujuan kanan; dua tujuan berbagi sisa lebar sama besar. Row ikut mencerminkan di RTL.
            Row(Modifier.fillMaxSize().selectableGroup(), verticalAlignment = Alignment.CenterVertically) {
                NavItem(
                    iconRes = R.drawable.phosphor_house,
                    label = stringResource(R.string.nav_home),
                    selected = selected == MainDestination.HOME,
                    onClick = onHome,
                    modifier = Modifier.weight(1f),
                )
                Spacer(Modifier.width(RecordGap))
                NavItem(
                    iconRes = R.drawable.phosphor_clock_counter_clockwise,
                    label = stringResource(R.string.nav_history),
                    selected = selected == MainDestination.HISTORY,
                    onClick = onHistory,
                    modifier = Modifier.weight(1f),
                )
            }
        }
        RecordButton(onRecord, Modifier.align(Alignment.BottomCenter).offset(y = -RecordRingLift))
    }
}

@Composable
private fun NavItem(iconRes: Int, label: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val tint = if (selected) BrandBlueDark else MutedInk
    Column(
        modifier = modifier
            .padding(horizontal = 8.dp)
            .defaultMinSize(minHeight = 48.dp)
            .clip(RoundedCornerShape(16.dp))
            .selectable(selected = selected, onClick = onClick, role = Role.Tab)
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterVertically),
    ) {
        // Label sudah dibaca TalkBack sebagai teks tab; ikon dekoratif.
        Icon(painter = painterResource(iconRes), contentDescription = null, tint = tint, modifier = Modifier.size(24.dp))
        Text(
            text = label,
            modifier = Modifier.fillMaxWidth(),
            color = tint,
            textAlign = TextAlign.Center,
            fontSize = 12.sp,
            style = MaterialTheme.typography.labelSmall,
        )
    }
}

@Composable
private fun RecordButton(onRecord: () -> Unit, modifier: Modifier = Modifier) {
    val description = stringResource(R.string.nav_record)
    Surface(
        modifier = modifier.size(RecordRingSize),
        shape = CircleShape,
        color = Color.White,
        border = BorderStroke(1.dp, Color(0xFFC7D9F5)),
    ) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Surface(
                modifier = Modifier.size(RecordButtonSize).clip(CircleShape).clickable(role = Role.Button, onClickLabel = description, onClick = onRecord),
                shape = CircleShape,
                color = BrandBlue,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        painter = painterResource(R.drawable.phosphor_camera),
                        contentDescription = description,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp),
                    )
                }
            }
        }
    }
}
