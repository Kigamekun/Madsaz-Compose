package id.madsaz.app.feature.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.madsaz.app.ui.components.CompactContainer
import id.madsaz.app.ui.theme.BrandBlue
import id.madsaz.app.ui.theme.BrandBlueDark
import id.madsaz.app.ui.theme.Border
import id.madsaz.app.ui.theme.Ink
import id.madsaz.app.ui.theme.MutedInk

private val AuthBrand = BrandBlueDark

@Composable
fun AuthScreen(
    state: AuthUiState,
    onSubmit: (Boolean, String, String, String) -> Unit,
    onForgotPassword: () -> Unit,
) {
    var register by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val submitting = state is AuthUiState.Submitting

    CompactContainer {
        Box(Modifier.fillMaxSize().background(AuthBrand)) {
            Column(
                modifier = Modifier.align(Alignment.TopStart).padding(start = 20.dp, top = 118.dp, end = 20.dp),
                verticalArrangement = Arrangement.spacedBy(7.dp),
            ) {
                Text("madsaz", color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.Bold)
                Text(
                    if (register) "A calmer way to begin" else "A calmer way to listen",
                    color = Color.White,
                    style = MaterialTheme.typography.headlineMedium,
                )
                Text(
                    if (register) "Create an account to keep your baby's latest cues."
                    else "Pick up where you left off with Pandu's latest cues.",
                    color = Color.White.copy(alpha = 0.88f),
                    style = MaterialTheme.typography.bodySmall,
                )
            }

            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(if (register) 517.dp else 511.dp)
                    .clip(RoundedCornerShape(topStart = 26.dp, topEnd = 26.dp))
                    .background(Color.White)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
                    .padding(top = if (register) 31.dp else 32.dp, bottom = 16.dp),
            ) {
                Text(
                    if (register) "Register" else "Log in",
                    color = Ink,
                    fontFamily = MaterialTheme.typography.headlineMedium.fontFamily,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    "Your recordings stay private to your account.",
                    modifier = Modifier.padding(top = 5.dp),
                    color = MutedInk,
                    style = MaterialTheme.typography.bodySmall,
                )
                Spacer(Modifier.height(18.dp))

                if (register) {
                    AuthField(name, { name = it }, "Name")
                    Spacer(Modifier.height(10.dp))
                }
                AuthField(email, { email = it }, "Email address", KeyboardType.Email)
                Spacer(Modifier.height(10.dp))
                AuthField(password, { password = it }, "Password", KeyboardType.Password, secret = true)

                if (state is AuthUiState.Error) {
                    Text(
                        state.message,
                        modifier = Modifier.padding(top = 10.dp),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }

                if (!register) {
                    TextButton(
                        onClick = onForgotPassword,
                        modifier = Modifier.align(Alignment.End).height(44.dp),
                    ) {
                        Text("Forgot password?", color = BrandBlueDark, style = MaterialTheme.typography.labelMedium)
                    }
                } else {
                    Spacer(Modifier.height(26.dp))
                }

                Button(
                    onClick = { onSubmit(register, name, email, password) },
                    enabled = !submitting,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BrandBlue),
                ) {
                    if (submitting) CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp)
                    else Text(if (register) "Create account" else "Log in", color = Color.White)
                }

                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 13.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        if (register) "Already have an account?" else "New to Madsaz?",
                        color = MutedInk,
                        style = MaterialTheme.typography.bodySmall,
                    )
                    Spacer(Modifier.width(4.dp))
                    TextButton(onClick = { register = !register }, enabled = !submitting) {
                        Text(
                            if (register) "Log in" else "Create an account",
                            color = BrandBlueDark,
                            style = MaterialTheme.typography.labelMedium,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AuthField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    secret: Boolean = false,
) {
    var visible by remember { mutableStateOf(false) }
    var focused by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxWidth().height(50.dp),
        shape = RoundedCornerShape(18.dp),
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, if (focused) BrandBlue else Border),
    ) {
        Row(
            modifier = Modifier.padding(start = 16.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(Modifier.weight(1f)) {
                if (value.isEmpty()) {
                    Text(label, color = MutedInk, style = MaterialTheme.typography.bodySmall)
                }
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    modifier = Modifier.fillMaxWidth().onFocusChanged { focused = it.isFocused },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                    textStyle = MaterialTheme.typography.bodySmall.copy(color = Ink),
                    cursorBrush = SolidColor(BrandBlue),
                    visualTransformation = if (secret && !visible) PasswordVisualTransformation() else VisualTransformation.None,
                )
            }
            if (secret) {
                IconButton(onClick = { visible = !visible }, modifier = Modifier.size(42.dp)) {
                    Icon(
                        imageVector = if (visible) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                        contentDescription = if (visible) "Hide password" else "Show password",
                        tint = MutedInk,
                        modifier = Modifier.size(19.dp),
                    )
                }
            }
        }
    }
}
