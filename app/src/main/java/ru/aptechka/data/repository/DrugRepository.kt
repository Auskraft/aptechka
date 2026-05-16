package ru.aptechka.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ru.aptechka.data.db.converter.toDomain
import ru.aptechka.data.db.converter.toEntity
import ru.aptechka.data.db.dao.DrugBatchDao
import ru.aptechka.data.db.dao.UserDrugDao
import ru.aptechka.domain.model.DrugBatch
import ru.aptechka.domain.model.UserDrug
import ru.aptechka.domain.model.UserDrugWithBatches

class DrugRepository(
    private val drugDao: UserDrugDao,
    private val batchDao: DrugBatchDao,
) {
    fun observeByKit(kitId: Long): Flow<List<UserDrugWithBatches>> =
        drugDao.getDrugsByKit(kitId).map { drugs ->
            drugs.map { drugEntity ->
                val drug = drugEntity.toDomain()
                val batches = batchDao.getBatchesForDrug(drug.id).map { it.toDomain() }
                UserDrugWithBatches(drug, batches)
            }
        }

    fun observeAllBatches(): Flow<List<DrugBatch>> =
        batchDao.getAllBatches().map { it.map { e -> e.toDomain() } }

    suspend fun getDrug(id: Long): UserDrug? = drugDao.getDrugById(id)?.toDomain()

    suspend fun getBatchesForDrug(drugId: Long): List<DrugBatch> =
        batchDao.getBatchesForDrug(drugId).map { it.toDomain() }

    suspend fun saveDrug(drug: UserDrug): Long = drugDao.insertDrug(drug.toEntity())
    suspend fun updateDrug(drug: UserDrug) = drugDao.updateDrug(drug.toEntity())
    suspend fun deleteDrug(drug: UserDrug) = drugDao.deleteDrug(drug.toEntity())

    suspend fun saveBatch(batch: DrugBatch): Long = batchDao.insertBatch(batch.toEntity())
    suspend fun updateBatch(batch: DrugBatch) = batchDao.updateBatch(batch.toEntity())
    suspend fun deleteBatch(batch: DrugBatch) = batchDao.deleteBatch(batch.toEntity())
}
