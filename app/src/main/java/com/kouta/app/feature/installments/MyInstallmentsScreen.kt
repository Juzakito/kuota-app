package com.kouta.app.feature.installments

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Nfc
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

    var autoDebitActive by remember { mutableStateOf(true) }
    var ownerName by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        val prof = journalStore.readBusinessProfile()
        ownerName = prof?.ownerName.orEmpty().ifBlank { prof?.businessName.orEmpty() }
    }

    val currentLevel = CreditLevel.fromNumber(userLevelNumber)
    val atmosphericBrush = Brush.verticalGradient(
        listOf(
            com.kouta.app.core.designsystem.theme.KuotaAtmosphericDeep,
            com.kouta.app.core.designsystem.theme.KuotaAtmosphericMid,
            com.kouta.app.core.designsystem.theme.KuotaAtmosphericLight
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
    ) {
        // -------------------------------------------------------------
        // 1. CABECERA ATMOSFÉRICA & TARJETA ESMERILADA (Estilo Vaulta)
        // -------------------------------------------------------------
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(atmosphericBrush)
                .padding(horizontal = 20.dp)
                .padding(top = 16.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = "Línea Kuota B2B",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "Tu línea sube con tus ventas verificadas",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.85f)
                )
            }

            // Tarjeta Esmerilada Principal (Vaulta Glass Card)
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 190.dp),
                shape = RoundedCornerShape(24.dp),
                color = Color.White.copy(alpha = 0.16f),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.28f)),
                shadowElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.25f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.CreditCard, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                            }
                            Text(
                                "KUOTA B2B",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White,
                                letterSpacing = 1.sp
                            )
                        }
                        Icon(
                            Icons.Default.Nfc,
                            contentDescription = "Contactless",
                            tint = Color.White.copy(alpha = 0.85f),
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    // Nivel y cupo de financiamiento
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = "NIVEL ${currentLevel.levelNumber} · ${currentLevel.tierName.uppercase()}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF5EE0E8),
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Puedes financiar hasta ${PurchaseOffer.formatPen(currentLevel.maxLineCents)}",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                    }

                    // Pie de tarjeta con titular, vencimiento y respaldo
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                "TITULAR COMERCIAL",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 9.sp
                            )
                            Text(
                                ownerName.ifBlank { "MI NEGOCIO B2B" }.uppercase(),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(50),
                            color = com.kouta.app.core.designsystem.theme.KuotaEmerald.copy(alpha = 0.25f),
                            border = BorderStroke(1.dp, com.kouta.app.core.designsystem.theme.KuotaEmerald.copy(alpha = 0.5f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF6EE7B7))
                                )
                                Text(
                                    "RESPALDO EN VENTAS",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // -------------------------------------------------------------
        // 2. BOTONERA CIRCULAR DE CONTROL (Estilo Vaulta - 4 Botones Azules)
        // -------------------------------------------------------------
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                "Control de Línea",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Botón 1: ⇄ Kuotear Factura
                VaultaCircularButton(
                    label = "Kuotear",
                    sublabel = "Factura",
                    icon = Icons.Default.SwapHoriz,
                    onClick = onOpenTrade
                )

                // Botón 2: ⬆️ Subir de nivel
                VaultaCircularButton(
                    label = "Subir",
                    sublabel = "De nivel",
                    icon = Icons.Default.ArrowUpward,
                    onClick = {
                        Toast.makeText(context, "Sube de nivel registrando más ventas comprobadas (Nivel actual: ${currentLevel.tierName})", Toast.LENGTH_LONG).show()
                    }
                )

                // Botón 3: 🔗 Crear Link
                VaultaCircularButton(
                    label = "Crear",
                    sublabel = "Link",
                    icon = Icons.Default.Link,
                    onClick = onOpenTrade
                )

                // Botón 4: ⚡ Débito Auto
                VaultaCircularButton(
                    label = "Débito",
                    sublabel = if (autoDebitActive) "Activo" else "Pausado",
                    icon = Icons.Default.ElectricBolt,
                    isActive = autoDebitActive,
                    onClick = {
                        autoDebitActive = !autoDebitActive
                        Toast.makeText(context, if (autoDebitActive) "Débito automático activado" else "Débito automático pausado", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }

        Spacer(Modifier.height(20.dp))

        // -------------------------------------------------------------
        // 3. TARJETAS GEMELAS DE RENDIMIENTO MENSUAL (This Month / Overall Uses)
        // -------------------------------------------------------------
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Tarjeta 1: Ventas del Mes vs. Meta de Nivel
            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Ventas del Mes",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.SemiBold
                        )
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = com.kouta.app.core.designsystem.theme.KuotaRoyalBlue.copy(alpha = 0.12f)
                        ) {
                            Text(
                                "50%",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = com.kouta.app.core.designsystem.theme.KuotaRoyalBlue,
                                fontSize = 10.sp
                            )
                        }
                    }

                    Text(
                        "S/ 18,000 / 36,000",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    LinearProgressIndicator(
                        progress = { 0.5f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = com.kouta.app.core.designsystem.theme.KuotaRoyalBlue,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )

                    Text(
                        "Meta para Nivel ${currentLevel.levelNumber + 1}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 10.sp
                    )
                }
            }

            // Tarjeta 2: Línea Utilizada vs. Cupo Total
            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Línea Utilizada",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.SemiBold
                        )
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = com.kouta.app.core.designsystem.theme.KuotaRoyalBlue.copy(alpha = 0.12f)
                        ) {
                            Text(
                                "80%",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = com.kouta.app.core.designsystem.theme.KuotaRoyalBlue,
                                fontSize = 10.sp
                            )
                        }
                    }

                    Text(
                        "S/ 2,880 / 3,600",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    LinearProgressIndicator(
                        progress = { 0.8f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = com.kouta.app.core.designsystem.theme.KuotaRoyalBlue,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )

                    Text(
                        "Disponible: S/ 720.00",
                        style = MaterialTheme.typography.labelSmall,
                        color = com.kouta.app.core.designsystem.theme.KuotaEmerald,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // -------------------------------------------------------------
        // 4. ESTADO DE CUENTA Y TARJETA (Card Statement)
        // -------------------------------------------------------------
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFEEF2FF)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.ReceiptLong, contentDescription = null, tint = com.kouta.app.core.designsystem.theme.KuotaRoyalBlue, modifier = Modifier.size(20.dp))
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            "Estado de cuenta",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Extracto de compras y cuotas semanales",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Surface(
                    shape = RoundedCornerShape(50),
                    color = com.kouta.app.core.designsystem.theme.KuotaRoyalBlue.copy(alpha = 0.1f)
                ) {
                    Text(
                        "Al día",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = com.kouta.app.core.designsystem.theme.KuotaRoyalBlue
                    )
                }
            }
        }

        Spacer(Modifier.height(18.dp))

        // -------------------------------------------------------------
        // 5. SECCIÓN DE CUOTAS ACTIVAS & NIVELES DE CRÉDITO
        // -------------------------------------------------------------
        TabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 20.dp).clip(RoundedCornerShape(16.dp))
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
                                    .background(com.kouta.app.core.designsystem.theme.KuotaCoral)
                            )
                        }
                    }
                }
            )
            Tab(
                selected = selectedTabIndex == 1,
                onClick = { selectedTabIndex = 1 },
                text = { Text("Los 6 Niveles") }
            )
        }

        Spacer(Modifier.height(12.dp))

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

        Spacer(Modifier.height(110.dp))
    }
}

