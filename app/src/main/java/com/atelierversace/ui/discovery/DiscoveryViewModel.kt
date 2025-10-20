package com.atelierversace.ui.discovery

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.atelierversace.data.model.PersonaProfile
import com.atelierversace.data.model.Perfume
import com.atelierversace.data.repository.PerfumeRepository
import com.atelierversace.utils.GeminiHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed class DiscoveryState {
    object Idle : DiscoveryState()
    object Loading : DiscoveryState()
    data class Success(val recommendations: List<PersonaProfile>) : DiscoveryState()
    data class Error(val message: String) : DiscoveryState()
}

class DiscoveryViewModel(
    private val repository: PerfumeRepository,
    private val geminiHelper: GeminiHelper
) : ViewModel() {

    val wishlist = repository.getWishlist()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val _discoveryState = MutableStateFlow<DiscoveryState>(DiscoveryState.Idle)
    val discoveryState: StateFlow<DiscoveryState> = _discoveryState

    fun searchPerfumes(query: String) {
        if (query.isBlank()) {
            _discoveryState.value = DiscoveryState.Idle
            return
        }

        viewModelScope.launch {
            _discoveryState.value = DiscoveryState.Loading

            try {
                val recommendations = geminiHelper.discoverPerfumes(query)

                if (recommendations.isEmpty()) {
                    _discoveryState.value = DiscoveryState.Error(
                        "No recommendations found. Try a different query."
                    )
                } else {
                    _discoveryState.value = DiscoveryState.Success(recommendations)
                }
            } catch (e: Exception) {
                _discoveryState.value = DiscoveryState.Error(
                    e.message ?: "Unknown error"
                )
            }
        }
    }

    fun addToWishlist(profile: PersonaProfile) {
        viewModelScope.launch {
            val perfume = Perfume(
                brand = profile.brand,
                name = profile.name,
                imageUri = "", // Placeholder
                analogy = profile.analogy,
                coreFeeling = profile.coreFeeling,
                localContext = profile.localContext,
                isWishlist = true
            )
            repository.addPerfume(perfume)
        }
    }

    fun removeFromWishlist(perfume: Perfume) {
        viewModelScope.launch {
            repository.deletePerfume(perfume)
        }
    }

    fun reset() {
        _discoveryState.value = DiscoveryState.Idle
    }
}