package com.kuota.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kuota.core.ui.theme.KuotaColors
import com.kuota.core.ui.theme.KuotaRadii

/**
 * Chip Pill oficial de Kuota App según DESIGN.md.
 * Regla dura: Cero azul en chips. Lima nunca como texto sobre blanco.
 * - Radio: 9999dp
 * - Seleccionado: Fondo Forest Ink (#163300) con texto Lime Voltage (#9FE870).
 * - No seleccionado: Fondo Fog (#E8EBE6) con texto Charcoal (#454745).
 */
@Composable
fun ChipPill(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: (@Composable () -> Unit)? = null
) {
    val backgroundColor = when {
        !enabled -> KuotaColors.Fog.copy(alpha = 0.5f)
        isSelected -> KuotaColors.ForestInk
        else -> KuotaColors.Fog
    }

    val textColor = when {
        !enabled -> KuotaColors.Pebble
        isSelected -> KuotaColors.LimeVoltage
        else -> KuotaColors.Charcoal
    }

    val shape = RoundedCornerShape(KuotaRadii.Pill)

    Box(
        modifier = modifier
            .defaultMinSize(minHeight = 36.dp)
            .clip(shape)
            .background(backgroundColor)
            .clickable(
                enabled = enabled,
                role = Role.Tab,
                onClick = onClick
            )
            .semantics {
                role = Role.Tab
                selected = isSelected
            }
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            leadingIcon?.let {
                it()
                Spacer(modifier = Modifier.width(6.dp))
            }
            Text(
                text = label,
                color = textColor,
                fontSize = 13.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
            )
        }
    }
}
