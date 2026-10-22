package id.madsaz.app.feature.profile

import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.os.Build
import android.graphics.ImageDecoder
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.exifinterface.media.ExifInterface
import androidx.compose.foundation.clickable
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.NotificationsNone
import androidx.compose.material.icons.rounded.PrivacyTip
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.WorkspacePremium
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.rememberModalBottomSheetState
import id.madsaz.app.ui.components.MadsazTextField
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.madsaz.app.R
import id.madsaz.app.data.remote.AccountProfile
import id.madsaz.app.feature.auth.DeleteAccountState
import id.madsaz.app.ui.components.CompactContainer
import id.madsaz.app.ui.components.ProfileAvatar
import id.madsaz.app.ui.components.formatStorageSize
import id.madsaz.app.ui.theme.BrandBlueDark
import id.madsaz.app.ui.theme.Ink
import id.madsaz.app.ui.theme.MutedInk
import java.io.ByteArrayOutputStream
import java.io.IOException
import kotlin.math.max
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private val CardBorder = Color(0xFFDEEAF2)

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
    loggingOut: Boolean = false,
    modifier: Modifier = Modifier,
) {
    var editing by remember { mutableStateOf(false) }
    var confirmingDelete by remember { mutableStateOf(false) }
    var photoErrorRes by remember { mutableStateOf<Int?>(null) }
    var photoBusy by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    // Photo Picker resmi: tanpa izin penyimpanan, pengguna hanya membagikan foto yang dipilih.
    val photoPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri: Uri? ->
        if (uri == null) {
            photoBusy = false
        } else scope.launch {
            try {
                val jpeg = withContext(Dispatchers.IO) { compressAvatar(context.contentResolver, uri) }
                photoErrorRes = null
                onUploadAvatar(jpeg)
            } catch (error: PhotoException) {
                photoErrorRes = error.messageRes
            } finally {
                photoBusy = false
            }
        }
    }
    val pickPhoto: () -> Unit = {
        if (!photoBusy && (state as? ProfileUiState.Content)?.saving != true) {
            photoBusy = true
            photoErrorRes = null
            photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }
    }

    Scaffold(modifier = modifier, containerColor = Color.White, contentWindowInsets = WindowInsets.statusBars) { padding ->
        CompactContainer(Modifier.padding(padding).consumeWindowInsets(padding)) {
            LazyColumn(
                modifier = Modifier.fillMaxSize().testTag("profile_screen"),
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 20.dp, bottom = contentPadding.calculateBottomPadding() + 20.dp),
            ) {
                item { Text(stringResource(R.string.profile_your_profile), style = MaterialTheme.typography.headlineMedium, color = Ink) }
                item {
                    when (state) {
                        ProfileUiState.Loading -> Box(Modifier.fillMaxWidth().height(96.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                        is ProfileUiState.Error -> ProfileErrorCard(stringResource(state.messageRes), onRefresh)
                        is ProfileUiState.Content -> ProfileCard(state.account, state.saving || photoBusy, onEdit = { editing = true }, onPhoto = pickPhoto)
                    }
                }
                val shownError = photoErrorRes ?: (state as? ProfileUiState.Content)?.errorRes
                if (shownError != null && !editing) item { Text(stringResource(shownError), color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }
                item { Text(stringResource(R.string.profile_account), Modifier.padding(top = 20.dp, bottom = 8.dp), style = MaterialTheme.typography.labelLarge, color = MutedInk) }
                item { ProfileRow(Icons.Rounded.WorkspacePremium, stringResource(R.string.plan_plus_brand), stringResource(R.string.profile_plus_subtitle), onPlan) }
                item { ProfileRow(Icons.Rounded.NotificationsNone, stringResource(R.string.notifications_title), stringResource(R.string.profile_notifications_subtitle), onNotifications) }
                item { ProfileRow(Icons.Rounded.PrivacyTip, stringResource(R.string.profile_privacy_title), stringResource(R.string.profile_privacy_subtitle), onPrivacy) }
                item { ProfileRow(Icons.Rounded.Language, stringResource(R.string.profile_language_title), stringResource(R.string.profile_language_subtitle), onLanguage) }
                item { Spacer(Modifier.height(16.dp)) }
                item {
                    ProfileRow(
                        Icons.AutoMirrored.Rounded.Logout,
                        stringResource(R.string.profile_logout_title),
                        stringResource(R.string.profile_logout_subtitle),
                        onLogout,
                        tag = "profile_logout",
                        enabled = !loggingOut,
                        loading = loggingOut,
                    )
                }
                item { ProfileRow(Icons.Rounded.DeleteOutline, stringResource(R.string.profile_delete_title), stringResource(R.string.profile_delete_subtitle), { confirmingDelete = true }, destructive = true, tag = "profile_delete") }
                item {
                    if (recordingCount != null && storageBytes != null) Text(
                        pluralStringResource(R.plurals.profile_usage, recordingCount, recordingCount, formatStorageSize(storageBytes)),
                        modifier = Modifier.fillMaxWidth().padding(top = 20.dp), style = MaterialTheme.typography.bodySmall, color = MutedInk, textAlign = TextAlign.Center,
                    )
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
            saving = state.saving || photoBusy,
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
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(stringResource(R.string.profile_delete_warning))
                Text(stringResource(R.string.profile_delete_subscription_note), color = MutedInk)
                MadsazTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = stringResource(R.string.profile_delete_password),
                    singleLine = true,
                    enabled = !deleting,
                    secret = true,
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
    Row(Modifier.fillMaxWidth().padding(vertical = 20.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        ProfileAvatar(account.avatarUrl, account.name, Modifier.size(56.dp).clip(CircleShape).clickable(enabled = !saving, role = Role.Button, onClickLabel = changePhoto, onClick = onPhoto))
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(account.name, style = MaterialTheme.typography.titleLarge, color = Ink)
            Text(account.email, style = MaterialTheme.typography.bodySmall, color = MutedInk)
        }
        if (saving) CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
        else TextButton(onClick = onEdit, modifier = Modifier.testTag("profile_edit")) { Text(stringResource(R.string.profile_edit)) }
    }
}

@Composable
private fun ProfileRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    destructive: Boolean = false,
    tag: String? = null,
    enabled: Boolean = true,
    loading: Boolean = false,
) {
    val accent = if (destructive) MaterialTheme.colorScheme.error else BrandBlueDark
    Column {
        Surface(onClick = onClick, enabled = enabled, color = Color.White, modifier = Modifier.fillMaxWidth().heightIn(min = 64.dp).then(if (tag != null) Modifier.testTag(tag) else Modifier)) {
            Row(Modifier.padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Icon(icon, null, Modifier.size(22.dp), tint = accent)
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(title, style = MaterialTheme.typography.titleMedium, color = if (destructive) accent else Ink)
                    Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MutedInk)
                }
                if (loading) CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp, color = accent)
                else Icon(Icons.AutoMirrored.Rounded.ArrowForward, null, Modifier.size(20.dp), tint = MutedInk)
            }
        }
        HorizontalDivider(color = CardBorder)
    }
}

@Composable
private fun ProfileErrorCard(message: String, onRefresh: () -> Unit) {
    Surface(shape = RoundedCornerShape(20.dp), color = Color.White) {
        Column(Modifier.fillMaxWidth().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(message, color = MaterialTheme.colorScheme.error)
            Button(onClick = onRefresh, colors = ButtonDefaults.buttonColors(containerColor = BrandBlueDark)) { Text(stringResource(R.string.common_retry)) }
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
    ModalBottomSheet(onDismissRequest = { if (!saving) onDismiss() }, sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true), containerColor = Color.White, dragHandle = null) {
        Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp).padding(top = 16.dp, bottom = 24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(R.string.profile_account_row_title), Modifier.weight(1f), color = Ink, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                IconButton(onClick = onDismiss, enabled = !saving) {
                    Icon(Icons.Rounded.Close, stringResource(R.string.common_close), tint = MutedInk)
                }
            }
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                ProfileAvatar(
                    account.avatarUrl, account.name,
                    Modifier.size(56.dp).clip(CircleShape).clickable(enabled = !saving, role = Role.Button, onClickLabel = changePhoto, onClick = onPhoto),
                )
                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(account.name, color = Ink, style = MaterialTheme.typography.titleMedium)
                    Text(stringResource(R.string.profile_tap_photo), color = MutedInk, fontSize = 12.sp)
                }
            }
            MadsazTextField(
                value = name,
                onValueChange = { name = it },
                label = stringResource(R.string.profile_field_name),
                enabled = !saving,
                isError = errorRes != null,
                singleLine = true,
                modifier = Modifier.fillMaxWidth().testTag("profile_name"),
            )
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(stringResource(R.string.profile_field_email), style = MaterialTheme.typography.labelLarge, color = Ink)
                Surface(Modifier.fillMaxWidth(), RoundedCornerShape(14.dp), Color.White, border = BorderStroke(1.dp, CardBorder)) {
                    Box(Modifier.fillMaxWidth().heightIn(min = 56.dp).padding(horizontal = 14.dp, vertical = 12.dp), contentAlignment = Alignment.CenterStart) {
                        SelectionContainer { Text(account.email, style = MaterialTheme.typography.bodyLarge, color = MutedInk) }
                    }
                }
            }
            if (errorRes != null) {
                Text(stringResource(errorRes), color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }
            Button(
                onClick = { onSave(name) },
                enabled = !saving && name.isNotBlank(),
                modifier = Modifier.fillMaxWidth().padding(top = 2.dp).heightIn(min = 52.dp).testTag("profile_save"),
                colors = ButtonDefaults.buttonColors(containerColor = BrandBlueDark),
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

private fun compressAvatar(resolver: ContentResolver, uri: Uri): ByteArray {
    val bitmap = try {
        decodeAvatar(resolver, uri)
    } catch (_: IOException) {
        throw PhotoException(R.string.profile_photo_unreadable)
    }
    return ByteArrayOutputStream().use { output ->
        bitmap.compress(Bitmap.CompressFormat.JPEG, AVATAR_JPEG_QUALITY, output)
        bitmap.recycle()
        output.toByteArray().also { if (it.size > AVATAR_MAX_BYTES) throw PhotoException(R.string.profile_photo_too_large) }
    }
}

/** Bitmap dengan sisi terpanjang paling banyak [AVATAR_MAX_PX] dan orientasi EXIF sudah diterapkan, sehingga foto kamera tidak tampil miring. */
private fun decodeAvatar(resolver: ContentResolver, uri: Uri): Bitmap {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        return ImageDecoder.decodeBitmap(ImageDecoder.createSource(resolver, uri)) { decoder, info, _ ->
            decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
            val longest = max(info.size.width, info.size.height)
            if (longest > AVATAR_MAX_PX) {
                decoder.setTargetSize(info.size.width * AVATAR_MAX_PX / longest, info.size.height * AVATAR_MAX_PX / longest)
            }
        }
    }
    // ImageDecoder baru ada di Android 9. Android 7-8: BitmapFactory (di-sample ke bawah), lalu putar sesuai EXIF dan kecilkan ke batas tepat.
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    (resolver.openInputStream(uri) ?: throw IOException("Cannot open $uri")).use { BitmapFactory.decodeStream(it, null, bounds) }
    if (bounds.outWidth <= 0 || bounds.outHeight <= 0) throw IOException("Not an image: $uri")
    var sample = 1
    while (max(bounds.outWidth, bounds.outHeight) / (sample * 2) >= AVATAR_MAX_PX) sample *= 2
    val options = BitmapFactory.Options().apply { inSampleSize = sample }
    val decoded = (resolver.openInputStream(uri) ?: throw IOException("Cannot open $uri")).use { BitmapFactory.decodeStream(it, null, options) }
        ?: throw IOException("Cannot decode $uri")
    val orientation = (resolver.openInputStream(uri) ?: throw IOException("Cannot open $uri")).use {
        ExifInterface(it).getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
    }
    val matrix = Matrix().apply {
        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> postRotate(90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> postRotate(180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> postRotate(270f)
            ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> postScale(-1f, 1f)
            ExifInterface.ORIENTATION_FLIP_VERTICAL -> postScale(1f, -1f)
            ExifInterface.ORIENTATION_TRANSPOSE -> { postRotate(90f); postScale(-1f, 1f) }
            ExifInterface.ORIENTATION_TRANSVERSE -> { postRotate(270f); postScale(-1f, 1f) }
        }
    }
    // inSampleSize hanya pangkat dua, jadi hasilnya bisa sampai dua kali batas; kecilkan di langkah yang sama dengan rotasi.
    val longest = max(decoded.width, decoded.height)
    if (longest > AVATAR_MAX_PX) AVATAR_MAX_PX.toFloat().div(longest).let { matrix.postScale(it, it) }
    if (matrix.isIdentity) return decoded
    return Bitmap.createBitmap(decoded, 0, 0, decoded.width, decoded.height, matrix, true).also { decoded.recycle() }
}
