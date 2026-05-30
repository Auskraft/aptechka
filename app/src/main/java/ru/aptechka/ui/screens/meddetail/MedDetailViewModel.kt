package ru.aptechka.ui.screens.meddetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import ru.aptechka.data.repository.DrugRepository
import ru.aptechka.data.repository.ShoppingRepository
import ru.aptechka.domain.model.DrugBatch
import ru.aptechka.domain.model.ShoppingItem
import ru.aptechka.domain.model.UserDrug

class MedDetailViewModel(
    private val drugId: Long,
    private val drugRepo: DrugRepository,
    private val shoppingRepo: ShoppingRepository,
) : ViewModel() {

    private val _drug = MutableStateFlow<UserDrug?>(null)
    val drug: StateFlow<UserDrug?> = _drug.asStateFlow()

    val batches: StateFlow<List<DrugBatch>> = drugRepo.observeBatches(drugId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _deleted = MutableStateFlow(false)
    val deleted: StateFlow<Boolean> = _deleted.asStateFlow()

    init {
        viewModelScope.launch { _drug.value = drugRepo.getDrug(drugId) }
    }

    fun adjustBatchQty(batch: DrugBatch, delta: Float) {
        viewModelScope.launch {
            val newQty = (batch.quantity + delta).coerceAtLeast(0f)
            drugRepo.updateBatch(batch.copy(quantity = newQty))
        }
    }

    fun addBatch(expirationDate: Long, quantity: Float) {
        viewModelScope.launch {
            drugRepo.saveBatch(
                DrugBatch(drugId = drugId, quantity = quantity, expirationDate = expirationDate),
            )
        }
    }

    fun deleteBatch(batch: DrugBatch) {
        viewModelScope.launch { drugRepo.deleteBatch(batch) }
    }

    fun addToShopping() {
        val drug = _drug.value ?: return
        viewModelScope.launch {
            shoppingRepo.save(
                ShoppingItem(name = drug.name, unit = drug.unit, kitId = drug.kitId),
            )
        }
    }

    fun deleteDrug() {
        val drug = _drug.value ?: return
        viewModelScope.launch {
            drugRepo.deleteDrug(drug)
            _deleted.value = true
        }
    }
}
