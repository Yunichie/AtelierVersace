package com.atelierversace.ui.discovery

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.atelierversace.data.model.PersonaProfile
import com.atelierversace.data.remote.PerfumeCloud
import com.atelierversace.data.repository.CloudPerfumeRepository
import com.atelierversace.data.repository.AIPersonalizationRepository
import com.atelierversace.utils.PersonalizedGeminiHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class DiscoveryState {
    object Idle : DiscoveryState()
    object Loading : DiscoveryState()
    data class Success(val recommendations: List<PersonaProfile>) : DiscoveryState()
    data class Error(val message: String) : DiscoveryState()
}

class DiscoveryViewModel(
    private val cloudRepository: CloudPerfumeRepository,
    private val geminiHelper: PersonalizedGeminiHelper,
    private val aiRepository: AIPersonalizationRepository
) : ViewModel() {

    private val _wishlist = MutableStateFlow<List<PerfumeCloud>>(emptyList())
    val wishlist: StateFlow<List<PerfumeCloud>> = _wishlist

    private val _discoveryState = MutableStateFlow<DiscoveryState>(DiscoveryState.Idle)
    val discoveryState: StateFlow<DiscoveryState> = _discoveryState

    private val _wishlistItems = MutableStateFlow<Set<String>>(emptySet())
    val wishlistItems: StateFlow<Set<String>> = _wishlistItems

    private var currentUserId: String? = null

    fun initialize(userId: String) {
        currentUserId = userId
        loadWishlist(userId)
    }

    private fun loadWishlist(userId: String) {
        viewModelScope.launch {
            val result = cloudRepository.getWishlist(userId)
            if (result.isSuccess) {
                val items = result.getOrNull() ?: emptyList()
                _wishlist.value = items
                _wishlistItems.value = items.map { "${it.brand}|${it.name}" }.toSet()
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
                val personalization = if (currentUserId != null) {
                    aiRepository.getPersonalization(currentUserId!!).getOrNull()
                } else null

                val recommendations = geminiHelper.discoverPersonalizedPerfumes(
                    query,
                    personalization
                )

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
        val key = "${profile.brand}|${profile.name}"
        val userId = currentUserId ?: return

        val currentSet = _wishlistItems.value
        if (currentSet.contains(key)) {
            _wishlistItems.value = currentSet - key
        } else {
            _wishlistItems.value = currentSet + key
        }

        viewModelScope.launch {
            val existing = _wishlist.value.find {
                it.brand == profile.brand && it.name == profile.name
            }

            if (existing != null) {
                cloudRepository.deletePerfume(existing.id ?: "")
            } else {
                val perfume = PerfumeCloud(
                    userId = userId,
                    brand = profile.brand,
                    name = profile.name,
                    imageUri = "",
                    analogy = profile.analogy,
                    coreFeeling = profile.coreFeeling,
                    localContext = profile.localContext,
                    topNotes = profile.topNotes.joinToString(", "),
                    middleNotes = profile.middleNotes.joinToString(", "),
                    baseNotes = profile.baseNotes.joinToString(", "),
                    isWishlist = true,
                    timestamp = System.currentTimeMillis().toString()
                )
                cloudRepository.addPerfume(perfume)
            }

            loadWishlist(userId)
        }
    }

    fun removeFromWishlist(perfume: PerfumeCloud) {
        val key = "${perfume.brand}|${perfume.name}"
        _wishlistItems.value = _wishlistItems.value - key

        viewModelScope.launch {
            cloudRepository.deletePerfume(perfume.id ?: "")
            currentUserId?.let { loadWishlist(it) }
        }
    }

    fun reset() {
        _discoveryState.value = DiscoveryState.Idle
    }
}