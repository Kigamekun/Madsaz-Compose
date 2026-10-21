package id.madsaz.app.feature.plan

import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import android.app.Activity
import android.content.Intent
import android.net.Uri
import java.text.NumberFormat
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import id.madsaz.app.R
import id.madsaz.app.data.remote.PlanStatus
import id.madsaz.app.ui.components.formatDate
import id.madsaz.app.ui.theme.BrandBlue
import id.madsaz.app.ui.theme.BrandBlueDark
import id.madsaz.app.ui.theme.Border
import id.madsaz.app.ui.theme.Ink
import id.madsaz.app.ui.theme.MutedInk

@OptIn(ExperimentalMaterial3Api::class)
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
    val sheetMaxHeight = LocalConfiguration.current.screenHeightDp.dp * 0.92f

    ModalBottomSheet(
        onDismissRequest = onBack,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = Color.White,
        dragHandle = null,
    ) {
        Column(Modifier.fillMaxWidth().heightIn(max = sheetMaxHeight).background(Color.White)) {
            PlanSheetHeader(
                title = if (showSubscription) R.string.plan_plus_brand else R.string.plan_title,
                onNavigateBack = if (showSubscription && !isPlus) ({ showSubscription = false }) else null,
                onDismiss = onBack,
            )
            when {
                // Tanpa data paket yang berhasil dimuat, jangan menampilkan Free maupun Plus.
                status == null -> PlanUnavailableContent(state, onRetry)
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
                )
                status is PlanStatus.Free -> FreePlanContent(
                    state = state,
                    status = status,
                    onExplorePlus = { showSubscription = true },
                    onContinue = onBack,
                    onRetry = onRetry,
                )
            }
        }
    }
}

@Composable
private fun PlanSheetHeader(title: Int, onNavigateBack: (() -> Unit)?, onDismiss: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(top = 12.dp, bottom = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (onNavigateBack != null) {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.AutoMirrored.Rounded.ArrowBack, stringResource(R.string.common_back), tint = Ink)
            }
        }
        Text(
            stringResource(title),
            modifier = Modifier.weight(1f),
            color = Ink,
            fontFamily = MaterialTheme.typography.headlineMedium.fontFamily,
            fontSize = 19.sp,
            fontWeight = FontWeight.SemiBold,
        )
        IconButton(onClick = onDismiss) {
            Icon(Icons.Rounded.Close, stringResource(R.string.common_close), tint = MutedInk)
        }
    }
}

