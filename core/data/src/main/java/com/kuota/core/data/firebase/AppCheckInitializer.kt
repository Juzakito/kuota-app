package com.kuota.core.data.firebase

import android.content.Context
import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory

object AppCheckInitializer {
    /**
     * Inicializa Firebase App Check según el flavor/entorno (PRD §6.6 y AGENTS.md):
     * - dev: DebugAppCheckProviderFactory (permite emuladores y desarrollo local).
     * - staging/prod: PlayIntegrityAppCheckProviderFactory (valida integridad en dispositivos reales).
     */
    fun init(context: Context, isDebugOrDev: Boolean) {
        try {
            FirebaseApp.initializeApp(context)
            val firebaseAppCheck = FirebaseAppCheck.getInstance()
            if (isDebugOrDev) {
                firebaseAppCheck.installAppCheckProviderFactory(
                    DebugAppCheckProviderFactory.getInstance()
                )
            } else {
                firebaseAppCheck.installAppCheckProviderFactory(
                    PlayIntegrityAppCheckProviderFactory.getInstance()
                )
            }
        } catch (e: Exception) {
            // Log estructurado de inicialización
        }
    }
}
