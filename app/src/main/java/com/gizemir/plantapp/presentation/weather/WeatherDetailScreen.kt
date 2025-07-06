package com.gizemir.plantapp.presentation.weather

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.gizemir.plantapp.R
import com.gizemir.plantapp.domain.model.weather.DayWeather
import androidx.navigation.NavController
import androidx.compose.material3.ExperimentalMaterial3Api

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherDetailScreen(
    viewModel: WeatherViewModel,
    initialCity: String,
    onCityChanged: (String) -> Unit,
    modifier: Modifier = Modifier,
    navController: NavController
) {
    var cityInput by remember { mutableStateOf(TextFieldValue(initialCity)) }
    val weekWeather by viewModel.weekWeather.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    
    var currentCity by remember { mutableStateOf(initialCity.ifBlank { "Istanbul" }) }

    LaunchedEffect(Unit) {
        Log.d("WeatherDetailScreen", "Initial setup for: $currentCity")
        if (weekWeather.isEmpty() && !isLoading && error == null) {
            viewModel.clearError()
            viewModel.setLoading(true)
            viewModel.getWeekWeather(currentCity)
        }
    }

    LaunchedEffect(currentCity) {
        Log.d("WeatherDetailScreen", "City changed to: $currentCity")
        viewModel.saveCity(currentCity)
        cityInput = cityInput.copy(text = currentCity)
    }

    DisposableEffect(Unit) {
        onDispose {
            Log.d("WeatherDetailScreen", "Screen disposal: $currentCity")
            try {
                onCityChanged(currentCity)
            } catch (e: Exception) {
                Log.e("WeatherDetailScreen", "Error in onCityChanged: ${e.message}")
            }
        }
    }
    
    BackHandler {
        Log.d("WeatherDetailScreen", "Back pressed: $currentCity")
        try {
            onCityChanged(currentCity)
            navController.popBackStack()
        } catch (e: Exception) {
            Log.e("WeatherDetailScreen", "Error in back handler: ${e.message}")
        }
    }

    LaunchedEffect(error) {
        if (error != null) {
            Log.e("WeatherDetailScreen", "Weather error: $error")
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .padding(top = 12.dp, bottom = 16.dp)
    ) {
        Text(
            text = "5-Day Forecast",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = cityInput,
                    onValueChange = { cityInput = it },
                    label = { Text("Enter City") },
                    singleLine = true,
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 56.dp),
                    shape = RoundedCornerShape(8.dp)
                )
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Button(
                    onClick = {
                        val cityName = cityInput.text.trim()
                        if (cityName.isNotBlank() && cityName != currentCity) {
                            Log.d("WeatherDetailScreen", "Searching for new city: $cityName")
                            currentCity = cityName
                            viewModel.clearError()
                            viewModel.clearWeatherData() 
                            viewModel.setLoading(true)
                            viewModel.getWeekWeather(cityName)
                        }
                    },
                    modifier = Modifier.heightIn(min = 56.dp),
                    shape = RoundedCornerShape(8.dp),
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Search")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
        ) {
            when {
                isLoading -> LoadingView(city = currentCity)
                error != null -> ErrorView(
                    error = error,
                    onRetry = {
                        Log.d("WeatherDetailScreen", "Retry for: $currentCity")
                        viewModel.clearError()
                        viewModel.setLoading(true)
                        viewModel.getWeekWeather(currentCity)
                    }
                )
                weekWeather.isEmpty() -> EmptyDataView(
                    city = currentCity,
                    onRetry = {
                        Log.d("WeatherDetailScreen", "Retry from empty state for: $currentCity")
                        viewModel.clearError()
                        viewModel.setLoading(true)
                        viewModel.getWeekWeather(currentCity)
                    }
                )
                else -> CityForecastView(city = currentCity, weekWeather = weekWeather)
            }
        }
    }
}

@Composable
private fun LoadingView(city: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(8.dp))
            Text("Loading forecast for $city...")
        }
    }
}

@Composable
private fun ErrorView(error: String?, onRetry: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Error: ${error ?: "Unknown error"}",
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Check logs for more details", 
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onRetry) {
                Text("Retry")
            }
        }
    }
}

@Composable
private fun EmptyDataView(city: String, onRetry: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(16.dp)) {
            Text(
                "No weather data available for $city. Please try another city or check your connection.",
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = onRetry) {
                Text("Try Again")
            }
        }
    }
}

