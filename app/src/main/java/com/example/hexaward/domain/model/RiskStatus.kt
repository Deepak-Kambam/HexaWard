package com.example.hexaward.domain.model

data class RiskStatus(
    val score: Int, // 0 to 100
    val level: RiskLevel,
    val triggers: List<SecuritySignal>
)

enum class RiskLevel {
    SAFE,
    LOW,
    MODERATE,
    HIGH,
    CRITICAL
}
