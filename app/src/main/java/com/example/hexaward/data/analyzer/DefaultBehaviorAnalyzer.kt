package com.example.hexaward.data.analyzer

import com.example.hexaward.domain.analyzer.BehaviorAnalyzer
import com.example.hexaward.domain.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DefaultBehaviorAnalyzer @Inject constructor(
    private val scoringEngine: RiskScoringEngine
) : BehaviorAnalyzer {

    private val _riskStatus = MutableStateFlow(RiskStatus(0, RiskLevel.SAFE, emptyList()))
    override val riskStatus: StateFlow<RiskStatus> = _riskStatus.asStateFlow()

    private val mutex = Mutex()
    private val recentSignals = mutableListOf<SecuritySignal>()
    
    // Max signals to keep in memory to prevent leaks
    private val MAX_SIGNAL_HISTORY = 50 

    override suspend fun processSignal(signal: SecuritySignal) {
        mutex.withLock {
            // 1. Add new signal
            recentSignals.add(signal)
            
            // 2. Pass signal to the scoring engine
            scoringEngine.processSignal(signal.type)

            // 3. Cleanup: Keep only signals from the last hour AND respect a max count
            val oneHourAgo = System.currentTimeMillis() - 3600000
            recentSignals.removeAll { it.timestamp < oneHourAgo }
            
            if (recentSignals.size > MAX_SIGNAL_HISTORY) {
                // Keep the most recent ones
                val toRemove = recentSignals.size - MAX_SIGNAL_HISTORY
                repeat(toRemove) { recentSignals.removeAt(0) }
            }

            updateRiskStatus()
        }
    }

    private fun updateRiskStatus() {
        val score = scoringEngine.currentRiskScore.value
        
        val level = when {
            score >= 80 -> RiskLevel.CRITICAL
            score >= 50 -> RiskLevel.HIGH
            score >= 25 -> RiskLevel.MODERATE
            score >= 10 -> RiskLevel.LOW
            else -> RiskLevel.SAFE
        }

        _riskStatus.update { 
            it.copy(
                score = score, 
                level = level, 
                // Create a defensive copy to prevent ConcurrentModificationException in UI
                triggers = recentSignals.toList() 
            ) 
        }
    }
}
