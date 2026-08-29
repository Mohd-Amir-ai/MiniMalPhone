package com.minimalphone.launcher.core.wallpaper

import android.app.WallpaperManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.os.Build
import android.util.Log

object WallpaperHelper {
    private const val TAG = "WallpaperHelper"

    /**
     * Renders the exact layered monochrome landscape into a high-resolution Bitmap
     * matching the device's native resolution, and applies it to BOTH the
     * System Home Wallpaper and Lock Screen Wallpaper.
     */
    fun applyDuneWallpaper(context: Context): Boolean {
        return try {
            val wm = WallpaperManager.getInstance(context)
            val metrics = context.resources.displayMetrics
            val width = metrics.widthPixels.coerceAtLeast(1080)
            val height = metrics.heightPixels.coerceAtLeast(1920)

            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)

            val paint = Paint(Paint.ANTI_ALIAS_FLAG)

            // Layer 0: Sky (#CDCFD2 - Light Silvery Gray)
            paint.color = 0xFFCDCFD2.toInt()
            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)

            // Layer 1: Distant Dunes (#AAB0B7)
            paint.color = 0xFFAAB0B7.toInt()
            val p1 = Path().apply {
                moveTo(0f, height * 0.36f)
                cubicTo(width * 0.25f, height * 0.34f, width * 0.65f, height * 0.39f, width.toFloat(), height * 0.37f)
                lineTo(width.toFloat(), height.toFloat())
                lineTo(0f, height.toFloat())
                close()
            }
            canvas.drawPath(p1, paint)

            // Layer 2: Mid Dunes (#787E86)
            paint.color = 0xFF787E86.toInt()
            val p2 = Path().apply {
                moveTo(0f, height * 0.44f)
                cubicTo(width * 0.35f, height * 0.47f, width * 0.70f, height * 0.42f, width.toFloat(), height * 0.46f)
                lineTo(width.toFloat(), height.toFloat())
                lineTo(0f, height.toFloat())
                close()
            }
            canvas.drawPath(p2, paint)

            // Layer 3: Foreground Ridges (#383C43)
            paint.color = 0xFF383C43.toInt()
            val p3 = Path().apply {
                moveTo(0f, height * 0.53f)
                cubicTo(width * 0.30f, height * 0.51f, width * 0.65f, height * 0.59f, width.toFloat(), height * 0.56f)
                lineTo(width.toFloat(), height.toFloat())
                lineTo(0f, height.toFloat())
                close()
            }
            canvas.drawPath(p3, paint)

            // Layer 4: Deep Base (#101114 - Pure Dark/Black)
            paint.color = 0xFF101114.toInt()
            val p4 = Path().apply {
                moveTo(0f, height * 0.67f)
                cubicTo(width * 0.40f, height * 0.71f, width * 0.75f, height * 0.64f, width.toFloat(), height * 0.69f)
                lineTo(width.toFloat(), height.toFloat())
                lineTo(0f, height.toFloat())
                close()
            }
            canvas.drawPath(p4, paint)

            // Apply to System and Lock Screen
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                wm.setBitmap(bitmap, null, true, WallpaperManager.FLAG_SYSTEM or WallpaperManager.FLAG_LOCK)
            } else {
                wm.setBitmap(bitmap)
            }
            Log.i(TAG, "Successfully applied wallpaper to Home and Lockscreen!")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to apply wallpaper: ${e.message}", e)
            false
        }
    }
}
