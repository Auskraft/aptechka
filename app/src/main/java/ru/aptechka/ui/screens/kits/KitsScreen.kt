package ru.aptechka.ui.screens.kits

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import org.koin.androidx.compose.koinViewModel
import ru.aptechka.R
import ru.aptechka.domain.model.Kit
import ru.aptechka.domain.model.KitWithStats
import ru.aptechka.ui.navigation.Screen
import ru.aptechka.ui.theme.KitColors
import ru.aptechka.ui.theme.LocalStatusColors

// ── Icon mapping ──────────────────────────────────────────────────────────────

fun kitIcon(key: String): ImageVector = when (key) {
    "home"     -> Icons.Outlined.Home
    "cottage"  -> Icons.Outlined.Cottage
    "car"      -> Icons.Outlined.DirectionsCar
    "backpack" -> Icons.Outlined.Backpack
    "child"    -> Icons.Outlined.ChildCare
    "elderly"  -> Icons.Outlined.Elderly
    "pets"     -> Icons.Outlined.Pets
    "work"     -> Icons.Outlined.Work
    "luggage"  -> Icons.Outlined.Luggage
    "beach"    -> Icons.Outlined.BeachAccess
    "fitness"  -> Icons.Outlined.FitnessCenter
    "medical"  -> Icons.Outlined.MedicalServices
    else       -> Icons.Outlined.MedicalServices
}

// ── Pluralization helpers ─────────────────────────────────────────────────────

private fun pluralDrugs(n: Int): String {
    val last = n % 10
    val mod  = n % 100
    return when {
        mod in 11..14  -> "$n препаратов"
        last == 1      -> "$n препарат"
        last in 2..4   -> "$n препарата"
        else           -> "$n препаратов"
    }
}

