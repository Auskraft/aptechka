package ru.aptechka.ui.screens.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ru.aptechka.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen() {
    Scaffold(
        topBar = {
            LargeTopAppBar(
                title = {
                    Text(
                        text  = stringResource(R.string.settings_title),
                        style = MaterialTheme.typography.displayMedium,
                    )
                },
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        LazyColumn(
            contentPadding = PaddingValues(
                start  = 16.dp,
                end    = 16.dp,
                top    = padding.calculateTopPadding() + 8.dp,
                bottom = padding.calculateBottomPadding() + 16.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item {
                SettingsTile {
                    SettingsRow(
                        icon      = Icons.Outlined.Language,
                        title     = stringResource(R.string.settings_language),
                        subtitle  = "Русский",
                        onClick   = { /* TODO */ },
                    )
                    HorizontalDivider(modifier = Modifier.padding(start = 56.dp))
                    SettingsRow(
                        icon      = Icons.Outlined.CloudUpload,
                        title     = stringResource(R.string.settings_backup),
                        subtitle  = "Резервных копий нет",
                        onClick   = { /* TODO */ },
                    )
                    HorizontalDivider(modifier = Modifier.padding(start = 56.dp))
                    SettingsRow(
                        icon      = Icons.Outlined.Palette,
                        title     = stringResource(R.string.settings_appearance),
                        subtitle  = "Системная · Material You",
                        onClick   = { /* TODO */ },
                    )
                    HorizontalDivider(modifier = Modifier.padding(start = 56.dp))
                    SettingsRow(
                        icon      = Icons.Outlined.Notifications,
                        title     = stringResource(R.string.settings_notifications),
                        subtitle  = "30, 7 дней · 10:00",
                        onClick   = { /* TODO */ },
                    )
                }
            }

            item {
                SettingsTile {
                    SettingsRow(
                        icon      = Icons.Outlined.Info,
                        title     = stringResource(R.string.settings_about),
                        subtitle  = "Версия 1.0",
                        showChevron = false,
                        onClick   = { },
                    )
                    HorizontalDivider(modifier = Modifier.padding(start = 56.dp))
                    SettingsRow(
                        icon      = Icons.Outlined.Policy,
                        title     = stringResource(R.string.settings_privacy),
                        onClick   = { /* TODO */ },
                    )
                    HorizontalDivider(modifier = Modifier.padding(start = 56.dp))
                    SettingsRow(
                        icon      = Icons.Outlined.StarOutline,
                        title     = stringResource(R.string.settings_review),
                        onClick   = { /* TODO */ },
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsTile(content: @Composable ColumnScope.() -> Unit) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
    ) {
        Column(content = content)
    }
}

@Composable
private fun SettingsRow(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    showChevron: Boolean = true,
    onClick: () -> Unit,
) {
    Row(
        modifier          = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint               = MaterialTheme.colorScheme.primary,
            modifier           = Modifier.size(24.dp),
        )
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            if (subtitle != null) {
                Text(
                    text  = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        if (showChevron) {
            Icon(
                Icons.Outlined.ChevronRight,
                contentDescription = null,
                tint               = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
