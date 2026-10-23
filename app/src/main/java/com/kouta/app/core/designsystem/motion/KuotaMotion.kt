package com.kouta.app.core.designsystem.motion

import android.content.Context
import android.provider.Settings
import android.view.HapticFeedbackConstants
import android.view.View
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/**
 * Sistema centralizado de animaciones y microinteracciones nativas para KUOTA.
 *
 * Diseñado específicamente para una experiencia financiera sobria, fluida y confiable:
 * - Microinteracciones (bordes de foco, teclas PIN, botones): 150–220 ms
 * - Transiciones entre pasos del flujo: 200–260 ms
 * - Respeta la preferencia de accesibilidad del sistema (`ANIMATOR_DURATION_SCALE`).
 */
object KuotaMotion {
    const val MICRO_DURATION_MS = 160
    const val STATE_DURATION_MS = 200
    const val SCREEN_ENTER_DURATION_MS = 240
    const val SCREEN_EXIT_DURATION_MS = 170
    const val ENTRANCE_DURATION_MS = 230

    val StandardEasing = FastOutSlowInEasing
    val DecelerateEasing = LinearOutSlowInEasing
    val AccelerateEasing = FastOutLinearInEasing
    val CrispOutEasing = CubicBezierEasing(0.22f, 1.0f, 0.36f, 1.0f)

    /**
     * Calcula la duración efectiva aplicando la escala de animación del sistema.
     * Si el usuario activó "Quitar animaciones" (escala <= 0f), devuelve 0 ms.
     */
    fun resolveDurationMs(baseDurationMs: Int, animatorScale: Float): Int {
        if (animatorScale <= 0f || baseDurationMs <= 0) return 0
        return (baseDurationMs * animatorScale.coerceIn(0.5f, 1.5f)).roundToInt()
    }

    /**
     * Comprueba si el dispositivo tiene desactivadas las animaciones por accesibilidad.
     */
    fun getSystemAnimatorScale(context: Context): Float {
        return runCatching {
            Settings.Global.getFloat(
                context.contentResolver,
                Settings.Global.ANIMATOR_DURATION_SCALE,
                1.0f
            )
        }.getOrDefault(1.0f)
    }

    /**
     * Respuesta háptica ligera para pulsaciones de teclado numérico o foco.
     */
    fun performKeyTapHaptic(view: View) {
        runCatching {
            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
        }
    }

    /**
     * Respuesta háptica discreta para acciones primarias ("Comenzar", "Continuar", "Finalizar").
     */
    fun performActionHaptic(view: View) {
        runCatching {
            view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
        }
    }

    /**
     * Transición direccional horizontal + opacidad para avanzar o retroceder entre pasos.
     */
    fun stepTransitionSpec(
        isForward: Boolean,
        animatorScale: Float
    ): ContentTransform {
        val enterMs = resolveDurationMs(SCREEN_ENTER_DURATION_MS, animatorScale)
        val exitMs = resolveDurationMs(SCREEN_EXIT_DURATION_MS, animatorScale)

        if (enterMs == 0) {
            return fadeIn(tween(0)) togetherWith fadeOut(tween(0))
        }

        val direction = if (isForward) 1 else -1
        val enter = slideInHorizontally(
            animationSpec = tween(durationMillis = enterMs, easing = CrispOutEasing),
            initialOffsetX = { fullWidth -> (fullWidth / 8) * direction }
        ) + fadeIn(
            animationSpec = tween(durationMillis = enterMs, easing = DecelerateEasing)
        )

        val exit = slideOutHorizontally(
            animationSpec = tween(durationMillis = exitMs, easing = AccelerateEasing),
            targetOffsetX = { fullWidth -> -(fullWidth / 10) * direction }
        ) + fadeOut(
            animationSpec = tween(durationMillis = exitMs, easing = AccelerateEasing)
        )

        return ContentTransform(
            targetContentEnter = enter,
            initialContentExit = exit,
            sizeTransform = SizeTransform(clip = false)
        )
    }
}

@Composable
fun rememberSystemAnimatorScale(): Float {
    val context = LocalContext.current
    return remember(context) { KuotaMotion.getSystemAnimatorScale(context) }
}

/**
 * Contenedor de transición direccional entre pasos del onboarding.
 */
@Composable
fun <S : Comparable<S>> KuotaStepAnimatedContent(
    targetState: S,
    modifier: Modifier = Modifier,
    label: String = "KuotaStepTransition",
    content: @Composable AnimatedContentScope.(targetState: S) -> Unit
) {
    val animatorScale = rememberSystemAnimatorScale()
    AnimatedContent(
        targetState = targetState,
        modifier = modifier,
        transitionSpec = {
            val isForward = targetState >= initialState
            KuotaMotion.stepTransitionSpec(
                isForward = isForward,
                animatorScale = animatorScale
            )
        },
        label = label,
        content = content
    )
}

/**
 * Entrada escalonada (fade + desplazamiento vertical corto) que solo se ejecuta
 * cuando `animateEntrance` es `true` (primera visita a la pantalla).
 * Si la pantalla ya fue visitada o el sistema reduce movimiento, permanece estática desde el inicio.
 */
