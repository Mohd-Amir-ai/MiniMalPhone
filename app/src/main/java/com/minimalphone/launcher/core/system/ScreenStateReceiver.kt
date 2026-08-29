package com.minimalphone.launcher.core.system

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.minimalphone.launcher.ui.lockscreen.LockScreenActivity

class ScreenStateReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_SCREEN_OFF) {
            val lockIntent = Intent(context, LockScreenActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            }
            context.startActivity(lockIntent)
        }
    }
}
