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
    Paper,      // White background, subtle Fog border
    Fog,        // Fog background, subtle panel
    Forest,     // Forest Ink background (dark section card)
    Spruce,     // Spruce background (tonal depth)
    Linen       // Linen Mist background (highlight container)
}

/**
 * Tarjeta base oficial de Kuota App según DESIGN.md.
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
        KuotaCardVariant.Forest -> KuotaColors.ForestInk
        KuotaCardVariant.Spruce -> KuotaColors.Spruce
        KuotaCardVariant.Linen -> KuotaColors.LinenMist
    }

    val contentColor = when (variant) {
        KuotaCardVariant.Paper, KuotaCardVariant.Fog -> KuotaColors.Charcoal
        KuotaCardVariant.Forest, KuotaCardVariant.Spruce -> KuotaColors.Paper
        KuotaCardVariant.Linen -> KuotaColors.ForestInk
    }

    val border = when (variant) {
        KuotaCardVariant.Paper -> BorderStroke(1.dp, KuotaColors.Fog)
        KuotaCardVariant.Linen -> BorderStroke(1.dp, KuotaColors.Spruce.copy(alpha = 0.2f))
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
