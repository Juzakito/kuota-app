package com.kouta.app.domain.model

data class Business(
    val id: String,
    val name: String,
    val ruc: String,
    val isVerified: Boolean = false,
    val salesConnectionStatus: SalesConnectionStatus = SalesConnectionStatus.NOT_CONNECTED
)

enum class SalesConnectionStatus {
    NOT_CONNECTED,
    CONNECTING,
    ACTIVE,
    EXPIRED,
    ERROR
}
