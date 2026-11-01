package com.kuota.core.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.kuota.core.ui.theme.KuotaColors
import com.kuota.core.ui.theme.KuotaRadii

enum class KuotaCardVariant {
    Paper,      // White background, subtle Periwinkle border
    Fog,        // Lavender background, subtle panel
    Forest,     // Lavender background with Periwinkle accent border (Light First)
    Spruce,     // Lavender background with Persian Blue accent border
    Linen       // White background with Periwinkle border
}

/**
 * Tarjeta base oficial de Kuota App según paleta Light First.
 * - Radio por defecto: 10dp (cards estándar) o 28dp (largeCards hero).
 * - Cero sombras duras.
 */
@Composable
fun KuotaCard(
    modifier: Modifier = Modifier,
    variant: KuotaCardVariant = KuotaCardVariant.Paper,
    isLargeRadius: Boolean = false,
    contentPadding: Dp = 16.dp,
    content: @Composable BoxScope.() -> Unit
) {
    val shape = RoundedCornerShape(if (isLargeRadius) KuotaRadii.LargeCard else KuotaRadii.Card)

    val backgroundColor = when (variant) {
        KuotaCardVariant.Paper -> KuotaColors.Paper
        KuotaCardVariant.Fog -> KuotaColors.Fog
        KuotaCardVariant.Forest -> KuotaColors.Lavender
        KuotaCardVariant.Spruce -> KuotaColors.Lavender
        KuotaCardVariant.Linen -> KuotaColors.Paper
    }

    val contentColor = KuotaColors.PrussianBlue

    val border = when (variant) {
        KuotaCardVariant.Paper -> BorderStroke(1.dp, KuotaColors.Periwinkle.copy(alpha = 0.5f))
        KuotaCardVariant.Forest -> BorderStroke(1.5.dp, KuotaColors.Periwinkle)
        KuotaCardVariant.Spruce -> BorderStroke(1.5.dp, KuotaColors.PersianBlue.copy(alpha = 0.4f))
        KuotaCardVariant.Linen -> BorderStroke(1.dp, KuotaColors.Periwinkle)
        else -> null
    }

    Surface(
        modifier = modifier,
        shape = shape,
        color = backgroundColor,
        contentColor = contentColor,
        border = border,
        shadowElevation = 0.dp
    ) {
        Box(modifier = Modifier.padding(contentPadding)) {
            content()
        }
    }
}
