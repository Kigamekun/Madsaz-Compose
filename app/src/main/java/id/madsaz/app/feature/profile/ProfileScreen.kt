package id.madsaz.app.feature.profile

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.madsaz.app.data.remote.AccountProfile
import id.madsaz.app.ui.components.CompactContainer
import id.madsaz.app.ui.components.ProfileAvatar
import id.madsaz.app.ui.components.tr
import id.madsaz.app.ui.components.formatStorageSize
import id.madsaz.app.ui.theme.BrandBlue
import id.madsaz.app.ui.theme.Ink
import id.madsaz.app.ui.theme.MutedInk
import java.io.ByteArrayOutputStream
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private val HeaderBlue = Color(0xFF3E86AB)
private val CardBorder = Color(0xFFDEEAF2)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    state: ProfileUiState,
    recordingCount: Int?,
    storageBytes: Long?,
    onRefresh: () -> Unit,
    onUpdateName: (String) -> Unit,
    onUploadAvatar: (ByteArray) -> Unit,
    onLogout: () -> Unit,
    onPlan: () -> Unit,
    onNotifications: () -> Unit,
    onPrivacy: () -> Unit,
    onLanguage: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var editing by remember { mutableStateOf(false) }
    var photoError by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val photoPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) scope.launch {
            try {
                val jpeg = withContext(Dispatchers.IO) { compressPhoto(context.contentResolver, uri) }
                photoError = null
                onUploadAvatar(jpeg)
            } catch (error: Exception) {
                photoError = error.message ?: "Foto tidak dapat dibaca."
            }
        }
    }

    CompactContainer(modifier) {
        Box(Modifier.fillMaxSize().background(Color.White)) {
            Box(Modifier.fillMaxWidth().height(200.dp).background(HeaderBlue))
            Surface(
                modifier = Modifier.fillMaxSize().padding(top = 169.dp),
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                color = Color.White,
            ) { }
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 47.dp, bottom = 130.dp),
            ) {
                item {
                    when (state) {
                        ProfileUiState.Loading -> Box(Modifier.fillMaxWidth().height(154.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = BrandBlue)
                        }
                        is ProfileUiState.Error -> Column(Modifier.fillMaxWidth().padding(24.dp)) {
                            Text(state.message, color = Ink)
                            Button(onClick = onRefresh) { Text(tr("Try again", "Coba lagi")) }
                        }
                        is ProfileUiState.Content -> ProfileCard(state.account, { editing = true }) { photoPicker.launch("image/*") }
                    }
                }
                if (state is ProfileUiState.Content && (state.error != null || photoError != null)) {
                    item { Text(state.error ?: photoError.orEmpty(), color = Color(0xFFB00020), modifier = Modifier.padding(top = 8.dp)) }
                }
                item { Spacer(Modifier.height(24.dp)) }
                item { Text(tr("Account", "Akun"), color = Ink, fontSize = 20.sp, fontWeight = FontWeight.Bold) }
                item { Spacer(Modifier.height(16.dp)) }
                item { ProfileRow("A", tr("Account profile", "Profil akun"), tr("Name, email and photo", "Nama, email, dan foto"), Color(0xFFE5F4FC)) { editing = true } }
                item { ProfileRow("M", "Madsaz Plus", tr("Manage your plan", "Kelola paket"), Color.White, onPlan) }
                item { ProfileRow("N", tr("Notifications", "Notifikasi"), tr("Gentle reminders", "Pengingat"), Color.White, onNotifications) }
                item { ProfileRow("P", tr("Privacy policy", "Kebijakan privasi"), tr("How your data is handled", "Cara data digunakan"), Color.White, onPrivacy) }
                item { ProfileRow("G", tr("Language", "Bahasa"), "English / Bahasa Indonesia", Color.White, onLanguage) }
                item { ProfileRow("L", tr("Log out", "Keluar"), tr("See you soon", "Sampai jumpa"), Color(0xFFFFE5EB), onLogout) }
                item {
                    if (recordingCount != null && storageBytes != null) {
                        Text(tr("$recordingCount recordings", "$recordingCount rekaman") + " · ${formatStorageSize(storageBytes)}",
                            color = MutedInk, fontSize = 11.sp,
                            modifier = Modifier.fillMaxWidth().padding(top = 24.dp), textAlign = TextAlign.Center)
                    }
                }
            }
        }
    }

    if (editing && state is ProfileUiState.Content) {
        var name by remember(state.account.name) { mutableStateOf(state.account.name) }
        ModalBottomSheet(onDismissRequest = { editing = false }, containerColor = Color.White) {
            Column(Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(tr("Account profile", "Profil akun"), color = Ink, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    ProfileAvatar(state.account.avatarUrl, state.account.name, Modifier.size(84.dp).clip(CircleShape).clickable { photoPicker.launch("image/*") })
                }
                Text(tr("Tap photo to change", "Ketuk foto untuk mengganti"), color = MutedInk, fontSize = 12.sp)
                OutlinedTextField(name, { name = it }, label = { Text(tr("Name", "Nama")) }, singleLine = true,
                    modifier = Modifier.fillMaxWidth())
                OutlinedTextField(state.account.email, {}, label = { Text("Email") }, readOnly = true,
                    singleLine = true, modifier = Modifier.fillMaxWidth())
                Button(onClick = { onUpdateName(name); editing = false },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BrandBlue)) { Text(tr("Save changes", "Simpan perubahan")) }
            }
        }
    }
}

