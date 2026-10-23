package com.kouta.app.core.security

import android.content.Context
import java.util.concurrent.atomic.AtomicLong

/**
 * Control de inactividad de sesión en primer plano para KUOTA.
 *
 * Implementa la práctica estándar fintech de expirar la sesión visual y forzar
 * el bloqueo por PIN / biometría si el usuario deja la aplicación abierta sin interactuar
 * durante un periodo prolongado (5 minutos).
 */
object InactivityLockManager {

    /**
     * Tiempo límite de inactividad antes de forzar el bloqueo: 5 minutos.
     */
    const val INACTIVITY_TIMEOUT_MILLIS: Long = 5 * 60 * 1000L

    private val lastInteractionTimestamp = AtomicLong(System.currentTimeMillis())

    /**
     * Registra interacción activa del usuario (toques en pantalla, escritura de teclado).
     */
    fun notifyUserActivity() {
        lastInteractionTimestamp.set(System.currentTimeMillis())
    }

    /**
     * Comprueba si ha transcurrido el tiempo límite de inactividad.
     * Si expiró y el PIN está configurado, bloquea la aplicación inmediatamente.
     *
     * @return `true` si la aplicación fue bloqueada por inactividad.
     */
    fun checkInactivity(context: Context, nowMillis: Long = System.currentTimeMillis()): Boolean {
        if (!PinSecurityManager.isPinConfigured(context)) return false
        if (PinSecurityManager.isLocked(context)) return false

        val elapsed = nowMillis - lastInteractionTimestamp.get()
        if (elapsed >= INACTIVITY_TIMEOUT_MILLIS) {
            PinSecurityManager.lock(context)
            return true
        }
        return false
    }

    /**
     * Restablece el contador de inactividad tras un desbloqueo exitoso.
     */
    fun reset() {
        lastInteractionTimestamp.set(System.currentTimeMillis())
    }
}
