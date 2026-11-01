package com.kuota.core.data.parser

import com.kuota.core.model.SaleRecord
import com.kuota.core.model.SaleSource
import java.security.MessageDigest

/**
 * Resultado del análisis de una notificación bancaria.
 */
data class ParsedNotificationResult(
    val amountCents: Long,
    val source: SaleSource,
    val isVerified: Boolean,
    val txHash: String,
    val customerSnippet: String?,
    val timestampMs: Long
)

/**
 * Parser en dispositivo para notificaciones bancarias peruanas según PRD §8.3 y AGENTS.md.
 * REGLAS:
 * 1. Lista blanca de paquetes autorizados.
 * 2. Cero Double/Float para montos de dinero (aritmética entera Long pura en céntimos).
 * 3. Descarte inmediato del texto crudo (cero PII).
 * 4. Generación determinista de txHash idempotente para deduplicación.
 */
object BankNotificationParser {

    private val YAPE_PACKAGES = setOf(
        "com.bcp.innovacxion.yapeapp",
        "com.bcp.yape"
    )

    private val PLIN_PACKAGES = setOf(
        "pe.plin",
        "pe.com.interbank.mobilebanking",
        "com.bbva.nxt_peru",
        "com.scotiabank.peru.mobile"
    )

    private val BCP_PACKAGES = setOf(
        "com.bcp.bank.bcp"
    )

    private val MERCADO_PAGO_PACKAGES = setOf(
        "com.mercadopago.wallet"
    )

    fun isWhitelistedPackage(packageName: String): Boolean {
        val pkg = packageName.lowercase()
        return YAPE_PACKAGES.any { pkg.contains(it) } ||
                PLIN_PACKAGES.any { pkg.contains(it) } ||
                BCP_PACKAGES.any { pkg.contains(it) } ||
                MERCADO_PAGO_PACKAGES.any { pkg.contains(it) }
    }

    fun resolveSourceFromPackage(packageName: String, text: String): SaleSource {
        val pkg = packageName.lowercase()
        val lowerText = text.lowercase()

        return when {
            YAPE_PACKAGES.any { pkg.contains(it) } || lowerText.contains("yape") -> SaleSource.YAPE
            lowerText.contains("plin") -> SaleSource.PLIN
            PLIN_PACKAGES.any { pkg.contains(it) } -> SaleSource.PLIN
            BCP_PACKAGES.any { pkg.contains(it) } -> SaleSource.BCP
            MERCADO_PAGO_PACKAGES.any { pkg.contains(it) } -> SaleSource.MERCADO_PAGO
            else -> SaleSource.MANUAL_DECLARED
        }
    }

    /**
     * Convierte cadena monetaria "150.50" o "20" a céntimos (Long) sin utilizar Double ni Float.
     */
    fun parseAmountToCents(amountRaw: String): Long {
        val cleaned = amountRaw.replace(",", ".").trim()
        val parts = cleaned.split(".")
        val soles = parts[0].filter { it.isDigit() }.toLongOrNull() ?: return 0L

        val cents = when {
            parts.size < 2 -> 0L
            parts[1].length == 1 -> (parts[1] + "0").toLongOrNull() ?: 0L
            parts[1].length >= 2 -> parts[1].substring(0, 2).toLongOrNull() ?: 0L
            else -> 0L
        }

        return (soles * 100L) + cents
    }

    /**
     * Enmascara el nombre del emisor para prevenir persistencia de PII sensible.
     * Ej: "Juan Perez" -> "J*** P***"
     */
    fun maskCustomerName(rawName: String?): String? {
        if (rawName.isNullOrBlank()) return null
        val tokens = rawName.trim().split("\\s+".toRegex())
        val masked = tokens.joinToString(" ") { token ->
            if (token.length <= 1) token
            else token.first() + "***"
        }
        return masked.take(40)
    }

    /**
     * Extrae el monto de patrones comunes en notificaciones de bancos peruanos:
     * - "te envió S/ 50.00"
     * - "recibiste S/ 120"
     * - "Pago recibido por S/. 45.50"
     * - "Transfirió S/ 300.00"
     */
    fun extractAmountCents(content: String): Long {
        val patterns = listOf(
            Regex("""(?:S/\.?|soles|PEN)\s*([0-9]+(?:[.,][0-9]{1,2})?)""", RegexOption.IGNORE_CASE),
            Regex("""([0-9]+(?:[.,][0-9]{1,2})?)\s*(?:soles|S/\.?)""", RegexOption.IGNORE_CASE)
        )

        for (pattern in patterns) {
            val match = pattern.find(content)
            if (match != null) {
                val amountStr = match.groupValues[1]
                val cents = parseAmountToCents(amountStr)
                if (cents > 0L) return cents
            }
        }
        return 0L
    }

    /**
     * Genera un identificador único SHA-256 idempotente.
     * Agrupa en ventana de 1 minuto para deduplicar retransmisiones inmediatas de notificaciones.
     */
    fun generateTxHash(
        packageName: String,
        amountCents: Long,
        timestampMs: Long,
        senderSnippet: String?
    ): String {
        val timeBucket = timestampMs / 60_000L // 1 minuto
        val rawKey = "$packageName|$amountCents|$timeBucket|${senderSnippet.orEmpty()}"
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(rawKey.toByteArray(Charsets.UTF_8))
        return hashBytes.joinToString("") { "%02x".format(it) }
    }

    /**
     * Analiza el título y texto de una notificación.
     * Desecha el contenido crudo inmediatamente y devuelve únicamente el objeto tipado seguro.
     */
    fun parseNotification(
        packageName: String,
        title: String,
        text: String,
        timestampMs: Long = System.currentTimeMillis()
    ): ParsedNotificationResult? {
        if (!isWhitelistedPackage(packageName)) {
            return null
        }

        val combined = "$title $text"
        val amountCents = extractAmountCents(combined)
        if (amountCents <= 0L) {
            return null
        }

        val source = resolveSourceFromPackage(packageName, combined)

        // Intento de extraer snippet de nombre del título o texto: ej "Juan Pérez te envió..."
        val nameMatch = Regex("""^([A-Za-zÁÉÍÓÚáéíóúñÑ ]+?)\s+(?:te envió|te transfirió|te pagó)""", RegexOption.IGNORE_CASE)
            .find(combined)
        val customerSnippet = maskCustomerName(nameMatch?.groupValues?.get(1) ?: title.take(20))

        val txHash = generateTxHash(packageName, amountCents, timestampMs, customerSnippet)

        return ParsedNotificationResult(
            amountCents = amountCents,
            source = source,
            isVerified = true,
            txHash = txHash,
            customerSnippet = customerSnippet,
            timestampMs = timestampMs
        )
    }

    fun toSaleRecord(parsed: ParsedNotificationResult): SaleRecord {
        return SaleRecord(
            id = parsed.txHash,
            amountCents = parsed.amountCents,
            timestampMs = parsed.timestampMs,
            source = parsed.source,
            isVerified = parsed.isVerified,
            txHash = parsed.txHash,
            customerSnippet = parsed.customerSnippet,
            productName = "Cobro verificado ${parsed.source.name}"
        )
    }
}
