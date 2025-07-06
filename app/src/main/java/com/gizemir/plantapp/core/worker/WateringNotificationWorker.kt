package com.gizemir.plantapp.core.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.gizemir.plantapp.domain.model.notification.Notification
import com.gizemir.plantapp.domain.model.notification.NotificationType
import com.gizemir.plantapp.domain.repository.garden.GardenRepository
import com.gizemir.plantapp.domain.repository.notification.NotificationRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import java.util.Date

@HiltWorker
class WateringNotificationWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val gardenRepository: GardenRepository,
    private val notificationRepository: NotificationRepository,
    private val auth: FirebaseAuth
) : CoroutineWorker(context, workerParams) {

    companion object {
        const val PLANT_ID_KEY = "plant_id"
        const val PLANT_NAME_KEY = "plant_name"
        const val USER_ID_KEY = "user_id"
        const val TAG = "WateringNotificationWorker"
        
        private const val USE_MINUTES_FOR_TESTING = false
    }

    override suspend fun doWork(): Result {
        return try {
            val plantId = inputData.getLong(PLANT_ID_KEY, -1L)
            val plantName = inputData.getString(PLANT_NAME_KEY) ?: ""
            val userId = inputData.getString(USER_ID_KEY) ?: ""

            if (plantId == -1L || plantName.isEmpty() || userId.isEmpty()) {
                Log.e(TAG, "Invalid input data")
                return Result.failure()
            }

            val gardenPlants = gardenRepository.getGardenPlants(userId).first()
            val plant = gardenPlants.find { it.id == plantId }
            
            if (plant == null) {
                Log.d(TAG, "Plant no longer exists in garden")
                return Result.success()
            }


            val timeSinceWatering = if (USE_MINUTES_FOR_TESTING) {
                (System.currentTimeMillis() - plant.lastWateredDate) / (60 * 1000) // minutes
            } else {
                (System.currentTimeMillis() - plant.lastWateredDate) / (24 * 60 * 60 * 1000) // days
            }
            val timeUnit = if (USE_MINUTES_FOR_TESTING) "minutes" else "days"
            
            if (timeSinceWatering >= plant.wateringPeriodDays) {
                val notification = Notification(
                    userId = userId,
                    type = NotificationType.WATERING,
                    title = "Watering Reminder",
                    message = "Time to water your $plantName! It's been ${plant.wateringPeriodDays} $timeUnit since last watering.",
                    timestamp = Date()
                )

                notificationRepository.createNotification(notification)
                Log.d(TAG, "Watering notification created for $plantName")
            } else {
                Log.d(TAG, "Plant $plantName was already watered recently")
            }

            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Error in WateringNotificationWorker", e)
            Result.retry()
        }
    }
} 