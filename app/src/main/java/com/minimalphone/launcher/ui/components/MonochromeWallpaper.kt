package com.minimalphone.launcher.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path

/**
 * Procedural Vector Wallpaper matching the layered monochrome dune / landscape
 * aesthetic from the reference photo.
 * Scales dynamically to any aspect ratio with zero pixelation.
 */
@Composable
fun MonochromeWallpaper(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        // Layer 0: Sky (Silvery Light Gray)
        drawRect(Color(0xFFCDCFD2))

        // Layer 1: Distant Dunes (Soft Pale Slate)
        val path1 = Path().apply {
            moveTo(0f, h * 0.36f)
            cubicTo(w * 0.25f, h * 0.34f, w * 0.65f, h * 0.39f, w, h * 0.37f)
            lineTo(w, h)
            lineTo(0f, h)
            close()
        }
        drawPath(path1, Color(0xFFAAB0B7))

        // Layer 2: Mid Dunes (Medium Slate Gray)
        val path2 = Path().apply {
            moveTo(0f, h * 0.44f)
            cubicTo(w * 0.35f, h * 0.47f, w * 0.70f, h * 0.42f, w, h * 0.46f)
            lineTo(w, h)
            lineTo(0f, h)
            close()
        }
        drawPath(path2, Color(0xFF787E86))

        // Layer 3: Foreground Ridges (Dark Charcoal)
        val path3 = Path().apply {
            moveTo(0f, h * 0.53f)
            cubicTo(w * 0.30f, h * 0.51f, w * 0.65f, h * 0.59f, w, h * 0.56f)
            lineTo(w, h)
            lineTo(0f, h)
            close()
        }
        drawPath(path3, Color(0xFF383C43))

        // Layer 4: Deep Base (Pure Deep Black)
        val path4 = Path().apply {
            moveTo(0f, h * 0.67f)
            cubicTo(w * 0.40f, h * 0.71f, w * 0.75f, h * 0.64f, w, h * 0.69f)
            lineTo(w, h)
            lineTo(0f, h)
            close()
        }
        drawPath(path4, Color(0xFF101114))
    }
}
