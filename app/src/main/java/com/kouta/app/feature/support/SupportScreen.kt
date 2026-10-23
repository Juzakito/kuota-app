package com.kouta.app.feature.support

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.ManageAccounts
import androidx.compose.material.icons.filled.QuestionAnswer
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.kouta.app.core.analytics.KuotaAnalytics
import com.kouta.app.core.analytics.KuotaAnalyticsEvent
import com.kouta.app.core.designsystem.components.GroupCard
import com.kouta.app.core.designsystem.components.IconBubble
import com.kouta.app.core.designsystem.components.MenuRow
import com.kouta.app.core.designsystem.components.ScreenHeader
import com.kouta.app.core.designsystem.components.SectionTitle
import com.kouta.app.core.designsystem.components.SecurityNotice
import com.kouta.app.core.designsystem.components.StatusPill
import com.kouta.app.core.designsystem.theme.kuotaColors
import com.kouta.app.core.push.KuotaEvents
import com.kouta.app.core.sync.KuotaSync
import com.kouta.app.data.firebase.KuotaFirestoreService
import com.kouta.app.core.util.PeruBusinessCalendar
import com.kouta.app.data.local.ClaimSyncStatus
import com.kouta.app.data.local.LocalBusinessProfile
import com.kouta.app.data.local.LocalClaimRecord
import com.kouta.app.data.local.SalesJournalStore
import com.kouta.app.data.remote.KuotaApiClient
import com.kouta.app.domain.model.PurchaseOffer
import com.kouta.app.feature.account.AccountLinkDialog
import com.kouta.app.feature.account.unreachableMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.UUID

private val LIMA = ZoneId.of("America/Lima")
private val DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy")
private const val RESPONSE_BUSINESS_DAYS = 15
private const val SUPPORT_WHATSAPP = "51999999999"
private const val SUPPORT_EMAIL = "soporte@kuota.pe"
private const val SUPPORT_PHONE = "+5117000000"

private data class HelpTopic(val key: String, val title: String, val guidance: String, val icon: ImageVector)

// Temas de ayuda priorizados en PRD v5.2 §8.16.
private val helpTopics = listOf(
    HelpTopic(
        "PAGO_NO_REFLEJADO", "Pagué y no se refleja",
        "Los pagos con Yape, Plin o transferencia pueden tardar hasta 24 horas hábiles en verse en tu plan. Ten a la mano el número de operación, la fecha y el monto. Si pasado ese plazo no aparece, escríbenos o registra un reclamo.",
        Icons.Default.HourglassTop
    ),
    HelpTopic(
        "COBRO_DUPLICADO", "Me cobraron dos veces",
        "Revisa en tu banco o billetera que ambos cargos figuren como completados (no \"en proceso\"). Envíanos los dos números de operación: si se confirma el doble cobro, te devolvemos el monto.",
        Icons.Default.ContentCopy
    ),
    HelpTopic(
        "MONTO_FECHA", "El monto o la fecha de mi cuota no coincide",
        "Compara con el cronograma que aceptaste al confirmar la compra. Si algo no coincide, registra un reclamo indicando la cuota, el monto o la fecha que esperabas.",
        Icons.Default.CalendarMonth
    ),
    HelpTopic(
        "DEVOLUCION", "Devolución o anulación de una compra",
        "La devolución de mercadería se coordina con el proveedor. Cuando el proveedor la confirma, Kuota ajusta o anula tu plan de cuotas. Si el proveedor no responde, registra un reclamo.",
        Icons.Default.Replay
    ),
    HelpTopic(
        "ACCESO_DATOS", "Acceso a mi cuenta y mis datos",
        "Si no puedes ingresar, o quieres acceder, corregir o eliminar tus datos personales (Ley N° 29733), escríbenos desde el celular registrado. Nunca te pediremos tu código OTP.",
        Icons.Default.ManageAccounts
    )
)

