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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import androidx.compose.material3.ModalBottomSheet
import id.madsaz.app.R
import id.madsaz.app.data.remote.BabyProfile
import id.madsaz.app.ui.components.CompactContainer
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
    onUpdateBaby: (String, String?, Double?) -> Unit,
    onLogout: () -> Unit,
    onPlan: () -> Unit,
    onNotifications: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var editing by remember { mutableStateOf(false) }
    val babyProfileSubtitle = if (babyState is ProfileUiState.Content) {
        "Update ${babyState.baby.name}'s details"
    } else {
        "Update baby's details"
    }

    Scaffold(
        modifier = modifier,
        containerColor = ProfileHeaderBlue,
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
                    contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 47.dp, bottom = 130.dp),
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
                            is ProfileUiState.Error -> ProfileErrorCard(babyState.message, onRefresh)
                        }
                    }
                    item { Spacer(Modifier.height(18.dp)) }
                    item {
                        Text("Account", color = Ink, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    }
                    item { Spacer(Modifier.height(22.dp)) }
                    item { ProfileActionRow("B", "Baby profile", babyProfileSubtitle, RowBlue) { if (babyState is ProfileUiState.Content) editing = true } }
                    item { Spacer(Modifier.height(8.dp)) }
                    item { ProfileActionRow("M", "Madsaz Plus", "Manage your plan", Color.White, onPlan) }
                    item { Spacer(Modifier.height(8.dp)) }
                    item { ProfileActionRow("N", "Notifications", "Gentle reminders", Color.White, onNotifications) }
                    item { Spacer(Modifier.height(8.dp)) }
                    item { ProfileComingSoonRow("P", "Privacy & data", "Control your recordings") }
                    item { Spacer(Modifier.height(8.dp)) }
                    item { ProfileComingSoonRow("S", "Share feedback", "Help us improve") }
                    item { Spacer(Modifier.height(8.dp)) }
                    item { ProfileActionRow("L", "Log out", "See you soon", LogoutPink, onLogout) }
                    item { Spacer(Modifier.height(24.dp)) }
                    item {
                        val usage = if (recordingCount != null && storageBytes != null) "${recordingCount} recordings · ${formatStorageSize(storageBytes)}" else null
                        if (usage != null) Text(usage, color = MutedInk, fontSize = 11.sp, modifier = Modifier.fillMaxWidth(), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                    }
                }
            }
        }
    }

    if (editing && babyState is ProfileUiState.Content) {
        BabyEditSheet(baby = babyState.baby, onDismiss = { editing = false }) { name, date, weight ->
            onUpdateBaby(name, date, weight)
            editing = false
        }
    }
}

@Composable
private fun BabyHeader(baby: BabyProfile, lastRecordingAt: Long?, onEdit: () -> Unit) {
    Surface(shape = RoundedCornerShape(24.dp), color = BabyCard, border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFDEEAF2))) {
        Box(Modifier.fillMaxWidth().height(154.dp)) {
            Row(
                Modifier.align(Alignment.TopStart).fillMaxWidth().padding(start = 20.dp, end = 18.dp, top = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Your profile", Modifier.weight(1f), color = Ink, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text("Edit", Modifier.clip(CircleShape).clickable(onClick = onEdit).padding(horizontal = 8.dp, vertical = 6.dp), color = BrandBlue, fontSize = 13.sp)
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
                        lastRecordingAt?.let(::profileRecordingTime) ?: "Not analyzed yet",
                        color = BrandBlueDark,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(top = 5.dp),
                    )
                }
            }
        }
    }
}

private fun profileRecordingTime(epochMillis: Long): String {
    val days = java.time.Duration.between(java.time.Instant.ofEpochMilli(epochMillis), java.time.Instant.now()).toDays()
    return when {
        days < 1 -> "Last analyzed today"
        days == 1L -> "Last analyzed yesterday"
        else -> "Last analyzed $days days ago"
    }
}

