package com.ibn3abad.zakat_calculator

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class ZakatViewModel : ViewModel() {

    var inputValue by mutableStateOf("")
        private set

    var inputLiabilities by mutableStateOf("")
        private set

    var irrigationRate by mutableStateOf(0.10)
        private set

    var animalType by mutableStateOf(AnimalType.SHEEP)
        private set

    var nisabTypeForLiquid by mutableStateOf(0)
        private set

    val goldPricePerGram = 143.22
    val silverPricePerGram = 0.82

    val nisabGoldEuro: Double
        get() = 85 * goldPricePerGram

    val nisabSilverEuro: Double
        get() = 595 * silverPricePerGram

    val inputAssets: Double
        get() = parseDecimalInput(inputValue)

    val liabilities: Double
        get() = parseDecimalInput(inputLiabilities)

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
}