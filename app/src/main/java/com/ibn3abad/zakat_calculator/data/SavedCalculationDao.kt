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

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SavedCalculationDao {

    @Insert
    suspend fun insert(calculation: SavedCalculation): Long

    @Delete
    suspend fun delete(calculation: SavedCalculation)

    @Query("SELECT * FROM saved_calculations ORDER BY year DESC, timestamp DESC")
    fun getAll(): Flow<List<SavedCalculation>>

    @Query("SELECT DISTINCT year FROM saved_calculations ORDER BY year DESC")
    fun getAllYears(): Flow<List<Int>>
}