/**
 * @author     A. KHOUK
 * @date       12.05.2026
 * @version    3.24
 * @copyright  Copyright (c) 2026, A. KHOUK.
 * @license    This program is free software: you can redistribute it and/or modify
 *             it under the terms of the GNU General Public License as published by
 *             the Free Software Foundation, either version 3 of the License, or
 *             (at your option) any later version.
 */

package com.ibn3abad.zakat_calculator

import android.content.Context
import androidx.core.content.edit
import com.ibn3abad.zakat_calculator.data.model.MetalPrice
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.*
import java.net.URL

class MetalPriceRepository(context: Context) {

    companion object {
        // Deine Supabase-Werte
        private const val SUPABASE_URL = "https://olkifxyhpqdwlldbuqnb.supabase.co"
        private const val SUPABASE_ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Im9sa2lmeHlocHFkd2xsZGJ1cW5iIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NzgzNDQ2MDYsImV4cCI6MjA5MzkyMDYwNn0.HIOV8Lj6Bilno8nT9a-v6-7jQmUhfjo9-RHQ9QghkX0"

        private const val PREFS_NAME = "metal_prices"
        private const val KEY_PRICE_DATE = "price_date"
        private const val KEY_GOLD = "gold_gram_eur"
        private const val KEY_SILVER = "silver_gram_eur"
        private const val KEY_USDEUR = "usdeur"
    }

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /**
     * Gibt den aktuellen Preis zurück.
     * Holt nur dann vom Server, wenn lokal nichts da ist
     * oder die Daten älter als 30 Tage sind.
     */
    suspend fun getPrice(): MetalPrice? {
        val cached = getCached()
        // Wenn kein Cache da ist, oder Daten zu alt (24h), oder Goldpreis ungültig (<= 0) -> Neu laden
        if (cached == null || isOlderThan24Hours() || cached.goldGramEur <= 0) {
            val fresh = fetchFromSupabase()
            if (fresh != null) {
                saveToCache(fresh)
                return fresh
            }
        }
        return cached
    }

    private suspend fun fetchFromSupabase(): MetalPrice? = withContext(Dispatchers.IO) {
        try {
            val url = "$SUPABASE_URL/rest/v1/metalprice_monthly" +
                    "?select=price_date,gold_gram_eur,silver_gram_eur,usdeur" +
                    "&order=price_date.desc&limit=1"

            val connection = URL(url).openConnection() as java.net.HttpURLConnection
            connection.setRequestProperty("apikey", SUPABASE_ANON_KEY)
            connection.setRequestProperty("Authorization", "Bearer $SUPABASE_ANON_KEY")
            connection.setRequestProperty("Accept", "application/json")
            connection.connectTimeout = 10000
            connection.readTimeout = 10000

            val responseCode = connection.responseCode
            if (responseCode != 200) {
                val errorMsg = connection.errorStream?.bufferedReader()?.readText()
                android.util.Log.e("MetalPriceRepo", "Fehler von Supabase ($responseCode): $errorMsg")
                connection.disconnect()
                return@withContext null
            }

            val response = connection.inputStream.bufferedReader().readText()
            connection.disconnect()

            val jsonElement = Json.parseToJsonElement(response)
            if (jsonElement !is JsonArray || jsonElement.isEmpty()) {
                android.util.Log.w("MetalPriceRepo", "Keine Daten gefunden oder falsches Format: $response")
                return@withContext null
            }

            val obj = jsonElement[0].jsonObject
            MetalPrice(
                priceDate = obj["price_date"]?.jsonPrimitive?.content ?: "---",
                goldGramEur = obj["gold_gram_eur"]?.jsonPrimitive?.double ?: 0.0,
                silverGramEur = obj["silver_gram_eur"]?.jsonPrimitive?.double ?: 0.0,
                usdEur = obj["usdeur"]?.jsonPrimitive?.double ?: 1.0
            )
        } catch (e: Exception) {
            android.util.Log.e("MetalPriceRepo", "Netzwerkfehler: ${e.message}")
            null
        }
    }

    private fun getCached(): MetalPrice? {
        val date = prefs.getString(KEY_PRICE_DATE, null) ?: return null
        return MetalPrice(
            priceDate = date,
            goldGramEur = prefs.getFloat(KEY_GOLD, 0f).toDouble(),
            silverGramEur = prefs.getFloat(KEY_SILVER, 0f).toDouble(),
            usdEur = prefs.getFloat(KEY_USDEUR, 0f).toDouble()
        )
    }

    private fun saveToCache(price: MetalPrice) {
        prefs.edit {
            putString(KEY_PRICE_DATE, price.priceDate)
            putFloat(KEY_GOLD, price.goldGramEur.toFloat())
            putFloat(KEY_SILVER, price.silverGramEur.toFloat())
            putFloat(KEY_USDEUR, price.usdEur.toFloat())
            putLong("last_fetch_timestamp", System.currentTimeMillis())
        }
    }

    private fun isOlderThan24Hours(): Boolean {
        val lastFetch = prefs.getLong("last_fetch_timestamp", 0L)
        val oneDayInMillis = 24L * 60 * 60 * 1000
        return System.currentTimeMillis() - lastFetch > oneDayInMillis
    }
}