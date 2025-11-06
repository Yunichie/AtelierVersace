package com.atelierversace.data.repository

import com.atelierversace.data.remote.SupabaseClient
import com.atelierversace.data.remote.UserProfile
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class AuthRepository {
    private val client = SupabaseClient.client

    suspend fun signUp(email: String, password: String, displayName: String): Result<Unit> {
        return try {
            if (email.isBlank() || password.isBlank() || displayName.isBlank()) {
                return Result.failure(Exception("All fields are required"))
            }

            if (password.length < 6) {
                return Result.failure(Exception("Password must be at least 6 characters"))
            }

            client.auth.signUpWith(Email) {
                this.email = email
                this.password = password
            }

            val userId = client.auth.currentUserOrNull()?.id
                ?: return Result.failure(Exception("Failed to create user account"))

            val profile = UserProfile(
                id = userId,
                email = email,
                displayName = displayName,
                avatarUrl = null,
                preferredBrands = emptyList(),
                preferredNotes = emptyList(),
                commonOccasions = emptyList(),
                createdAt = System.currentTimeMillis().toString(),
                updatedAt = System.currentTimeMillis().toString()
            )

            // Insert profile
            try {
                client.from("user_profiles").insert(profile)
            } catch (e: Exception) {
                e.printStackTrace()
            }

            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            val errorMessage = when {
                e.message?.contains("already registered") == true -> "Email already registered"
                e.message?.contains("invalid email") == true -> "Invalid email format"
                e.message?.contains("weak password") == true -> "Password is too weak"
                else -> e.message ?: "Sign up failed"
            }
            Result.failure(Exception(errorMessage))
        }
    }

    suspend fun signIn(email: String, password: String): Result<Unit> {
        return try {
            if (email.isBlank() || password.isBlank()) {
                return Result.failure(Exception("Email and password are required"))
            }

            client.auth.signInWith(Email) {
                this.email = email
                this.password = password
            }

            val userId = client.auth.currentUserOrNull()?.id
                ?: return Result.failure(Exception("Authentication failed"))

            val profileResult = getUserProfile(userId)
            if (profileResult.isFailure) {
                val user = client.auth.currentUserOrNull()
                if (user != null) {
                    val profile = UserProfile(
                        id = userId,
                        email = user.email ?: email,
                        displayName = user.email?.substringBefore("@") ?: "User",
                        avatarUrl = null,
                        preferredBrands = emptyList(),
                        preferredNotes = emptyList(),
                        commonOccasions = emptyList(),
                        createdAt = System.currentTimeMillis().toString(),
                        updatedAt = System.currentTimeMillis().toString()
                    )

                    try {
                        client.from("user_profiles").insert(profile)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }

            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            val errorMessage = when {
                e.message?.contains("Invalid login credentials") == true -> "Invalid email or password"
                e.message?.contains("Email not confirmed") == true -> "Please verify your email"
                e.message?.contains("network") == true -> "Network error. Please check your connection"
                else -> e.message ?: "Sign in failed"
            }
            Result.failure(Exception(errorMessage))
        }
    }

    suspend fun signOut(): Result<Unit> {
        return try {
            client.auth.signOut()
            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(Exception(e.message ?: "Sign out failed"))
        }
    }

    suspend fun resetPassword(email: String): Result<Unit> {
        return try {
            if (email.isBlank()) {
                return Result.failure(Exception("Email is required"))
            }

            client.auth.resetPasswordForEmail(email)
            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(Exception(e.message ?: "Password reset failed"))
        }
    }

    fun getCurrentUser() = client.auth.currentUserOrNull()

    fun isUserLoggedIn() = client.auth.currentUserOrNull() != null

    suspend fun getUserProfile(userId: String): Result<UserProfile> {
        return try {
            val profile = client.from("user_profiles")
                .select {
                    filter {
                        eq("user_id", userId)
                    }
                }
                .decodeSingle<UserProfile>()
            Result.success(profile)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(Exception("Failed to load profile: ${e.message}"))
        }
    }

    suspend fun updateUserProfile(profile: UserProfile): Result<Unit> {
        return try {
            val updatedProfile = profile.copy(
                updatedAt = System.currentTimeMillis().toString()
            )

            client.from("user_profiles")
                .update(updatedProfile) {
                    filter {
                        eq("user_id", profile.id)
                    }
                }
            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(Exception("Failed to update profile: ${e.message}"))
        }
    }

    fun observeAuthState(): Flow<Boolean> = flow {
        emit(isUserLoggedIn())
    }
}