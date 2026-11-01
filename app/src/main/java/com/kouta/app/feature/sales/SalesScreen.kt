package com.kouta.app.feature.sales

import android.content.Intent
import android.graphics.BitmapFactory
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationManagerCompat
import com.kouta.app.R
import com.kouta.app.di.AppEntryPoint
import com.kuota.core.model.SaleRecord
import com.kuota.core.model.SaleSource
import com.kuota.core.model.SalesMonthSummary
import com.kuota.core.ui.components.AmountInput
import com.kuota.core.ui.components.ChipPill
import com.kuota.core.ui.components.EmptyState
import com.kuota.core.ui.components.KuotaCard
import com.kuota.core.ui.components.KuotaCardVariant
import com.kuota.core.ui.components.PillButton
import com.kuota.core.ui.components.PillButtonVariant
import com.kuota.core.ui.theme.KuotaColors
import com.kuota.core.ui.theme.KuotaRadii
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SalesScreen(
    onOpenMyProducts: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val entryPoint = remember(context) {
        runCatching {
            EntryPointAccessors.fromApplication(
                context.applicationContext,
                AppEntryPoint::class.java
            )
        }.getOrNull()
    }

    val authRepo = entryPoint?.authRepository()
    val salesRepo = entryPoint?.salesRepository()
    val userRepo = entryPoint?.userRepository()
    val uid = userRepo?.getCurrentUid().orEmpty()

    val userSummary by (userRepo?.observeUserSummary() ?: flowOf(null)).collectAsState(initial = null)
    val salesList by (if (uid.isNotBlank() && salesRepo != null) salesRepo.observeRecentSales(uid) else flowOf(emptyList())).collectAsState(initial = emptyList())
    val monthSummary by (if (uid.isNotBlank() && salesRepo != null) salesRepo.observeSalesSummary(uid) else flowOf(SalesMonthSummary())).collectAsState(initial = SalesMonthSummary())

    // Estado de permisos de notificación
    var hasNotificationPermission by remember {
        mutableStateOf(NotificationManagerCompat.getEnabledListenerPackages(context).contains(context.packageName))
    }

    // Filtros de búsqueda
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilterIndex by remember { mutableIntStateOf(0) } // 0: Todas, 1: Comprobadas, 2: Manuales
    var isSearchExpanded by remember { mutableStateOf(false) }

    // Diálogos modales
    var showManualSaleDialog by remember { mutableStateOf(false) }
    var showOcrConfirmDialog by remember { mutableStateOf(false) }
    var ocrDetectedAmountCents by remember { mutableLongStateOf(0L) }
    var ocrDetectedSource by remember { mutableStateOf(SaleSource.YAPE) }
    var isProcessingScore by remember { mutableStateOf(false) }

    // Launcher para selector de comprobante OCR
    val receiptPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            scope.launch {
                runCatching {
                    val stream = context.contentResolver.openInputStream(uri)
                    val bitmap = BitmapFactory.decodeStream(stream)
                    if (bitmap != null) {
                        val result = ReceiptOcrScanner.scanReceiptBitmap(bitmap)
                        if (result.isSuccess && result.amountCents > 0L) {
                            ocrDetectedAmountCents = result.amountCents
                            ocrDetectedSource = result.source
                            showOcrConfirmDialog = true
                        } else {
                            Toast.makeText(
                                context,
                                "No se detectó un monto claro en la imagen. Intenta con una captura más nítida.",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                }.onFailure {
                    Toast.makeText(context, "Error al procesar comprobante: ${it.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // Filtrado en memoria reactivo
    val filteredSales = remember(salesList, searchQuery, selectedFilterIndex) {
        salesList.filter { sale ->
            val matchesFilter = when (selectedFilterIndex) {
                1 -> sale.isVerified
                2 -> !sale.isVerified
                else -> true
            }
            val matchesSearch = if (searchQuery.isBlank()) {
                true
            } else {
                sale.amountInSoles.contains(searchQuery, ignoreCase = true) ||
                        (sale.customerSnippet?.contains(searchQuery, ignoreCase = true) == true) ||
                        (sale.productName?.contains(searchQuery, ignoreCase = true) == true)
            }
            matchesFilter && matchesSearch
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(KuotaColors.Paper)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 96.dp)
        ) {
            // CABECERA
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.sales_title),
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = KuotaColors.ForestInk
                        )
                        IconButton(
                            onClick = onOpenMyProducts,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(KuotaColors.Fog)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ReceiptLong,
                                contentDescription = "Mis productos",
                                tint = KuotaColors.ForestInk
                            )
                        }
                    }
                }
            }

            // TARJETA HÉROE CON DESGLOSE COMPROBADAS VS NO COMPROBADAS
            item {
                Box(modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)) {
                    KuotaCard(
                        variant = KuotaCardVariant.Forest,
                        isLargeRadius = true,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = stringResource(R.string.sales_hero_title),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = KuotaColors.Pebble
                                )
                                ChipPill(
                                    label = if (hasNotificationPermission) "Captura activa" else "Sin conexión",
                                    isSelected = hasNotificationPermission,
                                    onClick = {}
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = monthSummary.totalInSoles,
                                fontSize = 34.sp,
                                fontWeight = FontWeight.Black,
                                color = KuotaColors.LimeVoltage
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(KuotaRadii.Card))
                                    .background(KuotaColors.Spruce)
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        text = "Comprobadas (Yape/Plin)",
                                        fontSize = 11.sp,
                                        color = KuotaColors.Pebble
                                    )
                                    Text(
                                        text = monthSummary.verifiedInSoles,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = KuotaColors.Paper
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "Declaradas manual",
                                        fontSize = 11.sp,
                                        color = KuotaColors.Pebble
                                    )
                                    Text(
                                        text = monthSummary.unverifiedInSoles,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = KuotaColors.Paper
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // AVISO / PERMISOS DE NOTIFICACIONES BANCARIAS
            if (!hasNotificationPermission) {
                item {
                    Box(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
                        KuotaCard(
                            variant = KuotaCardVariant.Linen,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.NotificationsActive,
                                        contentDescription = null,
                                        tint = KuotaColors.ForestInk,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = stringResource(R.string.sales_notification_permission_title),
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = KuotaColors.ForestInk
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = stringResource(R.string.sales_notification_permission_desc),
                                    fontSize = 12.sp,
                                    lineHeight = 16.sp,
                                    color = KuotaColors.Charcoal
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                PillButton(
                                    text = stringResource(R.string.sales_notification_permission_btn),
                                    variant = PillButtonVariant.Primary,
                                    onClick = {
                                        runCatching {
                                            context.startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }
            }

            // BOTONES DE ACCIÓN: OCR & MANUAL
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    PillButton(
                        text = stringResource(R.string.sales_btn_scan_ocr),
                        variant = PillButtonVariant.Secondary,
                        onClick = { receiptPickerLauncher.launch("image/*") },
                        modifier = Modifier.weight(1f)
                    )
                    PillButton(
                        text = stringResource(R.string.sales_btn_manual),
                        variant = PillButtonVariant.Tertiary,
                        onClick = { showManualSaleDialog = true },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // CARD DE SCORE CREDITICIO Y RECALCULAR
            item {
                val score = userSummary?.riskScore ?: 650
                val level = userSummary?.level ?: 1
                Box(modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)) {
                    KuotaCard(
                        variant = KuotaCardVariant.Fog,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Speed,
                                        contentDescription = null,
                                        tint = KuotaColors.ForestInk,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = stringResource(R.string.sales_score_card_title),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = KuotaColors.ForestInk
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Nivel $level • Score $score pts",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Black,
                                    color = KuotaColors.ForestInk
                                )
                            }

                            if (isProcessingScore) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = KuotaColors.ForestInk,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                PillButton(
                                    text = "Actualizar",
                                    variant = PillButtonVariant.Secondary,
                                    onClick = {
                                        scope.launch {
                                            isProcessingScore = true
                                            salesRepo?.recomputeScore()
                                                ?.onSuccess { eval ->
                                                    Toast.makeText(
                                                        context,
                                                        "Score actualizado: ${eval.score} pts (Nivel ${eval.level})",
                                                        Toast.LENGTH_LONG
                                                    ).show()
                                                }
                                                ?.onFailure { err ->
                                                    Toast.makeText(
                                                        context,
                                                        "Error al actualizar score: ${err.message}",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                            isProcessingScore = false
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // BUSCADOR Y FILTROS
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Historial de cobros",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = KuotaColors.ForestInk
                        )
                        IconButton(onClick = { isSearchExpanded = !isSearchExpanded }) {
                            Icon(
                                imageVector = if (isSearchExpanded) Icons.Default.Close else Icons.Default.Search,
                                contentDescription = "Buscar",
                                tint = KuotaColors.ForestInk
                            )
                        }
                    }

                    AnimatedVisibility(visible = isSearchExpanded) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text(stringResource(R.string.sales_search_placeholder), fontSize = 13.sp) },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            shape = RoundedCornerShape(KuotaRadii.Pill),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = KuotaColors.ForestInk,
                                unfocusedBorderColor = KuotaColors.Fog
                            )
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ChipPill(
                            label = stringResource(R.string.sales_chip_all),
                            isSelected = selectedFilterIndex == 0,
                            onClick = { selectedFilterIndex = 0 }
                        )
                        ChipPill(
                            label = stringResource(R.string.sales_chip_verified),
                            isSelected = selectedFilterIndex == 1,
                            onClick = { selectedFilterIndex = 1 }
                        )
                        ChipPill(
                            label = stringResource(R.string.sales_chip_unverified),
                            isSelected = selectedFilterIndex == 2,
                            onClick = { selectedFilterIndex = 2 }
                        )
                    }
                }
            }

            // LISTA DE VENTAS
            if (filteredSales.isEmpty()) {
                item {
                    EmptyState(
                        title = stringResource(R.string.sales_empty_title),
                        description = stringResource(R.string.sales_empty_desc),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 40.dp)
                    )
                }
            } else {
                items(filteredSales, key = { it.id }) { sale ->
                    SaleItemRow(sale = sale)
                }
            }
        }
    }

    // MODAL REGISTRO MANUAL
    if (showManualSaleDialog) {
        var manualAmountCents by remember { mutableLongStateOf(0L) }
        var manualConcept by remember { mutableStateOf("") }
        var isSaving by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { if (!isSaving) showManualSaleDialog = false },
            title = {
                Text(
                    text = stringResource(R.string.sales_manual_dialog_title),
                    fontWeight = FontWeight.Bold,
                    color = KuotaColors.ForestInk
                )
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = stringResource(R.string.sales_manual_amount_label),
                        fontSize = 12.sp,
                        color = KuotaColors.Charcoal
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    AmountInput(
                        cents = manualAmountCents,
                        onCentsChange = { manualAmountCents = it }
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    OutlinedTextField(
                        value = manualConcept,
                        onValueChange = { manualConcept = it },
                        label = { Text(stringResource(R.string.sales_manual_concept_label)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(KuotaRadii.Card)
                    )
                }
            },
            confirmButton = {
                PillButton(
                    text = stringResource(R.string.sales_manual_btn_save),
                    variant = PillButtonVariant.Primary,
                    isLoading = isSaving,
                    enabled = manualAmountCents > 0L && !isSaving,
                    onClick = {
                        scope.launch {
                            isSaving = true
                            salesRepo?.registerManualSale(manualAmountCents, manualConcept, isOcr = false)
                                ?.onSuccess {
                                    Toast.makeText(context, "Venta registrada", Toast.LENGTH_SHORT).show()
                                    showManualSaleDialog = false
                                }
                                ?.onFailure {
                                    Toast.makeText(context, "Error: ${it.message}", Toast.LENGTH_SHORT).show()
                                }
                            isSaving = false
                        }
                    }
                )
            },
            dismissButton = {
                TextButton(onClick = { showManualSaleDialog = false }) {
                    Text("Cancelar", color = KuotaColors.Charcoal)
                }
            }
        )
    }

    // MODAL CONFIRMACIÓN OCR
    if (showOcrConfirmDialog) {
        var isSavingOcr by remember { mutableStateOf(false) }
        AlertDialog(
            onDismissRequest = { if (!isSavingOcr) showOcrConfirmDialog = false },
            title = {
                Text(
                    text = "Comprobante Detectado",
                    fontWeight = FontWeight.Bold,
                    color = KuotaColors.ForestInk
                )
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Inteligencia artificial en dispositivo detectó:",
                        fontSize = 13.sp,
                        color = KuotaColors.Charcoal
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "S/ ${(ocrDetectedAmountCents / 100.0).toString()}",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black,
                        color = KuotaColors.ForestInk
                    )
                    Text(
                        text = "Canal: ${ocrDetectedSource.name} (Verificado)",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = KuotaColors.Spruce
                    )
                }
            },
            confirmButton = {
                PillButton(
                    text = "Confirmar y registrar",
                    variant = PillButtonVariant.Primary,
                    isLoading = isSavingOcr,
                    onClick = {
                        scope.launch {
                            isSavingOcr = true
                            salesRepo?.registerManualSale(ocrDetectedAmountCents, "Comprobante ${ocrDetectedSource.name}", isOcr = true)
                                ?.onSuccess {
                                    Toast.makeText(context, "Comprobante verificado registrado", Toast.LENGTH_SHORT).show()
                                    showOcrConfirmDialog = false
                                }
                                ?.onFailure {
                                    Toast.makeText(context, "Error: ${it.message}", Toast.LENGTH_SHORT).show()
                                }
                            isSavingOcr = false
                        }
                    }
                )
            },
            dismissButton = {
                TextButton(onClick = { showOcrConfirmDialog = false }) {
                    Text("Descartar", color = KuotaColors.Charcoal)
                }
            }
        )
    }
}

@Composable
fun SaleItemRow(
    sale: SaleRecord,
    modifier: Modifier = Modifier
) {
    val formatter = remember { SimpleDateFormat("dd MMM, hh:mm a", Locale("es", "PE")) }
    val formattedDate = remember(sale.timestampMs) {
        if (sale.timestampMs > 0L) formatter.format(Date(sale.timestampMs)) else "Hoy"
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 5.dp)
    ) {
        KuotaCard(
            variant = KuotaCardVariant.Paper,
            contentPadding = 12.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(if (sale.isVerified) KuotaColors.LinenMist else KuotaColors.Fog),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (sale.isVerified) Icons.Default.CheckCircle else Icons.Default.ReceiptLong,
                            contentDescription = null,
                            tint = if (sale.isVerified) KuotaColors.ForestInk else KuotaColors.Charcoal,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = sale.productName ?: "Cobro ${sale.source.name}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = KuotaColors.ForestInk
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "${sale.source.name} • $formattedDate",
                                fontSize = 11.sp,
                                color = KuotaColors.Pebble
                            )
                            if (sale.customerSnippet != null) {
                                Text(
                                    text = " • ${sale.customerSnippet}",
                                    fontSize = 11.sp,
                                    color = KuotaColors.Charcoal
                                )
                            }
                        }
                    }
                }

                Text(
                    text = sale.amountInSoles,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black,
                    color = if (sale.isVerified) KuotaColors.ForestInk else KuotaColors.Charcoal
                )
            }
        }
    }
}
