package ru.aptechka.ui.screens.expiry

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import ru.aptechka.data.repository.DrugRepository
import ru.aptechka.data.repository.KitRepository
import ru.aptechka.data.repository.ShoppingRepository
import ru.aptechka.domain.model.DrugBatch
import ru.aptechka.domain.model.Kit
import ru.aptechka.domain.model.UserDrug
import ru.aptechka.util.FakeDrugBatchDao
import ru.aptechka.util.FakeKitDao
import ru.aptechka.util.FakeShoppingDao
import ru.aptechka.util.FakeUserDrugDao
import ru.aptechka.util.MainDispatcherRule

@OptIn(ExperimentalCoroutinesApi::class)
class ExpiryViewModelTest {

    @get:Rule
    val mainRule = MainDispatcherRule()

    @Test
    fun `addToShopping saves an item from the batch context`() = runTest(mainRule.dispatcher) {
        val shoppingDao = FakeShoppingDao()
        val vm = ExpiryViewModel(
            drugRepo = DrugRepository(FakeUserDrugDao(), FakeDrugBatchDao()),
            kitRepo = KitRepository(FakeKitDao()),
            shoppingRepo = ShoppingRepository(shoppingDao),
        )
        val ctx = BatchWithContext(
            batch = DrugBatch(id = 1, drugId = 1, quantity = 1f, expirationDate = 1_000L),
            drug = UserDrug(id = 1, kitId = 5, name = "Аспирин"),
            kit = Kit(id = 5, name = "Дом"),
        )

        vm.addToShopping(ctx)
        advanceUntilIdle()

        assertEquals(1, shoppingDao.inserted.size)
        assertEquals("Аспирин", shoppingDao.inserted.first().name)
        assertEquals(5L, shoppingDao.inserted.first().kitId)
    }
}
