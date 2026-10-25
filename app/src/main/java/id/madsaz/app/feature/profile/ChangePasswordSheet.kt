package id.madsaz.app.feature.profile

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import id.madsaz.app.R
import id.madsaz.app.ui.components.MadsazTextField
import id.madsaz.app.ui.theme.BrandBlueDark
import id.madsaz.app.ui.theme.Ink
import id.madsaz.app.ui.theme.MutedInk

/** Ubah kata sandi: kata sandi lama wajib, kata sandi baru diketik dua kali. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangePasswordSheet(state: ChangePasswordState, onSubmit: (String, String, String) -> Unit, onDismiss: () -> Unit) {
    val context = LocalContext.current
    var current by remember { mutableStateOf("") }
    var new by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
    val submitting = state is ChangePasswordState.Submitting
    LaunchedEffect(state is ChangePasswordState.Done) {
        if (state is ChangePasswordState.Done) {
            Toast.makeText(context, R.string.password_changed, Toast.LENGTH_SHORT).show()
            onDismiss()
        }
    }
    ModalBottomSheet(
        onDismissRequest = { if (!submitting) onDismiss() },
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = Color.White,
        dragHandle = null,
    ) {
        Column(
            Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp).padding(top = 16.dp, bottom = 24.dp).navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(R.string.profile_password_title), Modifier.weight(1f), color = Ink, style = MaterialTheme.typography.headlineSmall)
                IconButton(onClick = onDismiss, enabled = !submitting) { Icon(Icons.Rounded.Close, stringResource(R.string.common_close), tint = MutedInk) }
            }
            val error = state as? ChangePasswordState.Error
            MadsazTextField(current, { current = it }, stringResource(R.string.password_current), Modifier.testTag("password_current"), enabled = !submitting, secret = true,
                isError = error?.messageRes == R.string.error_current_password, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password))
            MadsazTextField(new, { new = it }, stringResource(R.string.password_new), Modifier.testTag("password_new"), enabled = !submitting, secret = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password))
            Text(stringResource(R.string.auth_password_hint), color = MutedInk, style = MaterialTheme.typography.bodySmall)
            MadsazTextField(confirm, { confirm = it }, stringResource(R.string.auth_field_confirm), Modifier.testTag("password_confirm"), enabled = !submitting, secret = true,
                isError = error?.messageRes == R.string.error_password_mismatch, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password))
            if (error != null) Text(stringResource(error.messageRes), color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            Button(
                onClick = { onSubmit(current, new, confirm) },
                enabled = !submitting,
                modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp).testTag("password_save"),
                colors = ButtonDefaults.buttonColors(containerColor = BrandBlueDark),
            ) {
                if (submitting) CircularProgressIndicator(Modifier.size(22.dp), color = Color.White, strokeWidth = 2.dp)
                else Text(stringResource(R.string.password_save))
            }
        }
    }
}
