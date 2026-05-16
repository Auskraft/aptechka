package ru.aptechka.ui.screens.shopping

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import org.koin.androidx.compose.koinViewModel
import ru.aptechka.R
import ru.aptechka.domain.model.ShoppingItem
import ru.aptechka.ui.theme.LocalStatusColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShoppingScreen(viewModel: ShoppingViewModel = koinViewModel()) {
    val toBuy     by viewModel.toBuy.collectAsState()
    val purchased by viewModel.purchased.collectAsState()
    var showAddSheet by remember { mutableStateOf(false) }

    val statusColors = LocalStatusColors.current

    Scaffold(
        topBar = {
            LargeTopAppBar(
                title = {
                    Column {
                        Text(
                            text  = stringResource(R.string.shopping_title),
                            style = MaterialTheme.typography.displayMedium,
                        )
                        if (toBuy.isNotEmpty() || purchased.isNotEmpty()) {
                            Text(
                                text  = buildString {
                                    if (toBuy.isNotEmpty())     append("${toBuy.size} к покупке")
                                    if (toBuy.isNotEmpty() && purchased.isNotEmpty()) append(" · ")
                                    if (purchased.isNotEmpty()) append("${purchased.size} разнести")
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { showAddSheet = true }) {
                        Icon(Icons.Outlined.Add, contentDescription = "Добавить")
                    }
                },
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        if (toBuy.isEmpty() && purchased.isEmpty()) {
            ShoppingEmptyState(
                modifier = Modifier.padding(padding),
                onAdd    = { showAddSheet = true },
            )
        } else {
            LazyColumn(
                contentPadding = PaddingValues(
                    start  = 16.dp,
                    end    = 16.dp,
                    top    = padding.calculateTopPadding() + 8.dp,
                    bottom = padding.calculateBottomPadding() + 16.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                // Purchased block (amber container)
                if (purchased.isNotEmpty()) {
                    item {
                        PurchasedBlock(
                            items     = purchased,
                            onDistribute = { /* TODO: open allocate form */ },
                        )
                    }
                }

                // To-buy header
                if (toBuy.isNotEmpty()) {
                    item {
                        SectionHeader("${stringResource(R.string.to_buy_header)} (${toBuy.size})")
                    }
                    items(toBuy, key = { it.id }) { item ->
                        ShopCard(
                            item      = item,
                            onToggle  = { viewModel.togglePurchased(item) },
                            onDelete  = { viewModel.deleteItem(item) },
                        )
                    }
                }
            }
        }
    }

    if (showAddSheet) {
        AddItemSheet(
            onDismiss = { showAddSheet = false },
            onConfirm = { name ->
                viewModel.addItem(name)
                showAddSheet = false
            },
        )
    }
}

// ── Purchased block ───────────────────────────────────────────────────────────

@Composable
private fun PurchasedBlock(items: List<ShoppingItem>, onDistribute: (ShoppingItem) -> Unit) {
    val statusColors = LocalStatusColors.current
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = statusColors.expiringContainer,
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier         = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White.copy(alpha = 0.4f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Outlined.Archive, null, tint = statusColors.expiringFg, modifier = Modifier.size(20.dp))
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        text  = "${stringResource(R.string.purchased_header)} · ${items.size}",
                        style = MaterialTheme.typography.titleMedium,
                        color = statusColors.expiringFg,
                    )
                    Text(
                        text  = stringResource(R.string.distribute_hint),
                        style = MaterialTheme.typography.bodySmall,
                        color = statusColors.expiringFg,
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
            items.forEach { item ->
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface,
                ) {
                    Row(
                        modifier      = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(item.name, style = MaterialTheme.typography.titleSmall)
                            Text("${item.quantity.toInt()} шт", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Button(
                            onClick = { onDistribute(item) },
                            colors  = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                            ),
                        ) {
                            Text(stringResource(R.string.distribute))
                        }
                    }
                }
            }
        }
    }
}

// ── Shop card ─────────────────────────────────────────────────────────────────

@Composable
private fun ShopCard(item: ShoppingItem, onToggle: () -> Unit, onDelete: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
    ) {
        Row(
            modifier      = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Checkbox
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .border(2.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(4.dp))
                    .clickable(onClick = onToggle),
                contentAlignment = Alignment.Center,
            ) {
                // empty — not purchased
            }
            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(item.name, style = MaterialTheme.typography.titleMedium)
                Text(
                    "${item.quantity.toInt()} шт",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            IconButton(onClick = onDelete) {
                Icon(Icons.Outlined.Close, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

// ── Section header ────────────────────────────────────────────────────────────

@Composable
private fun SectionHeader(text: String) {
    Text(
        text     = text,
        style    = MaterialTheme.typography.titleMedium,
        color    = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.padding(vertical = 8.dp),
    )
}

// ── Empty state ───────────────────────────────────────────────────────────────

@Composable
private fun ShoppingEmptyState(modifier: Modifier = Modifier, onAdd: () -> Unit) {
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
                Icon(Icons.Outlined.ShoppingCart, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(32.dp))
            }
            Spacer(Modifier.height(24.dp))
            Text(stringResource(R.string.shopping_empty_title), style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(8.dp))
            Text(
                stringResource(R.string.shopping_empty_body),
                style     = MaterialTheme.typography.bodyMedium,
                color     = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(24.dp))
            Button(onClick = onAdd) {
                Icon(Icons.Outlined.Add, null)
                Spacer(Modifier.width(8.dp))
                Text("Добавить препарат")
            }
        }
    }
}

// ── Add item sheet ────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddItemSheet(onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    var name by remember { mutableStateOf("") }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text("Добавить в покупки", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(
                value         = name,
                onValueChange = { name = it },
                label         = { Text("Название препарата") },
                singleLine    = true,
                modifier      = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(16.dp))
            Button(
                onClick  = { if (name.isNotBlank()) onConfirm(name.trim()) },
                enabled  = name.isNotBlank(),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Добавить")
            }
            Spacer(Modifier.height(32.dp))
        }
    }
}
