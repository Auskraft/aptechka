package ru.aptechka.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ru.aptechka.data.db.converter.toDomain
import ru.aptechka.data.db.converter.toEntity
import ru.aptechka.data.db.dao.KitDao
import ru.aptechka.domain.model.Kit

class KitRepository(private val dao: KitDao) {
    fun observeAll(): Flow<List<Kit>> = dao.getAllKits().map { it.map { e -> e.toDomain() } }
    suspend fun getById(id: Long): Kit? = dao.getKitById(id)?.toDomain()
    suspend fun save(kit: Kit): Long = dao.insertKit(kit.toEntity())
    suspend fun update(kit: Kit) = dao.updateKit(kit.toEntity())
    suspend fun delete(kit: Kit) = dao.deleteKit(kit.toEntity())
    suspend fun count(): Int = dao.getKitCount()
}
