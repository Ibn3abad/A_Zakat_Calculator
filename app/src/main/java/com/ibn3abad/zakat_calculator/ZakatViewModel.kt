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

package com.ibn3abad.zakat_calculator

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ibn3abad.zakat_calculator.data.SavedCalculation
import com.ibn3abad.zakat_calculator.data.ZakatDatabase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

class ZakatViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = ZakatDatabase.getDatabase(application).savedCalculationDao()

    var inputValue by mutableStateOf("")
        private set

    var inputLiabilities by mutableStateOf("")
        private set

    var irrigationRate by mutableDoubleStateOf(0.10)
        private set

    var animalType by mutableStateOf(AnimalType.SHEEP)
        private set

    var nisabTypeForLiquid by mutableIntStateOf(0)
        private set

    val nisabGoldEuro: Double
        get() = 85 * goldPricePerGram

    val nisabSilverEuro: Double
        get() = 595 * silverPricePerGram

    val inputAssets: Double
        get() = parseDecimalInput(inputValue)

    val liabilities: Double
        get() = parseDecimalInput(inputLiabilities)

    // Liste aller gespeicherten Berechnungen (reaktiv – aktualisiert sich automatisch)
    val savedCalculations = dao.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val savedYears = dao.getAllYears()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun getZakatBase(destination: AppDestinations): Double {
        return if (destination == AppDestinations.FIRMA) {
            (inputAssets - liabilities).coerceAtLeast(0.0)
        } else {
            inputAssets
        }
    }

    fun shouldShowResult(destination: AppDestinations): Boolean {
        return inputValue.isNotBlank() ||
                (destination == AppDestinations.FIRMA && inputLiabilities.isNotBlank())
    }

    fun onInputValueChange(value: String) {
        inputValue = normalizeDecimalInput(value)
    }

    fun onLiabilitiesChange(value: String) {
        inputLiabilities = normalizeDecimalInput(value)
    }

    fun onIrrigationRateChange(value: Double) {
        irrigationRate = value
    }

    fun onAnimalTypeChange(value: AnimalType) {
        animalType = value
    }

    fun onNisabTypeChange(value: Int) {
        nisabTypeForLiquid = value
    }

    /** Berechnung in der Datenbank speichern */
    fun saveCalculation(
        destination: AppDestinations,
        resultText: String,
        note: String = "",
        year: Int = Calendar.getInstance().get(Calendar.YEAR)
    ) {
        viewModelScope.launch {
            val (nType, nValue) = when (destination) {
                AppDestinations.LIQUIDITAET, AppDestinations.FIRMA, AppDestinations.AKTIEN -> {
                    if (nisabTypeForLiquid == 0) "Gold" to "${formatNumber(nisabGoldEuro, 0)} €"
                    else "Silber" to "${formatNumber(nisabSilverEuro, 0)} €"
                }
                AppDestinations.GOLD -> "Gold" to "85 g"
                AppDestinations.SILBER -> "Silber" to "595 g"
                AppDestinations.ERNTE -> "Ernte" to "650 kg"
                AppDestinations.TIERE -> {
                    val animal = when (animalType) {
                        AnimalType.SHEEP -> "Schafe"
                        AnimalType.COWS -> "Kühe"
                        AnimalType.CAMELS -> "Kamele"
                    }
                    val limit = when (animalType) {
                        AnimalType.SHEEP -> "40"
                        AnimalType.COWS -> "30"
                        AnimalType.CAMELS -> "5"
                    }
                    animal to limit
                }
                else -> "" to ""
            }

            dao.insert(
                SavedCalculation(
                    year = year,
                    timestamp = System.currentTimeMillis(),
                    category = destination.name,
                    inputValue = inputValue,
                    liabilities = inputLiabilities,
                    resultText = resultText,
                    note = note,
                    nisabType = nType,
                    nisabValue = nValue
                )
            )
        }
    }

    /** Berechnung löschen */
    fun deleteCalculation(calc: SavedCalculation) {
        viewModelScope.launch {
            dao.delete(calc)
        }
    }

    /** Eingabefelder zurücksetzen */
    fun resetInputs() {
        inputValue = ""
        inputLiabilities = ""
    }

    private val repo = MetalPriceRepository(application)

    var goldPricePerGram by mutableDoubleStateOf(143.22)
        private set
    var silverPricePerGram by mutableDoubleStateOf(0.82)
        private set
    var priceDate by mutableStateOf("Lade...")
        private set

    init {
        viewModelScope.launch {
            val price = repo.getPrice()
            if (price != null) {
                goldPricePerGram = price.goldGramEur
                silverPricePerGram = price.silverGramEur
                priceDate = price.priceDate
            } else {
                priceDate = "Offline"
            }
        }
    }
}
