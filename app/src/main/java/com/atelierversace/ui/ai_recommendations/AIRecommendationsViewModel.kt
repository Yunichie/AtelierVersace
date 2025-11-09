package com.atelierversace.ui.ai_recommendations

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.atelierversace.data.remote.PerfumeCloud
import com.atelierversace.data.remote.LayeringRecommendation
import com.atelierversace.data.remote.AIInteraction
import com.atelierversace.data.repository.AIPersonalizationRepository
import com.atelierversace.data.repository.AuthRepository
import com.atelierversace.data.repository.CloudPerfumeRepository
import com.atelierversace.data.repository.WeatherRepository
import com.atelierversace.utils.LayeringCombination
import com.atelierversace.utils.PersonalizedGeminiHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AIRecommendationsViewModel(
    private val authRepository: AuthRepository,
    private val cloudPerfumeRepository: CloudPerfumeRepository,
    private val aiRepository: AIPersonalizationRepository,
    private val weatherRepository: WeatherRepository,
    private val geminiHelper: PersonalizedGeminiHelper
) : ViewModel() {

    private val _todayRecommendation = MutableStateFlow<Triple<PerfumeCloud, String, String>?>(null)
    val todayRecommendation: StateFlow<Triple<PerfumeCloud, String, String>?> = _todayRecommendation

    private val _layeringCombinations = MutableStateFlow<List<LayeringCombination>>(emptyList())
    val layeringCombinations: StateFlow<List<LayeringCombination>> = _layeringCombinations

    private val _savedLayerings = MutableStateFlow<List<LayeringRecommendation>>(emptyList())
    val savedLayerings: StateFlow<List<LayeringRecommendation>> = _savedLayerings

    private val _occasionRecommendations = MutableStateFlow<Map<String, Triple<PerfumeCloud, String, String>>>(emptyMap())
    val occasionRecommendations: StateFlow<Map<String, Triple<PerfumeCloud, String, String>>> = _occasionRecommendations

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    init {
        loadTodayRecommendation()
        loadSavedLayerings()
    }

    private fun loadTodayRecommendation() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                val user = authRepository.getCurrentUser()
                if (user != null) {
                    val wardrobeResult = cloudPerfumeRepository.getWardrobe(user.id)
                    val personalizationResult = aiRepository.getPersonalization(user.id)
                    val weatherResult = weatherRepository.getCurrentWeather()

                    if (wardrobeResult.isSuccess && weatherResult.isSuccess) {
                        val wardrobe = wardrobeResult.getOrThrow()
                        val personalization = personalizationResult.getOrNull()
                        val weather = weatherResult.getOrThrow()

                        val recommendation = geminiHelper.generatePersonalizedRecommendation(
                            wardrobe,
                            personalization,
                            weather
                        )

                        _todayRecommendation.value = recommendation

                        if (recommendation != null) {
                            trackInteraction(
                                userId = user.id,
                                type = "daily_recommendation",
                                query = "Weather: ${weather.description}, ${weather.temperature}°C",
                                result = "${recommendation.first.brand} ${recommendation.first.name}"
                            )
                        }

                        if (personalization == null && wardrobe.isNotEmpty()) {
                            val newPersonalization = aiRepository.analyzeUserPreferences(user.id, wardrobe)
                            aiRepository.updatePersonalization(newPersonalization)
                        }
                    } else {
                        _errorMessage.value = "Unable to fetch wardrobe or weather data"
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _errorMessage.value = "Failed to load recommendation: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun generateLayering() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                val user = authRepository.getCurrentUser()
                if (user != null) {
                    val wardrobeResult = cloudPerfumeRepository.getWardrobe(user.id)
                    val personalizationResult = aiRepository.getPersonalization(user.id)

                    if (wardrobeResult.isSuccess) {
                        val wardrobe = wardrobeResult.getOrThrow()

                        if (wardrobe.size < 2) {
                            _errorMessage.value = "You need at least 2 perfumes to generate layering combinations"
                            return@launch
                        }

                        val personalization = personalizationResult.getOrNull()

                        val combinations = geminiHelper.generateLayeringCombinations(
                            wardrobe,
                            personalization
                        )

                        _layeringCombinations.value = combinations

                        trackInteraction(
                            userId = user.id,
                            type = "layering_generation",
                            query = "Wardrobe size: ${wardrobe.size}",
                            result = "${combinations.size} combinations generated"
                        )
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _errorMessage.value = "Failed to generate layering: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun saveLayering(combination: LayeringCombination) {
        viewModelScope.launch {
            try {
                val user = authRepository.getCurrentUser()
                if (user != null) {
                    val layering = LayeringRecommendation(
                        userId = user.id,
                        basePerfumeId = combination.basePerfume.id ?: "",
                        layerPerfumeId = combination.layerPerfume.id ?: "",
                        occasion = combination.occasion,
                        reasoning = "${combination.name}\n\n${combination.description}",
                        createdAt = System.currentTimeMillis().toString()
                    )

                    val result = aiRepository.saveLayeringRecommendation(layering)

                    if (result.isSuccess) {
                        loadSavedLayerings()

                        trackInteraction(
                            userId = user.id,
                            type = "layering_saved",
                            query = combination.name,
                            result = "${combination.basePerfume.brand} + ${combination.layerPerfume.brand}"
                        )
                    } else {
                        _errorMessage.value = "Failed to save layering"
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _errorMessage.value = "Error saving layering: ${e.message}"
            }
        }
    }

    fun removeSavedLayering(layeringId: String) {
        viewModelScope.launch {
            try {
                val result = aiRepository.deleteLayeringRecommendation(layeringId)

                if (result.isSuccess) {
                    loadSavedLayerings()
                } else {
                    _errorMessage.value = "Failed to remove layering"
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _errorMessage.value = "Error removing layering: ${e.message}"
            }
        }
    }

    private fun loadSavedLayerings() {
        viewModelScope.launch {
            try {
                val user = authRepository.getCurrentUser()
                if (user != null) {
                    val result = aiRepository.getLayeringHistory(user.id)

                    if (result.isSuccess) {
                        _savedLayerings.value = result.getOrNull() ?: emptyList()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                println("ERROR loading saved layerings: ${e.message}")
            }
        }
    }

    fun getOccasionRecommendation(occasion: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                val user = authRepository.getCurrentUser()
                if (user != null) {
                    val wardrobeResult = cloudPerfumeRepository.getWardrobe(user.id)
                    val personalizationResult = aiRepository.getPersonalization(user.id)
                    val weatherResult = weatherRepository.getCurrentWeather()

                    if (wardrobeResult.isSuccess && weatherResult.isSuccess) {
                        val wardrobe = wardrobeResult.getOrThrow()
                        val personalization = personalizationResult.getOrNull()
                        val weather = weatherResult.getOrThrow()

                        val recommendation = geminiHelper.generatePersonalizedRecommendation(
                            wardrobe,
                            personalization,
                            weather,
                            occasion
                        )

                        if (recommendation != null) {
                            val current = _occasionRecommendations.value.toMutableMap()
                            current[occasion] = recommendation
                            _occasionRecommendations.value = current

                            trackInteraction(
                                userId = user.id,
                                type = "occasion_recommendation",
                                query = occasion,
                                result = "${recommendation.first.brand} ${recommendation.first.name}"
                            )
                        } else {
                            _errorMessage.value = "No suitable perfume found for this occasion"
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _errorMessage.value = "Failed to get recommendation: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private suspend fun trackInteraction(
        userId: String,
        type: String,
        query: String,
        result: String
    ) {
        try {
            val interaction = AIInteraction(
                userId = userId,
                interactionType = type,
                query = query,
                result = result,
                wasHelpful = null,
                timestamp = System.currentTimeMillis().toString()
            )

            aiRepository.saveInteraction(interaction)
        } catch (e: Exception) {
            println("ERROR tracking interaction: ${e.message}")
        }
    }

    fun markInteractionHelpful(interactionId: String, helpful: Boolean) {
        viewModelScope.launch {
            try {
                aiRepository.updateInteractionFeedback(interactionId, helpful)
            } catch (e: Exception) {
                println("ERROR updating feedback: ${e.message}")
            }
        }
    }

    fun refreshRecommendations() {
        loadTodayRecommendation()
        if (_layeringCombinations.value.isNotEmpty()) {
            generateLayering()
        }
        loadSavedLayerings()
    }

    fun clearError() {
        _errorMessage.value = null
    }
}