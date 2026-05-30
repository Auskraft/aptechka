package ru.aptechka.ui.screens.expiry

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import ru.aptechka.data.repository.DrugRepository
import ru.aptechka.data.repository.KitRepository
import ru.aptechka.data.repository.ShoppingRepository
import ru.aptechka.domain.model.BatchStatus
import ru.aptechka.domain.model.DrugBatch
import ru.aptechka.domain.model.Kit
import ru.aptechka.domain.model.ShoppingItem
import ru.aptechka.domain.model.UserDrug
import ru.aptechka.ui.common.SnackbarDispatcher
import ru.aptechka.ui.common.SnackbarMessage

data class BatchWithContext(
    val batch: DrugBatch,
    val drug: UserDrug,
    val kit: Kit,
)

data class ExpiryUiState(
    val expired: List<BatchWithContext>  = emptyList(),
    val expiring: List<BatchWithContext> = emptyList(),
    val ok: List<BatchWithContext>       = emptyList(),
)

/**
 * Joins batches with their drug and kit, dropping orphans (drug/kit missing),
 * and partitions by status sorted by soonest expiry. Pure → unit-testable.
 */
internal fun buildExpiryUiState(
    batches: List<DrugBatch>,
    drugs: List<UserDrug>,
    kits: List<Kit>,
): ExpiryUiState {
    val drugById = drugs.associateBy { it.id }
    val kitById = kits.associateBy { it.id }

    val contexts = batches.mapNotNull { batch ->
        val drug = drugById[batch.drugId] ?: return@mapNotNull null
        val kit = kitById[drug.kitId] ?: return@mapNotNull null
        BatchWithContext(batch, drug, kit)
    }

    fun ofStatus(status: BatchStatus) =
        contexts.filter { it.batch.status == status }.sortedBy { it.batch.expirationDate }

    return ExpiryUiState(
        expired  = ofStatus(BatchStatus.EXPIRED),
        expiring = ofStatus(BatchStatus.EXPIRING_SOON),
        ok       = ofStatus(BatchStatus.ACTIVE),
    )
}

class ExpiryViewModel(
    private val drugRepo: DrugRepository,
    kitRepo: KitRepository,
    private val shoppingRepo: ShoppingRepository,
) : ViewModel() {

    val snackbar = SnackbarDispatcher()

    val uiState: StateFlow<ExpiryUiState> = combine(
        drugRepo.observeAllBatches(),
        drugRepo.observeAllDrugs(),
        kitRepo.observeAll(),
    ) { batches, drugs, kits ->
        buildExpiryUiState(batches, drugs, kits)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ExpiryUiState())

    fun deleteBatch(batch: DrugBatch) {
        viewModelScope.launch {
            drugRepo.deleteBatch(batch)
            snackbar.show(SnackbarMessage.BatchDeleted)
        }
    }

    fun addToShopping(ctx: BatchWithContext) {
        viewModelScope.launch {
            shoppingRepo.save(
                ShoppingItem(name = ctx.drug.name, unit = ctx.drug.unit, kitId = ctx.kit.id),
            )
            snackbar.show(SnackbarMessage.ToShopping(ctx.drug.name))
        }
    }
}
