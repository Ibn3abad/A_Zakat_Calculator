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

package com.ibn3abad.zakat_calculator.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ibn3abad.zakat_calculator.AppDestinations
import com.ibn3abad.zakat_calculator.R
import com.ibn3abad.zakat_calculator.ZakatViewModel
import com.ibn3abad.zakat_calculator.data.SavedCalculation
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HistoryScreen(viewModel: ZakatViewModel) {
    val context = LocalContext.current
    val items by viewModel.savedCalculations.collectAsState()
    val years by viewModel.savedYears.collectAsState()
    var itemToDelete by remember { mutableStateOf<SavedCalculation?>(null) }

    if (itemToDelete != null) {
        AlertDialog(
            onDismissRequest = { itemToDelete = null },
            title = { Text(stringResource(R.string.delete_dialog_title)) },
            text = { Text(stringResource(R.string.delete_dialog_text)) },
            confirmButton = {
                TextButton(onClick = {
                    itemToDelete?.let { viewModel.deleteCalculation(it) }
                    itemToDelete = null
                }) {
                    Text(stringResource(R.string.delete_dialog_confirm), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { itemToDelete = null }) {
                    Text(stringResource(R.string.delete_dialog_dismiss))
                }
            }
        )
    }

    if (items.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
            Text(
                text = stringResource(R.string.history_empty),
                color = Color.White,
                style = MaterialTheme.typography.bodyLarge
            )
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        years.forEach { year ->
            item {
                Text(
                    text = stringResource(R.string.history_year_label, year),
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White
                )
                // Gesamtsumme für das Jahr (nur €-Werte) – optional
                val yearTotal = items
                    .filter { it.year == year }
                    .mapNotNull { extractEuroValue(it.resultText) }
                    .sum()
                if (yearTotal > 0) {
                    Text(
                        text = stringResource(R.string.history_total_label, yearTotal),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.LightGray
                    )
                }
            }
            items(items.filter { it.year == year }) { calc ->
                CalculationCard(
                    calc = calc,
                    onDelete = { itemToDelete = calc },
                    onSharePdf = { sharePdf(context, calc) },
                    onShareImage = { shareImage(context, calc) }
                )
            }
        }
    }
}

@Composable
private fun CalculationCard(
    calc: SavedCalculation,
    onDelete: () -> Unit,
    onSharePdf: () -> Unit,
    onShareImage: () -> Unit
) {
    val dateFmt = remember { SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()) }

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(
            containerColor = Color(0xFF2C2C2C),
            contentColor = Color.White
        )
    ) {
        Column(Modifier.padding(16.dp)) {
            val destination = remember(calc.category) {
                try {
                    AppDestinations.valueOf(calc.category)
                } catch (e: Exception) {
                    null
                }
            }
            val categoryLabel = if (destination != null) {
                stringResource(destination.labelRes)
            } else {
                calc.category
            }

            Text(categoryLabel, style = MaterialTheme.typography.titleMedium, color = Color.White)
            Text(dateFmt.format(Date(calc.timestamp)), style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            Spacer(Modifier.height(8.dp))
            Text(stringResource(R.string.history_input_label, calc.inputValue), color = Color.White)
            if (calc.liabilities.isNotBlank()) {
                Text(stringResource(R.string.history_liabilities_label, calc.liabilities), color = Color.White)
            }

            val localizedNisabType = when (calc.nisabType) {
                "Gold" -> stringResource(R.string.gold)
                "Silber", "Silver" -> stringResource(R.string.silver)
                "Ernte", "Harvest" -> stringResource(R.string.cat_harvest)
                "Schafe", "Sheep" -> stringResource(R.string.animal_sheep)
                "Kühe", "Cows" -> stringResource(R.string.animal_cows)
                "Kamele", "Camels" -> stringResource(R.string.animal_camels)
                else -> calc.nisabType
            }

            if (calc.nisabValue.isNotBlank()) {
                Text(stringResource(R.string.history_nisab_label, localizedNisabType, calc.nisabValue), color = Color.White)
            }
            Text(
                stringResource(R.string.history_result_label, calc.resultText),
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            if (calc.note.isNotBlank()) {
                Text(stringResource(R.string.history_note_label, calc.note), color = Color.LightGray)
            }
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilledTonalButton(onClick = onSharePdf) {
                    Icon(Icons.Default.PictureAsPdf, null)
                    Spacer(Modifier.width(4.dp))
                    Text("PDF")
                }
                FilledTonalButton(onClick = onShareImage) {
                    Icon(Icons.Default.Share, null)
                    Spacer(Modifier.width(4.dp))
                    Text("Bild")
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = stringResource(R.string.history_delete_content_description)
                    )
                }
            }
        }
    }
}

private fun extractEuroValue(text: String): Double? {
    val regex = Regex("([0-9]+[.,][0-9]+)\\s*€")
    return regex.find(text)?.groupValues?.get(1)?.replace(',', '.')?.toDoubleOrNull()
}
