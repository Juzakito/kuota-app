package com.kuota.backend

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.nio.ByteBuffer
import java.nio.channels.FileChannel
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import java.time.Instant
import java.util.UUID

internal const val CURRENCY_PEN = "PEN"
internal const val LEDGER_ENV_DEMO = "DEMO"
internal const val LEDGER_ENV_PRODUCTION = "PRODUCTION"

@Serializable
internal enum class LedgerAccount {
    CASH,
    BUYER_RECEIVABLE,
    SUPPLIER_PAYABLE_IMMEDIATE,
    SUPPLIER_PAYABLE_DEFERRED,
    REVENUE_BUYER_FEE,
    REVENUE_SUPPLIER_FEE,
    PAYMENT_VARIANCE
}

@Serializable
internal enum class PostingDirection { DEBIT, CREDIT }

@Serializable
internal data class LedgerPosting(
    val account: LedgerAccount,
    val direction: PostingDirection,
    @SerialName("amount_cents") val amountCents: Long
)

@Serializable
internal data class LedgerTransaction(
    @SerialName("transaction_id") val transactionId: String,
    val environment: String,
    val kind: String,
    @SerialName("source_type") val sourceType: String,
    @SerialName("source_id") val sourceId: String,
    @SerialName("occurred_at") val occurredAt: String,
    val currency: String,
    val description: String,
    val metadata: Map<String, String> = emptyMap(),
    val postings: List<LedgerPosting>
)

@Serializable
internal data class AccountBalance(
    val account: LedgerAccount,
    @SerialName("net_debit_cents") val netDebitCents: Long
)

internal sealed interface LedgerAppendResult {
    data class Recorded(val transactionId: String) : LedgerAppendResult
    data class Duplicate(val transactionId: String) : LedgerAppendResult
}

internal sealed interface WebhookClaim {
    data class Claimed(val provider: String, val eventId: String) : WebhookClaim
    data class Duplicate(val status: String) : WebhookClaim
    data class InProgress(val provider: String, val eventId: String) : WebhookClaim
}

internal interface AccountingStore : AutoCloseable {
    val backendName: String
    fun append(transaction: LedgerTransaction): LedgerAppendResult
    fun transactions(limit: Int = 100): List<LedgerTransaction>
    fun balances(environment: String? = null): List<AccountBalance>
    fun beginWebhook(provider: String, eventId: String, environment: String, payloadSha256: String): WebhookClaim
    fun finishWebhook(provider: String, eventId: String, status: String, note: String? = null)
    override fun close() = Unit
}

internal fun LedgerTransaction.dedupeKey(): String =
    "${environment.uppercase()}|${kind.uppercase()}|${sourceType.uppercase()}|$sourceId"

internal fun LedgerTransaction.requireValid() {
    require(transactionId.isNotBlank()) { "Ledger transaction ID is blank." }
    require(environment in setOf(LEDGER_ENV_DEMO, LEDGER_ENV_PRODUCTION)) { "Invalid ledger environment '$environment'." }
    require(kind.isNotBlank()) { "Ledger kind is blank." }
    require(sourceType.isNotBlank()) { "Ledger source type is blank." }
    require(sourceId.isNotBlank()) { "Ledger source ID is blank." }
    require(currency.length == 3) { "Ledger currency must be a three-letter code." }
    require(description.isNotBlank()) { "Ledger description is blank." }
    runCatching { Instant.parse(occurredAt) }.getOrElse { throw IllegalArgumentException("Invalid occurred_at '$occurredAt'.", it) }
    require(postings.size >= 2) { "A double-entry transaction needs at least two postings." }
    postings.forEach {
        require(it.amountCents > 0) { "Ledger posting amounts must be positive." }
    }
    val debit = postings.filter { it.direction == PostingDirection.DEBIT }.sumOf { it.amountCents }
    val credit = postings.filter { it.direction == PostingDirection.CREDIT }.sumOf { it.amountCents }
    require(debit == credit) { "Ledger transaction is unbalanced: debit=$debit credit=$credit." }
    require(debit > 0) { "Ledger transaction has no value." }
}

