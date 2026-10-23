package com.kouta.app.feature.payments

import com.kouta.app.data.firebase.KuotaFirestoreService
import com.kouta.app.data.local.AppSettings

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.kouta.app.core.analytics.KuotaAnalytics
import com.kouta.app.core.analytics.KuotaAnalyticsEvent
import com.kouta.app.core.designsystem.components.KuotaAnimatedFeedback
import com.kouta.app.core.designsystem.components.KuotaFeedbackKind
import com.kouta.app.core.designsystem.components.KuotaLoadingRow
import com.kouta.app.core.designsystem.components.StatusPill
import com.kouta.app.core.designsystem.motion.KuotaMotion
import com.kouta.app.core.designsystem.motion.kuotaPressable
import com.kouta.app.core.designsystem.motion.kuotaTabSectionReveal
import com.kouta.app.core.designsystem.motion.rememberSystemAnimatorScale
import com.kouta.app.core.designsystem.theme.kuotaColors
import com.kouta.app.data.local.SalesJournalStore
import com.kouta.app.data.remote.ApiOutcome
import com.kouta.app.data.remote.CreatePaymentIntentBody
import com.kouta.app.data.remote.DemoPaymentEventBody
import com.kouta.app.data.remote.KuotaApiClient
import com.kouta.app.data.remote.PaymentIntentDto
import com.kouta.app.data.remote.PurchaseHistoryItemDto
import com.kouta.app.data.remote.QuoteInstallmentDto
import com.kouta.app.feature.account.AccountLinkDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale
import java.util.UUID

