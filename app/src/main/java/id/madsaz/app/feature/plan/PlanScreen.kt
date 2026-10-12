package id.madsaz.app.feature.plan

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.defaultMinSize
import id.madsaz.app.R
import id.madsaz.app.ui.components.CompactContainer
import id.madsaz.app.ui.theme.AppBackground
import id.madsaz.app.ui.theme.BrandBlue
import id.madsaz.app.ui.theme.BrandBlueDark
import id.madsaz.app.ui.theme.Border
import id.madsaz.app.ui.theme.Ink
import id.madsaz.app.ui.theme.MutedInk

@Composable
fun PlanScreen(
    state: PlanUiState,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    onPurchase: (Activity, String) -> Unit,
) {
    val context = LocalContext.current
    var selectedPlan by remember { mutableStateOf("yearly") }
    var showSubscription by remember(state.status?.code) { mutableStateOf(state.status?.code == "plus") }
    val isPlus = state.status?.code == "plus"
    val selectedPrice = state.prices[selectedPlan]

    BackHandler(enabled = showSubscription && !isPlus) { showSubscription = false }

    CompactContainer(Modifier.fillMaxSize().background(AppBackground)) {
        if (showSubscription) {
            SubscriptionContent(
                state = state,
                selectedPlan = selectedPlan,
                selectedPrice = selectedPrice,
                isPlus = isPlus,
                onSelectPlan = { selectedPlan = it },
                onPurchase = {
                    (context as? Activity)?.let { onPurchase(it, selectedPlan) }
                },
                onManage = {
                    val uri = Uri.parse("https://play.google.com/store/account/subscriptions?package=id.madsaz.app&sku=madsaz_plus")
                    context.startActivity(Intent(Intent.ACTION_VIEW, uri))
                },
                onRetry = onRetry,
                onBack = { if (isPlus) onBack() else showSubscription = false },
            )
        } else {
            FreePlanContent(
                state = state,
                onExplorePlus = { showSubscription = true },
                onContinue = onBack,
                onRetry = onRetry,
                onBack = onBack,
            )
        }
    }
}

