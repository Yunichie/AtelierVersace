package com.atelierversace.ui.discovery

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.atelierversace.data.model.PersonaProfile
import com.atelierversace.data.remote.PerfumeCloud
import com.atelierversace.data.repository.CloudPerfumeRepository
import com.atelierversace.data.repository.AIPersonalizationRepository
import com.atelierversace.data.repository.AuthRepository
import com.atelierversace.utils.PersonalizedGeminiHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

sealed class DiscoveryState {
    object Idle : DiscoveryState()
    object Loading : DiscoveryState()
    data class Success(val recommendations: List<PersonaProfile>) : DiscoveryState()
    data class Error(val message: String) : DiscoveryState()
}

class DiscoveryViewModel(
    private val cloudRepository: CloudPerfumeRepository,
    private val geminiHelper: PersonalizedGeminiHelper,
    private val aiRepository: AIPersonalizationRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _wishlist = MutableStateFlow<List<PerfumeCloud>>(emptyList())
    val wishlist: StateFlow<List<PerfumeCloud>> = _wishlist

    private val _discoveryState = MutableStateFlow<DiscoveryState>(DiscoveryState.Idle)
    val discoveryState: StateFlow<DiscoveryState> = _discoveryState

    private val _wishlistItems = MutableStateFlow<Set<String>>(emptySet())
    val wishlistItems: StateFlow<Set<String>> = _wishlistItems

    private var currentUserId: String? = null

    init {
        viewModelScope.launch {
            val user = authRepository.getCurrentUser()
            user?.let {
                println("DEBUG - Auto-initializing DiscoveryViewModel with userId: ${it.id}")
                initialize(it.id)
            }
        }
    }

    fun initialize(userId: String) {
        println("DEBUG - Initializing DiscoveryViewModel with userId: $userId")
        currentUserId = userId
        loadWishlist(userId)
    }

    private fun loadWishlist(userId: String) {
        viewModelScope.launch {
            println("DEBUG - Loading wishlist for userId: $userId")
            val result = cloudRepository.getWishlist(userId)
            if (result.isSuccess) {
                val items = result.getOrNull() ?: emptyList()
                _wishlist.value = items
                _wishlistItems.value = items.map { "${it.brand}|${it.name}" }.toSet()
                println("DEBUG - Loaded ${items.size} wishlist items")
                items.forEach {
                    println("DEBUG - Wishlist item: ${it.brand} | ${it.name} | ID: ${it.id}")
                }
            } else {
                println("ERROR - Failed to load wishlist: ${result.exceptionOrNull()?.message}")
                result.exceptionOrNull()?.printStackTrace()
            }
        }
    }

    fun isInWishlist(brand: String, name: String): Boolean {
        val key = "$brand|$name"
        val inWishlist = _wishlistItems.value.contains(key)
        println("DEBUG - isInWishlist($brand, $name) = $inWishlist")
        return inWishlist
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
                e.printStackTrace()
                _discoveryState.value = DiscoveryState.Error(
                    e.message ?: "Unknown error"
                )
            }
        }
    }

    fun toggleWishlist(profile: PersonaProfile) {
        val key = "${profile.brand}|${profile.name}"

        val userId = currentUserId ?: authRepository.getCurrentUser()?.id ?: run {
            println("ERROR: Cannot toggle wishlist - no user ID available")
            return
        }

        println("DEBUG - toggleWishlist called for: $key, userId: $userId")

        viewModelScope.launch {
            try {
                val currentSet = _wishlistItems.value
                val isCurrentlyInWishlist = currentSet.contains(key)

                println("DEBUG - Currently in wishlist: $isCurrentlyInWishlist")

                _wishlistItems.value = if (isCurrentlyInWishlist) {
                    currentSet - key
                } else {
                    currentSet + key
                }

                if (isCurrentlyInWishlist) {
                    val existing = _wishlist.value.find {
                        it.brand == profile.brand && it.name == profile.name
                    }

                    if (existing != null && existing.id != null) {
                        println("DEBUG - Removing from wishlist, id: ${existing.id}")

                        val result = cloudRepository.deletePerfume(existing.id)
                        if (result.isSuccess) {
                            println("DEBUG - Successfully removed from wishlist")
                            delay(300)
                            loadWishlist(userId)
                            updateAIPersonalization(userId)
                        } else {
                            _wishlistItems.value = currentSet
                            println("ERROR: Failed to remove from wishlist: ${result.exceptionOrNull()?.message}")
                            result.exceptionOrNull()?.printStackTrace()
                        }
                    } else {
                        println("ERROR: Could not find existing perfume to remove")
                        _wishlistItems.value = currentSet
                    }
                } else {
                    println("DEBUG - Adding to wishlist")

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
                        isFavorite = false,
                        timestamp = System.currentTimeMillis().toString()
                    )

                    println("DEBUG - Perfume object to add: $perfume")

                    val result = cloudRepository.addPerfume(perfume)
                    if (result.isSuccess) {
                        println("DEBUG - Successfully added to wishlist")
                        delay(300)
                        loadWishlist(userId)
                        updateAIPersonalization(userId)
                    } else {
                        _wishlistItems.value = currentSet
                        println("ERROR: Failed to add to wishlist: ${result.exceptionOrNull()?.message}")
                        result.exceptionOrNull()?.printStackTrace()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                println("ERROR: Exception in toggleWishlist: ${e.message}")
                loadWishlist(userId)
            }
        }
    }

    fun removeFromWishlist(perfume: PerfumeCloud) {
        val key = "${perfume.brand}|${perfume.name}"

        val userId = currentUserId ?: authRepository.getCurrentUser()?.id ?: return

        println("DEBUG - removeFromWishlist called for: $key")

        viewModelScope.launch {
            try {
                _wishlistItems.value = _wishlistItems.value - key

                if (perfume.id != null) {
                    val result = cloudRepository.deletePerfume(perfume.id)
                    if (result.isSuccess) {
                        println("DEBUG - Successfully removed from wishlist")
                        delay(300)
                        loadWishlist(userId)
                        updateAIPersonalization(userId)
                    } else {
                        println("ERROR - Failed to remove: ${result.exceptionOrNull()?.message}")
                        loadWishlist(userId)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                println("ERROR - Exception in removeFromWishlist: ${e.message}")
                loadWishlist(userId)
            }
        }
    }

    private suspend fun updateAIPersonalization(userId: String) {
        try {
            println("DEBUG - Updating AI personalization after wishlist change")

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

    fun reset() {
        _discoveryState.value = DiscoveryState.Idle
    }
}