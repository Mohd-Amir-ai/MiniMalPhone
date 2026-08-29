package com.minimalphone.launcher.core.system

import android.content.Context
import android.provider.Settings
import android.util.Log

object MonochromeModeHelper {
    private const val TAG = "MonochromeMode"

    /**
     * Enables system-wide Black & White (Monochromacy) hardware display mode.
     * Every app that launches will render completely in B/W!
     */
    fun enableMonochrome(context: Context): Boolean {
        return try {
            // Enable Daltonizer (color correction)
            Settings.Secure.putInt(
                context.contentResolver,
                "accessibility_display_daltonizer_enabled",
                1
            )
            // 0 corresponds to Monochromacy (True Black & White)
            Settings.Secure.putInt(
                context.contentResolver,
                "accessibility_display_daltonizer",
                0
            )
            Log.i(TAG, "Hardware Monochrome B/W mode active!")
            true
        } catch (e: Exception) {
            Log.w(TAG, "Could not set monochrome mode: ${e.message}")
            false
        }
    }
}
