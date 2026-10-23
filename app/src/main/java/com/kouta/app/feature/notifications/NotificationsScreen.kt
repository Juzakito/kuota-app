package com.kouta.app.feature.notifications

import android.Manifest
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.kouta.app.core.designsystem.components.GroupCard
import com.kouta.app.core.designsystem.components.IconBubble
import com.kouta.app.core.designsystem.components.MenuRow
import com.kouta.app.core.designsystem.components.ScreenHeader
import com.kouta.app.core.designsystem.components.SectionTitle
import com.kouta.app.core.designsystem.components.StatusPill
import com.kouta.app.core.designsystem.theme.kuotaColors
import com.kouta.app.core.push.IncomingNotifications
import com.kouta.app.core.push.KuotaEvents
import com.kouta.app.core.push.PushRegistrar
import com.kouta.app.core.push.PushStatus
import com.kouta.app.core.sync.KuotaSync
import com.kouta.app.core.util.NotificationHelper
import com.kouta.app.data.local.AppSettings
import com.kouta.app.data.local.LocalNotificationItem
import com.kouta.app.data.local.LocalNotificationSettings
import com.kouta.app.data.local.SalesJournalStore
import com.kouta.app.data.remote.ApiOutcome
import com.kouta.app.data.remote.KuotaApiClient
import com.kouta.app.domain.model.PurchaseOffer
import com.kouta.app.feature.account.AccountLinkDialog
import com.kouta.app.feature.account.unreachableMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private val LIMA = ZoneId.of("America/Lima")
private val ITEM_DATE = DateTimeFormatter.ofPattern("dd MMM · HH:mm", Locale("es", "PE"))

