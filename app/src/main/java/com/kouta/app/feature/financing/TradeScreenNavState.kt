package com.kouta.app.feature.financing

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue

/**
 * Estado compartido de navegación para la pantalla Kuotear (TradeScreen).
 * Permite cambiar directamente entre la pestaña de Comprar a crédito (0) y Cobrar con link (1).
 */
object TradeScreenNavState {
    var selectedTab by mutableIntStateOf(0)

    fun openBuy() {
        selectedTab = 0
    }

    fun openLink() {
        selectedTab = 1
    }
}
