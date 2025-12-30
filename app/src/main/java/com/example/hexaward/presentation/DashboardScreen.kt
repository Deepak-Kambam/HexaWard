package com.example.hexaward.presentation

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.GenericShape
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
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.core.graphics.drawable.toBitmap
import com.example.hexaward.domain.model.RiskStatus
import com.example.hexaward.domain.model.SecuritySignal
import com.example.hexaward.domain.model.SignalSeverity
import com.example.hexaward.domain.model.SignalType
import java.util.concurrent.TimeUnit
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: MainViewModel,
    onOpenDrawer: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val riskStatus by viewModel.riskStatus.collectAsState()
    val density = LocalDensity.current
    
    val industrialCardShape = remember(density) {
        GenericShape { size, _ ->
            val cut = with(density) { 24.dp.toPx() }
            moveTo(0f, 0f)
            lineTo(size.width - cut, 0f)
            lineTo(size.width, cut)
            lineTo(size.width, size.height)
            lineTo(cut, size.height)
            lineTo(0f, size.height - cut)
            close()
        }
    }
    
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
                        Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = animatedThemeColor)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "HEXAWARD", 
                            fontWeight = FontWeight.Black, 
                            letterSpacing = 4.sp,
                            color = animatedThemeColor
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onOpenDrawer) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu", tint = animatedThemeColor)
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
                .padding(padding)
        ) {
            DashboardContent(riskStatus, currentAnimatedValue, animatedThemeColor, industrialCardShape, context = androidx.compose.ui.platform.LocalContext.current)
        }
    }
}

enum class ThreatCategory(val displayName: String, val icon: ImageVector, val color: Color) {
    CRITICAL("Critical Threats", Icons.Default.Warning, Color(0xFFF44336)),
    FILE_THREATS("File Activity", Icons.Default.FolderDelete, Color(0xFFFF9800)),
    NETWORK_THREATS("Network Activity", Icons.Default.WifiOff, Color(0xFF2196F3)),
    PERMISSION_THREATS("Permission Issues", Icons.Default.GppBad, Color(0xFF9C27B0)),
    BACKGROUND_THREATS("Background Activity", Icons.Default.Terminal, Color(0xFF607D8B)),
    RESOURCE_THREATS("Resource Abuse", Icons.Default.Speed, Color(0xFFFF5722)),
    ALL("All Threats", Icons.AutoMirrored.Filled.List, Color(0xFF4CAF50))
}

enum class AppCategory(val displayName: String, val icon: ImageVector, val color: Color, val keywords: List<String>) {
    PAYMENT("Payment", Icons.Default.Payment, Color(0xFF4CAF50), listOf("pay", "bank", "wallet", "money", "finance", "upi", "paytm", "phonepe", "gpay")),
    SOCIAL_MEDIA("Social Media", Icons.Default.Groups, Color(0xFF2196F3), listOf("social", "chat", "messenger", "whatsapp", "telegram", "instagram", "facebook", "twitter", "snapchat")),
    ENTERTAINMENT("Entertainment", Icons.Default.Movie, Color(0xFF9C27B0), listOf("video", "music", "stream", "youtube", "netflix", "spotify", "game", "play")),
    SHOPPING("Shopping", Icons.Default.ShoppingCart, Color(0xFFFF9800), listOf("shop", "store", "amazon", "flipkart", "cart", "buy", "mall", "ecommerce")),
    PRODUCTIVITY("Productivity", Icons.Default.Work, Color(0xFF607D8B), listOf("office", "document", "note", "calendar", "email", "drive", "docs", "sheets")),
    COMMUNICATION("Communication", Icons.Default.Phone, Color(0xFF00BCD4), listOf("call", "sms", "dialer", "contact", "phone", "message")),
    SYSTEM("System", Icons.Default.Settings, Color(0xFF795548), listOf("android", "system", "google", "launcher", "service")),
    ALL_APPS("All Apps", Icons.AutoMirrored.Filled.List, Color(0xFF4CAF50), emptyList())
}

