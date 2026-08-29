package com.minimalphone.launcher.ui

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.minimalphone.launcher.theme.Black
import com.minimalphone.launcher.theme.ChalkWhite
import com.minimalphone.launcher.theme.LightGray
import com.minimalphone.launcher.theme.PureWhite
import kotlinx.coroutines.delay

@Composable
fun FrictionScreen(
    appName: String,
    creditCost: Int = 10,
    userCredits: Int,
    onProceed: () -> Unit,
    onCancel: () -> Unit
) {
    var secondsLeft by remember { mutableIntStateOf(5) }

    LaunchedEffect(Unit) {
        while (secondsLeft > 0) {
            delay(1000L)
            secondsLeft -= 1
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "breathing")
    val breathScale by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breathScale"
    )

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
                text = "MINDFUL PAUSE",
                style = MaterialTheme.typography.labelSmall,
                letterSpacing = 4.sp,
                color = LightGray
            )

            Spacer(modifier = Modifier.height(36.dp))

            // Animated Breathing Circle
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .scale(breathScale)
                    .border(2.dp, PureWhite.copy(alpha = 0.8f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (secondsLeft > 0) "${secondsLeft}s" else "Breathe",
                    style = MaterialTheme.typography.titleLarge,
                    color = ChalkWhite
                )
            }

            Spacer(modifier = Modifier.height(48.dp))

            Text(
                text = "Are you opening \"$appName\" with intention?",
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Light,
                color = ChalkWhite
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Cost: $creditCost Focus Credits  •  Your Balance: $userCredits pts",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = LightGray
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Action Buttons
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, PureWhite)
            ) {
                Text(
                    text = "Stay Focused (Cancel)",
                    color = ChalkWhite,
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            val canProceed = secondsLeft == 0 && userCredits >= creditCost
            Button(
                onClick = onProceed,
                enabled = canProceed,
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
                    text = if (secondsLeft > 0) {
                        "Reflect ($secondsLeft)"
                    } else if (userCredits < creditCost) {
                        "Not Enough Credits"
                    } else {
                        "Open Anyway (-$creditCost pts)"
                    },
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}
