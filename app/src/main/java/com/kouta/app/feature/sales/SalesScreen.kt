package com.kouta.app.feature.sales

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Inventory2
import com.kouta.app.data.local.ProductCatalogStore
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.kouta.app.R
import com.kouta.app.core.analytics.KuotaAnalytics
import com.kouta.app.core.analytics.KuotaAnalyticsEvent
import com.kouta.app.core.designsystem.components.KuotaLoadingRow
import com.kouta.app.core.designsystem.components.KuotaSummaryRow
import com.kouta.app.core.designsystem.motion.KuotaMotion
import com.kouta.app.core.designsystem.motion.kuotaTabSectionReveal
import com.kouta.app.core.designsystem.motion.rememberSystemAnimatorScale
import com.kouta.app.core.designsystem.theme.kuotaColors
import com.kouta.app.core.push.KuotaEvents
import com.kouta.app.core.sync.KuotaSync
import com.kouta.app.data.firebase.KuotaFirestoreService
import com.kouta.app.data.local.AppSettings
import com.kouta.app.data.local.ManualSale
import com.kouta.app.data.local.SaleSource
import com.kouta.app.data.local.SaleSyncStatus
import com.kouta.app.data.local.SalesJournalStore
import com.kouta.app.data.remote.ApiOutcome
import com.kouta.app.data.remote.KuotaApiClient
import com.kouta.app.data.remote.PurchaseHistoryItemDto
import com.kouta.app.domain.model.MerchantProduct
import com.kouta.app.domain.model.PurchaseOffer
import com.kouta.app.feature.account.AccountLinkDialog
import com.kouta.app.feature.account.unreachableMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun SalesScreen(onOpenMyProducts: () -> Unit = {}) {
    val context = LocalContext.current
    val store = remember(context) { SalesJournalStore(context) }
    val catalogStore = remember(context) { ProductCatalogStore(context) }
    val settings = remember(context) { AppSettings(context) }
    val scope = rememberCoroutineScope()
    var sales by remember { mutableStateOf<List<ManualSale>>(emptyList()) }
    var catalogProducts by remember { mutableStateOf<List<MerchantProduct>>(emptyList()) }
    var selectedCatalogProduct by remember { mutableStateOf<MerchantProduct?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var isSaving by remember { mutableStateOf(false) }
    var storageError by rememberSaveable { mutableStateOf<String?>(null) }
    var showEntryDialog by rememberSaveable { mutableStateOf(false) }
    var amountText by rememberSaveable { mutableStateOf("") }
    var editingSale by remember { mutableStateOf<ManualSale?>(null) }
    var pendingDelete by remember { mutableStateOf<ManualSale?>(null) }
    var hasSession by remember { mutableStateOf(KuotaApiClient.hasSession(context)) }
    var autoSync by remember { mutableStateOf(settings.salesAutoSync) }
    var lastSyncAt by remember { mutableStateOf(settings.lastSalesSyncEpochMillis) }
    var initialPhone by remember { mutableStateOf("") }
    var showLinkDialog by remember { mutableStateOf(false) }
    var showSourceDialog by remember { mutableStateOf(false) }
    var isSyncing by remember { mutableStateOf(false) }
    var syncMessage by remember { mutableStateOf<String?>(null) }
    var syncMessageIsError by remember { mutableStateOf(false) }
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var activityFilter by rememberSaveable { mutableStateOf("todas") }
    var demoPurchases by remember { mutableStateOf<List<PurchaseHistoryItemDto>>(emptyList()) }
    var isLoadingPurchases by remember { mutableStateOf(false) }
    var purchaseHistoryError by remember { mutableStateOf<String?>(null) }
    var purchaseReloadNonce by remember { mutableIntStateOf(0) }
    var expandedPurchaseId by remember { mutableStateOf<String?>(null) }
    val eventsRevision by KuotaEvents.revision.collectAsState()
    val listFadeMs = KuotaMotion.resolveDurationMs(220, rememberSystemAnimatorScale())

    LaunchedEffect(store, eventsRevision) {
        try {
            sales = withContext(Dispatchers.IO) {
                val prods = catalogStore.getAll()
                catalogProducts = prods
                val catalogPrices = prods.map { it.priceCents }.toSet()
                store.recalculateVerificationWithCatalog(catalogPrices)
            }
            initialPhone = withContext(Dispatchers.IO) {
                store.readBusinessProfile()?.phone.orEmpty().filter(Char::isDigit).takeLast(9)
            }
            lastSyncAt = settings.lastSalesSyncEpochMillis
            hasSession = KuotaApiClient.hasSession(context)
            autoSync = settings.salesAutoSync
            storageError = null
        } catch (_: Exception) {
            storageError = "No se pudo abrir el registro cifrado. No se borraron tus datos."
        } finally {
            isLoading = false
        }
    }

    LaunchedEffect(hasSession, eventsRevision, purchaseReloadNonce) {
        if (!hasSession) {
            demoPurchases = emptyList()
            purchaseHistoryError = null
            isLoadingPurchases = false
            return@LaunchedEffect
        }
        isLoadingPurchases = true
        val outcome = withContext(Dispatchers.IO) { KuotaApiClient.call(context) { listPurchases() } }
        isLoadingPurchases = false
        when (outcome) {
            is ApiOutcome.Ok -> {
                demoPurchases = outcome.value
                purchaseHistoryError = null
            }
            is ApiOutcome.Rejected -> purchaseHistoryError = buildString {
                append(outcome.message.ifBlank { "No pudimos cargar las compras demo." })
                if (outcome.requestId.isNotBlank()) append("\nReferencia para soporte: ${outcome.requestId}")
            }
            is ApiOutcome.Unreachable -> purchaseHistoryError = unreachableMessage(outcome.serverUrl)
            ApiOutcome.SessionExpired -> {
                hasSession = false
                purchaseHistoryError = "La sesión venció. Vuelve a vincular tu cuenta para ver las compras demo."
            }
        }
    }

    fun syncNow() {
        if (isSyncing) return
        isSyncing = true
        syncMessage = null
        scope.launch {
            val report = withContext(Dispatchers.IO) { KuotaSync.runOnce(context, forceSalesBackup = true) }
            sales = withContext(Dispatchers.IO) { runCatching { store.readAll() }.getOrDefault(sales) }
            hasSession = KuotaApiClient.hasSession(context)
            autoSync = settings.salesAutoSync
            lastSyncAt = settings.lastSalesSyncEpochMillis
            isSyncing = false
            val failed = report.sales.needsSession || report.sales.unreachable ||
                report.sales.rejected > 0 || report.sales.message != null
            syncMessageIsError = failed
            KuotaAnalytics.track(
                KuotaAnalyticsEvent.SALES_SYNC_STATUS,
                mapOf(
                    "source" to "MANUAL",
                    "sync_outcome" to if (failed) "FAILED" else "OK"
                )
            )
            syncMessage = when {
                report.sales.needsSession -> "Tu sesión venció. Vuelve a vincular tu cuenta para respaldar."
                report.sales.unreachable -> "Sin conexión con el servidor. Tus ventas siguen cifradas en este teléfono."
                report.sales.rejected > 0 -> "${report.sales.rejected} venta(s) necesitan revisión: ${report.sales.message ?: "el servidor las rechazó"}."
                report.sales.message != null -> report.sales.message
                report.sales.sent == 0 -> "Todo estaba respaldado."
                else -> "${report.sales.sent} venta(s) respaldada(s) en tu cuenta."
            }
        }
    }

    val now = System.currentTimeMillis()
    val sevenDaysMillis = 7L * 24 * 60 * 60 * 1000
    val recentSales = sales.filter { it.recordedAtEpochMillis >= now - sevenDaysMillis }
    val recentTotal = recentSales.sumOf { it.amountCents }
    val recentVerifiedSales = recentSales.filter { it.isVerified }
    val recentVerifiedTotal = recentVerifiedSales.sumOf { it.amountCents }
    val recentVerifiedCount = recentVerifiedSales.size
    val verifiedPercent = if (recentSales.isNotEmpty()) {
        ((recentVerifiedCount.toDouble() / recentSales.size) * 100).toInt()
    } else 0
    val pendingBackup = sales.count { it.syncStatus != SaleSyncStatus.SYNCED }
    val filteredSales = sales.filter { sale ->
        val matchesQuery = searchQuery.isBlank() ||
            PurchaseOffer.formatPen(sale.amountCents).contains(searchQuery.trim(), ignoreCase = true) ||
            (sale.productName ?: "").contains(searchQuery.trim(), ignoreCase = true) ||
            formatSaleDate(sale.recordedAtEpochMillis).contains(searchQuery.trim(), ignoreCase = true)
        val matchesFilter = when (activityFilter) {
            "semana" -> sale.recordedAtEpochMillis >= now - sevenDaysMillis
            "comprobadas" -> sale.isVerified
            "no_comprobadas" -> !sale.isVerified
            "link_pago" -> sale.source == SaleSource.LINK_PAGO
            "locales" -> sale.syncStatus == SaleSyncStatus.LOCAL_ONLY
            else -> true
        }
        matchesQuery && matchesFilter
    }

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    "Finanzas",
                    modifier = Modifier.semantics { heading() },
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Registro de ventas y comprobación automática",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { onOpenMyProducts() },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Inventory2, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("Mis productos", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                            Text("Gestiona artículos para comprobar ventas", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it.take(40) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Buscar por monto o fecha") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = if (searchQuery.isNotBlank()) {
                    {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "Limpiar búsqueda")
                        }
                    }
                } else null,
                singleLine = true,
                shape = RoundedCornerShape(16.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    "todas" to "Todas",
                    "comprobadas" to "Comprobadas",
                    "no_comprobadas" to "No comprobadas",
                    "link_pago" to "Link de Pago",
                    "semana" to "Esta semana",
                    "locales" to "Solo local"
                ).forEach { (key, label) ->
                    FilterChip(
                        selected = activityFilter == key,
                        onClick = { activityFilter = key },
                        label = { Text(label) }
                    )
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        "ÚLTIMOS 7 DÍAS · CRUCE AUTOMÁTICO",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = .8f),
                        fontWeight = FontWeight.SemiBold
                    )
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Metric("Total ventas", PurchaseOffer.formatPen(recentTotal))
                        Metric("Comprobadas", "${PurchaseOffer.formatPen(recentVerifiedTotal)} ($verifiedPercent%)")
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Metric("Total registros", recentSales.size.toString())
                        Metric("Ventas cruzadas", "$recentVerifiedCount de ${recentSales.size}")
                    }
                }
            }

            Button(
                onClick = {
                    editingSale = null
                    amountText = ""
                    selectedCatalogProduct = null
                    showEntryDialog = true
                },
                modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
                shape = RoundedCornerShape(14.dp)
            ) { Text("Registrar venta", fontWeight = FontWeight.SemiBold) }

            BackupCard(
                hasSession = hasSession,
                autoSync = autoSync,
                lastSyncAt = lastSyncAt,
                pendingBackup = pendingBackup,
                isSyncing = isSyncing,
                message = syncMessage,
                messageIsError = syncMessageIsError,
                onLink = { showLinkDialog = true },
                onToggleAutoSync = { enabled ->
                    autoSync = enabled
                    settings.salesAutoSync = enabled
                    syncMessageIsError = false
                    KuotaAnalytics.track(
                        KuotaAnalyticsEvent.SALES_SOURCE_AUTHORIZED,
                        mapOf("source" to "MANUAL_BACKUP", "status" to if (enabled) "ENABLED" else "DISABLED")
                    )
                    if (enabled) {
                        KuotaSync.schedulePeriodic(context)
                        KuotaSync.enqueue(context)
                        syncMessage = "Respaldo automático activo: se ejecuta cada 15 minutos con conexión."
                    } else {
                        syncMessage = "Respaldo automático desactivado. Tus ventas siguen cifradas en este teléfono."
                    }
                },
                onSyncNow = { syncNow() }
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Ventas registradas", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(
                    if (sales.isEmpty()) "Sin registros" else "${filteredSales.size} de ${sales.size}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            // Transición discreta entre estados reales de la lista (carga, error, vacío, filtro sin resultados, contenido)
            // y al cambiar de filtro. La búsqueda por texto no dispara el fundido para no distraer al escribir.
            val listBucket = when {
                isLoading -> "loading"
                storageError != null -> "error"
                sales.isEmpty() -> "empty"
                filteredSales.isEmpty() -> "filtered_empty"
                else -> "content"
            }
            Crossfade(
                targetState = listBucket to activityFilter,
                animationSpec = tween(listFadeMs, easing = KuotaMotion.StandardEasing),
                label = "activityListState"
            ) { (bucket, _) ->
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            when (bucket) {
                "loading" -> KuotaLoadingRow(visible = true, label = "Cargando registros…")
                "error" -> Text(storageError.orEmpty(), color = MaterialTheme.colorScheme.error)
                "empty" -> EmptyActivityState()
                "filtered_empty" -> FilteredActivityState(
                    onClear = {
                        searchQuery = ""
                        activityFilter = "todas"
                    }
                )
                else -> filteredSales.forEachIndexed { index, sale ->
                    key(sale.id) {
                    val (syncLabel, syncColor) = saleSyncLabel(sale)
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .kuotaTabSectionReveal(sectionIndex = index.coerceAtMost(5)),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(PurchaseOffer.formatPen(sale.amountCents), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                                    if (sale.source == SaleSource.LINK_PAGO) {
                                        Text(
                                            "LINK DE PAGO",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.primary,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    if (sale.isVerified) {
                                        Text("COMPROBADA", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.kuotaColors.accentTeal, fontWeight = FontWeight.Bold)
                                    } else {
                                        Text("NO COMPROBADA", style = MaterialTheme.typography.labelSmall, color = Color(0xFFFFB300), fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                            if (!sale.productName.isNullOrBlank()) {
                                Text(
                                    "Artículo: ${sale.productName}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            Text(formatSaleDate(sale.recordedAtEpochMillis), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                syncLabel,
                                style = MaterialTheme.typography.labelSmall,
                                color = syncColor,
                                fontWeight = FontWeight.SemiBold
                            )
                            if (sale.syncStatus == SaleSyncStatus.REJECTED && sale.syncError != null) {
                                Text(
                                    sale.syncError,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                TextButton(onClick = {
                                    editingSale = sale
                                    amountText = formatAmountInput(sale.amountCents)
                                    showEntryDialog = true
                                }) { Text("Corregir") }
                                TextButton(onClick = { pendingDelete = sale }) { Text("Eliminar", color = MaterialTheme.colorScheme.error) }
                            }
                        }
                    }
                    }
                }
            }
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text("Origen de ventas", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Text("Registro manual cifrado (AES-256)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    TextButton(onClick = { showSourceDialog = true }) { Text("Opciones", fontWeight = FontWeight.Bold) }
                }
            }

            Text(
                "Ingresos declarados cifrados en este teléfono · No modifican tu score",
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(110.dp))
        }
    }

    if (showLinkDialog) {
        AccountLinkDialog(
            initialPhone = initialPhone,
            onDismiss = { showLinkDialog = false },
            onLinked = {
                showLinkDialog = false
                hasSession = true
                syncNow()
            }
        )
    }

    if (showSourceDialog) {
        AlertDialog(
            onDismissRequest = { showSourceDialog = false },
            title = { Text("Origen de ventas") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    SourceOption(
                        title = "Registro manual cifrado",
                        description = "Activo. Tú ingresas cada venta y se guarda cifrada en este teléfono.",
                        status = "ACTIVO",
                        active = true
                    )
                    SourceOption(
                        title = "POS o agregador",
                        description = "Niubiz, Izipay, Culqi u otro. Requiere contrato y credenciales del comercio.",
                        status = "NO DISPONIBLE",
                        active = false
                    )
                    SourceOption(
                        title = "Yape / Plin",
                        description = "Sin API pública abierta. Requiere convenio con el proveedor o un agregador autorizado.",
                        status = "NO DISPONIBLE",
                        active = false
                    )
                    SourceOption(
                        title = "Extractos bancarios",
                        description = "Requiere autorización del banco y validación legal del tratamiento de datos.",
                        status = "NO DISPONIBLE",
                        active = false
                    )
                    Text(
                        "No simulamos conexiones: cuando se defina el proveedor, la integración aparecerá aquí.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = { TextButton(onClick = { showSourceDialog = false }) { Text("Entendido") } }
        )
    }

    if (showEntryDialog) {
        val parsedAmount = parseAmountToCents(amountText)
        AlertDialog(
            onDismissRequest = { if (!isSaving) showEntryDialog = false },
            title = { Text(if (editingSale == null) "Registrar venta" else "Corregir venta") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        "Ingresa el total en soles o selecciona un producto de tu catálogo para comprobación inmediata.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    if (catalogProducts.isNotEmpty()) {
                        Text(
                            "Productos de tu catálogo:",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            catalogProducts.take(6).forEach { prod ->
                                val isSelected = selectedCatalogProduct?.id == prod.id || parsedAmount == prod.priceCents
                                FilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        selectedCatalogProduct = prod
                                        amountText = formatAmountInput(prod.priceCents)
                                    },
                                    label = { Text("${prod.name} (${PurchaseOffer.formatPen(prod.priceCents)})") }
                                )
                            }
                        }
                    }
                    OutlinedTextField(
                        value = amountText,
                        onValueChange = { value ->
                            if (value.length <= 14 && value.all { it.isDigit() || it == '.' || it == ',' }) {
                                amountText = value
                                selectedCatalogProduct = null
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Monto de venta") },
                        prefix = { Text("S/ ") },
                        placeholder = { Text("0.00") },
                        singleLine = true,
                        isError = amountText.isNotBlank() && parsedAmount == null,
                        supportingText = {
                            val isMatched = parsedAmount != null && (selectedCatalogProduct != null || catalogProducts.any { it.priceCents == parsedAmount })
                            if (isMatched) {
                                Text("Comprobada con Mis productos", color = MaterialTheme.kuotaColors.accentTeal)
                            } else {
                                Text("Registro manual · monto libre")
                            }
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                }
            },
            confirmButton = {
                Button(
                    enabled = parsedAmount != null && !isSaving,
                    onClick = {
                        val cents = parsedAmount ?: return@Button
                        val original = editingSale
                        scope.launch {
                            isSaving = true
                            try {
                                sales = withContext(Dispatchers.IO) {
                                    val matchedProduct = selectedCatalogProduct ?: catalogProducts.firstOrNull { it.priceCents == cents }
                                    val isVerified = matchedProduct != null
                                    val prodName = matchedProduct?.name
                                    val updated = store.saveSale(
                                        existing = original,
                                        amountCents = cents,
                                        isVerified = isVerified,
                                        productName = prodName
                                    )
                                    val savedSale = updated.firstOrNull { it.id == (original?.id ?: updated.firstOrNull()?.id) } ?: updated.firstOrNull()
                                    if (savedSale != null) {
                                        KuotaFirestoreService.saveSale(context, savedSale)
                                    }
                                    if (autoSync && hasSession) store.markLocalSalesPending() else updated
                                }
                                storageError = null
                                if (autoSync && hasSession) KuotaSync.enqueue(context)
                                showEntryDialog = false
                            } catch (_: Exception) {
                                storageError = "No se pudo guardar la venta cifrada. Intenta de nuevo."
                            } finally {
                                isSaving = false
                            }
                        }
                    }
                ) { Text(if (isSaving) "Guardando…" else "Guardar") }
            },
            dismissButton = { TextButton(enabled = !isSaving, onClick = { showEntryDialog = false }) { Text("Cancelar") } }
        )
    }

    pendingDelete?.let { sale ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text("Eliminar registro") },
            text = {
                Text(
                    if (sale.syncStatus == SaleSyncStatus.SYNCED) {
                        "Se quitará ${PurchaseOffer.formatPen(sale.amountCents)} del historial y, en el próximo respaldo, también del servidor. Esto no afecta ninguna compra ni evaluación."
                    } else {
                        "Se quitará ${PurchaseOffer.formatPen(sale.amountCents)} del historial manual. Esto no afecta ninguna compra ni evaluación."
                    }
                )
            },
            confirmButton = {
                Button(onClick = {
                    scope.launch {
                        try {
                            sales = withContext(Dispatchers.IO) {
                                KuotaFirestoreService.deleteSale(context, sale.id)
                                store.delete(sale.id)
                            }
                            storageError = null
                            if (autoSync && hasSession) KuotaSync.enqueue(context)
                        } catch (_: Exception) {
                            storageError = "No se pudo actualizar el registro cifrado."
                        } finally {
                            pendingDelete = null
                        }
                    }
                }) { Text("Eliminar") }
            },
            dismissButton = { TextButton(onClick = { pendingDelete = null }) { Text("Cancelar") } }
        )
    }
}

@Composable
private fun EmptyActivityState() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 22.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Image(
                painter = painterResource(R.drawable.kuota_onboarding_avatar),
                contentDescription = null,
                modifier = Modifier.size(132.dp),
                contentScale = ContentScale.Fit
            )
            Text(
                "Aún no hay actividad",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                "Usa «Registrar venta» para anotar tus ventas del día.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun FilteredActivityState(onClear: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                Icons.Default.Search,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp)
            )
            Text(
                "No encontramos actividad",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                "Prueba con otro monto, fecha o filtro.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            TextButton(onClick = onClear) { Text("Limpiar filtros") }
        }
    }
}

@Composable
private fun DemoPurchaseEmptyCard(
    title: String,
    message: String,
    actionLabel: String?,
    onAction: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f).padding(end = 8.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Text(message, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (actionLabel != null) {
                OutlinedButton(onClick = onAction, shape = RoundedCornerShape(12.dp)) {
                    Text(actionLabel, fontWeight = FontWeight.SemiBold, maxLines = 1)
                }
            }
        }
    }
}

@Composable
private fun DemoPurchaseCard(
    purchase: PurchaseHistoryItemDto,
    expanded: Boolean,
    onToggle: () -> Unit
) {
    val paymentsByInstallment = remember(purchase.payments) {
        purchase.payments.groupBy { it.installmentId }.mapValues { (_, list) -> list.last() }
    }
    val paidCount = purchase.installments.count { it.status in setOf("DEMO_PAID", "PAID") }
    val animatorScale = rememberSystemAnimatorScale()
    val expandMs = KuotaMotion.resolveDurationMs(240, animatorScale).coerceAtLeast(60)
    val chevronRotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        animationSpec = tween(KuotaMotion.resolveDurationMs(200, animatorScale)),
        label = "purchaseChevron"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    purchase.supplierName.ifBlank { "Proveedor demo" },
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text("DEMO", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            }
            Text(
                PurchaseOffer.formatPen(purchase.purchaseAmountCents),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                "Estado ${purchase.status} · inicial ${PurchaseOffer.formatPen(purchase.buyerDownPaymentCents)} · $paidCount/${purchase.installments.size} cuotas pagadas",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                "Operación ${purchase.purchaseId.takeLast(10)} · actualizado ${purchase.updatedAt}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            TextButton(onClick = onToggle) {
                Text(if (expanded) "Ocultar desglose y cuotas" else "Ver desglose completo y cuotas")
                Icon(
                    Icons.Default.ExpandMore,
                    contentDescription = null,
                    modifier = Modifier
                        .padding(start = 4.dp)
                        .rotate(chevronRotation)
                )
            }
            AnimatedVisibility(
                visible = expanded,
                enter = fadeIn(tween(expandMs)) + expandVertically(tween(expandMs, easing = KuotaMotion.StandardEasing)),
                exit = fadeOut(tween(expandMs - 40)) + shrinkVertically(tween(expandMs - 40, easing = KuotaMotion.StandardEasing))
            ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        "Desglose B2B de la operación (PRD §18.18 / §18.20)",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                    KuotaSummaryRow("Total compra inventario", PurchaseOffer.formatPen(purchase.purchaseAmountCents))
                    KuotaSummaryRow("Inicial comprador MYPE", PurchaseOffer.formatPen(purchase.buyerDownPaymentCents))
                    KuotaSummaryRow("Aporte inicial Kouta", PurchaseOffer.formatPen(purchase.koutaInitialContributionCents))
                    KuotaSummaryRow("Abono inmediato proveedor (60%)", PurchaseOffer.formatPen(purchase.supplierImmediatePayoutCents))
                    KuotaSummaryRow("Abono diferido proveedor (40%)", PurchaseOffer.formatPen(purchase.supplierDeferredPayoutCents))
                    KuotaSummaryRow("Comisión proveedor (4%)", PurchaseOffer.formatPen(purchase.supplierFeeCents))
                    HorizontalDivider()
                    KuotaSummaryRow("Total posterior en cuotas MYPE", PurchaseOffer.formatPen(purchase.buyerTotalAfterDownPaymentCents), emphasize = true)
                }

                purchase.installments.forEach { installment ->
                    val payment = paymentsByInstallment[installment.installmentId]
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                                Text(
                                    "Cuota ${installment.sequenceNumber} · ${installment.status}",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    "Vence ${formatPurchaseDueDate(installment.dueAt)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                PurchaseOffer.formatPen(installment.amountCents),
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        payment?.let { p ->
                            Text(
                                "Comprobante #${p.paymentId.takeLast(8)} · ${p.status} · Conciliación: ${p.reconciliationStatus}" +
                                    (p.providerReference?.let { " · Ref: $it" } ?: ""),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
            }
            Text(
                "Registro demo: no es una venta, una deuda, un desembolso ni un ingreso real. Puedes pagar o simular eventos de estas cuotas en Tarjetas.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun formatPurchaseDueDate(raw: String): String = runCatching {
    LocalDate.parse(raw).format(DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.forLanguageTag("es-PE")))
}.getOrDefault(raw)

@Composable
private fun BackupCard(
    hasSession: Boolean,
    autoSync: Boolean,
    lastSyncAt: Long,
    pendingBackup: Int,
    isSyncing: Boolean,
    message: String?,
    messageIsError: Boolean,
    onLink: () -> Unit,
    onToggleAutoSync: (Boolean) -> Unit,
    onSyncNow: () -> Unit
) {
    val colors = MaterialTheme.kuotaColors
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Respaldo de ventas", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Text(
                    if (hasSession) "VINCULADA" else "SOLO LOCAL",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (hasSession) colors.success else colors.neutral,
                    fontWeight = FontWeight.Bold
                )
            }
            if (!hasSession) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Copia tus ventas en la nube de Kuota.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f).padding(end = 8.dp)
                    )
                    Button(onClick = onLink, shape = RoundedCornerShape(12.dp)) {
                        Text("Vincular", maxLines = 1)
                    }
                }
            } else {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Respaldo automático", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                        Text(
                            if (lastSyncAt > 0) "Último: ${formatSaleDate(lastSyncAt)}" else "Cada 15 min con conexión",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(checked = autoSync, onCheckedChange = onToggleAutoSync)
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        if (pendingBackup > 0) "$pendingBackup pendiente(s)" else "Al día",
                        style = MaterialTheme.typography.labelMedium,
                        color = if (pendingBackup > 0) colors.warningAccent else colors.success,
                        fontWeight = FontWeight.SemiBold
                    )
                    OutlinedButton(
                        onClick = onSyncNow,
                        enabled = !isSyncing,
                        shape = RoundedCornerShape(12.dp)
                    ) { Text(if (isSyncing) "Sincronizando…" else "Sincronizar") }
                }
            }
            message?.let {
                Text(
                    it,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (messageIsError) MaterialTheme.colorScheme.error else colors.success
                )
            }
        }
    }
}

