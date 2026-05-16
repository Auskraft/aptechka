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
import ru.aptechka.domain.model.BatchStatus
import ru.aptechka.domain.model.DrugBatch
import ru.aptechka.domain.model.Kit
import ru.aptechka.domain.model.UserDrug

data class BatchWithContext(
    val batch: DrugBatch,
    val drug: UserDrug,
    val kit: Kit,
)

class ExpiryViewModel(
    private val drugRepo: DrugRepository,
    private val kitRepo: KitRepository,
) : ViewModel() {

    data class UiState(
        val expired: List<BatchWithContext>  = emptyList(),
        val expiring: List<BatchWithContext> = emptyList(),
        val ok: List<BatchWithContext>       = emptyList(),
    )

    val uiState: StateFlow<UiState> = combine(
        drugRepo.observeAllBatches(),
        kitRepo.observeAll(),
    ) { allBatches, _ ->
        UiState(
            expired  = allBatches.filter { it.status == BatchStatus.EXPIRED }.map {
                BatchWithContext(it, makePlaceholderDrug(it.drugId), Kit(name = "Аптечка"))
            }.sortedBy { it.batch.expirationDate },
            expiring = allBatches.filter { it.status == BatchStatus.EXPIRING_SOON }.map {
                BatchWithContext(it, makePlaceholderDrug(it.drugId), Kit(name = "Аптечка"))
            }.sortedBy { it.batch.expirationDate },
            ok       = allBatches.filter { it.status == BatchStatus.ACTIVE }.map {
                BatchWithContext(it, makePlaceholderDrug(it.drugId), Kit(name = "Аптечка"))
            }.sortedBy { it.batch.expirationDate },
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UiState())

    private fun makePlaceholderDrug(drugId: Long) = UserDrug(
        id    = drugId,
        kitId = 0,
        name  = "Препарат #$drugId",
    )

    fun deleteBatch(batch: DrugBatch) {
        viewModelScope.launch { drugRepo.deleteBatch(batch) }
    }
}
