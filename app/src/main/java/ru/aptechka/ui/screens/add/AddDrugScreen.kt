package ru.aptechka.ui.screens.add

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import ru.aptechka.R
import ru.aptechka.domain.model.CatalogDrug
import ru.aptechka.domain.model.CategoryKey
import ru.aptechka.domain.model.FormKey
import ru.aptechka.ui.forms.Forms
import ru.aptechka.ui.theme.LocalDimens
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddDrugScreen(
    kitId: Long,
    catalogId: Long = -1L,
    navController: NavController,
    viewModel: AddDrugViewModel = koinViewModel(parameters = { parametersOf(kitId, catalogId) }),
) {
    val state by viewModel.state.collectAsState()
    val saved by viewModel.saved.collectAsState()

    LaunchedEffect(saved) {
        if (saved) navController.popBackStack()
    }

    val suggestions by viewModel.suggestions.collectAsState()
    var showSuggestions by remember { mutableStateOf(false) }
    var showFormSheet by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Outlined.Close, contentDescription = stringResource(R.string.cancel))
                    }
                },
                title = { Text(stringResource(R.string.new_drug_title)) },
                actions = {
                    TextButton(onClick = { viewModel.save() }, enabled = state.canSave) {
                        Text(stringResource(R.string.save))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        val dims = LocalDimens.current
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = dims.screenPadding),
            verticalArrangement = Arrangement.spacedBy(dims.xl),
        ) {
            Spacer(Modifier.height(4.dp))

            // ── Основное ─────────────────────────────────────────────
            SectionGroup(title = stringResource(R.string.section_main)) {
                OutlinedTextField(
                    value = state.name,
                    onValueChange = {
                        viewModel.onName(it)
                        showSuggestions = true
                    },
                    label = { Text(stringResource(R.string.field_drug_name)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )

                if (showSuggestions && suggestions.isNotEmpty()) {
                    CatalogSuggestions(
                        suggestions = suggestions,
                        onPick = {
                            viewModel.applyCatalog(it)
                            showSuggestions = false
                        },
                    )
                }

                // Форма выпуска picker
                FieldRow(
                    label = stringResource(R.string.field_form),
                    value = Forms.label(state.form),
                    leadingTile = {
                        FormTile(state.form, size = 40)
                    },
                    onClick = { showFormSheet = true },
                )

                OutlinedTextField(
                    value = state.dosage,
                    onValueChange = viewModel::onDosage,
                    label = { Text(stringResource(R.string.field_dosage)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )

                CategoryDropdown(
                    selected = state.category,
                    onSelect = viewModel::onCategory,
                )
            }

            // ── Партия ──────────────────────────────────────────────
            SectionGroup(title = stringResource(R.string.section_batch)) {
                FieldRow(
                    label = stringResource(R.string.field_expiry),
                    value = state.expirationDate?.let { fmtDate(it) }
                        ?: stringResource(R.string.field_expiry_hint),
                    leadingTile = {
                        Icon(
                            Icons.Outlined.CalendarMonth,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    },
                    onClick = { showDatePicker = true },
                )

                OutlinedTextField(
                    value = state.quantity,
                    onValueChange = viewModel::onQuantity,
                    label = { Text(stringResource(R.string.field_quantity)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    suffix = { Text(stringResource(R.string.unit_pieces)) },
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            // ── Дополнительно ───────────────────────────────────────
            SectionGroup(title = stringResource(R.string.section_extra)) {
                OutlinedTextField(
                    value = state.notes,
                    onValueChange = viewModel::onNotes,
                    label = { Text(stringResource(R.string.field_notes)) },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            Spacer(Modifier.height(24.dp))
        }
    }

    if (showFormSheet) {
        FormPickerSheet(
            selected = state.form,
            onSelect = {
                viewModel.onForm(it)
                showFormSheet = false
            },
            onDismiss = { showFormSheet = false },
        )
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = state.expirationDate,
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.onExpirationDate(datePickerState.selectedDateMillis)
                    showDatePicker = false
                }) { Text(stringResource(R.string.save)) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(stringResource(R.string.cancel))
                }
            },
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

// ── Section group ───────────────────────────────────────────────────────────

@Composable
private fun SectionGroup(title: String, content: @Composable ColumnScope.() -> Unit) {
    val dims = LocalDimens.current
    Column(verticalArrangement = Arrangement.spacedBy(dims.md)) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 4.dp),
        )
        content()
    }
}

// ── Pressable field row ─────────────────────────────────────────────────────

@Composable
private fun FieldRow(
    label: String,
    value: String,
    leadingTile: @Composable () -> Unit,
    onClick: () -> Unit,
) {
    val dims = LocalDimens.current
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(dims.radiusSm),
        color = MaterialTheme.colorScheme.surfaceContainer,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            leadingTile()
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleMedium,
                )
            }
            Icon(Icons.Outlined.ChevronRight, contentDescription = null)
        }
    }
}

// ── Category dropdown ───────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoryDropdown(
    selected: CategoryKey,
    onSelect: (CategoryKey) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
    ) {
        OutlinedTextField(
            value = Forms.label(selected),
            onValueChange = {},
            readOnly = true,
            label = { Text(stringResource(R.string.field_category)) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(MenuAnchorType.PrimaryNotEditable),
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            CategoryKey.entries.forEach { cat ->
                DropdownMenuItem(
                    text = { Text(Forms.label(cat)) },
                    onClick = {
                        onSelect(cat)
                        expanded = false
                    },
                )
            }
        }
    }
}

