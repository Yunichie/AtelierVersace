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
            println("DEBUG - Starting sign up for: $email")

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

            println("DEBUG - User created with ID: $userId")

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

            println("DEBUG - Inserting profile: $profile")

            try {
                client.from("user_profiles").insert(profile)
                println("DEBUG - Profile inserted successfully")
            } catch (e: Exception) {
                e.printStackTrace()
                println("ERROR - Failed to insert profile: ${e.message}")
            }

            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            println("ERROR - Sign up failed: ${e.message}")
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
            println("DEBUG - Starting sign in for: $email")

            if (email.isBlank() || password.isBlank()) {
                return Result.failure(Exception("Email and password are required"))
            }

            client.auth.signInWith(Email) {
                this.email = email
                this.password = password
            }

            val userId = client.auth.currentUserOrNull()?.id
                ?: return Result.failure(Exception("Authentication failed"))

            println("DEBUG - User signed in with ID: $userId")

            val profileResult = getUserProfile(userId)
            if (profileResult.isFailure) {
                println("DEBUG - Profile not found, creating default profile")
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
                        println("DEBUG - Default profile created")
                    } catch (e: Exception) {
                        e.printStackTrace()
                        println("ERROR - Failed to create default profile: ${e.message}")
                    }
                }
            }

            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            println("ERROR - Sign in failed: ${e.message}")
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
            println("DEBUG - Signing out")
            client.auth.signOut()
            println("DEBUG - Sign out successful")
            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            println("ERROR - Sign out failed: ${e.message}")
            Result.failure(Exception(e.message ?: "Sign out failed"))
        }
    }

    suspend fun resetPassword(email: String): Result<Unit> {
        return try {
            println("DEBUG - Resetting password for: $email")

            if (email.isBlank()) {
                return Result.failure(Exception("Email is required"))
            }

            client.auth.resetPasswordForEmail(email)
            println("DEBUG - Password reset email sent")
            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            println("ERROR - Password reset failed: ${e.message}")
            Result.failure(Exception(e.message ?: "Password reset failed"))
        }
    }

    fun getCurrentUser() = client.auth.currentUserOrNull()

    fun isUserLoggedIn() = client.auth.currentUserOrNull() != null

    suspend fun getUserProfile(userId: String): Result<UserProfile> {
        return try {
            println("DEBUG - Getting profile for userId: $userId")
            val profile = client.from("user_profiles")
                .select {
                    filter {
                        eq("user_id", userId)
                    }
                }
                .decodeSingle<UserProfile>()
            println("DEBUG - Profile retrieved: ${profile.displayName}")
            Result.success(profile)
        } catch (e: Exception) {
            e.printStackTrace()
            println("ERROR - Failed to load profile: ${e.message}")
            Result.failure(Exception("Failed to load profile: ${e.message}"))
        }
    }

    suspend fun updateUserProfile(profile: UserProfile): Result<Unit> {
        return try {
            println("DEBUG - Updating profile for userId: ${profile.id}")

            val updatedProfile = profile.copy(
                updatedAt = System.currentTimeMillis().toString()
            )

            client.from("user_profiles")
                .update(updatedProfile) {
                    filter {
                        eq("user_id", profile.id)
                    }
                }

            println("DEBUG - Profile updated successfully")
            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            println("ERROR - Failed to update profile: ${e.message}")
            Result.failure(Exception("Failed to update profile: ${e.message}"))
        }
    }

    fun observeAuthState(): Flow<Boolean> = flow {
        emit(isUserLoggedIn())
    }
}