fun getAppCategory(packageName: String, context: android.content.Context): AppCategory {
    try {
        val appInfo = context.packageManager.getApplicationInfo(packageName, 0)
        val appName = context.packageManager.getApplicationLabel(appInfo).toString().lowercase()
        val pkgName = packageName.lowercase()
        
        for (category in AppCategory.entries.filter { it != AppCategory.ALL_APPS }) {
            if (category.keywords.any { keyword -> 
                appName.contains(keyword) || pkgName.contains(keyword)
            }) {
                return category
            }
        }
    } catch (e: Exception) {
        // Package not found or error
    }
    return AppCategory.ALL_APPS
}

fun SecuritySignal.getCategory(): ThreatCategory {
    return when {
        severity == SignalSeverity.CRITICAL -> ThreatCategory.CRITICAL
        type == SignalType.MASS_FILE_MODIFICATION || type == SignalType.SUSPICIOUS_EXTENSION || type == SignalType.HONEYFILE_TOUCHED -> ThreatCategory.FILE_THREATS
        type == SignalType.NETWORK_ANOMALY -> ThreatCategory.NETWORK_THREATS
        type == SignalType.SUSPICIOUS_PERMISSION_COMBO -> ThreatCategory.PERMISSION_THREATS
        type == SignalType.UNAUTHORIZED_BACKGROUND_START -> ThreatCategory.BACKGROUND_THREATS
        type == SignalType.RESOURCE_SPIKE -> ThreatCategory.RESOURCE_THREATS
        else -> ThreatCategory.ALL
    }
}

