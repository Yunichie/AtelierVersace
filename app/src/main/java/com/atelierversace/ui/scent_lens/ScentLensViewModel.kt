package com.atelierversace.ui.scent_lens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.atelierversace.data.remote.PerfumeCloud
import com.atelierversace.data.model.PersonaProfile
import com.atelierversace.data.repository.CloudPerfumeRepository
import com.atelierversace.data.repository.AuthRepository
import com.atelierversace.utils.GeminiHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

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

    private val _state = MutableStateFlow<ScentLensState>(ScentLensState.Idle)
    val state: StateFlow<ScentLensState> = _state

    fun analyzePerfume(imageBytes: ByteArray, imageUri: String) {
        viewModelScope.launch {
            _state.value = ScentLensState.Loading

            try {
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
        onComplete: () -> Unit
    ) {
        viewModelScope.launch {
            try {
                val actualUserId = userId.ifEmpty {
                    authRepository.getCurrentUser()?.id ?: run {
                        _state.value = ScentLensState.Error("Not authenticated. Please sign in.")
                        return@launch
                    }
                }

                println("DEBUG - Adding perfume for user: $actualUserId")

                val perfume = PerfumeCloud(
                    userId = actualUserId,
                    brand = brand,
                    name = name,
                    imageUri = imageUri,
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
                    println("DEBUG - Successfully added perfume")
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

    fun reset() {
        _state.value = ScentLensState.Idle
    }
}