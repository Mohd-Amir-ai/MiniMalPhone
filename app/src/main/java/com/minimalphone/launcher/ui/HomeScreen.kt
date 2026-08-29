package com.minimalphone.launcher.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Search
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
import java.util.Calendar
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

    // Filter only events that are scheduled within the next 30 minutes
    fun isWithinNext30Minutes(timeStr: String): Boolean {
        return try {
            val sdf = SimpleDateFormat("h:mm a", Locale.getDefault())
            val parsed = sdf.parse(timeStr.trim()) ?: return false
            val calTask = Calendar.getInstance().apply { time = parsed }
            val taskMins = calTask.get(Calendar.HOUR_OF_DAY) * 60 + calTask.get(Calendar.MINUTE)

            val calNow = Calendar.getInstance()
            val nowMins = calNow.get(Calendar.HOUR_OF_DAY) * 60 + calNow.get(Calendar.MINUTE)

            val diff = taskMins - nowMins
            diff in 0..30
        } catch (e: Exception) {
            false
        }
    }

    val next30MinTasks = tasks.filter { !it.isCompleted && isWithinNext30Minutes(it.scheduledTime) }

    Box(modifier = modifier.fillMaxSize()) {
        // Procedural Layered Monochrome Wallpaper
        MonochromeWallpaper()

        // Content Layout
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 26.dp, vertical = 14.dp)
        ) {
            // 1. TOP HEADER: Clean minimal battery (Points box & wallpaper box removed per request)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (batteryPct >= 0) {
                    Text(
                        text = "$batteryPct%",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium,
                        color = topSecondaryTextColor
                    )
                }
            }

            // 1b. Set as Default Home banner (only shown if not yet default)
            if (!isDefaultLauncher) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0x22000000))
                        .border(1.dp, Color(0x33000000), RoundedCornerShape(8.dp))
                        .clickable { onRequestSetDefaultLauncher() }
                        .padding(horizontal = 12.dp, vertical = 6.dp),
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

            Spacer(modifier = Modifier.height(10.dp))

            // 2. TOP-LEFT STACKED CLOCK WIDGET
            Column(horizontalAlignment = Alignment.Start) {
                Text(
                    text = hourString.ifEmpty { "16" },
                    fontSize = 78.sp,
                    lineHeight = 76.sp,
                    fontWeight = FontWeight.Normal,
                    letterSpacing = (-2).sp,
                    color = topPrimaryTextColor
                )
                Text(
                    text = minuteString.ifEmpty { "42" },
                    fontSize = 78.sp,
                    lineHeight = 76.sp,
                    fontWeight = FontWeight.Normal,
                    letterSpacing = (-2).sp,
                    color = topPrimaryTextColor
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = dateString.ifEmpty { "Sat 29 August" },
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = topSecondaryTextColor
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 3. UPCOMING SCHEDULES (Only events within next 30 minutes, seamless with wallpaper)
            if (next30MinTasks.isNotEmpty()) {
                Text(
                    text = "Upcoming schedules (next 30m)",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = topPrimaryTextColor
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Seamless, minimal container with 100% wallpaper blending
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    next30MinTasks.forEach { task ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onToggleTask(task) }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(topPrimaryTextColor)
                                )

                                Spacer(modifier = Modifier.width(10.dp))

                                Text(
                                    text = task.title,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = topPrimaryTextColor
                                )
                            }

                            Text(
                                text = task.scheduledTime,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Normal,
                                color = topSecondaryTextColor
                            )
                        }
                    }
                }
            }

            // Pushes everything down so top section stays at top and icons at bottom
            Spacer(modifier = Modifier.weight(1f))

            // 4. BOTTOM SECTION: 4 Circular Minimal Icon Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 12.dp),
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
            .size(56.dp)
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