@Composable
fun DashboardContent(
    riskStatus: RiskStatus, 
    animatedScoreValue: Float, 
    themeColor: Color,
    industrialCardShape: androidx.compose.ui.graphics.Shape,
    context: android.content.Context
) {
    val listState = rememberLazyListState()
    var selectedThreatCategory by remember { mutableStateOf(ThreatCategory.ALL) }
    var selectedAppCategory by remember { mutableStateOf(AppCategory.ALL_APPS) }
    
    // Group threats by threat category
    val categorizedThreats = remember(riskStatus.triggers) {
        riskStatus.triggers.groupBy { it.getCategory() }
    }
    
    // Group threats by app category
    val appCategorizedThreats = remember(riskStatus.triggers) {
        riskStatus.triggers.groupBy { signal ->
            signal.metadata["package"]?.let { getAppCategory(it, context) } ?: AppCategory.ALL_APPS
        }
    }
    
    // Get threat count per threat category
    val threatCategoryCount = remember(categorizedThreats) {
        ThreatCategory.entries.associateWith { category ->
            when (category) {
                ThreatCategory.ALL -> riskStatus.triggers.size
                else -> categorizedThreats[category]?.size ?: 0
            }
        }
    }
    
    // Get threat count per app category
    val appCategoryCount = remember(appCategorizedThreats) {
        AppCategory.entries.associateWith { category ->
            when (category) {
                AppCategory.ALL_APPS -> riskStatus.triggers.size
                else -> appCategorizedThreats[category]?.size ?: 0
            }
        }
    }
    
    // Filter threats based on selected categories
    val filteredThreats = remember(riskStatus.triggers, selectedThreatCategory, selectedAppCategory) {
        var filtered = riskStatus.triggers
        
        // Filter by threat category
        if (selectedThreatCategory != ThreatCategory.ALL) {
            filtered = filtered.filter { it.getCategory() == selectedThreatCategory }
        }
        
        // Filter by app category
        if (selectedAppCategory != AppCategory.ALL_APPS) {
            filtered = filtered.filter { signal ->
                signal.metadata["package"]?.let { 
                    getAppCategory(it, context) == selectedAppCategory 
                } ?: false
            }
        }
        
        filtered
    }
    
    val meterAlpha by remember {
        derivedStateOf {
            val firstVisibleIndex = listState.firstVisibleItemIndex
            val firstVisibleOffset = listState.firstVisibleItemScrollOffset
            
            when {
                firstVisibleIndex > 0 -> 0f
                else -> {
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
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 8.dp, bottom = 32.dp)
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
        
        item {
            StatusSummaryCard(riskStatus, themeColor, industrialCardShape)
        }
        
        // Category Filter Section
        if (riskStatus.triggers.isNotEmpty()) {
            item {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(4.dp, 16.dp)
                                .background(themeColor)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "THREAT VAULT",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 2.sp,
                            color = themeColor
                        )
                        Spacer(Modifier.weight(1f))
                        Text(
                            text = "${filteredThreats.size} ITEMS",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = themeColor.copy(alpha = 0.7f)
                        )
                    }
                    
                    Spacer(Modifier.height(12.dp))
                    
                    // Threat Category chips
                    Text(
                        text = "THREAT TYPE",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp)
                    ) {
                        items(ThreatCategory.entries.size) { index ->
                            val category = ThreatCategory.entries[index]
                            val count = threatCategoryCount[category] ?: 0
                            if (count > 0 || category == ThreatCategory.ALL) {
                                CategoryChip(
                                    category = category,
                                    count = count,
                                    isSelected = selectedThreatCategory == category,
                                    onClick = { selectedThreatCategory = category },
                                    industrialCardShape = industrialCardShape
                                )
                            }
                        }
                    }
                    
                    Spacer(Modifier.height(12.dp))
                    
                    // App Category chips
                    Text(
                        text = "APP CATEGORY",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp)
                    ) {
                        items(AppCategory.entries.size) { index ->
                            val category = AppCategory.entries[index]
                            val count = appCategoryCount[category] ?: 0
                            if (count > 0 || category == AppCategory.ALL_APPS) {
                                AppCategoryChip(
                                    category = category,
                                    count = count,
                                    isSelected = selectedAppCategory == category,
                                    onClick = { selectedAppCategory = category },
                                    industrialCardShape = industrialCardShape
                                )
                            }
                        }
                    }
                }
            }
        } else {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(4.dp, 16.dp)
                            .background(themeColor)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "THREAT VAULT",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp,
                        color = themeColor
                    )
                }
            }
        }
        
        if (filteredThreats.isEmpty()) {
            item {
                if (selectedThreatCategory == ThreatCategory.ALL && selectedAppCategory == AppCategory.ALL_APPS) {
                    EmptyStateCard(themeColor, industrialCardShape)
                } else {
                    EmptyFilterCard(selectedThreatCategory, selectedAppCategory, industrialCardShape)
                }
            }
        } else {
            items(filteredThreats.reversed()) { alert ->
                ImprovedSignalCard(signal = alert, themeColor = themeColor, industrialCardShape = industrialCardShape, context = context)
            }
        }
    }
}

@Composable
fun RiskGauge(status: RiskStatus, animatedValue: Float, color: Color) {
    val displayScore = animatedValue.toInt()
    
    val statusMessage = when {
        displayScore < 25 -> "PROTECTION OPTIMAL"
        displayScore < 50 -> "THREATS DETECTED"
        displayScore < 75 -> "CAUTION REQUIRED"
        else -> "CRITICAL BREACH"
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(260.dp)) {
            Canvas(modifier = Modifier.size(240.dp)) {
                drawArc(
                    color = color.copy(alpha = 0.05f),
                    startAngle = 0f,
                    sweepAngle = 360f,
                    useCenter = false,
                    style = Stroke(width = 2.dp.toPx())
                )
            }

            if (displayScore > 70) {
                val infiniteTransition = rememberInfiniteTransition(label = "pulse")
                val pulseScale by infiniteTransition.animateFloat(
                    initialValue = 1f,
                    targetValue = 1.1f,
                    animationSpec = infiniteRepeatable(tween(1000), RepeatMode.Reverse),
                    label = "pulse"
                )
                Box(
                    modifier = Modifier
                        .size(200.dp)
                        .clip(CircleShape)
                        .border(1.dp, color.copy(alpha = 0.2f * pulseScale), CircleShape)
                )
            }

            Canvas(modifier = Modifier.size(220.dp)) {
                drawArc(
                    color = color.copy(alpha = 0.1f),
                    startAngle = 135f,
                    sweepAngle = 270f,
                    useCenter = false,
                    style = Stroke(width = 20.dp.toPx(), cap = StrokeCap.Butt)
                )
                drawArc(
                    color = color,
                    startAngle = 135f,
                    sweepAngle = (animatedValue / 100f) * 270f,
                    useCenter = false,
                    style = Stroke(width = 20.dp.toPx(), cap = StrokeCap.Butt)
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "$displayScore",
                    fontSize = 64.sp,
                    fontWeight = FontWeight.Black,
                    color = color,
                    letterSpacing = (-2).sp
                )
                Text(
                    text = status.level.name,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Black,
                    color = color,
                    letterSpacing = 2.sp
                )
            }
        }
        
        Spacer(Modifier.height(12.dp))
        Box(
            modifier = Modifier
                .border(1.dp, color.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
                .padding(horizontal = 12.dp, vertical = 4.dp)
        ) {
            Text(
                text = statusMessage,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = color,
                letterSpacing = 1.sp
            )
        }
    }
}

