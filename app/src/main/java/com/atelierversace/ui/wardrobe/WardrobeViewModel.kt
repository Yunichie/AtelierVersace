package com.atelierversace.ui.wardrobe

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.atelierversace.data.remote.PerfumeCloud
import com.atelierversace.data.repository.CloudPerfumeRepository
import com.atelierversace.data.repository.WeatherRepository
import com.atelierversace.data.repository.AIPersonalizationRepository
import com.atelierversace.utils.PersonalizedGeminiHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class RecommendationState {
    object Idle : RecommendationState()
    object Loading : RecommendationState()
    data class Success(val perfume: PerfumeCloud, val reason: String) : RecommendationState()
    data class Error(val message: String) : RecommendationState()
}

class WardrobeViewModel(
    private val cloudRepository: CloudPerfumeRepository,
    private val weatherRepository: WeatherRepository,
    private val geminiHelper: PersonalizedGeminiHelper,
    private val aiRepository: AIPersonalizationRepository
) : ViewModel() {

    private val _wardrobe = MutableStateFlow<List<PerfumeCloud>>(emptyList())
    val wardrobe: StateFlow<List<PerfumeCloud>> = _wardrobe

    private val _wishlist = MutableStateFlow<List<PerfumeCloud>>(emptyList())
    val wishlist: StateFlow<List<PerfumeCloud>> = _wishlist

    private val _recommendationState = MutableStateFlow<RecommendationState>(RecommendationState.Idle)
    val recommendationState: StateFlow<RecommendationState> = _recommendationState

    private val _favorites = MutableStateFlow<Set<String>>(emptySet())
    val favorites: StateFlow<Set<String>> = _favorites

    private var currentUserId: String? = null

    fun initialize(userId: String) {
        currentUserId = userId
        loadWardrobe(userId)
        loadWishlist(userId)
    }

    private fun loadWardrobe(userId: String) {
        viewModelScope.launch {
            val result = cloudRepository.getWardrobe(userId)
            if (result.isSuccess) {
                _wardrobe.value = result.getOrNull() ?: emptyList()
            }
        }
    }

    private fun loadWishlist(userId: String) {
        viewModelScope.launch {
            val result = cloudRepository.getWishlist(userId)
            if (result.isSuccess) {
                val wishlistItems = result.getOrNull() ?: emptyList()
                _wishlist.value = wishlistItems
                _favorites.value = wishlistItems.map { it.id ?: "" }.toSet()
            }
        }
    }

    fun isFavorite(perfumeId: String): Boolean {
        return _favorites.value.contains(perfumeId)
    }

    fun toggleFavorite(perfumeId: String) {
        viewModelScope.launch {
            val perfume = _wardrobe.value.find { it.id == perfumeId } ?: return@launch

            val isFavorite = _favorites.value.contains(perfumeId)

            cloudRepository.toggleFavorite(perfumeId, !isFavorite)

            if (isFavorite) {
                _favorites.value = _favorites.value - perfumeId
            } else {
                _favorites.value = _favorites.value + perfumeId
            }

            currentUserId?.let { loadWishlist(it) }
        }
    }

    fun getRecommendation(userQuery: String) {
        viewModelScope.launch {
            _recommendationState.value = RecommendationState.Loading

            try {
                val weatherResult = weatherRepository.getCurrentWeather()
                val perfumes = _wardrobe.value
                val userId = currentUserId

                if (perfumes.isEmpty()) {
                    _recommendationState.value = RecommendationState.Error(
                        "Your wardrobe is empty. Add some perfumes first!"
                    )
                    return@launch
                }

                if (weatherResult.isFailure) {
                    _recommendationState.value = RecommendationState.Error(
                        "Could not fetch weather data"
                    )
                    return@launch
                }

                val weather = weatherResult.getOrThrow()

                val personalization = if (userId != null) {
                    aiRepository.getPersonalization(userId).getOrNull()
                } else null

                val recommendation = geminiHelper.generatePersonalizedRecommendation(
                    perfumes,
                    personalization,
                    weather,
                    userQuery
                )

                if (recommendation == null) {
                    _recommendationState.value = RecommendationState.Error(
                        "Could not generate recommendation. Please try again."
                    )
                    return@launch
                }

                val (perfume, reason, _) = recommendation

                _recommendationState.value = RecommendationState.Success(perfume, reason)
            } catch (e: Exception) {
                _recommendationState.value = RecommendationState.Error(
                    e.message ?: "Unknown error occurred"
                )
            }
        }
    }

    fun resetRecommendation() {
        _recommendationState.value = RecommendationState.Idle
    }
}