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
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.madsaz.app.R

val BrandBlue = Color(0xFF4A9BC0)
val BrandBlueDark = Color(0xFF3A82AB)
val BrandBlueSoft = Color(0xFFE6F4FB)
val BrandCoral = Color(0xFFE38A73)
val BrandCoralSoft = Color(0xFFFFEDE8)
val AppBackground = Color(0xFFF7FBFF)
val Cream = Color(0xFFE6F4FB)
val Ink = Color(0xFF1C3A53)
val MutedInk = Color(0xFF5E7D93)
val Border = Color(0xFFDEEAF2)
val Success = Color(0xFF2E6E54)
val SuccessSoft = Color(0xFFE7F5FA)

private val Fredoka = variableFont(R.font.fredoka, 600f)
private val FredokaBold = variableFont(R.font.fredoka, 700f)
private val Quicksand = variableFont(R.font.quicksand, 400f)
private val QuicksandSemiBold = variableFont(R.font.quicksand, 600f)
private val QuicksandBold = variableFont(R.font.quicksand, 700f)

private val MadsazColors = lightColorScheme(
    primary = BrandBlue,
    onPrimary = Color.White,
    primaryContainer = BrandBlueSoft,
    onPrimaryContainer = BrandBlueDark,
    secondary = BrandCoral,
    onSecondary = Color.White,
    secondaryContainer = BrandCoralSoft,
    onSecondaryContainer = Color(0xFF6D3328),
    background = AppBackground,
    onBackground = Ink,
    surface = Color.White,
    onSurface = Ink,
    surfaceVariant = Color(0xFFF0F2FA),
    onSurfaceVariant = MutedInk,
    outline = Color(0xFF7080A2),
    outlineVariant = Border,
    error = Color(0xFFB3261E),
    errorContainer = Color(0xFFFFE3E0),
)

private val MadsazTypography = Typography(
    headlineLarge = TextStyle(
        fontFamily = FredokaBold,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 34.sp,
        platformStyle = PlatformTextStyle(includeFontPadding = false),
    ),
    headlineMedium = TextStyle(
        fontFamily = FredokaBold,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        platformStyle = PlatformTextStyle(includeFontPadding = false),
    ),
    titleLarge = TextStyle(
        fontFamily = Fredoka,
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp,
        lineHeight = 24.sp,
        platformStyle = PlatformTextStyle(includeFontPadding = false),
    ),
    titleMedium = TextStyle(
        fontFamily = QuicksandSemiBold,
        fontWeight = FontWeight.SemiBold,
        fontSize = 15.sp,
        lineHeight = 21.sp,
        platformStyle = PlatformTextStyle(includeFontPadding = false),
    ),
    bodyLarge = TextStyle(
        fontFamily = Quicksand,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        platformStyle = PlatformTextStyle(includeFontPadding = false),
    ),
    bodyMedium = TextStyle(
        fontFamily = Quicksand,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        platformStyle = PlatformTextStyle(includeFontPadding = false),
    ),
    bodySmall = TextStyle(
        fontFamily = Quicksand,
        fontSize = 12.sp,
        lineHeight = 17.sp,
        platformStyle = PlatformTextStyle(includeFontPadding = false),
    ),
    labelLarge = TextStyle(
        fontFamily = QuicksandBold,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        platformStyle = PlatformTextStyle(includeFontPadding = false),
    ),
    labelMedium = TextStyle(
        fontFamily = QuicksandSemiBold,
        fontWeight = FontWeight.SemiBold,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        platformStyle = PlatformTextStyle(includeFontPadding = false),
    ),
)

private val MadsazShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(28.dp),
)

@Composable
fun MadsazTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = MadsazColors,
        typography = MadsazTypography,
        shapes = MadsazShapes,
        content = content,
    )
}

@OptIn(ExperimentalTextApi::class)
private fun variableFont(resourceId: Int, weight: Float): FontFamily =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        FontFamily(
            Font(
                resourceId,
                weight = FontWeight(weight.toInt()),
                variationSettings = FontVariation.Settings(
                    FontVariation.weight(weight.toInt()),
                    FontVariation.width(100f),
                ),
            ),
        )
    } else {
        FontFamily.SansSerif
    }
