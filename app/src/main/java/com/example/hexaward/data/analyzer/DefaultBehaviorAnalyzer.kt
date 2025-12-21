package com.example.hexaward.data.analyzer

import com.example.hexaward.domain.analyzer.BehaviorAnalyzer
import com.example.hexaward.domain.model.RiskLevel
import com.example.hexaward.domain.model.RiskStatus
import com.example.hexaward.domain.model.SecuritySignal
import com.example.hexaward.domain.model.SignalSeverity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DefaultBehaviorAnalyzer @Inject constructor() : BehaviorAnalyzer {

    private val _riskStatus = MutableStateFlow(RiskStatus(0, RiskLevel.SAFE, emptyList()))
    override val riskStatus: StateFlow<RiskStatus> = _riskStatus.asStateFlow()

    private val recentSignals = mutableListOf<SecuritySignal>()

    override suspend fun processSignal(signal: SecuritySignal) {
        recentSignals.add(signal)
        // Keep only recent signals (e.g., last 1 hour)
        val oneHourAgo = System.currentTimeMillis() - 3600000
        recentSignals.removeAll { it.timestamp < oneHourAgo }

        calculateRisk()
    }

    private fun calculateRisk() {
        var score = 0
        recentSignals.forEach { signal ->
            score += when (signal.severity) {
                SignalSeverity.LOW -> 5
                SignalSeverity.MEDIUM -> 15
                SignalSeverity.HIGH -> 30
                SignalSeverity.CRITICAL -> 50
            }
        }

        val level = when {
            score >= 80 -> RiskLevel.CRITICAL
            score >= 50 -> RiskLevel.HIGH
            score >= 25 -> RiskLevel.MODERATE
            score >= 10 -> RiskLevel.LOW
            else -> RiskLevel.SAFE
        }

        _riskStatus.update { it.copy(score = score.coerceAtMost(100), level = level, triggers = recentSignals.toList()) }
    }
}
