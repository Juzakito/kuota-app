package com.kuota.backend

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.nio.file.Path
import java.time.Instant

private const val MAX_SALES_PER_BATCH = 100
private const val MAX_SALE_AMOUNT_CENTS = 10_000_000L
private const val MAX_SALES_BODY_BYTES = 128_000L
private const val MANUAL_SOURCE = "MANUAL"
private val saleIdRegex = Regex("^[A-Za-z0-9_-]{8,64}$")
private val earliestSale = Instant.parse("2000-01-01T00:00:00Z")

@Serializable
data class DeclaredSaleInput(
    @SerialName("client_sale_id") val clientSaleId: String,
    @SerialName("amount_cents") val amountCents: Long,
    @SerialName("occurred_at") val occurredAt: String,
    val source: String,
    val revision: Int,
    val deleted: Boolean = false
)

@Serializable
data class DeclaredSalesSyncRequest(val sales: List<DeclaredSaleInput> = emptyList())

@Serializable
data class DeclaredSaleRejection(
    @SerialName("client_sale_id") val clientSaleId: String,
    val code: String,
    val message: String
)

@Serializable
data class DeclaredSalesSyncResponse(
    val accepted: Int,
    val duplicates: Int,
    val rejected: List<DeclaredSaleRejection>,
    @SerialName("server_time") val serverTime: String
)

@Serializable
data class DeclaredSaleRecord(
    @SerialName("client_sale_id") val clientSaleId: String,
    @SerialName("amount_cents") val amountCents: Long,
    @SerialName("occurred_at") val occurredAt: String,
    val source: String,
    val revision: Int,
    val deleted: Boolean,
    @SerialName("received_at") val receivedAt: String,
    val verified: Boolean = false
)

@Serializable
data class DeclaredSalesListResponse(
    val sales: List<DeclaredSaleRecord>,
    val verified: Boolean = false,
    val environment: String = "DEMO"
)

@Serializable
private data class PersistedDeclaredSales(
    val users: Map<String, List<DeclaredSaleRecord>> = emptyMap()
)

data class DeclaredSalesSyncOutcome(
    val accepted: Int,
    val duplicates: Int,
    val rejected: List<DeclaredSaleRejection>,
    val serverTime: String
)

/**
 * Ventas declaradas por el usuario, no verificadas y sin efecto en scoring ni elegibilidad.
 * La clave (usuario, client_sale_id) y la revisión hacen el ingreso idempotente.
 */
class DeclaredSalesStore(private val file: Path) {
    private val lock = Any()

    fun sync(
        userId: String,
        inputs: List<DeclaredSaleInput>,
        now: Instant = Instant.now()
    ): DeclaredSalesSyncOutcome = synchronized(lock) {
        val rejected = mutableListOf<DeclaredSaleRejection>()
        val valid = mutableListOf<DeclaredSaleInput>()
        for (input in inputs) {
            val error = validate(input, now)
            if (error == null) valid += input else rejected += DeclaredSaleRejection(input.clientSaleId, error.first, error.second)
        }

        val snapshot = readSnapshot()
        val userSales = snapshot.users[userId].orEmpty().toMutableList()
        var accepted = 0
        var duplicates = 0
        val receivedAt = now.toString()

        for (input in valid) {
            val index = userSales.indexOfFirst { it.clientSaleId == input.clientSaleId }
            if (index < 0) {
                userSales += input.toRecord(receivedAt)
                accepted++
                continue
            }
            val current = userSales[index]
            when {
                input.revision > current.revision -> {
                    userSales[index] = input.toRecord(receivedAt)
                    accepted++
                }
                input.revision == current.revision && current.matches(input) -> duplicates++
                input.revision == current.revision -> rejected += DeclaredSaleRejection(
                    input.clientSaleId,
                    "SALE_REVISION_CONFLICT",
                    "Ya existe una versión distinta de esta venta con la misma revisión."
                )
                else -> rejected += DeclaredSaleRejection(
                    input.clientSaleId,
                    "SALE_REVISION_STALE",
                    "El servidor ya tiene una versión más reciente de esta venta."
                )
            }
        }

        if (accepted > 0) {
            val updatedUsers = snapshot.users + (userId to userSales.sortedBy { it.occurredAt })
            writeJsonFileAtomically(file, PersistedDeclaredSales.serializer(), snapshot.copy(users = updatedUsers))
        }
        DeclaredSalesSyncOutcome(accepted, duplicates, rejected, receivedAt)
    }