@Composable
fun SupportScreen(openClaimForm: Boolean = false, onBack: () -> Unit) {
    val context = LocalContext.current
    val store = remember(context) { SalesJournalStore(context) }
    val scope = rememberCoroutineScope()
    val revision by KuotaEvents.revision.collectAsState()

    var profile by remember { mutableStateOf<LocalBusinessProfile?>(null) }
    var hasSession by remember { mutableStateOf(false) }
    var expandedTopic by rememberSaveable { mutableStateOf<String?>(null) }
    var formSeed by remember { mutableStateOf<ClaimFormSeed?>(if (openClaimForm) ClaimFormSeed() else null) }
    var receiptClaimId by remember { mutableStateOf<String?>(null) }
    var showLinkDialog by remember { mutableStateOf(false) }
    var syncing by remember { mutableStateOf(false) }
    var syncMessage by remember { mutableStateOf<String?>(null) }

    suspend fun reload() {
        val loaded = withContext(Dispatchers.IO) { runCatching { store.readBusinessProfile() }.getOrNull() }
        profile = loaded
        hasSession = KuotaApiClient.hasSession(context)
    }

    fun syncNow(showResult: Boolean) {
        if (syncing) return
        syncing = true
        scope.launch {
            val report = withContext(Dispatchers.IO) { runCatching { KuotaSync.runOnce(context) }.getOrNull() }
            reload()
            syncing = false
            if (showResult) {
                syncMessage = when {
                    report == null -> "No se pudo actualizar. Intenta nuevamente."
                    report.needsSession -> "Vincula tu cuenta para enviar tus reclamos y ver su estado."
                    report.unreachable -> unreachableMessage(com.kouta.app.data.local.AppSettings(context).serverUrl)
                    report.sentClaims > 0 -> "Enviamos ${report.sentClaims} hoja(s) al equipo de atención."
                    else -> "Estado actualizado."
                }
            }
        }
    }

    LaunchedEffect(revision) { reload() }

    val claims = profile?.claims.orEmpty()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ScreenHeader("Ayuda y reclamos", "Resuelve dudas, contáctanos o registra un reclamo", onBack)
        SecurityNotice()

        SectionTitle("¿En qué te ayudamos?")
        GroupCard {
            helpTopics.forEachIndexed { index, topic ->
                val expanded = expandedTopic == topic.key
                MenuRow(
                    title = topic.title,
                    icon = topic.icon,
                    showDivider = index > 0,
                    trailing = { Icon(if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore, contentDescription = null) },
                    onClick = { expandedTopic = if (expanded) null else topic.key }
                )
                if (expanded) {
                    Column(
                        modifier = Modifier.padding(start = 64.dp, end = 16.dp, bottom = 14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(topic.guidance, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(onClick = { openWhatsApp(context, "Hola Kuota, necesito ayuda: ${topic.title}") }) { Text("Escríbenos") }
                            Button(onClick = { formSeed = ClaimFormSeed(helpTopic = topic.key, goodDescription = topic.title) }) { Text("Registrar reclamo") }
                        }
                    }
                }
            }
        }

        SectionTitle("Canales de atención")
        GroupCard {
            MenuRow("WhatsApp", "Respuesta en horario de atención", Icons.Default.QuestionAnswer) {
                openWhatsApp(context, "Hola Kuota, necesito ayuda con mi cuenta")
            }
            MenuRow("Correo", SUPPORT_EMAIL, Icons.Default.Email, showDivider = true) {
                launch(context, Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:$SUPPORT_EMAIL?subject=" + Uri.encode("Consulta Kuota"))), "No se encontró una app de correo.")
            }
            MenuRow("Central telefónica", "Lun. a vie. de 9:00 a 18:00", Icons.Default.Call, showDivider = true) {
                launch(context, Intent(Intent.ACTION_DIAL, Uri.parse("tel:$SUPPORT_PHONE")), "No se pudo abrir el marcador.")
            }
        }
        Text(
            "[DEMO] Canales de prueba. Antes de publicar deben estar atendidos por personas con escalamiento definido.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 4.dp)
        )

        SectionTitle("Libro de Reclamaciones")
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    IconBubble(Icons.Default.ReceiptLong, container = MaterialTheme.colorScheme.primary, tint = MaterialTheme.colorScheme.onPrimary)
                    Text("Hoja de reclamación virtual", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                }
                Text(
                    "Reclamo: disconformidad con el producto o servicio. Queja: malestar con la atención recibida. " +
                        "Respondemos en un plazo máximo de $RESPONSE_BUSINESS_DAYS días hábiles y recibirás una copia en tu correo (Ley N° 29571).",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Button(onClick = { formSeed = ClaimFormSeed() }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                    Text("Registrar reclamo o queja")
                }
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            SectionTitle("Mis reclamos (${claims.size})", modifier = Modifier.weight(1f))
            if (claims.isNotEmpty()) {
                IconButton(onClick = { syncNow(showResult = true) }, enabled = !syncing) {
                    if (syncing) CircularProgressIndicator(modifier = Modifier.height(18.dp), strokeWidth = 2.dp)
                    else Icon(Icons.Default.Sync, contentDescription = "Actualizar estado")
                }
            }
        }
        syncMessage?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(horizontal = 4.dp)) }
        if (!hasSession && claims.any { it.syncStatus == ClaimSyncStatus.PENDING }) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
            ) {
                Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        "Tienes hojas por enviar. Vincula tu cuenta para que lleguen al equipo de atención.",
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodySmall
                    )
                    Button(onClick = { showLinkDialog = true }) { Text("Vincular") }
                }
            }
        }
        if (claims.isEmpty()) {
            Text("Aún no registras reclamos.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(horizontal = 4.dp))
        } else {
            claims.forEach { claim ->
                ClaimCard(
                    claim = claim,
                    onOpen = { receiptClaimId = claim.claimId },
                    onFix = { formSeed = ClaimFormSeed.from(claim) }
                )
            }
        }
        Spacer(Modifier.height(8.dp))
    }

    formSeed?.let { seed ->
        ClaimFormDialog(
            seed = seed,
            profile = profile,
            onDismiss = { formSeed = null },
            onSubmit = { record ->
                scope.launch {
                    val saved = withContext(Dispatchers.IO) {
                        runCatching {
                            val exists = store.readBusinessProfile()?.claims?.any { it.claimId == record.claimId } == true
                            val profile = if (exists) store.updateClaim(record.claimId) { record } else store.addClaim(record)
                            runCatching {
                                KuotaFirestoreService.submitClaim(
                                    context = context,
                                    fullName = record.consumerName,
                                    documentType = record.documentType,
                                    documentNumber = record.documentNumber,
                                    email = record.email,
                                    phone = record.phone,
                                    detail = record.detail,
                                    consumerRequest = record.consumerRequest,
                                    amountClaimedCents = record.amountClaimedCents
                                )
                            }
                            profile
                        }.getOrNull()
                    }
                    if (saved == null) {
                        Toast.makeText(context, "Primero completa tu perfil del negocio.", Toast.LENGTH_LONG).show()
                        return@launch
                    }
                    formSeed = null
                    receiptClaimId = record.claimId
                    KuotaAnalytics.track(
                        KuotaAnalyticsEvent.SUPPORT_CASE_CREATED,
                        mapOf(
                            "claim_type" to record.type,
                            "help_topic" to (record.helpTopic ?: "GENERAL")
                        )
                    )
                    reload()
                    KuotaSync.enqueue(context)
                    syncNow(showResult = false)
                }
            }
        )
    }

    receiptClaimId?.let { id ->
        claims.firstOrNull { it.claimId == id }?.let { claim ->
            ClaimReceiptDialog(
                claim = claim,
                hasSession = hasSession,
                syncing = syncing,
                onLink = { showLinkDialog = true },
                onDismiss = { receiptClaimId = null }
            )
        }
    }

    if (showLinkDialog) {
        AccountLinkDialog(
            initialPhone = profile?.phone.orEmpty(),
            onDismiss = { showLinkDialog = false },
            onLinked = {
                showLinkDialog = false
                syncNow(showResult = true)
            }
        )
    }
}

