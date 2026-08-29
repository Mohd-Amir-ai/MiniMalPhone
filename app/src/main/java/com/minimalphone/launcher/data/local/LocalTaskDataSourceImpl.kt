package com.minimalphone.launcher.data.local

import com.minimalphone.launcher.domain.productivity.TaskDataSource
import com.minimalphone.launcher.domain.productivity.TaskDifficulty
import com.minimalphone.launcher.domain.productivity.TaskItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LocalTaskDataSourceImpl(
    private val store: LocalPreferencesStore
) : TaskDataSource {

    override suspend fun getTasks(): List<TaskItem> = withContext(Dispatchers.IO) {
        val ids = store.getTaskIds()
        val list = mutableListOf<TaskItem>()

        for (idStr in ids) {
            val id = idStr.toLongOrNull() ?: continue
            val title = store.getTaskTitle(id) ?: continue
            val done = store.getTaskDone(id)
            val diffStr = store.getTaskDifficulty(id)
            val difficulty = try { TaskDifficulty.valueOf(diffStr) } catch (e: Exception) { TaskDifficulty.MEDIUM }
            val time = store.getTaskTime(id)
            val date = store.getTaskDate(id)
            val order = store.getTaskOrder(id)

            list.add(
                TaskItem(
                    id = id,
                    title = title,
                    difficulty = difficulty,
                    scheduledTime = time,
                    scheduledDate = date,
                    isCompleted = done,
                    orderIndex = order
                )
            )
        }

        // Seed initial example tasks if brand new
        if (list.isEmpty()) {
            val task1 = TaskItem(
                id = 1001L,
                title = "Make a post on X",
                difficulty = TaskDifficulty.MEDIUM,
                scheduledTime = "2:30 PM",
                scheduledDate = "Today",
                orderIndex = 0
            )
            val task2 = TaskItem(
                id = 1002L,
                title = "Study book",
                difficulty = TaskDifficulty.HARD,
                scheduledTime = "3:00 PM",
                scheduledDate = "Today",
                orderIndex = 1
            )
            saveTask(task1)
            saveTask(task2)
            return@withContext listOf(task1, task2)
        }

        list.sortedBy { it.orderIndex }
    }

    override suspend fun saveTask(task: TaskItem): TaskItem = withContext(Dispatchers.IO) {
        store.saveTaskRaw(
            id = task.id,
            title = task.title,
            isDone = task.isCompleted,
            difficulty = task.difficulty.name,
            time = task.scheduledTime,
            date = task.scheduledDate,
            order = task.orderIndex
        )
        task
    }

    override suspend fun updateTaskStatus(taskId: Long, isCompleted: Boolean) = withContext(Dispatchers.IO) {
        store.updateTaskDoneRaw(taskId, isCompleted)
    }

    override suspend fun swapTaskTimingsAndPositions(taskIdA: Long, taskIdB: Long) = withContext(Dispatchers.IO) {
        val timeA = store.getTaskTime(taskIdA)
        val orderA = store.getTaskOrder(taskIdA)

        val timeB = store.getTaskTime(taskIdB)
        val orderB = store.getTaskOrder(taskIdB)

        // Swap: A gets B's time and order; B gets A's time and order!
        store.updateTaskTimeAndOrderRaw(taskIdA, newTime = timeB, newOrder = orderB)
        store.updateTaskTimeAndOrderRaw(taskIdB, newTime = timeA, newOrder = orderA)
    }

    override suspend fun deleteTask(taskId: Long) = withContext(Dispatchers.IO) {
        store.removeTaskRaw(taskId)
    }
}
