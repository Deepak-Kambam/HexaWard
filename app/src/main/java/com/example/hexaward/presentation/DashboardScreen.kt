package com.example.hexaward.presentation

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.graphics.drawable.toBitmap
import com.example.hexaward.domain.model.RiskStatus
import com.example.hexaward.domain.model.SecuritySignal
import com.example.hexaward.domain.model.SignalSeverity
import com.example.hexaward.domain.model.SignalType
import com.example.hexaward.domain.model.SignalSource
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: MainViewModel,
    onNavigateToSettings: () -> Unit
) {
    val riskStatus by viewModel.riskStatus.collectAsState()
    
    val startupAnimatable = remember { Animatable(0f) }
    
    LaunchedEffect(riskStatus.score) {
        startupAnimatable.animateTo(
            targetValue = 100f,
            animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing)
        )
        startupAnimatable.animateTo(
            targetValue = riskStatus.score.toFloat(),
            animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing)
        )
    }

    val currentAnimatedValue = startupAnimatable.value
    
    val themeColor = when {
        currentAnimatedValue < 25 -> Color(0xFF4CAF50)
        currentAnimatedValue < 50 -> Color(0xFF2196F3)
        currentAnimatedValue < 75 -> Color(0xFFFF9800)
        else -> Color(0xFFF44336)
    }
    
    val animatedThemeColor by animateColorAsState(
        targetValue = themeColor,
        animationSpec = tween(400),
        label = "theme_color"
    )

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Lock, contentDescription = null, tint = animatedThemeColor)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "HEXAWARD", 
                            fontWeight = FontWeight.ExtraBold, 
                            letterSpacing = 2.sp,
                            color = animatedThemeColor
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings", tint = animatedThemeColor)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            animatedThemeColor.copy(alpha = 0.15f),
                            MaterialTheme.colorScheme.surface
                        )
                    )
                )
                .padding(padding)
        ) {
            DashboardContent(riskStatus, currentAnimatedValue, animatedThemeColor)
        }
    }
}

@Composable
fun DashboardContent(riskStatus: RiskStatus, animatedScoreValue: Float, themeColor: Color) {
    val listState = rememberLazyListState()
    
    // Improved fade effect that allows scrolling to top
    val meterAlpha by remember {
        derivedStateOf {
            val firstVisibleIndex = listState.firstVisibleItemIndex
            val firstVisibleOffset = listState.firstVisibleItemScrollOffset
            
            when {
                firstVisibleIndex > 0 -> 0f
                else -> {
                    // Smooth fade over 400px instead of 500px
                    val fadeDistance = 400f
                    (1f - (firstVisibleOffset.toFloat() / fadeDistance)).coerceIn(0f, 1f)
                }
            }
        }
    }
    
    val meterScale by remember {
        derivedStateOf {
            0.85f + (0.15f * meterAlpha)
        }
    }

    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer {
                        alpha = meterAlpha
                        translationY = -(1f - meterAlpha) * 80f
                        scaleX = meterScale
                        scaleY = meterScale
                    },
                contentAlignment = Alignment.Center
            ) {
                RiskGauge(riskStatus, animatedScoreValue, themeColor)
            }
        }
        
        // Status Summary Card
        item {
            StatusSummaryCard(riskStatus, themeColor)
        }
        
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Notifications, 
                    contentDescription = null, 
                    modifier = Modifier.size(20.dp), 
                    tint = themeColor
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Recent Activity",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = themeColor
                )
                Spacer(Modifier.weight(1f))
                if (riskStatus.triggers.isNotEmpty()) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = themeColor.copy(alpha = 0.1f)
                    ) {
                        Text(
                            text = "${riskStatus.triggers.size}",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = themeColor,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }
        
        if (riskStatus.triggers.isEmpty()) {
            item {
                EmptyStateCard()
            }
        } else {
            // Display alerts as a simple vertical list instead of carousel
            items(riskStatus.triggers.reversed()) { alert ->
                ImprovedSignalCard(signal = alert)
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun RiskGauge(status: RiskStatus, animatedValue: Float, color: Color) {
    val displayScore = animatedValue.toInt()
    
    val statusMessage = when {
        displayScore < 25 -> "Well Protected"
        displayScore < 50 -> "Minor Threats"
        displayScore < 75 -> "Be Cautious"
        else -> "Action Needed!"
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(240.dp)) {
            if (displayScore > 70) {
                val infiniteTransition = rememberInfiniteTransition(label = "pulse")
                val scale by infiniteTransition.animateFloat(
                    initialValue = 1f,
                    targetValue = 1.15f,
                    animationSpec = infiniteRepeatable(tween(800), RepeatMode.Reverse),
                    label = "scale"
                )
                Box(
                    modifier = Modifier
                        .size(190.dp)
                        .clip(CircleShape)
                        .background(color.copy(alpha = 0.1f * scale))
                )
            }

            Canvas(modifier = Modifier.size(200.dp)) {
                drawArc(
                    color = color.copy(alpha = 0.1f),
                    startAngle = 135f,
                    sweepAngle = 270f,
                    useCenter = false,
                    style = Stroke(width = 18.dp.toPx(), cap = StrokeCap.Round)
                )
                drawArc(
                    color = color,
                    startAngle = 135f,
                    sweepAngle = (animatedValue / 100f) * 270f,
                    useCenter = false,
                    style = Stroke(width = 18.dp.toPx(), cap = StrokeCap.Round)
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "$displayScore",
                    fontSize = 54.sp,
                    fontWeight = FontWeight.Black,
                    color = color
                )
                Text(
                    text = status.level.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = color.copy(alpha = 0.9f),
                    letterSpacing = 1.sp
                )
            }
        }
        
        // Status message below the gauge
        Spacer(Modifier.height(8.dp))
        Text(
            text = statusMessage,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = color,
            letterSpacing = 0.5.sp
        )
    }
}

@Composable
fun StatusSummaryCard(riskStatus: RiskStatus, themeColor: Color) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = themeColor.copy(alpha = 0.08f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Equal weight for all items
            StatusItem(
                icon = Icons.Default.Shield,
                label = "Protection",
                value = if (riskStatus.score < 50) "Active" else "At Risk",
                color = themeColor,
                modifier = Modifier.weight(1f)
            )
            
            Divider(
                modifier = Modifier
                    .height(40.dp)
                    .width(1.dp),
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
            )
            
            StatusItem(
                icon = Icons.Default.BugReport,
                label = "Threats",
                value = "${riskStatus.triggers.count { it.severity >= SignalSeverity.MEDIUM }}",
                color = themeColor,
                modifier = Modifier.weight(1f)
            )
            
            Divider(
                modifier = Modifier
                    .height(40.dp)
                    .width(1.dp),
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
            )
            
            StatusItem(
                icon = Icons.Default.Security,
                label = "Status",
                value = riskStatus.level.name.lowercase().replaceFirstChar { it.uppercase() },
                color = themeColor,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun StatusItem(
    icon: ImageVector, 
    label: String, 
    value: String, 
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1
        )
    }
}

@Composable
fun EmptyStateCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF4CAF50).copy(alpha = 0.08f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = Color(0xFF4CAF50),
                modifier = Modifier.size(64.dp)
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = "All Clear!",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF4CAF50)
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "No security threats detected. Your device is protected.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
    }
}

