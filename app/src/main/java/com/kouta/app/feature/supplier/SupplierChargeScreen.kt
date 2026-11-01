package com.kouta.app.feature.supplier

import android.content.Intent
import android.net.Uri
import android.widget.Toast
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kouta.app.R
import com.kouta.app.di.AppEntryPoint
import com.kuota.core.ui.components.AmountInput
import com.kuota.core.ui.components.ChipPill
import com.kuota.core.ui.components.KuotaCard
import com.kuota.core.ui.components.PillButton
import com.kuota.core.ui.components.PillButtonVariant
import com.kuota.core.ui.theme.KuotaColors
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.launch
import java.util.Locale

@Composable
fun SupplierChargeScreen(
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
    val supplierRepository = entryPoint?.supplierRepository()

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Link WhatsApp, 1: Calculadora vs POS

    // Link WhatsApp form state
    var chargeAmountCents by remember { mutableLongStateOf(20_000L) } // S/ 200.00
    var clientPhone by remember { mutableStateOf("") }
    var clientDni by remember { mutableStateOf("") }
    var saleConcept by remember { mutableStateOf("") }
    var isGeneratingLink by remember { mutableStateOf(false) }
    var linkErrorMessage by remember { mutableStateOf<String?>(null) }

    // Calculadora vs POS state
    var monthlySalesCents by remember { mutableLongStateOf(1_000_000L) } // S/ 10,000.00
    var selectedPlan by remember { mutableStateOf("BASIC") } // "BASIC" (S/ 80+IGV) o "PRO" (S/ 120+IGV)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(KuotaColors.Fog)
            .padding(horizontal = 20.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(R.string.supplier_payments_title),
            color = KuotaColors.ForestInk,
            fontSize = 22.sp,
            fontWeight = FontWeight.Black
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = stringResource(R.string.supplier_payments_subtitle),
            color = KuotaColors.Charcoal,
            fontSize = 13.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Selector de Pestañas
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ChipPill(
                label = "Link de WhatsApp",
                isSelected = selectedTab == 0,
                onClick = { selectedTab = 0 }
            )
            ChipPill(
                label = "Calculadora vs POS",
                isSelected = selectedTab == 1,
                onClick = { selectedTab = 1 }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (selectedTab == 0) {
            // PESTAÑA 0: LINK DE COBRO POR WHATSAPP
            Text(
                text = stringResource(R.string.supplier_payments_amount_label),
                color = KuotaColors.ForestInk,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(8.dp))
            AmountInput(
                cents = chargeAmountCents,
                onCentsChange = { chargeAmountCents = it },
                maxCents = 1_500_000L,
                minCents = 19_900L,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Campo Celular
            OutlinedTextField(
                value = clientPhone,
                onValueChange = { clientPhone = it.filter { c -> c.isDigit() }.take(9) },
                label = { Text(stringResource(R.string.supplier_payments_phone_label)) },
                placeholder = { Text("987 654 321") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = KuotaColors.ForestInk,
                    focusedLabelColor = KuotaColors.ForestInk
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Campo DNI (opcional)
            OutlinedTextField(
                value = clientDni,
                onValueChange = { clientDni = it.filter { c -> c.isDigit() }.take(8) },
                label = { Text(stringResource(R.string.supplier_payments_dni_label)) },
                placeholder = { Text("8 dígitos") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = KuotaColors.ForestInk,
                    focusedLabelColor = KuotaColors.ForestInk
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Campo Concepto (opcional)
            OutlinedTextField(
                value = saleConcept,
                onValueChange = { saleConcept = it },
                label = { Text(stringResource(R.string.supplier_payments_concept_label)) },
                placeholder = { Text("Ej. 2 rollos de tela drill") },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = KuotaColors.ForestInk,
                    focusedLabelColor = KuotaColors.ForestInk
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Desglose transparente de comisión Kuota 2% y neto
            val feeCents = Math.round((chargeAmountCents * 200) / 10000.0)
            val netCents = chargeAmountCents - feeCents

            KuotaCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Liquidación transparente Kuota",
                        color = KuotaColors.ForestInk,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Monto a cobrar:", color = KuotaColors.Charcoal, fontSize = 13.sp)
                        Text(String.format(Locale.US, "S/ %.2f", chargeAmountCents / 100.0), color = KuotaColors.ForestInk, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(stringResource(R.string.supplier_payments_fee_label), color = KuotaColors.Charcoal, fontSize = 13.sp)
                        Text(String.format(Locale.US, "- S/ %.2f", feeCents / 100.0), color = KuotaColors.AlarmRed, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = KuotaColors.Fog)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(stringResource(R.string.supplier_payments_net_label), color = KuotaColors.ForestInk, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Text(String.format(Locale.US, "S/ %.2f", netCents / 100.0), color = KuotaColors.ForestInk, fontSize = 18.sp, fontWeight = FontWeight.Black)
                    }
                }
            }

            if (linkErrorMessage != null) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = linkErrorMessage!!,
                    color = KuotaColors.AlarmRed,
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            PillButton(
                text = stringResource(R.string.supplier_payments_btn_whatsapp),
                onClick = {
                    if (clientPhone.length < 9) {
                        linkErrorMessage = "Ingresa un número de celular de 9 dígitos."
                        return@PillButton
                    }
                    if (chargeAmountCents < 19_900L) {
                        linkErrorMessage = "El monto mínimo de cobro es S/ 199.00."
                        return@PillButton
                    }

                    linkErrorMessage = null
                    isGeneratingLink = true

                    scope.launch {
                        supplierRepository?.createPaymentLink(
                            amountCents = chargeAmountCents,
                            clientPhone = clientPhone,
                            clientDni = clientDni.takeIf { it.isNotBlank() },
                            concept = saleConcept.takeIf { it.isNotBlank() }
                        )?.onSuccess { paymentLink ->
                            isGeneratingLink = false
                            val waIntent = Intent(Intent.ACTION_VIEW, Uri.parse(paymentLink.whatsappUrl))
                            runCatching { context.startActivity(waIntent) }.onFailure {
                                Toast.makeText(context, "Link generado: ${paymentLink.shortUrl}", Toast.LENGTH_LONG).show()
                            }
                        }?.onFailure { error ->
                            isGeneratingLink = false
                            linkErrorMessage = error.message ?: "No se pudo generar el enlace de cobro."
                        }
                    }
                },
                isLoading = isGeneratingLink,
                variant = PillButtonVariant.Primary,
                modifier = Modifier.fillMaxWidth()
            )

        } else {
            // PESTAÑA 1: CALCULADORA DE AHORRO VS POS
            Text(
                text = stringResource(R.string.pos_calc_title),
                color = KuotaColors.ForestInk,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.pos_calc_subtitle),
                color = KuotaColors.Charcoal,
                fontSize = 13.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(R.string.pos_calc_sales_label),
                color = KuotaColors.ForestInk,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(8.dp))
            AmountInput(
                cents = monthlySalesCents,
                onCentsChange = { monthlySalesCents = it },
                maxCents = 10_000_000L,
                minCents = 100_000L,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Selector de plan
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ChipPill(
                    label = "Plan Basic (S/ 94.40/mes)",
                    isSelected = selectedPlan == "BASIC",
                    onClick = { selectedPlan = "BASIC" }
                )
                ChipPill(
                    label = "Plan Pro (S/ 141.60/mes)",
                    isSelected = selectedPlan == "PRO",
                    onClick = { selectedPlan = "PRO" }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Cálculo comparativo
            val posCostCents = Math.round((monthlySalesCents * 350) / 10000.0) // 3.5%
            val kuotaCommCents = Math.round((monthlySalesCents * 200) / 10000.0) // 2.0%
            val planCostCents = if (selectedPlan == "PRO") 14160L else 9440L
            val totalKuotaCostCents = kuotaCommCents + planCostCents
            val netSavingsCents = posCostCents - totalKuotaCostCents
            val isAboveThreshold = monthlySalesCents >= 630_000L // S/ 6,300

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
                        Text(stringResource(R.string.pos_calc_pos_cost), color = KuotaColors.Charcoal, fontSize = 13.sp)
                        Text(String.format(Locale.US, "S/ %.2f", posCostCents / 100.0), color = KuotaColors.AlarmRed, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(stringResource(R.string.pos_calc_kuota_cost), color = KuotaColors.Charcoal, fontSize = 13.sp)
                        Text(String.format(Locale.US, "S/ %.2f", totalKuotaCostCents / 100.0), color = KuotaColors.ForestInk, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = KuotaColors.Fog)

                    Text(
                        text = stringResource(R.string.pos_calc_savings_label),
                        color = KuotaColors.Charcoal,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = String.format(Locale.US, "S/ %.2f / mes", (if (netSavingsCents > 0) netSavingsCents else 0L) / 100.0),
                        color = KuotaColors.ForestInk,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            if (!isAboveThreshold) {
                Spacer(modifier = Modifier.height(14.dp))
                KuotaCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = KuotaColors.ForestInk,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = stringResource(R.string.pos_calc_threshold_warning),
                            color = KuotaColors.ForestInk,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}
