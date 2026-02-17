package com.harsh.worksphere.initial.auth.data.repo

import com.google.firebase.auth.FirebaseUser
import com.harsh.worksphere.core.utils.Result
import com.harsh.worksphere.initial.auth.data.remote.AuthDataSource
import com.harsh.worksphere.initial.auth.data.remote.GoogleSignInDataSource

class AuthRepository(
    private val googleDataSource: GoogleSignInDataSource,
    private val authDataSource: AuthDataSource
) {
    val currentUser: FirebaseUser?
        get() = authDataSource.currentUser

    suspend fun signInWithGoogle(): Result<FirebaseUser> {
        return when (val tokenResult = googleDataSource.signIn()) {
            is Result.Success -> {
                authDataSource.signInWithGoogle(tokenResult.data)
            }
            is Result.Error -> tokenResult
            is Result.Loading -> Result.Loading
        }
    }

    fun signOut() {
        authDataSource.signOut()
    }
}