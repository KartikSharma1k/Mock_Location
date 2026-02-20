package com.hestabit.fakelocation.data.repository

import android.app.Activity
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.hestabit.fakelocation.data.local.DataStoreManager
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

interface AuthRepository {
    val isUserAuthenticated: Boolean
    fun sendOtp(phoneNumber: String, activity: Activity, resendToken: PhoneAuthProvider.ForceResendingToken?): Flow<OtpResult>
    suspend fun verifyOtp(verificationId: String, code: String): Result<Boolean>
}

sealed class OtpResult {
    data class CodeSent(val verificationId: String, val token: PhoneAuthProvider.ForceResendingToken) : OtpResult()
    data class Error(val message: String) : OtpResult()
    object VerificationCompleted : OtpResult() // For auto-verification
}

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val dataStoreManager: DataStoreManager
) : AuthRepository {

    override val isUserAuthenticated: Boolean
        get() = auth.currentUser != null

    override fun sendOtp(phoneNumber: String, activity: Activity, resendToken: PhoneAuthProvider.ForceResendingToken?): Flow<OtpResult> = callbackFlow {
        val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                // Determine if we should sign in automatically or let UI handle it.
                // For this implementation, we will emit a simplified result or handle sign-in here.
                // Let's try to sign in.
                try {
                     auth.signInWithCredential(credential)
                         .addOnCompleteListener { task ->
                             if (task.isSuccessful) {
                                 trySend(OtpResult.VerificationCompleted)
                             } else {
                                 trySend(OtpResult.Error(task.exception?.message ?: "Auto-verification failed"))
                             }
                         }
                } catch (e: Exception) {
                    trySend(OtpResult.Error(e.message ?: "Auto-verification error"))
                }
            }

            override fun onVerificationFailed(e: FirebaseException) {
                trySend(OtpResult.Error(e.message ?: "Verification Failed"))
            }

            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                trySend(OtpResult.CodeSent(verificationId, token))
            }
        }

        val options = if(resendToken == null) {
            PhoneAuthOptions.newBuilder(auth)
                .setPhoneNumber(phoneNumber)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(activity)
                .setCallbacks(callbacks)
                .build()
        }else {
            PhoneAuthOptions.newBuilder(auth)
                .setPhoneNumber(phoneNumber)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(activity)
                .setCallbacks(callbacks)
                .setForceResendingToken(resendToken)
                .build()
        }
        
        PhoneAuthProvider.verifyPhoneNumber(options)
        
        awaitClose { }
    }

    override suspend fun verifyOtp(verificationId: String, code: String): Result<Boolean> {
        return try {
            val credential = PhoneAuthProvider.getCredential(verificationId, code)
            auth.signInWithCredential(credential).await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
