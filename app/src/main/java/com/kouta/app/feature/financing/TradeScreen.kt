package com.kouta.app.feature.financing

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.kouta.app.core.designsystem.theme.kuotaColors
import com.kouta.app.data.local.AppSettings
import com.kouta.app.data.local.CreditPurchaseStore
import com.kouta.app.data.local.SalesJournalStore
import com.kouta.app.domain.model.CreditLevel
import com.kouta.app.domain.model.CreditPurchase
import com.kouta.app.domain.model.MarketplaceSupplier
import com.kouta.app.domain.model.PurchaseOffer
import com.kouta.app.domain.model.PurchasePhase
import com.kouta.app.domain.model.SeedSuppliers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TradeScreen(
    initialSupplier: MarketplaceSupplier? = null,
    onNavigateToInstallments: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val creditStore = remember(context) { CreditPurchaseStore(context) }
    val journalStore = remember(context) { SalesJournalStore(context) }
    val appSettings = remember(context) { AppSettings(context) }
    val clipboardManager = LocalClipboardManager.current
    val kuotaColors = MaterialTheme.kuotaColors

    var selectedTab by remember { mutableIntStateOf(0) }
    var activePurchase by remember { mutableStateOf<CreditPurchase?>(null) }
    var userCreditLevel by remember { mutableStateOf<CreditLevel>(CreditLevel.INITIAL_LEVEL) }
    var buyerName by remember { mutableStateOf("") }
    var buyerPhone by remember { mutableStateOf("") }
    var buyerId by remember { mutableStateOf("") }

    // Phase 0: New Purchase Form
    var selectedSupplier by remember { mutableStateOf<MarketplaceSupplier>(initialSupplier ?: SeedSuppliers.items.first()) }
    var supplierDropdownExpanded by remember { mutableStateOf(false) }
    var purchaseAmountInput by remember { mutableStateOf("") }
    var purchaseFormError by remember { mutableStateOf<String?>(null) }

    // Phase 2: Term and Installment configuration
    var selectedTermDays by remember { mutableIntStateOf(7) }
    var selectedInstallmentCount by remember { mutableIntStateOf(3) }
    var showPhase2SuccessDialog by remember { mutableStateOf(false) }

    // Tab 1: Payment Link Generator
    var linkCustomerName by remember { mutableStateOf("") }
    var linkCustomerPhone by remember { mutableStateOf("") }
    var linkCustomerDni by remember { mutableStateOf("") }
    var linkAmountInput by remember { mutableStateOf("") }
    var generatedPaymentLink by remember { mutableStateOf<String?>(null) }

    fun reload() {
        scope.launch {
            val profile = journalStore.readBusinessProfile()
            val levelNum = profile?.creditLevel ?: appSettings.creditLevelNumber
            userCreditLevel = CreditLevel.fromNumber(levelNum)
            buyerName = profile?.businessName.orEmpty().ifBlank { profile?.ownerName.orEmpty() }.ifBlank { "Comercio Kuota" }
            buyerPhone = profile?.phone.orEmpty()
            buyerId = profile?.ruc.orEmpty().ifBlank { profile?.phone.orEmpty() }.ifBlank { "buyer-001" }
            activePurchase = creditStore.getActivePurchase()

            // Preconfigure default term based on level's available terms
            val terms = userCreditLevel.availableTermsDays
            if (selectedTermDays !in terms) {
                selectedTermDays = terms.getOrNull(1) ?: terms.firstOrNull() ?: 7
            }
        }
    }

    LaunchedEffect(Unit) {
        reload()
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            scope.launch {
                val decoded = withContext(Dispatchers.IO) {
                    QrCodeToolkit.decodeQrFromUri(context, uri)
                }
                if (!decoded.isNullOrBlank()) {
                    val matching = SeedSuppliers.items.firstOrNull { decoded.contains(it.id) || decoded.contains(it.name) }
                    if (matching != null) {
                        selectedSupplier = matching
                        Toast.makeText(context, "Proveedor detectado: ${matching.name}", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "QR leído: $decoded", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(context, "No se detectó un QR en la imagen", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    val voucherPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null && activePurchase != null) {
            scope.launch {
                creditStore.submitDownPaymentReceipt(activePurchase!!.id, uri.toString())
                Toast.makeText(context, "Voucher adjuntado con éxito", Toast.LENGTH_SHORT).show()
                reload()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Tab Row Switcher: Comprar vs Vender
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.QrCodeScanner, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Comprar al crédito")
                    }
                }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Link, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Vender con link (1%)")
                    }
                }
            )
        }

        if (selectedTab == 0) {
            // TAB 0: COMPRAR AL CRÉDITO (2 FASES)
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(18.dp)
            ) {
                if (activePurchase != null) {
                    val p = activePurchase!!

                    when (p.phase) {
                        PurchasePhase.PHASE_1_DOWN_PAYMENT_PENDING -> {
                            // Fase 1: Pendiente de pago inicial
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Column(modifier = Modifier.padding(20.dp)) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color(0xFFFFB300).copy(alpha = 0.15f))
                                            .padding(horizontal = 10.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            "FASE 1: PAGO DE CUOTA INICIAL",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFFFB300)
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    Text(
                                        "Paga la cuota inicial al proveedor",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        "Transfiere la cuota inicial directamente al proveedor. Kuota pagará el saldo restante en 48 horas.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )

                                    HorizontalDivider(modifier = Modifier.padding(vertical = 14.dp))

                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("Proveedor:")
                                        Text(p.supplierName, fontWeight = FontWeight.Bold)
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("Datos de pago:")
                                        Text(p.supplierPaymentDetails, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.primary)
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("Monto Total de la compra:")
                                        Text(PurchaseOffer.formatPen(p.totalAmountCents), fontWeight = FontWeight.Bold)
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("Cuota Inicial a pagar (${p.downPaymentPercent}%):", fontWeight = FontWeight.Bold)
                                        Text(PurchaseOffer.formatPen(p.downPaymentCents), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                    }

                                    Spacer(modifier = Modifier.height(18.dp))

                                    Button(
                                        onClick = { voucherPickerLauncher.launch("image/*") },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Icon(Icons.Default.Receipt, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Subir foto del comprobante")
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    // Demo simulation shortcut
                                    OutlinedButton(
                                        onClick = {
                                            scope.launch {
                                                creditStore.submitDownPaymentReceipt(p.id, "simulated_voucher_${System.currentTimeMillis()}")
                                                Toast.makeText(context, "Comprobante simulado enviado", Toast.LENGTH_SHORT).show()
                                                reload()
                                            }
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Text("Simular envío de voucher (Demo)")
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    TextButton(
                                        onClick = {
                                            scope.launch {
                                                creditStore.cancelPurchase(p.id)
                                                Toast.makeText(context, "Solicitud cancelada", Toast.LENGTH_SHORT).show()
                                                reload()
                                            }
                                        },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("Cancelar esta solicitud", color = MaterialTheme.colorScheme.error)
                                    }
                                }
                            }
                        }

                        PurchasePhase.PHASE_1_DOWN_PAYMENT_SUBMITTED -> {
                            // Fase 1: Voucher subido, esperando confirmación
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Column(
                                    modifier = Modifier.padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(48.dp),
                                        color = kuotaColors.accentTeal
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        "Esperando confirmación del proveedor",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        "Has enviado el voucher por ${PurchaseOffer.formatPen(p.downPaymentCents)}. El comercio ${p.supplierName} debe confirmar la recepción en su app Kuota.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        textAlign = TextAlign.Center
                                    )

                                    Spacer(modifier = Modifier.height(20.dp))

                                    Button(
                                        onClick = {
                                            val cleanPhone = p.supplierPhone.replace(" ", "")
                                            val msg = "Hola ${p.supplierName}, ya subí mi voucher de pago inicial Kuota por ${PurchaseOffer.formatPen(p.downPaymentCents)} para confirmar mi compra."
                                            val intent = Intent(Intent.ACTION_VIEW).apply {
                                                data = Uri.parse("https://api.whatsapp.com/send?phone=$cleanPhone&text=${URLEncoder.encode(msg, StandardCharsets.UTF_8.name())}")
                                            }
                                            runCatching { context.startActivity(intent) }
                                                .onFailure {
                                                    Toast.makeText(context, "Avisar al: ${p.supplierPhone}", Toast.LENGTH_LONG).show()
                                                }
                                        },
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366))
                                    ) {
                                        Text("Avisar al proveedor por WhatsApp", fontWeight = FontWeight.Bold, color = Color.White)
                                    }

                                    Spacer(modifier = Modifier.height(14.dp))

                                    // Direct testing bypass button
                                    OutlinedButton(
                                        onClick = {
                                            scope.launch {
                                                creditStore.supplierConfirmDownPayment(p.id)
                                                Toast.makeText(context, "¡Proveedor confirmó la cuota inicial!", Toast.LENGTH_SHORT).show()
                                                reload()
                                            }
                                        },
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Text("Simular confirmación del proveedor (Test)")
                                    }
                                }
                            }
                        }

                        PurchasePhase.PHASE_2_INSTALLMENTS_SELECTION -> {
                            // Fase 2: Elegir plazo en cuotas
                            val availableTerms = userCreditLevel.availableTermsDays
                            val installmentOptions = listOf(1, 2, 3, 4)
                            val esLocale = remember { Locale.forLanguageTag("es-PE") }
                            val scheduleDateFormatter = remember { SimpleDateFormat("EEEE d 'de' MMMM", esLocale) }
                            val now = System.currentTimeMillis()
                            val count = selectedInstallmentCount.coerceAtLeast(1)
                            val baseAmount = p.financedAmountCents / count
                            val remainder = p.financedAmountCents % count
                            val intervalMillis = (selectedTermDays.toLong() * 24L * 60L * 60L * 1000L) / count

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(22.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                            ) {
                                Column(modifier = Modifier.padding(20.dp)) {
                                    // Stage badge & status indicator
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(kuotaColors.accentTeal.copy(alpha = 0.15f))
                                                .padding(horizontal = 10.dp, vertical = 4.dp)
                                        ) {
                                            Text(
                                                "FASE 2: CRONOGRAMA DE CUOTAS",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = kuotaColors.accentTeal
                                            )
                                        }

                                        Text(
                                            "Inicial Confirmada ✓",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color(0xFF10B981),
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    Text(
                                        "Configura tu Plan de Pagos",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        "El proveedor ${p.supplierName} confirmó tu cuota inicial de ${PurchaseOffer.formatPen(p.downPaymentCents)}. Kuota financia el saldo restante a 0% comisiones.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )

                                    Spacer(modifier = Modifier.height(14.dp))

                                    // Financed balance pill
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
                                            .padding(14.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                Text(
                                                    "Saldo neto a financiar",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                                Text(
                                                    PurchaseOffer.formatPen(p.financedAmountCents),
                                                    style = MaterialTheme.typography.titleLarge,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(kuotaColors.accentTeal.copy(alpha = 0.2f))
                                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                                            ) {
                                                Text(
                                                    "0% Interés",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = kuotaColors.accentTeal
                                                )
                                            }
                                        }
                                    }

                                    HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

                                    // Step 1: Plazo total selector (strictly from availableTermsDays)
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            "1. Elige el plazo total:",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            "Nivel ${userCreditLevel.levelNumber} (${userCreditLevel.title})",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.primary,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))

                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        availableTerms.forEach { days ->
                                            FilterChip(
                                                selected = selectedTermDays == days,
                                                onClick = { selectedTermDays = days },
                                                label = { Text("$days días") },
                                                colors = FilterChipDefaults.filterChipColors(
                                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                                )
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(16.dp))

                                    // Step 2: Número de cuotas (1, 2, 3, 4)
                                    Text(
                                        "2. Elige en cuántas cuotas pagar el saldo:",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))

                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        installmentOptions.forEach { optCount ->
                                            FilterChip(
                                                selected = selectedInstallmentCount == optCount,
                                                onClick = { selectedInstallmentCount = optCount },
                                                label = {
                                                    Text(
                                                        if (optCount == 1) "1 cuota"
                                                        else "$optCount cuotas"
                                                    )
                                                },
                                                colors = FilterChipDefaults.filterChipColors(
                                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                                )
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(18.dp))

                                    // Step 3: Cronograma Visual Detallado
                                    Text(
                                        "Cronograma de Pagos Proyectado",
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        "Tus cuotas vencerán en las siguientes fechas programadas:",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )

                                    Spacer(modifier = Modifier.height(10.dp))

                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        (1..count).forEach { num ->
                                            val installmentAmount = if (num == 1) baseAmount + remainder else baseAmount
                                            val dueEpoch = now + (num * intervalMillis)
                                            val daysUntil = ((dueEpoch - now) / (24L * 3600_000L)).coerceAtLeast(1L)
                                            val formattedDate = scheduleDateFormatter.format(Date(dueEpoch))

                                            Card(
                                                modifier = Modifier.fillMaxWidth(),
                                                shape = RoundedCornerShape(12.dp),
                                                colors = CardDefaults.cardColors(
                                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                                                )
                                            ) {
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(horizontal = 14.dp, vertical = 10.dp),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Column {
                                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                                            Box(
                                                                modifier = Modifier
                                                                    .size(20.dp)
                                                                    .clip(CircleShape)
                                                                    .background(MaterialTheme.colorScheme.primary),
                                                                contentAlignment = Alignment.Center
                                                            ) {
                                                                Text(
                                                                    "$num",
                                                                    style = MaterialTheme.typography.labelSmall,
                                                                    color = MaterialTheme.colorScheme.onPrimary,
                                                                    fontWeight = FontWeight.Bold
                                                                )
                                                            }
                                                            Spacer(modifier = Modifier.width(8.dp))
                                                            Text(
                                                                "Cuota #$num de $count",
                                                                style = MaterialTheme.typography.bodySmall,
                                                                fontWeight = FontWeight.Bold
                                                            )
                                                        }
                                                        Spacer(modifier = Modifier.height(2.dp))
                                                        Text(
                                                            formattedDate.replaceFirstChar { it.uppercase() } + " (en $daysUntil días)",
                                                            style = MaterialTheme.typography.labelSmall,
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                                        )
                                                    }

                                                    Text(
                                                        PurchaseOffer.formatPen(installmentAmount),
                                                        style = MaterialTheme.typography.titleMedium,
                                                        fontWeight = FontWeight.Bold,
                                                        color = MaterialTheme.colorScheme.primary
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(14.dp))

                                    // 48h settlement reassurance card
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = CardDefaults.cardColors(containerColor = kuotaColors.accentTeal.copy(alpha = 0.08f))
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                Icons.Default.HourglassTop,
                                                contentDescription = null,
                                                tint = kuotaColors.accentTeal,
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Column {
                                                Text(
                                                    "Liquidación garantizada al proveedor",
                                                    style = MaterialTheme.typography.labelMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = kuotaColors.accentTeal
                                                )
                                                Text(
                                                    "Kuota transferirá ${PurchaseOffer.formatPen(p.supplierPayout48hCents)} a ${p.supplierName} en un plazo máximo de 48 horas.",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(20.dp))

                                    // Button: "Aceptar cronograma" (matches PRD 6.2 item 2)
                                    Button(
                                        onClick = {
                                            scope.launch {
                                                creditStore.confirmInstallmentPlan(
                                                    p.id,
                                                    selectedTermDays,
                                                    selectedInstallmentCount
                                                )
                                                showPhase2SuccessDialog = true
                                                reload()
                                            }
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .heightIn(min = 50.dp),
                                        shape = RoundedCornerShape(14.dp)
                                    ) {
                                        Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(20.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Aceptar cronograma", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        PurchasePhase.ACTIVE_IN_REPAYMENT -> {
                            // Crédito activo en curso
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Column(modifier = Modifier.padding(20.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Info, contentDescription = null, tint = kuotaColors.accentTeal)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Compra a Crédito en Curso", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Text(
                                        "Compraste en ${p.supplierName} por ${PurchaseOffer.formatPen(p.totalAmountCents)}. Ya pagaste la cuota inicial de ${PurchaseOffer.formatPen(p.downPaymentCents)}.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )

                                    Spacer(modifier = Modifier.height(14.dp))

                                    // Rule 3.2 Notice
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(Color(0xFFFFB300).copy(alpha = 0.12f))
                                            .padding(12.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFFFB300), modifier = Modifier.size(18.dp))
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                "Regla 3.2: Solo puedes tener 1 compra a crédito activa a la vez. Termina de liquidar este crédito para solicitar uno nuevo.",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onBackground
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(18.dp))

                                    Button(
                                        onClick = onNavigateToInstallments,
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Ver mis cuotas y realizar pago")
                                    }
                                }
                            }
                        }

                        else -> Unit
                    }
                } else {
                    // NO ACTIVE PURCHASE: Show Phase 0 Start Purchase Form
                    Text(
                        "Solicitar Compra a Crédito",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Compra mercadería o insumos con pago inicial flexible y Kuota financia el resto.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // User Level Banner
                    Card(
                        modifier = Modifier.fillMaxWidth(),
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
                                Text(
                                    "Tu Nivel: ${userCreditLevel.title}",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(userCreditLevel.badgeColorHex)
                                )
                                Text(
                                    "Línea máxima: ${PurchaseOffer.formatPen(userCreditLevel.maxAmountCents)}",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(userCreditLevel.badgeColorHex).copy(alpha = 0.15f))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    "Inicial: ${userCreditLevel.downPaymentPercent}%",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(userCreditLevel.badgeColorHex)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Supplier selector
                    Text("Selecciona el Proveedor:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))

                    ExposedDropdownMenuBox(
                        expanded = supplierDropdownExpanded,
                        onExpandedChange = { supplierDropdownExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = selectedSupplier.name,
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = supplierDropdownExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        ExposedDropdownMenu(
                            expanded = supplierDropdownExpanded,
                            onDismissRequest = { supplierDropdownExpanded = false }
                        ) {
                            SeedSuppliers.items.forEach { sup ->
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text(sup.name, fontWeight = FontWeight.Bold)
                                            Text(sup.displaySubtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    },
                                    onClick = {
                                        selectedSupplier = sup
                                        supplierDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedButton(
                        onClick = { galleryLauncher.launch("image/*") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Escanear QR de tienda desde galería")
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Amount Input
                    OutlinedTextField(
                        value = purchaseAmountInput,
                        onValueChange = {
                            purchaseAmountInput = it
                            purchaseFormError = null
                        },
                        label = { Text("Monto de la compra (S/)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    val enteredAmount = purchaseAmountInput.replace(",", ".").toDoubleOrNull() ?: 0.0
                    val enteredCents = (enteredAmount * 100).toLong()

                    if (enteredCents > 0) {
                        val downCents = (enteredCents * userCreditLevel.downPaymentPercent) / 100
                        val financedCents = enteredCents - downCents

                        Spacer(modifier = Modifier.height(14.dp))

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Cuota inicial directa al proveedor:")
                                    Text(PurchaseOffer.formatPen(downCents), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Financiado por Kuota:")
                                    Text(PurchaseOffer.formatPen(financedCents), fontWeight = FontWeight.Bold, color = kuotaColors.accentTeal)
                                }
                            }
                        }
                    }

                    if (purchaseFormError != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            purchaseFormError!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = {
                            if (enteredCents < 5000) {
                                purchaseFormError = "El monto mínimo de compra es S/ 50.00."
                                return@Button
                            }
                            if (enteredCents > userCreditLevel.maxAmountCents) {
                                purchaseFormError = "El monto supera tu línea disponible de ${PurchaseOffer.formatPen(userCreditLevel.maxAmountCents)}."
                                return@Button
                            }

                            scope.launch {
                                creditStore.createPurchase(
                                    supplierId = selectedSupplier.id,
                                    supplierName = selectedSupplier.name,
                                    supplierPhone = selectedSupplier.phone,
                                    supplierPaymentDetails = selectedSupplier.paymentDetails,
                                    buyerId = buyerId,
                                    buyerName = buyerName,
                                    buyerPhone = buyerPhone,
                                    totalAmountCents = enteredCents,
                                    creditLevelNumber = userCreditLevel.levelNumber,
                                    downPaymentPercent = userCreditLevel.downPaymentPercent
                                )
                                Toast.makeText(context, "Compra iniciada. Procede con la Fase 1 (Cuota inicial).", Toast.LENGTH_SHORT).show()
                                reload()
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Iniciar compra al crédito (Fase 1)", fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(80.dp))
            }
        } else {
            // TAB 1: VENDER CON LINK DE PAGO (1% COMISIÓN)
            val parsedLinkAmount = linkAmountInput.replace(",", ".").toDoubleOrNull() ?: 0.0
            val linkAmountCents = (parsedLinkAmount * 100).toLong()
            val linkFeeCents = (linkAmountCents * 1) / 100
            val linkNetCents = linkAmountCents - linkFeeCents

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(18.dp)
            ) {
                Text(
                    "Cobro con Link de Pago (1%)",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Genera un link seguro para cobrar a tus clientes por WhatsApp o redes. Comisión plana de 1%.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = linkCustomerName,
                    onValueChange = { linkCustomerName = it },
                    label = { Text("Nombre del cliente") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = linkCustomerDni,
                    onValueChange = { linkCustomerDni = it.filter { ch -> ch.isDigit() }.take(8) },
                    label = { Text("DNI del cliente (opcional)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = linkCustomerPhone,
                    onValueChange = { linkCustomerPhone = it },
                    label = { Text("WhatsApp del cliente") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = linkAmountInput,
                    onValueChange = { linkAmountInput = it },
                    label = { Text("Monto a cobrar en Soles (S/)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(14.dp))

                if (linkAmountCents > 0) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text("Desglose Transparente", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Monto venta:")
                                Text(PurchaseOffer.formatPen(linkAmountCents), fontWeight = FontWeight.Bold)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Comisión Kuota (1%):")
                                Text("- ${PurchaseOffer.formatPen(linkFeeCents)}", color = MaterialTheme.colorScheme.error)
                            }
                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Neto a recibir:", fontWeight = FontWeight.Bold)
                                Text(PurchaseOffer.formatPen(linkNetCents), fontWeight = FontWeight.Bold, color = kuotaColors.accentTeal)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }

                Button(
                    onClick = {
                        val encClient = URLEncoder.encode(linkCustomerName.trim().ifBlank { "Cliente" }, StandardCharsets.UTF_8.name())
                        val encSeller = URLEncoder.encode(buyerName, StandardCharsets.UTF_8.name())
                        generatedPaymentLink = "https://kuota.pe/pay?m=$linkAmountCents&c=$encClient&v=$encSeller"
                        Toast.makeText(context, "Link generado con éxito", Toast.LENGTH_SHORT).show()
                    },
                    enabled = linkAmountCents > 0,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Link, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Generar link de cobro")
                }

                if (generatedPaymentLink != null) {
                    Spacer(modifier = Modifier.height(18.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = kuotaColors.accentTeal.copy(alpha = 0.12f))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text("Enlace de cobro generado:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(generatedPaymentLink!!, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        clipboardManager.setText(AnnotatedString(generatedPaymentLink!!))
                                        Toast.makeText(context, "Enlace copiado al portapapeles", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Copiar")
                                }

                                Button(
                                    onClick = {
                                        val cleanPhone = linkCustomerPhone.replace(" ", "").replace("-", "")
                                        val message = "Hola ${linkCustomerName.trim().ifBlank { "" }}, aquí tienes tu enlace de pago Kuota por ${PurchaseOffer.formatPen(linkAmountCents)}: ${generatedPaymentLink!!}"
                                        val intent = Intent(Intent.ACTION_VIEW).apply {
                                            data = Uri.parse("https://api.whatsapp.com/send?phone=$cleanPhone&text=${URLEncoder.encode(message, StandardCharsets.UTF_8.name())}")
                                        }
                                        runCatching { context.startActivity(intent) }
                                            .onFailure {
                                                val shareIntent = Intent().apply {
                                                    action = Intent.ACTION_SEND
                                                    putExtra(Intent.EXTRA_TEXT, message)
                                                    type = "text/plain"
                                                }
                                                context.startActivity(Intent.createChooser(shareIntent, "Compartir enlace"))
                                            }
                                    },
                                    modifier = Modifier.weight(1.2f),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366))
                                ) {
                                    Text("Enviar por WhatsApp", fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }

    if (showPhase2SuccessDialog && activePurchase != null) {
        val p = activePurchase!!
        AlertDialog(
            onDismissRequest = { showPhase2SuccessDialog = false },
            icon = {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = kuotaColors.accentTeal,
                    modifier = Modifier.size(48.dp)
                )
            },
            title = {
                Text(
                    "¡Plan de Cuotas Activado!",
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Has aceptado el cronograma para tu compra en ${p.supplierName} por ${PurchaseOffer.formatPen(p.totalAmountCents)}.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        "• Número de cuotas: ${p.installmentCount ?: selectedInstallmentCount}\n• Plazo total: ${p.selectedTermDays ?: selectedTermDays} días\n• Próximo vencimiento: ${p.nextPendingInstallment?.dueEpochMillis?.let { SimpleDateFormat("dd/MM/yyyy", Locale.US).format(Date(it)) } ?: "Pronto"}\n• Kuota liquidará al proveedor en un plazo máximo de 48 horas.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showPhase2SuccessDialog = false
                        onNavigateToInstallments()
                    }
                ) {
                    Text("Ver en Mis Cuotas")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPhase2SuccessDialog = false }) {
                    Text("Continuar")
                }
            }
        )
    }
}
