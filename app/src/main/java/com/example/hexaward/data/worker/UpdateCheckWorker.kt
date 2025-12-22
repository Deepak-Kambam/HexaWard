package com.example.hexaward.data.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.example.hexaward.data.update.UpdateChecker
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.concurrent.TimeUnit

@HiltWorker
class UpdateCheckWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val updateChecker: UpdateChecker
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        Log.d("UpdateCheckWorker", "Checking for app updates...")
        
        return try {
            val updateInfo = updateChecker.checkForUpdates()
            
            if (updateInfo != null && updateInfo.isUpdateAvailable) {
                Log.d("UpdateCheckWorker", "Update available: ${updateInfo.latestVersion}")
                updateChecker.showUpdateNotification(updateInfo)
            } else {
                Log.d("UpdateCheckWorker", "No update available")
            }
            
            Result.success()
        } catch (e: Exception) {
            Log.e("UpdateCheckWorker", "Update check failed", e)
            Result.retry()
        }
    }

    companion object {
        private const val WORK_NAME = "UpdateCheckWork"

        fun enqueuePeriodicCheck(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val workRequest = PeriodicWorkRequestBuilder<UpdateCheckWorker>(
                24, TimeUnit.HOURS // Check once per day
            )
                .setConstraints(constraints)
                .setInitialDelay(1, TimeUnit.HOURS) // First check after 1 hour
                .setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL,
                    WorkRequest.MIN_BACKOFF_MILLIS,
                    TimeUnit.MILLISECONDS
                )
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                workRequest
            )
            
            Log.d("UpdateCheckWorker", "Periodic update check scheduled")
        }
        
        fun checkNow(context: Context) {
            val workRequest = OneTimeWorkRequestBuilder<UpdateCheckWorker>()
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .build()
            
            WorkManager.getInstance(context).enqueue(workRequest)
            Log.d("UpdateCheckWorker", "Immediate update check requested")
        }
    }
}
