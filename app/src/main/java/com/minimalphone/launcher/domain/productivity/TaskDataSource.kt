package com.minimalphone.launcher.domain.productivity

data class TaskItem(
    val id: Long = System.currentTimeMillis(),
    val title: String,
    val isCompleted: Boolean = false,
    val rewardPoints: Int = 15
)

data class AgendaItem(
    val id: Long,
    val title: String,
    val startTimeMillis: Long,
    val endTimeMillis: Long,
    val isAllDay: Boolean = false
)

/**
 * Extensible Task Data Source contract.
 * Allows swapping between:
 * - Local offline storage
 * - Google Tasks / Todoist / TickTick API
 * - Local Obsidian / Logseq Markdown files
 */
interface TaskDataSource {
    suspend fun getTasks(): List<TaskItem>
    suspend fun addTask(title: String, rewardPoints: Int = 15): TaskItem
    suspend fun updateTaskStatus(taskId: Long, isCompleted: Boolean)
    suspend fun deleteTask(taskId: Long)
}
