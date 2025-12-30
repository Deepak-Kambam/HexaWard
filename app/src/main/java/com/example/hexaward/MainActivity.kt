package com.example.hexaward

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import androidx.core.content.edit
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.hexaward.data.worker.SecurityWorker
import com.example.hexaward.data.worker.UpdateCheckWorker
import com.example.hexaward.presentation.*
import com.example.hexaward.ui.theme.HexaWardTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue

// Custom Industrial Geometric Shape for Drawer
val IndustrialDrawerShape = GenericShape { size, _ ->
    moveTo(0f, 0f)
    lineTo(size.width * 0.85f, 0f)
    lineTo(size.width, size.height * 0.05f)
    lineTo(size.width, size.height * 0.95f)
    lineTo(size.width * 0.85f, size.height)
    lineTo(0f, size.height)
    close()
}

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        splashScreen.setKeepOnScreenCondition { false }
        
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        try {
            val testWork = OneTimeWorkRequestBuilder<SecurityWorker>().build()
            WorkManager.getInstance(this).enqueue(testWork)
        } catch (e: Exception) {
            Log.e("HexaWardApp", "Failed to enqueue test worker", e)
        }
        
        UpdateCheckWorker.enqueuePeriodicCheck(this)
        UpdateCheckWorker.checkNow(this)
        
        val sharedPref = getSharedPreferences("hexa_pref", Context.MODE_PRIVATE)
        
        setContent {
            HexaWardTheme {
                val isOnboardingComplete = sharedPref.getBoolean("onboarding_finished", false)
                var currentScreen by remember { 
                    mutableStateOf(
                        if (isOnboardingComplete) "splash_to_main" else "splash"
                    ) 
                }
                
                // Add a small delay when transitioning to main to ensure proper cleanup
                var readyToShowMain by remember { mutableStateOf(false) }
                
                LaunchedEffect(currentScreen) {
                    if (currentScreen == "main") {
                        kotlinx.coroutines.delay(300) // Delay for smooth transition
                        readyToShowMain = true
                    } else {
                        readyToShowMain = false
                    }
                }

                Box(modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF0A0A0A)) 
                ) {
                    when (currentScreen) {
                        "splash" -> {
                            SplashScreen(onAnimationFinished = {
                                currentScreen = "onboarding"
                            })
                        }
                        "splash_to_main" -> {
                            SplashScreen(onAnimationFinished = {
                                currentScreen = "main"
                            })
                        }
                        "onboarding" -> {
                            OnboardingScreen(onFinished = {
                                sharedPref.edit { 
                                    putBoolean("onboarding_finished", true)
                                    apply()
                                }
                                currentScreen = "main"
                            })
                        }
                        "main" -> {
                            if (readyToShowMain) {
                                MainNavigationWrapper()
                            } else {
                                // Show loading during transition
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(
                                        color = Color(0xFF2196F3)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MainNavigationWrapper() {
    val viewModel: MainViewModel = hiltViewModel()
    MainNavigationContent(viewModel)
}

@Composable
fun MainNavigationContent(viewModel: MainViewModel) {
    val pagerState = rememberPagerState(pageCount = { 3 })
    val scope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    
    val riskStatus by viewModel.riskStatus.collectAsState()
    val settings by viewModel.settings.collectAsState()
    
    val themeColor = when {
        riskStatus.score < 25 -> Color(0xFF4CAF50)
        riskStatus.score < 50 -> Color(0xFF2196F3)
        riskStatus.score < 75 -> Color(0xFFFF9800)
        else -> Color(0xFFF44336)
    }

    // Bloom Effect values for drawer border glow
    val bloomIntensity by animateFloatAsState(
        targetValue = if (drawerState.isOpen) 1f else 0f,
        animationSpec = tween(600, easing = FastOutSlowInEasing),
        label = "bloom_intensity"
    )
    val bloomWidth by animateFloatAsState(
        targetValue = if (drawerState.isOpen) 8f else 1f,
        animationSpec = tween(600),
        label = "bloom_width"
    )

    // Advanced Back Handling
    BackHandler(enabled = drawerState.isOpen || pagerState.currentPage != 0) {
        if (drawerState.isOpen) {
            scope.launch { drawerState.close() }
        } else if (pagerState.currentPage != 0) {
            scope.launch { pagerState.animateScrollToPage(0) }
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        scrimColor = Color.Black.copy(alpha = 0.5f), 
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = Color.Transparent,
                drawerTonalElevation = 0.dp,
                drawerShape = IndustrialDrawerShape,
                modifier = Modifier
                    .width(320.dp)
                    .fillMaxHeight()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    // Background with reduced opacity
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.linearGradient(
                                    listOf(
                                        Color(0xFF0A0A0A).copy(alpha = 0.85f),
                                        Color(0xFF151515).copy(alpha = 0.90f)
                                    )
                                )
                            )
                    )
                    
                    // Bloom effect border glow
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val glowWidth = bloomWidth.dp.toPx()
                        val glowAlpha = bloomIntensity * 0.6f
                        
                        // Draw glowing border on the right edge
                        drawRect(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    themeColor.copy(alpha = glowAlpha * 0.3f),
                                    themeColor.copy(alpha = glowAlpha),
                                    themeColor.copy(alpha = glowAlpha * 0.3f)
                                ),
                                startX = size.width - glowWidth * 3,
                                endX = size.width
                            ),
                            topLeft = Offset(size.width - glowWidth * 3, 0f),
                            size = androidx.compose.ui.geometry.Size(glowWidth * 3, size.height)
                        )
                        
                        // Solid border line
                        drawLine(
                            color = themeColor.copy(alpha = 0.8f),
                            start = Offset(size.width - 1.dp.toPx(), 0f),
                            end = Offset(size.width - 1.dp.toPx(), size.height),
                            strokeWidth = 2.dp.toPx()
                        )
                    }
                    
                    if (settings.techGridEnabled) {
                        TechGridPattern(themeColor)
                    }

                    Column {
                        Spacer(Modifier.height(80.dp))
                        
                        Column(modifier = Modifier.padding(horizontal = 32.dp)) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.Shield, 
                                    null, 
                                    tint = themeColor.copy(alpha = 0.15f), 
                                    modifier = Modifier.size(80.dp)
                                )
                                Icon(
                                    Icons.Default.AdminPanelSettings, 
                                    null, 
                                    tint = themeColor, 
                                    modifier = Modifier.size(40.dp)
                                )
                            }
                            Spacer(Modifier.height(16.dp))
                            Text(
                                "HEXA-SYSTEM",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = themeColor,
                                letterSpacing = 4.sp
                            )
                            Box(
                                modifier = Modifier
                                    .padding(top = 4.dp)
                                    .background(themeColor.copy(0.15f), RoundedCornerShape(4.dp))
                                    .border(1.dp, themeColor.copy(0.4f), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    "SECURE PROTOCOL ACTIVE",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = themeColor,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        
                        Spacer(Modifier.height(48.dp))
                        
                        IndustrialNavItem("DASHBOARD", Icons.Default.Dashboard, pagerState.currentPage == 0, themeColor) {
                            scope.launch { pagerState.animateScrollToPage(0); drawerState.close() }
                        }
                        IndustrialNavItem("SECURITY LAB", Icons.Default.Science, pagerState.currentPage == 1, themeColor) {
                            scope.launch { pagerState.animateScrollToPage(1); drawerState.close() }
                        }
                        IndustrialNavItem("CORE SETTINGS", Icons.Default.Settings, pagerState.currentPage == 2, themeColor) {
                            scope.launch { pagerState.animateScrollToPage(2); drawerState.close() }
                        }
                        
                        Spacer(Modifier.weight(1f))
                        
                        Card(
                            modifier = Modifier.padding(24.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White.copy(0.08f)),
                            border = BorderStroke(1.dp, themeColor.copy(0.3f))
                        ) {
                            Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                CircularProgressIndicator(
                                    progress = { riskStatus.score / 100f },
                                    modifier = Modifier.size(24.dp),
                                    strokeWidth = 3.dp,
                                    color = themeColor
                                )
                                Spacer(Modifier.width(16.dp))
                                Column {
                                    Text("CORE STATUS", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                    Text(riskStatus.level.name, style = MaterialTheme.typography.bodySmall, color = themeColor, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    ) {
        // Global App Environment Background
        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawBehind {
                    if (settings.techGridEnabled) {
                        val step = 40.dp.toPx()
                        for (x in 0..size.width.toInt() step step.toInt()) {
                            drawLine(themeColor.copy(0.04f), start = Offset(x.toFloat(), 0f), end = Offset(x.toFloat(), size.height))
                        }
                        for (y in 0..size.height.toInt() step step.toInt()) {
                            drawLine(themeColor.copy(0.04f), start = Offset(0f, y.toFloat()), end = Offset(size.width, y.toFloat()))
                        }
                    }
                }
        ) {
            CornerAccents(themeColor)

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
                userScrollEnabled = true,
                beyondViewportPageCount = 1
            ) { page ->
                Box(
                    Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            val pageOffset = ((pagerState.currentPage - page) + pagerState.currentPageOffsetFraction).absoluteValue
                            alpha = lerp(start = 1f, stop = 0f, fraction = pageOffset.coerceIn(0f, 1f))
                            val scale = lerp(start = 1f, stop = 0.92f, fraction = pageOffset.coerceIn(0f, 1f))
                            scaleX = scale
                            scaleY = scale
                        }
                ) {
                    when (page) {
                        0 -> DashboardScreen(
                            viewModel = viewModel,
                            onOpenDrawer = { scope.launch { drawerState.open() } },
                            onNavigateToSettings = { scope.launch { pagerState.animateScrollToPage(2) } }
                        )
                        1 -> SecurityLabScreen(
                            viewModel = viewModel,
                            onOpenDrawer = { scope.launch { drawerState.open() } }
                        )
                        2 -> SettingsScreen(
                            viewModel = viewModel,
                            onOpenDrawer = { scope.launch { drawerState.open() } },
                            onNavigateBack = { scope.launch { pagerState.animateScrollToPage(0) } }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CornerAccents(color: Color) {
    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .drawBehind {
                    drawLine(color.copy(0.3f), Offset(0f, 0f), Offset(size.width, 0f), 2.dp.toPx())
                    drawLine(color.copy(0.3f), Offset(0f, 0f), Offset(0f, size.height), 2.dp.toPx())
                }
        )
        Box(
            modifier = Modifier
                .size(60.dp)
                .align(Alignment.BottomEnd)
                .drawBehind {
                    drawLine(color.copy(0.3f), Offset(size.width, size.height), Offset(0f, size.height), 2.dp.toPx())
                    drawLine(color.copy(0.3f), Offset(size.width, size.height), Offset(size.width, 0f), 2.dp.toPx())
                }
        )
    }
}

@Composable
fun TechGridPattern(color: Color) {
    androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
        val step = 40.dp.toPx()
        for (x in 0..size.width.toInt() step step.toInt()) {
            drawLine(color.copy(0.03f), start = Offset(x.toFloat(), 0f), end = Offset(x.toFloat(), size.height))
        }
        for (y in 0..size.height.toInt() step step.toInt()) {
            drawLine(color.copy(0.03f), start = Offset(0f, y.toFloat()), end = Offset(size.width, y.toFloat()))
        }
    }
}

@Composable
fun IndustrialNavItem(label: String, icon: ImageVector, selected: Boolean, themeColor: Color, onClick: () -> Unit) {
    val alpha by animateFloatAsState(if (selected) 1f else 0.5f, label = "alpha")
    val translationX by animateFloatAsState(if (selected) 16f else 0f, label = "translation")

    Surface(
        onClick = onClick,
        color = Color.Transparent,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .graphicsLayer { this.translationX = translationX }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 32.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, null, tint = themeColor.copy(alpha = alpha), modifier = Modifier.size(24.dp))
            Spacer(Modifier.width(20.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                color = if (selected) themeColor else Color.White.copy(0.6f),
                letterSpacing = 2.sp
            )
            if (selected) {
                Spacer(Modifier.weight(1f))
                Box(modifier = Modifier
                    .size(width = 12.dp, height = 2.dp)
                    .background(themeColor, RoundedCornerShape(1.dp))
                )
            }
        }
    }
}
