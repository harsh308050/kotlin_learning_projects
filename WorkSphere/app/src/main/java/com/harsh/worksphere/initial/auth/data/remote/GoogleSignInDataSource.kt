package com.harsh.worksphere.initial.auth.data.remote

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.harsh.worksphere.R
import com.harsh.worksphere.core.utils.Result

class GoogleSignInDataSource(private val context: Context) {

    private val credentialManager = CredentialManager.create(context)

    suspend fun signIn(): Result<String> { // Returns ID Token
        return try {
            val googleIdOption = GetGoogleIdOption.Builder()
                .setServerClientId(context.getString(R.string.default_web_client_id))
                .setFilterByAuthorizedAccounts(false)
                .setAutoSelectEnabled(true)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val result = credentialManager.getCredential(context, request)

            when (val credential = result.credential) {
                is CustomCredential -> {
                    if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                        val googleCredential = GoogleIdTokenCredential.createFrom(credential.data)
                        Result.Success(googleCredential.idToken)
                    } else {
                        Result.Error("Invalid credential type")
                    }
                }
                else -> Result.Error("Unknown credential type")
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "Google Sign-In failed")
        }
    }
}