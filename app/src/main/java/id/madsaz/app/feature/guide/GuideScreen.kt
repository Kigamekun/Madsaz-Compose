package id.madsaz.app.feature.guide

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Face
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.Lock
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
import id.madsaz.app.model.CryCategory
import id.madsaz.app.ui.components.CategoryBadge
import id.madsaz.app.ui.components.CompactContainer
import id.madsaz.app.ui.theme.AppBackground
import id.madsaz.app.ui.theme.BrandBlue
import id.madsaz.app.ui.theme.BrandBlueSoft
import id.madsaz.app.ui.theme.Cream
import id.madsaz.app.ui.theme.MutedInk

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun GuideScreen(onBack: () -> Unit, modifier: Modifier = Modifier) {
    Scaffold(
        modifier = modifier,
        containerColor = AppBackground,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.guide_title)) },
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
                item {
                    Surface(shape = MaterialTheme.shapes.large, color = BrandBlueSoft) {
                        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                            Text(stringResource(R.string.guide_intro_title), style = MaterialTheme.typography.titleLarge)
                            Text(
                                stringResource(R.string.guide_intro_body),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MutedInk,
                            )
                        }
                    }
                }
                item {
                    GuideStep(
                        number = "1",
                        icon = Icons.Rounded.LightMode,
                        title = stringResource(R.string.guide_light_title),
                        body = stringResource(R.string.guide_light_body),
                    )
                }
                item {
                    GuideStep(
                        number = "2",
                        icon = Icons.Rounded.Face,
                        title = stringResource(R.string.guide_frame_title),
                        body = stringResource(R.string.guide_frame_body),
                    )
                }
                item {
                    GuideStep(
                        number = "3",
                        icon = Icons.Rounded.GraphicEq,
                        title = stringResource(R.string.guide_sound_title),
                        body = stringResource(R.string.guide_sound_body),
                    )
                }
                item {
                    Column(
                        modifier = Modifier.padding(top = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(5.dp),
                    ) {
                        Text(stringResource(R.string.guide_categories_title), style = MaterialTheme.typography.titleLarge)
                        Text(
                            stringResource(R.string.guide_categories_body),
                            style = MaterialTheme.typography.bodySmall,
                            color = MutedInk,
                        )
                    }
                }
                items(CryCategory.entries.size) { index ->
                    val category = CryCategory.entries[index]
                    Surface(shape = MaterialTheme.shapes.medium, color = MaterialTheme.colorScheme.surface) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(14.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            CategoryBadge(category)
                            Text(
                                stringResource(category.meaningRes),
                                modifier = Modifier.weight(1f),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                            )
                        }
                    }
                }
                item {
                    Surface(shape = MaterialTheme.shapes.large, color = Cream) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.Top,
                        ) {
                            Icon(Icons.Rounded.Lock, contentDescription = null, tint = BrandBlue, modifier = Modifier.size(24.dp))
                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(stringResource(R.string.guide_privacy_title), style = MaterialTheme.typography.titleMedium)
                                Text(
                                    stringResource(R.string.guide_privacy_body),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MutedInk,
                                )
                            }
                        }
                    }
                }
                item { Spacer(Modifier.height(12.dp)) }
            }
        }
    }
}

@Composable
private fun GuideStep(
    number: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    body: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Surface(shape = MaterialTheme.shapes.medium, color = BrandBlue, contentColor = MaterialTheme.colorScheme.onPrimary) {
            Text(
                number,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
            )
        }
        Surface(modifier = Modifier.weight(1f), shape = MaterialTheme.shapes.medium, color = MaterialTheme.colorScheme.surface) {
            Row(
                modifier = Modifier.padding(14.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.Top,
            ) {
                Icon(icon, contentDescription = null, tint = BrandBlue, modifier = Modifier.size(22.dp))
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(title, style = MaterialTheme.typography.titleMedium)
                    Text(body, style = MaterialTheme.typography.bodySmall, color = MutedInk)
                }
            }
        }
    }
}