@Composable
private fun SourceOption(title: String, description: String, status: String, active: Boolean) {
    val colors = MaterialTheme.kuotaColors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                if (active) colors.infoContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .5f),
                RoundedCornerShape(12.dp)
            )
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            Text(
                status,
                style = MaterialTheme.typography.labelSmall,
                color = if (active) colors.info else colors.neutral,
                fontWeight = FontWeight.Bold
            )
        }
        Text(description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun saleSyncLabel(sale: ManualSale): Pair<String, Color> {
    val colors = MaterialTheme.kuotaColors
    return when (sale.syncStatus) {
        SaleSyncStatus.SYNCED -> "Respaldada · no verificada" to colors.success
        SaleSyncStatus.PENDING -> "Por respaldar" to colors.warningAccent
        SaleSyncStatus.REJECTED -> "Respaldo rechazado" to MaterialTheme.colorScheme.error
        else -> "Solo en este teléfono" to colors.neutral
    }
}

@Composable
private fun Metric(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onPrimary.copy(alpha = .82f))
        Text(value, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold)
    }
}

private fun parseAmountToCents(input: String): Long? {
    val normalized = input.trim().replace(',', '.')
    if (!normalized.matches(Regex("\\d+(?:\\.\\d{0,2})?"))) return null
    val parts = normalized.split('.', limit = 2)
    val soles = parts[0].toLongOrNull() ?: return null
    val cents = parts.getOrNull(1)?.padEnd(2, '0')?.toLongOrNull() ?: 0L
    if (soles == 0L && cents == 0L) return null
    return runCatching { Math.addExact(Math.multiplyExact(soles, 100L), cents) }.getOrNull()
}

private fun formatAmountInput(cents: Long): String = "${cents / 100}.${(cents % 100).toString().padStart(2, '0')}"

private fun formatSaleDate(epochMillis: Long): String = DateTimeFormatter
    .ofPattern("dd MMM yyyy · HH:mm", Locale.forLanguageTag("es-PE"))
    .withZone(ZoneId.systemDefault())
    .format(Instant.ofEpochMilli(epochMillis))
