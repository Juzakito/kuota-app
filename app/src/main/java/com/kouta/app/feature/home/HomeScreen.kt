package com.kouta.app.feature.home

import android.Manifest
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.NorthEast
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SouthWest
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kouta.app.R
import com.kouta.app.core.designsystem.components.GroupCard
import com.kouta.app.core.designsystem.components.IconBubble
import com.kouta.app.core.designsystem.components.StatusPill
import com.kouta.app.core.designsystem.motion.kuotaPressable
import com.kouta.app.core.designsystem.motion.kuotaTabSectionReveal
import com.kouta.app.core.designsystem.theme.kuotaColors
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
    val greetingName = ownerName.trim().ifBlank { businessName.trim() }
    val pendingSteps = listOf(hasSession, canNotify, kycSummary != null).count { !it }

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

    val userCreditLevel = remember(revision) {
        val lvl = appSettings.creditLevelNumber
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
    val availableLineCents = remember(userCreditLevel, appSettings.availableCreditLineCents) {
        appSettings.availableCreditLineCents.takeIf { it > 0 } ?: userCreditLevel.maxLineCents
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

    val atmosphericHeaderBrush = Brush.verticalGradient(
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
        // 1. CABECERA AZUL REAL & BALANCE DE FINANCIAMIENTO (HOME-01, HOME-02, Link Bank & Ethan Carter)
        // -------------------------------------------------------------
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(atmosphericHeaderBrush)
                .padding(horizontal = 20.dp)
                .padding(top = 16.dp, bottom = 22.dp)
                .kuotaTabSectionReveal(sectionIndex = 0),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Barra de perfil y acciones de cabecera
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = Color.White.copy(alpha = 0.16f),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.28f)),
                    modifier = Modifier
                        .weight(1f)
                        .clickable(onClick = onOpenProfile)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        HomeAvatar(greetingName)
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(1.dp)) {
                            Text(
                                if (greetingName.isBlank()) "Buenos días 👋" else "Buenos días, $greetingName 👋",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                "Línea respaldada en ventas reales",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.85f),
                                fontWeight = FontWeight.Medium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
                Spacer(Modifier.width(10.dp))
                // Buscador circular
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.16f))
                        .border(1.dp, Color.White.copy(alpha = 0.28f), CircleShape)
                        .clickable(onClick = onActivityClick),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Search, contentDescription = "Buscar ventas", tint = Color.White, modifier = Modifier.size(20.dp))
                }
                Spacer(Modifier.width(8.dp))
                // Notificaciones circular
                BadgedBox(
                    badge = {
                        if (unread > 0) {
                            Badge(containerColor = com.kouta.app.core.designsystem.theme.KuotaCoral, contentColor = Color.White) {
                                Text(if (unread > 9) "9+" else "$unread")
                            }
                        }
                    }
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.16f))
                            .border(1.dp, Color.White.copy(alpha = 0.28f), CircleShape)
                            .clickable(onClick = onNotificationsClick),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Notifications, contentDescription = "Notificaciones", tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                }
            }

            // =========================================================
            // TARJETA PRINCIPAL PROTAGONISTA FUSIONADA (PRD Rediseño UI/UX)
            // =========================================================
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = Color.White.copy(alpha = 0.15f),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.25f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Encabezado: "Puedes financiar hasta" + Nivel actual
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                "Puedes financiar hasta",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                            IconButton(
                                onClick = { showBalance = !showBalance },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    if (showBalance) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = "Ocultar monto",
                                    tint = Color.White.copy(alpha = 0.85f),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        // Badge Nivel actual (Ej. "Nivel 1")
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = Color.White.copy(alpha = 0.22f),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.35f))
                        ) {
                            Text(
                                text = "Nivel ${userCreditLevel.levelNumber}",
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    // Monto gigante protagonista
                    if (showBalance) {
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = integerPart,
                                style = MaterialTheme.typography.headlineLarge.copy(fontSize = 40.sp),
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White,
                                letterSpacing = (-1).sp
                            )
                            if (decimalPart.isNotEmpty()) {
                                Text(
                                    text = decimalPart,
                                    style = MaterialTheme.typography.titleLarge.copy(fontSize = 22.sp),
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White.copy(alpha = 0.8f),
                                    modifier = Modifier.padding(bottom = 4.dp)
                                )
                            }
                        }
                    } else {
                        Text(
                            text = "S/ ••••••••",
                            style = MaterialTheme.typography.headlineLarge.copy(fontSize = 36.sp),
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                    }

                    // Términos del nivel: ("50% inicial", "compras hasta S/ 1400")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = Color.White.copy(alpha = 0.16f)
                        ) {
                            Text(
                                text = "${userCreditLevel.downPaymentPercentage}% inicial",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                        }
                        Text(
                            text = "·",
                            color = Color.White.copy(alpha = 0.6f),
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Compras hasta ${PurchaseOffer.formatPen(maxPurchaseCapacityCents)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.9f),
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // Barra de progreso simple debajo (progreso visual limpio sin texto largo)
                    LinearProgressIndicator(
                        progress = { salesProgressRatio },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(5.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = com.kouta.app.core.designsystem.theme.KuotaEmerald,
                        trackColor = Color.White.copy(alpha = 0.25f)
                    )
                }
            }

            // =========================================================
            // SECCIÓN "PRÓXIMA CUOTA" (PRD Rediseño UI/UX)
            // =========================================================
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = Color.White.copy(alpha = 0.15f),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.22f))
            ) {
                if (nextUpcomingPair != null) {
                    val (activePurchase, nextInstallment) = nextUpcomingPair
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    Icons.Default.CalendarMonth,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    "Próxima cuota",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(com.kouta.app.core.designsystem.theme.KuotaCoral.copy(alpha = 0.35f))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    "Vence: ${nextInstallment.dueAt}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(
                                modifier = Modifier.weight(1f, fill = false),
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Text(
                                    "${activePurchase.supplierName} · Cuota #${nextInstallment.sequenceNumber}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.9f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                // Monto en tamaño reducido (fontSize = 19.sp, titleLarge)
                                Text(
                                    if (showBalance) PurchaseOffer.formatPen(nextInstallment.amountCents) else "S/ ••••••",
                                    style = MaterialTheme.typography.titleLarge.copy(fontSize = 19.sp),
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }

                            Spacer(Modifier.width(12.dp))

                            // Botón limpio sin quiebres de texto
                            Button(
                                onClick = { wizardTargetPair = activePurchase to nextInstallment },
                                shape = RoundedCornerShape(50),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.White,
                                    contentColor = Color(0xFF1E40AF)
                                ),
                                contentPadding = PaddingValues(horizontal = 18.dp, vertical = 8.dp),
                                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                            ) {
                                Text(
                                    text = "Pagar ahora",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.labelMedium,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }
                    }
                } else {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = com.kouta.app.core.designsystem.theme.KuotaEmerald,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                "No tienes cuotas pendientes",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        Text(
                            "Tu línea de crédito está al 100% disponible. Puedes realizar compras a crédito en cualquier proveedor aliado.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(onClick = onFinanceClick),
                            shape = RoundedCornerShape(12.dp),
                            color = Color.White.copy(alpha = 0.18f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Explorar proveedores aliados →", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = Color.White)
                                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(14.dp))

        // -------------------------------------------------------------
        // 2. ACCIONES RÁPIDAS: FILA DE CUATRO (HOME-04, Vaulta Style)
        // -------------------------------------------------------------
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .kuotaTabSectionReveal(sectionIndex = 1),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            val quickActions = listOf(
                Triple("Kuotear", Icons.Default.Storefront) { onFinanceClick() },
                Triple("Cobrar", Icons.Default.Link) {
                    TradeScreenNavState.openLink()
                    onFinanceClick()
                },
                Triple("Mis cuotas", Icons.Default.CalendarMonth) { onCardsClick() },
                Triple("Proveedores", Icons.Default.Store) { onFinanceClick() }
            )

            quickActions.forEach { (label, icon, onClick) ->
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable(onClick = onClick),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp, horizontal = 4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(icon, contentDescription = label, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                        }
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            maxLines = 1
                        )
                    }
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
                            .background(Color(0xFFE0F2FE))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(Icons.Default.ElectricBolt, contentDescription = null, tint = Color(0xFF0284C7), modifier = Modifier.size(14.dp))
                            Text(
                                "CONSEJO DE KUOTA",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0284C7)
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
                        color = if (isSelected) Color(0xFF0F172A) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.clickable { transactionTab = idx }
                    ) {
                        Text(
                            text = label,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
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
