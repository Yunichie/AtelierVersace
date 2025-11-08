package com.atelierversace.ui.scent_lens

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.atelierversace.data.remote.PerfumeCloud
import com.atelierversace.data.model.PersonaProfile
import com.atelierversace.data.repository.CloudPerfumeRepository
import com.atelierversace.data.repository.AuthRepository
import com.atelierversace.data.repository.AIPersonalizationRepository
import com.atelierversace.utils.GeminiHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

sealed class ScentLensState {
    object Idle : ScentLensState()
    object Loading : ScentLensState()
    data class Success(
        val brand: String,
        val name: String,
        val profile: PersonaProfile,
        val imageUri: String
    ) : ScentLensState()
    data class Error(val message: String) : ScentLensState()
}

class ScentLensViewModel(
    private val cloudRepository: CloudPerfumeRepository,
    private val geminiHelper: GeminiHelper,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val aiRepository = AIPersonalizationRepository()

    private val _state = MutableStateFlow<ScentLensState>(ScentLensState.Idle)
    val state: StateFlow<ScentLensState> = _state

    private var capturedImageBytes: ByteArray? = null

    fun analyzePerfume(imageBytes: ByteArray, imageUri: String) {
        viewModelScope.launch {
            _state.value = ScentLensState.Loading

            try {
                capturedImageBytes = imageBytes

                val identification = geminiHelper.identifyPerfume(imageBytes)

                if (identification == null) {
                    _state.value = ScentLensState.Error("Could not identify perfume. Please try again.")
                    return@launch
                }

                val (brand, name) = identification
                val profile = geminiHelper.generatePersonaProfile(brand, name)

                _state.value = ScentLensState.Success(brand, name, profile, imageUri)
            } catch (e: Exception) {
                e.printStackTrace()
                _state.value = ScentLensState.Error(e.message ?: "Unknown error occurred")
            }
        }
    }

    fun addToWardrobe(
        brand: String,
        name: String,
        profile: PersonaProfile,
        imageUri: String,
        userId: String,
        context: Context,
        onComplete: () -> Unit
    ) {
        viewModelScope.launch {
            try {
                val actualUserId = userId.ifEmpty {
                    authRepository.getCurrentUser()?.id ?: run {
                        println("ERROR - Not authenticated")
                        _state.value = ScentLensState.Error("Not authenticated. Please sign in.")
                        return@launch
                    }
                }

                println("DEBUG - Adding perfume for user: $actualUserId")
                println("DEBUG - Brand: $brand, Name: $name")

                val persistentImageUri = saveImageToInternalStorage(context, actualUserId)

                println("DEBUG - Persistent image URI: $persistentImageUri")

                val perfume = PerfumeCloud(
                    userId = actualUserId,
                    brand = brand,
                    name = name,
                    imageUri = persistentImageUri,
                    analogy = profile.analogy,
                    coreFeeling = profile.coreFeeling,
                    localContext = profile.localContext,
                    topNotes = profile.topNotes.joinToString(", "),
                    middleNotes = profile.middleNotes.joinToString(", "),
                    baseNotes = profile.baseNotes.joinToString(", "),
                    isWishlist = false,
                    isFavorite = false,
                    timestamp = System.currentTimeMillis().toString()
                )

                println("DEBUG - Perfume object: $perfume")

                val result = cloudRepository.addPerfume(perfume)

                if (result.isSuccess) {
                    println("DEBUG - Successfully added perfume to wardrobe")
                    capturedImageBytes = null

                    updateAIPersonalization(actualUserId)

                    _state.value = ScentLensState.Idle
                    onComplete()
                } else {
                    val error = result.exceptionOrNull()
                    println("ERROR - Failed to add perfume: ${error?.message}")
                    error?.printStackTrace()
                    _state.value = ScentLensState.Error(
                        error?.message ?: "Failed to add perfume to wardrobe"
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
                println("ERROR - Exception in addToWardrobe: ${e.message}")
                _state.value = ScentLensState.Error(e.message ?: "Failed to add perfume")
            }
        }
    }

    private suspend fun updateAIPersonalization(userId: String) {
        try {
            println("DEBUG - Updating AI personalization after adding to wardrobe")

            val wardrobeResult = cloudRepository.getWardrobe(userId)
            if (wardrobeResult.isSuccess) {
                val perfumes = wardrobeResult.getOrNull() ?: emptyList()

                if (perfumes.isNotEmpty()) {
                    val newPersonalization = aiRepository.analyzeUserPreferences(userId, perfumes)

                    val saveResult = aiRepository.updatePersonalization(newPersonalization)
                    if (saveResult.isSuccess) {
                        println("DEBUG - AI personalization updated successfully")
                    } else {
                        println("ERROR - Failed to save AI personalization: ${saveResult.exceptionOrNull()?.message}")
                    }
                } else {
                    println("DEBUG - Wardrobe empty, skipping AI personalization update")
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            println("ERROR - Failed to update AI personalization: ${e.message}")
        }
    }

    private fun saveImageToInternalStorage(context: Context, userId: String): String {
        try {
            val imageBytes = capturedImageBytes ?: return ""

            val userDir = File(context.filesDir, "perfume_images/$userId")
            if (!userDir.exists()) {
                userDir.mkdirs()
            }

            val filename = "perfume_${UUID.randomUUID()}.jpg"
            val imageFile = File(userDir, filename)

            val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)

            FileOutputStream(imageFile).use { outputStream ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
            }

            println("DEBUG - Image saved to: ${imageFile.absolutePath}")

            return imageFile.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            println("ERROR - Failed to save image: ${e.message}")
            return ""
        }
    }

    fun reset() {
        _state.value = ScentLensState.Idle
        capturedImageBytes = null
    }
}