private data class PaymentMethod(
    val name: String,
    val detail: String,
    val operationalBadge: String,
    val requiresManualConciliation: Boolean,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

private val paymentMethods = listOf(
    PaymentMethod(
        name = "Yape / Plin",
        detail = "Transferencia móvil directa por QR/número · La MYPE valida el comprobante",
        operationalBadge = "Sin cobro automático",
        requiresManualConciliation = false,
        icon = Icons.Default.AccountBalanceWallet
    ),
    PaymentMethod(
        name = "Transferencia BCP",
        detail = "Depósito o transferencia directa a cuenta corriente BCP",
        operationalBadge = "Conciliación manual",
        requiresManualConciliation = true,
        icon = Icons.Default.AccountBalance
    ),
    PaymentMethod(
        name = "BBVA / Interbank",
        detail = "Transferencia interbancaria CCI sujeta a horario de compensación bancaria",
        operationalBadge = "Conciliación manual",
        requiresManualConciliation = true,
        icon = Icons.Default.AccountBalance
    ),
    PaymentMethod(
        name = "Tarjeta de débito",
        detail = "Pasarela tokenizada en integración · Modo demostración sin almacenamiento de CVV",
        operationalBadge = "Modo demo",
        requiresManualConciliation = false,
        icon = Icons.Default.CreditCard
    )
)

@Composable
fun PaymentMethodsScreen(onOpenFinancing: () -> Unit = {}) {
    val context = LocalContext.current
    val store = remember(context) { SalesJournalStore(context) }
    val scope = rememberCoroutineScope()
    val kuotaColors = MaterialTheme.kuotaColors
    val selectionDurationMs = KuotaMotion.resolveDurationMs(200, rememberSystemAnimatorScale()).coerceAtLeast(60)

    // Categoría seleccionada (inicia siempre en Clásica, la más básica)
    var selectedCardIdName by rememberSaveable { mutableStateOf(KuotaCardId.CLASSIC.name) }
    val selectedCardId = remember(selectedCardIdName) {
        runCatching { KuotaCardId.valueOf(selectedCardIdName) }.getOrDefault(KuotaCardId.CLASSIC)
    }
    val selectedCardTier = remember(selectedCardId) {
        KuotaCardCatalog.firstOrNull { it.id == selectedCardId } ?: KuotaCardCatalog.first()
    }

    // Estados de modales minimalistas (evitan saturar la pantalla principal con texto)
    var showCardFullDetail by rememberSaveable { mutableStateOf(false) }
    var showCardSpecsSheet by remember { mutableStateOf(false) }
    var showLimitsSheet by remember { mutableStateOf(false) }
    var showPaymentMethodPicker by remember { mutableStateOf(false) }
    var showLearnMoreDialog by remember { mutableStateOf(false) }
    var isCardDemoPaused by rememberSaveable { mutableStateOf(false) }

    val activity = context as? android.app.Activity
    androidx.compose.runtime.LaunchedEffect(showCardFullDetail, showLimitsSheet) {
        if (showCardFullDetail || showLimitsSheet) {
            com.kouta.app.core.security.ScreenSecurityManager.enterSensitiveScreen(activity)
        } else {
            com.kouta.app.core.security.ScreenSecurityManager.exitSensitiveScreen(
                activity,
                com.kouta.app.core.security.PinSecurityManager.isLocked(context)
            )
        }
    }

    // Medio de pago preferido (independiente de la tarjeta seleccionada)
    var selectedMethod by remember { mutableStateOf(paymentMethods.first().name) }
    var pendingSelectedMethod by remember { mutableStateOf(paymentMethods.first().name) }
    var isSavingPaymentMethod by remember { mutableStateOf(false) }
    var paymentMethodSaveError by remember { mutableStateOf<String?>(null) }
    var businessName by remember { mutableStateOf("") }
    var ownerName by remember { mutableStateOf("") }

    var hasSession by remember { mutableStateOf(KuotaApiClient.hasSession(context)) }
    var showLinkDialog by remember { mutableStateOf(false) }
    var purchases by remember { mutableStateOf<List<PurchaseHistoryItemDto>>(emptyList()) }
    var isLoadingPurchases by remember { mutableStateOf(false) }
    var activeInstallmentActionId by remember { mutableStateOf<String?>(null) }
    var wizardSupplierName by remember { mutableStateOf("") }
    var wizardInstallments by remember { mutableStateOf<List<QuoteInstallmentDto>>(emptyList()) }
    var isSubmittingWizard by remember { mutableStateOf(false) }
    var feedbackMessage by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    fun loadPurchases() {
        hasSession = KuotaApiClient.hasSession(context)
        if (!hasSession) {
            purchases = emptyList()
            return
        }
        scope.launch {
            isLoadingPurchases = true
            errorMessage = null
            when (val outcome = withContext(Dispatchers.IO) { KuotaApiClient.call(context) { listPurchases() } }) {
                is ApiOutcome.Ok -> {
                    purchases = outcome.value
                    if (outcome.value.isNotEmpty()) {
                        KuotaAnalytics.track(
                            KuotaAnalyticsEvent.INSTALLMENT_VIEWED,
                            mapOf("status" to "LOADED", "demo_only" to "true")
                        )
                    }
                }
                ApiOutcome.SessionExpired -> {
                    hasSession = false
                    purchases = emptyList()
                }
                is ApiOutcome.Rejected -> errorMessage = outcome.message
                is ApiOutcome.Unreachable -> errorMessage = "No se pudo conectar al servidor (${outcome.serverUrl})."
            }
            isLoadingPurchases = false
        }
    }

    LaunchedEffect(Unit) {
        val profile = withContext(Dispatchers.IO) {
            runCatching { store.readBusinessProfile() }.getOrNull()
        }
        if (profile != null) {
            selectedMethod = profile.preferredPaymentMethod
            pendingSelectedMethod = profile.preferredPaymentMethod
            businessName = profile.businessName
            ownerName = profile.ownerName
        }
        loadPurchases()
    }

    fun selectMethod(method: String) {
        if (method == selectedMethod) return
        selectedMethod = method
        scope.launch {
            withContext(Dispatchers.IO) {
                runCatching { store.updatePreferredPaymentMethod(method) }
            }
        }
    }

    fun initiateInstallmentPayment(installment: QuoteInstallmentDto) {
        if (activeInstallmentActionId != null) return
        activeInstallmentActionId = installment.installmentId
        feedbackMessage = null
        errorMessage = null
        val idempotencyKey = "pay-intent-${installment.installmentId}-${UUID.randomUUID()}"
        scope.launch {
            KuotaAnalytics.track(
                KuotaAnalyticsEvent.PAYMENT_ATTEMPT_CREATED,
                mapOf(
                    "installment_sequence" to installment.sequenceNumber.toString(),
                    "demo_only" to "true"
                )
            )
            when (
                val outcome = withContext(Dispatchers.IO) {
                    KuotaApiClient.call(context) {
                        createPaymentIntent(
                            installmentId = installment.installmentId,
                            idempotencyKey = idempotencyKey,
                            body = CreatePaymentIntentBody("DEMO_MANUAL")
                        )
                    }
                }
            ) {
                is ApiOutcome.Ok -> {
                    val payment = outcome.value
                    feedbackMessage = "Intento creado (${payment.paymentId.takeLast(6)})."
                    payment.nextActionUrl?.takeIf { it.startsWith("https://") || it.startsWith("http://") }?.let { url ->
                        runCatching {
                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                        }
                    }
                    loadPurchases()
                }
                ApiOutcome.SessionExpired -> {
                    hasSession = false
                    showLinkDialog = true
                }
                is ApiOutcome.Rejected -> errorMessage = outcome.message
                is ApiOutcome.Unreachable -> errorMessage = "Sin conexión al servidor."
            }
            activeInstallmentActionId = null
        }
    }

    fun triggerDemoPaymentEvent(payment: PaymentIntentDto, eventType: String) {
        if (activeInstallmentActionId != null) return
        activeInstallmentActionId = payment.installmentId
        feedbackMessage = null
        errorMessage = null
        val idempotencyKey = "pay-event-${payment.paymentId}-$eventType-${UUID.randomUUID()}"
        scope.launch {
            when (
                val outcome = withContext(Dispatchers.IO) {
                    KuotaApiClient.call(context) {
                        applyDemoPaymentEvent(
                            paymentId = payment.paymentId,
                            idempotencyKey = idempotencyKey,
                            body = DemoPaymentEventBody(eventType)
                        )
                    }
                }
            ) {
                is ApiOutcome.Ok -> {
                    val updated = outcome.value
                    feedbackMessage = "Estado actualizado: ${updated.status}"
                    loadPurchases()
                }
                ApiOutcome.SessionExpired -> {
                    hasSession = false
                    showLinkDialog = true
                }
                is ApiOutcome.Rejected -> errorMessage = outcome.message
                is ApiOutcome.Unreachable -> errorMessage = "Sin conexión al servidor."
            }
            activeInstallmentActionId = null
        }
    }

    fun payInstallmentsWithWizard(
        installmentsToPay: List<QuoteInstallmentDto>,
        channelId: String,
        originBank: String,
        referenceNumber: String
    ) {
        if (isSubmittingWizard || installmentsToPay.isEmpty()) return
        scope.launch {
            isSubmittingWizard = true
            feedbackMessage = null
            errorMessage = null
            var paidCount = 0
            var totalPaidCents = 0L
            for (installment in installmentsToPay) {
                activeInstallmentActionId = installment.installmentId
                val outcome = withContext(Dispatchers.IO) {
                    val createOutcome = KuotaApiClient.call(context) {
                        createPaymentIntent(
                            installmentId = installment.installmentId,
                            idempotencyKey = "wizard-cards-pay-${installment.installmentId}-${UUID.randomUUID()}",
                            body = CreatePaymentIntentBody(paymentMethodId = channelId)
                        )
                    }
                    when (createOutcome) {
                        is ApiOutcome.Ok -> {
                            KuotaApiClient.call(context) {
                                applyDemoPaymentEvent(
                                    paymentId = createOutcome.value.paymentId,
                                    idempotencyKey = "wizard-cards-settle-${createOutcome.value.paymentId}-${UUID.randomUUID()}",
                                    body = DemoPaymentEventBody(eventType = "SUCCEED")
                                )
                            }
                        }
                        else -> createOutcome
                    }
                }
                when (outcome) {
                    is ApiOutcome.Ok -> {
                        paidCount += 1
                        totalPaidCents += installment.amountCents
                    }
                    ApiOutcome.SessionExpired -> {
                        hasSession = false
                        showLinkDialog = true
                    }
                    is ApiOutcome.Rejected -> errorMessage = outcome.message
                    is ApiOutcome.Unreachable -> errorMessage = "Sin conexión al servidor."
                }
            }
            activeInstallmentActionId = null
            isSubmittingWizard = false
            if (paidCount > 0) {
                feedbackMessage = "$paidCount cuota(s) pagada(s) · ${formatCentsPen(totalPaidCents)}"
                loadPurchases()
            }
        }
    }

    val paidCountTotal = remember(purchases) {
        purchases.sumOf { p ->
            p.installments.count { it.status in setOf("DEMO_PAID", "PAID") }
        }
    }
    val unpaidCountTotal = remember(purchases) {
        purchases.sumOf { p ->
            p.installments.count { it.status !in setOf("DEMO_PAID", "PAID") }
        }
    }
    val usedCreditCents = remember(purchases) {
        purchases.sumOf { p ->
            p.installments
                .filter { it.status !in setOf("DEMO_PAID", "PAID") }
                .sumOf { it.amountCents }
        }
    }
    val currentUnlockedTierId = remember(paidCountTotal) {
        resolveCurrentDemoTierId(paidCountTotal)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Encabezado minimalista idéntico a Lemon Cash ("Tarjetas" + píldora "+ Niveles" en una sola línea)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Tarjetas",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold
            )
            Surface(
                shape = RoundedCornerShape(50),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .clickable { showCardFullDetail = true }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 9.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        "Niveles",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }
        }

        // 2. Selector compacto de 4 niveles (1 sola palabra por nivel, sin truncamientos)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(50))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f))
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            KuotaCardCatalog.forEach { tier ->
                val isSelected = tier.id == selectedCardId
                val shortLabel = when (tier.id) {
                    KuotaCardId.CLASSIC -> "Clásica"
                    KuotaCardId.GOLD -> "Oro"
                    KuotaCardId.PLATINUM -> "Platino"
                    KuotaCardId.BLACK_INFINITE -> "Black"
                }
                val segmentBg by animateColorAsState(
                    targetValue = if (isSelected) MaterialTheme.colorScheme.surface else Color.Transparent,
                    animationSpec = tween(durationMillis = selectionDurationMs, easing = KuotaMotion.StandardEasing),
                    label = "tierSegmentBg_${tier.id}"
                )
                val segmentText by animateColorAsState(
                    targetValue = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                    animationSpec = tween(durationMillis = selectionDurationMs),
                    label = "tierSegmentText_${tier.id}"
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(50))
                        .background(segmentBg)
                        .kuotaPressable(
                            pressedScale = 0.96f,
                            hapticOnTap = !isSelected,
                            role = Role.Tab
                        ) { selectedCardIdName = tier.id.name }
                        .semantics { selected = isSelected }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        shortLabel,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = segmentText,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }
        }

        // 3. Tarjeta Principal (Idéntica a la tarjeta blanca superior de la 1.ª captura de Lemon Cash)
        // Crossfade breve al cambiar de nivel: solo cambia la presentación, nunca el estado de una operación.
        AnimatedContent(
            targetState = selectedCardTier,
            modifier = Modifier.kuotaTabSectionReveal(sectionIndex = 1),
            transitionSpec = {
                fadeIn(tween(selectionDurationMs + 40, easing = KuotaMotion.DecelerateEasing)) togetherWith
                    fadeOut(tween(selectionDurationMs - 40, easing = KuotaMotion.AccelerateEasing))
            },
            label = "lemonMainCardTier"
        ) { animatedTier ->
            LemonMainCardContainer(
                tier = animatedTier,
                isCurrentUserTier = animatedTier.id == currentUnlockedTierId,
                isPaused = isCardDemoPaused,
                selectedPaymentMethod = selectedMethod,
                onTogglePause = {
                    isCardDemoPaused = !isCardDemoPaused
                    Toast.makeText(
                        context,
                        if (isCardDemoPaused) "Línea pausada (demo)" else "Línea activa (demo)",
                        Toast.LENGTH_SHORT
                    ).show()
                },
                onOpenCardDetail = { showCardFullDetail = true },
                onOpenPaymentMethodPicker = { showPaymentMethodPicker = true }
            )
        }

        // 4. Dos tarjetas cuadradas limpias lado a lado (sin textos cortados con "...")
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .kuotaTabSectionReveal(sectionIndex = 2),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Cuadro izquierdo: Límite de categoría
            Card(
                modifier = Modifier
                    .weight(1f)
                    .height(136.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .kuotaPressable { showLimitsSheet = true },
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Límite de línea",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Ver límites",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        val maxWholePen = String.format(Locale.US, "%,d", selectedCardTier.maxCreditCents / 100L)
                        Text(
                            buildAnnotatedString {
                                withStyle(SpanStyle(color = selectedCardTier.accentColor, fontWeight = FontWeight.Bold)) {
                                    append("S/ ")
                                }
                                withStyle(SpanStyle(color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.ExtraBold)) {
                                    append(maxWholePen)
                                }
                            },
                            style = MaterialTheme.typography.titleLarge,
                            maxLines = 1,
                            softWrap = false
                        )
                        Text(
                            "Inicial ${selectedCardTier.downPaymentPercent}% QR",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }
            }

            // Cuadro derecho: Consumo / Cuotas
            Card(
                modifier = Modifier
                    .weight(1f)
                    .height(136.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .kuotaPressable {
                        if (purchases.isEmpty()) onOpenFinancing() else showLimitsSheet = true
                    },
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Mis cuotas",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Ver cuotas",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        val penAmountOnly = String.format(Locale.US, "%,.2f", usedCreditCents / 100.0)
                        Text(
                            buildAnnotatedString {
                                withStyle(SpanStyle(color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)) {
                                    append("S/ ")
                                }
                                withStyle(SpanStyle(color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.ExtraBold)) {
                                    append(penAmountOnly)
                                }
                            },
                            style = MaterialTheme.typography.titleLarge,
                            maxLines = 1,
                            softWrap = false
                        )
                        Text(
                            if (unpaidCountTotal > 0) "$unpaidCountTotal pendiente(s)" else "$paidCountTotal pagadas",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }
            }
        }

        // 5. Enlace limpio "Saber más sobre tarjetas ↗" (Idéntico al pie de la 1.ª captura de Lemon Cash)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .clickable { showLearnMoreDialog = true }
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Saber más sobre tarjetas",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.width(6.dp))
            Icon(
                Icons.AutoMirrored.Filled.OpenInNew,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.onSurface
            )
        }

        // 6. Sección de cuotas activas (solo despliega detalle cuando existen compras o mensajes)
        // Los banners aparecen únicamente tras la respuesta real del servidor.
        KuotaAnimatedFeedback(message = feedbackMessage, kind = KuotaFeedbackKind.SUCCESS)
        KuotaAnimatedFeedback(message = errorMessage, kind = KuotaFeedbackKind.ERROR)

        KuotaLoadingRow(visible = isLoadingPurchases, label = "Consultando tus cuotas…")

        if (isLoadingPurchases) {
            // El indicador animado se muestra arriba; no se renderizan datos parciales.
        } else if (!hasSession) {
            OutlinedButton(
                onClick = { showLinkDialog = true },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(50)
            ) {
                Text("Vincular celular para ver cuotas", fontWeight = FontWeight.SemiBold)
            }
        } else if (purchases.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Próximas cuotas",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = { loadPurchases() }) {
                    Icon(Icons.Default.Refresh, contentDescription = "Actualizar cuotas", modifier = Modifier.size(20.dp))
                }
            }
            purchases.forEach { purchase ->
                ActivePurchaseInstallmentsCard(
                    purchase = purchase,
                    activeActionInstallmentId = activeInstallmentActionId,
                    onInitiatePayment = { initiateInstallmentPayment(it) },
                    onLaunchWizard = { supplier, selected ->
                        wizardSupplierName = supplier
                        wizardInstallments = selected
                    },
                    onTriggerDemoEvent = { payment, eventType -> triggerDemoPaymentEvent(payment, eventType) }
                )
            }
        }
    }

    // Vista de Tarjeta a Pantalla Completa (Idéntica en limpieza a la 2.ª captura de Lemon Cash)
    if (showCardFullDetail) {
        LemonCleanCardDetailScreen(
            selectedTier = selectedCardTier,
            isPaused = isCardDemoPaused,
            selectedPaymentMethod = selectedMethod,
            onSelectTier = { tier -> selectedCardIdName = tier.id.name },
            onTogglePause = { isCardDemoPaused = !isCardDemoPaused },
            onOpenSpecs = { showCardSpecsSheet = true },
            onOpenLimits = { showLimitsSheet = true },
            onOpenPaymentMethodPicker = { showPaymentMethodPicker = true },
            onUseWithQr = {
                showCardFullDetail = false
                onOpenFinancing()
            },
            onDismiss = { showCardFullDetail = false }
        )
    }

    // Modal ligero para "Ver datos"
    if (showCardSpecsSheet) {
        AlertDialog(
            onDismissRequest = { showCardSpecsSheet = false },
            title = { Text(selectedCardTier.fullName, fontWeight = FontWeight.ExtraBold) },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        selectedCardTier.subtitle,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    HorizontalDivider()
                    TierDetailRow(label = "Rango de línea", value = selectedCardTier.creditRangeLabel)
                    TierDetailRow(label = "Inicial QR", value = "${selectedCardTier.downPaymentPercent}%")
                    TierDetailRow(label = "Ventas sugeridas", value = selectedCardTier.minMonthlySalesLabel)
                    TierDetailRow(label = "Membresía", value = selectedCardTier.annualFeeLabel)
                    TierDetailRow(label = "Estado", value = selectedCardTier.productStatusLabel)
                    HorizontalDivider()
                    selectedCardTier.benefits.forEach { benefit ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(
                                Icons.Default.Verified,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .size(16.dp)
                                    .padding(top = 2.dp)
                            )
                            Text(benefit, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showCardSpecsSheet = false }) {
                    Text("Cerrar")
                }
            }
        )
    }

    // Modal informativo de "Límites"
    if (showLimitsSheet) {
        val availableDemoCents = (selectedCardTier.maxCreditCents - usedCreditCents).coerceAtLeast(0L)
        val utilizationRatio = if (selectedCardTier.maxCreditCents > 0L) {
            (usedCreditCents.toFloat() / selectedCardTier.maxCreditCents.toFloat()).coerceIn(0f, 1f)
        } else {
            0f
        }
        AlertDialog(
            onDismissRequest = { showLimitsSheet = false },
            title = {
                Column {
                    Text(
                        "Límites · ${selectedCardTier.categoryName}",
                        fontWeight = FontWeight.ExtraBold,
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        "Nivel: ${selectedCardTier.fullName}",
                        style = MaterialTheme.typography.bodySmall,
                        color = selectedCardTier.accentColor,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        selectedCardTier.creditRangeLabel,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    LinearProgressIndicator(
                        progress = { utilizationRatio.coerceAtLeast(0.06f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = selectedCardTier.accentColor,
                        trackColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                    )

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            TierDetailRow(label = "Límite del producto", value = formatCentsPen(selectedCardTier.maxCreditCents))
                            TierDetailRow(label = "Límite individual", value = "En evaluación (Demo inicial)")
                            TierDetailRow(label = "Consumo contabilizado", value = formatCentsPen(usedCreditCents))
                            TierDetailRow(label = "Disponible de referencia", value = formatCentsPen(availableDemoCents))
                            TierDetailRow(label = "Cuotas pagadas a tiempo", value = "$paidCountTotal cuota(s)")
                            TierDetailRow(
                                label = "Requisito de nivel",
                                value = if (paidCountTotal >= selectedCardTier.requiredPaidInstallments) {
                                    "Completado"
                                } else {
                                    "Requiere ${selectedCardTier.requiredPaidInstallments} cuota(s)"
                                }
                            )
                            TierDetailRow(label = "Política de inicial", value = selectedCardTier.downPaymentPolicyLabel)
                            TierDetailRow(label = "Moneda contable", value = "Soles (PEN) · Céntimos exactos")
                        }
                    }

                    Text(
                        text = "Los importes mostrados son referenciales. El saldo disponible se calcula con precisión entera en céntimos deduciendo operaciones activas sin duplicidades.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showLimitsSheet = false }) {
                    Text("Entendido", fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    // Selector transaccional de medio de pago preferido
    if (showPaymentMethodPicker) {
        AlertDialog(
            onDismissRequest = {
                if (!isSavingPaymentMethod) {
                    pendingSelectedMethod = selectedMethod
                    paymentMethodSaveError = null
                    showPaymentMethodPicker = false
                }
            },
            title = {
                Column {
                    Text("Medio de pago preferido", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
                    Text(
                        "Selecciona la vía principal para gestionar cobros y cuotas MYPE",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    paymentMethods.forEach { method ->
                        val isMethodSelected = pendingSelectedMethod == method.name
                        val rowBg by animateColorAsState(
                            targetValue = if (isMethodSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else Color.Transparent,
                            animationSpec = tween(durationMillis = selectionDurationMs, easing = KuotaMotion.StandardEasing),
                            label = "methodRowBg_${method.name}"
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(rowBg)
                                .border(
                                    width = if (isMethodSelected) 1.dp else 0.dp,
                                    color = if (isMethodSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f) else Color.Transparent,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable(enabled = !isSavingPaymentMethod) {
                                    pendingSelectedMethod = method.name
                                    paymentMethodSaveError = null
                                }
                                .padding(vertical = 10.dp, horizontal = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            RadioButton(
                                selected = pendingSelectedMethod == method.name,
                                onClick = {
                                    if (!isSavingPaymentMethod) {
                                        pendingSelectedMethod = method.name
                                        paymentMethodSaveError = null
                                    }
                                }
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        method.name,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = when {
                                            method.requiresManualConciliation -> Color(0xFFF5B700).copy(alpha = 0.15f)
                                            method.name.contains("demo", ignoreCase = true) -> Color(0xFF00C2FF).copy(alpha = 0.15f)
                                            else -> MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                        }
                                    ) {
                                        Text(
                                            text = method.operationalBadge,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = when {
                                                method.requiresManualConciliation -> Color(0xFFF5B700)
                                                method.name.contains("demo", ignoreCase = true) -> Color(0xFF00C2FF)
                                                else -> MaterialTheme.colorScheme.primary
                                            },
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                                Text(
                                    method.detail,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    paymentMethodSaveError?.let { err ->
                        Text(
                            text = err,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        pendingSelectedMethod = selectedMethod
                        paymentMethodSaveError = null
                        showPaymentMethodPicker = false
                    },
                    enabled = !isSavingPaymentMethod
                ) {
                    Text("Cancelar")
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        isSavingPaymentMethod = true
                        paymentMethodSaveError = null
                        scope.launch {
                            val success = withContext(Dispatchers.IO) {
                                runCatching {
                                    store.updatePreferredPaymentMethod(pendingSelectedMethod)
                                    val settings = AppSettings(context)
                                    settings.preferredPaymentMethod = pendingSelectedMethod
                                    settings.preferredPaymentMethodUpdatedAt = System.currentTimeMillis()
                                    KuotaFirestoreService.updatePreferredPaymentMethod(context, pendingSelectedMethod)
                                    true
                                }.getOrDefault(false)
                            }
                            isSavingPaymentMethod = false
                            if (success) {
                                selectedMethod = pendingSelectedMethod
                                showPaymentMethodPicker = false
                                Toast.makeText(
                                    context,
                                    "Preferencia guardada: $pendingSelectedMethod",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                paymentMethodSaveError = "No se pudo registrar la preferencia. Intenta nuevamente."
                            }
                        }
                    },
                    enabled = !isSavingPaymentMethod,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    if (isSavingPaymentMethod) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Guardando...")
                    } else {
                        Text("Listo", fontWeight = FontWeight.Bold)
                    }
                }
            }
        )
    }

    // Modal informativo "Saber más sobre tarjetas"
    if (showLearnMoreDialog) {
        AlertDialog(
            onDismissRequest = { showLearnMoreDialog = false },
            title = { Text("Niveles de tarjetas KUOTA", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    KuotaCardCatalog.forEach { tier ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(tier.fullName, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                                Text("Inicial ${tier.downPaymentPercent}% QR", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Text(tier.creditRangeLabel, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                        }
                        HorizontalDivider()
                    }
                    Text(
                        "Kuota no emite tarjetas bancarias ni guarda CVV. Las tarjetas representan tu categoría referencial de financiamiento B2B en modo demostración.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showLearnMoreDialog = false }) {
                    Text("Entendido")
                }
            }
        )
    }

    if (showLinkDialog) {
        AccountLinkDialog(
            initialPhone = "",
            onDismiss = { showLinkDialog = false },
            onLinked = {
                showLinkDialog = false
                loadPurchases()
            }
        )
    }

    if (wizardInstallments.isNotEmpty()) {
        InstallmentPaymentWizardDialog(
            supplierName = wizardSupplierName.ifBlank { "Proveedor Mayorista" },
            selectedInstallments = wizardInstallments,
            isSubmitting = isSubmittingWizard,
            onDismiss = { wizardInstallments = emptyList() },
            onConfirmPayment = { channelId, originBank, _, _, referenceNumber, _ ->
                payInstallmentsWithWizard(
                    installmentsToPay = wizardInstallments,
                    channelId = channelId,
                    originBank = originBank,
                    referenceNumber = referenceNumber
                )
            }
        )
    }
}

@Composable
private fun LemonMainCardContainer(
    tier: KuotaCardTier,
    isCurrentUserTier: Boolean,
    isPaused: Boolean,
    selectedPaymentMethod: String,
    onTogglePause: () -> Unit,
    onOpenCardDetail: () -> Unit,
    onOpenPaymentMethodPicker: () -> Unit
) {
    val refDigits = when (tier.id) {
        KuotaCardId.CLASSIC -> "2500"
        KuotaCardId.GOLD -> "8000"
        KuotaCardId.PLATINUM -> "2000"
        KuotaCardId.BLACK_INFINITE -> "5000"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Fila superior: Miniatura + Nombre + *2500 + Píldora "Activa" + Botón Pausa + Flecha
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onOpenCardDetail)
                    .padding(horizontal = 18.dp, vertical = 18.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .width(104.dp)
                        .aspectRatio(1024f / 646f)
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, tier.accentColor.copy(alpha = 0.55f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = tier.imageRes),
                        contentDescription = tier.contentDescription,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .alpha(if (isPaused) 0.5f else 1f)
                    )
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        tier.fullName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        "*$refDigits",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                    val pillColor by animateColorAsState(
                        targetValue = when {
                            isPaused -> Color(0xFFF59E0B)
                            isCurrentUserTier -> Color(0xFF10B981)
                            else -> MaterialTheme.colorScheme.primary
                        },
                        animationSpec = tween(durationMillis = 200, easing = KuotaMotion.StandardEasing),
                        label = "tierStatusPillColor"
                    )
                    val pillLabel = when {
                        isPaused -> "Pausada"
                        isCurrentUserTier -> "Activa"
                        else -> "Demo"
                    }
                    Box(
                        modifier = Modifier
                            .background(pillColor.copy(alpha = 0.16f), RoundedCornerShape(50))
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text(
                            pillLabel,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = pillColor,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .kuotaPressable(pressedScale = 0.9f, role = Role.Button, onClick = onTogglePause),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        if (isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                        contentDescription = if (isPaused) "Reactivar" else "Pausar",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Icon(
                    Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Ver tarjeta",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }

            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 18.dp),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
            )

            // Fila inferior: "Moneda de pago   Yape / Plin 🇵🇪 →"
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onOpenPaymentMethodPicker)
                    .padding(horizontal = 18.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Moneda de pago",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        selectedPaymentMethod,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1
                    )
                    PeruFlagBadge()
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Cambiar medio de pago",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun PeruFlagBadge() {
    Row(
        modifier = Modifier
            .size(22.dp)
            .clip(CircleShape)
            .border(0.5.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .background(Color(0xFFD91023))
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .background(Color.White)
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .background(Color(0xFFD91023))
        )
    }
}

/**
 * Pantalla de detalle minimalista idéntica a la 2.ª captura de Lemon Cash (`media_1792704966490_b5492656.png`):
 * - Barra superior: "←    KUOTA Clásica"
 * - Carrusel limpio con las 4 imágenes de las tarjetas KUOTA (sin cajas de texto pesadas)
 * - Tres botones circulares debajo: "Pausar" | "Ver datos" | "Límites"
 * - Botón píldora completo inferior
 * - Una sola fila inferior de acción
 */
@Composable
private fun LemonCleanCardDetailScreen(
    selectedTier: KuotaCardTier,
    isPaused: Boolean,
    selectedPaymentMethod: String,
    onSelectTier: (KuotaCardTier) -> Unit,
    onTogglePause: () -> Unit,
    onOpenSpecs: () -> Unit,
    onOpenLimits: () -> Unit,
    onOpenPaymentMethodPicker: () -> Unit,
    onUseWithQr: () -> Unit,
    onDismiss: () -> Unit
) {
    val listState = rememberLazyListState(
        initialFirstVisibleItemIndex = KuotaCardCatalog.indexOfFirst { it.id == selectedTier.id }.coerceAtLeast(0)
    )
    val scope = rememberCoroutineScope()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(22.dp)
            ) {
                // Top Bar centrada: "←     KUOTA Clásica     "
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.align(Alignment.CenterStart)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                    Text(
                        selectedTier.fullName,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1
                    )
                }

                // Selector limpio de las 4 tarjetas
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(50))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    KuotaCardCatalog.forEachIndexed { index, tier ->
                        val active = tier.id == selectedTier.id
                        val shortLabel = when (tier.id) {
                            KuotaCardId.CLASSIC -> "Clásica"
                            KuotaCardId.GOLD -> "Oro"
                            KuotaCardId.PLATINUM -> "Platino"
                            KuotaCardId.BLACK_INFINITE -> "Black"
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(50))
                                .background(if (active) MaterialTheme.colorScheme.surface else Color.Transparent)
                                .clickable {
                                    onSelectTier(tier)
                                    scope.launch { runCatching { listState.animateScrollToItem(index) } }
                                }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                shortLabel,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (active) FontWeight.Bold else FontWeight.Medium,
                                color = if (active) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                }

                // Carrusel visual puro de las 4 tarjetas KUOTA (solo la imagen nítida, cero ruido visual)
                LazyRow(
                    state = listState,
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp)
                ) {
                    itemsIndexed(KuotaCardCatalog, key = { _, t -> t.id.name }) { _, tier ->
                        val isSelected = tier.id == selectedTier.id
                        val borderColor by animateColorAsState(
                            targetValue = if (isSelected) tier.accentColor else Color.Transparent,
                            label = "heroBorder"
                        )
                        Card(
                            modifier = Modifier
                                .width(328.dp)
                                .aspectRatio(1024f / 646f)
                                .semantics {
                                    role = Role.Tab
                                    selected = isSelected
                                }
                                .clickable { onSelectTier(tier) },
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                            border = BorderStroke(if (isSelected) 1.5.dp else 0.dp, borderColor),
                            elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 10.dp else 3.dp)
                        ) {
                            Image(
                                painter = painterResource(id = tier.imageRes),
                                contentDescription = tier.contentDescription,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(20.dp))
                                    .alpha(if (isPaused && isSelected) 0.5f else 1f)
                            )
                        }
                    }
                }

                // Línea de resumen de 1 sola línea debajo de la tarjeta
                Text(
                    "${selectedTier.creditRangeLabel} · Inicial ${selectedTier.downPaymentPercent}% QR",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                // Tres botones circulares debajo de la tarjeta: "Pausar" | "Ver datos" | "Límites"
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LemonCircleActionButton(
                        icon = if (isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                        label = if (isPaused) "Reactivar" else "Pausar",
                        active = isPaused,
                        onClick = onTogglePause
                    )
                    LemonCircleActionButton(
                        icon = Icons.Default.Visibility,
                        label = "Ver datos",
                        active = false,
                        onClick = onOpenSpecs
                    )
                    LemonCircleActionButton(
                        icon = Icons.Default.Tune,
                        label = "Límites",
                        active = false,
                        onClick = onOpenLimits
                    )
                }

                Spacer(Modifier.height(4.dp))

                // Botón píldora completo (como "Agregar a la Billetera de Google" en la Captura 2)
                Button(
                    onClick = onUseWithQr,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Icon(Icons.Default.QrCodeScanner, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(10.dp))
                    Text(
                        "Comprar con código QR",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Fila inferior minimalista (como "Eliminar tarjeta virtual" en la Captura 2)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .clickable(onClick = onOpenPaymentMethodPicker)
                        .padding(vertical = 10.dp, horizontal = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Shield,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            "Medio de pago: $selectedPaymentMethod",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1
                        )
                        Text(
                            "Diseño conceptual · Sin emisión ni CVV",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                    }
                    Icon(
                        Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun LemonCircleActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    active: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(62.dp)
                .clip(CircleShape)
                .background(
                    if (active) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.surfaceVariant
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                contentDescription = label,
                tint = if (active) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(24.dp)
            )
        }
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            softWrap = false
        )
    }
}

@Composable
private fun TierDetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(0.45f)
        )
        Text(
            value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(0.55f)
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ActivePurchaseInstallmentsCard(
    purchase: PurchaseHistoryItemDto,
    activeActionInstallmentId: String?,
    onInitiatePayment: (QuoteInstallmentDto) -> Unit,
    onLaunchWizard: (String, List<QuoteInstallmentDto>) -> Unit,
    onTriggerDemoEvent: (PaymentIntentDto, String) -> Unit
) {
    val paidCount = purchase.installments.count { it.status in setOf("DEMO_PAID", "PAID") }
    val totalCount = purchase.installments.size
    val paymentsByInstallment = remember(purchase.payments) {
        purchase.payments.groupBy { it.installmentId }.mapValues { (_, list) -> list.last() }
    }
    val unpaidInstallments = remember(purchase.installments) {
        purchase.installments.filter { it.status !in setOf("DEMO_PAID", "PAID") }
    }
    var selectedIds by remember(unpaidInstallments.map { it.installmentId }) {
        mutableStateOf(
            unpaidInstallments.firstOrNull()?.let { setOf(it.installmentId) } ?: emptySet()
        )
    }
    val selectedList = remember(unpaidInstallments, selectedIds) {
        unpaidInstallments.filter { it.installmentId in selectedIds }
    }
    val selectedTotalCents = remember(selectedList) {
        selectedList.sumOf { it.amountCents }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    purchase.supplierName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                StatusPill("$paidCount/$totalCount")
            }

            if (unpaidInstallments.isNotEmpty()) {
                Button(
                    onClick = { onLaunchWizard(purchase.supplierName, selectedList) },
                    enabled = selectedList.isNotEmpty() && activeActionInstallmentId == null,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(50)
                ) {
                    Text(
                        if (selectedList.isEmpty()) {
                            "Selecciona cuotas"
                        } else {
                            "Pagar ${selectedList.size} cuota(s) · ${formatCentsPen(selectedTotalCents)}"
                        },
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            purchase.installments.forEachIndexed { idx, installment ->
                if (idx > 0) HorizontalDivider()
                val latestPayment = paymentsByInstallment[installment.installmentId]
                val isPaid = installment.status in setOf("DEMO_PAID", "PAID")
                val isSelected = installment.installmentId in selectedIds
                InstallmentPaymentRow(
                    installment = installment,
                    latestPayment = latestPayment,
                    isSelected = isSelected,
                    onToggleSelect = if (!isPaid) {
                        {
                            selectedIds = if (isSelected) {
                                selectedIds - installment.installmentId
                            } else {
                                selectedIds + installment.installmentId
                            }
                        }
                    } else {
                        null
                    },
                    isBusy = activeActionInstallmentId == installment.installmentId,
                    onOpenWizardSingle = { onLaunchWizard(purchase.supplierName, listOf(installment)) },
                    onInitiatePayment = { onInitiatePayment(installment) },
                    onTriggerDemoEvent = { payment, eventType -> onTriggerDemoEvent(payment, eventType) }
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun InstallmentPaymentRow(
    installment: QuoteInstallmentDto,
    latestPayment: PaymentIntentDto?,
    isSelected: Boolean,
    onToggleSelect: (() -> Unit)?,
    isBusy: Boolean,
    onOpenWizardSingle: () -> Unit,
    onInitiatePayment: () -> Unit,
    onTriggerDemoEvent: (PaymentIntentDto, String) -> Unit
) {
    val kuotaColors = MaterialTheme.kuotaColors
    val isPaid = installment.status in setOf("DEMO_PAID", "PAID")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (onToggleSelect != null) {
                    Checkbox(
                        checked = isSelected,
                        onCheckedChange = { onToggleSelect() }
                    )
                }
                Column {
                    Text(
                        "Cuota #${installment.sequenceNumber} · ${formatCentsPen(installment.amountCents)}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        "Vence ${installment.dueAt}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            StatusPill(
                text = if (isPaid) "Pagada" else "Pendiente",
                container = if (isPaid) kuotaColors.successContainer else MaterialTheme.colorScheme.secondaryContainer,
                content = if (isPaid) kuotaColors.success else MaterialTheme.colorScheme.primary
            )
        }

        if (isBusy) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                Text("Procesando…", style = MaterialTheme.typography.bodySmall)
            }
        } else if (!isPaid || latestPayment != null) {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (!isPaid) {
                    TextButton(
                        onClick = onOpenWizardSingle,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text("Pagar", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    }
                    TextButton(
                        onClick = onInitiatePayment,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text("Intento demo", style = MaterialTheme.typography.labelMedium)
                    }
                }
                if (latestPayment != null && latestPayment.status in setOf("CREATED", "PENDING_USER_ACTION", "PROCESSING")) {
                    TextButton(
                        onClick = { onTriggerDemoEvent(latestPayment, "SUCCEED") },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text("Conciliar", style = MaterialTheme.typography.labelSmall)
                    }
                    TextButton(
                        onClick = { onTriggerDemoEvent(latestPayment, "FAIL") },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text("Fallo", style = MaterialTheme.typography.labelSmall)
                    }
                }
                if (latestPayment != null && latestPayment.status == "SUCCEEDED") {
                    TextButton(
                        onClick = { onTriggerDemoEvent(latestPayment, "REFUND") },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text("Reembolsar", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
    }
}

private fun formatCentsPen(cents: Long): String =
    String.format(Locale.US, "S/ %,.2f", cents / 100.0)
