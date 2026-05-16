package ru.aptechka.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import ru.aptechka.data.db.dao.*
import ru.aptechka.data.db.entity.*

@Database(
    entities = [
        KitEntity::class,
        UserDrugEntity::class,
        DrugBatchEntity::class,
        CatalogDrugEntity::class,
        ShoppingItemEntity::class,
        ReminderEntity::class,
        BarcodeMappingEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AptechkaDatabase : RoomDatabase() {
    abstract fun kitDao(): KitDao
    abstract fun userDrugDao(): UserDrugDao
    abstract fun drugBatchDao(): DrugBatchDao
    abstract fun shoppingDao(): ShoppingDao
    abstract fun reminderDao(): ReminderDao
    abstract fun catalogDrugDao(): CatalogDrugDao

    companion object {
        @Volatile
        private var INSTANCE: AptechkaDatabase? = null

        fun getDatabase(context: Context): AptechkaDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AptechkaDatabase::class.java,
                    "aptechka.db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}