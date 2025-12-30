package com.example.hexaward.presentation

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.GenericShape
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

enum class SettingsSubScreen {
    MAIN, PERSONALIZATION, ABOUT
}

data class HelpContent(
    val title: String, 
    val description: String, 
    val icon: ImageVector, 
    val color: Color
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: MainViewModel,
    onOpenDrawer: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val settings by viewModel.settings.collectAsState()
    val riskStatus by viewModel.riskStatus.collectAsState()
    val updateInfo by viewModel.updateInfo.collectAsState()
    val density = LocalDensity.current
    val haptic = LocalHapticFeedback.current
    
    var currentScreen by remember { mutableStateOf(SettingsSubScreen.MAIN) }
    var showUpdateDialog by remember { mutableStateOf(false) }
    
    val industrialCardShape = remember(density) {
        GenericShape { size, _ ->
            val cut = with(density) { 20.dp.toPx() }
            moveTo(0f, 0f)
            lineTo(size.width - cut, 0f)
            lineTo(size.width, cut)
            lineTo(size.width, size.height)
            lineTo(cut, size.height)
            lineTo(0f, size.height - cut)
            close()
        }
    }

    BackHandler(enabled = currentScreen != SettingsSubScreen.MAIN) {
        currentScreen = SettingsSubScreen.MAIN
    }

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

    AnimatedContent(
        targetState = currentScreen,
        transitionSpec = {
            if (targetState != SettingsSubScreen.MAIN) {
                (slideInHorizontally { it } + fadeIn()).togetherWith(slideOutHorizontally { -it } + fadeOut())
            } else {
                (slideInHorizontally { -it } + fadeIn()).togetherWith(slideOutHorizontally { it } + fadeOut())
            }
        },
        label = "settings_nav"
    ) { screen ->
        when (screen) {
            SettingsSubScreen.MAIN -> MainSettingsContent(
                onOpenDrawer = onOpenDrawer,
                onNavigateBack = onNavigateBack,
                onNavigateToPersonalization = { currentScreen = SettingsSubScreen.PERSONALIZATION },
                onNavigateToAbout = { currentScreen = SettingsSubScreen.ABOUT },
                animatedThemeColor = animatedThemeColor,
                industrialCardShape = industrialCardShape,
                updateInfo = updateInfo,
                settings = settings,
                viewModel = viewModel,
                onShowUpdate = { showUpdateDialog = true },
                onShowHelp = { showHelpInfo = it }
            )
            SettingsSubScreen.PERSONALIZATION -> PersonalizationScreen(
                viewModel = viewModel,
                settings = settings,
                onBack = { currentScreen = SettingsSubScreen.MAIN },
                themeColor = animatedThemeColor,
                industrialCardShape = industrialCardShape,
                onShowHelp = { showHelpInfo = it }
            )
            SettingsSubScreen.ABOUT -> AboutScreen(
                onBack = { currentScreen = SettingsSubScreen.MAIN },
                themeColor = animatedThemeColor,
                industrialCardShape = industrialCardShape
            )
        }
    }

    showHelpInfo?.let { content ->
        FeatureHelpDialog(
            content = content,
            industrialCardShape = industrialCardShape,
            onDismiss = { showHelpInfo = null }
        )
    }
    
    if (showUpdateDialog && updateInfo != null) {
        UpdateDialog(
            updateInfo = updateInfo!!,
            onDismiss = { showUpdateDialog = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainSettingsContent(
    onOpenDrawer: () -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateToPersonalization: () -> Unit,
    onNavigateToAbout: () -> Unit,
    animatedThemeColor: Color,
    industrialCardShape: androidx.compose.ui.graphics.Shape,
    updateInfo: com.example.hexaward.data.update.UpdateInfo?,
    settings: SecuritySettings,
    viewModel: MainViewModel,
    onShowUpdate: () -> Unit,
    onShowHelp: (HelpContent) -> Unit
) {
    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("SECURITY CONFIG", fontWeight = FontWeight.Black, letterSpacing = 4.sp, color = animatedThemeColor) },
                navigationIcon = {
                    IconButton(onClick = onOpenDrawer) { Icon(Icons.Default.Menu, "Menu", tint = animatedThemeColor) }
                },
                actions = {
                    IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = animatedThemeColor) }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp)
        ) {
            if (updateInfo != null && updateInfo.isUpdateAvailable) {
                item {
                    UpdateNotificationCard(updateInfo, industrialCardShape, onClick = onShowUpdate)
                }
            }
            
            item {
                SettingsSectionTitle("SHIELD PROTECTION", animatedThemeColor)
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    SettingsToggleItem(
                        title = "DECOY SHIELD",
                        description = "SMART TRAPS FOR ANOMALY DETECTION.",
                        icon = Icons.Default.Lock,
                        checked = settings.honeyfileEnabled,
                        industrialCardShape = industrialCardShape,
                        onCheckedChange = { viewModel.toggleHoneyfile(it) },
                        onLongPress = { t, d, i, c -> onShowHelp(HelpContent(t, d, i, c)) },
                        accentColor = Color(0xFF673AB7)
                    )
                    SettingsToggleItem(
                        title = "FILE GUARD",
                        description = "RAPID FILE SYSTEM MONITORING.",
                        icon = Icons.Default.Build,
                        checked = settings.fileActivityEnabled,
                        industrialCardShape = industrialCardShape,
                        onCheckedChange = { viewModel.toggleFileActivity(it) },
                        onLongPress = { t, d, i, c -> onShowHelp(HelpContent(t, d, i, c)) },
                        accentColor = Color(0xFF2196F3)
                    )
                    SettingsToggleItem(
                        title = "TRAFFIC WATCHER",
                        description = "ALERTS OF SUSPICIOUS CONNECTIONS.",
                        icon = Icons.Default.WifiLock,
                        checked = settings.networkObserverEnabled,
                        industrialCardShape = industrialCardShape,
                        onCheckedChange = { viewModel.toggleNetworkObserver(it) },
                        onLongPress = { t, d, i, c -> onShowHelp(HelpContent(t, d, i, c)) },
                        accentColor = Color(0xFF00BCD4)
                    )
                }
            }

            item {
                SettingsSectionTitle("SHIELD STRENGTH", animatedThemeColor)
                SettingsSliderItem(
                    title = "DETECTION INTENSITY",
                    description = "ADJUST SYSTEM SENSITIVITY BIAS.",
                    helpTitle = "SENSITIVITY BIAS",
                    helpDescription = "SCALES THE RISK ENGINE'S TOLERANCE FOR BEHAVIORAL ANOMALIES.",
                    icon = Icons.Default.Settings,
                    value = settings.sensitivity,
                    industrialCardShape = industrialCardShape,
                    onValueChange = { viewModel.setSensitivity(it) },
                    onLongPress = { t, d, i, c -> onShowHelp(HelpContent(t, d, i, c)) }
                )
            }

            item {
                SettingsSectionTitle("PERSONALIZATION", animatedThemeColor)
                Card(
                    modifier = Modifier.fillMaxWidth().clip(industrialCardShape).border(1.dp, Color.White.copy(0.1f), industrialCardShape).clickable { onNavigateToPersonalization() },
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(0.02f))
                ) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Palette, null, tint = Color.Gray)
                        Spacer(Modifier.width(16.dp))
                        Text("INTERFACE & HAPTICS", fontWeight = FontWeight.Black, color = Color.White, letterSpacing = 1.sp)
                        Spacer(Modifier.weight(1f))
                        Icon(Icons.Default.ChevronRight, null, tint = Color.Gray)
                    }
                }
            }

            item {
                SettingsSectionTitle("SYSTEM INFO", animatedThemeColor)
                Card(
                    modifier = Modifier.fillMaxWidth().clip(industrialCardShape).border(1.dp, Color.White.copy(0.1f), industrialCardShape).clickable { onNavigateToAbout() },
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(0.02f))
                ) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Info, null, tint = Color.Gray)
                        Spacer(Modifier.width(16.dp))
                        Text("ABOUT TERMINAL", fontWeight = FontWeight.Black, color = Color.White, letterSpacing = 1.sp)
                        Spacer(Modifier.weight(1f))
                        Icon(Icons.Default.ChevronRight, null, tint = Color.Gray)
                    }
                }
            }

            item {
                Button(
                    onClick = { viewModel.resetAllModules() },
                    modifier = Modifier.fillMaxWidth().height(56.dp).clip(industrialCardShape),
                    shape = industrialCardShape,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.8f))
                ) {
                    Text("RESET SECURITY STACK", fontWeight = FontWeight.Black, letterSpacing = 2.sp)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonalizationScreen(
    viewModel: MainViewModel,
    settings: SecuritySettings,
    onBack: () -> Unit, 
    themeColor: Color, 
    industrialCardShape: androidx.compose.ui.graphics.Shape,
    onShowHelp: (HelpContent) -> Unit
) {
    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("PERSONALIZATION", fontWeight = FontWeight.Black, letterSpacing = 4.sp, color = themeColor) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = themeColor) } },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { padding ->
        Column(Modifier.padding(padding).padding(horizontal = 20.dp).fillMaxSize(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            SettingsSectionTitle("INTERFACE PROTOCOLS", themeColor)
            SettingsToggleItem(
                title = "HAPTIC FEEDBACK",
                description = "TACTILE CONFIRMATION ON INTERACTION.",
                icon = Icons.Default.TouchApp,
                checked = settings.hapticsEnabled,
                industrialCardShape = industrialCardShape,
                onCheckedChange = { viewModel.toggleHaptics(it) },
                onLongPress = { t, d, i, c -> onShowHelp(HelpContent(t, d, i, c)) },
                accentColor = Color(0xFFE91E63)
            )
            SettingsToggleItem(
                title = "HUD TECH GRID",
                description = "SYSTEM-WIDE BLUEPRINT OVERLAY.",
                icon = Icons.Default.Grid4x4,
                checked = settings.techGridEnabled,
                industrialCardShape = industrialCardShape,
                onCheckedChange = { viewModel.toggleTechGrid(it) },
                onLongPress = { t, d, i, c -> onShowHelp(HelpContent(t, d, i, c)) },
                accentColor = Color(0xFF9C27B0)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(onBack: () -> Unit, themeColor: Color, industrialCardShape: androidx.compose.ui.graphics.Shape) {
    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("TERMINAL INFO", fontWeight = FontWeight.Black, letterSpacing = 4.sp, color = themeColor) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = themeColor) } },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { padding ->
        Column(Modifier.padding(padding).padding(24.dp).fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier.size(120.dp).clip(industrialCardShape).background(themeColor.copy(0.1f)).border(2.dp, themeColor.copy(0.3f), industrialCardShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.AdminPanelSettings, null, tint = themeColor, modifier = Modifier.size(60.dp))
            }
            Spacer(Modifier.height(24.dp))
            Text("HEXAWARD", fontWeight = FontWeight.Black, color = themeColor, letterSpacing = 4.sp, style = MaterialTheme.typography.headlineMedium)
            Text("PROACTIVE BEHAVIORAL ANALYSIS", style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(32.dp))
            Card(
                modifier = Modifier.fillMaxWidth().clip(industrialCardShape).border(1.dp, Color.White.copy(0.1f), industrialCardShape),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(0.03f))
            ) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    AboutInfoRow("VERSION", "1.0.42-STABLE", themeColor)
                    AboutInfoRow("BUILD", "INDUSTRIAL-X", themeColor)
                    AboutInfoRow("KERNEL", "HEXA-CORE V2", themeColor)
                    AboutInfoRow("SECURITY", "AES-256 GCM", themeColor)
                }
            }
        }
    }
}

