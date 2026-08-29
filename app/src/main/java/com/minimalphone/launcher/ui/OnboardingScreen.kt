package com.minimalphone.launcher.ui

import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.minimalphone.launcher.core.system.MonochromeModeHelper
import com.minimalphone.launcher.core.wallpaper.WallpaperHelper
import com.minimalphone.launcher.data.local.LocalPreferencesStore
import com.minimalphone.launcher.domain.apps.AppModel
import com.minimalphone.launcher.theme.*

@Composable
fun OnboardingScreen(
    prefsStore: LocalPreferencesStore,
    apps: List<AppModel>,
    isDefaultLauncher: Boolean,
    onRequestSetDefaultLauncher: () -> Unit,
    onToggleDistraction: (AppModel) -> Unit,
    onCompleteOnboarding: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var currentStep by remember { mutableIntStateOf(0) }
    var isWallpaperApplied by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(PureBlack)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(26.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Step Progress Indicator
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                listOf(0, 1, 2).forEach { stepIndex ->
                    Box(
                        modifier = Modifier
                            .size(width = if (stepIndex == currentStep) 32.dp else 12.dp, height = 4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(if (stepIndex == currentStep) PureWhite else Color(0xFF2C2F36))
                    )
                    if (stepIndex < 2) Spacer(modifier = Modifier.width(8.dp))
                }
            }

            // Step Content
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(vertical = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                when (currentStep) {
                    0 -> OnboardingStepWelcome()
                    1 -> OnboardingStepPermissions(
                        isDefaultLauncher = isDefaultLauncher,
                        isWallpaperApplied = isWallpaperApplied,
                        onRequestDefault = onRequestSetDefaultLauncher,
                        onApplyWallpaper = {
                            Thread {
                                WallpaperHelper.applyDuneWallpaper(context)
                                isWallpaperApplied = true
                            }.start()
                            Toast.makeText(context, "Dunes Wallpaper Applied!", Toast.LENGTH_SHORT).show()
                        }
                    )
                    2 -> OnboardingStepDistractions(
                        apps = apps,
                        onToggleDistraction = onToggleDistraction
                    )
                }
            }

            // Bottom Navigation Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (currentStep > 0) {
                    TextButton(onClick = { currentStep -= 1 }) {
                        Text("← Back", color = LightGray, fontSize = 14.sp)
                    }
                } else {
                    Spacer(modifier = Modifier.width(60.dp))
                }

                Button(
                    onClick = {
                        if (currentStep < 2) {
                            currentStep += 1
                        } else {
                            // Finish Onboarding
                            prefsStore.isFirstLaunchCompleted = true
                            prefsStore.credits = 50
                            onCompleteOnboarding()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PureWhite,
                        contentColor = PureBlack
                    ),
                    modifier = Modifier
                        .height(48.dp)
                        .padding(horizontal = 8.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = if (currentStep < 2) "Continue →" else "Enter MiniMalPhone (+50 pts)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = PureBlack
                    )
                }
            }
        }
    }
}

@Composable
private fun OnboardingStepWelcome() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "MINIMALPHONE",
            style = MaterialTheme.typography.labelSmall,
            letterSpacing = 4.sp,
            color = MidGray
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Reclaim your mind.\nStop doom-scrolling.",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Light,
            textAlign = TextAlign.Center,
            color = PureWhite,
            lineHeight = 36.sp
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Your smartphone is engineered to capture your attention for profit. MiniMalPhone turns it back into a calm, intentional productivity tool.\n\n• Zero app icons or red badges\n• 5-second mindful breathing pause\n• Earn focus credits with real tasks",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = LightGray,
            lineHeight = 22.sp,
            modifier = Modifier.padding(horizontal = 8.dp)
        )
    }
}

