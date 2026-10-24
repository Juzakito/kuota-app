package com.kouta.app.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.PendingActions
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.ui.graphics.vector.ImageVector

sealed class NavDestination(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    // Vendedor flow
    object Home : NavDestination("home", "Inicio", Icons.Default.Home)
    object Marketplace : NavDestination("marketplace", "Marketplace", Icons.Default.Storefront)
    object Trade : NavDestination("trade", "Comprar y vender", Icons.Default.QrCodeScanner)
    object Finance : NavDestination("finance", "Finanzas", Icons.AutoMirrored.Filled.ReceiptLong)
    object Installments : NavDestination("installments", "Mis cuotas", Icons.Default.CalendarMonth)
    object MyProducts : NavDestination("my_products", "Mis productos", Icons.Default.Inventory2)

    // Proveedor flow
    object SupplierHome : NavDestination("supplier_home", "Inicio", Icons.Default.Home)
    object SupplierPending : NavDestination("supplier_pending", "Por confirmar", Icons.Default.PendingActions)
    object SupplierCharge : NavDestination("supplier_charge", "Cobrar", Icons.Default.QrCodeScanner)
    object SupplierSales : NavDestination("supplier_sales", "Ventas", Icons.AutoMirrored.Filled.ReceiptLong)

    // Common / Modal
    object Profile : NavDestination("profile", "Menú", Icons.Default.Person)
    object Onboarding : NavDestination("onboarding", "Inicio", Icons.Default.Home)
    object Support : NavDestination("support?form={form}", "Ayuda", Icons.Default.HelpOutline)
    object Notifications : NavDestination("notifications", "Notificaciones", Icons.Default.NotificationsNone)
    object Legal : NavDestination("legal", "Marco legal", Icons.Default.Gavel)

    companion object {
        // Backward-compatibility aliases
        val Cards: NavDestination get() = Installments
        val Financing: NavDestination get() = Trade
        val Sales: NavDestination get() = Finance
    }
}

fun supportRoute(openForm: Boolean) = "support?form=$openForm"
