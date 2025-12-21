package com.example.hexaward.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hexaward.domain.analyzer.BehaviorAnalyzer
import com.example.hexaward.domain.model.RiskStatus
import com.example.hexaward.domain.monitor.SignalMonitor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val analyzer: BehaviorAnalyzer,
    private val monitors: Set<@JvmSuppressWildcards SignalMonitor>
) : ViewModel() {

    val riskStatus: StateFlow<RiskStatus> = analyzer.riskStatus

    init {
        startProtection()
    }

    private fun startProtection() {
        monitors.forEach { monitor ->
            monitor.startMonitoring()
            viewModelScope.launch {
                monitor.signals.collect { signal ->
                    analyzer.processSignal(signal)
                }
            }
        }
    }

    fun refreshStatus() {
        // Force a re-check if needed
    }
}
