package com.example.hexaward

import android.app.Application
import android.util.Log
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.example.hexaward.data.worker.SecurityWorker
import com.example.hexaward.data.worker.UpdateCheckWorker
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class HexaWardApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .setMinimumLoggingLevel(Log.DEBUG)
            .build()

    override fun onCreate() {
        super.onCreate()
        Log.d("HexaWardApp", "========================================")
        Log.d("HexaWardApp", "Application started. Initializing WorkManager...")
        Log.d("HexaWardApp", "========================================")
        
        // Initialize WorkManager explicitly to ensure Hilt factory is ready
        try {
            androidx.work.WorkManager.getInstance(this)
            Log.d("HexaWardApp", "✅ WorkManager initialized successfully")
            
            // Enqueue immediate test worker first
            val immediateWork = androidx.work.OneTimeWorkRequestBuilder<SecurityWorker>().build()
            androidx.work.WorkManager.getInstance(this).enqueue(immediateWork)
            Log.d("HexaWardApp", "✅ Immediate test worker enqueued")
            
            // Start periodic security monitoring
            SecurityWorker.enqueuePeriodicWork(this)
            Log.d("HexaWardApp", "✅ Periodic security worker enqueued")
            
            // Schedule periodic update checks
            UpdateCheckWorker.enqueuePeriodicCheck(this)
            Log.d("HexaWardApp", "✅ Update checker scheduled")
            
            // Check for updates immediately (for testing)
            UpdateCheckWorker.checkNow(this)
            Log.d("HexaWardApp", "✅ Immediate update check triggered")
        } catch (e: Exception) {
            Log.e("HexaWardApp", "❌ Failed to initialize WorkManager", e)
        }
    }
}
