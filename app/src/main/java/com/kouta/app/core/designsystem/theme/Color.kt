package com.kouta.app.core.designsystem.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// ---------------------------------------------------------------------------
// Paleta Oficial de 5 Colores Kuota (Light First)
// ---------------------------------------------------------------------------

val KuotaPaletteWhite = Color(0xFFFFFEFF)          // Pure bright canvas #FFFEFF
val KuotaPaletteLavender = Color(0xFFE6EDFD)       // Soft Ice Blue / Lavender surface #E6EDFD
val KuotaPalettePeriwinkle = Color(0xFFAFC5F6)     // Soft vivid blue accent #AFC5F6
val KuotaPalettePersianBlue = Color(0xFF1A3BB0)    // Electric brand blue / Primary CTA #1A3BB0
val KuotaPalettePrussianBlue = Color(0xFF0B2545)   // Deep contrast navy for headlines & body #0B2545

// ---------------------------------------------------------------------------
// Primitivas de marca (Mapeadas a los 5 colores)
// ---------------------------------------------------------------------------

val KuotaBlack = KuotaPalettePrussianBlue
val KuotaNavy900 = KuotaPalettePrussianBlue
val KuotaNavy700 = KuotaPalettePrussianBlue
val KuotaCobalt600 = KuotaPalettePersianBlue
val KuotaAction500 = KuotaPalettePersianBlue
val KuotaCian300 = KuotaPalettePeriwinkle
val KuotaSky300 = KuotaPalettePeriwinkle

val KuotaBgLight = KuotaPaletteWhite
val KuotaSurfaceLight = KuotaPaletteWhite
val KuotaSurface2Light = KuotaPaletteLavender
val KuotaTextLight = KuotaPalettePrussianBlue
val KuotaTextMutedLight = Color(0xFF3B577D)

val KuotaSuccess = Color(0xFF12B76A)
val KuotaSuccessContainer = Color(0xFFD1FAE5)
val KuotaWarning = Color(0xFFF5A524)
val KuotaWarningContainer = Color(0xFFFEF3C7)
val KuotaDanger = Color(0xFFE5484D)
val KuotaDangerContainer = Color(0xFFFEE2E2)

// Modo Oscuro redirigido a Light First (No dark mode permitido)
val KuotaBgDark = KuotaPaletteWhite
val KuotaSurfaceDark = KuotaPaletteWhite
val KuotaSurface2Dark = KuotaPaletteLavender
val KuotaTextDark = KuotaPalettePrussianBlue
val KuotaTextMutedDark = Color(0xFF3B577D)

// Degradados oficiales Light First
val KuotaHeroGradient = listOf(KuotaPaletteLavender, KuotaPaletteWhite)
val KuotaVoltageGradient = listOf(KuotaPalettePersianBlue, KuotaPalettePeriwinkle)

val KuotaRoyalBlue = KuotaPalettePersianBlue
val KuotaRoyalBlueDark = KuotaPalettePrussianBlue
val KuotaAtmosphericDeep = KuotaPaletteLavender
val KuotaAtmosphericMid = KuotaPaletteWhite
val KuotaAtmosphericLight = KuotaPaletteWhite
val KuotaAtmosphericGlow = KuotaPalettePeriwinkle

val KuotaSurfaceIce = KuotaPaletteLavender
val KuotaEmerald = KuotaSuccess
val KuotaEmeraldContainer = KuotaSuccessContainer
val KuotaCoral = KuotaDanger
val KuotaCoralContainer = KuotaDangerContainer
val KuotaAmber = KuotaWarning
val KuotaAmberContainer = KuotaWarningContainer

val KuotaBlue = KuotaPalettePersianBlue
val KuotaSky = KuotaPalettePeriwinkle
val KuotaTurquoise = KuotaPalettePeriwinkle
val KuotaWhite = KuotaPaletteWhite
val KuotaInk = KuotaPalettePrussianBlue
val KuotaBlueStrong = KuotaPalettePersianBlue

val KuotaBrandGradient = listOf(KuotaPalettePersianBlue, KuotaPalettePeriwinkle)

// ---------------------------------------------------------------------------
// Esquemas Material 3 (100% Light First)
// ---------------------------------------------------------------------------

