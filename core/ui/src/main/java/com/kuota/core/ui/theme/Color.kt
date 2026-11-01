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
 * Tokens de color oficiales de Kuota App según DESIGN.md.
 * Regla dura: Lima nunca como texto sobre blanco. Cero azul en chips.
 */
object KuotaColors {
    // Brand Darks
    val ForestInk = Color(0xFF163300)       // Dominant brand dark
    val ForestInkDark = Color(0xFF0E1F00)   // Deep dark
    val Spruce = Color(0xFF054D28)          // Secondary dark green for cards & depth
    val Obsidian = Color(0xFF0E0F0C)        // Display headlines, high contrast

    // Brand Accents
    val LimeVoltage = Color(0xFF9FE870)     // Accent, functional punctuation, primary CTA
    val LimeVoltageHover = Color(0xFF8CD85E)
    val LinenMist = Color(0xFFE2F6D5)       // Pale green wash for soft highlight surfaces

    // Neutral Surfaces
    val Fog = Color(0xFFE8EBE6)             // Card surfaces, subtle panels
    val Paper = Color(0xFFFFFFFF)           // Canvas, pure white

    // Text & Line Tones
    val Charcoal = Color(0xFF454745)        // Primary text, soft warm black
    val Slate = Color(0xFF6A6C6A)           // Supporting text, helper labels
    val Pebble = Color(0xFF868685)          // Muted text, borders
    val AlarmRed = Color(0xFFCB272F)        // Error accent
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
    primary = KuotaColors.ForestInk,
    onPrimary = KuotaColors.LimeVoltage,
    primaryContainer = KuotaColors.LinenMist,
    onPrimaryContainer = KuotaColors.ForestInk,
    secondary = KuotaColors.Spruce,
    onSecondary = KuotaColors.Paper,
    secondaryContainer = KuotaColors.Fog,
    onSecondaryContainer = KuotaColors.Charcoal,
    tertiary = KuotaColors.LimeVoltage,
    onTertiary = KuotaColors.ForestInk,
    background = KuotaColors.Paper,
    onBackground = KuotaColors.Charcoal,
    surface = KuotaColors.Paper,
    onSurface = KuotaColors.Charcoal,
    surfaceVariant = KuotaColors.Fog,
    onSurfaceVariant = KuotaColors.Slate,
    outline = KuotaColors.Pebble,
    error = KuotaColors.AlarmRed,
    onError = KuotaColors.Paper
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
