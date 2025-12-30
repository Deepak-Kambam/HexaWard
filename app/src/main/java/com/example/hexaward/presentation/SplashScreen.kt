package com.example.hexaward.presentation

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onAnimationFinished: () -> Unit) {
    val density = LocalDensity.current
    val infiniteTransition = rememberInfiniteTransition(label = "aesthetic_splash")
    
    // Industrial Geometric Shape
    val industrialShape = remember(density) {
        GenericShape { size, _ ->
            val cut = with(density) { 32.dp.toPx() }
            moveTo(cut, 0f)
            lineTo(size.width - cut, 0f)
            lineTo(size.width, cut)
            lineTo(size.width, size.height - cut)
            lineTo(size.width - cut, size.height)
            lineTo(cut, size.height)
            lineTo(0f, size.height - cut)
            lineTo(0f, cut)
            close()
        }
    }

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(tween(2000, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "pulse"
    )

    val scanProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(3000, easing = LinearEasing), RepeatMode.Restart),
        label = "scan"
    )

    LaunchedEffect(Unit) {
        delay(3500)
        onAnimationFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0A0A)),
        contentAlignment = Alignment.Center
    ) {
        // Persistent Grid Pattern
        TechGridPattern(Color(0xFF2196F3))

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(280.dp)) {
                // Central armored surface
                Box(
                    modifier = Modifier
                        .size(180.dp)
                        .clip(industrialShape)
                        .background(Color(0xFF2196F3).copy(alpha = 0.05f))
                        .border(2.dp, Color(0xFF2196F3).copy(alpha = 0.2f), industrialShape)
                )

                Canvas(modifier = Modifier.size(220.dp)) {
                    drawArc(
                        color = Color(0xFF2196F3).copy(alpha = 0.1f),
                        startAngle = 0f,
                        sweepAngle = 360f,
                        useCenter = false,
                        style = Stroke(width = 1.dp.toPx())
                    )
                }

                Box(contentAlignment = Alignment.Center) {
                    HexaWardLogo(
                        modifier = Modifier
                            .size(140.dp)
                            .scale(pulseScale),
                        color = Color(0xFF2196F3),
                        strokeWidth = 6f
                    )
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            Text(
                text = "HEXAWARD",
                fontSize = 48.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFF2196F3),
                letterSpacing = 8.sp,
                style = MaterialTheme.typography.headlineLarge
            )
            
            Text(
                text = "SECURE PROTOCOL ACTIVE",
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray,
                letterSpacing = 4.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp)
            )
            
            Spacer(modifier = Modifier.height(64.dp))
            
            // Industrial Loading
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = if (scanProgress < 0.5f) "INITIALIZING_CORE..." else "PROTOCOL_SECURE",
                    style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                    color = Color(0xFF2196F3).copy(alpha = 0.6f),
                    letterSpacing = 2.sp
                )
                Spacer(Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .width(200.dp)
                        .height(2.dp)
                        .background(Color.White.copy(alpha = 0.1f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(scanProgress)
                            .fillMaxHeight()
                            .background(Color(0xFF2196F3))
                    )
                }
            }
        }
    }
}

@Composable
fun TechGridPattern(color: Color) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val step = 40.dp.toPx()
        for (x in 0..size.width.toInt() step step.toInt()) {
            drawLine(color.copy(0.03f), start = Offset(x.toFloat(), 0f), end = Offset(x.toFloat(), size.height))
        }
        for (y in 0..size.height.toInt() step step.toInt()) {
            drawLine(color.copy(0.03f), start = Offset(0f, y.toFloat()), end = Offset(size.width, y.toFloat()))
        }
    }
}
