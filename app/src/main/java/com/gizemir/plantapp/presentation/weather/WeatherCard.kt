package com.gizemir.plantapp.presentation.weather

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.util.Log
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.gizemir.plantapp.R

@Composable
fun WeatherCard(
    city: String,
    date: String,
    temperature: String,
    description: String,
    iconUrl: String,
    humidity: String,
    windSpeed: String,
    onClick: () -> Unit
) {
    val defaultCity = "Istanbul"
    val defaultTemperature = "20°C"
    val defaultDescription = "Clear"
    val defaultIconCode = "01d"
    val defaultHumidity = "60"
    val defaultWindSpeed = "10"

    val showDefault = city.isBlank() || temperature == "--°C" || description.isBlank() || iconUrl.isBlank() || humidity == "--" || windSpeed == "--"

    val displayCity = if (showDefault) defaultCity else city
    val displayTemperature = if (showDefault) defaultTemperature else temperature
    val displayDescription = if (showDefault) defaultDescription else description
    val displayHumidity = if (showDefault) defaultHumidity else humidity
    val displayWindSpeed = if (showDefault) defaultWindSpeed else windSpeed

    val rawIconCode = if (showDefault) {
        defaultIconCode
    } else {
        Log.d("WeatherCard", "Original Icon URL: $iconUrl")
        
        when {
            !iconUrl.contains("/") -> {
                if (iconUrl.contains("@")) {
                    iconUrl.substringBefore("@")
                } else {
                    iconUrl
                }
            }
            
            iconUrl.contains("/") -> {
                val parts = iconUrl.split("/")
                val lastPart = parts.lastOrNull() ?: ""
                if (lastPart.contains(".")) {
                    val nameWithoutExtension = lastPart.substringBefore(".")
                    if (nameWithoutExtension.contains("@")) {
                        nameWithoutExtension.substringBefore("@")
                    } else {
                        nameWithoutExtension
                    }
                } else {
                    if (lastPart.contains("@")) {
                        lastPart.substringBefore("@")
                    } else {
                        lastPart
                    }
                }
            }
            
            else -> defaultIconCode
        }
    }
    
    val displayIconUrl = "http://openweathermap.org/img/wn/$rawIconCode@2x.png"
    
    Log.d("WeatherCard", "Temizlenmiş İkon Kodu: $rawIconCode")
    Log.d("WeatherCard", "Final İkon URL: $displayIconUrl")
    
    val weatherIconResId = when (rawIconCode.take(3)) {  // iconCode -> rawIconCode
        "01d" -> R.drawable.ic_clear_day    // Açık - gündüz
        "01n" -> R.drawable.ic_clear_night  // Açık - gece
        "02d", "02n", "03d", "03n", "04d", "04n" -> R.drawable.ic_cloudy  // Bulutlu
        "09d", "09n" -> R.drawable.ic_rain  // Yağmur
        "10d", "10n" -> R.drawable.ic_rain  // Hafif yağmur
        "11d", "11n" -> R.drawable.ic_storm // Fırtına
        "13d", "13n" -> R.drawable.ic_snow  // Kar
        "50d", "50n" -> R.drawable.ic_mist  // Sisli
        else -> R.drawable.ic_weather_placeholder // Varsayılan
    }
    
    val iconTint = when (rawIconCode.take(3)) {
        "01d" -> Color(0xFFFFB74D) // Güneşli gün - turuncu/sarı
        "01n" -> Color(0xFF9FA8DA) // Açık gece - lacivert
        "02d", "02n", "03d", "03n", "04d", "04n" -> Color(0xFFBDBDBD) // Bulutlu - gri
        "09d", "09n", "10d", "10n" -> Color(0xFF4FC3F7) // Yağmurlu - mavi
        "11d", "11n" -> Color(0xFF9575CD) // Fırtına - mor
        "13d", "13n" -> Color(0xFFE1F5FE) // Kar - açık mavi
        "50d", "50n" -> Color(0xFFE0E0E0) // Sisli - açık gri
        else -> Color.Gray // Varsayılan
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
            .clickable(enabled = true) { onClick() },
        elevation = CardDefaults.cardElevation(12.dp),
        shape = RoundedCornerShape(24.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primaryContainer,
                            MaterialTheme.colorScheme.secondaryContainer
                        )
                    )
                )
                .padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    modifier = Modifier.padding(end = 8.dp),
                    verticalArrangement = Arrangement.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = displayCity,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = date,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = displayTemperature,
                        style = MaterialTheme.typography.displayMedium.copy(fontSize = 32.sp),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = displayDescription,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    val context = LocalContext.current
                    
                    SubcomposeAsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(displayIconUrl)
                            .crossfade(true)
                            .diskCacheKey(rawIconCode)
                            .memoryCacheKey(rawIconCode)
                            .placeholder(weatherIconResId)
                            .error(weatherIconResId)
                            .build(),
                        contentDescription = "Weather icon for $displayCity",
                        modifier = Modifier
                            .size(52.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Fit,
                        loading = {
                            CircularProgressIndicator(
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        error = {
                            Log.e("WeatherCard", "API'den ikon yüklenemedi: $displayIconUrl, yerel ikon kullanılıyor.")
                            Image(
                                painter = painterResource(id = weatherIconResId),
                                contentDescription = "Weather icon for $displayDescription",
                                modifier = Modifier.size(52.dp),
                                colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(iconTint)
                            )
                        }
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Humidity: $displayHumidity",
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                        color = Color(0xFF4FC3F7),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Wind: $displayWindSpeed",
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                        color = Color(0xFF81C784),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

