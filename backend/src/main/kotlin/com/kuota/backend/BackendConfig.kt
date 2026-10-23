package com.kuota.backend

import org.slf4j.LoggerFactory
import java.nio.file.Path
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64

internal enum class RuntimeEnvironment { DEMO, PRODUCTION }

internal enum class PaymentProviderKind { DEMO, MERCADO_PAGO }

internal data class DatabaseConfig(
    val jdbcUrl: String,
    val username: String,
    val password: String,
    val maxPoolSize: Int
)

internal data class MercadoPagoConfig(
    val accessToken: String,
    val webhookSecret: String,
    val notificationUrl: String,
    val backUrl: String?,
    val apiBaseUrl: String = "https://api.mercadopago.com"
)

internal data class BackendConfig(
    val environment: RuntimeEnvironment,
    val paymentProvider: PaymentProviderKind,
    val port: Int,
    val host: String,
    val demoStorePath: Path,
    val database: DatabaseConfig?,
    val webhookHmacSecret: String?,
    val adminToken: String?,
    val mercadoPago: MercadoPagoConfig?,
    val jwtSecret: String,
    val qrSigningSecret: String
) {
    val isProduction: Boolean get() = environment == RuntimeEnvironment.PRODUCTION
    val dataDirectory: Path get() = demoStorePath.toAbsolutePath().parent ?: Path.of("data")

    fun isAdminTokenValid(candidate: String?): Boolean {
        val expected = adminToken ?: return false
        if (candidate.isNullOrEmpty()) return false
        return MessageDigest.isEqual(expected.toByteArray(Charsets.UTF_8), candidate.toByteArray(Charsets.UTF_8))
    }
}

internal object BackendConfigLoader {
    private val logger = LoggerFactory.getLogger(BackendConfigLoader::class.java)