@Composable
fun StatusSummaryCard(riskStatus: RiskStatus, themeColor: Color, industrialCardShape: androidx.compose.ui.graphics.Shape) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(industrialCardShape)
            .border(1.dp, themeColor.copy(alpha = 0.2f), industrialCardShape),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.03f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            StatusItem(
                icon = Icons.Default.Security,
                label = "SHIELD",
                value = if (riskStatus.score < 50) "ACTIVE" else "WARN",
                color = themeColor,
                modifier = Modifier.weight(1f)
            )
            
            VerticalDivider(
                modifier = Modifier.height(40.dp),
                color = themeColor.copy(alpha = 0.1f)
            )
            
            StatusItem(
                icon = Icons.Default.Memory,
                label = "SYSTEM",
                value = "${riskStatus.score}%",
                color = themeColor,
                modifier = Modifier.weight(1f)
            )
            
            VerticalDivider(
                modifier = Modifier.height(40.dp),
                color = themeColor.copy(alpha = 0.1f)
            )
            
            StatusItem(
                icon = Icons.Default.Analytics,
                label = "ENGINE",
                value = riskStatus.level.name.take(3),
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
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color.copy(alpha = 0.6f),
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Black,
            color = color,
            maxLines = 1
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = Color.Gray,
            letterSpacing = 1.sp
        )
    }
}

