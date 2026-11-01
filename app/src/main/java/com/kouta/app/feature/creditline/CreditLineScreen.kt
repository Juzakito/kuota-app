package com.kouta.app.feature.creditline

import androidx.compose.foundation.background
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kouta.app.R
import com.kuota.core.data.repository.UserRepository
import com.kuota.core.model.UserSummary
import com.kuota.core.ui.components.ChipPill
import com.kuota.core.ui.components.KuotaCard
import com.kuota.core.ui.components.PillButton
import com.kuota.core.ui.components.PillButtonVariant
import com.kuota.core.ui.components.ProgressBarHorizontal
import com.kuota.core.ui.theme.KuotaColors
import java.util.Locale

data class CreditLevelInfo(
    val level: Int,
    val name: String,
    val maxCreditCents: Long,
    val downPaymentPercent: Int,
    val termsDescription: String,
    val unlockRequirement: String
)

val CREDIT_LEVELS = listOf(
    CreditLevelInfo(1, "Brote", 700_00L, 50, "3 cuotas semanales", "DNI verificado y negocio registrado"),
    CreditLevelInfo(2, "Raíz", 1_500_00L, 40, "3 cuotas semanales", "3 cuotas pagadas puntualmente"),
    CreditLevelInfo(3, "Tallo", 3_000_00L, 30, "3 a 4 cuotas semanales", "8 cuotas pagadas puntualmente"),
    CreditLevelInfo(4, "Rama", 5_000_00L, 25, "4 a 6 cuotas semanales", "15 cuotas pagadas puntualmente"),
    CreditLevelInfo(5, "Copa", 8_000_00L, 20, "6 cuotas semanales", "25 cuotas pagadas puntualmente"),
    CreditLevelInfo(6, "Fruto", 12_000_00L, 10, "6 a 12 cuotas semanales", "40 cuotas pagadas puntualmente")
)

@Composable
fun CreditLineScreen(
    onNavigateBack: () -> Unit,
    onNavigateToPurchase: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CreditLineViewModel = hiltViewModel()
) {
    val summary by viewModel.summary.collectAsState(initial = null)
    var showLevelsModal by remember { mutableStateOf(false) }

    val userLevel = summary?.level ?: 1
    val maxCredit = summary?.maxCreditCents ?: 70_000L
    val usedCredit = summary?.usedCreditCents ?: 0L
    val availableCredit = summary?.availableCreditCents ?: 70_000L
    val usageProgress = if (maxCredit > 0) (usedCredit.toFloat() / maxCredit.toFloat()).coerceIn(0f, 1f) else 0f

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(KuotaColors.Fog)
            .padding(horizontal = 20.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = stringResource(R.string.btn_back),
                    tint = KuotaColors.ForestInk
                )
            }
            Text(
                text = stringResource(R.string.credit_line_title),
                color = KuotaColors.ForestInk,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = { showLevelsModal = true }) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = stringResource(R.string.credit_line_levels_info),
                    tint = KuotaColors.ForestInk
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Hero Card: Disponible
        KuotaCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.credit_line_available),
                        color = KuotaColors.Charcoal,
                        fontSize = 13.sp
                    )
                    ChipPill(
                        label = "Nivel $userLevel · ${CREDIT_LEVELS.find { it.level == userLevel }?.name ?: "Brote"}",
                        isSelected = true,
                        onClick = { showLevelsModal = true }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = String.format(Locale.US, "S/ %.2f", availableCredit / 100.0),
                    color = KuotaColors.ForestInk,
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Black
                )

                Spacer(modifier = Modifier.height(16.dp))

                ProgressBarHorizontal(
                    progress = usageProgress,
                    fillColor = KuotaColors.ForestInk,
                    trackColor = KuotaColors.Fog,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = stringResource(R.string.credit_line_used),
                            color = KuotaColors.Charcoal,
                            fontSize = 12.sp
                        )
                        Text(
                            text = String.format(Locale.US, "S/ %.2f", usedCredit / 100.0),
                            color = KuotaColors.ForestInk,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = stringResource(R.string.credit_line_total),
                            color = KuotaColors.Charcoal,
                            fontSize = 12.sp
                        )
                        Text(
                            text = String.format(Locale.US, "S/ %.2f", maxCredit / 100.0),
                            color = KuotaColors.ForestInk,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // CTA Comprar al crédito
        PillButton(
            text = "Usar mi crédito en compras",
            onClick = onNavigateToPurchase,
            variant = PillButtonVariant.Primary,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Escalera de 6 niveles
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Escalera de niveles Kuota",
                color = KuotaColors.ForestInk,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            TextButton(onClick = { showLevelsModal = true }) {
                Text(
                    text = "Ver detalles",
                    color = KuotaColors.ForestInk,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        CREDIT_LEVELS.forEach { levelInfo ->
            val isCurrent = levelInfo.level == userLevel
            val isUnlocked = levelInfo.level <= userLevel

            KuotaCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isCurrent) KuotaColors.LimeVoltage
                                    else if (isUnlocked) KuotaColors.Fog
                                    else KuotaColors.Paper
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isUnlocked) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = KuotaColors.ForestInk,
                                    modifier = Modifier.size(20.dp)
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = KuotaColors.Charcoal,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Nivel ${levelInfo.level} · ${levelInfo.name}",
                                    color = KuotaColors.ForestInk,
                                    fontSize = 15.sp,
                                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium
                                )
                                if (isCurrent) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "(Actual)",
                                        color = KuotaColors.Charcoal,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Text(
                                text = "Inicial ${levelInfo.downPaymentPercent}% · ${levelInfo.termsDescription}",
                                color = KuotaColors.Charcoal,
                                fontSize = 12.sp
                            )
                        }
                    }

                    Text(
                        text = String.format(Locale.US, "Hasta S/ %.0f", levelInfo.maxCreditCents / 100.0),
                        color = KuotaColors.ForestInk,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }

    // Modal de Niveles e Información
    if (showLevelsModal) {
        AlertDialog(
            onDismissRequest = { showLevelsModal = false },
            title = {
                Text(
                    text = stringResource(R.string.credit_line_modal_title),
                    color = KuotaColors.ForestInk,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text(
                        text = "El sistema de crédito Kuota premia el pago puntual de tu MYPE. Cada nivel reduce la cuota inicial y amplía tu línea de crédito.",
                        color = KuotaColors.Charcoal,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    CREDIT_LEVELS.forEach { lvl ->
                        HorizontalDivider(color = KuotaColors.Fog, modifier = Modifier.padding(vertical = 6.dp))
                        Text(
                            text = "Nivel ${lvl.level}: ${lvl.name} (Hasta S/ ${lvl.maxCreditCents / 100})",
                            color = KuotaColors.ForestInk,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Inicial: ${lvl.downPaymentPercent}% · ${lvl.termsDescription}",
                            color = KuotaColors.Charcoal,
                            fontSize = 12.sp
                        )
                        Text(
                            text = "Requisito: ${lvl.unlockRequirement}",
                            color = KuotaColors.ForestInk,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showLevelsModal = false }) {
                    Text(
                        text = stringResource(R.string.credit_line_modal_close),
                        color = KuotaColors.ForestInk,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            containerColor = KuotaColors.Paper
        )
    }
}
