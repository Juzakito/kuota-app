package com.kuota.core.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.kuota.core.model.FeesConfig
import com.kuota.core.model.LevelsConfig
import com.kuota.core.model.ScoringConfig
import com.kuota.core.model.TermsConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

interface ConfigRepository {
    suspend fun getLevelsConfig(): LevelsConfig
    suspend fun getFeesConfig(): FeesConfig
    suspend fun getTermsConfig(): TermsConfig
    suspend fun getScoringConfig(): ScoringConfig
    fun observeLevelsConfig(): Flow<LevelsConfig>
}

@Singleton
class FirebaseConfigRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) : ConfigRepository {

    override suspend fun getLevelsConfig(): LevelsConfig {
        return try {
            val snapshot = firestore.collection("config").document("levels").get().await()
            snapshot.toObject(LevelsConfig::class.java) ?: LevelsConfig()
        } catch (e: Exception) {
            LevelsConfig()
        }
    }

    override suspend fun getFeesConfig(): FeesConfig {
        return try {
            val snapshot = firestore.collection("config").document("fees").get().await()
            snapshot.toObject(FeesConfig::class.java) ?: FeesConfig()
        } catch (e: Exception) {
            FeesConfig()
        }
    }

    override suspend fun getTermsConfig(): TermsConfig {
        return try {
            val snapshot = firestore.collection("config").document("terms").get().await()
            snapshot.toObject(TermsConfig::class.java) ?: TermsConfig()
        } catch (e: Exception) {
            TermsConfig()
        }
    }

    override suspend fun getScoringConfig(): ScoringConfig {
        return try {
            val snapshot = firestore.collection("config").document("scoring").get().await()
            snapshot.toObject(ScoringConfig::class.java) ?: ScoringConfig()
        } catch (e: Exception) {
            ScoringConfig()
        }
    }

    override fun observeLevelsConfig(): Flow<LevelsConfig> = flow {
        emit(getLevelsConfig())
    }
}