@Composable
fun AboutInfoRow(label: String, value: String, themeColor: Color) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, fontWeight = FontWeight.Bold, color = Color.Gray, style = MaterialTheme.typography.labelSmall)
        Text(value, fontWeight = FontWeight.Black, color = themeColor, style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
fun IndustrialSwitch(checked: Boolean, onCheckedChange: (Boolean) -> Unit, accentColor: Color) {
    val density = LocalDensity.current
    val thumbOffset by animateDpAsState(if (checked) 24.dp else 0.dp, label = "thumb")
    
    val switchShape = remember(density) {
        GenericShape { size, _ ->
            val cut = size.height * 0.3f
            moveTo(0f, cut)
            lineTo(cut, 0f)
            lineTo(size.width - cut, 0f)
            lineTo(size.width, cut)
            lineTo(size.width, size.height - cut)
            lineTo(size.width - cut, size.height)
            lineTo(cut, size.height)
            lineTo(0f, size.height - cut)
            close()
        }
    }

    Box(
        modifier = Modifier.width(48.dp).height(24.dp).clip(switchShape).background(if (checked) accentColor.copy(0.2f) else Color.White.copy(0.1f)).border(1.dp, if (checked) accentColor.copy(0.5f) else Color.White.copy(0.2f), switchShape).clickable { onCheckedChange(!checked) },
        contentAlignment = Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier.padding(2.dp).offset(x = thumbOffset).size(20.dp).clip(switchShape).background(if (checked) accentColor else Color.Gray)
        )
    }
}

