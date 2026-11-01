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
 * Chip Pill oficial de Kuota App (Light First).
 * - Radio: 9999dp
 * - Seleccionado: Fondo Persian Blue (#1A3BB0) con texto White (#FFFEFF).
 * - No seleccionado: Fondo Lavender (#E6EDFD) con texto Prussian Blue (#0B2545).
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
        !enabled -> KuotaColors.Lavender.copy(alpha = 0.5f)
        isSelected -> KuotaColors.PersianBlue
        else -> KuotaColors.Lavender
    }

    val textColor = when {
        !enabled -> KuotaColors.Periwinkle
        isSelected -> KuotaColors.White
        else -> KuotaColors.PrussianBlue
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
