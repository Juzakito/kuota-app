package com.kouta.app.core.security

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.AtomicFile
import android.util.Base64
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.security.KeyStore
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec

@Serializable
data class PersistedPinState(
    val saltBase64: String = "",
    val verifierBase64: String = "",
    val failedAttempts: Int = 0,
    val lockoutUntilEpochMillis: Long = 0L,
    val isLocked: Boolean = true,
    val biometricEnabled: Boolean = false,
    val lastBackgroundEpochMillis: Long = 0L,
    val updatedAtEpochMillis: Long = 0L
) {
    val isConfigured: Boolean
        get() = saltBase64.isNotBlank() && verifierBase64.isNotBlank()
}

sealed interface PinVerifyResult {
    data object Success : PinVerifyResult
    data class InvalidPin(
        val failedAttempts: Int,
        val attemptsBeforeLockout: Int,
        val lockoutDurationSeconds: Long
    ) : PinVerifyResult
    data class LockedOut(
        val failedAttempts: Int,
        val remainingSeconds: Long
    ) : PinVerifyResult
}

/**
 * Gestor criptográfico del bloqueo por PIN de 6 dígitos de KUOTA.
 *
 * - Nunca almacena el PIN en texto plano ni reversible.
 * - Deriva un verificador de 256 bits mediante PBKDF2WithHmacSHA256 (65,536 iteraciones) con sal aleatoria de 16 bytes.
 * - Compara el verificador en tiempo constante ([MessageDigest.isEqual]).
 * - Cifra todo el archivo de estado (verificador, intentos fallidos, timestamp de bloqueo y bandera `isLocked`)
 *   con AES-256-GCM respaldado por Android Keystore, evitando que cerrar o matar el proceso permita
 *   saltarse el bloqueo o reiniciar el contador de intentos fallidos.
 */
object PinSecurityManager {
    private const val FILE_NAME = "kuota_pin_security_state.bin"
    private const val KEY_ALIAS = "kuota_pin_security_keystore_alias"
    private const val ANDROID_KEY_STORE = "AndroidKeyStore"
    private const val TRANSFORMATION = "AES/GCM/NoPadding"
    private const val PBKDF2_ALGORITHM = "PBKDF2WithHmacSHA256"
    internal const val PBKDF2_ITERATIONS = 65_536
    internal const val PBKDF2_KEY_BITS = 256
    internal const val SALT_BYTES = 16
    internal const val PIN_LENGTH = 6
    internal const val FREE_ATTEMPTS_BEFORE_COOLDOWN = 3

