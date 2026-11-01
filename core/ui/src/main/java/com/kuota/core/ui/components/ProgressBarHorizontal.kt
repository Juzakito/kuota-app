package com.kuota.core.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.kuota.core.ui.theme.KuotaColors
import com.kuota.core.ui.theme.KuotaRadii

/**
 * Barra de progreso horizontal continua según DESIGN.md.
 * Utilizada en Onboarding (PRD §8.1), KYC y progreso de pago de cuotas.
 *
 * @param progress Progreso normalizado entre 0.0f y 1.0f
 * @param fillColor Color de la barra de progreso (por defecto Lime Voltage)
 * @param trackColor Color del riel de fondo (por defecto Linen Mist)
 * @param height Altura de la barra (por defecto 8dp)
 */
@Composable
fun ProgressBarHorizontal(
    progress: Float,
    modifier: Modifier = Modifier,
    fillColor: Color = KuotaColors.LimeVoltage,
    trackColor: Color = KuotaColors.LinenMist,
    height: Dp = 8.dp
) {
    val clampedProgress = progress.coerceIn(0f, 1f)
    val animatedProgress by animateFloatAsState(
        targetValue = clampedProgress,
        animationSpec = tween(durationMillis = 400),
        label = "ProgressBarHorizontalAnimation"
    )

    val shape = RoundedCornerShape(KuotaRadii.Pill)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(shape)
            .background(trackColor)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(animatedProgress)
                .fillMaxHeight()
                .clip(shape)
                .background(fillColor)
        )
    }
}
