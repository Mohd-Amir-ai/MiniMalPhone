package com.minimalphone.launcher.domain.friction.interventions

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.minimalphone.launcher.domain.friction.FrictionIntervention
import com.minimalphone.launcher.domain.friction.FrictionType
import com.minimalphone.launcher.theme.AccentBorder
import com.minimalphone.launcher.theme.Black
import com.minimalphone.launcher.theme.ChalkWhite
import com.minimalphone.launcher.theme.LightGray
import com.minimalphone.launcher.theme.MidGray
import com.minimalphone.launcher.theme.PureWhite

class IntentionPromptIntervention(
    override val defaultCost: Int = 10
) : FrictionIntervention {

    override val type: FrictionType = FrictionType.INTENTION_PROMPT

    @Composable
    override fun RenderUI(
        appName: String,
        creditCost: Int,
        userCredits: Int,
        onProceed: () -> Unit,
        onCancel: () -> Unit
    ) {
        var intentionText by remember { mutableStateOf("") }
        val isIntentionValid = intentionText.trim().length >= 4 && userCredits >= creditCost

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Black)
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "CONSCIOUS INTENTION",
                    style = MaterialTheme.typography.labelSmall,
                    letterSpacing = 4.sp,
                    color = LightGray
                )

                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    text = "Why do you need to open \"$appName\" right now?",
                    style = MaterialTheme.typography.titleLarge,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Light,
                    color = ChalkWhite
                )

                Spacer(modifier = Modifier.height(24.dp))

                OutlinedTextField(
                    value = intentionText,
                    onValueChange = { intentionText = it },
                    placeholder = { Text("e.g. Replying to urgent message from John", color = MidGray) },
                    singleLine = false,
                    maxLines = 3,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = ChalkWhite,
                        unfocusedTextColor = ChalkWhite,
                        focusedBorderColor = PureWhite,
                        unfocusedBorderColor = AccentBorder
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Cost: $creditCost Focus Credits  •  Balance: $userCredits pts",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = LightGray
                )

                Spacer(modifier = Modifier.height(36.dp))

                OutlinedButton(
                    onClick = onCancel,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, PureWhite)
                ) {
                    Text(
                        text = "I don't need this (Cancel)",
                        color = ChalkWhite,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onProceed,
                    enabled = isIntentionValid,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PureWhite,
                        disabledContainerColor = PureWhite.copy(alpha = 0.15f),
                        contentColor = Black,
                        disabledContentColor = LightGray
                    )
                ) {
                    Text(
                        text = if (intentionText.trim().length < 4) {
                            "State Intention First"
                        } else if (userCredits < creditCost) {
                            "Not Enough Credits"
                        } else {
                            "Proceed Mindfully (-$creditCost pts)"
                        },
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }
}
