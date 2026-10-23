package com.kouta.app.feature.onboarding

import android.os.SystemClock
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material.icons.filled.PersonOutline
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kouta.app.R
import com.kouta.app.core.analytics.KuotaAnalytics
import com.kouta.app.core.analytics.KuotaAnalyticsEvent
import com.kouta.app.core.designsystem.motion.KuotaMotion
import com.kouta.app.core.designsystem.motion.KuotaStepAnimatedContent
import com.kouta.app.core.designsystem.motion.kuotaEntranceReveal
import com.kouta.app.core.designsystem.motion.kuotaErrorMicroOffset
import com.kouta.app.core.designsystem.motion.rememberSystemAnimatorScale
import com.kouta.app.core.security.PinSecurityManager
import com.kouta.app.data.local.LocalBusinessProfile
import com.kouta.app.data.local.OnboardingDraft
import com.kouta.app.data.local.SalesJournalStore
import com.kouta.app.feature.security.KuotaSixDigitCircles
import com.kouta.app.feature.security.KuotaSixDigitKeypad
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun OnboardingScreen(onOnboardingComplete: () -> Unit) {
    val context = LocalContext.current
    val store = remember(context) { SalesJournalStore(context) }
    val scope = rememberCoroutineScope()
    var step by rememberSaveable { mutableIntStateOf(0) }
    var visitedMask by rememberSaveable { mutableIntStateOf(0) }
    var ownerName by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var phone by rememberSaveable { mutableStateOf("") }
    var businessTaxId by rememberSaveable { mutableStateOf("") }
    var createdPin by rememberSaveable { mutableStateOf("") }
    var confirmPin by rememberSaveable { mutableStateOf("") }
    var error by rememberSaveable { mutableStateOf<String?>(null) }
    var errorPulseKey by rememberSaveable { mutableIntStateOf(0) }
    var draftLoaded by remember { mutableStateOf(false) }
    var isSavingProfile by remember { mutableStateOf(false) }

    val isFirstVisitToCurrentStep = (visitedMask and (1 shl step)) == 0
    LaunchedEffect(step, draftLoaded) {
        if (draftLoaded) {
            delay(280L)
            visitedMask = visitedMask or (1 shl step)
        }
    }

    LaunchedEffect(store) {
        val draft = withContext(Dispatchers.IO) { runCatching { store.readOnboardingDraft() }.getOrNull() }
        if (draft != null) {
            step = draft.step.coerceIn(0, 6)
            ownerName = draft.ownerName
            email = draft.email
            phone = draft.phone
            businessTaxId = draft.ruc
        }
        draftLoaded = true
        KuotaAnalytics.track(
            KuotaAnalyticsEvent.ONBOARDING_STARTED,
            mapOf("step" to step.toString())
        )
    }

    LaunchedEffect(draftLoaded, step, ownerName, email, phone, businessTaxId) {
        if (draftLoaded && step in 1..6) {
            withContext(Dispatchers.IO) {
                runCatching {
                    // Nunca guardamos el PIN en el borrador; solo hasta el paso 6 (creación de PIN)
                    store.saveOnboardingDraft(
                        OnboardingDraft(step.coerceAtMost(6), false, ownerName.trim(), email.trim(), phone, businessTaxId)
                    )
                }
            }
        }
    }

    // Validación automática al completar los 6 dígitos en Confirmar PIN (Paso 7)
    LaunchedEffect(step, confirmPin, createdPin) {
        if (step == 7 && confirmPin.length == PinSecurityManager.PIN_LENGTH) {
            if (confirmPin != createdPin) {
                error = "Los PIN no coinciden. Inténtalo nuevamente."
                errorPulseKey++
                delay(260L)
                confirmPin = ""
            } else {
                error = null
            }
        }
    }

    fun finishOnboarding() {
        if (isSavingProfile) return
        if (confirmPin != createdPin || !PinSecurityManager.isValidPinFormat(confirmPin)) {
            error = "Los PIN no coinciden. Vuelve a ingresarlo."
            errorPulseKey++
            confirmPin = ""
            return
        }
        isSavingProfile = true
        error = null
        scope.launch {
            val saved = withContext(Dispatchers.IO) {
                runCatching {
                    val pinOk = PinSecurityManager.setupPin(context, confirmPin)
                    if (!pinOk) error("No se pudo configurar el PIN")
                    val existing = store.readBusinessProfile()
                    val base = existing ?: LocalBusinessProfile(email = "", phone = "", ruc = "")
                    store.saveBusinessProfile(
                        base.copy(
                            ownerName = ownerName.trim().ifBlank { existing?.ownerName.orEmpty() },
                            email = email.trim(),
                            phone = if (phone.isNotBlank()) phone else existing?.phone.orEmpty(),
                            ruc = if (businessTaxId.isNotBlank()) businessTaxId else existing?.ruc.orEmpty()
                        )
                    )
                }
            }
            isSavingProfile = false
            if (saved.isSuccess) {
                createdPin = ""
                confirmPin = ""
                KuotaAnalytics.track(
                    KuotaAnalyticsEvent.BUSINESS_PROFILE_COMPLETED,
                    mapOf("status" to "COMPLETED")
                )
                onOnboardingComplete()
            } else {
                error = "No se pudo guardar el perfil en este teléfono. Intenta nuevamente."
                errorPulseKey++
            }
        }
    }

    val isNameValid = ownerName.trim().length >= 2
    val isEmailValid = email.trim().matches(Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$"))
    val isPhoneValid = phone.filter(Char::isDigit).length == 9
    val isRucValid = businessTaxId.filter(Char::isDigit).length == 11
    val isCreatePinValid = createdPin.length == PinSecurityManager.PIN_LENGTH
    val isConfirmPinValid = confirmPin.length == PinSecurityManager.PIN_LENGTH && confirmPin == createdPin

    val canContinue = when (step) {
        2 -> isNameValid
        3 -> isEmailValid
        4 -> isPhoneValid
        5 -> isRucValid
        6 -> isCreatePinValid
        7 -> isConfirmPinValid
        else -> true
    }
    val lastStep = 7

    // Nivel superior: transición entre Pantalla 1 (Bienvenida), Pantalla 2 (Antes de empezar) y Flujo de Registro (Pasos 2..7)
    val stageBucket = when (step) {
        0 -> 0
        1 -> 1
        else -> 2
    }

    KuotaStepAnimatedContent(
        targetState = stageBucket,
        modifier = Modifier.fillMaxSize(),
        label = "OnboardingStageTransition"
    ) { currentStage ->
        when (currentStage) {
            0 -> {
                WelcomeHeroScreen(
                    animateEntrance = (visitedMask and 1) == 0,
                    onStartClick = {
                        error = null
                        step = 1
                    }
                )
            }

            1 -> {
                SecondLandingScreen(
                    animateEntrance = (visitedMask and (1 shl 1)) == 0,
                    onBackClick = {
                        error = null
                        step = 0
                    },
                    onContinueClick = {
                        error = null
                        step = 2
                    }
                )
            }

            else -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF02060E))
                ) {
                    Image(
                        painter = painterResource(R.drawable.kuota_welcome_bg),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.FillBounds,
                        alpha = 0.55f
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 24.dp, vertical = 20.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
                            // Cabecera Kuota + Atrás (fija y estable durante el registro)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Image(
                                        painter = painterResource(R.drawable.kuota_mark),
                                        contentDescription = "Logo Kuota",
                                        modifier = Modifier.height(38.dp)
                                    )
                                    Text(
                                        text = "kuota",
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color(0xFF2E7BFF)
                                    )
                                }
                                Spacer(Modifier.weight(1f))
                                TextButton(
                                    enabled = !isSavingProfile,
                                    onClick = {
                                        if (step > 0) {
                                            error = null
                                            step--
                                        }
                                    }
                                ) {
                                    Text(
                                        text = "Atrás",
                                        color = Color(0xFF2E8BFF),
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }

                            // Indicador de progreso sincronizado (5 pasos de registro)
                            val progressStepIndex = when (step) {
                                2 -> 1
                                3 -> 2
                                4 -> 3
                                5 -> 4
                                else -> 5
                            }
                            val progressLabel = when (step) {
                                2 -> "PASO 1 DE 5"
                                3 -> "2 de 5"
                                4 -> "3 DE 5"
                                5 -> "4 de 5"
                                else -> "5 de 5"
                            }
                            OnboardingStepProgress(
                                label = progressLabel,
                                currentStep = progressStepIndex,
                                totalSteps = 5
                            )

                            // Contenido animado paso a paso (avance y retroceso direccional)
                            KuotaStepAnimatedContent(
                                targetState = step.coerceIn(2, 7),
                                modifier = Modifier.fillMaxWidth(),
                                label = "OnboardingFormStepTransition"
                            ) { currentStep ->
                                val stepFirstVisit = (visitedMask and (1 shl currentStep)) == 0
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(18.dp)
                                ) {
                                    when (currentStep) {
                                        2 -> {
                                            // Pantalla 3: Nombre del cliente
                                            Column(
                                                modifier = Modifier.kuotaEntranceReveal(stepFirstVisit, delayMs = 0),
                                                verticalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Text(
                                                    text = "¿Cómo te llamas?",
                                                    color = Color.White,
                                                    fontSize = 31.sp,
                                                    fontWeight = FontWeight.ExtraBold
                                                )
                                                Text(
                                                    text = "Usaremos tu nombre para saludarte en la app.",
                                                    color = Color(0xFFA0B2C8),
                                                    fontSize = 15.sp
                                                )
                                            }
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(vertical = 6.dp)
                                                    .kuotaEntranceReveal(stepFirstVisit, delayMs = 45, initialOffsetY = 8.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Image(
                                                    painter = painterResource(R.drawable.kuota_onb_step1_avatar),
                                                    contentDescription = null,
                                                    modifier = Modifier.size(136.dp),
                                                    contentScale = ContentScale.Fit
                                                )
                                            }
                                            Column(
                                                modifier = Modifier.kuotaEntranceReveal(stepFirstVisit, delayMs = 80),
                                                verticalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                KuotaOnboardingInputBox(
                                                    topLabel = "Tu nombre",
                                                    value = ownerName,
                                                    onValueChange = { ownerName = it.take(60); error = null },
                                                    placeholder = "Escribe tu nombre",
                                                    leadingIcon = Icons.Default.PersonOutline,
                                                    keyboardType = KeyboardType.Text,
                                                    isValid = isNameValid
                                                )
                                                KuotaInlineValidationMessage(
                                                    visible = ownerName.isNotEmpty() && !isNameValid,
                                                    text = "Ingresa al menos 2 caracteres para continuar.",
                                                    isError = false
                                                )
                                            }
                                        }

                                        3 -> {
                                            // Pantalla 4: Correo electrónico
                                            val hasEmailFormatWarning = email.trim().length >= 4 && !isEmailValid
                                            Column(
                                                modifier = Modifier.kuotaEntranceReveal(stepFirstVisit, delayMs = 0),
                                                verticalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Text(
                                                    text = buildAnnotatedString {
                                                        withStyle(SpanStyle(color = Color.White, fontWeight = FontWeight.ExtraBold)) {
                                                            append("Ingresa tu ")
                                                        }
                                                        withStyle(SpanStyle(color = Color(0xFF23D5FF), fontWeight = FontWeight.ExtraBold)) {
                                                            append("correo")
                                                        }
                                                    },
                                                    fontSize = 31.sp
                                                )
                                                Text(
                                                    text = "Usaremos este correo para información\nimportante de tu negocio.",
                                                    color = Color(0xFFA0B2C8),
                                                    fontSize = 15.sp,
                                                    lineHeight = 21.sp
                                                )
                                            }
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(vertical = 4.dp)
                                                    .kuotaEntranceReveal(stepFirstVisit, delayMs = 45, initialOffsetY = 8.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Image(
                                                    painter = painterResource(R.drawable.kuota_onb_step2_email),
                                                    contentDescription = null,
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .height(148.dp),
                                                    contentScale = ContentScale.Fit
                                                )
                                            }
                                            Column(
                                                modifier = Modifier.kuotaEntranceReveal(stepFirstVisit, delayMs = 80),
                                                verticalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                KuotaOnboardingInputBox(
                                                    topLabel = null,
                                                    value = email,
                                                    onValueChange = { email = it.take(120); error = null },
                                                    placeholder = "nombre@empresa.com",
                                                    leadingIcon = Icons.Default.MailOutline,
                                                    keyboardType = KeyboardType.Email,
                                                    isValid = isEmailValid,
                                                    isError = hasEmailFormatWarning
                                                )
                                                KuotaInlineValidationMessage(
                                                    visible = hasEmailFormatWarning,
                                                    text = "Ingresa un formato de correo válido (ejemplo: nombre@empresa.com).",
                                                    isError = true
                                                )
                                                Text(
                                                    text = "Usa un correo al que tengas acceso.",
                                                    color = Color(0xFF7B93B2),
                                                    fontSize = 13.sp,
                                                    modifier = Modifier.padding(start = 4.dp)
                                                )
                                            }
                                        }

                                        4 -> {
                                            // Pantalla 5: Número de celular
                                            val cleanDigits = phone.filter(Char::isDigit)
                                            val hasPhoneWarning = cleanDigits.isNotEmpty() && cleanDigits.length < 9
                                            Column(
                                                modifier = Modifier.kuotaEntranceReveal(stepFirstVisit, delayMs = 0),
                                                verticalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Text(
                                                    text = buildAnnotatedString {
                                                        withStyle(SpanStyle(color = Color.White, fontWeight = FontWeight.ExtraBold)) {
                                                            append("Tu ")
                                                        }
                                                        withStyle(SpanStyle(color = Color(0xFF23D5FF), fontWeight = FontWeight.ExtraBold)) {
                                                            append("celular")
                                                        }
                                                    },
                                                    fontSize = 31.sp
                                                )
                                                Text(
                                                    text = "Para guardar los datos de contacto del negocio.",
                                                    color = Color(0xFFA0B2C8),
                                                    fontSize = 15.sp
                                                )
                                            }
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(vertical = 4.dp)
                                                    .kuotaEntranceReveal(stepFirstVisit, delayMs = 45, initialOffsetY = 8.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Image(
                                                    painter = painterResource(R.drawable.kuota_onb_step3_phone),
                                                    contentDescription = null,
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .height(210.dp),
                                                    contentScale = ContentScale.Fit
                                                )
                                            }
                                            Column(
                                                modifier = Modifier.kuotaEntranceReveal(stepFirstVisit, delayMs = 80),
                                                verticalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                KuotaOnboardingInputBox(
                                                    topLabel = "Tu celular",
                                                    value = phone,
                                                    onValueChange = { value ->
                                                        if (value.all(Char::isDigit) && value.length <= 9) phone = value
                                                        error = null
                                                    },
                                                    placeholder = "9XX XXX XXX",
                                                    prefix = "+51 ",
                                                    leadingIcon = Icons.Default.Phone,
                                                    keyboardType = KeyboardType.Phone,
                                                    isValid = isPhoneValid
                                                )
                                                KuotaInlineValidationMessage(
                                                    visible = hasPhoneWarning,
                                                    text = "Completa los 9 dígitos de tu número móvil (${cleanDigits.length}/9).",
                                                    isError = false
                                                )
                                            }
                                        }

                                        5 -> {
                                            // Pantalla 6: RUC del negocio
                                            val cleanRuc = businessTaxId.filter(Char::isDigit)
                                            val hasValidRucPrefix = cleanRuc.length < 2 ||
                                                cleanRuc.startsWith("10") ||
                                                cleanRuc.startsWith("15") ||
                                                cleanRuc.startsWith("17") ||
                                                cleanRuc.startsWith("20")
                                            Column(
                                                modifier = Modifier.kuotaEntranceReveal(stepFirstVisit, delayMs = 0),
                                                verticalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Text(
                                                    text = "Tu negocio",
                                                    color = Color.White,
                                                    fontSize = 31.sp,
                                                    fontWeight = FontWeight.ExtraBold
                                                )
                                                Text(
                                                    text = "Guarda el RUC para tenerlo a mano en este\nteléfono.",
                                                    color = Color(0xFFA0B2C8),
                                                    fontSize = 15.sp,
                                                    lineHeight = 21.sp
                                                )
                                            }
                                            Column(
                                                modifier = Modifier.kuotaEntranceReveal(stepFirstVisit, delayMs = 45),
                                                verticalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                KuotaOnboardingInputBox(
                                                    topLabel = "RUC",
                                                    value = businessTaxId,
                                                    onValueChange = { value ->
                                                        if (value.all(Char::isDigit) && value.length <= 11) businessTaxId = value
                                                        error = null
                                                    },
                                                    placeholder = "Ingresa los 11 dígitos de tu RUC",
                                                    leadingIcon = Icons.Default.Description,
                                                    keyboardType = KeyboardType.Number,
                                                    isValid = isRucValid,
                                                    isError = cleanRuc.length >= 2 && !hasValidRucPrefix
                                                )
                                                KuotaInlineValidationMessage(
                                                    visible = isRucValid,
                                                    text = "Formato de 11 dígitos completado (registro local en el dispositivo).",
                                                    isError = false,
                                                    isSuccess = true
                                                )
                                                KuotaInlineValidationMessage(
                                                    visible = cleanRuc.isNotEmpty() && !isRucValid,
                                                    text = "Ingresa los 11 dígitos numéricos del RUC (${cleanRuc.length}/11).",
                                                    isError = !hasValidRucPrefix
                                                )
                                            }
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(vertical = 2.dp)
                                                    .kuotaEntranceReveal(stepFirstVisit, delayMs = 80, initialOffsetY = 8.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Image(
                                                    painter = painterResource(R.drawable.kuota_onb_step4_ruc),
                                                    contentDescription = null,
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .height(185.dp),
                                                    contentScale = ContentScale.Fit
                                                )
                                            }
                                            Card(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .kuotaEntranceReveal(stepFirstVisit, delayMs = 110, initialOffsetY = 8.dp),
                                                shape = RoundedCornerShape(18.dp),
                                                colors = CardDefaults.cardColors(containerColor = Color(0xFF071630)),
                                                border = BorderStroke(1.dp, Color(0xFF1A4B96))
                                            ) {
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(horizontal = 16.dp, vertical = 14.dp),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                                                ) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(46.dp)
                                                            .clip(RoundedCornerShape(14.dp))
                                                            .background(Color(0xFF0C2A5E)),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.VerifiedUser,
                                                            contentDescription = null,
                                                            tint = Color(0xFF26D7FF),
                                                            modifier = Modifier.size(26.dp)
                                                        )
                                                    }
                                                    Box(
                                                        modifier = Modifier
                                                            .width(1.dp)
                                                            .height(34.dp)
                                                            .background(Color(0xFF1A4B96))
                                                    )
                                                    Text(
                                                        text = "Tu información se guarda cifrada\nen este dispositivo.",
                                                        color = Color(0xFFC5D4E8),
                                                        fontSize = 14.sp,
                                                        lineHeight = 19.sp
                                                    )
                                                }
                                            }
                                        }

                                        else -> {
                                            // Pantalla 7 (Crear PIN) y Pantalla 8 (Confirmar PIN)
                                            val isConfirmPinStep = currentStep == 7
                                            val activePinValue = if (isConfirmPinStep) confirmPin else createdPin
                                            Column(
                                                modifier = Modifier.kuotaEntranceReveal(stepFirstVisit, delayMs = 0),
                                                verticalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Text(
                                                    text = buildAnnotatedString {
                                                        withStyle(SpanStyle(color = Color.White, fontWeight = FontWeight.ExtraBold)) {
                                                            append(if (isConfirmPinStep) "Confirma tu " else "Crea tu ")
                                                        }
                                                        withStyle(SpanStyle(color = Color(0xFF23D5FF), fontWeight = FontWeight.ExtraBold)) {
                                                            append("PIN")
                                                        }
                                                    },
                                                    fontSize = 31.sp
                                                )
                                                Text(
                                                    text = if (isConfirmPinStep) {
                                                        "Ingresa nuevamente el PIN para confirmar\ny finalizar tu registro."
                                                    } else {
                                                        "Define un PIN de 6 dígitos para proteger tu cuenta\ncada vez que regreses a KUOTA."
                                                    },
                                                    color = Color(0xFFA0B2C8),
                                                    fontSize = 15.sp,
                                                    lineHeight = 21.sp
                                                )
                                            }

                                            Column(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(vertical = 12.dp)
                                                    .kuotaEntranceReveal(stepFirstVisit, delayMs = 45, initialOffsetY = 6.dp),
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                verticalArrangement = Arrangement.spacedBy(10.dp)
                                            ) {
                                                KuotaSixDigitCircles(
                                                    enteredPin = activePinValue,
                                                    showDigits = false,
                                                    hasError = error != null,
                                                    errorPulseKey = errorPulseKey
                                                )

                                                KuotaInlineValidationMessage(
                                                    visible = !isConfirmPinStep && isCreatePinValid && error == null,
                                                    text = "PIN de 6 dígitos completado. Pulsa Continuar.",
                                                    isError = false,
                                                    isSuccess = true
                                                )
                                                KuotaInlineValidationMessage(
                                                    visible = isConfirmPinStep && isConfirmPinValid && error == null,
                                                    text = "PIN confirmado correctamente. Pulsa Finalizar registro.",
                                                    isError = false,
                                                    isSuccess = true
                                                )
                                            }

                                            Box(
                                                modifier = Modifier.kuotaEntranceReveal(stepFirstVisit, delayMs = 80, initialOffsetY = 8.dp)
                                            ) {
                                                KuotaSixDigitKeypad(
                                                    enabled = !isSavingProfile,
                                                    showBiometricKey = false,
                                                    onDigitClick = { digit ->
                                                        error = null
                                                        if (isConfirmPinStep) {
                                                            if (confirmPin.length < PinSecurityManager.PIN_LENGTH) {
                                                                confirmPin += digit
                                                            }
                                                        } else {
                                                            if (createdPin.length < PinSecurityManager.PIN_LENGTH) {
                                                                createdPin += digit
                                                            }
                                                        }
                                                    },
                                                    onBackspaceClick = {
                                                        error = null
                                                        if (isConfirmPinStep) {
                                                            if (confirmPin.isNotEmpty()) confirmPin = confirmPin.dropLast(1)
                                                        } else {
                                                            if (createdPin.isNotEmpty()) createdPin = createdPin.dropLast(1)
                                                        }
                                                    },
                                                    onBiometricClick = {}
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            KuotaInlineValidationMessage(
                                visible = error != null,
                                text = error.orEmpty(),
                                isError = true,
                                modifier = Modifier.kuotaErrorMicroOffset(errorPulseKey)
                            )
                        }

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 24.dp, bottom = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            val buttonLabel = when {
                                isSavingProfile -> "Guardando…"
                                step == 5 -> "Guardar perfil"
                                step == lastStep -> "Finalizar registro"
                                else -> "Continuar"
                            }
                            val isEnabled = canContinue && !isSavingProfile && draftLoaded
                            KuotaGradientPillButton(
                                text = buttonLabel,
                                showArrow = step < 6,
                                enabled = isEnabled,
                                onClick = {
                                    error = null
                                    if (step == 6) {
                                        confirmPin = ""
                                        step = 7
                                    } else if (step == lastStep) {
                                        finishOnboarding()
                                    } else {
                                        step++
                                    }
                                }
                            )

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = Color(0xFF28B8FF),
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    text = when (step) {
                                        3 -> "Tu información se guarda de forma segura en este dispositivo."
                                        6, 7 -> "Tu información se guardará de forma segura en este dispositivo."
                                        else -> "Perfil local · guardado cifrado en este teléfono"
                                    },
                                    color = Color(0xFF8CA2C0),
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WelcomeHeroScreen(
    animateEntrance: Boolean,
    onStartClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF02060E))
    ) {
        Image(
            painter = painterResource(R.drawable.kuota_welcome_bg),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp)
                .padding(top = 80.dp, bottom = 52.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(Modifier.height(8.dp))

            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(R.drawable.kuota_welcome_logo),
                    contentDescription = "Kuota",
                    modifier = Modifier
                        .size(196.dp)
                        .kuotaEntranceReveal(
                            animateEntrance = animateEntrance,
                            delayMs = 0,
                            initialOffsetY = 8.dp
                        ),
                    contentScale = ContentScale.Fit
                )

                Spacer(Modifier.height(34.dp))

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.kuotaEntranceReveal(
                        animateEntrance = animateEntrance,
                        delayMs = 65,
                        initialOffsetY = 10.dp
                    )
                ) {
                    Text(
                        text = "Bienvenido a",
                        color = Color.White,
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = "Kuota",
                        style = TextStyle(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xFF3388FF),
                                    Color(0xFF36DCFF)
                                )
                            ),
                            fontSize = 46.sp,
                            fontWeight = FontWeight.ExtraBold,
                            textAlign = TextAlign.Center
                        )
                    )
                }

                Spacer(Modifier.height(16.dp))

                Text(
                    text = "Tu negocio, más simple,\nmás cerca de ti.",
                    color = Color(0xFFA2B3C7),
                    fontSize = 17.sp,
                    lineHeight = 24.sp,
                    fontWeight = FontWeight.Normal,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.kuotaEntranceReveal(
                        animateEntrance = animateEntrance,
                        delayMs = 120,
                        initialOffsetY = 10.dp
                    )
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .kuotaEntranceReveal(
                        animateEntrance = animateEntrance,
                        delayMs = 170,
                        initialOffsetY = 10.dp
                    )
            ) {
                KuotaGradientPillButton(
                    text = "Comenzar",
                    showArrow = true,
                    enabled = true,
                    onClick = onStartClick
                )
            }
        }
    }
}

