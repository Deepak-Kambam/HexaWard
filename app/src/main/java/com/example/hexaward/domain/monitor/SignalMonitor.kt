package com.example.hexaward.domain.monitor

import com.example.hexaward.domain.model.SecuritySignal
import kotlinx.coroutines.flow.Flow

interface SignalMonitor {
    val signals: Flow<SecuritySignal>
    fun startMonitoring()
    fun stopMonitoring()
}
