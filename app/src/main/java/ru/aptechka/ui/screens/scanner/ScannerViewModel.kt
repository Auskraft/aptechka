package ru.aptechka.ui.screens.scanner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ru.aptechka.data.repository.CatalogRepository
import ru.aptechka.domain.model.CatalogDrug

data class ScanResult(val barcode: String, val match: CatalogDrug?)

class ScannerViewModel(
    private val catalogRepo: CatalogRepository,
) : ViewModel() {

    private val _result = MutableStateFlow<ScanResult?>(null)
    /** null while scanning; set once a barcode is recognized. */
    val result: StateFlow<ScanResult?> = _result.asStateFlow()

    fun onBarcodeScanned(barcode: String) {
        if (_result.value != null) return // already handled one
        viewModelScope.launch {
            _result.value = ScanResult(barcode, catalogRepo.findByBarcode(barcode))
        }
    }

    /** Resume scanning ("Повторить" / "Не он"). */
    fun reset() {
        _result.value = null
    }
}
