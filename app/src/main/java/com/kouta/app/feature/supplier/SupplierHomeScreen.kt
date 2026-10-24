package com.kouta.app.feature.supplier

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.PendingActions
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kouta.app.core.designsystem.theme.kuotaColors
import com.kouta.app.data.local.CreditPurchaseStore
import com.kouta.app.data.local.PaymentLinkStore
import com.kouta.app.data.local.SalesJournalStore
import com.kouta.app.domain.model.CreditPurchase
import com.kouta.app.domain.model.PaymentLink
import com.kouta.app.domain.model.PaymentLinkStatus
import com.kouta.app.domain.model.PurchaseOffer
import com.kouta.app.domain.model.PurchasePhase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun SupplierHomeScreen(
    onOpenPending: () -> Unit,
    onOpenCharge: () -> Unit,
    onOpenSales: () -> Unit,
    onOpenProfile: () -> Unit,
    onOpenNotifications: () -> Unit
) {
    val context = LocalContext.current
    val journalStore = remember(context) { SalesJournalStore(context) }
    val creditStore = remember(context) { CreditPurchaseStore(context) }
    val linkStore = remember(context) { PaymentLinkStore(context) }
    val kuotaColors = MaterialTheme.kuotaColors

    var businessName by remember { mutableStateOf("") }
    var ownerName by remember { mutableStateOf("") }
    var commercialCategory by remember { mutableStateOf("") }
    var commercialZone by remember { mutableStateOf("") }
    var pendingPurchasesCount by remember { mutableIntStateOf(0) }
    var totalWeekSalesCents by remember { mutableStateOf(0L) }
    var qrWeekSalesCents by remember { mutableStateOf(0L) }
    var linkWeekSalesCents by remember { mutableStateOf(0L) }
    var pendingPayout48hCents by remember { mutableStateOf(0L) }
    var pendingPayoutPurchases by remember { mutableStateOf<List<CreditPurchase>>(emptyList()) }
    var recentTransactions by remember { mutableStateOf<List<CreditPurchase>>(emptyList()) }

    LaunchedEffect(Unit) {
        val profile = withContext(Dispatchers.IO) { journalStore.readBusinessProfile() }
        businessName = profile?.businessName.orEmpty().ifBlank { "Comercio Aliado" }
        ownerName = profile?.ownerName.orEmpty()
        commercialCategory = profile?.commercialCategory.orEmpty().ifBlank { "Textil y Confección" }
        commercialZone = profile?.commercialZone.orEmpty().ifBlank { "Gamarra, La Victoria" }

        val allPurchases = withContext(Dispatchers.IO) { creditStore.getAll() }
        val allLinks = withContext(Dispatchers.IO) { linkStore.getAll() }

        pendingPurchasesCount = allPurchases.count {
            it.phase == PurchasePhase.PHASE_1_DOWN_PAYMENT_SUBMITTED || it.phase == PurchasePhase.PHASE_1_DOWN_PAYMENT_PENDING
        }

        val confirmedPurchases = allPurchases.filter {
            it.phase in setOf(PurchasePhase.PHASE_2_INSTALLMENTS_SELECTION, PurchasePhase.ACTIVE_IN_REPAYMENT, PurchasePhase.COMPLETED)
        }
        val paidLinks = allLinks.filter { it.status == PaymentLinkStatus.PAGADO }

        val now = System.currentTimeMillis()
        val sevenDaysAgo = now - (7L * 24 * 3600_000L)

        val weekQr = confirmedPurchases.filter { it.createdAtEpochMillis >= sevenDaysAgo }
        val weekLinks = paidLinks.filter { (it.paidAtEpochMillis ?: it.createdAtEpochMillis) >= sevenDaysAgo }

        qrWeekSalesCents = weekQr.sumOf { it.totalAmountCents }
        linkWeekSalesCents = weekLinks.sumOf { it.amountCents }
        totalWeekSalesCents = qrWeekSalesCents + linkWeekSalesCents

        val pendingPayouts = confirmedPurchases.filter { it.phase != PurchasePhase.COMPLETED }
        pendingPayoutPurchases = pendingPayouts
        pendingPayout48hCents = pendingPayouts.sumOf { it.supplierPayout48hCents }
        recentTransactions = allPurchases.take(5)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top Bar
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.verticalGradient(kuotaColors.headerGradient))
                    .padding(horizontal = 20.dp, vertical = 20.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { onOpenProfile() }
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                                    .border(1.5.dp, MaterialTheme.colorScheme.primary, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Storefront,
                                    contentDescription = "Perfil",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        businessName,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = kuotaColors.headerContent
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(kuotaColors.accentTeal.copy(alpha = 0.2f))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            "PROVEEDOR",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = kuotaColors.accentTeal
                                        )
                                    }
                                }
                                Text(
                                    "$commercialCategory · $commercialZone",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = kuotaColors.headerContent.copy(alpha = 0.7f)
                                )
                            }
                        }

                        IconButton(onClick = onOpenNotifications) {
                            Icon(
                                Icons.Default.NotificationsNone,
                                contentDescription = "Notificaciones",
                                tint = kuotaColors.headerContent
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Banner de alertas para pagos por confirmar (PRD 13.5)
                    if (pendingPurchasesCount > 0) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onOpenPending() },
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = kuotaColors.accentTeal.copy(alpha = 0.18f))
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .background(kuotaColors.accentTeal),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            "$pendingPurchasesCount",
                                            fontWeight = FontWeight.Bold,
                                            color = Color.Black,
                                            style = MaterialTheme.typography.labelMedium
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text("Cuotas iniciales por confirmar", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                        Text("Toca para verificar los vouchers recibidos", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                                Icon(Icons.Default.ArrowForward, contentDescription = null, tint = kuotaColors.accentTeal)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    // Hub Financiero: Ventas de la semana y Por cobrar en 48h (PRD 13.7)
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                    ) {
                        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        "Por liquidar en 48 horas (PRD §13.7)",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        PurchaseOffer.formatPen(pendingPayout48hCents),
                                        style = MaterialTheme.typography.headlineMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(kuotaColors.accentTeal.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.HourglassTop,
                                        contentDescription = null,
                                        tint = kuotaColors.accentTeal,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }

                            HorizontalDivider()

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("Ventas de la semana", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(PurchaseOffer.formatPen(totalWeekSalesCents), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("Desglose por canal", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(
                                        "QR: ${PurchaseOffer.formatPen(qrWeekSalesCents)} · Links: ${PurchaseOffer.formatPen(linkWeekSalesCents)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }

                            Text(
                                "Kuota te transfiere el saldo restante (98% de la venta menos la inicial) directamente a tu cuenta bancaria en 48 horas hábiles.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // Quick Actions Row
        item {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Operaciones Rápidas",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Por confirmar Card
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onOpenPending() },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (pendingPurchasesCount > 0) kuotaColors.accentTeal.copy(alpha = 0.15f)
                            else MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.PendingActions,
                                    contentDescription = null,
                                    tint = if (pendingPurchasesCount > 0) kuotaColors.accentTeal else MaterialTheme.colorScheme.primary
                                )
                                if (pendingPurchasesCount > 0) {
                                    Box(
                                        modifier = Modifier
                                            .clip(CircleShape)
                                            .background(kuotaColors.accentTeal)
                                            .padding(horizontal = 7.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            "$pendingPurchasesCount",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.Black
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Text("Por confirmar", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                            Text("Cuotas iniciales", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    // Cobrar con QR Card
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onOpenCharge() },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Icon(Icons.Default.QrCodeScanner, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.height(10.dp))
                            Text("Cobrar con QR", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                            Text("Código tienda", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    // Ventas Card
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onOpenSales() },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Icon(Icons.Default.PointOfSale, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.height(10.dp))
                            Text("Mis Ventas", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                            Text("Historial 48h", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }

        // Recent Movements
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Actividad Reciente", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(
                        "Ver todas",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clickable { onOpenSales() }
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
            }
        }

        if (recentTransactions.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Aún no registras ventas a través de Kuota.\nMuestra tu QR a los vendedores para empezar.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        } else {
            items(recentTransactions, key = { it.id }) { tx ->
                val opCode = "OP-${tx.id.takeLast(6).uppercase()}"
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    if (tx.buyerName.isNotBlank()) tx.buyerName else "Comprador Kuota",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(opCode, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Text(
                                "Venta: ${PurchaseOffer.formatPen(tx.totalAmountCents)} · Inicial recibida: ${PurchaseOffer.formatPen(tx.downPaymentCents)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                "+ ${PurchaseOffer.formatPen(tx.supplierPayout48hCents)}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = kuotaColors.accentTeal
                            )
                            Text(
                                "48h neto",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}
