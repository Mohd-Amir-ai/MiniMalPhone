package com.minimalphone.launcher.data.economy

import com.minimalphone.launcher.core.crash.CrashReporter
import com.minimalphone.launcher.data.local.LocalPreferencesStore
import com.minimalphone.launcher.domain.economy.EconomyEngine
import com.minimalphone.launcher.domain.economy.EconomyEvent

class EconomyEngineImpl(
    private val store: LocalPreferencesStore,
    private val crashReporter: CrashReporter
) : EconomyEngine {

    override fun getCurrentBalance(): Int = store.credits

    override fun canAfford(cost: Int): Boolean = store.credits >= cost

    override fun processEvent(event: EconomyEvent): Boolean {
        return when (event) {
            is EconomyEvent.TaskCompleted -> {
                store.credits += event.rewardAmount
                crashReporter.logBreadcrumb("Economy", "Task completed (+${event.rewardAmount} pts). Balance: ${store.credits}")
                true
            }
            is EconomyEvent.TaskUncompleted -> {
                store.credits = (store.credits - event.rewardAmount).coerceAtLeast(0)
                crashReporter.logBreadcrumb("Economy", "Task uncompleted (-${event.rewardAmount} pts). Balance: ${store.credits}")
                true
            }
            is EconomyEvent.AppFrictionBypassed -> {
                if (canAfford(event.costAmount)) {
                    store.credits -= event.costAmount
                    crashReporter.logBreadcrumb("Economy", "Friction bypassed for ${event.packageName} (-${event.costAmount} pts). Balance: ${store.credits}")
                    true
                } else {
                    crashReporter.logBreadcrumb("Economy", "Friction bypass denied (insufficient credits). Balance: ${store.credits}")
                    false
                }
            }
            is EconomyEvent.DeepWorkCompleted -> {
                store.credits += event.rewardAmount
                crashReporter.logBreadcrumb("Economy", "Deep work session (${event.durationMinutes}m) (+${event.rewardAmount} pts). Balance: ${store.credits}")
                true
            }
        }
    }
}
