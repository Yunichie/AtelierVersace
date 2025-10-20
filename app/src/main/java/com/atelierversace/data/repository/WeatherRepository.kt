package com.atelierversace.data.repository

import com.atelierversace.utils.WeatherHelper
import com.atelierversace.utils.WeatherData

class WeatherRepository(private val weatherHelper: WeatherHelper) {

    suspend fun getCurrentWeather(): Result<WeatherData> {
        return try {
            val weather = weatherHelper.fetchWeather()
            Result.success(weather)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}