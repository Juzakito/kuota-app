package com.kouta.app.data.local

import android.content.Context
import com.kouta.app.BuildConfig

/** Preferencias no sensibles del dispositivo (el JWT vive cifrado en [TokenManager]). */
class AppSettings(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences("kuota_app_settings", Context.MODE_PRIVATE)

    var serverUrl: String
        get() = prefs.getString(KEY_SERVER_URL, null)
            ?.takeIf { it.isNotBlank() && it != "http://127.0.0.1:8080/" }
            ?: BuildConfig.KUOTA_API_BASE_URL
        set(value) {
            val normalized = value.trim().let { if (it.endsWith("/")) it else "$it/" }
            prefs.edit().putString(KEY_SERVER_URL, normalized).apply()
        }

    val isServerUrlCustom: Boolean get() = prefs.contains(KEY_SERVER_URL)

    fun resetServerUrl() = prefs.edit().remove(KEY_SERVER_URL).apply()

    var fcmToken: String?
        get() = prefs.getString(KEY_FCM_TOKEN, null)
        set(value) = prefs.edit().putString(KEY_FCM_TOKEN, value).apply()

    /** Token "pull-<uuid>" para dispositivos sin Firebase: el servidor encola y la app consulta. */
    val pullToken: String
        get() = prefs.getString(KEY_PULL_TOKEN, null) ?: ("pull-" + java.util.UUID.randomUUID()).also {
            prefs.edit().putString(KEY_PULL_TOKEN, it).apply()
        }

    /** Huella token+preferencias ya registrada en el backend; evita re-registrar sin cambios. */
    var registeredFingerprint: String?
        get() = prefs.getString(KEY_REGISTERED, null)
        set(value) = prefs.edit().putString(KEY_REGISTERED, value).apply()

    var pushMode: String?
        get() = prefs.getString(KEY_PUSH_MODE, null)
        set(value) = prefs.edit().putString(KEY_PUSH_MODE, value).apply()

    var lastPushError: String?
        get() = prefs.getString(KEY_PUSH_ERROR, null)
        set(value) = prefs.edit().putString(KEY_PUSH_ERROR, value).apply()

    var quietHours: String?
        get() = prefs.getString(KEY_QUIET_HOURS, null)
        set(value) = prefs.edit().putString(KEY_QUIET_HOURS, value).apply()

    var linkedPhone: String?
        get() = prefs.getString(KEY_LINKED_PHONE, null)
        set(value) = prefs.edit().putString(KEY_LINKED_PHONE, value).apply()

    /** Respaldo periódico de ventas declaradas; requiere cuenta vinculada. */
    var salesAutoSync: Boolean
        get() = prefs.getBoolean(KEY_SALES_AUTO_SYNC, false)
        set(value) = prefs.edit().putBoolean(KEY_SALES_AUTO_SYNC, value).apply()

    var lastSalesSyncEpochMillis: Long
        get() = prefs.getLong(KEY_LAST_SALES_SYNC, 0L)
        set(value) = prefs.edit().putLong(KEY_LAST_SALES_SYNC, value).apply()

    var notificationPermissionAsked: Boolean
        get() = prefs.getBoolean(KEY_PERMISSION_ASKED, false)
        set(value) = prefs.edit().putBoolean(KEY_PERMISSION_ASKED, value).apply()

    /** Resumen enmascarado de la verificación de identidad demo (p. ej. "Ana Pérez · DNI ****678"). */
    var kycSummary: String?
        get() = prefs.getString(KEY_KYC_SUMMARY, null)
        set(value) = prefs.edit().putString(KEY_KYC_SUMMARY, value).apply()

    /** Estado formal del proceso KYC (NOT_STARTED, DRAFT, PENDING_REVIEW, VERIFIED, REJECTED). */
    var kycStatus: String
        get() = prefs.getString(KEY_KYC_STATUS, "NOT_STARTED") ?: "NOT_STARTED"
        set(value) = prefs.edit().putString(KEY_KYC_STATUS, value).apply()

    var kycRequestId: String?
        get() = prefs.getString(KEY_KYC_REQUEST_ID, null)
        set(value) = prefs.edit().putString(KEY_KYC_REQUEST_ID, value).apply()

    var kycDni: String?
        get() = prefs.getString(KEY_KYC_DNI, null)
        set(value) = prefs.edit().putString(KEY_KYC_DNI, value).apply()

    var kycFullName: String?
        get() = prefs.getString(KEY_KYC_FULL_NAME, null)
        set(value) = prefs.edit().putString(KEY_KYC_FULL_NAME, value).apply()

    var kycConsentGiven: Boolean
        get() = prefs.getBoolean(KEY_KYC_CONSENT, false)
        set(value) = prefs.edit().putBoolean(KEY_KYC_CONSENT, value).apply()

    var kycSubmittedEpochMillis: Long
        get() = prefs.getLong(KEY_KYC_SUBMITTED_AT, 0L)
        set(value) = prefs.edit().putLong(KEY_KYC_SUBMITTED_AT, value).apply()

    /** Medio de pago preferido persistente del comerciante MYPE. */
    var preferredPaymentMethod: String
        get() = prefs.getString(KEY_PREFERRED_PAYMENT_METHOD, "Yape / Plin") ?: "Yape / Plin"
        set(value) = prefs.edit().putString(KEY_PREFERRED_PAYMENT_METHOD, value).apply()

    var preferredPaymentMethodUpdatedAt: Long
        get() = prefs.getLong(KEY_PAYMENT_METHOD_UPDATED_AT, 0L)
        set(value) = prefs.edit().putLong(KEY_PAYMENT_METHOD_UPDATED_AT, value).apply()

    /** SYSTEM, LIGHT o DARK. */
    var themeMode: String
        get() = prefs.getString(KEY_THEME_MODE, null) ?: "SYSTEM"
        set(value) = prefs.edit().putString(KEY_THEME_MODE, value).apply()

    fun clearAll() = prefs.edit().clear().apply()

    fun clearSession() {
        prefs.edit()
            .remove(KEY_REGISTERED)
            .remove(KEY_PUSH_MODE)
            .remove(KEY_PUSH_ERROR)
            .remove(KEY_LINKED_PHONE)
            .remove(KEY_LAST_SALES_SYNC)
            .apply()
    }

    private companion object {
        const val KEY_SERVER_URL = "server_url"
        const val KEY_FCM_TOKEN = "fcm_token"
        const val KEY_PULL_TOKEN = "pull_token"
        const val KEY_REGISTERED = "registered_fingerprint"
        const val KEY_PUSH_MODE = "push_mode"
        const val KEY_PUSH_ERROR = "push_error"
        const val KEY_QUIET_HOURS = "quiet_hours"
        const val KEY_LINKED_PHONE = "linked_phone"
        const val KEY_SALES_AUTO_SYNC = "sales_auto_sync"
        const val KEY_LAST_SALES_SYNC = "last_sales_sync"
        const val KEY_PERMISSION_ASKED = "notification_permission_asked"
        const val KEY_KYC_SUMMARY = "kyc_summary"
        const val KEY_KYC_STATUS = "kyc_status"
        const val KEY_KYC_REQUEST_ID = "kyc_request_id"
        const val KEY_KYC_DNI = "kyc_dni"
        const val KEY_KYC_FULL_NAME = "kyc_full_name"
        const val KEY_KYC_CONSENT = "kyc_consent"
        const val KEY_KYC_SUBMITTED_AT = "kyc_submitted_at"
        const val KEY_PREFERRED_PAYMENT_METHOD = "preferred_payment_method"
        const val KEY_PAYMENT_METHOD_UPDATED_AT = "preferred_payment_method_updated_at"
        const val KEY_THEME_MODE = "theme_mode"
    }
}
