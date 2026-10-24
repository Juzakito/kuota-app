package com.kouta.app.feature.supplier

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.kouta.app.core.designsystem.theme.kuotaColors
import com.kouta.app.data.local.CreditPurchaseStore
import com.kouta.app.data.local.PaymentLinkStore
import com.kouta.app.domain.model.CreditPurchase
import com.kouta.app.domain.model.PaymentLink
import com.kouta.app.domain.model.PaymentLinkStatus
import com.kouta.app.domain.model.PurchaseOffer
import com.kouta.app.domain.model.PurchasePhase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private sealed class UnifiedSupplierSale(
    val id: String,
    val epochMillis: Long,
    val grossAmountCents: Long,
    val netAmountCents: Long,
    val channel: String
) {
    class QrBusiness(val purchase: CreditPurchase) : UnifiedSupplierSale(
        id = purchase.id,
        epochMillis = purchase.createdAtEpochMillis,
        grossAmountCents = purchase.totalAmountCents,
        netAmountCents = purchase.supplierPayout48hCents,
        channel = "QR_BUSINESS"
    )

    class PaymentLinkItem(val link: PaymentLink) : UnifiedSupplierSale(
        id = link.id,
        epochMillis = link.paidAtEpochMillis ?: link.createdAtEpochMillis,
        grossAmountCents = link.amountCents,
        netAmountCents = link.netAmountCents,
        channel = "LINK_PAGO"
    )
}

