package com.ibn3abad.zakat_calculator.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "saved_calculations")
data class SavedCalculation(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val year: Int,
    val timestamp: Long,        // Datum/Zeit der Speicherung
    val category: String,       // z.B. "GOLD", "LIQUIDITAET" – Name aus AppDestinations
    val inputValue: String,     // Was der Benutzer eingegeben hat
    val liabilities: String,    // Nur für FIRMA, sonst leer
    val resultText: String,     // Das berechnete Ergebnis
    val note: String = ""       // Optionale Notiz vom Benutzer
)