    fun listForUser(userId: String): List<DeclaredSaleRecord> = synchronized(lock) {
        readSnapshot().users[userId].orEmpty().sortedByDescending { it.occurredAt }
    }

    private fun readSnapshot(): PersistedDeclaredSales =
        readJsonFile(file, PersistedDeclaredSales.serializer()) ?: PersistedDeclaredSales()

    private fun validate(input: DeclaredSaleInput, now: Instant): Pair<String, String>? {
        if (!saleIdRegex.matches(input.clientSaleId)) {
            return "SALE_ID_INVALID" to "El identificador de venta no es válido."
        }
        if (input.amountCents !in 1..MAX_SALE_AMOUNT_CENTS) {
            return "SALE_AMOUNT_INVALID" to "El monto debe ser mayor a cero y no superar S/ 100,000.00."
        }
        if (input.source != MANUAL_SOURCE) {
            return "SALE_SOURCE_NOT_SUPPORTED" to "Solo se aceptan ventas de registro manual en este endpoint."
        }
        if (input.revision < 0) {
            return "SALE_REVISION_INVALID" to "La revisión de la venta no es válida."
        }
        val occurredAt = runCatching { Instant.parse(input.occurredAt) }.getOrNull()
            ?: return "SALE_DATE_INVALID" to "La fecha de la venta no es válida."
        if (occurredAt.isBefore(earliestSale)) {
            return "SALE_DATE_INVALID" to "La fecha de la venta es demasiado antigua."
        }
        if (occurredAt.isAfter(now.plusSeconds(600))) {
            return "SALE_DATE_FUTURE" to "La fecha de la venta no puede estar en el futuro."
        }
        return null
    }

    private fun DeclaredSaleInput.toRecord(receivedAt: String) = DeclaredSaleRecord(
        clientSaleId = clientSaleId,
        amountCents = amountCents,
        occurredAt = occurredAt,
        source = source,
        revision = revision,
        deleted = deleted,
        receivedAt = receivedAt,
        verified = false
    )

    private fun DeclaredSaleRecord.matches(input: DeclaredSaleInput): Boolean =
        amountCents == input.amountCents &&
            occurredAt == input.occurredAt &&
            source == input.source &&
            deleted == input.deleted &&
            revision == input.revision
}

fun Route.declaredSalesRoutes(store: DeclaredSalesStore) {
    post("/sales/declared/sync") {
        val user = call.requireUser() ?: return@post
        val contentLength = call.request.headers[io.ktor.http.HttpHeaders.ContentLength]?.toLongOrNull()
        if (contentLength != null && contentLength > MAX_SALES_BODY_BYTES) {
            call.respond(HttpStatusCode.PayloadTooLarge, call.apiError("SALES_BATCH_TOO_LARGE", "El lote de ventas es demasiado grande."))
            return@post
        }
        val request = runCatching { call.receive<DeclaredSalesSyncRequest>() }.getOrElse {
            call.respond(HttpStatusCode.BadRequest, call.apiError("SALES_MALFORMED", "No se pudo leer el lote de ventas."))
            return@post
        }
        if (request.sales.isEmpty()) {
            call.respond(HttpStatusCode.BadRequest, call.apiError("SALES_BATCH_EMPTY", "El lote de ventas está vacío."))
            return@post
        }
        if (request.sales.size > MAX_SALES_PER_BATCH) {
            call.respond(HttpStatusCode.PayloadTooLarge, call.apiError("SALES_BATCH_TOO_LARGE", "El lote admite hasta $MAX_SALES_PER_BATCH ventas."))
            return@post
        }
        if (request.sales.map { it.clientSaleId }.toSet().size != request.sales.size) {
            call.respond(HttpStatusCode.BadRequest, call.apiError("SALES_IDS_DUPLICATED", "El lote contiene identificadores de venta repetidos."))
            return@post
        }

        val outcome = store.sync(user.userId, request.sales)
        call.respond(
            DeclaredSalesSyncResponse(
                accepted = outcome.accepted,
                duplicates = outcome.duplicates,
                rejected = outcome.rejected,
                serverTime = outcome.serverTime
            )
        )
    }

    get("/sales/declared") {
        val user = call.requireUser() ?: return@get
        call.respond(
            DeclaredSalesListResponse(
                sales = store.listForUser(user.userId),
                verified = false,
                environment = "DEMO"
            )
        )
    }
}