@Composable
private fun SecondLandingScreen(
    animateEntrance: Boolean,
    onBackClick: () -> Unit,
    onContinueClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF030B1A),
                        Color(0xFF02060E),
                        Color(0xFF030917)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onBackClick) {
                    Text(
                        text = "Atrás",
                        color = Color(0xFF2E8BFF),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.kuotaEntranceReveal(
                        animateEntrance = animateEntrance,
                        delayMs = 0,
                        initialOffsetY = 10.dp
                    )
                ) {
                    Text(
                        text = buildAnnotatedString {
                            withStyle(SpanStyle(color = Color.White, fontWeight = FontWeight.ExtraBold)) {
                                append("Kuota ")
                            }
                            withStyle(
                                SpanStyle(
                                    brush = Brush.horizontalGradient(
                                        listOf(Color(0xFF1DC8FF), Color(0xFF38E5FF))
                                    ),
                                    fontWeight = FontWeight.ExtraBold
                                )
                            ) {
                                append("para tu negocio")
                            }
                        },
                        fontSize = 31.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 36.sp
                    )

                    Text(
                        text = "Organiza tus ventas y lleva el control\nde tu negocio desde un solo lugar.",
                        color = Color(0xFFA8B9D0),
                        fontSize = 16.sp,
                        lineHeight = 22.sp,
                        textAlign = TextAlign.Center
                    )
                }

                Image(
                    painter = painterResource(R.drawable.kuota_onb_landing2_hero),
                    contentDescription = "Kuota para tu negocio",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(265.dp)
                        .padding(vertical = 4.dp)
                        .kuotaEntranceReveal(
                            animateEntrance = animateEntrance,
                            delayMs = 55,
                            initialOffsetY = 10.dp
                        ),
                    contentScale = ContentScale.Fit
                )

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .kuotaEntranceReveal(
                            animateEntrance = animateEntrance,
                            delayMs = 110,
                            initialOffsetY = 10.dp
                        ),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF071A3E)),
                    border = BorderStroke(1.dp, Color(0xFF184287))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 18.dp, vertical = 18.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0xFF0D316E)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Storefront,
                                contentDescription = null,
                                tint = Color(0xFF28D5FF),
                                modifier = Modifier.size(30.dp)
                            )
                        }

                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "PERFIL LOCAL Y PRIVADO",
                                color = Color(0xFF29C9FF),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.8.sp
                            )
                            Text(
                                text = "Tu negocio, a la mano.",
                                color = Color.White,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = "Tus datos se guardan cifrados en este teléfono.",
                                color = Color(0xFFA2B5CE),
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp, bottom = 12.dp)
                    .kuotaEntranceReveal(
                        animateEntrance = animateEntrance,
                        delayMs = 155,
                        initialOffsetY = 8.dp
                    ),
                contentAlignment = Alignment.Center
            ) {
                KuotaGradientPillButton(
                    text = "Continuar",
                    showArrow = true,
                    enabled = true,
                    onClick = onContinueClick
                )
            }
        }
    }
}

