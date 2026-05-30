package ru.aptechka.ui.screens.kits

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import ru.aptechka.data.repository.DrugRepository
import ru.aptechka.data.repository.KitRepository
import ru.aptechka.data.repository.ShoppingRepository
import ru.aptechka.domain.model.DrugBatch
import ru.aptechka.domain.model.Kit
import ru.aptechka.domain.model.ShoppingItem
import ru.aptechka.domain.model.UserDrug
import ru.aptechka.domain.model.UserDrugWithBatches

class KitDetailViewModel(
    private val kitId: Long,
    private val drugRepo: DrugRepository,
    private val kitRepo: KitRepository,
    private val shoppingRepo: ShoppingRepository,
) : ViewModel() {

    private val _kit = MutableStateFlow<Kit?>(null)
    val kit: StateFlow<Kit?> = _kit.asStateFlow()

    val drugs: StateFlow<List<UserDrugWithBatches>> = drugRepo.observeByKit(kitId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    init {
        viewModelScope.launch {
            _kit.value = kitRepo.getById(kitId)
        }
    }

    fun deleteDrug(drug: UserDrug) {
        viewModelScope.launch { drugRepo.deleteDrug(drug) }
    }

    fun addToShopping(drug: UserDrug) {
        viewModelScope.launch {
            shoppingRepo.save(ShoppingItem(name = drug.name, unit = drug.unit, kitId = drug.kitId))
        }
    }

    fun deleteBatch(batch: DrugBatch) {
        viewModelScope.launch { drugRepo.deleteBatch(batch) }
    }

    fun adjustBatchQty(batch: DrugBatch, delta: Float) {
        viewModelScope.launch {
            val newQty = (batch.quantity + delta).coerceAtLeast(0f)
            drugRepo.updateBatch(batch.copy(quantity = newQty))
        }
    }
}
