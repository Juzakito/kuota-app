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
                    val summary = snapshot.toObject(UserSummary::class.java)
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
