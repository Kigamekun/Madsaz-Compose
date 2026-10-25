package id.madsaz.app.feature.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import id.madsaz.app.R
import id.madsaz.app.ui.components.CompactContainer
import id.madsaz.app.ui.components.MadsazTextField
import id.madsaz.app.ui.theme.BrandBlueDark
import id.madsaz.app.ui.theme.Ink
import id.madsaz.app.ui.theme.MutedInk

/** Email -> BE mengirim tautan -> pengguna membuat kata sandi baru di halaman web BE. Jawaban sukses sama untuk email apa pun. */
@Composable
fun ForgotPasswordScreen(state: ForgotPasswordState, onSend: (String) -> Unit, onBack: () -> Unit) {
    var email by rememberSaveable { mutableStateOf("") }
    val submitting = state is ForgotPasswordState.Submitting
    CompactContainer {
        Column(
            Modifier.fillMaxSize().background(Color.White).statusBarsPadding().imePadding().verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp).navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            IconButton(onClick = onBack, modifier = Modifier.testTag("forgot_back")) {
                Icon(Icons.AutoMirrored.Rounded.ArrowBack, stringResource(R.string.common_back), tint = Ink)
            }
            if (state is ForgotPasswordState.Sent) {
                Icon(Icons.Rounded.Email, null, tint = BrandBlueDark, modifier = Modifier.size(40.dp))
                Text(stringResource(R.string.forgot_sent_title), color = Ink, style = MaterialTheme.typography.headlineMedium, modifier = Modifier.testTag("forgot_sent"))
                Text(stringResource(R.string.forgot_sent_body), color = MutedInk, style = MaterialTheme.typography.bodyMedium)
                Button(
                    onClick = onBack,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BrandBlueDark),
                ) { Text(stringResource(R.string.forgot_back)) }
            } else {
                Text(stringResource(R.string.forgot_title), color = Ink, style = MaterialTheme.typography.headlineMedium)
                Text(stringResource(R.string.forgot_body), color = MutedInk, style = MaterialTheme.typography.bodyMedium)
                MadsazTextField(
                    email,
                    { email = it },
                    stringResource(R.string.auth_field_email),
                    Modifier.testTag("forgot_email"),
                    enabled = !submitting,
                    isError = state is ForgotPasswordState.Error,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { onSend(email) }),
                    placeholder = stringResource(R.string.auth_placeholder_email),
                    leadingIcon = Icons.Rounded.Email,
                )
                if (state is ForgotPasswordState.Error) {
                    Text(stringResource(state.messageRes), color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
                Button(
                    onClick = { onSend(email) },
                    enabled = !submitting,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp).testTag("forgot_send"),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BrandBlueDark),
                ) {
                    if (submitting) CircularProgressIndicator(Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                    else Text(stringResource(R.string.forgot_send))
                }
            }
        }
    }
}
