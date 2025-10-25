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

    private val _wishlistItems = MutableStateFlow<Set<String>>(emptySet())
    val wishlistItems: StateFlow<Set<String>> = _wishlistItems

    init {
        viewModelScope.launch {
            wishlist.collect { perfumes ->
                _wishlistItems.value = perfumes.map { "${it.brand}|${it.name}" }.toSet()
            }
        }
    }

    fun isInWishlist(brand: String, name: String): Boolean {
        return _wishlistItems.value.contains("$brand|$name")
    }

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

    fun toggleWishlist(profile: PersonaProfile) {
        viewModelScope.launch {
            val existing = wishlist.value.find {
                it.brand == profile.brand && it.name == profile.name
            }

            if (existing != null) {
                repository.deletePerfume(existing)
            } else {
                val perfume = Perfume(
                    brand = profile.brand,
                    name = profile.name,
                    imageUri = "",
                    analogy = profile.analogy,
                    coreFeeling = profile.coreFeeling,
                    localContext = profile.localContext,
                    topNotes = profile.topNotes.joinToString(", "),
                    middleNotes = profile.middleNotes.joinToString(", "),
                    baseNotes = profile.baseNotes.joinToString(", "),
                    isWishlist = true
                )
                repository.addPerfume(perfume)
            }
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
