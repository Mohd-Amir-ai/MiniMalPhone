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

    private var lastVibrateTime = 0L

    /**
     * Triggers boosted physical vibration motor tick pulse (+30% stronger)
     * with mechanical sound click feedback.
     */
    fun triggerScrollTick(context: Context, view: View?) {
        val now = System.currentTimeMillis()
        if (now - lastVibrateTime < 25) return // Prevent overlapping vibration clipping
        lastVibrateTime = now

        try {
            // 1. Direct hardware linear vibration motor activation (Boosted by 30%)
            val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                manager?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }

            if (vibrator != null && vibrator.hasVibrator()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    // 20ms duration at 245/255 amplitude (+30% punchier)
                    vibrator.vibrate(VibrationEffect.createOneShot(20, 245))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(20)
                }
            }

            // 2. View-level haptic tick
            view?.performHapticFeedback(
                HapticFeedbackConstants.CLOCK_TICK,
                HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING or HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING
            )
            // 3. System click sound
            view?.playSoundEffect(SoundEffectConstants.CLICK)
        } catch (e: Exception) {
            // Silently ignore
        }
    }
}