@Composable
private fun CityForecastView(city: String, weekWeather: List<DayWeather>) {
    Column(modifier = Modifier.fillMaxSize()) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = city,
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.padding(16.dp),
                textAlign = TextAlign.Center
            )
        }
        
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(weekWeather) { dayWeather ->
                DailyForecastItem(dayWeather = dayWeather)
            }
        }
    }
}

@Composable
fun DailyForecastItem(dayWeather: DayWeather) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1.5f)) {
                Text(
                    text = dayWeather.day,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = dayWeather.date,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Log.d("DailyForecastItem", "Original Icon URL from API: ${dayWeather.iconUrl}")

            val now = remember { java.util.Calendar.getInstance() }
            val hour = now.get(java.util.Calendar.HOUR_OF_DAY)
            val dayNightSuffix = if (hour in 6..18) "d" else "n"

            val baseIconCode = when {
                !dayWeather.iconUrl.contains("/") && dayWeather.iconUrl.length >= 2 -> {
                    dayWeather.iconUrl.substring(0, 2)
                }
                dayWeather.iconUrl.contains("/") -> {
                    val lastPart = dayWeather.iconUrl.substringAfterLast("/")
                    val codePart = lastPart.substringBefore(".").substringBefore("@")
                    if (codePart.length >= 2) codePart.substring(0, 2) else "01"
                }
                else -> "01"
            }

            val apiIconCode = baseIconCode + dayNightSuffix
            
            Log.d("DailyForecastItem", "Base Icon Code: $baseIconCode, Current Day/Night Suffix: $dayNightSuffix, Final API Icon Code: $apiIconCode")

            val iconUrl = "http://openweathermap.org/img/wn/${apiIconCode}@2x.png"

            val weatherIconResId = when (apiIconCode) {
                "01d" -> R.drawable.ic_clear_day
                "01n" -> R.drawable.ic_clear_night
                "02d" -> R.drawable.ic_cloudy
                "02n" -> R.drawable.ic_cloudy
                "03d" -> R.drawable.ic_cloudy
                "03n" -> R.drawable.ic_cloudy
                "04d" -> R.drawable.ic_cloudy
                "04n" -> R.drawable.ic_cloudy
                "09d" -> R.drawable.ic_rain
                "09n" -> R.drawable.ic_rain
                "10d" -> R.drawable.ic_rain
                "10n" -> R.drawable.ic_rain
                "11d" -> R.drawable.ic_storm
                "11n" -> R.drawable.ic_storm
                "13d" -> R.drawable.ic_snow
                "13n" -> R.drawable.ic_snow
                "50d" -> R.drawable.ic_mist
                "50n" -> R.drawable.ic_mist
                else -> R.drawable.ic_weather_placeholder
            }

            val context = LocalContext.current

            SubcomposeAsyncImage(
                model = ImageRequest.Builder(context)
                    .data(iconUrl)
                    .crossfade(true)
                    .diskCacheKey(apiIconCode)
                    .memoryCacheKey(apiIconCode)
                    .build(),
                contentDescription = "Weather icon for ${dayWeather.day}",
                modifier = Modifier
                    .size(50.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Fit,
                loading = {
                    CircularProgressIndicator(
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(24.dp)
                    )
                },
                error = {
                    val iconTint = when (apiIconCode) {
                        "01d" -> Color(0xFFFFB74D)
                        "01n" -> Color(0xFF9FA8DA)
                        "02d", "02n", "03d", "03n", "04d", "04n" -> Color(0xFFBDBDBD)
                        "09d", "09n", "10d", "10n" -> Color(0xFF4FC3F7)
                        "11d", "11n" -> Color(0xFF9575CD)
                        "13d", "13n" -> Color(0xFFE1F5FE)
                        "50d", "50n" -> Color(0xFFE0E0E0)
                        else -> Color.Gray
                    }
                    Log.e("DailyForecastItem", "API'den ikon yüklenemedi: $iconUrl, yerel ikon kullanılıyor.")
                    Image(
                        painter = painterResource(id = weatherIconResId),
                        contentDescription = "Weather icon for ${dayWeather.day}",
                        modifier = Modifier.size(50.dp),
                        colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(iconTint)
                    )
                }
            )

            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = dayWeather.temperature,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Humidity: ${dayWeather.humidity}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
