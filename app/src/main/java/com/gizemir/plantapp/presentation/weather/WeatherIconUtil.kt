package com.gizemir.plantapp.presentation.weather

import android.util.Log


object WeatherIconUtil {
    
    private const val DEFAULT_ICON = "01d"
    private const val ICON_BASE_URL = "https://openweathermap.org/img/wn/"
    private const val ICON_SIZE_SUFFIX = "@2x.png"
    

    fun extractIconCode(iconSource: String?): String {
        if (iconSource.isNullOrBlank()) return DEFAULT_ICON
        
        return try {
            if (iconSource.length == 3 && !iconSource.contains("/") && !iconSource.contains(".")) {
                return iconSource
            }
            
            if (iconSource.contains("/")) {
                val lastSegment = iconSource.substringAfterLast("/")
                if (lastSegment.contains("@")) {
                    return lastSegment.substringBefore("@")
                } else if (lastSegment.contains(".")) {
                    return lastSegment.substringBefore(".")
                }
                return lastSegment.take(3) // Örneğin: "10d"
            }
            
            if (iconSource.contains("@")) {
                return iconSource.substringBefore("@")
            } else if (iconSource.contains(".")) {
                return iconSource.substringBefore(".")
            }
            
            DEFAULT_ICON
            
        } catch (e: Exception) {
            Log.e("WeatherIconUtil", "Error extracting icon code from $iconSource", e)
            DEFAULT_ICON
        }
    }
    

    fun buildIconUrl(iconCode: String): String {
        val safeIconCode = if (iconCode.isBlank()) DEFAULT_ICON else iconCode
        return "$ICON_BASE_URL$safeIconCode$ICON_SIZE_SUFFIX"
    }
}
