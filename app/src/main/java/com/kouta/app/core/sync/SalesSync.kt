package com.kouta.app.core.sync

import android.content.Context
import com.kouta.app.data.local.AppSettings
import com.kouta.app.data.local.ManualSale
import com.kouta.app.data.local.SalesJournalStore
import com.kouta.app.data.remote.ApiOutcome
import com.kouta.app.data.remote.DeclaredSaleDto
import com.kouta.app.data.remote.DeclaredSalesSyncBody
import com.kouta.app.data.remote.KuotaApiClient
import java.time.Instant

data class SalesBackupReport(
    val sent: Int = 0,
    val pending: Int = 0,
    val rejected: Int = 0,
    val needsSession: Boolean = false,
    val unreachable: Boolean = false,
    val message: String? = null
)

/**
 * Respalda las ventas declaradas en el servidor de Kuota.
 * No verifica cobros ni consulta Yape/Plin: solo copia lo que el usuario registró.
 */
object SalesSync {
    private const val MAX_BATCH = 100

    suspend fun run(context: Context, includeRejected: Boolean = false): SalesBackupReport {
        val store = SalesJournalStore(context)
        val settings = AppSettings(context)
        if (!KuotaApiClient.hasSession(context)) {
            return SalesBackupReport(pending = pendingCount(store), needsSession = true)
        }

        val candidates = runCatching { store.readSalesForBackup(includeRejected) }
            .getOrElse { return SalesBackupReport(message = "No se pudo leer el registro cifrado.") }
        if (candidates.isEmpty()) return SalesBackupReport(pending = 0)

        var sent = 0
        var rejectedCount = 0
        var needsSession = false
        var unreachable = false
        var message: String? = null

        for (batch in candidates.chunked(MAX_BATCH)) {
            val outcome = KuotaApiClient.call(context) {
                syncDeclaredSales(DeclaredSalesSyncBody(batch.map { it.toDto() }))
            }
            when (outcome) {
                is ApiOutcome.Ok -> {
                    val rejectedDtos = outcome.value.rejected.associateBy { it.clientSaleId }
                    val synced = batch.associate { it.id to it.revision }
                        .filterKeys { id -> id !in rejectedDtos }
                    val rejected = batch.mapNotNull { sale ->
                        rejectedDtos[sale.id]?.let { dto ->
                            sale.id to (sale.revision to dto.message.ifBlank { "El servidor rechazó esta venta." })
                        }
                    }.toMap()
                    val syncedAt = runCatching { Instant.parse(outcome.value.serverTime).toEpochMilli() }
                        .getOrDefault(System.currentTimeMillis())
                    runCatching { store.applySalesBackup(synced, rejected, syncedAt) }
                    sent += synced.size
                    rejectedCount += rejected.size
                    settings.lastSalesSyncEpochMillis = syncedAt
                }
                ApiOutcome.SessionExpired -> {
                    needsSession = true
                    break
                }
                is ApiOutcome.Unreachable -> {
                    unreachable = true
                    break
                }
                is ApiOutcome.Rejected -> {
                    message = outcome.message
                    break
                }
            }
        }

        return SalesBackupReport(
            sent = sent,
            pending = pendingCount(store),
            rejected = rejectedCount,
            needsSession = needsSession,
            unreachable = unreachable,
            message = message
        )
    }

    fun pendingCount(context: Context): Int =
        runCatching { SalesJournalStore(context).readSalesForBackup().size }.getOrDefault(0)

    private fun pendingCount(store: SalesJournalStore): Int =
        runCatching { store.readSalesForBackup().size }.getOrDefault(0)

    private fun ManualSale.toDto() = DeclaredSaleDto(
        clientSaleId = id,
        amountCents = amountCents,
        occurredAt = Instant.ofEpochMilli(recordedAtEpochMillis).toString(),
        source = source,
        revision = revision,
        deleted = deletedAtEpochMillis != null
    )
}
