package ru.aptechka.ui.screens.kits

import org.junit.Assert.assertEquals
import org.junit.Test
import ru.aptechka.domain.model.DrugBatch
import ru.aptechka.domain.model.Kit
import ru.aptechka.domain.model.UserDrug

private const val DAY = 24L * 60 * 60 * 1000

private fun batch(drugId: Long, daysFromNow: Long) = DrugBatch(
    id = 0,
    drugId = drugId,
    quantity = 1f,
    expirationDate = System.currentTimeMillis() + daysFromNow * DAY,
)

class KitsWithStatsTest {

    private val kits = listOf(
        Kit(id = 10, name = "Дом"),
        Kit(id = 20, name = "Дача"),
        Kit(id = 30, name = "Пусто"),
    )
    private val drugs = listOf(
        UserDrug(id = 1, kitId = 10, name = "Нурофен"),
        UserDrug(id = 2, kitId = 10, name = "Аспирин"),
        UserDrug(id = 3, kitId = 20, name = "Цитрамон"),
    )
    private val batches = listOf(
        batch(drugId = 1, daysFromNow = -2),  // kit 10: expired
        batch(drugId = 1, daysFromNow = 300), // kit 10: ok
        batch(drugId = 2, daysFromNow = 10),  // kit 10: expiring
        batch(drugId = 3, daysFromNow = 300), // kit 20: ok
    )

    @Test
    fun `counts are scoped per kit, not global`() {
        val stats = buildKitsWithStats(kits, drugs, batches).associateBy { it.kit.id }

        val home = stats.getValue(10L)
        assertEquals(2, home.totalDrugs)
        assertEquals(1, home.expiredCount)
        assertEquals(1, home.expiringSoonCount)

        val dacha = stats.getValue(20L)
        assertEquals(1, dacha.totalDrugs)
        assertEquals(0, dacha.expiredCount)
        assertEquals(0, dacha.expiringSoonCount)
    }

    @Test
    fun `kit with no drugs reports zeros`() {
        val empty = buildKitsWithStats(kits, drugs, batches).first { it.kit.id == 30L }
        assertEquals(0, empty.totalDrugs)
        assertEquals(0, empty.expiredCount)
        assertEquals(0, empty.expiringSoonCount)
    }

    @Test
    fun `total expired across kits equals sum of per-kit counts`() {
        val stats = buildKitsWithStats(kits, drugs, batches)
        assertEquals(1, stats.sumOf { it.expiredCount })
        assertEquals(1, stats.sumOf { it.expiringSoonCount })
    }
}
