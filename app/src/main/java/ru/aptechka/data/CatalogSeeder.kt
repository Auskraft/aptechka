package ru.aptechka.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import ru.aptechka.data.db.dao.CatalogDrugDao
import ru.aptechka.data.db.entity.CatalogDrugEntity

/** Seeds the drug catalog from assets/catalog.json on first launch (idempotent). */
class CatalogSeeder(
    private val context: Context,
    private val dao: CatalogDrugDao,
) {
    suspend fun seedIfEmpty() = withContext(Dispatchers.IO) {
        if (dao.count() > 0) return@withContext

        val json = context.assets.open("catalog.json").bufferedReader().use { it.readText() }
        val array = JSONArray(json)
        val entities = ArrayList<CatalogDrugEntity>(array.length())
        for (i in 0 until array.length()) {
            val o = array.getJSONObject(i)
            entities += CatalogDrugEntity(
                name = o.getString("name"),
                internationalName = o.optString("internationalName"),
                form = o.optString("form", "OTHER"),
                unit = "MG",
                category = o.optString("category", "OTHER"),
            )
        }
        dao.insertAll(entities)
    }
}
