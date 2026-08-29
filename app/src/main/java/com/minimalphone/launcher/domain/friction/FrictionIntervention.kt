package com.minimalphone.launcher.domain.friction

import androidx.compose.runtime.Composable

enum class FrictionType(val id: String, val title: String) {
    BREATHING("breathing", "Mindful Breathing (5s)"),
    INTENTION_PROMPT("intention", "Conscious Intention Prompt"),
    TIME_BUDGET("budget", "Strict Time Budget")
}

/**
 * Pluggable Anti-Doomscroll Intervention Contract.
 * Any developer can create a new intervention (e.g. mental math, quote typing, physical pushups)
 * by implementing this interface.
 */
interface FrictionIntervention {
    val type: FrictionType
    val defaultCost: Int

    @Composable
    fun RenderUI(
        appName: String,
        creditCost: Int,
        userCredits: Int,
        onProceed: () -> Unit,
        onCancel: () -> Unit
    )
}
