package com.kouta.app.feature.supplier

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.PendingActions
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import com.kuota.core.ui.components.ChipPill
import com.kuota.core.ui.components.KuotaCard
import com.kuota.core.ui.components.PillButton
import com.kuota.core.ui.components.PillButtonVariant
import com.kuota.core.ui.theme.KuotaColors
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.flow.flowOf
import java.util.Locale

@Composable
fun SupplierHomeScreen(
    onOpenPending: () -> Unit,
    onOpenCharge: () -> Unit,
    onOpenSales: () -> Unit,
    onOpenProfile: () -> Unit,
    onOpenNotifications: () -> Unit,
    onOpenQr: () -> Unit = {}
) {
    val context = LocalContext.current
    val entryPoint = remember(context) {
        runCatching {
            EntryPointAccessors.fromApplication(
                context.applicationContext,
                AppEntryPoint::class.java
            )
        }.getOrNull()
    }

    val userRepo = entryPoint?.userRepository()
    val supplierRepo = entryPoint?.supplierRepository()
    val authRepo = entryPoint?.authRepository()
    val currentUid = authRepo?.currentUid.orEmpty()

    val userSummary by (userRepo?.observeUserSummary() ?: flowOf(null)).collectAsState(initial = null)
    val pendingPurchases by (supplierRepo?.observePendingPurchasesForApproval(currentUid) ?: flowOf(emptyList()))
        .collectAsState(initial = emptyList())
    val payouts by (supplierRepo?.observeSupplierPayouts(currentUid) ?: flowOf(emptyList()))
        .collectAsState(initial = emptyList())

    val pendingAdvanceCents = payouts.filter { it.isAdvance && !it.isSettled }.sumOf { it.amountCents }
    val totalSettledCents = payouts.filter { it.isSettled }.sumOf { it.amountCents }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(KuotaColors.Fog)
            .padding(horizontal = 20.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Encabezado
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = userSummary?.businessName ?: "Mi Negocio MYPE",
                    color = KuotaColors.ForestInk,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Panel de Proveedor Kuota",
                    color = KuotaColors.Charcoal,
                    fontSize = 12.sp
                )
            }

            Row {
                IconButton(onClick = onOpenNotifications) {
                    Icon(
                        imageVector = Icons.Default.NotificationsNone,
                        contentDescription = "Avisos",
                        tint = KuotaColors.ForestInk
                    )
                }
                IconButton(onClick = onOpenProfile) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Cuenta",
                        tint = KuotaColors.ForestInk
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Hero Card: Cobros esperados (Adelanto 60% en 48h)
        KuotaCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.supplier_advance_label),
                        color = KuotaColors.Charcoal,
                        fontSize = 13.sp
                    )
                    ChipPill(
                        label = "Liquidación 48h",
                        isSelected = true,
                        onClick = {}
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = String.format(Locale.US, "S/ %.2f", pendingAdvanceCents / 100.0),
                    color = KuotaColors.ForestInk,
                    fontSize = 34.sp,
                    fontWeight = FontWeight.Black
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Liquidado histórico:",
                            color = KuotaColors.Charcoal,
                            fontSize = 12.sp
                        )
                        Text(
                            text = String.format(Locale.US, "S/ %.2f", totalSettledCents / 100.0),
                            color = KuotaColors.ForestInk,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Cuenta destino:",
                            color = KuotaColors.Charcoal,
                            fontSize = 12.sp
                        )
                        Text(
                            text = "BCP Soles ****4812",
                            color = KuotaColors.ForestInk,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Botones de acción rápida
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(modifier = Modifier.weight(1f)) {
                PillButton(
                    text = "Mi QR",
                    onClick = onOpenQr,
                    variant = PillButtonVariant.Primary,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            Box(modifier = Modifier.weight(1f)) {
                PillButton(
                    text = "Cobrar",
                    onClick = onOpenCharge,
                    variant = PillButtonVariant.Secondary,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Sección: Por confirmar inicial
        if (pendingPurchases.isNotEmpty()) {
            KuotaCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
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
                                imageVector = Icons.Default.PendingActions,
                                contentDescription = null,
                                tint = KuotaColors.ForestInk,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "${pendingPurchases.size} compra(s) por confirmar",
                                color = KuotaColors.ForestInk,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Confirma la inicial del cliente",
                                color = KuotaColors.Charcoal,
                                fontSize = 12.sp
                            )
                        }
                    }

                    PillButton(
                        text = "Ver",
                        onClick = onOpenPending,
                        variant = PillButtonVariant.Primary,
                        modifier = Modifier.height(36.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }

        // Historial de liquidaciones esperadas
        Text(
            text = "Próximas transferencias programadas",
            color = KuotaColors.ForestInk,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (payouts.isEmpty()) {
            KuotaCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "No tienes liquidaciones pendientes por el momento.",
                    color = KuotaColors.Charcoal,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(16.dp)
                )
            }
        } else {
            payouts.take(5).forEach { payout ->
                KuotaCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = if (payout.isAdvance) "Adelanto 60% de venta" else "Cuota semanal diferida",
                                color = KuotaColors.ForestInk,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Fecha estimada: ${payout.dueDate.take(10)}",
                                color = KuotaColors.Charcoal,
                                fontSize = 12.sp
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = String.format(Locale.US, "S/ %.2f", payout.amountCents / 100.0),
                                color = KuotaColors.ForestInk,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (payout.isSettled) "Depositado" else "En proceso",
                                color = if (payout.isSettled) KuotaColors.ForestInk else KuotaColors.Charcoal,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}