/** Bandeja interna + preferencias + diagnóstico del envío remoto (PRD §8.3 y §8.15). */
@Composable
fun NotificationsScreen(onBack: () -> Unit, onOpenDeepLink: (String) -> Unit) {
    val context = LocalContext.current
    val store = remember(context) { SalesJournalStore(context) }
    val settings = remember(context) { AppSettings(context) }
    val scope = rememberCoroutineScope()
    val revision by KuotaEvents.revision.collectAsState()
    val lifecycleOwner = LocalLifecycleOwner.current

    var items by remember { mutableStateOf<List<LocalNotificationItem>>(emptyList()) }
    var notificationSettings by remember { mutableStateOf(LocalNotificationSettings()) }
    var businessPhone by remember { mutableStateOf("") }
    var hasSession by remember { mutableStateOf(false) }
    var pushStatus by remember { mutableStateOf<PushStatus?>(null) }
    var statusLoading by remember { mutableStateOf(true) }
    var canPostNow by remember { mutableStateOf(NotificationHelper.canPost(context)) }
    var permissionAsked by remember { mutableStateOf(settings.notificationPermissionAsked) }
    var showLinkDialog by remember { mutableStateOf(false) }
    var testing by remember { mutableStateOf(false) }
    var testMessage by remember { mutableStateOf<String?>(null) }
    var showAdvanced by rememberSaveable { mutableStateOf(false) }
    var urlDraft by remember { mutableStateOf(settings.serverUrl) }
    var serverMessage by remember { mutableStateOf<String?>(null) }
    var connecting by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        canPostNow = NotificationHelper.canPost(context)
        if (granted) testMessage = "Notificaciones activadas en este teléfono."
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) canPostNow = NotificationHelper.canPost(context)
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    suspend fun reload() {
        val loaded = withContext(Dispatchers.IO) {
            val profile = runCatching { store.readBusinessProfile() }.getOrNull()
            val inbox = runCatching { store.readNotifications() }.getOrNull().orEmpty()
            profile to inbox
        }
        notificationSettings = loaded.first?.notificationSettings ?: LocalNotificationSettings()
        businessPhone = loaded.first?.phone.orEmpty()
        items = loaded.second
        hasSession = KuotaApiClient.hasSession(context)
    }

    fun refreshPushStatus(force: Boolean = false) {
        statusLoading = true
        scope.launch {
            val status = withContext(Dispatchers.IO) {
                if (!KuotaApiClient.hasSession(context)) {
                    PushStatus.NoSession
                } else {
                    runCatching { PushRegistrar.register(context, force) }.getOrElse {
                        PushStatus.Error(it.message ?: "No se pudo consultar el estado de los avisos.")
                    }
                }
            }
            pushStatus = status
            statusLoading = false
        }
    }

    fun saveSettings(updated: LocalNotificationSettings) {
        notificationSettings = updated
        scope.launch {
            withContext(Dispatchers.IO) { runCatching { store.updateNotificationSettings(updated) } }
            KuotaSync.enqueue(context)
        }
    }

    fun openSystemNotificationSettings() {
        val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
            .putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
        runCatching { context.startActivity(intent) }
    }

    fun sendTest() {
        if (testing) return
        testing = true
        testMessage = null
        scope.launch {
            val message = withContext(Dispatchers.IO) {
                if (KuotaApiClient.hasSession(context)) {
                    when (val outcome = KuotaApiClient.call(context) { testPush() }) {
                        is ApiOutcome.Ok -> {
                            val mode = outcome.value.pushMode
                            if (PushRegistrar.isPullToken(PushRegistrar.registeredToken(context))) {
                                runCatching { PushRegistrar.pollInbox(context) }
                                "El servidor encoló el aviso y ya lo descargamos. Revisa la bandeja y la barra de estado."
                            } else if (outcome.value.delivered > 0) {
                                "El servidor envió el aviso a ${outcome.value.delivered} dispositivo(s)."
                            } else {
                                "El servidor registró el aviso en modo $mode. No saldrá de este teléfono hasta que haya Firebase configurado."
                            }
                        }
                        is ApiOutcome.Rejected -> outcome.message
                        ApiOutcome.SessionExpired -> "La sesión venció. Vuelve a vincular tu cuenta."
                        is ApiOutcome.Unreachable -> unreachableMessage(outcome.serverUrl)
                    }
                } else {
                    val id = "test-" + System.currentTimeMillis()
                    IncomingNotifications.handle(
                        context,
                        id,
                        mapOf(
                            "notification_id" to id,
                            "type" to "TEST",
                            "title" to "Recordatorio de prueba",
                            "body" to "Así verás los avisos de cuotas y soporte de Kuota.",
                            "deep_link" to "kuota://notificaciones"
                        )
                    )
                    if (NotificationHelper.canPost(context)) {
                        "Aviso de prueba enviado a la barra de estado."
                    } else {
                        "Guardamos el aviso en la bandeja, pero Android no permite mostrarlo. Activa el permiso de notificaciones."
                    }
                }
            }
            testing = false
            testMessage = message
            reload()
        }
    }

    fun testConnection() {
        if (connecting) return
        connecting = true
        serverMessage = null
        scope.launch {
            val outcome = withContext(Dispatchers.IO) { KuotaApiClient.call(context) { health() } }
            connecting = false
            serverMessage = when (outcome) {
                is ApiOutcome.Ok -> "Conexión correcta con ${AppSettings(context).serverUrl}"
                is ApiOutcome.Unreachable -> unreachableMessage(outcome.serverUrl)
                is ApiOutcome.Rejected -> "El servidor respondió ${outcome.httpCode}: ${outcome.message}"
                ApiOutcome.SessionExpired -> "El servidor rechazó la sesión."
            }
        }
    }

    fun saveServerUrl() {
        if (!KuotaApiClient.isValidServerUrl(urlDraft)) {
            serverMessage = "Escribe una dirección http:// o https:// válida, por ejemplo http://192.168.1.10:8080/"
            return
        }
        settings.serverUrl = urlDraft
        settings.registeredFingerprint = null
        urlDraft = settings.serverUrl
        serverMessage = "Dirección guardada. El dispositivo se registrará de nuevo en la próxima sincronización."
        KuotaSync.enqueue(context)
        refreshPushStatus(force = true)
    }

    fun resetServerUrl() {
        settings.resetServerUrl()
        settings.registeredFingerprint = null
        urlDraft = settings.serverUrl
        serverMessage = "Usando la dirección de fábrica: ${settings.serverUrl}"
        KuotaSync.enqueue(context)
        refreshPushStatus(force = true)
    }

    LaunchedEffect(revision) { reload() }
    LaunchedEffect(Unit) { refreshPushStatus() }

    val unread = items.count { !it.read }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ScreenHeader("Notificaciones", "Cuotas, soporte y novedades de Kuota", onBack)

        if (!canPostNow) {
            PermissionCard(
                asked = permissionAsked,
                onRequest = {
                    permissionAsked = true
                    settings.notificationPermissionAsked = true
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && NotificationHelper.hasPermission(context)) {
                        openSystemNotificationSettings()
                    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    } else {
                        openSystemNotificationSettings()
                    }
                },
                onOpenSettings = ::openSystemNotificationSettings
            )
        }

        SectionTitle("Estado de los avisos")
        GroupCard {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                when {
                    statusLoading -> CircularProgressIndicator(modifier = Modifier.size(34.dp), strokeWidth = 3.dp)
                    pushStatus is PushStatus.Registered -> IconBubble(Icons.Default.NotificationsActive, container = MaterialTheme.colorScheme.primary, tint = MaterialTheme.colorScheme.onPrimary)
                    else -> IconBubble(Icons.Default.NotificationsOff, container = MaterialTheme.colorScheme.surfaceVariant, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(pushTitle(pushStatus), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    Text(pushDetail(pushStatus), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                (pushStatus as? PushStatus.Registered)?.let { StatusPill(it.mode) }
            }
            if (pushStatus is PushStatus.Error) {
                HorizontalDivider()
                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = { refreshPushStatus(force = true) }) { Text("Reintentar") }
                }
            }
            if (pushStatus == PushStatus.NoSession && !statusLoading) {
                HorizontalDivider()
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Sin cuenta vinculada los avisos quedan solo en este teléfono y tus reclamos no llegan al equipo de atención.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Button(onClick = { showLinkDialog = true }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                        Text("Vincular mi cuenta")
                    }
                }
            }
        }
        Text(
            "Horario de silencio: ${pushStatus.quietHoursOr(settings.quietHours)} (hora de Lima). " +
                "Las cuotas vencidas solo se recuerdan en días hábiles, según el Código de Protección al Consumidor (art. 62).",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 4.dp)
        )

        SectionTitle("Qué quieres recibir")
        GroupCard {
            ToggleRow(
                title = "Recordatorios de cuotas",
                subtitle = "Vencimientos y pagos pendientes",
                checked = notificationSettings.installmentRemindersEnabled,
                onChange = { saveSettings(notificationSettings.copy(installmentRemindersEnabled = it)) }
            )
            HorizontalDivider(modifier = Modifier.padding(start = 16.dp))
            ToggleRow(
                title = "Respuestas de soporte",
                subtitle = "Avances de tus reclamos y consultas",
                checked = notificationSettings.supportUpdatesEnabled,
                onChange = { saveSettings(notificationSettings.copy(supportUpdatesEnabled = it)) }
            )
            HorizontalDivider(modifier = Modifier.padding(start = 16.dp))
            ToggleRow(
                title = "Novedades y promociones",
                subtitle = "Nuevas funciones y beneficios (desactivado por defecto)",
                checked = notificationSettings.newsAndPromotionsEnabled,
                onChange = { saveSettings(notificationSettings.copy(newsAndPromotionsEnabled = it)) }
            )
        }

        SectionTitle("Privacidad en pantalla bloqueada")
        GroupCard {
            ToggleRow(
                title = "Mostrar detalle al bloquear",
                subtitle = if (notificationSettings.lockScreenDetail) {
                    "Se verá el monto y el detalle sin desbloquear el teléfono."
                } else {
                    "Recomendado: solo verás que tienes un aviso de Kuota."
                },
                checked = notificationSettings.lockScreenDetail,
                onChange = { saveSettings(notificationSettings.copy(lockScreenDetail = it)) }
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            SectionTitle("Bandeja ($unread sin leer)", modifier = Modifier.weight(1f))
            if (unread > 0) {
                TextButton(onClick = {
                    scope.launch {
                        withContext(Dispatchers.IO) { runCatching { store.markNotificationsRead() } }
                        reload()
                    }
                }) { Text("Marcar todo leído") }
            }
        }
        if (items.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Text(
                    "Aún no tienes avisos. Aquí verás los recordatorios de cuotas y las respuestas de soporte.",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            items.forEach { item ->
                NotificationCard(
                    item = item,
                    onOpen = {
                        scope.launch {
                            withContext(Dispatchers.IO) { runCatching { store.markNotificationsRead(setOf(item.id)) } }
                            reload()
                            item.deepLink?.let(onOpenDeepLink)
                        }
                    }
                )
            }
        }

        SectionTitle("Probar")
        GroupCard {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    if (hasSession) {
                        "El servidor envía un aviso de prueba a este teléfono con el mismo recorrido que un recordatorio real."
                    } else {
                        "Con la cuenta vinculada probamos el recorrido completo desde el servidor. Sin cuenta, mostramos un aviso local."
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Button(onClick = ::sendTest, enabled = !testing, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                    if (testing) CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                    else Text(if (hasSession) "Enviar aviso de prueba" else "Probar aviso local")
                }
                testMessage?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary) }
            }
        }

        SectionTitle("Avanzado")
        GroupCard {
            MenuRow(
                title = "Dirección del servidor",
                subtitle = settings.serverUrl,
                icon = Icons.Default.Sync,
                trailing = { Icon(if (showAdvanced) Icons.Default.ExpandLess else Icons.Default.ExpandMore, contentDescription = null) },
                onClick = { showAdvanced = !showAdvanced }
            )
            if (showAdvanced) {
                HorizontalDivider()
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = urlDraft,
                        onValueChange = { urlDraft = it.take(200); serverMessage = null },
                        label = { Text("URL del backend") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        "En el celular con cable USB puedes usar http://127.0.0.1:8080/ con \"adb reverse tcp:8080 tcp:8080\".",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = ::saveServerUrl, modifier = Modifier.weight(1f)) { Text("Guardar") }
                        OutlinedButton(onClick = ::testConnection, enabled = !connecting, modifier = Modifier.weight(1f)) {
                            Text(if (connecting) "Probando…" else "Probar conexión")
                        }
                    }
                    TextButton(onClick = ::resetServerUrl, enabled = settings.isServerUrlCustom) { Text("Restablecer dirección de fábrica") }
                    serverMessage?.let {
                        Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }

        Text(
            "Kuota Perú v${com.kouta.app.BuildConfig.VERSION_NAME} (build ${com.kouta.app.BuildConfig.VERSION_CODE})",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 4.dp, bottom = 12.dp)
        )
    }

    if (showLinkDialog) {
        AccountLinkDialog(
            initialPhone = businessPhone,
            onDismiss = { showLinkDialog = false },
            onLinked = {
                showLinkDialog = false
                scope.launch {
                    reload()
                    refreshPushStatus(force = true)
                    withContext(Dispatchers.IO) { runCatching { KuotaSync.runOnce(context) } }
                }
            }
        )
    }
}