@Composable
fun Modifier.kuotaEntranceReveal(
    animateEntrance: Boolean,
    delayMs: Int = 0,
    initialOffsetY: Dp = 10.dp
): Modifier {
    val animatorScale = rememberSystemAnimatorScale()
    val density = LocalDensity.current
    val initialPx = with(density) { initialOffsetY.toPx() }
    // La decisión se toma una sola vez por composición: cambios posteriores de `animateEntrance`
    // (p. ej. marcar la pantalla como visitada) no deben cortar una animación en curso.
    val shouldAnimate = remember { animateEntrance && animatorScale > 0f }

    val alphaAnim = remember { Animatable(if (shouldAnimate) 0f else 1f) }
    val offsetYAnim = remember { Animatable(if (shouldAnimate) initialPx else 0f) }

    LaunchedEffect(Unit) {
        if (shouldAnimate) {
            val effectiveDelay = KuotaMotion.resolveDurationMs(delayMs, animatorScale).toLong()
            val effectiveDuration = KuotaMotion.resolveDurationMs(KuotaMotion.ENTRANCE_DURATION_MS, animatorScale)
            if (effectiveDelay > 0L) delay(effectiveDelay)
            launch {
                alphaAnim.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(durationMillis = effectiveDuration, easing = KuotaMotion.DecelerateEasing)
                )
            }
            launch {
                offsetYAnim.animateTo(
                    targetValue = 0f,
                    animationSpec = tween(durationMillis = effectiveDuration, easing = KuotaMotion.CrispOutEasing)
                )
            }
        } else {
            alphaAnim.snapTo(1f)
            offsetYAnim.snapTo(0f)
        }
    }

    return this.graphicsLayer {
        alpha = alphaAnim.value
        translationY = offsetYAnim.value
    }
}

/**
 * Desplazamiento horizontal breve y discreto (±5.dp) para indicar un error de validación o PIN
 * sin sacudidas exageradas.
 */
@Composable
fun Modifier.kuotaErrorMicroOffset(triggerKey: Int): Modifier {
    val animatorScale = rememberSystemAnimatorScale()
    val density = LocalDensity.current
    val maxOffsetPx = with(density) { 5.dp.toPx() }
    val offsetX = remember { Animatable(0f) }

    LaunchedEffect(triggerKey) {
        if (triggerKey > 0 && animatorScale > 0f) {
            val stepMs = KuotaMotion.resolveDurationMs(45, animatorScale)
            offsetX.animateTo(-maxOffsetPx, tween(stepMs, easing = KuotaMotion.StandardEasing))
            offsetX.animateTo(maxOffsetPx, tween(stepMs, easing = KuotaMotion.StandardEasing))
            offsetX.animateTo(-maxOffsetPx * 0.5f, tween(stepMs, easing = KuotaMotion.StandardEasing))
            offsetX.animateTo(0f, tween(stepMs, easing = KuotaMotion.DecelerateEasing))
        } else {
            offsetX.snapTo(0f)
        }
    }

    return this.offset { IntOffset(offsetX.value.roundToInt(), 0) }
}

/**
 * Aparición progresiva por bloques para las 5 pestañas principales (Cuenta, Tarjetas, QR, Actividad, Menú).
 * No retrasa ni bloquea la interacción táctil y respeta la reducción de movimiento del sistema.
 */
@Composable
fun Modifier.kuotaTabSectionReveal(
    sectionIndex: Int = 0,
    initialOffsetY: Dp = 8.dp
): Modifier {
    // Se anima al abrir la pestaña; tras rotación/recreación (estado restaurado) queda estática.
    var hasRevealed by rememberSaveable(sectionIndex) { mutableStateOf(false) }
    val animateNow = remember { !hasRevealed }
    val delayMs = (sectionIndex.coerceIn(0, 5) * 32)
    LaunchedEffect(Unit) {
        hasRevealed = true
    }
    return this.kuotaEntranceReveal(
        animateEntrance = animateNow,
        delayMs = delayMs,
        initialOffsetY = initialOffsetY
    )
}

/**
 * Microinteracción táctil reutilizable (100–160 ms) con variación sutil de escala y feedback háptico ligero.
 */
@Composable
fun Modifier.kuotaPressable(
    enabled: Boolean = true,
    pressedScale: Float = 0.975f,
    hapticOnTap: Boolean = true,
    role: Role? = null,
    onClick: () -> Unit
): Modifier {
    val view = LocalView.current
    val animatorScale = rememberSystemAnimatorScale()
    val duration = KuotaMotion.resolveDurationMs(135, animatorScale)
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed && enabled) pressedScale else 1.0f,
        animationSpec = tween(durationMillis = duration, easing = KuotaMotion.StandardEasing),
        label = "kuotaPressableScale"
    )

    return this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .clickable(
            interactionSource = interactionSource,
            indication = LocalIndication.current,
            enabled = enabled,
            role = role,
            onClick = {
                if (hapticOnTap) {
                    KuotaMotion.performKeyTapHaptic(view)
                }
                onClick()
            }
        )
}
