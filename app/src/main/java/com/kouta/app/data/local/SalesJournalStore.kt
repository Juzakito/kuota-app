package com.kouta.app.data.local

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.AtomicFile
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

@Serializable
data class ManualSale(
    val id: String,
    val amountCents: Long,
    val recordedAtEpochMillis: Long,
    val source: String = SaleSource.MANUAL,
    /** Sube en cada corrección o eliminación para que el servidor resuelva la última versión. */
    val revision: Int = 0,
    val syncStatus: String = SaleSyncStatus.LOCAL_ONLY,
    val syncedAtEpochMillis: Long? = null,
    val syncError: String? = null,
    /** Tombstone: la venta sigue en el archivo cifrado hasta que el servidor confirma el borrado. */
    val deletedAtEpochMillis: Long? = null
)

object SaleSource {
    const val MANUAL = "MANUAL"
}

object SaleSyncStatus {
    const val LOCAL_ONLY = "SOLO_LOCAL"
    const val PENDING = "PENDIENTE"
    const val SYNCED = "RESPALDADA"
    const val REJECTED = "RECHAZADA"
}

object ClaimSyncStatus {
    /** Hojas creadas antes de v1.2.0: solo tipo y detalle, no se envían al servidor. */
    const val LOCAL_ONLY = "SOLO_LOCAL"
    const val PENDING = "PENDIENTE"
    const val SENT = "ENVIADO"
    const val REJECTED = "RECHAZADO"
}

@Serializable
data class LocalClaimRecord(
    val claimId: String,
    val type: String, // "Reclamo" or "Queja"
    val detail: String,
    val createdAtEpochMillis: Long,
    val status: String = "REGISTRADO_LOCAL",
    val helpTopic: String? = null,
    val consumerName: String = "",
    val documentType: String = "DNI",
    val documentNumber: String = "",
    val address: String = "",
    val phone: String = "",
    val email: String = "",
    val goodType: String = "SERVICIO",
    val goodDescription: String = "",
    val amountClaimedCents: Long? = null,
    val operationReference: String? = null,
    val consumerRequest: String = "",
    val syncStatus: String = ClaimSyncStatus.LOCAL_ONLY,
    val syncError: String? = null,
    val officialNumber: String? = null,
    val responseDueDate: String? = null,
    val estimatedDueDate: String? = null,
    val channels: List<String> = emptyList(),
    val responseText: String? = null
)

@Serializable
data class LocalNotificationSettings(
    val installmentRemindersEnabled: Boolean = true,
    val newsAndPromotionsEnabled: Boolean = false,
    val supportUpdatesEnabled: Boolean = true,
    val lockScreenDetail: Boolean = false
)

@Serializable
data class LocalNotificationItem(
    val id: String,
    val type: String,
    val title: String,
    val body: String,
    val deepLink: String? = null,
    val amountCents: Long? = null,
    val receivedAtEpochMillis: Long,
    val read: Boolean = false
)

@Serializable
data class LocalBusinessProfile(
    val ownerName: String = "",
    val businessName: String = "",
    val email: String,
    val phone: String,
    val ruc: String,
    val preferredPaymentMethod: String = "Yape / Plin",
    val notificationSettings: LocalNotificationSettings = LocalNotificationSettings(),
    val claims: List<LocalClaimRecord> = emptyList()
)

@Serializable
data class OnboardingDraft(
    val step: Int,
    val isReturningUser: Boolean,
    val ownerName: String = "",
    val email: String,
    val phone: String,
    val ruc: String
)

@Serializable
private data class SalesJournalSnapshot(
    val sales: List<ManualSale> = emptyList(),
    val businessProfile: LocalBusinessProfile? = null,
    val onboardingDraft: OnboardingDraft? = null,
    val notifications: List<LocalNotificationItem> = emptyList()
)

/** Stores user-entered sales locally, encrypted with a key protected by Android Keystore. */
class SalesJournalStore(context: Context) {
    private val file = AtomicFile(File(context.applicationContext.filesDir, FILE_NAME))

    fun readAll(): List<ManualSale> = synchronized(LOCK) {
        readSnapshot().sales.filter { it.deletedAtEpochMillis == null }.sortedByDescending { it.recordedAtEpochMillis }
    }

    /** Ventas e tombtones que aún no confirma el servidor, en orden de registro. */
    fun readSalesForBackup(includeRejected: Boolean = false): List<ManualSale> = synchronized(LOCK) {
        readSnapshot().sales
            .filter { it.syncStatus != SaleSyncStatus.SYNCED }
            .filter { includeRejected || it.syncStatus != SaleSyncStatus.REJECTED }
            .sortedBy { it.recordedAtEpochMillis }
    }