internal val KuotaLightColorScheme = lightColorScheme(
    primary = KuotaPalettePersianBlue,
    onPrimary = KuotaPaletteWhite,
    primaryContainer = KuotaPaletteLavender,
    onPrimaryContainer = KuotaPalettePrussianBlue,
    inversePrimary = KuotaPalettePeriwinkle,
    secondary = KuotaPalettePersianBlue,
    onSecondary = KuotaPaletteWhite,
    secondaryContainer = KuotaPaletteLavender,
    onSecondaryContainer = KuotaPalettePrussianBlue,
    tertiary = KuotaPalettePeriwinkle,
    onTertiary = KuotaPalettePrussianBlue,
    tertiaryContainer = KuotaPaletteLavender,
    onTertiaryContainer = KuotaPalettePrussianBlue,
    background = KuotaPaletteWhite,
    onBackground = KuotaPalettePrussianBlue,
    surface = KuotaPaletteWhite,
    onSurface = KuotaPalettePrussianBlue,
    surfaceVariant = KuotaPaletteLavender,
    onSurfaceVariant = KuotaPalettePrussianBlue.copy(alpha = 0.75f),
    surfaceTint = KuotaPalettePersianBlue,
    inverseSurface = KuotaPalettePrussianBlue,
    inverseOnSurface = KuotaPaletteWhite,
    error = Color(0xFFBA1A1A),
    onError = KuotaPaletteWhite,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
    outline = KuotaPalettePeriwinkle,
    outlineVariant = KuotaPaletteLavender,
    scrim = Color(0xFF000000),
    surfaceBright = KuotaPaletteWhite,
    surfaceDim = KuotaPaletteLavender,
    surfaceContainerLowest = KuotaPaletteWhite,
    surfaceContainerLow = KuotaPaletteWhite,
    surfaceContainer = KuotaPaletteLavender,
    surfaceContainerHigh = KuotaPaletteLavender,
    surfaceContainerHighest = KuotaPalettePeriwinkle
)

// DarkColorScheme redirige a Light para garantizar Light First total
internal val KuotaDarkColorScheme = KuotaLightColorScheme

// ---------------------------------------------------------------------------
// Tokens extendidos
// ---------------------------------------------------------------------------

@Immutable
data class KuotaExtendedColors(
    val brandGradient: List<Color>,
    val onBrand: Color,
    val headerGradient: List<Color>,
    val headerContent: Color,
    val headerGlass: Color,
    val suggestionGradient: List<Color>,
    val suggestionContent: Color,
    val suggestionAccent: Color,
    val mediaBackdrop: Color,
    val avatarTint: Color,
    val warningAccent: Color,
    val warningContainer: Color,
    val warningContent: Color,
    val success: Color,
    val successContainer: Color,
    val info: Color,
    val infoContainer: Color,
    val neutral: Color,
    val accentTeal: Color = KuotaPalettePeriwinkle
)

internal val KuotaLightExtendedColors = KuotaExtendedColors(
    brandGradient = KuotaBrandGradient,
    onBrand = KuotaPaletteWhite,
    headerGradient = listOf(KuotaPaletteLavender, KuotaPaletteWhite),
    headerContent = KuotaPalettePrussianBlue,
    headerGlass = KuotaPaletteWhite.copy(alpha = 0.85f),
    suggestionGradient = listOf(KuotaPaletteLavender, KuotaPaletteWhite),
    suggestionContent = KuotaPalettePrussianBlue,
    suggestionAccent = KuotaPalettePersianBlue,
    mediaBackdrop = KuotaPaletteLavender,
    avatarTint = KuotaPalettePersianBlue,
    warningAccent = KuotaWarning,
    warningContainer = KuotaWarningContainer,
    warningContent = Color(0xFF92400E),
    success = KuotaSuccess,
    successContainer = KuotaSuccessContainer,
    info = KuotaPalettePersianBlue,
    infoContainer = KuotaPaletteLavender,
    neutral = KuotaPalettePeriwinkle
)

internal val KuotaDarkExtendedColors = KuotaLightExtendedColors

val LocalKuotaExtendedColors = staticCompositionLocalOf { KuotaLightExtendedColors }
