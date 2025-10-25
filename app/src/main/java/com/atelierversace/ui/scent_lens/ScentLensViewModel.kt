package com.atelierversace.ui.scent_lens

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.atelierversace.data.model.Perfume
import com.atelierversace.data.model.PersonaProfile
import com.atelierversace.data.repository.PerfumeRepository
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
    private val repository: PerfumeRepository,
    private val geminiHelper: GeminiHelper
) : ViewModel() {

    private val _state = MutableStateFlow<ScentLensState>(ScentLensState.Idle)
    val state: StateFlow<ScentLensState> = _state

    fun analyzePerfume(bitmap: Bitmap, imageUri: String) {
        viewModelScope.launch {
            _state.value = ScentLensState.Loading

            try {
                val identification = geminiHelper.identifyPerfume(bitmap)

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

    fun addToWardrobe(brand: String, name: String, profile: PersonaProfile, imageUri: String, onComplete: () -> Unit) {
        viewModelScope.launch {
            val perfume = Perfume(
                brand = brand,
                name = name,
                imageUri = imageUri,
                analogy = profile.analogy,
                coreFeeling = profile.coreFeeling,
                localContext = profile.localContext,
                topNotes = profile.topNotes.joinToString(", "),
                middleNotes = profile.middleNotes.joinToString(", "),
                baseNotes = profile.baseNotes.joinToString(", "),
                isWishlist = false
            )
            repository.addPerfume(perfume)
            _state.value = ScentLensState.Idle
            onComplete()
        }
    }

    fun reset() {
        _state.value = ScentLensState.Idle
    }
}
