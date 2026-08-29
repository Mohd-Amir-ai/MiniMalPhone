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

    /**
     * Disables Daltonizer color correction, restoring original 100% full vibrant colors.
     */
    fun disableMonochrome(context: Context): Boolean {
        return try {
            Settings.Secure.putInt(
                context.contentResolver,
                "accessibility_display_daltonizer_enabled",
                0
            )
            Log.i(TAG, "Hardware Monochrome disabled, colors restored!")
            true
        } catch (e: Exception) {
            Log.w(TAG, "Could not disable monochrome mode: ${e.message}")
            false
        }
    }

    /**
     * Checks if Daltonizer monochromacy is currently active on the device.
     */
    fun isMonochromeActive(context: Context): Boolean {
        return try {
            val enabled = Settings.Secure.getInt(
                context.contentResolver,
                "accessibility_display_daltonizer_enabled",
                0
            ) == 1
            val mode = Settings.Secure.getInt(
                context.contentResolver,
                "accessibility_display_daltonizer",
                -1
            )
            enabled && mode == 0
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Diagnostic: Checks whether WRITE_SECURE_SETTINGS has been granted to MiniMalPhone.
     */
    fun isPermissionGranted(context: Context): Boolean {
        return try {
            // Read current value and attempt a no-op write to test write permission
            val current = Settings.Secure.getInt(
                context.contentResolver,
                "accessibility_display_daltonizer_enabled",
                0
            )
            Settings.Secure.putInt(
                context.contentResolver,
                "accessibility_display_daltonizer_enabled",
                current
            )
            true
        } catch (e: SecurityException) {
            false
        } catch (e: Exception) {
            false
        }
    }
}
