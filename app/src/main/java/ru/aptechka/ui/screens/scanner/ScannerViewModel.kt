package ru.aptechka.ui.screens.scanner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ru.aptechka.data.repository.CatalogRepository
import ru.aptechka.domain.model.CatalogDrug

/** A recognized package, matched to a catalog drug. */
data class ScanResult(val recognized: String, val match: CatalogDrug)

class ScannerViewModel(
    private val catalogRepo: CatalogRepository,
) : ViewModel() {

    private val _result = MutableStateFlow<ScanResult?>(null)
    /** null while scanning; set once a package name is matched to the catalog. */
    val result: StateFlow<ScanResult?> = _result.asStateFlow()

    private var inFlight = false

    /** Called with OCR'd text from each camera frame; matches drug name to catalog. */
    fun onTextRecognized(text: String) {
        if (_result.value != null || inFlight) return
        inFlight = true
        viewModelScope.launch {
            try {
                matchCatalog(text)?.let { hit ->
                    _result.value = ScanResult(hit.internationalName.ifBlank { hit.name }, hit)
                }
            } finally {
                inFlight = false
            }
        }
    }

    private suspend fun matchCatalog(text: String): CatalogDrug? {
        val tokens = text.split(Regex("[^\\p{L}]+"))
            .map { it.trim() }
            .filter { it.length >= 4 }
            .distinct()
            .sortedByDescending { it.length }
            .take(8)
        for (token in tokens) {
            val hit = catalogRepo.search(token).firstOrNull {
                it.internationalName.equals(token, ignoreCase = true) ||
                    it.name.equals(token, ignoreCase = true) ||
                    it.internationalName.contains(token, ignoreCase = true) ||
                    it.name.contains(token, ignoreCase = true)
            }
            if (hit != null) return hit
        }
        return null
    }

    fun reset() {
        _result.value = null
    }
}
