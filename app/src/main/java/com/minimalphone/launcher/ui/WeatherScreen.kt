package com.minimalphone.launcher.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.minimalphone.launcher.data.weather.WeatherRepository
import com.minimalphone.launcher.domain.weather.DailyForecast
import com.minimalphone.launcher.domain.weather.GeocodingCity
import com.minimalphone.launcher.domain.weather.WeatherData
import com.minimalphone.launcher.ui.components.MinimalWeatherIcon
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun WeatherScreen(
    weatherRepository: WeatherRepository,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    var weatherData by remember { mutableStateOf<WeatherData?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var showLocationDialog by remember { mutableStateOf(false) }

    fun loadWeather() {
        scope.launch {
            // First load from local storage instantly (0 delay)
            if (weatherData == null) {
                weatherData = weatherRepository.getCachedWeather()
            }
            isLoading = true
            val fresh = weatherRepository.fetchWeather()
            if (fresh != null) {
                weatherData = fresh
            }
            isLoading = false
        }
    }

    LaunchedEffect(Unit) {
        loadWeather()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF000000)) // Deep matte black matching user photo
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 22.dp, vertical = 12.dp)
        ) {
            // 1. TOP HEADER: Back arrow, City Name (Center), Location settings (Right)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back to Tasks",
                        tint = Color.White
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { showLocationDialog = true }
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = weatherData?.cityName ?: "Nagpur",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Normal,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Change Location",
                        tint = Color(0x88FFFFFF),
                        modifier = Modifier.size(16.dp)
                    )
                }

                IconButton(
                    onClick = { showLocationDialog = true },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = "Location Settings",
                        tint = Color.White
                    )
                }
            }

            if (weatherData != null) {
                val data = weatherData!!

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    item {
                        Spacer(modifier = Modifier.height(24.dp))

                        // 2. CENTER MINIMAL WEATHER ICON (Matching user's photo!)
                        MinimalWeatherIcon(
                            weatherCode = data.weatherCode,
                            size = 140.dp,
                            strokeWidth = 3.5.dp,
                            color = Color.White
                        )

                        Spacer(modifier = Modifier.height(32.dp))

                        // 3. TEMPERATURE & CONDITION (e.g. 27°C light rain)
                        Text(
                            text = "${data.currentTemp}°C ${data.condition}",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Normal,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        // Subtitle: feels like 29.31°C
                        Text(
                            text = String.format("feels like %.2f°C", data.apparentTemp),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Normal,
                            color = Color(0xBBFFFFFF)
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        // High & Low: H: 27°C L: 27°C
                        Text(
                            text = "H: ${data.highTemp}°C   L: ${data.lowTemp}°C",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Normal,
                            color = Color(0x77FFFFFF)
                        )

                        Spacer(modifier = Modifier.height(38.dp))

                        // Section divider for 1-week forecast
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "7-Day Forecast",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0x88FFFFFF)
                            )
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(14.dp),
                                    strokeWidth = 2.dp,
                                    color = Color.White
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    // 4. ONE-WEEK WEATHER FORECAST ROWS
                    items(data.dailyForecast) { day ->
                        ForecastRow(forecast = day)
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    item {
                        Spacer(modifier = Modifier.height(32.dp))
                    }
                }
            } else {
                // Loading / Initial State
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(32.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Fetching Open-Meteo Weather...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0x88FFFFFF)
                        )
                    }
                }
            }
        }

        // Location Settings Modal
        if (showLocationDialog) {
            LocationSettingsDialog(
                currentCity = weatherData?.cityName ?: "Nagpur",
                weatherRepository = weatherRepository,
                onDismiss = { showLocationDialog = false },
                onCitySelected = { city ->
                    showLocationDialog = false
                    scope.launch {
                        isLoading = true
                        val updated = weatherRepository.fetchWeather(
                            lat = city.latitude,
                            lon = city.longitude,
                            cityName = city.name
                        )
                        if (updated != null) {
                            weatherData = updated
                        }
                        isLoading = false
                    }
                }
            )
        }
    }
}

