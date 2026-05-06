package com.ibn3abad.zakat_calculator.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [SavedCalculation::class],
    version = 1,
    exportSchema = false
)
abstract class ZakatDatabase : RoomDatabase() {

    abstract fun savedCalculationDao(): SavedCalculationDao

    companion object {
        @Volatile
        private var INSTANCE: ZakatDatabase? = null

        fun getDatabase(context: Context): ZakatDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ZakatDatabase::class.java,
                    "zakat_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}