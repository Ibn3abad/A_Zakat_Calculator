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

package com.ibn3abad.zakat_calculator.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MetalPrice(
    @SerialName("price_date")
    val priceDate: String,
    @SerialName("gold_gram_eur")
    val goldGramEur: Double,
    @SerialName("silver_gram_eur")
    val silverGramEur: Double,
    @SerialName("usdeur")
    val usdEur: Double
)
