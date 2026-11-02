package com.kouta.app.feature.home

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.NorthEast
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.SouthWest
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import com.kouta.app.feature.financing.TradeScreenNavState
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.kouta.app.R
import com.kouta.app.di.AppEntryPoint
import dagger.hilt.android.EntryPointAccessors
import com.kouta.app.core.designsystem.components.GroupCard
import com.kouta.app.core.designsystem.components.IconBubble
import com.kouta.app.core.designsystem.components.StatusPill
import com.kouta.app.core.designsystem.motion.kuotaPressable
import com.kouta.app.core.designsystem.motion.kuotaTabSectionReveal
import com.kouta.app.core.designsystem.theme.kuotaColors
import com.kuota.core.ui.theme.KuotaColors
import com.kouta.app.core.push.KuotaEvents
import com.kouta.app.core.sync.KuotaSync
import com.kouta.app.core.util.NotificationHelper
import com.kouta.app.data.local.AppSettings
import com.kouta.app.data.local.ClaimSyncStatus
import com.kouta.app.data.local.CreditPurchaseStore
import com.kouta.app.data.local.LocalClaimRecord
import com.kouta.app.data.local.ManualSale
import com.kouta.app.data.local.SalesJournalStore
import com.kouta.app.data.remote.ApiOutcome
import com.kouta.app.data.remote.CreatePaymentIntentBody
import com.kouta.app.data.remote.DemoPaymentEventBody
import com.kouta.app.data.remote.KuotaApiClient
import com.kouta.app.data.remote.PurchaseHistoryItemDto
import com.kouta.app.data.remote.QuoteInstallmentDto
import com.kouta.app.domain.model.PurchaseOffer
import com.kouta.app.domain.model.PurchasePhase
import com.kouta.app.feature.account.AccountLinkDialog
import com.kouta.app.feature.kyc.DniVerificationScreen
import com.kouta.app.feature.payments.InstallmentPaymentWizardDialog
import com.kouta.app.feature.payments.KuotaCardCatalog
import com.kouta.app.feature.payments.resolveCurrentDemoTierId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale
import java.util.UUID

data class StorePartner(
    val name: String,
    val category: String,
    val initialPercent: Int,
    val logoDrawable: Int,
    val address: String,
    val distance: String,
    val highlight: String = "Desde $initialPercent% inicial"
)

val defaultStorePartners = listOf(
    StorePartner("Adidas", "Ropa & Calzado Deportivo", 20, com.kouta.app.R.drawable.ic_brand_adidas, "Jockey Plaza / Real Plaza", "a 1.5 km", "Desde 20% inicial"),
    StorePartner("BKS", "Bicicletas & Accesorios", 30, com.kouta.app.R.drawable.ic_brand_bks, "Av. La Marina 1840", "a 2.3 km", "Desde 30% inicial"),
    StorePartner("MIA", "Moda Femenina & Denim", 20, com.kouta.app.R.drawable.ic_brand_mia, "Galería San Pedro Tda 104", "a 450 m", "Desde 1 cuota"),
    StorePartner("Textil Gamarra", "Telas & Confecciones al por mayor", 30, com.kouta.app.R.drawable.ic_brand_store, "Jr. Gamarra 840, La Victoria", "a 350 m", "Desde 30% inicial"),
    StorePartner("Confecciones Mary", "Moda Urbana & Denim Mayorista", 40, com.kouta.app.R.drawable.ic_brand_store, "Galería Santa Rosa Tda 205", "a 500 m", "Desde 40% inicial"),
    StorePartner("Distribuidora Central", "Abarrotes & Insumos B2B", 20, com.kouta.app.R.drawable.ic_brand_store, "Av. Aviación 1200", "a 1.2 km", "Desde 20% inicial")
)

