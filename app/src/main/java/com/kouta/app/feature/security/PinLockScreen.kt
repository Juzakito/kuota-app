package com.kouta.app.feature.security

import android.content.Context
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.biometric.BiometricPrompt
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.kouta.app.R
import com.kouta.app.core.designsystem.motion.KuotaMotion
import com.kouta.app.core.designsystem.motion.kuotaErrorMicroOffset
import com.kouta.app.core.designsystem.motion.rememberSystemAnimatorScale
import com.kouta.app.core.push.KuotaEvents
import com.kouta.app.core.push.PushRegistrar
import com.kouta.app.core.security.BiometricAuthManager
import com.kouta.app.core.security.BiometricAuthResult
import com.kouta.app.core.security.BiometricCapability
import com.kouta.app.core.security.PinSecurityManager
import com.kouta.app.core.security.PinVerifyResult
import com.kouta.app.core.security.findFragmentActivity
import com.kouta.app.data.local.AppSettings
import com.kouta.app.data.local.LocalBusinessProfile
import com.kouta.app.data.local.SalesJournalStore
import com.kouta.app.data.local.TokenManager
import com.kouta.app.data.remote.ApiOutcome
import com.kouta.app.data.remote.KuotaApiClient
import com.kouta.app.data.remote.RequestOtpBody
import com.kouta.app.data.remote.VerifyOtpBody
import com.kuota.core.ui.theme.KuotaColors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Pantalla de bloqueo por PIN de 6 dígitos que protege el acceso a KUOTA al volver de segundo plano
 * o tras el reinicio del proceso.
 */
