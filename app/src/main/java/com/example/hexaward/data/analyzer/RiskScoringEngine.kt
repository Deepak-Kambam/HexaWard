package com.example.hexaward.data.analyzer

import com.example.hexaward.domain.model.SignalType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RiskScoringEngine @Inject constructor() {

    private val _currentRiskScore = MutableStateFlow(0)
    val currentRiskScore: StateFlow<Int> = _currentRiskScore.asStateFlow()

    // Weights representing how "indicative" a behavior is of ransomware
    private val weights = mapOf(
        SignalType.MASS_FILE_MODIFICATION to 45, // Strongest indicator
        SignalType.HONEYFILE_TOUCHED to 50,       // Definitive indicator
        SignalType.RESOURCE_SPIKE to 25,         // Supporting indicator
        SignalType.SUSPICIOUS_EXTENSION to 30,    // Direct indicator
        SignalType.NETWORK_ANOMALY to 20,
        SignalType.SUSPICIOUS_PERMISSION_COMBO to 15
    )

    private val activeSignals = mutableSetOf<SignalType>()

    /**
     * Updates the risk score based on new signals.
     * Logic: We use weighted accumulation to avoid false positives 
     * but escalate quickly when multiple types appear.
     */
    fun processSignal(type: SignalType) {
        activeSignals.add(type)
        calculate()
    }

    fun clearSignals() {
        activeSignals.clear()
        _currentRiskScore.value = 0
    }

    private fun calculate() {
        var score = 0
        activeSignals.forEach { signal ->
            score += weights[signal] ?: 0
        }
        
        // Cap at 100
        _currentRiskScore.value = score.coerceIn(0, 100)
    }
}
