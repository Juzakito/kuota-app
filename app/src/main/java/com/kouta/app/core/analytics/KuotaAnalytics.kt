package com.kouta.app.core.analytics

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.Instant

/**
 * Catálogo oficial de eventos de producto según PRD §15 y §18.14.
 * Ningún evento acepta ni almacena PII (DNI, nombres, teléfonos, correos, direcciones, tokens o credenciales).
 */
enum class KuotaAnalyticsEvent(val eventName: String) {
    ONBOARDING_STARTED("onboarding_started"),
    BUSINESS_PROFILE_COMPLETED("business_profile_completed"),
    SALES_SOURCE_AUTHORIZED("sales_source_authorized"),
    SALES_SYNC_STATUS("sales_sync_status"),
    ELIGIBILITY_SUBMITTED("eligibility_submitted"),
    ELIGIBILITY_RESULT("eligibility_result"),
    INVENTORY_QUOTE_CREATED("inventory_quote_created"),
    OFFER_VIEWED("offer_viewed"),
    OFFER_ACCEPTED("offer_accepted"),
    SUPPLIER_DISBURSEMENT_STATUS("supplier_disbursement_status"),
    PLAN_ACTIVATED("plan_activated"),
    INSTALLMENT_VIEWED("installment_viewed"),
    PAYMENT_ATTEMPT_CREATED("payment_attempt_created"),
    PAYMENT_STATUS_CHANGED("payment_status_changed"),
    SUPPLIER_SETTLEMENT_STATUS("supplier_settlement_status"),
    SUPPORT_CASE_CREATED("support_case_created"),
    REFUND_STATUS_CHANGED("refund_status_changed")
}

data class RecordedAnalyticsEvent(
    val eventName: String,
    val occurredAt: String,
    val properties: Map<String, String>
)

object KuotaAnalytics {
    private const val MAX_BUFFER_SIZE = 50
    private const val MAX_VALUE_LENGTH = 48

    private val allowedPropertyKeys = setOf(
        "step",
        "source",
        "status",
        "result_status",
        "term_days",
        "risk_tier",
        "policy_version",
        "down_payment_bps",
        "reconciliation_status",
        "claim_type",
        "channel",
        "sync_outcome",
        "installment_sequence",
        "demo_only",
        "has_history",
        "reason_code",
        "environment"
    )

    private val forbiddenKeyFragments = listOf(
        "dni",
        "doc",
        "phone",
        "telefono",
        "celular",
        "email",
        "correo",
        "name",
        "nombre",
        "titular",
        "address",
        "direccion",
        "token",
        "secret",
        "password",
        "otp",
        "jwt",
        "signature",
        "payload",
        "amount",
        "monto",
        "account",
        "card",
        "tarjeta"
    )

    // Detecta correos, tokens JWT (eyJ...) o secuencias numéricas de 7+ dígitos (DNI/RUC/teléfono/cuenta).
    private val emailPattern = Regex("[A-Za-z0-9._%+\\-]+@[A-Za-z0-9.\\-]+\\.[A-Za-z]{2,}")
    private val jwtPattern = Regex("\\beyJ[A-Za-z0-9_\\-]+\\.[A-Za-z0-9_\\-]+")
    private val longDigitsPattern = Regex("\\b\\d{7,}\\b")

    private val lock = Any()
    private val _events = MutableStateFlow<List<RecordedAnalyticsEvent>>(emptyList())
    val events: StateFlow<List<RecordedAnalyticsEvent>> = _events.asStateFlow()

    @Volatile
    var enabled: Boolean = true

    fun track(
        event: KuotaAnalyticsEvent,
        properties: Map<String, String> = emptyMap(),
        now: Instant = Instant.now()
    ): RecordedAnalyticsEvent? {
        if (!enabled) return null
        val sanitized = sanitizeProperties(properties)
        val record = RecordedAnalyticsEvent(
            eventName = event.eventName,
            occurredAt = now.toString(),
            properties = sanitized
        )
        synchronized(lock) {
            val updated = (_events.value + record).takeLast(MAX_BUFFER_SIZE)
            _events.value = updated
        }
        return record
    }

    fun sanitizeProperties(raw: Map<String, String>): Map<String, String> {
        if (raw.isEmpty()) return emptyMap()
        val result = linkedMapOf<String, String>()
        for ((rawKey, rawValue) in raw) {
            val key = rawKey.trim().lowercase()
            if (key.isEmpty()) continue
            if (key !in allowedPropertyKeys) continue
            if (forbiddenKeyFragments.any { key.contains(it) }) continue

            val trimmedValue = rawValue.trim().take(MAX_VALUE_LENGTH)
            if (trimmedValue.isEmpty()) continue
            if (containsPotentialPii(trimmedValue)) continue

            result[key] = trimmedValue
        }
        return result
    }

    fun containsPotentialPii(value: String): Boolean {
        if (emailPattern.containsMatchIn(value)) return true
        if (jwtPattern.containsMatchIn(value)) return true
        if (longDigitsPattern.containsMatchIn(value)) return true
        val lower = value.lowercase()
        if (lower.startsWith("bearer ") || lower.contains("kuota_")) return true
        return false
    }

    fun clearForTesting() {
        synchronized(lock) {
            _events.value = emptyList()
        }
    }
}
