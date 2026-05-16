package ru.aptechka.domain.model

data class CatalogDrug(
    val id: Long = 0,
    val name: String,
    val internationalName: String = "",
    val manufacturer: String = "",
    val form: FormKey = FormKey.OTHER,
    val unit: UnitKey = UnitKey.MG,
    val category: CategoryKey = CategoryKey.OTHER,
    val barcode: String = "",
    val description: String = "",
    val storageConditions: String = "",
    val sideEffects: String = "",
    val contraindications: String = ""
)