package id.madsaz.app.feature.plan

import android.app.Activity
import android.content.Intent
import android.net.Uri
import java.text.NumberFormat
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
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import id.madsaz.app.R
import id.madsaz.app.data.remote.PlanStatus
import id.madsaz.app.ui.components.CompactContainer
import id.madsaz.app.ui.components.formatDate
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
    onPurchase: (Activity, PlanInterval) -> Unit,
) {
    val context = LocalContext.current
    var selectedPlan by remember { mutableStateOf(PlanInterval.YEARLY) }
    val status = state.status
    val isPlus = status is PlanStatus.Plus
    var showSubscription by remember(isPlus) { mutableStateOf(isPlus) }
    val selectedPrice = state.prices[selectedPlan]?.formatted

    BackHandler(enabled = showSubscription && !isPlus) { showSubscription = false }

    CompactContainer(Modifier.fillMaxSize().background(AppBackground)) {
        when {
            // Tanpa data paket yang berhasil dimuat, jangan menampilkan Free maupun Plus.
            status == null -> PlanUnavailableContent(state, onRetry, onBack)
            showSubscription -> SubscriptionContent(
                state = state,
                selectedPlan = selectedPlan,
                selectedPrice = selectedPrice,
                isPlus = isPlus,
                onSelectPlan = { selectedPlan = it },
                onPurchase = { (context as? Activity)?.let { onPurchase(it, selectedPlan) } },
                onManage = {
                    val uri = Uri.parse("https://play.google.com/store/account/subscriptions?package=${context.packageName}&sku=${state.productId}")
                    context.startActivity(Intent(Intent.ACTION_VIEW, uri))
                },
                onRetry = onRetry,
                onBack = { if (isPlus) onBack() else showSubscription = false },
            )
            status is PlanStatus.Free -> FreePlanContent(
                state = state,
                status = status,
                onExplorePlus = { showSubscription = true },
                onContinue = onBack,
                onRetry = onRetry,
                onBack = onBack,
            )
            else -> Unit
        }
    }
}

@Composable
private fun PlanUnavailableContent(state: PlanUiState, onRetry: () -> Unit, onBack: () -> Unit) {
    Column(
        Modifier.fillMaxSize().background(Color.White).statusBarsPadding().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (state.loading) {
            CircularProgressIndicator(color = BrandBlue)
        } else {
            Text(stringResource(state.errorRes ?: R.string.error_unknown), color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center)
            OutlinedButton(onClick = onRetry) { Text(stringResource(R.string.common_retry)) }
            OutlinedButton(onClick = onBack) { Text(stringResource(R.string.common_back)) }
        }
    }
}

