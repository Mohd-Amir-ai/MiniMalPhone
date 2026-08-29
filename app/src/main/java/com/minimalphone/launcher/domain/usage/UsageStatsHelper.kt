package com.minimalphone.launcher.domain.usage

import android.app.AppOpsManager
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Process
import android.provider.Settings
import java.util.Calendar

data class AppUsageItem(
    val packageName: String,
    val appName: String,
    val totalTimeInForegroundMs: Long
) {
    val formattedDuration: String
        get() {
            val totalMinutes = totalTimeInForegroundMs / (1000 * 60)
            val hours = totalMinutes / 60
            val minutes = totalMinutes % 60
            return when {
                hours > 0 && minutes > 0 -> "${hours}h ${minutes}m"
                hours > 0 -> "${hours}h"
                else -> "${minutes}m"
            }
        }
}

data class DailyUsageSummary(
    val totalScreenTimeMs: Long,
    val topApps: List<AppUsageItem>
) {
    val formattedTotalTime: String
        get() {
            val totalMinutes = totalScreenTimeMs / (1000 * 60)
            val hours = totalMinutes / 60
            val minutes = totalMinutes % 60
            return when {
                hours > 0 && minutes > 0 -> "${hours}h ${minutes}m"
                hours > 0 -> "${hours}h"
                totalMinutes > 0 -> "${minutes}m"
                else -> "0m"
            }
        }
}

class UsageStatsHelper(private val context: Context) {

    fun hasUsagePermission(): Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as? AppOpsManager ?: return false
        val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            appOps.unsafeCheckOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(),
                context.packageName
            )
        } else {
            @Suppress("DEPRECATION")
            appOps.checkOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(),
                context.packageName
            )
        }
        return mode == AppOpsManager.MODE_ALLOWED
    }

    fun openUsageSettings() {
        try {
            val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            val intent = Intent(Settings.ACTION_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        }
    }

    fun getDailyUsageSummary(): DailyUsageSummary {
        if (!hasUsagePermission()) {
            return DailyUsageSummary(0L, emptyList())
        }

        val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager
            ?: return DailyUsageSummary(0L, emptyList())

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val startTime = calendar.timeInMillis
        val endTime = System.currentTimeMillis()

        val stats = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            startTime,
            endTime
        ) ?: return DailyUsageSummary(0L, emptyList())

        val pm = context.packageManager
        val aggregated = mutableMapOf<String, Long>()

        for (usage in stats) {
            if (usage.totalTimeInForeground > 0) {
                val current = aggregated.getOrDefault(usage.packageName, 0L)
                aggregated[usage.packageName] = current + usage.totalTimeInForeground
            }
        }

        // Filter out launcher, system UI, and minimal apps (< 30s)
        val filtered = aggregated.filter { (pkg, timeMs) ->
            timeMs >= 30_000L &&
            pkg != context.packageName &&
            pkg != "com.android.systemui" &&
            pkg != "android"
        }

        val totalScreenTimeMs = filtered.values.sum()

        val topApps = filtered.map { (pkg, timeMs) ->
            val label = try {
                val appInfo = pm.getApplicationInfo(pkg, 0)
                pm.getApplicationLabel(appInfo).toString()
            } catch (e: Exception) {
                pkg.substringAfterLast(".")
            }
            AppUsageItem(
                packageName = pkg,
                appName = label,
                totalTimeInForegroundMs = timeMs
            )
        }.sortedByDescending { it.totalTimeInForegroundMs }

        return DailyUsageSummary(totalScreenTimeMs, topApps)
    }
}
