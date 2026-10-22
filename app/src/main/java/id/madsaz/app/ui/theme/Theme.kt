package id.madsaz.app.ui.theme

import android.os.Build
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.madsaz.app.R

val BrandBlue = Color(0xFF4A9BC0)
val BrandBlueDark = Color(0xFF286B90)
val BrandBlueSoft = Color(0xFFE6F4FB)
val BrandCoral = Color(0xFFE38A73)
val BrandCoralSoft = Color(0xFFFFEDE8)
val AppBackground = Color.White
val Cream = BrandBlueSoft
val Ink = Color(0xFF1C3A53)
val MutedInk = Color(0xFF49677D)
val Border = Color(0xFFDEEAF2)
val Success = Color(0xFF2E6E54)
val SuccessSoft = Color(0xFFE7F5FA)

// Harus dideklarasikan sebelum Fredoka/Quicksand: properti top-level diinisialisasi berurutan.
private val FontWeights = listOf(400, 500, 600, 700)
private val Fredoka = fontFamily(R.font.fredoka, listOf(R.font.fredoka_400, R.font.fredoka_500, R.font.fredoka_600, R.font.fredoka_700))
private val Quicksand = fontFamily(R.font.quicksand, listOf(R.font.quicksand_400, R.font.quicksand_500, R.font.quicksand_600, R.font.quicksand_700))
private val Body = TextStyle(fontFamily = Quicksand, fontWeight = FontWeight.Normal, platformStyle = PlatformTextStyle(includeFontPadding = false))
private val Heading = Body.copy(fontFamily = Fredoka, fontWeight = FontWeight.Medium)

private val MadsazColors = lightColorScheme(
    primary = BrandBlueDark,
    onPrimary = Color.White,
    primaryContainer = BrandBlueSoft,
    onPrimaryContainer = Ink,
    secondary = BrandCoral,
    onSecondary = Color(0xFF53261D),
    secondaryContainer = BrandCoralSoft,
    onSecondaryContainer = Color(0xFF6D3328),
    background = AppBackground,
    onBackground = Ink,
    surface = Color.White,
    onSurface = Ink,
    surfaceVariant = AppBackground,
    surfaceContainer = AppBackground,
    surfaceContainerLow = AppBackground,
    surfaceContainerHigh = BrandBlueSoft,
    onSurfaceVariant = MutedInk,
    outline = MutedInk,
    outlineVariant = Border,
    error = Color(0xFFB3261E),
    errorContainer = Color(0xFFFFE3E0),
)

private val MadsazTypography = Typography(
    displayLarge = Heading.copy(fontSize = 36.sp, lineHeight = 44.sp),
    displayMedium = Heading.copy(fontSize = 32.sp, lineHeight = 40.sp),
    displaySmall = Heading.copy(fontSize = 28.sp, lineHeight = 36.sp),
    headlineLarge = Heading.copy(fontSize = 28.sp, lineHeight = 34.sp),
    headlineMedium = Heading.copy(fontSize = 24.sp, lineHeight = 30.sp),
    headlineSmall = Heading.copy(fontSize = 22.sp, lineHeight = 28.sp),
    titleLarge = Body.copy(fontWeight = FontWeight.Bold, fontSize = 18.sp, lineHeight = 24.sp),
    titleMedium = Body.copy(fontWeight = FontWeight.SemiBold, fontSize = 15.sp, lineHeight = 22.sp),
    titleSmall = Body.copy(fontWeight = FontWeight.SemiBold, fontSize = 14.sp, lineHeight = 20.sp),
    bodyLarge = Body.copy(fontSize = 16.sp, lineHeight = 24.sp),
    bodyMedium = Body.copy(fontSize = 14.sp, lineHeight = 21.sp),
    bodySmall = Body.copy(fontSize = 12.sp, lineHeight = 18.sp),
    labelLarge = Body.copy(fontWeight = FontWeight.Bold, fontSize = 14.sp, lineHeight = 20.sp),
    labelMedium = Body.copy(fontWeight = FontWeight.SemiBold, fontSize = 12.sp, lineHeight = 18.sp),
    labelSmall = Body.copy(fontWeight = FontWeight.SemiBold, fontSize = 12.sp, lineHeight = 16.sp),
)

private val MadsazShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(24.dp),
)

@Composable
fun MadsazTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = MadsazColors, typography = MadsazTypography, shapes = MadsazShapes, content = content)
}

/**
 * Android 8+ memakai satu berkas font variabel. Font variabel hanya didukung Android 8 ke atas (dokumentasi Compose),
 * jadi Android 7.x memakai instans statis per bobot ([staticFonts], urutan sama dengan [FontWeights]) yang dibuat dari berkas variabel yang sama.
 */
@OptIn(ExperimentalTextApi::class)
private fun fontFamily(variableFont: Int, staticFonts: List<Int>): FontFamily =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        FontFamily(
            FontWeights.map { weight ->
                Font(variableFont, weight = FontWeight(weight), variationSettings = FontVariation.Settings(FontVariation.weight(weight)))
            },
        )
    } else {
        FontFamily(FontWeights.zip(staticFonts) { weight, font -> Font(font, weight = FontWeight(weight)) })
    }
