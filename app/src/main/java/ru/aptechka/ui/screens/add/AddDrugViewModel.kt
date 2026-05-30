package ru.aptechka.ui.screens.add

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ru.aptechka.data.repository.DrugRepository
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
) : ViewModel() {

    private val _state = MutableStateFlow(AddDrugState())
    val state: StateFlow<AddDrugState> = _state.asStateFlow()

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
