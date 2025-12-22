package com.example.hexaward.data.update

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.util.Log
import androidx.core.content.FileProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

sealed class DownloadState {
    object Idle : DownloadState()
    data class Downloading(val progress: Int) : DownloadState()
    data class Downloaded(val file: File) : DownloadState()
    data class Error(val message: String) : DownloadState()
}

@Singleton
class ApkDownloader @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val TAG = "ApkDownloader"
    }

    private val _downloadState = MutableStateFlow<DownloadState>(DownloadState.Idle)
    val downloadState: StateFlow<DownloadState> = _downloadState

    private var downloadId: Long = -1
    private var downloadReceiver: BroadcastReceiver? = null
    private var installReceiver: BroadcastReceiver? = null

    fun downloadAndInstall(downloadUrl: String, version: String) {
        try {
            Log.d(TAG, "Starting download: $downloadUrl")
            
            val fileName = "HexaWard-v$version.apk"
            val request = DownloadManager.Request(Uri.parse(downloadUrl)).apply {
                setTitle("HexaWard Update")
                setDescription("Downloading version $version")
                setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
                setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
            }

            val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            downloadId = downloadManager.enqueue(request)

            _downloadState.value = DownloadState.Downloading(0)

            // Register receiver to listen for download completion
            downloadReceiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context, intent: Intent) {
                    val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
                    if (id == downloadId) {
                        handleDownloadComplete(downloadManager, id)
                    }
                }
            }

            context.registerReceiver(
                downloadReceiver,
                IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE),
                Context.RECEIVER_NOT_EXPORTED
            )

            // Start progress monitoring
            monitorProgress(downloadManager)

        } catch (e: Exception) {
            Log.e(TAG, "Download failed", e)
            _downloadState.value = DownloadState.Error(e.message ?: "Download failed")
        }
    }

    private fun monitorProgress(downloadManager: DownloadManager) {
        Thread {
            var downloading = true
            while (downloading) {
                val query = DownloadManager.Query().setFilterById(downloadId)
                val cursor = downloadManager.query(query)
                
                if (cursor.moveToFirst()) {
                    val bytesDownloaded = cursor.getLong(
                        cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR)
                    )
                    val bytesTotal = cursor.getLong(
                        cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_TOTAL_SIZE_BYTES)
                    )
                    
                    if (bytesTotal > 0) {
                        val progress = ((bytesDownloaded * 100) / bytesTotal).toInt()
                        _downloadState.value = DownloadState.Downloading(progress)
                    }

                    val status = cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS))
                    
                    when (status) {
                        DownloadManager.STATUS_RUNNING, DownloadManager.STATUS_PENDING -> {
                            downloading = true
                        }
                        DownloadManager.STATUS_FAILED -> {
                            val reason = cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_REASON))
                            cursor.close()
                            _downloadState.value = DownloadState.Error("Download failed (code: $reason)")
                            downloading = false
                        }
                        else -> {
                            downloading = false
                        }
                    }
                } else {
                    // Download was cancelled
                    cursor.close()
                    Log.d(TAG, "Download was cancelled by user")
                    _downloadState.value = DownloadState.Idle
                    downloading = false
                }
                
                if (!cursor.isClosed) {
                    cursor.close()
                }
                
                if (downloading) {
                    Thread.sleep(500)
                }
            }
        }.start()
    }

    private fun handleDownloadComplete(downloadManager: DownloadManager, id: Long) {
        val query = DownloadManager.Query().setFilterById(id)
        val cursor = downloadManager.query(query)

        if (cursor.moveToFirst()) {
            val status = cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS))
            
            if (status == DownloadManager.STATUS_SUCCESSFUL) {
                val uriString = cursor.getString(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_LOCAL_URI))
                cursor.close()
                
                if (uriString != null) {
                    val file = File(Uri.parse(uriString).path ?: "")
                    _downloadState.value = DownloadState.Downloaded(file)
                    installApk(file)
                } else {
                    _downloadState.value = DownloadState.Error("Downloaded file not found")
                }
            } else {
                cursor.close()
                _downloadState.value = DownloadState.Error("Download failed")
            }
        } else {
            cursor.close()
            _downloadState.value = DownloadState.Error("Download not found")
        }

        // Unregister receiver
        try {
            downloadReceiver?.let { context.unregisterReceiver(it) }
        } catch (e: Exception) {
            Log.e(TAG, "Error unregistering receiver", e)
        }
    }

    private fun installApk(file: File) {
        try {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    // Use FileProvider for Android N and above
                    val uri = FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.fileprovider",
                        file
                    )
                    setDataAndType(uri, "application/vnd.android.package-archive")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                } else {
                    setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive")
                }
            }
            
            // Register receiver to listen for installation completion
            registerInstallationCompleteReceiver()
            
            context.startActivity(intent)
            Log.d(TAG, "APK installation started")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to install APK", e)
            _downloadState.value = DownloadState.Error("Installation failed: ${e.message}")
        }
    }
    
    private fun registerInstallationCompleteReceiver() {
        try {
            // Unregister previous receiver if exists
            installReceiver?.let { 
                try {
                    context.unregisterReceiver(it)
                } catch (e: Exception) {
                    Log.e(TAG, "Error unregistering install receiver", e)
                }
            }
            
            installReceiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context, intent: Intent) {
                    when (intent.action) {
                        Intent.ACTION_PACKAGE_REPLACED -> {
                            val packageName = intent.data?.schemeSpecificPart
                            if (packageName == context.packageName) {
                                Log.d(TAG, "App updated successfully! Restarting...")
                                // App was updated, restart it
                                restartApp()
                            }
                        }
                        Intent.ACTION_PACKAGE_ADDED -> {
                            val packageName = intent.data?.schemeSpecificPart
                            if (packageName == context.packageName) {
                                Log.d(TAG, "App installed successfully!")
                            }
                        }
                    }
                }
            }
            
            val filter = IntentFilter().apply {
                addAction(Intent.ACTION_PACKAGE_REPLACED)
                addAction(Intent.ACTION_PACKAGE_ADDED)
                addDataScheme("package")
            }
            
            context.registerReceiver(installReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
            Log.d(TAG, "Installation completion receiver registered")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to register installation receiver", e)
        }
    }
    
    private fun restartApp() {
        try {
            // Clear download state
            _downloadState.value = DownloadState.Idle
            
            // Unregister receivers
            try {
                installReceiver?.let { context.unregisterReceiver(it) }
                downloadReceiver?.let { context.unregisterReceiver(it) }
            } catch (e: Exception) {
                Log.e(TAG, "Error unregistering receivers", e)
            }
            
            // Restart the app
            val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)
            intent?.let {
                it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                context.startActivity(it)
                
                // Kill current process to ensure clean restart
                android.os.Process.killProcess(android.os.Process.myPid())
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to restart app", e)
        }
    }

    fun reset() {
        _downloadState.value = DownloadState.Idle
        
        // Unregister all receivers
        try {
            downloadReceiver?.let { context.unregisterReceiver(it) }
            downloadReceiver = null
        } catch (e: Exception) {
            Log.e(TAG, "Error unregistering download receiver", e)
        }
        
        try {
            installReceiver?.let { context.unregisterReceiver(it) }
            installReceiver = null
        } catch (e: Exception) {
            Log.e(TAG, "Error unregistering install receiver", e)
        }
    }
}
