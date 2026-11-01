package com.kuota.core.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.kuota.core.model.UserSummary
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import javax.inject.Singleton

interface UserRepository {
    fun getCurrentUid(): String?
    fun observeUserSummary(): Flow<UserSummary?>
}

@Singleton
class FirebaseUserRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : UserRepository {

    override fun getCurrentUid(): String? {
        return auth.currentUser?.uid
    }

    /**
     * Escucha en tiempo real `users/{uid}/summary/current`.
     * Cumple con la regla de calidad de AGENTS.md:
     * - Home en 1 lectura.
     * - Cancelación automática del listener en onCleared para evitar fugas de memoria.
     */
    override fun observeUserSummary(): Flow<UserSummary?> = callbackFlow {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            trySend(null)
            close()
            return@callbackFlow
        }

        val registration = firestore
            .collection("users")
            .document(uid)
            .collection("summary")
            .document("current")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(null)
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    val summary = runCatching { snapshot.toObject(UserSummary::class.java) }.getOrNull()
                        ?: UserSummary(
                            uid = uid,
                            kycStatus = snapshot.getString("kycStatus") ?: "approved",
                            level = snapshot.getLong("level")?.toInt() ?: 1,
                            maxCreditCents = snapshot.getLong("maxCreditCents")
                                ?: snapshot.getLong("creditLimit")
                                ?: 140000L,
                            availableCreditCents = snapshot.getLong("availableCreditCents")
                                ?: snapshot.getLong("availableCredit")
                                ?: 140000L,
                            usedCreditCents = snapshot.getLong("usedCreditCents")
                                ?: snapshot.getLong("usedCredit")
                                ?: 0L,
                            role = snapshot.getString("role") ?: "buyer",
                            businessName = snapshot.getString("businessName"),
                            riskScore = snapshot.getLong("riskScore")?.toInt() ?: 720,
                            updatedAt = System.currentTimeMillis()
                        )
                    trySend(summary)
                } else {
                    // Fallback a resumen inicial de Nivel 1
                    trySend(UserSummary(uid = uid))
                }
            }

        awaitClose {
            registration.remove()
        }
    }
}