@Composable
private fun OnboardingStepProgress(
    label: String,
    currentStep: Int,
    totalSteps: Int
) {
    val animatorScale = rememberSystemAnimatorScale()
    val stateDuration = KuotaMotion.resolveDurationMs(KuotaMotion.STATE_DURATION_MS, animatorScale)

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = label,
            color = Color(0xFF28B8FF),
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.6.sp
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            for (i in 1..totalSteps) {
                val targetColor = when {
                    i == currentStep -> Color(0xFF25DCFF)
                    i < currentStep -> Color(0xFF1E5EC9)
                    else -> Color(0xFF132647)
                }
                val targetWidth = if (i == currentStep) 52.dp else 40.dp
                val pillColor by animateColorAsState(
                    targetValue = targetColor,
                    animationSpec = tween(durationMillis = stateDuration, easing = KuotaMotion.StandardEasing),
                    label = "stepPillColor_$i"
                )
                val pillWidth by animateDpAsState(
                    targetValue = targetWidth,
                    animationSpec = tween(durationMillis = stateDuration, easing = KuotaMotion.StandardEasing),
                    label = "stepPillWidth_$i"
                )
                Box(
                    modifier = Modifier
                        .width(pillWidth)
                        .height(6.dp)
                        .clip(RoundedCornerShape(50))
                        .background(pillColor)
                )
            }
        }
    }
}

