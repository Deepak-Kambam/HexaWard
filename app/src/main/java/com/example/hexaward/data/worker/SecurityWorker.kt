package com.example.hexaward.data.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.example.hexaward.domain.monitor.SignalMonitor
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.concurrent.TimeUnit

/**
 * Periodically triggers security monitors to perform background checks.
 * This ensures that even if the app is not in the foreground, we still
 * get snapshots of device behavior.
 */
@HiltWorker
class SecurityWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val monitors: Set<@JvmSuppressWildcards SignalMonitor>
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        // Trigger a one-off check for all monitors that support it
        monitors.forEach { it.startMonitoring() }
        
        return Result.success()
    }

    companion object {
        private const val WORK_NAME = "HexaWardSecurityWork"

        fun enqueuePeriodicWork(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                .build()

            val workRequest = PeriodicWorkRequestBuilder<SecurityWorker>(
                15, TimeUnit.MINUTES // Minimum interval for periodic work
            )
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                workRequest
            )
        }
    }
}
