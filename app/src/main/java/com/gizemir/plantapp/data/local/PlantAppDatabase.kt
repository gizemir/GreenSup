package com.gizemir.plantapp.data.local

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import android.content.Context
import com.gizemir.plantapp.data.local.converter.Converters
import com.gizemir.plantapp.data.converter.GardenConverters
import com.gizemir.plantapp.data.local.dao.favorites.FavoritePlantDao
import com.gizemir.plantapp.data.local.dao.forum.CommentDao
import com.gizemir.plantapp.data.local.dao.forum.PostDao

import com.gizemir.plantapp.data.local.dao.plant_search.PlantDao
import com.gizemir.plantapp.data.local.dao.plant_search.PlantDetailDao
import com.gizemir.plantapp.data.local.dao.plant_search.PlantSearchQueryDao
import com.gizemir.plantapp.data.local.dao.weather.DayWeatherDao
import com.gizemir.plantapp.data.local.dao.weather.WeatherDao
import com.gizemir.plantapp.data.local.dao.plant_care.PlantCareDao
import com.gizemir.plantapp.data.local.dao.plant_analysis.PlantAnalysisDao

import com.gizemir.plantapp.data.local.entity.favorites.FavoritePlantEntity
import com.gizemir.plantapp.data.local.entity.plant_care.PlantCareEntity
import com.gizemir.plantapp.data.local.entity.forum.CommentEntity
import com.gizemir.plantapp.data.local.entity.forum.PostEntity

import com.gizemir.plantapp.data.local.entity.plant_search.PlantDetailEntity
import com.gizemir.plantapp.data.local.entity.plant_search.PlantEntity
import com.gizemir.plantapp.data.local.entity.plant_search.PlantSearchQueryEntity
import com.gizemir.plantapp.data.local.entity.weather.DayWeatherEntity
import com.gizemir.plantapp.data.local.entity.weather.WeatherEntity
import com.gizemir.plantapp.data.local.entity.plant_analysis.DiseaseDetectionEntity
import com.gizemir.plantapp.data.local.entity.garden.GardenPlantEntity
import com.gizemir.plantapp.data.local.dao.garden.GardenPlantDao


@Database(
    entities = [
        PlantEntity::class,
        PlantDetailEntity::class,
        PlantSearchQueryEntity::class,
        WeatherEntity::class,
        DayWeatherEntity::class,
        PostEntity::class,
        CommentEntity::class,
        FavoritePlantEntity::class,
        PlantCareEntity::class,
        DiseaseDetectionEntity::class,
        GardenPlantEntity::class
    ],
    version = 18,
    exportSchema = false
)
@TypeConverters(Converters::class, GardenConverters::class)
abstract class PlantAppDatabase : RoomDatabase() {
    
    abstract fun plantDao(): PlantDao
    abstract fun plantDetailDao(): PlantDetailDao
    abstract fun plantSearchQueryDao(): PlantSearchQueryDao
    abstract fun weatherDao(): WeatherDao
    abstract fun dayWeatherDao(): DayWeatherDao
    abstract fun postDao(): PostDao
    abstract fun commentDao(): CommentDao
    abstract fun favoritePlantDao(): FavoritePlantDao

    abstract fun plantCareDao(): PlantCareDao
    abstract fun plantAnalysisDao(): PlantAnalysisDao
    abstract fun gardenPlantDao(): GardenPlantDao

    
    companion object {
        @Volatile
        private var INSTANCE: PlantAppDatabase? = null
        
        val MIGRATION_15_16 = object : Migration(15, 16) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("""
                    CREATE TABLE favorite_plants_new (
                        userId TEXT NOT NULL,
                        plantId INTEGER NOT NULL,
                        commonName TEXT NOT NULL,
                        scientificName TEXT NOT NULL,
                        imageUrl TEXT,
                        userImageUri TEXT,
                        family TEXT,
                        genus TEXT,
                        addedAt INTEGER NOT NULL,
                        source TEXT NOT NULL,
                        PRIMARY KEY(userId, plantId)
                    )
                """.trimIndent())
                

                database.execSQL("""
                    INSERT INTO favorite_plants_new 
                    (userId, plantId, commonName, scientificName, imageUrl, userImageUri, family, genus, addedAt, source)
                    SELECT 'unknown_user', plantId, commonName, scientificName, imageUrl, userImageUri, family, genus, addedAt, source
                    FROM favorite_plants
                """.trimIndent())
                
                database.execSQL("DROP TABLE favorite_plants")
                
                database.execSQL("ALTER TABLE favorite_plants_new RENAME TO favorite_plants")
            }
        }
        
        fun getDatabase(context: Context): PlantAppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PlantAppDatabase::class.java,
                    "plant_app_database"
                )
                    .addMigrations(MIGRATION_15_16)
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