@Composable
private fun ForecastRow(forecast: DailyForecast) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFF111215))
            .border(1.dp, Color(0xFF1E2024), RoundedCornerShape(10.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Day + Date
        Column(modifier = Modifier.width(80.dp)) {
            Text(
                text = forecast.dayName,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White
            )
            Text(
                text = forecast.date,
                fontSize = 12.sp,
                color = Color(0x66FFFFFF)
            )
        }

        // Mini Weather Icon + Condition description
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            MinimalWeatherIcon(
                weatherCode = forecast.weatherCode,
                size = 32.dp,
                strokeWidth = 2.dp,
                color = Color.White
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = forecast.conditionDescription,
                fontSize = 14.sp,
                color = Color(0xDDFFFFFF)
            )
        }

        // High / Low temperatures
        Text(
            text = "${forecast.tempMax}° / ${forecast.tempMin}°",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.White
        )
    }
}

@Composable
private fun LocationSettingsDialog(
    currentCity: String,
    weatherRepository: WeatherRepository,
    onDismiss: () -> Unit,
    onCitySelected: (GeocodingCity) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val searchResults = remember { mutableStateListOf<GeocodingCity>() }
    var isSearching by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    var searchJob by remember { mutableStateOf<Job?>(null) }

    val presetCities = remember {
        listOf(
            GeocodingCity("Nagpur", "India", "Maharashtra", 21.1458, 79.0882),
            GeocodingCity("Mumbai", "India", "Maharashtra", 19.0760, 72.8777),
            GeocodingCity("New Delhi", "India", "Delhi", 28.6139, 77.2090),
            GeocodingCity("Bengaluru", "India", "Karnataka", 12.9716, 77.5946),
            GeocodingCity("London", "United Kingdom", "England", 51.5074, -0.1278),
            GeocodingCity("Tokyo", "Japan", "Tokyo", 35.6762, 139.6503),
            GeocodingCity("New York", "United States", "New York", 40.7128, -74.0060)
        )
    }

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF141518))
                .border(1.dp, Color(0xFF2C2F36), RoundedCornerShape(16.dp))
                .padding(20.dp)
        ) {
            Column {
                Text(
                    text = "Weather Location",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Search TextField
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { query ->
                        searchQuery = query
                        searchJob?.cancel()
                        if (query.trim().length >= 2) {
                            searchJob = scope.launch {
                                delay(350)
                                isSearching = true
                                val results = weatherRepository.searchCities(query)
                                searchResults.clear()
                                searchResults.addAll(results)
                                isSearching = false
                            }
                        } else {
                            searchResults.clear()
                        }
                    },
                    placeholder = { Text("Type city name...", color = Color(0x66FFFFFF)) },
                    singleLine = true,
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = Color(0x88FFFFFF)
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color.White,
                        unfocusedBorderColor = Color(0xFF3B3E45),
                        cursorColor = Color.White
                    )
                )

                Spacer(modifier = Modifier.height(14.dp))

                // If searching, show results
                if (searchResults.isNotEmpty()) {
                    Text(
                        text = "Search Results",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0x88FFFFFF)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyColumn(modifier = Modifier.heightIn(max = 200.dp)) {
                        items(searchResults) { city ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onCitySelected(city) }
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = city.name,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color.White
                                    )
                                    val subtitle = listOfNotNull(city.admin1, city.country).joinToString(", ")
                                    if (subtitle.isNotEmpty()) {
                                        Text(
                                            text = subtitle,
                                            fontSize = 12.sp,
                                            color = Color(0x77FFFFFF)
                                        )
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // Quick Presets
                    Text(
                        text = "Popular Locations",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0x88FFFFFF)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyColumn(modifier = Modifier.heightIn(max = 220.dp)) {
                        items(presetCities) { city ->
                            val isCurrent = city.name.equals(currentCity, ignoreCase = true)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isCurrent) Color(0x22FFFFFF) else Color.Transparent)
                                    .clickable { onCitySelected(city) }
                                    .padding(horizontal = 8.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.LocationOn,
                                        contentDescription = null,
                                        tint = if (isCurrent) Color.White else Color(0x66FFFFFF),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "${city.name}, ${city.country}",
                                        fontSize = 14.sp,
                                        color = if (isCurrent) Color.White else Color(0xDDFFFFFF),
                                        fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                                if (isCurrent) {
                                    Text(
                                        text = "Selected",
                                        fontSize = 11.sp,
                                        color = Color(0x88FFFFFF)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Close Button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF22252B))
                        .clickable(onClick = onDismiss)
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Cancel",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = Color.White
                    )
                }
            }
        }
    }
}
