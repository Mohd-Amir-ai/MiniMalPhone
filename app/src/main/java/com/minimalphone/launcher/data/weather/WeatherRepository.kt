package com.minimalphone.launcher.data.weather

import com.minimalphone.launcher.data.local.LocalPreferencesStore
import com.minimalphone.launcher.domain.weather.DailyForecast
import com.minimalphone.launcher.domain.weather.GeocodingCity
import com.minimalphone.launcher.domain.weather.WeatherData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

class WeatherRepository(private val prefsStore: LocalPreferencesStore) {

    suspend fun getCachedWeather(): WeatherData? = withContext(Dispatchers.IO) {
        val cached = prefsStore.getCachedWeatherJson() ?: return@withContext null
        parseWeatherData(cached, prefsStore.getWeatherCity())
    }

    suspend fun fetchWeather(
        lat: Double = prefsStore.getWeatherLat(),
        lon: Double = prefsStore.getWeatherLon(),
        cityName: String = prefsStore.getWeatherCity()
    ): WeatherData? = withContext(Dispatchers.IO) {
        try {
            val urlString = "https://api.open-meteo.com/v1/forecast?" +
                    "latitude=$lat&longitude=$lon" +
                    "&current=temperature_2m,apparent_temperature,weather_code" +
                    "&daily=weather_code,temperature_2m_max,temperature_2m_min" +
                    "&timezone=auto"

            val url = URL(urlString)
            val connection = (url.openConnection() as HttpURLConnection).apply {
                connectTimeout = 8000
                readTimeout = 8000
                requestMethod = "GET"
            }

            if (connection.responseCode == 200) {
                val reader = BufferedReader(InputStreamReader(connection.inputStream))
                val jsonString = reader.readText()
                reader.close()

                // Cache for offline / instant load
                prefsStore.setCachedWeatherJson(jsonString)
                parseWeatherData(jsonString, cityName)
            } else {
                getCachedWeather()
            }
        } catch (e: Exception) {
            getCachedWeather()
        }
    }

    suspend fun searchCities(query: String): List<GeocodingCity> = withContext(Dispatchers.IO) {
        if (query.isBlank()) return@withContext emptyList()
        try {
            val encoded = URLEncoder.encode(query.trim(), "UTF-8")
            val urlString = "https://geocoding-api.open-meteo.com/v1/search?name=$encoded&count=5&language=en&format=json"
            val url = URL(urlString)
            val connection = (url.openConnection() as HttpURLConnection).apply {
                connectTimeout = 6000
                readTimeout = 6000
                requestMethod = "GET"
            }

            if (connection.responseCode == 200) {
                val reader = BufferedReader(InputStreamReader(connection.inputStream))
                val jsonString = reader.readText()
                reader.close()

                val root = JSONObject(jsonString)
                val results = root.optJSONArray("results") ?: return@withContext emptyList()
                val list = mutableListOf<GeocodingCity>()
                for (i in 0 until results.length()) {
                    val item = results.getJSONObject(i)
                    list.add(
                        GeocodingCity(
                            name = item.optString("name", "Unknown"),
                            country = item.optString("country", ""),
                            admin1 = item.optString("admin1", null),
                            latitude = item.optDouble("latitude", 0.0),
                            longitude = item.optDouble("longitude", 0.0)
                        )
                    )
                }
                list
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun parseWeatherData(jsonString: String, cityName: String): WeatherData? {
        return try {
            val root = JSONObject(jsonString)
            val current = root.getJSONObject("current")
            val daily = root.getJSONObject("daily")

            val currentTemp = current.getDouble("temperature_2m").roundToInt()
            val apparentTemp = current.getDouble("apparent_temperature")
            val weatherCode = current.getInt("weather_code")
            val condition = getWeatherCondition(weatherCode)

            val dailyCodes = daily.getJSONArray("weather_code")
            val dailyMaxs = daily.getJSONArray("temperature_2m_max")
            val dailyMins = daily.getJSONArray("temperature_2m_min")
            val dailyTimes = daily.getJSONArray("time")

            val todayMax = if (dailyMaxs.length() > 0) dailyMaxs.getDouble(0).roundToInt() else currentTemp
            val todayMin = if (dailyMins.length() > 0) dailyMins.getDouble(0).roundToInt() else currentTemp

            val forecastList = mutableListOf<DailyForecast>()
            val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val outputDayFormat = SimpleDateFormat("EEE", Locale.getDefault())
            val outputDateFormat = SimpleDateFormat("d MMM", Locale.getDefault())

            for (i in 0 until dailyTimes.length().coerceAtMost(7)) {
                val dateStr = dailyTimes.getString(i)
                val code = dailyCodes.getInt(i)
                val maxT = dailyMaxs.getDouble(i).roundToInt()
                val minT = dailyMins.getDouble(i).roundToInt()

                val parsedDate: Date? = try { inputFormat.parse(dateStr) } catch (e: Exception) { null }
                val dayName = if (i == 0) "Today" else parsedDate?.let { outputDayFormat.format(it) } ?: dateStr
                val displayDate = parsedDate?.let { outputDateFormat.format(it) } ?: dateStr

                forecastList.add(
                    DailyForecast(
                        date = displayDate,
                        dayName = dayName,
                        weatherCode = code,
                        conditionDescription = getWeatherCondition(code),
                        tempMax = maxT,
                        tempMin = minT
                    )
                )
            }

            WeatherData(
                cityName = cityName,
                currentTemp = currentTemp,
                apparentTemp = apparentTemp,
                condition = condition,
                weatherCode = weatherCode,
                highTemp = todayMax,
                lowTemp = todayMin,
                dailyForecast = forecastList
            )
        } catch (e: Exception) {
            null
        }
    }

    companion object {
        fun getWeatherCondition(code: Int): String {
            return when (code) {
                0 -> "Clear sky"
                1 -> "Mainly clear"
                2 -> "Partly cloudy"
                3 -> "Overcast"
                45, 48 -> "Foggy"
                51, 53, 55 -> "Drizzle"
                56, 57 -> "Freezing drizzle"
                61 -> "light rain"
                63 -> "Moderate rain"
                65 -> "Heavy rain"
                66, 67 -> "Freezing rain"
                71 -> "Slight snow"
                73 -> "Moderate snow"
                75 -> "Heavy snow"
                77 -> "Snow grains"
                80, 81, 82 -> "Rain showers"
                85, 86 -> "Snow showers"
                95 -> "Thunderstorm"
                96, 99 -> "Thunderstorm with hail"
                else -> "Fair"
            }
        }
    }
}
