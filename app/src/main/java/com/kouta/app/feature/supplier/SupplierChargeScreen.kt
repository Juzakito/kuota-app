package com.kouta.app.feature.supplier

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import java.util.Locale
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Surface
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.kouta.app.core.designsystem.theme.kuotaColors
import com.kouta.app.data.local.PaymentLinkStore
import com.kouta.app.data.local.SalesJournalStore
import com.kouta.app.domain.model.PaymentLink
import com.kouta.app.domain.model.PaymentLinkStatus
import com.kouta.app.domain.model.PurchaseOffer
import com.kouta.app.feature.financing.QrCodeToolkit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupplierChargeScreen() {
    val context = LocalContext.current
    val journalStore = remember(context) { SalesJournalStore(context) }
    val linkStore = remember(context) { PaymentLinkStore(context) }
    val clipboardManager = LocalClipboardManager.current
    val scope = rememberCoroutineScope()
    val kuotaColors = MaterialTheme.kuotaColors

    var selectedTab by remember { mutableIntStateOf(0) }
    var businessName by remember { mutableStateOf("Mi Comercio") }
    var commercialCategory by remember { mutableStateOf("Textil y Confección") }
    var commercialZone by remember { mutableStateOf("Gamarra, La Victoria") }
    var rucNumber by remember { mutableStateOf("") }
    var qrBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var qrPayload by remember { mutableStateOf("") }

    // Link de pago states
    var clientName by remember { mutableStateOf("") }
    var clientDni by remember { mutableStateOf("") }
    var clientPhone by remember { mutableStateOf("") }
    var linkConcept by remember { mutableStateOf("") }
    var amountInput by remember { mutableStateOf("") }
    var receiptType by remember { mutableStateOf("BOLETA") }
    var isReceiptDropdownExpanded by remember { mutableStateOf(false) }
    var internalNote by remember { mutableStateOf("") }
    var thirdPartyDataConsent by remember { mutableStateOf(false) }
    var lastCreatedLink by remember { mutableStateOf<PaymentLink?>(null) }
    var myLinks by remember { mutableStateOf<List<PaymentLink>>(emptyList()) }

    fun refreshLinks() {
        scope.launch {
            myLinks = withContext(Dispatchers.IO) { linkStore.getAll() }
        }
    }

    LaunchedEffect(Unit) {
        val profile = withContext(Dispatchers.IO) { journalStore.readBusinessProfile() }
        businessName = profile?.businessName.orEmpty().ifBlank { "Comercio Kuota" }
        commercialCategory = profile?.commercialCategory.orEmpty().ifBlank { "Comercio Aliado" }
        commercialZone = profile?.commercialZone.orEmpty().ifBlank { "Gamarra, La Victoria" }
        rucNumber = profile?.ruc.orEmpty()
        val id = profile?.ruc.orEmpty().ifBlank { profile?.phone.orEmpty() }.ifBlank { "sup-001" }
        val encodedName = URLEncoder.encode(businessName, StandardCharsets.UTF_8.name())
        qrPayload = "kuota://pay?supplierId=$id&name=$encodedName"
        qrBitmap = QrCodeToolkit.generateQrBitmap(qrPayload, 600)
        refreshLinks()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.QrCode2, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("QR Negocio")
                    }
                }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Link, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Link de Pago (1%)")
                    }
                }
            )
        }

        if (selectedTab == 0) {
            // QR Negocio Tab (PRD 13.4)
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Código QR Oficial de Tienda",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Colócalo en tu mostrador o muéstralo a las pymes compradoras",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(20.dp))

                // QR Container Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            businessName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                        Text(
                            "$commercialCategory · $commercialZone",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF2D5FCC),
                            fontWeight = FontWeight.SemiBold
                        )
                        if (rucNumber.isNotBlank()) {
                            Text(
                                "RUC: $rucNumber",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF64748B)
                            )
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        if (qrBitmap != null) {
                            Image(
                                bitmap = qrBitmap!!.asImageBitmap(),
                                contentDescription = "QR de Pago Kuota",
                                modifier = Modifier
                                    .size(240.dp)
                                    .clip(RoundedCornerShape(12.dp))
                            )
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFF1F5F9))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                "Cobro de cuota inicial en mano · Saldo 98% en 48h",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Información Operativa PRD 13.4
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            "¿Cómo funciona QR Business? (PRD §13.4)",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "• El comprador digita el monto al escanear; tú no necesitas digitar nada.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            "• Cobras la cuota inicial directo por Yape, Plin o transferencia bancaria.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            "• Kuota te transfiere el saldo restante (98% total - inicial) en 48 horas hábiles.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            clipboardManager.setText(AnnotatedString(qrPayload))
                            Toast.makeText(context, "Código QR copiado al portapapeles", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Copiar QR")
                    }

                    Button(
                        onClick = {
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, "Paga con Kuota en $businessName usando este código: $qrPayload")
                                type = "text/plain"
                            }
                            context.startActivity(Intent.createChooser(sendIntent, "Compartir QR de tienda"))
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Compartir")
                    }
                }

                Spacer(modifier = Modifier.height(80.dp))
            }
        } else {
            // Link de Pago Tab (PRD 13.3)
            val parsedAmount = amountInput.replace(",", ".").toDoubleOrNull() ?: 0.0
            val amountCents = (parsedAmount * 100).toLong()
            val feeCents = (amountCents * 1L) / 100L
            val netCents = amountCents - feeCents

            val isDniValid = clientDni.length == 8 && clientDni.all { it.isDigit() }
            val isPhoneValid = clientPhone.length == 9 && clientPhone.startsWith("9") && clientPhone.all { it.isDigit() }
            val isNameValid = clientName.trim().isNotBlank()
            val isConceptValid = linkConcept.trim().isNotBlank()
            val canGenerate = amountCents > 0L && isNameValid && isDniValid && isPhoneValid && isConceptValid && thirdPartyDataConsent

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    "Generador de Link de Pago",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Cobra a clientes finales a distancia. Comisión fija de 1% (PRD §13.3).",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = linkConcept,
                    onValueChange = { linkConcept = it },
                    label = { Text("Concepto o producto a cobrar *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "Monto total a cobrar",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                "S/ ",
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (amountInput.isNotBlank()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                                )
                            )
                            BasicTextField(
                                value = amountInput,
                                onValueChange = { amountInput = it },
                                textStyle = MaterialTheme.typography.headlineLarge.copy(
                                    fontSize = 38.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.primary
                                ),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                                decorationBox = { innerTextField ->
                                    if (amountInput.isEmpty()) {
                                        Text(
                                            "0.00",
                                            style = MaterialTheme.typography.headlineLarge.copy(
                                                fontSize = 38.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f)
                                            )
                                        )
                                    }
                                    innerTextField()
                                }
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Botones rápidos (+20, +50, +100, +200) (PRD §5)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(20.0, 50.0, 100.0, 200.0).forEach { preset ->
                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable {
                                            val cur = amountInput.replace(",", ".").toDoubleOrNull() ?: 0.0
                                            amountInput = String.format(Locale.US, "%.2f", cur + preset)
                                        },
                                    shape = RoundedCornerShape(50),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                                ) {
                                    Text(
                                        "+${preset.toInt()}",
                                        modifier = Modifier.padding(vertical = 8.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }

                Text(
                    "Datos del cliente (comprobante y Ley N° 29733):",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )

                OutlinedTextField(
                    value = clientName,
                    onValueChange = { clientName = it },
                    label = { Text("Nombre o Razón Social *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = clientDni,
                        onValueChange = { if (it.length <= 8 && it.all(Char::isDigit)) clientDni = it },
                        label = { Text("DNI (8 dígitos) *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        isError = clientDni.isNotEmpty() && !isDniValid,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = clientPhone,
                        onValueChange = { if (it.length <= 9 && it.all(Char::isDigit)) clientPhone = it },
                        label = { Text("WhatsApp *") },
                        prefix = { Text("+51 ") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        isError = clientPhone.isNotEmpty() && !isPhoneValid,
                        modifier = Modifier.weight(1.2f),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ExposedDropdownMenuBox(
                        expanded = isReceiptDropdownExpanded,
                        onExpandedChange = { isReceiptDropdownExpanded = it },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = receiptType,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Comprobante") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isReceiptDropdownExpanded) },
                            modifier = Modifier
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = true)
                                .fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        ExposedDropdownMenu(
                            expanded = isReceiptDropdownExpanded,
                            onDismissRequest = { isReceiptDropdownExpanded = false }
                        ) {
                            DropdownMenuItem(text = { Text("Boleta de Venta") }, onClick = { receiptType = "BOLETA"; isReceiptDropdownExpanded = false })
                            DropdownMenuItem(text = { Text("Factura Comercial") }, onClick = { receiptType = "FACTURA"; isReceiptDropdownExpanded = false })
                        }
                    }

                    OutlinedTextField(
                        value = internalNote,
                        onValueChange = { internalNote = it },
                        label = { Text("Nota interna") },
                        singleLine = true,
                        modifier = Modifier.weight(1.2f),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                // Checkbox Ley 29733
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = thirdPartyDataConsent,
                        onCheckedChange = { thirdPartyDataConsent = it },
                        colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary)
                    )
                    Text(
                        "Cuento con consentimiento del cliente para el tratamiento de sus datos de facturación (Ley N° 29733).",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Desglose Contable 1%
                if (amountCents > 0L) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("Desglose Contable (PRD §13.3)", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Monto a cobrar:")
                                Text(PurchaseOffer.formatPen(amountCents), fontWeight = FontWeight.Bold)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Comisión Kuota (1% plano):")
                                Text("- ${PurchaseOffer.formatPen(feeCents)}", color = MaterialTheme.colorScheme.error)
                            }
                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Abono neto en tu cuenta (99%):", fontWeight = FontWeight.Bold)
                                Text(PurchaseOffer.formatPen(netCents), fontWeight = FontWeight.Bold, color = kuotaColors.accentTeal)
                            }
                        }
                    }
                }

                Button(
                    onClick = {
                        scope.launch {
                            val newLink = linkStore.createLink(
                                sellerId = rucNumber.ifBlank { "sup-001" },
                                sellerName = businessName,
                                customerName = clientName.trim(),
                                customerDni = clientDni.trim(),
                                customerPhone = clientPhone.trim(),
                                concept = linkConcept.trim(),
                                quantity = 1,
                                unitPriceCents = amountCents,
                                totalAmountCents = amountCents,
                                receiptType = receiptType,
                                internalNote = internalNote.trim()
                            )
                            lastCreatedLink = newLink
                            refreshLinks()
                            Toast.makeText(context, "Link generado con éxito", Toast.LENGTH_SHORT).show()
                        }
                    },
                    enabled = canGenerate,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 52.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Link, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Generar Link de Cobro (1%)", fontWeight = FontWeight.Bold)
                }

                // Tarjeta de Enlace Generado Reciente
                if (lastCreatedLink != null) {
                    val link = lastCreatedLink!!
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = kuotaColors.accentTeal.copy(alpha = 0.12f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Enlace Listo para Enviar", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(kuotaColors.accentTeal.copy(alpha = 0.25f))
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(link.status.label, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = kuotaColors.accentTeal)
                                }
                            }
                            Text(link.linkUrl, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        clipboardManager.setText(AnnotatedString(link.linkUrl))
                                        Toast.makeText(context, "Enlace copiado", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Copiar", style = MaterialTheme.typography.labelSmall)
                                }

                                Button(
                                    onClick = {
                                        scope.launch {
                                            linkStore.markAsSent(link.id)
                                            lastCreatedLink = link.copy(status = PaymentLinkStatus.ENVIADO)
                                            refreshLinks()
                                        }
                                        val cleanPhone = link.customerPhone.replace(" ", "").replace("-", "")
                                        val message = "Hola ${link.customerName}, aquí tienes tu enlace de pago Kuota por ${PurchaseOffer.formatPen(link.amountCents)}: ${link.linkUrl}"
                                        val intent = Intent(Intent.ACTION_VIEW).apply {
                                            data = Uri.parse("https://api.whatsapp.com/send?phone=51$cleanPhone&text=${URLEncoder.encode(message, StandardCharsets.UTF_8.name())}")
                                        }
                                        runCatching { context.startActivity(intent) }
                                            .onFailure {
                                                val shareIntent = Intent().apply {
                                                    action = Intent.ACTION_SEND
                                                    putExtra(Intent.EXTRA_TEXT, message)
                                                    type = "text/plain"
                                                }
                                                context.startActivity(Intent.createChooser(shareIntent, "Compartir enlace"))
                                            }
                                    },
                                    modifier = Modifier.weight(1.3f),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366))
                                ) {
                                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("WhatsApp", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }

                            if (link.status != PaymentLinkStatus.PAGADO) {
                                Button(
                                    onClick = {
                                        scope.launch {
                                            linkStore.markAsPaid(link.id)
                                            lastCreatedLink = link.copy(status = PaymentLinkStatus.PAGADO)
                                            refreshLinks()
                                            Toast.makeText(context, "¡Pago simulado! Venta comprobada registrada en Finanzas.", Toast.LENGTH_LONG).show()
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary,
                                        contentColor = MaterialTheme.colorScheme.onPrimary
                                    )
                                ) {
                                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Simular pago del cliente (Demo)", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                // Historial de Links de Pago del Proveedor
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    "Links Generados (${myLinks.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                if (myLinks.isEmpty()) {
                    Text(
                        "Aún no has generado links de cobro.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    myLinks.take(10).forEach { item ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(item.customerName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(
                                                when (item.displayStatus) {
                                                    PaymentLinkStatus.PAGADO -> kuotaColors.accentTeal.copy(alpha = 0.2f)
                                                    PaymentLinkStatus.ENVIADO -> MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                                    PaymentLinkStatus.VENCIDO -> MaterialTheme.colorScheme.error.copy(alpha = 0.2f)
                                                    else -> MaterialTheme.colorScheme.surfaceVariant
                                                }
                                            )
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            item.displayStatus.label,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = when (item.displayStatus) {
                                                PaymentLinkStatus.PAGADO -> kuotaColors.accentTeal
                                                PaymentLinkStatus.ENVIADO -> MaterialTheme.colorScheme.primary
                                                PaymentLinkStatus.VENCIDO -> MaterialTheme.colorScheme.error
                                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                                            }
                                        )
                                    }
                                }
                                Text("Concepto: ${item.concept}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Cobro: ${PurchaseOffer.formatPen(item.amountCents)}", style = MaterialTheme.typography.bodySmall)
                                    Text("Neto: ${PurchaseOffer.formatPen(item.netAmountCents)}", fontWeight = FontWeight.Bold, color = kuotaColors.accentTeal, style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}
