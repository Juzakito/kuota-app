package com.kuota.core.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Tokens de color oficiales de Kuota App conforme a la paleta de 5 colores (Light First).
 * 1. White: #FFFEFF
 * 2. Lavender: #E6EDFD
 * 3. Periwinkle: #AFC5F6
 * 4. Persian Blue: #1A3BB0
 * 5. Prussian Blue: #0B2545
 */
object KuotaColors {
    // 5 Official Palette Colors from User (Light First)
    val White = Color(0xFFFFFEFF)          // Pure bright canvas #FFFEFF
    val Lavender = Color(0xFFE6EDFD)       // Light Ice Blue / Lavender surface #E6EDFD
    val Periwinkle = Color(0xFFAFC5F6)     // Soft vivid blue accent #AFC5F6
    val PersianBlue = Color(0xFF1A3BB0)    // Electric brand blue / Primary CTA #1A3BB0
    val PrussianBlue = Color(0xFF0B2545)   // Deep contrast navy for headlines & body #0B2545

    // Brand Darks / Primary Texts (Deep high contrast Prussian Blue)
    val ForestInk = PrussianBlue           // Dominant brand dark typography & icons #0B2545
    val ForestInkDark = PrussianBlue       // Deep contrast #0B2545
    val Spruce = PersianBlue               // Secondary brand blue #1A3BB0
    val Obsidian = PrussianBlue            // Display headlines, high contrast #0B2545

    // Brand Accents (Vivid Persian Blue & Soft Periwinkle)
    val LimeVoltage = PersianBlue          // Primary CTA action #1A3BB0
    val LimeVoltageHover = Color(0xFF152E8C)
    val LinenMist = Lavender               // Pale bright blue wash #E6EDFD

    // Neutral Surfaces (Predominantly light and bright)
    val Fog = Lavender                     // Card surfaces, subtle panels #E6EDFD
    val Paper = White                      // Canvas, crisp pure white #FFFEFF

    // Text & Line Tones
    val Charcoal = PrussianBlue            // Primary readable text (deep Prussian Blue)
    val Slate = Color(0xFF3B577D)          // Supporting text, helper labels
    val Pebble = Periwinkle                // Muted accent, subtle borders #AFC5F6
    val AlarmRed = Color(0xFFCB272F)       // Error accent
}

/**
 * Radios de curvatura de esquinas oficiales de DESIGN.md.
 */
object KuotaRadii {
    val Pill: Dp = 9999.dp                  // Buttons, tags, navSegments, chips
    val Card: Dp = 10.dp                    // Standard cards, inputs
    val LargeCard: Dp = 28.dp               // Large hero cards
}

@Immutable
data class KuotaCustomColors(
    val forestInk: Color = KuotaColors.ForestInk,
    val limeVoltage: Color = KuotaColors.LimeVoltage,
    val spruce: Color = KuotaColors.Spruce,
    val linenMist: Color = KuotaColors.LinenMist,
    val fog: Color = KuotaColors.Fog,
    val paper: Color = KuotaColors.Paper,
    val charcoal: Color = KuotaColors.Charcoal,
    val obsidian: Color = KuotaColors.Obsidian,
    val pebble: Color = KuotaColors.Pebble,
    val slate: Color = KuotaColors.Slate,
    val alarmRed: Color = KuotaColors.AlarmRed,
)

val LocalKuotaColors = staticCompositionLocalOf { KuotaCustomColors() }

private val KuotaLightColorScheme: ColorScheme = lightColorScheme(
    primary = KuotaColors.PersianBlue,
    onPrimary = KuotaColors.White,
    primaryContainer = KuotaColors.Lavender,
    onPrimaryContainer = KuotaColors.PrussianBlue,
    secondary = KuotaColors.Periwinkle,
    onSecondary = KuotaColors.PrussianBlue,
    secondaryContainer = KuotaColors.Lavender,
    onSecondaryContainer = KuotaColors.PrussianBlue,
    tertiary = KuotaColors.PersianBlue,
    onTertiary = KuotaColors.White,
    background = KuotaColors.White,
    onBackground = KuotaColors.PrussianBlue,
    surface = KuotaColors.White,
    onSurface = KuotaColors.PrussianBlue,
    surfaceVariant = KuotaColors.Lavender,
    onSurfaceVariant = KuotaColors.Slate,
    outline = KuotaColors.Periwinkle,
    error = KuotaColors.AlarmRed,
    onError = KuotaColors.White
)

@Composable
fun KuotaTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = KuotaLightColorScheme,
        typography = KuotaTypography,
        content = content
    )
}

object KuotaThemeTokens {
    val colors: KuotaCustomColors
        @Composable
        @ReadOnlyComposable
        get() = LocalKuotaColors.current
}