private fun dueDateOf(claim: LocalClaimRecord): LocalDate? =
    (claim.responseDueDate ?: claim.estimatedDueDate)?.let { runCatching { LocalDate.parse(it) }.getOrNull() }

@Composable
private fun statusLabel(claim: LocalClaimRecord): Pair<String, Color> {
    val kuotaColors = MaterialTheme.kuotaColors
    return when (claim.syncStatus) {
        ClaimSyncStatus.LOCAL_ONLY -> "Solo en este teléfono" to kuotaColors.neutral
        ClaimSyncStatus.PENDING -> "Por enviar" to kuotaColors.warningAccent
        ClaimSyncStatus.REJECTED -> "Requiere corrección" to MaterialTheme.colorScheme.error
        else -> when (claim.status) {
            "RESPONDIDO" -> "Respondido" to kuotaColors.success
            "EN_ATENCION" -> "En atención" to kuotaColors.info
            else -> "Recibido" to MaterialTheme.colorScheme.secondary
        }
    }
}

private fun channelLabel(channel: String): String = when (channel) {
    "CORREO_SOPORTE" -> "correo de atención"
    "COPIA_CONSUMIDOR" -> "copia a tu correo"
    "ZENDESK" -> "Zendesk"
    "FRESHDESK" -> "Freshdesk"
    "ARCHIVO_LOCAL" -> "archivo del servidor (demo)"
    else -> channel.lowercase()
}