@Composable
private fun KuotaOnboardingInputBox(
    topLabel: String?,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    leadingIcon: ImageVector,
    keyboardType: KeyboardType,
    prefix: String? = null,
    isValid: Boolean = false,
    isError: Boolean = false
) {
    val animatorScale = rememberSystemAnimatorScale()
    val microDuration = KuotaMotion.resolveDurationMs(KuotaMotion.MICRO_DURATION_MS, animatorScale)
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    val borderColor by animateColorAsState(
        targetValue = when {
            isError -> Color(0xFFFF6B6B)
            isFocused -> Color(0xFF28D5FF)
            isValid && value.isNotEmpty() -> Color(0xFF249CFF)
            else -> Color(0xFF1F64C9)
        },
        animationSpec = tween(durationMillis = microDuration, easing = KuotaMotion.StandardEasing),
        label = "inputBorderColor"
    )
    val borderWidth by animateDpAsState(
        targetValue = if (isFocused || isError) 1.8.dp else 1.2.dp,
        animationSpec = tween(durationMillis = microDuration, easing = KuotaMotion.StandardEasing),
        label = "inputBorderWidth"
    )
    val containerColor by animateColorAsState(
        targetValue = if (isFocused) Color(0xFF0A1B38) else Color(0xFF08162F),
        animationSpec = tween(durationMillis = microDuration, easing = KuotaMotion.StandardEasing),
        label = "inputContainerColor"
    )
    val iconTint by animateColorAsState(
        targetValue = when {
            isError -> Color(0xFFFF6B6B)
            isFocused || isValid -> Color(0xFF25DCFF)
            else -> Color(0xFF28C8FF)
        },
        animationSpec = tween(durationMillis = microDuration),
        label = "inputIconTint"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(containerColor)
            .border(borderWidth, borderColor, RoundedCornerShape(18.dp))
            .padding(horizontal = 18.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        if (topLabel != null && prefix == null && topLabel != "RUC") {
            Text(
                text = topLabel,
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = leadingIcon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(24.dp)
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                if (topLabel != null && (prefix != null || topLabel == "RUC")) {
                    Text(
                        text = topLabel,
                        color = if (topLabel == "RUC") Color.White else Color(0xFF9AB0CC),
                        fontSize = if (topLabel == "RUC") 15.sp else 13.sp,
                        fontWeight = if (topLabel == "RUC") FontWeight.Bold else FontWeight.Medium
                    )
                }
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    interactionSource = interactionSource,
                    singleLine = true,
                    textStyle = TextStyle(
                        color = Color.White,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                    cursorBrush = SolidColor(Color(0xFF28D5FF)),
                    decorationBox = { innerTextField ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (prefix != null) {
                                Text(
                                    text = prefix,
                                    color = if (value.isEmpty()) Color(0xFF6E84A3) else Color.White,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            Box(modifier = Modifier.weight(1f)) {
                                if (value.isEmpty()) {
                                    Text(
                                        text = placeholder,
                                        color = Color(0xFF6E84A3),
                                        fontSize = 16.sp
                                    )
                                }
                                innerTextField()
                            }
                        }
                    }
                )
            }

            AnimatedVisibility(
                visible = isValid && value.isNotEmpty() && !isError,
                enter = fadeIn(tween(microDuration)) + scaleIn(tween(microDuration), initialScale = 0.85f),
                exit = fadeOut(tween(microDuration)) + scaleOut(tween(microDuration), targetScale = 0.85f)
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Campo válido",
                    tint = Color(0xFF25DCFF),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun KuotaInlineValidationMessage(
    visible: Boolean,
    text: String,
    isError: Boolean,
    isSuccess: Boolean = false,
    modifier: Modifier = Modifier
) {
    val animatorScale = rememberSystemAnimatorScale()
    val duration = KuotaMotion.resolveDurationMs(KuotaMotion.MICRO_DURATION_MS, animatorScale)
    val messageColor by animateColorAsState(
        targetValue = when {
            isError -> Color(0xFFFF6B6B)
            isSuccess -> Color(0xFF25DCFF)
            else -> Color(0xFF8CA2C0)
        },
        animationSpec = tween(durationMillis = duration),
        label = "validationMsgColor"
    )

    AnimatedVisibility(
        visible = visible && text.isNotBlank(),
        enter = fadeIn(tween(duration)) + expandVertically(tween(duration), expandFrom = Alignment.Top),
        exit = fadeOut(tween(duration)) + shrinkVertically(tween(duration), shrinkTowards = Alignment.Top),
        modifier = modifier
    ) {
        Text(
            text = text,
            color = messageColor,
            fontSize = 13.sp,
            fontWeight = if (isError || isSuccess) FontWeight.Medium else FontWeight.Normal,
            modifier = Modifier.padding(start = 4.dp, top = 2.dp)
        )
    }
}

@Composable
private fun KuotaGradientPillButton(
    text: String,
    showArrow: Boolean,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val view = LocalView.current
    val animatorScale = rememberSystemAnimatorScale()
    val microDuration = KuotaMotion.resolveDurationMs(KuotaMotion.MICRO_DURATION_MS, animatorScale)
    val stateDuration = KuotaMotion.resolveDurationMs(KuotaMotion.STATE_DURATION_MS, animatorScale)

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    var lastClickTimeMs by remember { mutableLongStateOf(0L) }

    val scale by animateFloatAsState(
        targetValue = if (isPressed && enabled) 0.975f else 1.0f,
        animationSpec = tween(durationMillis = microDuration, easing = KuotaMotion.StandardEasing),
        label = "pillBtnScale"
    )
    val buttonAlpha by animateFloatAsState(
        targetValue = if (enabled) 1.0f else 0.72f,
        animationSpec = tween(durationMillis = stateDuration, easing = KuotaMotion.StandardEasing),
        label = "pillBtnAlpha"
    )
    val contentColor by animateColorAsState(
        targetValue = if (enabled) Color.White else Color(0xFF7B8FA8),
        animationSpec = tween(durationMillis = stateDuration),
        label = "pillBtnContentColor"
    )

    val activeBrush = Brush.horizontalGradient(
        colors = listOf(
            Color(0xFF0A64FF),
            Color(0xFF1F9DFF),
            Color(0xFF35DAFF)
        )
    )
    val disabledBrush = Brush.horizontalGradient(
        colors = listOf(
            Color(0xFF1A2536),
            Color(0xFF1E2B3E)
        )
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .scale(scale)
            .alpha(buttonAlpha)
            .then(
                if (enabled) {
                    Modifier.shadow(
                        elevation = 14.dp,
                        shape = RoundedCornerShape(50),
                        ambientColor = Color(0xFF1E9BFF),
                        spotColor = Color(0xFF35DAFF)
                    )
                } else {
                    Modifier
                }
            )
            .clip(RoundedCornerShape(50))
            .background(if (enabled) activeBrush else disabledBrush)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                onClick = {
                    val now = SystemClock.elapsedRealtime()
                    if (now - lastClickTimeMs >= 320L) {
                        lastClickTimeMs = now
                        KuotaMotion.performActionHaptic(view)
                        onClick()
                    }
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = text,
                color = contentColor,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            if (showArrow) {
                Spacer(Modifier.width(10.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
