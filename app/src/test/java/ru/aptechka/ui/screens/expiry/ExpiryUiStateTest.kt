package ru.aptechka.ui.screens.expiry

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import ru.aptechka.domain.model.DrugBatch
import ru.aptechka.domain.model.Kit
import ru.aptechka.domain.model.UserDrug

private const val DAY = 24L * 60 * 60 * 1000

private fun batch(id: Long, drugId: Long, daysFromNow: Long) = DrugBatch(
    id = id,
    drugId = drugId,
    quantity = 1f,
    expirationDate = System.currentTimeMillis() + daysFromNow * DAY,
)

class ExpiryUiStateTest {

    private val drugs = listOf(
        UserDrug(id = 1, kitId = 10, name = "Нурофен"),
        UserDrug(id = 2, kitId = 20, name = "Аспирин"),
    )
    private val kits = listOf(
        Kit(id = 10, name = "Дом"),
        Kit(id = 20, name = "Дача"),
    )

    @Test
    fun `partitions batches by status with resolved drug and kit names`() {
        val state = buildExpiryUiState(
            batches = listOf(
                batch(id = 1, drugId = 1, daysFromNow = -3),  // expired
                batch(id = 2, drugId = 2, daysFromNow = 10),  // expiring
                batch(id = 3, drugId = 1, daysFromNow = 200), // ok
            ),
            drugs = drugs,
            kits = kits,
        )

        assertEquals(1, state.expired.size)
        assertEquals("Нурофен", state.expired.first().drug.name)
        assertEquals("Дом", state.expired.first().kit.name)

        assertEquals(1, state.expiring.size)
        assertEquals("Аспирин", state.expiring.first().drug.name)
        assertEquals("Дача", state.expiring.first().kit.name)

        assertEquals(1, state.ok.size)
    }

    @Test
    fun `drops orphan batches whose drug is missing`() {
        val state = buildExpiryUiState(
            batches = listOf(batch(id = 9, drugId = 999, daysFromNow = -1)),
            drugs = drugs,
            kits = kits,
        )
        assertTrue(state.expired.isEmpty())
        assertTrue(state.expiring.isEmpty())
        assertTrue(state.ok.isEmpty())
    }

    @Test
    fun `sorts each bucket by soonest expiry first`() {
        val state = buildExpiryUiState(
            batches = listOf(
                batch(id = 1, drugId = 1, daysFromNow = 300),
                batch(id = 2, drugId = 1, daysFromNow = 120),
            ),
            drugs = drugs,
            kits = kits,
        )
        assertEquals(2L, state.ok.first().batch.id)
        assertEquals(1L, state.ok.last().batch.id)
    }
}
