package com.kuota.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kuota.core.ui.theme.KuotaColors
import com.kuota.core.ui.theme.KuotaRadii
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

/**
 * Campo de entrada gigante para montos financieros según DESIGN.md y AGENTS.md.
 * Regla dura: Dinero en céntimos (Long), moneda PEN. Cero Double/Float.
 *
 * @param cents Monto en céntimos (ej. 70000L = S/ 700.00)
 * @param onCentsChange Retorna el nuevo monto en céntimos (Long)
 * @param maxCents Límite superior permitido en céntimos
 * @param minCents Límite inferior permitido en céntimos (por defecto S/ 199.00 = 19900L)
 */
@Composable
fun AmountInput(
    cents: Long,
    onCentsChange: (Long) -> Unit,
    modifier: Modifier = Modifier,
    maxCents: Long = 70_000L,
    minCents: Long = 19_900L,
    enabled: Boolean = true
) {
    val formatter = remember {
        val symbols = DecimalFormatSymbols(Locale.forLanguageTag("es-PE")).apply {
            decimalSeparator = '.'
            groupingSeparator = ','
        }
        DecimalFormat("#,##0.00", symbols)
    }

    val solesValue = cents / 100.0 // Solo para formateo visual de texto
    val formattedDisplay = formatter.format(solesValue)
    val isOverLimit = cents > maxCents
    val isUnderMin = cents in 1 until minCents

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(KuotaColors.Paper, RoundedCornerShape(KuotaRadii.Card))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Monto de compra",
            color = KuotaColors.Slate,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "S/ ",
                color = if (isOverLimit) KuotaColors.AlarmRed else KuotaColors.ForestInk,
                fontSize = 36.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.SansSerif
            )

            BasicTextField(
                value = (cents / 100).toString().let { if (cents == 0L) "" else it },
                onValueChange = { input ->
                    val cleanDigits = input.filter { it.isDigit() }
                    val newCents = if (cleanDigits.isEmpty()) 0L else (cleanDigits.toLongOrNull() ?: 0L) * 100L
                    onCentsChange(newCents)
                },
                modifier = Modifier,
                enabled = enabled,
                textStyle = TextStyle(
                    color = if (isOverLimit) KuotaColors.AlarmRed else KuotaColors.ForestInk,
                    fontSize = 44.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.SansSerif,
                    textAlign = TextAlign.Start
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                cursorBrush = SolidColor(KuotaColors.ForestInk),
                decorationBox = { innerTextField ->
                    if (cents == 0L) {
                        Text(
                            text = "0",
                            color = KuotaColors.Pebble,
                            fontSize = 44.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.SansSerif
                        )
                    }
                    innerTextField()
                }
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        when {
            isOverLimit -> {
                Text(
                    text = "Límite superado (Máx. S/ ${maxCents / 100})",
                    color = KuotaColors.AlarmRed,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
            isUnderMin -> {
                Text(
                    text = "Monto mínimo: S/ ${minCents / 100}",
                    color = KuotaColors.Slate,
                    fontSize = 12.sp
                )
            }
            else -> {
                Text(
                    text = "Tope actual: S/ ${maxCents / 100}.00",
                    color = KuotaColors.Pebble,
                    fontSize = 11.sp
                )
            }
        }
    }
}
