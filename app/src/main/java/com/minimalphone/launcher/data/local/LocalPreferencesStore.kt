package com.minimalphone.launcher.data.local

import android.content.Context
import android.content.SharedPreferences

class LocalPreferencesStore(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("minimal_phone_prefs_v2", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_CREDITS = "focus_credits"
        private const val KEY_FAVORITES = "fav_apps"
        private const val KEY_HIDDEN = "hidden_apps"
        private const val KEY_DISTRACTIONS = "distraction_apps"
        private const val KEY_TASK_IDS = "task_id_set"
        private const val PREFIX_TASK_TITLE = "task_t_"
        private const val PREFIX_TASK_DONE = "task_d_"
        private const val PREFIX_TASK_POINTS = "task_p_"
    }

    var credits: Int
        get() = prefs.getInt(KEY_CREDITS, 30)
        set(value) = prefs.edit().putInt(KEY_CREDITS, value.coerceAtLeast(0)).apply()

    fun getFavoritePackageNames(): Set<String> =
        prefs.getStringSet(KEY_FAVORITES, emptySet()) ?: emptySet()

    fun setFavoritePackageNames(packages: Set<String>) {
        prefs.edit().putStringSet(KEY_FAVORITES, packages).apply()
    }

    fun getHiddenPackageNames(): Set<String> =
        prefs.getStringSet(KEY_HIDDEN, emptySet()) ?: emptySet()

    fun setHiddenPackageNames(packages: Set<String>) {
        prefs.edit().putStringSet(KEY_HIDDEN, packages).apply()
    }

    fun getDistractionPackageNames(): Set<String> =
        prefs.getStringSet(KEY_DISTRACTIONS, emptySet()) ?: emptySet()

    fun setDistractionPackageNames(packages: Set<String>) {
        prefs.edit().putStringSet(KEY_DISTRACTIONS, packages).apply()
    }

    // Task local store
    fun getTaskIds(): Set<String> =
        prefs.getStringSet(KEY_TASK_IDS, emptySet()) ?: emptySet()

    fun saveTaskRaw(id: Long, title: String, isDone: Boolean, points: Int) {
        val ids = getTaskIds().toMutableSet()
        ids.add(id.toString())
        prefs.edit()
            .putStringSet(KEY_TASK_IDS, ids)
            .putString("$PREFIX_TASK_TITLE$id", title)
            .putBoolean("$PREFIX_TASK_DONE$id", isDone)
            .putInt("$PREFIX_TASK_POINTS$id", points)
            .apply()
    }

    fun updateTaskDoneRaw(id: Long, isDone: Boolean) {
        prefs.edit().putBoolean("$PREFIX_TASK_DONE$id", isDone).apply()
    }

    fun removeTaskRaw(id: Long) {
        val ids = getTaskIds().toMutableSet()
        ids.remove(id.toString())
        prefs.edit()
            .putStringSet(KEY_TASK_IDS, ids)
            .remove("$PREFIX_TASK_TITLE$id")
            .remove("$PREFIX_TASK_DONE$id")
            .remove("$PREFIX_TASK_POINTS$id")
            .apply()
    }

    fun getTaskTitle(id: Long): String? = prefs.getString("$PREFIX_TASK_TITLE$id", null)
    fun getTaskDone(id: Long): Boolean = prefs.getBoolean("$PREFIX_TASK_DONE$id", false)
    fun getTaskPoints(id: Long): Int = prefs.getInt("$PREFIX_TASK_POINTS$id", 15)
}