@Composable
private fun VaultaCircularButton(
    label: String,
    sublabel: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isActive: Boolean = true,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Surface(
            modifier = Modifier.size(54.dp),
            shape = CircleShape,
            color = if (isActive) Color(0xFF2563EB) else Color(0xFFE2E8F0),
            shadowElevation = 4.dp
        ) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Icon(
                    icon,
                    contentDescription = "$label $sublabel",
                    tint = if (isActive) Color.White else Color(0xFF64748B),
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                sublabel,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 10.sp
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

                                val statusText = activePurchase.statusLabel
                                val statusColor = when (activePurchase.phase) {
                                    PurchasePhase.PHASE_2_INSTALLMENTS_SELECTION -> kuotaColors.accentTeal
                                    PurchasePhase.ACTIVE_IN_REPAYMENT -> if (activePurchase.isOverdue) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                                    PurchasePhase.COMPLETED -> Color(0xFF10B981)
                                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                                }
                                Text(
                                    statusText,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = statusColor
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

                            if (activePurchase.phase == PurchasePhase.PHASE_2_INSTALLMENTS_SELECTION) {
                                Spacer(modifier = Modifier.height(14.dp))
                                Button(
                                    onClick = onOpenTrade,
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Elegir plazo y cronograma de cuotas")
                                }
                            }

                            // Installment list
                            if (activePurchase.installments.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(14.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        "Cronograma de Cuotas",
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        "${activePurchase.totalPaidInstallments} de ${activePurchase.installments.size} pagadas",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))

                                activePurchase.installments.forEach { inst ->
                                    val isInstOverdue = !inst.isPaid && inst.dueEpochMillis < System.currentTimeMillis()
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(
                                                if (inst.isPaid) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                                                else if (isInstOverdue) MaterialTheme.colorScheme.error.copy(alpha = 0.12f)
                                                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                            )
                                            .padding(horizontal = 12.dp, vertical = 10.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    "Cuota #${inst.number} · ${dateFormat.format(Date(inst.dueEpochMillis))}",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                if (isInstOverdue) {
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text(
                                                        "Vencida",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = MaterialTheme.colorScheme.error,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }
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
