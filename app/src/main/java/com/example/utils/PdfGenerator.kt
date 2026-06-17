package com.example.utils

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import java.io.File
import java.io.FileOutputStream

object PdfGenerator {

    fun generateSalarySlip(
        context: Context,
        fileName: String,
        userName: String,
        startDate: String,
        endDate: String,
        dutyDays: Double,
        dutyPay: Double,
        overtimePay: Double,
        foodAllowance: Double,
        pfDeduction: Double,
        advance: Double,
        netPayable: Double,
        currencySymbol: String
    ): File? {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 size
        val page = pdfDocument.startPage(pageInfo)
        val canvas: Canvas = page.canvas

        val titlePaint = Paint().apply {
            color = Color.rgb(67, 97, 238) // Theme Cosmic Blue
            textSize = 24f
            isFakeBoldText = true
            isAntiAlias = true
        }

        val subtitlePaint = Paint().apply {
            color = Color.DKGRAY
            textSize = 14f
            isFakeBoldText = true
            isAntiAlias = true
        }

        val textPaint = Paint().apply {
            color = Color.BLACK
            textSize = 11f
            isAntiAlias = true
        }

        val boldTextPaint = Paint().apply {
            color = Color.BLACK
            textSize = 12f
            isFakeBoldText = true
            isAntiAlias = true
        }

        val linePaint = Paint().apply {
            color = Color.LTGRAY
            strokeWidth = 1f
        }

        val themeHeaderPaint = Paint().apply {
            color = Color.rgb(67, 97, 238)
        }

        val themeHeaderWhiteTextPaint = Paint().apply {
            color = Color.WHITE
            textSize = 12f
            isFakeBoldText = true
            isAntiAlias = true
        }

        // Draw header
        canvas.drawText("Duty Tracker Pro", 40f, 50f, titlePaint)
        canvas.drawText("SALARY SLIP", 40f, 75f, subtitlePaint)
        
        canvas.drawText("Period: $startDate to $endDate", 40f, 100f, textPaint)
        canvas.drawText("Employee Name: $userName", 40f, 120f, boldTextPaint)

        // Draw horizontal break line
        canvas.drawLine(40f, 135f, 555f, 135f, linePaint)

        // Draw Table Header
        canvas.drawRect(40f, 155f, 555f, 185f, themeHeaderPaint)
        canvas.drawText("Description", 50f, 175f, themeHeaderWhiteTextPaint)
        canvas.drawText("Amount", 420f, 175f, themeHeaderWhiteTextPaint)

        var currentY = 210f
        val lineSpacing = 30f

        val items = listOf(
            Pair("Duty Pay ($dutyDays worked days)", dutyPay),
            Pair("Food Allowance", foodAllowance),
            Pair("Overtime Pay", overtimePay),
            Pair("PF Deduction (Fixed Amount)", -pfDeduction),
            Pair("Advance Payments Deducted", -advance)
        )

        for (item in items) {
            canvas.drawText(item.first, 50f, currentY, textPaint)
            val prefix = if (item.second < 0) "- $currencySymbol" else "$currencySymbol "
            val absValue = Math.abs(item.second)
            canvas.drawText("$prefix${String.format("%.0f", absValue)}", 420f, currentY, textPaint)
            canvas.drawLine(40f, currentY + 10f, 555f, currentY + 10f, linePaint)
            currentY += lineSpacing
        }

        // Net Payable Block
        currentY += 10f
        canvas.drawText("NET PAYABLE", 50f, currentY, boldTextPaint)
        canvas.drawText("$currencySymbol ${String.format("%.0f", netPayable)}", 420f, currentY, boldTextPaint)
        canvas.drawLine(40f, currentY + 10f, 555f, currentY + 10f, linePaint)

        // Footer note
        canvas.drawText("Generated natively via Duty Tracker Pro | Made in India 🇮🇳", 40f, 780f, Paint().apply {
            color = Color.GRAY
            textSize = 9f
            isAntiAlias = true
        })

        pdfDocument.finishPage(page)

        val directory = context.getExternalFilesDir(null) ?: context.filesDir
        val file = File(directory, fileName)

        try {
            val fileOutputStream = FileOutputStream(file)
            pdfDocument.writeTo(fileOutputStream)
            pdfDocument.close()
            fileOutputStream.close()
            return file
        } catch (e: Exception) {
            e.printStackTrace()
            pdfDocument.close()
            return null
        }
    }
}