@Composable
fun ImprovedSignalCard(signal: SecuritySignal) {
    val severityData = getSeverityData(signal.severity)
    val signalInfo = getSignalInfo(signal)
    val timeAgo = getTimeAgo(signal.timestamp)
    val context = androidx.compose.ui.platform.LocalContext.current
    
    // Get app icon if package name is available
    val appIcon = remember(signal.metadata["package"]) {
        signal.metadata["package"]?.let { packageName ->
            try {
                context.packageManager.getApplicationIcon(packageName)
            } catch (e: Exception) {
                null
            }
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = severityData.color.copy(alpha = 0.08f)
        ),
        border = BorderStroke(1.dp, severityData.color.copy(alpha = 0.2f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Icon - Show app icon if available, otherwise show threat icon
                Surface(
                    modifier = Modifier.size(44.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = if (appIcon != null) Color.Transparent else severityData.color.copy(alpha = 0.15f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        if (appIcon != null) {
                            Image(
                                bitmap = appIcon.toBitmap().asImageBitmap(),
                                contentDescription = "App Icon",
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(12.dp))
                            )
                        } else {
                            Icon(
                                imageVector = signalInfo.icon,
                                contentDescription = null,
                                tint = severityData.color,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
                
                Spacer(Modifier.width(12.dp))
                
                // Title and time
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = signalInfo.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = timeAgo,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
                
                // Severity badge
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = severityData.color.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = severityData.label,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = severityData.color,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }
            
            Spacer(Modifier.height(12.dp))
            
            // Description
            Text(
                text = signalInfo.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                lineHeight = 20.sp
            )
            
            // Additional info if available
            signal.metadata["package"]?.let { packageName ->
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Apps,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = "App: ${packageName.substringAfterLast('.')}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            // Recommendation
            if (signalInfo.recommendation.isNotEmpty()) {
                Spacer(Modifier.height(12.dp))
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            Icons.Default.Lightbulb,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = signalInfo.recommendation,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
            
            // Action Buttons
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // View App Info button (if package name available)
                signal.metadata["package"]?.let { packageName ->
                    OutlinedButton(
                        onClick = {
                            try {
                                val intent = android.content.Intent(
                                    android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                    android.net.Uri.parse("package:$packageName")
                                )
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                // Handle error silently
                            }
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = severityData.color
                        ),
                        border = BorderStroke(1.dp, severityData.color.copy(alpha = 0.5f))
                    ) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            "App Info",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
                
                // View in Settings button
                OutlinedButton(
                    onClick = {
                        try {
                            val intent = when (signal.type) {
                                SignalType.SUSPICIOUS_PERMISSION_COMBO -> 
                                    android.content.Intent(android.provider.Settings.ACTION_APPLICATION_SETTINGS)
                                SignalType.RESOURCE_SPIKE -> 
                                    android.content.Intent(android.provider.Settings.ACTION_BATTERY_SAVER_SETTINGS)
                                SignalType.NETWORK_ANOMALY -> 
                                    android.content.Intent(android.provider.Settings.ACTION_DATA_USAGE_SETTINGS)
                                SignalType.UNAUTHORIZED_BACKGROUND_START -> 
                                    android.content.Intent(android.provider.Settings.ACTION_APPLICATION_SETTINGS)
                                else -> 
                                    android.content.Intent(android.provider.Settings.ACTION_SECURITY_SETTINGS)
                            }
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            // Fallback to general settings
                            try {
                                context.startActivity(android.content.Intent(android.provider.Settings.ACTION_SETTINGS))
                            } catch (ex: Exception) {
                                // Handle error silently
                            }
                        }
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        Icons.Default.Settings,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        "Settings",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

// Helper data classes and functions
data class SeverityData(
    val color: Color,
    val label: String
)

data class SignalInfo(
    val icon: ImageVector,
    val title: String,
    val description: String,
    val recommendation: String
)

fun getSeverityData(severity: SignalSeverity): SeverityData {
    return when (severity) {
        SignalSeverity.CRITICAL -> SeverityData(
            color = Color(0xFFB00020),
            label = "Critical"
        )
        SignalSeverity.HIGH -> SeverityData(
            color = Color(0xFFFF5722),
            label = "High"
        )
        SignalSeverity.MEDIUM -> SeverityData(
            color = Color(0xFFFFC107),
            label = "Medium"
        )
        SignalSeverity.LOW -> SeverityData(
            color = Color(0xFF03DAC5),
            label = "Low"
        )
    }
}

fun getSignalInfo(signal: SecuritySignal): SignalInfo {
    return when (signal.type) {
        SignalType.MASS_FILE_MODIFICATION -> SignalInfo(
            icon = Icons.Default.FolderDelete,
            title = "Mass File Changes Detected",
            description = "Multiple files were modified rapidly. This could indicate ransomware activity encrypting your files.",
            recommendation = "Review recent file changes and consider backing up important data immediately."
        )
        SignalType.HONEYFILE_TOUCHED -> SignalInfo(
            icon = Icons.Default.Warning,
            title = "Decoy File Accessed",
            description = "A protected decoy file was accessed or modified. This is a strong indicator of unauthorized file scanning.",
            recommendation = "Investigate which app accessed this file. Consider removing suspicious apps."
        )
        SignalType.SUSPICIOUS_PERMISSION_COMBO -> SignalInfo(
            icon = Icons.Default.AdminPanelSettings,
            title = "Risky Permissions Detected",
            description = signal.description,
            recommendation = "Review app permissions in Settings. Consider removing unnecessary permissions."
        )
        SignalType.RESOURCE_SPIKE -> SignalInfo(
            icon = Icons.Default.Speed,
            title = "Unusual Resource Usage",
            description = "An app is consuming excessive CPU or memory. This could indicate cryptocurrency mining or other malicious activity.",
            recommendation = "Check battery usage in Settings to identify the app causing high resource consumption."
        )
        SignalType.NETWORK_ANOMALY -> SignalInfo(
            icon = Icons.Default.CloudOff,
            title = "Suspicious Network Activity",
            description = "Unusual network connections detected. Data may be transmitted to unknown servers.",
            recommendation = "Monitor your data usage and consider disabling network access for suspicious apps."
        )
        SignalType.SUSPICIOUS_EXTENSION -> SignalInfo(
            icon = Icons.Default.InsertDriveFile,
            title = "Suspicious File Extension",
            description = "Files with unusual extensions commonly used by ransomware were detected.",
            recommendation = "Do not open files with unknown extensions. Scan your device with antivirus software."
        )
        SignalType.UNAUTHORIZED_BACKGROUND_START -> SignalInfo(
            icon = Icons.Default.PlayArrow,
            title = "Unauthorized Background Activity",
            description = "An app started running in the background without your permission.",
            recommendation = "Review background app activity in Settings and restrict unnecessary background processes."
        )
    }
}

fun getTimeAgo(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    
    val seconds = TimeUnit.MILLISECONDS.toSeconds(diff)
    val minutes = TimeUnit.MILLISECONDS.toMinutes(diff)
    val hours = TimeUnit.MILLISECONDS.toHours(diff)
    val days = TimeUnit.MILLISECONDS.toDays(diff)
    
    return when {
        seconds < 60 -> "Just now"
        minutes < 60 -> "$minutes ${if (minutes == 1L) "minute" else "minutes"} ago"
        hours < 24 -> "$hours ${if (hours == 1L) "hour" else "hours"} ago"
        days < 7 -> "$days ${if (days == 1L) "day" else "days"} ago"
        else -> "${days / 7} ${if (days / 7 == 1L) "week" else "weeks"} ago"
    }
}
