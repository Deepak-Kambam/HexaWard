package com.example.hexaward.data.monitor

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import com.example.hexaward.domain.model.SecuritySignal
import com.example.hexaward.domain.model.SignalSeverity
import com.example.hexaward.domain.model.SignalSource
import com.example.hexaward.domain.monitor.SignalMonitor
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Monitors apps for dangerous permission combinations that might indicate
 * exfiltration or ransomware capabilities (e.g., Storage + Internet).
 */
@Singleton
class PermissionWatchdog @Inject constructor(
    @ApplicationContext private val context: Context
) : SignalMonitor {

    private val _signals = MutableSharedFlow<SecuritySignal>()
    override val signals: SharedFlow<SecuritySignal> = _signals.asSharedFlow()
    
    private val scope = CoroutineScope(Dispatchers.Default)

    override fun startMonitoring() {
        checkDangerousPermissions()
    }

    override fun stopMonitoring() {}

    private fun checkDangerousPermissions() {
        val pm = context.packageManager
        val packages = pm.getInstalledPackages(PackageManager.GET_PERMISSIONS)

        for (pkg in packages) {
            if (isSystemApp(pkg) || pkg.packageName == context.packageName) continue

            val requestedPermissions = pkg.requestedPermissions ?: continue
            
            val hasStorage = requestedPermissions.any { 
                it.contains("READ_EXTERNAL_STORAGE") || it.contains("WRITE_EXTERNAL_STORAGE") || it.contains("MANAGE_EXTERNAL_STORAGE") 
            }
            val hasInternet = requestedPermissions.contains(android.Manifest.permission.INTERNET)

            if (hasStorage && hasInternet) {
                scope.launch {
                    val signal = SecuritySignal(
                        source = SignalSource.PERMISSION_WATCHDOG,
                        severity = SignalSeverity.LOW,
                        description = "Suspicious capability: App '${pkg.packageName}' has both Storage and Internet access.",
                        metadata = mapOf("package" to pkg.packageName)
                    )
                    _signals.emit(signal)
                }
            }
        }
    }

    private fun isSystemApp(pkg: PackageInfo): Boolean {
        val flags = pkg.applicationInfo?.flags ?: return false
        return (flags and android.content.pm.ApplicationInfo.FLAG_SYSTEM) != 0
    }
}