internal object LedgerTransactions {
    fun purchase(
        environment: String,
        purchaseId: String,
        occurredAt: String,
        purchaseAmountCents: Long,
        buyerDownPaymentCents: Long,
        buyerInstallmentsTotalCents: Long,
        buyerFeeCents: Long,
        supplierImmediateCents: Long,
        supplierDeferredNetCents: Long,
        supplierFeeCents: Long,
        supplierId: String,
        currency: String = CURRENCY_PEN
    ): LedgerTransaction {
        require(purchaseAmountCents > 0) { "Purchase amount must be positive." }
        require(buyerDownPaymentCents >= 0) { "Buyer down payment cannot be negative." }
        require(buyerInstallmentsTotalCents >= 0) { "Buyer installment total cannot be negative." }
        require(buyerFeeCents >= 0) { "Buyer fee cannot be negative." }
        require(supplierImmediateCents >= 0) { "Supplier immediate payout cannot be negative." }
        require(supplierDeferredNetCents >= 0) { "Supplier deferred payout cannot be negative." }
        require(supplierFeeCents >= 0) { "Supplier fee cannot be negative." }
        require(buyerDownPaymentCents + buyerInstallmentsTotalCents == purchaseAmountCents + buyerFeeCents) {
            "Buyer receivable must equal purchase amount plus buyer fee."
        }
        require(supplierImmediateCents + supplierDeferredNetCents + supplierFeeCents == purchaseAmountCents) {
            "Supplier payable plus supplier fee must equal the purchase amount."
        }

        return LedgerTransaction(
            transactionId = UUID.randomUUID().toString(),
            environment = environment.uppercase(),
            kind = "PURCHASE_RECOGNITION",
            sourceType = "PURCHASE",
            sourceId = purchaseId,
            occurredAt = occurredAt,
            currency = currency,
            description = "Reconocimiento de compra financiada $purchaseId",
            metadata = mapOf(
                "purchase_id" to purchaseId,
                "supplier_id" to supplierId,
                "purchase_amount_cents" to purchaseAmountCents.toString(),
                "buyer_fee_basis_points" to "500",
                "supplier_fee_basis_points" to "400"
            ),
            postings = listOf(
                LedgerPosting(LedgerAccount.BUYER_RECEIVABLE, PostingDirection.DEBIT, purchaseAmountCents + buyerFeeCents),
                LedgerPosting(LedgerAccount.SUPPLIER_PAYABLE_IMMEDIATE, PostingDirection.CREDIT, supplierImmediateCents),
                LedgerPosting(LedgerAccount.SUPPLIER_PAYABLE_DEFERRED, PostingDirection.CREDIT, supplierDeferredNetCents),
                LedgerPosting(LedgerAccount.REVENUE_BUYER_FEE, PostingDirection.CREDIT, buyerFeeCents),
                LedgerPosting(LedgerAccount.REVENUE_SUPPLIER_FEE, PostingDirection.CREDIT, supplierFeeCents)
            )
        ).also { it.requireValid() }
    }

    fun installmentPaid(
        environment: String,
        internalPaymentId: String,
        provider: String,
        providerPaymentId: String,
        purchaseId: String,
        installmentId: String,
        amountCents: Long,
        occurredAt: String,
        currency: String = CURRENCY_PEN
    ): LedgerTransaction {
        require(amountCents > 0) { "Installment amount must be positive." }
        require(providerPaymentId.isNotBlank()) { "Provider payment reference is required." }
        return LedgerTransaction(
            transactionId = UUID.randomUUID().toString(),
            environment = environment.uppercase(),
            kind = "INSTALLMENT_PAYMENT",
            sourceType = provider.uppercase(),
            sourceId = providerPaymentId,
            occurredAt = occurredAt,
            currency = currency,
            description = "Pago confirmado de cuota $installmentId",
            metadata = mapOf(
                "payment_id" to internalPaymentId,
                "purchase_id" to purchaseId,
                "installment_id" to installmentId,
                "provider" to provider
            ),
            postings = listOf(
                LedgerPosting(LedgerAccount.CASH, PostingDirection.DEBIT, amountCents),
                LedgerPosting(LedgerAccount.BUYER_RECEIVABLE, PostingDirection.CREDIT, amountCents)
            )
        ).also { it.requireValid() }
    }

