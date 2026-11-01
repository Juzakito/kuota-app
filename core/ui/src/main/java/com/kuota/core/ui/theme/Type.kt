package com.kuota.core.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.unit.sp
import com.kuota.core.ui.R

/**
 * Tipografía oficial Kuota App basada en Inter según DESIGN.md.
 * - Inter regular / medium / semibold / bold para interfaz y lectura.
 * - Inter Black 900 con tracking -0.03em para display hero y cifras financieras.
 * Implementado vía Google Fonts Provider con fallback a SansSerif del sistema.
 */
private val googleFontProvider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs
)

private val interFont = GoogleFont("Inter")

val InterFontFamily = FontFamily(
    Font(googleFont = interFont, fontProvider = googleFontProvider, weight = FontWeight.Normal),
    Font(googleFont = interFont, fontProvider = googleFontProvider, weight = FontWeight.Medium),
    Font(googleFont = interFont, fontProvider = googleFontProvider, weight = FontWeight.SemiBold),
    Font(googleFont = interFont, fontProvider = googleFontProvider, weight = FontWeight.Bold),
    Font(googleFont = interFont, fontProvider = googleFontProvider, weight = FontWeight.Black)
)

val KuotaTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.Black, // 900
        fontSize = 48.sp,
        lineHeight = 44.sp,
        letterSpacing = (-1.5).sp,
        color = KuotaColors.Obsidian
    ),
    displayMedium = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.Black, // 900
        fontSize = 36.sp,
        lineHeight = 36.sp,
        letterSpacing = (-1.0).sp,
        color = KuotaColors.Obsidian
    ),
    displaySmall = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 32.sp,
        letterSpacing = (-0.5).sp,
        color = KuotaColors.Obsidian
    ),
    headlineLarge = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        lineHeight = 28.sp,
        color = KuotaColors.Obsidian
    ),
    headlineMedium = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 24.sp,
        color = KuotaColors.Obsidian
    ),
    headlineSmall = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 22.sp,
        color = KuotaColors.Obsidian
    ),
    titleLarge = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 20.sp,
        color = KuotaColors.Charcoal
    ),
    titleMedium = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 18.sp,
        color = KuotaColors.Charcoal
    ),
    bodyLarge = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        color = KuotaColors.Charcoal
    ),
    bodyMedium = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        color = KuotaColors.Charcoal
    ),
    bodySmall = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        color = KuotaColors.Slate
    ),
    labelLarge = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 16.sp,
        color = KuotaColors.ForestInk
    ),
    labelMedium = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 14.sp,
        color = KuotaColors.ForestInk
    ),
    labelSmall = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 10.sp,
        lineHeight = 12.sp,
        color = KuotaColors.Slate
    )
)