private fun PushStatus?.quietHoursOr(fallback: String?): String =
    (this as? PushStatus.Registered)?.quietHours ?: fallback ?: "20:00–07:00"

private fun pushTitle(status: PushStatus?): String = when (status) {
    null -> "Consultando el estado…"
    PushStatus.NoSession -> "Sin cuenta vinculada"
    is PushStatus.Registered -> when (status.mode) {
        "FCM" -> "Avisos automáticos activos"
        "PULL" -> "Avisos por consulta periódica"
        "DRY_RUN" -> "Servidor en modo demostración"
        else -> "Avisos del servidor activos"
    }
    is PushStatus.Error -> "Sin conexión con el servidor"
}

private fun pushDetail(status: PushStatus?): String = when (status) {
    null -> "Revisando tu registro en el servidor."
    PushStatus.NoSession -> "Vincula tu cuenta para que el servidor pueda enviarte recordatorios."
    is PushStatus.Registered -> when (status.mode) {
        "FCM" -> "Recibirás recordatorios aunque la app esté cerrada."
        "PULL" -> "Sin Firebase en este teléfono, consultamos el servidor al abrir la app y cada 15 minutos."
        "DRY_RUN" -> "Tu dispositivo está registrado, pero faltan credenciales de Firebase: los avisos quedan en la bandeja."
        else -> "Tu dispositivo está registrado en el servidor."
    }
    is PushStatus.Error -> status.message
}

