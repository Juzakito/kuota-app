package com.kouta.app.feature.payments

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.kouta.app.core.designsystem.components.StatusPill
import com.kouta.app.core.designsystem.theme.kuotaColors
import com.kouta.app.data.remote.QuoteInstallmentDto
import com.kouta.app.domain.model.PurchaseOffer
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

private enum class WizardStep(val stepIndex: Int, val totalSteps: Int) {
    SELECT_METHOD(1, 5),
    ACCOUNT_DETAILS(2, 5),
    ORIGIN_AND_REFERENCE(3, 5),
    REVIEW_BEFORE_CONFIRM(4, 5),
    VERIFYING_AND_TIMELINE(5, 5)
}

private data class PaymentChannelOption(
    val id: String,
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val beneficiaryName: String,
    val accountLabel: String,
    val accountValue: String,
    val secondaryLabel: String,
    val secondaryValue: String,
    val ruc: String = "20609999991",
    val bankName: String
)

private val PAYMENT_CHANNELS = listOf(
    PaymentChannelOption(
        id = "TRANSFERENCIA",
        title = "Transferencia",
        subtitle = "BCP, BBVA, Interbank o Scotiabank",
        icon = Icons.Default.AccountBalance,
        beneficiaryName = "Kuota Perú S.A.C.",
        accountLabel = "Cuenta Corriente Soles (BCP)",
        accountValue = "191-2698452-0-88",
        secondaryLabel = "Código Interbancario (CCI)",
        secondaryValue = "00219100269845208854",
        bankName = "Banco de Crédito del Perú (BCP)"
    ),
    PaymentChannelOption(
        id = "PAGO_MOVIL",
        title = "Pago Móvil (Yape / Plin)",
        subtitle = "Pago inmediato con número de celular",
        icon = Icons.Default.PhoneAndroid,
        beneficiaryName = "Kuota Perú S.A.C.",
        accountLabel = "Celular afiliado Yape / Plin",
        accountValue = "999888777",
        secondaryLabel = "Titular visible en billetera",
        secondaryValue = "KUOTA PERU RECAUDACION",
        bankName = "Yape / Plin Interoperable"
    ),
    PaymentChannelOption(
        id = "DEPOSITO",
        title = "Depósito en Agente",
        subtitle = "Agentes BCP, KasNet o MultiRed",
        icon = Icons.Default.Storefront,
        beneficiaryName = "Kuota Perú S.A.C.",
        accountLabel = "Código de Convenio / Cuenta",
        accountValue = "191-2698452-0-88",
        secondaryLabel = "Referencia de comercio",
        secondaryValue = "KUOTA-INVENTARIO-B2B",
        bankName = "Red de Agentes Autorizados"
    )
)

private val PERU_BANKS = listOf(
    "Banco de Crédito del Perú (BCP)",
    "Yape",
    "Plin",
    "BBVA Perú",
    "Interbank",
    "Scotiabank Perú",
    "Banco de la Nación"
)

