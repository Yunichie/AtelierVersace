package com.atelierversace.ui.profile

import android.content.Context
import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.atelierversace.data.remote.AIPersonalization
import com.atelierversace.data.remote.UserProfile
import com.atelierversace.data.repository.AIPersonalizationRepository
import com.atelierversace.data.repository.AuthRepository
import com.atelierversace.data.repository.CloudPerfumeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

class ProfileViewModel(
    private val authRepository: AuthRepository,
    private val cloudPerfumeRepository: CloudPerfumeRepository,
    private val aiRepository: AIPersonalizationRepository
) : ViewModel() {

    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    val userProfile: StateFlow<UserProfile?> = _userProfile

    private val _personalization = MutableStateFlow<AIPersonalization?>(null)
    val personalization: StateFlow<AIPersonalization?> = _personalization

    private val _wardrobeCount = MutableStateFlow(0)
    val wardrobeCount: StateFlow<Int> = _wardrobeCount

    private val _wishlistCount = MutableStateFlow(0)
    val wishlistCount: StateFlow<Int> = _wishlistCount

    private val _favoritesCount = MutableStateFlow(0)
    val favoritesCount: StateFlow<Int> = _favoritesCount

    private val _isUploading = MutableStateFlow(false)
    val isUploading: StateFlow<Boolean> = _isUploading

    private val _uploadError = MutableStateFlow<String?>(null)
    val uploadError: StateFlow<String?> = _uploadError

    private var isRefreshing = false

    init {
        loadProfile()
        loadPersonalization()
        loadStats()
        startPeriodicRefresh()
    }

    private fun startPeriodicRefresh() {
        viewModelScope.launch {
            while (true) {
                delay(2000)
                if (!isRefreshing) {
                    loadStats()
                    loadPersonalization()
                }
            }
        }
    }

    private fun loadProfile() {
        viewModelScope.launch {
            try {
                val user = authRepository.getCurrentUser()
                if (user != null) {
                    println("DEBUG - Loading profile for user: ${user.id}")
                    val profileResult = authRepository.getUserProfile(user.id)
                    if (profileResult.isSuccess) {
                        _userProfile.value = profileResult.getOrThrow()
                        println("DEBUG - Profile loaded: ${_userProfile.value?.displayName}")
                    } else {
                        println("ERROR - Failed to load profile: ${profileResult.exceptionOrNull()?.message}")
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                println("ERROR - Exception loading profile: ${e.message}")
            }
        }
    }

    private fun loadPersonalization() {
        viewModelScope.launch {
            try {
                val user = authRepository.getCurrentUser()
                if (user != null) {
                    println("DEBUG - Loading personalization for user: ${user.id}")
                    val result = aiRepository.getPersonalization(user.id)
                    if (result.isSuccess) {
                        val personalizationData = result.getOrNull()
                        println("DEBUG - Personalization loaded: ${personalizationData?.styleProfile}")
                        _personalization.value = personalizationData
                    } else {
                        println("DEBUG - No personalization found for user")
                        _personalization.value = null
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                println("ERROR - Failed to load personalization: ${e.message}")
            }
        }
    }

    private fun loadStats() {
        viewModelScope.launch {
            try {
                isRefreshing = true
                val user = authRepository.getCurrentUser()
                if (user != null) {
                    val wardrobeResult = cloudPerfumeRepository.getWardrobe(user.id)
                    if (wardrobeResult.isSuccess) {
                        _wardrobeCount.value = wardrobeResult.getOrNull()?.size ?: 0
                    }

                    val wishlistResult = cloudPerfumeRepository.getWishlist(user.id)
                    if (wishlistResult.isSuccess) {
                        _wishlistCount.value = wishlistResult.getOrNull()?.size ?: 0
                    }

                    val favoritesResult = cloudPerfumeRepository.getFavorites(user.id)
                    if (favoritesResult.isSuccess) {
                        _favoritesCount.value = favoritesResult.getOrNull()?.size ?: 0
                    }

                    println("DEBUG - Profile stats updated: Wardrobe=${_wardrobeCount.value}, Wishlist=${_wishlistCount.value}, Favorites=${_favoritesCount.value}")
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isRefreshing = false
            }
        }
    }

    fun uploadProfilePicture(bitmap: Bitmap, context: Context) {
        viewModelScope.launch {
            _isUploading.value = true
            _uploadError.value = null

            try {
                println("DEBUG - Starting profile picture upload")

                val user = authRepository.getCurrentUser()

                if (user == null) {
                    println("ERROR - No current user found")
                    _uploadError.value = "Not authenticated. Please sign out and sign in again."
                    _isUploading.value = false
                    return@launch
                }

                println("DEBUG - User ID: ${user.id}")
                println("DEBUG - User Email: ${user.email}")

                var currentProfile = _userProfile.value
                if (currentProfile == null) {
                    println("DEBUG - Profile not cached, loading from database")
                    val profileResult = authRepository.getUserProfile(user.id)
                    if (profileResult.isSuccess) {
                        currentProfile = profileResult.getOrThrow()
                        _userProfile.value = currentProfile
                        println("DEBUG - Profile loaded: ${currentProfile.displayName}")
                    } else {
                        println("ERROR - Failed to load profile: ${profileResult.exceptionOrNull()?.message}")
                        _uploadError.value = "Failed to load profile: ${profileResult.exceptionOrNull()?.message}"
                        _isUploading.value = false
                        return@launch
                    }
                }

                println("DEBUG - Compressing and saving bitmap")

                val tempFile = File(context.cacheDir, "temp_avatar_${UUID.randomUUID()}.jpg")
                FileOutputStream(tempFile).use { outputStream ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
                }

                println("DEBUG - Temp file created: ${tempFile.absolutePath}, size: ${tempFile.length()} bytes")

                println("DEBUG - Uploading to Supabase Storage")
                val uploadResult = authRepository.uploadProfilePicture(user.id, tempFile)

                tempFile.delete()
                println("DEBUG - Temp file deleted")

                if (uploadResult.isSuccess) {
                    val avatarUrl = uploadResult.getOrThrow()
                    println("DEBUG - Upload successful, URL: $avatarUrl")

                    currentProfile.avatarUrl?.let { oldUrl ->
                        println("DEBUG - Deleting old avatar: $oldUrl")
                        authRepository.deleteProfilePicture(oldUrl)
                    }

                    println("DEBUG - Updating profile in database")
                    val updatedProfile = currentProfile.copy(avatarUrl = avatarUrl)
                    val updateResult = authRepository.updateUserProfile(updatedProfile)

                    if (updateResult.isSuccess) {
                        _userProfile.value = updatedProfile
                        println("DEBUG - Profile picture updated successfully")
                    } else {
                        println("ERROR - Failed to update profile: ${updateResult.exceptionOrNull()?.message}")
                        _uploadError.value = "Failed to update profile: ${updateResult.exceptionOrNull()?.message}"
                    }
                } else {
                    val error = uploadResult.exceptionOrNull()
                    println("ERROR - Upload failed: ${error?.message}")
                    _uploadError.value = error?.message ?: "Upload failed"
                }
            } catch (e: Exception) {
                e.printStackTrace()
                println("ERROR - Exception during upload: ${e.message}")
                _uploadError.value = "Error uploading picture: ${e.message}"
            } finally {
                _isUploading.value = false
                println("DEBUG - Upload process completed")
            }
        }
    }

    fun removeProfilePicture() {
        viewModelScope.launch {
            _isUploading.value = true
            _uploadError.value = null

            try {
                println("DEBUG - Starting profile picture removal")

                val user = authRepository.getCurrentUser()

                if (user == null) {
                    println("ERROR - No current user found")
                    _uploadError.value = "Not authenticated. Please sign out and sign in again."
                    _isUploading.value = false
                    return@launch
                }

                var currentProfile = _userProfile.value
                if (currentProfile == null) {
                    println("DEBUG - Profile not cached, loading from database")
                    val profileResult = authRepository.getUserProfile(user.id)
                    if (profileResult.isSuccess) {
                        currentProfile = profileResult.getOrThrow()
                        _userProfile.value = currentProfile
                    } else {
                        _uploadError.value = "Failed to load profile"
                        _isUploading.value = false
                        return@launch
                    }
                }

                currentProfile.avatarUrl?.let { avatarUrl ->
                    println("DEBUG - Deleting avatar: $avatarUrl")
                    authRepository.deleteProfilePicture(avatarUrl)
                }

                val updatedProfile = currentProfile.copy(avatarUrl = null)
                val updateResult = authRepository.updateUserProfile(updatedProfile)

                if (updateResult.isSuccess) {
                    _userProfile.value = updatedProfile
                    println("DEBUG - Profile picture removed successfully")
                } else {
                    println("ERROR - Failed to update profile: ${updateResult.exceptionOrNull()?.message}")
                    _uploadError.value = "Failed to update profile: ${updateResult.exceptionOrNull()?.message}"
                }
            } catch (e: Exception) {
                e.printStackTrace()
                println("ERROR - Exception removing picture: ${e.message}")
                _uploadError.value = "Error removing picture: ${e.message}"
            } finally {
                _isUploading.value = false
            }
        }
    }

    fun clearUploadError() {
        _uploadError.value = null
    }

    fun refresh() {
        loadProfile()
        loadPersonalization()
        loadStats()
    }
}