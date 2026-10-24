package com.kouta.app.feature.installments

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import com.kouta.app.data.local.AppSettings
import com.kouta.app.data.local.CreditPurchaseStore
import com.kouta.app.data.local.SalesJournalStore
import com.kouta.app.domain.model.CreditInstallment
import com.kouta.app.domain.model.CreditLevel
import com.kouta.app.domain.model.CreditPurchase
import com.kouta.app.domain.model.PurchaseOffer
import com.kouta.app.domain.model.PurchasePhase
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun MyInstallmentsScreen(
    onOpenTrade: () -> Unit
) {
    val context = LocalContext.current
    val creditStore = remember(context) { CreditPurchaseStore(context) }
    val journalStore = remember(context) { SalesJournalStore(context) }
    val appSettings = remember(context) { AppSettings(context) }
    val scope = rememberCoroutineScope()
    val kuotaColors = MaterialTheme.kuotaColors

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var activePurchase by remember { mutableStateOf<CreditPurchase?>(null) }
    var purchaseHistory by remember { mutableStateOf<List<CreditPurchase>>(emptyList()) }
    var userLevelNumber by remember { mutableIntStateOf(1) }
    var totalVerifiedSalesCents by remember { mutableStateOf(0L) }
    var payingInstallment by remember { mutableStateOf<CreditInstallment?>(null) }

    fun reload() {
        scope.launch {
            val profile = journalStore.readBusinessProfile()
            userLevelNumber = profile?.creditLevel ?: appSettings.creditLevelNumber
            val all = creditStore.getAll()
            activePurchase = all.firstOrNull { it.isOpen }
            purchaseHistory = all.filter { !it.isOpen }

            val sales = journalStore.readAll()
            totalVerifiedSalesCents = sales.filter { it.isVerified }.sumOf { it.amountCents }
        }
    }

    LaunchedEffect(Unit) {
        reload()
    }

    val currentLevel = CreditLevel.fromNumber(userLevelNumber)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.verticalGradient(kuotaColors.headerGradient))
                .padding(horizontal = 20.dp, vertical = 20.dp)
        ) {
            Column {
                Text(
                    text = "Mis cuotas & Niveles",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = kuotaColors.headerContent
                )
                Text(
                    text = "Sistema de crédito escalonado y gestión de pagos",
                    style = MaterialTheme.typography.bodySmall,
                    color = kuotaColors.headerContent.copy(alpha = 0.75f)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Current Level Summary Card
                CurrentLevelSummaryCard(
                    level = currentLevel,
                    verifiedSalesCents = totalVerifiedSalesCents
                )
            }
        }

        // Tabs: 0 = "Cuotas Activas", 1 = "Los 6 Niveles"
        TabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary
        ) {
            Tab(
                selected = selectedTabIndex == 0,
                onClick = { selectedTabIndex = 0 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Cuotas Activas")
                        if (activePurchase?.nextPendingInstallment != null) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(kuotaColors.accentTeal)
                            )
                        }
                    }
                }
            )
            Tab(
                selected = selectedTabIndex == 1,
                onClick = { selectedTabIndex = 1 },
                text = { Text("Niveles de Crédito") }
            )
        }

        if (selectedTabIndex == 0) {
            // Cuotas Activas Tab
            ActiveInstallmentsTabContent(
                activePurchase = activePurchase,
                history = purchaseHistory,
                onOpenTrade = onOpenTrade,
                onPayInstallment = { inst ->
                    scope.launch {
                        if (activePurchase != null) {
                            creditStore.payInstallment(activePurchase!!.id, inst.number)
                            Toast.makeText(context, "¡Cuota #${inst.number} pagada con éxito!", Toast.LENGTH_SHORT).show()
                            reload()
                        }
                    }
                }
            )
        } else {
            // Niveles de Crédito Tab (All 6 levels)
            CreditLevelsTabContent(
                userLevelNumber = userLevelNumber
            )
        }
    }
}