@Composable
private fun ProfileCard(account: AccountProfile, onEdit: () -> Unit, onPhoto: () -> Unit) {
    Surface(shape = RoundedCornerShape(24.dp), color = Color.White, border = BorderStroke(1.dp, CardBorder)) {
        Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(tr("Your profile", "Profil Anda"), modifier = Modifier.weight(1f), color = Ink, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text(tr("Edit", "Ubah"), modifier = Modifier.clickable(onClick = onEdit).padding(8.dp), color = BrandBlue)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                ProfileAvatar(account.avatarUrl, account.name, Modifier.size(72.dp).clip(CircleShape).clickable(onClick = onPhoto))
                Column(Modifier.padding(start = 12.dp)) {
                    Text(account.name.ifBlank { tr("Your name", "Nama Anda") }, color = Ink, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text(account.email, color = MutedInk, fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
private fun ProfileRow(badge: String, title: String, subtitle: String, color: Color, onClick: () -> Unit) {
    Surface(onClick = onClick, modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp).height(54.dp),
        shape = RoundedCornerShape(16.dp), color = color, border = BorderStroke(1.dp, CardBorder)) {
        Row(Modifier.fillMaxSize().padding(horizontal = 14.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(badge, color = BrandBlue, fontSize = 13.sp, modifier = Modifier.padding(end = 16.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(title, color = Ink, fontSize = 13.sp, lineHeight = 18.sp, fontWeight = FontWeight.SemiBold)
                Text(subtitle, color = MutedInk, fontSize = 11.sp, lineHeight = 15.sp)
            }
            Icon(Icons.Rounded.ChevronRight, null, tint = MutedInk, modifier = Modifier.size(20.dp))
        }
    }
}

private fun compressPhoto(resolver: android.content.ContentResolver, uri: Uri): ByteArray {
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
    if (bounds.outWidth <= 0 || bounds.outHeight <= 0) error("Foto tidak valid.")
    val options = BitmapFactory.Options().apply {
        inSampleSize = 1
        while (bounds.outWidth / inSampleSize > 1024 || bounds.outHeight / inSampleSize > 1024) inSampleSize *= 2
    }
    val bitmap = resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, options) }
        ?: error("Foto tidak dapat dibaca.")
    return ByteArrayOutputStream().use { output ->
        bitmap.compress(Bitmap.CompressFormat.JPEG, 82, output)
        bitmap.recycle()
        output.toByteArray().also { if (it.size > 5 * 1024 * 1024) error("Foto terlalu besar.") }
    }
}
