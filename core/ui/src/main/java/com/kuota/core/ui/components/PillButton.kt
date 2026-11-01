package com.kuota.core.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kuota.core.ui.theme.KuotaColors
import com.kuota.core.ui.theme.KuotaRadii

enum class PillButtonVariant {
    Primary,    // Lime Voltage fill + Forest Ink text (DESIGN.md)
    Secondary,  // Paper fill + 1.5dp Forest Ink border + Forest Ink text
    Tertiary    // Text only button with Forest Ink text
}

/**
 * Botón pill oficial de Kuota App conforme a DESIGN.md.
 * - Radio: 9999dp
 * - Altura táctil mínima: 48dp (accesibilidad WCAG 2.1)
 * - Primario: Relleno Lime Voltage (#9FE870) con texto Forest Ink (#163300).
 * - Secundario: Relleno Paper (#FFFFFF) con borde Forest Ink (#163300).
 */
@Composable
fun PillButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: PillButtonVariant = PillButtonVariant.Primary,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    leadingIcon: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null
) {
    val shape = RoundedCornerShape(KuotaRadii.Pill)
    val contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)

    when (variant) {
        PillButtonVariant.Primary -> {
            Button(
                onClick = onClick,
                modifier = modifier
                    .defaultMinSize(minHeight = 48.dp)
                    .semantics { role = Role.Button },
                enabled = enabled && !isLoading,
                shape = shape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = KuotaColors.LimeVoltage,
                    contentColor = KuotaColors.White,
                    disabledContainerColor = KuotaColors.Fog,
                    disabledContentColor = KuotaColors.Pebble
                ),
                contentPadding = contentPadding,
                elevation = null
            ) {
                ButtonContent(
                    text = text,
                    textColor = if (enabled) KuotaColors.White else KuotaColors.Pebble,
                    isLoading = isLoading,
                    loadingColor = KuotaColors.White,
                    leadingIcon = leadingIcon,
                    trailingIcon = trailingIcon
                )
            }
        }
        PillButtonVariant.Secondary -> {
            OutlinedButton(
                onClick = onClick,
                modifier = modifier
                    .defaultMinSize(minHeight = 48.dp)
                    .semantics { role = Role.Button },
                enabled = enabled && !isLoading,
                shape = shape,
                border = BorderStroke(
                    width = 1.5.dp,
                    color = if (enabled) KuotaColors.PersianBlue else KuotaColors.Pebble
                ),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = KuotaColors.Paper,
                    contentColor = KuotaColors.PersianBlue,
                    disabledContentColor = KuotaColors.Pebble
                ),
                contentPadding = contentPadding
            ) {
                ButtonContent(
                    text = text,
                    textColor = if (enabled) KuotaColors.PersianBlue else KuotaColors.Pebble,
                    isLoading = isLoading,
                    loadingColor = KuotaColors.PersianBlue,
                    leadingIcon = leadingIcon,
                    trailingIcon = trailingIcon
                )
            }
        }
        PillButtonVariant.Tertiary -> {
            TextButton(
                onClick = onClick,
                modifier = modifier
                    .defaultMinSize(minHeight = 48.dp)
                    .semantics { role = Role.Button },
                enabled = enabled && !isLoading,
                shape = shape,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = KuotaColors.PersianBlue,
                    disabledContentColor = KuotaColors.Pebble
                ),
                contentPadding = contentPadding
            ) {
                ButtonContent(
                    text = text,
                    textColor = if (enabled) KuotaColors.PersianBlue else KuotaColors.Pebble,
                    isLoading = isLoading,
                    loadingColor = KuotaColors.PersianBlue,
                    leadingIcon = leadingIcon,
                    trailingIcon = trailingIcon
                )
            }
        }
    }
}

@Composable
private fun ButtonContent(
    text: String,
    textColor: Color,
    isLoading: Boolean,
    loadingColor: Color,
    leadingIcon: (@Composable () -> Unit)?,
    trailingIcon: (@Composable () -> Unit)?
) {
    if (isLoading) {
        CircularProgressIndicator(
            modifier = Modifier.size(20.dp),
            strokeWidth = 2.dp,
            color = loadingColor
        )
    } else {
        Row(verticalAlignment = Alignment.CenterVertically) {
            leadingIcon?.let {
                it()
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = text,
                color = textColor,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
            trailingIcon?.let {
                Spacer(modifier = Modifier.width(8.dp))
                it()
            }
        }
    }
}
