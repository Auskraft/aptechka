package ru.aptechka.data.repository

import ru.aptechka.data.db.converter.toDomain
import ru.aptechka.data.db.dao.CatalogDrugDao
import ru.aptechka.domain.model.CatalogDrug

class CatalogRepository(private val dao: CatalogDrugDao) {
    suspend fun findByBarcode(barcode: String): CatalogDrug? =
        dao.getByBarcode(barcode)?.toDomain()

    suspend fun search(query: String): List<CatalogDrug> =
        dao.searchCatalog(query).map { it.toDomain() }
}
