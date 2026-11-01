package id.madsaz.app.feature.auth

import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import id.madsaz.app.data.local.AppLanguage
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.foundation.clickable
import androidx.compose.ui.semantics.Role
import id.madsaz.app.R
import id.madsaz.app.ui.components.CompactContainer
import id.madsaz.app.ui.components.LightStatusBarIcons
import id.madsaz.app.ui.components.MadsazTextField
import id.madsaz.app.ui.theme.BrandBlueDark
import id.madsaz.app.ui.theme.BrandBlue
import id.madsaz.app.ui.theme.Ink
import id.madsaz.app.ui.theme.MutedInk

/** Kartu menimpa tepi bawah hero sebesar ini. */
private val CardOverlap = 20.dp
private val MinHeroHeight = 150.dp

/** Logo dan tombol bahasa di header hero sama besar, seperti logo di header Beranda. */
private val HeaderControlSize = 40.dp

@Composable
fun AuthScreen(
    state: AuthUiState,
    startInRegister: Boolean,
    onSubmit: (register: Boolean, name: String, email: String, password: String, confirmPassword: String, acceptTerms: Boolean) -> Unit,
    onForgotPassword: () -> Unit,
    onTryFree: () -> Unit,
    onOpenTerms: () -> Unit,
    onOpenPrivacy: () -> Unit,
    onLanguage: () -> Unit,
    guestDailyLimit: Int? = null,
) {
    var register by rememberSaveable(startInRegister) { mutableStateOf(startInRegister) }
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var acceptTerms by remember { mutableStateOf(false) }
    var showGuestInfo by remember { mutableStateOf(false) }
    // Tinggi isi kartu per mode (px), diukur setelah tata letak; nilai awal 0 membuat hero maksimal sampai pengukuran pertama.
    var cardContentPx by remember(register) { mutableIntStateOf(0) }
    val submitting = state is AuthUiState.Submitting
    val language = LocalConfiguration.current.locales[0].language
    val languageDescription = stringResource(R.string.language_title)
    val focus = LocalFocusManager.current
    val submit = { if (!submitting) { focus.clearFocus(); onSubmit(register, name, email, password, confirmPassword, acceptTerms) } }

    LightStatusBarIcons()
    CompactContainer {
        BoxWithConstraints(Modifier.fillMaxSize().background(Color(0xFFEFF8FC)).imePadding()) {
            // Tinggi hero = sisa layar di atas kartu: kartu pas mengisi layar (tanpa sisa kosong di bawah dan tanpa digulir di layar
            // umum). Bila formulir lebih tinggi dari itu (layar pendek, keyboard, huruf besar), hero berhenti mengecil dan kartu digulir.
            val density = LocalDensity.current
            val heroHeight = (maxHeight - with(density) { cardContentPx.toDp() } + CardOverlap).coerceIn(MinHeroHeight, maxHeight * 0.45f)
            Image(
                painter = painterResource(R.drawable.madsaz_login_regis_v5),
                contentDescription = null,
                modifier = Modifier.fillMaxWidth().height(heroHeight),
                contentScale = ContentScale.Crop,
            )
            Box(
                Modifier.fillMaxWidth().height(heroHeight)
                    .background(Brush.horizontalGradient(0f to BrandBlueDark.copy(alpha = 0.32f), 0.7f to BrandBlueDark.copy(alpha = 0.48f), 1f to Color.Transparent)),
            )
            Column(
                modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(start = 20.dp, top = 16.dp, end = 20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Row(Modifier.fillMaxWidth().heightIn(min = 44.dp), verticalAlignment = Alignment.CenterVertically) {
                    Surface(shape = RoundedCornerShape(13.dp), color = Color.White, modifier = Modifier.size(HeaderControlSize)) {
                        Image(painterResource(R.drawable.ic_launcher_foreground), null, Modifier.fillMaxSize().scale(1.55f))
                    }
                    Text(
                        stringResource(R.string.auth_brand),
                        modifier = Modifier.padding(start = 10.dp).weight(1f),
                        color = Color.White,
                        fontFamily = MaterialTheme.typography.headlineMedium.fontFamily,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                    )
                    // Bahasa bisa diganti sebelum masuk; layar ini dan dokumen S&K/privasi langsung mengikuti.
                    // Area sentuh 48dp (standar aksesibilitas) berisi lingkaran visual seukuran logo.
                    Box(
                        Modifier.size(48.dp).clip(CircleShape).clickable(role = Role.Button, onClick = onLanguage).testTag("auth_language"),
                        contentAlignment = Alignment.Center,
                    ) {
                        Surface(shape = CircleShape, color = Color.White, modifier = Modifier.size(HeaderControlSize)) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    AppLanguage.flag(language),
                                    fontSize = 24.sp,
                                    lineHeight = 28.sp,
                                    modifier = Modifier.semantics { contentDescription = languageDescription },
                                )
                            }
                        }
                    }
                }
            }

            Column(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .fillMaxHeight()
                    // Kartu menempel ke tepi bawah layar seperti sheet; hanya sudut atas yang membulat.
                    .padding(top = heroHeight - CardOverlap)
                    .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                    .background(Color.White)
                    // Selalu bisa digulir: mode daftar lebih tinggi dari kartu di layar pendek, dan tanpa keyboard tombol kirim terpotong.
                    .verticalScroll(rememberScrollState())
                    // Diukur sebelum padding: tinggi seluruh isi kartu termasuk padding dan inset bilah navigasi.
                    .onSizeChanged { cardContentPx = it.height }
                    .padding(horizontal = 20.dp)
                    .padding(top = 25.dp, bottom = 20.dp)
                    // Ruang bawah untuk bilah navigasi sistem ada di dalam kartu, bukan celah di bawahnya.
                    .navigationBarsPadding(),
                verticalArrangement = Arrangement.spacedBy(if (register) 8.dp else 12.dp),
            ) {
                Text(
                    stringResource(if (register) R.string.auth_register_title else R.string.auth_login_title),
                    color = Ink,
                    fontFamily = MaterialTheme.typography.headlineMedium.fontFamily,
                    fontSize = 30.sp,
                    fontWeight = FontWeight.SemiBold,
                )
                if (!register) Text(stringResource(R.string.auth_privacy_note), color = MutedInk, style = MaterialTheme.typography.bodyMedium)

                if (register) MadsazTextField(
                    name,
                    { name = it },
                    stringResource(R.string.auth_field_name),
                    Modifier.testTag("auth_name"),
                    enabled = !submitting,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { focus.moveFocus(FocusDirection.Next) }),
                    placeholder = stringResource(R.string.auth_placeholder_name),
                    leadingIcon = Icons.Rounded.Person,
                )
                MadsazTextField(
                    email,
                    { email = it },
                    stringResource(R.string.auth_field_email),
                    Modifier.testTag("auth_email"),
                    enabled = !submitting,
                    isError = state is AuthUiState.Error,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { focus.moveFocus(FocusDirection.Next) }),
                    placeholder = stringResource(R.string.auth_placeholder_email),
                    leadingIcon = Icons.Rounded.Email,
                )
                MadsazTextField(
                    password,
                    { password = it },
                    stringResource(R.string.auth_field_password),
                    Modifier.testTag("auth_password"),
                    enabled = !submitting,
                    isError = state is AuthUiState.Error,
                    secret = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = if (register) ImeAction.Next else ImeAction.Done),
                    keyboardActions = KeyboardActions(onNext = { focus.moveFocus(FocusDirection.Next) }, onDone = { submit() }),
                    placeholder = stringResource(R.string.auth_placeholder_password),
                    leadingIcon = Icons.Rounded.Lock,
                )
                if (register) {
                    MadsazTextField(
                        confirmPassword,
                        { confirmPassword = it },
                        stringResource(R.string.auth_field_confirm),
                        Modifier.testTag("auth_confirm"),
                        enabled = !submitting,
                        isError = state is AuthUiState.Error && state.messageRes == R.string.error_password_mismatch,
                        secret = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { submit() }),
                        placeholder = stringResource(R.string.auth_placeholder_confirm),
                        leadingIcon = Icons.Rounded.Lock,
                    )
                    TermsAgreement(acceptTerms, { acceptTerms = it }, !submitting, onOpenTerms, onOpenPrivacy)
                }
                if (state is AuthUiState.Error) {
                    Text(stringResource(state.messageRes), color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
                if (!register) {
                    TextButton(onClick = onForgotPassword, modifier = Modifier.align(Alignment.End).heightIn(min = 44.dp)) {
                        Text(stringResource(R.string.auth_forgot), color = BrandBlueDark)
                    }
                }
                Button(
                    onClick = submit,
                    enabled = !submitting,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp).testTag("auth_submit"),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BrandBlueDark),
                ) {
                    if (submitting) CircularProgressIndicator(Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                    else Text(stringResource(if (register) R.string.auth_submit_register else R.string.auth_submit_login))
                }
                if (!register) OutlinedButton(
                    onClick = { showGuestInfo = true },
                    enabled = !submitting,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp).testTag("auth_try_free"),
                    shape = RoundedCornerShape(18.dp),
                    border = BorderStroke(1.dp, BrandBlueDark),
                ) {
                    Text(
                        if (guestDailyLimit == null) stringResource(R.string.auth_try_free)
                        else stringResource(R.string.auth_try_free_daily, guestDailyLimit),
                        color = BrandBlueDark,
                    )
                }
                Row(Modifier.fillMaxWidth().padding(top = 2.dp), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                    Text(stringResource(if (register) R.string.auth_prompt_have_account else R.string.auth_prompt_new), color = MutedInk, style = MaterialTheme.typography.bodySmall)
                    TextButton(onClick = { register = !register }, enabled = !submitting, modifier = Modifier.testTag("auth_toggle")) {
                        Text(stringResource(if (register) R.string.auth_login_title else R.string.auth_switch_to_register), color = BrandBlueDark)
                    }
                }
            }
        }
    }
    if (showGuestInfo) AlertDialog(
        onDismissRequest = { showGuestInfo = false },
        title = { Text(stringResource(R.string.guest_info_title)) },
        text = { Text(
            stringResource(R.string.guest_info_body) +
                (guestDailyLimit?.let { " " + stringResource(R.string.guest_info_daily_limit, it) } ?: ""),
        ) },
        confirmButton = {
            TextButton(onClick = { showGuestInfo = false; onTryFree() }, modifier = Modifier.testTag("guest_info_start")) {
                Text(stringResource(R.string.guest_info_start), color = BrandBlueDark)
            }
        },
        dismissButton = { TextButton(onClick = { showGuestInfo = false }) { Text(stringResource(R.string.common_cancel)) } },
    )
}

