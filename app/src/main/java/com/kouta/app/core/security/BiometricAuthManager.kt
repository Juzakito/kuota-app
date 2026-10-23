package com.kouta.app.core.security

import android.content.Context
import android.content.ContextWrapper
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

/**
 * Capacidad del dispositivo para autenticación biométrica real mediante AndroidX Biometric.
 */
sealed interface BiometricCapability {
    /** El dispositivo dispone de hardware biométrico compatible y el usuario tiene credenciales enroladas. */
    data class Available(val authenticators: Int) : BiometricCapability

    /** El dispositivo cuenta con hardware biométrico pero el usuario no ha registrado huella ni rostro. */
    data object NoneEnrolled : BiometricCapability

    /** El dispositivo no cuenta con sensor biométrico compatible. */
    data object NoHardware : BiometricCapability

    /** El sensor biométrico no está disponible temporalmente (ocupado, reiniciando o en falla). */
    data object HardwareUnavailable : BiometricCapability

    /** Sistema operativo o hardware no compatible. */
    data object Unsupported : BiometricCapability
}

/**
 * Resultado explícito de un intento de autenticación biométrica nativa.
 */
sealed interface BiometricAuthResult {
    /** Autenticación confirmada por el sistema Android. */
    data object Success : BiometricAuthResult

    /** Huella o rostro no reconocido por el sensor; Android permite reintentar en el mismo diálogo. */
    data object Failed : BiometricAuthResult

    /** El usuario pulsó el botón negativo nativo "Usar PIN". */
    data object NegativeButtonPin : BiometricAuthResult

    /** El usuario canceló el diálogo (botón Atrás, toque fuera o deslizamiento). */
    data object UserCanceled : BiometricAuthResult

    /** Bloqueo temporal por intentos fallidos repetidos en el sensor biométrico. */
    data class Lockout(val isPermanent: Boolean, val message: String) : BiometricAuthResult

    /** Error de hardware o de la biblioteca. */
    data class Error(val errorCode: Int, val message: String) : BiometricAuthResult
}

/**
 * Gestor oficial de autenticación biométrica para KUOTA basado en [BiometricManager] y [BiometricPrompt].
 *
 * Principios de seguridad:
 * 1. Nunca simula ni da por buena una autenticación sin la respuesta verificada del sistema operativo.
 * 2. Valida la disponibilidad antes de sugerir u ofrecer el desbloqueo biométrico.
 * 3. Permite siempre fallback explícito y seguro al PIN de 6 dígitos de KUOTA.
 * 4. Evita ciclos de bloqueo infinito o diálogos duplicados por recomposición de Jetpack Compose.
 */
object BiometricAuthManager {

    private val LOCK = Any()
    @Volatile
    private var activePrompt: BiometricPrompt? = null
    @Volatile
    private var isAuthenticating: Boolean = false

    /**
     * Inspecciona las capacidades de autenticación biométrica del dispositivo.
     * Evalúa prioritariamente [BIOMETRIC_STRONG] y secundariamente [BIOMETRIC_STRONG] or [BIOMETRIC_WEAK].
     */
    fun checkCapability(context: Context): BiometricCapability {
        val biometricManager = runCatching { BiometricManager.from(context) }.getOrNull()
            ?: return BiometricCapability.Unsupported

        // 1. Probar clase fuerte (Strong / Class 3)
        when (biometricManager.canAuthenticate(BIOMETRIC_STRONG)) {
            BiometricManager.BIOMETRIC_SUCCESS -> return BiometricCapability.Available(BIOMETRIC_STRONG)
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> return BiometricCapability.NoneEnrolled
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> return BiometricCapability.NoHardware
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> return BiometricCapability.HardwareUnavailable
            else -> Unit
        }

        // 2. Fallback a Strong or Weak (Class 2, ej. reconocimiento facial seguro o sensor óptico)
        val combined = BIOMETRIC_STRONG or BIOMETRIC_WEAK
        return when (biometricManager.canAuthenticate(combined)) {
            BiometricManager.BIOMETRIC_SUCCESS -> BiometricCapability.Available(combined)
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> BiometricCapability.NoneEnrolled
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> BiometricCapability.NoHardware
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> BiometricCapability.HardwareUnavailable
            else -> BiometricCapability.Unsupported
        }
    }

    /**
     * Determina si la biometría está disponible y lista para autenticar en este momento.
     */
    fun isAvailable(context: Context): Boolean =
        checkCapability(context) is BiometricCapability.Available

