package com.gizemir.plantapp.core.worker

import android.util.Log
import androidx.work.*
import com.gizemir.plantapp.domain.model.garden.GardenPlant
import kotlinx.coroutines.guava.await
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WateringSchedulerService @Inject constructor(
    private val workManager: WorkManager
) {
    companion object {
        private const val TAG = "WateringSchedulerService"
        private const val WATERING_WORK_PREFIX = "watering_reminder_"
        
        private const val USE_MINUTES_FOR_TESTING = false

    }

    fun scheduleWateringReminder(plant: GardenPlant, userId: String) {
        try {
            cancelWateringReminder(plant.id)

            val delayAmount = plant.wateringPeriodDays.toLong()
            val timeUnit = if (USE_MINUTES_FOR_TESTING) TimeUnit.MINUTES else TimeUnit.DAYS
            val delayText = if (USE_MINUTES_FOR_TESTING) "minutes" else "days"
            
            val inputData = Data.Builder()
                .putLong(WateringNotificationWorker.PLANT_ID_KEY, plant.id)
                .putString(WateringNotificationWorker.PLANT_NAME_KEY, plant.name)
                .putString(WateringNotificationWorker.USER_ID_KEY, userId)
                .build()

            val workRequest = OneTimeWorkRequestBuilder<WateringNotificationWorker>()
                .setInitialDelay(delayAmount, timeUnit)
                .setInputData(inputData)
                .addTag(getWorkTag(plant.id))
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .build()

            workManager.enqueue(workRequest)
            
            Log.d(TAG, "Scheduled watering reminder for ${plant.name} in $delayAmount $delayText")
            
        } catch (e: Exception) {
            Log.e(TAG, "Error scheduling watering reminder for ${plant.name}", e)
        }
    }


    fun cancelWateringReminder(plantId: Long) {
        try {
            val workTag = getWorkTag(plantId)
            workManager.cancelAllWorkByTag(workTag)
            Log.d(TAG, "Cancelled watering reminder for plant ID: $plantId")
        } catch (e: Exception) {
            Log.e(TAG, "Error cancelling watering reminder for plant ID: $plantId", e)
        }
    }


    fun cancelAllWateringReminders() {
        try {
            workManager.cancelAllWorkByTag(WATERING_WORK_PREFIX)
            Log.d(TAG, "Cancelled all watering reminders")
        } catch (e: Exception) {
            Log.e(TAG, "Error cancelling all watering reminders", e)
        }
    }


    suspend fun hasActiveReminder(plantId: Long): Boolean {
        return try {
            val workInfos = workManager.getWorkInfosByTag(getWorkTag(plantId))
            val workInfosList = workInfos.await()
            workInfosList.any { it.state == WorkInfo.State.ENQUEUED || it.state == WorkInfo.State.RUNNING }
        } catch (e: Exception) {
            Log.e(TAG, "Error checking active reminder for plant ID: $plantId", e)
            false
        }
    }

    private fun getWorkTag(plantId: Long): String {
        return "$WATERING_WORK_PREFIX$plantId"
    }
} 