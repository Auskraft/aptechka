package ru.aptechka.ui.screens.shopping

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import ru.aptechka.data.repository.ShoppingRepository
import ru.aptechka.domain.model.ShoppingItem

class ShoppingViewModel(private val repo: ShoppingRepository) : ViewModel() {

    val toBuy: StateFlow<List<ShoppingItem>> = repo.observeAll()
        .map { it.filter { item -> !item.isPurchased } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val purchased: StateFlow<List<ShoppingItem>> = repo.observeAll()
        .map { it.filter { item -> item.isPurchased } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun addItem(name: String) {
        viewModelScope.launch {
            repo.save(ShoppingItem(name = name))
        }
    }

    fun togglePurchased(item: ShoppingItem) {
        viewModelScope.launch {
            repo.update(item.copy(isPurchased = !item.isPurchased))
        }
    }

    fun deleteItem(item: ShoppingItem) {
        viewModelScope.launch { repo.delete(item) }
    }

    fun clearPurchased() {
        viewModelScope.launch { repo.clearPurchased() }
    }
}
