package com.atelierversace.ui.scent_lens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.atelierversace.data.remote.PerfumeCloud
import com.atelierversace.data.model.PersonaProfile
import com.atelierversace.data.repository.CloudPerfumeRepository
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
    private val geminiHelper: GeminiHelper
) : ViewModel() {

    private val _state = MutableStateFlow<ScentLensState>(ScentLensState.Idle)
    val state: StateFlow<ScentLensState> = _state

    fun analyzePerfume(imageBytes: ByteArray, imageUri: String) {
        viewModelScope.launch {
            _state.value = ScentLensState.Loading

            try {
                val identification = geminiHelper.identifyPerfume(imageBytes)

                if (identification == null) {
                    _state.value = ScentLensState.Error("Could not identify perfume")
                    return@launch
                }

                val (brand, name) = identification
                val profile = geminiHelper.generatePersonaProfile(brand, name)

                _state.value = ScentLensState.Success(brand, name, profile, imageUri)
            } catch (e: Exception) {
                _state.value = ScentLensState.Error(e.message ?: "Unknown error")
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
                val perfume = PerfumeCloud(
                    userId = userId,
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
                    timestamp = System.currentTimeMillis().toString()
                )

                val result = cloudRepository.addPerfume(perfume)

                if (result.isSuccess) {
                    _state.value = ScentLensState.Idle
                    onComplete()
                } else {
                    _state.value = ScentLensState.Error("Failed to add perfume")
                }
            } catch (e: Exception) {
                _state.value = ScentLensState.Error(e.message ?: "Failed to add perfume")
            }
        }
    }

    fun reset() {
        _state.value = ScentLensState.Idle
    }
}