package com.example.hexaward.presentation

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit
) {
    val settings by viewModel.settings.collectAsState()
    val riskStatus by viewModel.riskStatus.collectAsState()
    val updateInfo by viewModel.updateInfo.collectAsState()
    var showUpdateDialog by remember { mutableStateOf(false) }
    
    // Debug: Log update info state
    LaunchedEffect(updateInfo) {
        android.util.Log.d("SettingsScreen", "Update info: $updateInfo")
        android.util.Log.d("SettingsScreen", "Is available: ${updateInfo?.isUpdateAvailable}")
    }

    // Help Dialog State
    var showHelpInfo by remember { mutableStateOf<HelpContent?>(null) }

    val themeColor = when {
        riskStatus.score < 25 -> Color(0xFF4CAF50)
        riskStatus.score < 50 -> Color(0xFF2196F3)
        riskStatus.score < 75 -> Color(0xFFFF9800)
        else -> Color(0xFFF44336)
    }

    val animatedThemeColor by animateColorAsState(
        targetValue = themeColor,
        animationSpec = tween(1000),
        label = "settings_theme_color"
    )

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        "SECURITY CONFIG", 
                        fontWeight = FontWeight.ExtraBold, 
                        letterSpacing = 2.sp,
                        color = animatedThemeColor
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack, 
                            contentDescription = "Back",
                            tint = animatedThemeColor
                        )
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
                            MaterialTheme.colorScheme.surface,
                            MaterialTheme.colorScheme.surface
                        )
                    )
                )
                .padding(padding)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
                contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp)
            ) {
                // Update notification card
                if (updateInfo != null && updateInfo!!.isUpdateAvailable) {
                    item {
                        UpdateNotificationCard(
                            updateInfo = updateInfo!!,
                            onClick = { showUpdateDialog = true }
                        )
                    }
                }
                
                item {
                    SettingsSectionTitle("Shield Protection", animatedThemeColor)
                    
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        SettingsToggleItem(
                            title = "Decoy Shield",
                            description = "Places smart traps to catch hackers.",
                            helpTitle = "What is Decoy Shield?",
                            helpDescription = "This feature sets 'bait' with fake files. If a virus tries to attack your phone, it hits these traps first, allowing HexaWard to kill the threat before your real photos or documents are ever touched.",
                            icon = Icons.Default.Lock,
                            checked = settings.honeyfileEnabled,
                            onCheckedChange = { viewModel.toggleHoneyfile(it) },
                            onLongPress = { title, desc, icon, color -> showHelpInfo = HelpContent(title, desc, icon, color) },
                            accentColor = Color(0xFF673AB7)
                        )
                        SettingsToggleItem(
                            title = "File Guard",
                            description = "Scans for suspicious file activity.",
                            helpTitle = "What is File Guard?",
                            helpDescription = "Think of this as a 24/7 guard for your folders. It watches for apps trying to change many files at once—like ransomware trying to lock you out. We block it the moment it looks suspicious.",
                            icon = Icons.Default.Build,
                            checked = settings.fileActivityEnabled,
                            onCheckedChange = { viewModel.toggleFileActivity(it) },
                            onLongPress = { title, desc, icon, color -> showHelpInfo = HelpContent(title, desc, icon, color) },
                            accentColor = Color(0xFF2196F3)
                        )
                        SettingsToggleItem(
                            title = "Traffic Watcher",
                            description = "Alerts you of suspicious connections.",
                            helpTitle = "What is Traffic Watcher?",
                            helpDescription = "This monitors your internet connection. If a hidden app tries to send your private information to a suspicious server in another country, we'll alert you and block the leak immediately.",
                            icon = Icons.Default.Info,
                            checked = settings.networkObserverEnabled,
                            onCheckedChange = { viewModel.toggleNetworkObserver(it) },
                            onLongPress = { title, desc, icon, color -> showHelpInfo = HelpContent(title, desc, icon, color) },
                            accentColor = Color(0xFF00BCD4)
                        )
                    }
                }

                item {
                    SettingsSectionTitle("Shield Strength", animatedThemeColor)
                    SettingsSliderItem(
                        title = "Protection Level",
                        description = "Adjust how strictly we block apps.",
                        helpTitle = "About Protection Levels",
                        helpDescription = "Higher levels make the app more 'suspicious' of everything. 'Balanced' is perfect for daily use, while 'Paranoid' provides maximum security but may occasionally ask for your permission on apps you trust.",
                        icon = Icons.Default.Settings,
                        value = settings.sensitivity,
                        onValueChange = { viewModel.setSensitivity(it) },
                        onLongPress = { title, desc, icon, color -> showHelpInfo = HelpContent(title, desc, icon, color) }
                    )
                }

                item {
                    SettingsSectionTitle("Alert Preferences", animatedThemeColor)
                    SettingsToggleItem(
                        title = "Critical Alerts",
                        description = "Immediate notification for threats.",
                        helpTitle = "Why use Critical Alerts?",
                        helpDescription = "When enabled, HexaWard will bypass your silent mode to warn you if a high-level threat like an active virus or data leak is detected. It ensures you can take action before it's too late.",
                        icon = Icons.Default.Notifications,
                        checked = settings.highPriorityAlerts,
                        onCheckedChange = { viewModel.toggleHighPriorityAlerts(it) },
                        onLongPress = { title, desc, icon, color -> showHelpInfo = HelpContent(title, desc, icon, color) },
                        accentColor = Color(0xFFFF9800)
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedButton(
                        onClick = { viewModel.resetAllModules() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f))
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("RESET SECURITY STACK", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    // Glassmorphism Info Dialog
    if (showHelpInfo != null) {
        FeatureHelpDialog(
            content = showHelpInfo!!,
            onDismiss = { showHelpInfo = null }
        )
    }
    
    // Update Dialog
    if (showUpdateDialog && updateInfo != null) {
        UpdateDialog(
            updateInfo = updateInfo!!,
            onDismiss = { 
                showUpdateDialog = false
                // Don't dismiss update notification - keep it visible in Settings
            }
        )
    }
}

data class HelpContent(val title: String, val description: String, val icon: ImageVector, val color: Color)

@Composable
fun FeatureHelpDialog(
    content: HelpContent,
    onDismiss: () -> Unit
) {
    val themeColor = content.color
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(onClick = onDismiss),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .padding(32.dp)
                    .fillMaxWidth()
                    .clickable(enabled = false) {}, 
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                border = BorderStroke(1.dp, themeColor.copy(alpha = 0.4f))
            ) {
                Box(
                    modifier = Modifier
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    themeColor.copy(alpha = 0.2f),
                                    MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                                )
                            )
                        )
                        .padding(24.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Surface(
                            modifier = Modifier.size(64.dp),
                            shape = RoundedCornerShape(18.dp),
                            color = themeColor.copy(alpha = 0.15f),
                            border = BorderStroke(1.dp, themeColor.copy(alpha = 0.3f))
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(content.icon, null, tint = themeColor, modifier = Modifier.size(36.dp))
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(20.dp))
                        
                        Text(
                            text = content.title,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = themeColor,
                            textAlign = TextAlign.Center
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text(
                            text = content.description,
                            style = MaterialTheme.typography.bodyLarge,
                            lineHeight = 24.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                            textAlign = TextAlign.Center
                        )
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        Button(
                            onClick = onDismiss,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = themeColor),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text("UNDERSTOOD", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun UpdateNotificationCard(
    updateInfo: com.example.hexaward.data.update.UpdateInfo,
    onClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF4CAF50).copy(alpha = 0.1f)
        ),
        border = BorderStroke(2.dp, Color(0xFF4CAF50).copy(alpha = pulseAlpha))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Pulsing red dot
            Box(
                modifier = Modifier.size(12.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFFFF5722).copy(alpha = pulseAlpha))
                )
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFFFF5722))
                )
            }
            
            Spacer(Modifier.width(16.dp))
            
            // Icon
            Surface(
                modifier = Modifier.size(48.dp),
                shape = RoundedCornerShape(14.dp),
                color = Color(0xFF4CAF50).copy(alpha = 0.2f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.SystemUpdate,
                        contentDescription = null,
                        tint = Color(0xFF4CAF50),
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
            
            Spacer(Modifier.width(16.dp))
            
            // Content
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Update Available",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF4CAF50)
                    )
                    Spacer(Modifier.width(8.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFFF5722).copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = "NEW",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFFFF5722),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Version ${updateInfo.latestVersion} is ready to install",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // Arrow
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = Color(0xFF4CAF50),
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
fun SettingsSectionTitle(title: String, color: Color) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = color.copy(alpha = 0.9f),
        modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
    )
}

