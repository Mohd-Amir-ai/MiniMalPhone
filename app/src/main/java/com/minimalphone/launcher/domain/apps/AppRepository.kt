package com.minimalphone.launcher.domain.apps

data class AppModel(
    val label: String,
    val packageName: String,
    val isFavorite: Boolean = false,
    val isHidden: Boolean = false,
    val isDistraction: Boolean = false
)

enum class EssentialAppType {
    PHONE,
    SEARCH,
    MESSAGES,
    CAMERA
}

/**
 * Extensible App Repository contract.
 */
interface AppRepository {
    suspend fun getInstalledApps(): List<AppModel>
    suspend fun toggleFavorite(packageName: String): Boolean
    suspend fun toggleDistraction(packageName: String): Boolean
    suspend fun toggleHidden(packageName: String): Boolean
    fun launchApp(packageName: String): Boolean
    fun launchEssentialApp(type: EssentialAppType): Boolean
}
