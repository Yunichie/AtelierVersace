package com.atelierversace

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.atelierversace.data.local.AppDatabase
import com.atelierversace.data.repository.PerfumeRepository
import com.atelierversace.data.repository.WeatherRepository
import com.atelierversace.ui.scent_lens.ScentLensViewModel
import com.atelierversace.ui.wardrobe.WardrobeViewModel
import com.atelierversace.ui.discovery.DiscoveryViewModel
import com.atelierversace.utils.GeminiHelper
import com.atelierversace.utils.WeatherHelper

class MainActivity : ComponentActivity() {

    private lateinit var database: AppDatabase
    private lateinit var perfumeRepository: PerfumeRepository
    private lateinit var weatherRepository: WeatherRepository
    private lateinit var geminiHelper: GeminiHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize dependencies
        database = AppDatabase.getDatabase(applicationContext)
        perfumeRepository = PerfumeRepository(database.perfumeDao())
        weatherRepository = WeatherRepository(WeatherHelper())
        geminiHelper = GeminiHelper()

        // Create ViewModels
        val scentLensViewModel = ViewModelProvider(
            this,
            object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return ScentLensViewModel(perfumeRepository, geminiHelper) as T
                }
            }
        )[ScentLensViewModel::class.java]

        val wardrobeViewModel = ViewModelProvider(
            this,
            object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return WardrobeViewModel(perfumeRepository, weatherRepository, geminiHelper) as T
                }
            }
        )[WardrobeViewModel::class.java]

        val discoveryViewModel = ViewModelProvider(
            this,
            object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return DiscoveryViewModel(perfumeRepository, geminiHelper) as T
                }
            }
        )[DiscoveryViewModel::class.java]

        setContent {
            MaterialTheme(
                colorScheme = lightColorScheme(
                    primary = androidx.compose.ui.graphics.Color(0xFF6B4EFF),
                    secondary = androidx.compose.ui.graphics.Color(0xFFFF6B9D)
                )
            ) {
                AtelierVersaceApp(
                    scentLensViewModel = scentLensViewModel,
                    wardrobeViewModel = wardrobeViewModel,
                    discoveryViewModel = discoveryViewModel
                )
            }
        }
    }
}