package com.minimalphone.launcher.domain.weather

data class DailyForecast(
    val date: String, // e.g. "Sat 29", "Sun 30"
    val dayName: String, // e.g. "Today", "Sun", "Mon"
    val weatherCode: Int,
    val conditionDescription: String,
    val tempMax: Int,
    val tempMin: Int
)

data class WeatherData(
    val cityName: String,
    val currentTemp: Int,
    val apparentTemp: Double,
    val condition: String,
    val weatherCode: Int,
    val highTemp: Int,
    val lowTemp: Int,
    val dailyForecast: List<DailyForecast>
)

data class GeocodingCity(
    val name: String,
    val country: String,
    val admin1: String?,
    val latitude: Double,
    val longitude: Double
)
