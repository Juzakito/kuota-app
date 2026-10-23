package com.kouta.app.core.security

import android.app.Activity
import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.kouta.app.data.local.TokenManager

/**
 * Gestor de cierre de sesión seguro y revocación de credenciales para KUOTA.
 *
 * Sigue las recomendaciones del NIST y las mejores prácticas de seguridad bancaria:
 * 1. Revoca la sesión de Firebase Authentication (`signOut()`).
 * 2. Borra las claves de sesión JWT cifradas con Android Keystore en [TokenManager].
 * 3. Restablece los candados de idempotencia y temporizadores de inactividad.
 * 4. Bloquea inmediatamente la interfaz mediante [PinSecurityManager.lock] y activa `FLAG_SECURE`.
 */
object SecureLogoutManager {

    /**
     * Ejecuta el cierre de sesión completo y seguro.
     */
    fun performLogout(context: Context, activity: Activity? = null) {
        val appContext = context.applicationContext

        // 1. Cierra la sesión activa en Firebase Auth
        try {
            FirebaseAuth.getInstance().signOut()
        } catch (_: Exception) {}

        // 2. Destruye los tokens JWT almacenados en SharedPreferences cifradas
        try {
            TokenManager(appContext).clearToken()
        } catch (_: Exception) {}

        // 3. Restablece candados de operaciones activas
        FintechOperationLock.reset()
        InactivityLockManager.reset()

        // 4. Bloquea la aplicación por PIN inmediatamente
        PinSecurityManager.lock(appContext)

        // 5. Aplica FLAG_SECURE a la ventana para evitar capturas en el switcher de tareas
        ScreenSecurityManager.applyPolicy(activity, isAppLocked = true)
    }
}
