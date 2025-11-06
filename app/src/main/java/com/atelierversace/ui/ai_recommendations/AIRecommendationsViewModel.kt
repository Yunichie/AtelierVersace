package com.atelierversace.ui.ai_recommendations

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.atelierversace.data.remote.PerfumeCloud
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

    private val _occasionRecommendations = MutableStateFlow<Map<String, Triple<PerfumeCloud, String, String>>>(emptyMap())
    val occasionRecommendations: StateFlow<Map<String, Triple<PerfumeCloud, String, String>>> = _occasionRecommendations

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    init {
        loadTodayRecommendation()
    }

    private fun loadTodayRecommendation() {
        viewModelScope.launch {
            _isLoading.value = true
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

                        if (personalization == null && wardrobe.isNotEmpty()) {
                            val newPersonalization = aiRepository.analyzeUserPreferences(user.id, wardrobe)
                            aiRepository.updatePersonalization(newPersonalization)
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun generateLayering() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val user = authRepository.getCurrentUser()
                if (user != null) {
                    val wardrobeResult = cloudPerfumeRepository.getWardrobe(user.id)
                    val personalizationResult = aiRepository.getPersonalization(user.id)

                    if (wardrobeResult.isSuccess) {
                        val wardrobe = wardrobeResult.getOrThrow()
                        val personalization = personalizationResult.getOrNull()

                        val combinations = geminiHelper.generateLayeringCombinations(
                            wardrobe,
                            personalization
                        )

                        _layeringCombinations.value = combinations
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun getOccasionRecommendation(occasion: String) {
        viewModelScope.launch {
            _isLoading.value = true
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
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun refreshRecommendations() {
        loadTodayRecommendation()
        if (_layeringCombinations.value.isNotEmpty()) {
            generateLayering()
        }
    }
}