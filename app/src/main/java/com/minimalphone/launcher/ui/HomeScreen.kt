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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.minimalphone.launcher.domain.apps.EssentialAppType
import com.minimalphone.launcher.domain.productivity.EventItem
import com.minimalphone.launcher.theme.DarkCard
import com.minimalphone.launcher.theme.DarkGray600
import com.minimalphone.launcher.theme.DarkGray700
import com.minimalphone.launcher.theme.LightGray
import com.minimalphone.launcher.theme.MidGray
import com.minimalphone.launcher.theme.PureBlack
import com.minimalphone.launcher.theme.PureWhite
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

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(PureBlack)
            .padding(horizontal = 26.dp, vertical = 30.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // TOP HEADER: Focus points & subtle battery
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Reward Points Pill
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(DarkCard)
                    .border(1.dp, DarkGray600, RoundedCornerShape(6.dp))
                    .clickable { onNavigateToTasks() }
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "$focusCredits pts",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = PureWhite
                )
            }

            if (batteryPct >= 0) {
                Text(
                    text = "$batteryPct%",
                    style = MaterialTheme.typography.labelSmall,
                    color = MidGray
                )
            }
        }

        // UPPER-MIDDLE SECTION: Left-Aligned Time Widget + Immediately followed by Upcoming Schedules
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
        ) {
            // Left-aligned Stacked Minimalist Clock (inspired by photo)
            Column(horizontalAlignment = Alignment.Start) {
                Text(
                    text = hourString.ifEmpty { "17" },
                    fontSize = 62.sp,
                    lineHeight = 62.sp,
                    fontWeight = FontWeight.Light,
                    letterSpacing = (-1).sp,
                    color = PureWhite
                )
                Text(
                    text = minuteString.ifEmpty { "06" },
                    fontSize = 62.sp,
                    lineHeight = 62.sp,
                    fontWeight = FontWeight.Light,
                    letterSpacing = (-1).sp,
                    color = PureWhite
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = dateString.ifEmpty { "Thu 6 March" },
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Normal,
                    color = LightGray
                )
            }

            Spacer(modifier = Modifier.height(30.dp))

            // Immediately After the Time Widget: Upcoming Schedules
            Text(
                text = "UPCOMING SCHEDULES",
                style = MaterialTheme.typography.labelSmall,
                letterSpacing = 2.sp,
                color = MidGray
            )

            Spacer(modifier = Modifier.height(10.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(DarkCard)
                    .border(1.dp, DarkGray600, RoundedCornerShape(12.dp))
                    .padding(horizontal = 18.dp, vertical = 16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
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
                                        .background(if (event.isCompleted) MidGray else PureWhite)
                                )

                                Spacer(modifier = Modifier.width(12.dp))

                                Text(
                                    text = event.title,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Normal,
                                    color = if (event.isCompleted) MidGray else PureWhite
                                )
                            }

                            Text(
                                text = event.timeFormatted,
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (event.isCompleted) MidGray else LightGray
                            )
                        }
                    }
                }
            }
        }

        // BOTTOM SECTION: Circular Minimal App Icons + Navigation
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Row of 4 Circular Minimalist Icon Shortcuts (Phone, Search, Messages, Camera)
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

            Spacer(modifier = Modifier.height(18.dp))

            // Navigation footers
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "← Tasks",
                    style = MaterialTheme.typography.labelSmall,
                    color = MidGray,
                    modifier = Modifier
                        .clickable { onNavigateToTasks() }
                        .padding(vertical = 4.dp)
                )

                Text(
                    text = "All Apps →",
                    style = MaterialTheme.typography.labelSmall,
                    color = MidGray,
                    modifier = Modifier
                        .clickable { onNavigateToDrawer() }
                        .padding(vertical = 4.dp)
                )
            }
        }
    }
}

/**
 * Minimal Circular Icon Button matching the reference setup in the uploaded photo.
 */
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
            .background(DarkGray700)
            .border(1.dp, DarkGray600, CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = PureWhite,
            modifier = Modifier.size(24.dp)
        )
    }
}