@Composable
private fun PlanUnavailableContent(state: PlanUiState, onRetry: () -> Unit) {
    Column(
        Modifier.fillMaxWidth().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (state.loading) {
            CircularProgressIndicator(color = BrandBlue)
        } else {
            Text(stringResource(state.errorRes ?: R.string.error_unknown), color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center)
            OutlinedButton(onClick = onRetry) { Text(stringResource(R.string.common_retry)) }
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
) {
    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp),
    ) {
        item {
            Surface(shape = RoundedCornerShape(24.dp), color = BrandBlue) {
                Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(stringResource(R.string.plan_free_title), style = MaterialTheme.typography.titleMedium, color = Color.White)
                    Text(stringResource(R.string.plan_monthly_analyses), style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.88f))
                    Text(stringResource(R.string.plan_used_of, status.used, status.limit), style = MaterialTheme.typography.headlineMedium, color = Color.White)
                    Box(Modifier.fillMaxWidth().height(7.dp).background(Color.White.copy(alpha = 0.26f), CircleShape)) {
                        if (status.limit > 0) Box(
                            Modifier.fillMaxWidth((status.used.toFloat() / status.limit).coerceIn(0f, 1f))
                                .height(7.dp).background(Color(0xFFFF7474), CircleShape),
                        )
                    }
                    Text(
                        pluralStringResource(R.plurals.plan_remaining_month, status.remaining, status.remaining),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.9f),
                    )
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
                modifier = Modifier.padding(top = 20.dp, bottom = 14.dp),
                color = Ink,
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
                modifier = Modifier.fillMaxWidth().padding(top = 22.dp).heightIn(min = 52.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BrandBlue),
            ) { Text(stringResource(R.string.plan_explore_plus), fontWeight = FontWeight.SemiBold) }
        }
        item {
            Surface(
                onClick = onContinue,
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                shape = RoundedCornerShape(16.dp),
                color = Color.Transparent,
            ) {
                Box(Modifier.fillMaxWidth().heightIn(min = 48.dp), contentAlignment = Alignment.Center) {
                    Text(
                        stringResource(R.string.plan_continue_free),
                        color = BrandBlueDark,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    )
                }
            }
            Text(
                stringResource(R.string.plan_limit_reset),
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 24.dp),
                color = MutedInk,
                fontSize = 12.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            )
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
) {
    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(bottom = 20.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp),
    ) {
        item {
            Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(top = 4.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Surface(shape = RoundedCornerShape(24.dp), color = BrandBlue) {
                    Row(Modifier.fillMaxWidth().padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(stringResource(R.string.plan_plus_tagline), style = MaterialTheme.typography.titleLarge, color = Color.White)
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Surface(Modifier.size(6.dp), CircleShape, Color.White.copy(alpha = 0.9f)) {}
                                Text(stringResource(R.string.plan_plus_benefit_unlimited), style = MaterialTheme.typography.bodySmall, color = Color.White)
                            }
                        }
                        Image(painterResource(R.drawable.cue_owh), contentDescription = null, modifier = Modifier.padding(start = 8.dp).size(72.dp))
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
                    modifier = Modifier.padding(start = 20.dp, top = 18.dp, bottom = 14.dp),
                    color = Ink,
                    fontFamily = MaterialTheme.typography.headlineMedium.fontFamily,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            item {
                val savings = state.yearlySavingsPercent
                Box(Modifier.fillMaxWidth().padding(horizontal = 20.dp)) { PlanOption(
                    title = stringResource(R.string.plan_yearly),
                    price = state.prices[PlanInterval.YEARLY]?.formatted,
                    // "Paling hemat" dan badge hanya bila harga Play memang lebih murah daripada 12 bulan paket bulanan.
                    helper = savings?.let { stringResource(R.string.plan_best_value) },
                    badge = savings?.let { stringResource(R.string.plan_save_badge, NumberFormat.getPercentInstance().format(it / 100.0)) },
                    selected = selectedPlan == PlanInterval.YEARLY,
                    onClick = { onSelectPlan(PlanInterval.YEARLY) },
                ) }
            }
            item { Spacer(Modifier.height(14.dp)) }
            item {
                Box(Modifier.fillMaxWidth().padding(horizontal = 20.dp)) { PlanOption(
                    title = stringResource(R.string.plan_monthly),
                    price = state.prices[PlanInterval.MONTHLY]?.formatted,
                    helper = stringResource(R.string.plan_flexible),
                    selected = selectedPlan == PlanInterval.MONTHLY,
                    onClick = { onSelectPlan(PlanInterval.MONTHLY) },
                ) }
            }
        }
        if (state.errorRes != null) {
            item {
                Column(Modifier.padding(horizontal = 20.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(stringResource(state.errorRes), color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    OutlinedButton(onClick = onRetry) { Text(stringResource(R.string.common_retry)) }
                }
            }
        }
        item {
            if (isPlus) {
                Button(
                    onClick = onManage,
                    modifier = Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, top = 24.dp).heightIn(min = 52.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BrandBlue),
                ) { Text(stringResource(R.string.plan_manage)) }
            } else {
                Button(
                    onClick = onPurchase,
                    enabled = state.purchaseEnabled && selectedPrice != null && !state.purchasing,
                    modifier = Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, top = 24.dp).heightIn(min = 52.dp),
                    shape = RoundedCornerShape(16.dp),
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
                modifier = Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, top = 24.dp, bottom = 20.dp),
                color = MutedInk,
                fontSize = 12.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            )
        }
    }
}

@Composable
private fun PlusStatus(plus: PlanStatus.Plus) {
    val date = formatDate(plus.expiresAt.toEpochMilli())
    Surface(
        modifier = Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, top = 20.dp),
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
                    fontSize = if (price == null) 12.sp else 18.sp, lineHeight = 22.sp, fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 6.dp))
            }
            Column(horizontalAlignment = Alignment.End) {
                if (badge != null) {
                    Surface(shape = CircleShape, color = Color.White) {
                        Text(badge, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), color = BrandBlueDark, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    if (helper != null) Text(helper, color = Color.White, fontSize = 12.sp, modifier = Modifier.padding(top = 6.dp))
                } else if (helper != null) {
                    Text(helper, color = if (selected) Color.White else MutedInk, fontSize = 12.sp)
                }
            }
        }
    }
}
