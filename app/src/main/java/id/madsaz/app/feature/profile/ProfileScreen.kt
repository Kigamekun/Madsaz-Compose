package id.madsaz.app.feature.profile

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.madsaz.app.R
import id.madsaz.app.data.remote.AccountProfile
import id.madsaz.app.feature.auth.DeleteAccountState
import id.madsaz.app.ui.components.CompactContainer
import id.madsaz.app.ui.components.LightStatusBarIcons
import id.madsaz.app.ui.components.ProfileAvatar
import id.madsaz.app.ui.components.formatStorageSize
import id.madsaz.app.ui.theme.BrandBlue
import id.madsaz.app.ui.theme.BrandBlueDark
import id.madsaz.app.ui.theme.Ink
import id.madsaz.app.ui.theme.MutedInk
import java.io.ByteArrayOutputStream
import java.io.IOException
import kotlin.math.max
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private val HeaderBlue = Color(0xFF3E86AB)
private val CardBorder = Color(0xFFDEEAF2)
private val RowBlue = Color(0xFFE5F4FC)
private val LogoutPink = Color(0xFFFFE5EB)

@Composable
fun ProfileScreen(
    state: ProfileUiState,
    recordingCount: Int?,
    storageBytes: Long?,
    onRefresh: () -> Unit,
    onUpdateName: (String) -> Unit,
    onUploadAvatar: (ByteArray) -> Unit,
    onDismissProfileError: () -> Unit,
    onLogout: () -> Unit,
    deleteState: DeleteAccountState,
    onDeleteAccount: (String) -> Unit,
    onDismissDeleteAccount: () -> Unit,
    onPlan: () -> Unit,
    onNotifications: () -> Unit,
    onPrivacy: () -> Unit,
    onLanguage: () -> Unit,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
) {
    LightStatusBarIcons()
    var editing by remember { mutableStateOf(false) }
    var confirmingDelete by remember { mutableStateOf(false) }
    var photoErrorRes by remember { mutableStateOf<Int?>(null) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    // Photo Picker resmi: tanpa izin penyimpanan, pengguna hanya membagikan foto yang dipilih.
    val photoPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri: Uri? ->
        if (uri != null) scope.launch {
            try {
                val jpeg = withContext(Dispatchers.IO) { compressAvatar(context.contentResolver, uri) }
                photoErrorRes = null
                onUploadAvatar(jpeg)
            } catch (error: PhotoException) {
                photoErrorRes = error.messageRes
            }
        }
    }
    val pickPhoto = { photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }

    Scaffold(
        modifier = modifier,
        containerColor = HeaderBlue,
        contentWindowInsets = WindowInsets.statusBars,
    ) { padding ->
        CompactContainer(Modifier.padding(padding)) {
            Box(Modifier.fillMaxSize().background(HeaderBlue)) {
                Surface(
                    modifier = Modifier.align(Alignment.TopCenter).fillMaxWidth().padding(top = 169.dp).fillMaxSize(),
                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                    color = Color.White,
                ) { }
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 47.dp, bottom = contentPadding.calculateBottomPadding() + 20.dp),
                ) {
                    item {
                        when (state) {
                            ProfileUiState.Loading -> Box(Modifier.fillMaxWidth().height(154.dp), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(color = BrandBlue)
                            }
                            is ProfileUiState.Error -> ProfileErrorCard(stringResource(state.messageRes), onRefresh)
                            is ProfileUiState.Content -> ProfileCard(state.account, state.saving, onEdit = { editing = true }, onPhoto = pickPhoto)
                        }
                    }
                    val shownError = photoErrorRes ?: (state as? ProfileUiState.Content)?.errorRes
                    if (shownError != null && !editing) {
                        item { Text(stringResource(shownError), color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 8.dp)) }
                    }
                    item { Spacer(Modifier.height(24.dp)) }
                    item { Text(stringResource(R.string.profile_account), color = Ink, fontSize = 20.sp, fontWeight = FontWeight.Bold) }
                    item { Spacer(Modifier.height(16.dp)) }
                    item {
                        ProfileRow(
                            stringResource(R.string.profile_badge_account), stringResource(R.string.profile_account_row_title),
                            stringResource(R.string.profile_account_row_subtitle), RowBlue,
                            onClick = { if (state is ProfileUiState.Content) editing = true },
                        )
                    }
                    item { ProfileRow(stringResource(R.string.profile_badge_plus), stringResource(R.string.plan_plus_brand), stringResource(R.string.profile_plus_subtitle), Color.White, onPlan) }
                    item { ProfileRow(stringResource(R.string.profile_badge_notifications), stringResource(R.string.notifications_title), stringResource(R.string.profile_notifications_subtitle), Color.White, onNotifications) }
                    item { ProfileRow(stringResource(R.string.profile_badge_privacy), stringResource(R.string.profile_privacy_title), stringResource(R.string.profile_privacy_subtitle), Color.White, onPrivacy) }
                    item { ProfileRow(stringResource(R.string.profile_badge_language), stringResource(R.string.profile_language_title), stringResource(R.string.profile_language_subtitle), Color.White, onLanguage) }
                    item { ProfileRow(stringResource(R.string.profile_badge_logout), stringResource(R.string.profile_logout_title), stringResource(R.string.profile_logout_subtitle), LogoutPink, onLogout, tag = "profile_logout") }
                    item {
                        ProfileRow(
                            stringResource(R.string.profile_badge_delete), stringResource(R.string.profile_delete_title),
                            stringResource(R.string.profile_delete_subtitle), LogoutPink,
                            onClick = { confirmingDelete = true }, tag = "profile_delete",
                        )
                    }
                    item {
                        if (recordingCount != null && storageBytes != null) {
                            Text(
                                pluralStringResource(R.plurals.profile_usage, recordingCount, recordingCount, formatStorageSize(storageBytes)),
                                color = MutedInk,
                                fontSize = 11.sp,
                                modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                                textAlign = TextAlign.Center,
                            )
                        }
                    }
                }
            }
        }
    }

    if (editing && state is ProfileUiState.Content) {
        // Sheet tetap terbuka (dengan isian pengguna) sampai simpan berhasil; kegagalan tampil di dalam sheet.
        var submitted by remember { mutableStateOf(false) }
        LaunchedEffect(state.saving, state.errorRes) {
            if (submitted && !state.saving) {
                submitted = false
                if (state.errorRes == null) editing = false
            }
        }
        AccountEditSheet(
            account = state.account,
            saving = state.saving,
            errorRes = photoErrorRes ?: state.errorRes,
            onPhoto = pickPhoto,
            onDismiss = {
                onDismissProfileError()
                photoErrorRes = null
                editing = false
            },
        ) { name ->
            submitted = true
            onUpdateName(name)
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
private fun ProfileCard(account: AccountProfile, saving: Boolean, onEdit: () -> Unit, onPhoto: () -> Unit) {
    val changePhoto = stringResource(R.string.profile_change_photo)
    Surface(shape = RoundedCornerShape(24.dp), color = Color.White, border = BorderStroke(1.dp, CardBorder)) {
        Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(R.string.profile_your_profile), Modifier.weight(1f), color = Ink, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                if (saving) CircularProgressIndicator(Modifier.size(18.dp), color = BrandBlue, strokeWidth = 2.dp)
                else Text(stringResource(R.string.profile_edit), Modifier.clip(CircleShape).clickable(role = Role.Button, onClick = onEdit).padding(8.dp), color = BrandBlue)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                ProfileAvatar(
                    account.avatarUrl, account.name,
                    Modifier.size(72.dp).clip(CircleShape).clickable(role = Role.Button, onClickLabel = changePhoto, onClick = onPhoto),
                )
                Column(Modifier.padding(start = 12.dp)) {
                    Text(account.name, color = Ink, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text(account.email, color = MutedInk, fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
private fun ProfileRow(badge: String, title: String, subtitle: String, color: Color, onClick: () -> Unit, tag: String? = null) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp).height(54.dp).then(if (tag != null) Modifier.testTag(tag) else Modifier),
        shape = RoundedCornerShape(16.dp),
        color = color,
        border = BorderStroke(1.dp, CardBorder),
    ) {
        Row(Modifier.fillMaxSize().padding(horizontal = 14.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(badge, color = BrandBlueDark, fontSize = 13.sp, modifier = Modifier.padding(end = 16.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(title, color = Ink, fontSize = 13.sp, lineHeight = 18.sp, fontWeight = FontWeight.SemiBold)
                Text(subtitle, color = MutedInk, fontSize = 11.sp, lineHeight = 15.sp)
            }
            Icon(Icons.Rounded.ChevronRight, null, tint = MutedInk, modifier = Modifier.size(20.dp))
        }
    }
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
private fun AccountEditSheet(
    account: AccountProfile,
    saving: Boolean,
    @StringRes errorRes: Int?,
    onPhoto: () -> Unit,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit,
) {
    var name by remember(account.name) { mutableStateOf(account.name) }
    val changePhoto = stringResource(R.string.profile_change_photo)
    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = Color.White) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(stringResource(R.string.profile_account_row_title), color = Ink, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                ProfileAvatar(
                    account.avatarUrl, account.name,
                    Modifier.size(84.dp).clip(CircleShape).clickable(role = Role.Button, onClickLabel = changePhoto, onClick = onPhoto),
                )
            }
            Text(stringResource(R.string.profile_tap_photo), color = MutedInk, fontSize = 12.sp, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(stringResource(R.string.profile_field_name)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = account.email,
                onValueChange = {},
                label = { Text(stringResource(R.string.profile_field_email)) },
                readOnly = true,
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            if (errorRes != null) {
                Text(stringResource(errorRes), color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }
            Button(
                onClick = { onSave(name) },
                enabled = !saving && name.isNotBlank(),
                modifier = Modifier.fillMaxWidth().height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BrandBlue),
            ) {
                if (saving) CircularProgressIndicator(Modifier.size(22.dp), color = Color.White, strokeWidth = 2.dp)
                else Text(stringResource(R.string.profile_save))
            }
        }
    }
}

/** Foto yang dipilih tidak bisa dipakai sebagai avatar; [messageRes] dijelaskan ke pengguna. */
private class PhotoException(@StringRes val messageRes: Int) : Exception()

// BE menerima avatar sampai 5 MB; sisi terpanjang 1024 px dengan JPEG 82 jauh di bawahnya.
private const val AVATAR_MAX_PX = 1024
private const val AVATAR_JPEG_QUALITY = 82
private const val AVATAR_MAX_BYTES = 5 * 1024 * 1024

// ImageDecoder menerapkan orientasi EXIF sendiri, sehingga foto kamera tidak tampil miring.
private fun compressAvatar(resolver: ContentResolver, uri: Uri): ByteArray {
    val bitmap = try {
        ImageDecoder.decodeBitmap(ImageDecoder.createSource(resolver, uri)) { decoder, info, _ ->
            decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
            val longest = max(info.size.width, info.size.height)
            if (longest > AVATAR_MAX_PX) {
                decoder.setTargetSize(info.size.width * AVATAR_MAX_PX / longest, info.size.height * AVATAR_MAX_PX / longest)
            }
        }
    } catch (_: IOException) {
        throw PhotoException(R.string.profile_photo_unreadable)
    }
    return ByteArrayOutputStream().use { output ->
        bitmap.compress(Bitmap.CompressFormat.JPEG, AVATAR_JPEG_QUALITY, output)
        bitmap.recycle()
        output.toByteArray().also { if (it.size > AVATAR_MAX_BYTES) throw PhotoException(R.string.profile_photo_too_large) }
    }
}
