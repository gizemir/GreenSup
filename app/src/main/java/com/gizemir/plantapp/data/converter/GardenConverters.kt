package com.gizemir.plantapp.data.converter

import androidx.room.TypeConverter
import com.gizemir.plantapp.domain.model.garden.GardenNote
import com.gizemir.plantapp.domain.model.garden.GardenPhoto
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class GardenConverters {
    private val gson = Gson()

    @TypeConverter
    fun fromNoteList(notes: List<GardenNote>?): String {
        return gson.toJson(notes)
    }

    @TypeConverter
    fun toNoteList(data: String?): List<GardenNote> {
        if (data.isNullOrEmpty()) return emptyList()
        val type = object : TypeToken<List<GardenNote>>() {}.type
        return gson.fromJson(data, type)
    }

    @TypeConverter
    fun fromPhotoList(photos: List<GardenPhoto>?): String {
        return gson.toJson(photos)
    }

    @TypeConverter
    fun toPhotoList(data: String?): List<GardenPhoto> {
        if (data.isNullOrEmpty()) return emptyList()
        val type = object : TypeToken<List<GardenPhoto>>() {}.type
        return gson.fromJson(data, type)
    }
} 