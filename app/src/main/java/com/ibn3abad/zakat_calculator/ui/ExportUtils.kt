package com.ibn3abad.zakat_calculator.ui

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import com.ibn3abad.zakat_calculator.R
import com.ibn3abad.zakat_calculator.data.SavedCalculation
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private fun lines(context: Context, calc: SavedCalculation): List<String> {
    val dateStr = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()).format(Date(calc.timestamp))
    val list = mutableListOf(
        context.getString(R.string.share_file_title),
        "",
        context.getString(R.string.history_year_label, calc.year),
        context.getString(R.string.history_input_label, calc.inputValue),
        "Datum: $dateStr",
        "Kategorie: ${calc.category}"
    )
    if (calc.liabilities.isNotBlank()) {
        list += context.getString(R.string.history_liabilities_label, calc.liabilities)
    }
    list += context.getString(R.string.history_result_label, calc.resultText)
    if (calc.note.isNotBlank()) {
        list += context.getString(R.string.history_note_label, calc.note)
    }
    return list
}

/** Erstellt eine PDF und gibt die URI zurück */
fun createPdf(context: Context, calc: SavedCalculation): File {
    val pdf = PdfDocument()
    val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4
    val page = pdf.startPage(pageInfo)
    val canvas = page.canvas

    val titlePaint = Paint().apply {
        color = Color.BLACK
        textSize = 22f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }
    val textPaint = Paint().apply {
        color = Color.BLACK
        textSize = 14f
    }

    var y = 60f
    val x = 50f
    lines(context, calc).forEachIndexed { index, line ->
        val paint = if (index == 0) titlePaint else textPaint
        canvas.drawText(line, x, y, paint)
        y += if (index == 0) 36f else 22f
    }

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

    val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#6650a4")
        textSize = 64f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }
    val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.DKGRAY
        textSize = 38f
    }

    var y = 120f
    val x = 80f
    lines(context, calc).forEachIndexed { index, line ->
        val paint = if (index == 0) titlePaint else textPaint
        canvas.drawText(line, x, y, paint)
        y += if (index == 0) 90f else 60f
    }

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