    private val LOCK = Any()
    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }
    private val secureRandom = SecureRandom()

    private val _stateFlow = MutableStateFlow(PersistedPinState())
    val stateFlow: StateFlow<PersistedPinState> = _stateFlow.asStateFlow()

    @Volatile
    private var initialized = false

    fun initialize(context: Context): PersistedPinState = synchronized(LOCK) {
        val loaded = readStateInternal(context.applicationContext)
        // Al iniciar un proceso nuevo (cold start o tras process death), si existe un PIN configurado,
        // forzamos que inicie bloqueado para que nunca se pueda saltar la autenticación.
        val enforced = if (loaded.isConfigured && !initialized) {
            val lockedState = loaded.copy(isLocked = true)
            writeStateInternal(context.applicationContext, lockedState)
            lockedState
        } else {
            loaded
        }
        initialized = true
        _stateFlow.value = enforced
        enforced
    }

    fun currentState(context: Context): PersistedPinState = synchronized(LOCK) {
        if (!initialized) initialize(context) else _stateFlow.value
    }

    fun isPinConfigured(context: Context): Boolean = currentState(context).isConfigured

    fun isLocked(context: Context): Boolean {
        val state = currentState(context)
        return state.isConfigured && state.isLocked
    }

    /**
     * Fuerza el bloqueo de la aplicación inmediatamente (por inactividad, solicitud de usuario o reautenticación).
     */
    fun lock(context: Context) = synchronized(LOCK) {
        val current = currentState(context)
        if (!current.isConfigured || current.isLocked) return@synchronized
        val now = System.currentTimeMillis()
        val lockedState = current.copy(
            isLocked = true,
            updatedAtEpochMillis = now
        )
        writeStateInternal(context.applicationContext, lockedState)
        _stateFlow.value = lockedState
    }

    fun isValidPinFormat(pin: String): Boolean =
        pin.length == PIN_LENGTH && pin.all { it in '0'..'9' }

    /**
     * Se invoca cuando la aplicación pasa a segundo plano (ON_STOP sin cambio de configuración).
     * Persiste inmediatamente `isLocked = true` en almacenamiento cifrado.
     */
    fun onAppBackgrounded(context: Context) = synchronized(LOCK) {
        val current = currentState(context)
        if (!current.isConfigured) return@synchronized
        val now = System.currentTimeMillis()
        val updated = current.copy(
            isLocked = true,
            lastBackgroundEpochMillis = now,
            updatedAtEpochMillis = now
        )
        writeStateInternal(context.applicationContext, updated)
        _stateFlow.value = updated
    }

    /**
     * Se invoca al regresar a primer plano (ON_START).
     * Recarga el estado persistido para asegurar coherencia tras recreación del proceso.
     */
    fun onAppForegrounded(context: Context): PersistedPinState = synchronized(LOCK) {
        val loaded = readStateInternal(context.applicationContext)
        _stateFlow.value = loaded
        loaded
    }

    /**
     * Configura o actualiza el PIN de 6 dígitos usando sal criptográfica aleatoria y PBKDF2-HMAC-SHA256.
     */
    fun setupPin(context: Context, rawPin: String): Boolean = synchronized(LOCK) {
        if (!isValidPinFormat(rawPin)) return@synchronized false
        val salt = ByteArray(SALT_BYTES).also { secureRandom.nextBytes(it) }
        val verifier = deriveKeyPbkdf2(rawPin, salt)
        val now = System.currentTimeMillis()
        val current = readStateInternal(context.applicationContext)
        val newState = current.copy(
            saltBase64 = Base64.encodeToString(salt, Base64.NO_WRAP),
            verifierBase64 = Base64.encodeToString(verifier, Base64.NO_WRAP),
            failedAttempts = 0,
            lockoutUntilEpochMillis = 0L,
            isLocked = false,
            updatedAtEpochMillis = now
        )
        writeStateInternal(context.applicationContext, newState)
        initialized = true
        _stateFlow.value = newState
        true
    }

    /**
     * Verifica el PIN de 6 dígitos con comparación en tiempo constante y aplica bloqueo progresivo
     * ante intentos fallidos repetidos.
     */
    fun verifyPin(
        context: Context,
        candidatePin: String,
        nowEpochMillis: Long = System.currentTimeMillis()
    ): PinVerifyResult = synchronized(LOCK) {
        val current = currentState(context)
        if (!current.isConfigured) return@synchronized PinVerifyResult.Success

        if (current.lockoutUntilEpochMillis > nowEpochMillis) {
            val remainingSec = ((current.lockoutUntilEpochMillis - nowEpochMillis + 999L) / 1000L).coerceAtLeast(1L)
            return@synchronized PinVerifyResult.LockedOut(
                failedAttempts = current.failedAttempts,
                remainingSeconds = remainingSec
            )
        }

        if (!isValidPinFormat(candidatePin)) {
            return@synchronized registerFailedAttempt(context.applicationContext, current, nowEpochMillis)
        }

        val salt = runCatching { Base64.decode(current.saltBase64, Base64.NO_WRAP) }.getOrNull()
        val expectedVerifier = runCatching { Base64.decode(current.verifierBase64, Base64.NO_WRAP) }.getOrNull()
        if (salt == null || expectedVerifier == null || salt.isEmpty() || expectedVerifier.isEmpty()) {
            return@synchronized registerFailedAttempt(context.applicationContext, current, nowEpochMillis)
        }

        val candidateVerifier = deriveKeyPbkdf2(candidatePin, salt)
        val matches = constantTimeEquals(expectedVerifier, candidateVerifier)
        // Limpiamos el arreglo en memoria
        candidateVerifier.fill(0)

        if (matches) {
            val unlocked = current.copy(
                failedAttempts = 0,
                lockoutUntilEpochMillis = 0L,
                isLocked = false,
                updatedAtEpochMillis = nowEpochMillis
            )
            writeStateInternal(context.applicationContext, unlocked)
            _stateFlow.value = unlocked
            PinVerifyResult.Success
        } else {
            registerFailedAttempt(context.applicationContext, current, nowEpochMillis)
        }
    }

    fun onBiometricAuthenticated(context: Context) = synchronized(LOCK) {
        val current = currentState(context)
        if (!current.isConfigured) return@synchronized
        val now = System.currentTimeMillis()
        if (current.lockoutUntilEpochMillis > now) return@synchronized
        val unlocked = current.copy(
            failedAttempts = 0,
            lockoutUntilEpochMillis = 0L,
            isLocked = false,
            updatedAtEpochMillis = now
        )
        writeStateInternal(context.applicationContext, unlocked)
        _stateFlow.value = unlocked
    }

    fun setBiometricEnabled(context: Context, enabled: Boolean) = synchronized(LOCK) {
        val current = currentState(context)
        val updated = current.copy(
            biometricEnabled = enabled,
            updatedAtEpochMillis = System.currentTimeMillis()
        )
        writeStateInternal(context.applicationContext, updated)
        _stateFlow.value = updated
    }

    fun clearPin(context: Context) = synchronized(LOCK) {
        val cleared = PersistedPinState(isLocked = false)
        val file = AtomicFile(File(context.applicationContext.filesDir, FILE_NAME))
        runCatching { file.delete() }
        _stateFlow.value = cleared
    }

    private fun registerFailedAttempt(
        appContext: Context,
        current: PersistedPinState,
        nowEpochMillis: Long
    ): PinVerifyResult {
        val newAttempts = current.failedAttempts + 1
        val cooldownMillis = computeCooldownMillis(newAttempts)
        val lockoutUntil = if (cooldownMillis > 0L) nowEpochMillis + cooldownMillis else 0L
        val updated = current.copy(
            failedAttempts = newAttempts,
            lockoutUntilEpochMillis = lockoutUntil,
            isLocked = true,
            updatedAtEpochMillis = nowEpochMillis
        )
        writeStateInternal(appContext, updated)
        _stateFlow.value = updated

        return if (cooldownMillis > 0L) {
            PinVerifyResult.LockedOut(
                failedAttempts = newAttempts,
                remainingSeconds = cooldownMillis / 1000L
            )
        } else {
            PinVerifyResult.InvalidPin(
                failedAttempts = newAttempts,
                attemptsBeforeLockout = (FREE_ATTEMPTS_BEFORE_COOLDOWN - newAttempts).coerceAtLeast(0),
                lockoutDurationSeconds = 0L
            )
        }
    }

    /**
     * Tiempos de espera progresivos ante intentos fallidos repetidos:
     * - Intentos 1 y 2: sin espera (permite reintentar de inmediato)
     * - Intento 3: 30 segundos
     * - Intento 4: 60 segundos (1 minuto)
     * - Intento 5: 300 segundos (5 minutos)
     * - Intento 6 o más: 900 segundos (15 minutos)
     */
    internal fun computeCooldownMillis(failedAttempts: Int): Long = when {
        failedAttempts < FREE_ATTEMPTS_BEFORE_COOLDOWN -> 0L
        failedAttempts == 3 -> 30_000L
        failedAttempts == 4 -> 60_000L
        failedAttempts == 5 -> 300_000L
        else -> 900_000L
    }

    internal fun deriveKeyPbkdf2(pin: String, salt: ByteArray): ByteArray {
        val chars = pin.toCharArray()
        return try {
            val spec = PBEKeySpec(chars, salt, PBKDF2_ITERATIONS, PBKDF2_KEY_BITS)
            val factory = SecretKeyFactory.getInstance(PBKDF2_ALGORITHM)
            factory.generateSecret(spec).encoded
        } finally {
            chars.fill('\u0000')
        }
    }

    internal fun constantTimeEquals(a: ByteArray, b: ByteArray): Boolean =
        MessageDigest.isEqual(a, b)

    private fun readStateInternal(appContext: Context): PersistedPinState {
        val file = AtomicFile(File(appContext.filesDir, FILE_NAME))
        if (!file.baseFile.exists()) return PersistedPinState(isLocked = false)
        return try {
            val raw = file.readFully()
            if (raw.size <= 12) return PersistedPinState(isLocked = true)
            val iv = raw.copyOfRange(0, 12)
            val cipherText = raw.copyOfRange(12, raw.size)
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.DECRYPT_MODE, getOrCreateSecretKey(), GCMParameterSpec(128, iv))
            val plain = cipher.doFinal(cipherText)
            json.decodeFromString(PersistedPinState.serializer(), plain.toString(Charsets.UTF_8))
        } catch (_: Exception) {
            // Si el archivo cifrado fue manipulado, mantenemos el bloqueo activo por seguridad.
            PersistedPinState(isLocked = true)
        }
    }

    private fun writeStateInternal(appContext: Context, state: PersistedPinState) {
        val file = AtomicFile(File(appContext.filesDir, FILE_NAME))
        val plain = json.encodeToString(PersistedPinState.serializer(), state).toByteArray(Charsets.UTF_8)
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, getOrCreateSecretKey())
        val iv = cipher.iv
        val cipherText = cipher.doFinal(plain)
        var stream: java.io.FileOutputStream? = null
        try {
            stream = file.startWrite()
            stream.write(iv)
            stream.write(cipherText)
            file.finishWrite(stream)
        } catch (e: Exception) {
            if (stream != null) file.failWrite(stream)
            throw e
        }
    }

    private fun getOrCreateSecretKey(): SecretKey {
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
}
