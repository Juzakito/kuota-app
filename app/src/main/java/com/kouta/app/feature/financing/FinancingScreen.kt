package com.kouta.app.feature.financing

import android.graphics.Bitmap
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.kouta.app.core.analytics.KuotaAnalytics
import com.kouta.app.core.analytics.KuotaAnalyticsEvent
import com.kouta.app.core.designsystem.components.DemoBanner
import com.kouta.app.core.designsystem.components.KuotaAnimatedFeedback
import com.kouta.app.core.designsystem.components.KuotaFeedbackKind
import com.kouta.app.core.designsystem.components.KuotaLoadingRow
import com.kouta.app.core.designsystem.components.StatusPill
import com.kouta.app.core.designsystem.motion.KuotaMotion
import com.kouta.app.core.designsystem.motion.kuotaTabSectionReveal
import com.kouta.app.core.designsystem.motion.rememberSystemAnimatorScale
import com.kouta.app.core.designsystem.theme.kuotaColors
import com.kouta.app.data.remote.ApiOutcome
import com.kouta.app.data.remote.ConfirmSupplierSaleBody
import com.kouta.app.data.remote.CreatePaymentIntentBody
import com.kouta.app.data.remote.CreatePurchaseBody
import com.kouta.app.data.remote.DemoPaymentEventBody
import com.kouta.app.data.remote.EligibilityCheckBody
import com.kouta.app.data.remote.EligibilityCheckDto
import com.kouta.app.data.remote.GenerateDemoQrDto
import com.kouta.app.data.remote.KuotaApiClient
import com.kouta.app.data.remote.PaymentIntentDto
import com.kouta.app.data.remote.PurchaseDto
import com.kouta.app.data.remote.QuoteDto
import com.kouta.app.data.remote.QuoteInstallmentDto
import com.kouta.app.data.remote.QuoteRequestBody
import com.kouta.app.data.remote.SupplierConfirmationDto
import com.kouta.app.data.remote.VerifiedSupplierQrDto
import com.kouta.app.data.remote.VerifySupplierQrBody
import com.kouta.app.domain.model.PurchaseOffer
import com.kouta.app.feature.account.AccountLinkDialog
import com.kouta.app.feature.account.unreachableMessage
import com.kouta.app.feature.payments.InstallmentPaymentWizardDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.UUID

private val scheduleDateFormat = DateTimeFormatter.ofPattern("dd MMM", Locale.forLanguageTag("es-PE"))
private const val DEMO_BUSINESS_ID = "demo-buyer-001"
private const val DEMO_CONSENT_MARKER = "DEMO_ONLY_NOT_ACCEPTED"

