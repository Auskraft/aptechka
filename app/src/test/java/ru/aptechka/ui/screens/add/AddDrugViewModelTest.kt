package ru.aptechka.ui.screens.add

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import ru.aptechka.data.repository.CatalogRepository
import ru.aptechka.data.repository.DrugRepository
import ru.aptechka.domain.model.CategoryKey
import ru.aptechka.domain.model.FormKey
import ru.aptechka.util.FakeCatalogDrugDao
import ru.aptechka.util.FakeDrugBatchDao
import ru.aptechka.util.FakeUserDrugDao
import ru.aptechka.util.MainDispatcherRule

@OptIn(ExperimentalCoroutinesApi::class)
class AddDrugViewModelTest {

    @get:Rule
    val mainRule = MainDispatcherRule()

    @Test
    fun `save inserts drug and its first batch then flags saved`() = runTest(mainRule.dispatcher) {
        val drugDao = FakeUserDrugDao()
        val batchDao = FakeDrugBatchDao()
        val vm = AddDrugViewModel(
            kitId = 7,
            catalogId = -1L,
            drugRepo = DrugRepository(drugDao, batchDao),
            catalogRepo = CatalogRepository(FakeCatalogDrugDao()),
        )

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
        val vm = AddDrugViewModel(
            kitId = 7,
            catalogId = -1L,
            drugRepo = DrugRepository(drugDao, batchDao),
            catalogRepo = CatalogRepository(FakeCatalogDrugDao()),
        )

        vm.onName("   ") // blank, no date
        vm.save()
        advanceUntilIdle()

        assertTrue(drugDao.inserted.isEmpty())
        assertTrue(batchDao.inserted.isEmpty())
        assertFalse(vm.saved.value)
    }
}
