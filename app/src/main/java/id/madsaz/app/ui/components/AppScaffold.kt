package id.madsaz.app.ui.components

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
            .size(width = 222.dp, height = 76.dp),
    ) {
        Surface(
            modifier = Modifier.offset(y = 9.dp).size(width = 222.dp, height = 67.dp),
            shape = RoundedCornerShape(27.dp),
            color = Color.White,
            border = BorderStroke(1.dp, Color(0xFFDFE3F2)),
            shadowElevation = 4.dp,
        ) { }
        Surface(
            modifier = Modifier.offset(x = 69.dp, y = (-16).dp).size(84.dp),
            shape = CircleShape,
            color = Color.White,
            border = BorderStroke(1.dp, Color(0xFFC7D9F5)),
        ) { }
        Surface(
            modifier = Modifier
                .offset(x = 81.dp, y = (-4).dp)
                .size(60.dp)
                .clickable(onClick = onRecord),
            shape = CircleShape,
            color = BrandBlue,
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    painter = painterResource(R.drawable.phosphor_microphone),
                    contentDescription = stringResource(R.string.nav_record),
                    tint = Color.White,
                    modifier = Modifier.size(24.dp),
                )
            }
        }
        Box(
            modifier = Modifier
                .offset(x = 18.dp, y = 18.dp)
                .size(width = 58.dp, height = 52.dp)
                .clickable(onClick = onHome),
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
                .clickable(onClick = onHistory),
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
