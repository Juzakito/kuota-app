package com.kouta.app.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.ui.graphics.vector.ImageVector

sealed class NavDestination(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    object Home : NavDestination("home", "Cuenta", Icons.Default.AccountBalance)
    object Cards : NavDestination("cards", "Tarjetas", Icons.Default.CreditCard)
    object Sales : NavDestination("sales", "Actividad", Icons.AutoMirrored.Filled.ReceiptLong)
    object Financing : NavDestination("financing", "Compras", Icons.Default.ShoppingBag)
    object Profile : NavDestination("profile", "Menú", Icons.Default.Menu)
    object Onboarding : NavDestination("onboarding", "Inicio", Icons.Default.Home)
    object Support : NavDestination("support?form={form}", "Ayuda", Icons.Default.HelpOutline)
    object Notifications : NavDestination("notifications", "Notificaciones", Icons.Default.NotificationsNone)
    object Legal : NavDestination("legal", "Marco legal", Icons.Default.Gavel)
}

fun supportRoute(openForm: Boolean) = "support?form=$openForm"
