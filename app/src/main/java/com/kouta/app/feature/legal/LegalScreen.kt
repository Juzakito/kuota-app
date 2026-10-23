package com.kouta.app.feature.legal

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kouta.app.BuildConfig
import com.kouta.app.core.analytics.KuotaAnalytics
import com.kouta.app.core.designsystem.components.GroupCard
import com.kouta.app.core.designsystem.components.IconBubble
import com.kouta.app.core.designsystem.components.ScreenHeader
import com.kouta.app.core.designsystem.components.SectionTitle
import com.kouta.app.core.designsystem.components.StatusPill
import com.kouta.app.core.designsystem.theme.kuotaColors

private data class LegalDocument(
    val title: String,
    val summary: String,
    val missing: List<String>
)

private data class PrdDecisionItem(
    val code: String,
    val title: String,
    val owner: String,
    val targetGate: String,
    val detail: String
)

private val PRD_DECISIONS = listOf(
    PrdDecisionItem(
        code = "D-01",
        title = "Originador / acreedor legal",
        owner = "Dirección + Legal",
        targetGate = "Antes de piloto real",
        detail = "Definir fondeo propio, originador aliado o vehículo dedicado."
    ),
    PrdDecisionItem(
        code = "D-02",
        title = "Quién asume la mora",
        owner = "Dirección + Riesgo + Legal",
        targetGate = "Antes de piloto real",
        detail = "Definir si la mora la asume Kuota, proveedor, esquema mixto u originador."
    ),
    PrdDecisionItem(
        code = "D-03",
        title = "Perímetro SBS / crédito comercial",
        owner = "Legal regulatorio Perú",
        targetGate = "Antes de piloto real",
        detail = "Dictamen sobre registro de casa de préstamo, factoring o crédito comercial B2B."
    ),
    PrdDecisionItem(
        code = "D-04",
        title = "Contrato de proveedor B2B (60/40 y 4% fee)",
        owner = "Comercial + Legal",
        targetGate = "Antes de afiliar proveedores",
        detail = "Política de adelanto 60%, saldo 40%, comisión 4%, reversos y disputas."
    ),
    PrdDecisionItem(
        code = "D-05",
        title = "T&C, Privacidad y consentimiento",
        owner = "Legal / Privacidad",
        targetGate = "Antes de publicar app",
        detail = "Textos definitivos conforme a Ley 29733, DS 016-2024-JUS y Ley 29571."
    ),
    PrdDecisionItem(
        code = "D-06",
        title = "Pasarela de pagos y liquidación Perú",
        owner = "Producto + Finanzas + Tech",
        targetGate = "Antes de cobros reales",
        detail = "Selección y homologación de Culqi, Niubiz, Izipay o Kushki."
    ),
    PrdDecisionItem(
        code = "D-07",
        title = "Firma electrónica y evidencia contractual",
        owner = "Legal + Tech",
        targetGate = "Antes de contratos reales",
        detail = "Proveedor y trazabilidad criptográfica de aceptación contractual."
    ),
    PrdDecisionItem(
        code = "D-08",
        title = "Proveedor KYC / identidad documental",
        owner = "Riesgo + Legal + Tech",
        targetGate = "Antes de evaluación real",
        detail = "Integración de validación biométrica/documental y listas restrictivas."
    ),
    PrdDecisionItem(
        code = "D-09",
        title = "Fuentes válidas de ventas verificadas",
        owner = "Riesgo + Producto + Tech",
        targetGate = "Antes de usar ventas en score",
        detail = "Homologación de POS, facturación electrónica, adquirencia o extractos."
    ),
    PrdDecisionItem(
        code = "D-10",
        title = "Política de precios al comprador (fee fijo 4%)",
        owner = "Dirección + Legal + Finanzas",
        targetGate = "Antes de cotizar en producción",
        detail = "Estructura legal de fee fijo 4%, comisión o equivalente, mora y transparencia."
    ),
    PrdDecisionItem(
        code = "D-11",
        title = "Evolución de cuota inicial (30% a 0%)",
        owner = "Riesgo + Finanzas + Producto",
        targetGate = "Antes de mejora en producción",
        detail = "Reglas definitivas de reducción progresiva de 30% a 0% según historial de pago."
    ),
    PrdDecisionItem(
        code = "D-12",
        title = "Canal operativo del Libro de Reclamaciones",
        owner = "Operaciones + Legal",
        targetGate = "Antes de atención pública",
        detail = "Flujo operativo para responder reclamos y quejas dentro de 15 días hábiles."
    )
)

