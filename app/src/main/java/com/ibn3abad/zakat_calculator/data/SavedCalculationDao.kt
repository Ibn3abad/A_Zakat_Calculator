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

    @Query("DELETE FROM saved_calculations WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM saved_calculations ORDER BY year DESC, timestamp DESC")
    fun getAll(): Flow<List<SavedCalculation>>

    @Query("SELECT * FROM saved_calculations WHERE year = :year ORDER BY timestamp DESC")
    fun getByYear(year: Int): Flow<List<SavedCalculation>>

    @Query("SELECT DISTINCT year FROM saved_calculations ORDER BY year DESC")
    fun getAllYears(): Flow<List<Int>>
}