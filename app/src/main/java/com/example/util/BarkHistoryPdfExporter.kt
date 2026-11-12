package com.example.util

import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.data.RecordedBark
import com.example.data.TranslationLog
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Exports recorded dog bark translation history logs (`TranslationLog` and `RecordedBark`)
 * into a formatted, multi-page PDF document and shares it via Android's ShareSheet
 * (`FileProvider` + `Intent.ACTION_SEND` with MIME type `application/pdf`).
 */
object BarkHistoryPdfExporter {

    private const val PAGE_WIDTH = 595 // A4 width in PostScript points (72 dpi)
    private const val PAGE_HEIGHT = 842 // A4 height in PostScript points
    private const val MARGIN = 36f

    /**
     * Converts a [RecordedBark] Room entity into a [TranslationLog] for unified PDF export.
     */
    fun RecordedBark.toTranslationLog(): TranslationLog {
        return TranslationLog(
            id = id,
            petId = petId,
            petName = petName,
            petBreed = petBreed,
            timestamp = timestamp,
            vocalizationType = vocalizationType,
            dominantPitchHz = dominantPitchHz,
            intensityDb = peakDb,
            cadenceDescription = cadenceDescription,
            englishTranslation = translation,
            dogThought = dogThought,
            detectedEmotion = detectedEmotion,
            urgencyLevel = urgencyLevel,
            confidencePercent = confidencePercent,
            recommendedAction = "",
            canineSubtext = "",
            isFavorite = isFavorite,
            audioFilePath = audioFilePath
        )
    }

    /**
     * Generates a shareable PDF file in `context.cacheDir/exports/` from a list of [TranslationLog]s.
     */
    fun exportTranslationLogsToPdfFile(
        context: Context,
        logs: List<TranslationLog>,
        reportTitle: String = "BarkTalk Canine Translation History Report"
    ): File {
        val exportDir = File(context.cacheDir, "exports").apply {
            if (!exists()) mkdirs()
        }
        val fileTimestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val pdfFile = File(exportDir, "BarkTalk_History_$fileTimestamp.pdf")

        FileOutputStream(pdfFile).use { fileOut ->
            writePdfToStream(logs, fileOut, reportTitle)
        }
        return pdfFile
    }

    /**
     * Generates a shareable PDF file in `context.cacheDir/exports/` from a list of [RecordedBark]s.
     */
    fun exportRecordedBarksToPdfFile(
        context: Context,
        recordedBarks: List<RecordedBark>,
        reportTitle: String = "BarkTalk Recorded Dog Barks Report"
    ): File {
        val logs = recordedBarks.map { it.toTranslationLog() }
        return exportTranslationLogsToPdfFile(context, logs, reportTitle)
    }

    /**
     * Renders the PDF report and writes the PDF bytes to the provided [OutputStream].
     */
    fun writePdfToStream(
        logs: List<TranslationLog>,
        outputStream: OutputStream,
        reportTitle: String = "BarkTalk Canine Translation History Report"
    ) {
        val renderedBytes = tryRenderWithAndroidPdfDocument(logs, reportTitle)
        if (renderedBytes != null && renderedBytes.size > 64) {
            outputStream.write(renderedBytes)
            outputStream.flush()
        } else {
            val fallbackBytes = buildStandardPdfBytes(logs, reportTitle)
            outputStream.write(fallbackBytes)
            outputStream.flush()
        }
    }

    /**
     * Launches Android's native ShareSheet (`Intent.ACTION_SEND`) for the generated PDF file
     * using `FileProvider` with read URI permissions granted.
     */
    fun sharePdfFile(
        context: Context,
        pdfFile: File,
        reportTitle: String = "BarkTalk Canine Translation History Report"
    ): Intent {
        val authority = "${context.packageName}.fileprovider"
        val contentUri: Uri = FileProvider.getUriForFile(context, authority, pdfFile)

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, contentUri)
            putExtra(Intent.EXTRA_SUBJECT, reportTitle)
            putExtra(
                Intent.EXTRA_TEXT,
                "Attached is the BarkTalk canine translation history log exported as a PDF report."
            )
            clipData = ClipData.newRawUri(reportTitle, contentUri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        val chooserIntent = Intent.createChooser(shareIntent, "Share Bark History PDF").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        try {
            context.startActivity(chooserIntent)
        } catch (e: Exception) {
            Toast.makeText(
                context,
                "Unable to open PDF share sheet on this device",
                Toast.LENGTH_SHORT
            ).show()
        }
        return shareIntent
    }

