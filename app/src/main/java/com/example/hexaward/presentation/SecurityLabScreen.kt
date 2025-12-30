package com.example.hexaward.presentation

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.animation.core.AnimationConstants.DefaultDurationMillis
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
fun SecurityLabScreen(
    viewModel: MainViewModel,
    onOpenDrawer: () -> Unit
) {
    val riskStatus by viewModel.riskStatus.collectAsState()
    val labStates by viewModel.labStates.collectAsState()
    val density = LocalDensity.current

    // Industrial Geometric Card Shape
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

    // State for navigation and help
    var selectedTool by remember { mutableStateOf<LabTool?>(null) }
    var helpTool by remember { mutableStateOf<LabTool?>(null) }

    // Internal Back Navigation Handler
    BackHandler(enabled = selectedTool != null) {
        selectedTool = null
    }

    val themeColor = when {
        riskStatus.score < 25 -> Color(0xFF4CAF50)
        riskStatus.score < 50 -> Color(0xFF2196F3)
        riskStatus.score < 75 -> Color(0xFFFF9800)
        else -> Color(0xFFF44336)
    }

    val animatedThemeColor by animateColorAsState(
        targetValue = themeColor,
        animationSpec = tween(1000),
        label = "lab_theme_color"
    )

    // Animate background color based on selected tool
    val backgroundColor by animateColorAsState(
        targetValue = if (selectedTool != null) {
            selectedTool!!.color.copy(alpha = 0.15f)
        } else {
            Color(0xFF0A0A0A)
        },
        animationSpec = tween(600),
        label = "background_color"
    )

    SharedTransitionLayout {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundColor)
        ) {
            AnimatedContent(
                targetState = selectedTool,
                transitionSpec = {
                    if (targetState != null) {
                        (slideInHorizontally { it } + fadeIn()).togetherWith(slideOutHorizontally { -it } + fadeOut())
                    } else {
                        (slideInHorizontally { -it } + fadeIn()).togetherWith(slideOutHorizontally { it } + fadeOut())
                    }
                },
                label = "lab_navigation"
            ) { tool ->
                if (tool == null) {
                    Scaffold(
                        containerColor = Color.Transparent,
                        topBar = {
                            CenterAlignedTopAppBar(
                                title = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        HexaWardLogo(
                                            modifier = Modifier.size(28.dp),
                                            color = animatedThemeColor,
                                            strokeWidth = 2.5f
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        Text(
                                            "SECURITY LAB",
                                            fontWeight = FontWeight.Black,
                                            letterSpacing = 4.sp,
                                            color = animatedThemeColor
                                        )
                                    }
                                },
                                navigationIcon = {
                                    IconButton(onClick = onOpenDrawer) {
                                        Icon(
                                            Icons.Default.Menu,
                                            contentDescription = "Menu",
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
                                .padding(padding)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 20.dp)
                            ) {
                                LabHeader(animatedThemeColor)

                                Spacer(Modifier.height(32.dp))

                                val tools = remember {
                                    listOf(
                                        LabTool(
                                            "DEEP SCAN",
                                            "Full system integrity check.",
                                            Icons.Default.Search,
                                            Color(0xFF673AB7),
                                            "Deep Scan analyzes every hidden directory and system file for code signatures matching known ransomware variants and zero-day exploits."
                                        ),
                                        LabTool(
                                            "PERMS AUDIT",
                                            "Find risky app access.",
                                            Icons.Default.Security,
                                            Color(0xFF2196F3),
                                            "Audits installed applications for dangerous permission combinations often used by data exfiltration malware."
                                        ),
                                        LabTool(
                                            "NETWORK LAB",
                                            "Monitor data packets.",
                                            Icons.Default.Router,
                                            Color(0xFF00BCD4),
                                            "Real-time packet inspection to identify unauthorized connections to suspicious remote command-and-control servers."
                                        ),
                                        LabTool(
                                            "APP ANALYZER",
                                            "Verify package signatures.",
                                            Icons.Default.AppRegistration,
                                            Color(0xFF4CAF50),
                                            "Decompiles and checks the authenticity of app signatures to prevent 'Repackaging' attacks."
                                        ),
                                        LabTool(
                                            "PRIVACY VAULT",
                                            "Secure your sensitive files.",
                                            Icons.Default.VpnKey,
                                            Color(0xFFFF9800),
                                            "A hardware-backed encrypted storage area for sensitive documents. Invisible to unauthorized processes."
                                        ),
                                        LabTool(
                                            "KILL SWITCH",
                                            "Instantly block all data.",
                                            Icons.Default.PowerSettingsNew,
                                            Color(0xFFF44336),
                                            "Emergency measure that severs all background data connections and prevents new process execution."
                                        )
                                    )
                                }

                                LazyVerticalGrid(
                                    columns = GridCells.Fixed(2),
                                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(16.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    items(tools) { toolItem ->
                                        LabToolCard(
                                            tool = toolItem,
                                            state = labStates[toolItem.title] ?: LabToolState.Idle,
                                            industrialCardShape = industrialCardShape,
                                            sharedTransitionScope = this@SharedTransitionLayout,
                                            animatedVisibilityScope = this@AnimatedContent,
                                            onNavigate = { selectedTool = toolItem },
                                            onLongPress = { helpTool = toolItem }
                                        )
                                    }
                                }

                                Spacer(Modifier.height(16.dp))

                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 24.dp)
                                        .clip(industrialCardShape)
                                        .border(
                                            1.dp,
                                            animatedThemeColor.copy(alpha = 0.2f),
                                            industrialCardShape
                                        ),
                                    colors = CardDefaults.cardColors(
                                        containerColor = Color.White.copy(
                                            alpha = 0.03f
                                        )
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier.padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            Icons.Default.Info,
                                            contentDescription = null,
                                            tint = animatedThemeColor,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(Modifier.width(12.dp))
                                        Text(
                                            "LAB ENVIRONMENT: LONG-PRESS TOOLS FOR INTEL.",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.Gray,
                                            letterSpacing = 1.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                } else {
                    ToolDetailScreen(
                        tool = tool,
                        state = labStates[tool.title] ?: LabToolState.Idle,
                        industrialCardShape = industrialCardShape,
                        sharedTransitionScope = this@SharedTransitionLayout,
                        animatedVisibilityScope = this@AnimatedContent,
                        onRun = { viewModel.runLabTool(tool.title) },
                        onBack = { selectedTool = null }
                    )
                }
            }
        }
    }

    if (helpTool != null) {
        FeatureHelpDialog(
            content = HelpContent(
                helpTool!!.title,
                helpTool!!.longDesc,
                helpTool!!.icon,
                helpTool!!.color
            ),
            industrialCardShape = industrialCardShape,
            onDismiss = { helpTool = null }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
fun ToolDetailScreen(
    tool: LabTool,
    state: LabToolState,
    industrialCardShape: androidx.compose.ui.graphics.Shape,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    onRun: () -> Unit,
    onBack: () -> Unit
) {
    val isRunning = state is LabToolState.Running
    val isFinished = state is LabToolState.Finished

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(tool.title, fontWeight = FontWeight.Black, letterSpacing = 4.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                with(sharedTransitionScope) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(200.dp)
                            .sharedBounds(
                                sharedContentState = rememberSharedContentState(key = "tool-icon-${tool.title}"),
                                animatedVisibilityScope = animatedVisibilityScope,
                                resizeMode = SharedTransitionScope.ResizeMode.ScaleToBounds(),
                                boundsTransform = { _, _ ->
                                    tween(durationMillis = 500, easing = FastOutSlowInEasing)
                                }
                            )
                    ) {
                        if (isRunning) {
                            CircularProgressIndicator(
                                progress = { (state as LabToolState.Running).progress },
                                modifier = Modifier.fillMaxSize(),
                                color = tool.color,
                                strokeWidth = 12.dp,
                                trackColor = tool.color.copy(alpha = 0.1f),
                                strokeCap = androidx.compose.ui.graphics.StrokeCap.Butt
                            )
                        } else {
                            Surface(
                                modifier = Modifier.size(160.dp),
                                shape = industrialCardShape,
                                color = tool.color.copy(alpha = 0.1f),
                                border = BorderStroke(2.dp, tool.color.copy(alpha = 0.3f))
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = if (isFinished) Icons.Default.CheckCircle else tool.icon,
                                        contentDescription = null,
                                        tint = tool.color,
                                        modifier = Modifier.size(80.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(32.dp))

                with(sharedTransitionScope) {
                    Text(
                        text = tool.title,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Black,
                        color = tool.color,
                        letterSpacing = 3.sp,
                        modifier = Modifier.sharedBounds(
                            sharedContentState = rememberSharedContentState(key = "tool-title-${tool.title}"),
                            animatedVisibilityScope = animatedVisibilityScope,
                            boundsTransform = { _, _ ->
                                tween(durationMillis = 500, easing = FastOutSlowInEasing)
                            }
                        )
                    )
                }

                Spacer(Modifier.height(8.dp))

                Text(
                    text = when (state) {
                        is LabToolState.Idle -> "READY FOR PROTOCOL"
                        is LabToolState.Running -> state.status.uppercase()
                        is LabToolState.Finished -> "PROTOCOL COMPLETED"
                    },
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Black,
                    color = tool.color,
                    letterSpacing = 2.sp
                )

                Spacer(Modifier.height(24.dp))

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(industrialCardShape)
                        .border(1.dp, tool.color.copy(alpha = 0.2f), industrialCardShape),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.03f))
                ) {
                    Column(Modifier.padding(24.dp)) {
                        Text(
                            text = "SYSTEM INTEL",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Black,
                            color = tool.color,
                            letterSpacing = 2.sp
                        )
                        Spacer(Modifier.height(12.dp))
                        Text(
                            text = if (isFinished) (state as LabToolState.Finished).result else tool.longDesc,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.7f),
                            lineHeight = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(Modifier.weight(1f))

                Button(
                    onClick = onRun,
                    enabled = !isRunning,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .clip(industrialCardShape),
                    shape = industrialCardShape,
                    colors = ButtonDefaults.buttonColors(containerColor = tool.color.copy(alpha = 0.8f))
                ) {
                    Text(
                        if (isFinished) "RE-INITIALIZE" else "EXECUTE PROTOCOL",
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp
                    )
                }

                Spacer(Modifier.height(12.dp))
            }
        }
    }
}

@Composable
fun LabHeader(themeColor: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        Text(
            "CORE SYSTEM DIAGNOSTICS",
            style = MaterialTheme.typography.labelMedium,
            color = themeColor,
            fontWeight = FontWeight.Black,
            letterSpacing = 2.sp
        )
        Text(
            "MANUAL INSPECTION TOOLS FOR THREAT MITIGATION",
            style = MaterialTheme.typography.labelSmall,
            color = Color.Gray,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

data class LabTool(
    val title: String,
    val desc: String,
    val icon: ImageVector,
    val color: Color,
    val longDesc: String = ""
)

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun LabToolCard(
    tool: LabTool,
    state: LabToolState,
    industrialCardShape: androidx.compose.ui.graphics.Shape,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    onNavigate: () -> Unit,
    onLongPress: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val isRunning = state is LabToolState.Running
    val isFinished = state is LabToolState.Finished

    Card(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(industrialCardShape)
            .border(
                width = if (isRunning) 2.dp else 1.dp,
                color = if (isRunning) tool.color else tool.color.copy(alpha = 0.2f),
                shape = industrialCardShape
            )
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { onNavigate() },
                    onLongPress = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onLongPress()
                    }
                )
            },
        colors = CardDefaults.cardColors(
            containerColor = if (isRunning) tool.color.copy(alpha = 0.05f) else Color.White.copy(
                0.02f
            )
        )
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                with(sharedTransitionScope) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .sharedBounds(
                                sharedContentState = rememberSharedContentState(key = "tool-icon-${tool.title}"),
                                animatedVisibilityScope = animatedVisibilityScope,
                                resizeMode = SharedTransitionScope.ResizeMode.ScaleToBounds(),
                                boundsTransform = { _, _ ->
                                    tween(durationMillis = 500, easing = FastOutSlowInEasing)
                                }
                            )
                    ) {
                        if (isRunning) {
                            CircularProgressIndicator(
                                progress = { (state as LabToolState.Running).progress },
                                modifier = Modifier.size(48.dp),
                                color = tool.color,
                                strokeWidth = 4.dp,
                                trackColor = tool.color.copy(alpha = 0.1f),
                                strokeCap = androidx.compose.ui.graphics.StrokeCap.Butt
                            )
                        } else {
                            Surface(
                                modifier = Modifier.size(48.dp),
                                shape = RoundedCornerShape(8.dp),
                                color = if (isFinished) tool.color else tool.color.copy(alpha = 0.1f),
                                border = BorderStroke(1.dp, tool.color.copy(alpha = 0.2f))
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = if (isFinished) Icons.Default.Check else tool.icon,
                                        contentDescription = null,
                                        tint = if (isFinished) Color.White else tool.color,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    with(sharedTransitionScope) {
                        Text(
                            text = tool.title,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            textAlign = TextAlign.Center,
                            letterSpacing = 1.sp,
                            modifier = Modifier.sharedBounds(
                                sharedContentState = rememberSharedContentState(key = "tool-title-${tool.title}"),
                                animatedVisibilityScope = animatedVisibilityScope,
                                boundsTransform = { _, _ ->
                                    tween(durationMillis = 500, easing = FastOutSlowInEasing)
                                }
                            )
                        )
                    }

                    Spacer(Modifier.height(4.dp))

                    Text(
                        text = when (state) {
                            is LabToolState.Idle -> tool.desc.uppercase()
                            is LabToolState.Running -> "EXECUTING..."
                            is LabToolState.Finished -> "COMPLETE"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isRunning) tool.color else Color.Gray,
                        textAlign = TextAlign.Center,
                        lineHeight = 14.sp,
                        maxLines = 2,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}