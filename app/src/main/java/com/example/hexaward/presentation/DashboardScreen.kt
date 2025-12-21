package com.example.hexaward.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hexaward.domain.model.RiskLevel
import com.example.hexaward.domain.model.RiskStatus
import com.example.hexaward.domain.model.SecuritySignal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(viewModel: MainViewModel) {
    val riskStatus by viewModel.riskStatus.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("HexaWard Security") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            RiskIndicator(riskStatus)
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "Recent Security Signals",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.align(Alignment.Start)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(riskStatus.triggers.reversed()) { signal ->
                    SignalItem(signal)
                }
            }
        }
    }
}

@Composable
fun RiskIndicator(status: RiskStatus) {
    val color = when (status.level) {
        RiskLevel.SAFE -> Color.Green
        RiskLevel.LOW -> Color.Cyan
        RiskLevel.MODERATE -> Color.Yellow
        RiskLevel.HIGH -> Color(0xFFFFA500) // Orange
        RiskLevel.CRITICAL -> Color.Red
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f))
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = status.level.name,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = "Risk Score: ${status.score}/100",
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@Composable
fun SignalItem(signal: SecuritySignal) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = signal.source.name,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = signal.severity.name,
                    style = MaterialTheme.typography.labelSmall,
                    color = when (signal.severity) {
                        com.example.hexaward.domain.model.SignalSeverity.CRITICAL -> Color.Red
                        else -> MaterialTheme.colorScheme.secondary
                    }
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = signal.description, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
