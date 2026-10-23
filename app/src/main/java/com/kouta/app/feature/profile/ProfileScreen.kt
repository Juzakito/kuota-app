package com.kouta.app.feature.profile

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Surface
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.kouta.app.BuildConfig
import com.kouta.app.core.designsystem.components.GroupCard
import com.kouta.app.core.designsystem.components.KuotaAnimatedFeedback
import com.kouta.app.core.designsystem.components.KuotaFeedbackKind
import com.kouta.app.core.designsystem.components.MenuRow
import com.kouta.app.core.designsystem.components.ScreenHeader
import com.kouta.app.core.designsystem.components.SectionTitle
import com.kouta.app.core.designsystem.components.StatusPill
import com.kouta.app.core.designsystem.motion.KuotaMotion
import com.kouta.app.core.designsystem.motion.kuotaTabSectionReveal
import com.kouta.app.core.designsystem.motion.rememberSystemAnimatorScale
import com.kouta.app.core.designsystem.theme.ThemeController
import com.kouta.app.core.designsystem.theme.ThemeMode
import com.kouta.app.core.push.KuotaEvents
import com.kouta.app.core.push.PushRegistrar
import com.kouta.app.core.security.BiometricAuthManager
import com.kouta.app.core.security.BiometricAuthResult
import com.kouta.app.core.security.BiometricCapability
import com.kouta.app.core.security.PinSecurityManager
import com.kouta.app.core.security.findFragmentActivity
import com.kouta.app.core.sync.KuotaSync
import com.kouta.app.data.firebase.KuotaFirestoreService
import com.kouta.app.data.local.AppSettings
import com.kouta.app.data.local.LocalBusinessProfile
import com.kouta.app.data.local.SalesJournalStore
import com.kouta.app.data.local.TokenManager
import com.kouta.app.data.remote.KuotaApiClient
import com.kouta.app.feature.account.AccountLinkDialog
import com.kouta.app.feature.kyc.DniVerificationScreen
import com.kouta.app.feature.security.PinSetupBarrierScreen
import androidx.compose.material3.OutlinedButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun ProfileScreen(
    onLocalProfileDeleted: () -> Unit = {},
    onOpenSupport: (Boolean) -> Unit = {},
    onOpenNotifications: () -> Unit = {},
    onOpenLegal: () -> Unit = {},
    onOpenCards: () -> Unit = {}
) {
    val context = LocalContext.current
    val view = LocalView.current
    val animScale = rememberSystemAnimatorScale()
    val store = remember(context) { SalesJournalStore(context) }
    val appSettings = remember(context) { AppSettings(context) }
    val tokenManager = remember(context) { TokenManager(context) }
    val scope = rememberCoroutineScope()
    val revision by KuotaEvents.revision.collectAsState()
    val themeMode by ThemeController.mode.collectAsState()
    val pinState by PinSecurityManager.stateFlow.collectAsState()

    var isLoading by remember { mutableStateOf(true) }
    var isSaving by remember { mutableStateOf(false) }
    var isUnlinking by remember { mutableStateOf(false) }

    var existingProfile by remember { mutableStateOf<LocalBusinessProfile?>(null) }
    var ownerName by remember { mutableStateOf("") }
    var businessName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var ruc by remember { mutableStateOf("") }
    var preferredPaymentMethod by remember { mutableStateOf("Yape / Plin") }

    var hasSession by remember { mutableStateOf(false) }
    var kycSummary by remember { mutableStateOf<String?>(null) }

    var showEditForm by remember { mutableStateOf(false) }
    var showSecurityDialog by remember { mutableStateOf(false) }
    var showChangePinScreen by remember { mutableStateOf(false) }
    var showAccountDialog by remember { mutableStateOf(false) }
    var showLinkDialog by remember { mutableStateOf(false) }
    var showKycDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }

    var feedback by remember { mutableStateOf<String?>(null) }
    var isError by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val loaded = withContext(Dispatchers.IO) { runCatching { store.readBusinessProfile() }.getOrNull() }
        existingProfile = loaded
        loaded?.let {
            ownerName = it.ownerName
            businessName = it.businessName
            email = it.email
            phone = it.phone
            ruc = it.ruc
            preferredPaymentMethod = it.preferredPaymentMethod
        }
        hasSession = KuotaApiClient.hasSession(context)
        kycSummary = appSettings.kycSummary
        isLoading = false
    }

    LaunchedEffect(revision) {
        if (!isLoading) {
            hasSession = KuotaApiClient.hasSession(context)
            kycSummary = appSettings.kycSummary
        }
    }

    fun saveProfile() {
        val validEmail = email.trim().matches(Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$"))
        when {
            ownerName.isNotBlank() && ownerName.trim().length < 2 -> { feedback = "Revisa tu nombre."; isError = true }
            businessName.trim().length < 2 -> { feedback = "Ingresa el nombre comercial de tu negocio."; isError = true }
            !validEmail -> { feedback = "Revisa el correo electrónico."; isError = true }
            phone.isNotBlank() && phone.length != 9 -> { feedback = "El celular debe tener 9 dígitos."; isError = true }
            ruc.isNotBlank() && ruc.length != 11 -> { feedback = "El RUC debe tener 11 dígitos."; isError = true }
            else -> {
                isSaving = true
                feedback = null
                scope.launch {
                    val result = withContext(Dispatchers.IO) {
                        runCatching {
                            val base = existingProfile
                                ?: LocalBusinessProfile(email = email.trim(), phone = phone, ruc = ruc)
                            val updated = base.copy(
                                ownerName = ownerName.trim(),
                                businessName = businessName.trim(),
                                email = email.trim(),
                                phone = phone,
                                ruc = ruc,
                                preferredPaymentMethod = preferredPaymentMethod
                            )
                            store.saveBusinessProfile(updated)
                            runCatching { KuotaFirestoreService.saveMerchantProfile(context, updated) }
                            updated
                        }
                    }
                    isSaving = false
                    result.onSuccess {
                        existingProfile = it
                        isError = false
                        feedback = "Perfil guardado y sincronizado con Cloud Firestore."
                        KuotaEvents.bump()
                    }.onFailure {
                        isError = true
                        feedback = "No se pudo guardar el perfil."
                    }
                }
            }
        }
    }

    fun unlinkAccount() {
        if (isUnlinking) return
        isUnlinking = true
        scope.launch {
            withContext(Dispatchers.IO) {
                runCatching { PushRegistrar.unregister(context) }
                tokenManager.clearToken()
                appSettings.clearSession()
            }
            isUnlinking = false
            hasSession = false
            showAccountDialog = false
            KuotaEvents.bump()
            Toast.makeText(context, "Sesión cerrada. Los datos del negocio siguen en este teléfono.", Toast.LENGTH_SHORT).show()
        }
    }

    fun deleteProfile() {
        isSaving = true
        scope.launch {
            val result = withContext(Dispatchers.IO) {
                runCatching {
                    runCatching { PushRegistrar.unregister(context) }
                    store.clearBusinessProfileAndDraft()
                    tokenManager.clearToken()
                    appSettings.clearSession()
                    appSettings.kycSummary = null
                    PinSecurityManager.clearPin(context)
                }
            }
            isSaving = false
            if (result.isSuccess) {
                showDeleteConfirmation = false
                KuotaEvents.bump()
                onLocalProfileDeleted()
            } else {
                isError = true
                feedback = "No se pudo borrar el perfil local."
            }
        }
    }

    val linkedPhone = appSettings.linkedPhone?.takeIf { it.isNotBlank() }?.takeLast(9)
    val businessAndPhoneLine = listOf(
        businessName.trim().takeIf { it.isNotEmpty() },
        phone.takeIf { it.isNotEmpty() }?.let { "+51 $it" }
    ).filterNotNull().joinToString(" · ")

    if (showChangePinScreen) {
        PinSetupBarrierScreen(
            isRecoveryFlow = true,
            onPinConfigured = { showChangePinScreen = false },
            onCancelIfOptional = { showChangePinScreen = false }
        )
        return
    }

    val editChevronRotation by animateFloatAsState(
        targetValue = if (showEditForm) 180f else 0f,
        animationSpec = tween(durationMillis = (200 * animScale).toInt()),
        label = "profile_edit_chevron"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ScreenHeader("Menú", "Tu cuenta y preferencias")

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .kuotaTabSectionReveal(0),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    AccountAvatar(ownerName.ifBlank { businessName })
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            ownerName.ifBlank { businessName.ifBlank { "Tu cuenta" } },
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            businessAndPhoneLine.ifBlank { "Mi negocio MYPE" },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = .88f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (email.isNotBlank()) {
                            Text(
                                email.trim(),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = .72f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatusPill(
                        text = if (hasSession) "Cuenta vinculada" else "Solo local",
                        container = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = .12f),
                        content = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    StatusPill(
                        text = if (kycSummary != null) "DNI verificado" else "DNI pendiente",
                        container = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = .12f),
                        content = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .kuotaTabSectionReveal(1),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SectionTitle("Mi negocio")
            GroupCard {
                MenuRow(
                    title = "Datos del negocio",
                    subtitle = if (ruc.isNotBlank()) "RUC $ruc" else "Nombre, correo, celular y RUC",
                    icon = Icons.Default.Storefront,
                    trailing = {
                        Icon(
                            Icons.Default.ExpandMore,
                            contentDescription = null,
                            modifier = Modifier.rotate(editChevronRotation)
                        )
                    },
                    onClick = { showEditForm = !showEditForm; feedback = null }
                )
                AnimatedVisibility(
                    visible = showEditForm,
                    enter = fadeIn(tween((180 * animScale).toInt())) + expandVertically(tween((220 * animScale).toInt())),
                    exit = fadeOut(tween((140 * animScale).toInt())) + shrinkVertically(tween((180 * animScale).toInt()))
                ) {
                    Column {
                        HorizontalDivider()
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            if (isLoading) {
                                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                            } else {
                                OutlinedTextField(
                                    value = ownerName,
                                    onValueChange = { ownerName = it.take(60); feedback = null },
                                    modifier = Modifier.fillMaxWidth(),
                                    label = { Text("Tu nombre") },
                                    singleLine = true
                                )
                                OutlinedTextField(
                                    value = businessName,
                                    onValueChange = { businessName = it.take(100); feedback = null },
                                    modifier = Modifier.fillMaxWidth(),
                                    label = { Text("Nombre comercial de la MYPE") },
                                    singleLine = true
                                )
                                OutlinedTextField(
                                    value = email,
                                    onValueChange = { email = it.take(120); feedback = null },
                                    modifier = Modifier.fillMaxWidth(),
                                    label = { Text("Correo electrónico") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                    singleLine = true
                                )
                                OutlinedTextField(
                                    value = phone,
                                    onValueChange = { value -> if (value.all(Char::isDigit) && value.length <= 9) phone = value; feedback = null },
                                    modifier = Modifier.fillMaxWidth(),
                                    label = { Text("Celular (+51)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                    singleLine = true
                                )
                                OutlinedTextField(
                                    value = ruc,
                                    onValueChange = { value -> if (value.all(Char::isDigit) && value.length <= 11) ruc = value; feedback = null },
                                    modifier = Modifier.fillMaxWidth(),
                                    label = { Text("RUC (11 dígitos)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true
                                )
                                Text(
                                    "Se guardan cifrados con Android Keystore AES-256 en este teléfono.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                KuotaAnimatedFeedback(
                                    message = feedback,
                                    kind = if (isError) KuotaFeedbackKind.ERROR else KuotaFeedbackKind.SUCCESS
                                )
                                Button(
                                    onClick = ::saveProfile,
                                    enabled = !isSaving,
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp)
                                ) { Text(if (isSaving) "Guardando…" else "Guardar cambios") }
                            }
                        }
                    }
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .kuotaTabSectionReveal(2),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SectionTitle("Identidad y seguridad")
            GroupCard {
                MenuRow(
                    title = "Cuenta y sesión",
                    subtitle = if (hasSession) "Vinculada${linkedPhone?.let { " · +51 $it" }.orEmpty()}" else "Solo local · toca para vincular",
                    icon = Icons.Default.Link,
                    badge = if (hasSession) "Activa" else null,
                    onClick = { if (hasSession) showAccountDialog = true else showLinkDialog = true }
                )
                MenuRow(
                    title = "Verificación de DNI",
                    subtitle = kycSummary ?: "Pendiente · valida tu DNI",
                    icon = Icons.Default.Badge,
                    showDivider = true,
                    onClick = { showKycDialog = true }
                )
                MenuRow(
                    title = "Seguridad y cifrado",
                    subtitle = if (pinState.biometricEnabled) "PIN 6 dígitos + Huella activa" else "PIN 6 dígitos y AES-256",
                    icon = Icons.Default.Lock,
                    showDivider = true,
                    onClick = { showSecurityDialog = true }
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .kuotaTabSectionReveal(3),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SectionTitle("Pagos y avisos")
            GroupCard {
                MenuRow(
                    title = "Tarjetas y cuotas",
                    subtitle = "Preferencia: $preferredPaymentMethod",
                    icon = Icons.Default.CreditCard,
                    badge = "4",
                    onClick = onOpenCards
                )
                MenuRow(
                    title = "Notificaciones",
                    subtitle = "Alertas de cuotas y soporte",
                    icon = Icons.Default.NotificationsNone,
                    showDivider = true,
                    onClick = onOpenNotifications
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .kuotaTabSectionReveal(4),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SectionTitle("Apariencia")
            GroupCard {
                MenuRow(
                    title = "Automático",
                    subtitle = "Según el tema del teléfono",
                    icon = Icons.Default.BrightnessAuto,
                    trailing = { RadioButton(selected = themeMode == ThemeMode.SYSTEM, onClick = null) },
                    onClick = {
                        KuotaMotion.performKeyTapHaptic(view)
                        ThemeController.set(context, ThemeMode.SYSTEM)
                    }
                )
                MenuRow(
                    title = "Claro",
                    subtitle = "Fondo claro",
                    icon = Icons.Default.LightMode,
                    showDivider = true,
                    trailing = { RadioButton(selected = themeMode == ThemeMode.LIGHT, onClick = null) },
                    onClick = {
                        KuotaMotion.performKeyTapHaptic(view)
                        ThemeController.set(context, ThemeMode.LIGHT)
                    }
                )
                MenuRow(
                    title = "Oscuro",
                    subtitle = "Fondo oscuro",
                    icon = Icons.Default.DarkMode,
                    showDivider = true,
                    trailing = { RadioButton(selected = themeMode == ThemeMode.DARK, onClick = null) },
                    onClick = {
                        KuotaMotion.performKeyTapHaptic(view)
                        ThemeController.set(context, ThemeMode.DARK)
                    }
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .kuotaTabSectionReveal(5),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SectionTitle("Legal")
            GroupCard {
                MenuRow(
                    title = "Marco legal y privacidad",
                    subtitle = "Términos, Pagaré y Ley N° 29733",
                    icon = Icons.Default.Gavel,
                    badge = "Docs",
                    onClick = onOpenLegal
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .kuotaTabSectionReveal(6),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SectionTitle("Ayuda")
            GroupCard {
                MenuRow(
                    title = "Ayuda y reclamos",
                    subtitle = "Canales de atención y soporte",
                    icon = Icons.Default.Help,
                    onClick = { onOpenSupport(false) }
                )
                MenuRow(
                    title = "Libro de Reclamaciones",
                    subtitle = "Ley N° 29571 · INDECOPI",
                    icon = Icons.Default.ReceiptLong,
                    showDivider = true,
                    onClick = { onOpenSupport(true) }
                )
            }
        }

        Spacer(Modifier.height(4.dp))
        TextButton(
            onClick = { showDeleteConfirmation = true },
            enabled = !isSaving,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Eliminar perfil local y reiniciar la app", color = MaterialTheme.colorScheme.error)
        }
        Text(
            "Kuota Perú v${BuildConfig.VERSION_NAME} (build ${BuildConfig.VERSION_CODE}) · targetSdk 36",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 4.dp, bottom = 12.dp)
        )
    }

    if (showSecurityDialog) {
        val biometricCapability = remember(context) { BiometricAuthManager.checkCapability(context) }
        var biometricError by remember { mutableStateOf<String?>(null) }

        AlertDialog(
            onDismissRequest = { showSecurityDialog = false },
            title = { Text("Seguridad y datos del teléfono") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Text(" PIN de 6 dígitos y AES-256 activos", fontWeight = FontWeight.Bold)
                    }
                    Text(
                        "Tu PIN de 6 dígitos se protege con PBKDF2-HMAC-SHA256 + sal aleatoria y Android Keystore AES-GCM. " +
                            "Cada vez que KUOTA pasa a segundo plano se bloquea automáticamente.",
                        style = MaterialTheme.typography.bodySmall
                    )

                    HorizontalDivider()

                    if (biometricCapability is BiometricCapability.Available) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    Icons.Default.Fingerprint,
                                    contentDescription = null,
                                    tint = if (pinState.biometricEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Column {
                                    Text(
                                        "Desbloqueo biométrico",
                                        fontWeight = FontWeight.SemiBold,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Text(
                                        if (pinState.biometricEnabled) "Huella/Rostro activo" else "Inactivo · solo PIN de 6 dígitos",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Switch(
                                checked = pinState.biometricEnabled,
                                onCheckedChange = { enable ->
                                    if (enable) {
                                        val activity = context.findFragmentActivity()
                                        if (activity == null) {
                                            biometricError = "No se pudo acceder a la ventana de autenticación."
                                            return@Switch
                                        }
                                        BiometricAuthManager.authenticate(
                                            activity = activity,
                                            title = "Verifica tu huella",
                                            subtitle = "Confirma tu identidad para activar el desbloqueo biométrico",
                                            negativeButtonText = "Cancelar",
                                            onResult = { result ->
                                                when (result) {
                                                    is BiometricAuthResult.Success -> {
                                                        PinSecurityManager.setBiometricEnabled(context, true)
                                                        biometricError = null
                                                        Toast.makeText(context, "Desbloqueo biométrico activado.", Toast.LENGTH_SHORT).show()
                                                    }
                                                    is BiometricAuthResult.Failed -> {
                                                        biometricError = "Huella no reconocida."
                                                    }
                                                    is BiometricAuthResult.UserCanceled,
                                                    is BiometricAuthResult.NegativeButtonPin -> {
                                                        biometricError = null
                                                    }
                                                    is BiometricAuthResult.Lockout -> {
                                                        biometricError = "Sensor bloqueado temporalmente por intentos fallidos."
                                                    }
                                                    is BiometricAuthResult.Error -> {
                                                        biometricError = result.message
                                                    }
                                                }
                                            }
                                        )
                                    } else {
                                        PinSecurityManager.setBiometricEnabled(context, false)
                                        biometricError = null
                                        Toast.makeText(context, "Desbloqueo biométrico desactivado.", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            )
                        }
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Fingerprint, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
                            Column {
                                Text(
                                    "Biometría en este dispositivo",
                                    fontWeight = FontWeight.SemiBold,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    BiometricAuthManager.getCapabilityLabel(biometricCapability),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    biometricError?.let {
                        Text(
                            it,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    HorizontalDivider()

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                showSecurityDialog = false
                                showChangePinScreen = true
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Cambiar PIN", maxLines = 1)
                        }
                        OutlinedButton(
                            onClick = {
                                showSecurityDialog = false
                                PinSecurityManager.onAppBackgrounded(context)
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Bloquear ahora", maxLines = 1)
                        }
                    }
                    Spacer(Modifier.height(2.dp))
                    Button(
                        onClick = {
                            val summary = "MYPE Kuota Perú\n" +
                                "Nombre: $ownerName\n" +
                                "Negocio: $businessName\n" +
                                "Email: $email\n" +
                                "Celular: $phone\n" +
                                "RUC: $ruc\n" +
                                "Pago preferido: $preferredPaymentMethod\n" +
                                "Cuenta: ${if (hasSession) "vinculada" else "solo local"}\n" +
                                "Identidad: ${kycSummary ?: "sin verificar"}\n" +
                                "Biometría: ${if (pinState.biometricEnabled) "activa" else "desactivada"}"
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("Perfil MYPE Kuota", summary))
                            Toast.makeText(context, "Resumen copiado al portapapeles.", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
                        Text("Copiar resumen para soporte")
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showSecurityDialog = false }) { Text("Entendido") } }
        )
    }

    if (showAccountDialog) {
        AlertDialog(
            onDismissRequest = { if (!isUnlinking) showAccountDialog = false },
            title = { Text("Cuenta vinculada") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Celular: +51 ${linkedPhone ?: "—"}", fontWeight = FontWeight.SemiBold)
                    Text(
                        "Con la cuenta vinculada tus reclamos llegan al equipo de atención y recibes avisos del servidor. " +
                            "Los datos del negocio siguen guardados en este teléfono si cierras la sesión.",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = ::unlinkAccount, enabled = !isUnlinking) {
                    Text(if (isUnlinking) "Cerrando…" else "Cerrar sesión", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = { TextButton(onClick = { showAccountDialog = false }, enabled = !isUnlinking) { Text("Cancelar") } }
        )
    }

    if (showLinkDialog) {
        AccountLinkDialog(
            initialPhone = phone,
            onDismiss = { showLinkDialog = false },
            onLinked = {
                showLinkDialog = false
                hasSession = true
                scope.launch { withContext(Dispatchers.IO) { runCatching { KuotaSync.runOnce(context) } } }
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
                        Toast.makeText(context, "Identidad registrada: $summary", Toast.LENGTH_SHORT).show()
                    },
                    onDismiss = { showKycDialog = false }
                )
            }
        }
    }

    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text("¿Eliminar el perfil local?") },
            text = {
                Text(
                    "Se borrarán los datos del negocio, la sesión, la verificación de identidad, las notificaciones y el avance del registro. " +
                        "Las ventas manuales se conservarán. Esta acción no se puede deshacer."
                )
            },
            dismissButton = { TextButton(onClick = { showDeleteConfirmation = false }) { Text("Cancelar") } },
            confirmButton = {
                TextButton(onClick = ::deleteProfile, enabled = !isSaving) {
                    Text(if (isSaving) "Eliminando…" else "Eliminar", color = MaterialTheme.colorScheme.error)
                }
            }
        )
    }
}

@Composable
private fun AccountAvatar(name: String) {
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
            .size(52.dp)
            .background(MaterialTheme.colorScheme.surface, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        if (initials.isBlank()) {
            Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        } else {
            Text(
                initials,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}
