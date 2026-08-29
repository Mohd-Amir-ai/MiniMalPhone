package com.minimalphone.launcher.model

data class TaskItem(
    val id: Long = System.currentTimeMillis(),
    val title: String,
    val isCompleted: Boolean = false,
    val creditReward: Int = 15
)
