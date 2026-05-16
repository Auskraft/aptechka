package ru.aptechka.domain.model

data class ShoppingItem(
    val id: Long = 0,
    val name: String,
    val quantity: Float = 1f,
    val unit: UnitKey = UnitKey.MG,
    val kitId: Long? = null,
    val catalogDrugId: Long? = null,
    val isPurchased: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

data class Reminder(
    val id: Long = 0,
    val drugId: Long? = null,
    val kitId: Long? = null,
    val title: String,
    val message: String = "",
    val daysBeforeExpiry: Int = 30,
    val isGlobal: Boolean = false,
    val isEnabled: Boolean = true
)

data class BarcodeMapping(
    val id: Long = 0,
    val barcode: String,
    val catalogDrugId: Long,
    val source: String = "user"
)