@Composable
fun EmptyStateCard(themeColor: Color, industrialCardShape: androidx.compose.ui.graphics.Shape) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(industrialCardShape)
            .border(1.dp, Color(0xFF4CAF50).copy(alpha = 0.2f), industrialCardShape),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF4CAF50).copy(alpha = 0.03f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.VerifiedUser,
                contentDescription = null,
                tint = Color(0xFF4CAF50),
                modifier = Modifier.size(48.dp)
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = "SYSTEM SECURE",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp,
                color = Color(0xFF4CAF50)
            )
            Text(
                text = "NO ANOMALIES DETECTED IN LAST SCAN",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = Color.Gray,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@Composable
fun EmptyCategoryCard(category: ThreatCategory, industrialCardShape: androidx.compose.ui.graphics.Shape) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(industrialCardShape)
            .border(1.dp, category.color.copy(alpha = 0.2f), industrialCardShape),
        colors = CardDefaults.cardColors(
            containerColor = category.color.copy(alpha = 0.03f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = category.icon,
                contentDescription = null,
                tint = category.color,
                modifier = Modifier.size(48.dp)
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = "NO ${category.displayName.uppercase()}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp,
                color = category.color
            )
            Text(
                text = "This category has no detected threats",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = Color.Gray,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@Composable
fun EmptyFilterCard(threatCategory: ThreatCategory, appCategory: AppCategory, industrialCardShape: androidx.compose.ui.graphics.Shape) {
    val displayColor = if (threatCategory != ThreatCategory.ALL) threatCategory.color else appCategory.color
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(industrialCardShape)
            .border(1.dp, displayColor.copy(alpha = 0.2f), industrialCardShape),
        colors = CardDefaults.cardColors(
            containerColor = displayColor.copy(alpha = 0.03f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.FilterAlt,
                contentDescription = null,
                tint = displayColor,
                modifier = Modifier.size(48.dp)
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = "NO MATCHING THREATS",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp,
                color = displayColor
            )
            
            Column(
                modifier = Modifier.padding(top = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (threatCategory != ThreatCategory.ALL) {
                    Text(
                        text = "Type: ${threatCategory.displayName}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray
                    )
                }
                if (appCategory != AppCategory.ALL_APPS) {
                    Text(
                        text = "Category: ${appCategory.displayName}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}

@Composable
fun AppCategoryChip(
    category: AppCategory,
    count: Int,
    isSelected: Boolean,
    onClick: () -> Unit,
    industrialCardShape: androidx.compose.ui.graphics.Shape
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) category.color.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.05f),
        label = "bg_color"
    )
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) category.color else Color.White.copy(alpha = 0.1f),
        label = "border_color"
    )
    val textColor by animateColorAsState(
        targetValue = if (isSelected) category.color else Color.White.copy(alpha = 0.6f),
        label = "text_color"
    )
    
    Surface(
        onClick = onClick,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .border(1.5.dp, borderColor, RoundedCornerShape(12.dp)),
        color = backgroundColor
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = category.icon,
                contentDescription = null,
                tint = if (isSelected) category.color else Color.White.copy(alpha = 0.6f),
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = category.displayName.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
                color = textColor,
                letterSpacing = 1.sp
            )
            if (count > 0) {
                Box(
                    modifier = Modifier
                        .background(if (isSelected) category.color else Color.White.copy(0.2f), CircleShape)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = count.toString(),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Black,
                        color = if (isSelected) Color.White else Color.White.copy(0.8f),
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}

@Composable
fun CategoryChip(
    category: ThreatCategory,
    count: Int,
    isSelected: Boolean,
    onClick: () -> Unit,
    industrialCardShape: androidx.compose.ui.graphics.Shape
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) category.color.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.05f),
        label = "bg_color"
    )
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) category.color else Color.White.copy(alpha = 0.1f),
        label = "border_color"
    )
    val textColor by animateColorAsState(
        targetValue = if (isSelected) category.color else Color.White.copy(alpha = 0.6f),
        label = "text_color"
    )
    
    Surface(
        onClick = onClick,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .border(1.5.dp, borderColor, RoundedCornerShape(12.dp)),
        color = backgroundColor
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = category.icon,
                contentDescription = null,
                tint = if (isSelected) category.color else Color.White.copy(alpha = 0.6f),
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = if (category == ThreatCategory.ALL) "ALL" else category.displayName.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
                color = textColor,
                letterSpacing = 1.sp
            )
            if (count > 0) {
                Box(
                    modifier = Modifier
                        .background(if (isSelected) category.color else Color.White.copy(0.2f), CircleShape)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = count.toString(),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Black,
                        color = if (isSelected) Color.White else Color.White.copy(0.8f),
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}

@Composable
fun ImprovedSignalCard(signal: SecuritySignal, themeColor: Color, industrialCardShape: androidx.compose.ui.graphics.Shape, context: android.content.Context) {
    val severityData = getSeverityData(signal.severity)
    val signalInfo = getSignalInfo(signal)
    val timeAgo = getTimeAgo(signal.timestamp)
    
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
        modifier = Modifier
            .fillMaxWidth()
            .clip(industrialCardShape)
            .border(1.dp, severityData.color.copy(alpha = 0.2f), industrialCardShape),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.02f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier.size(44.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = severityData.color.copy(alpha = 0.1f),
                    border = BorderStroke(1.dp, severityData.color.copy(alpha = 0.2f))
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        if (appIcon != null) {
                            Image(
                                bitmap = appIcon.toBitmap().asImageBitmap(),
                                contentDescription = "App Icon",
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(RoundedCornerShape(4.dp))
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
                
                Spacer(Modifier.width(16.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = signalInfo.title.uppercase(),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = timeAgo.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray
                    )
                }
                
                Box(
                    modifier = Modifier
                        .background(severityData.color.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                        .border(1.dp, severityData.color.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = severityData.label.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Black,
                        color = severityData.color
                    )
                }
            }
            
            Spacer(Modifier.height(16.dp))
            
            Text(
                text = signalInfo.description,
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.7f),
                lineHeight = 18.sp
            )
            
            Spacer(Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { 
                        signal.metadata["package"]?.let { packageName ->
                            try {
                                val intent = android.content.Intent(
                                    android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                    android.net.Uri.parse("package:$packageName")
                                )
                                context.startActivity(intent)
                            } catch (e: Exception) {}
                        }
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(
                        "ANALYSIS",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                }
                
                Button(
                    onClick = { 
                        try {
                            val intent = android.content.Intent(android.provider.Settings.ACTION_SECURITY_SETTINGS)
                            context.startActivity(intent)
                        } catch (e: Exception) {}
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = severityData.color.copy(alpha = 0.8f)),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(
                        "RESPOND",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                }
            }
        }
    }
}

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

fun getSeverityData(severity: com.example.hexaward.domain.model.SignalSeverity): SeverityData {
    return when (severity) {
        com.example.hexaward.domain.model.SignalSeverity.CRITICAL -> SeverityData(
            color = Color(0xFFF44336),
            label = "CRITICAL"
        )
        com.example.hexaward.domain.model.SignalSeverity.HIGH -> SeverityData(
            color = Color(0xFFFF9800),
            label = "HIGH"
        )
        com.example.hexaward.domain.model.SignalSeverity.MEDIUM -> SeverityData(
            color = Color(0xFF2196F3),
            label = "MEDIUM"
        )
        com.example.hexaward.domain.model.SignalSeverity.LOW -> SeverityData(
            color = Color(0xFF4CAF50),
            label = "LOW"
        )
    }
}

fun getSignalInfo(signal: SecuritySignal): SignalInfo {
    return when (signal.type) {
        SignalType.MASS_FILE_MODIFICATION -> SignalInfo(
            icon = Icons.Default.FolderDelete,
            title = "File Mass Mod",
            description = "Rapid modification of multiple system-critical directories detected.",
            recommendation = ""
        )
        SignalType.HONEYFILE_TOUCHED -> SignalInfo(
            icon = Icons.Default.ReportProblem,
            title = "Decoy Trigger",
            description = "Unauthorized access attempt on secure decoy system file.",
            recommendation = ""
        )
        SignalType.SUSPICIOUS_PERMISSION_COMBO -> SignalInfo(
            icon = Icons.Default.GppBad,
            title = "Perm Conflict",
            description = signal.description,
            recommendation = ""
        )
        SignalType.RESOURCE_SPIKE -> SignalInfo(
            icon = Icons.Default.Speed,
            title = "Power Spike",
            description = "Abnormal CPU/Memory consumption detected by background process.",
            recommendation = ""
        )
        SignalType.NETWORK_ANOMALY -> SignalInfo(
            icon = Icons.Default.WifiOff,
            title = "Net Anomaly",
            description = "Suspicious outbound connection to unverified remote endpoint.",
            recommendation = ""
        )
        SignalType.SUSPICIOUS_EXTENSION -> SignalInfo(
            icon = Icons.AutoMirrored.Filled.InsertDriveFile,
            title = "Ext Violation",
            description = "File system contains extensions associated with known encryption malware.",
            recommendation = ""
        )
        SignalType.UNAUTHORIZED_BACKGROUND_START -> SignalInfo(
            icon = Icons.Default.Terminal,
            title = "Exec Violation",
            description = "Unauthorized background process execution attempt detected.",
            recommendation = ""
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
        seconds < 60 -> "NOW"
        minutes < 60 -> "${minutes}M AGO"
        hours < 24 -> "${hours}H AGO"
        days < 7 -> "${days}D AGO"
        else -> "${days / 7}W AGO"
    }
}
