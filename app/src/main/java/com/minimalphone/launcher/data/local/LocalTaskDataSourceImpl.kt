package com.minimalphone.launcher.data.local

import com.minimalphone.launcher.domain.productivity.TaskDataSource
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
            val points = store.getTaskPoints(id)
            list.add(TaskItem(id = id, title = title, isCompleted = done, rewardPoints = points))
        }
        list.sortedBy { it.id }
    }

    override suspend fun addTask(title: String, rewardPoints: Int): TaskItem = withContext(Dispatchers.IO) {
        val newTask = TaskItem(
            id = System.currentTimeMillis(),
            title = title,
            isCompleted = false,
            rewardPoints = rewardPoints
        )
        store.saveTaskRaw(newTask.id, newTask.title, newTask.isCompleted, newTask.rewardPoints)
        newTask
    }

    override suspend fun updateTaskStatus(taskId: Long, isCompleted: Boolean) = withContext(Dispatchers.IO) {
        store.updateTaskDoneRaw(taskId, isCompleted)
    }

    override suspend fun deleteTask(taskId: Long) = withContext(Dispatchers.IO) {
        store.removeTaskRaw(taskId)
    }
}