@Composable
fun HomeScreen(
    onFinanceClick: () -> Unit,
    onCardsClick: () -> Unit = {},
    onActivityClick: () -> Unit,
    onSupportClick: () -> Unit,
    onNotificationsClick: () -> Unit,
    onOpenProfile: () -> Unit
) {
    val context = LocalContext.current
    val appEntryPoint = remember(context) {
        runCatching {
            EntryPointAccessors.fromApplication(
                context.applicationContext,
                AppEntryPoint::class.java
            )
        }.getOrNull()
    }
    val userSummary by (appEntryPoint?.userRepository()?.observeUserSummary()
        ?: kotlinx.coroutines.flow.flowOf(null))
        .collectAsState(initial = null)

    val store = remember(context) { SalesJournalStore(context) }
    val appSettings = remember(context) { AppSettings(context) }
    val scope = rememberCoroutineScope()
    val revision by KuotaEvents.revision.collectAsState()

    var ownerName by remember { mutableStateOf("") }
    var businessName by remember { mutableStateOf("") }
    var businessPhone by remember { mutableStateOf("") }
    var recentSales by remember { mutableStateOf<List<ManualSale>>(emptyList()) }
    var claims by remember { mutableStateOf<List<LocalClaimRecord>>(emptyList()) }
    var unread by remember { mutableStateOf(0) }
    var storageAvailable by remember { mutableStateOf(true) }
    var hasSession by remember { mutableStateOf(false) }
    var canNotify by remember { mutableStateOf(false) }
    var kycSummary by remember { mutableStateOf<String?>(null) }
    var showLinkDialog by remember { mutableStateOf(false) }
    var showKycDialog by remember { mutableStateOf(false) }
    var showFullCalendarDialog by remember { mutableStateOf(false) }
    var purchases by remember { mutableStateOf<List<PurchaseHistoryItemDto>>(emptyList()) }
    var wizardTargetPair by remember { mutableStateOf<Pair<PurchaseHistoryItemDto, QuoteInstallmentDto>?>(null) }
    var isPayingInstallment by remember { mutableStateOf(false) }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        canNotify = NotificationHelper.canPost(context)
        if (granted) {
            Toast.makeText(context, "Notificaciones activadas en este teléfono.", Toast.LENGTH_SHORT).show()
        } else {
            onNotificationsClick()
        }
    }

    suspend fun reloadPurchases() {
        val activeSession = KuotaApiClient.hasSession(context)
        hasSession = activeSession

        val localDtos = withContext(Dispatchers.IO) {
            val creditStore = CreditPurchaseStore(context)
            val allLocal = creditStore.getAll()
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            allLocal
                .filter { it.phase in setOf(PurchasePhase.ACTIVE_IN_REPAYMENT, PurchasePhase.COMPLETED) && it.installments.isNotEmpty() }
                .map { cp ->
                    PurchaseHistoryItemDto(
                        purchaseId = cp.id,
                        status = if (cp.phase == PurchasePhase.COMPLETED) "PAID" else "ACTIVE",
                        updatedAt = dateFormat.format(Date(cp.createdAtEpochMillis)),
                        supplierName = cp.supplierName,
                        purchaseAmountCents = cp.totalAmountCents,
                        buyerDownPaymentCents = cp.downPaymentCents,
                        buyerTotalAfterDownPaymentCents = cp.financedAmountCents,
                        supplierImmediatePayoutCents = cp.downPaymentCents,
                        supplierDeferredPayoutCents = cp.supplierPayout48hCents,
                        koutaInitialContributionCents = cp.financedAmountCents,
                        supplierFeeCents = cp.kuotaFeeCents,
                        installments = cp.installments.map { inst ->
                            QuoteInstallmentDto(
                                installmentId = "${cp.id}_${inst.number}",
                                sequenceNumber = inst.number,
                                amountCents = inst.amountCents,
                                dueAt = dateFormat.format(Date(inst.dueEpochMillis)),
                                status = if (inst.isPaid) "PAID" else "PENDING"
                            )
                        }
                    )
                }
        }

        if (activeSession) {
            val remotePurchases = withContext(Dispatchers.IO) {
                KuotaApiClient.call(context) { listPurchases() }
            }
            if (remotePurchases is ApiOutcome.Ok) {
                val localIds = localDtos.map { it.purchaseId }.toSet()
                purchases = localDtos + remotePurchases.value.filterNot { it.purchaseId in localIds }
            } else {
                purchases = localDtos
            }
        } else {
            purchases = localDtos
        }
    }

    LaunchedEffect(revision) {
        val snapshot = withContext(Dispatchers.IO) {
            runCatching {
                val profile = store.readBusinessProfile()
                val cutoff = System.currentTimeMillis() - 7L * 24 * 60 * 60 * 1000
                HomeSnapshot(
                    ownerName = profile?.ownerName.orEmpty(),
                    businessName = profile?.businessName.orEmpty(),
                    phone = profile?.phone.orEmpty(),
                    claims = profile?.claims.orEmpty(),
                    sales = store.readAll().filter { it.recordedAtEpochMillis >= cutoff },
                    unread = store.readNotifications().count { !it.read }
                )
            }
        }
        snapshot.onSuccess {
            ownerName = it.ownerName
            businessName = it.businessName
            businessPhone = it.phone
            claims = it.claims
            recentSales = it.sales
            unread = it.unread
            storageAvailable = true
        }.onFailure { storageAvailable = false }
        canNotify = NotificationHelper.canPost(context)
        kycSummary = appSettings.kycSummary
        reloadPurchases()
    }

    val kuotaColors = MaterialTheme.kuotaColors
    val headerContent = kuotaColors.headerContent
    val headerBrush = Brush.verticalGradient(kuotaColors.headerGradient)
    val warningContainer = kuotaColors.warningContainer
    val warningContent = kuotaColors.warningContent
    val warningAccent = kuotaColors.warningAccent

    val openClaims = claims.count { it.status != "RESPONDIDO" }
    val salesTotal = recentSales.sumOf { it.amountCents }
    val isKycApproved = userSummary?.kycStatus == "approved" || kycSummary != null
    val effectiveBusinessName = userSummary?.businessName?.trim()?.ifBlank { null } ?: businessName.trim().ifBlank { "Mi Negocio" }
    val greetingName = userSummary?.businessName?.trim()?.ifBlank { null } ?: ownerName.trim().ifBlank { businessName.trim() }.ifBlank { "Emprendedor" }
    val pendingSteps = listOf(hasSession, canNotify, isKycApproved).count { !it }

    val pendingInstallmentsList = remember(purchases) {
        purchases
            .flatMap { purchase ->
                purchase.installments
                    .filter { it.status !in setOf("DEMO_PAID", "PAID") }
                    .map { purchase to it }
            }
            .sortedBy { (_, installment) -> installment.dueAt }
    }
    val nextUpcomingPair: Pair<PurchaseHistoryItemDto, QuoteInstallmentDto>? = pendingInstallmentsList.firstOrNull()
    val totalPaidInstallments = remember(purchases) {
        purchases.sumOf { p -> p.installments.count { it.status in setOf("DEMO_PAID", "PAID") } }
    }
    val currentCardTier = remember(totalPaidInstallments) {
        val tierId = resolveCurrentDemoTierId(totalPaidInstallments)
        KuotaCardCatalog.firstOrNull { it.id == tierId } ?: KuotaCardCatalog.first()
    }

    val userCreditLevel = remember(userSummary, revision) {
        val lvl = userSummary?.level ?: appSettings.creditLevelNumber
        com.kouta.app.domain.model.CreditLevel.fromNumber(lvl)
    }

    val levelSubtitle = "Nivel ${userCreditLevel.levelNumber}: ${userCreditLevel.title} · Inicial ${userCreditLevel.downPaymentPercent}%"

    val quickAccess = listOf(
        QuickAccess("Comprar QR", Icons.Default.QrCodeScanner, onFinanceClick),
        QuickAccess(
            "Tarjetas",
            Icons.Default.CreditCard,
            onCardsClick,
            badge = if (pendingInstallmentsList.isNotEmpty()) "${pendingInstallmentsList.size}" else "4"
        ),
        QuickAccess(
            "Calendario",
            Icons.Default.CalendarMonth,
            onClick = { showFullCalendarDialog = true },
            badge = pendingInstallmentsList.size.takeIf { it > 0 }?.toString()
        ),
        QuickAccess("Actividad", Icons.Default.PointOfSale, onActivityClick),
        QuickAccess(
            "Ayuda",
            Icons.Default.SupportAgent,
            onSupportClick,
            badge = openClaims.takeIf { it > 0 }?.let { if (it > 9) "9+" else "$it" }
        ),
        QuickAccess(
            "Avisos",
            Icons.Default.NotificationsNone,
            onNotificationsClick,
            badge = unread.takeIf { it > 0 }?.let { if (it > 9) "9+" else "$it" }
        ),
        QuickAccess(
            "DNI / KYC",
            Icons.Default.Badge,
            onClick = { showKycDialog = true },
            badge = if (kycSummary == null) "!" else null
        ),
        QuickAccess("Mi negocio", Icons.Default.Storefront, onOpenProfile)
    )

    var showBalance by rememberSaveable { mutableStateOf(true) }
    var transactionTab by rememberSaveable { mutableIntStateOf(0) } // 0: Movimientos, 1: Cuotas por pagar

    // Datos coherentes PRD HOME-01 y HOME-02
    val availableLineCents = remember(userSummary, userCreditLevel) {
        userSummary?.availableCreditCents ?: appSettings.availableCreditLineCents.takeIf { it > 0 } ?: userCreditLevel.maxLineCents
    }
    val maxPurchaseCapacityCents = remember(userCreditLevel, availableLineCents) {
        userCreditLevel.calculateMaxPurchaseCents(availableLineCents)
    }

    val formattedLine = PurchaseOffer.formatPen(availableLineCents)
    val integerPart = formattedLine.substringBefore(".")
    val decimalPart = if (formattedLine.contains(".")) "." + formattedLine.substringAfter(".") else ""

    val nextLevelNum = (userCreditLevel.levelNumber + 1).coerceAtMost(6)
    val nextLevel = com.kouta.app.domain.model.CreditLevel.fromNumber(nextLevelNum)
    val nextLevelGoalCents = nextLevel.minLineCents * 2L
    val salesProgressRatio = (salesTotal.toFloat() / nextLevelGoalCents.toFloat()).coerceIn(0f, 1f)

    val principalLineCents = remember(availableLineCents) { (availableLineCents * 70L) / 100L }
    val cotidianaLineCents = remember(availableLineCents, principalLineCents) { availableLineCents - principalLineCents }

    var showLevelModal by remember { mutableStateOf(false) }
    var showNearbyStoresModal by remember { mutableStateOf(false) }
    var showHelpModal by remember { mutableStateOf(false) }
    var showHowToUseModal by remember { mutableStateOf(false) }
    var showWhereToUseModal by remember { mutableStateOf(false) }
    var showPrincipalLineModal by remember { mutableStateOf(false) }
    var showCotidianaLineModal by remember { mutableStateOf(false) }
    var showPayMobileModal by remember { mutableStateOf(false) }
    var showPayServicesModal by remember { mutableStateOf(false) }
    var showCouponsModal by remember { mutableStateOf(false) }
    var showInviteEarnModal by remember { mutableStateOf(false) }
    var selectedStoreForQuickView by remember { mutableStateOf<StorePartner?>(null) }

    val atmosphericHeaderBrush = Brush.verticalGradient(
        listOf(
            Color(0xFFFDE047), // Vibrant Cashea energetic yellow
            Color(0xFFFEF08A).copy(alpha = 0.85f),
            Color(0xFFFEF9C3).copy(alpha = 0.45f),
            KuotaColors.White
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
    ) {
        // -------------------------------------------------------------
        // CABECERA ESTILO CASHEA B2B: DOS LÍNEAS DE COMPRA
        // -------------------------------------------------------------
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(atmosphericHeaderBrush)
                .padding(horizontal = 20.dp)
                .padding(top = 16.dp, bottom = 12.dp)
                .kuotaTabSectionReveal(sectionIndex = 0),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Fila Superior: [Píldora Nivel 1] [Píldora Tiendas cerca] [Círculo Ayuda (?)]
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // 1. Píldora Nivel
                Surface(
                    shape = RoundedCornerShape(50),
                    color = KuotaColors.White,
                    border = BorderStroke(1.dp, KuotaColors.Periwinkle.copy(alpha = 0.7f)),
                    modifier = Modifier.clickable { showLevelModal = true },
                    shadowElevation = 1.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Image(
                            painter = painterResource(R.drawable.kuota_mark),
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Nivel ${userCreditLevel.levelNumber}",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = KuotaColors.PrussianBlue
                        )
                    }
                }

                // 2. Píldora Tiendas Cerca
                Surface(
                    shape = RoundedCornerShape(50),
                    color = KuotaColors.White,
                    border = BorderStroke(1.dp, KuotaColors.Periwinkle.copy(alpha = 0.7f)),
                    modifier = Modifier.clickable { showNearbyStoresModal = true },
                    shadowElevation = 1.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = KuotaColors.PersianBlue,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Tiendas cerca",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = KuotaColors.PrussianBlue
                        )
                    }
                }

                // 3. Botón Circular Ayuda (?)
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(KuotaColors.White)
                        .border(1.dp, KuotaColors.Periwinkle.copy(alpha = 0.7f), CircleShape)
                        .clickable { showHelpModal = true },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.HelpOutline,
                        contentDescription = "Ayuda",
                        tint = KuotaColors.PersianBlue,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Encabezado de Sección: "Tus líneas" + "¿Cómo las uso?"
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Tus líneas",
                    style = MaterialTheme.typography.titleLarge.copy(fontSize = 23.sp),
                    fontWeight = FontWeight.ExtraBold,
                    color = KuotaColors.PrussianBlue
                )
                Text(
                    text = "¿Cómo las uso?",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = KuotaColors.PersianBlue,
                    modifier = Modifier
                        .clickable { showHowToUseModal = true }
                        .padding(vertical = 4.dp, horizontal = 2.dp)
                )
            }

            // Tarjeta Tip flotante: "¿Dónde uso mis líneas?"
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showWhereToUseModal = true },
                shape = RoundedCornerShape(16.dp),
                color = KuotaColors.Lavender,
                border = BorderStroke(1.dp, KuotaColors.Periwinkle.copy(alpha = 0.7f)),
                shadowElevation = 1.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Image(
                        painter = painterResource(R.drawable.ic_brand_store),
                        contentDescription = null,
                        modifier = Modifier.size(42.dp)
                    )
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = "<Desde 1 cuota con la Cotidiana.>",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Medium,
                            color = KuotaColors.PrussianBlue.copy(alpha = 0.75f)
                        )
                        Text(
                            text = "¿Dónde uso mis líneas?",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = KuotaColors.PrussianBlue
                        )
                    }
                }
            }

            // Dos tarjetas de línea en paralelo: Principal y Cotidiana
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Tarjeta 1: Principal
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { showPrincipalLineModal = true },
                    shape = RoundedCornerShape(16.dp),
                    color = KuotaColors.White,
                    border = BorderStroke(1.dp, KuotaColors.Periwinkle.copy(alpha = 0.7f)),
                    shadowElevation = 1.dp
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
                                text = "Principal",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = KuotaColors.PrussianBlue
                            )
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = null,
                                tint = KuotaColors.PrussianBlue.copy(alpha = 0.6f),
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        Row(verticalAlignment = Alignment.Top) {
                            Text(
                                text = "S/ ${principalLineCents / 100}",
                                style = MaterialTheme.typography.titleLarge.copy(fontSize = 18.sp),
                                fontWeight = FontWeight.ExtraBold,
                                color = KuotaColors.PrussianBlue
                            )
                            Text(
                                text = ".00",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                fontWeight = FontWeight.Bold,
                                color = KuotaColors.PrussianBlue,
                                modifier = Modifier.padding(top = 1.dp)
                            )
                            Text(
                                text = " de S/ ${availableLineCents / 100}",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                color = KuotaColors.PrussianBlue.copy(alpha = 0.6f),
                                modifier = Modifier.padding(start = 3.dp, top = 2.dp)
                            )
                        }

                        // Barra acento en amarillo/dorado
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.65f)
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(Color(0xFFFACC15))
                        )
                    }
                }

                // Tarjeta 2: Cotidiana
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { showCotidianaLineModal = true },
                    shape = RoundedCornerShape(16.dp),
                    color = KuotaColors.White,
                    border = BorderStroke(1.dp, KuotaColors.Periwinkle.copy(alpha = 0.7f)),
                    shadowElevation = 1.dp
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
                                text = "Cotidiana",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = KuotaColors.PrussianBlue
                            )
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = null,
                                tint = KuotaColors.PrussianBlue.copy(alpha = 0.6f),
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        Row(verticalAlignment = Alignment.Top) {
                            Text(
                                text = "S/ ${cotidianaLineCents / 100}",
                                style = MaterialTheme.typography.titleLarge.copy(fontSize = 18.sp),
                                fontWeight = FontWeight.ExtraBold,
                                color = KuotaColors.PrussianBlue
                            )
                            Text(
                                text = ".00",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                fontWeight = FontWeight.Bold,
                                color = KuotaColors.PrussianBlue,
                                modifier = Modifier.padding(top = 1.dp)
                            )
                            Text(
                                text = " de S/ ${availableLineCents / 100}",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                color = KuotaColors.PrussianBlue.copy(alpha = 0.6f),
                                modifier = Modifier.padding(start = 3.dp, top = 2.dp)
                            )
                        }

                        // Barra acento en amarillo/dorado
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.5f)
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(Color(0xFFFACC15))
                        )
                    }
                }
            }
        }

        // -------------------------------------------------------------
        // CUATRO ACCIONES RÁPIDAS (Pagar celular, Pagar servicios, Canjear cupones, Invitar y ganar)
        // -------------------------------------------------------------
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 6.dp)
                .kuotaTabSectionReveal(sectionIndex = 1),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            val quickActions = listOf(
                Triple("Pagar celular", Icons.Default.Smartphone) { showPayMobileModal = true },
                Triple("Pagar servicios", Icons.AutoMirrored.Filled.ReceiptLong) { showPayServicesModal = true },
                Triple("Canjear cupones", Icons.Default.ConfirmationNumber) { showCouponsModal = true },
                Triple("Invitar y ganar", Icons.Default.CardGiftcard) { showInviteEarnModal = true }
            )

            quickActions.forEach { (label, icon, onClick) ->
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clickable(onClick = onClick),
                    shape = RoundedCornerShape(16.dp),
                    color = KuotaColors.White,
                    border = BorderStroke(1.dp, KuotaColors.Periwinkle.copy(alpha = 0.6f)),
                    shadowElevation = 1.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp, horizontal = 4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFFFEF9C3).copy(alpha = 0.6f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = label,
                                tint = KuotaColors.PrussianBlue,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                            fontWeight = FontWeight.Bold,
                            color = KuotaColors.PrussianBlue,
                            textAlign = TextAlign.Center,
                            maxLines = 2,
                            lineHeight = 14.sp
                        )
                    }
                }
            }
        }

        // -------------------------------------------------------------
        // TIENDAS DISPONIBLES (Carrusel de comercios y marcas aliadas)
        // -------------------------------------------------------------
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .clickable { showNearbyStoresModal = true },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Ver todas las tiendas disponibles",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = KuotaColors.PrussianBlue
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = KuotaColors.PrussianBlue,
                    modifier = Modifier.size(20.dp)
                )
            }

            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(defaultStorePartners) { store ->
                    Surface(
                        modifier = Modifier
                            .width(108.dp)
                            .height(84.dp)
                            .clickable { selectedStoreForQuickView = store },
                        shape = RoundedCornerShape(14.dp),
                        color = KuotaColors.White,
                        border = BorderStroke(1.dp, KuotaColors.Periwinkle.copy(alpha = 0.6f)),
                        shadowElevation = 1.dp
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(store.logoDrawable),
                                contentDescription = store.name,
                                modifier = Modifier.size(54.dp),
                                contentScale = ContentScale.Fit
                            )
                        }
                    }
                }
            }
        }

        // Recordatorio de cuota pendiente si existe
        if (nextUpcomingPair != null) {
            val (activePurchase, nextInstallment) = nextUpcomingPair
            Spacer(Modifier.height(14.dp))
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                shape = RoundedCornerShape(16.dp),
                color = KuotaColors.Lavender,
                border = BorderStroke(1.dp, KuotaColors.Periwinkle.copy(alpha = 0.7f))
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = KuotaColors.PersianBlue, modifier = Modifier.size(15.dp))
                            Text("Próxima cuota por pagar", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = KuotaColors.PersianBlue)
                        }
                        Text(
                            "${activePurchase.supplierName} · Cuota #${nextInstallment.sequenceNumber} (${PurchaseOffer.formatPen(nextInstallment.amountCents)})",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = KuotaColors.PrussianBlue
                        )
                        Text("Vence el ${nextInstallment.dueAt}", style = MaterialTheme.typography.labelSmall, color = KuotaColors.PrussianBlue.copy(alpha = 0.7f))
                    }
                    Button(
                        onClick = { wizardTargetPair = activePurchase to nextInstallment },
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(containerColor = KuotaColors.PersianBlue, contentColor = KuotaColors.White),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text("Pagar", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Banner inferior de llamado a compra al crédito
        Spacer(Modifier.height(14.dp))
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .clickable { onFinanceClick() },
            shape = RoundedCornerShape(20.dp),
            color = Color(0xFFFEF08A),
            border = BorderStroke(1.dp, Color(0xFFFACC15).copy(alpha = 0.7f))
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Lo que necesitas para tu negocio",
                    style = MaterialTheme.typography.titleMedium.copy(fontSize = 18.sp),
                    fontWeight = FontWeight.ExtraBold,
                    color = KuotaColors.PrussianBlue
                )
                Text(
                    text = "Compra hoy en tiendas aliadas y paga desde 1 hasta 4 cuotas semanales fijas sin intereses ocultos.",
                    style = MaterialTheme.typography.bodySmall,
                    color = KuotaColors.PrussianBlue.copy(alpha = 0.8f),
                    lineHeight = 18.sp
                )
                Row(
                    modifier = Modifier.padding(top = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "Escanear QR de compra",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = KuotaColors.PersianBlue
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = KuotaColors.PersianBlue,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        Spacer(Modifier.height(14.dp))

        // -------------------------------------------------------------
        // 4. CONSEJOS DE KUOTA (HOME-06, Adaptación Bill Negotiator Ethan Carter)
        // -------------------------------------------------------------
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .kuotaTabSectionReveal(sectionIndex = 3),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(KuotaColors.Lavender)
                            .border(1.dp, KuotaColors.Periwinkle.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(Icons.Default.ElectricBolt, contentDescription = null, tint = KuotaColors.PersianBlue, modifier = Modifier.size(14.dp))
                            Text(
                                "CONSEJO DE KUOTA",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = KuotaColors.PersianBlue
                            )
                        }
                    }
                }

                Text(
                    text = "Tus ventas verificadas aumentan tu récord crediticio. Con pagos puntuales y cobros recurrentes aceleras tu avance hacia el Nivel $nextLevelNum con inicial reducida al ${nextLevel.downPaymentPercentage}%.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 20.sp
                )

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = com.kouta.app.core.designsystem.theme.KuotaRoyalBlue.copy(alpha = 0.08f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onCardsClick)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 11.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "Ver escala de 6 niveles de crédito →",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = com.kouta.app.core.designsystem.theme.KuotaRoyalBlue
                        )
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = com.kouta.app.core.designsystem.theme.KuotaRoyalBlue,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(14.dp))

        // -------------------------------------------------------------
        // 5. BENEFICIOS DE LA SEMANA (HOME-09, "La Cosecha" Cashea B2B)
        // -------------------------------------------------------------
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color(0xFFD97706), modifier = Modifier.size(18.dp))
                    Text("Beneficios de la Semana", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
                Text("7 días restantes", style = MaterialTheme.typography.labelSmall, color = Color(0xFFD97706), fontWeight = FontWeight.Bold)
            }

            Spacer(Modifier.height(10.dp))

            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val benefits = listOf(
                    Triple("Textil Gamarra Mayorista", "Inicial reducida al 30% esta semana (antes 50%)", "Textil"),
                    Triple("Distribuidora Alicorp", "Financiamiento directo en hasta 4 cuotas semanales", "Alimentos"),
                    Triple("Empaques & Cajas Perú", "10% de descuento automático al kuotear tu pedido", "Empaques")
                )
                items(benefits) { (supplier, promo, tag) ->
                    Card(
                        modifier = Modifier
                            .width(260.dp)
                            .clickable(onClick = onFinanceClick),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFFFEF3C7))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(tag, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color(0xFFB45309))
                            }
                            Text(supplier, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                            Text(promo, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // -------------------------------------------------------------
        // 6. ACTIVIDAD Y CUOTAS PROGRAMADAS (HOME-07 & HOME-08, Ethan Carter "Bills & Payments")
        // -------------------------------------------------------------
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "Actividad y Cuotas Programadas",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            // Selector de píldoras
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("Movimientos (Ventas)", "Cuotas por pagar (${pendingInstallmentsList.size})").forEachIndexed { idx, label ->
                    val isSelected = transactionTab == idx
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = if (isSelected) KuotaColors.PersianBlue else KuotaColors.Lavender,
                        border = BorderStroke(1.dp, if (isSelected) KuotaColors.PersianBlue else KuotaColors.Periwinkle.copy(alpha = 0.6f)),
                        modifier = Modifier.clickable { transactionTab = idx }
                    ) {
                        Text(
                            text = label,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) KuotaColors.White else KuotaColors.PrussianBlue
                        )
                    }
                }
            }

            if (transactionTab == 0) {
                // Tab Movimientos
                if (recentSales.isNotEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            recentSales.take(5).forEach { sale ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(CircleShape)
                                                .background(com.kouta.app.core.designsystem.theme.KuotaEmerald.copy(alpha = 0.15f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                Icons.Default.SouthWest,
                                                contentDescription = null,
                                                tint = com.kouta.app.core.designsystem.theme.KuotaEmerald,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                        Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                                            Text(
                                                sale.productName?.takeIf { it.isNotBlank() } ?: "Venta ${sale.paymentMethod}",
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                "${sale.paymentMethod} · ${if (sale.isVerified) "Verificada ✓" else "Registrada"}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = if (sale.isVerified) com.kouta.app.core.designsystem.theme.KuotaEmerald else MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    Text(
                                        "+ ${PurchaseOffer.formatPen(sale.amountCents)}",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = com.kouta.app.core.designsystem.theme.KuotaEmerald
                                    )
                                }
                            }
                        }
                    }
                } else {
                    // Estado vacío (HOME-08)
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                Icons.Default.Storefront,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(44.dp)
                            )
                            Text(
                                "Aún no tienes movimientos",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "Cobra tu primera venta con link o registra un pago para empezar a construir tu línea de crédito.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                            Button(
                                onClick = {
                                    TradeScreenNavState.openLink()
                                    onFinanceClick()
                                },
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Cobrar mi primera venta (1%)")
                            }
                        }
                    }
                }
            } else {
                // Tab Cuotas por pagar
                if (pendingInstallmentsList.isNotEmpty()) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        pendingInstallmentsList.forEach { (purchase, installment) ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                        Text(
                                            "${purchase.supplierName} · Cuota #${installment.sequenceNumber}",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            "Vence: ${installment.dueAt}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = com.kouta.app.core.designsystem.theme.KuotaCoral,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                    Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Text(
                                            PurchaseOffer.formatPen(installment.amountCents),
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = com.kouta.app.core.designsystem.theme.KuotaCoral
                                        )
                                        OutlinedButton(
                                            onClick = { wizardTargetPair = purchase to installment },
                                            shape = RoundedCornerShape(50),
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                            modifier = Modifier.height(32.dp)
                                        ) {
                                            Text("Pagar cuota", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = com.kouta.app.core.designsystem.theme.KuotaEmerald,
                                modifier = Modifier.size(44.dp)
                            )
                            Text(
                                "¡Estás al día!",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "No tienes cuotas pendientes por pagar. Tu línea de financiamiento está 100% libre.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(18.dp))
        Text(
            "Datos locales cifrados con AES-256 · Respaldado en tus ventas",
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(110.dp))
    }
    if (showLinkDialog) {
        AccountLinkDialog(
            initialPhone = businessPhone,
            onDismiss = { showLinkDialog = false },
            onLinked = {
                showLinkDialog = false
                hasSession = true
                scope.launch {
                    withContext(Dispatchers.IO) { runCatching { KuotaSync.runOnce(context) } }
                    unread = withContext(Dispatchers.IO) { runCatching { store.readNotifications().count { !it.read } }.getOrDefault(unread) }
                    reloadPurchases()
                }
            }
        )
    }

    if (showKycDialog) {
        androidx.compose.ui.window.Dialog(
            onDismissRequest = { showKycDialog = false },
            properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.95f)
                    .fillMaxHeight(0.92f),
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.background,
                tonalElevation = 6.dp
            ) {
                DniVerificationScreen(
                    onVerificationSuccess = { dni, name ->
                        val summary = "$name · DNI ****${dni.takeLast(3)}"
                        appSettings.kycSummary = summary
                        kycSummary = summary
                        showKycDialog = false
                        KuotaEvents.bump()
                        Toast.makeText(context, "Identidad registrada: $summary", Toast.LENGTH_SHORT).show()
                    },
                    onDismiss = { showKycDialog = false }
                )
            }
        }
    }

    if (showFullCalendarDialog) {
        FullInstallmentsCalendarDialog(
            purchases = purchases,
            onDismiss = { showFullCalendarDialog = false },
            onPayInstallment = { purchase, installment ->
                showFullCalendarDialog = false
                wizardTargetPair = purchase to installment
            },
            onOpenCards = {
                showFullCalendarDialog = false
                onCardsClick()
            },
            onOpenQr = {
                showFullCalendarDialog = false
                onFinanceClick()
            }
        )
    }

    wizardTargetPair?.let { (targetPurchase, targetInstallment) ->
        InstallmentPaymentWizardDialog(
            supplierName = targetPurchase.supplierName,
            selectedInstallments = listOf(targetInstallment),
            isSubmitting = isPayingInstallment,
            onDismiss = { if (!isPayingInstallment) wizardTargetPair = null },
            onConfirmPayment = { channelId, originBank, _, paymentDate, referenceNumber, _ ->
                if (isPayingInstallment) return@InstallmentPaymentWizardDialog
                isPayingInstallment = true
                scope.launch {
                    val localPaid = withContext(Dispatchers.IO) {
                        val creditStore = CreditPurchaseStore(context)
                        creditStore.payInstallment(targetPurchase.purchaseId, targetInstallment.sequenceNumber)
                    }

                    if (localPaid != null) {
                        reloadPurchases()
                        KuotaEvents.bump()
                        Toast.makeText(
                            context,
                            "Cuota #${targetInstallment.sequenceNumber} conciliada ($channelId · $originBank · Op. $referenceNumber · $paymentDate)",
                            Toast.LENGTH_LONG
                        ).show()
                        wizardTargetPair = null
                    } else {
                        val outcome = withContext(Dispatchers.IO) {
                            val createOutcome = KuotaApiClient.call(context) {
                                createPaymentIntent(
                                    installmentId = targetInstallment.installmentId,
                                    idempotencyKey = "home-wizard-pay-${targetInstallment.installmentId}-${UUID.randomUUID()}",
                                    body = CreatePaymentIntentBody(paymentMethodId = channelId)
                                )
                            }
                            when (createOutcome) {
                                is ApiOutcome.Ok -> {
                                    KuotaApiClient.call(context) {
                                        applyDemoPaymentEvent(
                                            paymentId = createOutcome.value.paymentId,
                                            idempotencyKey = "home-wizard-settle-${createOutcome.value.paymentId}-${UUID.randomUUID()}",
                                            body = DemoPaymentEventBody(eventType = "SUCCEED")
                                        )
                                    }
                                }
                                else -> createOutcome
                            }
                        }
                        when (outcome) {
                            is ApiOutcome.Ok -> {
                                reloadPurchases()
                                KuotaEvents.bump()
                                Toast.makeText(
                                    context,
                                    "Cuota #${targetInstallment.sequenceNumber} conciliada ($channelId · $originBank · Op. $referenceNumber · $paymentDate)",
                                    Toast.LENGTH_LONG
                                ).show()
                                wizardTargetPair = null
                            }
                            is ApiOutcome.Rejected -> {
                                Toast.makeText(context, outcome.message, Toast.LENGTH_SHORT).show()
                            }
                            is ApiOutcome.Unreachable -> {
                                Toast.makeText(context, "No se pudo conectar al servidor (${outcome.serverUrl}).", Toast.LENGTH_SHORT).show()
                            }
                            ApiOutcome.SessionExpired -> {
                                showLinkDialog = true
                            }
                        }
                    }
                    isPayingInstallment = false
                }
            }
        )
    }

    if (showLevelModal) {
        LevelExplanationDialog(
            userCreditLevel = userCreditLevel,
            availableLineCents = availableLineCents,
            onDismiss = { showLevelModal = false },
            onViewAllLevels = {
                showLevelModal = false
                onCardsClick()
            }
        )
    }

    if (showNearbyStoresModal) {
        NearbyStoresSheetDialog(
            onDismiss = { showNearbyStoresModal = false },
            onStoreClick = { store ->
                showNearbyStoresModal = false
                selectedStoreForQuickView = store
            },
            onScanQr = {
                showNearbyStoresModal = false
                onFinanceClick()
            }
        )
    }

    if (showHelpModal) {
        HelpSupportSheetDialog(
            onDismiss = { showHelpModal = false }
        )
    }

    if (showHowToUseModal) {
        HowToUseLinesSheetDialog(
            onDismiss = { showHowToUseModal = false },
            onStartShopping = {
                showHowToUseModal = false
                onFinanceClick()
            }
        )
    }

    if (showWhereToUseModal) {
        WhereToUseLinesSheetDialog(
            onDismiss = { showWhereToUseModal = false },
            onScanQr = {
                showWhereToUseModal = false
                onFinanceClick()
            },
            onExploreStores = {
                showWhereToUseModal = false
                showNearbyStoresModal = true
            }
        )
    }

    if (showPrincipalLineModal) {
        PrincipalLineSheetDialog(
            principalLineCents = principalLineCents,
            totalLineCents = availableLineCents,
            downPaymentPercentage = userCreditLevel.downPaymentPercentage,
            onDismiss = { showPrincipalLineModal = false },
            onFinance = {
                showPrincipalLineModal = false
                onFinanceClick()
            }
        )
    }

    if (showCotidianaLineModal) {
        CotidianaLineSheetDialog(
            cotidianaLineCents = cotidianaLineCents,
            totalLineCents = availableLineCents,
            onDismiss = { showCotidianaLineModal = false },
            onFinance = {
                showCotidianaLineModal = false
                onFinanceClick()
            }
        )
    }

    if (showPayMobileModal) {
        PayMobileRechargeSheetDialog(
            availableCents = cotidianaLineCents,
            onDismiss = { showPayMobileModal = false },
            onRechargeSuccess = { operator, phone, amountCents ->
                showPayMobileModal = false
                Toast.makeText(context, "¡Recarga exitosa! S/ ${amountCents / 100} a $operator $phone", Toast.LENGTH_LONG).show()
                KuotaEvents.bump()
            }
        )
    }

    if (showPayServicesModal) {
        PayServicesSheetDialog(
            availableCents = cotidianaLineCents,
            onDismiss = { showPayServicesModal = false },
            onPaymentSuccess = { serviceName, supplyCode, amountCents ->
                showPayServicesModal = false
                Toast.makeText(context, "¡Pago registrado! S/ ${amountCents / 100} en $serviceName (Sum: $supplyCode)", Toast.LENGTH_LONG).show()
                KuotaEvents.bump()
            }
        )
    }

    if (showCouponsModal) {
        CouponsSheetDialog(
            onDismiss = { showCouponsModal = false },
            onApplyCoupon = { code, discountDesc ->
                showCouponsModal = false
                Toast.makeText(context, "Cupón $code activado: $discountDesc", Toast.LENGTH_LONG).show()
            }
        )
    }

    if (showInviteEarnModal) {
        InviteEarnSheetDialog(
            onDismiss = { showInviteEarnModal = false }
        )
    }

    selectedStoreForQuickView?.let { store ->
        StorePartnerDetailSheetDialog(
            store = store,
            onDismiss = { selectedStoreForQuickView = null },
            onScanQr = {
                selectedStoreForQuickView = null
                onFinanceClick()
            }
        )
    }
}