@Composable
private fun OnboardingStepPermissions(
    isDefaultLauncher: Boolean,
    isWallpaperApplied: Boolean,
    onRequestDefault: () -> Unit,
    onApplyWallpaper: () -> Unit
) {
    val context = LocalContext.current
    val isGrayscaleGranted = remember { MonochromeModeHelper.isPermissionGranted(context) }

    Column(
        horizontalAlignment = Alignment.Start,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "DEVICE SETUP",
            style = MaterialTheme.typography.labelSmall,
            letterSpacing = 2.sp,
            color = MidGray
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Configure Your Focus Shell",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = PureWhite
        )

        Spacer(modifier = Modifier.height(22.dp))

        // 1. Default Home Role Card
        SetupActionCard(
            title = "Default Home Launcher",
            subtitle = if (isDefaultLauncher) "MiniMalPhone is your default home app" else "Tap to set as default launcher",
            isDone = isDefaultLauncher,
            buttonLabel = if (isDefaultLauncher) "Configured ✓" else "Set as Default →",
            onClick = onRequestDefault
        )

        Spacer(modifier = Modifier.height(14.dp))

        // 2. Wallpaper Card
        SetupActionCard(
            title = "Layered Dunes Wallpaper",
            subtitle = if (isWallpaperApplied) "Monochrome landscape applied to device" else "Set aesthetic procedural wallpaper",
            isDone = isWallpaperApplied,
            buttonLabel = if (isWallpaperApplied) "Applied ✓" else "Apply Wallpaper →",
            onClick = onApplyWallpaper
        )

        Spacer(modifier = Modifier.height(14.dp))

        // 3. Grayscale Status Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFF141518))
                .border(1.dp, Color(0xFF2A2D33), RoundedCornerShape(10.dp))
                .padding(14.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Hardware B/W Mode",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = PureWhite
                    )
                    Text(
                        text = if (isGrayscaleGranted) "ACTIVE" else "ADB REQUIRED",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isGrayscaleGranted) Color(0xFF7CE38B) else Color(0xFFFFB74D)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (isGrayscaleGranted) "All apps render in monochrome." else "Run 'manager.py' on PC once to grant secure settings.",
                    style = MaterialTheme.typography.bodySmall,
                    color = LightGray
                )
            }
        }
    }
}

@Composable
private fun SetupActionCard(
    title: String,
    subtitle: String,
    isDone: Boolean,
    buttonLabel: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFF141518))
            .border(1.dp, if (isDone) Color(0xFF4CAF50) else Color(0xFF2A2D33), RoundedCornerShape(10.dp))
            .padding(14.dp)
    ) {
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = PureWhite
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = LightGray
            )
            Spacer(modifier = Modifier.height(10.dp))
            Button(
                onClick = onClick,
                enabled = !isDone,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isDone) Color(0xFF253325) else PureWhite,
                    contentColor = if (isDone) Color(0xFF7CE38B) else PureBlack,
                    disabledContainerColor = Color(0xFF253325),
                    disabledContentColor = Color(0xFF7CE38B)
                ),
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = buttonLabel,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = if (isDone) Color(0xFF7CE38B) else PureBlack
                )
            }
        }
    }
}

@Composable
private fun OnboardingStepDistractions(
    apps: List<AppModel>,
    onToggleDistraction: (AppModel) -> Unit
) {
    val commonDistractionPackages = remember {
        setOf("instagram", "youtube", "twitter", "reddit", "facebook", "tiktok", "snapchat", "chrome")
    }

    val sortedApps = remember(apps) {
        apps.sortedWith(
            compareByDescending<AppModel> { app ->
                commonDistractionPackages.any { app.packageName.lowercase().contains(it) } || app.isDistraction
            }.thenBy { it.label.lowercase() }
        )
    }

    Column(
        horizontalAlignment = Alignment.Start,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "MINDFUL FRICTION",
            style = MaterialTheme.typography.labelSmall,
            letterSpacing = 2.sp,
            color = MidGray
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Select Distraction Traps",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = PureWhite
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Flag apps that impulsively steal your attention. Opening them will require a 5-second breathing pause.",
            style = MaterialTheme.typography.bodySmall,
            color = LightGray
        )

        Spacer(modifier = Modifier.height(14.dp))

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 340.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF141518))
                .border(1.dp, Color(0xFF25272D), RoundedCornerShape(8.dp))
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            items(sortedApps, key = { it.packageName }) { app ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onToggleDistraction(app) }
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = app.label,
                        fontSize = 14.sp,
                        color = if (app.isDistraction) PureWhite else LightGray,
                        fontWeight = if (app.isDistraction) FontWeight.SemiBold else FontWeight.Normal
                    )

                    Checkbox(
                        checked = app.isDistraction,
                        onCheckedChange = { onToggleDistraction(app) },
                        colors = CheckboxDefaults.colors(
                            checkedColor = PureWhite,
                            checkmarkColor = PureBlack,
                            uncheckedColor = MidGray
                        )
                    )
                }
            }
        }
    }
}