@Composable
private fun PermissionCard(asked: Boolean, onRequest: () -> Unit, onOpenSettings: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Activa las notificaciones", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            Text(
                "Android necesita tu permiso para mostrarte recordatorios de cuotas y respuestas de soporte. Puedes desactivarlo cuando quieras.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onRequest, modifier = Modifier.weight(1f)) { Text(if (asked) "Permitir de nuevo" else "Permitir notificaciones") }
                OutlinedButton(onClick = onOpenSettings) { Text("Ajustes") }
            }
        }
    }
}

@Composable
private fun ToggleRow(title: String, subtitle: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onChange(!checked) }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(checked = checked, onCheckedChange = onChange)
    }
}

@Composable
private fun NotificationCard(item: LocalNotificationItem, onOpen: () -> Unit) {
    val colors = MaterialTheme.kuotaColors
    val style = when {
        item.type.startsWith("INSTALLMENT") -> Triple(Icons.Default.AccountBalanceWallet, colors.success, colors.successContainer)
        item.type.startsWith("SUPPORT") -> Triple(Icons.Default.SupportAgent, colors.info, colors.infoContainer)
        else -> Triple(Icons.Default.Campaign, colors.warningAccent, colors.warningContainer)
    }
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onOpen),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            IconBubble(style.first, container = style.third, tint = style.second)
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        item.title,
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = if (item.read) FontWeight.Medium else FontWeight.Bold
                    )
                    if (!item.read) {
                        Box(modifier = Modifier.size(8.dp).background(MaterialTheme.colorScheme.primary, CircleShape))
                    }
                }
                if (item.body.isNotBlank()) {
                    Text(item.body, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                item.amountCents?.let {
                    Text(PurchaseOffer.formatPen(it), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold, color = style.second)
                }
                Text(
                    Instant.ofEpochMilli(item.receivedAtEpochMillis).atZone(LIMA).format(ITEM_DATE),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (item.deepLink != null) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(16.dp))
            }
        }
    }
}