@Composable
private fun UpcomingPaymentsDateStripCard(
    pendingInstallmentsWithPurchase: List<Pair<PurchaseHistoryItemDto, QuoteInstallmentDto>>,
    onPayInstallment: (PurchaseHistoryItemDto, QuoteInstallmentDto) -> Unit,
    onOpenFullCalendar: () -> Unit,
    onOpenCards: () -> Unit,
    onSimulateQrPurchase: () -> Unit
) {
    val esLocale = remember { Locale.forLanguageTag("es-PE") }
    val dayNameFormatter = remember { DateTimeFormatter.ofPattern("EEE", esLocale) }
    val fullDateFormatter = remember { DateTimeFormatter.ofPattern("EEEE d 'de' MMMM", esLocale) }
    val monthShortFormatter = remember { DateTimeFormatter.ofPattern("MMM", esLocale) }

    val datesToShow = remember(pendingInstallmentsWithPurchase) {
        val dueDates = pendingInstallmentsWithPurchase.mapNotNull { (_, inst) ->
            runCatching { LocalDate.parse(inst.dueAt) }.getOrNull()
        }.distinct().sorted()

        if (dueDates.isNotEmpty()) {
            val result = mutableListOf<LocalDate>()
            result.addAll(dueDates.take(5))
            var cursor = result.last()
            while (result.size < 5) {
                cursor = cursor.plusDays(7)
                if (cursor !in result) result.add(cursor)
            }
            result.sorted()
        } else {
            val today = LocalDate.now()
            (0 until 5).map { offset -> today.plusDays(offset.toLong()) }
        }
    }

    var selectedDateIndex by remember(datesToShow) { mutableIntStateOf(0) }
    val safeIndex = selectedDateIndex.coerceIn(0, (datesToShow.size - 1).coerceAtLeast(0))
    val selectedDate = datesToShow.getOrElse(safeIndex) { LocalDate.now() }

    val installmentsByDate = remember(pendingInstallmentsWithPurchase) {
        pendingInstallmentsWithPurchase.groupBy { (_, inst) ->
            runCatching { LocalDate.parse(inst.dueAt) }.getOrNull()
        }
    }

    val installmentsOnSelectedDate = installmentsByDate[selectedDate].orEmpty()
    val nearestPair = pendingInstallmentsWithPurchase.firstOrNull()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onOpenFullCalendar),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    if (pendingInstallmentsWithPurchase.isNotEmpty()) {
                        "Próximos pagos (${pendingInstallmentsWithPurchase.size})"
                    } else {
                        "Próximos pagos"
                    },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Surface(
                    shape = RoundedCornerShape(50),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.14f),
                    modifier = Modifier.clickable(onClick = onOpenFullCalendar)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            Icons.Default.CalendarMonth,
                            contentDescription = "Abrir calendario completo",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            "Agenda",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // Tira interactiva de cápsulas de fecha con punto indicador de vencimiento
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                datesToShow.forEachIndexed { index, date ->
                    val isSelected = index == safeIndex
                    val hasDueOnDate = !installmentsByDate[date].isNullOrEmpty()
                    val dayNumber = date.dayOfMonth.toString()
                    val dayLabel = date.format(dayNameFormatter).replaceFirstChar { it.uppercase() }.take(3)
                    val monthLabel = date.format(monthShortFormatter).replaceFirstChar { it.uppercase() }.take(3)

                    Column(
                        modifier = Modifier
                            .width(56.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.48f)
                            )
                            .border(
                                width = if (hasDueOnDate && !isSelected) 1.dp else 0.dp,
                                color = if (hasDueOnDate && !isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.65f) else Color.Transparent,
                                shape = RoundedCornerShape(24.dp)
                            )
                            .clickable { selectedDateIndex = index }
                            .padding(vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            monthLabel,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            dayNumber,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            dayLabel,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Box(
                            modifier = Modifier
                                .padding(top = 2.dp)
                                .size(5.dp)
                                .clip(CircleShape)
                                .background(
                                    when {
                                        hasDueOnDate && isSelected -> MaterialTheme.colorScheme.onPrimary
                                        hasDueOnDate -> MaterialTheme.colorScheme.primary
                                        else -> Color.Transparent
                                    }
                                )
                        )
                    }
                }
            }

            // Barra compacta de estado de la fecha seleccionada
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.42f),
                modifier = Modifier.fillMaxWidth()
            ) {
                val formattedDateTitle = selectedDate.format(fullDateFormatter).replaceFirstChar { it.uppercase() }
                if (installmentsOnSelectedDate.isNotEmpty()) {
                    Column(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        installmentsOnSelectedDate.forEach { (purchase, installment) ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        "${purchase.supplierName} · Cuota #${installment.sequenceNumber}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        PurchaseOffer.formatPen(installment.amountCents),
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Button(
                                    onClick = { onPayInstallment(purchase, installment) },
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                                ) {
                                    Text("Pagar", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                } else if (nearestPair != null) {
                    val (nearPurchase, nearInst) = nearestPair
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "$formattedDateTitle · Libre",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                "Próxima: Cuota #${nearInst.sequenceNumber} (${PurchaseOffer.formatPen(nearInst.amountCents)}) · ${nearInst.dueAt}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        TextButton(onClick = { onPayInstallment(nearPurchase, nearInst) }) {
                            Text("Adelantar", fontWeight = FontWeight.Bold, maxLines = 1)
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(onClick = onOpenFullCalendar)
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "$formattedDateTitle · Sin pagos programados",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        StatusPill("Libre")
                    }
                }
            }
        }
    }
}

