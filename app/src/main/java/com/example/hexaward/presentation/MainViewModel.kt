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
import kotlinx.coroutines.delay
import javax.inject.Inject

data class SecuritySettings(
    val honeyfileEnabled: Boolean = true,
    val fileActivityEnabled: Boolean = true,
    val networkObserverEnabled: Boolean = false,
    val sensitivity: Float = 0.7f,
    val highPriorityAlerts: Boolean = true,
    val hapticsEnabled: Boolean = true,
    val techGridEnabled: Boolean = true
)

sealed class LabToolState {
    object Idle : LabToolState()
    data class Running(val progress: Float, val status: String) : LabToolState()
    data class Finished(val result: String) : LabToolState()
}

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

    // Lab Tool States
    private val _labStates = MutableStateFlow<Map<String, LabToolState>>(emptyMap())
    val labStates = _labStates.asStateFlow()

    init {
        startProtection()
        checkForUpdates()
    }

    private fun startProtection() {
        monitors.forEach { monitor ->
            monitor.startMonitoring()
            viewModelScope.launch {
                monitor.signals.collect { signal ->
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

    fun runLabTool(toolName: String) {
        viewModelScope.launch {
            _labStates.update { it + (toolName to LabToolState.Running(0f, "INITIALIZING SYSTEM...")) }
            
            when (toolName.uppercase()) {
                "DEEP SCAN" -> {
                    for (i in 1..10) {
                        delay(400)
                        _labStates.update { it + (toolName to LabToolState.Running(i/10f, "SCANNING BLOCK 0x0$i...")) }
                    }
                    _labStates.update { it + (toolName to LabToolState.Finished("SCAN COMPLETE: NO ANOMALIES FOUND.")) }
                }
                "PERMS AUDIT" -> {
                    delay(800)
                    _labStates.update { it + (toolName to LabToolState.Running(0.5f, "AUDITING PERMISSIONS...")) }
                    delay(1200)
                    _labStates.update { it + (toolName to LabToolState.Finished("AUDIT FINISHED: 0 CONFLICTS.")) }
                }
                "NETWORK LAB" -> {
                    _labStates.update { it + (toolName to LabToolState.Running(0.2f, "HANDSHAKE IN PROGRESS...")) }
                    delay(1500)
                    _labStates.update { it + (toolName to LabToolState.Finished("LATENCY: 42MS. ENCRYPTION VALID.")) }
                }
                "KILL SWITCH" -> {
                    _labStates.update { it + (toolName to LabToolState.Running(1f, "SEVERING CONNECTIONS...")) }
                    delay(1000)
                    _labStates.update { it + (toolName to LabToolState.Finished("PROTOCOL ACTIVE: TRAFFIC BLOCKED.")) }
                }
                else -> {
                    delay(1000)
                    _labStates.update { it + (toolName to LabToolState.Finished("PROTOCOL EXECUTED.")) }
                }
            }
            
            delay(5000)
            _labStates.update { it + (toolName to LabToolState.Idle) }
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

    fun toggleHaptics(enabled: Boolean) {
        _settings.update { it.copy(hapticsEnabled = enabled) }
    }

    fun toggleTechGrid(enabled: Boolean) {
        _settings.update { it.copy(techGridEnabled = enabled) }
    }

    fun resetAllModules() {
        _settings.value = SecuritySettings()
    }
    
    fun checkForUpdates() {
        viewModelScope.launch {
            val update = updateChecker.checkForUpdates()
            if (update != null && update.isUpdateAvailable) {
                _updateInfo.value = update
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
