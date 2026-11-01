package com.kuota.core.data.repository

import android.app.Activity
import com.google.firebase.FirebaseException
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

interface AuthRepository {
    val currentUser: FirebaseUser?
    val currentUid: String?
    fun observeAuthState(): Flow<FirebaseUser?>
    
    fun sendOtp(
        phoneNumber: String,
        activity: Activity,
        onCodeSent: (verificationId: String) -> Unit,
        onVerificationCompleted: (PhoneAuthCredential) -> Unit,
        onError: (Exception) -> Unit
    )

    suspend fun signInWithCredential(credential: PhoneAuthCredential): Result<AuthResult>
    suspend fun verifyOtp(verificationId: String, smsCode: String): Result<AuthResult>
    suspend fun signInAnonymously(): Result<AuthResult>
    fun signOut()
}

@Singleton
class FirebaseAuthRepository @Inject constructor(
    private val auth: FirebaseAuth
) : AuthRepository {

    override val currentUser: FirebaseUser?
        get() = auth.currentUser

    override val currentUid: String?
        get() = auth.currentUser?.uid

    override fun observeAuthState(): Flow<FirebaseUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            trySend(firebaseAuth.currentUser)
        }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    override fun sendOtp(
        phoneNumber: String,
        activity: Activity,
        onCodeSent: (verificationId: String) -> Unit,
        onVerificationCompleted: (PhoneAuthCredential) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                onVerificationCompleted(credential)
            }

            override fun onVerificationFailed(e: FirebaseException) {
                onError(e)
            }

            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                onCodeSent(verificationId)
            }
        }

        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(callbacks)
            .build()

        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    override suspend fun signInWithCredential(credential: PhoneAuthCredential): Result<AuthResult> =
        runCatching {
            auth.signInWithCredential(credential).await()
        }

    override suspend fun verifyOtp(verificationId: String, smsCode: String): Result<AuthResult> =
        runCatching {
            val credential = PhoneAuthProvider.getCredential(verificationId, smsCode)
            auth.signInWithCredential(credential).await()
        }

    override suspend fun signInAnonymously(): Result<AuthResult> = runCatching {
        auth.signInAnonymously().await()
    }

    override fun signOut() {
        auth.signOut()
    }
}
