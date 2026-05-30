package ru.aptechka.ui.screens.expiry

import androidx.compose.foundation.background
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
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.koin.androidx.compose.koinViewModel
import ru.aptechka.R
import ru.aptechka.domain.model.BatchStatus
import ru.aptechka.domain.model.DrugBatch
import ru.aptechka.ui.common.SwipeAction
import ru.aptechka.ui.common.SwipeRow
import ru.aptechka.ui.common.rememberMessageSnackbarHostState
import ru.aptechka.ui.forms.expiryLabel
import ru.aptechka.ui.theme.LocalDimens
import ru.aptechka.ui.theme.LocalStatusColors
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpiryScreen(viewModel: ExpiryViewModel = koinViewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }

    val tabs = listOf(
        Triple(stringResource(R.string.tab_expired), uiState.expired.size, BatchStatus.EXPIRED),
        Triple(stringResource(R.string.tab_expiring), uiState.expiring.size, BatchStatus.EXPIRING_SOON),
        Triple(stringResource(R.string.tab_ok), uiState.ok.size, null),
    )

    val currentList = when (selectedTab) {
        0    -> uiState.expired
        1    -> uiState.expiring
        else -> uiState.ok
    }

    val statusColors = LocalStatusColors.current
    val dims = LocalDimens.current

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val snackbarHostState = rememberMessageSnackbarHostState(viewModel.snackbar.messages)

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            LargeTopAppBar(
                scrollBehavior = scrollBehavior,
                title = {
                    Column {
                        Text(
                            text  = stringResource(R.string.expiry_title),
                            style = MaterialTheme.typography.displayMedium,
                        )
                        val expiredCount  = uiState.expired.size
                        val expiringCount = uiState.expiring.size
                        if (expiredCount > 0 || expiringCount > 0) {
                            val sub = listOfNotNull(
                                stringResource(R.string.count_expired, expiredCount).takeIf { expiredCount > 0 },
                                stringResource(R.string.count_soon, expiringCount).takeIf { expiringCount > 0 },
                            ).joinToString(" · ")
                            Text(
                                text  = sub,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        Column(modifier = Modifier.padding(top = padding.calculateTopPadding())) {
            // Tabs
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor   = MaterialTheme.colorScheme.background,
                contentColor     = MaterialTheme.colorScheme.primary,
            ) {
                tabs.forEachIndexed { idx, (label, count, status) ->
                    Tab(
                        selected = selectedTab == idx,
                        onClick  = { selectedTab = idx },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(label)
                                if (count > 0 && status != null) {
                                    Spacer(Modifier.width(4.dp))
                                    val badgeColor = if (status == BatchStatus.EXPIRED)
                                        statusColors.expiredFg else statusColors.expiringFg
                                    Box(
                                        modifier         = Modifier
                                            .clip(CircleShape)
                                            .background(badgeColor)
                                            .padding(horizontal = 6.dp, vertical = 1.dp),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Text(
                                            text  = count.toString(),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color.White,
                                        )
                                    }
                                }
                            }
                        },
                    )
                }
            }

            if (currentList.isEmpty()) {
                ExpiryEmptyState(tab = selectedTab)
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(
                        start  = dims.screenPadding,
                        end    = dims.screenPadding,
                        top    = dims.md,
                        bottom = padding.calculateBottomPadding() + dims.screenPadding,
                    ),
                    verticalArrangement = Arrangement.spacedBy(dims.itemGap),
                ) {
                    items(currentList, key = { it.batch.id }) { ctx ->
                        SwipeRow(
                            startToEnd = SwipeAction(
                                icon = Icons.Outlined.AddShoppingCart,
                                container = MaterialTheme.colorScheme.primary,
                                iconTint = MaterialTheme.colorScheme.onPrimary,
                                dismiss = false,
                                onAction = { viewModel.addToShopping(ctx) },
                            ),
                            endToStart = SwipeAction(
                                icon = Icons.Outlined.Delete,
                                container = statusColors.expiredFg,
                                iconTint = Color.White,
                                dismiss = true,
                                onAction = { viewModel.deleteBatch(ctx.batch) },
                            ),
                        ) {
                            BatchRow(
                                ctx       = ctx,
                                onDelete  = { viewModel.deleteBatch(ctx.batch) },
                                onToCart  = { viewModel.addToShopping(ctx) },
                            )
                        }
                    }
                }
            }
        }
    }
}