@Composable
fun InstallmentPaymentWizardDialog(
    supplierName: String,
    selectedInstallments: List<QuoteInstallmentDto>,
    isSubmitting: Boolean,
    onDismiss: () -> Unit,
    onConfirmPayment: (
        channelId: String,
        originBank: String,
        payerPhone: String,
        paymentDate: String,
        referenceNumber: String,
        receiptUri: String?
    ) -> Unit
) {
    val context = LocalContext.current
    val kuotaColors = MaterialTheme.kuotaColors
    val totalAmountCents = remember(selectedInstallments) {
        selectedInstallments.sumOf { it.amountCents }
    }
    val totalFormatted = remember(totalAmountCents) {
        PurchaseOffer.formatPen(totalAmountCents)
    }

    var currentStep by remember { mutableStateOf(WizardStep.SELECT_METHOD) }

    val activity = context as? android.app.Activity
    androidx.compose.runtime.DisposableEffect(Unit) {
        com.kouta.app.core.security.ScreenSecurityManager.enterSensitiveScreen(activity)
        onDispose {
            com.kouta.app.core.security.ScreenSecurityManager.exitSensitiveScreen(
                activity,
                com.kouta.app.core.security.PinSecurityManager.isLocked(context)
            )
        }
    }
    var selectedChannel by remember { mutableStateOf(PAYMENT_CHANNELS.first()) }
    var selectedOriginBank by remember { mutableStateOf(PERU_BANKS.first()) }
    var payerPhone by remember { mutableStateOf("987654321") }
    var paymentDate by remember {
        mutableStateOf(LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
    }
    var referenceNumber by remember { mutableStateOf("482910") }
    var referenceError by remember { mutableStateOf<String?>(null) }
    var attachedReceiptUri by remember { mutableStateOf<Uri?>(null) }
    var timelineTabIndex by remember { mutableIntStateOf(0) }
    val timestampNow = remember {
        LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"))
    }

    val receiptPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            attachedReceiptUri = uri
            Toast.makeText(context, "Comprobante adjuntado correctamente", Toast.LENGTH_SHORT).show()
        }
    }

    Dialog(
        onDismissRequest = { if (!isSubmitting) onDismiss() },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Encabezado superior con barra de progreso al estilo de las referencias
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            IconButton(
                                onClick = {
                                    when (currentStep) {
                                        WizardStep.SELECT_METHOD -> onDismiss()
                                        WizardStep.ACCOUNT_DETAILS -> currentStep = WizardStep.SELECT_METHOD
                                        WizardStep.ORIGIN_AND_REFERENCE -> currentStep = WizardStep.ACCOUNT_DETAILS
                                        WizardStep.REVIEW_BEFORE_CONFIRM -> currentStep = WizardStep.ORIGIN_AND_REFERENCE
                                        WizardStep.VERIFYING_AND_TIMELINE -> onDismiss()
                                    }
                                },
                                enabled = !isSubmitting
                            ) {
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Volver",
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }

                            LinearProgressIndicator(
                                progress = { currentStep.stepIndex.toFloat() / currentStep.totalSteps.toFloat() },
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(horizontal = 12.dp)
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(50)),
                                color = MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.16f)
                            )

                            IconButton(
                                onClick = onDismiss,
                                enabled = !isSubmitting
                            ) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Cerrar",
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }

                        // Resumen compacto de cuotas, monto y tienda (como en los slides)
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                SummaryHeaderRow(
                                    label = "Cuotas a pagar",
                                    value = if (selectedInstallments.size == 1) {
                                        "1 cuota (Cuota #${selectedInstallments.first().sequenceNumber})"
                                    } else {
                                        "${selectedInstallments.size} cuotas seleccionadas"
                                    }
                                )
                                SummaryHeaderRow(label = "Monto total", value = totalFormatted, highlight = true)
                                SummaryHeaderRow(label = "Proveedor", value = supplierName.ifBlank { "Proveedor afiliado" })
                            }
                        }
                    }
                }

                // Contenido de cada paso
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp, vertical = 18.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    when (currentStep) {
                        WizardStep.SELECT_METHOD -> {
                            Text(
                                "¿Cómo vas a pagar?",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "Selecciona el medio con el que realizarás el abono de tu cuota para mostrarte los datos exactos de recaudación.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            PAYMENT_CHANNELS.forEach { channel ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            selectedChannel = channel
                                            if (channel.id == "PAGO_MOVIL") {
                                                selectedOriginBank = "Yape"
                                            }
                                            currentStep = WizardStep.ACCOUNT_DETAILS
                                        },
                                    shape = RoundedCornerShape(18.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(18.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(44.dp)
                                                .background(MaterialTheme.colorScheme.secondaryContainer, CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                channel.icon,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                            Text(
                                                channel.title,
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                channel.subtitle,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
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

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = kuotaColors.infoContainer)
                            ) {
                                Text(
                                    "A continuación te mostraremos los datos para el pago. Asegúrate de copiarlos correctamente para validar tu abono más rápido.",
                                    modifier = Modifier.padding(14.dp),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = kuotaColors.info
                                )
                            }
                        }

                        WizardStep.ACCOUNT_DETAILS -> {
                            Text(
                                if (selectedChannel.id == "PAGO_MOVIL") "Transfiere con Yape o Plin" else "Transferir a esta cuenta",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = kuotaColors.warningContainer)
                            ) {
                                Text(
                                    "ℹ️ Asegúrate de copiar los datos correctamente. Importe exacto: $totalFormatted",
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = kuotaColors.warningContent,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            CopyableFieldCard(
                                context = context,
                                label = "Titular",
                                value = selectedChannel.beneficiaryName
                            )
                            CopyableFieldCard(
                                context = context,
                                label = selectedChannel.accountLabel,
                                value = selectedChannel.accountValue
                            )
                            CopyableFieldCard(
                                context = context,
                                label = selectedChannel.secondaryLabel,
                                value = selectedChannel.secondaryValue
                            )
                            CopyableFieldCard(
                                context = context,
                                label = "RUC",
                                value = selectedChannel.ruc
                            )
                            CopyableFieldCard(
                                context = context,
                                label = "Entidad recaudadora",
                                value = selectedChannel.bankName
                            )

                            Spacer(modifier = Modifier.height(4.dp))
                            Button(
                                onClick = { currentStep = WizardStep.ORIGIN_AND_REFERENCE },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Text("Ya realicé el pago · Ingresar referencia", fontWeight = FontWeight.Bold)
                            }
                        }

                        WizardStep.ORIGIN_AND_REFERENCE -> {
                            Text(
                                "Confirma los datos de tu pago",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "Ingresa el banco o billetera de origen y los últimos 6 dígitos (o más) del número de operación.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Text(
                                "1. ¿Desde qué banco o billetera pagaste?",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Column {
                                    PERU_BANKS.forEachIndexed { index, bank ->
                                        if (index > 0) HorizontalDivider()
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable { selectedOriginBank = bank }
                                                .padding(horizontal = 16.dp, vertical = 10.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                bank,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = if (selectedOriginBank == bank) FontWeight.Bold else FontWeight.Normal,
                                                modifier = Modifier.weight(1f)
                                            )
                                            RadioButton(
                                                selected = selectedOriginBank == bank,
                                                onClick = { selectedOriginBank = bank }
                                            )
                                        }
                                    }
                                }
                            }

                            Text(
                                "2. Número de referencia y teléfono",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            OutlinedTextField(
                                value = referenceNumber,
                                onValueChange = { value ->
                                    if (value.all { it.isLetterOrDigit() || it == '-' } && value.length <= 20) {
                                        referenceNumber = value.uppercase()
                                        referenceError = null
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                label = { Text("Nro. de referencia / operación (mín. 6 caracteres)") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = payerPhone,
                                    onValueChange = { value ->
                                        if (value.all(Char::isDigit) && value.length <= 9) {
                                            payerPhone = value
                                        }
                                    },
                                    modifier = Modifier.weight(1f),
                                    label = { Text("Celular de pago") },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                                )
                                OutlinedTextField(
                                    value = paymentDate,
                                    onValueChange = { paymentDate = it.take(10) },
                                    modifier = Modifier.weight(1f),
                                    label = { Text("Fecha de pago") },
                                    singleLine = true
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf("Hoy" to LocalDate.now(), "Ayer" to LocalDate.now().minusDays(1)).forEach { (label, date) ->
                                    val formatted = date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                                    FilterChip(
                                        selected = paymentDate == formatted,
                                        onClick = { paymentDate = formatted },
                                        label = { Text("$label ($formatted)") }
                                    )
                                }
                            }

                            referenceError?.let {
                                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                            }

                            Button(
                                onClick = {
                                    if (referenceNumber.trim().length < 6) {
                                        referenceError = "Ingresa al menos los últimos 6 dígitos del número de referencia."
                                        return@Button
                                    }
                                    currentStep = WizardStep.REVIEW_BEFORE_CONFIRM
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Text("Confirmar número y revisar", fontWeight = FontWeight.Bold)
                            }
                        }

                        WizardStep.REVIEW_BEFORE_CONFIRM -> {
                            Text(
                                "Revisa los datos antes de confirmar tu pago",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "Por favor, verifica que el número de referencia y el monto coincidan con el pago realizado para validarlo de inmediato.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(18.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    ReviewDataRow(
                                        label = "Medio / Canal",
                                        value = selectedChannel.title,
                                        onModify = { currentStep = WizardStep.SELECT_METHOD }
                                    )
                                    HorizontalDivider()
                                    ReviewDataRow(
                                        label = "Banco / Billetera",
                                        value = selectedOriginBank,
                                        onModify = { currentStep = WizardStep.ORIGIN_AND_REFERENCE }
                                    )
                                    HorizontalDivider()
                                    ReviewDataRow(
                                        label = "Teléfono",
                                        value = "+51 $payerPhone",
                                        onModify = { currentStep = WizardStep.ORIGIN_AND_REFERENCE }
                                    )
                                    HorizontalDivider()
                                    ReviewDataRow(
                                        label = "Fecha",
                                        value = paymentDate,
                                        onModify = { currentStep = WizardStep.ORIGIN_AND_REFERENCE }
                                    )
                                    HorizontalDivider()
                                    ReviewDataRow(
                                        label = "Monto",
                                        value = totalFormatted,
                                        onModify = null
                                    )
                                    HorizontalDivider()
                                    ReviewDataRow(
                                        label = "Nro. Referencia",
                                        value = referenceNumber,
                                        onModify = { currentStep = WizardStep.ORIGIN_AND_REFERENCE }
                                    )
                                }
                            }

                            OutlinedButton(
                                onClick = { receiptPicker.launch("image/*") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Icon(Icons.Default.UploadFile, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
                                Text(
                                    if (attachedReceiptUri != null) "Comprobante adjuntado ✓ (Cambiar)"
                                    else "Subir captura de comprobante (opcional)"
                                )
                            }

                            Button(
                                onClick = {
                                    if (!com.kouta.app.core.security.FintechOperationLock.canExecuteAction("confirm_installment_payment")) return@Button
                                    currentStep = WizardStep.VERIFYING_AND_TIMELINE
                                    onConfirmPayment(
                                        selectedChannel.id,
                                        selectedOriginBank,
                                        payerPhone,
                                        paymentDate,
                                        referenceNumber,
                                        attachedReceiptUri?.toString()
                                    )
                                },
                                enabled = !isSubmitting,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(54.dp),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Text(
                                    if (isSubmitting) "Procesando pago…" else "Confirmar y acreditar pago",
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        WizardStep.VERIFYING_AND_TIMELINE -> {
                            // Vista combinada: "Estamos verificando tu pago" (Cashea) + Línea de tiempo "Updates | Details" (Wise)
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(22.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(20.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(64.dp)
                                            .background(
                                                if (isSubmitting) MaterialTheme.colorScheme.primaryContainer else kuotaColors.successContainer,
                                                CircleShape
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (isSubmitting) {
                                            CircularProgressIndicator(modifier = Modifier.size(30.dp), strokeWidth = 3.dp)
                                        } else {
                                            Icon(
                                                Icons.Default.Check,
                                                contentDescription = null,
                                                tint = kuotaColors.success,
                                                modifier = Modifier.size(34.dp)
                                            )
                                        }
                                    }

                                    Text(
                                        if (isSubmitting) "Estamos verificando tu pago" else "Pago acreditado en tu plan",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center
                                    )
                                    Text(
                                        totalFormatted,
                                        style = MaterialTheme.typography.headlineMedium,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    StatusPill(
                                        text = if (isSubmitting) "Procesando referencia #$referenceNumber" else "Abono conciliado · Ref #$referenceNumber",
                                        container = if (isSubmitting) kuotaColors.infoContainer else kuotaColors.successContainer,
                                        content = if (isSubmitting) kuotaColors.info else kuotaColors.success
                                    )
                                }
                            }

                            // Pestañas Updates | Details inspiradas en Wise
                            TabRow(
                                selectedTabIndex = timelineTabIndex,
                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                            ) {
                                Tab(
                                    selected = timelineTabIndex == 0,
                                    onClick = { timelineTabIndex = 0 },
                                    text = { Text("Seguimiento", fontWeight = FontWeight.SemiBold) }
                                )
                                Tab(
                                    selected = timelineTabIndex == 1,
                                    onClick = { timelineTabIndex = 1 },
                                    text = { Text("Detalles", fontWeight = FontWeight.SemiBold) }
                                )
                            }

                            if (timelineTabIndex == 0) {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(18.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(18.dp),
                                        verticalArrangement = Arrangement.spacedBy(14.dp)
                                    ) {
                                        TimelineStepRow(
                                            time = "$paymentDate · $timestampNow",
                                            title = "Registraste tu orden de pago en Kuota",
                                            completed = true,
                                            active = false
                                        )
                                        TimelineStepRow(
                                            time = "$paymentDate · $timestampNow",
                                            title = "Recibimos tu referencia #$referenceNumber ($selectedOriginBank)",
                                            completed = true,
                                            active = false
                                        )
                                        TimelineStepRow(
                                            time = "$paymentDate · $timestampNow",
                                            title = if (isSubmitting) "Conciliando movimiento en el libro contable…" else "Movimiento conciliado en el ledger de doble partida",
                                            completed = !isSubmitting,
                                            active = isSubmitting
                                        )
                                        TimelineStepRow(
                                            time = if (isSubmitting) "En breve" else "$paymentDate · $timestampNow",
                                            title = "Cuota(s) marcada(s) como PAGADA y progreso de nivel actualizado ⭐",
                                            completed = !isSubmitting,
                                            active = false,
                                            isLast = true
                                        )
                                    }
                                }
                            } else {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(18.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(18.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        SummaryHeaderRow("Canal utilizado", selectedChannel.title)
                                        SummaryHeaderRow("Banco / Billetera", selectedOriginBank)
                                        SummaryHeaderRow("Nro. Referencia", referenceNumber)
                                        SummaryHeaderRow("Teléfono pagador", "+51 $payerPhone")
                                        SummaryHeaderRow("Fecha declarada", paymentDate)
                                        SummaryHeaderRow(
                                            "Comprobante adjunto",
                                            if (attachedReceiptUri != null) "Sí (imagen verificada)" else "No requerido en demo"
                                        )
                                    }
                                }
                            }

                            if (!isSubmitting) {
                                Button(
                                    onClick = onDismiss,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(52.dp),
                                    shape = RoundedCornerShape(14.dp)
                                ) {
                                    Text("Listo · Volver a mis cuotas", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SummaryHeaderRow(label: String, value: String, highlight: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            value,
            style = if (highlight) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color = if (highlight) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun CopyableFieldCard(
    context: Context,
    label: String,
    value: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    value,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
            IconButton(
                onClick = {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    clipboard.setPrimaryClip(ClipData.newPlainText(label, value))
                    Toast.makeText(context, "$label copiado: $value", Toast.LENGTH_SHORT).show()
                }
            ) {
                Icon(
                    Icons.Default.ContentCopy,
                    contentDescription = "Copiar $label",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun ReviewDataRow(
    label: String,
    value: String,
    onModify: (() -> Unit)?
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
        }
        if (onModify != null) {
            TextButton(onClick = onModify) {
                Text("Modificar", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun TimelineStepRow(
    time: String,
    title: String,
    completed: Boolean,
    active: Boolean,
    isLast: Boolean = false
) {
    val kuotaColors = MaterialTheme.kuotaColors
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .background(
                        when {
                            completed -> kuotaColors.successContainer
                            active -> MaterialTheme.colorScheme.primaryContainer
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        },
                        CircleShape
                    )
                    .border(
                        width = if (active) 2.dp else 0.dp,
                        color = if (active) MaterialTheme.colorScheme.primary else Color.Transparent,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (completed) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = null,
                        tint = kuotaColors.success,
                        modifier = Modifier.size(14.dp)
                    )
                } else if (active) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(MaterialTheme.colorScheme.primary, CircleShape)
                    )
                }
            }
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(26.dp)
                        .background(
                            if (completed) kuotaColors.success.copy(alpha = 0.4f)
                            else MaterialTheme.colorScheme.outlineVariant
                        )
                )
            }
        }
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                time,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (completed || active) FontWeight.SemiBold else FontWeight.Normal,
                color = if (completed || active) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