private val LEGAL_DOCUMENTS = listOf(
    LegalDocument(
        title = "Términos y Condiciones",
        summary = "Sin versión aprobada. La app no muestra ni acepta este documento.",
        missing = listOf(
            "Identidad del proveedor y descripción exacta del servicio.",
            "Precios, comisiones, tasas, mora y forma de cálculo.",
            "Reclamos, ley aplicable y solución de controversias.",
            "Versión, fecha y evidencia auditable de aceptación.",
            "Revisión de cláusulas abusivas y depósito ante INDECOPI si corresponde."
        )
    ),
    LegalDocument(
        title = "Política de Privacidad",
        summary = "Debe cumplir la Ley 29733 y el DS 016-2024-JUS. Aún no está publicada.",
        missing = listOf(
            "Responsable del tratamiento y canal de contacto.",
            "Finalidades, base legal, datos tratados y plazo de conservación.",
            "Derechos de acceso, rectificación, cancelación y oposición.",
            "Encargados, transferencias y medidas de seguridad.",
            "Protocolo de incidentes y versión vigente de la política."
        )
    ),
    LegalDocument(
        title = "Contratos de financiamiento de inventario",
        summary = "Dependen de la decisión D-01/D-02 y del dictamen legal peruano.",
        missing = listOf(
            "Partes, acreedor, desembolso, cuotas, tasas y cargos.",
            "Mora, cobranza, garantías y asunción del riesgo.",
            "Propiedad, traslado y seguro del inventario.",
            "Firma electrónica y evidencia de aceptación.",
            "Solución de controversias y ley aplicable."
        )
    )
)

private val PENDING_ITEMS = listOf(
    "Dictamen legal de D-01/D-02 y del perímetro ante la SBS (D-03).",
    "Contrato de proveedor B2B 60/40 con fee 4% (D-04) y política de precios al comprador (D-10).",
    "Términos y Condiciones aprobados y Política de Privacidad conforme al DS 016-2024-JUS (D-05).",
    "Homologación de pasarela de pagos (D-06), firma electrónica (D-07), KYC (D-08) y ventas verificadas (D-09).",
    "Política de evolución de cuota inicial 30% a 0% en producción (D-11) y atención del Libro en 15 días hábiles (D-12)."
)

