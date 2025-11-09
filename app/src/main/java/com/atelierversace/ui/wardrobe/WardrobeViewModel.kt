package com.atelierversace.ui.wardrobe

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.atelierversace.data.remote.PerfumeCloud
import com.atelierversace.data.remote.LayeringRecommendation
import com.atelierversace.data.repository.CloudPerfumeRepository
import com.atelierversace.data.repository.WeatherRepository
import com.atelierversace.data.repository.AIPersonalizationRepository
import com.atelierversace.utils.PersonalizedGeminiHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

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

    private val _recommendationState = MutableStateFlow<RecommendationState>(RecommendationState.Idle)
    val recommendationState: StateFlow<RecommendationState> = _recommendationState

    private val _favoriteIds = MutableStateFlow<Set<String>>(emptySet())
    val favoriteIds: StateFlow<Set<String>> = _favoriteIds

    private val _savedLayerings = MutableStateFlow<List<Triple<LayeringRecommendation, PerfumeCloud?, PerfumeCloud?>>>(emptyList())
    val savedLayerings: StateFlow<List<Triple<LayeringRecommendation, PerfumeCloud?, PerfumeCloud?>>> = _savedLayerings

    private val _isLoadingLayerings = MutableStateFlow(false)
    val isLoadingLayerings: StateFlow<Boolean> = _isLoadingLayerings

    private var currentUserId: String? = null

    fun initialize(userId: String) {
        currentUserId = userId
        loadWardrobe(userId)
        loadFavorites(userId)
        loadSavedLayerings(userId)
    }

    private fun loadWardrobe(userId: String) {
        viewModelScope.launch {
            val result = cloudRepository.getWardrobe(userId)
            if (result.isSuccess) {
                _wardrobe.value = result.getOrNull() ?: emptyList()
                println("DEBUG - Loaded ${_wardrobe.value.size} wardrobe items")
            }
        }
    }

    private fun loadFavorites(userId: String) {
        viewModelScope.launch {
            val result = cloudRepository.getFavorites(userId)
            if (result.isSuccess) {
                val favoriteItems = result.getOrNull() ?: emptyList()
                _favoriteIds.value = favoriteItems.map { it.id ?: "" }.toSet()
                println("DEBUG - Loaded ${_favoriteIds.value.size} favorites")
            }
        }
    }

    fun loadSavedLayerings(userId: String) {
        viewModelScope.launch {
            _isLoadingLayerings.value = true
            try {
                val layeringsResult = aiRepository.getLayeringHistory(userId)

                if (layeringsResult.isSuccess) {
                    val layerings = layeringsResult.getOrThrow()
                    val wardrobe = _wardrobe.value

                    val enriched = layerings.map { layering ->
                        val basePerfume = wardrobe.find { it.id == layering.basePerfumeId }
                        val layerPerfume = wardrobe.find { it.id == layering.layerPerfumeId }
                        Triple(layering, basePerfume, layerPerfume)
                    }.filter { it.second != null && it.third != null }

                    _savedLayerings.value = enriched
                    println("DEBUG - Loaded ${enriched.size} saved layerings")
                } else {
                    println("ERROR - Failed to load layerings: ${layeringsResult.exceptionOrNull()?.message}")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                println("ERROR - Exception loading layerings: ${e.message}")
            } finally {
                _isLoadingLayerings.value = false
            }
        }
    }

    fun removeSavedLayering(layeringId: String) {
        viewModelScope.launch {
            try {
                val result = aiRepository.deleteLayeringRecommendation(layeringId)

                if (result.isSuccess) {
                    currentUserId?.let { loadSavedLayerings(it) }
                    println("DEBUG - Removed saved layering: $layeringId")
                } else {
                    println("ERROR - Failed to remove layering: ${result.exceptionOrNull()?.message}")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                println("ERROR - Exception removing layering: ${e.message}")
            }
        }
    }

    fun toggleFavorite(perfumeId: String) {
        viewModelScope.launch {
            val currentFavorites = _favoriteIds.value
            val isFavorite = currentFavorites.contains(perfumeId)

            println("DEBUG - toggleFavorite: $perfumeId, current state: $isFavorite")

            _favoriteIds.value = if (isFavorite) {
                currentFavorites - perfumeId
            } else {
                currentFavorites + perfumeId
            }

            val result = cloudRepository.toggleFavorite(perfumeId, !isFavorite)

            if (result.isSuccess) {
                println("DEBUG - Toggle favorite successful")
                delay(300)
                currentUserId?.let {
                    loadFavorites(it)
                    updateAIPersonalization(it)
                }
            } else {
                println("ERROR - Toggle favorite failed: ${result.exceptionOrNull()?.message}")
                _favoriteIds.value = currentFavorites
            }
        }
    }

    fun deletePerfume(perfumeId: String) {
        viewModelScope.launch {
            try {
                println("DEBUG - Deleting perfume: $perfumeId")
                val result = cloudRepository.deletePerfume(perfumeId)

                if (result.isSuccess) {
                    println("DEBUG - Delete successful")
                    currentUserId?.let { userId ->
                        loadWardrobe(userId)
                        loadFavorites(userId)
                        loadSavedLayerings(userId)
                        updateAIPersonalization(userId)
                    }
                } else {
                    println("ERROR - Delete failed: ${result.exceptionOrNull()?.message}")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                println("ERROR - Exception deleting perfume: ${e.message}")
            }
        }
    }

    private suspend fun updateAIPersonalization(userId: String) {
        try {
            println("DEBUG - Updating AI personalization")
            val wardrobeResult = cloudRepository.getWardrobe(userId)

            if (wardrobeResult.isSuccess) {
                val perfumes = wardrobeResult.getOrNull() ?: emptyList()

                if (perfumes.isNotEmpty()) {
                    val newPersonalization = aiRepository.analyzeUserPreferences(userId, perfumes)
                    val saveResult = aiRepository.updatePersonalization(newPersonalization)

                    if (saveResult.isSuccess) {
                        println("DEBUG - AI personalization updated")
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            println("ERROR - Failed to update AI personalization: ${e.message}")
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