    fun markLocalSalesPending(): List<ManualSale> = synchronized(LOCK) {
        val snapshot = readSnapshot()
        if (snapshot.sales.none { it.syncStatus == SaleSyncStatus.LOCAL_ONLY }) return@synchronized visibleSales(snapshot)
        val updated = snapshot.sales.map {
            if (it.syncStatus == SaleSyncStatus.LOCAL_ONLY) it.copy(syncStatus = SaleSyncStatus.PENDING) else it
        }
        writeSnapshot(snapshot.copy(sales = updated))
        visibleSales(snapshot.copy(sales = updated))
    }

    fun readBusinessProfile(): LocalBusinessProfile? = synchronized(LOCK) { readSnapshot().businessProfile }

    fun readOnboardingDraft(): OnboardingDraft? = synchronized(LOCK) { readSnapshot().onboardingDraft }

    fun saveOnboardingDraft(draft: OnboardingDraft) = synchronized(LOCK) {
        writeSnapshot(readSnapshot().copy(onboardingDraft = draft))
    }

    fun saveBusinessProfile(profile: LocalBusinessProfile) = synchronized(LOCK) {
        writeSnapshot(readSnapshot().copy(businessProfile = profile, onboardingDraft = null))
    }

    fun addClaim(claim: LocalClaimRecord): LocalBusinessProfile? = updateProfile { profile ->
        profile.copy(claims = (profile.claims + claim).sortedByDescending { it.createdAtEpochMillis })
    }

    fun updateClaim(claimId: String, transform: (LocalClaimRecord) -> LocalClaimRecord): LocalBusinessProfile? =
        updateProfile { profile -> profile.copy(claims = profile.claims.map { if (it.claimId == claimId) transform(it) else it }) }

    fun updateNotificationSettings(settings: LocalNotificationSettings): LocalBusinessProfile? =
        updateProfile { it.copy(notificationSettings = settings) }

    fun updatePreferredPaymentMethod(method: String): LocalBusinessProfile? =
        updateProfile { it.copy(preferredPaymentMethod = method) }

    fun clearBusinessProfileAndDraft() = synchronized(LOCK) {
        writeSnapshot(readSnapshot().copy(businessProfile = null, onboardingDraft = null, notifications = emptyList()))
    }

    fun readNotifications(): List<LocalNotificationItem> = synchronized(LOCK) {
        readSnapshot().notifications.sortedByDescending { it.receivedAtEpochMillis }
    }

    fun addNotification(item: LocalNotificationItem): List<LocalNotificationItem> = synchronized(LOCK) {
        val snapshot = readSnapshot()
        val updated = (snapshot.notifications.filterNot { it.id == item.id } + item)
            .sortedByDescending { it.receivedAtEpochMillis }
            .take(MAX_NOTIFICATIONS)
        writeSnapshot(snapshot.copy(notifications = updated))
        updated
    }

    fun markNotificationsRead(ids: Set<String>? = null): List<LocalNotificationItem> = synchronized(LOCK) {
        val snapshot = readSnapshot()
        val updated = snapshot.notifications.map { if (ids == null || it.id in ids) it.copy(read = true) else it }
        writeSnapshot(snapshot.copy(notifications = updated))
        updated.sortedByDescending { it.receivedAtEpochMillis }
    }

    fun saveSale(existing: ManualSale?, amountCents: Long, now: Long = System.currentTimeMillis()): List<ManualSale> = synchronized(LOCK) {
        val snapshot = readSnapshot()
        val current = existing?.let { candidate -> snapshot.sales.firstOrNull { it.id == candidate.id } }
        val updated = if (current == null) {
            snapshot.sales + ManualSale(
                id = existing?.id ?: java.util.UUID.randomUUID().toString(),
                amountCents = amountCents,
                recordedAtEpochMillis = now
            )
        } else {
            snapshot.sales.map {
                if (it.id == current.id) it.copy(
                    amountCents = amountCents,
                    revision = it.revision + 1,
                    syncStatus = SaleSyncStatus.PENDING,
                    syncedAtEpochMillis = null,
                    syncError = null
                ) else it
            }
        }
        writeSnapshot(snapshot.copy(sales = updated.sortedByDescending { it.recordedAtEpochMillis }))
        updated.filter { it.deletedAtEpochMillis == null }.sortedByDescending { it.recordedAtEpochMillis }
    }

