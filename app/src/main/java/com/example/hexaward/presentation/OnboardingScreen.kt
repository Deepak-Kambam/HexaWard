package com.example.hexaward.presentation

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

@Composable
fun OnboardingScreen(onFinished: () -> Unit) {
    val density = LocalDensity.current
    
    val industrialShape = remember(density) {
        GenericShape { size, _ ->
            val cut = with(density) { 24.dp.toPx() }
            moveTo(cut, 0f)
            lineTo(size.width - cut, 0f)
            lineTo(size.width, cut)
            lineTo(size.width, size.height)
            lineTo(0f, size.height)
            lineTo(0f, cut)
            close()
        }
    }

    val pages = listOf(
        OnboardingPage(
            "ARMORED SUITE",
            "INITIALIZING HIGH-PERFORMANCE SHIELD AGAINST DIGITAL ANOMALIES.",
            Icons.Default.Security,
            Color(0xFF2196F3)
        ),
        OnboardingPage(
            "RISK ENGINE",
            "REAL-TIME BEHAVIORAL ANALYSIS WITH BIKE-DASHBOARD PRECISION.",
            Icons.Default.Speed,
            Color(0xFF4CAF50)
        ),
        OnboardingPage(
            "SECRET INTEL",
            "UNCOVER HIDDEN PROTOCOLS WITH ADVANCED GESTURE TRIGGERS.",
            Icons.Default.Fingerprint,
            Color(0xFF673AB7)
        ),
        OnboardingPage(
            "CORE NAVIGATOR",
            "SEAMLESS SYSTEM TRANSITIONS THROUGH INDUSTRIAL INTERFACE LAYERS.",
            Icons.AutoMirrored.Filled.ArrowForward,
            Color(0xFFFF9800)
        )
    )

    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF0A0A0A)).windowInsetsPadding(WindowInsets.systemBars)) {
        TechGridPattern(pages[pagerState.currentPage].color)

        Column(modifier = Modifier.fillMaxSize()) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f)
            ) { index ->
                val page = pages[index]
                Column(
                    modifier = Modifier.fillMaxSize().padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(160.dp)
                            .clip(industrialShape)
                            .background(page.color.copy(alpha = 0.05f))
                            .border(2.dp, page.color.copy(alpha = 0.2f), industrialShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = page.icon,
                            contentDescription = null,
                            tint = page.color,
                            modifier = Modifier.size(80.dp)
                        )
                    }

                    Spacer(Modifier.height(64.dp))

                    Text(
                        text = page.title,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Black,
                        color = page.color,
                        letterSpacing = 4.sp
                    )

                    Spacer(Modifier.height(16.dp))

                    Text(
                        text = page.description,
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.Gray,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        lineHeight = 24.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
            }

            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    repeat(pages.size) { i ->
                        val active = pagerState.currentPage == i
                        Box(
                            modifier = Modifier
                                .size(width = if (active) 24.dp else 8.dp, height = 4.dp)
                                .background(if (active) pages[pagerState.currentPage].color else Color.White.copy(0.1f))
                        )
                    }
                }
                
                Spacer(Modifier.height(48.dp))
                
                Button(
                    onClick = {
                        if (pagerState.currentPage < pages.size - 1) {
                            scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                        } else {
                            onFinished()
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(64.dp).clip(industrialShape),
                    shape = industrialShape,
                    colors = ButtonDefaults.buttonColors(containerColor = pages[pagerState.currentPage].color.copy(0.8f))
                ) {
                    Text(
                        if (pagerState.currentPage == pages.size - 1) "INITIALIZE SYSTEM" else "CONTINUE_SEQUENCE",
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp
                    )
                }
            }
        }
    }
}

data class OnboardingPage(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val color: Color
)