@Composable
private fun FullInstallmentsCalendarDialog(
    purchases: List<PurchaseHistoryItemDto>,
    onDismiss: () -> Unit,
    onPayInstallment: (PurchaseHistoryItemDto, QuoteInstallmentDto) -> Unit,
    onOpenCards: () -> Unit,
    onOpenQr: () -> Unit
) {
    val allInstallments = remember(purchases) {
        purchases.flatMap { p -> p.installments.map { p to it } }
            .sortedBy { (_, inst) -> inst.dueAt }
    }
    val pendingCount = allInstallments.count { (_, inst) -> inst.status !in setOf("DEMO_PAID", "PAID") }
    val paidCount = allInstallments.size - pendingCount

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Cronograma y calendario Kuota", fontWeight = FontWeight.Bold)
                Text(
                    "$pendingCount pendiente${if (pendingCount == 1) "" else "s"} · $paidCount pagada${if (paidCount == 1) "" else "s"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (allInstallments.isEmpty()) {
                    Text(
                        "Aún no registras cronogramas activos. Realiza una compra con QR en el apartado central para generar tus cuotas semanales.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Button(
                        onClick = onOpenQr,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.QrCodeScanner, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Ir a Comprar con QR")
                    }
                } else {
                    allInstallments.forEach { (purchase, inst) ->
                        val isPaid = inst.status in setOf("DEMO_PAID", "PAID")
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            "${purchase.supplierName} · Cuota #${inst.sequenceNumber}",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            "Vencimiento: ${inst.dueAt}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            PurchaseOffer.formatPen(inst.amountCents),
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = if (isPaid) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary
                                        )
                                        StatusPill(if (isPaid) "Pagada" else "Pendiente")
                                    }
                                }
                                if (!isPaid) {
                                    OutlinedButton(
                                        onClick = { onPayInstallment(purchase, inst) },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Text("Adelantar / Conciliar cuota #${inst.sequenceNumber}")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onOpenCards) {
                Text("Ir a Tarjetas y cuotas")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cerrar")
            }
        }
    )
}

