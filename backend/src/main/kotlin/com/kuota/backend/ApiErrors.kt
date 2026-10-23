package com.kuota.backend

import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.createApplicationPlugin
import io.ktor.server.response.header
import io.ktor.util.AttributeKey
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.UUID

private val requestIdKey = AttributeKey<String>("KuotaRequestId")
private val validRequestId = Regex("^[A-Za-z0-9._:-]{1,64}$")

/**
 * Adds a correlation id to every response. An inbound id is reused only when it is
 * short and alphanumeric so it cannot be used for header injection.
 */
internal val RequestIdPlugin = createApplicationPlugin("KuotaRequestId") {
    onCall { call ->
        val incoming = call.request.headers["X-Request-Id"]?.trim()?.takeIf(validRequestId::matches)
        val requestId = incoming ?: UUID.randomUUID().toString()
        call.attributes.put(requestIdKey, requestId)
        call.response.header("X-Request-Id", requestId)
        call.response.header("X-Content-Type-Options", "nosniff")
        call.response.header("Referrer-Policy", "no-referrer")
    }
}

internal fun ApplicationCall.requestId(): String =
    attributes.getOrNull(requestIdKey) ?: "unavailable"

@Serializable
internal data class ApiError(
    val code: String,
    val message: String,
    @SerialName("request_id") val requestId: String = "",
    val retryable: Boolean = false
)

internal fun ApplicationCall.apiError(
    code: String,
    message: String,
    retryable: Boolean = false
): ApiError = ApiError(code, message, requestId(), retryable)