@Composable
private fun CurrentLevelSummaryCard(
    level: CreditLevel,
    verifiedSalesCents: Long
) {
    val levelColor = Color(level.badgeColorHex)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
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
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(levelColor)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "NIVEL ${level.levelNumber}: ${level.title.uppercase()}",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = levelColor
                    )
                }

                Text(
                    text = "Línea: ${PurchaseOffer.formatPen(level.maxAmountCents)}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Level specs
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Cuota inicial", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("${level.downPaymentPercent}%", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                }
                Column {
                    Text("Plazo máximo", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("${level.maxTermDays} días", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                }
                Column {
                    Text("Ventas para ascender", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        if (level.levelNumber == 6) "Nivel Máximo ⭐" else PurchaseOffer.formatPen(level.minMonthlySalesCents),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Progress bar
            val progress = (level.levelNumber.toFloat() / 6f).coerceIn(0f, 1f)
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = levelColor,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        }
    }
}

@Composable
private fun ActiveInstallmentsTabContent(
    activePurchase: CreditPurchase?,
    history: List<CreditPurchase>,
    onOpenTrade: () -> Unit,
    onPayInstallment: (CreditInstallment) -> Unit
) {
    val kuotaColors = MaterialTheme.kuotaColors
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy", Locale.US) }

    if (activePurchase == null && history.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Default.CalendarMonth,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                    modifier = Modifier.size(60.dp)
                )
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    "No tienes cuotas pendientes",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    "Cuando compres a crédito con un proveedor verificado, podrás consultar tus cronogramas y pagar aquí.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                Spacer(modifier = Modifier.height(20.dp))
                Button(onClick = onOpenTrade, shape = RoundedCornerShape(12.dp)) {
                    Icon(Icons.Default.QrCodeScanner, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Comprar al crédito con QR")
                }
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            if (activePurchase != null) {
                item {
                    Text(
                        "Crédito Activo",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
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
                                Column {
                                    Text(
                                        activePurchase.supplierName,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        "Compra total: ${PurchaseOffer.formatPen(activePurchase.totalAmountCents)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                val statusText = when (activePurchase.phase) {
                                    PurchasePhase.PHASE_1_DOWN_PAYMENT_PENDING -> "Fase 1: Inicial pendiente"
                                    PurchasePhase.PHASE_1_DOWN_PAYMENT_SUBMITTED -> "Fase 1: Esperando proveedor"
                                    PurchasePhase.PHASE_2_INSTALLMENTS_SELECTION -> "Fase 2: Elegir plazo"
                                    PurchasePhase.ACTIVE_IN_REPAYMENT -> "En pago"
                                    else -> "Completado"
                                }
                                Text(
                                    statusText,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                            // Breakdown
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("Inicial pagada", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(PurchaseOffer.formatPen(activePurchase.downPaymentCents), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                }
                                Column {
                                    Text("Financiado Kuota", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(PurchaseOffer.formatPen(activePurchase.financedAmountCents), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = kuotaColors.accentTeal)
                                }
                                Column {
                                    Text("Plazo elegido", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("${activePurchase.selectedTermDays ?: 0} días", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                }
                            }

                            // Installment list
                            if (activePurchase.installments.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(14.dp))
                                Text(
                                    "Cronograma de Cuotas",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(8.dp))

                                activePurchase.installments.forEach { inst ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(
                                                if (inst.isPaid) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                                                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                            )
                                            .padding(horizontal = 12.dp, vertical = 10.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(
                                                "Cuota #${inst.number} · ${dateFormat.format(Date(inst.dueEpochMillis))}",
                                                style = MaterialTheme.typography.bodySmall,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                PurchaseOffer.formatPen(inst.amountCents),
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = if (inst.isPaid) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.primary
                                            )
                                        }

                                        if (inst.isPaid) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    Icons.Default.CheckCircle,
                                                    contentDescription = null,
                                                    tint = kuotaColors.accentTeal,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("Pagada", style = MaterialTheme.typography.labelSmall, color = kuotaColors.accentTeal)
                                            }
                                        } else {
                                            Button(
                                                onClick = { onPayInstallment(inst) },
                                                shape = RoundedCornerShape(8.dp),
                                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                            ) {
                                                Text("Pagar", style = MaterialTheme.typography.labelSmall)
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                }
                            }
                        }
                    }
                }
            }

            if (history.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        "Historial de Créditos Pagados",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                items(history, key = { it.id }) { item ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(item.supplierName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                Text("Financiado: ${PurchaseOffer.formatPen(item.financedAmountCents)}", style = MaterialTheme.typography.bodySmall)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = kuotaColors.accentTeal, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Completado", style = MaterialTheme.typography.labelSmall, color = kuotaColors.accentTeal)
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

@Composable
private fun CreditLevelsTabContent(
    userLevelNumber: Int
) {
    val levels = CreditLevel.values()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                "Escalera de Crédito Kuota",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                "Sube de nivel registrando ventas comprobadas y pagando tus cuotas a tiempo.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(6.dp))
        }

        items(levels) { lvl ->
            val isCurrent = lvl.levelNumber == userLevelNumber
            val isUnlocked = lvl.levelNumber <= userLevelNumber
            val lvlColor = Color(lvl.badgeColorHex)

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = if (isCurrent) 2.dp else 1.dp,
                        color = if (isCurrent) lvlColor else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(16.dp)
                    ),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isCurrent) lvlColor.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface
                )
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
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(lvlColor.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "${lvl.levelNumber}",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = lvlColor
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    lvl.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    "Hasta ${PurchaseOffer.formatPen(lvl.maxAmountCents)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = lvlColor,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        if (isCurrent) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(lvlColor)
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text("NIVEL ACTUAL", style = MaterialTheme.typography.labelSmall, color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        } else if (!isUnlocked) {
                            Icon(Icons.Default.Lock, contentDescription = "Bloqueado", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        } else {
                            Icon(Icons.Default.CheckCircle, contentDescription = "Superado", tint = Color(0xFF55DDE0))
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Cuota inicial", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("${lvl.downPaymentPercent}%", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                        }
                        Column {
                            Text("Plazo máximo", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("${lvl.maxTermDays} días", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                        }
                        Column {
                            Text("Ventas requeridas", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                if (lvl.levelNumber == 1) "Sin requisito" else PurchaseOffer.formatPen(lvl.minMonthlySalesCents),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
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
