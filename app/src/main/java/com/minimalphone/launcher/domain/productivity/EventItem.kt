package com.minimalphone.launcher.domain.productivity

/**
 * Represents a scheduled daily event or milestone.
 */
data class EventItem(
    val id: Long = System.currentTimeMillis(),
    val title: String,
    val timeFormatted: String,
    val isCompleted: Boolean = false
)
