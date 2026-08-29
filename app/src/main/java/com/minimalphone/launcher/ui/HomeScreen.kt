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
import com.minimalphone.launcher.domain.productivity.EventItem
import com.minimalphone.launcher.ui.components.MonochromeWallpaper
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(
    events: List<EventItem>,
    focusCredits: Int,
    batteryPct: Int,
    onLaunchEssential: (EssentialAppType) -> Unit,
    onToggleEvent: (EventItem) -> Unit,
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
    val topPrimaryTextColor = Color(0xFF1E2126)
    val topSecondaryTextColor = Color(0xFF4A4E56)

    Box(modifier = modifier.fillMaxSize()) {
        // Procedural Layered Monochrome Wallpaper
        MonochromeWallpaper()

        // Content Layout (Positioned from Top to Bottom)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 26.dp, vertical = 28.dp)
        ) {
            // 1. TOP HEADER: Reward points pill & battery indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Focus Points Pill (Dark on light backdrop)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0x33000000))
                        .border(1.dp, Color(0x33000000), RoundedCornerShape(6.dp))
                        .clickable { onNavigateToTasks() }
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "$focusCredits pts",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = topPrimaryTextColor
                    )
                }

                if (batteryPct >= 0) {
                    Text(
                        text = "$batteryPct%",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium,
                        color = topSecondaryTextColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 2. TOP-LEFT STACKED CLOCK WIDGET (Directly matching photo)
            Column(horizontalAlignment = Alignment.Start) {
                Text(
                    text = hourString.ifEmpty { "17" },
                    fontSize = 66.sp,
                    lineHeight = 66.sp,
                    fontWeight = FontWeight.Light,
                    letterSpacing = (-1.5).sp,
                    color = topPrimaryTextColor
                )
                Text(
                    text = minuteString.ifEmpty { "06" },
                    fontSize = 66.sp,
                    lineHeight = 66.sp,
                    fontWeight = FontWeight.Light,
                    letterSpacing = (-1.5).sp,
                    color = topPrimaryTextColor
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = dateString.ifEmpty { "Thu 6 March" },
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Normal,
                    color = topSecondaryTextColor
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 3. UPCOMING SCHEDULES (Placed directly under the clock widget)
            Text(
                text = "UPCOMING SCHEDULES",
                style = MaterialTheme.typography.labelSmall,
                letterSpacing = 2.sp,
                color = topSecondaryTextColor
            )

            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xDD1C1E23))
                    .border(1.dp, Color(0x44FFFFFF), RoundedCornerShape(12.dp))
                    .padding(horizontal = 18.dp, vertical = 14.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    events.forEach { event ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onToggleEvent(event) },
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
                                        .background(if (event.isCompleted) Color(0xFF6E727A) else Color.White)
                                )

                                Spacer(modifier = Modifier.width(12.dp))

                                Text(
                                    text = event.title,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Normal,
                                    color = if (event.isCompleted) Color(0xFF6E727A) else Color.White
                                )
                            }

                            Text(
                                text = event.timeFormatted,
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (event.isCompleted) Color(0xFF6E727A) else Color(0xFFD0D3D8)
                            )
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