@Composable
fun SettingsToggleItem(
    title: String,
    description: String,
    helpTitle: String,
    helpDescription: String,
    icon: ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    onLongPress: (String, String, ImageVector, Color) -> Unit,
    accentColor: Color
) {
    val haptic = LocalHapticFeedback.current
    val animatedBgColor by animateColorAsState(
        targetValue = if (checked) accentColor.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        animationSpec = tween(500), label = "bgColor"
    )

    val scale by animateFloatAsState(
        targetValue = if (checked) 1.02f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy), label = "scale"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .clip(RoundedCornerShape(20.dp))
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { onCheckedChange(!checked) },
                    onLongPress = { 
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onLongPress(helpTitle, helpDescription, icon, accentColor) 
                    }
                )
            },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = animatedBgColor),
        border = BorderStroke(
            width = 1.dp,
            color = if (checked) accentColor.copy(alpha = 0.5f) else Color.White.copy(alpha = 0.1f)
        )
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val iconBgColor by animateColorAsState(
                targetValue = if (checked) accentColor.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                label = "iconBg"
            )

            Surface(
                modifier = Modifier.size(44.dp),
                shape = RoundedCornerShape(12.dp),
                color = iconBgColor,
                tonalElevation = 2.dp,
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (checked) accentColor else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title, 
                    fontWeight = FontWeight.ExtraBold, 
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = description, 
                    style = MaterialTheme.typography.bodySmall, 
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
            
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = accentColor,
                    uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                    uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            )
        }
    }
}