    fun fromEnvironment(env: Map<String, String> = System.getenv()): BackendConfig {
        fun value(name: String): String? = env[name]?.trim()?.takeIf(String::isNotEmpty)

        val environment = when (value("KUOTA_ENVIRONMENT")?.lowercase() ?: "demo") {
            "demo" -> RuntimeEnvironment.DEMO
            "production", "prod" -> RuntimeEnvironment.PRODUCTION
            else -> error("KUOTA_ENVIRONMENT must be 'demo' or 'production'.")
        }
        val provider = when (value("KUOTA_PAYMENT_PROVIDER")?.lowercase() ?: "demo") {
            "demo" -> PaymentProviderKind.DEMO
            "mercadopago", "mercado_pago", "mercado-pago" -> PaymentProviderKind.MERCADO_PAGO
            else -> error("KUOTA_PAYMENT_PROVIDER must be 'demo' or 'mercadopago'.")
        }
        val port = value("PORT")?.toIntOrNull() ?: 8080
        if (port !in 1..65535) error("PORT must be between 1 and 65535.")
        val host = value("HOST") ?: "127.0.0.1"
        val demoStorePath = value("KUOTA_DEMO_STORE")?.let(Path::of)
            ?: Path.of("data", "kuota-demo-store.json")

        val database = value("KUOTA_DATABASE_URL")?.let { rawUrl ->
            val jdbcUrl = if (rawUrl.startsWith("jdbc:", ignoreCase = true)) rawUrl else "jdbc:$rawUrl"
            val username = value("KUOTA_DATABASE_USER") ?: "kuota"
            val password = value("KUOTA_DATABASE_PASSWORD") ?: ""
            val poolSize = value("KUOTA_DATABASE_POOL_SIZE")?.toIntOrNull() ?: 5
            if (poolSize !in 1..20) error("KUOTA_DATABASE_POOL_SIZE must be between 1 and 20.")
            DatabaseConfig(jdbcUrl, username, password, poolSize)
        }

        val mercadoPago = if (provider == PaymentProviderKind.MERCADO_PAGO) {
            val accessToken = value("KUOTA_MP_ACCESS_TOKEN")
                ?: error("KUOTA_MP_ACCESS_TOKEN is required when KUOTA_PAYMENT_PROVIDER=mercadopago.")
            val webhookSecret = value("KUOTA_MP_WEBHOOK_SECRET")
                ?: error("KUOTA_MP_WEBHOOK_SECRET is required when KUOTA_PAYMENT_PROVIDER=mercadopago.")
            val notificationUrl = value("KUOTA_MP_NOTIFICATION_URL")
                ?: value("KUOTA_PUBLIC_BASE_URL")?.trimEnd('/')?.let { "$it/v1/webhooks/mercadopago" }
                ?: error("KUOTA_MP_NOTIFICATION_URL (or KUOTA_PUBLIC_BASE_URL) is required when KUOTA_PAYMENT_PROVIDER=mercadopago.")
            require(notificationUrl.startsWith("https://") || notificationUrl.startsWith("http://")) {
                "KUOTA_MP_NOTIFICATION_URL must be an absolute http(s) URL."
            }
            if (environment == RuntimeEnvironment.PRODUCTION && !notificationUrl.startsWith("https://")) {
                error("KUOTA_MP_NOTIFICATION_URL must use HTTPS in production.")
            }
            val backUrl = value("KUOTA_MP_BACK_URL")
            if (backUrl != null) {
                require(backUrl.startsWith("https://") || backUrl.startsWith("http://")) {
                    "KUOTA_MP_BACK_URL must be an absolute http(s) URL."
                }
            }
            MercadoPagoConfig(
                accessToken = accessToken,
                webhookSecret = webhookSecret,
                notificationUrl = notificationUrl,
                backUrl = backUrl,
                apiBaseUrl = value("KUOTA_MP_API_BASE_URL") ?: "https://api.mercadopago.com"
            )
        } else {
            null
        }

        val webhookHmacSecret = value("KUOTA_WEBHOOK_SECRET")
        val adminToken = value("KUOTA_ADMIN_TOKEN")
        val configuredJwtSecret = value("KUOTA_JWT_SECRET")
        if (configuredJwtSecret != null && configuredJwtSecret.length < JWT_MIN_SECRET_LENGTH) {
            error("KUOTA_JWT_SECRET must have at least $JWT_MIN_SECRET_LENGTH characters.")
        }
        val configuredQrSigningSecret = value("KUOTA_QR_SIGNING_SECRET")
        if (configuredQrSigningSecret != null && configuredQrSigningSecret.length < QR_SIGNING_MIN_SECRET_LENGTH) {
            error("KUOTA_QR_SIGNING_SECRET must have at least $QR_SIGNING_MIN_SECRET_LENGTH characters.")
        }

        if (provider == PaymentProviderKind.MERCADO_PAGO && database == null) {
            error("KUOTA_PAYMENT_PROVIDER=mercadopago requires KUOTA_DATABASE_URL; the file ledger is demo-only.")
        }

        if (environment == RuntimeEnvironment.PRODUCTION) {
            if (provider != PaymentProviderKind.MERCADO_PAGO) {
                error("Production requires KUOTA_PAYMENT_PROVIDER=mercadopago. The demo simulator cannot be used in production.")
            }
            if (database == null) {
                error("Production requires KUOTA_DATABASE_URL pointing to PostgreSQL; the file ledger is demo-only.")
            }
            if (webhookHmacSecret == null || webhookHmacSecret.length < 32) {
                error("Production requires KUOTA_WEBHOOK_SECRET with at least 32 characters for generic HMAC webhooks.")
            }
            if (adminToken == null || adminToken.length < 32) {
                error("Production requires KUOTA_ADMIN_TOKEN with at least 32 characters for ledger inspection.")
            }
            if (configuredJwtSecret == null) {
                error("Production requires KUOTA_JWT_SECRET with at least $JWT_MIN_SECRET_LENGTH characters so access tokens survive restarts.")
            }
            if (configuredQrSigningSecret == null) {
                error("Production requires KUOTA_QR_SIGNING_SECRET with at least $QR_SIGNING_MIN_SECRET_LENGTH characters so signed supplier QRs survive restarts.")
            }
        }

        val jwtSecret = configuredJwtSecret ?: run {
            logger.warn(
                "KUOTA_JWT_SECRET is not set; using an ephemeral demo secret. Access tokens will be invalid after a restart."
            )
            ephemeralSecret()
        }
        val qrSigningSecret = configuredQrSigningSecret ?: run {
            logger.warn(
                "KUOTA_QR_SIGNING_SECRET is not set; using an ephemeral demo secret. Supplier QRs will be invalid after a restart."
            )
            ephemeralSecret()
        }

        return BackendConfig(
            environment = environment,
            paymentProvider = provider,
            port = port,
            host = host,
            demoStorePath = demoStorePath,
            database = database,
            webhookHmacSecret = webhookHmacSecret,
            adminToken = adminToken,
            mercadoPago = mercadoPago,
            jwtSecret = jwtSecret,
            qrSigningSecret = qrSigningSecret
        )
    }

    private fun ephemeralSecret(): String {
        val bytes = ByteArray(32)
        SecureRandom().nextBytes(bytes)
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
    }
}
