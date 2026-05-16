package ru.aptechka.domain.model

data class StorageLocation(
    val id: Long = 0,
    val kitId: Long,
    val name: String,
    val description: String = ""
)

data class Kit(
    val id: Long = 0,
    val name: String,
    val description: String = "",
    val iconName: String = "ic_kit_default",
    val colorHex: String = "#4CAF50",
    val createdAt: Long = System.currentTimeMillis()
)

data class KitWithStats(
    val kit: Kit,
    val totalDrugs: Int = 0,
    val expiredCount: Int = 0,
    val expiringSoonCount: Int = 0
)