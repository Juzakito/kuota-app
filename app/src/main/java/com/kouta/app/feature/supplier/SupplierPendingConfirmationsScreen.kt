package com.kouta.app.feature.supplier

import android.widget.Toast
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
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PendingActions
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import com.kouta.app.domain.model.CreditPurchase
import com.kouta.app.domain.model.PurchaseOffer
import com.kouta.app.domain.model.PurchasePhase
import kotlinx.coroutines.launch

@Composable
fun SupplierPendingConfirmationsScreen() {
    val context = LocalContext.current
    val creditStore = remember(context) { CreditPurchaseStore(context) }
    val scope = rememberCoroutineScope()
    val kuotaColors = MaterialTheme.kuotaColors

    var pendingList by remember { mutableStateOf<List<CreditPurchase>>(emptyList()) }
    var viewingVoucherPurchase by remember { mutableStateOf<CreditPurchase?>(null) }
    var rejectingPurchase by remember { mutableStateOf<CreditPurchase?>(null) }

    fun reload() {
        scope.launch {
            val all = creditStore.getAll()
            pendingList = all.filter {
                it.phase in setOf(
                    PurchasePhase.PHASE_1_DOWN_PAYMENT_SUBMITTED,
                    PurchasePhase.PHASE_1_DOWN_PAYMENT_PENDING,
                    PurchasePhase.PHASE_2_INSTALLMENTS_SELECTION
                )
            }
        }
    }

    LaunchedEffect(Unit) {
        reload()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 20.dp, vertical = 18.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.PendingActions,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        "Pagos por Confirmar",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Confirma las cuotas iniciales recibidas para liberar la venta",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        if (pendingList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = kuotaColors.accentTeal,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        "¡Todo al día!",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        "No tienes cuotas iniciales pendientes de confirmar.\nCuando un comprador suba su voucher aparecerá aquí.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(pendingList, key = { it.id }) { purchase ->
                    PendingItemCard(
                        purchase = purchase,
                        onViewVoucher = { viewingVoucherPurchase = purchase },
                        onConfirmPayment = {
                            scope.launch {
                                creditStore.supplierConfirmDownPayment(purchase.id)
                                Toast.makeText(
                                    context,
                                    "¡Pago confirmado! La venta pasará a liquidación en 48h.",
                                    Toast.LENGTH_LONG
                                ).show()
                                reload()
                            }
                        },
                        onRejectPayment = { rejectingPurchase = purchase }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }

    if (viewingVoucherPurchase != null) {
        val p = viewingVoucherPurchase!!
        AlertDialog(
            onDismissRequest = { viewingVoucherPurchase = null },
            title = {
                Text("Comprobante de Pago Inicial")
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        "Comprador: ${if (p.buyerName.isNotBlank()) p.buyerName else "Comercio"}",
                        fontWeight = FontWeight.Bold
                    )
                    Text("Monto inicial a verificar: ${PurchaseOffer.formatPen(p.downPaymentCents)}")
                    Spacer(modifier = Modifier.height(6.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.Receipt,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Voucher adjunto por el comprador\n(Transferencia Yape / Plin / BCP)",
                                style = MaterialTheme.typography.bodySmall,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val toConfirm = viewingVoucherPurchase!!
                        viewingVoucherPurchase = null
                        scope.launch {
                            creditStore.supplierConfirmDownPayment(toConfirm.id)
                            Toast.makeText(context, "Pago confirmado con éxito", Toast.LENGTH_SHORT).show()
                            reload()
                        }
                    }
                ) {
                    Text("Confirmar que recibí el dinero")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewingVoucherPurchase = null }) {
                    Text("Cerrar")
                }
            }
        )
    }

    if (rejectingPurchase != null) {
        val toReject = rejectingPurchase!!
        AlertDialog(
            onDismissRequest = { rejectingPurchase = null },
            title = { Text("¿Rechazar cuota inicial?") },
            text = {
                Text(
                    "Operación OP-${toReject.id.takeLast(6).uppercase()}.\n\n" +
                    "Si no recibiste la transferencia o el comprobante adjunto no es válido, se cancelará esta operación y el comprador será notificado para que intente nuevamente."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val id = toReject.id
                        rejectingPurchase = null
                        scope.launch {
                            creditStore.supplierRejectDownPayment(id)
                            Toast.makeText(context, "Operación cancelada. Se notificó al comprador.", Toast.LENGTH_SHORT).show()
                            reload()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Sí, rechazar pago")
                }
            },
            dismissButton = {
                TextButton(onClick = { rejectingPurchase = null }) {
                    Text("Volver")
                }
            }
        )
    }
}

@Composable
private fun PendingItemCard(
    purchase: CreditPurchase,
    onViewVoucher: () -> Unit,
    onConfirmPayment: () -> Unit,
    onRejectPayment: () -> Unit
) {
    val kuotaColors = MaterialTheme.kuotaColors
    val isWaitingForBuyer = purchase.phase == PurchasePhase.PHASE_2_INSTALLMENTS_SELECTION
    val hasVoucher = purchase.phase == PurchasePhase.PHASE_1_DOWN_PAYMENT_SUBMITTED
    val opCode = "OP-${purchase.id.takeLast(6).uppercase()}"

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (purchase.buyerName.isNotBlank()) purchase.buyerName else "Comprador Kuota",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = opCode,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        if (purchase.buyerPhone.isNotBlank()) {
                            Text(
                                text = "Cel: ${purchase.buyerPhone}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            when {
                                isWaitingForBuyer -> MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                hasVoucher -> kuotaColors.accentTeal.copy(alpha = 0.2f)
                                else -> MaterialTheme.colorScheme.surfaceVariant
                            }
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = when {
                            isWaitingForBuyer -> "Esperando al comprador"
                            hasVoucher -> "Voucher Subido"
                            else -> "Esperando voucher"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = when {
                            isWaitingForBuyer -> MaterialTheme.colorScheme.primary
                            hasVoucher -> kuotaColors.accentTeal
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Total Venta", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(PurchaseOffer.formatPen(purchase.totalAmountCents), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                }
                Column {
                    Text("Cuota Inicial", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                    Text(
                        PurchaseOffer.formatPen(purchase.downPaymentCents),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Column {
                    Text("Saldo Kuota 48h", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        PurchaseOffer.formatPen(purchase.supplierPayout48hCents),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = kuotaColors.accentTeal
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (isWaitingForBuyer) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.HourglassTop,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            "Inicial confirmada. El comprador está eligiendo sus cuotas. Cuando acepte, la venta pasará a Por cobrar (48h).",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (hasVoucher) {
                        OutlinedButton(
                            onClick = onViewVoucher,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Voucher", style = MaterialTheme.typography.labelSmall)
                        }
                    }

                    OutlinedButton(
                        onClick = onRejectPayment,
                        modifier = Modifier.weight(1.1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) {
                        Icon(Icons.Default.Cancel, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("No recibí pago", style = MaterialTheme.typography.labelSmall)
                    }

                    Button(
                        onClick = onConfirmPayment,
                        modifier = Modifier.weight(1.3f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = kuotaColors.accentTeal,
                            contentColor = Color.Black
                        )
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Confirmar pago", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
