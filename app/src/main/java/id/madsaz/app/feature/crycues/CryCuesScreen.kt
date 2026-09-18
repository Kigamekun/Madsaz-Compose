package id.madsaz.app.feature.crycues

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.madsaz.app.R
import id.madsaz.app.ui.components.CompactContainer
import id.madsaz.app.ui.theme.Ink
import id.madsaz.app.ui.theme.MutedInk

private val CryCuesSurface = Color(0xFFF4F5FD)
private val GrowthBlue = Color(0xFFB9CEF9)
private val MetricBorder = Color(0xFFE4E7F2)

@Composable
fun CryCuesScreen(
    onSeeAll: () -> Unit,
    contentPadding: PaddingValues = PaddingValues(),
    modifier: Modifier = Modifier,
) {
    CompactContainer(modifier.padding(contentPadding)) {
        Box(Modifier.fillMaxSize().background(CryCuesSurface)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(302.dp)
                    .background(GrowthBlue),
            ) {
                Image(
                    painter = painterResource(R.drawable.madsaz_cry_cues_photo),
                    contentDescription = stringResource(R.string.cry_cues_hero_description),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(372.dp)
                        .offset(y = (-70).dp),
                    contentScale = ContentScale.Crop,
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .fillMaxWidth()
                        .height(24.dp)
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.Transparent, CryCuesSurface),
                            ),
                        ),
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(620.dp)
                    .offset(y = 302.dp)
                    .background(CryCuesSurface),
            ) {
                Text(
                    text = stringResource(R.string.cry_cues_title),
                    modifier = Modifier.offset(x = 24.dp, y = 25.dp),
                    color = Ink,
                    fontFamily = MaterialTheme.typography.headlineMedium.fontFamily,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = stringResource(R.string.cry_cues_see_all),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = (-22).dp, y = 30.dp)
                        .clickable(onClick = onSeeAll),
                    color = Color(0xFF5667D9),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                )

                CueMetric(
                    modifier = Modifier.offset(x = 24.dp, y = 64.dp),
                    icon = R.drawable.phosphor_bowl_food,
                    iconTint = Color(0xFFE38A73),
                    iconBackground = Color(0xFFFFF0E8),
                    title = "Neh",
                    meaning = "Hunger",
                    detail = "Needs milk",
                )
                CueMetric(
                    modifier = Modifier.offset(x = 201.dp, y = 64.dp),
                    icon = R.drawable.phosphor_moon_stars,
                    iconTint = Color(0xFF7567D8),
                    iconBackground = Color(0xFFEEEAFE),
                    title = "Owh",
                    meaning = "Sleepy",
                    detail = "Time to rest",
                )
                CueMetric(
                    modifier = Modifier.offset(x = 24.dp, y = 186.dp),
                    icon = R.drawable.phosphor_baby,
                    iconTint = Color(0xFF4B9EB5),
                    iconBackground = Color(0xFFE7F5FA),
                    title = "Eh",
                    meaning = "Needs a burp",
                    detail = "After feeding",
                    titleOffset = 58.dp,
                )
                CueMetric(
                    modifier = Modifier.offset(x = 201.dp, y = 186.dp),
                    icon = R.drawable.phosphor_heartbeat,
                    iconTint = Color(0xFFDD717D),
                    iconBackground = Color(0xFFFFE8EC),
                    title = "Heh",
                    meaning = "Uncomfortable",
                    detail = "Check comfort",
                    titleOffset = 30.dp,
                )
                CueMetric(
                    modifier = Modifier.offset(x = 24.dp, y = 308.dp),
                    width = 342,
                    height = 84,
                    icon = R.drawable.phosphor_wind,
                    iconTint = Color(0xFF56A986),
                    iconBackground = Color(0xFFE7F5EE),
                    title = "Eairh",
                    meaning = "Lower gas",
                    detail = "Tummy discomfort",
                    compact = true,
                )
            }
        }
    }
}

@Composable
private fun CueMetric(
    modifier: Modifier,
    icon: Int,
    iconTint: Color,
    iconBackground: Color,
    title: String,
    meaning: String,
    detail: String,
    width: Int = 165,
    height: Int = 108,
    titleOffset: androidx.compose.ui.unit.Dp = 60.dp,
    compact: Boolean = false,
) {
    Surface(
        modifier = modifier.width(width.dp).height(height.dp),
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, MetricBorder),
    ) {
        if (compact) {
            Box(Modifier.fillMaxSize()) {
                Surface(
                    modifier = Modifier.offset(x = 14.dp, y = 24.dp).size(36.dp),
                    shape = CircleShape,
                    color = iconBackground,
                ) {
                    Icon(
                        painter = painterResource(icon),
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.padding(6.dp),
                    )
                }
                Text(
                    title,
                    modifier = Modifier.offset(x = 58.dp, y = 10.dp),
                    color = Ink,
                    fontFamily = MaterialTheme.typography.headlineMedium.fontFamily,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    meaning,
                    modifier = Modifier.offset(x = 58.dp, y = 36.dp),
                    color = Ink,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    detail,
                    modifier = Modifier.offset(x = 58.dp, y = 60.dp),
                    color = MutedInk,
                    fontSize = 10.sp,
                )
            }
        } else {
            Box(Modifier.fillMaxSize()) {
                Surface(
                    modifier = Modifier.offset(x = 14.dp, y = 14.dp).size(36.dp),
                    shape = CircleShape,
                    color = iconBackground,
                ) {
                    Icon(
                        painter = painterResource(icon),
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.padding(6.dp),
                    )
                }
                Text(
                    title,
                    modifier = Modifier.offset(x = titleOffset, y = 22.dp),
                    color = Ink,
                    fontFamily = MaterialTheme.typography.headlineMedium.fontFamily,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    meaning,
                    modifier = Modifier.align(Alignment.TopCenter).offset(y = 57.dp),
                    color = Ink,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    detail,
                    modifier = Modifier.align(Alignment.TopCenter).offset(y = 82.dp),
                    color = MutedInk,
                    fontSize = 10.sp,
                )
            }
        }
    }
}