@Composable
private fun ClaimCard(claim: LocalClaimRecord, onOpen: () -> Unit, onFix: () -> Unit) {
    val (label, color) = statusLabel(claim)
    val due = dueDateOf(claim)
    val today = LocalDate.now(LIMA)
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onOpen),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "${claim.type} · ${claim.officialNumber ?: claim.claimId.take(8).uppercase()}",
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                StatusPill(label, container = color.copy(alpha = .12f), content = color)
            }
            Text(claim.detail, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2)
            if (due != null && claim.status != "RESPONDIDO" && claim.syncStatus != ClaimSyncStatus.LOCAL_ONLY) {
                val remaining = PeruBusinessCalendar.businessDaysUntil(today, due)
                val estimated = if (claim.responseDueDate == null) " (estimado)" else ""
                Text(
                    "Respuesta hasta el ${due.format(DATE_FORMAT)}$estimated · " + if (remaining > 0) "faltan $remaining días hábiles" else "plazo cumplido",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium
                )
            }
            claim.responseText?.let {
                Text("Respuesta de Kuota: $it", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.kuotaColors.success)
            }
            when (claim.syncStatus) {
                ClaimSyncStatus.REJECTED -> {
                    Text(claim.syncError ?: "El servidor no aceptó la hoja.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                    TextButton(onClick = onFix) { Text("Corregir y enviar") }
                }
                ClaimSyncStatus.LOCAL_ONLY -> {
                    Text(
                        "Registrada en una versión anterior y guardada solo aquí. Complétala para que el equipo la atienda.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    TextButton(onClick = onFix) { Text("Completar y enviar") }
                }
            }
        }
    }
}