@Composable
private fun BuyTodaySegmentedCard(onOpenQr: () -> Unit) {
    var selectedMode by remember { mutableIntStateOf(0) }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Compra hoy",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    if (selectedMode == 0) "Cámara en local" else "WhatsApp o galería",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(50))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(4.dp)
            ) {
                listOf("En tienda (QR)" to 0, "Compartir QR" to 1).forEach { (label, idx) ->
                    val active = selectedMode == idx
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(50))
                            .background(if (active) MaterialTheme.colorScheme.surface else Color.Transparent)
                            .clickable {
                                selectedMode = idx
                            }
                            .padding(vertical = 9.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            label,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = if (active) FontWeight.Bold else FontWeight.Medium,
                            color = if (active) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                    }
                }
            }

            OutlinedButton(
                onClick = onOpenQr,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.QrCodeScanner, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(
                    if (selectedMode == 0) "Abrir escáner QR" else "Generar o subir QR",
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1
                )
            }
        }
    }
}

private data class HomeSnapshot(
    val ownerName: String,
    val businessName: String,
    val phone: String,
    val claims: List<LocalClaimRecord>,
    val sales: List<ManualSale>,
    val unread: Int
)

private data class QuickAccess(
    val label: String,
    val icon: ImageVector,
    val onClick: () -> Unit,
    val badge: String? = null
)

