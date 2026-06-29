/**
 * @author     A. KHOUK
 * @date       12.05.2026
 * @version    3.27
 * @copyright  Copyright (c) 2026, A. KHOUK.
 * @license    This program is free software: you can redistribute it and/or modify
 *             it under the terms of the GNU General Public License as published by
 *             the Free Software Foundation, either version 3 of the License, or
 *             (at your option) any later version.
 */

package com.ibn3abad.zakat_calculator.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [SavedCalculation::class],
    version = 2,
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
                )
                    .fallbackToDestructiveMigration(true)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}