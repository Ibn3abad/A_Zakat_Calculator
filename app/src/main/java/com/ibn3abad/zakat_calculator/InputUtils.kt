package com.ibn3abad.zakat_calculator

import java.util.Locale

fun normalizeDecimalInput(input: String): String {
    val normalized = input.replace(',', '.')
    return if (normalized.matches(Regex("^\\d*\\.?\\d*$"))) {
        normalized
    } else {
        input.dropLast(1)
    }
}

fun parseDecimalInput(input: String): Double {
    return input.replace(',', '.').toDoubleOrNull() ?: 0.0
}

fun formatNumber(value: Double, decimals: Int = 2): String {
    return String.format(Locale.US, "%.${decimals}f", value)
}