package com.kuota.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kuota.core.ui.theme.KuotaColors
import com.kuota.core.ui.theme.KuotaTheme

/**
 * Storybook / Showcase oficial de componentes Kuota App.
 * Permite previsualizar en Android Studio todos los componentes base
 * construidos con los tokens de DESIGN.md.
 */
@Composable
fun ComponentShowcase() {
    var amountCents by remember { mutableLongStateOf(70_000L) }
    var selectedChip by remember { mutableStateOf("15 días") }
    var selectedTab by remember { mutableStateOf(KuotaNavTab.Home) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(KuotaColors.Paper)
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Título de la galería
        Column {
            Text(
                text = "KUOTA DESIGN SYSTEM",
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
                color = KuotaColors.ForestInk,
                letterSpacing = 1.sp
            )
            Text(
                text = "Showcase de Componentes Base",
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                color = KuotaColors.Obsidian
            )
            Text(
                text = "Tokens oficiales: Forest Ink (#163300) y Lime Voltage (#9FE870).",
                fontSize = 12.sp,
                color = KuotaColors.Slate
            )
        }

        HorizontalDivider(color = KuotaColors.Fog)

        // 1. Pill Buttons
        SectionBlock(title = "1. Pill Buttons (9999dp)") {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                PillButton(
                    text = "PillButton Primario (Lime)",
                    onClick = {},
                    variant = PillButtonVariant.Primary,
                    modifier = Modifier.fillMaxWidth()
                )
                PillButton(
                    text = "PillButton Secundario (Borde Forest)",
                    onClick = {},
                    variant = PillButtonVariant.Secondary,
                    modifier = Modifier.fillMaxWidth()
                )
                PillButton(
                    text = "PillButton Terciario (Texto)",
                    onClick = {},
                    variant = PillButtonVariant.Tertiary,
                    modifier = Modifier.fillMaxWidth()
                )
                PillButton(
                    text = "Procesando operación...",
                    onClick = {},
                    isLoading = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        HorizontalDivider(color = KuotaColors.Fog)

        // 2. Chip Pills
        SectionBlock(title = "2. Chip Pills (Selección, Cero Azul)") {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("15 días", "30 días", "60 días").forEach { term ->
                    ChipPill(
                        label = term,
                        isSelected = selectedChip == term,
                        onClick = { selectedChip = term }
                    )
                }
            }
        }

        HorizontalDivider(color = KuotaColors.Fog)

        // 3. Amount Input Gigante
        SectionBlock(title = "3. AmountInput Gigante (Céntimos Long)") {
            AmountInput(
                cents = amountCents,
                onCentsChange = { amountCents = it },
                maxCents = 70_000L
            )
        }

        HorizontalDivider(color = KuotaColors.Fog)

        // 4. Progress Bar Horizontal
        SectionBlock(title = "4. ProgressBarHorizontal (Continuo)") {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Onboarding / Cuotas (65%)",
                    fontSize = 12.sp,
                    color = KuotaColors.Charcoal
                )
                ProgressBarHorizontal(progress = 0.65f)
            }
        }

        HorizontalDivider(color = KuotaColors.Fog)

        // 5. Kuota Cards
        SectionBlock(title = "5. KuotaCards (10dp / 28dp)") {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                KuotaCard(variant = KuotaCardVariant.Paper) {
                    Text(text = "Tarjeta Paper (10dp) · Superficie estándar", color = KuotaColors.Charcoal)
                }
                KuotaCard(variant = KuotaCardVariant.Linen) {
                    Text(text = "Tarjeta Linen Mist (10dp) · Contenedor de destaque", color = KuotaColors.ForestInk)
                }
                KuotaCard(variant = KuotaCardVariant.Forest, isLargeRadius = true) {
                    Text(
                        text = "Tarjeta Hero Forest Ink (28dp) · Línea de Crédito",
                        color = KuotaColors.LimeVoltage,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        HorizontalDivider(color = KuotaColors.Fog)

        // 6. Bottom Navigation
        SectionBlock(title = "6. BottomNav con Botón Central QR Plano") {
            KuotaBottomNav(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it },
                onQrClick = {}
            )
        }

        HorizontalDivider(color = KuotaColors.Fog)

        // 7. Empty State
        SectionBlock(title = "7. EmptyState") {
            EmptyState(
                title = "Sin compras pendientes",
                description = "Cuando escanees un QR en un proveedor afiliado, tus cuotas semanales aparecerán aquí.",
                actionButtonText = "Explorar proveedores",
                onActionClick = {}
            )
        }

        HorizontalDivider(color = KuotaColors.Fog)

        // 8. Error State
        SectionBlock(title = "8. ErrorState") {
            ErrorState(
                title = "Sin conexión con el servidor",
                message = "Verifica tu conexión a internet. Kuota reintentará sincronizar tus datos de forma segura.",
                onRetry = {},
                referenceCode = "ERR_OFFLINE_404"
            )
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun SectionBlock(
    title: String,
    content: @Composable () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = title,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = KuotaColors.Obsidian
        )
        content()
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 1800)
@Composable
fun ComponentShowcasePreview() {
    KuotaTheme {
        ComponentShowcase()
    }
}
