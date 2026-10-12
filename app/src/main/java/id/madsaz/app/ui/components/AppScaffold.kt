package id.madsaz.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import id.madsaz.app.R
import id.madsaz.app.ui.theme.AppBackground
import id.madsaz.app.ui.theme.BrandBlue
import id.madsaz.app.ui.theme.MutedInk

enum class MainDestination {
    HOME,
    HISTORY,
    PROFILE,
}

@Composable
fun MadsazScaffold(
    selected: MainDestination,
    onHome: () -> Unit,
    onHistory: () -> Unit,
    onProfile: () -> Unit,
    onRecord: () -> Unit,
    content: @Composable (PaddingValues) -> Unit,
) {
    Box(Modifier.fillMaxSize().background(AppBackground)) {
        content(PaddingValues())
        BottomNavModern(
            selected = selected,
            onHome = onHome,
            onHistory = onHistory,
            onRecord = onRecord,
        )
    }
}

@Composable
private fun BoxScope.BottomNavModern(
    selected: MainDestination,
    onHome: () -> Unit,
    onHistory: () -> Unit,
    onRecord: () -> Unit,
) {
    Box(
        modifier = Modifier
            .align(Alignment.BottomCenter)
            .offset(y = (-16).dp)
            .size(width = 222.dp, height = 76.dp)
            .zIndex(1f),
    ) {
        Surface(
            modifier = Modifier.offset(y = 9.dp).size(width = 222.dp, height = 67.dp),
            shape = RoundedCornerShape(27.dp),
            color = Color.White,
            border = BorderStroke(1.dp, Color(0xFFDFE3F2)),
            shadowElevation = 4.dp,
        ) { }
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = (-16).dp)
                .size(88.dp),
            contentAlignment = Alignment.Center,
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                shape = CircleShape,
                color = Color.White,
                border = BorderStroke(1.dp, Color(0xFFC7D9F5)),
            ) { }
            Surface(
                modifier = Modifier.size(64.dp).clickable(onClick = onRecord),
                shape = CircleShape,
                color = BrandBlue,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        painter = painterResource(R.drawable.phosphor_camera),
                        contentDescription = stringResource(R.string.nav_record),
                        tint = Color.White,
                        modifier = Modifier.size(24.dp),
                    )
                }
            }
        }
        Box(
            modifier = Modifier
                .offset(x = 18.dp, y = 18.dp)
                .size(width = 58.dp, height = 52.dp)
                .clip(RoundedCornerShape(16.dp))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onHome,
                ),
        ) {
            Icon(
                painter = painterResource(R.drawable.phosphor_house),
                contentDescription = stringResource(R.string.nav_home),
                tint = if (selected == MainDestination.HOME) BrandBlue else MutedInk,
                modifier = Modifier.align(Alignment.TopCenter).size(24.dp),
            )
            Text(
                text = stringResource(R.string.nav_home),
                modifier = Modifier.align(Alignment.BottomCenter).width(58.dp),
                color = if (selected == MainDestination.HOME) BrandBlue else MutedInk,
                textAlign = TextAlign.Center,
                fontSize = 9.sp,
                style = MaterialTheme.typography.labelSmall,
            )
        }
        Box(
            modifier = Modifier
                .offset(x = 157.dp, y = 18.dp)
                .size(width = 50.dp, height = 52.dp)
                .clip(RoundedCornerShape(16.dp))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onHistory,
                ),
        ) {
            Icon(
                painter = painterResource(R.drawable.phosphor_clock_counter_clockwise),
                contentDescription = stringResource(R.string.nav_history),
                tint = if (selected == MainDestination.HISTORY) BrandBlue else MutedInk,
                modifier = Modifier.align(Alignment.TopCenter).size(24.dp),
            )
            Text(
                text = stringResource(R.string.nav_history),
                modifier = Modifier.align(Alignment.BottomCenter).width(50.dp),
                color = if (selected == MainDestination.HISTORY) BrandBlue else MutedInk,
                textAlign = TextAlign.Center,
                fontSize = 9.sp,
                style = MaterialTheme.typography.labelSmall,
            )
        }
    }
}
