package com.minimalphone.launcher.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Clean, line-art minimalist weather icon matching the user's reference photo.
 * White outline with rounded caps on pure black background.
 */
@Composable
fun MinimalWeatherIcon(
    weatherCode: Int,
    modifier: Modifier = Modifier,
    size: Dp = 120.dp,
    strokeWidth: Dp = 3.5.dp,
    color: Color = Color.White
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val strokePx = strokeWidth.toPx()

        when (weatherCode) {
            0 -> {
                // Clear Sky / Sun: Minimalist circular sun with clean rays
                val center = Offset(w / 2, h / 2)
                val radius = w * 0.22f
                drawCircle(
                    color = color,
                    radius = radius,
                    center = center,
                    style = Stroke(width = strokePx)
                )
                val rayLength = w * 0.12f
                val rayDist = radius + w * 0.08f
                for (i in 0 until 8) {
                    val angle = Math.toRadians((i * 45).toDouble())
                    val startX = center.x + (rayDist * Math.cos(angle)).toFloat()
                    val startY = center.y + (rayDist * Math.sin(angle)).toFloat()
                    val endX = center.x + ((rayDist + rayLength) * Math.cos(angle)).toFloat()
                    val endY = center.y + ((rayDist + rayLength) * Math.sin(angle)).toFloat()
                    drawLine(
                        color = color,
                        start = Offset(startX, startY),
                        end = Offset(endX, endY),
                        strokeWidth = strokePx,
                        cap = StrokeCap.Round
                    )
                }
            }
            1, 2, 3 -> {
                // Partly cloudy / Overcast: Clean cloud outline
                drawCloudPath(w, h, strokePx, color, offsetY = h * 0.1f)
            }
            71, 73, 75, 77, 85, 86 -> {
                // Snow: Cloud outline + snow dots
                drawCloudPath(w, h, strokePx, color, offsetY = 0f)
                val dotRadius = strokePx * 0.8f
                val dropOffsets = listOf(
                    Offset(w * 0.35f, h * 0.72f),
                    Offset(w * 0.50f, h * 0.80f),
                    Offset(w * 0.65f, h * 0.72f)
                )
                dropOffsets.forEach { pos ->
                    drawCircle(color = color, radius = dotRadius, center = pos)
                }
            }
            95, 96, 99 -> {
                // Thunderstorm: Cloud outline + lightning bolt
                drawCloudPath(w, h, strokePx, color, offsetY = -h * 0.05f)
                val bolt = Path().apply {
                    moveTo(w * 0.52f, h * 0.60f)
                    lineTo(w * 0.44f, h * 0.75f)
                    lineTo(w * 0.50f, h * 0.75f)
                    lineTo(w * 0.42f, h * 0.92f)
                }
                drawPath(bolt, color = color, style = Stroke(width = strokePx, cap = StrokeCap.Round))
            }
            else -> {
                // Rain / Drizzle / Showers: Cloud outline + diagonal rain drops (MATCHING USER'S PHOTO!)
                drawCloudPath(w, h, strokePx, color, offsetY = -h * 0.04f)

                // 2 Rows of diagonal falling rain drops
                val dropLen = h * 0.09f
                val dx = -dropLen * 0.35f // diagonal tilt matching photo

                val drops = listOf(
                    // Row 1
                    Offset(w * 0.36f, h * 0.63f),
                    Offset(w * 0.50f, h * 0.63f),
                    Offset(w * 0.64f, h * 0.63f),
                    // Row 2
                    Offset(w * 0.43f, h * 0.78f),
                    Offset(w * 0.57f, h * 0.78f),
                    Offset(w * 0.71f, h * 0.78f)
                )

                drops.forEach { pt ->
                    drawLine(
                        color = color,
                        start = pt,
                        end = Offset(pt.x + dx, pt.y + dropLen),
                        strokeWidth = strokePx * 1.1f,
                        cap = StrokeCap.Round
                    )
                }
            }
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawCloudPath(
    w: Float,
    h: Float,
    strokePx: Float,
    color: Color,
    offsetY: Float
) {
    val cloud = Path().apply {
        // Base width: from w*0.22 to w*0.78
        val baseLeft = w * 0.24f
        val baseRight = w * 0.76f
        val baseY = h * 0.56f + offsetY

        // Start bottom-left, draw flat base
        moveTo(baseLeft + 12f, baseY)
        lineTo(baseRight - 12f, baseY)

        // Arc right corner
        cubicTo(
            baseRight, baseY,
            baseRight + 4f, baseY - 8f,
            baseRight, baseY - 18f
        )

        // Arc right bump
        cubicTo(
            baseRight + 6f, baseY - 40f,
            baseRight - 14f, baseY - 50f,
            baseRight - 28f, baseY - 42f
        )

        // Top main central puff
        cubicTo(
            baseRight - 36f, baseY - 78f,
            baseLeft + 36f, baseY - 78f,
            baseLeft + 28f, baseY - 42f
        )

        // Left bump
        cubicTo(
            baseLeft + 14f, baseY - 50f,
            baseLeft - 6f, baseY - 40f,
            baseLeft, baseY - 18f
        )

        // Arc left corner into base
        cubicTo(
            baseLeft - 4f, baseY - 8f,
            baseLeft, baseY,
            baseLeft + 12f, baseY
        )
        close()
    }
    drawPath(cloud, color = color, style = Stroke(width = strokePx, cap = StrokeCap.Round))
}