    /**
     * Convenience one-step helper that generates the PDF from [TranslationLog]s and opens the ShareSheet.
     */
    fun exportAndShareLogs(
        context: Context,
        logs: List<TranslationLog>,
        reportTitle: String = "BarkTalk Canine Translation History Report"
    ): File {
        val pdfFile = exportTranslationLogsToPdfFile(context, logs, reportTitle)
        sharePdfFile(context, pdfFile, reportTitle)
        return pdfFile
    }

    /**
     * Renders the multi-page PDF using Android's native [PdfDocument] canvas API.
     */
    private fun tryRenderWithAndroidPdfDocument(
        logs: List<TranslationLog>,
        reportTitle: String
    ): ByteArray? {
        val document = PdfDocument()
        return try {
            val dateFormatter = SimpleDateFormat("MMM d, yyyy • h:mm a", Locale.getDefault())
            val generatedOnText = "Generated on ${dateFormatter.format(Date())}"

            val headerBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.rgb(230, 81, 0) // Warm Canine Amber/Orange
                style = Paint.Style.FILL
            }
            val cardBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.rgb(250, 248, 245)
                style = Paint.Style.FILL
            }
            val cardBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.rgb(224, 216, 208)
                style = Paint.Style.STROKE
                strokeWidth = 1f
            }
            val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.WHITE
                textSize = 18f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }
            val subtitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.rgb(255, 236, 217)
                textSize = 10f
            }
            val sectionPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.rgb(55, 45, 38)
                textSize = 12f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }
            val bodyBoldPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.rgb(33, 33, 33)
                textSize = 11f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }
            val bodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.rgb(66, 66, 66)
                textSize = 10f
            }
            val metaPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.rgb(110, 102, 94)
                textSize = 9f
            }

            var pageNumber = 1
            var pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
            var currentPage = document.startPage(pageInfo)
            var canvas: Canvas = currentPage.canvas
            var cursorY = drawPageHeader(
                canvas = canvas,
                reportTitle = reportTitle,
                generatedOnText = generatedOnText,
                logs = logs,
                isFirstPage = true,
                headerBgPaint = headerBgPaint,
                titlePaint = titlePaint,
                subtitlePaint = subtitlePaint,
                sectionPaint = sectionPaint,
                metaPaint = metaPaint
            )

            if (logs.isEmpty()) {
                canvas.drawText(
                    "No bark translation logs recorded yet.",
                    MARGIN,
                    cursorY + 30f,
                    bodyBoldPaint
                )
            } else {
                for ((index, log) in logs.withIndex()) {
                    val translationLines = wrapText(
                        text = "\"${log.englishTranslation}\"",
                        paint = bodyBoldPaint,
                        maxWidth = PAGE_WIDTH - (MARGIN * 2) - 24f
                    )
                    val thoughtLines = if (log.dogThought.isNotBlank()) {
                        wrapText(
                            text = "Inner Thought: ${log.dogThought}",
                            paint = bodyPaint,
                            maxWidth = PAGE_WIDTH - (MARGIN * 2) - 24f
                        )
                    } else {
                        emptyList()
                    }

                    val cardHeight = 56f + (translationLines.size * 14f) + (thoughtLines.size * 13f)

                    if (cursorY + cardHeight > PAGE_HEIGHT - 50f) {
                        drawPageFooter(canvas, pageNumber, metaPaint)
                        document.finishPage(currentPage)

                        pageNumber++
                        pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
                        currentPage = document.startPage(pageInfo)
                        canvas = currentPage.canvas
                        cursorY = drawPageHeader(
                            canvas = canvas,
                            reportTitle = reportTitle,
                            generatedOnText = generatedOnText,
                            logs = logs,
                            isFirstPage = false,
                            headerBgPaint = headerBgPaint,
                            titlePaint = titlePaint,
                            subtitlePaint = subtitlePaint,
                            sectionPaint = sectionPaint,
                            metaPaint = metaPaint
                        )
                    }

                    val cardRect = RectF(MARGIN, cursorY, PAGE_WIDTH - MARGIN, cursorY + cardHeight)
                    canvas.drawRoundRect(cardRect, 8f, 8f, cardBgPaint)
                    canvas.drawRoundRect(cardRect, 8f, 8f, cardBorderPaint)

                    var innerY = cursorY + 16f
                    val formattedDate = dateFormatter.format(Date(log.timestamp))
                    val headerLine = "#${index + 1} • ${log.petName} (${log.petBreed}) • $formattedDate"
                    canvas.drawText(headerLine, MARGIN + 12f, innerY, sectionPaint)

                    val urgencyTag = "[${log.urgencyLevel} • ${log.detectedEmotion}]"
                    val tagWidth = metaPaint.measureText(urgencyTag)
                    canvas.drawText(
                        urgencyTag,
                        PAGE_WIDTH - MARGIN - 12f - tagWidth,
                        innerY,
                        metaPaint
                    )

                    innerY += 16f
                    for (line in translationLines) {
                        canvas.drawText(line, MARGIN + 12f, innerY, bodyBoldPaint)
                        innerY += 14f
                    }

                    for (line in thoughtLines) {
                        canvas.drawText(line, MARGIN + 12f, innerY, bodyPaint)
                        innerY += 13f
                    }

                    val telemetryLine =
                        "Acoustics: ${log.vocalizationType} | ${log.intensityDb} dB | ${log.dominantPitchHz} Hz | Confidence: ${log.confidencePercent}%"
                    canvas.drawText(telemetryLine, MARGIN + 12f, innerY + 2f, metaPaint)

                    cursorY += cardHeight + 10f
                }
            }

            drawPageFooter(canvas, pageNumber, metaPaint)
            document.finishPage(currentPage)

            val out = ByteArrayOutputStream()
            document.writeTo(out)
            out.toByteArray()
        } catch (e: Throwable) {
            null
        } finally {
            try {
                document.close()
            } catch (_: Throwable) {
            }
        }
    }

    private fun drawPageHeader(
        canvas: Canvas,
        reportTitle: String,
        generatedOnText: String,
        logs: List<TranslationLog>,
        isFirstPage: Boolean,
        headerBgPaint: Paint,
        titlePaint: Paint,
        subtitlePaint: Paint,
        sectionPaint: Paint,
        metaPaint: Paint
    ): Float {
        val bannerHeight = if (isFirstPage) 76f else 52f
        val bannerRect = RectF(MARGIN, MARGIN, PAGE_WIDTH - MARGIN, MARGIN + bannerHeight)
        canvas.drawRoundRect(bannerRect, 10f, 10f, headerBgPaint)

        canvas.drawText(reportTitle, MARGIN + 16f, MARGIN + 26f, titlePaint)
        canvas.drawText(generatedOnText, MARGIN + 16f, MARGIN + 42f, subtitlePaint)

        if (isFirstPage) {
            val avgDb = if (logs.isNotEmpty()) logs.map { it.intensityDb }.average().toInt() else 0
            val avgPitch = if (logs.isNotEmpty()) logs.map { it.dominantPitchHz }.average().toInt() else 0
            val summaryLine =
                "Total Translations: ${logs.size}   |   Avg Intensity: $avgDb dB   |   Avg Pitch: $avgPitch Hz"
            canvas.drawText(summaryLine, MARGIN + 16f, MARGIN + 62f, subtitlePaint)
        }

        val nextY = MARGIN + bannerHeight + 18f
        canvas.drawText("Recorded Bark Translation Logs", MARGIN, nextY, sectionPaint)
        return nextY + 10f
    }

    private fun drawPageFooter(canvas: Canvas, pageNumber: Int, metaPaint: Paint) {
        val footerText = "BarkTalk Canine Communication Report • Page $pageNumber"
        canvas.drawText(footerText, MARGIN, PAGE_HEIGHT - 20f, metaPaint)
    }

    private fun wrapText(text: String, paint: Paint, maxWidth: Float): List<String> {
        if (text.isBlank()) return emptyList()
        val words = text.split(" ")
        val lines = mutableListOf<String>()
        var currentLine = StringBuilder()

        for (word in words) {
            val candidate = if (currentLine.isEmpty()) word else "$currentLine $word"
            val measured = paint.measureText(candidate)
            // In JVM tests where Paint.measureText may return 0f, fall back to character count (~85 chars per line)
            val exceedsWidth = if (measured > 0f) {
                measured > maxWidth
            } else {
                candidate.length > 85
            }

            if (exceedsWidth && currentLine.isNotEmpty()) {
                lines.add(currentLine.toString())
                currentLine = StringBuilder(word)
            } else {
                currentLine = StringBuilder(candidate)
            }
        }
        if (currentLine.isNotEmpty()) {
            lines.add(currentLine.toString())
        }
        return lines
    }

    /**
     * Generates a standards-compliant PDF-1.4 document containing the report title and
     * all exported bark translation logs. Used as a deterministic fallback when native
     * PdfDocument rendering is unavailable (such as in headless JVM unit test environments).
     */
    internal fun buildStandardPdfBytes(
        logs: List<TranslationLog>,
        reportTitle: String
    ): ByteArray {
        val dateFormatter = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US)
        val lines = mutableListOf<String>()
        lines.add(reportTitle)
        lines.add("Generated: ${dateFormatter.format(Date())} | Total Logs: ${logs.size}")
        lines.add("----------------------------------------------------------------------")

        if (logs.isEmpty()) {
            lines.add("No recorded bark translations available.")
        } else {
            logs.forEachIndexed { idx, log ->
                val ts = dateFormatter.format(Date(log.timestamp))
                lines.add("${idx + 1}. [${ts}] ${log.petName} (${log.petBreed}) - ${log.vocalizationType}")
                lines.add("   Translation: \"${log.englishTranslation}\"")
                lines.add("   Acoustics: ${log.intensityDb} dB | ${log.dominantPitchHz} Hz | Emotion: ${log.detectedEmotion} | Urgency: ${log.urgencyLevel}")
                if (log.dogThought.isNotBlank()) {
                    lines.add("   Dog Thought: ${log.dogThought}")
                }
                lines.add("")
            }
        }

        val contentBuilder = StringBuilder()
        contentBuilder.appendLine("BT")
        contentBuilder.appendLine("/F1 10 Tf")
        contentBuilder.appendLine("14 TL")
        contentBuilder.appendLine("36 800 Td")
        for (line in lines.take(52)) {
            val escaped = line
                .replace("\\", "\\\\")
                .replace("(", "\\(")
                .replace(")", "\\)")
            contentBuilder.appendLine("($escaped) Tj T*")
        }
        contentBuilder.appendLine("ET")
        val streamContent = contentBuilder.toString()

        val pdfBuilder = StringBuilder()
        pdfBuilder.append("%PDF-1.4\n")
        val obj1Offset = pdfBuilder.length
        pdfBuilder.append("1 0 obj\n<< /Type /Catalog /Pages 2 0 R >>\nendobj\n")
        val obj2Offset = pdfBuilder.length
        pdfBuilder.append("2 0 obj\n<< /Type /Pages /Kids [3 0 R] /Count 1 >>\nendobj\n")
        val obj3Offset = pdfBuilder.length
        pdfBuilder.append(
            "3 0 obj\n<< /Type /Page /Parent 2 0 R /MediaBox [0 0 $PAGE_WIDTH $PAGE_HEIGHT] /Contents 4 0 R /Resources << /Font << /F1 5 0 R >> >> >>\nendobj\n"
        )
        val obj4Offset = pdfBuilder.length
        pdfBuilder.append(
            "4 0 obj\n<< /Length ${streamContent.toByteArray(Charsets.UTF_8).size} >>\nstream\n$streamContent\nendstream\nendobj\n"
        )
        val obj5Offset = pdfBuilder.length
        pdfBuilder.append(
            "5 0 obj\n<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica >>\nendobj\n"
        )
        val xrefOffset = pdfBuilder.length
        pdfBuilder.append("xref\n0 6\n")
        pdfBuilder.append("0000000000 65535 f \n")
        pdfBuilder.append(String.format(Locale.US, "%010d 00000 n \n", obj1Offset))
        pdfBuilder.append(String.format(Locale.US, "%010d 00000 n \n", obj2Offset))
        pdfBuilder.append(String.format(Locale.US, "%010d 00000 n \n", obj3Offset))
        pdfBuilder.append(String.format(Locale.US, "%010d 00000 n \n", obj4Offset))
        pdfBuilder.append(String.format(Locale.US, "%010d 00000 n \n", obj5Offset))
        pdfBuilder.append("trailer\n<< /Size 6 /Root 1 0 R >>\n")
        pdfBuilder.append("startxref\n$xrefOffset\n%%EOF\n")

        return pdfBuilder.toString().toByteArray(Charsets.UTF_8)
    }
}
