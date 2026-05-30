package ru.aptechka.ui.screens.add

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import ru.aptechka.data.db.dao.DrugBatchDao
import ru.aptechka.data.db.dao.UserDrugDao
import ru.aptechka.data.db.entity.DrugBatchEntity
import ru.aptechka.data.db.entity.UserDrugEntity
import ru.aptechka.data.repository.DrugRepository
import ru.aptechka.domain.model.CategoryKey
import ru.aptechka.domain.model.FormKey
import ru.aptechka.util.MainDispatcherRule

private class FakeUserDrugDao : UserDrugDao {
    val inserted = mutableListOf<UserDrugEntity>()
    private var nextId = 1L

    override suspend fun insertDrug(drug: UserDrugEntity): Long {
        val id = nextId++
        inserted += drug.copy(id = id)
        return id
    }

    override fun getDrugsByKit(kitId: Long): Flow<List<UserDrugEntity>> = flowOf(emptyList())
    override suspend fun getDrugById(id: Long): UserDrugEntity? = inserted.find { it.id == id }
    override suspend fun updateDrug(drug: UserDrugEntity) = Unit
    override suspend fun deleteDrug(drug: UserDrugEntity) = Unit
}

private class FakeDrugBatchDao : DrugBatchDao {
    val inserted = mutableListOf<DrugBatchEntity>()
    private var nextId = 1L

    override suspend fun insertBatch(batch: DrugBatchEntity): Long {
        val id = nextId++
        inserted += batch.copy(id = id)
        return id
    }

    override fun getBatchesByDrug(drugId: Long): Flow<List<DrugBatchEntity>> = flowOf(emptyList())
    override suspend fun getBatchesForDrug(drugId: Long): List<DrugBatchEntity> = emptyList()
    override fun getAllBatches(): Flow<List<DrugBatchEntity>> = flowOf(emptyList())
    override suspend fun updateBatch(batch: DrugBatchEntity) = Unit
    override suspend fun deleteBatch(batch: DrugBatchEntity) = Unit
}

@OptIn(ExperimentalCoroutinesApi::class)
class AddDrugViewModelTest {

    @get:Rule
    val mainRule = MainDispatcherRule()

    @Test
    fun `save inserts drug and its first batch then flags saved`() = runTest(mainRule.dispatcher) {
        val drugDao = FakeUserDrugDao()
        val batchDao = FakeDrugBatchDao()
        val vm = AddDrugViewModel(kitId = 7, drugRepo = DrugRepository(drugDao, batchDao))

        vm.onName("Нурофен")
        vm.onForm(FormKey.SYRUP)
        vm.onCategory(CategoryKey.ANALGESIC)
        vm.onQuantity("3")
        vm.onExpirationDate(123_456_789L)
        vm.save()
        advanceUntilIdle()

        assertEquals(1, drugDao.inserted.size)
        val drug = drugDao.inserted.first()
        assertEquals("Нурофен", drug.name)
        assertEquals(7L, drug.kitId)
        assertEquals("SYRUP", drug.form)
        assertEquals("ANALGESIC", drug.category)

        assertEquals(1, batchDao.inserted.size)
        val batch = batchDao.inserted.first()
        assertEquals(3f, batch.quantity)
        assertEquals(123_456_789L, batch.expirationDate)
        assertEquals(drug.id, batch.drugId)

        assertTrue(vm.saved.value)
    }

    @Test
    fun `save is a no-op when state is invalid`() = runTest(mainRule.dispatcher) {
        val drugDao = FakeUserDrugDao()
        val batchDao = FakeDrugBatchDao()
        val vm = AddDrugViewModel(kitId = 7, drugRepo = DrugRepository(drugDao, batchDao))

        vm.onName("   ") // blank, no date
        vm.save()
        advanceUntilIdle()

        assertTrue(drugDao.inserted.isEmpty())
        assertTrue(batchDao.inserted.isEmpty())
        assertFalse(vm.saved.value)
    }
}