@Composable
fun SupplierSalesHistoryScreen() {
    val context = LocalContext.current
    val creditStore = remember(context) { CreditPurchaseStore(context) }
    val linkStore = remember(context) { PaymentLinkStore(context) }
    val kuotaColors = MaterialTheme.kuotaColors
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy · HH:mm", Locale.US) }

    var allSales by remember { mutableStateOf<List<UnifiedSupplierSale>>(emptyList()) }
    var selectedFilter by remember { mutableStateOf("Todas") }

    LaunchedEffect(Unit) {
        val qrList = withContext(Dispatchers.IO) {
            creditStore.getAll().filter {
                it.phase in setOf(
                    PurchasePhase.PHASE_2_INSTALLMENTS_SELECTION,
                    PurchasePhase.ACTIVE_IN_REPAYMENT,
                    PurchasePhase.COMPLETED
                )
            }.map { UnifiedSupplierSale.QrBusiness(it) }
        }
        val linksList = withContext(Dispatchers.IO) {
            linkStore.getAll().map { UnifiedSupplierSale.PaymentLinkItem(it) }
        }
        allSales = (qrList + linksList).sortedByDescending { it.epochMillis }
    }

    val filtered = remember(allSales, selectedFilter) {
        when (selectedFilter) {
            "QR Business (48h)" -> allSales.filter { it is UnifiedSupplierSale.QrBusiness }
            "Links de Pago (1%)" -> allSales.filter { it is UnifiedSupplierSale.PaymentLinkItem }
            "Por liquidar (48h)" -> allSales.filter {
                it is UnifiedSupplierSale.QrBusiness && it.purchase.phase != PurchasePhase.COMPLETED
            }
            "Liquidadas / Pagadas" -> allSales.filter {
                (it is UnifiedSupplierSale.QrBusiness && it.purchase.phase == PurchasePhase.COMPLETED) ||
                (it is UnifiedSupplierSale.PaymentLinkItem && it.link.status == PaymentLinkStatus.PAGADO)
            }
            else -> allSales
        }
    }

    val totalVolumeCents = allSales.sumOf { it.grossAmountCents }
    val pendingPayoutCents = allSales.filterIsInstance<UnifiedSupplierSale.QrBusiness>()
        .filter { it.purchase.phase != PurchasePhase.COMPLETED }
        .sumOf { it.purchase.supplierPayout48hCents }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top summary
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(20.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.AutoMirrored.Filled.ReceiptLong,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        "Historial de Ventas Kuota",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Canales QR Business (98%) y Links de Pago (1%)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Volumen Total", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(PurchaseOffer.formatPen(totalVolumeCents), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                }

                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = kuotaColors.accentTeal.copy(alpha = 0.12f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Por liquidar (48h)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            PurchaseOffer.formatPen(pendingPayoutCents),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = kuotaColors.accentTeal
                        )
                    }
                }
            }
        }

        // Filters
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("Todas", "QR Business (48h)", "Links de Pago (1%)", "Por liquidar (48h)", "Liquidadas / Pagadas").forEach { filter ->
                FilterChip(
                    selected = selectedFilter == filter,
                    onClick = { selectedFilter = filter },
                    label = { Text(filter, style = MaterialTheme.typography.labelMedium) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            }
        }

        if (filtered.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "No hay ventas en esta categoría.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filtered, key = { it.id }) { item ->
                    when (item) {
                        is UnifiedSupplierSale.QrBusiness -> {
                            val sale = item.purchase
                            val isCompleted = sale.phase == PurchasePhase.COMPLETED
                            val opCode = "OP-${sale.id.takeLast(6).uppercase()}"

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.QrCode2, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Column {
                                                Text(
                                                    if (sale.buyerName.isNotBlank()) sale.buyerName else "Comprador Kuota",
                                                    style = MaterialTheme.typography.titleMedium,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Text(opCode, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            }
                                        }

                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(
                                                    if (isCompleted) kuotaColors.accentTeal.copy(alpha = 0.2f)
                                                    else MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                                )
                                                .padding(horizontal = 8.dp, vertical = 3.dp)
                                        ) {
                                            Text(
                                                if (isCompleted) "Liquidada" else "En liquidación 48h",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isCompleted) kuotaColors.accentTeal else MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }

                                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("Total venta:", style = MaterialTheme.typography.bodySmall)
                                        Text(PurchaseOffer.formatPen(sale.totalAmountCents), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                    }
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("Inicial cobrada en mano:", style = MaterialTheme.typography.bodySmall)
                                        Text(PurchaseOffer.formatPen(sale.downPaymentCents), style = MaterialTheme.typography.bodySmall)
                                    }
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("Comisión Kuota (2%):", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                                        Text("- ${PurchaseOffer.formatPen(sale.kuotaFeeCents)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                                    }
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("Abono Kuota a 48h:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                                        Text(PurchaseOffer.formatPen(sale.supplierPayout48hCents), fontWeight = FontWeight.Bold, color = kuotaColors.accentTeal)
                                    }

                                    Text(
                                        dateFormat.format(Date(sale.createdAtEpochMillis)),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                        is UnifiedSupplierSale.PaymentLinkItem -> {
                            val link = item.link

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.Link, contentDescription = null, tint = kuotaColors.accentTeal, modifier = Modifier.size(20.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Column {
                                                Text(link.customerName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                                Text("Link: ${link.concept}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            }
                                        }

                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(
                                                    when (link.displayStatus) {
                                                        PaymentLinkStatus.PAGADO -> kuotaColors.accentTeal.copy(alpha = 0.2f)
                                                        PaymentLinkStatus.ENVIADO -> MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                                        PaymentLinkStatus.VENCIDO -> MaterialTheme.colorScheme.error.copy(alpha = 0.15f)
                                                        else -> MaterialTheme.colorScheme.surfaceVariant
                                                    }
                                                )
                                                .padding(horizontal = 8.dp, vertical = 3.dp)
                                        ) {
                                            Text(
                                                link.displayStatus.label,
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = when (link.displayStatus) {
                                                    PaymentLinkStatus.PAGADO -> kuotaColors.accentTeal
                                                    PaymentLinkStatus.ENVIADO -> MaterialTheme.colorScheme.primary
                                                    PaymentLinkStatus.VENCIDO -> MaterialTheme.colorScheme.error
                                                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                                                }
                                            )
                                        }
                                    }

                                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("Monto cobrado:", style = MaterialTheme.typography.bodySmall)
                                        Text(PurchaseOffer.formatPen(link.amountCents), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                    }
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("Comisión Kuota (1% plano):", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                                        Text("- ${PurchaseOffer.formatPen(link.feeCents)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                                    }
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("Neto recibido (99%):", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                                        Text(PurchaseOffer.formatPen(link.netAmountCents), fontWeight = FontWeight.Bold, color = kuotaColors.accentTeal)
                                    }

                                    Text(
                                        dateFormat.format(Date(link.createdAtEpochMillis)),
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
    }
}