@Composable
fun LegalScreen(onBack: () -> Unit) {
    val colors = MaterialTheme.kuotaColors
    var openDocument by remember { mutableStateOf<LegalDocument?>(null) }
    val analyticsEvents by KuotaAnalytics.events.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ScreenHeader(
            title = "Marco legal",
            subtitle = "Decisiones D-01 a D-12, privacidad y transparencia",
            onBack = onBack
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = colors.warningContainer)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.Top) {
                    Icon(
                        Icons.Default.Gavel,
                        contentDescription = null,
                        tint = colors.warningAccent,
                        modifier = Modifier.size(26.dp)
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            "Sin aprobación legal ni registro SBS",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = colors.warningContent
                        )
                        Text(
                            "Kuota está en modo demostración: no otorga crédito real, no cobra cuotas y no registra aceptaciones de documentos no aprobados.",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.warningContent
                        )
                    }
                }
                StatusPill(
                    text = "MODO DEMO · DOCUMENTOS NO VIGENTES",
                    container = colors.warningAccent.copy(alpha = .14f),
                    content = colors.warningContent
                )
            }
        }

        SectionTitle("Acreedor y mora · D-01 / D-02")
        GroupCard {
            LegalStatusRow(
                icon = Icons.Default.AccountBalance,
                title = "Entidad que presta el dinero",
                status = "Pendiente",
                detail = "No se ha acordado el modelo regulatorio ante la SBS. Kuota no presta dinero real."
            )
            LegalStatusRow(
                icon = Icons.Default.Gavel,
                title = "Responsable de la mora",
                status = "Pendiente",
                detail = "No se ha definido quién asume el impago ni cómo se cobraría.",
                showDivider = true
            )
            LegalStatusRow(
                icon = Icons.AutoMirrored.Filled.ReceiptLong,
                title = "Contrato aplicable",
                status = "En borrador",
                detail = "La estructura contractual depende de D-01/D-02 y del dictamen legal.",
                showDivider = true
            )
            Text(
                "Mientras estas decisiones no estén cerradas, la app no genera deudas ni obligaciones de pago.",
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        SectionTitle("Matriz de decisiones · D-01 a D-12")
        GroupCard {
            PRD_DECISIONS.forEachIndexed { index, item ->
                if (index > 0) {
                    HorizontalDivider(
                        modifier = Modifier.padding(start = 16.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = .5f)
                    )
                }
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "${item.code} · ${item.title}",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.weight(1f)
                        )
                        StatusPill(
                            text = item.targetGate,
                            container = colors.warningContainer,
                            content = colors.warningContent
                        )
                    }
                    Text(
                        "Responsable: ${item.owner}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        item.detail,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        SectionTitle("Documentos legales")
        GroupCard {
            LEGAL_DOCUMENTS.forEachIndexed { index, document ->
                DocumentRow(
                    document = document,
                    showDivider = index > 0,
                    onClick = { openDocument = document }
                )
            }
        }

        SectionTitle("Datos y privacidad")
        GroupCard {
            LegalStatusRow(
                icon = Icons.Default.Lock,
                title = "Datos que tratamos",
                status = "Cifrado local",
                detail = "Perfil, ventas manuales, reclamos y avisos. El respaldo requiere cuenta vinculada.",
                statusContainer = colors.infoContainer,
                statusContent = colors.info
            )
            LegalStatusRow(
                icon = Icons.Default.Shield,
                title = "Derechos de datos",
                status = "Pendiente",
                detail = "Ley 29733: acceso, rectificación, cancelación y oposición. Aún no hay canal formal aprobado.",
                showDivider = true
            )
            LegalStatusRow(
                icon = Icons.Default.Lock,
                title = "Terceros y transferencias",
                status = "Sin integraciones",
                detail = "No hay conexión real con Yape/Plin/POS/bancos, centrales de riesgo ni publicidad.",
                showDivider = true,
                statusContainer = colors.infoContainer,
                statusContent = colors.info
            )
        }

        SectionTitle("Analítica de producto sin PII")
        GroupCard {
            LegalStatusRow(
                icon = Icons.Default.Analytics,
                title = "Eventos registrados en sesión (${analyticsEvents.size})",
                status = "Sin PII",
                detail = "Cumple PRD §15 y §18.14: bloquea DNI, RUC, teléfonos, emails, tokens y fotos.",
                statusContainer = colors.successContainer,
                statusContent = colors.success
            )
            if (analyticsEvents.isNotEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    analyticsEvents.takeLast(6).reversed().forEach { record ->
                        Text(
                            "• ${record.eventName} ${if (record.properties.isEmpty()) "" else record.properties.toString()}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        SectionTitle("Qué falta para operar")
        GroupCard {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                PENDING_ITEMS.forEach { item ->
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.Top) {
                        Box(
                            modifier = Modifier
                                .padding(top = 6.dp)
                                .size(6.dp)
                                .background(colors.warningAccent, CircleShape)
                        )
                        Text(
                            item,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        Text(
            "Este estado es informativo y no constituye asesoría legal ni aprobación regulatoria. " +
                "Revisión: 22/10/2026 · Kuota Perú v${BuildConfig.VERSION_NAME}.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 4.dp, end = 4.dp, bottom = 12.dp)
        )
    }

    openDocument?.let { document ->
        DocumentDialog(document = document, onDismiss = { openDocument = null })
    }
}

@Composable
private fun LegalStatusRow(
    icon: ImageVector,
    title: String,
    status: String,
    detail: String,
    showDivider: Boolean = false,
    statusContainer: Color = MaterialTheme.kuotaColors.warningContainer,
    statusContent: Color = MaterialTheme.kuotaColors.warningContent
) {
    if (showDivider) {
        HorizontalDivider(
            modifier = Modifier.padding(start = 62.dp),
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = .5f)
        )
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        IconBubble(icon)
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
                StatusPill(text = status, container = statusContainer, content = statusContent)
            }
            Text(
                detail,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun DocumentRow(document: LegalDocument, showDivider: Boolean, onClick: () -> Unit) {
    if (showDivider) {
        HorizontalDivider(
            modifier = Modifier.padding(start = 62.dp),
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = .5f)
        )
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        IconBubble(Icons.Default.Description)
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(document.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            Text(
                document.summary,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            StatusPill(
                text = "Borrador",
                container = MaterialTheme.kuotaColors.warningContainer,
                content = MaterialTheme.kuotaColors.warningContent
            )
        }
        Icon(
            Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun DocumentDialog(document: LegalDocument, onDismiss: () -> Unit) {
    val colors = MaterialTheme.kuotaColors
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(document.title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatusPill(
                        text = "Borrador",
                        container = colors.warningContainer,
                        content = colors.warningContent
                    )
                    Text(
                        "No vigente",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(document.summary, style = MaterialTheme.typography.bodyMedium)
                Text(
                    "Qué falta revisar y aprobar",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                document.missing.forEach { item ->
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.Top) {
                        Box(
                            modifier = Modifier
                                .padding(top = 6.dp)
                                .size(5.dp)
                                .background(MaterialTheme.colorScheme.primary, CircleShape)
                        )
                        Text(
                            item,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Text(
                    "No se puede aceptar ni firmar desde la app hasta que exista dictamen legal y una versión aprobada.",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.warningContent
                )
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Entendido") } }
    )
}