@Composable
fun SettingsToggleItem(
    title: String,
    description: String,
    icon: ImageVector,
    checked: Boolean,
    industrialCardShape: androidx.compose.ui.graphics.Shape,
    onCheckedChange: (Boolean) -> Unit,
    onLongPress: (String, String, ImageVector, Color) -> Unit,
    accentColor: Color
) {
    val haptic = LocalHapticFeedback.current
    Card(
        modifier = Modifier.fillMaxWidth().clip(industrialCardShape).border(1.dp, if (checked) accentColor.copy(0.4f) else Color.White.copy(0.1f), industrialCardShape).pointerInput(Unit) {
            detectTapGestures(onTap = { onCheckedChange(!checked) }, onLongPress = { 
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onLongPress(title, description, icon, accentColor) 
            })
        },
        colors = CardDefaults.cardColors(containerColor = if (checked) accentColor.copy(0.05f) else Color.White.copy(0.02f))
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = if (checked) accentColor else Color.Gray, modifier = Modifier.size(24.dp))
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Black, color = Color.White, letterSpacing = 1.sp, style = MaterialTheme.typography.labelLarge)
                Text(description, style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontWeight = FontWeight.Bold)
            }
            IndustrialSwitch(checked = checked, onCheckedChange = onCheckedChange, accentColor = accentColor)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsSliderItem(
    title: String,
    description: String,
    helpTitle: String,
    helpDescription: String,
    icon: ImageVector,
    value: Float,
    industrialCardShape: androidx.compose.ui.graphics.Shape,
    onValueChange: (Float) -> Unit,
    onLongPress: (String, String, ImageVector, Color) -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val color = when {
        value < 0.3f -> Color(0xFF4CAF50)
        value < 0.6f -> Color(0xFF2196F3)
        value < 0.85f -> Color(0xFFFF9800)
        else -> Color(0xFFF44336)
    }
    
    val thumbShape = remember {
        GenericShape { size, _ ->
            val cut = size.height * 0.3f
            moveTo(0f, cut)
            lineTo(cut, 0f)
            lineTo(size.width - cut, 0f)
            lineTo(size.width, cut)
            lineTo(size.width, size.height - cut)
            lineTo(size.width - cut, size.height)
            lineTo(cut, size.height)
            lineTo(0f, size.height - cut)
            close()
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth().clip(industrialCardShape).border(1.dp, color.copy(0.3f), industrialCardShape).pointerInput(Unit) {
            detectTapGestures(onLongPress = { 
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onLongPress(helpTitle, helpDescription, icon, color) 
            })
        },
        colors = CardDefaults.cardColors(containerColor = color.copy(0.05f))
    ) {
        Column(Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, null, tint = color)
                Spacer(Modifier.width(12.dp))
                Text(title, fontWeight = FontWeight.Black, color = Color.White, letterSpacing = 1.sp, style = MaterialTheme.typography.labelLarge)
            }
            Spacer(Modifier.height(16.dp))
            
            Slider(
                value = value, 
                onValueChange = onValueChange,
                colors = SliderDefaults.colors(
                    thumbColor = color,
                    activeTrackColor = color,
                    inactiveTrackColor = color.copy(alpha = 0.1f)
                ),
                thumb = {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(thumbShape)
                            .background(color)
                            .border(1.dp, Color.White.copy(0.3f), thumbShape)
                    )
                }
            )
            
            Text(
                if (value < 0.3f) "CONSERVATIVE" else if (value < 0.6f) "BALANCED" else if (value < 0.85f) "AGGRESSIVE" else "PARANOID",
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Black,
                color = color,
                letterSpacing = 2.sp,
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}

@Composable
fun FeatureHelpDialog(content: HelpContent, industrialCardShape: androidx.compose.ui.graphics.Shape, onDismiss: () -> Unit) {
    val themeColor = content.color
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Box(modifier = Modifier.fillMaxSize().clickable(onClick = onDismiss), contentAlignment = Alignment.Center) {
            Card(
                modifier = Modifier.padding(32.dp).fillMaxWidth().clip(industrialCardShape).border(1.dp, themeColor.copy(alpha = 0.4f), industrialCardShape).clickable(enabled = false) {}, 
                colors = CardDefaults.cardColors(containerColor = Color(0xFF121212).copy(alpha = 0.95f))
            ) {
                Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(content.icon, null, tint = themeColor, modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(text = content.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black, color = themeColor, letterSpacing = 2.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(text = content.description, style = MaterialTheme.typography.bodyMedium, lineHeight = 24.sp, color = Color.White.copy(alpha = 0.8f), textAlign = TextAlign.Center, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth().clip(industrialCardShape), colors = ButtonDefaults.buttonColors(containerColor = themeColor), shape = industrialCardShape) {
                        Text("UNDERSTOOD", fontWeight = FontWeight.Black)
                    }
                }
            }
        }
    }
}

@Composable
fun UpdateNotificationCard(updateInfo: com.example.hexaward.data.update.UpdateInfo, industrialCardShape: androidx.compose.ui.graphics.Shape, onClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().clip(industrialCardShape).border(1.dp, Color(0xFF4CAF50).copy(alpha = 0.3f), industrialCardShape).clickable(onClick = onClick), colors = CardDefaults.cardColors(containerColor = Color(0xFF4CAF50).copy(alpha = 0.05f))) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.SystemUpdate, null, tint = Color(0xFF4CAF50))
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text("UPDATE READY", fontWeight = FontWeight.Black, color = Color(0xFF4CAF50), letterSpacing = 1.sp, style = MaterialTheme.typography.labelLarge)
                Text("VER ${updateInfo.latestVersion}", style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontWeight = FontWeight.Bold)
            }
            Icon(Icons.Default.ChevronRight, null, tint = Color(0xFF4CAF50))
        }
    }
}

@Composable
fun SettingsSectionTitle(title: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 8.dp)) {
        Box(Modifier.size(4.dp, 12.dp).background(color))
        Spacer(Modifier.width(8.dp))
        Text(text = title, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Black, color = color, letterSpacing = 2.sp)
    }
}
