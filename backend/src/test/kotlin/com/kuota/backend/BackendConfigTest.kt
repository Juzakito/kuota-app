package com.kuota.backend

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class BackendConfigTest {

    @Test
    fun `demo defaults run without database or payment credentials`() {
        val config = BackendConfigLoader.fromEnvironment(emptyMap())

        assertEquals(RuntimeEnvironment.DEMO, config.environment)
        assertEquals(PaymentProviderKind.DEMO, config.paymentProvider)
        assertEquals("127.0.0.1", config.host)
        assertEquals(8080, config.port)
        assertNull(config.database)
        assertNull(config.mercadoPago)
        assertNull(config.webhookHmacSecret)
        assertNull(config.adminToken)
        assertTrue(config.jwtSecret.length >= JWT_MIN_SECRET_LENGTH)
        assertTrue(config.qrSigningSecret.length >= QR_SIGNING_MIN_SECRET_LENGTH)
        assertNotEquals(config.jwtSecret, config.qrSigningSecret)
        assertFalse(config.isProduction)
    }

    @Test
    fun `demo generates distinct ephemeral secrets per configuration`() {
        val first = BackendConfigLoader.fromEnvironment(emptyMap())
        val second = BackendConfigLoader.fromEnvironment(emptyMap())

        assertTrue(first.jwtSecret.length >= JWT_MIN_SECRET_LENGTH)
        assertTrue(first.qrSigningSecret.length >= QR_SIGNING_MIN_SECRET_LENGTH)
        assertNotEquals(first.jwtSecret, second.jwtSecret)
        assertNotEquals(first.qrSigningSecret, second.qrSigningSecret)
    }

    @Test
    fun `production refuses the demo simulator`() {
        val error = assertFailsWith<IllegalStateException> {
            BackendConfigLoader.fromEnvironment(mapOf("KUOTA_ENVIRONMENT" to "production"))
        }

        assertTrue(error.message.orEmpty().contains("KUOTA_PAYMENT_PROVIDER=mercadopago"))
    }

    @Test
    fun `production refuses the file ledger without postgresql`() {
        val error = assertFailsWith<IllegalStateException> {
            BackendConfigLoader.fromEnvironment(productionEnvironment())
        }

        assertTrue(error.message.orEmpty().contains("KUOTA_DATABASE_URL"))
    }

    @Test
    fun `production refuses short secrets`() {
        val environment = productionEnvironment().apply {
            put("KUOTA_DATABASE_URL", "postgresql://kuota:secret@localhost:5432/kuota")
            put("KUOTA_WEBHOOK_SECRET", "short")
            put("KUOTA_ADMIN_TOKEN", "short")
        }

        val hmacError = assertFailsWith<IllegalStateException> {
            BackendConfigLoader.fromEnvironment(environment)
        }
        assertTrue(hmacError.message.orEmpty().contains("KUOTA_WEBHOOK_SECRET"))

        environment["KUOTA_WEBHOOK_SECRET"] = "w".repeat(32)
        val adminError = assertFailsWith<IllegalStateException> {
            BackendConfigLoader.fromEnvironment(environment)
        }
        assertTrue(adminError.message.orEmpty().contains("KUOTA_ADMIN_TOKEN"))
    }

    @Test
    fun `production requires a persistent jwt secret`() {
        val environment = completeProductionEnvironment()
        environment.remove("KUOTA_JWT_SECRET")

        val error = assertFailsWith<IllegalStateException> {
            BackendConfigLoader.fromEnvironment(environment)
        }

        assertTrue(error.message.orEmpty().contains("KUOTA_JWT_SECRET"))
    }

    @Test
    fun `short jwt secrets are rejected in any environment`() {
        val error = assertFailsWith<IllegalStateException> {
            BackendConfigLoader.fromEnvironment(mapOf("KUOTA_JWT_SECRET" to "short"))
        }

        assertTrue(error.message.orEmpty().contains("KUOTA_JWT_SECRET"))
    }

    @Test
    fun `production requires a persistent qr signing secret`() {
        val environment = completeProductionEnvironment()
        environment.remove("KUOTA_QR_SIGNING_SECRET")

        val error = assertFailsWith<IllegalStateException> {
            BackendConfigLoader.fromEnvironment(environment)
        }

        assertTrue(error.message.orEmpty().contains("KUOTA_QR_SIGNING_SECRET"))
    }

    @Test
    fun `short qr signing secrets are rejected in any environment`() {
        val error = assertFailsWith<IllegalStateException> {
            BackendConfigLoader.fromEnvironment(mapOf("KUOTA_QR_SIGNING_SECRET" to "short"))
        }

        assertTrue(error.message.orEmpty().contains("KUOTA_QR_SIGNING_SECRET"))
    }

    @Test
    fun `production accepts a complete configuration`() {
        val config = BackendConfigLoader.fromEnvironment(
            productionEnvironment().apply {
                put("KUOTA_DATABASE_URL", "postgresql://kuota:secret@localhost:5432/kuota")
                put("KUOTA_DATABASE_USER", "kuota_app")
                put("KUOTA_DATABASE_PASSWORD", "secret")
                put("KUOTA_WEBHOOK_SECRET", "w".repeat(32))
                put("KUOTA_ADMIN_TOKEN", "a".repeat(32))
            }
        )

        assertTrue(config.isProduction)
        assertEquals(PaymentProviderKind.MERCADO_PAGO, config.paymentProvider)
        assertNotNull(config.database)
        assertEquals("jdbc:postgresql://kuota:secret@localhost:5432/kuota", config.database?.jdbcUrl)
        assertEquals("kuota_app", config.database?.username)
        assertNotNull(config.mercadoPago)
        assertEquals("j".repeat(32), config.jwtSecret)
        assertEquals("q".repeat(32), config.qrSigningSecret)
        assertTrue(config.isAdminTokenValid("a".repeat(32)))
        assertFalse(config.isAdminTokenValid("a".repeat(31)))
        assertFalse(config.isAdminTokenValid(null))
    }

    @Test
    fun `mercadopago derives its notification url from the public base url`() {
        val config = BackendConfigLoader.fromEnvironment(
            mapOf(
                "KUOTA_PAYMENT_PROVIDER" to "mercadopago",
                "KUOTA_MP_ACCESS_TOKEN" to "test-token",
                "KUOTA_MP_WEBHOOK_SECRET" to "mp-secret",
                "KUOTA_PUBLIC_BASE_URL" to "https://payments.example.com/",
                "KUOTA_DATABASE_URL" to "postgresql://127.0.0.1:5433/kuota"
            )
        )

        assertEquals(
            "https://payments.example.com/v1/webhooks/mercadopago",
            config.mercadoPago?.notificationUrl
        )
        assertNotNull(config.database)
        assertFalse(config.isProduction)
    }

    @Test
    fun `mercadopago refuses the file ledger even in demo`() {
        val error = assertFailsWith<IllegalStateException> {
            BackendConfigLoader.fromEnvironment(
                mapOf(
                    "KUOTA_PAYMENT_PROVIDER" to "mercadopago",
                    "KUOTA_MP_ACCESS_TOKEN" to "test-token",
                    "KUOTA_MP_WEBHOOK_SECRET" to "mp-secret",
                    "KUOTA_PUBLIC_BASE_URL" to "https://payments.example.com/"
                )
            )
        }

        assertTrue(error.message.orEmpty().contains("KUOTA_DATABASE_URL"))
    }

    @Test
    fun `mercadopago requires an access token and webhook secret`() {
        val tokenError = assertFailsWith<IllegalStateException> {
            BackendConfigLoader.fromEnvironment(mapOf("KUOTA_PAYMENT_PROVIDER" to "mercadopago"))
        }
        assertTrue(tokenError.message.orEmpty().contains("KUOTA_MP_ACCESS_TOKEN"))

        val secretError = assertFailsWith<IllegalStateException> {
            BackendConfigLoader.fromEnvironment(
                mapOf(
                    "KUOTA_PAYMENT_PROVIDER" to "mercadopago",
                    "KUOTA_MP_ACCESS_TOKEN" to "test-token"
                )
            )
        }
        assertTrue(secretError.message.orEmpty().contains("KUOTA_MP_WEBHOOK_SECRET"))
    }

    @Test
    fun `unknown environment values are rejected`() {
        assertFailsWith<IllegalStateException> {
            BackendConfigLoader.fromEnvironment(mapOf("KUOTA_ENVIRONMENT" to "staging"))
        }
        assertFailsWith<IllegalStateException> {
            BackendConfigLoader.fromEnvironment(mapOf("KUOTA_PAYMENT_PROVIDER" to "culqi"))
        }
        assertFailsWith<IllegalStateException> {
            BackendConfigLoader.fromEnvironment(mapOf("PORT" to "70000"))
        }
    }

    private fun productionEnvironment(): MutableMap<String, String> = mutableMapOf(
        "KUOTA_ENVIRONMENT" to "production",
        "KUOTA_PAYMENT_PROVIDER" to "mercadopago",
        "KUOTA_MP_ACCESS_TOKEN" to "test-token",
        "KUOTA_MP_WEBHOOK_SECRET" to "mp-webhook-secret",
        "KUOTA_MP_NOTIFICATION_URL" to "https://payments.example.com/v1/webhooks/mercadopago",
        "KUOTA_JWT_SECRET" to "j".repeat(32),
        "KUOTA_QR_SIGNING_SECRET" to "q".repeat(32)
    )

    private fun completeProductionEnvironment(): MutableMap<String, String> =
        productionEnvironment().apply {
            put("KUOTA_DATABASE_URL", "postgresql://kuota:secret@localhost:5432/kuota")
            put("KUOTA_DATABASE_USER", "kuota_app")
            put("KUOTA_DATABASE_PASSWORD", "secret")
            put("KUOTA_WEBHOOK_SECRET", "w".repeat(32))
            put("KUOTA_ADMIN_TOKEN", "a".repeat(32))
        }
}
