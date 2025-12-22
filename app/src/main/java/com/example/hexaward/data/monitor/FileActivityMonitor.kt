package com.example.hexaward.data.monitor

import android.content.Context
import com.example.hexaward.domain.model.SecuritySignal
import com.example.hexaward.domain.model.SignalSeverity
import com.example.hexaward.domain.model.SignalSource
import com.example.hexaward.domain.model.SignalType
import com.example.hexaward.domain.monitor.SignalMonitor
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FileActivityMonitor @Inject constructor(
    @ApplicationContext private val context: Context
) : SignalMonitor {

    private val _signals = MutableSharedFlow<SecuritySignal>()
    override val signals: SharedFlow<SecuritySignal> = _signals.asSharedFlow()

    override fun startMonitoring() {
        // Implementation for detecting rapid file renames or modifications
    }

    override fun stopMonitoring() {}

    private suspend fun emitSignal(description: String, type: SignalType, severity: SignalSeverity) {
        _signals.emit(
            SecuritySignal(
                source = SignalSource.FILE_ACTIVITY,
                type = type,
                severity = severity,
                description = description
            )
        )
    }
}
