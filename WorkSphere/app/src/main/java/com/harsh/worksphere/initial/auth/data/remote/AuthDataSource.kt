package com.harsh.worksphere.initial.auth.data.remote

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.harsh.worksphere.core.firebase.FirebaseModule
import com.harsh.worksphere.core.utils.Result
import kotlinx.coroutines.tasks.await

class AuthDataSource {

    private val auth: FirebaseAuth = FirebaseModule.auth

    public val currentUser: FirebaseUser?
        get() = auth.currentUser

    suspend fun signInWithGoogle(idToken: String): Result<FirebaseUser> {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val result = auth.signInWithCredential(credential).await()
            result.user?.let {
                Result.Success(it)
            } ?: Result.Error("Authentication failed: User is null")
        } catch (e: Exception) {
            Result.Error(e.message ?: "Google Sign-In failed")
        }
    }

    fun signOut() {
        auth.signOut()
    }
}