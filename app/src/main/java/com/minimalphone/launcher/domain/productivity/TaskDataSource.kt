package com.minimalphone.launcher.domain.productivity

enum class TaskDifficulty(val label: String, val points: Int) {
    EASY("Easy", 10),
    MEDIUM("Medium", 20),
    HARD("Hard", 35),
    SUPER_HARD("Super Hard", 50)
}

data class TaskItem(
    val id: Long = System.currentTimeMillis(),
    val title: String,
    val difficulty: TaskDifficulty = TaskDifficulty.MEDIUM,
    val scheduledTime: String = "12:00 PM",
    val scheduledDate: String = "Today",
    val isCompleted: Boolean = false,
    val orderIndex: Int = 0
) {
    val rewardPoints: Int get() = difficulty.points
}

data class AgendaItem(
    val id: Long,
    val title: String,
    val startTimeMillis: Long,
    val endTimeMillis: Long,
    val isAllDay: Boolean = false
)

/**
 * Extensible Task & Schedule Data Source contract.
 */
interface TaskDataSource {
    suspend fun getTasks(): List<TaskItem>
    suspend fun saveTask(task: TaskItem): TaskItem
    suspend fun updateTaskStatus(taskId: Long, isCompleted: Boolean)
    suspend fun swapTaskTimingsAndPositions(taskIdA: Long, taskIdB: Long)
    suspend fun deleteTask(taskId: Long)
}
