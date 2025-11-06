package com.atelierversace.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.atelierversace.data.remote.UserProfile
import com.atelierversace.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class AuthState {
    object Loading : AuthState()
    object Unauthenticated : AuthState()
    data class Authenticated(val user: UserProfile) : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Unauthenticated)
    val authState: StateFlow<AuthState> = _authState

    init {
        checkAuthStatus()
    }

    private fun checkAuthStatus() {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val user = authRepository.getCurrentUser()
            if (user != null) {
                val profileResult = authRepository.getUserProfile(user.id)
                if (profileResult.isSuccess) {
                    _authState.value = AuthState.Authenticated(profileResult.getOrThrow())
                } else {
                    _authState.value = AuthState.Unauthenticated
                }
            } else {
                _authState.value = AuthState.Unauthenticated
            }
        }
    }

    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val result = authRepository.signIn(email, password)

            if (result.isSuccess) {
                val user = authRepository.getCurrentUser()
                if (user != null) {
                    val profileResult = authRepository.getUserProfile(user.id)
                    if (profileResult.isSuccess) {
                        _authState.value = AuthState.Authenticated(profileResult.getOrThrow())
                    } else {
                        _authState.value = AuthState.Error("Failed to load profile")
                    }
                } else {
                    _authState.value = AuthState.Error("Authentication failed")
                }
            } else {
                _authState.value = AuthState.Error(
                    result.exceptionOrNull()?.message ?: "Sign in failed"
                )
            }
        }
    }

    fun signUp(email: String, password: String, displayName: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val result = authRepository.signUp(email, password, displayName)

            if (result.isSuccess) {
                val user = authRepository.getCurrentUser()
                if (user != null) {
                    val profileResult = authRepository.getUserProfile(user.id)
                    if (profileResult.isSuccess) {
                        _authState.value = AuthState.Authenticated(profileResult.getOrThrow())
                    } else {
                        _authState.value = AuthState.Error("Failed to create profile")
                    }
                } else {
                    _authState.value = AuthState.Error("Registration failed")
                }
            } else {
                _authState.value = AuthState.Error(
                    result.exceptionOrNull()?.message ?: "Sign up failed"
                )
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            authRepository.signOut()
            _authState.value = AuthState.Unauthenticated
        }
    }

    fun resetPassword(email: String) {
        viewModelScope.launch {
            authRepository.resetPassword(email)
        }
    }
}