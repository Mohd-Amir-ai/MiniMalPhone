package com.minimalphone.launcher.model

data class AppInfo(
    val label: String,
    val packageName: String,
    val isFavorite: Boolean = false,
    val isHidden: Boolean = false,
    val isDistraction: Boolean = false
)
