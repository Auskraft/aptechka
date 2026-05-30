package ru.aptechka.ui.screens.meddetail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import ru.aptechka.R
import ru.aptechka.domain.model.BatchStatus
import ru.aptechka.domain.model.DrugBatch
import ru.aptechka.domain.model.UserDrug
import ru.aptechka.ui.forms.Forms
import ru.aptechka.ui.forms.expiryLabel
import ru.aptechka.ui.theme.LocalDimens
import ru.aptechka.ui.theme.LocalStatusColors
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedDetailScreen(
    drugId: Long,
    navController: NavController,
    viewModel: MedDetailViewModel = koinViewModel(parameters = { parametersOf(drugId) }),
) {
    val drug by viewModel.drug.collectAsState()
    val batches by viewModel.batches.collectAsState()
    val deleted by viewModel.deleted.collectAsState()
    val dims = LocalDimens.current

    var showAddBatch by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(deleted) { if (deleted) navController.popBackStack() }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = stringResource(R.string.cd_back))
                    }
                },
                title = {
                    Text(
                        text = drug?.name.orEmpty(),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
                actions = {
                    Box {
                        var expanded by remember { mutableStateOf(false) }
                        IconButton(onClick = { expanded = true }) {
                            Icon(Icons.Outlined.MoreVert, contentDescription = stringResource(R.string.cd_menu))
                        }
                        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.delete_drug), color = MaterialTheme.colorScheme.error) },
                                onClick = { expanded = false; showDeleteDialog = true },
                                leadingIcon = { Icon(Icons.Outlined.Delete, null, tint = MaterialTheme.colorScheme.error) },
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        drug?.let { d ->
            LazyColumn(
                contentPadding = PaddingValues(
                    start = dims.screenPadding,
                    end = dims.screenPadding,
                    top = padding.calculateTopPadding() + dims.sm,
                    bottom = padding.calculateBottomPadding() + dims.xxxl,
                ),
                verticalArrangement = Arrangement.spacedBy(dims.cardGap),
            ) {
                item { Hero(d) }
                item { ActionRow(onToShopping = { viewModel.addToShopping() }) }
                item { StatsCard(batches) }
                item {
                    Text(
                        text = stringResource(R.string.section_batches),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(top = dims.sm),
                    )
                }
                items(batches, key = { it.id }) { batch ->
                    BatchCard(
                        batch = batch,
                        onMinus = { viewModel.adjustBatchQty(batch, -1f) },
                        onPlus = { viewModel.adjustBatchQty(batch, +1f) },
                        onDelete = { viewModel.deleteBatch(batch) },
                    )
                }
                item { AddBatchCta(onClick = { showAddBatch = true }) }
                if (d.notes.isNotBlank()) {
                    item { NotesCard(d.notes) }
                }
            }
        } ?: Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    }

    if (showAddBatch) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showAddBatch = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { viewModel.addBatch(it, 1f) }
                        showAddBatch = false
                    },
                    enabled = datePickerState.selectedDateMillis != null,
                ) { Text(stringResource(R.string.save)) }
            },
            dismissButton = {
                TextButton(onClick = { showAddBatch = false }) { Text(stringResource(R.string.cancel)) }
            },
        ) { DatePicker(state = datePickerState) }
    }

    if (showDeleteDialog) {
        val name = drug?.name.orEmpty()
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(stringResource(R.string.delete_confirm_title, name)) },
            text = { Text(stringResource(R.string.delete_drug_confirm)) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteDrug()
                    showDeleteDialog = false
                }) { Text(stringResource(R.string.delete), color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text(stringResource(R.string.cancel)) }
            },
        )
    }
}

// ── Hero ──────────────────────────────────────────────────────────────────────

@Composable
private fun Hero(drug: UserDrug) {
    val dims = LocalDimens.current
    Surface(
        shape = RoundedCornerShape(dims.radiusLg),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(dims.lg),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(dims.radiusMd))
                    .background(Forms.color(drug.form)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Forms.icon(drug.form), null, tint = Color.White, modifier = Modifier.size(36.dp))
            }
            Spacer(Modifier.width(dims.lg))
            Column {
                Text(drug.name, style = MaterialTheme.typography.headlineSmall)
                Spacer(Modifier.height(dims.xs))
                AssistChip(
                    onClick = {},
                    enabled = false,
                    label = { Text(Forms.label(drug.category)) },
                )
                Text(
                    text = Forms.label(drug.form),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = dims.xs),
                )
            }
        }
    }
}

