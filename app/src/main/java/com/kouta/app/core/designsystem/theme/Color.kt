package com.kouta.app.core.designsystem.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// ---------------------------------------------------------------------------
// Primitivas de marca. Cambiar aquí actualiza todo el sistema.
// ---------------------------------------------------------------------------

/** Negro del logo Kuota: base del modo oscuro. */
val KuotaBlack = Color(0xFF05070A)

/** Azul de marca. */
val KuotaBlue = Color(0xFF5B8FF9)

/** Celeste de marca. */
val KuotaSky = Color(0xFFA8DFFF)

/** Turquesa de marca. */
val KuotaTurquoise = Color(0xFF55DDE0)

val KuotaWhite = Color(0xFFFFFFFF)

/** Tinta sobre superficies de marca (azul, celeste o turquesa). */
val KuotaInk = Color(0xFF04101F)

/** Variante accesible del azul para texto sobre fondos claros (contraste >= 5.5:1). */
val KuotaBlueStrong = Color(0xFF2D5FCC)

/** Degradado de identidad: azul -> celeste -> turquesa. */
val KuotaBrandGradient = listOf(KuotaBlue, KuotaSky, KuotaTurquoise)

// ---------------------------------------------------------------------------
// Esquemas Material 3
// ---------------------------------------------------------------------------

internal val KuotaLightColorScheme = lightColorScheme(
    primary = KuotaBlueStrong,
    onPrimary = KuotaWhite,
    primaryContainer = Color(0xFFD9E6FF),
    onPrimaryContainer = Color(0xFF0B2A5B),
    inversePrimary = KuotaSky,
    secondary = Color(0xFF0E7490),
    onSecondary = KuotaWhite,
    secondaryContainer = Color(0xFFD8F3FA),
    onSecondaryContainer = Color(0xFF06333B),
    tertiary = Color(0xFF0B7C7F),
    onTertiary = KuotaWhite,
    tertiaryContainer = Color(0xFFBFF7F5),
    onTertiaryContainer = Color(0xFF00201F),
    background = Color(0xFFF7FAFF),
    onBackground = Color(0xFF0A1017),
    surface = KuotaWhite,
    onSurface = Color(0xFF0A1017),
    surfaceVariant = Color(0xFFE3EAF4),
    onSurfaceVariant = Color(0xFF46505F),
    surfaceTint = KuotaBlueStrong,
    inverseSurface = Color(0xFF1E2733),
    inverseOnSurface = Color(0xFFF1F5FB),
    error = Color(0xFFBA1A1A),
    onError = KuotaWhite,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
    outline = Color(0xFF77828F),
    outlineVariant = Color(0xFFD5DDE8),
    scrim = Color(0xFF000000),
    surfaceBright = KuotaWhite,
    surfaceDim = Color(0xFFD7DFEA),
    surfaceContainerLowest = KuotaWhite,
    surfaceContainerLow = Color(0xFFF2F6FC),
    surfaceContainer = Color(0xFFECF2FA),
    surfaceContainerHigh = Color(0xFFE6EDF7),
    surfaceContainerHighest = Color(0xFFDFE8F4)
)

internal val KuotaDarkColorScheme = darkColorScheme(
    primary = KuotaBlue,
    onPrimary = KuotaInk,
    primaryContainer = Color(0xFF123A73),
    onPrimaryContainer = Color(0xFFD7E5FF),
    inversePrimary = KuotaBlueStrong,
    secondary = KuotaSky,
    onSecondary = Color(0xFF052033),
    secondaryContainer = Color(0xFF123246),
    onSecondaryContainer = Color(0xFFCFEBFF),
    tertiary = KuotaTurquoise,
    onTertiary = Color(0xFF00201F),
    tertiaryContainer = Color(0xFF0B3B3D),
    onTertiaryContainer = Color(0xFFB7FAF8),
    background = KuotaBlack,
    onBackground = Color(0xFFE9EFF7),
    surface = Color(0xFF0B0F15),
    onSurface = Color(0xFFE9EFF7),
    surfaceVariant = Color(0xFF1B2430),
    onSurfaceVariant = Color(0xFFB8C4D4),
    surfaceTint = KuotaBlue,
    inverseSurface = Color(0xFFE9EFF7),
    inverseOnSurface = Color(0xFF1B2430),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    outline = Color(0xFF3D4A5C),
    outlineVariant = Color(0xFF26313F),
    scrim = Color(0xFF000000),
    surfaceBright = Color(0xFF26313F),
    surfaceDim = KuotaBlack,
    surfaceContainerLowest = KuotaBlack,
    surfaceContainerLow = Color(0xFF090D13),
    surfaceContainer = Color(0xFF10161F),
    surfaceContainerHigh = Color(0xFF151D27),
    surfaceContainerHighest = Color(0xFF1B2430)
)

// ---------------------------------------------------------------------------
// Tokens extendidos: identidad, avisos y estados fuera de los roles M3.
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
    val neutral: Color
)

internal val KuotaLightExtendedColors = KuotaExtendedColors(
    brandGradient = KuotaBrandGradient,
    onBrand = KuotaInk,
    headerGradient = listOf(Color(0xFFEAF3FF), Color(0xFFE1F7F8)),
    headerContent = Color(0xFF071426),
    headerGlass = KuotaWhite.copy(alpha = .65f),
    suggestionGradient = listOf(Color(0xFFE6F0FF), Color(0xFFDDF7F3)),
    suggestionContent = Color(0xFF071426),
    suggestionAccent = KuotaBlueStrong,
    mediaBackdrop = Color(0xFFE8F1FF),
    avatarTint = KuotaBlueStrong,
    warningAccent = Color(0xFFB45309),
    warningContainer = Color(0xFFFFF4E5),
    warningContent = Color(0xFF7C2D12),
    success = Color(0xFF166534),
    successContainer = Color(0xFFDCFCE7),
    info = Color(0xFF1D4ED8),
    infoContainer = Color(0xFFE1EAFE),
    neutral = Color(0xFF4B5563)
)

internal val KuotaDarkExtendedColors = KuotaExtendedColors(
    brandGradient = KuotaBrandGradient,
    onBrand = KuotaInk,
    headerGradient = listOf(Color(0xFF0E1C33), KuotaBlack),
    headerContent = Color(0xFFF2F7FF),
    headerGlass = KuotaWhite.copy(alpha = .10f),
    suggestionGradient = listOf(Color(0xFF0E1C33), Color(0xFF092227)),
    suggestionContent = Color(0xFFEAF4FF),
    suggestionAccent = KuotaSky,
    mediaBackdrop = Color(0xFF0E1723),
    avatarTint = KuotaSky,
    warningAccent = Color(0xFFF0B46C),
    warningContainer = Color(0xFF36281A),
    warningContent = Color(0xFFFFD7A3),
    success = Color(0xFF6EE7A8),
    successContainer = Color(0xFF0D2E1F),
    info = Color(0xFF9CC0FF),
    infoContainer = Color(0xFF122A52),
    neutral = Color(0xFFA3ADBB)
)

val LocalKuotaExtendedColors = staticCompositionLocalOf { KuotaLightExtendedColors }
