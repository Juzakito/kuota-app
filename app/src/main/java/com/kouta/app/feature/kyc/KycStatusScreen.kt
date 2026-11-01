package com.kouta.app.feature.kyc

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kouta.app.R
import com.kuota.core.model.KycStatus
import com.kuota.core.model.UserSummary
import com.kuota.core.ui.components.KuotaCard
import com.kuota.core.ui.components.KuotaCardVariant
import com.kuota.core.ui.components.PillButton
import com.kuota.core.ui.components.PillButtonVariant
import com.kuota.core.ui.theme.KuotaColors
import com.kuota.core.ui.theme.KuotaTypography

@Composable
fun KycStatusScreen(
    status: KycStatus,
    summary: UserSummary?,
    onNavigateToHome: () -> Unit,
    onRetryKyc: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(KuotaColors.White)
            .padding(horizontal = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(top = 48.dp, bottom = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            when (status) {
                KycStatus.PENDING -> {
                    // Ícono circular de espera
                    Box(
                        modifier = Modifier
                            .size(96.dp)
                            .clip(CircleShape)
                            .background(KuotaColors.Lavender),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(64.dp),
                            color = KuotaColors.PersianBlue,
                            strokeWidth = 4.dp
                        )
                        Icon(
                            imageVector = Icons.Default.AccessTime,
                            contentDescription = null,
                            tint = KuotaColors.PersianBlue,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(28.dp))

                    Text(
                        text = stringResource(R.string.kyc_status_pending_title),
                        style = KuotaTypography.headlineMedium,
                        color = KuotaColors.PrussianBlue,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = stringResource(R.string.kyc_status_pending_subtitle),
                        style = KuotaTypography.bodyMedium,
                        color = KuotaColors.Charcoal,
                        textAlign = TextAlign.Center,
                        lineHeight = 22.sp
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    KuotaCard(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            Text(
                                text = "Estado de tu cuenta",
                                fontWeight = FontWeight.Bold,
                                color = KuotaColors.PrussianBlue,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "Validación RENIEC:", color = KuotaColors.Charcoal, fontSize = 13.sp)
                                Text(text = "En revisión", color = KuotaColors.PersianBlue, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "Tiempo estimado:", color = KuotaColors.Charcoal, fontSize = 13.sp)
                                Text(text = "< 15 minutos", color = KuotaColors.PersianBlue, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = stringResource(R.string.onboarding_legal_disclaimer),
                        style = KuotaTypography.bodySmall,
                        color = KuotaColors.Charcoal.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center
                    )
                }

                KycStatus.APPROVED -> {
                    // Badge hero de aprobación
                    Box(
                        modifier = Modifier
                            .size(96.dp)
                            .clip(CircleShape)
                            .background(KuotaColors.PersianBlue),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = KuotaColors.White,
                            modifier = Modifier.size(56.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(28.dp))

                    Text(
                        text = stringResource(R.string.kyc_status_approved_title),
                        style = KuotaTypography.headlineMedium,
                        color = KuotaColors.PrussianBlue,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = stringResource(R.string.kyc_status_approved_subtitle),
                        style = KuotaTypography.bodyMedium,
                        color = KuotaColors.Charcoal,
                        textAlign = TextAlign.Center,
                        lineHeight = 22.sp
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    // Tarjeta de Línea de Crédito Otorgada
                    KuotaCard(
                        modifier = Modifier.fillMaxWidth(),
                        variant = KuotaCardVariant.Forest
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "LÍNEA DISPONIBLE INICIAL",
                                color = KuotaColors.PersianBlue,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "S/ 1,400.00",
                                color = KuotaColors.PrussianBlue,
                                style = KuotaTypography.displayLarge
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Nivel 1 • Pago semanal flexible",
                                color = KuotaColors.Slate,
                                fontSize = 13.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(36.dp))

                    PillButton(
                        text = stringResource(R.string.kyc_status_approved_cta),
                        onClick = onNavigateToHome,
                        variant = PillButtonVariant.Primary,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                KycStatus.REJECTED -> {
                    Box(
                        modifier = Modifier
                            .size(96.dp)
                            .clip(CircleShape)
                            .background(KuotaColors.AlarmRed.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ErrorOutline,
                            contentDescription = null,
                            tint = KuotaColors.AlarmRed,
                            modifier = Modifier.size(56.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(28.dp))

                    Text(
                        text = stringResource(R.string.kyc_status_rejected_title),
                        style = KuotaTypography.headlineMedium,
                        color = KuotaColors.ForestInk,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = stringResource(R.string.kyc_status_rejected_subtitle),
                        style = KuotaTypography.bodyMedium,
                        color = KuotaColors.Charcoal,
                        textAlign = TextAlign.Center,
                        lineHeight = 22.sp
                    )

                    Spacer(modifier = Modifier.height(36.dp))

                    PillButton(
                        text = stringResource(R.string.kyc_status_rejected_cta),
                        onClick = onRetryKyc,
                        variant = PillButtonVariant.Primary,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                KycStatus.UNSUBMITTED -> {
                    Text(
                        text = "Por favor completa tu verificación",
                        style = KuotaTypography.headlineMedium,
                        color = KuotaColors.ForestInk
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    PillButton(
                        text = "Iniciar verificación",
                        onClick = onRetryKyc,
                        variant = PillButtonVariant.Primary,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}
