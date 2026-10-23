package com.kouta.app.domain.model

import java.util.Locale

/** Formatting for user-entered PEN amounts. */
object PurchaseOffer {
    fun formatPen(cents: Long): String {
        val wholeSoles = cents / 100
        val remainingCents = cents % 100
        return String.format(Locale.US, "S/ %,d.%02d", wholeSoles, remainingCents)
    }
}
