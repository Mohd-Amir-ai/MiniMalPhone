package com.minimalphone.launcher.ui

import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.minimalphone.launcher.core.system.MonochromeModeHelper
import com.minimalphone.launcher.core.wallpaper.WallpaperHelper
import com.minimalphone.launcher.data.local.LocalPreferencesStore
import com.minimalphone.launcher.domain.apps.AppModel
import com.minimalphone.launcher.domain.economy.EconomyEngine
import com.minimalphone.launcher.theme.*

@Composable
fun SettingsScreen(
    prefsStore: LocalPreferencesStore,
    economyEngine: EconomyEngine,
    apps: List<AppModel>,
    onToggleDistractionApp: (AppModel) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    var isMonochrome by remember { mutableStateOf(prefsStore.isMonochromeEnabled) }
    var isCustomLockScreen by remember { mutableStateOf(prefsStore.isCustomLockScreenEnabled) }
    var is24Hour by remember { mutableStateOf(prefsStore.is24HourFormat) }
    var showLockEvents by remember { mutableStateOf(prefsStore.showLockScreenEvents) }
    var showLockStatus by remember { mutableStateOf(prefsStore.showLockScreenStatus) }
    var isPitchBlack by remember { mutableStateOf(prefsStore.isPitchBlackWallpaper) }
    var frictionSeconds by remember { mutableIntStateOf(prefsStore.frictionCountdownSeconds) }
    var frictionCost by remember { mutableIntStateOf(prefsStore.frictionCost) }
    var currentBalance by remember { mutableIntStateOf(prefsStore.credits) }

    var isAdbGranted by remember { mutableStateOf(MonochromeModeHelper.isPermissionGranted(context)) }
    var showDistractionModal by remember { mutableStateOf(false) }

    // Re-check ADB permission on render
    LaunchedEffect(Unit) {
        isAdbGranted = MonochromeModeHelper.isPermissionGranted(context)
    }

    fun handleRestoreAndExit() {
        // 1. Immediately disable hardware grayscale Daltonizer
        MonochromeModeHelper.disableMonochrome(context)
        prefsStore.isMonochromeEnabled = false
        isMonochrome = false

        // 2. Disable custom lockscreen interceptor
        prefsStore.isCustomLockScreenEnabled = false
        isCustomLockScreen = false

        Toast.makeText(context, "Colors restored! Select your default home app.", Toast.LENGTH_LONG).show()

        // 3. Open Android Default Home settings
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val intent = Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS)
                context.startActivity(intent)
            } else {
                val intent = Intent(Settings.ACTION_HOME_SETTINGS)
                context.startActivity(intent)
            }
        } catch (e: Exception) {
            try {
                val intent = Intent(Settings.ACTION_SETTINGS)
                context.startActivity(intent)
            } catch (ignored: Exception) {}
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(PureBlack)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 22.dp, vertical = 12.dp)
        ) {
            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = onNavigateBack, modifier = Modifier.size(36.dp)) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back to Home",
                        tint = PureWhite
                    )
                }

                Text(
                    text = "SETTINGS",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp,
                    color = PureWhite
                )

                Spacer(modifier = Modifier.size(36.dp))
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // 1. 🚨 EMERGENCY & EXIT
                item {
                    SettingsSectionCard(title = "🚨 EMERGENCY & EXIT") {
                        Text(
                            text = "Want to turn off MiniMalPhone and return your phone to its normal original state?",
                            style = MaterialTheme.typography.bodySmall,
                            color = LightGray
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = { handleRestoreAndExit() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF2A1515),
                                contentColor = Color(0xFFFF6B6B)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .border(1.dp, Color(0xFF6B2222), RoundedCornerShape(8.dp)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "Turn Off MiniMalPhone & Restore Defaults",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Color(0xFFFF6B6B)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        TextButton(
                            onClick = {
                                try {
                                    val intent = Intent(Intent.ACTION_SET_WALLPAPER)
                                    context.startActivity(Intent.createChooser(intent, "Select Wallpaper"))
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Open Settings > Wallpaper to restore image", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Open Samsung Wallpaper Picker →",
                                color = LightGray,
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                // 2. 🎨 DISPLAY & AESTHETICS
                item {
                    SettingsSectionCard(title = "🎨 DISPLAY & AESTHETICS") {
                        // Hardware Monochrome Switch
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "System-Wide Grayscale",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = PureWhite
                                )
                                Text(
                                    text = if (isAdbGranted) "Active • All apps render in true B/W" else "Requires WRITE_SECURE_SETTINGS via ADB",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isAdbGranted) Color(0xFF7CE38B) else Color(0xFFFFB74D)
                                )
                            }

                            Switch(
                                checked = isMonochrome,
                                onCheckedChange = { checked ->
                                    isMonochrome = checked
                                    prefsStore.isMonochromeEnabled = checked
                                    if (checked) {
                                        MonochromeModeHelper.enableMonochrome(context)
                                    } else {
                                        MonochromeModeHelper.disableMonochrome(context)
                                    }
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = PureBlack,
                                    checkedTrackColor = PureWhite,
                                    uncheckedThumbColor = LightGray,
                                    uncheckedTrackColor = Color(0xFF222428)
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Wallpaper Selector
                        Text(
                            text = "WALLPAPER THEME",
                            style = MaterialTheme.typography.labelSmall,
                            letterSpacing = 1.sp,
                            color = MidGray
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OptionChip(
                                label = "Layered Dunes",
                                isSelected = !isPitchBlack,
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    isPitchBlack = false
                                    prefsStore.isPitchBlackWallpaper = false
                                    Thread {
                                        WallpaperHelper.applyDuneWallpaper(context)
                                    }.start()
                                    Toast.makeText(context, "Dunes wallpaper applied!", Toast.LENGTH_SHORT).show()
                                }
                            )

                            OptionChip(
                                label = "Pure OLED Black",
                                isSelected = isPitchBlack,
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    isPitchBlack = true
                                    prefsStore.isPitchBlackWallpaper = true
                                    Thread {
                                        WallpaperHelper.applyPitchBlackWallpaper(context)
                                    }.start()
                                    Toast.makeText(context, "Pure Black wallpaper applied!", Toast.LENGTH_SHORT).show()
                                }
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // 12h vs 24h Clock
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "24-Hour Time Format",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = PureWhite
                                )
                                Text(
                                    text = if (is24Hour) "16:42" else "4:42 PM",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = LightGray
                                )
                            }

                            Switch(
                                checked = is24Hour,
                                onCheckedChange = {
                                    is24Hour = it
                                    prefsStore.is24HourFormat = it
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = PureBlack,
                                    checkedTrackColor = PureWhite,
                                    uncheckedThumbColor = LightGray,
                                    uncheckedTrackColor = Color(0xFF222428)
                                )
                            )
                        }
                    }
                }

                // 3. 🔒 LOCK SCREEN
                item {
                    SettingsSectionCard(title = "🔒 MINIMAL LOCK SCREEN") {
                        // Toggle Custom Lock Screen
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Enable Minimal Lock Screen",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = PureWhite
                                )
                                Text(
                                    text = "Displays matching stacked clock & schedules over keyguard",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = LightGray
                                )
                            }

                            Switch(
                                checked = isCustomLockScreen,
                                onCheckedChange = {
                                    isCustomLockScreen = it
                                    prefsStore.isCustomLockScreenEnabled = it
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = PureBlack,
                                    checkedTrackColor = PureWhite,
                                    uncheckedThumbColor = LightGray,
                                    uncheckedTrackColor = Color(0xFF222428)
                                )
                            )
                        }

                        if (isCustomLockScreen) {
                            Spacer(modifier = Modifier.height(12.dp))

                            // Show 30m events toggle
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Show Upcoming Schedules (30m)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = LightGray
                                )
                                Switch(
                                    checked = showLockEvents,
                                    onCheckedChange = {
                                        showLockEvents = it
                                        prefsStore.showLockScreenEvents = it
                                    },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = PureBlack,
                                        checkedTrackColor = PureWhite,
                                        uncheckedThumbColor = LightGray,
                                        uncheckedTrackColor = Color(0xFF222428)
                                    )
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Show status icons toggle
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Show Signal & Battery Indicators",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = LightGray
                                )
                                Switch(
                                    checked = showLockStatus,
                                    onCheckedChange = {
                                        showLockStatus = it
                                        prefsStore.showLockScreenStatus = it
                                    },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = PureBlack,
                                        checkedTrackColor = PureWhite,
                                        uncheckedThumbColor = LightGray,
                                        uncheckedTrackColor = Color(0xFF222428)
                                    )
                                )
                            }
                        }
                    }
                }

                // 4. ⏳ FRICTION & FOCUS ECONOMY
                item {
                    SettingsSectionCard(title = "⏳ FRICTION & FOCUS ECONOMY") {
                        // Current Credits Banner
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "CURRENT FOCUS BALANCE",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MidGray
                                )
                                Text(
                                    text = "$currentBalance credits",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PureWhite
                                )
                            }

                            TextButton(onClick = {
                                prefsStore.credits = 50
                                currentBalance = 50
                                Toast.makeText(context, "Balance reset to 50 credits", Toast.LENGTH_SHORT).show()
                            }) {
                                Text("Reset to 50", color = LightGray, fontSize = 12.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Breathing Duration Chips
                        Text(
                            text = "MINDFUL PAUSE DURATION",
                            style = MaterialTheme.typography.labelSmall,
                            letterSpacing = 1.sp,
                            color = MidGray
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf(3, 5, 10, 15).forEach { s ->
                                OptionChip(
                                    label = "${s}s",
                                    isSelected = frictionSeconds == s,
                                    modifier = Modifier.weight(1f),
                                    onClick = {
                                        frictionSeconds = s
                                        prefsStore.frictionCountdownSeconds = s
                                    }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Bypass Cost Chips
                        Text(
                            text = "CREDIT COST PER BYPASS",
                            style = MaterialTheme.typography.labelSmall,
                            letterSpacing = 1.sp,
                            color = MidGray
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf(5, 10, 20).forEach { cost ->
                                OptionChip(
                                    label = "$cost pts",
                                    isSelected = frictionCost == cost,
                                    modifier = Modifier.weight(1f),
                                    onClick = {
                                        frictionCost = cost
                                        prefsStore.frictionCost = cost
                                    }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Manage Distraction Apps Button
                        Button(
                            onClick = { showDistractionModal = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF1E2024),
                                contentColor = PureWhite
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .border(1.dp, Color(0xFF333740), RoundedCornerShape(8.dp)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "Manage Distraction Apps (${apps.count { it.isDistraction }})",
                                fontSize = 13.sp,
                                color = PureWhite
                            )
                        }
                    }
                }

                // 5. ℹ️ ABOUT & VERSION
                item {
                    SettingsSectionCard(title = "ℹ️ ABOUT MINIMALPHONE") {
                        Text(
                            text = "MiniMalPhone v1.0.0",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = PureWhite
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "An intentional, open-source productivity instrument designed to eliminate smartphone addiction.",
                            style = MaterialTheme.typography.bodySmall,
                            color = LightGray
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "MIT License • 100% Offline & Private",
                            style = MaterialTheme.typography.labelSmall,
                            color = MidGray
                        )
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }

        // Manage Distractions Modal
        if (showDistractionModal) {
            DistractionAppsManagerModal(
                apps = apps,
                onToggleDistraction = { onToggleDistractionApp(it) },
                onDismiss = { showDistractionModal = false }
            )
        }
    }
}

@Composable
private fun SettingsSectionCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF111215))
            .border(1.dp, Color(0xFF20232A), RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                letterSpacing = 1.5.sp,
                fontWeight = FontWeight.Bold,
                color = MidGray
            )
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun OptionChip(
    label: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(if (isSelected) PureWhite else Color(0xFF1E2024))
            .border(1.dp, if (isSelected) PureWhite else Color(0xFF333740), RoundedCornerShape(6.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) PureBlack else PureWhite
        )
    }
}

@Composable
private fun DistractionAppsManagerModal(
    apps: List<AppModel>,
    onToggleDistraction: (AppModel) -> Unit,
    onDismiss: () -> Unit
) {
    var query by remember { mutableStateOf("") }
    val filtered = remember(query, apps) {
        apps.filter { it.label.contains(query, ignoreCase = true) }
            .sortedWith(compareByDescending<AppModel> { it.isDistraction }.thenBy { it.label.lowercase() })
    }

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(Color(0xFF141518))
                .border(1.dp, Color(0xFF2C2F36), RoundedCornerShape(14.dp))
                .padding(18.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Distraction Apps",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = PureWhite
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = LightGray)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = { Text("Filter apps…", color = MidGray, fontSize = 13.sp) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = PureWhite,
                        unfocusedTextColor = PureWhite,
                        focusedBorderColor = PureWhite,
                        unfocusedBorderColor = DarkGray600
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                LazyColumn(modifier = Modifier.heightIn(max = 320.dp)) {
                    items(filtered, key = { it.packageName }) { app ->
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
    }
}
