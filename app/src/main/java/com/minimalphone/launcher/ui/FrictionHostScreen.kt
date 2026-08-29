package com.minimalphone.launcher.ui

import androidx.compose.runtime.Composable
import com.minimalphone.launcher.domain.friction.FrictionIntervention

/**
 * Host container for any pluggable FrictionIntervention.
 */
@Composable
fun FrictionHostScreen(
    intervention: FrictionIntervention,
    appName: String,
    creditCost: Int = intervention.defaultCost,
    userCredits: Int,
    onProceed: () -> Unit,
    onCancel: () -> Unit
) {
    intervention.RenderUI(
        appName = appName,
        creditCost = creditCost,
        userCredits = userCredits,
        onProceed = onProceed,
        onCancel = onCancel
    )
}