@Composable
private fun FreePlanContent(
    state: PlanUiState,
    status: PlanStatus.Free,
    onExplorePlus: () -> Unit,
    onContinue: () -> Unit,
    onRetry: () -> Unit,
    onBack: () -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().background(Color.White),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp),
    ) {
        item {
            Row(Modifier.fillMaxWidth().statusBarsPadding().padding(bottom = 24.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = stringResource(R.string.common_back), tint = Ink)
                }
                Text(stringResource(R.string.plan_title), color = Ink, fontFamily = MaterialTheme.typography.headlineMedium.fontFamily,
                    fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
            }
        }
        item {
            Surface(shape = RoundedCornerShape(24.dp), color = BrandBlue) {
                Box(Modifier.fillMaxWidth().height(174.dp)) {
                    Column(Modifier.offset(x = 20.dp, y = 16.dp)) {
                        Text(stringResource(R.string.plan_free_title), color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Text(stringResource(R.string.plan_monthly_analyses), color = Color.White, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
                        Text(
                            stringResource(R.string.plan_used_of, status.used, status.limit),
                            color = Color.White,
                            fontSize = 23.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 6.dp),
                        )
                        Text(
                            pluralStringResource(R.plurals.plan_remaining_month, status.remaining, status.remaining),
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 11.sp,
                            modifier = Modifier.padding(top = 4.dp),
                        )
                    }
                    Surface(
                        modifier = Modifier.offset(x = 20.dp, y = 130.dp),
                        shape = CircleShape,
                        color = Color.White,
                    ) {
                        Text(
                            pluralStringResource(R.plurals.plan_remaining_chip, status.remaining, status.remaining),
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 5.dp),
                            color = BrandBlueDark,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                    UsageBars(used = status.used, modifier = Modifier.align(Alignment.BottomEnd).padding(end = 36.dp, bottom = 22.dp))
                }
            }
        }
        if (state.errorRes != null) {
            item {
                Column(Modifier.padding(top = 12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(stringResource(state.errorRes), color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    OutlinedButton(onClick = onRetry) { Text(stringResource(R.string.common_retry)) }
                }
            }
        }
        item {
            Text(
                stringResource(R.string.plan_included_title),
                modifier = Modifier.padding(top = 30.dp, bottom = 16.dp),
                color = Color(0xFF0B2A2D),
                fontFamily = MaterialTheme.typography.headlineMedium.fontFamily,
                fontSize = 19.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                FreeBenefit(pluralStringResource(R.plurals.plan_benefit_analyses, status.limit, status.limit))
                FreeBenefit(stringResource(R.string.plan_benefit_cues))
                FreeBenefit(stringResource(R.string.plan_benefit_recent))
            }
        }
        item {
            Button(
                onClick = onExplorePlus,
                modifier = Modifier.fillMaxWidth().padding(top = 38.dp).height(52.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BrandBlue),
            ) { Text(stringResource(R.string.plan_explore_plus), fontWeight = FontWeight.SemiBold) }
        }
        item {
            Text(
                stringResource(R.string.plan_continue_free),
                modifier = Modifier.fillMaxWidth().clickable(onClick = onContinue).padding(top = 24.dp, bottom = 12.dp),
                color = BrandBlueDark,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            )
            Text(
                stringResource(R.string.plan_limit_reset),
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
    selectedPlan: PlanInterval,
    selectedPrice: String?,
    isPlus: Boolean,
    onSelectPlan: (PlanInterval) -> Unit,
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
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = stringResource(R.string.common_back), tint = Ink)
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
                            Text(stringResource(R.string.plan_plus_brand), color = Color.White, fontSize = 12.sp)
                            Text(stringResource(R.string.plan_plus_tagline), color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 5.dp))
                            listOf(R.string.plan_plus_benefit_unlimited, R.string.plan_plus_benefit_history, R.string.plan_plus_benefit_cues).forEach { item ->
                                Row(Modifier.padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Box(Modifier.size(9.dp).background(Color.White, CircleShape))
                                    Text(stringResource(item), color = Color.White, fontSize = 11.sp, modifier = Modifier.padding(start = 10.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
        val plus = state.status as? PlanStatus.Plus
        if (plus != null) {
            item { PlusStatus(plus) }
        } else {
            item {
                Text(
                    stringResource(R.string.plan_choose),
                    modifier = Modifier.padding(top = 20.dp, bottom = 16.dp),
                    color = Color(0xFF0B2A2D),
                    fontFamily = MaterialTheme.typography.headlineMedium.fontFamily,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            item {
                val savings = state.yearlySavingsPercent
                PlanOption(
                    title = stringResource(R.string.plan_yearly),
                    price = state.prices[PlanInterval.YEARLY]?.formatted,
                    // "Paling hemat" dan badge hanya bila harga Play memang lebih murah daripada 12 bulan paket bulanan.
                    helper = savings?.let { stringResource(R.string.plan_best_value) },
                    badge = savings?.let { stringResource(R.string.plan_save_badge, NumberFormat.getPercentInstance().format(it / 100.0)) },
                    selected = selectedPlan == PlanInterval.YEARLY,
                    onClick = { onSelectPlan(PlanInterval.YEARLY) },
                )
            }
            item { Spacer(Modifier.height(14.dp)) }
            item {
                PlanOption(
                    title = stringResource(R.string.plan_monthly),
                    price = state.prices[PlanInterval.MONTHLY]?.formatted,
                    helper = stringResource(R.string.plan_flexible),
                    selected = selectedPlan == PlanInterval.MONTHLY,
                    onClick = { onSelectPlan(PlanInterval.MONTHLY) },
                )
            }
        }
        if (state.errorRes != null) {
            item {
                Column(Modifier.padding(top = 12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(stringResource(state.errorRes), color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    OutlinedButton(onClick = onRetry) { Text(stringResource(R.string.common_retry)) }
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
                ) { Text(stringResource(R.string.plan_manage)) }
            } else {
                Button(
                    onClick = onPurchase,
                    enabled = state.purchaseEnabled && selectedPrice != null && !state.purchasing,
                    modifier = Modifier.fillMaxWidth().padding(top = 28.dp).height(52.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BrandBlue),
                ) {
                    if (state.purchasing) CircularProgressIndicator(Modifier.size(22.dp), color = Color.White, strokeWidth = 2.dp)
                    else Text(stringResource(R.string.plan_start))
                }
            }
        }
        item {
            Text(
                stringResource(R.string.plan_cancel_anytime),
                modifier = Modifier.fillMaxWidth().padding(top = 28.dp, bottom = 20.dp),
                color = MutedInk,
                fontSize = 10.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            )
        }
    }
}

@Composable
private fun PlusStatus(plus: PlanStatus.Plus) {
    val date = formatDate(plus.expiresAt.toEpochMilli())
    Surface(
        modifier = Modifier.fillMaxWidth().padding(top = 20.dp),
        shape = RoundedCornerShape(22.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Border),
    ) {
        Column(Modifier.padding(horizontal = 20.dp, vertical = 18.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(stringResource(R.string.plan_plus_active), color = Ink, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
            Text(
                stringResource(if (plus.autoRenewing) R.string.plan_plus_renews else R.string.plan_plus_ends, date),
                color = MutedInk,
                fontSize = 12.sp,
            )
        }
    }
}

@Composable
private fun PlanOption(
    title: String,
    price: String?,
    helper: String?,
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
                Text(price ?: stringResource(R.string.plan_price_unavailable), color = if (selected) Color.White else Ink,
                    fontSize = 18.sp, lineHeight = 22.sp, fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 6.dp))
            }
            Column(horizontalAlignment = Alignment.End) {
                if (badge != null) {
                    Surface(shape = CircleShape, color = Color.White) {
                        Text(badge, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), color = BrandBlueDark, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                    if (helper != null) Text(helper, color = Color.White, fontSize = 10.sp, modifier = Modifier.padding(top = 6.dp))
                } else if (helper != null) {
                    Text(helper, color = if (selected) Color.White else MutedInk, fontSize = 10.sp)
                }
            }
        }
    }
}
