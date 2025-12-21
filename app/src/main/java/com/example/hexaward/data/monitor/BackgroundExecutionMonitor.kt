package com.example.hexaward.data.monitor

import android.content.Context
import com.example.hexaward.domain.model.SecuritySignal
import com.example.hexaward.domain.monitor.SignalMonitor
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BackgroundExecutionMonitor @Inject constructor(
    @ApplicationContext private val context: Context
) : SignalMonitor {

    private val _signals = MutableSharedFlow<SecuritySignal>()
    override val signals: SharedFlow<SecuritySignal> = _signals.asSharedFlow()

    override fun startMonitoring() {
        // Detect silent foreground services & auto-restarts
    }

    override fun stopMonitoring() {
    }
}
