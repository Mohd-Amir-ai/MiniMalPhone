package com.minimalphone.launcher.data.apps

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import android.provider.Telephony
import com.minimalphone.launcher.core.crash.CrashReporter
import com.minimalphone.launcher.data.local.LocalPreferencesStore
import com.minimalphone.launcher.domain.apps.AppModel
import com.minimalphone.launcher.domain.apps.AppRepository
import com.minimalphone.launcher.domain.apps.EssentialAppType
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

    override fun launchEssentialApp(type: EssentialAppType): Boolean {
        return try {
            val intent = when (type) {
                EssentialAppType.PHONE -> Intent(Intent.ACTION_DIAL)
                EssentialAppType.SEARCH -> Intent(Intent.ACTION_WEB_SEARCH).apply {
                    putExtra(SearchManager.QUERY, "")
                }
                EssentialAppType.MESSAGES -> {
                    val defaultSms = Telephony.Sms.getDefaultSmsPackage(context)
                    if (defaultSms != null) {
                        context.packageManager.getLaunchIntentForPackage(defaultSms) ?: Intent(Intent.ACTION_MAIN).apply {
                            addCategory(Intent.CATEGORY_APP_MESSAGING)
                        }
                    } else {
                        Intent(Intent.ACTION_MAIN).apply {
                            addCategory(Intent.CATEGORY_APP_MESSAGING)
                        }
                    }
                }
                EssentialAppType.CAMERA -> Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA)
            }
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
            crashReporter.logBreadcrumb("AppLaunch", "Launched essential app: $type")
            true
        } catch (e: Exception) {
            if (type == EssentialAppType.SEARCH) {
                try {
                    val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com"))
                    browserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(browserIntent)
                    return true
                } catch (ignored: Exception) {}
            }
            crashReporter.logException(e, "Failed to launch essential app: $type")
            false
        }
    }
}