// ── Screen ────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KitsScreen(
    navController: NavController,
    viewModel: KitsViewModel = koinViewModel(),
) {
    val kits by viewModel.kits.collectAsState()
    val kitsWithStats by viewModel.kitsWithStats.collectAsState()

    var showSearch by remember { mutableStateOf(false) }
    var showCreateDialog by remember { mutableStateOf(false) }
    var kitToDelete by remember { mutableStateOf<Kit?>(null) }

    val totalExpired  = kitsWithStats.sumOf { it.expiredCount }
    val totalExpiring = kitsWithStats.sumOf { it.expiringSoonCount }

    Scaffold(
        topBar = {
            LargeTopAppBar(
                title = {
                    Column {
                        Text(
                            text  = stringResource(R.string.kits_title),
                            style = MaterialTheme.typography.displayMedium,
                        )
                        if (kits.isNotEmpty()) {
                            val sub = buildString {
                                append(pluralDrugs(kits.size))
                                // In future: total drug count across all kits
                            }
                            Text(
                                text  = sub,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { showSearch = true }) {
                        Icon(Icons.Outlined.Search, contentDescription = "Поиск")
                    }
                },
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick           = { showCreateDialog = true },
                icon              = { Icon(Icons.Outlined.Add, contentDescription = null) },
                text              = { Text(stringResource(R.string.add_kit)) },
                containerColor    = MaterialTheme.colorScheme.primaryContainer,
                contentColor      = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        if (kits.isEmpty()) {
            KitsEmptyState(
                modifier = Modifier.padding(padding),
                onCreate = { showCreateDialog = true },
            )
        } else {
            val columns = if (kits.size <= 2) 1 else 2
            LazyVerticalGrid(
                columns         = GridCells.Fixed(columns),
                contentPadding  = PaddingValues(
                    start  = 16.dp,
                    end    = 16.dp,
                    top    = padding.calculateTopPadding() + 8.dp,
                    bottom = padding.calculateBottomPadding() + 96.dp,
                ),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement   = Arrangement.spacedBy(12.dp),
            ) {
                // Attention row
                if (totalExpired > 0 || totalExpiring > 0) {
                    item(span = { GridItemSpan(columns) }) {
                        AttentionRow(expired = totalExpired, expiring = totalExpiring)
                    }
                }

                // Section header
                item(span = { GridItemSpan(columns) }) {
                    Text(
                        text     = stringResource(R.string.my_kits_header),
                        style    = MaterialTheme.typography.labelSmall,
                        color    = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp),
                    )
                }

                // Kit cards
                items(kitsWithStats, key = { it.kit.id }) { ks ->
                    KitCard(
                        kitWithStats = ks,
                        onClick      = { navController.navigate(Screen.KitDetail.go(ks.kit.id)) },
                        onDelete     = { kitToDelete = ks.kit },
                    )
                }

                // Add new kit card
                item {
                    AddKitCard(onClick = { showCreateDialog = true })
                }
            }
        }
    }

    // ── Dialogs ──────────────────────────────────────────────────────────────
    if (showCreateDialog) {
        CreateKitDialog(
            onConfirm = { name, color, icon ->
                viewModel.createKit(name, color, icon)
                showCreateDialog = false
            },
            onDismiss = { showCreateDialog = false },
        )
    }

    kitToDelete?.let { kit ->
        AlertDialog(
            onDismissRequest = { kitToDelete = null },
            title = { Text("Удалить «${kit.name}»?") },
            text  = { Text(stringResource(R.string.delete_kit_confirm)) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteKit(kit)
                    kitToDelete = null
                }) {
                    Text(
                        text  = stringResource(R.string.delete),
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { kitToDelete = null }) { Text(stringResource(R.string.cancel)) }
            },
        )
    }
}

// ── Attention row ─────────────────────────────────────────────────────────────

@Composable
private fun AttentionRow(expired: Int, expiring: Int) {
    val statusColors = LocalStatusColors.current
    Row(
        modifier            = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        if (expired > 0) {
            AttentionCard(
                count     = expired,
                label     = "просрочено",
                fg        = statusColors.expiredFg,
                container = statusColors.expiredContainer,
                icon      = Icons.Outlined.Warning,
                modifier  = Modifier.weight(1f),
            )
        }
        if (expiring > 0) {
            AttentionCard(
                count     = expiring,
                label     = "скоро истекут",
                fg        = statusColors.expiringFg,
                container = statusColors.expiringContainer,
                icon      = Icons.Outlined.Schedule,
                modifier  = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun AttentionCard(
    count: Int,
    label: String,
    fg: Color,
    container: Color,
    icon: ImageVector,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(container)
            .padding(horizontal = 14.dp, vertical = 16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier        = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.4f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, contentDescription = null, tint = fg, modifier = Modifier.size(22.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column {
                Text(
                    text  = count.toString(),
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontSize   = 22.sp,
                        fontWeight = FontWeight.Bold,
                    ),
                    color = fg,
                )
                Text(
                    text  = label,
                    style = MaterialTheme.typography.bodySmall,
                    color = fg,
                )
            }
        }
    }
}

// ── Kit card ──────────────────────────────────────────────────────────────────

@Composable
private fun KitCard(
    kitWithStats: KitWithStats,
    onClick: () -> Unit,
    onDelete: () -> Unit,
) {
    val kit     = kitWithStats.kit
    val accent  = KitColors.get(kit.colorHex)
    val hasProb = kitWithStats.expiredCount > 0 || kitWithStats.expiringSoonCount > 0
    val statusColors = LocalStatusColors.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(accent.container)
            .clickable(onClick = onClick)
            .padding(14.dp),
    ) {
        Column {
            Row(
                modifier     = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
            ) {
                Icon(
                    imageVector        = kitIcon(kit.iconName),
                    contentDescription = null,
                    tint               = accent.fg,
                    modifier           = Modifier.size(32.dp),
                )
                Spacer(Modifier.weight(1f))
                // Status badge
                when {
                    kitWithStats.expiredCount > 0 ->
                        StatusBadge(kitWithStats.expiredCount, statusColors.expiredFg)
                    kitWithStats.expiringSoonCount > 0 ->
                        StatusBadge(kitWithStats.expiringSoonCount, statusColors.expiringFg)
                }
                // More menu
                Box {
                    var expanded by remember { mutableStateOf(false) }
                    IconButton(
                        onClick  = { expanded = true },
                        modifier = Modifier.size(24.dp),
                    ) {
                        Icon(
                            Icons.Outlined.MoreVert,
                            contentDescription = null,
                            tint               = accent.fg,
                            modifier           = Modifier.size(20.dp),
                        )
                    }
                    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        DropdownMenuItem(
                            text    = { Text("Переименовать") },
                            onClick = { expanded = false /* TODO */ },
                        )
                        DropdownMenuItem(
                            text    = { Text("Удалить", color = MaterialTheme.colorScheme.error) },
                            onClick = { expanded = false; onDelete() },
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            Text(
                text     = kit.name,
                style    = MaterialTheme.typography.titleLarge,
                color    = accent.fg,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            Spacer(Modifier.height(4.dp))

            if (!hasProb) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Outlined.CheckCircleOutline,
                        contentDescription = null,
                        tint               = accent.fg,
                        modifier           = Modifier.size(14.dp),
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text  = "всё в норме",
                        style = MaterialTheme.typography.bodySmall,
                        color = accent.fg,
                    )
                }
            } else {
                Text(
                    text  = buildString {
                        if (kitWithStats.expiredCount > 0) append("${kitWithStats.expiredCount} просрочено")
                        if (kitWithStats.expiredCount > 0 && kitWithStats.expiringSoonCount > 0) append(" · ")
                        if (kitWithStats.expiringSoonCount > 0) append("${kitWithStats.expiringSoonCount} скоро")
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = accent.fg,
                )
            }
        }
    }
}

@Composable
private fun StatusBadge(count: Int, color: Color) {
    Box(
        modifier         = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(color)
            .padding(horizontal = 7.dp, vertical = 1.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text  = count.toString(),
            style = MaterialTheme.typography.labelSmall,
            color = Color.White,
        )
    }
}

// ── Add-kit card ──────────────────────────────────────────────────────────────

@Composable
private fun AddKitCard(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .border(
                BorderStroke(1.5.dp, MaterialTheme.colorScheme.outlineVariant),
                RoundedCornerShape(20.dp),
            )
            .clickable(onClick = onClick)
            .padding(14.dp),
    ) {
        Column {
            Box(
                modifier         = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainer),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Outlined.Add,
                    contentDescription = null,
                    tint     = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp),
                )
            }
            Spacer(Modifier.height(12.dp))
            Text(
                text  = stringResource(R.string.new_kit_card_title),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text  = stringResource(R.string.new_kit_card_sub),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

// ── Empty state ───────────────────────────────────────────────────────────────

@Composable
private fun KitsEmptyState(modifier: Modifier = Modifier, onCreate: () -> Unit) {
    Box(
        modifier         = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier            = Modifier.padding(32.dp),
        ) {
            Box(
                modifier         = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceContainer),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Outlined.MedicalServices,
                    contentDescription = null,
                    tint     = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(32.dp),
                )
            }
            Spacer(Modifier.height(24.dp))
            Text(
                text  = stringResource(R.string.kits_empty_title),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text      = stringResource(R.string.kits_empty_body),
                style     = MaterialTheme.typography.bodyMedium,
                color     = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(24.dp))
            Button(onClick = onCreate) {
                Icon(Icons.Outlined.Add, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Создать аптечку")
            }
        }
    }
}

// ── Create kit dialog ─────────────────────────────────────────────────────────

@Composable
private fun CreateKitDialog(
    onConfirm: (String, String, String) -> Unit,
    onDismiss: () -> Unit,
) {
    var name      by remember { mutableStateOf("") }
    var colorKey  by remember { mutableStateOf("green") }
    var iconKey   by remember { mutableStateOf("home") }

    val colorKeys = listOf("green", "blue", "orange", "violet", "pink", "teal", "amber", "grey")
    val iconKeys  = listOf("home", "cottage", "car", "backpack", "child", "elderly", "pets", "work")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.create_kit)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value         = name,
                    onValueChange = { name = it },
                    label         = { Text(stringResource(R.string.kit_name_hint)) },
                    singleLine    = true,
                    modifier      = Modifier.fillMaxWidth(),
                )

                // Color picker
                Text("Цвет", style = MaterialTheme.typography.labelMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    colorKeys.forEach { key ->
                        val palette = KitColors.get(key)
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(palette.fg)
                                .then(
                                    if (key == colorKey) Modifier.border(
                                        3.dp, MaterialTheme.colorScheme.onSurface, CircleShape
                                    ) else Modifier
                                )
                                .clickable { colorKey = key },
                        )
                    }
                }

                // Icon picker
                Text("Иконка", style = MaterialTheme.typography.labelMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    iconKeys.forEach { key ->
                        val accent = KitColors.get(colorKey)
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (key == iconKey) accent.container else MaterialTheme.colorScheme.surfaceContainer)
                                .clickable { iconKey = key },
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                kitIcon(key),
                                contentDescription = null,
                                tint     = if (key == iconKey) accent.fg else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp),
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick  = { if (name.isNotBlank()) onConfirm(name.trim(), colorKey, iconKey) },
                enabled  = name.isNotBlank(),
            ) { Text(stringResource(R.string.save)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
        },
    )
}
