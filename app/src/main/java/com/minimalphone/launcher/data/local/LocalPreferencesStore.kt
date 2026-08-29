package com.minimalphone.launcher.data.local

import android.content.Context
import android.content.SharedPreferences

class LocalPreferencesStore(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("minimal_phone_prefs_v3", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_CREDITS = "focus_credits"
        private const val KEY_FAVORITES = "fav_apps"
        private const val KEY_HIDDEN = "hidden_apps"
        private const val KEY_DISTRACTIONS = "distraction_apps"
        private const val KEY_TASK_IDS = "task_id_set"
        private const val PREFIX_TASK_TITLE = "task_t_"
        private const val PREFIX_TASK_DONE = "task_d_"
        private const val PREFIX_TASK_DIFF = "task_diff_"
        private const val PREFIX_TASK_TIME = "task_time_"
        private const val PREFIX_TASK_DATE = "task_date_"
        private const val PREFIX_TASK_ORDER = "task_order_"
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

    fun saveTaskRaw(
        id: Long,
        title: String,
        isDone: Boolean,
        difficulty: String,
        time: String,
        date: String,
        order: Int
    ) {
        val ids = getTaskIds().toMutableSet()
        ids.add(id.toString())
        prefs.edit()
            .putStringSet(KEY_TASK_IDS, ids)
            .putString("$PREFIX_TASK_TITLE$id", title)
            .putBoolean("$PREFIX_TASK_DONE$id", isDone)
            .putString("$PREFIX_TASK_DIFF$id", difficulty)
            .putString("$PREFIX_TASK_TIME$id", time)
            .putString("$PREFIX_TASK_DATE$id", date)
            .putInt("$PREFIX_TASK_ORDER$id", order)
            .apply()
    }

    fun updateTaskDoneRaw(id: Long, isDone: Boolean) {
        prefs.edit().putBoolean("$PREFIX_TASK_DONE$id", isDone).apply()
    }

    fun updateTaskTimeAndOrderRaw(id: Long, newTime: String, newOrder: Int) {
        prefs.edit()
            .putString("$PREFIX_TASK_TIME$id", newTime)
            .putInt("$PREFIX_TASK_ORDER$id", newOrder)
            .apply()
    }

    fun removeTaskRaw(id: Long) {
        val ids = getTaskIds().toMutableSet()
        ids.remove(id.toString())
        prefs.edit()
            .putStringSet(KEY_TASK_IDS, ids)
            .remove("$PREFIX_TASK_TITLE$id")
            .remove("$PREFIX_TASK_DONE$id")
            .remove("$PREFIX_TASK_DIFF$id")
            .remove("$PREFIX_TASK_TIME$id")
            .remove("$PREFIX_TASK_DATE$id")
            .remove("$PREFIX_TASK_ORDER$id")
            .apply()
    }

    fun getTaskTitle(id: Long): String? = prefs.getString("$PREFIX_TASK_TITLE$id", null)
    fun getTaskDone(id: Long): Boolean = prefs.getBoolean("$PREFIX_TASK_DONE$id", false)
    fun getTaskDifficulty(id: Long): String = prefs.getString("$PREFIX_TASK_DIFF$id", "MEDIUM") ?: "MEDIUM"
    fun getTaskTime(id: Long): String = prefs.getString("$PREFIX_TASK_TIME$id", "12:00 PM") ?: "12:00 PM"
    fun getTaskDate(id: Long): String = prefs.getString("$PREFIX_TASK_DATE$id", "Today") ?: "Today"
    fun getTaskOrder(id: Long): Int = prefs.getInt("$PREFIX_TASK_ORDER$id", 0)

    // Weather caching & Location preferences
    fun getCachedWeatherJson(): String? = prefs.getString("weather_cached_json", null)
    fun setCachedWeatherJson(json: String) = prefs.edit().putString("weather_cached_json", json).apply()

    fun getWeatherCity(): String = prefs.getString("weather_city", "Nagpur") ?: "Nagpur"
    fun setWeatherCity(city: String) = prefs.edit().putString("weather_city", city).apply()

    fun getWeatherLat(): Double = prefs.getString("weather_lat", "21.1458")?.toDoubleOrNull() ?: 21.1458
    fun setWeatherLat(lat: Double) = prefs.edit().putString("weather_lat", lat.toString()).apply()

    fun getWeatherLon(): Double = prefs.getString("weather_lon", "79.0882")?.toDoubleOrNull() ?: 79.0882
    fun setWeatherLon(lon: Double) = prefs.edit().putString("weather_lon", lon.toString()).apply()
}
