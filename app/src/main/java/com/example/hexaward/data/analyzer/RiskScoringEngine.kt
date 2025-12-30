package com.example.hexaward.data.analyzer

import com.example.hexaward.domain.model.SignalType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RiskScoringEngine @Inject constructor() {

    private val _currentRiskScore = MutableStateFlow(0)
    val currentRiskScore: StateFlow<Int> = _currentRiskScore.asStateFlow()

    private val mutex = Mutex()

    // Weights representing how "indicative" a behavior is of ransomware
    private val weights = mapOf(
        SignalType.MASS_FILE_MODIFICATION to 45, // Strongest indicator
        SignalType.HONEYFILE_TOUCHED to 50,       // Definitive indicator
        SignalType.RESOURCE_SPIKE to 25,         // Supporting indicator
        SignalType.SUSPICIOUS_EXTENSION to 30,    // Direct indicator
        SignalType.NETWORK_ANOMALY to 20,
        SignalType.SUSPICIOUS_PERMISSION_COMBO to 15,
        SignalType.UNAUTHORIZED_BACKGROUND_START to 10
    )

    // Store timestamps of when each signal type was last seen
    private val signalTimestamps = mutableMapOf<SignalType, Long>()
    
    // How long a signal stays "active" in the score (e.g., 30 minutes)
    private val SIGNAL_EXPIRY_MS = 30 * 60 * 1000L

    /**
     * Updates the risk score based on new signals.
     */
    suspend fun processSignal(type: SignalType) {
        mutex.withLock {
            signalTimestamps[type] = System.currentTimeMillis()
            calculateInternal()
        }
    }

    suspend fun clearSignals() {
        mutex.withLock {
            signalTimestamps.clear()
            _currentRiskScore.value = 0
        }
    }

    /**
     * Can be called periodically to expire old signals and reduce risk score
     */
    suspend fun decayScore() {
        mutex.withLock {
            val now = System.currentTimeMillis()
            val beforeSize = signalTimestamps.size
            signalTimestamps.entries.removeIf { now - it.value > SIGNAL_EXPIRY_MS }
            
            if (signalTimestamps.size != beforeSize) {
                calculateInternal()
            }
        }
    }

    private fun calculateInternal() {
        var totalScore = 0
        val now = System.currentTimeMillis()
        
        signalTimestamps.forEach { (type, timestamp) ->
            val weight = weights[type] ?: 0
            
            // Apply a minor decay based on time (fresh signals carry full weight, 
            // signals nearing expiry carry half weight)
            val age = now - timestamp
            val ageFactor = if (age < SIGNAL_EXPIRY_MS / 2) 1.0 else 0.5
            
            totalScore += (weight * ageFactor).toInt()
        }
        
        _currentRiskScore.value = totalScore.coerceIn(0, 100)
    }
}
