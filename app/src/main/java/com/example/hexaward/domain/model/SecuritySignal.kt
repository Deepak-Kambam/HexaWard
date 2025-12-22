package com.example.hexaward.domain.model

import java.util.UUID

enum class SignalSource {
    FILE_ACTIVITY,
    PERMISSION_WATCHDOG,
    RESOURCE_USAGE,
    BACKGROUND_EXECUTION,
    NETWORK_OBSERVER,
    HONEYFILE
}

enum class SignalType {
    MASS_FILE_MODIFICATION,
    RESOURCE_SPIKE,
    SUSPICIOUS_EXTENSION,
    SUSPICIOUS_PERMISSION_COMBO,
    HONEYFILE_TOUCHED,
    NETWORK_ANOMALY,
    UNAUTHORIZED_BACKGROUND_START
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
    val type: SignalType,
    val severity: SignalSeverity,
    val description: String,
    val timestamp: Long = System.currentTimeMillis(),
    val metadata: Map<String, String> = emptyMap()
)
