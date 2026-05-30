package ru.aptechka.data.db.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import ru.aptechka.data.db.entity.*

@Dao
interface KitDao {
    @Query("SELECT * FROM kits ORDER BY createdAt DESC")
    fun getAllKits(): Flow<List<KitEntity>>

    @Query("SELECT * FROM kits WHERE id = :id")
    suspend fun getKitById(id: Long): KitEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertKit(kit: KitEntity): Long

    @Update
    suspend fun updateKit(kit: KitEntity)

    @Delete
    suspend fun deleteKit(kit: KitEntity)

    @Query("SELECT COUNT(*) FROM kits")
    suspend fun getKitCount(): Int
}

@Dao
interface UserDrugDao {
    @Query("SELECT * FROM user_drugs WHERE kitId = :kitId ORDER BY name ASC")
    fun getDrugsByKit(kitId: Long): Flow<List<UserDrugEntity>>

    @Query("SELECT * FROM user_drugs")
    fun getAllDrugs(): Flow<List<UserDrugEntity>>

    @Query("SELECT * FROM user_drugs WHERE id = :id")
    suspend fun getDrugById(id: Long): UserDrugEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDrug(drug: UserDrugEntity): Long

    @Update
    suspend fun updateDrug(drug: UserDrugEntity)

    @Delete
    suspend fun deleteDrug(drug: UserDrugEntity)
}

@Dao
interface DrugBatchDao {
    @Query("SELECT * FROM drug_batches WHERE drugId = :drugId ORDER BY expirationDate ASC")
    fun getBatchesByDrug(drugId: Long): Flow<List<DrugBatchEntity>>

    @Query("SELECT * FROM drug_batches WHERE drugId = :drugId ORDER BY expirationDate ASC")
    suspend fun getBatchesForDrug(drugId: Long): List<DrugBatchEntity>

    @Query("SELECT * FROM drug_batches ORDER BY expirationDate ASC")
    fun getAllBatches(): Flow<List<DrugBatchEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBatch(batch: DrugBatchEntity): Long

    @Update
    suspend fun updateBatch(batch: DrugBatchEntity)

    @Delete
    suspend fun deleteBatch(batch: DrugBatchEntity)
}

@Dao
interface ShoppingDao {
    @Query("SELECT * FROM shopping_items ORDER BY createdAt DESC")
    fun getAllShoppingItems(): Flow<List<ShoppingItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: ShoppingItemEntity): Long

    @Update
    suspend fun updateItem(item: ShoppingItemEntity)

    @Delete
    suspend fun deleteItem(item: ShoppingItemEntity)

    @Query("DELETE FROM shopping_items WHERE isPurchased = 1")
    suspend fun deletePurchasedItems()
}

@Dao
interface ReminderDao {
    @Query("SELECT * FROM reminders")
    fun getAllReminders(): Flow<List<ReminderEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminder(reminder: ReminderEntity): Long

    @Update
    suspend fun updateReminder(reminder: ReminderEntity)

    @Delete
    suspend fun deleteReminder(reminder: ReminderEntity)
}

@Dao
interface CatalogDrugDao {
    @Query("SELECT * FROM catalog_drugs WHERE name LIKE '%' || :query || '%' OR internationalName LIKE '%' || :query || '%' ORDER BY name LIMIT 30")
    suspend fun searchCatalog(query: String): List<CatalogDrugEntity>

    @Query("SELECT COUNT(*) FROM catalog_drugs")
    suspend fun count(): Int

    @Query("SELECT * FROM catalog_drugs WHERE barcode = :barcode LIMIT 1")
    suspend fun getByBarcode(barcode: String): CatalogDrugEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(drugs: List<CatalogDrugEntity>)
}