/** Persetujuan wajib saat mendaftar: tautan membuka dokumen penuh tanpa kehilangan isian formulir. */
@Composable
private fun TermsAgreement(
    accepted: Boolean,
    onChange: (Boolean) -> Unit,
    enabled: Boolean,
    onOpenTerms: () -> Unit,
    onOpenPrivacy: () -> Unit,
) {
    val linkStyle = SpanStyle(color = BrandBlueDark, fontWeight = FontWeight.SemiBold, textDecoration = TextDecoration.Underline)
    val termsLabel = stringResource(R.string.auth_terms_link)
    val privacyLabel = stringResource(R.string.profile_privacy_title)
    val template = stringResource(R.string.auth_terms_agree)
    // %1$s = S&K dan %2$s = kebijakan privasi; urutannya mengikuti terjemahan masing-masing bahasa.
    val text = buildAnnotatedString {
        var cursor = 0
        Regex("%([12])[$]s").findAll(template).forEach { match ->
            append(template.substring(cursor, match.range.first))
            val terms = match.groupValues[1] == "1"
            withLink(LinkAnnotation.Clickable("terms-or-privacy", TextLinkStyles(linkStyle)) { if (terms) onOpenTerms() else onOpenPrivacy() }) {
                append(if (terms) termsLabel else privacyLabel)
            }
            cursor = match.range.last + 1
        }
        append(template.substring(cursor))
    }
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Checkbox(accepted, onChange, enabled = enabled, modifier = Modifier.testTag("auth_terms"))
        Text(text, color = Ink, style = MaterialTheme.typography.bodySmall)
    }
}