@Composable
private fun HomeAvatar(name: String) {
    val tint = MaterialTheme.kuotaColors.avatarTint
    val initials = remember(name) {
        val parts = name.trim().split(Regex("\\s+")).filter { it.isNotBlank() }
        when {
            parts.isEmpty() -> ""
            parts.size == 1 -> parts[0].take(1).uppercase()
            else -> (parts[0].take(1) + parts[1].take(1)).uppercase()
        }
    }
    Box(
        modifier = Modifier
            .size(40.dp)
            .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        if (initials.isBlank()) {
            Icon(Icons.Default.Person, contentDescription = null, tint = tint)
        } else {
            Text(initials, color = tint, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
        }
    }
}

@Composable
private fun HomeSectionTitle(
    text: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text,
            modifier = Modifier.weight(1f).semantics { heading() },
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        if (actionLabel != null && onAction != null) {
            TextButton(onClick = onAction) {
                Text(actionLabel, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun QuickAccessCard(access: QuickAccess) {
    Card(
        modifier = Modifier
            .width(104.dp)
            .heightIn(min = 104.dp)
            .clip(RoundedCornerShape(20.dp))
            .kuotaPressable(pressedScale = 0.95f, onClick = access.onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 10.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = .8f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(access.icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
                }
                access.badge?.let { count ->
                    Badge(
                        modifier = Modifier.align(Alignment.TopEnd).offset(x = 6.dp, y = (-4).dp),
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ) { Text(count) }
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(
                access.label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}

@Composable
private fun SuggestionCard(onClick: () -> Unit) {
    val kuotaColors = MaterialTheme.kuotaColors
    val accent = kuotaColors.suggestionAccent
    val title = kuotaColors.suggestionContent
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(Brush.linearGradient(kuotaColors.suggestionGradient))
            .clickable(onClick = onClick)
    ) {
        Row(modifier = Modifier.fillMaxWidth().heightIn(min = 200.dp)) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(start = 20.dp, top = 18.dp, bottom = 18.dp, end = 8.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        "COMPRA Y PAGA EN CUOTAS",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = accent
                    )
                    Text(
                        "Haz crecer tu inventario",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = title
                    )
                    Text(
                        "Escanea el QR del proveedor y elige tu plazo.",
                        style = MaterialTheme.typography.bodySmall,
                        color = title.copy(alpha = .72f)
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Comprar con QR", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = accent)
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = accent, modifier = Modifier.size(18.dp))
                }
            }
            Image(
                painter = painterResource(R.drawable.kuota_onboarding_avatar),
                contentDescription = null,
                modifier = Modifier
                    .width(140.dp)
                    .heightIn(min = 200.dp)
                    .padding(vertical = 8.dp, horizontal = 4.dp),
                contentScale = ContentScale.Fit,
                alignment = Alignment.Center
            )
        }
    }
}

@Composable
private fun ActivityMetric(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun CheckRow(
    title: String,
    subtitle: String,
    done: Boolean,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = !done, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        IconBubble(
            icon,
            container = if (done) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondaryContainer,
            tint = if (done) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary
        )
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Icon(
            if (done) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
            contentDescription = null,
            tint = if (done) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
        )
    }
}

// -------------------------------------------------------------------------
// DIÁLOGOS Y SHEETS MODALES INTERACTIVOS PARA HOME CASHEA STYLE
// -------------------------------------------------------------------------

@Composable
fun LevelExplanationDialog(
    userCreditLevel: com.kouta.app.domain.model.CreditLevel,
    availableLineCents: Long,
    onDismiss: () -> Unit,
    onViewAllLevels: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f))
                .clickable(onClick = onDismiss),
            contentAlignment = Alignment.BottomCenter
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = false) {},
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                color = KuotaColors.White
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .width(40.dp)
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(KuotaColors.Periwinkle.copy(alpha = 0.6f))
                            .align(Alignment.CenterHorizontally)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFFEF9C3)),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painter = painterResource(R.drawable.kuota_mark),
                                    contentDescription = null,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "Nivel ${userCreditLevel.levelNumber} · Kuota",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = KuotaColors.PrussianBlue
                                )
                                Text(
                                    text = "Escala de Crédito para Comerciantes",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = KuotaColors.PrussianBlue.copy(alpha = 0.7f)
                                )
                            }
                        }

                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = KuotaColors.PrussianBlue)
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = KuotaColors.Lavender,
                        border = BorderStroke(1.dp, KuotaColors.Periwinkle.copy(alpha = 0.6f))
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Línea aprobada", style = MaterialTheme.typography.bodyMedium, color = KuotaColors.PrussianBlue)
                                Text(
                                    PurchaseOffer.formatPen(availableLineCents),
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = KuotaColors.PrussianBlue
                                )
                            }
                            HorizontalDivider(color = KuotaColors.Periwinkle.copy(alpha = 0.4f))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Inicial requerida", style = MaterialTheme.typography.bodyMedium, color = KuotaColors.PrussianBlue)
                                Text(
                                    "${userCreditLevel.downPaymentPercentage}% en tiendas aliadas",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = KuotaColors.PersianBlue
                                )
                            }
                            HorizontalDivider(color = KuotaColors.Periwinkle.copy(alpha = 0.4f))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Plazo de pago", style = MaterialTheme.typography.bodyMedium, color = KuotaColors.PrussianBlue)
                                Text(
                                    "Hasta 4 cuotas semanales fijas",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = KuotaColors.PrussianBlue
                                )
                            }
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            "Requisitos para subir al Nivel ${(userCreditLevel.levelNumber + 1).coerceAtMost(6)}:",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = KuotaColors.PrussianBlue
                        )
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = KuotaColors.PersianBlue, modifier = Modifier.size(16.dp))
                            Text("Paga 3 cuotas semanales consecutivas a tiempo", style = MaterialTheme.typography.bodySmall, color = KuotaColors.PrussianBlue)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = KuotaColors.PersianBlue, modifier = Modifier.size(16.dp))
                            Text("Registra al menos S/ 500 en ventas en tu negocio", style = MaterialTheme.typography.bodySmall, color = KuotaColors.PrussianBlue)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = KuotaColors.PersianBlue, modifier = Modifier.size(16.dp))
                            Text("Cero días de mora acumulada", style = MaterialTheme.typography.bodySmall, color = KuotaColors.PrussianBlue)
                        }
                    }

                    Button(
                        onClick = onViewAllLevels,
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(containerColor = KuotaColors.PersianBlue, contentColor = KuotaColors.White)
                    ) {
                        Text("Ver escala de 6 niveles de crédito", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun NearbyStoresSheetDialog(
    onDismiss: () -> Unit,
    onStoreClick: (StorePartner) -> Unit,
    onScanQr: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Todos") }
    val categories = listOf("Todos", "Textil", "Calzado", "Moda", "Abarrotes")

    val filteredStores = remember(searchQuery, selectedCategory) {
        defaultStorePartners.filter { store ->
            val matchesCategory = selectedCategory == "Todos" || store.category.contains(selectedCategory, ignoreCase = true)
            val matchesQuery = searchQuery.isBlank() || store.name.contains(searchQuery, ignoreCase = true) || store.address.contains(searchQuery, ignoreCase = true)
            matchesCategory && matchesQuery
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f))
                .clickable(onClick = onDismiss),
            contentAlignment = Alignment.BottomCenter
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.85f)
                    .clickable(enabled = false) {},
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                color = KuotaColors.White
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .width(40.dp)
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(KuotaColors.Periwinkle.copy(alpha = 0.6f))
                            .align(Alignment.CenterHorizontally)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                "Tiendas y Proveedores Cerca",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = KuotaColors.PrussianBlue
                            )
                            Text(
                                "Compra al crédito con tu Línea Principal o Cotidiana",
                                style = MaterialTheme.typography.bodySmall,
                                color = KuotaColors.PrussianBlue.copy(alpha = 0.7f)
                            )
                        }
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = KuotaColors.PrussianBlue)
                        }
                    }

                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Buscar por nombre, rubro o galería...", color = KuotaColors.PrussianBlue.copy(alpha = 0.5f)) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = KuotaColors.PersianBlue) },
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = KuotaColors.PersianBlue,
                            unfocusedBorderColor = KuotaColors.Periwinkle
                        )
                    )

                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(categories) { cat ->
                            val isSelected = cat == selectedCategory
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = if (isSelected) KuotaColors.PersianBlue else KuotaColors.Lavender,
                                border = BorderStroke(1.dp, if (isSelected) KuotaColors.PersianBlue else KuotaColors.Periwinkle),
                                modifier = Modifier.clickable { selectedCategory = cat }
                            ) {
                                Text(
                                    text = cat,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) KuotaColors.White else KuotaColors.PrussianBlue
                                )
                            }
                        }
                    }

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        filteredStores.forEach { store ->
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onStoreClick(store) },
                                shape = RoundedCornerShape(16.dp),
                                color = KuotaColors.White,
                                border = BorderStroke(1.dp, KuotaColors.Periwinkle.copy(alpha = 0.6f)),
                                shadowElevation = 1.dp
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(52.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(Color(0xFFFEF9C3).copy(alpha = 0.5f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Image(
                                            painter = painterResource(store.logoDrawable),
                                            contentDescription = store.name,
                                            modifier = Modifier.size(36.dp),
                                            contentScale = ContentScale.Fit
                                        )
                                    }

                                    Column(
                                        modifier = Modifier.weight(1f),
                                        verticalArrangement = Arrangement.spacedBy(2.dp)
                                    ) {
                                        Text(store.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = KuotaColors.PrussianBlue)
                                        Text(store.category, style = MaterialTheme.typography.bodySmall, color = KuotaColors.PrussianBlue.copy(alpha = 0.7f))
                                        Text("${store.address} · ${store.distance}", style = MaterialTheme.typography.labelSmall, color = KuotaColors.PersianBlue, fontWeight = FontWeight.SemiBold)
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(50),
                                        color = Color(0xFFFEF08A),
                                        border = BorderStroke(1.dp, Color(0xFFFACC15))
                                    ) {
                                        Text(
                                            text = store.highlight,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                            fontWeight = FontWeight.Bold,
                                            color = KuotaColors.PrussianBlue
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Button(
                        onClick = onScanQr,
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(containerColor = KuotaColors.PersianBlue, contentColor = KuotaColors.White)
                    ) {
                        Icon(Icons.Default.QrCodeScanner, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Escanear QR de compra en tienda", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun HelpSupportSheetDialog(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f))
                .clickable(onClick = onDismiss),
            contentAlignment = Alignment.BottomCenter
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = false) {},
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                color = KuotaColors.White
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .width(40.dp)
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(KuotaColors.Periwinkle.copy(alpha = 0.6f))
                            .align(Alignment.CenterHorizontally)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Centro de Ayuda y Soporte",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = KuotaColors.PrussianBlue
                        )
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = KuotaColors.PrussianBlue)
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = KuotaColors.Lavender,
                        border = BorderStroke(1.dp, KuotaColors.Periwinkle.copy(alpha = 0.6f))
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text("Preguntas Frecuentes", fontWeight = FontWeight.Bold, color = KuotaColors.PrussianBlue)
                            Text(
                                "¿Cómo comprar con Kuota?\n" +
                                "1. Acércate a una tienda aliada o pide por WhatsApp.\n" +
                                "2. Escanea el código QR oficial del comercio.\n" +
                                "3. Elige tu plan en cuotas y confirma.",
                                style = MaterialTheme.typography.bodySmall,
                                color = KuotaColors.PrussianBlue,
                                lineHeight = 18.sp
                            )
                            HorizontalDivider(color = KuotaColors.Periwinkle.copy(alpha = 0.4f))
                            Text(
                                "¿Cómo pagar mis cuotas?\n" +
                                "Puedes pagar directamente desde la sección 'Mis cuotas' o 'Próximos pagos' usando Yape, Plin o transferencia bancaria.",
                                style = MaterialTheme.typography.bodySmall,
                                color = KuotaColors.PrussianBlue,
                                lineHeight = 18.sp
                            )
                        }
                    }

                    Button(
                        onClick = {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/51999999999?text=Hola%20Kuota,%20necesito%20ayuda%20con%20mi%20cuenta"))
                            runCatching { context.startActivity(intent) }
                        },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(containerColor = KuotaColors.PersianBlue, contentColor = KuotaColors.White)
                    ) {
                        Icon(Icons.Default.SupportAgent, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Chatear por WhatsApp con Soporte", fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(50),
                        border = BorderStroke(1.dp, KuotaColors.Periwinkle)
                    ) {
                        Text("Cerrar", color = KuotaColors.PrussianBlue, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun HowToUseLinesSheetDialog(
    onDismiss: () -> Unit,
    onStartShopping: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f))
                .clickable(onClick = onDismiss),
            contentAlignment = Alignment.BottomCenter
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = false) {},
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                color = KuotaColors.White
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .width(40.dp)
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(KuotaColors.Periwinkle.copy(alpha = 0.6f))
                            .align(Alignment.CenterHorizontally)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "¿Cómo uso mis dos líneas?",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = KuotaColors.PrussianBlue
                        )
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = KuotaColors.PrussianBlue)
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFFFEF9C3).copy(alpha = 0.6f),
                        border = BorderStroke(1.dp, Color(0xFFFACC15).copy(alpha = 0.7f))
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                "1. La Línea Principal (70% del cupo)",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = KuotaColors.PrussianBlue
                            )
                            Text(
                                "Para metas grandes y compras mayoristas: stock de temporada, pedidos de telas, fardos y mercadería para tu negocio en hasta 4 cuotas semanales.",
                                style = MaterialTheme.typography.bodySmall,
                                color = KuotaColors.PrussianBlue.copy(alpha = 0.85f),
                                lineHeight = 18.sp
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = KuotaColors.Lavender,
                        border = BorderStroke(1.dp, KuotaColors.Periwinkle.copy(alpha = 0.7f))
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                "2. La Línea Cotidiana (30% del cupo)",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = KuotaColors.PrussianBlue
                            )
                            Text(
                                "Para tu día a día: insumos urgentes, empaques, repuestos, recargas de celular y servicios básicos. Paga desde 1 cuota a 7 o 14 días.",
                                style = MaterialTheme.typography.bodySmall,
                                color = KuotaColors.PrussianBlue.copy(alpha = 0.85f),
                                lineHeight = 18.sp
                            )
                        }
                    }

                    Button(
                        onClick = onStartShopping,
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(containerColor = KuotaColors.PersianBlue, contentColor = KuotaColors.White)
                    ) {
                        Text("¡Entendido, empezar a comprar!", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun WhereToUseLinesSheetDialog(
    onDismiss: () -> Unit,
    onScanQr: () -> Unit,
    onExploreStores: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f))
                .clickable(onClick = onDismiss),
            contentAlignment = Alignment.BottomCenter
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = false) {},
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                color = KuotaColors.White
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .width(40.dp)
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(KuotaColors.Periwinkle.copy(alpha = 0.6f))
                            .align(Alignment.CenterHorizontally)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "¿Dónde uso mis líneas?",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = KuotaColors.PrussianBlue
                        )
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = KuotaColors.PrussianBlue)
                        }
                    }

                    val places = listOf(
                        Triple("Tiendas Físicas Aliadas", "Gamarra, Polvos Azules, Caquetá, Mesa Redonda y más de 250 locales comerciales.", Icons.Default.Storefront),
                        Triple("Pedidos por WhatsApp", "Pide a tus proveedores de confianza y escanea el QR que te compartan.", Icons.Default.PhoneAndroid),
                        Triple("Recargas y Servicios", "Paga la recarga de tu celular y recibos de luz, agua o internet directamente en la app.", Icons.Default.Smartphone)
                    )

                    places.forEach { (title, desc, icon) ->
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = KuotaColors.Lavender,
                            border = BorderStroke(1.dp, KuotaColors.Periwinkle.copy(alpha = 0.6f))
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(CircleShape)
                                        .background(KuotaColors.White),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(icon, contentDescription = null, tint = KuotaColors.PersianBlue, modifier = Modifier.size(20.dp))
                                }
                                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Text(title, fontWeight = FontWeight.Bold, color = KuotaColors.PrussianBlue, style = MaterialTheme.typography.titleSmall)
                                    Text(desc, style = MaterialTheme.typography.bodySmall, color = KuotaColors.PrussianBlue.copy(alpha = 0.75f), lineHeight = 16.sp)
                                }
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = onExploreStores,
                            modifier = Modifier.weight(1f).height(48.dp),
                            shape = RoundedCornerShape(50),
                            border = BorderStroke(1.dp, KuotaColors.PersianBlue)
                        ) {
                            Text("Ver tiendas", color = KuotaColors.PersianBlue, fontWeight = FontWeight.Bold)
                        }
                        Button(
                            onClick = onScanQr,
                            modifier = Modifier.weight(1f).height(48.dp),
                            shape = RoundedCornerShape(50),
                            colors = ButtonDefaults.buttonColors(containerColor = KuotaColors.PersianBlue, contentColor = KuotaColors.White)
                        ) {
                            Text("Escanear QR", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PrincipalLineSheetDialog(
    principalLineCents: Long,
    totalLineCents: Long,
    downPaymentPercentage: Int,
    onDismiss: () -> Unit,
    onFinance: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f))
                .clickable(onClick = onDismiss),
            contentAlignment = Alignment.BottomCenter
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = false) {},
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                color = KuotaColors.White
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .width(40.dp)
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(KuotaColors.Periwinkle.copy(alpha = 0.6f))
                            .align(Alignment.CenterHorizontally)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Línea Principal", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = KuotaColors.PrussianBlue)
                            Text("Para metas y compras mayores", style = MaterialTheme.typography.bodySmall, color = KuotaColors.PrussianBlue.copy(alpha = 0.7f))
                        }
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = KuotaColors.PrussianBlue)
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFFFEF9C3).copy(alpha = 0.5f),
                        border = BorderStroke(1.dp, Color(0xFFFACC15).copy(alpha = 0.7f))
                    ) {
                        Column(
                            modifier = Modifier.padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text("Disponible en Línea Principal", style = MaterialTheme.typography.bodyMedium, color = KuotaColors.PrussianBlue)
                            Text(
                                "S/ ${principalLineCents / 100}.00",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = KuotaColors.PrussianBlue
                            )
                            Text(
                                "De tu línea total de ${PurchaseOffer.formatPen(totalLineCents)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = KuotaColors.PrussianBlue.copy(alpha = 0.7f)
                            )
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Inicial requerida:", style = MaterialTheme.typography.bodyMedium, color = KuotaColors.PrussianBlue)
                            Text("$downPaymentPercentage%", fontWeight = FontWeight.Bold, color = KuotaColors.PersianBlue)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Plazos disponibles:", style = MaterialTheme.typography.bodyMedium, color = KuotaColors.PrussianBlue)
                            Text("Hasta 4 cuotas semanales", fontWeight = FontWeight.Bold, color = KuotaColors.PrussianBlue)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Ideal para:", style = MaterialTheme.typography.bodyMedium, color = KuotaColors.PrussianBlue)
                            Text("Mercadería y stock mayorista", fontWeight = FontWeight.Bold, color = KuotaColors.PrussianBlue)
                        }
                    }

                    Button(
                        onClick = onFinance,
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(containerColor = KuotaColors.PersianBlue, contentColor = KuotaColors.White)
                    ) {
                        Text("Escanear QR de compra", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun CotidianaLineSheetDialog(
    cotidianaLineCents: Long,
    totalLineCents: Long,
    onDismiss: () -> Unit,
    onFinance: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f))
                .clickable(onClick = onDismiss),
            contentAlignment = Alignment.BottomCenter
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = false) {},
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                color = KuotaColors.White
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .width(40.dp)
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(KuotaColors.Periwinkle.copy(alpha = 0.6f))
                            .align(Alignment.CenterHorizontally)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Línea Cotidiana", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = KuotaColors.PrussianBlue)
                            Text("Para tu día a día desde 1 cuota", style = MaterialTheme.typography.bodySmall, color = KuotaColors.PrussianBlue.copy(alpha = 0.7f))
                        }
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = KuotaColors.PrussianBlue)
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = KuotaColors.Lavender,
                        border = BorderStroke(1.dp, KuotaColors.Periwinkle.copy(alpha = 0.7f))
                    ) {
                        Column(
                            modifier = Modifier.padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text("Disponible en Línea Cotidiana", style = MaterialTheme.typography.bodyMedium, color = KuotaColors.PrussianBlue)
                            Text(
                                "S/ ${cotidianaLineCents / 100}.00",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = KuotaColors.PrussianBlue
                            )
                            Text(
                                "De tu línea total de ${PurchaseOffer.formatPen(totalLineCents)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = KuotaColors.PrussianBlue.copy(alpha = 0.7f)
                            )
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Plazo de pago:", style = MaterialTheme.typography.bodyMedium, color = KuotaColors.PrussianBlue)
                            Text("Desde 1 cuota (a 7 o 14 días)", fontWeight = FontWeight.Bold, color = KuotaColors.PersianBlue)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Inicial:", style = MaterialTheme.typography.bodyMedium, color = KuotaColors.PrussianBlue)
                            Text("Mínima o S/ 0 según tienda", fontWeight = FontWeight.Bold, color = KuotaColors.PrussianBlue)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Ideal para:", style = MaterialTheme.typography.bodyMedium, color = KuotaColors.PrussianBlue)
                            Text("Insumos rápidos, recargas y servicios", fontWeight = FontWeight.Bold, color = KuotaColors.PrussianBlue)
                        }
                    }

                    Button(
                        onClick = onFinance,
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(containerColor = KuotaColors.PersianBlue, contentColor = KuotaColors.White)
                    ) {
                        Text("Usar en compra o servicios", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun PayMobileRechargeSheetDialog(
    availableCents: Long,
    onDismiss: () -> Unit,
    onRechargeSuccess: (operator: String, phone: String, amountCents: Long) -> Unit
) {
    var selectedOperator by remember { mutableStateOf("Claro") }
    var phoneNumber by remember { mutableStateOf("") }
    var selectedAmountCents by remember { mutableLongStateOf(1000L) }
    val operators = listOf("Claro", "Movistar", "Entel", "Bitel")
    val amounts = listOf(500L to "S/ 5", 1000L to "S/ 10", 2000L to "S/ 20", 3000L to "S/ 30", 5000L to "S/ 50")

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f))
                .clickable(onClick = onDismiss),
            contentAlignment = Alignment.BottomCenter
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = false) {},
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                color = KuotaColors.White
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .width(40.dp)
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(KuotaColors.Periwinkle.copy(alpha = 0.6f))
                            .align(Alignment.CenterHorizontally)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Pagar Celular / Recargas", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = KuotaColors.PrussianBlue)
                            Text("Financia con tu Línea Cotidiana", style = MaterialTheme.typography.bodySmall, color = KuotaColors.PrussianBlue.copy(alpha = 0.7f))
                        }
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = KuotaColors.PrussianBlue)
                        }
                    }

                    Text("Selecciona tu operador:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = KuotaColors.PrussianBlue)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        operators.forEach { op ->
                            val isSelected = op == selectedOperator
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { selectedOperator = op },
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) KuotaColors.PersianBlue else KuotaColors.Lavender,
                                border = BorderStroke(1.dp, if (isSelected) KuotaColors.PersianBlue else KuotaColors.Periwinkle)
                            ) {
                                Text(
                                    text = op,
                                    modifier = Modifier.padding(vertical = 10.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    color = if (isSelected) KuotaColors.White else KuotaColors.PrussianBlue
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = phoneNumber,
                        onValueChange = { if (it.length <= 9) phoneNumber = it.filter { ch -> ch.isDigit() } },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Número de celular (9 dígitos)") },
                        leadingIcon = { Text("+51", modifier = Modifier.padding(start = 12.dp), fontWeight = FontWeight.Bold, color = KuotaColors.PrussianBlue) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = KuotaColors.PersianBlue,
                            unfocusedBorderColor = KuotaColors.Periwinkle
                        )
                    )

                    Text("Monto a recargar:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = KuotaColors.PrussianBlue)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        amounts.forEach { (cents, label) ->
                            val isSelected = cents == selectedAmountCents
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { selectedAmountCents = cents },
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) Color(0xFFFEF08A) else KuotaColors.Lavender,
                                border = BorderStroke(1.dp, if (isSelected) Color(0xFFFACC15) else KuotaColors.Periwinkle)
                            ) {
                                Text(
                                    text = label,
                                    modifier = Modifier.padding(vertical = 10.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    color = KuotaColors.PrussianBlue
                                )
                            }
                        }
                    }

                    Button(
                        onClick = {
                            if (phoneNumber.length == 9) {
                                onRechargeSuccess(selectedOperator, phoneNumber, selectedAmountCents)
                            }
                        },
                        enabled = phoneNumber.length == 9,
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(containerColor = KuotaColors.PersianBlue, contentColor = KuotaColors.White)
                    ) {
                        Text("Recargar S/ ${selectedAmountCents / 100} en 1 cuota", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun PayServicesSheetDialog(
    availableCents: Long,
    onDismiss: () -> Unit,
    onPaymentSuccess: (serviceName: String, supplyCode: String, amountCents: Long) -> Unit
) {
    var selectedService by remember { mutableStateOf("Luz del Sur") }
    var supplyCode by remember { mutableStateOf("") }
    val services = listOf("Luz del Sur", "Sedapal", "Enel", "Movistar Hogar", "Claro Hogar", "Win Internet")

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f))
                .clickable(onClick = onDismiss),
            contentAlignment = Alignment.BottomCenter
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = false) {},
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                color = KuotaColors.White
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .width(40.dp)
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(KuotaColors.Periwinkle.copy(alpha = 0.6f))
                            .align(Alignment.CenterHorizontally)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Pagar Servicios Básicos", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = KuotaColors.PrussianBlue)
                            Text("Paga tus recibos con tu Línea Cotidiana", style = MaterialTheme.typography.bodySmall, color = KuotaColors.PrussianBlue.copy(alpha = 0.7f))
                        }
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = KuotaColors.PrussianBlue)
                        }
                    }

                    Text("Selecciona la empresa o servicio:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = KuotaColors.PrussianBlue)
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(services) { srv ->
                            val isSelected = srv == selectedService
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = if (isSelected) KuotaColors.PersianBlue else KuotaColors.Lavender,
                                border = BorderStroke(1.dp, if (isSelected) KuotaColors.PersianBlue else KuotaColors.Periwinkle),
                                modifier = Modifier.clickable { selectedService = srv }
                            ) {
                                Text(
                                    text = srv,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) KuotaColors.White else KuotaColors.PrussianBlue
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = supplyCode,
                        onValueChange = { supplyCode = it.filter { ch -> ch.isDigit() } },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Número de suministro / código de cliente") },
                        placeholder = { Text("Ej. 1849204") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = KuotaColors.PersianBlue,
                            unfocusedBorderColor = KuotaColors.Periwinkle
                        )
                    )

                    if (supplyCode.length >= 6) {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = Color(0xFFFEF9C3).copy(alpha = 0.6f),
                            border = BorderStroke(1.dp, Color(0xFFFACC15).copy(alpha = 0.7f))
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Recibo de $selectedService", fontWeight = FontWeight.Bold, color = KuotaColors.PrussianBlue)
                                    Text("Vencimiento: en 4 días", style = MaterialTheme.typography.labelSmall, color = KuotaColors.PrussianBlue.copy(alpha = 0.7f))
                                }
                                Text("S/ 86.40", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, color = KuotaColors.PrussianBlue)
                            }
                        }
                    }

                    Button(
                        onClick = {
                            if (supplyCode.length >= 6) {
                                onPaymentSuccess(selectedService, supplyCode, 8640L)
                            }
                        },
                        enabled = supplyCode.length >= 6,
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(containerColor = KuotaColors.PersianBlue, contentColor = KuotaColors.White)
                    ) {
                        Text(if (supplyCode.length >= 6) "Pagar S/ 86.40 en 1 cuota" else "Ingresa tu número de suministro", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun CouponsSheetDialog(
    onDismiss: () -> Unit,
    onApplyCoupon: (code: String, discountDesc: String) -> Unit
) {
    var couponInput by remember { mutableStateOf("") }
    val coupons = listOf(
        Pair("GAMARRA50", "S/ 50 de descuento en compras de mercadería textil > S/ 300"),
        Pair("INICIALCERO", "0% de inicial en tu primera compra con Línea Cotidiana"),
        Pair("PUNTUAL20", "S/ 20 de saldo a favor al completar 3 cuotas puntuales")
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f))
                .clickable(onClick = onDismiss),
            contentAlignment = Alignment.BottomCenter
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = false) {},
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                color = KuotaColors.White
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .width(40.dp)
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(KuotaColors.Periwinkle.copy(alpha = 0.6f))
                            .align(Alignment.CenterHorizontally)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Canjear Cupones",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = KuotaColors.PrussianBlue
                        )
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = KuotaColors.PrussianBlue)
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = couponInput,
                            onValueChange = { couponInput = it.uppercase() },
                            modifier = Modifier.weight(1f),
                            placeholder = { Text("Código de cupón") },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = KuotaColors.PersianBlue,
                                unfocusedBorderColor = KuotaColors.Periwinkle
                            )
                        )
                        Button(
                            onClick = {
                                if (couponInput.isNotBlank()) {
                                    onApplyCoupon(couponInput, "Descuento activado exitosamente")
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = KuotaColors.PersianBlue, contentColor = KuotaColors.White)
                        ) {
                            Text("Canjear", fontWeight = FontWeight.Bold)
                        }
                    }

                    Text("Cupones disponibles para tu cuenta:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = KuotaColors.PrussianBlue)

                    coupons.forEach { (code, desc) ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onApplyCoupon(code, desc) },
                            shape = RoundedCornerShape(14.dp),
                            color = KuotaColors.Lavender,
                            border = BorderStroke(1.dp, KuotaColors.Periwinkle.copy(alpha = 0.7f))
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = Color(0xFFFEF08A),
                                        border = BorderStroke(1.dp, Color(0xFFFACC15))
                                    ) {
                                        Text(
                                            code,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = KuotaColors.PrussianBlue
                                        )
                                    }
                                    Text(desc, style = MaterialTheme.typography.bodySmall, color = KuotaColors.PrussianBlue)
                                }
                                Text("Aplicar →", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = KuotaColors.PersianBlue)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun InviteEarnSheetDialog(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val referralCode = "KUOTA-JOSE-789"

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f))
                .clickable(onClick = onDismiss),
            contentAlignment = Alignment.BottomCenter
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = false) {},
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                color = KuotaColors.White
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .width(40.dp)
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(KuotaColors.Periwinkle.copy(alpha = 0.6f))
                            .align(Alignment.CenterHorizontally)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Invitar y Ganar",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = KuotaColors.PrussianBlue
                        )
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = KuotaColors.PrussianBlue)
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFFFEF9C3).copy(alpha = 0.7f),
                        border = BorderStroke(1.dp, Color(0xFFFACC15).copy(alpha = 0.7f))
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                "¡Gana S/ 25 en crédito para tu negocio!",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = KuotaColors.PrussianBlue
                            )
                            Text(
                                "Comparte tu código con otros comerciantes y proveedores. Cuando realicen su primera compra al crédito, ambos recibirán S/ 25 de descuento.",
                                style = MaterialTheme.typography.bodySmall,
                                color = KuotaColors.PrussianBlue.copy(alpha = 0.85f),
                                lineHeight = 18.sp
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = KuotaColors.Lavender,
                        border = BorderStroke(1.dp, KuotaColors.Periwinkle)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Tu código de invitación", style = MaterialTheme.typography.labelSmall, color = KuotaColors.PrussianBlue.copy(alpha = 0.7f))
                                Text(referralCode, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, color = KuotaColors.PrussianBlue)
                            }
                            IconButton(onClick = {
                                clipboardManager.setText(AnnotatedString(referralCode))
                                Toast.makeText(context, "Código copiado: $referralCode", Toast.LENGTH_SHORT).show()
                            }) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "Copiar código", tint = KuotaColors.PersianBlue)
                            }
                        }
                    }

                    Button(
                        onClick = {
                            val shareMessage = "¡Hola! Te invito a usar Kuota App para comprar mercadería al crédito en cuotas semanales sin intereses ocultos. Usa mi código $referralCode y llévate S/ 25 de descuento en tu primera compra: https://kuota.pe/app"
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, shareMessage)
                            }
                            context.startActivity(Intent.createChooser(intent, "Compartir invitación Kuota"))
                        },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(containerColor = KuotaColors.PersianBlue, contentColor = KuotaColors.White)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Compartir por WhatsApp", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun StorePartnerDetailSheetDialog(
    store: StorePartner,
    onDismiss: () -> Unit,
    onScanQr: () -> Unit
) {
    val context = LocalContext.current

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f))
                .clickable(onClick = onDismiss),
            contentAlignment = Alignment.BottomCenter
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = false) {},
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                color = KuotaColors.White
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .width(40.dp)
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(KuotaColors.Periwinkle.copy(alpha = 0.6f))
                            .align(Alignment.CenterHorizontally)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFFFEF9C3).copy(alpha = 0.5f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painter = painterResource(store.logoDrawable),
                                    contentDescription = store.name,
                                    modifier = Modifier.size(40.dp),
                                    contentScale = ContentScale.Fit
                                )
                            }
                            Column {
                                Text(store.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = KuotaColors.PrussianBlue)
                                Text(store.category, style = MaterialTheme.typography.bodySmall, color = KuotaColors.PrussianBlue.copy(alpha = 0.7f))
                            }
                        }

                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = KuotaColors.PrussianBlue)
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = KuotaColors.Lavender,
                        border = BorderStroke(1.dp, KuotaColors.Periwinkle.copy(alpha = 0.6f))
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(Icons.Default.LocationOn, contentDescription = null, tint = KuotaColors.PersianBlue, modifier = Modifier.size(16.dp))
                                Text(store.address, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium, color = KuotaColors.PrussianBlue)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(16.dp))
                                Text("4.9 (120+ ventas verificadas con Kuota)", style = MaterialTheme.typography.labelSmall, color = KuotaColors.PrussianBlue.copy(alpha = 0.8f))
                            }
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = KuotaColors.PersianBlue, modifier = Modifier.size(16.dp))
                                Text("Acepta Línea Principal y Línea Cotidiana", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = KuotaColors.PersianBlue)
                            }
                        }
                    }

                    Button(
                        onClick = onScanQr,
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(containerColor = KuotaColors.PersianBlue, contentColor = KuotaColors.White)
                    ) {
                        Icon(Icons.Default.QrCodeScanner, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Escanear QR en ${store.name}", fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/51999999999?text=Hola%20${Uri.encode(store.name)},%20quisiera%20comprar%20al%20crédito%20con%20Kuota"))
                            runCatching { context.startActivity(intent) }
                        },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(50),
                        border = BorderStroke(1.dp, KuotaColors.PersianBlue)
                    ) {
                        Text("Contactar por WhatsApp", color = KuotaColors.PersianBlue, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