    /**
     * Devuelve una descripción legible de la capacidad para la interfaz (ej. para textos y accesibilidad).
     */
    fun getCapabilityLabel(capability: BiometricCapability): String = when (capability) {
        is BiometricCapability.Available -> "Huella digital o biometría disponible"
        BiometricCapability.NoneEnrolled -> "No tienes huellas ni rostro registrados en los ajustes del teléfono"
        BiometricCapability.NoHardware -> "Este teléfono no cuenta con lector de huellas compatible"
        BiometricCapability.HardwareUnavailable -> "El sensor biométrico no está disponible en este momento"
        BiometricCapability.Unsupported -> "Biometría no admitida en esta versión de Android"
    }

    /**
     * Muestra el diálogo nativo de autenticación de Android [BiometricPrompt].
     *
     * @param activity Actividad que aloja el diálogo (debe heredar de [FragmentActivity]).
     * @param title Título del diálogo nativo.
     * @param subtitle Subtítulo explicativo.
     * @param negativeButtonText Texto del botón de escape; KUOTA utiliza "Usar PIN".
     * @param onResult Callback invocado en el hilo principal con el resultado de la autenticación.
     */
    fun authenticate(
        activity: FragmentActivity,
        title: String = "Desbloquea KUOTA",
        subtitle: String = "Confirma tu identidad para continuar",
        negativeButtonText: String = "Usar PIN",
        onResult: (BiometricAuthResult) -> Unit
    ) = synchronized(LOCK) {
        val capability = checkCapability(activity)
        if (capability !is BiometricCapability.Available) {
            when (capability) {
                BiometricCapability.NoneEnrolled -> onResult(BiometricAuthResult.Error(BiometricPrompt.ERROR_NO_BIOMETRICS, "No hay huella configurada en el teléfono."))
                BiometricCapability.NoHardware -> onResult(BiometricAuthResult.Error(BiometricPrompt.ERROR_HW_NOT_PRESENT, "Dispositivo sin lector biométrico."))
                BiometricCapability.HardwareUnavailable -> onResult(BiometricAuthResult.Error(BiometricPrompt.ERROR_HW_UNAVAILABLE, "Lector biométrico no disponible."))
                else -> onResult(BiometricAuthResult.Error(BiometricPrompt.ERROR_UNABLE_TO_PROCESS, "Biometría no admitida."))
            }
            return@synchronized
        }

        // Cancelamos cualquier prompt previo activo para evitar ventanas superpuestas
        cancelActivePromptInternal()

        val executor = ContextCompat.getMainExecutor(activity)
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setNegativeButtonText(negativeButtonText)
            .setAllowedAuthenticators(capability.authenticators)
            .setConfirmationRequired(false)
            .build()

        val prompt = BiometricPrompt(
            activity,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    synchronized(LOCK) {
                        isAuthenticating = false
                        activePrompt = null
                    }
                    onResult(BiometricAuthResult.Success)
                }

                override fun onAuthenticationFailed() {
                    // El sensor leyó pero no reconoció. Android mantiene el diálogo abierto para reintentar.
                    onResult(BiometricAuthResult.Failed)
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    synchronized(LOCK) {
                        isAuthenticating = false
                        activePrompt = null
                    }
                    val result = when (errorCode) {
                        BiometricPrompt.ERROR_NEGATIVE_BUTTON -> BiometricAuthResult.NegativeButtonPin
                        BiometricPrompt.ERROR_USER_CANCELED,
                        BiometricPrompt.ERROR_CANCELED -> BiometricAuthResult.UserCanceled
                        BiometricPrompt.ERROR_LOCKOUT -> BiometricAuthResult.Lockout(isPermanent = false, errString.toString())
                        BiometricPrompt.ERROR_LOCKOUT_PERMANENT -> BiometricAuthResult.Lockout(isPermanent = true, errString.toString())
                        else -> BiometricAuthResult.Error(errorCode, errString.toString())
                    }
                    onResult(result)
                }
            }
        )

        activePrompt = prompt
        isAuthenticating = true
        prompt.authenticate(promptInfo)
    }

    /**
     * Cancela de forma segura el diálogo biométrico activo si existe.
     */
    fun cancelActivePrompt() = synchronized(LOCK) {
        cancelActivePromptInternal()
    }

    private fun cancelActivePromptInternal() {
        if (isAuthenticating) {
            runCatching { activePrompt?.cancelAuthentication() }
            activePrompt = null
            isAuthenticating = false
        }
    }
}

/**
 * Función utilitaria para obtener el [FragmentActivity] a partir de cualquier [Context].
 */
fun Context.findFragmentActivity(): FragmentActivity? {
    var current: Context? = this
    while (current is ContextWrapper) {
        if (current is FragmentActivity) return current
        current = current.baseContext
    }
    return null
}
