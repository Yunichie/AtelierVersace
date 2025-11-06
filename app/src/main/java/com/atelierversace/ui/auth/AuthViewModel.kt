package com.atelierversace.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.atelierversace.data.remote.UserProfile
import com.atelierversace.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

sealed class AuthState {
    object Loading : AuthState()
    object Unauthenticated : AuthState()
    data class Authenticated(val user: UserProfile) : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    val authState: StateFlow<AuthState> = _authState

    init {
        checkAuthStatus()
    }

    private fun checkAuthStatus() {
        viewModelScope.launch {
            try {
                _authState.value = AuthState.Loading

                val user = authRepository.getCurrentUser()
                if (user != null) {
                    val profileResult = authRepository.getUserProfile(user.id)
                    if (profileResult.isSuccess) {
                        _authState.value = AuthState.Authenticated(profileResult.getOrThrow())
                    } else {
                        val newProfile = UserProfile(
                            id = user.id,
                            email = user.email ?: "",
                            displayName = user.email?.substringBefore("@") ?: "User",
                            avatarUrl = null,
                            preferredBrands = emptyList(),
                            preferredNotes = emptyList(),
                            commonOccasions = emptyList(),
                            createdAt = System.currentTimeMillis().toString(),
                            updatedAt = System.currentTimeMillis().toString()
                        )

                        try {
                            authRepository.updateUserProfile(newProfile)
                            _authState.value = AuthState.Authenticated(newProfile)
                        } catch (e: Exception) {
                            e.printStackTrace()
                            _authState.value = AuthState.Authenticated(newProfile)
                        }
                    }
                } else {
                    _authState.value = AuthState.Unauthenticated
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _authState.value = AuthState.Unauthenticated
            }
        }
    }

    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            try {
                _authState.value = AuthState.Loading

                val result = authRepository.signIn(email, password)

                if (result.isSuccess) {
                    delay(500)

                    val user = authRepository.getCurrentUser()
                    if (user != null) {
                        val profileResult = authRepository.getUserProfile(user.id)
                        if (profileResult.isSuccess) {
                            _authState.value = AuthState.Authenticated(profileResult.getOrThrow())
                        } else {
                            val newProfile = UserProfile(
                                id = user.id,
                                email = user.email ?: email,
                                displayName = user.email?.substringBefore("@") ?: "User",
                                avatarUrl = null,
                                preferredBrands = emptyList(),
                                preferredNotes = emptyList(),
                                commonOccasions = emptyList(),
                                createdAt = System.currentTimeMillis().toString(),
                                updatedAt = System.currentTimeMillis().toString()
                            )

                            _authState.value = AuthState.Authenticated(newProfile)
                        }
                    } else {
                        _authState.value = AuthState.Error("Authentication failed. Please try again.")
                    }
                } else {
                    _authState.value = AuthState.Error(
                        result.exceptionOrNull()?.message ?: "Sign in failed"
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _authState.value = AuthState.Error(
                    e.message ?: "An unexpected error occurred"
                )
            }
        }
    }

    fun signUp(email: String, password: String, displayName: String) {
        viewModelScope.launch {
            try {
                _authState.value = AuthState.Loading

                val result = authRepository.signUp(email, password, displayName)

                if (result.isSuccess) {
                    delay(500)

                    val user = authRepository.getCurrentUser()
                    if (user != null) {
                        val profileResult = authRepository.getUserProfile(user.id)
                        if (profileResult.isSuccess) {
                            _authState.value = AuthState.Authenticated(profileResult.getOrThrow())
                        } else {
                            val newProfile = UserProfile(
                                id = user.id,
                                email = email,
                                displayName = displayName,
                                avatarUrl = null,
                                preferredBrands = emptyList(),
                                preferredNotes = emptyList(),
                                commonOccasions = emptyList(),
                                createdAt = System.currentTimeMillis().toString(),
                                updatedAt = System.currentTimeMillis().toString()
                            )
                            _authState.value = AuthState.Authenticated(newProfile)
                        }
                    } else {
                        _authState.value = AuthState.Error("Registration failed. Please try again.")
                    }
                } else {
                    _authState.value = AuthState.Error(
                        result.exceptionOrNull()?.message ?: "Sign up failed"
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _authState.value = AuthState.Error(
                    e.message ?: "An unexpected error occurred"
                )
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            try {
                authRepository.signOut()
                _authState.value = AuthState.Unauthenticated
            } catch (e: Exception) {
                e.printStackTrace()
                _authState.value = AuthState.Unauthenticated
            }
        }
    }

    fun resetPassword(email: String) {
        viewModelScope.launch {
            try {
                authRepository.resetPassword(email)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun clearError() {
        if (_authState.value is AuthState.Error) {
            _authState.value = AuthState.Unauthenticated
        }
    }
}