@Composable
fun PinLockBarrierScreen(
    profile: LocalBusinessProfile?,
    onUnlocked: () -> Unit,
    onSwitchUserReset: () -> Unit
) {
    BackHandler(enabled = true) {
        // Impide retroceder o saltar la barrera de seguridad mediante el botón o gesto Atrás.
    }

    val context = LocalContext.current
    val pinState by PinSecurityManager.stateFlow.collectAsState()
    var enteredPin by rememberSaveable { mutableStateOf("") }
    var showPinDigits by rememberSaveable { mutableStateOf(false) }
    var errorMessage by rememberSaveable { mutableStateOf<String?>(null) }
    var remainingLockoutSeconds by remember { mutableLongStateOf(0L) }
    var showRecoveryDialog by rememberSaveable { mutableStateOf(false) }
    var showSwitchUserDialog by rememberSaveable { mutableStateOf(false) }
    var isResettingPinAfterRecovery by rememberSaveable { mutableStateOf(false) }

    val canUseBiometrics = remember(context, pinState.biometricEnabled) {
        pinState.biometricEnabled && BiometricAuthManager.isAvailable(context)
    }
    var hasAutoPromptedBiometric by rememberSaveable { mutableStateOf(false) }

    // Actualiza la cuenta regresiva cuando existe un bloqueo temporal por intentos fallidos
    LaunchedEffect(pinState.lockoutUntilEpochMillis, pinState.failedAttempts) {
        while (true) {
            val now = System.currentTimeMillis()
            val diff = pinState.lockoutUntilEpochMillis - now
            if (diff > 0L) {
                remainingLockoutSeconds = (diff + 999L) / 1000L
                delay(250L)
            } else {
                if (remainingLockoutSeconds > 0L) {
                    remainingLockoutSeconds = 0L
                    errorMessage = null
                }
                break
            }
        }
    }

    if (isResettingPinAfterRecovery) {
        PinSetupBarrierScreen(
            isRecoveryFlow = true,
            onPinConfigured = {
                isResettingPinAfterRecovery = false
                onUnlocked()
            }
        )
        return
    }

    val userDisplayName = profile?.ownerName?.trim()?.takeIf { it.isNotEmpty() }
        ?: profile?.businessName?.trim()?.takeIf { it.isNotEmpty() }
        ?: "Usuario Kuota"

    val userIdentifier = profile?.email?.trim()?.takeIf { it.isNotEmpty() }
        ?: profile?.phone?.trim()?.takeIf { it.isNotEmpty() }?.let { "+51 $it" }
        ?: profile?.ruc?.trim()?.takeIf { it.isNotEmpty() }?.let { "RUC $it" }
        ?: userDisplayName

    val isLockedOut = remainingLockoutSeconds > 0L

    fun handleDigitPress(digit: Char) {
        if (isLockedOut || enteredPin.length >= PinSecurityManager.PIN_LENGTH) return
        errorMessage = null
        val updated = enteredPin + digit
        enteredPin = updated
        if (updated.length == PinSecurityManager.PIN_LENGTH) {
            when (val result = PinSecurityManager.verifyPin(context, updated)) {
                is PinVerifyResult.Success -> {
                    enteredPin = ""
                    errorMessage = null
                    onUnlocked()
                }
                is PinVerifyResult.InvalidPin -> {
                    enteredPin = ""
                    errorMessage = "PIN incorrecto. Te queda${if (result.attemptsBeforeLockout == 1) "" else "n"} ${result.attemptsBeforeLockout} intento${if (result.attemptsBeforeLockout == 1) "" else "s"} antes del bloqueo temporal."
                }
                is PinVerifyResult.LockedOut -> {
                    enteredPin = ""
                    remainingLockoutSeconds = result.remainingSeconds
                    errorMessage = "Demasiados intentos fallidos (${result.failedAttempts}). Espera ${formatCooldown(result.remainingSeconds)}."
                }
            }
        }
    }

    fun handleBackspace() {
        if (isLockedOut || enteredPin.isEmpty()) return
        errorMessage = null
        enteredPin = enteredPin.dropLast(1)
    }

    fun triggerBiometricUnlock() {
        if (isLockedOut) return
        val activity = context.findFragmentActivity() ?: return
        BiometricAuthManager.authenticate(
            activity = activity,
            title = "Desbloquea KUOTA",
            subtitle = "Confirma tu identidad para continuar",
            negativeButtonText = "Usar PIN",
            onResult = { result ->
                when (result) {
                    is BiometricAuthResult.Success -> {
                        PinSecurityManager.onBiometricAuthenticated(context)
                        enteredPin = ""
                        errorMessage = null
                        onUnlocked()
                    }
                    is BiometricAuthResult.NegativeButtonPin -> {
                        errorMessage = null
                    }
                    is BiometricAuthResult.UserCanceled -> {
                        // Diálogo cancelado; el usuario puede teclear su PIN de 6 dígitos
                    }
                    is BiometricAuthResult.Failed -> {
                        errorMessage = "Huella no reconocida. Intenta de nuevo o ingresa tu PIN."
                    }
                    is BiometricAuthResult.Lockout -> {
                        errorMessage = if (result.isPermanent) {
                            "Sensor biométrico bloqueado permanentemente. Ingresa tu PIN de 6 dígitos."
                        } else {
                            "Demasiados intentos con huella. Espera 30 s o ingresa tu PIN."
                        }
                    }
                    is BiometricAuthResult.Error -> {
                        if (result.errorCode != BiometricPrompt.ERROR_USER_CANCELED &&
                            result.errorCode != BiometricPrompt.ERROR_CANCELED
                        ) {
                            errorMessage = result.message
                        }
                    }
                }
            }
        )
    }

    LaunchedEffect(canUseBiometrics) {
        if (canUseBiometrics && !hasAutoPromptedBiometric && !isLockedOut) {
            hasAutoPromptedBiometric = true
            delay(150L)
            triggerBiometricUnlock()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(KuotaColors.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Bloque superior: Avatar + "Ingresa tu clave" + Identificador del perfil + Indicadores
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                LockScreenUserAvatar(name = userDisplayName)

                Spacer(Modifier.height(2.dp))

                Text(
                    text = "Desbloquea KUOTA",
                    color = KuotaColors.PrussianBlue,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "Confirma tu identidad para continuar",
                    color = KuotaColors.PrussianBlue.copy(alpha = 0.7f),
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center
                )

                Surface(
                    shape = RoundedCornerShape(50),
                    color = KuotaColors.Lavender,
                    border = BorderStroke(1.dp, KuotaColors.Periwinkle)
                ) {
                    Text(
                        text = userIdentifier,
                        color = KuotaColors.PrussianBlue,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 5.dp)
                    )
                }

                Spacer(Modifier.height(8.dp))

                // 6 círculos + botón mostrar/ocultar PIN
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Spacer(Modifier.width(36.dp))
                    KuotaSixDigitCircles(
                        enteredPin = enteredPin,
                        showDigits = showPinDigits,
                        hasError = errorMessage != null || isLockedOut
                    )
                    IconButton(
                        onClick = { showPinDigits = !showPinDigits },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = if (showPinDigits) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = if (showPinDigits) "Ocultar PIN" else "Mostrar PIN",
                            tint = KuotaColors.PersianBlue,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                if (isLockedOut) {
                    Text(
                        text = "Bloqueo temporal por seguridad · intenta en ${formatCooldown(remainingLockoutSeconds)}",
                        color = KuotaColors.AlarmRed,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                } else if (errorMessage != null) {
                    Text(
                        text = errorMessage.orEmpty(),
                        color = KuotaColors.AlarmRed,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                } else {
                    Text(
                        text = if (canUseBiometrics) "Usa tu huella digital o ingresa tu PIN de 6 dígitos" else "PIN de seguridad de 6 dígitos",
                        color = KuotaColors.PrussianBlue.copy(alpha = 0.65f),
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )
                }

                if (canUseBiometrics && !isLockedOut) {
                    OutlinedButton(
                        onClick = ::triggerBiometricUnlock,
                        modifier = Modifier
                            .fillMaxWidth(0.84f)
                            .height(42.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, KuotaColors.Periwinkle),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = KuotaColors.PersianBlue,
                            containerColor = KuotaColors.Lavender
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Fingerprint,
                            contentDescription = null,
                            tint = KuotaColors.PersianBlue,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "Desbloquear con biometría",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            color = KuotaColors.PersianBlue
                        )
                    }
                }
            }

            // Teclado numérico personalizado de 6 dígitos (0-9 + borrar + biometría opcional)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                KuotaSixDigitKeypad(
                    enabled = !isLockedOut,
                    showBiometricKey = canUseBiometrics,
                    onDigitClick = ::handleDigitPress,
                    onBackspaceClick = ::handleBackspace,
                    onBiometricClick = ::triggerBiometricUnlock
                )

                // Acciones reales de recuperación y cambio de usuario
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = { showRecoveryDialog = true }) {
                        Text(
                            text = "Olvidé mi clave",
                            color = KuotaColors.PersianBlue,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Text(
                        text = "·",
                        color = KuotaColors.Periwinkle,
                        fontSize = 14.sp
                    )
                    TextButton(onClick = { showSwitchUserDialog = true }) {
                        Text(
                            text = "No soy ${userDisplayName.split(" ").firstOrNull() ?: "este usuario"}",
                            color = KuotaColors.PrussianBlue.copy(alpha = 0.65f),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }

    if (showRecoveryDialog) {
        PinRecoveryDialog(
            profile = profile,
            onDismiss = { showRecoveryDialog = false },
            onRecoveryVerified = {
                showRecoveryDialog = false
                isResettingPinAfterRecovery = true
            }
        )
    }

    if (showSwitchUserDialog) {
        SwitchUserConfirmationDialog(
            userDisplayName = userDisplayName,
            onDismiss = { showSwitchUserDialog = false },
            onConfirmSwitchUser = {
                showSwitchUserDialog = false
                onSwitchUserReset()
            }
        )
    }
}

/**
 * Barrera de creación y confirmación de PIN de 6 dígitos para usuarios existentes que aún no tenían
 * un PIN configurado o que acaban de verificar su identidad en el flujo de recuperación.
 */
@Composable
fun PinSetupBarrierScreen(
    isRecoveryFlow: Boolean = false,
    onPinConfigured: () -> Unit,
    onCancelIfOptional: (() -> Unit)? = null
) {
    BackHandler(enabled = onCancelIfOptional == null) {
        // Si es obligatorio, bloquea el botón Atrás.
    }

    val context = LocalContext.current
    var isConfirmStage by rememberSaveable { mutableStateOf(false) }
    var firstPin by rememberSaveable { mutableStateOf("") }
    var currentPin by rememberSaveable { mutableStateOf("") }
    var showPinDigits by rememberSaveable { mutableStateOf(false) }
    var errorText by rememberSaveable { mutableStateOf<String?>(null) }
    var showBiometricOptInDialog by rememberSaveable { mutableStateOf(false) }
    var biometricOptInError by rememberSaveable { mutableStateOf<String?>(null) }

    fun onDigit(digit: Char) {
        if (currentPin.length >= PinSecurityManager.PIN_LENGTH) return
        errorText = null
        currentPin += digit
    }

    fun onBackspace() {
        if (currentPin.isEmpty()) return
        errorText = null
        currentPin = currentPin.dropLast(1)
    }

    fun onPrimaryAction() {
        if (currentPin.length != PinSecurityManager.PIN_LENGTH) return
        if (!isConfirmStage) {
            firstPin = currentPin
            currentPin = ""
            errorText = null
            isConfirmStage = true
        } else {
            if (currentPin != firstPin) {
                errorText = "Los PIN no coinciden. Inténtalo de nuevo."
                currentPin = ""
            } else {
                val saved = PinSecurityManager.setupPin(context, currentPin)
                if (saved) {
                    val capability = BiometricAuthManager.checkCapability(context)
                    if (capability is BiometricCapability.Available && !isRecoveryFlow) {
                        showBiometricOptInDialog = true
                    } else {
                        Toast.makeText(context, "PIN de 6 dígitos configurado con éxito.", Toast.LENGTH_SHORT).show()
                        onPinConfigured()
                    }
                } else {
                    errorText = "No se pudo guardar el PIN seguro. Intenta nuevamente."
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(KuotaColors.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 22.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(R.drawable.ic_kuota_logo_horizontal),
                        contentDescription = "Logo Kuota",
                        modifier = Modifier.height(32.dp),
                        contentScale = ContentScale.Fit
                    )
                    Spacer(Modifier.weight(1f))
                    if (isConfirmStage) {
                        TextButton(
                            onClick = {
                                isConfirmStage = false
                                currentPin = ""
                                errorText = null
                            }
                        ) {
                            Text("Atrás", color = KuotaColors.PersianBlue, fontWeight = FontWeight.SemiBold)
                        }
                    } else if (onCancelIfOptional != null) {
                        TextButton(onClick = onCancelIfOptional) {
                            Text("Cancelar", color = KuotaColors.PersianBlue, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = buildAnnotatedString {
                            withStyle(SpanStyle(color = KuotaColors.PrussianBlue, fontWeight = FontWeight.ExtraBold)) {
                                append(if (!isConfirmStage) "Crea tu " else "Confirma tu ")
                            }
                            withStyle(SpanStyle(color = KuotaColors.PersianBlue, fontWeight = FontWeight.ExtraBold)) {
                                append("PIN")
                            }
                        },
                        fontSize = 31.sp
                    )
                    Text(
                        text = if (!isConfirmStage) {
                            if (isRecoveryFlow) {
                                "Define tu nuevo PIN de 6 dígitos para proteger el acceso a tu cuenta."
                            } else {
                                "Configura una clave de 6 dígitos para proteger tu información al volver a KUOTA."
                            }
                        } else {
                            "Ingresa nuevamente el PIN para confirmar\ny activar tu clave de seguridad."
                        },
                        color = KuotaColors.PrussianBlue.copy(alpha = 0.7f),
                        fontSize = 15.sp,
                        lineHeight = 21.sp
                    )
                }
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Spacer(Modifier.width(36.dp))
                    KuotaSixDigitCircles(
                        enteredPin = currentPin,
                        showDigits = showPinDigits,
                        hasError = errorText != null
                    )
                    IconButton(
                        onClick = { showPinDigits = !showPinDigits },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = if (showPinDigits) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = if (showPinDigits) "Ocultar PIN" else "Mostrar PIN",
                            tint = KuotaColors.PersianBlue,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                if (errorText != null) {
                    Text(
                        text = errorText.orEmpty(),
                        color = KuotaColors.AlarmRed,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                KuotaSixDigitKeypad(
                    enabled = true,
                    showBiometricKey = false,
                    onDigitClick = ::onDigit,
                    onBackspaceClick = ::onBackspace,
                    onBiometricClick = {}
                )

                val isReady = currentPin.length == PinSecurityManager.PIN_LENGTH

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .clip(RoundedCornerShape(50))
                        .background(if (isReady) KuotaColors.PersianBlue else KuotaColors.Lavender)
                        .then(
                            if (!isReady) Modifier.border(1.dp, KuotaColors.Periwinkle.copy(alpha = 0.6f), RoundedCornerShape(50))
                            else Modifier
                        )
                        .clickable(enabled = isReady, onClick = ::onPrimaryAction),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (!isConfirmStage) "Continuar" else "Guardar PIN",
                        color = if (isReady) KuotaColors.White else KuotaColors.Periwinkle,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = KuotaColors.PersianBlue,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "Tu información se guardará de forma segura en este dispositivo.",
                        color = KuotaColors.PrussianBlue.copy(alpha = 0.65f),
                        fontSize = 12.sp
                    )
                }
            }
        }
    }

    if (showBiometricOptInDialog) {
        AlertDialog(
            onDismissRequest = {
                PinSecurityManager.setBiometricEnabled(context, false)
                showBiometricOptInDialog = false
                onPinConfigured()
            },
            containerColor = KuotaColors.White,
            titleContentColor = KuotaColors.PrussianBlue,
            textContentColor = KuotaColors.PrussianBlue.copy(alpha = 0.8f),
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Fingerprint,
                        contentDescription = null,
                        tint = KuotaColors.PersianBlue,
                        modifier = Modifier.size(28.dp)
                    )
                    Text(
                        text = "¿Activar desbloqueo biométrico?",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = KuotaColors.PrussianBlue
                    )
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Ingresa a KUOTA de forma rápida y segura usando tu huella digital o datos biométricos admitidos por tu teléfono.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = KuotaColors.PrussianBlue.copy(alpha = 0.8f)
                    )
                    Text(
                        text = "Tu PIN de 6 dígitos seguirá siempre disponible como método de respaldo.",
                        style = MaterialTheme.typography.bodySmall,
                        color = KuotaColors.PrussianBlue.copy(alpha = 0.6f)
                    )
                    if (biometricOptInError != null) {
                        Text(
                            text = biometricOptInError.orEmpty(),
                            color = KuotaColors.AlarmRed,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val activity = context.findFragmentActivity()
                        if (activity == null) {
                            biometricOptInError = "No se pudo iniciar el lector biométrico."
                            return@Button
                        }
                        BiometricAuthManager.authenticate(
                            activity = activity,
                            title = "Verifica tu huella",
                            subtitle = "Toca el sensor biométrico para activar esta opción en KUOTA",
                            negativeButtonText = "Cancelar",
                            onResult = { result ->
                                when (result) {
                                    is BiometricAuthResult.Success -> {
                                        PinSecurityManager.setBiometricEnabled(context, true)
                                        showBiometricOptInDialog = false
                                        Toast.makeText(context, "Desbloqueo biométrico activado con éxito.", Toast.LENGTH_SHORT).show()
                                        onPinConfigured()
                                    }
                                    is BiometricAuthResult.Failed -> {
                                        biometricOptInError = "Huella no reconocida. Intenta nuevamente."
                                    }
                                    is BiometricAuthResult.UserCanceled,
                                    is BiometricAuthResult.NegativeButtonPin -> {
                                        biometricOptInError = "Autenticación cancelada. Podrás activarla más tarde en Menú."
                                    }
                                    is BiometricAuthResult.Lockout -> {
                                        biometricOptInError = "Sensor biométrico bloqueado temporalmente por intentos fallidos."
                                    }
                                    is BiometricAuthResult.Error -> {
                                        biometricOptInError = result.message
                                    }
                                }
                            }
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = KuotaColors.PersianBlue)
                ) {
                    Text("Activar ahora", fontWeight = FontWeight.Bold, color = KuotaColors.White)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        PinSecurityManager.setBiometricEnabled(context, false)
                        showBiometricOptInDialog = false
                        Toast.makeText(context, "PIN de 6 dígitos configurado.", Toast.LENGTH_SHORT).show()
                        onPinConfigured()
                    }
                ) {
                    Text("Ahora no, usar solo PIN", color = KuotaColors.PrussianBlue.copy(alpha = 0.6f))
                }
            }
        )
    }
}

/**
 * Seis indicadores circulares para el PIN de 6 dígitos (fiel a la captura de referencia).
 * Anima únicamente el indicador recién introducido con un pulso discreto de escala,
 * respetando la configuración de reducción de movimiento del sistema.
 */
@Composable
fun KuotaSixDigitCircles(
    enteredPin: String,
    showDigits: Boolean = false,
    hasError: Boolean = false,
    errorPulseKey: Int = 0
) {
    val animatorScale = rememberSystemAnimatorScale()
    var previousLength by remember { mutableIntStateOf(enteredPin.length) }
    var activePulseIndex by remember { mutableIntStateOf(-1) }

    LaunchedEffect(enteredPin.length) {
        if (enteredPin.length > previousLength && enteredPin.isNotEmpty()) {
            activePulseIndex = enteredPin.length - 1
        } else {
            activePulseIndex = -1
        }
        previousLength = enteredPin.length
    }

    val effectiveErrorKey = remember(hasError, errorPulseKey) {
        if (errorPulseKey > 0) errorPulseKey else if (hasError) 1 else 0
    }

    Row(
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .kuotaErrorMicroOffset(effectiveErrorKey)
            .semantics {
                contentDescription = "${enteredPin.length} de ${PinSecurityManager.PIN_LENGTH} dígitos ingresados"
            }
    ) {
        for (i in 0 until PinSecurityManager.PIN_LENGTH) {
            val isFilled = i < enteredPin.length
            val dotScale = remember(i) { Animatable(1f) }

            LaunchedEffect(isFilled, activePulseIndex) {
                if (isFilled && activePulseIndex == i && animatorScale > 0f) {
                    val upMs = KuotaMotion.resolveDurationMs(85, animatorScale)
                    val downMs = KuotaMotion.resolveDurationMs(105, animatorScale)
                    dotScale.animateTo(1.16f, tween(upMs, easing = KuotaMotion.DecelerateEasing))
                    dotScale.animateTo(1.0f, tween(downMs, easing = KuotaMotion.StandardEasing))
                } else {
                    dotScale.snapTo(1.0f)
                }
            }

            val colorDuration = KuotaMotion.resolveDurationMs(140, animatorScale)
            val borderColor by animateColorAsState(
                targetValue = when {
                    hasError -> KuotaColors.AlarmRed
                    isFilled -> KuotaColors.PersianBlue
                    else -> KuotaColors.Periwinkle
                },
                animationSpec = tween(durationMillis = colorDuration),
                label = "pinDotBorder"
            )
            val fillColor by animateColorAsState(
                targetValue = when {
                    hasError && isFilled -> KuotaColors.AlarmRed
                    isFilled && !showDigits -> KuotaColors.PersianBlue
                    isFilled && showDigits -> KuotaColors.White
                    else -> KuotaColors.Lavender
                },
                animationSpec = tween(durationMillis = colorDuration),
                label = "pinDotFill"
            )

            Box(
                modifier = Modifier
                    .size(30.dp)
                    .scale(dotScale.value)
                    .clip(CircleShape)
                    .background(fillColor)
                    .border(1.5.dp, borderColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (isFilled && showDigits) {
                    Text(
                        text = enteredPin[i].toString(),
                        color = KuotaColors.PersianBlue,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }
    }
}

/**
 * Teclado numérico personalizado de 3x4 para introducir el PIN de 6 dígitos (fiel a la captura adjunta).
 * Incluye respuesta háptica ligera y microinteracción de escala instantánea sin retrasar la entrada.
 */
@Composable
fun KuotaSixDigitKeypad(
    enabled: Boolean,
    showBiometricKey: Boolean,
    onDigitClick: (Char) -> Unit,
    onBackspaceClick: () -> Unit,
    onBiometricClick: () -> Unit
) {
    val view = LocalView.current
    val rows = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9")
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        rows.forEach { rowDigits ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                rowDigits.forEach { digitStr ->
                    PinKeyCell(
                        modifier = Modifier.weight(1f),
                        enabled = enabled,
                        onClick = {
                            KuotaMotion.performKeyTapHaptic(view)
                            onDigitClick(digitStr[0])
                        }
                    ) {
                        Text(
                            text = digitStr,
                            color = if (enabled) KuotaColors.PrussianBlue else KuotaColors.Periwinkle,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        // Cuarta fila: [Biometría / Vacío] [0] [Borrar ⌫]
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (showBiometricKey) {
                PinKeyCell(
                    modifier = Modifier.weight(1f),
                    enabled = enabled,
                    onClick = {
                        KuotaMotion.performKeyTapHaptic(view)
                        onBiometricClick()
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Fingerprint,
                        contentDescription = "Desbloquear con huella o biometría",
                        tint = if (enabled) KuotaColors.PersianBlue else KuotaColors.Periwinkle,
                        modifier = Modifier.size(26.dp)
                    )
                }
            } else {
                Spacer(modifier = Modifier.weight(1f).height(58.dp))
            }

            PinKeyCell(
                modifier = Modifier.weight(1f),
                enabled = enabled,
                onClick = {
                    KuotaMotion.performKeyTapHaptic(view)
                    onDigitClick('0')
                }
            ) {
                Text(
                    text = "0",
                    color = if (enabled) KuotaColors.PrussianBlue else KuotaColors.Periwinkle,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            PinKeyCell(
                modifier = Modifier.weight(1f),
                enabled = enabled,
                isBackspaceKey = true,
                onClick = {
                    KuotaMotion.performKeyTapHaptic(view)
                    onBackspaceClick()
                }
            ) { isPressed ->
                val iconScale by animateFloatAsState(
                    targetValue = if (isPressed) 0.88f else 1.0f,
                    animationSpec = tween(durationMillis = 110),
                    label = "backspaceIconScale"
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Backspace,
                    contentDescription = "Borrar último dígito",
                    tint = if (!enabled) KuotaColors.Periwinkle else if (isPressed) KuotaColors.PersianBlue else KuotaColors.PrussianBlue,
                    modifier = Modifier
                        .size(22.dp)
                        .scale(iconScale)
                )
            }
        }
    }
}

@Composable
private fun PinKeyCell(
    modifier: Modifier = Modifier,
    enabled: Boolean,
    isBackspaceKey: Boolean = false,
    onClick: () -> Unit,
    content: @Composable (isPressed: Boolean) -> Unit
) {
    val animatorScale = rememberSystemAnimatorScale()
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val microDuration = KuotaMotion.resolveDurationMs(120, animatorScale)

    val keyScale by animateFloatAsState(
        targetValue = if (isPressed && enabled) 0.96f else 1.0f,
        animationSpec = tween(durationMillis = microDuration, easing = KuotaMotion.StandardEasing),
        label = "pinKeyScale"
    )
    val bgColor by animateColorAsState(
        targetValue = if (isPressed && enabled) KuotaColors.Periwinkle else KuotaColors.Lavender,
        animationSpec = tween(durationMillis = microDuration),
        label = "pinKeyBg"
    )
    val borderColor by animateColorAsState(
        targetValue = if (isPressed && enabled) {
            KuotaColors.PersianBlue
        } else {
            KuotaColors.Periwinkle.copy(alpha = 0.6f)
        },
        animationSpec = tween(durationMillis = microDuration),
        label = "pinKeyBorder"
    )

    Box(
        modifier = modifier
            .height(58.dp)
            .scale(keyScale)
            .clip(RoundedCornerShape(22.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(22.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        content(isPressed)
    }
}

@Composable
private fun LockScreenUserAvatar(name: String) {
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
            .size(68.dp)
            .clip(CircleShape)
            .background(KuotaColors.Lavender)
            .border(2.dp, KuotaColors.PersianBlue, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        if (initials.isBlank()) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                tint = KuotaColors.PersianBlue,
                modifier = Modifier.size(34.dp)
            )
        } else {
            Text(
                text = initials,
                color = KuotaColors.PersianBlue,
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}

@Composable
private fun PinRecoveryDialog(
    profile: LocalBusinessProfile?,
    onDismiss: () -> Unit,
    onRecoveryVerified: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val hasRemoteSession = remember(context) { KuotaApiClient.hasSession(context) }
    val registeredPhone = profile?.phone?.filter(Char::isDigit).orEmpty()
    val registeredRuc = profile?.ruc?.filter(Char::isDigit).orEmpty()

    var mode by rememberSaveable { mutableIntStateOf(if (hasRemoteSession) 0 else 1) } // 0 = OTP servidor, 1 = Verificación perfil local
    var otpSent by rememberSaveable { mutableStateOf(false) }
    var otpCode by rememberSaveable { mutableStateOf("") }
    var inputRuc by rememberSaveable { mutableStateOf("") }
    var inputPhone by rememberSaveable { mutableStateOf("") }
    var isBusy by remember { mutableStateOf(false) }
    var errorMsg by rememberSaveable { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = { if (!isBusy) onDismiss() },
        title = {
            Text("Recuperar acceso", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (mode == 0 && registeredPhone.length == 9) {
                    Text(
                        "Tu cuenta está vinculada. Enviaremos un código OTP al celular terminado en ***${registeredPhone.takeLast(3)} para autorizar el cambio de tu PIN.",
                        style = MaterialTheme.typography.bodySmall
                    )
                    if (!otpSent) {
                        Button(
                            onClick = {
                                isBusy = true
                                errorMsg = null
                                scope.launch {
                                    val outcome = withContext(Dispatchers.IO) {
                                        KuotaApiClient.call(context) {
                                            requestOtp(RequestOtpBody(phone = registeredPhone))
                                        }
                                    }
                                    isBusy = false
                                    when (outcome) {
                                        is ApiOutcome.Ok -> otpSent = true
                                        else -> {
                                            // Si el servidor no está accesible en este momento, ofrece verificación local verificada
                                            errorMsg = "Servidor no disponible en este momento. Puedes validar los datos registrados de tu MYPE."
                                            mode = 1
                                        }
                                    }
                                }
                            },
                            enabled = !isBusy,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(if (isBusy) "Enviando…" else "Enviar código OTP")
                        }
                    } else {
                        OutlinedTextField(
                            value = otpCode,
                            onValueChange = { if (it.all(Char::isDigit) && it.length <= 6) otpCode = it; errorMsg = null },
                            label = { Text("Código OTP (6 dígitos)") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Button(
                            onClick = {
                                isBusy = true
                                errorMsg = null
                                scope.launch {
                                    val outcome = withContext(Dispatchers.IO) {
                                        KuotaApiClient.call(context) {
                                            verifyOtp(VerifyOtpBody(phone = registeredPhone, otpCode = otpCode))
                                        }
                                    }
                                    isBusy = false
                                    when (outcome) {
                                        is ApiOutcome.Ok -> onRecoveryVerified()
                                        is ApiOutcome.Rejected -> errorMsg = outcome.message.ifBlank { "Código OTP inválido." }
                                        else -> errorMsg = "No se pudo verificar con el servidor."
                                    }
                                }
                            },
                            enabled = otpCode.length == 6 && !isBusy,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(if (isBusy) "Verificando…" else "Verificar OTP y crear nuevo PIN")
                        }
                    }
                    TextButton(onClick = { mode = 1; errorMsg = null }) {
                        Text("Validar con RUC y celular del negocio")
                    }
                } else {
                    Text(
                        "Este perfil está cifrado localmente en tu dispositivo. No existe recuperación remota sin sesión activa. Para restablecer tu PIN sin perder tus datos, confirma el RUC y el celular registrados en este teléfono:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = inputRuc,
                        onValueChange = { if (it.all(Char::isDigit) && it.length <= 11) inputRuc = it; errorMsg = null },
                        label = { Text("RUC registrado (11 dígitos)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = inputPhone,
                        onValueChange = { if (it.all(Char::isDigit) && it.length <= 9) inputPhone = it; errorMsg = null },
                        label = { Text("Celular registrado (9 dígitos)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Button(
                        onClick = {
                            if (registeredRuc.isNotBlank() && registeredPhone.isNotBlank() &&
                                inputRuc == registeredRuc && inputPhone == registeredPhone
                            ) {
                                onRecoveryVerified()
                            } else {
                                errorMsg = "El RUC o el celular no coinciden con el perfil registrado en este dispositivo."
                            }
                        },
                        enabled = inputRuc.length == 11 && inputPhone.length == 9,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Verificar identidad y restablecer PIN")
                    }
                }

                errorMsg?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isBusy) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
private fun SwitchUserConfirmationDialog(
    userDisplayName: String,
    onDismiss: () -> Unit,
    onConfirmSwitchUser: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isWorking by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = { if (!isWorking) onDismiss() },
        title = {
            Text("¿No eres $userDisplayName?", fontWeight = FontWeight.Bold)
        },
        text = {
            Text(
                "Para proteger la información privada de este negocio, cambiar de usuario cerrará la sesión vinculada y restablecerá el perfil activo en este dispositivo para volver a la pantalla de registro inicial.",
                style = MaterialTheme.typography.bodySmall
            )
        },
        confirmButton = {
            OutlinedButton(
                enabled = !isWorking,
                onClick = {
                    isWorking = true
                    scope.launch {
                        withContext(Dispatchers.IO) {
                            runCatching { PushRegistrar.unregister(context) }
                            val store = SalesJournalStore(context)
                            val tokenManager = TokenManager(context)
                            val appSettings = AppSettings(context)
                            store.clearBusinessProfileAndDraft()
                            tokenManager.clearToken()
                            appSettings.clearSession()
                            appSettings.kycSummary = null
                            PinSecurityManager.clearPin(context)
                        }
                        isWorking = false
                        KuotaEvents.bump()
                        onConfirmSwitchUser()
                    }
                }
            ) {
                Text(
                    if (isWorking) "Cerrando…" else "Cambiar de usuario",
                    color = MaterialTheme.colorScheme.error
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isWorking) {
                Text("Cancelar")
            }
        }
    )
}

private fun formatCooldown(seconds: Long): String {
    val mins = seconds / 60
    val secs = seconds % 60
    return "%02d:%02d".format(mins, secs)
}
