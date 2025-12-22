package com.example.hexaward.presentation

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun SplashScreen(onAnimationFinished: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "aesthetic_splash")
    
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(tween(2000, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "pulse"
    )

    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(12000, easing = LinearEasing), RepeatMode.Restart),
        label = "rotation"
    )

    val scanProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(3000, easing = LinearEasing), RepeatMode.Restart),
        label = "scan"
    )

    val cursorAlpha by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(400), RepeatMode.Reverse),
        label = "cursor"
    )

    LaunchedEffect(Unit) {
        delay(3500)
        onAnimationFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF1A237E).copy(alpha = 0.2f),
                        MaterialTheme.colorScheme.surface
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(280.dp)) {
                Canvas(modifier = Modifier.size(220.dp)) {
                    drawCircle(
                        brush = Brush.radialGradient(
                            listOf(Color(0xFF2196F3).copy(alpha = 0.15f), Color.Transparent)
                        ),
                        radius = size.width / 2 * pulseScale
                    )
                }

                Canvas(modifier = Modifier.fillMaxSize()) {
                    withTransform({ rotate(rotation) }) {
                        val radius = 110.dp.toPx()
                        for (i in 0 until 6) {
                            val angle = Math.toRadians(i * 60.0).toFloat()
                            drawCircle(
                                color = Color(0xFF2196F3).copy(alpha = 0.4f),
                                radius = 4.dp.toPx(),
                                center = Offset(
                                    center.x + radius * cos(angle),
                                    center.y + radius * sin(angle)
                                )
                            )
                        }
                    }
                }

                Canvas(
                    modifier = Modifier
                        .size(160.dp)
                        .graphicsLayer {
                            scaleX = pulseScale
                            scaleY = pulseScale
                        }
                ) {
                    val hexPath = createHexagonPath(size)
                    drawPath(
                        path = hexPath,
                        color = Color(0xFF2196F3).copy(alpha = 0.2f),
                        style = Stroke(8.dp.toPx())
                    )
                    drawPath(
                        path = hexPath,
                        color = Color(0xFF2196F3),
                        style = Stroke(2.dp.toPx(), cap = StrokeCap.Round)
                    )
                    withTransform({ rotate(scanProgress * 360f) }) {
                        drawArc(
                            brush = Brush.sweepGradient(
                                0f to Color.Transparent,
                                0.5f to Color(0xFF2196F3).copy(alpha = 0.3f),
                                1f to Color.Transparent
                            ),
                            startAngle = 0f,
                            sweepAngle = 90f,
                            useCenter = true,
                            alpha = 0.5f
                        )
                    }
                }

                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = null,
                    tint = Color(0xFF2196F3),
                    modifier = Modifier
                        .size(64.dp)
                        .scale(pulseScale)
                        .alpha(0.9f)
                )
            }

            Spacer(modifier = Modifier.height(40.dp))

            Text(
                text = "HEXAWARD",
                fontSize = 42.sp,
                fontWeight = FontWeight.Light,
                color = Color(0xFF2196F3),
                letterSpacing = 12.sp,
                modifier = Modifier.alpha(0.8f)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "CYBERNETIC DATA GUARD",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                letterSpacing = 4.sp,
                fontWeight = FontWeight.Medium
            )
            
            Spacer(modifier = Modifier.height(54.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.alpha(0.6f)
            ) {
                Text(
                    text = when {
                        scanProgress < 0.25f -> "CORE_BOOT_SEQUENCE"
                        scanProgress < 0.5f -> "SCANNING_ENCRYPTED_VOIDS"
                        scanProgress < 0.75f -> "SHIELD_SYNCHRONIZATION"
                        else -> "ACCESS_GRANTED_SUCCESS"
                    },
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp
                    ),
                    color = Color(0xFF2196F3),
                    letterSpacing = 2.sp
                )
                Spacer(modifier = Modifier.width(6.dp))
                Box(
                    modifier = Modifier
                        .size(width = 6.dp, height = 12.dp)
                        .background(Color(0xFF2196F3).copy(alpha = cursorAlpha))
                )
            }
        }
    }
}

private fun createHexagonPath(size: Size): Path {
    return Path().apply {
        val radius = size.minDimension / 2
        val center = Offset(size.width / 2, size.height / 2)
        for (i in 0 until 6) {
            val angle = Math.toRadians(i * 60.0 - 90.0).toFloat()
            val x = center.x + radius * cos(angle)
            val y = center.y + radius * sin(angle)
            if (i == 0) moveTo(x, y) else lineTo(x, y)
        }
        close()
    }
}
