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
import id.madsaz.app.R
import id.madsaz.app.ui.components.CompactContainer
import id.madsaz.app.ui.components.LightStatusBarIcons
import id.madsaz.app.ui.components.MadsazTextField
import id.madsaz.app.ui.theme.BrandBlueDark
import id.madsaz.app.ui.theme.BrandBlue
import id.madsaz.app.ui.theme.Ink
import id.madsaz.app.ui.theme.MutedInk

@Composable
fun AuthScreen(state: AuthUiState, onSubmit: (Boolean, String, String, String) -> Unit, onForgotPassword: () -> Unit) {
    var register by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val submitting = state is AuthUiState.Submitting
    val focus = LocalFocusManager.current
    val submit = { if (!submitting) { focus.clearFocus(); onSubmit(register, name, email, password) } }

    LightStatusBarIcons()
    CompactContainer {
        BoxWithConstraints(Modifier.fillMaxSize().background(Color(0xFFEFF8FC)).imePadding()) {
            val heroHeight = maxHeight * 0.42f
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
                    Surface(shape = RoundedCornerShape(14.dp), color = Color.White, modifier = Modifier.size(44.dp)) {
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
                }
                Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        stringResource(if (register) R.string.auth_headline_register else R.string.auth_headline_login),
                        color = Color.White,
                        fontFamily = MaterialTheme.typography.headlineMedium.fontFamily,
                        fontSize = 24.sp,
                        lineHeight = 30.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        stringResource(if (register) R.string.auth_sub_register else R.string.auth_sub_login),
                        color = Color.White,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 14.sp,
                            lineHeight = 21.sp,
                            fontWeight = FontWeight.Medium,
                        ),
                    )
                }
            }

            Column(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .fillMaxHeight()
                    // Kartu berhenti di atas bilah navigasi sistem (3 tombol) agar baris "sudah punya akun" tidak tertimpa.
                    .navigationBarsPadding()
                    .padding(top = heroHeight - 20.dp, bottom = 12.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .background(Color.White)
                    // Selalu bisa digulir: mode daftar (tiga kolom) lebih tinggi dari kartu di layar pendek, dan tanpa keyboard tombol kirim terpotong.
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
                    .padding(top = 25.dp, bottom = 20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    stringResource(if (register) R.string.auth_register_title else R.string.auth_login_title),
                    color = Ink,
                    fontFamily = MaterialTheme.typography.headlineMedium.fontFamily,
                    fontSize = 30.sp,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(stringResource(R.string.auth_privacy_note), color = MutedInk, style = MaterialTheme.typography.bodyMedium)

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
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { submit() }),
                    placeholder = stringResource(R.string.auth_placeholder_password),
                    leadingIcon = Icons.Rounded.Lock,
                )
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
                Row(Modifier.fillMaxWidth().padding(top = 2.dp), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                    Text(stringResource(if (register) R.string.auth_prompt_have_account else R.string.auth_prompt_new), color = MutedInk, style = MaterialTheme.typography.bodySmall)
                    TextButton(onClick = { register = !register }, enabled = !submitting, modifier = Modifier.testTag("auth_toggle")) {
                        Text(stringResource(if (register) R.string.auth_login_title else R.string.auth_switch_to_register), color = BrandBlueDark)
                    }
                }
            }
        }
    }
}