// ── Form picker bottom sheet ────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FormPickerSheet(
    selected: FormKey,
    onSelect: (FormKey) -> Unit,
    onDismiss: () -> Unit,
) {
    val dims = LocalDimens.current
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Text(
            text = stringResource(R.string.field_form),
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(horizontal = dims.xxl, vertical = dims.sm),
        )
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(horizontal = dims.screenPadding, vertical = dims.sm),
            horizontalArrangement = Arrangement.spacedBy(dims.cardGap),
            verticalArrangement = Arrangement.spacedBy(dims.cardGap),
            modifier = Modifier.heightIn(max = 420.dp),
        ) {
            items(FormKey.entries.toList()) { form ->
                val isSelected = form == selected
                Surface(
                    onClick = { onSelect(form) },
                    shape = RoundedCornerShape(dims.radiusSm),
                    color = if (isSelected) MaterialTheme.colorScheme.secondaryContainer
                    else MaterialTheme.colorScheme.surfaceContainer,
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        FormTile(form, size = 40)
                        Spacer(Modifier.width(12.dp))
                        Text(Forms.label(form), style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
        }
        Spacer(Modifier.height(16.dp))
    }
}

// ── Catalog autocomplete suggestions ─────────────────────────────────────────

@Composable
private fun CatalogSuggestions(suggestions: List<CatalogDrug>, onPick: (CatalogDrug) -> Unit) {
    val dims = LocalDimens.current
    Surface(
        shape = RoundedCornerShape(dims.radiusSm),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        tonalElevation = 3.dp,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column {
            suggestions.take(6).forEach { drug ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onPick(drug) }
                        .padding(horizontal = dims.lg, vertical = dims.md),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(dims.radiusXs))
                            .background(Forms.color(drug.form)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Forms.icon(drug.form), null, tint = Color.White, modifier = Modifier.size(18.dp))
                    }
                    Spacer(Modifier.width(dims.md))
                    Column(Modifier.weight(1f)) {
                        Text(
                            drug.name,
                            style = MaterialTheme.typography.titleSmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            "${Forms.label(drug.form)} · ${Forms.label(drug.category)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
        }
    }
}

// ── Form tile ───────────────────────────────────────────────────────────────

@Composable
private fun FormTile(form: FormKey, size: Int) {
    Box(
        modifier = Modifier
            .size(size.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(Forms.color(form)),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            Forms.icon(form),
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size((size * 0.5).dp),
        )
    }
}

// ── Date formatting ─────────────────────────────────────────────────────────

private val dateFmt = SimpleDateFormat("d MMMM yyyy", Locale("ru"))
private fun fmtDate(ts: Long): String = dateFmt.format(java.util.Date(ts))
