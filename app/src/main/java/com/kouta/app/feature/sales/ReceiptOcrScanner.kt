package com.kouta.app.feature.sales

import android.graphics.Bitmap
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.kuota.core.data.parser.BankNotificationParser
import com.kuota.core.model.SaleSource
import kotlinx.coroutines.tasks.await

data class OcrScanResult(
    val amountCents: Long,
    val source: SaleSource,
    val isSuccess: Boolean,
    val detectedTextSnippet: String? = null
)

/**
 * Escáner de comprobantes en el dispositivo mediante Google ML Kit Text Recognition.
 * Permite registrar ventas a partir de capturas de pantalla o fotos de comprobantes de pago
 * (Yape, Plin, BCP) cuando el vendedor no tenía habilitadas las notificaciones o cobró por otro canal.
 */
object ReceiptOcrScanner {

    private val recognizer by lazy {
        TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    }

    suspend fun scanReceiptBitmap(bitmap: Bitmap): OcrScanResult = runCatching {
        val image = InputImage.fromBitmap(bitmap, 0)
        val visionText = recognizer.process(image).await()
        val text = visionText.text

        val amountCents = BankNotificationParser.extractAmountCents(text)
        val lowerText = text.lowercase()

        val source = when {
            lowerText.contains("yape") -> SaleSource.YAPE
            lowerText.contains("plin") -> SaleSource.PLIN
            lowerText.contains("bcp") -> SaleSource.BCP
            lowerText.contains("bbva") -> SaleSource.BBVA
            lowerText.contains("interbank") -> SaleSource.INTERBANK
            lowerText.contains("scotiabank") -> SaleSource.SCOTIABANK
            lowerText.contains("mercado pago") -> SaleSource.MERCADO_PAGO
            else -> SaleSource.MANUAL_OCR
        }

        if (amountCents > 0L) {
            OcrScanResult(
                amountCents = amountCents,
                source = source,
                isSuccess = true,
                detectedTextSnippet = "Comprobante verificado ${source.name}"
            )
        } else {
            OcrScanResult(
                amountCents = 0L,
                source = source,
                isSuccess = false
            )
        }
    }.getOrElse { err ->
        OcrScanResult(
            amountCents = 0L,
            source = SaleSource.MANUAL_DECLARED,
            isSuccess = false,
            detectedTextSnippet = err.message
        )
    }
}