@Composable
private fun FreePlanContent(state: PlanUiState, onExplorePlus: () -> Unit, onContinue: () -> Unit, onRetry: () -> Unit, onBack: () -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().background(Color.White),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp),
    ) {
        item {
            Row(Modifier.fillMaxWidth().statusBarsPadding().padding(bottom = 24.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back", tint = Ink)
                }
                Text("Your plan", color = Ink, fontFamily = MaterialTheme.typography.headlineMedium.fontFamily,
                    fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
            }
        }
        item {
            Surface(shape = RoundedCornerShape(24.dp), color = BrandBlue) {
                Box(Modifier.fillMaxWidth().height(174.dp)) {
                    Column(Modifier.offset(x = 20.dp, y = 16.dp)) {
                        Text("Free plan", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Text("Monthly analyses", color = Color.White, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
                        if (state.status != null) {
                            Text(
                                "${state.status.used} of 5 used",
                                color = Color.White,
                                fontSize = 23.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(top = 6.dp),
                            )
                            Text(
                                "${state.status.remaining} recordings left this month",
                                color = Color.White.copy(alpha = 0.9f),
                                fontSize = 11.sp,
                                modifier = Modifier.padding(top = 4.dp),
                            )
                        } else if (state.loading) {
                            Row(Modifier.padding(top = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                                CircularProgressIndicator(Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                                Text("Loading plan…", Modifier.padding(start = 8.dp), color = Color.White, fontSize = 12.sp)
                            }
                        }
                    }
                    if (state.status != null) {
                        Surface(
                            modifier = Modifier.offset(x = 20.dp, y = 130.dp),
                            shape = CircleShape,
                            color = Color.White,
                        ) {
                            Text(
                                "${state.status.remaining} recordings left",
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 5.dp),
                                color = BrandBlueDark,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }
                    UsageBars(used = state.status?.used ?: 0, modifier = Modifier.align(Alignment.BottomEnd).padding(end = 36.dp, bottom = 22.dp))
                }
            }
        }
        if (state.error != null) {
            item {
                Column(Modifier.padding(top = 12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(state.error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    OutlinedButton(onClick = onRetry) { Text("Try again") }
                }
            }
        }
        item {
            Text(
                "Included in Free",
                modifier = Modifier.padding(top = 30.dp, bottom = 16.dp),
                color = Color(0xFF0B2A2D),
                fontFamily = MaterialTheme.typography.headlineMedium.fontFamily,
                fontSize = 19.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }
        items(3) { index ->
            FreeBenefit(
                when (index) {
                    0 -> "5 cry analyses each month"
                    1 -> "Basic cue interpretation"
                    else -> "Recent recordings saved"
                },
            )
            if (index < 2) Spacer(Modifier.height(16.dp))
        }
        item {
            Button(
                onClick = onExplorePlus,
                modifier = Modifier.fillMaxWidth().padding(top = 38.dp).height(52.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BrandBlue),
            ) { Text("Explore Madsaz Plus", fontWeight = FontWeight.SemiBold) }
        }
        item {
            Text(
                "Continue with free plan",
                modifier = Modifier.fillMaxWidth().clickable(onClick = onContinue).padding(top = 24.dp, bottom = 12.dp),
                color = BrandBlueDark,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            )
            Text(
                "Your limit resets on the first day of each month.",
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 24.dp),
                color = MutedInk,
                fontSize = 10.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            )
        }
    }
}

@Composable
private fun UsageBars(used: Int, modifier: Modifier = Modifier) {
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(5.dp), verticalAlignment = Alignment.Bottom) {
        listOf(18, 26, 34, 46, 56).forEachIndexed { index, height ->
            val color = when {
                index < used -> Color(0xFFF15F5F)
                else -> Color.White.copy(alpha = 0.94f)
            }
            Box(Modifier.width(8.dp).height(height.dp).background(color, RoundedCornerShape(5.dp)))
        }
    }
}

@Composable
private fun FreeBenefit(label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Surface(Modifier.size(28.dp), CircleShape, Color(0xFFE7F5FA), border = BorderStroke(1.dp, Color(0xFF2E6E54))) {
            Box(contentAlignment = Alignment.Center) {
                Icon(Icons.Rounded.Check, null, tint = Color(0xFF2E6E54), modifier = Modifier.size(18.dp))
            }
        }
        Text(label, modifier = Modifier.padding(start = 14.dp), color = Ink, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun SubscriptionContent(
    state: PlanUiState,
    selectedPlan: String,
    selectedPrice: String?,
    isPlus: Boolean,
    onSelectPlan: (String) -> Unit,
    onPurchase: () -> Unit,
    onManage: () -> Unit,
    onRetry: () -> Unit,
    onBack: () -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().background(AppBackground),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp),
    ) {
        item {
            Box(Modifier.fillMaxWidth().height(324.dp)) {
                Image(
                    painter = painterResource(R.drawable.madsaz_home_photo),
                    contentDescription = null,
                    modifier = Modifier.fillMaxWidth().height(252.dp),
                    contentScale = ContentScale.Crop,
                )
                Surface(
                    modifier = Modifier.align(Alignment.TopStart).statusBarsPadding().padding(start = 8.dp, top = 8.dp),
                    shape = CircleShape, color = Color.White,
                ) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back", tint = Ink)
                    }
                }
                Box(
                    Modifier.align(Alignment.BottomStart).fillMaxWidth().height(116.dp)
                        .background(Brush.verticalGradient(listOf(Color.Transparent, AppBackground))),
                )
                Surface(
                    modifier = Modifier.align(Alignment.TopCenter).offset(y = 166.dp).fillMaxWidth().height(158.dp),
                    shape = RoundedCornerShape(28.dp),
                    color = BrandBlue,
                ) {
                    Box(Modifier.fillMaxSize()) {
                        Column(Modifier.padding(start = 20.dp, top = 20.dp, end = 20.dp)) {
                            Text("Madsaz Plus", color = Color.White, fontSize = 12.sp)
                            Text("More calm, more context.", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 5.dp))
                            listOf("Unlimited recordings", "Full history & insights", "Learn every cue").forEach { item ->
                                Row(Modifier.padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Box(Modifier.size(9.dp).background(Color.White, CircleShape))
                                    Text(item, color = Color.White, fontSize = 11.sp, modifier = Modifier.padding(start = 10.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
        item {
            Text(
                "Choose your plan",
                modifier = Modifier.padding(top = 20.dp, bottom = 16.dp),
                color = Color(0xFF0B2A2D),
                fontFamily = MaterialTheme.typography.headlineMedium.fontFamily,
                fontSize = 19.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }
        item {
            PlanOption(
                title = "Yearly",
                price = state.prices["yearly"],
                helper = "Best value",
                badge = "SAVE 30%",
                selected = selectedPlan == "yearly",
                onClick = { onSelectPlan("yearly") },
            )
        }
        item { Spacer(Modifier.height(14.dp)) }
        item {
            PlanOption(
                title = "Monthly",
                price = state.prices["monthly"],
                helper = "Flexible",
                selected = selectedPlan == "monthly",
                onClick = { onSelectPlan("monthly") },
            )
        }
        if (state.error != null) {
            item {
                Column(Modifier.padding(top = 12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(state.error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    OutlinedButton(onClick = onRetry) { Text("Try again") }
                }
            }
        }
        item {
            if (isPlus) {
                Button(
                    onClick = onManage,
                    modifier = Modifier.fillMaxWidth().padding(top = 28.dp).height(52.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BrandBlue),
                ) { Text("Manage subscription in Google Play") }
            } else {
                Button(
                    onClick = onPurchase,
                    enabled = state.purchaseEnabled && selectedPrice != null && !state.purchasing,
                    modifier = Modifier.fillMaxWidth().padding(top = 28.dp).height(52.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BrandBlue),
                ) {
                    if (state.purchasing) CircularProgressIndicator(Modifier.size(22.dp), color = Color.White, strokeWidth = 2.dp)
                    else Text("Start Madsaz Plus")
                }
            }
        }
        item {
            Text(
                "Cancel anytime. Your free plan stays available.",
                modifier = Modifier.fillMaxWidth().padding(top = 28.dp, bottom = 20.dp),
                color = MutedInk,
                fontSize = 10.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            )
        }
    }
}

@Composable
private fun PlanOption(
    title: String,
    price: String?,
    helper: String,
    selected: Boolean,
    onClick: () -> Unit,
    badge: String? = null,
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().defaultMinSize(minHeight = if (badge != null) 98.dp else 76.dp),
        shape = RoundedCornerShape(22.dp),
        color = if (selected) BrandBlue else Color.White,
        border = if (selected) null else BorderStroke(1.dp, Border),
    ) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text(title, color = if (selected) Color.White else Ink, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                Text(price ?: "Price unavailable", color = if (selected) Color.White else Ink,
                    fontSize = 18.sp, lineHeight = 22.sp, fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 6.dp))
            }
            Column(horizontalAlignment = Alignment.End) {
                if (badge != null) {
                    Surface(shape = CircleShape, color = Color.White) {
                        Text(badge, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), color = BrandBlueDark, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                    Text(helper, color = Color.White, fontSize = 10.sp, modifier = Modifier.padding(top = 6.dp))
                } else {
                    Text(helper, color = MutedInk, fontSize = 10.sp)
                }
            }
        }
    }
}
