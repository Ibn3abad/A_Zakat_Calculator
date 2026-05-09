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

package com.ibn3abad.zakat_calculator.ui

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import com.ibn3abad.zakat_calculator.AppDestinations
import com.ibn3abad.zakat_calculator.R
import com.ibn3abad.zakat_calculator.data.SavedCalculation
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private fun getCategoryLabel(context: Context, categoryName: String): String {
    return try {
        val dest = AppDestinations.valueOf(categoryName)
        context.getString(dest.labelRes)
    } catch (e: Exception) {
        categoryName
    }
}

private fun lines(context: Context, calc: SavedCalculation): List<String> {
    val dateStr = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()).format(Date(calc.timestamp))
    val categoryLabel = getCategoryLabel(context, calc.category)
    
    val list = mutableListOf(
        context.getString(R.string.share_file_title),
        "",
        context.getString(R.string.history_year_label, calc.year),
        "Kategorie: $categoryLabel",
        context.getString(R.string.history_input_label, calc.inputValue)
    )
    if (calc.liabilities.isNotBlank()) {
        list += context.getString(R.string.history_liabilities_label, calc.liabilities)
    }
    list += context.getString(R.string.history_result_label, calc.resultText)
    if (calc.note.isNotBlank()) {
        list += context.getString(R.string.history_note_label, calc.note)
    }
    list += ""
    list += "Datum: $dateStr"
    
    return list
}

/** Erstellt eine PDF und gibt die URI zurück */
fun createPdf(context: Context, calc: SavedCalculation): File {
    val pdf = PdfDocument()
    val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4
    val page = pdf.startPage(pageInfo)
    val canvas = page.canvas

    val primaryColor = Color.parseColor("#6650a4")
    
    // Header
    val headerPaint = Paint().apply { color = primaryColor }
    canvas.drawRect(0f, 0f, 595f, 100f, headerPaint)

    // Logo im Header
    val logo = BitmapFactory.decodeResource(context.resources, R.drawable.app_logo)
    if (logo != null) {
        val scaledLogo = Bitmap.createScaledBitmap(logo, 70, 70, true)
        canvas.drawBitmap(scaledLogo, 40f, 15f, null)
    }

    // App Name im Header
    val appNamePaint = Paint().apply {
        color = Color.WHITE
        textSize = 24f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        isAntiAlias = true
    }
    canvas.drawText(context.getString(R.string.app_name), 130f, 60f, appNamePaint)

    // Titel und Inhalt Paints
    val titlePaint = Paint().apply {
        color = Color.BLACK
        textSize = 22f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        isAntiAlias = true
    }
    val textPaint = Paint().apply {
        color = Color.BLACK
        textSize = 14f
        isAntiAlias = true
    }

    var y = 160f
    val x = 50f
    lines(context, calc).forEachIndexed { index, line ->
        if (line.isEmpty()) {
            y += 10f
            return@forEachIndexed
        }
        
        val paint = if (index == 0) titlePaint else textPaint
        canvas.drawText(line, x, y, paint)
        y += if (index == 0) 40f else 25f
        
        // Trennlinie nach dem Haupttitel
        if (index == 0) {
            val linePaint = Paint().apply {
                color = Color.LTGRAY
                strokeWidth = 1f
            }
            canvas.drawLine(x, y - 15f, 545f, y - 15f, linePaint)
            y += 10f
        }
    }

    // Footer mit Website
    val footerPaint = Paint().apply {
        color = Color.GRAY
        textSize = 10f
        isAntiAlias = true
    }
    canvas.drawText(context.getString(R.string.website_url), x, 800f, footerPaint)

    pdf.finishPage(page)

    val dir = File(context.cacheDir, "exports").apply { mkdirs() }
    val file = File(dir, "zakat_${calc.year}_${calc.id}.pdf")
    FileOutputStream(file).use { pdf.writeTo(it) }
    pdf.close()
    return file
}

/** Erstellt ein PNG-Bild mit den Berechnungs-Daten */
fun createImage(context: Context, calc: SavedCalculation): File {
    val width = 1080
    val height = 1350
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    canvas.drawColor(Color.WHITE)

    val primaryColor = Color.parseColor("#6650a4")
    
    // Header
    val headerRect = Rect(0, 0, width, 250)
    canvas.drawRect(headerRect, Paint().apply { color = primaryColor })

    // Logo im Header
    val logo = BitmapFactory.decodeResource(context.resources, R.drawable.app_logo)
    if (logo != null) {
        val scaledLogo = Bitmap.createScaledBitmap(logo, 180, 180, true)
        canvas.drawBitmap(scaledLogo, 80f, 35f, null)
    }

    // App Name im Header
    val appNamePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textSize = 60f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }
    canvas.drawText(context.getString(R.string.app_name), 300f, 145f, appNamePaint)

    // Inhalt Paints
    val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLACK
        textSize = 64f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }
    val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.DKGRAY
        textSize = 38f
    }

    var y = 380f
    val x = 80f
    lines(context, calc).forEachIndexed { index, line ->
        if (line.isEmpty()) {
            y += 20f
            return@forEachIndexed
        }
        
        val paint = if (index == 0) titlePaint else textPaint
        canvas.drawText(line, x, y, paint)
        y += if (index == 0) 100f else 70f
        
        // Trennlinie nach dem Haupttitel
        if (index == 0) {
             canvas.drawLine(x, y - 30f, width - 80f, y - 30f, Paint().apply { 
                 color = Color.LTGRAY
                 strokeWidth = 3f 
             })
             y += 40f
        }
    }
    
    // Footer mit Website
    val footerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.GRAY
        textSize = 28f
    }
    canvas.drawText(context.getString(R.string.website_url), x, height - 80f, footerPaint)

    val dir = File(context.cacheDir, "exports").apply { mkdirs() }
    val file = File(dir, "zakat_${calc.year}_${calc.id}.png")
    FileOutputStream(file).use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
    bitmap.recycle()
    return file
}

/** Teilt die Datei über das System-Share-Sheet (WhatsApp, Signal, E-Mail, ...) */
fun shareFile(context: Context, file: File, mimeType: String) {
    val uri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        file
    )
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = mimeType
        putExtra(Intent.EXTRA_STREAM, uri)
        putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.share_file_title))
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(intent, context.getString(R.string.share_chooser_title)))
}

fun sharePdf(context: Context, calc: SavedCalculation) {
    val file = createPdf(context, calc)
    shareFile(context, file, "application/pdf")
}

fun shareImage(context: Context, calc: SavedCalculation) {
    val file = createImage(context, calc)
    shareFile(context, file, "image/png")
}
