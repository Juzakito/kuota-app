package com.kouta.app

import android.app.Application
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory
import com.kouta.app.core.sync.KuotaSync
import com.kouta.app.core.util.NotificationHelper
import dagger.hilt.android.HiltAndroidApp

/**
 * Aplicación principal KUOTA con inicialización de seguridad centralizada:
 * - Canales de notificación seguros
 * - Firebase App Check con Play Integrity en release y DebugProvider en debug
 * - Tareas periódicas de sincronización con SQLite / Cloud Firestore
 */
@HiltAndroidApp
class KoutaApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
        initFirebaseAppCheck()
        NotificationHelper.ensureChannels(this)
        KuotaSync.schedulePeriodic(this)
        KuotaSync.enqueue(this)
    }

    /**
     * Inicializa la atestación de integridad del dispositivo y del binario de KUOTA (Firebase App Check).
     * En producción utiliza Google Play Integrity API para garantizar que las peticiones a Firestore
     * provengan únicamente del APK legítimo y firmado.
     */
    private fun initFirebaseAppCheck() {
        try {
            val appCheck = FirebaseAppCheck.getInstance()
            if (BuildConfig.DEBUG) {
                appCheck.installAppCheckProviderFactory(
                    DebugAppCheckProviderFactory.getInstance()
                )
                Log.d("KoutaApp", "Firebase App Check configurado en modo DEBUG.")
            } else {
                appCheck.installAppCheckProviderFactory(
                    PlayIntegrityAppCheckProviderFactory.getInstance()
                )
                Log.i("KoutaApp", "Firebase App Check activo con Play Integrity API.")
            }
        } catch (e: Exception) {
            Log.w("KoutaApp", "No se pudo inicializar Firebase App Check: ${e.message}")
        }
    }
}
