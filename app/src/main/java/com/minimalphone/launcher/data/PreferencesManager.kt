package com.minimalphone.launcher.data

import android.content.Context
import android.content.SharedPreferences
import com.minimalphone.launcher.model.TaskItem

class PreferencesManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("minimal_phone_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_CREDITS = "focus_credits"
        private const val KEY_FAVORITES = "favorite_packages"
        private const val KEY_HIDDEN = "hidden_packages"
        private const val KEY_DISTRACTIONS = "distraction_packages"
        private const val KEY_TASKS_PREFIX = "task_"
        private const val KEY_TASK_IDS = "task_ids"
    }

    var focusCredits: Int
        get() = prefs.getInt(KEY_CREDITS, 30)
        set(value) = prefs.edit().putInt(KEY_CREDITS, value.coerceAtLeast(0)).apply()

    fun addCredits(amount: Int) {
        focusCredits += amount
    }

    fun spendCredits(amount: Int): Boolean {
        return if (focusCredits >= amount) {
            focusCredits -= amount
            true
        } else {
            false
        }
    }

    fun getFavorites(): Set<String> {
        return prefs.getStringSet(KEY_FAVORITES, emptySet()) ?: emptySet()
    }

    fun toggleFavorite(packageName: String): Boolean {
        val current = getFavorites().toMutableSet()
        val isFav = if (current.contains(packageName)) {
            current.remove(packageName)
            false
        } else {
            current.add(packageName)
            true
        }
        prefs.edit().putStringSet(KEY_FAVORITES, current).apply()
        return isFav
    }

    fun getHiddenApps(): Set<String> {
        return prefs.getStringSet(KEY_HIDDEN, emptySet()) ?: emptySet()
    }

    fun toggleHidden(packageName: String): Boolean {
        val current = getHiddenApps().toMutableSet()
        val isHidden = if (current.contains(packageName)) {
            current.remove(packageName)
            false
        } else {
            current.add(packageName)
            true
        }
        prefs.edit().putStringSet(KEY_HIDDEN, current).apply()
        return isHidden
    }

    fun getDistractionApps(): Set<String> {
        return prefs.getStringSet(KEY_DISTRACTIONS, emptySet()) ?: emptySet()
    }

    fun toggleDistraction(packageName: String): Boolean {
        val current = getDistractionApps().toMutableSet()
        val isDistraction = if (current.contains(packageName)) {
            current.remove(packageName)
            false
        } else {
            current.add(packageName)
            true
        }
        prefs.edit().putStringSet(KEY_DISTRACTIONS, current).apply()
        return isDistraction
    }

    fun getTasks(): List<TaskItem> {
        val ids = prefs.getStringSet(KEY_TASK_IDS, emptySet()) ?: emptySet()
        val tasks = mutableListOf<TaskItem>()
        for (idStr in ids) {
            val title = prefs.getString("${KEY_TASKS_PREFIX}${idStr}_title", null) ?: continue
            val isCompleted = prefs.getBoolean("${KEY_TASKS_PREFIX}${idStr}_done", false)
            val id = idStr.toLongOrNull() ?: continue
            tasks.add(TaskItem(id = id, title = title, isCompleted = isCompleted))
        }
        return tasks.sortedBy { it.id }
    }

    fun saveTask(task: TaskItem) {
        val ids = (prefs.getStringSet(KEY_TASK_IDS, emptySet()) ?: emptySet()).toMutableSet()
        val idStr = task.id.toString()
        ids.add(idStr)
        prefs.edit()
            .putStringSet(KEY_TASK_IDS, ids)
            .putString("${KEY_TASKS_PREFIX}${idStr}_title", task.title)
            .putBoolean("${KEY_TASKS_PREFIX}${idStr}_done", task.isCompleted)
            .apply()
    }

    fun updateTaskCompletion(taskId: Long, isCompleted: Boolean) {
        val idStr = taskId.toString()
        prefs.edit().putBoolean("${KEY_TASKS_PREFIX}${idStr}_done", isCompleted).apply()
    }

    fun deleteTask(taskId: Long) {
        val ids = (prefs.getStringSet(KEY_TASK_IDS, emptySet()) ?: emptySet()).toMutableSet()
        val idStr = taskId.toString()
        ids.remove(idStr)
        prefs.edit()
            .putStringSet(KEY_TASK_IDS, ids)
            .remove("${KEY_TASKS_PREFIX}${idStr}_title")
            .remove("${KEY_TASKS_PREFIX}${idStr}_done")
            .apply()
    }
}
