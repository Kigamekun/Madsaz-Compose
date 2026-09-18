package id.madsaz.app.feature.profile

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.PermCameraMic
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import id.madsaz.app.R
import id.madsaz.app.ui.components.CompactContainer
import id.madsaz.app.ui.components.formatStorageSize
import id.madsaz.app.ui.theme.AppBackground
import id.madsaz.app.ui.theme.BrandBlue
import id.madsaz.app.ui.theme.BrandBlueSoft
import id.madsaz.app.ui.theme.Cream
import id.madsaz.app.ui.theme.MutedInk
import id.madsaz.app.ui.theme.Success
import id.madsaz.app.ui.theme.SuccessSoft

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun ProfileScreen(
    recordingCount: Int?,
    storageBytes: Long?,
    permissionsGranted: Boolean,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        containerColor = AppBackground,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.profile_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Rounded.ArrowBack, contentDescription = stringResource(R.string.common_back))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AppBackground),
            )
        },
    ) { padding ->
        CompactContainer(Modifier.padding(padding)) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                item { ProfileHeader() }
                item { SectionLabel(stringResource(R.string.profile_device_title)) }
                item {
                    Surface(shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.surface) {
                        Column {
                            ProfileRow(
                                icon = Icons.Rounded.PermCameraMic,
                                title = stringResource(R.string.profile_permissions),
                                value = stringResource(
                                    if (permissionsGranted) R.string.profile_permission_granted
                                    else R.string.profile_permission_missing,
                                ),
                                valueColor = if (permissionsGranted) Success else MaterialTheme.colorScheme.error,
                            )
                            ProfileRow(
                                icon = Icons.Rounded.Folder,
                                title = stringResource(R.string.profile_storage),
                                value = if (recordingCount != null && storageBytes != null) {
                                    "${stringResource(R.string.profile_recording_count, recordingCount)} · ${formatStorageSize(storageBytes)}"
                                } else {
                                    stringResource(R.string.common_loading)
                                },
                            )
                        }
                    }
                }
                item {
                    InfoCard(
                        icon = Icons.Rounded.Lock,
                        title = stringResource(R.string.profile_privacy_title),
                        body = stringResource(R.string.profile_privacy_body),
                        color = BrandBlueSoft,
                    )
                }
                item {
                    InfoCard(
                        icon = Icons.Rounded.Info,
                        title = stringResource(R.string.profile_about_title),
                        body = stringResource(R.string.profile_about_body),
                        color = Cream,
                    )
                }
                item {
                    Text(
                        stringResource(R.string.profile_version),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = MutedInk,
                    )
                }
                item { Spacer(Modifier.height(12.dp)) }
            }
        }
    }
}

@Composable
private fun ProfileHeader() {
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(7.dp),
    ) {
        Surface(
            modifier = Modifier.size(78.dp),
            shape = CircleShape,
            color = BrandBlue,
            contentColor = MaterialTheme.colorScheme.onPrimary,
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text("M", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            }
        }
        Text(stringResource(R.string.profile_name), style = MaterialTheme.typography.titleLarge)
        Surface(shape = CircleShape, color = SuccessSoft, contentColor = Success) {
            Text(
                stringResource(R.string.profile_status),
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                style = MaterialTheme.typography.labelMedium,
            )
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(text, style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 4.dp))
}

@Composable
private fun ProfileRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    value: String,
    valueColor: androidx.compose.ui.graphics.Color = MutedInk,
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(15.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, tint = BrandBlue, modifier = Modifier.size(23.dp))
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(value, style = MaterialTheme.typography.bodySmall, color = valueColor)
        }
    }
}

@Composable
private fun InfoCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    body: String,
    color: androidx.compose.ui.graphics.Color,
) {
    Surface(shape = MaterialTheme.shapes.large, color = color) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Icon(icon, contentDescription = null, tint = BrandBlue, modifier = Modifier.size(24.dp))
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Text(body, style = MaterialTheme.typography.bodySmall, color = MutedInk)
            }
        }
    }
}
