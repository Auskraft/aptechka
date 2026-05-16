package ru.aptechka.domain.model

data class UserDrug(
    val id: Long = 0,
    val kitId: Long,
    val catalogDrugId: Long? = null,
    val name: String,
    val form: FormKey = FormKey.OTHER,
    val unit: UnitKey = UnitKey.MG,
    val category: CategoryKey = CategoryKey.OTHER,
    val notes: String = "",
    val imageUri: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

data class DrugBatch(
    val id: Long = 0,
    val drugId: Long,
    val quantity: Float,
    val expirationDate: Long,
    val purchaseDate: Long = System.currentTimeMillis(),
    val price: Float? = null,
    val barcode: String = "",
    val notes: String = ""
) {
    val status: BatchStatus
        get() {
            val today = System.currentTimeMillis()
            val thirtyDays = 30L * 24 * 60 * 60 * 1000
            return when {
                expirationDate < today -> BatchStatus.EXPIRED
                expirationDate <= today + thirtyDays -> BatchStatus.EXPIRING_SOON
                else -> BatchStatus.ACTIVE
            }
        }
}

data class UserDrugWithBatches(
    val drug: UserDrug,
    val batches: List<DrugBatch> = emptyList()
) {
    val totalQuantity: Float get() = batches.filter { it.status != BatchStatus.EXPIRED }.sumOf { it.quantity.toDouble() }.toFloat()
    val nearestExpiry: Long? get() = batches.filter { it.status != BatchStatus.EXPIRED }.minOfOrNull { it.expirationDate }
    val worstStatus: BatchStatus get() = batches.minOfOrNull { it.status.ordinal }?.let { BatchStatus.entries[it] } ?: BatchStatus.ACTIVE
}