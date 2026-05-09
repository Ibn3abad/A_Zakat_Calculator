/**
 * @author     A. KHOUK
 * @date       06.04.2026
 * @version    2.15
 * @copyright  Copyright (c) 2026, A. KHOUK.
 * @license    This program is free software: you can redistribute it and/or modify
 *             it under the terms of the GNU General Public License as published by
 *             the Free Software Foundation, either version 3 of the License, or
 *             (at your option) any later version.
 */

package com.ibn3abad.zakat_calculator

import android.content.Context
import com.ibn3abad.zakat_calculator.data.model.MetalPrice
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.*
import java.net.URL

class MetalPriceRepository(private val context: Context) {

    companion object {
        // Deine Supabase-Werte
        private const val SUPABASE_URL = "https://olkifxyhpqdwlldbuqnb.supabase.co"
        private const val SUPABASE_ANON_KEY = "sb_publishable_tLyFyHduXH7dTQsVrjy2MA_FvXC7KU5"

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
        if (cached != null && !isOlderThan30Days()) {
            return cached
        }
        return fetchFromSupabase()?.also { saveToCache(it) }
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

            val response = connection.inputStream.bufferedReader().readText()
            connection.disconnect()

            // PostgREST gibt ein Array zurück
            val array = Json.parseToJsonElement(response).jsonArray
            if (array.isEmpty()) return@withContext null

            val obj = array[0].jsonObject
            MetalPrice(
                price_date = obj["price_date"]!!.jsonPrimitive.content,
                gold_gram_eur = obj["gold_gram_eur"]!!.jsonPrimitive.double,
                silver_gram_eur = obj["silver_gram_eur"]!!.jsonPrimitive.double,
                usdeur = obj["usdeur"]!!.jsonPrimitive.double
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun getCached(): MetalPrice? {
        val date = prefs.getString(KEY_PRICE_DATE, null) ?: return null
        return MetalPrice(
            price_date = date,
            gold_gram_eur = prefs.getFloat(KEY_GOLD, 0f).toDouble(),
            silver_gram_eur = prefs.getFloat(KEY_SILVER, 0f).toDouble(),
            usdeur = prefs.getFloat(KEY_USDEUR, 0f).toDouble()
        )
    }

    private fun saveToCache(price: MetalPrice) {
        prefs.edit()
            .putString(KEY_PRICE_DATE, price.price_date)
            .putFloat(KEY_GOLD, price.gold_gram_eur.toFloat())
            .putFloat(KEY_SILVER, price.silver_gram_eur.toFloat())
            .putFloat(KEY_USDEUR, price.usdeur.toFloat())
            .putLong("last_fetch_timestamp", System.currentTimeMillis())
            .apply()
    }

    private fun isOlderThan30Days(): Boolean {
        val lastFetch = prefs.getLong("last_fetch_timestamp", 0L)
        val thirtyDaysInMillis = 30L * 24 * 60 * 60 * 1000
        return System.currentTimeMillis() - lastFetch > thirtyDaysInMillis
    }
}