    fun delete(saleId: String, now: Long = System.currentTimeMillis()): List<ManualSale> = synchronized(LOCK) {
        val snapshot = readSnapshot()
        val target = snapshot.sales.firstOrNull { it.id == saleId } ?: return@synchronized visibleSales(snapshot)
        val updated = if (target.syncedAtEpochMillis == null) {
            snapshot.sales.filterNot { it.id == saleId }
        } else {
            snapshot.sales.map {
                if (it.id == saleId) it.copy(
                    deletedAtEpochMillis = now,
                    revision = it.revision + 1,
                    syncStatus = SaleSyncStatus.PENDING,
                    syncedAtEpochMillis = null,
                    syncError = null
                ) else it
            }
        }
        writeSnapshot(snapshot.copy(sales = updated))
        visibleSales(snapshot.copy(sales = updated))
    }

    /**
     * Aplica la respuesta del servidor en una sola escritura; los tombtones confirmados se descartan.
     * Las revisiones evitan marcar como respaldada una corrección hecha mientras el envío estaba en curso.
     */
    fun applySalesBackup(
        syncedRevisions: Map<String, Int>,
        rejected: Map<String, Pair<Int, String>>,
        syncedAtEpochMillis: Long
    ): List<ManualSale> = synchronized(LOCK) {
        val snapshot = readSnapshot()
        var changed = false
        val updated = snapshot.sales.mapNotNull { sale ->
            val syncedRevision = syncedRevisions[sale.id]
            val rejectedEntry = rejected[sale.id]
            when {
                syncedRevision != null && sale.revision == syncedRevision -> {
                    changed = true
                    if (sale.deletedAtEpochMillis != null) null
                    else sale.copy(syncStatus = SaleSyncStatus.SYNCED, syncedAtEpochMillis = syncedAtEpochMillis, syncError = null)
                }
                rejectedEntry != null && sale.revision == rejectedEntry.first -> {
                    changed = true
                    sale.copy(syncStatus = SaleSyncStatus.REJECTED, syncedAtEpochMillis = null, syncError = rejectedEntry.second)
                }
                else -> sale
            }
        }
        if (changed) writeSnapshot(snapshot.copy(sales = updated))
        visibleSales(snapshot.copy(sales = updated))
    }

    private fun visibleSales(snapshot: SalesJournalSnapshot): List<ManualSale> =
        snapshot.sales.filter { it.deletedAtEpochMillis == null }.sortedByDescending { it.recordedAtEpochMillis }

    private fun updateProfile(transform: (LocalBusinessProfile) -> LocalBusinessProfile): LocalBusinessProfile? = synchronized(LOCK) {
        val snapshot = readSnapshot()
        val profile = snapshot.businessProfile ?: return null
        val updatedProfile = transform(profile)
        writeSnapshot(snapshot.copy(businessProfile = updatedProfile))
        updatedProfile
    }

    private fun readSnapshot(): SalesJournalSnapshot {
        if (!file.baseFile.exists() && !File(file.baseFile.path + ".bak").exists()) return SalesJournalSnapshot()
        val encrypted = file.openRead().use { it.readBytes() }
        val ivSize = 12
        require(encrypted.size > ivSize) { "El registro local está incompleto." }
        val iv = encrypted.copyOfRange(0, ivSize)
        val ciphertext = encrypted.copyOfRange(ivSize, encrypted.size)
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.DECRYPT_MODE, getOrCreateKey(), GCMParameterSpec(TAG_SIZE_BITS, iv))
        val json = cipher.doFinal(ciphertext).toString(Charsets.UTF_8)
        return JSON.decodeFromString<SalesJournalSnapshot>(json)
    }

    private fun writeSnapshot(snapshot: SalesJournalSnapshot) {
        val plain = JSON.encodeToString(snapshot).toByteArray(Charsets.UTF_8)
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, getOrCreateKey())
        val encrypted = cipher.iv + cipher.doFinal(plain)
        var stream: java.io.FileOutputStream? = null
        try {
            stream = file.startWrite()
            stream.write(encrypted)
            file.finishWrite(stream)
        } catch (failure: Exception) {
            stream?.let(file::failWrite)
            throw failure
        }
    }

    private fun getOrCreateKey(): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEY_STORE).apply { load(null) }
        (keyStore.getKey(KEY_ALIAS, null) as? SecretKey)?.let { return it }
        val generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEY_STORE)
        generator.init(
            KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .build()
        )
        return generator.generateKey()
    }

    private companion object {
        const val FILE_NAME = "kuota_manual_sales.enc"
        const val KEY_ALIAS = "kuota_manual_sales_v1"
        const val ANDROID_KEY_STORE = "AndroidKeyStore"
        const val TRANSFORMATION = "AES/GCM/NoPadding"
        const val TAG_SIZE_BITS = 128
        const val MAX_NOTIFICATIONS = 30
        // Una sola instancia de archivo por proceso: pantallas, worker y servicio push comparten el lock.
        val LOCK = Any()
        val JSON = Json { ignoreUnknownKeys = true }
    }
}
