package com.example.hexaward.domain.analyzer

import com.example.hexaward.domain.model.RiskStatus
import com.example.hexaward.domain.model.SecuritySignal
import kotlinx.coroutines.flow.StateFlow

interface BehaviorAnalyzer {
    val riskStatus: StateFlow<RiskStatus>
    suspend fun processSignal(signal: SecuritySignal)
}
