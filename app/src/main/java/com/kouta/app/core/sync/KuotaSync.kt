package com.kouta.app.core.sync

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.kouta.app.core.push.KuotaEvents
import com.kouta.app.core.push.PushRegistrar
import com.kouta.app.core.push.PushStatus
import com.kouta.app.data.local.AppSettings
import com.kouta.app.data.local.ClaimSyncStatus
import com.kouta.app.data.local.LocalClaimRecord
import com.kouta.app.data.local.SalesJournalStore
import com.kouta.app.data.remote.ApiOutcome
import com.kouta.app.data.remote.ClaimReceiptDto
import com.kouta.app.data.remote.ClaimSubmissionBody
import com.kouta.app.data.remote.KuotaApiClient
import java.time.Instant
import java.util.concurrent.TimeUnit

data class SyncReport(
    val sentClaims: Int = 0,
    val pendingClaims: Int = 0,
    val needsSession: Boolean = false,
    val unreachable: Boolean = false,
    val push: PushStatus? = null,
    val sales: SalesBackupReport = SalesBackupReport()
)

object KuotaSync {
    private const val ONE_TIME = "kuota-sync"
    private const val PERIODIC = "kuota-sync-periodic"

    private val networkConstraint = Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()

    fun enqueue(context: Context) {
        val request = OneTimeWorkRequestBuilder<KuotaSyncWorker>()
            .setConstraints(networkConstraint)
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
            .build()
        WorkManager.getInstance(context).enqueueUniqueWork(ONE_TIME, ExistingWorkPolicy.APPEND_OR_REPLACE, request)
    }

    /** Respaldo cada 15 min: reintenta reclamos pendientes y, sin Firebase, consulta la bandeja del servidor. */
    fun schedulePeriodic(context: Context) {
        val request = PeriodicWorkRequestBuilder<KuotaSyncWorker>(15, TimeUnit.MINUTES)
            .setConstraints(networkConstraint)
            .build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(PERIODIC, ExistingPeriodicWorkPolicy.KEEP, request)
    }

    suspend fun runOnce(context: Context, forceSalesBackup: Boolean = false): SyncReport {
        val settings = AppSettings(context)
        if (!KuotaApiClient.hasSession(context)) {
            val pending = pendingClaims(context).size
            return SyncReport(
                pendingClaims = pending,
                needsSession = true,
                push = PushStatus.NoSession,
                sales = SalesBackupReport(pending = SalesSync.pendingCount(context), needsSession = true)
            )
        }
        val store = SalesJournalStore(context)
        var sent = 0
        var unreachable = false
        var needsSession = false

        for (claim in pendingClaims(context)) {
            when (val outcome = KuotaApiClient.call(context) { submitClaim(claim.claimId, claim.toSubmission()) }) {
                is ApiOutcome.Ok -> {
                    store.updateClaim(claim.claimId) { it.applyReceipt(outcome.value) }
                    sent++
                }
                is ApiOutcome.Rejected -> if (outcome.httpCode in 400..499) {
                    store.updateClaim(claim.claimId) {
                        it.copy(syncStatus = ClaimSyncStatus.REJECTED, syncError = outcome.message)
                    }
                } else {
                    unreachable = true
                }
                ApiOutcome.SessionExpired -> needsSession = true
                is ApiOutcome.Unreachable -> unreachable = true
            }
            if (needsSession || unreachable) break
        }

        val hasSentClaims = runCatching { store.readBusinessProfile()?.claims?.any { it.syncStatus == ClaimSyncStatus.SENT } }
            .getOrNull() == true
        if (!needsSession && !unreachable && hasSentClaims) {
            when (val outcome = KuotaApiClient.call(context) { listClaims() }) {
                is ApiOutcome.Ok -> outcome.value.claims.forEach { receipt ->
                    store.updateClaim(receipt.clientClaimId) { it.applyReceipt(receipt) }
                }
                ApiOutcome.SessionExpired -> needsSession = true
                is ApiOutcome.Unreachable -> unreachable = true
                is ApiOutcome.Rejected -> Unit
            }
        }

        val sales = if (needsSession || unreachable) {
            SalesBackupReport(
                pending = SalesSync.pendingCount(context),
                needsSession = needsSession,
                unreachable = unreachable
            )
        } else if (forceSalesBackup || settings.salesAutoSync) {
            SalesSync.run(context, includeRejected = forceSalesBackup)
        } else {
            SalesBackupReport(pending = SalesSync.pendingCount(context))
        }
        if (sales.needsSession) needsSession = true
        if (sales.unreachable) unreachable = true

        val push = if (needsSession) PushStatus.NoSession else PushRegistrar.register(context)
        if (!needsSession && !PushRegistrar.pollInbox(context)) unreachable = true

        KuotaEvents.bump()
        return SyncReport(
            sentClaims = sent,
            pendingClaims = pendingClaims(context).size,
            needsSession = needsSession,
            unreachable = unreachable,
            push = push,
            sales = sales
        )
    }

    private fun pendingClaims(context: Context): List<LocalClaimRecord> =
        runCatching { SalesJournalStore(context).readBusinessProfile()?.claims.orEmpty() }.getOrDefault(emptyList())
            .filter { it.syncStatus == ClaimSyncStatus.PENDING }
            .sortedBy { it.createdAtEpochMillis }

    private fun LocalClaimRecord.toSubmission() = ClaimSubmissionBody(
        clientClaimId = claimId,
        type = type.uppercase(),
        helpTopic = helpTopic,
        consumerName = consumerName,
        documentType = documentType,
        documentNumber = documentNumber,
        address = address,
        phone = phone,
        email = email,
        goodType = goodType,
        goodDescription = goodDescription,
        amountClaimedCents = amountClaimedCents,
        operationReference = operationReference,
        detail = detail,
        consumerRequest = consumerRequest,
        createdAtDevice = Instant.ofEpochMilli(createdAtEpochMillis).toString()
    )

    private fun LocalClaimRecord.applyReceipt(receipt: ClaimReceiptDto) = copy(
        syncStatus = ClaimSyncStatus.SENT,
        syncError = null,
        officialNumber = receipt.claimNumber,
        responseDueDate = receipt.responseDueDate,
        status = receipt.status,
        channels = receipt.channels.map { "${it.channel}:${it.status}" },
        responseText = receipt.responseText
    )
}

class KuotaSyncWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val report = runCatching { KuotaSync.runOnce(applicationContext) }.getOrElse { return Result.retry() }
        val pending = report.pendingClaims + report.sales.pending
        return if (report.unreachable && pending > 0 && runAttemptCount < 8) Result.retry() else Result.success()
    }
}
