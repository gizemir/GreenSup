package com.gizemir.plantapp.data.local.converter

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.gizemir.plantapp.domain.model.plant_analysis.Disease
import com.gizemir.plantapp.domain.model.plant_analysis.PlantSuggestion


class Converters {
    
    @TypeConverter
    fun fromStringList(value: List<String>?): String? {
        return if (value == null) null else Gson().toJson(value)
    }
    
    @TypeConverter
    fun toStringList(value: String?): List<String>? {
        return if (value == null) null else {
            val listType = object : TypeToken<List<String>>() {}.type
            Gson().fromJson(value, listType)
        }
    }
    
    @TypeConverter
    fun fromIntList(value: List<Int>?): String? {
        return if (value == null) null else Gson().toJson(value)
    }
    
    @TypeConverter
    fun toIntList(value: String?): List<Int>? {
        return if (value == null) null else {
            val listType = object : TypeToken<List<Int>>() {}.type
            Gson().fromJson(value, listType)
        }
    }
    
    @TypeConverter
    fun fromDiseaseList(value: List<Disease>?): String? {
        return if (value == null) null else Gson().toJson(value)
    }
    
    @TypeConverter
    fun toDiseaseList(value: String?): List<Disease>? {
        return if (value == null) null else {
            val listType = object : TypeToken<List<Disease>>() {}.type
            Gson().fromJson(value, listType)
        }
    }
    
    @TypeConverter
    fun fromPlantSuggestionList(value: List<PlantSuggestion>?): String? {
        return if (value == null) null else Gson().toJson(value)
    }
    
    @TypeConverter
    fun toPlantSuggestionList(value: String?): List<PlantSuggestion>? {
        return if (value == null) null else {
            val listType = object : TypeToken<List<PlantSuggestion>>() {}.type
            Gson().fromJson(value, listType)
        }
    }

} 