    fun installmentRefunded(
        environment: String,
        internalPaymentId: String,
        provider: String,
        providerPaymentId: String,
        purchaseId: String,
        installmentId: String,
        amountCents: Long,
        occurredAt: String,
        reason: String,
        currency: String = CURRENCY_PEN
    ): LedgerTransaction {
        require(amountCents > 0) { "Refund amount must be positive." }
        return LedgerTransaction(
            transactionId = UUID.randomUUID().toString(),
            environment = environment.uppercase(),
            kind = if (reason == "CHARGEBACK") "INSTALLMENT_CHARGEBACK" else "INSTALLMENT_REFUND",
            sourceType = provider.uppercase(),
            sourceId = providerPaymentId,
            occurredAt = occurredAt,
            currency = currency,
            description = "Reversión de pago de cuota $installmentId ($reason)",
            metadata = mapOf(
                "payment_id" to internalPaymentId,
                "purchase_id" to purchaseId,
                "installment_id" to installmentId,
                "provider" to provider,
                "reason" to reason
            ),
            postings = listOf(
                LedgerPosting(LedgerAccount.BUYER_RECEIVABLE, PostingDirection.DEBIT, amountCents),
                LedgerPosting(LedgerAccount.CASH, PostingDirection.CREDIT, amountCents)
            )
        ).also { it.requireValid() }
    }

    fun installmentReconciled(
        environment: String,
        internalPaymentId: String,
        provider: String,
        providerPaymentId: String,
        purchaseId: String,
        installmentId: String,
        expectedAmountCents: Long,
        providerAmountCents: Long,
        occurredAt: String,
        currency: String = CURRENCY_PEN
    ): LedgerTransaction {
        require(expectedAmountCents > 0) { "Expected installment amount must be positive." }
        require(providerAmountCents > 0) { "Provider amount must be positive." }
        val variance = providerAmountCents - expectedAmountCents
        val postings = buildList {
            add(LedgerPosting(LedgerAccount.CASH, PostingDirection.DEBIT, providerAmountCents))
            add(LedgerPosting(LedgerAccount.BUYER_RECEIVABLE, PostingDirection.CREDIT, expectedAmountCents))
            if (variance > 0) add(LedgerPosting(LedgerAccount.PAYMENT_VARIANCE, PostingDirection.CREDIT, variance))
            if (variance < 0) add(LedgerPosting(LedgerAccount.PAYMENT_VARIANCE, PostingDirection.DEBIT, -variance))
        }
        return LedgerTransaction(
            transactionId = UUID.randomUUID().toString(),
            environment = environment.uppercase(),
            kind = "INSTALLMENT_RECONCILIATION",
            sourceType = provider.uppercase(),
            sourceId = providerPaymentId,
            occurredAt = occurredAt,
            currency = currency,
            description = "Conciliación manual de la cuota $installmentId",
            metadata = mapOf(
                "payment_id" to internalPaymentId,
                "purchase_id" to purchaseId,
                "installment_id" to installmentId,
                "provider" to provider,
                "expected_amount_cents" to expectedAmountCents.toString(),
                "provider_amount_cents" to providerAmountCents.toString()
            ),
            postings = postings
        ).also { it.requireValid() }
    }
}

