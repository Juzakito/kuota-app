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
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import com.kouta.app.data.local.PaymentLinkStore
import com.kouta.app.data.local.ProductCatalogStore
import com.kouta.app.data.local.SalesJournalStore
import com.kouta.app.domain.model.CreditLevel
import com.kouta.app.domain.model.CreditPurchase
import com.kouta.app.domain.model.MarketplaceSupplier
import com.kouta.app.domain.model.MerchantProduct
import com.kouta.app.domain.model.PaymentLink
import com.kouta.app.domain.model.PaymentLinkStatus
import com.kouta.app.domain.model.PurchaseEligibilityCheck
import com.kouta.app.domain.model.PurchaseOffer
import com.kouta.app.domain.model.PurchasePhase
import com.kouta.app.domain.model.SeedSuppliers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
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
    val paymentLinkStore = remember(context) { PaymentLinkStore(context) }
    val productCatalogStore = remember(context) { ProductCatalogStore(context) }
    val appSettings = remember(context) { AppSettings(context) }
    val clipboardManager = LocalClipboardManager.current
    val kuotaColors = MaterialTheme.kuotaColors

    var selectedTab by remember { mutableIntStateOf(TradeScreenNavState.selectedTab) }
    LaunchedEffect(TradeScreenNavState.selectedTab) {
        selectedTab = TradeScreenNavState.selectedTab
    }
    var activePurchase by remember { mutableStateOf<CreditPurchase?>(null) }
    var userCreditLevel by remember { mutableStateOf<CreditLevel>(CreditLevel.INITIAL_LEVEL) }
    var buyerName by remember { mutableStateOf("") }
    var buyerPhone by remember { mutableStateOf("") }
    var buyerId by remember { mutableStateOf("") }

    // Cuenta regresiva de 10 min y términos para compra Cashea B2B
    var countdownSeconds by remember { mutableIntStateOf(600) }
    var termsAccepted by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf("Todos") }
    val supplierCategories = listOf("Todos", "Textil", "Tecnología", "Calzado", "Belleza", "Empaques")

    LaunchedEffect(activePurchase?.phase) {
        if (activePurchase?.phase == PurchasePhase.PHASE_1_DOWN_PAYMENT_PENDING) {
            countdownSeconds = 600
            while (countdownSeconds > 0) {
                delay(1000)
                countdownSeconds--
            }
        }
    }

    // Phase 0: New Purchase Form
    var selectedSupplier by remember { mutableStateOf<MarketplaceSupplier>(initialSupplier ?: SeedSuppliers.items.first()) }
    var supplierDropdownExpanded by remember { mutableStateOf(false) }
    var purchaseAmountInput by remember { mutableStateOf("") }
    var purchaseFormError by remember { mutableStateOf<String?>(null) }

    // Phase 2: Term and Installment configuration
    var selectedTermDays by remember { mutableIntStateOf(21) }
    var selectedInstallmentCount by remember { mutableIntStateOf(3) }
    var showPhase2SuccessDialog by remember { mutableStateOf(false) }

    // Tab 1: Payment Link Generator & History (PRD 6.4 B)
    var linkCustomerName by remember { mutableStateOf("") }
    var linkCustomerPhone by remember { mutableStateOf("") }
    var linkCustomerDni by remember { mutableStateOf("") }
    var linkAmountInput by remember { mutableStateOf("") }
    var linkProducts by remember { mutableStateOf<List<MerchantProduct>>(emptyList()) }
    var selectedProduct by remember { mutableStateOf<MerchantProduct?>(null) }
    var linkQuantity by remember { mutableIntStateOf(1) }
    var linkCustomConcept by remember { mutableStateOf("") }
    var linkReceiptType by remember { mutableStateOf("BOLETA") }
    var linkInternalNote by remember { mutableStateOf("") }
    var linkConsentAccepted by remember { mutableStateOf(false) }
    var linkFormError by remember { mutableStateOf<String?>(null) }
    var linkHistory by remember { mutableStateOf<List<PaymentLink>>(emptyList()) }
    var newlyCreatedLink by remember { mutableStateOf<PaymentLink?>(null) }

    fun reload() {
        scope.launch {
            val profile = journalStore.readBusinessProfile()
            val levelNum = profile?.creditLevel ?: appSettings.creditLevelNumber
            userCreditLevel = CreditLevel.fromNumber(levelNum)
            buyerName = profile?.businessName.orEmpty().ifBlank { profile?.ownerName.orEmpty() }.ifBlank { "Comercio Kuota" }
            buyerPhone = profile?.phone.orEmpty()
            buyerId = profile?.ruc.orEmpty().ifBlank { profile?.phone.orEmpty() }.ifBlank { "buyer-001" }
            activePurchase = creditStore.getActivePurchase()
            linkProducts = withContext(Dispatchers.IO) { productCatalogStore.getAll() }
            linkHistory = paymentLinkStore.getAll()

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
                onClick = {
                    selectedTab = 0
                    TradeScreenNavState.selectedTab = 0
                },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Storefront, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Comprar al crédito")
                    }
                }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = {
                    selectedTab = 1
                    TradeScreenNavState.selectedTab = 1
                },
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
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(Color(0xFFFFB300).copy(alpha = 0.15f))
                                                .padding(horizontal = 10.dp, vertical = 4.dp)
                                        ) {
                                            Text(
                                                "PASO 1 DE 3: PAGO DE INICIAL",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFFFFB300)
                                            )
                                        }

                                        val minLeft = countdownSeconds / 60
                                        val secLeft = countdownSeconds % 60
                                        val timerDisplay = "⏱ ${minLeft.toString().padStart(2, '0')}:${secLeft.toString().padStart(2, '0')}"
                                        Surface(
                                            shape = RoundedCornerShape(50),
                                            color = MaterialTheme.colorScheme.error.copy(alpha = 0.12f)
                                        ) {
                                            Text(
                                                text = timerDisplay,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.error
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    Text(
                                        "Paga la cuota inicial al proveedor",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        "Transfiere la cuota inicial directamente al proveedor vía Yape, Plin o transferencia. Kuota liquidará el saldo al comercio.",
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
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(p.supplierPaymentDetails, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.primary)
                                            Spacer(Modifier.width(6.dp))
                                            Icon(
                                                Icons.Default.ContentCopy,
                                                contentDescription = "Copiar datos",
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier
                                                    .size(16.dp)
                                                    .clickable {
                                                        clipboardManager.setText(AnnotatedString(p.supplierPaymentDetails))
                                                        Toast.makeText(context, "Datos de pago copiados", Toast.LENGTH_SHORT).show()
                                                    }
                                            )
                                        }
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
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("Saldo financiado por Kuota:")
                                        Text(PurchaseOffer.formatPen(p.financedAmountCents), fontWeight = FontWeight.SemiBold, color = kuotaColors.accentTeal)
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("Comisión plana Kuota (5%):")
                                        Text(PurchaseOffer.formatPen(p.kuotaFeeCents), fontWeight = FontWeight.SemiBold)
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
                                                "PASO 2 DE 3: CRONOGRAMA DE CUOTAS",
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
                                        "Configura tu Plan de Cuotas Semanales",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        "El proveedor ${p.supplierName} confirmó tu inicial de ${PurchaseOffer.formatPen(p.downPaymentCents)}. Kuota financia el saldo restante a 0% intereses.",
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

                                    // Step 1: Plazo en cuotas semanales
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            "Elige el número de cuotas semanales:",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            userCreditLevel.title,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.primary,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))

                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        userCreditLevel.availableInstallmentCounts.forEach { optCount ->
                                            FilterChip(
                                                selected = selectedInstallmentCount == optCount,
                                                onClick = {
                                                    selectedInstallmentCount = optCount
                                                    selectedTermDays = optCount * 7
                                                },
                                                label = {
                                                    Text("$optCount cuotas semanales")
                                                },
                                                colors = FilterChipDefaults.filterChipColors(
                                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                                )
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(18.dp))

                                    // Step 2: Cronograma Visual Detallado
                                    Text(
                                        "Cronograma Semanal Proyectado",
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        "Tus cuotas vencerán en las siguientes fechas programadas:",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )

                                    Spacer(modifier = Modifier.height(10.dp))

                                    val totalRepaymentCents = p.financedAmountCents + p.kuotaFeeCents
                                    val count = selectedInstallmentCount.coerceAtLeast(1)
                                    val baseAmount = totalRepaymentCents / count
                                    val remainder = totalRepaymentCents % count
                                    val oneWeekMillis = 7L * 24L * 60L * 60L * 1000L

                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        (1..count).forEach { num ->
                                            val installmentAmount = if (num == count) baseAmount + remainder else baseAmount
                                            val dueEpoch = now + (num * oneWeekMillis)
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
                                                                "Semana #$num de $count",
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

                                    Spacer(modifier = Modifier.height(16.dp))

                                    // Promissory note and terms acceptance
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { termsAccepted = !termsAccepted }
                                            .padding(vertical = 4.dp)
                                    ) {
                                        Checkbox(
                                            checked = termsAccepted,
                                            onCheckedChange = { termsAccepted = it }
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        Text(
                                            "Acepto las condiciones comerciales, el cronograma y el pagaré digital Kuota 2.0 (versión registrada).",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(16.dp))

                                    // Button: "Confirmar compra y activar cuotas"
                                    Button(
                                        onClick = {
                                            scope.launch {
                                                creditStore.confirmInstallmentPlan(
                                                    p.id,
                                                    selectedInstallmentCount * 7,
                                                    selectedInstallmentCount
                                                )
                                                showPhase2SuccessDialog = true
                                                reload()
                                            }
                                        },
                                        enabled = termsAccepted,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .heightIn(min = 50.dp),
                                        shape = RoundedCornerShape(14.dp)
                                    ) {
                                        Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(20.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Confirmar compra y activar cuotas", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
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
                        val availableLineCents = appSettings.availableCreditLineCents.takeIf { it > 0 } ?: userCreditLevel.maxLineCents
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
                                    "Puedes financiar hasta: ${PurchaseOffer.formatPen(availableLineCents)}",
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

                    // Categorías de Proveedores
                    Text("Categorías de Comercio:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        supplierCategories.forEach { cat ->
                            FilterChip(
                                selected = selectedCategory == cat,
                                onClick = { selectedCategory = cat },
                                label = { Text(cat) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Supplier selector
                    Text("Selecciona el Proveedor:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))

                    val filteredSuppliers = remember(selectedCategory) {
                        if (selectedCategory == "Todos") SeedSuppliers.items
                        else SeedSuppliers.items.filter { it.commercialCategory.contains(selectedCategory, ignoreCase = true) || it.displaySubtitle.contains(selectedCategory, ignoreCase = true) }
                    }

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
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = true),
                            shape = RoundedCornerShape(12.dp)
                        )
                        ExposedDropdownMenu(
                            expanded = supplierDropdownExpanded,
                            onDismissRequest = { supplierDropdownExpanded = false }
                        ) {
                            filteredSuppliers.forEach { sup ->
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(sup.name, fontWeight = FontWeight.Bold)
                                                Spacer(Modifier.width(6.dp))
                                                Text("✓ Verificado", style = MaterialTheme.typography.labelSmall, color = Color(0xFF10B981))
                                            }
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
                    val availableLineCents = appSettings.availableCreditLineCents.takeIf { it > 0 } ?: userCreditLevel.maxLineCents
                    val check = PurchaseEligibilityCheck.validate(
                        totalAmountCents = enteredCents,
                        currentLevel = userCreditLevel,
                        availableLineCents = availableLineCents,
                        hasActiveOpenTransaction = activePurchase != null,
                        installmentCount = selectedInstallmentCount
                    )

                    if (enteredCents > 0) {
                        Spacer(modifier = Modifier.height(14.dp))

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    "Simulación Cashea B2B en Tiempo Real",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )

                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Cuota inicial directa (${userCreditLevel.downPaymentPercent}%):")
                                    Text(PurchaseOffer.formatPen(check.downPaymentCents), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                }
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Saldo financiado por Kuota:")
                                    Text(PurchaseOffer.formatPen(check.financedCents), fontWeight = FontWeight.Bold, color = kuotaColors.accentTeal)
                                }
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Comisión plana Kuota (5%):")
                                    Text(PurchaseOffer.formatPen(check.kuotaFeeCents), fontWeight = FontWeight.SemiBold)
                                }

                                Spacer(Modifier.height(4.dp))

                                // Installment count chips
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Cuotas semanales:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        userCreditLevel.availableInstallmentCounts.forEach { opt ->
                                            FilterChip(
                                                selected = selectedInstallmentCount == opt,
                                                onClick = {
                                                    selectedInstallmentCount = opt
                                                    selectedTermDays = opt * 7
                                                },
                                                label = { Text("$opt cuotas") },
                                                colors = FilterChipDefaults.filterChipColors(
                                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                                )
                                            )
                                        }
                                    }
                                }

                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Cuota semanal estimada:", fontWeight = FontWeight.Bold)
                                    Text(
                                        "${PurchaseOffer.formatPen(check.weeklyInstallmentCents)} / sem",
                                        fontWeight = FontWeight.ExtraBold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }

                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Total a pagar (Inicial + Cuotas):", style = MaterialTheme.typography.labelSmall)
                                    Text(PurchaseOffer.formatPen(check.totalToPayCents), fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    if (!check.isEligible && check.errorMessage != null && enteredCents > 0) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    check.errorMessage,
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    style = MaterialTheme.typography.bodySmall
                                )
                                if (check.financedCents > availableLineCents) {
                                    Button(
                                        onClick = {
                                            purchaseAmountInput = String.format(Locale.US, "%.2f", check.maxPurchaseCents / 100.0)
                                        },
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                                    ) {
                                        Text("Ajustar al monto máximo (${PurchaseOffer.formatPen(check.maxPurchaseCents)})")
                                    }
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
                            if (!check.isEligible) {
                                purchaseFormError = check.errorMessage
                                return@Button
                            }
                            if (enteredCents < 5000) {
                                purchaseFormError = "El monto mínimo de compra es S/ 50.00."
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
                                Toast.makeText(context, "Compra iniciada. Procede con el pago de la cuota inicial.", Toast.LENGTH_SHORT).show()
                                reload()
                            }
                        },
                        enabled = check.isEligible && enteredCents >= 5000,
                        modifier = Modifier.fillMaxWidth().heightIn(min = 50.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Continuar a pago de inicial →", fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(110.dp))
            }
        } else {
            // TAB 1: VENDER CON LINK DE PAGO (1% COMISIÓN - PRD 6.4 B)
            val parsedManualAmount = linkAmountInput.replace(",", ".").toDoubleOrNull() ?: 0.0
            val computedAmountCents = if (selectedProduct != null) {
                selectedProduct!!.priceCents * linkQuantity
            } else {
                (parsedManualAmount * 100).toLong()
            }
            val activeConcept = selectedProduct?.name ?: linkCustomConcept.trim().ifBlank { "Venta comercial" }
            val linkFeeCents = (computedAmountCents * 1L) / 100L
            val linkNetCents = computedAmountCents - linkFeeCents

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(18.dp)
            ) {
                Text(
                    "Cobro con Link de Pago (Comisión 1%)",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Genera links para cobrar a tus clientes por WhatsApp o redes. Al pagarse, se registra automáticamente como Venta Comprobada.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Section A: Concepto / Producto de Mis Productos
                Text(
                    "1. Concepto o producto a cobrar:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedProduct == null,
                        onClick = { selectedProduct = null },
                        label = { Text("Monto libre / Manual") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                    linkProducts.forEach { prod ->
                        FilterChip(
                            selected = selectedProduct?.id == prod.id,
                            onClick = {
                                selectedProduct = prod
                                linkQuantity = 1
                            },
                            label = { Text("${prod.name} (${PurchaseOffer.formatPen(prod.priceCents)})") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (selectedProduct != null) {
                    // Stepper de Cantidad
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(selectedProduct!!.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                Text("Precio unitario: ${PurchaseOffer.formatPen(selectedProduct!!.priceCents)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = { if (linkQuantity > 1) linkQuantity-- },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(Icons.Default.Remove, contentDescription = "Menos", modifier = Modifier.size(16.dp))
                                }
                                Text("$linkQuantity", fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp))
                                IconButton(
                                    onClick = { linkQuantity++ },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = "Más", modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                } else {
                    OutlinedTextField(
                        value = linkCustomConcept,
                        onValueChange = { linkCustomConcept = it },
                        label = { Text("Concepto de la venta (ej. Lote de polos, asesoría)") },
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
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Section B: Datos del Cliente (Obligatorios según PRD 6.4 B)
                Text(
                    "2. Datos del cliente (obligatorios):",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = linkCustomerName,
                    onValueChange = { linkCustomerName = it },
                    label = { Text("Nombre completo del cliente") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = linkCustomerDni,
                    onValueChange = {
                        val digits = it.filter { ch -> ch.isDigit() }.take(8)
                        linkCustomerDni = digits
                    },
                    label = { Text("DNI del cliente (8 dígitos)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = linkCustomerPhone,
                    onValueChange = { linkCustomerPhone = it },
                    label = { Text("WhatsApp del cliente (9 dígitos)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Comprobante y notas
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    FilterChip(
                        selected = linkReceiptType == "BOLETA",
                        onClick = { linkReceiptType = "BOLETA" },
                        label = { Text("Boleta") },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = linkReceiptType == "FACTURA",
                        onClick = { linkReceiptType = "FACTURA" },
                        label = { Text("Factura") },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = linkInternalNote,
                    onValueChange = { linkInternalNote = it },
                    label = { Text("Nota interna (opcional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Consentimiento Ley 29733 (Datos Personales de terceros)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { linkConsentAccepted = !linkConsentAccepted }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = linkConsentAccepted,
                        onCheckedChange = { linkConsentAccepted = it },
                        colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        "Cuento con el consentimiento del cliente para el uso de su DNI y teléfono para la emisión del link de pago (Ley N° 29733).",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (linkFormError != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        linkFormError!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (computedAmountCents > 0) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text("Desglose Transparente", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Monto cobrado:")
                                Text(PurchaseOffer.formatPen(computedAmountCents), fontWeight = FontWeight.Bold)
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
                        if (linkCustomerName.isBlank()) {
                            linkFormError = "Ingresa el nombre del cliente."
                            return@Button
                        }
                        if (linkCustomerDni.length != 8) {
                            linkFormError = "El DNI del cliente debe tener exactamente 8 dígitos."
                            return@Button
                        }
                        if (linkCustomerPhone.filter { it.isDigit() }.length < 9) {
                            linkFormError = "Ingresa un número de celular/WhatsApp válido (mínimo 9 dígitos)."
                            return@Button
                        }
                        if (computedAmountCents <= 0) {
                            linkFormError = "El monto debe ser mayor a S/ 0.00."
                            return@Button
                        }
                        if (!linkConsentAccepted) {
                            linkFormError = "Debes aceptar la declaración de datos personales (Ley N° 29733)."
                            return@Button
                        }

                        linkFormError = null
                        scope.launch {
                            val created = paymentLinkStore.createLink(
                                sellerId = buyerId,
                                sellerName = buyerName,
                                customerName = linkCustomerName,
                                customerDni = linkCustomerDni,
                                customerPhone = linkCustomerPhone,
                                concept = activeConcept,
                                quantity = linkQuantity,
                                unitPriceCents = if (selectedProduct != null) selectedProduct!!.priceCents else computedAmountCents,
                                totalAmountCents = computedAmountCents,
                                receiptType = linkReceiptType,
                                internalNote = linkInternalNote,
                                validityHours = 24
                            )
                            newlyCreatedLink = created
                            linkHistory = paymentLinkStore.getAll()
                            Toast.makeText(context, "¡Link de pago generado con éxito!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    enabled = computedAmountCents > 0,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 48.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Link, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Generar link de cobro (1% comisión)", fontWeight = FontWeight.Bold)
                }

                if (newlyCreatedLink != null) {
                    val nl = newlyCreatedLink!!
                    Spacer(modifier = Modifier.height(18.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = kuotaColors.accentTeal.copy(alpha = 0.12f)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, kuotaColors.accentTeal.copy(alpha = 0.4f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Link de Pago Listo", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Text("Vence en 24h", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "Para: ${nl.customerName} · ${PurchaseOffer.formatPen(nl.amountCents)} (${nl.concept})",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(nl.linkUrl, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)

                            Spacer(modifier = Modifier.height(14.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        clipboardManager.setText(AnnotatedString(nl.linkUrl))
                                        Toast.makeText(context, "Enlace copiado al portapapeles", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Copiar")
                                }

                                Button(
                                    onClick = {
                                        scope.launch {
                                            paymentLinkStore.markAsSent(nl.id)
                                            linkHistory = paymentLinkStore.getAll()
                                        }
                                        val cleanPhone = nl.customerPhone.replace(" ", "").replace("-", "")
                                        val message = "Hola ${nl.customerName}, aquí tienes tu link de pago Kuota por ${PurchaseOffer.formatPen(nl.amountCents)} correspondiente a ${nl.concept}: ${nl.linkUrl}"
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
                                    modifier = Modifier.weight(1.3f),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366))
                                ) {
                                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("WhatsApp", fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Demo shortcut
                            OutlinedButton(
                                onClick = {
                                    scope.launch {
                                        paymentLinkStore.markAsPaid(nl.id)
                                        linkHistory = paymentLinkStore.getAll()
                                        newlyCreatedLink = paymentLinkStore.getById(nl.id)
                                        Toast.makeText(context, "¡Cobro simulado exitoso! Registrado como Venta Comprobada en Finanzas.", Toast.LENGTH_LONG).show()
                                    }
                                },
                                enabled = nl.status != PaymentLinkStatus.PAGADO,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text(if (nl.status == PaymentLinkStatus.PAGADO) "Pagado ✓" else "Simular pago del cliente (Demo)")
                            }
                        }
                    }
                }

                // Section C: Historial de Links emitidos
                if (linkHistory.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        "Historial de Links Generados (${linkHistory.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        linkHistory.forEach { link ->
                            val isPaid = link.status == PaymentLinkStatus.PAGADO
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(link.customerName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)

                                        val statusColor = when (link.displayStatus) {
                                            PaymentLinkStatus.PAGADO -> Color(0xFF10B981)
                                            PaymentLinkStatus.ENVIADO -> Color(0xFF8B5CF6)
                                            PaymentLinkStatus.GENERADO -> MaterialTheme.colorScheme.primary
                                            PaymentLinkStatus.VENCIDO -> MaterialTheme.colorScheme.error
                                            PaymentLinkStatus.ANULADO -> Color.Gray
                                        }
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(statusColor.copy(alpha = 0.15f))
                                                .padding(horizontal = 8.dp, vertical = 3.dp)
                                        ) {
                                            Text(link.displayStatus.label, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = statusColor)
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        "${link.concept} · Total: ${PurchaseOffer.formatPen(link.amountCents)} (Neto: ${PurchaseOffer.formatPen(link.netAmountCents)})",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )

                                    if (!isPaid) {
                                        Spacer(modifier = Modifier.height(10.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            OutlinedButton(
                                                onClick = {
                                                    clipboardManager.setText(AnnotatedString(link.linkUrl))
                                                    Toast.makeText(context, "Enlace copiado", Toast.LENGTH_SHORT).show()
                                                },
                                                modifier = Modifier.weight(1f),
                                                shape = RoundedCornerShape(8.dp)
                                            ) {
                                                Text("Copiar", style = MaterialTheme.typography.labelSmall)
                                            }

                                            Button(
                                                onClick = {
                                                    scope.launch {
                                                        paymentLinkStore.markAsPaid(link.id)
                                                        linkHistory = paymentLinkStore.getAll()
                                                        Toast.makeText(context, "¡Venta Comprobada registrada en Finanzas!", Toast.LENGTH_SHORT).show()
                                                    }
                                                },
                                                modifier = Modifier.weight(1.4f),
                                                shape = RoundedCornerShape(8.dp),
                                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                            ) {
                                                Text("Simular pago", style = MaterialTheme.typography.labelSmall)
                                            }
                                        }
                                    } else {
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            "✓ Venta comprobada registrada automáticamente en Finanzas",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color(0xFF10B981),
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(110.dp))
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