// ── Action row ──────────────────────────────────────────────────────────────

@Composable
private fun ActionRow(onToShopping: () -> Unit) {
    FilledTonalButton(
        onClick = onToShopping,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Icon(Icons.Outlined.ShoppingCart, null, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(LocalDimens.current.sm))
        Text(stringResource(R.string.to_shopping))
    }
}

// ── Stats card ────────────────────────────────────────────────────────────────

@Composable
private fun StatsCard(batches: List<DrugBatch>) {
    val dims = LocalDimens.current
    val totalQty = batches.filter { it.status != BatchStatus.EXPIRED }
        .sumOf { it.quantity.toDouble() }.toFloat()

    Surface(
        shape = RoundedCornerShape(dims.radiusMd),
        color = MaterialTheme.colorScheme.surfaceContainer,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(dims.lg),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            StatCell(
                value = formatQty(totalQty),
                label = stringResource(R.string.med_qty_total),
                modifier = Modifier.weight(1f),
            )
            VerticalDivider(modifier = Modifier.height(40.dp))
            StatCell(
                value = batches.size.toString(),
                label = stringResource(R.string.section_batches),
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun StatCell(value: String, label: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
        )
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

// ── Batch card ────────────────────────────────────────────────────────────────

@Composable
private fun BatchCard(
    batch: DrugBatch,
    onMinus: () -> Unit,
    onPlus: () -> Unit,
    onDelete: () -> Unit,
) {
    val dims = LocalDimens.current
    val statusColors = LocalStatusColors.current
    val statusFg = when (batch.status) {
        BatchStatus.EXPIRED -> statusColors.expiredFg
        BatchStatus.EXPIRING_SOON -> statusColors.expiringFg
        else -> statusColors.okFg
    }
    val monthYear = remember(batch.expirationDate) {
        SimpleDateFormat("LLLL yyyy", Locale("ru")).format(Date(batch.expirationDate))
            .replaceFirstChar { it.uppercase() }
    }

    Surface(
        shape = RoundedCornerShape(dims.radiusMd),
        color = MaterialTheme.colorScheme.surfaceContainer,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(modifier = Modifier.height(IntrinsicSize.Min)) {
            Box(
                Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(statusFg),
            )
            Column(modifier = Modifier.padding(dims.md).weight(1f)) {
                Text(monthYear, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = expiryLabel(batch.expirationDate),
                    style = MaterialTheme.typography.bodyMedium,
                    color = statusFg,
                )
                Spacer(Modifier.height(dims.sm))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    FilledTonalIconButton(onClick = onMinus, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Outlined.Remove, contentDescription = stringResource(R.string.cd_decrease))
                    }
                    Text(
                        text = formatQty(batch.quantity),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(horizontal = dims.md),
                    )
                    FilledTonalIconButton(onClick = onPlus, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Outlined.Add, contentDescription = stringResource(R.string.cd_increase))
                    }
                    Spacer(Modifier.weight(1f))
                    IconButton(onClick = onDelete) {
                        Icon(
                            Icons.Outlined.Delete,
                            contentDescription = stringResource(R.string.cd_delete),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

// ── Add-batch CTA ─────────────────────────────────────────────────────────────

@Composable
private fun AddBatchCta(onClick: () -> Unit) {
    OutlinedButton(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Icon(Icons.Outlined.Add, null, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(LocalDimens.current.sm))
        Text(stringResource(R.string.add_batch))
    }
}

// ── Notes ─────────────────────────────────────────────────────────────────────

@Composable
private fun NotesCard(notes: String) {
    val dims = LocalDimens.current
    Surface(
        shape = RoundedCornerShape(dims.radiusMd),
        color = MaterialTheme.colorScheme.surfaceContainer,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(dims.lg)) {
            Text(stringResource(R.string.how_to_take), style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(dims.sm))
            Text(notes, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

private fun formatQty(qty: Float): String =
    if (qty == qty.toLong().toFloat()) qty.toLong().toString() else "%.1f".format(qty)
