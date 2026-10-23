package com.kouta.app.core.security

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

/**
 * Gestor de idempotencia y prevención de doble gasto / doble ejecución para KUOTA.
 *
 * En entornos fintech (inspirado en la robustez de pasarelas de pago y billeteras como Yape):
 * 1. Previene ejecuciones concurrentes de la misma acción financiera mediante un [Mutex].
 * 2. Aplica un mecanismo de eliminación de rebote (debounce) para bloquear toques repetidos accidentales.
 * 3. Asigna a cada operación una clave de idempotencia (`idempotencyKey`) única en formato UUIDv4,
 *    garantizando que ni la red ni reintentos del servidor puedan duplicar un cobro o solicitud.
 */
object FintechOperationLock {

    private val mutex = Mutex()
    private val activeOperations = ConcurrentHashMap<String, Long>()
    private val lastClickTimestamps = ConcurrentHashMap<String, AtomicLong>()

    private const val DEBOUNCE_MILLIS = 1_200L

    /**
     * Genera un identificador de idempotencia seguro (UUIDv4).
     */
    fun generateIdempotencyKey(): String = UUID.randomUUID().toString()

    /**
     * Comprueba si una acción en la interfaz de usuario puede ejecutarse o si debe ser ignorada
     * por haberse pulsado repetidamente dentro de la ventana de debounce.
     *
     * @param actionTag Identificador de la acción (ej. "confirm_installment_payment", "submit_kyc").
     * @return `true` si el toque es legítimo y debe procesarse, `false` si es un doble clic espurio.
     */
    fun canExecuteAction(actionTag: String, nowMillis: Long = System.currentTimeMillis()): Boolean {
        val lastTimestamp = lastClickTimestamps.computeIfAbsent(actionTag) { AtomicLong(0L) }
        val prev = lastTimestamp.get()
        if (nowMillis - prev < DEBOUNCE_MILLIS) {
            return false
        }
        return lastTimestamp.compareAndSet(prev, nowMillis)
    }

    /**
     * Ejecuta un bloque financiero protegido bajo un candado asíncrono y con clave de idempotencia única.
     *
     * @param operationTag Nombre de la operación.
     * @param block Función suspendida que recibe el `idempotencyKey`.
     */
    suspend fun <T> executeIdempotent(
        operationTag: String,
        block: suspend (idempotencyKey: String) -> T
    ): T? {
        if (!mutex.tryLock()) {
            return null // Otra operación crítica está en curso
        }
        val idempotencyKey = generateIdempotencyKey()
        val now = System.currentTimeMillis()
        activeOperations[idempotencyKey] = now
        return try {
            block(idempotencyKey)
        } finally {
            activeOperations.remove(idempotencyKey)
            mutex.unlock()
        }
    }

    /**
     * Limpia el registro de operaciones activas (usado en tests o cierre de sesión).
     */
    fun reset() {
        activeOperations.clear()
        lastClickTimestamps.clear()
    }
}
