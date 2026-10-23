package com.kouta.app.core.security

import android.app.Activity
import android.view.WindowManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Gestor dinámico de la bandera de seguridad de pantalla (`FLAG_SECURE`).
 *
 * En aplicaciones financieras (como Yape o KUOTA):
 * - Se activa permanentemente cuando la app está bloqueada por PIN o biometría,
 *   evitando que el sistema operativo capture la miniatura de la pantalla en la vista
 *   de tareas recientes (Recent Apps / Multitarea).
 * - Se activa selectivamente cuando el usuario navega a flujos de alta sensibilidad:
 *   Verificación KYC de DNI, visualización de credenciales de tarjetas y configuración de PIN.
 * - Se libera en pantallas públicas / no sensibles cuando la sesión está desbloqueada,
 *   permitiendo una experiencia fluida sin restricciones innecesarias.
 */
object ScreenSecurityManager {

    private val _isSensitiveScreenActive = MutableStateFlow(false)
    val isSensitiveScreenActive: StateFlow<Boolean> = _isSensitiveScreenActive.asStateFlow()

    /**
     * Declara que la pantalla actual contiene información financiera o de identidad confidencial.
     */
    fun enterSensitiveScreen(activity: Activity?) {
        _isSensitiveScreenActive.value = true
        activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
    }

    /**
     * Declara que la pantalla actual no contiene datos de alta sensibilidad.
     */
    fun exitSensitiveScreen(activity: Activity?, isAppLocked: Boolean) {
        _isSensitiveScreenActive.value = false
        if (!isAppLocked) {
            activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
        }
    }

    /**
     * Aplica la política de seguridad según el estado global de bloqueo y sensibilidad.
     */
    fun applyPolicy(activity: Activity?, isAppLocked: Boolean) {
        val window = activity?.window ?: return
        if (isAppLocked || _isSensitiveScreenActive.value) {
            window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
        }
    }
}
