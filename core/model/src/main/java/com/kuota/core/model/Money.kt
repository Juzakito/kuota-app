package com.kuota.core.model

import kotlinx.serialization.Serializable
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

/**
 * Representación de dinero en céntimos para Kuota App.
 *
 * Reglas duras de AGENTS.md:
 * - Dinero en céntimos (Long), moneda PEN, zona America/Lima.
 * - Cero Double/Float para montos de negocio.
 */
@Serializable
@JvmInline
value class Money(val cents: Long) : Comparable<Money> {

    init {
        // En Kuota el dinero puede ser positivo o cero (y negativo en balances contables compensatorios)
    }

    override fun compareTo(other: Money): Int = cents.compareTo(other.cents)

    operator fun plus(other: Money): Money = Money(cents + other.cents)
    operator fun minus(other: Money): Money = Money(cents - other.cents)

    fun toFormattedString(): String {
        val symbols = DecimalFormatSymbols(Locale.forLanguageTag("es-PE")).apply {
            decimalSeparator = '.'
            groupingSeparator = ','
        }
        val formatter = DecimalFormat("#,##0.00", symbols)
        val soles = cents / 100.0
        return "S/ ${formatter.format(soles)}"
    }

    companion object {
        val ZERO = Money(0L)
        fun fromSoles(soles: Long): Money = Money(soles * 100L)
    }
}
