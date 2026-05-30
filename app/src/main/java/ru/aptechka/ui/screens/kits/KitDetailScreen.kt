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
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import ru.aptechka.R
import ru.aptechka.ui.navigation.Screen
import ru.aptechka.domain.model.BatchStatus
import ru.aptechka.domain.model.UserDrugWithBatches
import ru.aptechka.ui.forms.Forms
import ru.aptechka.ui.forms.expiryLabel
import ru.aptechka.ui.theme.KitColors
import ru.aptechka.ui.theme.LocalDimens
import ru.aptechka.ui.theme.LocalStatusColors
import java.text.SimpleDateFormat
import java.util.*

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
    val kitName     = kit?.name     ?: stringResource(R.string.kits_title)
    val kitColorKey = kit?.colorHex ?: "green"
    val kitIconKey  = kit?.iconName ?: "home"

    val drugs by viewModel.drugs.collectAsState()
    val dims = LocalDimens.current
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
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = stringResource(R.string.cd_back))
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
                        Icon(Icons.Outlined.Search, contentDescription = stringResource(R.string.cd_search))
                    }
                    IconButton(onClick = { /* TODO: kit menu */ }) {
                        Icon(Icons.Outlined.MoreVert, contentDescription = stringResource(R.string.cd_menu))
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
                        verticalArrangement = Arrangement.spacedBy(dims.itemGap),
                    ) {
                        FabMenuItem(
                            label   = stringResource(R.string.scan),
                            icon    = Icons.Outlined.QrCodeScanner,
                            onClick = { showFabMenu = false /* TODO: scanner */ },
                        )
                        FabMenuItem(
                            label   = stringResource(R.string.manual),
                            icon    = Icons.Outlined.Edit,
                            onClick = {
                                showFabMenu = false
                                navController.navigate(Screen.AddDrug.go(kitId))
                            },
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
                start  = dims.screenPadding,
                end    = dims.screenPadding,
                top    = padding.calculateTopPadding() + dims.xs,
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
                        onClick         = { navController.navigate(Screen.MedDetail.go(dw.drug.id)) },
                        onToCart        = { viewModel.addToShopping(dw.drug) },
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
            title = { Text(stringResource(R.string.delete_confirm_title, dw.drug.name)) },
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
    val dims = LocalDimens.current
    LazyRow(
        modifier            = modifier,
        horizontalArrangement = Arrangement.spacedBy(dims.itemGap),
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
    val dims          = LocalDimens.current
    val worstStatus   = drugWithBatches.worstStatus
    val nearestExpiry = drugWithBatches.nearestExpiry
    val totalQty      = drugWithBatches.totalQuantity

    val statusColor = when (worstStatus) {
        BatchStatus.EXPIRED      -> statusColors.expiredFg
        BatchStatus.EXPIRING_SOON -> statusColors.expiringFg
        else                     -> statusColors.okFg
    }

    val expiryText = if (nearestExpiry == null) {
        stringResource(R.string.expiry_unknown)
    } else {
        expiryLabel(nearestExpiry)
    }

    val formLabel = Forms.label(drug.form)

    val qtyNumber = if (totalQty == totalQty.toLong().toFloat()) {
        totalQty.toLong().toString()
    } else {
        "%.1f".format(totalQty)
    }
    val qtyStr = stringResource(R.string.qty_pieces, qtyNumber)

    Surface(
        modifier  = modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(dims.radiusMd),
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
                    .background(Forms.color(drug.form)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Forms.icon(drug.form),
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
                    text  = "$qtyStr · $expiryText",
                    style = MaterialTheme.typography.bodyMedium,
                    color = statusColor,
                )
                if (drugWithBatches.batches.size > 1) {
                    Text(
                        text  = "· " + pluralStringResource(
                            R.plurals.plural_batches,
                            drugWithBatches.batches.size,
                            drugWithBatches.batches.size,
                        ),
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

// ── FAB menu item ─────────────────────────────────────────────────────────────

@Composable
private fun FabMenuItem(label: String, icon: ImageVector, onClick: () -> Unit) {
    val dims = LocalDimens.current
    Row(verticalAlignment = Alignment.CenterVertically) {
        Surface(
            shape = RoundedCornerShape(dims.radiusPill),
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