@Composable
private fun ClaimReceiptDialog(claim: LocalClaimRecord, hasSession: Boolean, syncing: Boolean, onLink: () -> Unit, onDismiss: () -> Unit) {
    val due = dueDateOf(claim)
    val delivered = claim.channels.mapNotNull { entry ->
        val (channel, status) = entry.split(':', limit = 2).let { it[0] to it.getOrElse(1) { "" } }
        if (status == "DESACTIVADO") null else channelLabel(channel) + if (status == "ENVIADO") " ✓" else " (en cola)"
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (claim.officialNumber != null) "Constancia de registro" else "Hoja guardada") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ReceiptLine("Tipo", claim.type)
                ReceiptLine("N.° de hoja", claim.officialNumber ?: "Se asigna al enviarla")
                ReceiptLine("Estado", statusLabel(claim).first)
                due?.let { ReceiptLine("Respuesta hasta", it.format(DATE_FORMAT) + if (claim.responseDueDate == null) " (estimado)" else "") }
                if (delivered.isNotEmpty()) ReceiptLine("Copias", delivered.joinToString(", "))
                HorizontalDivider()
                when {
                    claim.syncStatus == ClaimSyncStatus.SENT -> Text(
                        "El equipo de atención ya tiene tu hoja. Te avisaremos cuando haya una respuesta y también la recibirás en ${claim.email.ifBlank { "tu correo" }}.",
                        style = MaterialTheme.typography.bodySmall
                    )
                    syncing -> Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        CircularProgressIndicator(modifier = Modifier.height(16.dp), strokeWidth = 2.dp)
                        Text("Enviando al equipo de atención…", style = MaterialTheme.typography.bodySmall)
                    }
                    !hasSession -> Text(
                        "Está guardada cifrada en este teléfono. Vincula tu cuenta para enviarla; el plazo de respuesta corre desde que la recibimos.",
                        style = MaterialTheme.typography.bodySmall
                    )
                    else -> Text(
                        "Está guardada en este teléfono y se enviará automáticamente apenas haya conexión con Kuota.",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            if (!hasSession && claim.syncStatus == ClaimSyncStatus.PENDING) {
                Button(onClick = onLink) { Text("Vincular cuenta") }
            } else {
                Button(onClick = onDismiss) { Text("Listo") }
            }
        },
        dismissButton = if (!hasSession && claim.syncStatus == ClaimSyncStatus.PENDING) {
            { TextButton(onClick = onDismiss) { Text("Después") } }
        } else null
    )
}

@Composable
private fun ReceiptLine(label: String, value: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(label, modifier = Modifier.weight(.42f), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, modifier = Modifier.weight(.58f), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
    }
}

private data class ClaimFormSeed(
    val existingId: String? = null,
    val createdAt: Long? = null,
    val type: String = "Reclamo",
    val helpTopic: String? = null,
    val consumerName: String = "",
    val documentType: String = "DNI",
    val documentNumber: String = "",
    val address: String = "",
    val phone: String? = null,
    val email: String? = null,
    val goodType: String = "SERVICIO",
    val goodDescription: String = "",
    val amount: String = "",
    val operationReference: String = "",
    val detail: String = "",
    val consumerRequest: String = ""
) {
    companion object {
        fun from(claim: LocalClaimRecord) = ClaimFormSeed(
            existingId = claim.claimId,
            createdAt = claim.createdAtEpochMillis,
            type = claim.type,
            helpTopic = claim.helpTopic,
            consumerName = claim.consumerName,
            documentType = claim.documentType,
            documentNumber = claim.documentNumber,
            address = claim.address,
            phone = claim.phone.ifBlank { null },
            email = claim.email.ifBlank { null },
            goodType = claim.goodType,
            goodDescription = claim.goodDescription,
            amount = claim.amountClaimedCents?.let { BigDecimal(it).movePointLeft(2).toPlainString() }.orEmpty(),
            operationReference = claim.operationReference.orEmpty(),
            detail = claim.detail,
            consumerRequest = claim.consumerRequest
        )
    }
}