// ── Batch row ─────────────────────────────────────────────────────────────────

@Composable
private fun BatchRow(
    ctx: BatchWithContext,
    onDelete: () -> Unit,
    onToCart: () -> Unit,
) {
    val batch        = ctx.batch
    val statusColors = LocalStatusColors.current
    val dims         = LocalDimens.current

    val (statusFg, statusContainer) = when (batch.status) {
        BatchStatus.EXPIRED       -> statusColors.expiredFg  to statusColors.expiredContainer
        BatchStatus.EXPIRING_SOON -> statusColors.expiringFg to statusColors.expiringContainer
        else                      -> statusColors.okFg       to statusColors.okContainer
    }

    val daysLabel = expiryLabel(batch.expirationDate, short = true)

    val dayStr  = SimpleDateFormat("d", Locale("ru")).format(Date(batch.expirationDate))
    val monStr  = SimpleDateFormat("MMM yy", Locale("ru")).format(Date(batch.expirationDate))

    Surface(
        shape = RoundedCornerShape(dims.radiusMd),
        color = MaterialTheme.colorScheme.surfaceContainer,
    ) {
        Row(modifier = Modifier.height(IntrinsicSize.Min)) {
            // Status stripe
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(statusFg),
            )

            // Date column
            Column(
                modifier            = Modifier
                    .padding(horizontal = 12.dp, vertical = 10.dp)
                    .width(48.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text  = dayStr,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontSize   = 24.sp,
                        fontWeight = FontWeight.Bold,
                    ),
                    color = statusFg,
                )
                Text(
                    text  = monStr,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            // Content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 10.dp),
            ) {
                Text(
                    text  = ctx.drug.name,
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    text  = "${ctx.kit.name} · " + stringResource(R.string.qty_pieces, batch.quantity.toInt().toString()),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text  = daysLabel,
                    style = MaterialTheme.typography.bodySmall,
                    color = statusFg,
                )
            }

            // Menu
            Box {
                var expanded by remember { mutableStateOf(false) }
                IconButton(onClick = { expanded = true }) {
                    Icon(Icons.Outlined.MoreVert, null)
                }
                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    DropdownMenuItem(
                        text        = { Text(stringResource(R.string.to_shopping)) },
                        onClick     = { expanded = false; onToCart() },
                        leadingIcon = { Icon(Icons.Outlined.ShoppingCart, null) },
                    )
                    DropdownMenuItem(
                        text        = { Text(stringResource(R.string.edit_expiry)) },
                        onClick     = { expanded = false /* TODO */ },
                        leadingIcon = { Icon(Icons.Outlined.EditCalendar, null) },
                    )
                    DropdownMenuItem(
                        text        = { Text(stringResource(R.string.delete), color = MaterialTheme.colorScheme.error) },
                        onClick     = { expanded = false; onDelete() },
                        leadingIcon = { Icon(Icons.Outlined.Delete, null, tint = MaterialTheme.colorScheme.error) },
                    )
                }
            }
        }
    }
}

// ── Empty states ──────────────────────────────────────────────────────────────

@Composable
private fun ExpiryEmptyState(tab: Int) {
    val (icon, text) = when (tab) {
        0    -> Icons.Outlined.CheckCircleOutline to stringResource(R.string.empty_expired)
        1    -> Icons.Outlined.CalendarMonth      to stringResource(R.string.empty_expiring)
        else -> Icons.Outlined.Inventory2         to stringResource(R.string.empty_ok)
    }
    Box(
        modifier         = Modifier
            .fillMaxWidth()
            .padding(top = 80.dp),
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
                Icon(icon, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(32.dp))
            }
            Spacer(Modifier.height(24.dp))
            Text(text, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}
