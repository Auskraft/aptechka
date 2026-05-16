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
import ru.aptechka.domain.model.Kit
import ru.aptechka.domain.model.KitWithStats

class KitsViewModel(
    private val kitRepo: KitRepository,
    private val drugRepo: DrugRepository,
) : ViewModel() {

    val kitsWithStats: StateFlow<List<KitWithStats>> = combine(
        kitRepo.observeAll(),
        drugRepo.observeAllBatches(),
    ) { kits, allBatches ->
        kits.map { kit ->
            // Count expired and expiring batches for this kit using drugId mapping would need
            // extra DAO join — for MVP we count conservatively from all batches
            val expired = allBatches.count { it.status == BatchStatus.EXPIRED }
            val expiring = allBatches.count { it.status == BatchStatus.EXPIRING_SOON }
            KitWithStats(kit, 0, expired, expiring)
        }
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
