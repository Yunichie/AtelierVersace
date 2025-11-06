package com.atelierversace

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.atelierversace.data.repository.*
import com.atelierversace.ui.ai_recommendations.AIRecommendationsViewModel
import com.atelierversace.ui.auth.AuthViewModel
import com.atelierversace.ui.discovery.DiscoveryViewModel
import com.atelierversace.ui.profile.ProfileViewModel
import com.atelierversace.ui.scent_lens.ScentLensViewModel
import com.atelierversace.ui.theme.AtelierVersaceTheme
import com.atelierversace.ui.wardrobe.WardrobeViewModel
import com.atelierversace.utils.GeminiHelper
import com.atelierversace.utils.PersonalizedGeminiHelper
import com.atelierversace.utils.WeatherHelper

class MainActivity : ComponentActivity() {

    private lateinit var authRepository: AuthRepository
    private lateinit var cloudPerfumeRepository: CloudPerfumeRepository
    private lateinit var aiPersonalizationRepository: AIPersonalizationRepository
    private lateinit var weatherRepository: WeatherRepository

    private lateinit var geminiHelper: GeminiHelper
    private lateinit var personalizedGeminiHelper: PersonalizedGeminiHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        authRepository = AuthRepository()
        cloudPerfumeRepository = CloudPerfumeRepository()
        aiPersonalizationRepository = AIPersonalizationRepository()
        weatherRepository = WeatherRepository(WeatherHelper())

        geminiHelper = GeminiHelper()
        personalizedGeminiHelper = PersonalizedGeminiHelper()

        val authViewModel = ViewModelProvider(
            this,
            AuthViewModelFactory(authRepository)
        )[AuthViewModel::class.java]

        val profileViewModel = ViewModelProvider(
            this,
            ProfileViewModelFactory(
                authRepository,
                cloudPerfumeRepository,
                aiPersonalizationRepository
            )
        )[ProfileViewModel::class.java]

        val scentLensViewModel = ViewModelProvider(
            this,
            ScentLensViewModelFactory(
                cloudPerfumeRepository,
                geminiHelper,
                authRepository
            )
        )[ScentLensViewModel::class.java]

        val wardrobeViewModel = ViewModelProvider(
            this,
            WardrobeViewModelFactory(
                cloudPerfumeRepository,
                weatherRepository,
                personalizedGeminiHelper,
                aiPersonalizationRepository
            )
        )[WardrobeViewModel::class.java]

        val discoveryViewModel = ViewModelProvider(
            this,
            DiscoveryViewModelFactory(
                cloudPerfumeRepository,
                personalizedGeminiHelper,
                aiPersonalizationRepository,
                authRepository
            )
        )[DiscoveryViewModel::class.java]

        val aiRecommendationsViewModel = ViewModelProvider(
            this,
            AIRecommendationsViewModelFactory(
                authRepository,
                cloudPerfumeRepository,
                aiPersonalizationRepository,
                weatherRepository,
                personalizedGeminiHelper
            )
        )[AIRecommendationsViewModel::class.java]

        setContent {
            AtelierVersaceTheme {
                AtelierVersaceApp(
                    authViewModel = authViewModel,
                    scentLensViewModel = scentLensViewModel,
                    wardrobeViewModel = wardrobeViewModel,
                    discoveryViewModel = discoveryViewModel,
                    aiRecommendationsViewModel = aiRecommendationsViewModel,
                    profileViewModel = profileViewModel
                )
            }
        }
    }
}

class AuthViewModelFactory(
    private val authRepository: AuthRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AuthViewModel(authRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class ProfileViewModelFactory(
    private val authRepository: AuthRepository,
    private val cloudPerfumeRepository: CloudPerfumeRepository,
    private val aiPersonalizationRepository: AIPersonalizationRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ProfileViewModel(
                authRepository,
                cloudPerfumeRepository,
                aiPersonalizationRepository
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class ScentLensViewModelFactory(
    private val cloudPerfumeRepository: CloudPerfumeRepository,
    private val geminiHelper: GeminiHelper,
    private val authRepository: AuthRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ScentLensViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ScentLensViewModel(cloudPerfumeRepository, geminiHelper, authRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class WardrobeViewModelFactory(
    private val cloudPerfumeRepository: CloudPerfumeRepository,
    private val weatherRepository: WeatherRepository,
    private val personalizedGeminiHelper: PersonalizedGeminiHelper,
    private val aiPersonalizationRepository: AIPersonalizationRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WardrobeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return WardrobeViewModel(
                cloudPerfumeRepository,
                weatherRepository,
                personalizedGeminiHelper,
                aiPersonalizationRepository
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class DiscoveryViewModelFactory(
    private val cloudPerfumeRepository: CloudPerfumeRepository,
    private val personalizedGeminiHelper: PersonalizedGeminiHelper,
    private val aiPersonalizationRepository: AIPersonalizationRepository,
    private val authRepository: AuthRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DiscoveryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DiscoveryViewModel(
                cloudPerfumeRepository,
                personalizedGeminiHelper,
                aiPersonalizationRepository,
                authRepository
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class AIRecommendationsViewModelFactory(
    private val authRepository: AuthRepository,
    private val cloudPerfumeRepository: CloudPerfumeRepository,
    private val aiPersonalizationRepository: AIPersonalizationRepository,
    private val weatherRepository: WeatherRepository,
    private val personalizedGeminiHelper: PersonalizedGeminiHelper
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AIRecommendationsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AIRecommendationsViewModel(
                authRepository,
                cloudPerfumeRepository,
                aiPersonalizationRepository,
                weatherRepository,
                personalizedGeminiHelper
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}