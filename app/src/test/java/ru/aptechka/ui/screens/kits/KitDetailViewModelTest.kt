package ru.aptechka.ui.screens.kits

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import ru.aptechka.data.repository.DrugRepository
import ru.aptechka.data.repository.KitRepository
import ru.aptechka.data.repository.ShoppingRepository
import ru.aptechka.domain.model.UserDrug
import ru.aptechka.util.FakeDrugBatchDao
import ru.aptechka.util.FakeKitDao
import ru.aptechka.util.FakeShoppingDao
import ru.aptechka.util.FakeUserDrugDao
import ru.aptechka.util.MainDispatcherRule

@OptIn(ExperimentalCoroutinesApi::class)
class KitDetailViewModelTest {

    @get:Rule
    val mainRule = MainDispatcherRule()

    @Test
    fun `addToShopping saves an item named after the drug, scoped to its kit`() = runTest(mainRule.dispatcher) {
        val shoppingDao = FakeShoppingDao()
        val vm = KitDetailViewModel(
            kitId = 5,
            drugRepo = DrugRepository(FakeUserDrugDao(), FakeDrugBatchDao()),
            kitRepo = KitRepository(FakeKitDao()),
            shoppingRepo = ShoppingRepository(shoppingDao),
        )

        vm.addToShopping(UserDrug(id = 1, kitId = 5, name = "Нурофен"))
        advanceUntilIdle()

        assertEquals(1, shoppingDao.inserted.size)
        assertEquals("Нурофен", shoppingDao.inserted.first().name)
        assertEquals(5L, shoppingDao.inserted.first().kitId)
    }
}
