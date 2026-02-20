package com.harsh.worksphere.initial.auth.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.harsh.worksphere.core.utils.Result
import com.harsh.worksphere.initial.auth.data.repo.AuthRepository
import com.harsh.worksphere.initial.auth.data.model.*
import com.harsh.worksphere.initial.auth.data.repo.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    sealed class UiState {
        data object Idle : UiState()
        data object Loading : UiState()
        data class Success(val authResult: AuthResult) : UiState()
        data class Error(val message: String) : UiState()
    }

    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    fun signInWithGoogle() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading

            when (val authResult = authRepository.signInWithGoogle()) {
                is Result.Success -> {
                    val firebaseUser = authResult.data
                    handleSignInSuccess(
                        firebaseUid = firebaseUser.uid, // Firebase Auth UID
                        email = firebaseUser.email ?: "",
                        name = firebaseUser.displayName ?: "",
                        profilePic = firebaseUser.photoUrl?.toString()
                    )
                }
                is Result.Error -> {
                    _uiState.value = UiState.Error(authResult.message)
                }
                is Result.Loading -> {
                    _uiState.value = UiState.Loading
                }
            }
        }
    }

    private suspend fun handleSignInSuccess(
        firebaseUid: String,
        email: String,
        name: String,
        profilePic: String?
    ) {
        when (val userResult = userRepository.getUser(email)) {
            is Result.Success -> {
                val existingUser = userResult.data

                if (existingUser != null) {
                    // User was pre-created by manager - UPDATE with Firebase UID and Google profile pic
                    val updatedUser = existingUser.copy(
                        userId = firebaseUid, // Update to Firebase Auth UID
                        profilePic = if (existingUser.profilePic.isNullOrEmpty()) profilePic else existingUser.profilePic, // Only use Google photo if no profile pic is set
                        name = name.ifEmpty { existingUser.name } // Use Google name if available, keep existing if not
                    )

                    when (val updateResult = userRepository.updateUser(updatedUser)) {
                        is Result.Success -> {
                            _uiState.value = UiState.Success(
                                AuthResult(
                                    user = updatedUser,
                                    isNewUser = false,
                                    role = updatedUser.role
                                )
                            )
                        }
                        is Result.Error -> {
                            _uiState.value = UiState.Error(updateResult.message)
                        }
                        is Result.Loading -> Unit
                    }
                } else {
                    // Completely new user - create from scratch
                    createNewUser(firebaseUid, email, name, profilePic)
                }
            }
            is Result.Error -> {
                _uiState.value = UiState.Error(userResult.message)
            }
            is Result.Loading -> Unit
        }
    }

    private suspend fun createNewUser(
        userId: String,
        email: String,
        name: String,
        profilePic: String?
    ) {
        when (val roleResult = userRepository.determineUserRole()) {
            is Result.Success -> {
                val role = roleResult.data
                val user = User(
                    userId = userId,
                    email = email,
                    name = name,
                    profilePic = profilePic,
                    role = role,
                    status = UserStatus.OFFLINE
                )

                when (val createResult = userRepository.createUser(user)) {
                    is Result.Success -> {
                        _uiState.value = UiState.Success(
                            AuthResult(
                                user = user,
                                isNewUser = true,
                                role = role
                            )
                        )
                    }
                    is Result.Error -> {
                        _uiState.value = UiState.Error(createResult.message)
                    }
                    is Result.Loading -> Unit
                }
            }
            is Result.Error -> {
                _uiState.value = UiState.Error(roleResult.message)
            }
            is Result.Loading -> Unit
        }
    }

    fun resetState() {
        _uiState.value = UiState.Idle
    }

    class Factory(
        private val authRepository: AuthRepository,
        private val userRepository: UserRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
                return AuthViewModel(authRepository, userRepository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}