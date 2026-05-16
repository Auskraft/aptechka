package ru.aptechka.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ru.aptechka.data.db.converter.toDomain
import ru.aptechka.data.db.converter.toEntity
import ru.aptechka.data.db.dao.ShoppingDao
import ru.aptechka.domain.model.ShoppingItem

class ShoppingRepository(private val dao: ShoppingDao) {
    fun observeAll(): Flow<List<ShoppingItem>> =
        dao.getAllShoppingItems().map { it.map { e -> e.toDomain() } }

    suspend fun save(item: ShoppingItem) = dao.insertItem(item.toEntity())
    suspend fun update(item: ShoppingItem) = dao.updateItem(item.toEntity())
    suspend fun delete(item: ShoppingItem) = dao.deleteItem(item.toEntity())
    suspend fun clearPurchased() = dao.deletePurchasedItems()
}
