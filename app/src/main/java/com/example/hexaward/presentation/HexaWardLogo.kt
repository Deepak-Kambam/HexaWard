package com.example.hexaward.presentation

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun HexaWardLogo(
    modifier: Modifier = Modifier,
    color: Color,
    strokeWidth: Float? = null
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
        
        // 2. Inner Shield
        // A tech-shield shape: slightly smaller than hex
        val shieldHeight = radius * 1.1f // From top to bottom tip
        val shieldWidth = radius * 0.9f
        
        val shieldPath = Path().apply {
            val topY = center.y - shieldHeight * 0.4f
            val bottomY = center.y + shieldHeight * 0.5f
            val leftX = center.x - shieldWidth * 0.6f
            val rightX = center.x + shieldWidth * 0.6f
            
            // Top horizontal line (slightly indented)
            moveTo(leftX, topY)
            lineTo(center.x, topY + shieldHeight * 0.1f) // Small dip in center top
            lineTo(rightX, topY)
            
            // Sides curving to bottom point
            cubicTo(
                rightX, topY + shieldHeight * 0.5f, // Control point 1
                center.x, bottomY,                  // Control point 2 (approx)
                center.x, bottomY                   // End point
            )
            cubicTo(
                center.x, bottomY,
                leftX, topY + shieldHeight * 0.5f,
                leftX, topY
            )
            close()
        }
        
        drawPath(
            path = shieldPath,
            color = color.copy(alpha = 0.15f),
            style = Fill
        )
        drawPath(
            path = shieldPath,
            color = color.copy(alpha = 0.8f),
            style = Stroke(width = effectiveStrokeWidth * 0.7f)
        )
        
        // 3. Central Core Node
        drawCircle(
            color = color,
            radius = radius * 0.15f,
            center = center
        )
        
        // 4. Circuit Lines (Decorative)
        // Top line
        drawLine(
            color = color.copy(alpha = 0.6f),
            start = Offset(center.x, center.y - radius * 0.15f),
            end = Offset(center.x, center.y - radius * 0.4f), // Connecting to shield dip
            strokeWidth = effectiveStrokeWidth * 0.5f
        )
    }
}
