package com.minimalphone.launcher.core.system

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.HapticFeedbackConstants
import android.view.SoundEffectConstants
import android.view.View

object HapticHelper {

    /**
     * Triggers a guaranteed physical vibration motor tick pulse on the phone,
     * plus auditory mechanical click feedback.
     */
    fun triggerScrollTick(context: Context, view: View?) {
        try {
            // 1. Direct hardware linear vibration motor activation
            val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                manager?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }

            if (vibrator != null && vibrator.hasVibrator()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    // Send a sharp 14ms vibration pulse with solid amplitude
                    vibrator.vibrate(VibrationEffect.createOneShot(14, 180))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(14)
                }
            }

            // 2. View-level haptic feedback (ignoring global setting to guarantee response)
            if (view != null) {
                view.performHapticFeedback(
                    HapticFeedbackConstants.CLOCK_TICK,
                    HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING or HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING
                )
                // 3. Native system mechanical click sound
                view.playSoundEffect(SoundEffectConstants.CLICK)
            }
        } catch (e: Exception) {
            // Silently handle any restriction
        }
    }
}
