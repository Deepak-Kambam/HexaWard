package com.example.hexaward.domain.model

import java.util.UUID

enum class SignalSource {
    FILE_ACTIVITY,
    PERMISSION_WATCHDOG,
    RESOURCE_USAGE,
    BACKGROUND_EXECUTION,
    NETWORK_OBSERVER
}

enum class SignalSeverity {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL
}

data class SecuritySignal(
    val id: String = UUID.randomUUID().toString(),
    val source: SignalSource,
    val severity: SignalSeverity,
    val description: String,
    val timestamp: Long = System.currentTimeMillis(),
    val metadata: Map<String, String> = emptyMap()
)
