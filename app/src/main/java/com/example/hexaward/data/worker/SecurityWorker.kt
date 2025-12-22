package com.example.hexaward.data.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.example.hexaward.domain.monitor.SignalMonitor
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import java.util.concurrent.TimeUnit

/**
 * EntryPoint to access monitors from Hilt in Worker context
 */
@EntryPoint
@InstallIn(SingletonComponent::class)
interface SecurityWorkerEntryPoint {
    fun getMonitors(): Set<@JvmSuppressWildcards SignalMonitor>
}

@HiltWorker
class SecurityWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    // Get monitors from Hilt entry point instead of constructor injection
    private val monitors: Set<SignalMonitor> by lazy {
        EntryPointAccessors.fromApplication(
            applicationContext,
            SecurityWorkerEntryPoint::class.java
        ).getMonitors()
    }

    override suspend fun doWork(): Result {
        Log.d("SecurityWorker", "========================================")
        Log.d("SecurityWorker", "⚡ WORKER TRIGGERED! Initializing monitors...")
        Log.d("SecurityWorker", "========================================")
        
        return try {
            Log.d("SecurityWorker", "🔍 Monitors available: ${monitors.size}")
            
            // Check if monitors were properly injected
            if (monitors.isEmpty()) {
                Log.w("SecurityWorker", "❌ No monitors injected, will retry")
                return Result.retry()
            }
            
            Log.d("SecurityWorker", "✅ Found ${monitors.size} monitors, starting them...")
            
            monitors.forEach { monitor ->
                try {
                    monitor.startMonitoring()
                    Log.d("SecurityWorker", "  ✅ Started: ${monitor.javaClass.simpleName}")
                } catch (e: Exception) {
                    Log.e("SecurityWorker", "  ❌ Failed to start ${monitor.javaClass.simpleName}", e)
                }
            }
            Log.d("SecurityWorker", "🎉 All monitors started successfully")
            Log.d("SecurityWorker", "========================================")
            Result.success()
        } catch (e: Exception) {
            Log.e("SecurityWorker", "❌ Critical worker failure", e)
            Result.retry()
        }
    }

    companion object {
        private const val WORK_NAME = "HexaWardSecurityWork"

        fun enqueuePeriodicWork(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                .build()

            val workRequest = PeriodicWorkRequestBuilder<SecurityWorker>(
                15, TimeUnit.MINUTES
            )
                .setConstraints(constraints)
                .setInitialDelay(30, TimeUnit.SECONDS) // Add delay to ensure Hilt is fully initialized
                .setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL,
                    WorkRequest.MIN_BACKOFF_MILLIS,
                    TimeUnit.MILLISECONDS
                )
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                workRequest
            )
            Log.d("SecurityWorker", "Periodic work enqueued successfully with 30s initial delay")
        }
    }
}
