package id.madsaz.app.feature.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.annotation.StringRes
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.text.format.DateUtils
import java.text.NumberFormat
import java.time.LocalDate
import androidx.compose.material3.ModalBottomSheet
import id.madsaz.app.R
import id.madsaz.app.data.remote.BabyProfile
import id.madsaz.app.feature.auth.DeleteAccountState
import id.madsaz.app.ui.components.CompactContainer
import id.madsaz.app.ui.components.LightStatusBarIcons
import id.madsaz.app.ui.components.formatStorageSize
import id.madsaz.app.ui.theme.BrandBlue
import id.madsaz.app.ui.theme.BrandBlueDark
import id.madsaz.app.ui.theme.Ink
import id.madsaz.app.ui.theme.MutedInk

private val ProfileSurface = Color.White
private val ProfileHeaderBlue = Color(0xFF3E86AB)
private val BabyCard = Color.White
private val RowBlue = Color(0xFFE5F4FC)
private val LogoutPink = Color(0xFFFFE5EB)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    babyState: ProfileUiState,
    recordingCount: Int?,
    storageBytes: Long?,
    lastRecordingAt: Long?,
    onRefresh: () -> Unit,
    onUpdateBaby: (String, LocalDate?, Double?) -> Unit,
    onDismissProfileError: () -> Unit,
    onLogout: () -> Unit,
    deleteState: DeleteAccountState,
    onDeleteAccount: (String) -> Unit,
    onDismissDeleteAccount: () -> Unit,
    onPlan: () -> Unit,
    onNotifications: () -> Unit,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
) {
    LightStatusBarIcons()
    var editing by remember { mutableStateOf(false) }
    var confirmingDelete by remember { mutableStateOf(false) }
    val babyProfileSubtitle = if (babyState is ProfileUiState.Content) {
        stringResource(R.string.profile_baby_row_subtitle, babyState.baby.name)
    } else {
        stringResource(R.string.profile_baby_row_subtitle_generic)
    }

    Scaffold(
        modifier = modifier,
        containerColor = ProfileHeaderBlue,
        contentWindowInsets = WindowInsets.statusBars,
    ) { padding ->
        CompactContainer(Modifier.padding(padding)) {
            Box(Modifier.fillMaxSize().background(ProfileHeaderBlue)) {
                Surface(
                    modifier = Modifier.align(Alignment.TopCenter).fillMaxWidth().padding(top = 169.dp).fillMaxSize(),
                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                    color = ProfileSurface,
                ) { }
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 47.dp, bottom = contentPadding.calculateBottomPadding() + 20.dp),
                    verticalArrangement = Arrangement.spacedBy(0.dp),
                ) {
                    item {
                        when (babyState) {
                            ProfileUiState.Loading -> ProfileLoadingCard()
                            is ProfileUiState.Content -> BabyHeader(
                                baby = babyState.baby,
                                lastRecordingAt = lastRecordingAt,
                                onEdit = { editing = true },
                            )
                            is ProfileUiState.Error -> ProfileErrorCard(stringResource(babyState.messageRes), onRefresh)
                        }
                    }
                    item { Spacer(Modifier.height(18.dp)) }
                    item {
                        Text(stringResource(R.string.profile_account), color = Ink, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    }
                    item { Spacer(Modifier.height(22.dp)) }
                    item { ProfileActionRow(stringResource(R.string.profile_badge_baby), stringResource(R.string.profile_baby_row_title), babyProfileSubtitle, RowBlue, onClick = { if (babyState is ProfileUiState.Content) editing = true }) }
                    item { Spacer(Modifier.height(8.dp)) }
                    item { ProfileActionRow(stringResource(R.string.profile_badge_plus), stringResource(R.string.plan_plus_brand), stringResource(R.string.profile_plus_subtitle), Color.White, onPlan) }
                    item { Spacer(Modifier.height(8.dp)) }
                    item { ProfileActionRow(stringResource(R.string.profile_badge_notifications), stringResource(R.string.notifications_title), stringResource(R.string.profile_notifications_subtitle), Color.White, onNotifications) }
                    item { Spacer(Modifier.height(8.dp)) }
                    item { ProfileComingSoonRow(stringResource(R.string.profile_badge_privacy), stringResource(R.string.profile_privacy_title), stringResource(R.string.profile_privacy_subtitle)) }
                    item { Spacer(Modifier.height(8.dp)) }
                    item { ProfileComingSoonRow(stringResource(R.string.profile_badge_feedback), stringResource(R.string.profile_feedback_title), stringResource(R.string.profile_feedback_subtitle)) }
                    item { Spacer(Modifier.height(8.dp)) }
                    item { ProfileActionRow(stringResource(R.string.profile_badge_logout), stringResource(R.string.profile_logout_title), stringResource(R.string.profile_logout_subtitle), LogoutPink, onClick = onLogout, tag = "profile_logout") }
                    item { Spacer(Modifier.height(8.dp)) }
                    item {
                        ProfileActionRow(
                            stringResource(R.string.profile_badge_delete),
                            stringResource(R.string.profile_delete_title),
                            stringResource(R.string.profile_delete_subtitle),
                            LogoutPink,
                            onClick = { confirmingDelete = true },
                            tag = "profile_delete",
                        )
                    }
                    item { Spacer(Modifier.height(24.dp)) }
                    item {
                        val usage = if (recordingCount != null && storageBytes != null) pluralStringResource(R.plurals.profile_usage, recordingCount, recordingCount, formatStorageSize(storageBytes)) else null
                        if (usage != null) Text(usage, color = MutedInk, fontSize = 11.sp, modifier = Modifier.fillMaxWidth(), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                    }
                }
            }
        }
    }

    if (editing && babyState is ProfileUiState.Content) {
        // Sheet tetap terbuka (dengan isian pengguna) sampai simpan berhasil; kegagalan tampil di dalam sheet.
        var submitted by remember { mutableStateOf(false) }
        LaunchedEffect(babyState.saving, babyState.errorRes) {
            if (submitted && !babyState.saving) {
                submitted = false
                if (babyState.errorRes == null) editing = false
            }
        }
        BabyEditSheet(
            baby = babyState.baby,
            saving = babyState.saving,
            errorRes = babyState.errorRes,
            onDismiss = {
                onDismissProfileError()
                editing = false
            },
        ) { name, date, weight ->
            submitted = true
            onUpdateBaby(name, date, weight)
        }
    }

    if (confirmingDelete) {
        DeleteAccountDialog(
            state = deleteState,
            onConfirm = onDeleteAccount,
            onDismiss = {
                onDismissDeleteAccount()
                confirmingDelete = false
            },
        )
    }
}