private val EMAIL_REGEX = Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ClaimFormDialog(
    seed: ClaimFormSeed,
    profile: LocalBusinessProfile?,
    onDismiss: () -> Unit,
    onSubmit: (LocalClaimRecord) -> Unit
) {
    var type by remember { mutableStateOf(seed.type) }
    var consumerName by remember { mutableStateOf(seed.consumerName) }
    var documentType by remember { mutableStateOf(seed.documentType) }
    var documentNumber by remember { mutableStateOf(seed.documentNumber) }
    var address by remember { mutableStateOf(seed.address) }
    var phone by remember { mutableStateOf(seed.phone ?: profile?.phone.orEmpty()) }
    var email by remember { mutableStateOf(seed.email ?: profile?.email.orEmpty()) }
    var goodType by remember { mutableStateOf(seed.goodType) }
    var goodDescription by remember { mutableStateOf(seed.goodDescription.ifBlank { "Financiamiento de compras de inventario con Kuota" }) }
    var amount by remember { mutableStateOf(seed.amount) }
    var operationReference by remember { mutableStateOf(seed.operationReference) }
    var detail by remember { mutableStateOf(seed.detail) }
    var consumerRequest by remember { mutableStateOf(seed.consumerRequest) }
    var error by remember { mutableStateOf<String?>(null) }

    val amountCents = amount.replace(',', '.').trim().takeIf { it.isNotEmpty() }?.let {
        runCatching { BigDecimal(it).movePointRight(2).setScale(0, RoundingMode.HALF_UP).longValueExact() }.getOrNull()
    }
    val docLength = when (documentType) { "DNI" -> 8..8; "RUC" -> 11..11; "CE" -> 8..12; else -> 6..12 }

    fun validate(): String? = when {
        consumerName.trim().length < 3 -> "Ingresa tu nombre completo."
        documentNumber.length !in docLength -> "Revisa el número de documento."
        address.trim().length < 5 -> "Ingresa tu domicilio."
        phone.filter(Char::isDigit).length !in 9..15 -> "Ingresa un teléfono válido."
        !email.trim().matches(EMAIL_REGEX) -> "Ingresa un correo válido: ahí recibirás la copia y la respuesta."
        goodDescription.trim().length < 3 -> "Describe el producto o servicio."
        amount.isNotBlank() && (amountCents == null || amountCents < 0) -> "Revisa el monto reclamado."
        detail.trim().length < 10 -> "Describe lo ocurrido (mínimo 10 caracteres)."
        consumerRequest.trim().length < 5 -> "Indica qué solicitas a Kuota."
        else -> null
    }

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Column(modifier = Modifier.fillMaxSize().imePadding()) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 18.dp, vertical = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ScreenHeader("Hoja de reclamación", "Libro de Reclamaciones virtual · Ley N° 29571", onBack = onDismiss)

                    FormSection("1. Tipo")
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(selected = type == "Reclamo", onClick = { type = "Reclamo" }, label = { Text("Reclamo") })
                        FilterChip(selected = type == "Queja", onClick = { type = "Queja" }, label = { Text("Queja") })
                    }
                    Text(
                        if (type == "Reclamo") "Disconformidad con el producto o servicio (cobros, montos, cuotas, devoluciones)."
                        else "Malestar con la atención recibida, sin relación directa con el producto o servicio.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    FormSection("2. Tus datos")
                    FormField(consumerName, { consumerName = it.take(120) }, "Nombre completo")
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("DNI", "RUC", "CE", "PASAPORTE").forEach { option ->
                            FilterChip(
                                selected = documentType == option,
                                onClick = { documentType = option; documentNumber = "" },
                                label = { Text(if (option == "PASAPORTE") "Pasaporte" else option) }
                            )
                        }
                    }
                    FormField(
                        documentNumber,
                        { value ->
                            val allowed = if (documentType == "DNI" || documentType == "RUC") value.all(Char::isDigit) else value.all(Char::isLetterOrDigit)
                            if (allowed && value.length <= docLength.last) documentNumber = value.uppercase()
                        },
                        "Número de documento",
                        keyboard = if (documentType == "DNI" || documentType == "RUC") KeyboardType.Number else KeyboardType.Text
                    )
                    FormField(address, { address = it.take(200) }, "Domicilio")
                    FormField(phone, { value -> if (value.all(Char::isDigit) && value.length <= 15) phone = value }, "Teléfono", keyboard = KeyboardType.Phone)
                    FormField(email, { email = it.take(120) }, "Correo (recibirás la copia aquí)", keyboard = KeyboardType.Email)

                    FormSection("3. Producto o servicio")
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(selected = goodType == "SERVICIO", onClick = { goodType = "SERVICIO" }, label = { Text("Servicio") })
                        FilterChip(selected = goodType == "PRODUCTO", onClick = { goodType = "PRODUCTO" }, label = { Text("Producto") })
                    }
                    FormField(goodDescription, { goodDescription = it.take(200) }, "Descripción")
                    FormField(amount, { value -> if (value.all { it.isDigit() || it == '.' || it == ',' } && value.length <= 12) amount = value }, "Monto reclamado en S/ (opcional)", keyboard = KeyboardType.Decimal)
                    FormField(operationReference, { operationReference = it.take(60) }, "N.° de operación o compra (opcional)")

                    FormSection("4. Detalle")
                    FormField(detail, { detail = it.take(2000) }, "¿Qué ocurrió?", singleLine = false)
                    FormField(consumerRequest, { consumerRequest = it.take(1000) }, "¿Qué solicitas?", singleLine = false)

                    error?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }
                    Text(
                        "La formulación del reclamo no impide acudir a otras vías de solución de controversias ni es requisito previo para interponer una denuncia ante el INDECOPI.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                HorizontalDivider()
                Row(modifier = Modifier.fillMaxWidth().padding(14.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) { Text("Cancelar") }
                    Button(
                        onClick = {
                            val problem = validate()
                            if (problem != null) {
                                error = problem
                                return@Button
                            }
                            val now = System.currentTimeMillis()
                            onSubmit(
                                LocalClaimRecord(
                                    claimId = seed.existingId ?: UUID.randomUUID().toString(),
                                    type = type,
                                    detail = detail.trim(),
                                    createdAtEpochMillis = seed.createdAt ?: now,
                                    status = "REGISTRADO_LOCAL",
                                    helpTopic = seed.helpTopic,
                                    consumerName = consumerName.trim(),
                                    documentType = documentType,
                                    documentNumber = documentNumber,
                                    address = address.trim(),
                                    phone = phone,
                                    email = email.trim(),
                                    goodType = goodType,
                                    goodDescription = goodDescription.trim(),
                                    amountClaimedCents = amountCents,
                                    operationReference = operationReference.trim().ifBlank { null },
                                    consumerRequest = consumerRequest.trim(),
                                    syncStatus = ClaimSyncStatus.PENDING,
                                    estimatedDueDate = PeruBusinessCalendar.addBusinessDays(LocalDate.now(LIMA), RESPONSE_BUSINESS_DAYS).toString()
                                )
                            )
                        },
                        modifier = Modifier.weight(1f)
                    ) { Text("Enviar hoja") }
                }
            }
        }
    }
}

@Composable
private fun FormSection(title: String) {
    Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 6.dp))
}

@Composable
private fun FormField(
    value: String,
    onChange: (String) -> Unit,
    label: String,
    keyboard: KeyboardType = KeyboardType.Text,
    singleLine: Boolean = true
) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label) },
        singleLine = singleLine,
        minLines = if (singleLine) 1 else 3,
        keyboardOptions = KeyboardOptions(keyboardType = keyboard),
        modifier = Modifier.fillMaxWidth()
    )
}

private fun openWhatsApp(context: Context, message: String) {
    launch(context, Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/$SUPPORT_WHATSAPP?text=" + Uri.encode(message))), "No se encontró WhatsApp ni un navegador.")
}

private fun launch(context: Context, intent: Intent, failure: String) {
    runCatching { context.startActivity(intent) }.onFailure { Toast.makeText(context, failure, Toast.LENGTH_SHORT).show() }
}

@Suppress("unused")
private fun formatAmount(cents: Long?) = cents?.let(PurchaseOffer::formatPen)
