package com.example.hexaward.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hexaward.data.update.ApkDownloader
import com.example.hexaward.data.update.UpdateChecker
import com.example.hexaward.data.update.UpdateInfo
import com.example.hexaward.domain.analyzer.BehaviorAnalyzer
import com.example.hexaward.domain.model.RiskStatus
import com.example.hexaward.domain.monitor.SignalMonitor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SecuritySettings(
    val honeyfileEnabled: Boolean = true,
    val fileActivityEnabled: Boolean = true,
    val networkObserverEnabled: Boolean = false,
    val sensitivity: Float = 0.7f,
    val highPriorityAlerts: Boolean = true
)

@HiltViewModel
class MainViewModel @Inject constructor(
    private val analyzer: BehaviorAnalyzer,
    private val monitors: Set<@JvmSuppressWildcards SignalMonitor>,
    private val updateChecker: UpdateChecker,
    private val apkDownloader: ApkDownloader
) : ViewModel() {

    val riskStatus: StateFlow<RiskStatus> = analyzer.riskStatus

    private val _settings = MutableStateFlow(SecuritySettings())
    val settings: StateFlow<SecuritySettings> = _settings.asStateFlow()
    
    private val _updateInfo = MutableStateFlow<UpdateInfo?>(null)
    val updateInfo: StateFlow<UpdateInfo?> = _updateInfo.asStateFlow()
    
    val downloadState = apkDownloader.downloadState

    init {
        startProtection()
        // Check for updates immediately when ViewModel is created
        checkForUpdates()
    }

    private fun startProtection() {
        monitors.forEach { monitor ->
            monitor.startMonitoring()
            viewModelScope.launch {
                monitor.signals.collect { signal ->
                    // Only process signals if the corresponding module is enabled
                    if (isModuleEnabled(signal.source.name)) {
                        analyzer.processSignal(signal)
                    }
                }
            }
        }
    }

    private fun isModuleEnabled(sourceName: String): Boolean {
        return when (sourceName) {
            "HONEYFILE" -> _settings.value.honeyfileEnabled
            "FILE_ACTIVITY" -> _settings.value.fileActivityEnabled
            "NETWORK_OBSERVER" -> _settings.value.networkObserverEnabled
            else -> true
        }
    }

    fun toggleHoneyfile(enabled: Boolean) {
        _settings.update { it.copy(honeyfileEnabled = enabled) }
    }

    fun toggleFileActivity(enabled: Boolean) {
        _settings.update { it.copy(fileActivityEnabled = enabled) }
    }

    fun toggleNetworkObserver(enabled: Boolean) {
        _settings.update { it.copy(networkObserverEnabled = enabled) }
    }

    fun setSensitivity(value: Float) {
        _settings.update { it.copy(sensitivity = value) }
    }

    fun toggleHighPriorityAlerts(enabled: Boolean) {
        _settings.update { it.copy(highPriorityAlerts = enabled) }
    }

    fun resetAllModules() {
        _settings.value = SecuritySettings()
    }
    
    fun checkForUpdates() {
        viewModelScope.launch {
            android.util.Log.d("MainViewModel", "Checking for updates...")
            val update = updateChecker.checkForUpdates()
            android.util.Log.d("MainViewModel", "Update result: $update")
            if (update != null && update.isUpdateAvailable) {
                android.util.Log.d("MainViewModel", "Update IS available, setting state")
                _updateInfo.value = update
            } else {
                android.util.Log.d("MainViewModel", "No update available or null")
            }
        }
    }
    
    fun dismissUpdate() {
        _updateInfo.value = null
    }
    
    fun startDownload(downloadUrl: String, version: String) {
        apkDownloader.downloadAndInstall(downloadUrl, version)
    }
}
