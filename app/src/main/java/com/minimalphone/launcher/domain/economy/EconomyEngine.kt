package com.minimalphone.launcher.domain.economy

sealed class EconomyEvent {
    data class TaskCompleted(val taskId: Long, val rewardAmount: Int = 15) : EconomyEvent()
    data class TaskUncompleted(val taskId: Long, val rewardAmount: Int = 15) : EconomyEvent()
    data class AppFrictionBypassed(val packageName: String, val costAmount: Int = 10) : EconomyEvent()
    data class DeepWorkCompleted(val durationMinutes: Int, val rewardAmount: Int = 25) : EconomyEvent()
}

data class EconomyState(
    val balance: Int = 30,
    val totalEarned: Int = 0,
    val totalSpent: Int = 0
)

/**
 * Pluggable Economy & Reward Engine contract.
 * Decouples points calculation, streak logic, and dopamine reward policies from the UI.
 */
interface EconomyEngine {
    fun getCurrentBalance(): Int
    fun processEvent(event: EconomyEvent): Boolean
    fun canAfford(cost: Int): Boolean
}
