package com.minimalphone.launcher.core.system

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings

data class OemLauncherInfo(
    val packageName: String,
    val label: String
)

object DefaultLauncherHelper {

    /**
     * Finds the device's native OEM launcher (e.g., Samsung One UI Home, Pixel Launcher, etc.)
     * by querying all home launcher activities and excluding MiniMalPhone.
     */
    fun getOemLauncherInfo(context: Context): OemLauncherInfo? {
        val pm = context.packageManager
        val homeIntent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_HOME)
        }

        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PackageManager.MATCH_ALL
        } else {
            0
        }

        val resolveInfos = pm.queryIntentActivities(homeIntent, flags)
        for (info in resolveInfos) {
            val pkg = info.activityInfo.packageName
            if (pkg != context.packageName) {
                val appLabel = try {
                    val appInfo = pm.getApplicationInfo(pkg, 0)
                    pm.getApplicationLabel(appInfo).toString()
                } catch (e: Exception) {
                    info.loadLabel(pm).toString()
                }
                return OemLauncherInfo(packageName = pkg, label = appLabel)
            }
        }
        return null
    }

    /**
     * 1-Tap Safe Restore:
     * 1. Disables system monochrome display mode (restores color).
     * 2. Clears MiniMalPhone's preferred activity state.
     * 3. Launches the native OEM launcher directly.
     * 4. Opens system Home Settings so the user can reconfirm One UI as default if desired.
     */
    fun restoreDefaultLauncher(context: Context) {
        // 1. Immediately disable Daltonizer B/W
        MonochromeModeHelper.disableMonochrome(context)

        // 2. Clear MiniMalPhone's preferred home status
        @Suppress("DEPRECATION")
        context.packageManager.clearPackagePreferredActivities(context.packageName)

        // 3. Launch OEM launcher directly
        val oemInfo = getOemLauncherInfo(context)
        val homeIntent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_HOME)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            if (oemInfo != null) {
                setPackage(oemInfo.packageName)
            }
        }

        try {
            context.startActivity(homeIntent)
        } catch (e: Exception) {
            // Fallback to system home settings
            val settingsIntent = Intent(Settings.ACTION_HOME_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            try {
                context.startActivity(settingsIntent)
            } catch (ignored: Exception) {}
        }

        // 4. Also launch Settings.ACTION_HOME_SETTINGS to ensure user sets One UI persistently
        try {
            val settingsIntent = Intent(Settings.ACTION_HOME_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(settingsIntent)
        } catch (ignored: Exception) {}
    }
}