internal class FileAccountingStore(
    private val ledgerFile: Path,
    private val webhookFile: Path,
    private val json: Json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        explicitNulls = false
    }
) : AccountingStore {
    private val lock = Any()
    private val ledgerEntries = LinkedHashMap<String, LedgerTransaction>()
    private val webhookEvents = LinkedHashMap<String, WebhookEventLine>()
    private val inProgress = mutableSetOf<String>()

    override val backendName: String = "FILE_NDJSON"

    init {
        ledgerFile.parent?.let { Files.createDirectories(it) }
        webhookFile.parent?.let { Files.createDirectories(it) }
        loadLedger()
        loadWebhookEvents()
    }

    override fun append(transaction: LedgerTransaction): LedgerAppendResult = synchronized(lock) {
        transaction.requireValid()
        val key = transaction.dedupeKey()
        ledgerEntries[key]?.let { existing ->
            val comparable = transaction.copy(
                transactionId = existing.transactionId,
                occurredAt = existing.occurredAt
            )
            check(existing == comparable) { "Conflicting ledger entry for $key; refusing to overwrite financial history." }
            return LedgerAppendResult.Duplicate(existing.transactionId)
        }
        appendLine(ledgerFile, json.encodeToString(transaction))
        ledgerEntries[key] = transaction
        LedgerAppendResult.Recorded(transaction.transactionId)
    }

    override fun transactions(limit: Int): List<LedgerTransaction> = synchronized(lock) {
        require(limit in 1..5_000) { "Ledger transaction limit must be between 1 and 5000." }
        ledgerEntries.values.toList().asReversed().take(limit)
    }

    override fun balances(environment: String?): List<AccountBalance> = synchronized(lock) {
        val normalized = environment?.uppercase()
        val totals = mutableMapOf<LedgerAccount, Long>()
        ledgerEntries.values
            .filter { normalized == null || it.environment == normalized }
            .forEach { transaction ->
                transaction.postings.forEach { posting ->
                    val signed = if (posting.direction == PostingDirection.DEBIT) posting.amountCents else -posting.amountCents
                    totals[posting.account] = Math.addExact(totals[posting.account] ?: 0L, signed)
                }
            }
        totals.entries.sortedBy { it.key.name }.map { AccountBalance(it.key, it.value) }
    }

    override fun beginWebhook(provider: String, eventId: String, environment: String, payloadSha256: String): WebhookClaim = synchronized(lock) {
        val key = webhookKey(provider, eventId)
        if (key in inProgress) return WebhookClaim.InProgress(provider, eventId)
        val existing = webhookEvents[key]
        if (existing != null && existing.status in setOf(WEBHOOK_COMPLETED, WEBHOOK_IGNORED)) {
            return WebhookClaim.Duplicate(existing.status)
        }
        val now = Instant.now().toString()
        val line = WebhookEventLine(
            provider = provider,
            eventId = eventId,
            environment = environment.uppercase(),
            payloadSha256 = payloadSha256,
            receivedAt = now,
            updatedAt = now,
            status = WEBHOOK_RECEIVED,
            note = null
        )
        appendLine(webhookFile, json.encodeToString(line))
        webhookEvents[key] = line
        inProgress.add(key)
        WebhookClaim.Claimed(provider, eventId)
    }

    override fun finishWebhook(provider: String, eventId: String, status: String, note: String?) {
        synchronized(lock) {
            val key = webhookKey(provider, eventId)
            val existing = webhookEvents[key]
            val line = WebhookEventLine(
                provider = provider,
                eventId = eventId,
                environment = existing?.environment ?: LEDGER_ENV_DEMO,
                payloadSha256 = existing?.payloadSha256 ?: "",
                receivedAt = existing?.receivedAt ?: Instant.now().toString(),
                updatedAt = Instant.now().toString(),
                status = status.uppercase(),
                note = note
            )
            appendLine(webhookFile, json.encodeToString(line))
            webhookEvents[key] = line
            inProgress.remove(key)
        }
    }

    private fun loadLedger() {
        if (!Files.exists(ledgerFile)) return
        Files.readAllLines(ledgerFile, StandardCharsets.UTF_8).forEachIndexed { index, line ->
            if (line.isBlank()) return@forEachIndexed
            val transaction = runCatching { json.decodeFromString<LedgerTransaction>(line) }
                .getOrElse { throw IllegalStateException("Corrupt ledger record at ${ledgerFile.toAbsolutePath()}:${index + 1}", it) }
            transaction.requireValid()
            val key = transaction.dedupeKey()
            check(ledgerEntries.putIfAbsent(key, transaction) == null) {
                "Duplicate ledger key $key at ${ledgerFile.toAbsolutePath()}:${index + 1}"
            }
        }
    }

    private fun loadWebhookEvents() {
        if (!Files.exists(webhookFile)) return
        Files.readAllLines(webhookFile, StandardCharsets.UTF_8).forEachIndexed { index, line ->
            if (line.isBlank()) return@forEachIndexed
            val event = runCatching { json.decodeFromString<WebhookEventLine>(line) }
                .getOrElse { throw IllegalStateException("Corrupt webhook record at ${webhookFile.toAbsolutePath()}:${index + 1}", it) }
            webhookEvents[webhookKey(event.provider, event.eventId)] = event
        }
    }

    private fun appendLine(path: Path, line: String) {
        val bytes = (line + "\n").toByteArray(StandardCharsets.UTF_8)
        FileChannel.open(
            path,
            StandardOpenOption.CREATE,
            StandardOpenOption.WRITE,
            StandardOpenOption.APPEND
        ).use { channel ->
            var buffer = ByteBuffer.wrap(bytes)
            while (buffer.hasRemaining()) channel.write(buffer)
            channel.force(true)
        }
    }

    private fun webhookKey(provider: String, eventId: String): String = "${provider.lowercase()}|$eventId"
}

@Serializable
private data class WebhookEventLine(
    val provider: String,
    @SerialName("event_id") val eventId: String,
    val environment: String,
    @SerialName("payload_sha256") val payloadSha256: String,
    @SerialName("received_at") val receivedAt: String,
    @SerialName("updated_at") val updatedAt: String,
    val status: String,
    val note: String? = null
)

internal const val WEBHOOK_RECEIVED = "RECEIVED"
internal const val WEBHOOK_COMPLETED = "COMPLETED"
internal const val WEBHOOK_IGNORED = "IGNORED"
internal const val WEBHOOK_FAILED = "FAILED"
