package ru.aptechka.data.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "kits")
data class KitEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val description: String = "",
    val iconName: String = "ic_kit_default",
    val colorHex: String = "#4CAF50",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "catalog_drugs")
data class CatalogDrugEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val internationalName: String = "",
    val manufacturer: String = "",
    val form: String = "OTHER",
    val unit: String = "MG",
    val category: String = "OTHER",
    val barcode: String = "",
    val description: String = "",
    val storageConditions: String = "",
    val sideEffects: String = "",
    val contraindications: String = ""
)

@Entity(
    tableName = "user_drugs",
    foreignKeys = [ForeignKey(
        entity = KitEntity::class,
        parentColumns = ["id"],
        childColumns = ["kitId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("kitId")]
)
data class UserDrugEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val kitId: Long,
    val catalogDrugId: Long? = null,
    val name: String,
    val form: String = "OTHER",
    val unit: String = "MG",
    val category: String = "OTHER",
    val notes: String = "",
    val imageUri: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "drug_batches",
    foreignKeys = [ForeignKey(
        entity = UserDrugEntity::class,
        parentColumns = ["id"],
        childColumns = ["drugId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("drugId")]
)
data class DrugBatchEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val drugId: Long,
    val quantity: Float,
    val expirationDate: Long,
    val purchaseDate: Long = System.currentTimeMillis(),
    val price: Float? = null,
    val barcode: String = "",
    val notes: String = ""
)

@Entity(tableName = "shopping_items")
data class ShoppingItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val quantity: Float = 1f,
    val unit: String = "MG",
    val kitId: Long? = null,
    val catalogDrugId: Long? = null,
    val isPurchased: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "reminders")
data class ReminderEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val drugId: Long? = null,
    val kitId: Long? = null,
    val title: String,
    val message: String = "",
    val daysBeforeExpiry: Int = 30,
    val isGlobal: Boolean = false,
    val isEnabled: Boolean = true
)

@Entity(tableName = "barcode_mappings")
data class BarcodeMappingEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val barcode: String,
    val catalogDrugId: Long,
    val source: String = "user"
)