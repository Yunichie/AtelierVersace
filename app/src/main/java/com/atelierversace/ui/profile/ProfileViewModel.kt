package com.atelierversace.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.atelierversace.data.remote.AIPersonalization
import com.atelierversace.data.remote.UserProfile
import com.atelierversace.data.repository.AIPersonalizationRepository
import com.atelierversace.data.repository.AuthRepository
import com.atelierversace.data.repository.CloudPerfumeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

class ProfileViewModel(
    private val authRepository: AuthRepository,
    private val cloudPerfumeRepository: CloudPerfumeRepository,
    private val aiRepository: AIPersonalizationRepository
) : ViewModel() {

    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    val userProfile: StateFlow<UserProfile?> = _userProfile

    private val _personalization = MutableStateFlow<AIPersonalization?>(null)
    val personalization: StateFlow<AIPersonalization?> = _personalization

    private val _wardrobeCount = MutableStateFlow(0)
    val wardrobeCount: StateFlow<Int> = _wardrobeCount

    private val _wishlistCount = MutableStateFlow(0)
    val wishlistCount: StateFlow<Int> = _wishlistCount

    private val _favoritesCount = MutableStateFlow(0)
    val favoritesCount: StateFlow<Int> = _favoritesCount

    private var isRefreshing = false

    init {
        loadProfile()
        loadPersonalization()
        loadStats()
        startPeriodicRefresh()
    }

    private fun startPeriodicRefresh() {
        viewModelScope.launch {
            while (true) {
                delay(3000)
                if (!isRefreshing) {
                    loadStats()
                }
            }
        }
    }

    private fun loadProfile() {
        viewModelScope.launch {
            try {
                val user = authRepository.getCurrentUser()
                if (user != null) {
                    val profileResult = authRepository.getUserProfile(user.id)
                    if (profileResult.isSuccess) {
                        _userProfile.value = profileResult.getOrThrow()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun loadPersonalization() {
        viewModelScope.launch {
            try {
                val user = authRepository.getCurrentUser()
                if (user != null) {
                    val result = aiRepository.getPersonalization(user.id)
                    if (result.isSuccess) {
                        _personalization.value = result.getOrNull()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun loadStats() {
        viewModelScope.launch {
            try {
                isRefreshing = true
                val user = authRepository.getCurrentUser()
                if (user != null) {
                    val wardrobeResult = cloudPerfumeRepository.getWardrobe(user.id)
                    if (wardrobeResult.isSuccess) {
                        _wardrobeCount.value = wardrobeResult.getOrNull()?.size ?: 0
                    }

                    val wishlistResult = cloudPerfumeRepository.getWishlist(user.id)
                    if (wishlistResult.isSuccess) {
                        _wishlistCount.value = wishlistResult.getOrNull()?.size ?: 0
                    }

                    val favoritesResult = cloudPerfumeRepository.getFavorites(user.id)
                    if (favoritesResult.isSuccess) {
                        _favoritesCount.value = favoritesResult.getOrNull()?.size ?: 0
                    }

                    println("DEBUG - Profile stats updated: Wardrobe=${_wardrobeCount.value}, Wishlist=${_wishlistCount.value}, Favorites=${_favoritesCount.value}")
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isRefreshing = false
            }
        }
    }

    fun refresh() {
        loadProfile()
        loadPersonalization()
        loadStats()
    }
}