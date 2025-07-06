package com.gizemir.plantapp.presentation.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = YellowGreen80,
    secondary = Yellow80,
    tertiary = LimeGreen80,
    background = DarkYellowGreenBackground,
    surface = CardYellowDark,
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onTertiary = Color.Black,
    onBackground = Color(0xFFE8F5E8),
    onSurface = Color(0xFFE0F2E0),
    primaryContainer = WeatherCardDarkPrimary,
    onPrimaryContainer = YellowGreen80,
    secondaryContainer = WeatherCardDarkSecondary,
    onSecondaryContainer = Color(0xFFFFF9C4),
    outline = YellowGreen.copy(alpha = 0.7f),
    surfaceVariant = CardYellowDarkVariant,
    onSurfaceVariant = Color(0xFFD4E6D4),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6)
)

private val LightColorScheme = lightColorScheme(
    primary = YellowGreen40,
    secondary = Yellow40,
    tertiary = LimeGreen40,
    background = YellowGreenBackground,
    surface = CardYellowLight,
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onTertiary = Color.White,
    onBackground = DarkGreen,
    onSurface = DarkGreen,
    primaryContainer = LightYellow,
    onPrimaryContainer = DarkGreen,
    secondaryContainer = LemonYellow,
    onSecondaryContainer = DarkGreen,
    outline = YellowGreen.copy(alpha = 0.5f),
    surfaceVariant = CardYellowMedium,
    onSurfaceVariant = Color(0xFF424942),
    error = Color(0xFFD32F2F),
    onError = Color.White,
    errorContainer = Color(0xFFFFEBEE),
    onErrorContainer = Color(0xFFD32F2F)
)

@Composable
fun PlantAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}