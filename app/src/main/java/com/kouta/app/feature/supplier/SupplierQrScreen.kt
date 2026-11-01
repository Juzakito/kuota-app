package com.kouta.app.feature.supplier

import android.content.Intent
import android.graphics.Bitmap
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kouta.app.R
import com.kouta.app.feature.financing.QrCodeToolkit
import com.kuota.core.ui.components.AmountInput
import com.kuota.core.ui.components.ChipPill
import com.kuota.core.ui.components.KuotaCard
import com.kuota.core.ui.components.PillButton
import com.kuota.core.ui.components.PillButtonVariant
import com.kuota.core.ui.theme.KuotaColors
import java.util.Locale

@Composable
fun SupplierQrScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SupplierQrViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(KuotaColors.Fog)
            .padding(horizontal = 20.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Encabezado
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.btn_back),
                    tint = KuotaColors.ForestInk
                )
            }
            Text(
                text = stringResource(R.string.supplier_qr_title),
                color = KuotaColors.ForestInk,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.width(48.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Selector de tipo de QR: Mostrador (estático) o Con Monto (dinámico)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ChipPill(
                label = stringResource(R.string.supplier_qr_tab_static),
                isSelected = state.selectedTab == 0,
                onClick = { viewModel.onTabSelected(0) }
            )
            ChipPill(
                label = stringResource(R.string.supplier_qr_tab_dynamic),
                isSelected = state.selectedTab == 1,
                onClick = { viewModel.onTabSelected(1) }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (state.selectedTab == 1) {
            // Sección monto dinámico
            Text(
                text = stringResource(R.string.supplier_payments_amount_label),
                color = KuotaColors.ForestInk,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(8.dp))
            AmountInput(
                cents = state.dynamicAmountCents,
                onCentsChange = viewModel::onAmountChanged,
                maxCents = 1_200_000L,
                minCents = 19_900L,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Tarjeta con el QR visual
        val activePayload = if (state.selectedTab == 0) state.staticQrPayload else state.dynamicQrPayload

        KuotaCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (state.selectedTab == 0) "QR de Mostrador" else "QR por S/ ${String.format(Locale.US, "%.2f", state.dynamicAmountCents / 100.0)}",
                    color = KuotaColors.ForestInk,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = if (state.selectedTab == 0) stringResource(R.string.supplier_qr_static_desc) else stringResource(R.string.supplier_qr_dynamic_desc),
                    color = KuotaColors.Charcoal,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(20.dp))

                if (state.isLoading) {
                    Box(
                        modifier = Modifier
                            .size(240.dp)
                            .background(KuotaColors.Paper, shape = CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = KuotaColors.ForestInk)
                    }
                } else if (activePayload != null) {
                    val qrBitmap = remember(activePayload) {
                        runCatching { QrCodeToolkit.generateQrBitmap(activePayload, 560) }.getOrNull()
                    }

                    if (qrBitmap != null) {
                        Image(
                            bitmap = qrBitmap.asImageBitmap(),
                            contentDescription = "Código QR Kuota",
                            modifier = Modifier
                                .size(240.dp)
                                .clip(RoundedCornerShape(com.kuota.core.ui.theme.KuotaRadii.Card))
                        )
                    } else {
                        Box(
                            modifier = Modifier.size(240.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.QrCode,
                                contentDescription = null,
                                tint = KuotaColors.Charcoal,
                                modifier = Modifier.size(64.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Válido para compras al crédito Kuota",
                    color = KuotaColors.Charcoal,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Botón Compartir
        PillButton(
            text = stringResource(R.string.supplier_qr_share),
            onClick = {
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, "Paga o compra al crédito en mi negocio con Kuota: ${activePayload ?: ""}")
                }
                context.startActivity(Intent.createChooser(shareIntent, "Compartir QR Kuota"))
            },
            variant = PillButtonVariant.Primary,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(32.dp))
    }
}
