package ru.aptechka.ui.screens.meddetail

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import ru.aptechka.data.db.entity.UserDrugEntity
import ru.aptechka.data.repository.DrugRepository
import ru.aptechka.data.repository.ShoppingRepository
import ru.aptechka.domain.model.DrugBatch
import ru.aptechka.util.FakeDrugBatchDao
import ru.aptechka.util.FakeShoppingDao
import ru.aptechka.util.FakeUserDrugDao
import ru.aptechka.util.MainDispatcherRule

@OptIn(ExperimentalCoroutinesApi::class)
class MedDetailViewModelTest {

    @get:Rule
    val mainRule = MainDispatcherRule()

    private fun batch(qty: Float) = DrugBatch(id = 1, drugId = 7, quantity = qty, expirationDate = 1_000L)

    @Test
    fun `addBatch inserts a batch for the drug`() = runTest(mainRule.dispatcher) {
        val batchDao = FakeDrugBatchDao()
        val vm = MedDetailViewModel(
            drugId = 7,
            drugRepo = DrugRepository(FakeUserDrugDao(), batchDao),
            shoppingRepo = ShoppingRepository(FakeShoppingDao()),
        )

        vm.addBatch(expirationDate = 5_000L, quantity = 2f)
        advanceUntilIdle()

        assertEquals(1, batchDao.inserted.size)
        assertEquals(7L, batchDao.inserted.first().drugId)
        assertEquals(2f, batchDao.inserted.first().quantity)
        assertEquals(5_000L, batchDao.inserted.first().expirationDate)
    }

    @Test
    fun `adjustBatchQty never goes below zero`() = runTest(mainRule.dispatcher) {
        val batchDao = FakeDrugBatchDao()
        val vm = MedDetailViewModel(7, DrugRepository(FakeUserDrugDao(), batchDao), ShoppingRepository(FakeShoppingDao()))

        vm.adjustBatchQty(batch(qty = 0f), delta = -1f)
        advanceUntilIdle()

        assertEquals(0f, batchDao.updated.first().quantity)
    }

    @Test
    fun `adjustBatchQty increments quantity`() = runTest(mainRule.dispatcher) {
        val batchDao = FakeDrugBatchDao()
        val vm = MedDetailViewModel(7, DrugRepository(FakeUserDrugDao(), batchDao), ShoppingRepository(FakeShoppingDao()))

        vm.adjustBatchQty(batch(qty = 3f), delta = 1f)
        advanceUntilIdle()

        assertEquals(4f, batchDao.updated.first().quantity)
    }

    @Test
    fun `addToShopping saves an item named after the drug`() = runTest(mainRule.dispatcher) {
        val drugDao = FakeUserDrugDao()
        drugDao.insertDrug(UserDrugEntity(kitId = 3, name = "Нурофен")) // id = 1
        val shoppingDao = FakeShoppingDao()
        val vm = MedDetailViewModel(1, DrugRepository(drugDao, FakeDrugBatchDao()), ShoppingRepository(shoppingDao))
        advanceUntilIdle() // let init load the drug

        vm.addToShopping()
        advanceUntilIdle()

        assertEquals(1, shoppingDao.inserted.size)
        assertEquals("Нурофен", shoppingDao.inserted.first().name)
        assertEquals(3L, shoppingDao.inserted.first().kitId)
    }

    @Test
    fun `deleteDrug removes the drug and flags deleted`() = runTest(mainRule.dispatcher) {
        val drugDao = FakeUserDrugDao()
        drugDao.insertDrug(UserDrugEntity(kitId = 3, name = "Нурофен")) // id = 1
        val vm = MedDetailViewModel(1, DrugRepository(drugDao, FakeDrugBatchDao()), ShoppingRepository(FakeShoppingDao()))
        advanceUntilIdle()

        vm.deleteDrug()
        advanceUntilIdle()

        assertEquals(1, drugDao.deleted.size)
        assertTrue(vm.deleted.value)
    }
}
