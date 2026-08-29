package com.minimalphone.launcher.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Wallpaper
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.minimalphone.launcher.domain.apps.EssentialAppType
import com.minimalphone.launcher.domain.productivity.TaskItem
import com.minimalphone.launcher.ui.components.MonochromeWallpaper
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(
    tasks: List<TaskItem>,
    focusCredits: Int,
    batteryPct: Int,
    isDefaultLauncher: Boolean,
    onLaunchEssential: (EssentialAppType) -> Unit,
    onToggleTask: (TaskItem) -> Unit,
    onRequestSetDefaultLauncher: () -> Unit,
    onApplyDeviceWallpaper: () -> Unit,
    onNavigateToTasks: () -> Unit,
    onNavigateToDrawer: () -> Unit,
    modifier: Modifier = Modifier
) {
    var hourString by remember { mutableStateOf("") }
    var minuteString by remember { mutableStateOf("") }
    var dateString by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        val hourFormat = SimpleDateFormat("HH", Locale.getDefault())
        val minuteFormat = SimpleDateFormat("mm", Locale.getDefault())
        val dateFormat = SimpleDateFormat("EEE d MMMM", Locale.getDefault())
        while (true) {
            val now = Date()
            hourString = hourFormat.format(now)
            minuteString = minuteFormat.format(now)
            dateString = dateFormat.format(now)
            delay(1000L)
        }
    }

    // Top Charcoal typography for high contrast over the light-gray sky
    val topPrimaryTextColor = Color(0xFF16181C)
    val topSecondaryTextColor = Color(0xFF484C54)

    val upcomingTasks = tasks.filter { !it.isCompleted }.take(3)

    Box(modifier = modifier.fillMaxSize()) {
        // Procedural Layered Monochrome Wallpaper
        MonochromeWallpaper()

        // Content Layout
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 26.dp, vertical = 26.dp)
        ) {
            // 1. TOP HEADER: Points earned so far & Battery
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Points Earned So Far Pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0x33000000))
                        .border(1.dp, Color(0x33000000), RoundedCornerShape(6.dp))
                        .clickable { onNavigateToTasks() }
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = "Points earned so far: $focusCredits pts",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = topPrimaryTextColor
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Sync Device Wallpaper icon button
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(Color(0x22000000))
                            .clickable(onClick = onApplyDeviceWallpaper),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Wallpaper,
                            contentDescription = "Sync Wallpaper",
                            tint = topPrimaryTextColor,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    if (batteryPct >= 0) {
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "$batteryPct%",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Medium,
                            color = topSecondaryTextColor
                        )
                    }
                }
            }

            // 1b. Set as Default Home banner (if not default)
            if (!isDefaultLauncher) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0x33000000))
                        .border(1.dp, Color(0x33000000), RoundedCornerShape(8.dp))
                        .clickable { onRequestSetDefaultLauncher() }
                        .padding(horizontal = 12.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Set as Default Home Launcher",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium,
                        color = topPrimaryTextColor
                    )
                    Text(
                        text = "Set Now →",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = topPrimaryTextColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 2. TOP-LEFT STACKED CLOCK WIDGET (Bolder and bigger)
            Column(horizontalAlignment = Alignment.Start) {
                Text(
                    text = hourString.ifEmpty { "17" },
                    fontSize = 78.sp,
                    lineHeight = 76.sp,
                    fontWeight = FontWeight.Normal,
                    letterSpacing = (-2).sp,
                    color = topPrimaryTextColor
                )
                Text(
                    text = minuteString.ifEmpty { "06" },
                    fontSize = 78.sp,
                    lineHeight = 76.sp,
                    fontWeight = FontWeight.Normal,
                    letterSpacing = (-2).sp,
                    color = topPrimaryTextColor
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = dateString.ifEmpty { "Thu 6 March" },
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = topSecondaryTextColor
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 3. UPCOMING SCHEDULES (Non-all capital text)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Upcoming schedules",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = topPrimaryTextColor
                )

                Text(
                    text = "View all →",
                    style = MaterialTheme.typography.labelSmall,
                    color = topSecondaryTextColor,
                    modifier = Modifier.clickable { onNavigateToTasks() }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xDD1C1E23))
                    .border(1.dp, Color(0x44FFFFFF), RoundedCornerShape(12.dp))
                    .padding(horizontal = 18.dp, vertical = 14.dp)
            ) {
                if (upcomingTasks.isEmpty()) {
                    Text(
                        text = "All caught up! Tap 'View all' to add a schedule.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFFA0A4AC)
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        upcomingTasks.forEach { task ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onToggleTask(task) },
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(Color.White)
                                    )

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Text(
                                        text = task.title,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Normal,
                                        color = Color.White
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = task.scheduledTime,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color(0xFFD0D3D8)
                                    )

                                    Spacer(modifier = Modifier.width(8.dp))

                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(3.dp))
                                            .background(Color(0xFF2C2F36))
                                            .padding(horizontal = 5.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "+${task.rewardPoints}",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontSize = 9.sp,
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Pushes everything else down so time and schedules stay firmly at the top
            Spacer(modifier = Modifier.weight(1f))

            // 4. BOTTOM SECTION: 4 Circular Minimal Icon Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                MinimalCircularIconButton(
                    icon = Icons.Default.Phone,
                    contentDescription = "Phone",
                    onClick = { onLaunchEssential(EssentialAppType.PHONE) }
                )

                MinimalCircularIconButton(
                    icon = Icons.Default.Search,
                    contentDescription = "Google Search",
                    onClick = { onLaunchEssential(EssentialAppType.SEARCH) }
                )

                MinimalCircularIconButton(
                    icon = Icons.Default.Message,
                    contentDescription = "Messages",
                    onClick = { onLaunchEssential(EssentialAppType.MESSAGES) }
                )

                MinimalCircularIconButton(
                    icon = Icons.Default.CameraAlt,
                    contentDescription = "Camera",
                    onClick = { onLaunchEssential(EssentialAppType.CAMERA) }
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Minimal Navigation Footers
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "← Tasks",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF8E9299),
                    modifier = Modifier
                        .clickable { onNavigateToTasks() }
                        .padding(vertical = 4.dp)
                )

                Text(
                    text = "All Apps →",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF8E9299),
                    modifier = Modifier
                        .clickable { onNavigateToDrawer() }
                        .padding(vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun MinimalCircularIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(54.dp)
            .clip(CircleShape)
            .background(Color(0xFF222429))
            .border(1.dp, Color(0xFF3E434D), CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = Color.White,
            modifier = Modifier.size(24.dp)
        )
    }
}
