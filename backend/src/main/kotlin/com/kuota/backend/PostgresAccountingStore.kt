package com.kuota.backend

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.flywaydb.core.Flyway
import java.sql.Connection
import java.sql.SQLException
import java.time.OffsetDateTime
import java.util.UUID

internal class PostgresAccountingStore(
    database: DatabaseConfig,
    private val json: Json = Json { encodeDefaults = true }
) : AccountingStore {
    private val dataSource = HikariDataSource(HikariConfig().apply {
        jdbcUrl = database.jdbcUrl
        username = database.username
        password = database.password
        maximumPoolSize = database.maxPoolSize
        minimumIdle = 1
        poolName = "kuota-accounting"
        connectionTimeout = 10_000
        validationTimeout = 5_000
        initializationFailTimeout = 10_000
        isAutoCommit = true
    })

    override val backendName: String = "POSTGRES"

    init {
        Flyway.configure()
            .dataSource(dataSource)
            .locations("classpath:db/migration")
            .cleanDisabled(true)
            .load()
            .migrate()
    }

    override fun append(transaction: LedgerTransaction): LedgerAppendResult {
        transaction.requireValid()
        dataSource.connection.use { connection ->
            connection.autoCommit = false
            try {
                insertHeader(connection, transaction)
                insertPostings(connection, transaction)
                connection.commit()
                return LedgerAppendResult.Recorded(transaction.transactionId)
            } catch (error: SQLException) {
                runCatching { connection.rollback() }
                if (error.sqlState == SQLSTATE_UNIQUE_VIOLATION &&
                    error.message.orEmpty().contains("ledger_transaction_source_unique")
                ) {
                    val existingId = findTransactionId(connection, transaction) ?: throw error
                    return LedgerAppendResult.Duplicate(existingId)
                }
                throw error
            } finally {
                runCatching { connection.autoCommit = true }
            }
        }
    }

    override fun transactions(limit: Int): List<LedgerTransaction> {
        require(limit in 1..5_000) { "Ledger transaction limit must be between 1 and 5000." }
        dataSource.connection.use { connection ->
            val headers = mutableListOf<HeaderRow>()
            connection.prepareStatement(
                """
                SELECT id, environment, kind, source_type, source_id, occurred_at, currency, description, metadata
                FROM ledger_transaction
                ORDER BY created_at DESC, id DESC
                LIMIT ?
                """.trimIndent()
            ).use { statement ->
                statement.setInt(1, limit)
                statement.executeQuery().use { rows ->
                    while (rows.next()) {
                        headers += HeaderRow(
                            transactionId = rows.getObject("id", UUID::class.java).toString(),
                            environment = rows.getString("environment"),
                            kind = rows.getString("kind"),
                            sourceType = rows.getString("source_type"),
                            sourceId = rows.getString("source_id"),
                            occurredAt = rows.getTimestamp("occurred_at").toInstant().toString(),
                            currency = rows.getString("currency").trim(),
                            description = rows.getString("description"),
                            metadata = json.decodeFromString<Map<String, String>>(rows.getString("metadata") ?: "{}")
                        )
                    }
                }
            }
            if (headers.isEmpty()) return emptyList()

            val postings = headers.associate { it.transactionId to mutableListOf<LedgerPosting>() }
            val transactionIds = connection.createArrayOf("uuid", headers.map { UUID.fromString(it.transactionId) }.toTypedArray())
            connection.prepareStatement(
                """
                SELECT transaction_id, account, direction, amount_cents
                FROM ledger_posting
                WHERE transaction_id = ANY(?)
                ORDER BY id ASC
                """.trimIndent()
            ).use { statement ->
                statement.setArray(1, transactionIds)
                statement.executeQuery().use { rows ->
                    while (rows.next()) {
                        val transactionId = rows.getObject("transaction_id", UUID::class.java).toString()
                        postings.getValue(transactionId) += LedgerPosting(
                            account = LedgerAccount.valueOf(rows.getString("account")),
                            direction = PostingDirection.valueOf(rows.getString("direction")),
                            amountCents = rows.getLong("amount_cents")
                        )
                    }
                }
            }
            return headers.map { header ->
                LedgerTransaction(
                    transactionId = header.transactionId,
                    environment = header.environment,
                    kind = header.kind,
                    sourceType = header.sourceType,
                    sourceId = header.sourceId,
                    occurredAt = header.occurredAt,
                    currency = header.currency,
                    description = header.description,
                    metadata = header.metadata,
                    postings = postings.getValue(header.transactionId)
                )
            }
        }
    }

    override fun balances(environment: String?): List<AccountBalance> {
        val normalized = environment?.uppercase()
        val sql = buildString {
            append(
                """
                SELECT p.account,
                       SUM(CASE WHEN p.direction = 'DEBIT' THEN p.amount_cents ELSE -p.amount_cents END) AS net
                FROM ledger_posting p
                JOIN ledger_transaction t ON t.id = p.transaction_id
                """.trimIndent()
            )
            if (normalized != null) append("\nWHERE t.environment = ?")
            append("\nGROUP BY p.account\nORDER BY p.account")
        }
        dataSource.connection.use { connection ->
            connection.prepareStatement(sql).use { statement ->
                if (normalized != null) statement.setString(1, normalized)
                statement.executeQuery().use { rows ->
                    val result = mutableListOf<AccountBalance>()
                    while (rows.next()) {
                        result += AccountBalance(
                            account = LedgerAccount.valueOf(rows.getString("account")),
                            netDebitCents = rows.getLong("net")
                        )
                    }
                    return result
                }
            }
        }
    }

    override fun beginWebhook(provider: String, eventId: String, environment: String, payloadSha256: String): WebhookClaim {
        val normalizedProvider = provider.lowercase()
        dataSource.connection.use { connection ->
            val inserted = connection.prepareStatement(
                """
                INSERT INTO webhook_event (provider, event_id, environment, payload_sha256, status, attempts, received_at, updated_at)
                VALUES (?, ?, ?, ?, 'RECEIVED', 1, now(), now())
                ON CONFLICT (provider, event_id) DO NOTHING
                """.trimIndent()
            ).use { statement ->
                statement.setString(1, normalizedProvider)
                statement.setString(2, eventId)
                statement.setString(3, environment.uppercase())
                statement.setString(4, payloadSha256)
                statement.executeUpdate()
            }
            if (inserted == 1) return WebhookClaim.Claimed(normalizedProvider, eventId)

            val status = connection.prepareStatement(
                "SELECT status FROM webhook_event WHERE provider = ? AND event_id = ?"
            ).use { statement ->
                statement.setString(1, normalizedProvider)
                statement.setString(2, eventId)
                statement.executeQuery().use { rows -> if (rows.next()) rows.getString("status") else null }
            }
            if (status == null) return WebhookClaim.Claimed(normalizedProvider, eventId)
            if (status in setOf(WEBHOOK_COMPLETED, WEBHOOK_IGNORED)) return WebhookClaim.Duplicate(status)

            val reclaimed = connection.prepareStatement(
                """
                UPDATE webhook_event
                SET status = 'RECEIVED',
                    environment = ?,
                    payload_sha256 = ?,
                    attempts = attempts + 1,
                    received_at = now(),
                    updated_at = now(),
                    completed_at = NULL,
                    note = NULL
                WHERE provider = ? AND event_id = ?
                  AND (
                      status = 'FAILED'
                      OR (status = 'RECEIVED' AND received_at < now() - interval '10 minutes')
                  )
                """.trimIndent()
            ).use { statement ->
                statement.setString(1, environment.uppercase())
                statement.setString(2, payloadSha256)
                statement.setString(3, normalizedProvider)
                statement.setString(4, eventId)
                statement.executeUpdate()
            }
            return if (reclaimed == 1) {
                WebhookClaim.Claimed(normalizedProvider, eventId)
            } else {
                WebhookClaim.InProgress(normalizedProvider, eventId)
            }
        }
    }

    override fun finishWebhook(provider: String, eventId: String, status: String, note: String?) {
        val normalizedStatus = status.uppercase()
        require(normalizedStatus in setOf(WEBHOOK_COMPLETED, WEBHOOK_FAILED, WEBHOOK_IGNORED)) {
            "Invalid webhook completion status '$status'."
        }
        dataSource.connection.use { connection ->
            connection.prepareStatement(
                """
                UPDATE webhook_event
                SET status = ?,
                    note = ?,
                    updated_at = now(),
                    completed_at = CASE WHEN ? IN ('COMPLETED', 'IGNORED') THEN now() ELSE NULL END
                WHERE provider = ? AND event_id = ?
                """.trimIndent()
            ).use { statement ->
                statement.setString(1, normalizedStatus)
                statement.setString(2, note)
                statement.setString(3, normalizedStatus)
                statement.setString(4, provider.lowercase())
                statement.setString(5, eventId)
                statement.executeUpdate()
            }
        }
    }

    override fun close() {
        dataSource.close()
    }

    private fun insertHeader(connection: Connection, transaction: LedgerTransaction) {
        connection.prepareStatement(
            """
            INSERT INTO ledger_transaction
                (id, environment, kind, source_type, source_id, occurred_at, currency, description, metadata)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?::jsonb)
            """.trimIndent()
        ).use { statement ->
            statement.setObject(1, UUID.fromString(transaction.transactionId))
            statement.setString(2, transaction.environment.uppercase())
            statement.setString(3, transaction.kind)
            statement.setString(4, transaction.sourceType)
            statement.setString(5, transaction.sourceId)
            statement.setObject(6, OffsetDateTime.parse(transaction.occurredAt))
            statement.setString(7, transaction.currency)
            statement.setString(8, transaction.description)
            statement.setString(9, json.encodeToString(transaction.metadata))
            statement.executeUpdate()
        }
    }

    private fun insertPostings(connection: Connection, transaction: LedgerTransaction) {
        connection.prepareStatement(
            "INSERT INTO ledger_posting (transaction_id, account, direction, amount_cents, currency) VALUES (?, ?, ?, ?, ?)"
        ).use { statement ->
            transaction.postings.forEach { posting ->
                statement.setObject(1, UUID.fromString(transaction.transactionId))
                statement.setString(2, posting.account.name)
                statement.setString(3, posting.direction.name)
                statement.setLong(4, posting.amountCents)
                statement.setString(5, transaction.currency)
                statement.addBatch()
            }
            statement.executeBatch()
        }
    }

    private fun findTransactionId(connection: Connection, transaction: LedgerTransaction): String? {
        connection.prepareStatement(
            """
            SELECT id FROM ledger_transaction
            WHERE environment = ? AND kind = ? AND source_type = ? AND source_id = ?
            """.trimIndent()
        ).use { statement ->
            statement.setString(1, transaction.environment.uppercase())
            statement.setString(2, transaction.kind)
            statement.setString(3, transaction.sourceType)
            statement.setString(4, transaction.sourceId)
            statement.executeQuery().use { rows ->
                return if (rows.next()) rows.getObject("id", UUID::class.java).toString() else null
            }
        }
    }

    private data class HeaderRow(
        val transactionId: String,
        val environment: String,
        val kind: String,
        val sourceType: String,
        val sourceId: String,
        val occurredAt: String,
        val currency: String,
        val description: String,
        val metadata: Map<String, String>
    )

    private companion object {
        const val SQLSTATE_UNIQUE_VIOLATION = "23505"
    }
}
