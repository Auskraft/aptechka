package ru.aptechka.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import ru.aptechka.domain.model.BatchStatus
import ru.aptechka.domain.model.DrugBatch
import ru.aptechka.domain.model.UserDrug
import ru.aptechka.domain.model.UserDrugWithBatches

private const val DAY = 24L * 60 * 60 * 1000

private fun batch(daysFromNow: Long, qty: Float = 1f) = DrugBatch(
    id = daysFromNow, // unique-ish id for the test
    drugId = 1,
    quantity = qty,
    expirationDate = System.currentTimeMillis() + daysFromNow * DAY,
)

class DrugStatusTest {

    @Test
    fun `batch expired in the past is EXPIRED`() {
        assertEquals(BatchStatus.EXPIRED, batch(daysFromNow = -1).status)
    }

    @Test
    fun `batch within 30 days is EXPIRING_SOON`() {
        assertEquals(BatchStatus.EXPIRING_SOON, batch(daysFromNow = 10).status)
    }

    @Test
    fun `batch far in the future is ACTIVE`() {
        assertEquals(BatchStatus.ACTIVE, batch(daysFromNow = 200).status)
    }

    @Test
    fun `totalQuantity ignores expired batches`() {
        val dw = UserDrugWithBatches(
            drug = UserDrug(id = 1, kitId = 1, name = "Тест"),
            batches = listOf(
                batch(daysFromNow = -5, qty = 3f),  // expired -> excluded
                batch(daysFromNow = 100, qty = 2f),
                batch(daysFromNow = 5, qty = 4f),
            ),
        )
        assertEquals(6f, dw.totalQuantity)
    }

    @Test
    fun `nearestExpiry returns soonest non-expired batch`() {
        val soon = batch(daysFromNow = 5)
        val dw = UserDrugWithBatches(
            drug = UserDrug(id = 1, kitId = 1, name = "Тест"),
            batches = listOf(batch(daysFromNow = 100), soon, batch(daysFromNow = 50)),
        )
        assertEquals(soon.expirationDate, dw.nearestExpiry)
    }

    @Test
    fun `nearestExpiry is null when all batches expired`() {
        val dw = UserDrugWithBatches(
            drug = UserDrug(id = 1, kitId = 1, name = "Тест"),
            batches = listOf(batch(daysFromNow = -1), batch(daysFromNow = -10)),
        )
        assertNull(dw.nearestExpiry)
    }

    @Test
    fun `worstStatus reflects the most urgent batch`() {
        val dw = UserDrugWithBatches(
            drug = UserDrug(id = 1, kitId = 1, name = "Тест"),
            batches = listOf(batch(daysFromNow = 200), batch(daysFromNow = -1)),
        )
        assertEquals(BatchStatus.EXPIRED, dw.worstStatus)
    }
}
