package com.kouta.app.feature.purchase

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kouta.app.R
import com.kuota.core.model.InstallmentScheduleItem
import com.kuota.core.model.PurchaseQuote
import com.kuota.core.ui.components.AmountInput
import com.kuota.core.ui.components.ChipPill
import com.kuota.core.ui.components.KuotaCard
import com.kuota.core.ui.components.PillButton
import com.kuota.core.ui.components.PillButtonVariant
import com.kuota.core.ui.components.ProgressBarHorizontal
import com.kuota.core.ui.theme.KuotaColors
import java.util.Locale

@Composable
fun PurchaseFlowScreen(
    viewModel: PurchaseViewModel,
    onNavigateBack: () -> Unit,
    onFinishToHome: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(KuotaColors.Fog)
            .padding(horizontal = 20.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Encabezado superior con botón de cerrar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.purchase_title),
                color = KuotaColors.ForestInk,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            IconButton(
                onClick = {
                    if (state.currentStep == PurchaseFlowStep.STEP_2_WAITING) {
                        viewModel.cancelPurchase()
                    } else if (state.currentStep == PurchaseFlowStep.STEP_SUCCESS) {
                        onFinishToHome()
                    } else {
                        onNavigateBack()
                    }
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = stringResource(R.string.btn_back),
                    tint = KuotaColors.ForestInk
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Barra de progreso de 3 pasos
        val progress = when (state.currentStep) {
            PurchaseFlowStep.STEP_1_DETAILS -> 0.33f
            PurchaseFlowStep.STEP_2_WAITING -> 0.66f
            PurchaseFlowStep.STEP_3_CONFIRM -> 0.90f
            PurchaseFlowStep.STEP_SUCCESS -> 1.0f
        }
        ProgressBarHorizontal(
            progress = progress,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Contenido según el paso actual
        when (state.currentStep) {
            PurchaseFlowStep.STEP_1_DETAILS -> {
                Step1DetailsContent(
                    state = state,
                    onAmountChanged = viewModel::onAmountChanged,
                    onInstallmentSelected = viewModel::onInstallmentsSelected,
                    onSubmit = viewModel::requestPurchase
                )
            }
            PurchaseFlowStep.STEP_2_WAITING -> {
                Step2WaitingContent(
                    state = state,
                    onCancel = viewModel::cancelPurchase
                )
            }
            PurchaseFlowStep.STEP_3_CONFIRM -> {
                Step3ConfirmContent(
                    state = state,
                    onConfirm = viewModel::confirmPurchase,
                    onCancel = viewModel::cancelPurchase
                )
            }
            PurchaseFlowStep.STEP_SUCCESS -> {
                StepSuccessContent(
                    state = state,
                    onFinish = {
                        viewModel.reset()
                        onFinishToHome()
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun Step1DetailsContent(
    state: PurchaseUiState,
    onAmountChanged: (String) -> Unit,
    onInstallmentSelected: (Int) -> Unit,
    onSubmit: () -> Unit
) {
    Column {
        Text(
            text = stringResource(R.string.purchase_step1_title),
            color = KuotaColors.ForestInk,
            fontSize = 24.sp,
            fontWeight = FontWeight.Black
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = stringResource(R.string.purchase_step1_subtitle),
            color = KuotaColors.Charcoal,
            fontSize = 14.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Tarjeta Proveedor
        KuotaCard(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(KuotaColors.LimeVoltage),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Storefront,
                        contentDescription = state.supplierName,
                        tint = KuotaColors.ForestInk,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = stringResource(R.string.purchase_supplier_label),
                        color = KuotaColors.Charcoal,
                        fontSize = 12.sp
                    )
                    Text(
                        text = state.supplierName,
                        color = KuotaColors.ForestInk,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Monto Input gigante
        Text(
            text = stringResource(R.string.purchase_amount_label),
            color = KuotaColors.ForestInk,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(8.dp))
        AmountInput(
            cents = state.amountCents,
            onCentsChange = { newCents ->
                onAmountChanged((newCents / 100).toString())
            },
            maxCents = 1_200_000L,
            minCents = 19_900L,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Plazo de cuotas
        Text(
            text = stringResource(R.string.purchase_installments_label),
            color = KuotaColors.ForestInk,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            state.availableInstallmentOptions.forEach { count ->
                ChipPill(
                    label = "$count cuotas",
                    isSelected = state.selectedInstallmentsCount == count,
                    onClick = { onInstallmentSelected(count) }
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Desglose financiero
        if (state.quote != null) {
            FinancialBreakdownCard(quote = state.quote)
        }

        // Error si lo hay
        if (state.errorMessage != null) {
            Spacer(modifier = Modifier.height(12.dp))
            KuotaCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = state.errorMessage,
                    color = KuotaColors.AlarmRed,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(14.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Nota legal
        Text(
            text = stringResource(R.string.purchase_legal_disclaimer),
            color = KuotaColors.Charcoal,
            fontSize = 11.sp,
            lineHeight = 15.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(20.dp))

        PillButton(
            text = stringResource(R.string.purchase_btn_submit),
            onClick = onSubmit,
            enabled = state.isEligible && state.amountCents > 0 && !state.isLoading,
            isLoading = state.isLoading,
            variant = PillButtonVariant.Primary,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun Step2WaitingContent(
    state: PurchaseUiState,
    onCancel: () -> Unit
) {
    val quote = state.quote
    val minutes = state.remainingSeconds / 60
    val seconds = state.remainingSeconds % 60
    val timeFormatted = String.format(Locale.US, "%02d:%02d", minutes, seconds)

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(KuotaColors.LimeVoltage),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.HourglassTop,
                contentDescription = null,
                tint = KuotaColors.ForestInk,
                modifier = Modifier.size(36.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(R.string.purchase_step2_title),
            color = KuotaColors.ForestInk,
            fontSize = 22.sp,
            fontWeight = FontWeight.Black,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = stringResource(R.string.purchase_step2_subtitle),
            color = KuotaColors.Charcoal,
            fontSize = 14.sp,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Tarjeta con monto de inicial a entregar
        KuotaCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.purchase_down_payment_label),
                    color = KuotaColors.Charcoal,
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                val downPaymentFormatted = String.format(Locale.US, "S/ %.2f", (quote?.downPaymentCents ?: 0L) / 100.0)
                Text(
                    text = downPaymentFormatted,
                    color = KuotaColors.ForestInk,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "A entregar a: ${state.supplierName}",
                    color = KuotaColors.Charcoal,
                    fontSize = 13.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Contador regresivo TTL 15 min
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(16.dp),
                color = KuotaColors.ForestInk,
                strokeWidth = 2.dp
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "${stringResource(R.string.purchase_ttl_countdown)} $timeFormatted",
                color = if (state.remainingSeconds < 120) KuotaColors.AlarmRed else KuotaColors.ForestInk,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = stringResource(R.string.purchase_waiting_provider),
            color = KuotaColors.Charcoal,
            fontSize = 12.sp
        )

        Spacer(modifier = Modifier.height(28.dp))

        PillButton(
            text = stringResource(R.string.purchase_btn_cancel),
            onClick = onCancel,
            variant = PillButtonVariant.Secondary,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun Step3ConfirmContent(
    state: PurchaseUiState,
    onConfirm: () -> Unit,
    onCancel: () -> Unit
) {
    val quote = state.quote

    Column {
        Text(
            text = stringResource(R.string.purchase_step3_title),
            color = KuotaColors.ForestInk,
            fontSize = 24.sp,
            fontWeight = FontWeight.Black
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = stringResource(R.string.purchase_step3_subtitle),
            color = KuotaColors.Charcoal,
            fontSize = 14.sp
        )

        Spacer(modifier = Modifier.height(20.dp))

        if (quote != null) {
            FinancialBreakdownCard(quote = quote)
            Spacer(modifier = Modifier.height(16.dp))
            InstallmentsScheduleCard(installments = quote.installments)
        }

        if (state.errorMessage != null) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = state.errorMessage,
                color = KuotaColors.AlarmRed,
                fontSize = 13.sp
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        PillButton(
            text = stringResource(R.string.purchase_btn_confirm),
            onClick = onConfirm,
            isLoading = state.isLoading,
            variant = PillButtonVariant.Primary,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        PillButton(
            text = stringResource(R.string.purchase_btn_cancel),
            onClick = onCancel,
            variant = PillButtonVariant.Secondary,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun StepSuccessContent(
    state: PurchaseUiState,
    onFinish: () -> Unit
) {
    val quote = state.quote
    val firstInstallment = quote?.installments?.firstOrNull()

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(KuotaColors.LimeVoltage),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = KuotaColors.ForestInk,
                modifier = Modifier.size(44.dp)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = stringResource(R.string.purchase_success_title),
            color = KuotaColors.ForestInk,
            fontSize = 24.sp,
            fontWeight = FontWeight.Black,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = stringResource(R.string.purchase_success_subtitle),
            color = KuotaColors.Charcoal,
            fontSize = 14.sp,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        KuotaCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Total financiado:",
                        color = KuotaColors.Charcoal,
                        fontSize = 14.sp
                    )
                    Text(
                        text = String.format(Locale.US, "S/ %.2f", (quote?.totalDebtCents ?: 0L) / 100.0),
                        color = KuotaColors.ForestInk,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = KuotaColors.Fog)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = stringResource(R.string.purchase_success_first_due),
                        color = KuotaColors.Charcoal,
                        fontSize = 14.sp
                    )
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = String.format(Locale.US, "S/ %.2f", (firstInstallment?.amountCents ?: 0L) / 100.0),
                            color = KuotaColors.ForestInk,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = firstInstallment?.dueDate?.take(10) ?: "",
                            color = KuotaColors.Charcoal,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        PillButton(
            text = stringResource(R.string.purchase_btn_finish),
            onClick = onFinish,
            variant = PillButtonVariant.Primary,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun FinancialBreakdownCard(quote: PurchaseQuote) {
    KuotaCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = stringResource(R.string.purchase_breakdown_title),
                color = KuotaColors.ForestInk,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            BreakdownRow(
                label = stringResource(R.string.purchase_down_payment_label),
                amountCents = quote.downPaymentCents,
                subtitle = "${quote.downPaymentPercent}% a pagar al proveedor hoy"
            )

            BreakdownRow(
                label = stringResource(R.string.purchase_financed_label),
                amountCents = quote.financedAmountCents,
                subtitle = "Cubierto por Kuota"
            )

            BreakdownRow(
                label = stringResource(R.string.purchase_fee_label),
                amountCents = quote.buyerFeeCents,
                subtitle = "5% fijo sobre financiado"
            )

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 10.dp),
                color = KuotaColors.Fog
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.purchase_total_debt_label),
                    color = KuotaColors.ForestInk,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = String.format(Locale.US, "S/ %.2f", quote.totalDebtCents / 100.0),
                    color = KuotaColors.ForestInk,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black
                )
            }
        }
    }
}

@Composable
private fun InstallmentsScheduleCard(installments: List<InstallmentScheduleItem>) {
    KuotaCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = stringResource(R.string.purchase_installments_schedule_title),
                color = KuotaColors.ForestInk,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(10.dp))

            installments.forEach { item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Cuota ${item.installmentNumber}",
                            color = KuotaColors.ForestInk,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = item.dueDate.take(10),
                            color = KuotaColors.Charcoal,
                            fontSize = 12.sp
                        )
                    }
                    Text(
                        text = String.format(Locale.US, "S/ %.2f", item.amountCents / 100.0),
                        color = KuotaColors.ForestInk,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun BreakdownRow(
    label: String,
    amountCents: Long,
    subtitle: String? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                color = KuotaColors.ForestInk,
                fontSize = 13.sp
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    color = KuotaColors.Charcoal,
                    fontSize = 11.sp
                )
            }
        }
        Text(
            text = String.format(Locale.US, "S/ %.2f", amountCents / 100.0),
            color = KuotaColors.ForestInk,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
