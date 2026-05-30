package ru.aptechka.ui.screens.add

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import ru.aptechka.data.repository.CatalogRepository
import ru.aptechka.data.repository.DrugRepository
import ru.aptechka.domain.model.CatalogDrug
import ru.aptechka.domain.model.CategoryKey
import ru.aptechka.domain.model.DrugBatch
import ru.aptechka.domain.model.FormKey
import ru.aptechka.domain.model.UnitKey
import ru.aptechka.domain.model.UserDrug

data class AddDrugState(
    val name: String = "",
    val form: FormKey = FormKey.TABLET,
    val dosage: String = "",
    val unit: UnitKey = UnitKey.MG,
    val category: CategoryKey = CategoryKey.OTHER,
    val notes: String = "",
    val quantity: String = "1",
    val expirationDate: Long? = null,
) {
    val canSave: Boolean
        get() = name.isNotBlank() &&
            expirationDate != null &&
            (quantity.toFloatOrNull() ?: 0f) > 0f
}

class AddDrugViewModel(
    private val kitId: Long,
    private val drugRepo: DrugRepository,
    private val catalogRepo: CatalogRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(AddDrugState())
    val state: StateFlow<AddDrugState> = _state.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val suggestions: StateFlow<List<CatalogDrug>> = _state
        .map { it.name }
        .distinctUntilChanged()
        .mapLatest { query ->
            val q = query.trim()
            if (q.length >= 2) catalogRepo.search(q) else emptyList()
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _saved = MutableStateFlow(false)
    val saved: StateFlow<Boolean> = _saved.asStateFlow()

    fun onName(value: String) = _state.update { it.copy(name = value) }
    fun onForm(value: FormKey) = _state.update { it.copy(form = value) }
    fun onDosage(value: String) = _state.update { it.copy(dosage = value) }
    fun onUnit(value: UnitKey) = _state.update { it.copy(unit = value) }
    fun onCategory(value: CategoryKey) = _state.update { it.copy(category = value) }
    fun onNotes(value: String) = _state.update { it.copy(notes = value) }
    fun onQuantity(value: String) = _state.update { it.copy(quantity = value.filter { c -> c.isDigit() || c == '.' }) }
    fun onExpirationDate(value: Long?) = _state.update { it.copy(expirationDate = value) }

    fun applyCatalog(drug: CatalogDrug) = _state.update {
        it.copy(name = drug.name, form = drug.form, category = drug.category, unit = drug.unit)
    }

    fun save() {
        val s = _state.value
        if (!s.canSave) return
        viewModelScope.launch {
            val drugId = drugRepo.saveDrug(
                UserDrug(
                    kitId = kitId,
                    name = s.name.trim(),
                    form = s.form,
                    unit = s.unit,
                    category = s.category,
                    notes = s.notes.trim(),
                )
            )
            drugRepo.saveBatch(
                DrugBatch(
                    drugId = drugId,
                    quantity = s.quantity.toFloatOrNull() ?: 1f,
                    expirationDate = s.expirationDate!!,
                )
            )
            _saved.value = true
        }
    }
}

private inline fun <T> MutableStateFlow<T>.update(transform: (T) -> T) {
    value = transform(value)
}
