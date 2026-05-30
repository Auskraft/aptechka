package ru.aptechka.ui.screens.add

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AddDrugStateTest {

    @Test
    fun `cannot save with blank name`() {
        val s = AddDrugState(name = "  ", expirationDate = 1_000L, quantity = "1")
        assertFalse(s.canSave)
    }

    @Test
    fun `cannot save without expiration date`() {
        val s = AddDrugState(name = "Нурофен", expirationDate = null, quantity = "1")
        assertFalse(s.canSave)
    }

    @Test
    fun `cannot save with zero quantity`() {
        val s = AddDrugState(name = "Нурофен", expirationDate = 1_000L, quantity = "0")
        assertFalse(s.canSave)
    }

    @Test
    fun `cannot save with non-numeric quantity`() {
        val s = AddDrugState(name = "Нурофен", expirationDate = 1_000L, quantity = "")
        assertFalse(s.canSave)
    }

    @Test
    fun `can save when name, date and positive quantity are present`() {
        val s = AddDrugState(name = "Нурофен", expirationDate = 1_000L, quantity = "2")
        assertTrue(s.canSave)
    }
}
