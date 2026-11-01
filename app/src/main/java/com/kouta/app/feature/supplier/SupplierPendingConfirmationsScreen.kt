package com.kouta.app.feature.supplier

import android.widget.Toast
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kouta.app.R
import com.kouta.app.di.AppEntryPoint
import com.kuota.core.model.CreditPurchase
import com.kuota.core.ui.components.EmptyState
import com.kuota.core.ui.components.KuotaCard
import com.kuota.core.ui.components.PillButton
import com.kuota.core.ui.components.PillButtonVariant
import com.kuota.core.ui.theme.KuotaColors
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import java.util.Locale

@Composable
fun SupplierPendingConfirmationsScreen(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val entryPoint = remember(context) {
        runCatching {
            EntryPointAccessors.fromApplication(
                context.applicationContext,
                AppEntryPoint::class.java
            )
        }.getOrNull()
    }

    val supplierRepo = entryPoint?.supplierRepository()
    val purchaseRepo = entryPoint?.purchaseRepository()
    val authRepo = entryPoint?.authRepository()
    val currentUid = authRepo?.currentUid.orEmpty()

    val pendingPurchases by (supplierRepo?.observePendingPurchasesForApproval(currentUid) ?: flowOf(emptyList()))
        .collectAsState(initial = emptyList())

    var approvingPurchaseId by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(KuotaColors.Fog)
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(R.string.supplier_pending_approvals_title),
            color = KuotaColors.ForestInk,
            fontSize = 22.sp,
            fontWeight = FontWeight.Black
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Revisa y confirma las cuotas iniciales entregadas por tus clientes en efectivo, Yape o POS.",
            color = KuotaColors.Charcoal,
            fontSize = 13.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (pendingPurchases.isEmpty()) {
            EmptyState(
                title = "Sin solicitudes pendientes",
                description = "Cuando un cliente escanee tu QR o solicite una compra al crédito, verás aquí el monto de inicial a confirmar.",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 40.dp)
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(pendingPurchases, key = { it.purchaseId }) { purchase ->
                    PendingPurchaseItemCard(
                        purchase = purchase,
                        isApproving = approvingPurchaseId == purchase.purchaseId,
                        onApprove = {
                            approvingPurchaseId = purchase.purchaseId
                            scope.launch {
                                purchaseRepo?.providerApprovePurchase(purchase.purchaseId)
                                    ?.onSuccess {
                                        approvingPurchaseId = null
                                        Toast.makeText(context, "¡Inicial confirmada! El cliente ya puede finalizar su compra.", Toast.LENGTH_SHORT).show()
                                    }
                                    ?.onFailure { error ->
                                        approvingPurchaseId = null
                                        Toast.makeText(context, error.message ?: "Error al confirmar", Toast.LENGTH_LONG).show()
                                    }
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun PendingPurchaseItemCard(
    purchase: CreditPurchase,
    isApproving: Boolean,
    onApprove: () -> Unit
) {
    KuotaCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
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
                            .background(KuotaColors.LimeVoltage),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = KuotaColors.ForestInk,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Cliente Kuota",
                            color = KuotaColors.ForestInk,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "ID: ${purchase.purchaseId}",
                            color = KuotaColors.Charcoal,
                            fontSize = 11.sp
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.HourglassTop,
                        contentDescription = null,
                        tint = KuotaColors.ForestInk,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Por confirmar",
                        color = KuotaColors.ForestInk,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Venta total:",
                        color = KuotaColors.Charcoal,
                        fontSize = 12.sp
                    )
                    Text(
                        text = String.format(Locale.US, "S/ %.2f", purchase.totalAmountCents / 100.0),
                        color = KuotaColors.ForestInk,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Inicial que debes recibir:",
                        color = KuotaColors.Charcoal,
                        fontSize = 12.sp
                    )
                    Text(
                        text = String.format(Locale.US, "S/ %.2f", purchase.downPaymentCents / 100.0),
                        color = KuotaColors.ForestInk,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            PillButton(
                text = stringResource(R.string.supplier_btn_approve_initial),
                onClick = onApprove,
                isLoading = isApproving,
                variant = PillButtonVariant.Primary,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
