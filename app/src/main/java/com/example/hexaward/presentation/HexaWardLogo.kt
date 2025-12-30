package com.example.hexaward.presentation

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.PaintingStyle
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.toArgb
import android.graphics.BlurMaskFilter
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun HexaWardLogo(
    modifier: Modifier = Modifier,
    color: Color,
    strokeWidth: Float? = null,
    showGlow: Boolean = true
) {
    Canvas(modifier = modifier.aspectRatio(1f)) {
        val center = this.center
        val radius = size.minDimension / 2f
        val effectiveStrokeWidth = strokeWidth ?: (radius * 0.08f)
        
        // 1. Outer Hexagon Ring
        val hexPath = Path().apply {
            for (i in 0 until 6) {
                // Pointy top: -90 degrees (270)
                val angleRad = Math.toRadians((270 + 60 * i).toDouble())
                val x = center.x + radius * cos(angleRad).toFloat()
                val y = center.y + radius * sin(angleRad).toFloat()
                if (i == 0) moveTo(x, y) else lineTo(x, y)
            }
            close()
        }
        
        // 2. Inner Shield
        val shieldHeight = radius * 1.1f 
        val shieldWidth = radius * 0.9f
        
        val shieldPath = Path().apply {
            val topY = center.y - shieldHeight * 0.4f
            val bottomY = center.y + shieldHeight * 0.5f
            val leftX = center.x - shieldWidth * 0.6f
            val rightX = center.x + shieldWidth * 0.6f
            
            moveTo(leftX, topY)
            lineTo(center.x, topY + shieldHeight * 0.1f) 
            lineTo(rightX, topY)
            
            cubicTo(
                rightX, topY + shieldHeight * 0.5f, 
                center.x, bottomY,                  
                center.x, bottomY                   
            )
            cubicTo(
                center.x, bottomY,
                leftX, topY + shieldHeight * 0.5f,
                leftX, topY
            )
            close()
        }

        // --- GLOW LAYER (Draws behind everything) ---
        if (showGlow) {
            val glowPaint = Paint().apply {
                this.color = color
                this.style = PaintingStyle.Stroke
                this.strokeWidth = effectiveStrokeWidth
            }
            
            // Apply native blur mask
            glowPaint.asFrameworkPaint().maskFilter = BlurMaskFilter(
                radius * 0.15f, 
                BlurMaskFilter.Blur.NORMAL
            )
            
            drawIntoCanvas { canvas ->
                // Glow for Hexagon
                canvas.drawPath(hexPath, glowPaint)
                
                // Glow for Shield
                canvas.drawPath(shieldPath, glowPaint)
                
                // Extra intense glow for the center core
                val coreGlowPaint = Paint().apply {
                    this.color = color
                    this.style = PaintingStyle.Fill
                }
                coreGlowPaint.asFrameworkPaint().maskFilter = BlurMaskFilter(
                    radius * 0.3f, 
                    BlurMaskFilter.Blur.NORMAL
                )
                canvas.drawCircle(center, radius * 0.2f, coreGlowPaint)
            }
        }
        
        // --- MAIN GRAPHICS LAYER ---

        // Draw Hexagon Background (faint)
        drawPath(
            path = hexPath,
            color = color.copy(alpha = 0.05f),
            style = Fill
        )

        // Draw Hexagon Border
        drawPath(
            path = hexPath,
            color = color,
            style = Stroke(width = effectiveStrokeWidth)
        )
        
        // Draw Shield Fill
        drawPath(
            path = shieldPath,
            color = color.copy(alpha = 0.15f),
            style = Fill
        )
        // Draw Shield Border
        drawPath(
            path = shieldPath,
            color = color.copy(alpha = 0.8f),
            style = Stroke(width = effectiveStrokeWidth * 0.7f)
        )
        
        // 3. Central Core Node (Bright)
        drawCircle(
            color = color,
            radius = radius * 0.15f,
            center = center
        )
        
        // 4. Circuit Lines (Decorative)
        drawLine(
            color = color.copy(alpha = 0.6f),
            start = Offset(center.x, center.y - radius * 0.15f),
            end = Offset(center.x, center.y - radius * 0.4f), 
            strokeWidth = effectiveStrokeWidth * 0.5f
        )
    }
}