@Composable
fun FinancingScreen(onOpenCards: () -> Unit = {}) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val kuotaColors = MaterialTheme.kuotaColors
    val qrFadeMs = KuotaMotion.resolveDurationMs(220, rememberSystemAnimatorScale()).coerceAtLeast(40)

    var showCameraScanner by remember { mutableStateOf(false) }
    var showQrGeneratorDialog by remember { mutableStateOf(false) }
    var generatorAmountInput by remember { mutableStateOf("1000") }
    var generatedQrDto by remember { mutableStateOf<GenerateDemoQrDto?>(null) }
    var generatedQrBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isGeneratingQr by remember { mutableStateOf(false) }

    var verifiedQr by remember { mutableStateOf<VerifiedSupplierQrDto?>(null) }
    var verifiedPayload by remember { mutableStateOf<String?>(null) }
    var eligibilityDecision by remember { mutableStateOf<EligibilityCheckDto?>(null) }
    var simulatedHistoryOverride by remember { mutableStateOf<Int?>(null) }
    var quote by remember { mutableStateOf<QuoteDto?>(null) }
    var quoteError by remember { mutableStateOf<String?>(null) }
    var isQuoting by remember { mutableStateOf(false) }
    var quoteRequestNonce by remember { mutableIntStateOf(0) }
    var selectedTermDays by remember { mutableIntStateOf(30) }
    var confirmation by remember { mutableStateOf<SupplierConfirmationDto?>(null) }
    var isConfirming by remember { mutableStateOf(false) }
    var actionError by remember { mutableStateOf<String?>(null) }
    var isVerifyingQr by remember { mutableStateOf(false) }
    var qrError by remember { mutableStateOf<String?>(null) }
    var showSchedule by remember { mutableStateOf(true) }
    var purchase by remember { mutableStateOf<PurchaseDto?>(null) }
    var activeInstallments by remember { mutableStateOf<List<QuoteInstallmentDto>>(emptyList()) }
    var latestPaymentsByInstallment by remember { mutableStateOf<Map<String, PaymentIntentDto>>(emptyMap()) }
    var payingInstallmentId by remember { mutableStateOf<String?>(null) }
    var wizardInstallments by remember { mutableStateOf<List<QuoteInstallmentDto>>(emptyList()) }
    var isSubmittingWizard by remember { mutableStateOf(false) }
    var paymentSuccessMessage by remember { mutableStateOf<String?>(null) }
    var isPurchasing by remember { mutableStateOf(false) }
    var purchaseError by remember { mutableStateOf<String?>(null) }
    var hasSession by remember { mutableStateOf(KuotaApiClient.hasSession(context)) }
    var showLinkDialog by remember { mutableStateOf(false) }
    val purchaseIdempotencyKey = remember(quote?.quoteId) { "demo-purchase-${UUID.randomUUID()}" }

    fun verifyPayloadAndStartFlow(rawPayload: String) {
        val payload = rawPayload.trim()
        if (payload.isEmpty() || isVerifyingQr) return
        scope.launch {
            isVerifyingQr = true
            qrError = null
            actionError = null
            val outcome = withContext(Dispatchers.IO) {
                KuotaApiClient.call(context) { verifySupplierQr(VerifySupplierQrBody(payload)) }
            }
            isVerifyingQr = false
            when (outcome) {
                is ApiOutcome.Ok -> {
                    verifiedQr = outcome.value
                    verifiedPayload = payload
                    quote = null
                    quoteError = null
                    confirmation = null
                    purchase = null
                    activeInstallments = emptyList()
                    latestPaymentsByInstallment = emptyMap()
                    paymentSuccessMessage = null
                    actionError = null
                    showCameraScanner = false
                    showQrGeneratorDialog = false
                }
                is ApiOutcome.Rejected -> qrError = apiErrorMessage(outcome)
                is ApiOutcome.Unreachable -> qrError = apiErrorMessage(outcome)
                ApiOutcome.SessionExpired -> qrError = apiErrorMessage(outcome)
            }
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            val decoded = withContext(Dispatchers.IO) {
                QrCodeToolkit.decodeQrFromUri(context, uri)
            }
            if (decoded.isNullOrBlank()) {
                qrError = "No se encontró un código QR válido en la imagen seleccionada."
                Toast.makeText(context, "No se detectó un QR en esa imagen.", Toast.LENGTH_LONG).show()
            } else {
                showCameraScanner = false
                verifyPayloadAndStartFlow(decoded)
            }
        }
    }

    fun generateVisualQr(amountPen: Double) {
        if (isGeneratingQr) return
        scope.launch {
            isGeneratingQr = true
            qrError = null
            val outcome = withContext(Dispatchers.IO) {
                KuotaApiClient.call(context) { generateDemoQr(amountPen) }
            }
            when (outcome) {
                is ApiOutcome.Ok -> {
                    val dto = outcome.value
                    val bmp = withContext(Dispatchers.Default) {
                        QrCodeToolkit.generateQrBitmap(dto.signedQrPayload, 640)
                    }
                    generatedQrDto = dto
                    generatedQrBitmap = bmp
                }
                is ApiOutcome.Rejected -> qrError = apiErrorMessage(outcome)
                is ApiOutcome.Unreachable -> qrError = apiErrorMessage(outcome)
                ApiOutcome.SessionExpired -> qrError = apiErrorMessage(outcome)
            }
            isGeneratingQr = false
        }
    }

    fun confirmAndRegisterPurchase() {
        val currentQuote = quote ?: return
        val payload = verifiedPayload ?: return
        if (isConfirming || isPurchasing) return
        if (!hasSession) {
            showLinkDialog = true
            return
        }
        scope.launch {
            isConfirming = true
            isPurchasing = true
            actionError = null
            purchaseError = null

            // 1. Confirmación del proveedor si aún no se hizo
            if (confirmation == null) {
                val confOutcome = withContext(Dispatchers.IO) {
                    KuotaApiClient.call(context) {
                        confirmSupplierSale(ConfirmSupplierSaleBody(payload))
                    }
                }
                when (confOutcome) {
                    is ApiOutcome.Ok -> {
                        confirmation = confOutcome.value
                        KuotaAnalytics.track(
                            KuotaAnalyticsEvent.SUPPLIER_DISBURSEMENT_STATUS,
                            mapOf("status" to confOutcome.value.status, "demo_only" to "true")
                        )
                    }
                    is ApiOutcome.Rejected -> {
                        actionError = apiErrorMessage(confOutcome)
                        isConfirming = false
                        isPurchasing = false
                        return@launch
                    }
                    is ApiOutcome.Unreachable -> {
                        actionError = apiErrorMessage(confOutcome)
                        isConfirming = false
                        isPurchasing = false
                        return@launch
                    }
                    ApiOutcome.SessionExpired -> {
                        hasSession = false
                        isConfirming = false
                        isPurchasing = false
                        showLinkDialog = true
                        return@launch
                    }
                }
            }
            isConfirming = false

            // 2. Registro de la compra y activación de cuotas
            KuotaAnalytics.track(
                KuotaAnalyticsEvent.OFFER_ACCEPTED,
                mapOf(
                    "term_days" to selectedTermDays.toString(),
                    "down_payment_bps" to currentQuote.downPaymentBasisPoints.toString(),
                    "demo_only" to "true"
                )
            )
            val outcome = withContext(Dispatchers.IO) {
                KuotaApiClient.call(context) {
                    createPurchase(
                        purchaseIdempotencyKey,
                        CreatePurchaseBody(
                            quoteId = currentQuote.quoteId,
                            quoteVersion = currentQuote.quoteVersion,
                            acceptedTermsVersion = DEMO_CONSENT_MARKER,
                            acceptedPrivacyNoticeVersion = DEMO_CONSENT_MARKER
                        )
                    )
                }
            }
            isPurchasing = false
            when (outcome) {
                is ApiOutcome.Ok -> {
                    purchase = outcome.value
                    activeInstallments = currentQuote.installments
                    KuotaAnalytics.track(
                        KuotaAnalyticsEvent.PLAN_ACTIVATED,
                        mapOf("status" to outcome.value.status, "demo_only" to "true")
                    )
                }
                is ApiOutcome.Rejected -> purchaseError = apiErrorMessage(outcome)
                is ApiOutcome.Unreachable -> purchaseError = apiErrorMessage(outcome)
                ApiOutcome.SessionExpired -> {
                    hasSession = false
                    purchaseError = apiErrorMessage(outcome)
                    showLinkDialog = true
                }
            }
        }
    }

    fun payInstallmentImmediately(installment: QuoteInstallmentDto) {
        if (payingInstallmentId != null) return
        scope.launch {
            payingInstallmentId = installment.installmentId
            purchaseError = null
            paymentSuccessMessage = null
            KuotaAnalytics.track(
                KuotaAnalyticsEvent.PAYMENT_ATTEMPT_CREATED,
                mapOf(
                    "installment_sequence" to installment.sequenceNumber.toString(),
                    "demo_only" to "true"
                )
            )
            val outcome = withContext(Dispatchers.IO) {
                val createOutcome = KuotaApiClient.call(context) {
                    createPaymentIntent(
                        installmentId = installment.installmentId,
                        idempotencyKey = "qr-pay-${installment.installmentId}-${UUID.randomUUID()}",
                        body = CreatePaymentIntentBody(paymentMethodId = "DEMO_MANUAL")
                    )
                }
                when (createOutcome) {
                    is ApiOutcome.Ok -> {
                        KuotaApiClient.call(context) {
                            applyDemoPaymentEvent(
                                paymentId = createOutcome.value.paymentId,
                                idempotencyKey = "qr-settle-${createOutcome.value.paymentId}-${UUID.randomUUID()}",
                                body = DemoPaymentEventBody(eventType = "SUCCEED")
                            )
                        }
                    }
                    else -> createOutcome
                }
            }
            payingInstallmentId = null
            when (outcome) {
                is ApiOutcome.Ok -> {
                    val paidPayment = outcome.value
                    latestPaymentsByInstallment = latestPaymentsByInstallment + (installment.installmentId to paidPayment)
                    activeInstallments = activeInstallments.map {
                        if (it.installmentId == installment.installmentId) it.copy(status = "DEMO_PAID") else it
                    }
                    paymentSuccessMessage = "Cuota ${installment.sequenceNumber} pagada (${PurchaseOffer.formatPen(installment.amountCents)}) · Comprobante ${paidPayment.providerReference ?: paidPayment.paymentId.takeLast(8)}"
                    KuotaAnalytics.track(
                        KuotaAnalyticsEvent.PAYMENT_STATUS_CHANGED,
                        mapOf(
                            "status" to paidPayment.status,
                            "reconciliation_status" to paidPayment.reconciliationStatus,
                            "demo_only" to "true"
                        )
                    )
                }
                is ApiOutcome.Rejected -> purchaseError = apiErrorMessage(outcome)
                is ApiOutcome.Unreachable -> purchaseError = apiErrorMessage(outcome)
                ApiOutcome.SessionExpired -> {
                    hasSession = false
                    showLinkDialog = true
                }
            }
        }
    }

    fun payInstallmentsFromWizard(
        installmentsToPay: List<QuoteInstallmentDto>,
        channelId: String,
        originBank: String,
        referenceNumber: String
    ) {
        if (isSubmittingWizard || installmentsToPay.isEmpty()) return
        scope.launch {
            isSubmittingWizard = true
            purchaseError = null
            paymentSuccessMessage = null
            var paidCount = 0
            var totalPaidCents = 0L
            for (installment in installmentsToPay) {
                payingInstallmentId = installment.installmentId
                KuotaAnalytics.track(
                    KuotaAnalyticsEvent.PAYMENT_ATTEMPT_CREATED,
                    mapOf(
                        "installment_sequence" to installment.sequenceNumber.toString(),
                        "channel_id" to channelId,
                        "origin_bank" to originBank,
                        "demo_only" to "true"
                    )
                )
                val outcome = withContext(Dispatchers.IO) {
                    val createOutcome = KuotaApiClient.call(context) {
                        createPaymentIntent(
                            installmentId = installment.installmentId,
                            idempotencyKey = "wizard-pay-${installment.installmentId}-${UUID.randomUUID()}",
                            body = CreatePaymentIntentBody(paymentMethodId = channelId)
                        )
                    }
                    when (createOutcome) {
                        is ApiOutcome.Ok -> {
                            KuotaApiClient.call(context) {
                                applyDemoPaymentEvent(
                                    paymentId = createOutcome.value.paymentId,
                                    idempotencyKey = "wizard-settle-${createOutcome.value.paymentId}-${UUID.randomUUID()}",
                                    body = DemoPaymentEventBody(eventType = "SUCCEED")
                                )
                            }
                        }
                        else -> createOutcome
                    }
                }
                when (outcome) {
                    is ApiOutcome.Ok -> {
                        val paidPayment = outcome.value
                        latestPaymentsByInstallment = latestPaymentsByInstallment + (installment.installmentId to paidPayment)
                        activeInstallments = activeInstallments.map {
                            if (it.installmentId == installment.installmentId) it.copy(status = "DEMO_PAID") else it
                        }
                        paidCount += 1
                        totalPaidCents += installment.amountCents
                        KuotaAnalytics.track(
                            KuotaAnalyticsEvent.PAYMENT_STATUS_CHANGED,
                            mapOf(
                                "status" to paidPayment.status,
                                "reconciliation_status" to paidPayment.reconciliationStatus,
                                "demo_only" to "true"
                            )
                        )
                    }
                    is ApiOutcome.Rejected -> purchaseError = apiErrorMessage(outcome)
                    is ApiOutcome.Unreachable -> purchaseError = apiErrorMessage(outcome)
                    ApiOutcome.SessionExpired -> {
                        hasSession = false
                        showLinkDialog = true
                    }
                }
            }
            payingInstallmentId = null
            isSubmittingWizard = false
            if (paidCount > 0) {
                paymentSuccessMessage =
                    "$paidCount cuota(s) pagada(s) (${PurchaseOffer.formatPen(totalPaidCents)}) · Ref #$referenceNumber ($originBank)"
            }
        }
    }

    fun resetFlow() {
        verifiedQr = null
        verifiedPayload = null
        eligibilityDecision = null
        quote = null
        quoteError = null
        confirmation = null
        actionError = null
        purchase = null
        activeInstallments = emptyList()
        latestPaymentsByInstallment = emptyMap()
        wizardInstallments = emptyList()
        paymentSuccessMessage = null
        purchaseError = null
        qrError = null
    }

    LaunchedEffect(verifiedQr?.transactionId, selectedTermDays, simulatedHistoryOverride, quoteRequestNonce) {
        val qr = verifiedQr
        val payload = verifiedPayload
        if (qr == null || payload == null) {
            eligibilityDecision = null
            quote = null
            quoteError = null
            return@LaunchedEffect
        }
        isQuoting = true
        quoteError = null
        KuotaAnalytics.track(
            KuotaAnalyticsEvent.ELIGIBILITY_SUBMITTED,
            mapOf("term_days" to selectedTermDays.toString(), "demo_only" to "true")
        )
        val eligibility = withContext(Dispatchers.IO) {
            KuotaApiClient.call(context) {
                checkEligibility(
                    EligibilityCheckBody(
                        businessId = DEMO_BUSINESS_ID,
                        amountCents = qr.amountCents,
                        termDays = selectedTermDays,
                        consentVersion = DEMO_CONSENT_MARKER,
                        simulatedPaidInstallments = simulatedHistoryOverride
                    )
                )
            }
        }
        when (eligibility) {
            is ApiOutcome.Ok -> {
                eligibilityDecision = eligibility.value
                KuotaAnalytics.track(
                    KuotaAnalyticsEvent.ELIGIBILITY_RESULT,
                    mapOf(
                        "result_status" to eligibility.value.status,
                        "risk_tier" to eligibility.value.riskTier,
                        "policy_version" to eligibility.value.policyVersion,
                        "down_payment_bps" to eligibility.value.downPaymentBasisPoints.toString()
                    )
                )
                val quoteOutcome = withContext(Dispatchers.IO) {
                    KuotaApiClient.call(context) {
                        requestQuote(
                            QuoteRequestBody(
                                businessId = DEMO_BUSINESS_ID,
                                supplierQrPayload = payload,
                                amountCents = qr.amountCents,
                                termDays = selectedTermDays,
                                eligibilityDecisionId = eligibility.value.decisionId
                            )
                        )
                    }
                }
                when (quoteOutcome) {
                    is ApiOutcome.Ok -> {
                        quote = quoteOutcome.value
                        KuotaAnalytics.track(
                            KuotaAnalyticsEvent.INVENTORY_QUOTE_CREATED,
                            mapOf(
                                "term_days" to selectedTermDays.toString(),
                                "down_payment_bps" to quoteOutcome.value.downPaymentBasisPoints.toString(),
                                "policy_version" to quoteOutcome.value.policyVersion
                            )
                        )
                        KuotaAnalytics.track(
                            KuotaAnalyticsEvent.OFFER_VIEWED,
                            mapOf(
                                "term_days" to selectedTermDays.toString(),
                                "down_payment_bps" to quoteOutcome.value.downPaymentBasisPoints.toString()
                            )
                        )
                    }
                    is ApiOutcome.Rejected -> quoteError = apiErrorMessage(quoteOutcome)
                    is ApiOutcome.Unreachable -> quoteError = apiErrorMessage(quoteOutcome)
                    ApiOutcome.SessionExpired -> quoteError = apiErrorMessage(quoteOutcome)
                }
            }
            is ApiOutcome.Rejected -> quoteError = apiErrorMessage(eligibility)
            is ApiOutcome.Unreachable -> quoteError = apiErrorMessage(eligibility)
            ApiOutcome.SessionExpired -> quoteError = apiErrorMessage(eligibility)
        }
        isQuoting = false
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        DemoBanner()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    "Compra y Pago con QR",
                    modifier = Modifier.semantics { heading() },
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Escanea con tu cámara, sube una imagen QR recibida o genera un QR para compartir y pagar en cuotas",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .kuotaTabSectionReveal(sectionIndex = 0),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Icon(Icons.Default.Storefront, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary)
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = verifiedQr?.supplierName ?: "Listo para escanear o generar QR",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Text(
                            text = verifiedQr?.let {
                                "QR verificado · ${PurchaseOffer.formatPen(it.amountCents)} · Transacción ${it.transactionId.takeLast(8)}"
                            } ?: "Usa la cámara, sube un QR de tu galería o genera uno para compartir por WhatsApp.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = .88f)
                        )
                    }
                }
            }

            KuotaLoadingRow(visible = isVerifyingQr, label = "Verificando QR con el servidor…")
            KuotaAnimatedFeedback(message = qrError, kind = KuotaFeedbackKind.ERROR)

            if (verifiedQr == null) {
                // Tarjeta de acciones reales de QR: Cámara, Generar/Compartir y Galería
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .kuotaTabSectionReveal(sectionIndex = 1),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            "¿Cómo deseas continuar?",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Button(
                            onClick = { showCameraScanner = true },
                            modifier = Modifier.fillMaxWidth().height(52.dp),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
                            Text("Escanear QR con la cámara", fontWeight = FontWeight.SemiBold)
                        }

                        OutlinedButton(
                            onClick = {
                                showQrGeneratorDialog = true
                                if (generatedQrDto == null) {
                                    generateVisualQr(1_000.0)
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Icon(Icons.Default.QrCode2, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
                            Text("Generar QR para compartir o pagar", fontWeight = FontWeight.SemiBold)
                        }

                        OutlinedButton(
                            onClick = { galleryLauncher.launch("image/*") },
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
                            Text("Subir imagen QR de galería / WhatsApp", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            } else {
                Text("Plazo de Financiamiento:", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(15, 30, 60, 90).forEach { days ->
                        FilterChip(
                            selected = selectedTermDays == days,
                            onClick = {
                                selectedTermDays = days
                                confirmation = null
                                purchase = null
                                purchaseError = null
                            },
                            label = { Text("$days días") }
                        )
                    }
                }

                Text(
                    "Política backend de inicial según historial (PRD §18.21):",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val options = listOf(
                        null to "Automático (según tus pagos)",
                        0 to "Entrada (30% inicial)",
                        1 to "1 cuota pagada (20%)",
                        2 to "2 cuotas pagadas (10%)",
                        3 to "3+ cuotas pagadas (0%)"
                    )
                    options.forEach { (overrideCount, label) ->
                        FilterChip(
                            selected = simulatedHistoryOverride == overrideCount,
                            onClick = {
                                simulatedHistoryOverride = overrideCount
                                confirmation = null
                                purchase = null
                                purchaseError = null
                            },
                            label = { Text(label) }
                        )
                    }
                }

                when {
                    isQuoting -> {
                        KuotaLoadingRow(visible = true, label = "Consultando elegibilidad y cotización…")
                    }

                    quoteError != null -> {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                        ) {
                            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("No pudimos obtener la cotización", fontWeight = FontWeight.Bold)
                                Text(quoteError.orEmpty(), style = MaterialTheme.typography.bodySmall)
                                Button(onClick = { quoteRequestNonce += 1 }) { Text("Reintentar") }
                            }
                        }
                    }

                    quote != null -> QuoteContent(
                        supplierName = verifiedQr?.supplierName ?: "Proveedor Mayorista",
                        quote = quote!!,
                        eligibility = eligibilityDecision,
                        showSchedule = showSchedule,
                        onToggleSchedule = { showSchedule = !showSchedule }
                    )
                }
            }

            actionError?.let {
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }
            purchaseError?.let {
                Text(
                    it,
                    modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            if (confirmation != null) {
                ConfirmationCard(confirmation!!)
            }

            if (purchase != null) {
                PurchaseAndPayCard(
                    purchase = purchase!!,
                    installments = activeInstallments,
                    paymentsByInstallment = latestPaymentsByInstallment,
                    payingInstallmentId = payingInstallmentId,
                    paymentSuccessMessage = paymentSuccessMessage,
                    onPayInstallment = ::payInstallmentImmediately,
                    onLaunchWizardForInstallments = { selected ->
                        wizardInstallments = selected
                    },
                    onOpenCards = onOpenCards
                )
            } else if (quote != null) {
                Button(
                    onClick = { confirmAndRegisterPurchase() },
                    enabled = !isConfirming && !isPurchasing,
                    modifier = Modifier.fillMaxWidth().height(54.dp),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Default.Payment, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
                    Text(
                        when {
                            isConfirming || isPurchasing -> "Confirmando compra y activando plan…"
                            hasSession -> "Confirmar compra · Hoy pagas ${PurchaseOffer.formatPen(quote!!.buyerDownPaymentCents)}"
                            else -> "Vincular cuenta (OTP 123456) y confirmar compra"
                        },
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (verifiedQr != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = { showCameraScanner = true },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.padding(end = 6.dp))
                        Text("Cámara")
                    }
                    OutlinedButton(
                        onClick = { showQrGeneratorDialog = true },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(Icons.Default.QrCode2, contentDescription = null, modifier = Modifier.padding(end = 6.dp))
                        Text("Generar QR")
                    }
                }
            }
        }
    }

    if (showCameraScanner) {
        QrCameraScannerDialog(
            onDismiss = { showCameraScanner = false },
            onPickFromGallery = { galleryLauncher.launch("image/*") },
            onQrDetected = { rawText ->
                verifyPayloadAndStartFlow(rawText)
            }
        )
    }

    if (showQrGeneratorDialog) {
        AlertDialog(
            onDismissRequest = { if (!isGeneratingQr && !isVerifyingQr) showQrGeneratorDialog = false },
            title = { Text("Generar QR de proveedor") },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        "Genera un código QR real para compartirlo por WhatsApp (y escanearlo con otra cámara) o úsalo directamente aquí para hacer el pago.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = generatorAmountInput,
                            onValueChange = { value ->
                                if (value.all { it.isDigit() || it == '.' } && value.length <= 9) {
                                    generatorAmountInput = value
                                }
                            },
                            modifier = Modifier.weight(1f),
                            label = { Text("Monto en S/ (PEN)") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                        )
                        Button(
                            onClick = {
                                val amount = generatorAmountInput.toDoubleOrNull()?.coerceIn(10.0, 100_000.0) ?: 1_000.0
                                generateVisualQr(amount)
                            },
                            enabled = !isGeneratingQr
                        ) {
                            Text(if (isGeneratingQr) "…" else "Actualizar")
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("500" to 500.0, "1000" to 1_000.0, "2500" to 2_500.0).forEach { (label, amount) ->
                            FilterChip(
                                selected = generatorAmountInput == label,
                                onClick = {
                                    generatorAmountInput = label
                                    generateVisualQr(amount)
                                },
                                label = { Text("S/ $label") }
                            )
                        }
                    }

                    KuotaLoadingRow(visible = isGeneratingQr, label = "Generando QR…")

                    val currentDto = generatedQrDto
                    val currentBmp = generatedQrBitmap
                    // El QR aparece con un fundido breve solo después de recibir el resultado real del servidor.
                    // No se escala ni se deforma el patrón para no afectar su lectura.
                    AnimatedContent(
                        targetState = currentBmp,
                        transitionSpec = {
                            fadeIn(tween(qrFadeMs, easing = KuotaMotion.DecelerateEasing)) togetherWith
                                fadeOut(tween(qrFadeMs / 2))
                        },
                        label = "generatedQrBitmap"
                    ) { animatedBmp ->
                        if (currentDto != null && animatedBmp != null) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(18.dp))
                                        .background(Color.White)
                                        .padding(12.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Image(
                                        bitmap = animatedBmp.asImageBitmap(),
                                        contentDescription = "Código QR generado",
                                        modifier = Modifier.size(210.dp)
                                    )
                                }
                                Text(
                                    "${currentDto.supplierName}\nMonto: ${PurchaseOffer.formatPen(currentDto.amountCents)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                    if (currentDto != null && currentBmp != null) {
                        OutlinedButton(
                            onClick = {
                                QrCodeToolkit.shareSupplierQrImage(
                                    context = context,
                                    qrPayload = currentDto.signedQrPayload,
                                    supplierName = currentDto.supplierName.ifBlank { "Distribuidora Mayorista Lima S.A.C." },
                                    amountFormatted = PurchaseOffer.formatPen(currentDto.amountCents)
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
                            Text("Compartir imagen QR (WhatsApp / Otros)", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val payload = generatedQrDto?.signedQrPayload ?: return@Button
                        verifyPayloadAndStartFlow(payload)
                    },
                    enabled = generatedQrDto != null && !isVerifyingQr && !isGeneratingQr
                ) {
                    Text(if (isVerifyingQr) "Verificando…" else "Usar este QR y pagar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showQrGeneratorDialog = false }) {
                    Text("Cerrar")
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
                hasSession = true
                confirmAndRegisterPurchase()
            }
        )
    }

    if (wizardInstallments.isNotEmpty()) {
        InstallmentPaymentWizardDialog(
            supplierName = verifiedQr?.supplierName ?: "Proveedor Mayorista",
            selectedInstallments = wizardInstallments,
            isSubmitting = isSubmittingWizard,
            onDismiss = { wizardInstallments = emptyList() },
            onConfirmPayment = { channelId, originBank, _, _, referenceNumber, _ ->
                payInstallmentsFromWizard(
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
private fun PurchaseAndPayCard(
    purchase: PurchaseDto,
    installments: List<QuoteInstallmentDto>,
    paymentsByInstallment: Map<String, PaymentIntentDto>,
    payingInstallmentId: String?,
    paymentSuccessMessage: String?,
    onPayInstallment: (QuoteInstallmentDto) -> Unit,
    onLaunchWizardForInstallments: (List<QuoteInstallmentDto>) -> Unit,
    onOpenCards: () -> Unit
) {
    val kuotaColors = MaterialTheme.kuotaColors
    val unpaidInstallments = remember(installments) {
        installments.filter { it.status !in setOf("DEMO_PAID", "PAID") }
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
        modifier = Modifier
            .fillMaxWidth()
            .semantics { liveRegion = LiveRegionMode.Polite },
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = kuotaColors.success)
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Confirma tus pagos · Plan activado",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Operación ${purchase.purchaseId.takeLast(8)} · Estado ${purchase.status}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Banner de gamificación: Gana más ⭐ pagando más cuotas (PDF 2 Págs. 9 y 10)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(kuotaColors.warningContainer)
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    Icons.Default.Star,
                    contentDescription = null,
                    tint = kuotaColors.warningContent,
                    modifier = Modifier.size(20.dp)
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "¡Gana más ⭐ pagando más cuotas!",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = kuotaColors.warningContent
                    )
                    Text(
                        "Selecciona una o varias cuotas para adelantar tu pago y reducir tu inicial hasta 0%.",
                        style = MaterialTheme.typography.bodySmall,
                        color = kuotaColors.warningContent
                    )
                }
            }

            paymentSuccessMessage?.let { msg ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = kuotaColors.successContainer)
                ) {
                    Text(
                        msg,
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = kuotaColors.success,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            installments.forEach { inst ->
                val isPaid = inst.status in setOf("DEMO_PAID", "PAID")
                val payment = paymentsByInstallment[inst.installmentId]
                val isBusy = payingInstallmentId == inst.installmentId
                val isSelected = inst.installmentId in selectedIds
                HorizontalDivider()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(enabled = !isPaid) {
                            selectedIds = if (isSelected) {
                                selectedIds - inst.installmentId
                            } else {
                                selectedIds + inst.installmentId
                            }
                        }
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (!isPaid) {
                        Checkbox(
                            checked = isSelected,
                            onCheckedChange = { checked ->
                                selectedIds = if (checked) {
                                    selectedIds + inst.installmentId
                                } else {
                                    selectedIds - inst.installmentId
                                }
                            }
                        )
                    }
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = if (isPaid) 4.dp else 4.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            "Cuota ${inst.sequenceNumber} · ${PurchaseOffer.formatPen(inst.amountCents)}",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Vence: ${inst.dueAt}" + (payment?.providerReference?.let { " · Ref: $it" } ?: ""),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (isPaid) {
                        StatusPill(
                            text = "PAGADA",
                            container = kuotaColors.successContainer,
                            content = kuotaColors.success
                        )
                    } else {
                        TextButton(
                            onClick = { onPayInstallment(inst) },
                            enabled = payingInstallmentId == null
                        ) {
                            Text(if (isBusy) "Pagando…" else "Pago 1-clic")
                        }
                    }
                }
            }

            if (unpaidInstallments.isNotEmpty()) {
                Button(
                    onClick = {
                        if (selectedList.isNotEmpty()) {
                            onLaunchWizardForInstallments(selectedList)
                        } else {
                            onLaunchWizardForInstallments(listOf(unpaidInstallments.first()))
                        }
                    },
                    enabled = payingInstallmentId == null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Default.Payment, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
                    Text(
                        if (selectedList.isNotEmpty()) {
                            "Pagar ${selectedList.size} cuota(s) · ${PurchaseOffer.formatPen(selectedTotalCents)}"
                        } else {
                            "Seleccionar y pagar cuota"
                        },
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            OutlinedButton(
                onClick = onOpenCards,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Ver recibos, reversos y conciliación en Tarjetas", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun QuoteContent(
    supplierName: String,
    quote: QuoteDto,
    eligibility: EligibilityCheckDto?,
    showSchedule: Boolean,
    onToggleSchedule: () -> Unit
) {
    val kuotaColors = MaterialTheme.kuotaColors
    val downPaymentPct = quote.downPaymentBasisPoints / 100
    val koutaInitialPct = 60 - downPaymentPct
    val progressRatio = (downPaymentPct.coerceIn(5, 100)) / 100f

    // 1. Banner "Confirma tu compra · Hoy pagas S/ ..." (PDF 1 Págs. 3, 4, 7, 8)
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        "Confirma tu compra",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        supplierName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                StatusPill(
                    text = "Inicial $downPaymentPct%",
                    container = kuotaColors.infoContainer,
                    content = kuotaColors.info
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f))
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        "Hoy pagas (tu inicial $downPaymentPct%)",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        "Total inventario: ${PurchaseOffer.formatPen(quote.purchaseAmountCents)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.78f)
                    )
                }
                Text(
                    PurchaseOffer.formatPen(quote.buyerDownPaymentCents),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "Hoy $downPaymentPct%: ${PurchaseOffer.formatPen(quote.buyerDownPaymentCents)}",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        "${quote.installments.size} cuotas: ${PurchaseOffer.formatPen(quote.buyerTotalAfterDownPaymentCents)}",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                LinearProgressIndicator(
                    progress = { progressRatio },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                )
                Text(
                    "Desembolso B2B al proveedor: 60% hoy (${PurchaseOffer.formatPen(quote.supplierImmediatePayoutCents)} = tu $downPaymentPct% + $koutaInitialPct% Kuota) y 40% diferido (${PurchaseOffer.formatPen(quote.supplierDeferredPayoutCents)}).",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }

    eligibility?.let { decision ->
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    "Decisión backend · Política ${decision.policyVersion}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Score interno: ${decision.riskScore} (${decision.riskTier}) · Inicial aprobado: ${decision.downPaymentBasisPoints / 100}% · Cuotas previas pagadas: ${decision.completedInstallmentsCount}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }
    }

    // 2. Tarjeta "Tu plan de pagos" con línea de tiempo vertical (PDF 1 Págs. 3 y 7)
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        "Tu plan de pagos",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Inicial $downPaymentPct% + ${quote.installments.size} Cuotas semanales",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                StatusPill(
                    text = "${quote.installments.size} CUOTAS",
                    container = MaterialTheme.colorScheme.secondaryContainer,
                    content = MaterialTheme.colorScheme.primary
                )
            }

            HorizontalDivider()

            // Nodo 0: Hoy paga la inicial
            TimelinePaymentNode(
                badgeText = "HOY",
                isPrimaryNode = true,
                showBottomConnector = quote.installments.isNotEmpty(),
                title = "Hoy paga la inicial ($downPaymentPct%)",
                subtitle = "Aporte Kuota al proveedor: ${PurchaseOffer.formatPen(quote.koutaInitialContributionCents)} ($koutaInitialPct%)",
                amountFormatted = PurchaseOffer.formatPen(quote.buyerDownPaymentCents)
            )

            // Nodos 1..N: Cuotas semanales
            quote.installments.forEachIndexed { idx, installment ->
                val dueLabel = runCatching {
                    LocalDate.parse(installment.dueAt).format(scheduleDateFormat)
                }.getOrDefault(installment.dueAt)
                TimelinePaymentNode(
                    badgeText = "${installment.sequenceNumber}",
                    isPrimaryNode = false,
                    showBottomConnector = idx < quote.installments.lastIndex,
                    title = "Cuota ${installment.sequenceNumber}",
                    subtitle = "Vence $dueLabel",
                    amountFormatted = PurchaseOffer.formatPen(installment.amountCents)
                )
            }
        }
    }

    // 3. Desglose contable B2B 60/40 expandible
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onToggleSchedule)
                    .semantics { role = Role.Button }
                    .padding(18.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        "Desglose B2B 60/40 y comisiones",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Cotización ${quote.quoteVersion} · Vence ${quote.expiresAt.take(10)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Icon(
                    if (showSchedule) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (showSchedule) "Ocultar desglose" else "Ver desglose"
                )
            }
            if (showSchedule) {
                HorizontalDivider()
                Column(
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    BreakdownLine("Monto total de inventario", PurchaseOffer.formatPen(quote.purchaseAmountCents))
                    BreakdownLine("Cuota inicial MYPE ($downPaymentPct%)", PurchaseOffer.formatPen(quote.buyerDownPaymentCents))
                    BreakdownLine("Aporte inicial Kouta ($koutaInitialPct%)", PurchaseOffer.formatPen(quote.koutaInitialContributionCents))
                    HorizontalDivider()
                    BreakdownLine("Abono inmediato al proveedor (60%)", PurchaseOffer.formatPen(quote.supplierImmediatePayoutCents), isBold = true)
                    BreakdownLine("Abono diferido al proveedor (40%)", PurchaseOffer.formatPen(quote.supplierDeferredPayoutCents))
                    BreakdownLine("Comisión de proveedor (4%)", PurchaseOffer.formatPen(quote.supplierFeeCents))
                    HorizontalDivider()
                    BreakdownLine("Comisión de comprador", PurchaseOffer.formatPen(quote.buyerFeeCents))
                    BreakdownLine("Obligación posterior MYPE", PurchaseOffer.formatPen(quote.buyerTotalAfterDownPaymentCents), isBold = true)
                    BreakdownLine(
                        "Cuotas semanales (${quote.installments.size})",
                        quote.installments.firstOrNull()?.let { PurchaseOffer.formatPen(it.amountCents) } ?: "—",
                        isBold = true
                    )
                }
            }
        }
    }
}

@Composable
private fun TimelinePaymentNode(
    badgeText: String,
    isPrimaryNode: Boolean,
    showBottomConnector: Boolean,
    title: String,
    subtitle: String,
    amountFormatted: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(
                        if (isPrimaryNode) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.secondaryContainer
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isPrimaryNode) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                } else {
                    Text(
                        badgeText,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            if (showBottomConnector) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(26.dp)
                        .background(MaterialTheme.colorScheme.outlineVariant)
                )
            }
        }
        Row(
            modifier = Modifier
                .weight(1f)
                .padding(top = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (isPrimaryNode) FontWeight.Bold else FontWeight.SemiBold,
                    color = if (isPrimaryNode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                amountFormatted,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = if (isPrimaryNode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun ConfirmationCard(confirmation: SupplierConfirmationDto) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text("Confirmación de proveedor registrada", fontWeight = FontWeight.Bold)
                Text(
                    "${confirmation.supplierName} confirmó ${PurchaseOffer.formatPen(confirmation.amountCents)}. Abono inmediato ${PurchaseOffer.formatPen(confirmation.immediatePayoutCents)} (60%) y diferido ${PurchaseOffer.formatPen(confirmation.deferredPayoutCents)} (40%).",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
private fun BreakdownLine(label: String, value: String, isBold: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = if (isBold) MaterialTheme.typography.bodyMedium else MaterialTheme.typography.bodySmall,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal
        )
        Text(
            text = value,
            style = if (isBold) MaterialTheme.typography.bodyMedium else MaterialTheme.typography.bodySmall,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.SemiBold
        )
    }
}

private fun apiErrorMessage(outcome: ApiOutcome<*>): String = when (outcome) {
    is ApiOutcome.Rejected -> buildString {
        append(outcome.message.ifBlank { "El servidor rechazó la solicitud." })
        if (outcome.requestId.isNotBlank()) append("\nReferencia para soporte: ${outcome.requestId}")
    }
    is ApiOutcome.Unreachable -> unreachableMessage(outcome.serverUrl)
    ApiOutcome.SessionExpired -> "La sesión venció. Vuelve a vincular tu cuenta para registrar la compra demo."
    is ApiOutcome.Ok -> ""
}
