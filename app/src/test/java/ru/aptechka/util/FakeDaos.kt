package ru.aptechka.util

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import ru.aptechka.data.db.dao.DrugBatchDao
import ru.aptechka.data.db.dao.KitDao
import ru.aptechka.data.db.dao.ShoppingDao
import ru.aptechka.data.db.dao.UserDrugDao
import ru.aptechka.data.db.entity.DrugBatchEntity
import ru.aptechka.data.db.entity.KitEntity
import ru.aptechka.data.db.entity.ShoppingItemEntity
import ru.aptechka.data.db.entity.UserDrugEntity

/** In-memory fakes recording DAO calls, shared across ViewModel unit tests. */

class FakeUserDrugDao : UserDrugDao {
    val inserted = mutableListOf<UserDrugEntity>()
    val updated = mutableListOf<UserDrugEntity>()
    val deleted = mutableListOf<UserDrugEntity>()
    private var nextId = 1L

    override suspend fun insertDrug(drug: UserDrugEntity): Long {
        val id = nextId++
        inserted += drug.copy(id = id)
        return id
    }

    override fun getDrugsByKit(kitId: Long): Flow<List<UserDrugEntity>> = flowOf(emptyList())
    override suspend fun getDrugById(id: Long): UserDrugEntity? = inserted.find { it.id == id }
    override suspend fun updateDrug(drug: UserDrugEntity) { updated += drug }
    override suspend fun deleteDrug(drug: UserDrugEntity) { deleted += drug }
}

class FakeDrugBatchDao : DrugBatchDao {
    val inserted = mutableListOf<DrugBatchEntity>()
    val updated = mutableListOf<DrugBatchEntity>()
    val deleted = mutableListOf<DrugBatchEntity>()
    private var nextId = 1L

    override suspend fun insertBatch(batch: DrugBatchEntity): Long {
        val id = nextId++
        inserted += batch.copy(id = id)
        return id
    }

    override fun getBatchesByDrug(drugId: Long): Flow<List<DrugBatchEntity>> = flowOf(emptyList())
    override suspend fun getBatchesForDrug(drugId: Long): List<DrugBatchEntity> = emptyList()
    override fun getAllBatches(): Flow<List<DrugBatchEntity>> = flowOf(emptyList())
    override suspend fun updateBatch(batch: DrugBatchEntity) { updated += batch }
    override suspend fun deleteBatch(batch: DrugBatchEntity) { deleted += batch }
}

class FakeKitDao : KitDao {
    val inserted = mutableListOf<KitEntity>()
    private var nextId = 1L

    override fun getAllKits(): Flow<List<KitEntity>> = flowOf(emptyList())
    override suspend fun getKitById(id: Long): KitEntity? = inserted.find { it.id == id }
    override suspend fun insertKit(kit: KitEntity): Long {
        val id = nextId++
        inserted += kit.copy(id = id)
        return id
    }
    override suspend fun updateKit(kit: KitEntity) = Unit
    override suspend fun deleteKit(kit: KitEntity) = Unit
    override suspend fun getKitCount(): Int = inserted.size
}

class FakeShoppingDao : ShoppingDao {
    val inserted = mutableListOf<ShoppingItemEntity>()
    private var nextId = 1L

    override fun getAllShoppingItems(): Flow<List<ShoppingItemEntity>> = flowOf(emptyList())
    override suspend fun insertItem(item: ShoppingItemEntity): Long {
        val id = nextId++
        inserted += item.copy(id = id)
        return id
    }

    override suspend fun updateItem(item: ShoppingItemEntity) = Unit
    override suspend fun deleteItem(item: ShoppingItemEntity) = Unit
    override suspend fun deletePurchasedItems() = Unit
}
