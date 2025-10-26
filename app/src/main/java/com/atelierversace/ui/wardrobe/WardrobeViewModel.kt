package com.atelierversace.ui.wardrobe

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.atelierversace.data.model.Perfume
import com.atelierversace.data.repository.PerfumeRepository
import com.atelierversace.data.repository.WeatherRepository
import com.atelierversace.utils.GeminiHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed class RecommendationState {
    object Idle : RecommendationState()
    object Loading : RecommendationState()
    data class Success(val perfume: Perfume, val reason: String) : RecommendationState()
    data class Error(val message: String) : RecommendationState()
}

class WardrobeViewModel(
    private val perfumeRepository: PerfumeRepository,
    private val weatherRepository: WeatherRepository,
    private val geminiHelper: GeminiHelper
) : ViewModel() {

    val wardrobe = perfumeRepository.getWardrobe()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val wishlist = perfumeRepository.getWishlist()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val _recommendationState = MutableStateFlow<RecommendationState>(RecommendationState.Idle)
    val recommendationState: StateFlow<RecommendationState> = _recommendationState

    private val _favorites = MutableStateFlow<Set<Int>>(emptySet())
    val favorites: StateFlow<Set<Int>> = _favorites

    init {
        viewModelScope.launch {
            wishlist.collect { wishlistPerfumes ->
                _favorites.value = wishlistPerfumes.map { it.id }.toSet()
            }
        }
    }

    fun isFavorite(perfumeId: Int): Boolean {
        return _favorites.value.contains(perfumeId)
    }

    fun toggleFavorite(perfumeId: Int) {
        viewModelScope.launch {
            val perfume = wardrobe.value.find { it.id == perfumeId } ?: return@launch

            val existingInWishlist = wishlist.value.find {
                it.brand == perfume.brand && it.name == perfume.name
            }

            if (existingInWishlist != null) {
                perfumeRepository.deletePerfume(existingInWishlist)
                _favorites.value = _favorites.value - perfumeId
            } else {
                val wishlistPerfume = perfume.copy(
                    id = 0,
                    isWishlist = true,
                    timestamp = System.currentTimeMillis()
                )
                perfumeRepository.addPerfume(wishlistPerfume)
                _favorites.value = _favorites.value + perfumeId
            }
        }
    }

    fun getRecommendation(userQuery: String) {
        viewModelScope.launch {
            _recommendationState.value = RecommendationState.Loading

            try {
                val weatherResult = weatherRepository.getCurrentWeather()
                val perfumes = wardrobe.value

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
                val recommendation = geminiHelper.recommendPerfumeWithQuery(
                    perfumes,
                    weather,
                    userQuery
                )

                if (recommendation == null) {
                    _recommendationState.value = RecommendationState.Error(
                        "Could not generate recommendation. Please try again."
                    )
                    return@launch
                }

                _recommendationState.value = RecommendationState.Success(
                    recommendation.first,
                    recommendation.second
                )
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