package com.example.hexaward.presentation

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class OnboardingPage(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val color: Color,
    val demoAction: @Composable (Color) -> Unit
)

@Composable
fun OnboardingScreen(onFinished: () -> Unit) {
    val pages = listOf(
        OnboardingPage(
            "Welcome to HexaWard",
            "Your high-performance shield against invisible digital threats.",
            Icons.Default.Lock,
            Color(0xFF2196F3)
        ) { color ->
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(200.dp)) {
                val infiniteTransition = rememberInfiniteTransition(label = "pulse")
                val scale by infiniteTransition.animateFloat(
                    initialValue = 1f, targetValue = 1.2f,
                    animationSpec = infiniteRepeatable(tween(1000), RepeatMode.Reverse), label = "scale"
                )
                Box(Modifier.size(150.dp).clip(CircleShape).background(color.copy(alpha = 0.1f * scale)))
                Icon(Icons.Default.Lock, null, tint = color, modifier = Modifier.size(100.dp))
            }
        },
        OnboardingPage(
            "Dynamic Risk Meter",
            "Watch your security status in real-time with our 'Bike Dashboard' style meter.",
            Icons.Default.Settings,
            Color(0xFF4CAF50)
        ) { color ->
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(200.dp)) {
                val animValue = remember { Animatable(0f) }
                LaunchedEffect(Unit) {
                    while(true) {
                        animValue.animateTo(100f, tween(1500, easing = FastOutSlowInEasing))
                        delay(500)
                        animValue.snapTo(0f)
                    }
                }
                Canvas(Modifier.size(160.dp)) {
                    drawArc(color.copy(alpha = 0.1f), 135f, 270f, false, style = Stroke(12.dp.toPx(), cap = StrokeCap.Round))
                    drawArc(color, 135f, (animValue.value / 100f) * 270f, false, style = Stroke(12.dp.toPx(), cap = StrokeCap.Round))
                }
                Text("${animValue.value.toInt()}", fontSize = 32.sp, fontWeight = FontWeight.Black, color = color)
            }
        },
        OnboardingPage(
            "Secret Insights",
            "Hidden gestures! Long-press any feature in settings to reveal its secret description.",
            Icons.Default.Notifications,
            Color(0xFF673AB7)
        ) { color ->
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(200.dp)) {
                val isPressed = remember { mutableStateOf(false) }
                LaunchedEffect(Unit) {
                    while(true) {
                        delay(1000)
                        isPressed.value = true
                        delay(1500)
                        isPressed.value = false
                    }
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = if(isPressed.value) 0.3f else 0.1f)),
                        modifier = Modifier.size(140.dp, 80.dp),
                        border = BorderStroke(1.dp, color.copy(alpha = 0.5f))
                    ) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("HOLD ME", color = color, fontWeight = FontWeight.Bold)
                        }
                    }
                    AnimatedVisibility(visible = isPressed.value) {
                        Text("SECRET REVEALED!", Modifier.padding(top = 16.dp), color = color, fontWeight = FontWeight.ExtraBold)
                    }
                }
            }
        },
        OnboardingPage(
            "Liquid Navigation",
            "Swipe left or right to glide between your Dashboard and Security Config.",
            Icons.Default.ArrowForward,
            Color(0xFFFF9800)
        ) { color ->
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(200.dp)) {
                val offset = remember { Animatable(0f) }
                LaunchedEffect(Unit) {
                    while(true) {
                        offset.animateTo(50f, tween(1000))
                        offset.animateTo(-50f, tween(1000))
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(60.dp).clip(CircleShape).background(color.copy(alpha = 0.2f)))
                    Spacer(Modifier.width(20.dp).offset(x = offset.value.dp))
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, null, tint = color, modifier = Modifier.size(40.dp))
                }
            }
        }
    )

    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()

    Scaffold { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface)
                .padding(padding)
        ) {
            HorizontalPager(state = pagerState) { index ->
                val page = pages[index]
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    page.demoAction(page.color)
                    Spacer(Modifier.height(48.dp))
                    Text(
                        text = page.title,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Black,
                        color = page.color,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = page.description,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center,
                        lineHeight = 24.sp
                    )
                }
            }

            // Footer
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 48.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Page Indicator
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    repeat(pages.size) { i ->
                        val active = pagerState.currentPage == i
                        Box(
                            modifier = Modifier
                                .size(if (active) 24.dp else 8.dp, 8.dp)
                                .clip(CircleShape)
                                .background(if (active) pages[pagerState.currentPage].color else MaterialTheme.colorScheme.outlineVariant)
                                .animateContentSize()
                        )
                    }
                }
                
                Spacer(Modifier.height(32.dp))
                
                Button(
                    onClick = {
                        if (pagerState.currentPage < pages.size - 1) {
                            scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                        } else {
                            onFinished()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth(0.7f)
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = pages[pagerState.currentPage].color)
                ) {
                    Text(
                        if (pagerState.currentPage == pages.size - 1) "GET STARTED" else "CONTINUE",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
