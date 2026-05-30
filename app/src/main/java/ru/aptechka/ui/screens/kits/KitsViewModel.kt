package ru.aptechka.ui.screens.kits

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
import ru.aptechka.domain.model.KitWithStats
import ru.aptechka.domain.model.UserDrug

/**
 * Per-kit stats: drug count and expired/expiring batch counts scoped to each
 * kit (batch → drug → kit). Pure → unit-testable.
 */
internal fun buildKitsWithStats(
    kits: List<Kit>,
    drugs: List<UserDrug>,
    batches: List<DrugBatch>,
): List<KitWithStats> {
    val drugsByKit = drugs.groupBy { it.kitId }
    val batchesByDrug = batches.groupBy { it.drugId }
    return kits.map { kit ->
        val kitDrugs = drugsByKit[kit.id].orEmpty()
        val kitBatches = kitDrugs.flatMap { batchesByDrug[it.id].orEmpty() }
        KitWithStats(
            kit = kit,
            totalDrugs = kitDrugs.size,
            expiredCount = kitBatches.count { it.status == BatchStatus.EXPIRED },
            expiringSoonCount = kitBatches.count { it.status == BatchStatus.EXPIRING_SOON },
        )
    }
}

class KitsViewModel(
    private val kitRepo: KitRepository,
    private val drugRepo: DrugRepository,
) : ViewModel() {

    val kitsWithStats: StateFlow<List<KitWithStats>> = combine(
        kitRepo.observeAll(),
        drugRepo.observeAllDrugs(),
        drugRepo.observeAllBatches(),
    ) { kits, drugs, batches ->
        buildKitsWithStats(kits, drugs, batches)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val kits: StateFlow<List<Kit>> = kitRepo.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun createKit(name: String, colorKey: String = "green", iconKey: String = "home") {
        viewModelScope.launch {
            kitRepo.save(
                Kit(
                    name     = name,
                    colorHex = colorKey,
                    iconName = iconKey,
                )
            )
        }
    }

    fun deleteKit(kit: Kit) {
        viewModelScope.launch { kitRepo.delete(kit) }
    }
}
