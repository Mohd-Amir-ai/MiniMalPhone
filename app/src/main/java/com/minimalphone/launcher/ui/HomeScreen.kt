package com.minimalphone.launcher.ui

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.minimalphone.launcher.domain.apps.AppModel
import com.minimalphone.launcher.domain.productivity.TaskItem
import com.minimalphone.launcher.theme.AccentBorder
import com.minimalphone.launcher.theme.Black
import com.minimalphone.launcher.theme.ChalkWhite
import com.minimalphone.launcher.theme.DarkCard
import com.minimalphone.launcher.theme.LightGray
import com.minimalphone.launcher.theme.MidGray
import com.minimalphone.launcher.theme.PureWhite
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(
    favoriteApps: List<AppModel>,
    tasks: List<TaskItem>,
    focusCredits: Int,
    batteryPct: Int,
    onLaunchApp: (AppModel) -> Unit,
    onToggleTask: (TaskItem) -> Unit,
    onNavigateToTasks: () -> Unit,
    onNavigateToDrawer: () -> Unit,
    modifier: Modifier = Modifier
) {
    var currentTime by remember { mutableStateOf("") }
    var currentDate by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val dateFormat = SimpleDateFormat("EEEE, MMMM d", Locale.getDefault())
        while (true) {
            val now = Date()
            currentTime = timeFormat.format(now)
            currentDate = dateFormat.format(now)
            delay(1000L)
        }
    }

    val pendingTasks = tasks.filter { !it.isCompleted }.take(3)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Black)
            .padding(horizontal = 28.dp, vertical = 36.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Modular Slot 1: Header (Date, Battery, Focus Credits, Time)
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = currentDate.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    letterSpacing = 2.sp,
                    color = LightGray
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (batteryPct >= 0) {
                        Text(
                            text = "$batteryPct%",
                            style = MaterialTheme.typography.labelSmall,
                            color = LightGray,
                            modifier = Modifier.padding(end = 12.dp)
                        )
                    }
                    Text(
                        text = "$focusCredits pts",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = PureWhite
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = currentTime.ifEmpty { "--:--" },
                style = MaterialTheme.typography.displayLarge,
                fontWeight = FontWeight.Light,
                color = PureWhite
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Modular Slot 2: Rule of 3 Priority Tasks Widget
            if (pendingTasks.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(DarkCard)
                        .clickable { onNavigateToTasks() }
                        .padding(16.dp)
                ) {
                    Column {
                        Text(
                            text = "TODAY'S PRIORITIES",
                            style = MaterialTheme.typography.labelSmall,
                            letterSpacing = 2.sp,
                            color = LightGray
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        pendingTasks.forEach { task ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(AccentBorder)
                                        .clickable { onToggleTask(task) }
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = task.title,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = ChalkWhite
                                )
                            }
                        }
                    }
                }
            }
        }

        // Modular Slot 3: Text Pinned Apps
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (favoriteApps.isEmpty()) {
                Text(
                    text = "No pinned apps.\nSwipe to app drawer and long-press to pin.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MidGray
                )
            } else {
                favoriteApps.take(6).forEach { app ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onLaunchApp(app) }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = app.label.lowercase(),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Light,
                            color = PureWhite
                        )

                        if (app.isDistraction) {
                            Text(
                                text = "mindful",
                                style = MaterialTheme.typography.labelSmall,
                                color = MidGray
                            )
                        }
                    }
                }
            }
        }

        // Modular Slot 4: Navigation Footnotes
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "← Tasks",
                style = MaterialTheme.typography.bodyMedium,
                color = LightGray,
                modifier = Modifier
                    .clickable { onNavigateToTasks() }
                    .padding(8.dp)
            )

            Text(
                text = "All Apps →",
                style = MaterialTheme.typography.bodyMedium,
                color = LightGray,
                modifier = Modifier
                    .clickable { onNavigateToDrawer() }
                    .padding(8.dp)
            )
        }
    }
}