@Composable
private fun DeleteAccountDialog(state: DeleteAccountState, onConfirm: (String) -> Unit, onDismiss: () -> Unit) {
    var password by remember { mutableStateOf("") }
    val deleting = state is DeleteAccountState.Deleting
    AlertDialog(
        onDismissRequest = { if (!deleting) onDismiss() },
        title = { Text(stringResource(R.string.profile_delete_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(stringResource(R.string.profile_delete_warning))
                Text(stringResource(R.string.profile_delete_subscription_note), color = MutedInk)
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text(stringResource(R.string.profile_delete_password)) },
                    singleLine = true,
                    enabled = !deleting,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    isError = state is DeleteAccountState.Error,
                    modifier = Modifier.fillMaxWidth().testTag("delete_password"),
                )
                if (state is DeleteAccountState.Error) {
                    Text(stringResource(state.messageRes), color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(password) }, enabled = !deleting && password.isNotEmpty(), modifier = Modifier.testTag("delete_confirm")) {
                if (deleting) CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
                else Text(stringResource(R.string.profile_delete_confirm), color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !deleting) { Text(stringResource(R.string.common_cancel)) }
        },
    )
}

@Composable
private fun BabyHeader(baby: BabyProfile, lastRecordingAt: Long?, onEdit: () -> Unit) {
    Surface(shape = RoundedCornerShape(24.dp), color = BabyCard, border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFDEEAF2))) {
        Box(Modifier.fillMaxWidth().height(154.dp)) {
            Row(
                Modifier.align(Alignment.TopStart).fillMaxWidth().padding(start = 20.dp, end = 18.dp, top = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(stringResource(R.string.profile_your_profile), Modifier.weight(1f), color = Ink, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text(stringResource(R.string.profile_edit), Modifier.clip(CircleShape).clickable(onClick = onEdit).padding(horizontal = 8.dp, vertical = 6.dp), color = BrandBlue, fontSize = 13.sp)
            }
            Row(
                Modifier.align(Alignment.BottomStart).padding(start = 18.dp, end = 16.dp, bottom = 26.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(Modifier.size(72.dp), CircleShape, Color(0xFFE6F4FB)) {
                    Image(painterResource(R.drawable.madsaz_home_photo), null, Modifier.fillMaxSize().padding(5.dp).clip(CircleShape), contentScale = ContentScale.Crop)
                }
                Column(Modifier.padding(start = 12.dp).weight(1f)) {
                    Text(baby.name, color = Ink, fontSize = 21.sp, fontWeight = FontWeight.Bold)
                    Text(babySummary(baby), color = MutedInk, fontSize = 13.sp)
                    Text(
                        lastRecordingAt?.let { stringResource(R.string.profile_last_analyzed, relativeDay(it)) } ?: stringResource(R.string.profile_not_analyzed),
                        color = BrandBlueDark,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(top = 5.dp),
                    )
                }
            }
        }
    }
}

// Teks relatif ("Hari ini", "Kemarin", "3 hari lalu") dari sistem, sudah mengikuti bahasa dan aturan jamaknya.
private fun relativeDay(epochMillis: Long): String =
    DateUtils.getRelativeTimeSpanString(epochMillis, System.currentTimeMillis(), DateUtils.DAY_IN_MILLIS).toString()

@Composable
private fun babySummary(baby: BabyProfile): String {
    val age = baby.birthDate?.let {
        val months = java.time.Period.between(it, LocalDate.now()).toTotalMonths().toInt()
        pluralStringResource(R.plurals.profile_age_months, months, months)
    }
    val weight = baby.weightKg?.let {
        stringResource(R.string.profile_weight_kg, NumberFormat.getInstance().apply { maximumFractionDigits = 2 }.format(it))
    }
    return listOfNotNull(age, weight).joinToString(" · ").ifBlank { stringResource(R.string.profile_baby_row_title) }
}

@Composable
private fun ProfileActionRow(badge: String, title: String, subtitle: String, color: Color, onClick: () -> Unit, tag: String? = null) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(46.dp).then(if (tag != null) Modifier.testTag(tag) else Modifier),
        shape = RoundedCornerShape(16.dp),
        color = color,
        border = androidx.compose.foundation.BorderStroke(1.dp, if (isPressed) BrandBlue else Color(0xFFDEEAF2)),
        interactionSource = interactionSource,
    ) {
        Row(Modifier.fillMaxSize().padding(horizontal = 14.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(Modifier.size(20.dp), CircleShape, Color.White.copy(alpha = 0.7f)) {
                Box(contentAlignment = Alignment.Center) {
                    Text(badge, color = BrandBlueDark, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }
            }
            Column(Modifier.weight(1f).padding(start = 14.dp)) {
                Text(title, color = Ink, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, lineHeight = 16.sp)
                Text(subtitle, color = MutedInk, fontSize = 11.sp, lineHeight = 13.sp)
            }
            Icon(Icons.Rounded.ChevronRight, null, tint = MutedInk, modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
private fun ProfileComingSoonRow(badge: String, title: String, subtitle: String) {
    Surface(
        modifier = Modifier.fillMaxWidth().height(46.dp),
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFDEEAF2)),
    ) {
        Row(Modifier.fillMaxSize().padding(horizontal = 14.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(Modifier.size(20.dp), CircleShape, Color(0xFFF2F8FC)) {
                Box(contentAlignment = Alignment.Center) {
                    Text(badge, color = BrandBlueDark, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }
            }
            Column(Modifier.weight(1f).padding(start = 14.dp)) {
                Text(title, color = Ink, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, lineHeight = 16.sp)
                Text(subtitle, color = MutedInk, fontSize = 11.sp, lineHeight = 13.sp)
            }
            Icon(Icons.Rounded.ChevronRight, null, tint = MutedInk, modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
private fun ProfileLoadingCard() {
    Box(Modifier.fillMaxWidth().height(240.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = BrandBlue) }
}

@Composable
private fun ProfileErrorCard(message: String, onRefresh: () -> Unit) {
    Surface(shape = RoundedCornerShape(20.dp), color = Color.White) {
        Column(Modifier.fillMaxWidth().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(message, color = MaterialTheme.colorScheme.error)
            Button(onClick = onRefresh, colors = ButtonDefaults.buttonColors(containerColor = BrandBlue)) { Text(stringResource(R.string.common_retry)) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BabyEditSheet(
    baby: BabyProfile,
    saving: Boolean,
    @StringRes errorRes: Int?,
    onDismiss: () -> Unit,
    onSave: (String, LocalDate?, Double?) -> Unit,
) {
    var name by remember { mutableStateOf(baby.name) }
    var birthDate by remember { mutableStateOf(baby.birthDate) }
    var weight by remember { mutableStateOf(baby.weightKg?.toString().orEmpty()) }
    var weightInvalid by remember { mutableStateOf(false) }
    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = ProfileSurface) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(stringResource(R.string.profile_baby_row_title), color = Ink, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Text(stringResource(R.string.profile_sheet_subtitle), color = MutedInk, fontSize = 13.sp)
            ProfileInput(name, { name = it }, stringResource(R.string.profile_field_baby_name))
            Text(stringResource(R.string.profile_field_birth_date), color = Ink, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            BirthDatePicker(birthDate) { birthDate = it }
            ProfileInput(weight, { weight = it; weightInvalid = false }, stringResource(R.string.profile_field_weight), KeyboardType.Decimal, isError = weightInvalid)
            val message = if (weightInvalid) R.string.error_weight_invalid else errorRes
            if (message != null) {
                Text(stringResource(message), color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }
            Button(
                onClick = {
                    // Isian berat adalah input form: koma desimal diterima, isian yang bukan angka ditolak (bukan dianggap kosong).
                    val parsed = weight.trim().replace(',', '.')
                    val weightKg = if (parsed.isEmpty()) null else parsed.toDoubleOrNull()
                    if (parsed.isNotEmpty() && weightKg == null) weightInvalid = true else onSave(name, birthDate, weightKg)
                },
                enabled = !saving && name.isNotBlank(),
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4EA0C3)),
            ) {
                if (saving) CircularProgressIndicator(Modifier.size(22.dp), color = Color.White, strokeWidth = 2.dp)
                else Text(stringResource(R.string.profile_save), fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun ProfileInput(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    type: KeyboardType = KeyboardType.Text,
    isError: Boolean = false,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        singleLine = true,
        isError = isError,
        keyboardOptions = KeyboardOptions(keyboardType = type),
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = BrandBlue),
    )
}

/** Posisi awal tiga kolom tanggal. Hanya tampilan: tanggal baru tersimpan setelah pengguna mengubahnya. */
@Composable
private fun BirthDatePicker(value: LocalDate?, onDateChange: (LocalDate) -> Unit) {
    val today = remember { LocalDate.now() }
    val initial = value ?: today
    var day by remember(value) { mutableStateOf(initial.dayOfMonth) }
    var month by remember(value) { mutableStateOf(initial.monthValue) }
    var year by remember(value) { mutableStateOf(initial.year) }
    val maxYear = today.year
    val minYear = maxYear - 20
    val maxMonth = if (year == today.year) today.monthValue else 12
    val maxDay = LocalDate.of(year, month, 1).lengthOfMonth()
        .coerceAtMost(if (year == today.year && month == today.monthValue) today.dayOfMonth else 31)

    fun emitDate(nextDay: Int = day, nextMonth: Int = month, nextYear: Int = year) {
        val safeMonth = nextMonth.coerceAtMost(if (nextYear == today.year) today.monthValue else 12)
        val lastDay = LocalDate.of(nextYear, safeMonth, 1).lengthOfMonth()
            .coerceAtMost(if (nextYear == today.year && safeMonth == today.monthValue) today.dayOfMonth else 31)
        val safeDay = nextDay.coerceAtMost(lastDay)
        day = safeDay
        month = safeMonth
        year = nextYear
        onDateChange(LocalDate.of(nextYear, safeMonth, safeDay))
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        DateValueSelector(day, 1, maxDay, stringResource(R.string.date_unit_day), Modifier.weight(0.8f), { emitDate(nextDay = it) }) { it.toString().padStart(2, '0') }
        DateValueSelector(month, 1, maxMonth, stringResource(R.string.date_unit_month), Modifier.weight(0.8f), { emitDate(nextMonth = it) }) { it.toString().padStart(2, '0') }
        DateValueSelector(year, minYear, maxYear, stringResource(R.string.date_unit_year), Modifier.weight(1.4f), { emitDate(nextYear = it) }) { it.toString() }
    }
}

@Composable
private fun DateValueSelector(
    value: Int,
    min: Int,
    max: Int,
    unit: String,
    modifier: Modifier,
    onValueChange: (Int) -> Unit,
    label: (Int) -> String,
) {
    Surface(
        modifier = modifier.height(56.dp).pointerInput(value, min, max) {
            var dragDistance = 0f
            detectVerticalDragGestures(
                onDragStart = { dragDistance = 0f },
                onDragEnd = {
                    if (dragDistance < -18f && value < max) onValueChange(value + 1)
                    if (dragDistance > 18f && value > min) onValueChange(value - 1)
                },
                onVerticalDrag = { _, amount -> dragDistance += amount },
            )
        },
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF9BAFD0)),
    ) {
        Row(Modifier.fillMaxSize().padding(start = 14.dp, end = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(label(value), color = Ink, fontSize = 20.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(Modifier.size(24.dp).clickable(enabled = value < max) { onValueChange(value + 1) }, contentAlignment = Alignment.Center) {
                    Icon(Icons.Rounded.KeyboardArrowUp, stringResource(R.string.date_increase, unit), tint = BrandBlue, modifier = Modifier.size(19.dp))
                }
                Box(Modifier.size(24.dp).clickable(enabled = value > min) { onValueChange(value - 1) }, contentAlignment = Alignment.Center) {
                    Icon(Icons.Rounded.KeyboardArrowDown, stringResource(R.string.date_decrease, unit), tint = BrandBlue, modifier = Modifier.size(19.dp))
                }
            }
        }
    }
}
