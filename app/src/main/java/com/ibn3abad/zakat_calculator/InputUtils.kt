/**
 * @author     A. KHOUK
 * @date       12.05.2026
 * @version    3.20
 * @copyright  Copyright (c) 2026, A. KHOUK.
 * @license    This program is free software: you can redistribute it and/or modify
 *             it under the terms of the GNU General Public License as published by
 *             the Free Software Foundation, either version 3 of the License, or
 *             (at your option) any later version.
 */

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