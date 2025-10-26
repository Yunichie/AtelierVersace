package com.atelierversace.utils

import com.google.gson.annotations.SerializedName
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import com.atelierversace.BuildConfig

data class WeatherData(
    val temperature: Double,
    val humidity: Int,
    val description: String
)

data class WeatherResponse(
    @SerializedName("main") val main: Main,
    @SerializedName("weather") val weather: List<Weather>
)

data class Main(
    @SerializedName("temp") val temp: Double,
    @SerializedName("humidity") val humidity: Int
)

data class Weather(
    @SerializedName("description") val description: String
)

interface WeatherApi {
    @GET("weather")
    suspend fun getWeather(
        @Query("q") city: String,
        @Query("appid") apiKey: String,
        @Query("units") units: String = "metric"
    ): WeatherResponse
}

class WeatherHelper {
    private val apiKey = BuildConfig.OPENWEATHER_KEY

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://api.openweathermap.org/data/2.5/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val api = retrofit.create(WeatherApi::class.java)

    suspend fun fetchWeather(city: String = "Surabaya"): WeatherData {
        val response = api.getWeather(city, apiKey)
        return WeatherData(
            temperature = response.main.temp,
            humidity = response.main.humidity,
            description = response.weather.firstOrNull()?.description ?: "Unknown"
        )
    }
}