@Composable
fun SettingsSliderItem(
    title: String,
    description: String,
    helpTitle: String,
    helpDescription: String,
    icon: ImageVector,
    value: Float,
    onValueChange: (Float) -> Unit,
    onLongPress: (String, String, ImageVector, Color) -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val sensitivityLabel = when {
        value < 0.3f -> "Conservative"
        value < 0.6f -> "Balanced"
        value < 0.85f -> "Aggressive"
        else -> "Paranoid"
    }

    val sensitivityColor = animateColorAsState(
        targetValue = when {
            value < 0.3f -> Color(0xFF4CAF50) // Green
            value < 0.6f -> Color(0xFF2196F3) // Blue
            value < 0.85f -> Color(0xFFFF9800) // Orange
            else -> Color(0xFFF44336) // Red
        },
        animationSpec = tween(500), label = "sliderColor"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .pointerInput(Unit) {
                detectTapGestures(
                    onLongPress = { 
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onLongPress(helpTitle, helpDescription, icon, sensitivityColor.value) 
                    }
                )
            },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = sensitivityColor.value.copy(alpha = 0.1f)),
        border = BorderStroke(1.dp, sensitivityColor.value.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = sensitivityColor.value)
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = title, 
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = description, 
                style = MaterialTheme.typography.bodySmall, 
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Slider(
                value = value,
                onValueChange = onValueChange,
                valueRange = 0f..1f,
                colors = SliderDefaults.colors(
                    thumbColor = sensitivityColor.value,
                    activeTrackColor = sensitivityColor.value,
                    inactiveTrackColor = sensitivityColor.value.copy(alpha = 0.2f)
                )
            )
            
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                AnimatedContent(
                    targetState = sensitivityLabel,
                    transitionSpec = {
                        (slideInVertically { it } + fadeIn()).togetherWith(slideOutVertically { -it } + fadeOut())
                    }, label = "labelTransition"
                ) { label ->
                    Text(
                        text = label.uppercase(),
                        style = MaterialTheme.typography.labelLarge,
                        color = sensitivityColor.value,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                }
            }
        }
    }
}
