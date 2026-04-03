package com.ibn3abad.zakat_calculator

object ZakatCalculator {

    fun calculateZakatResult(
        destination: AppDestinations,
        zakatBase: Double,
        nisabTypeForLiquid: Int,
        nisabGoldEuro: Double,
        nisabSilverEuro: Double,
        irrigationRate: Double,
        animalType: AnimalType,
        goldPricePerGram: Double,
        silverPricePerGram: Double,
        underNisabText: String,
        resGiveText: String,
        resBaseText: String,
        resZakatAmountText: String,
        resSheepText: String,
        resTabiText: String,
        resMusinnahText: String,
        resBintMakhadText: String,
        resBintLabunText: String,
        resHiqqahText: String,
        resJadhahText: String,
        resCamelRuleText: String
    ): String {
        return when (destination) {
            AppDestinations.ERNTE -> {
                if (zakatBase >= 650) {
                    "$resGiveText: ${formatNumber(zakatBase * irrigationRate)} kg"
                } else {
                    underNisabText
                }
            }

            AppDestinations.TIERE -> {
                calculateAnimalZakat(
                    animalType = animalType,
                    count = zakatBase.toInt(),
                    underNisabText = underNisabText,
                    resSheepText = resSheepText,
                    resTabiText = resTabiText,
                    resMusinnahText = resMusinnahText,
                    resBintMakhadText = resBintMakhadText,
                    resBintLabunText = resBintLabunText,
                    resHiqqahText = resHiqqahText,
                    resJadhahText = resJadhahText,
                    resCamelRuleText = resCamelRuleText
                )
            }

            AppDestinations.GOLD -> {
                if (zakatBase >= 85) {
                    "${formatNumber((zakatBase * goldPricePerGram) * 0.025)} €"
                } else {
                    "$underNisabText (85g)"
                }
            }

            AppDestinations.SILBER -> {
                if (zakatBase >= 595) {
                    "${formatNumber((zakatBase * silverPricePerGram) * 0.025)} €"
                } else {
                    "$underNisabText (595g)"
                }
            }

            AppDestinations.LIQUIDITAET,
            AppDestinations.FIRMA,
            AppDestinations.AKTIEN -> {
                val nisab = if (nisabTypeForLiquid == 0) nisabGoldEuro else nisabSilverEuro

                if (zakatBase >= nisab) {
                    val result = zakatBase * 0.025
                    if (destination == AppDestinations.FIRMA) {
                        "$resBaseText ${formatNumber(zakatBase)} €\n$resZakatAmountText ${formatNumber(result)} €"
                    } else {
                        "${formatNumber(result)} €"
                    }
                } else {
                    underNisabText
                }
            }
        }
    }

    fun calculateAnimalZakat(
        animalType: AnimalType,
        count: Int,
        underNisabText: String,
        resSheepText: String,
        resTabiText: String,
        resMusinnahText: String,
        resBintMakhadText: String,
        resBintLabunText: String,
        resHiqqahText: String,
        resJadhahText: String,
        resCamelRuleText: String
    ): String {
        return when (animalType) {
            AnimalType.SHEEP -> when {
                count < 40 -> "$underNisabText (40)"
                count <= 120 -> "1 $resSheepText"
                count <= 200 -> "2 $resSheepText"
                count <= 399 -> "3 $resSheepText"
                else -> "${count / 100} $resSheepText"
            }

            AnimalType.COWS -> when {
                count < 30 -> "$underNisabText (30)"
                count in 30..39 -> "1 $resTabiText"
                count in 40..59 -> "1 $resMusinnahText"
                else -> calculateCowZakatCombination(
                    count = count,
                    resTabiText = resTabiText,
                    resMusinnahText = resMusinnahText,
                    underNisabText = underNisabText
                )
            }

            AnimalType.CAMELS -> when {
                count < 5 -> "$underNisabText (5)"
                count in 5..9 -> "1 $resSheepText"
                count in 10..14 -> "2 $resSheepText"
                count in 15..19 -> "3 $resSheepText"
                count in 20..24 -> "4 $resSheepText"
                count in 25..35 -> "1 $resBintMakhadText"
                count in 36..45 -> "1 $resBintLabunText"
                count in 46..60 -> "1 $resHiqqahText"
                count in 61..75 -> "1 $resJadhahText"
                count in 76..90 -> "2 $resBintLabunText"
                count in 91..120 -> "2 $resHiqqahText"
                else -> resCamelRuleText
            }
        }
    }

    fun calculateCowZakatCombination(
        count: Int,
        resTabiText: String,
        resMusinnahText: String,
        underNisabText: String
    ): String {
        if (count < 30) return "$underNisabText (30)"

        var bestThirty = -1
        var bestForty = -1
        var bestUnits = Int.MAX_VALUE

        for (forty in 0..(count / 40)) {
            for (thirty in 0..(count / 30)) {
                val total = forty * 40 + thirty * 30
                val units = forty + thirty

                if (total == count && units < bestUnits) {
                    bestForty = forty
                    bestThirty = thirty
                    bestUnits = units
                }
            }
        }

        if (bestThirty == -1 && bestForty == -1) {
            var bestRemainder = Int.MAX_VALUE

            for (forty in 0..(count / 40)) {
                for (thirty in 0..(count / 30)) {
                    val total = forty * 40 + thirty * 30
                    if (total <= count) {
                        val remainder = count - total
                        val units = forty + thirty

                        if (remainder < bestRemainder || (remainder == bestRemainder && units < bestUnits)) {
                            bestRemainder = remainder
                            bestForty = forty
                            bestThirty = thirty
                            bestUnits = units
                        }
                    }
                }
            }
        }

        val parts = mutableListOf<String>()
        if (bestThirty > 0) parts.add("$bestThirty $resTabiText")
        if (bestForty > 0) parts.add("$bestForty $resMusinnahText")

        return if (parts.isEmpty()) underNisabText else parts.joinToString(" + ")
    }
}