private fun babySummary(baby: BabyProfile): String {
    val age = baby.birthDate?.let { date ->
        runCatching {
            val months = java.time.Period.between(java.time.LocalDate.parse(date), java.time.LocalDate.now()).toTotalMonths()
            "$months months"
        }.getOrNull()
    }
    val weight = baby.weightKg?.let { "${it} kg" }
    return listOfNotNull(age, weight).joinToString(" · ").ifBlank { "Baby profile" }
}

@Composable
private fun ProfileActionRow(badge: String, title: String, subtitle: String, color: Color, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(46.dp),
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
            Button(onClick = onRefresh, colors = ButtonDefaults.buttonColors(containerColor = BrandBlue)) { Text("Coba lagi") }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BabyEditSheet(baby: BabyProfile, onDismiss: () -> Unit, onSave: (String, String?, Double?) -> Unit) {
    var name by remember { mutableStateOf(baby.name) }
    var birthDate by remember { mutableStateOf(baby.birthDate.orEmpty()) }
    var weight by remember { mutableStateOf(baby.weightKg?.toString().orEmpty()) }
    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = ProfileSurface) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Baby profile", color = Ink, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Text("Update your baby's details", color = MutedInk, fontSize = 13.sp)
            ProfileInput(name, { name = it }, "Baby name")
            Text("Birth date (dd/mm/yyyy)", color = Ink, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            BirthDatePicker(birthDate) { birthDate = it }
            ProfileInput(weight, { weight = it }, "Weight (kg)", KeyboardType.Decimal)
            Button(
                onClick = { onSave(name, birthDate.ifBlank { null }, weight.toDoubleOrNull()) },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4EA0C3)),
            ) { Text("Save changes", fontWeight = FontWeight.Bold) }
        }
    }
}

@Composable
private fun ProfileInput(value: String, onValueChange: (String) -> Unit, label: String, type: KeyboardType = KeyboardType.Text) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = type),
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = BrandBlue),
    )
}

private data class DateParts(val day: Int, val month: Int, val year: Int)

private fun parseDateParts(value: String): DateParts {
    return runCatching {
        val date = LocalDate.parse(value)
        DateParts(date.dayOfMonth, date.monthValue, date.year)
    }.getOrElse {
        val today = LocalDate.now()
        DateParts(today.dayOfMonth, today.monthValue, today.year)
    }
}

@Composable
private fun BirthDatePicker(value: String, onDateChange: (String) -> Unit) {
    val initial = remember(value) { parseDateParts(value) }
    var day by remember(value) { mutableStateOf(initial.day) }
    var month by remember(value) { mutableStateOf(initial.month) }
    var year by remember(value) { mutableStateOf(initial.year) }
    val today = LocalDate.now()
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
        onDateChange("%04d-%02d-%02d".format(nextYear, safeMonth, safeDay))
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        DateValueSelector(day, 1, maxDay, "hari", Modifier.weight(0.8f), { emitDate(nextDay = it) }) { it.toString().padStart(2, '0') }
        DateValueSelector(month, 1, maxMonth, "bulan", Modifier.weight(0.8f), { emitDate(nextMonth = it) }) { it.toString().padStart(2, '0') }
        DateValueSelector(year, minYear, maxYear, "tahun", Modifier.weight(1.4f), { emitDate(nextYear = it) }) { it.toString() }
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
                    Icon(Icons.Rounded.KeyboardArrowUp, "Tambah $unit", tint = BrandBlue, modifier = Modifier.size(19.dp))
                }
                Box(Modifier.size(24.dp).clickable(enabled = value > min) { onValueChange(value - 1) }, contentAlignment = Alignment.Center) {
                    Icon(Icons.Rounded.KeyboardArrowDown, "Kurangi $unit", tint = BrandBlue, modifier = Modifier.size(19.dp))
                }
            }
        }
    }
}
