package com.example.hexaward.data.update

/**
 * UPDATE SYSTEM SETUP GUIDE:
 * 
 * 1. TESTING (Current Mode):
 *    - TEST_MODE = true (shows fake update dialog)
 *    - No internet/server needed
 *    - Perfect for testing the UI
 * 
 * 2. PRODUCTION SETUP:
 *    a) Set TEST_MODE = false
 *    b) Use GitHub Releases:
 *       - Create a GitHub release with your APK as an asset
 *       - Update UPDATE_CHECK_URL to your GitHub releases API endpoint
 *       - Example: https://api.github.com/repos/username/repo/releases/latest
 *    
 *    c) GitHub Release format:
 *       - Create a release on GitHub with tag format "v1.0.0"
 *       - Upload your APK file as a release asset
 *       - Release notes will be automatically used
 *    
 *    d) Update UPDATE_CHECK_URL with your GitHub releases API URL
 *    
 * 3. RELEASE PROCESS:
 *    - Build APK: Build > Build Bundle(s) / APK(s) > Build APK(s)
 *    - Create a new GitHub release and upload the APK
 *    - App will auto-check and notify users!
 */

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.hexaward.R
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton

data class UpdateInfo(
    val latestVersion: String,
    val currentVersion: String,
    val downloadUrl: String,
    val releaseNotes: String,
    val isUpdateAvailable: Boolean
)

@Singleton
class UpdateChecker @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val TAG = "UpdateChecker"
        private const val CHANNEL_ID = "app_updates"
        private const val NOTIFICATION_ID = 1001
        
        // GitHub Releases API URL (update with your repository)
        private const val UPDATE_CHECK_URL = "https://api.github.com/repos/Deepak-Kambam/HexaWard/releases/latest"
        
        // Set to true to enable test mode (shows fake update for testing)
        private const val TEST_MODE = false  // Changed from true to false to use GitHub API by default
    }

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "App Updates",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for app updates"
                enableVibration(true)
            }
            
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    suspend fun checkForUpdates(): UpdateInfo? = withContext(Dispatchers.IO) {
        try {
            val currentVersion = getCurrentVersion()
            Log.d(TAG, "Current version: $currentVersion")
            
            // TEST MODE: Return fake update for testing
            if (TEST_MODE) {
                Log.d(TAG, "TEST MODE: Returning fake update")
                // Using a real APK URL for testing - you can replace with your own
                // This uses a sample APK from GitHub (replace with your actual APK)
                return@withContext UpdateInfo(
                    latestVersion = "2.0.0",
                    currentVersion = currentVersion,
                    // For testing, use your own hosted APK or build and host one
                    // Example: Upload your APK to GitHub releases and use that URL
                    downloadUrl = "https://github.com/AppIntro/AppIntro/releases/download/v6.2.0/AppIntro-6.2.0.apk",
                    releaseNotes = "🎉 New Features:\n" +
                            "• Improved dashboard UI\n" +
                            "• App icons in alerts\n" +
                            "• Better notifications\n" +
                            "• In-app updates\n" +
                            "• Bug fixes and improvements",
                    isUpdateAvailable = true
                )
            }
            
            val (latestVersion, downloadUrl, releaseNotes) = fetchLatestVersionFromGitHub()
                ?: return@withContext null
            
            Log.d(TAG, "Latest version: $latestVersion")
            
            val isUpdateAvailable = isNewerVersion(currentVersion, latestVersion)
            
            UpdateInfo(
                latestVersion = latestVersion,
                currentVersion = currentVersion,
                downloadUrl = downloadUrl,
                releaseNotes = releaseNotes,
                isUpdateAvailable = isUpdateAvailable
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error checking for updates", e)
            null
        }
    }

    private fun fetchLatestVersion(): Triple<String, String, String>? {
        return try {
            val url = URL(UPDATE_CHECK_URL)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.setRequestProperty("Accept", "application/json")
            connection.connectTimeout = 10000
            connection.readTimeout = 10000
            
            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                val json = JSONObject(response)
                
                // Simple JSON format:
                // {
                //   "version": "1.2.0",
                //   "downloadUrl": "https://example.com/app.apk",
                //   "releaseNotes": "What's new..."
                // }
                val version = json.getString("version").removePrefix("v")
                val downloadUrl = json.getString("downloadUrl")
                val releaseNotes = json.optString("releaseNotes", "Bug fixes and improvements")
                
                Triple(version, downloadUrl, releaseNotes)
            } else {
                Log.w(TAG, "Update check failed with code: ${connection.responseCode}")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching update info", e)
            null
        }
    }
    
    // Alternative: GitHub Releases API method (if you prefer GitHub releases)
    private fun fetchLatestVersionFromGitHub(): Triple<String, String, String>? {
        return try {
            val url = URL(UPDATE_CHECK_URL)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.setRequestProperty("Accept", "application/json")
            connection.connectTimeout = 10000
            connection.readTimeout = 10000
            
            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                val json = JSONObject(response)
                
                val tagName = json.getString("tag_name").removePrefix("v")
                val releaseNotes = json.optString("body", "No release notes available")
                
                // Get download URL for APK from assets
                val assets = json.getJSONArray("assets")
                var downloadUrl = ""
                for (i in 0 until assets.length()) {
                    val asset = assets.getJSONObject(i)
                    val name = asset.getString("name")
                    if (name.endsWith(".apk")) {
                        downloadUrl = asset.getString("browser_download_url")
                        break
                    }
                }
                
                if (downloadUrl.isEmpty()) {
                    Log.w(TAG, "No APK found in release assets")
                    return null
                }
                
                Triple(tagName, downloadUrl, releaseNotes)
            } else {
                Log.w(TAG, "Update check failed with code: ${connection.responseCode}")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching from GitHub", e)
            null
        }
    }

    private fun getCurrentVersion(): String {
        return try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            packageInfo.versionName ?: "1.0.0"
        } catch (e: Exception) {
            "1.0.0"
        }
    }

    private fun isNewerVersion(current: String, latest: String): Boolean {
        return try {
            val currentParts = current.split(".").map { it.toIntOrNull() ?: 0 }
            val latestParts = latest.split(".").map { it.toIntOrNull() ?: 0 }
            
            for (i in 0 until maxOf(currentParts.size, latestParts.size)) {
                val currentPart = currentParts.getOrNull(i) ?: 0
                val latestPart = latestParts.getOrNull(i) ?: 0
                
                if (latestPart > currentPart) return true
                if (latestPart < currentPart) return false
            }
            false
        } catch (e: Exception) {
            false
        }
    }

    fun showUpdateNotification(updateInfo: UpdateInfo) {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse(updateInfo.downloadUrl)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("HexaWard Update Available")
            .setContentText("Version ${updateInfo.latestVersion} is now available")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("New version ${updateInfo.latestVersion} is available!\n\n${updateInfo.releaseNotes.take(200)}")
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .addAction(
                android.R.drawable.stat_sys_download,
                "Download",
                pendingIntent
            )
            .build()

        try {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
            Log.d(TAG, "Update notification shown")
        } catch (e: SecurityException) {
            Log.e(TAG, "No notification permission", e)
        }
    }
}
