package ru.aptechka.ui.screens.kits

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.navigation.NavController
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import ru.aptechka.R
import ru.aptechka.domain.model.BatchStatus
import ru.aptechka.domain.model.FormKey
import ru.aptechka.domain.model.UserDrugWithBatches
import ru.aptechka.ui.theme.KitColors
import ru.aptechka.ui.theme.LocalStatusColors
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs

// ── Sort enum ─────────────────────────────────────────────────────────────────

private enum class SortMode { EXPIRY, NAME, LOCATION, STATUS }

// ── Screen ────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KitDetailScreen(
    kitId: Long,
    navController: NavController,
    viewModel: KitDetailViewModel = koinViewModel(parameters = { parametersOf(kitId) }),
) {
    val kit by viewModel.kit.collectAsState()
    val kitName     = kit?.name     ?: "Аптечка"
    val kitColorKey = kit?.colorHex ?: "green"
    val kitIconKey  = kit?.iconName ?: "home"

    val drugs by viewModel.drugs.collectAsState()
    var sortMode by remember { mutableStateOf(SortMode.EXPIRY) }
    var showFabMenu by remember { mutableStateOf(false) }
    var drugToDelete by remember { mutableStateOf<UserDrugWithBatches?>(null) }

    val sorted = remember(drugs, sortMode) {
        when (sortMode) {
            SortMode.NAME     -> drugs.sortedBy { it.drug.name }
            SortMode.EXPIRY   -> drugs.sortedBy { it.nearestExpiry ?: Long.MAX_VALUE }
            SortMode.LOCATION -> drugs.sortedBy { it.drug.notes } // location placeholder
            SortMode.STATUS   -> drugs.sortedBy { it.worstStatus.ordinal }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Outlined.ArrowBack, contentDescription = "Назад")
                    }
                },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            kitIcon(kitIconKey),
                            contentDescription = null,
                            tint     = KitColors.get(kitColorKey).fg,
                            modifier = Modifier.size(22.dp),
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text     = kitName,
                            style    = MaterialTheme.typography.headlineLarge,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { /* TODO: search in kit */ }) {
                        Icon(Icons.Outlined.Search, contentDescription = "Поиск")
                    }
                    IconButton(onClick = { /* TODO: kit menu */ }) {
                        Icon(Icons.Outlined.MoreVert, contentDescription = "Меню")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
        floatingActionButton = {
            Column(horizontalAlignment = Alignment.End) {
                AnimatedVisibility(visible = showFabMenu) {
                    Column(
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        FabMenuItem(
                            label   = stringResource(R.string.scan),
                            icon    = Icons.Outlined.QrCodeScanner,
                            onClick = { showFabMenu = false /* TODO: scanner */ },
                        )
                        FabMenuItem(
                            label   = stringResource(R.string.manual),
                            icon    = Icons.Outlined.Edit,
                            onClick = { showFabMenu = false /* TODO: add drug form */ },
                        )
                        Spacer(Modifier.height(4.dp))
                    }
                }
                ExtendedFloatingActionButton(
                    onClick        = { showFabMenu = !showFabMenu },
                    icon           = { Icon(if (showFabMenu) Icons.Outlined.Close else Icons.Outlined.Add, null) },
                    text           = { Text(if (showFabMenu) "" else stringResource(R.string.add_drug)) },
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor   = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        LazyColumn(
            contentPadding = PaddingValues(
                start  = 16.dp,
                end    = 16.dp,
                top    = padding.calculateTopPadding() + 4.dp,
                bottom = padding.calculateBottomPadding() + 96.dp,
            ),
        ) {
            // Sort chips
            item {
                SortChipsRow(
                    current  = sortMode,
                    onChange = { sortMode = it },
                    modifier = Modifier.padding(bottom = 8.dp),
                )
            }

            if (sorted.isEmpty()) {
                item { KitEmptyState() }
            } else {
                items(sorted, key = { it.drug.id }) { dw ->
                    DrugRow(
                        drugWithBatches = dw,
                        onClick         = { /* TODO: med detail */ },
                        onToCart        = { /* TODO */ },
                        onDelete        = { drugToDelete = dw },
                        modifier        = Modifier.padding(bottom = 8.dp),
                    )
                }
            }
        }
    }

    // Dismiss FAB menu on back-area tap
    if (showFabMenu) {
        Box(
            Modifier
                .fillMaxSize()
                .clickable(indication = null, interactionSource = remember { MutableInteractionSource() }) {
                    showFabMenu = false
                }
        )
    }

    drugToDelete?.let { dw ->
        AlertDialog(
            onDismissRequest = { drugToDelete = null },
            title = { Text("Удалить «${dw.drug.name}»?") },
            text  = { Text(stringResource(R.string.delete_drug_confirm)) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteDrug(dw.drug)
                    drugToDelete = null
                }) {
                    Text(stringResource(R.string.delete), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { drugToDelete = null }) { Text(stringResource(R.string.cancel)) }
            },
        )
    }
}

// ── Sort chips ────────────────────────────────────────────────────────────────

@Composable
private fun SortChipsRow(
    current: SortMode,
    onChange: (SortMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    val chips = listOf(
        SortMode.EXPIRY   to stringResource(R.string.sort_by_expiry),
        SortMode.NAME     to stringResource(R.string.sort_by_name),
        SortMode.LOCATION to stringResource(R.string.sort_by_location),
        SortMode.STATUS   to stringResource(R.string.sort_by_status),
    )
    LazyRow(
        modifier            = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(chips) { (mode, label) ->
            FilterChip(
                selected = current == mode,
                onClick  = { onChange(mode) },
                label    = { Text(label, style = MaterialTheme.typography.labelLarge) },
                leadingIcon = if (current == mode) {
                    { Icon(Icons.Outlined.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                } else null,
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor     = MaterialTheme.colorScheme.secondaryContainer,
                    selectedLabelColor         = MaterialTheme.colorScheme.onSecondaryContainer,
                    selectedLeadingIconColor   = MaterialTheme.colorScheme.onSecondaryContainer,
                ),
            )
        }
    }
}

// ── Drug row ──────────────────────────────────────────────────────────────────

@Composable
private fun DrugRow(
    drugWithBatches: UserDrugWithBatches,
    onClick: () -> Unit,
    onToCart: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val drug          = drugWithBatches.drug
    val statusColors  = LocalStatusColors.current
    val worstStatus   = drugWithBatches.worstStatus
    val nearestExpiry = drugWithBatches.nearestExpiry
    val totalQty      = drugWithBatches.totalQuantity

    val statusColor = when (worstStatus) {
        BatchStatus.EXPIRED      -> statusColors.expiredFg
        BatchStatus.EXPIRING_SOON -> statusColors.expiringFg
        else                     -> statusColors.okFg
    }

    val expiryLabel = nearestExpiry?.let { ts ->
        val days = ((ts - System.currentTimeMillis()) / 86_400_000L).toInt()
        when {
            days < 0  -> "истёк ${abs(days)} дней назад"
            days == 0 -> "истекает сегодня"
            days < 60 -> "истекает через $days дн."
            else      -> {
                val months = days / 30
                "истекает через $months мес."
            }
        }
    } ?: "срок не указан"

    val formLabel = when (drug.form) {
        FormKey.TABLET      -> "Таблетки"
        FormKey.CAPSULE     -> "Капсулы"
        FormKey.SYRUP       -> "Сироп"
        FormKey.DROPS       -> "Капли"
        FormKey.OINTMENT    -> "Мазь"
        FormKey.INJECTION   -> "Инъекция"
        FormKey.SPRAY       -> "Спрей"
        FormKey.PATCH       -> "Пластырь"
        FormKey.SUPPOSITORY -> "Суппозитории"
        else                -> "Другое"
    }

    val qtyStr = when {
        totalQty == totalQty.toLong().toFloat() -> "${totalQty.toLong()} шт"
        else -> "${"%.1f".format(totalQty)} шт"
    }

    Surface(
        modifier  = modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(16.dp),
        color     = MaterialTheme.colorScheme.surfaceContainer,
        onClick   = onClick,
    ) {
        Row(modifier = Modifier.height(IntrinsicSize.Min)) {
            // Status stripe
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(statusColor),
            )

            // Form tile
            Box(
                modifier         = Modifier
                    .padding(10.dp)
                    .size(56.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(formTileColor(drug.form)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    formIcon(drug.form),
                    contentDescription = null,
                    tint     = Color.White,
                    modifier = Modifier.size(28.dp),
                )
            }

            // Content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 10.dp),
            ) {
                Text(
                    text     = "${drug.name} · $formLabel",
                    style    = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text  = "$qtyStr · $expiryLabel",
                    style = MaterialTheme.typography.bodyMedium,
                    color = statusColor,
                )
                if (drugWithBatches.batches.size > 1) {
                    Text(
                        text  = "· ${drugWithBatches.batches.size} партии",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            // Status dot + menu
            Column(
                modifier            = Modifier.padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(statusColor),
                )
                Box {
                    var expanded by remember { mutableStateOf(false) }
                    IconButton(onClick = { expanded = true }, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Outlined.MoreVert, null, modifier = Modifier.size(18.dp))
                    }
                    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        DropdownMenuItem(
                            text    = { Text(stringResource(R.string.to_shopping)) },
                            onClick = { expanded = false; onToCart() },
                            leadingIcon = { Icon(Icons.Outlined.ShoppingCart, null) },
                        )
                        DropdownMenuItem(
                            text    = { Text(stringResource(R.string.delete), color = MaterialTheme.colorScheme.error) },
                            onClick = { expanded = false; onDelete() },
                            leadingIcon = { Icon(Icons.Outlined.Delete, null, tint = MaterialTheme.colorScheme.error) },
                        )
                    }
                }
            }
        }
    }
}

// ── Form helpers ──────────────────────────────────────────────────────────────

private fun formIcon(form: FormKey) = when (form) {
    FormKey.TABLET      -> Icons.Outlined.Medication
    FormKey.CAPSULE     -> Icons.Outlined.Medication
    FormKey.SYRUP       -> Icons.Outlined.LocalDrink
    FormKey.DROPS       -> Icons.Outlined.Opacity
    FormKey.OINTMENT    -> Icons.Outlined.Spa
    FormKey.INJECTION   -> Icons.Outlined.Vaccines
    FormKey.SPRAY       -> Icons.Outlined.Air
    FormKey.PATCH       -> Icons.Outlined.Healing
    FormKey.SUPPOSITORY -> Icons.Outlined.Medication
    else                -> Icons.Outlined.Medication
}

private fun formTileColor(form: FormKey) = when (form) {
    FormKey.TABLET      -> Color(0xFF4A7C59)
    FormKey.CAPSULE     -> Color(0xFF426EA3)
    FormKey.SYRUP       -> Color(0xFFB57A14)
    FormKey.DROPS       -> Color(0xFF4E6868)
    FormKey.OINTMENT    -> Color(0xFF836A47)
    FormKey.INJECTION   -> Color(0xFF6E5A9B)
    FormKey.SPRAY       -> Color(0xFF347D7D)
    FormKey.PATCH       -> Color(0xFFA65082)
    else                -> Color(0xFF6E6A60)
}

// ── FAB menu item ─────────────────────────────────────────────────────────────

@Composable
private fun FabMenuItem(label: String, icon: ImageVector, onClick: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Surface(
            shape = RoundedCornerShape(999.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            onClick = onClick,
        ) {
            Text(label, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
        }
        Spacer(Modifier.width(8.dp))
        SmallFloatingActionButton(
            onClick        = onClick,
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        ) {
            Icon(icon, contentDescription = label)
        }
    }
}

// ── Empty state ───────────────────────────────────────────────────────────────

@Composable
private fun KitEmptyState() {
    Box(
        modifier         = Modifier
            .fillMaxWidth()
            .padding(top = 64.dp),
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
                    Icons.Outlined.Inventory2,
                    contentDescription = null,
                    tint     = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(32.dp),
                )
            }
            Spacer(Modifier.height(24.dp))
            Text(
                text  = stringResource(R.string.kit_empty_title),
                style = MaterialTheme.typography.titleLarge,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text      = stringResource(R.string.kit_empty_body),
                style     = MaterialTheme.typography.bodyMedium,
                color     = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}
