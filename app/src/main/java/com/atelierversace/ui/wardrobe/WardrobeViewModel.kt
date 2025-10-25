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
import kotlinx.coroutines.flow.combine
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

    private val _recommendationState = MutableStateFlow<RecommendationState>(RecommendationState.Idle)
    val recommendationState: StateFlow<RecommendationState> = _recommendationState

    // Track favorites
    private val _favoriteUpdateTrigger = MutableStateFlow(0)
    private val _favorites = MutableStateFlow<Set<Int>>(emptySet())

    val favorites: StateFlow<Set<Int>> = combine(
        wardrobe,
        _favoriteUpdateTrigger
    ) { perfumes, _ ->
        _favorites.value
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptySet())

    fun isFavorite(perfumeId: Int): Boolean {
        return _favorites.value.contains(perfumeId)
    }

    fun toggleFavorite(perfumeId: Int) {
        _favorites.value = if (_favorites.value.contains(perfumeId)) {
            _favorites.value - perfumeId
        } else {
            _favorites.value + perfumeId
        }
        _favoriteUpdateTrigger.value++
    }

    fun getRecommendation(occasion: String) {
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
                val recommendation = geminiHelper.recommendPerfume(perfumes, weather, occasion)

                if (recommendation == null) {
                    _recommendationState.value = RecommendationState.Error(
                        "Could not generate recommendation"
                    )
                    return@launch
                }

                _recommendationState.value = RecommendationState.Success(
                    recommendation.first,
                    recommendation.second
                )
            } catch (e: Exception) {
                _recommendationState.value = RecommendationState.Error(
                    e.message ?: "Unknown error"
                )
            }
        }
    }

    fun resetRecommendation() {
        _recommendationState.value = RecommendationState.Idle
    }
}