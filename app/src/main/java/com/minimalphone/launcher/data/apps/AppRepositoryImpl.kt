package com.minimalphone.launcher.data.apps

import android.content.Context
import android.content.Intent
import com.minimalphone.launcher.core.crash.CrashReporter
import com.minimalphone.launcher.data.local.LocalPreferencesStore
import com.minimalphone.launcher.domain.apps.AppModel
import com.minimalphone.launcher.domain.apps.AppRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AppRepositoryImpl(
    private val context: Context,
    private val store: LocalPreferencesStore,
    private val crashReporter: CrashReporter
) : AppRepository {

    override suspend fun getInstalledApps(): List<AppModel> = withContext(Dispatchers.IO) {
        val pm = context.packageManager
        val intent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        val resolveInfos = pm.queryIntentActivities(intent, 0)
        val favorites = store.getFavoritePackageNames()
        val hidden = store.getHiddenPackageNames()
        val distractions = store.getDistractionPackageNames()

        val apps = resolveInfos
            .filter { it.activityInfo.packageName != context.packageName }
            .map { resolveInfo ->
                val pkg = resolveInfo.activityInfo.packageName
                val label = resolveInfo.loadLabel(pm).toString()
                AppModel(
                    label = label,
                    packageName = pkg,
                    isFavorite = favorites.contains(pkg),
                    isHidden = hidden.contains(pkg),
                    isDistraction = distractions.contains(pkg)
                )
            }
            .sortedBy { it.label.lowercase() }

        // Seed default favorites if first run
        if (favorites.isEmpty() && apps.isNotEmpty()) {
            val defaults = apps.filter {
                val lower = it.label.lowercase()
                lower.contains("phone") || lower.contains("message") ||
                    lower.contains("camera") || lower.contains("browser") || lower.contains("chrome")
            }.take(4).map { it.packageName }.toSet()
            store.setFavoritePackageNames(defaults)
            return@withContext apps.map { it.copy(isFavorite = defaults.contains(it.packageName)) }
        }

        apps
    }

    override suspend fun toggleFavorite(packageName: String): Boolean = withContext(Dispatchers.IO) {
        val current = store.getFavoritePackageNames().toMutableSet()
        val isFav = if (current.contains(packageName)) {
            current.remove(packageName)
            false
        } else {
            current.add(packageName)
            true
        }
        store.setFavoritePackageNames(current)
        isFav
    }

    override suspend fun toggleDistraction(packageName: String): Boolean = withContext(Dispatchers.IO) {
        val current = store.getDistractionPackageNames().toMutableSet()
        val isDistraction = if (current.contains(packageName)) {
            current.remove(packageName)
            false
        } else {
            current.add(packageName)
            true
        }
        store.setDistractionPackageNames(current)
        isDistraction
    }

    override suspend fun toggleHidden(packageName: String): Boolean = withContext(Dispatchers.IO) {
        val current = store.getHiddenPackageNames().toMutableSet()
        val isHidden = if (current.contains(packageName)) {
            current.remove(packageName)
            false
        } else {
            current.add(packageName)
            true
        }
        store.setHiddenPackageNames(current)
        isHidden
    }

    override fun launchApp(packageName: String): Boolean {
        return try {
            val launchIntent = context.packageManager.getLaunchIntentForPackage(packageName)
            if (launchIntent != null) {
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(launchIntent)
                crashReporter.logBreadcrumb("AppLaunch", "Successfully launched $packageName")
                true
            } else {
                crashReporter.logBreadcrumb("AppLaunch", "No launch intent found for $packageName")
                false
            }
        } catch (e: Exception) {
            crashReporter.logException(e, "Failed to launch $packageName")
            false
        }
    }
}
