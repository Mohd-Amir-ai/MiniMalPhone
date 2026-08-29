package com.minimalphone.launcher.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.minimalphone.launcher.domain.productivity.TaskDifficulty
import com.minimalphone.launcher.domain.productivity.TaskItem
import com.minimalphone.launcher.theme.DarkCard
import com.minimalphone.launcher.theme.DarkGray600
import com.minimalphone.launcher.theme.LightGray
import com.minimalphone.launcher.theme.MidGray
import com.minimalphone.launcher.theme.PureBlack
import com.minimalphone.launcher.theme.PureWhite

@Composable
fun TaskEditorModal(
    initialTask: TaskItem? = null,
    onDismiss: () -> Unit,
    onSave: (TaskItem) -> Unit
) {
    var title by remember { mutableStateOf(initialTask?.title ?: "") }
    var selectedDifficulty by remember { mutableStateOf(initialTask?.difficulty ?: TaskDifficulty.MEDIUM) }
    var selectedDate by remember { mutableStateOf(initialTask?.scheduledDate ?: "Today") }

    // Parse initial time e.g. "6:30 AM" or "2:30 PM"
    var hour by remember {
        mutableIntStateOf(
            initialTask?.scheduledTime?.split(":")?.getOrNull(0)?.toIntOrNull() ?: 6
        )
    }
    var minute by remember {
        mutableIntStateOf(
            initialTask?.scheduledTime?.split(":")?.getOrNull(1)?.split(" ")?.getOrNull(0)?.toIntOrNull() ?: 30
        )
    }
    var period by remember {
        mutableStateOf(
            if (initialTask?.scheduledTime?.contains("PM") == true) "PM" else "AM"
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF141518),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, DarkGray600, RoundedCornerShape(16.dp)),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onDismiss) {
                    Text("Cancel", color = LightGray, fontSize = 16.sp)
                }

                Text(
                    text = if (initialTask == null) "New Task" else "Edit Task",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = PureWhite
                )

                TextButton(
                    onClick = {
                        if (title.isNotBlank()) {
                            val formattedTime = String.format("%d:%02d %s", hour, minute, period)
                            val taskToSave = initialTask?.copy(
                                title = title.trim(),
                                difficulty = selectedDifficulty,
                                scheduledTime = formattedTime,
                                scheduledDate = selectedDate
                            ) ?: TaskItem(
                                title = title.trim(),
                                difficulty = selectedDifficulty,
                                scheduledTime = formattedTime,
                                scheduledDate = selectedDate
                            )
                            onSave(taskToSave)
                        }
                    },
                    enabled = title.isNotBlank()
                ) {
                    Text(
                        text = "Save",
                        color = if (title.isNotBlank()) PureWhite else MidGray,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1. Task Name Input
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    placeholder = { Text("What needs to be done?", color = MidGray) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = PureWhite,
                        unfocusedTextColor = PureWhite,
                        focusedBorderColor = PureWhite,
                        unfocusedBorderColor = DarkGray600
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // 2. Difficulty Selector with Dynamic Points
                Column {
                    Text(
                        text = "DIFFICULTY & REWARD",
                        style = MaterialTheme.typography.labelSmall,
                        letterSpacing = 1.5.sp,
                        color = MidGray
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        TaskDifficulty.values().forEach { diff ->
                            val isSelected = selectedDifficulty == diff
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isSelected) PureWhite else DarkCard)
                                    .border(1.dp, if (isSelected) PureWhite else DarkGray600, RoundedCornerShape(6.dp))
                                    .clickable { selectedDifficulty = diff }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = diff.label,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) PureBlack else PureWhite
                                    )
                                    Text(
                                        text = "+${diff.points} pts",
                                        fontSize = 10.sp,
                                        color = if (isSelected) PureBlack.copy(alpha = 0.8f) else LightGray
                                    )
                                }
                            }
                        }
                    }
                }

                // 3. Date Selection Chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Today", "Tomorrow").forEach { d ->
                        val isSelected = selectedDate == d
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSelected) Color(0x33FFFFFF) else DarkCard)
                                .border(1.dp, if (isSelected) PureWhite else DarkGray600, RoundedCornerShape(6.dp))
                            .clickable { selectedDate = d }
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = d,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isSelected) PureWhite else LightGray
                            )
                        }
                    }
                }

                // 4. SOTA Tumbler Time Picker (Matching user's photo)
                Column {
                    Text(
                        text = "SCHEDULE TIME",
                        style = MaterialTheme.typography.labelSmall,
                        letterSpacing = 1.5.sp,
                        color = MidGray
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    TumblerTimePicker(
                        selectedHour = hour,
                        selectedMinute = minute,
                        selectedPeriod = period,
                        onTimeChange = { h, m, p ->
                            hour = h
                            minute = m
                            period = p
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .border(1.dp, DarkGray600, RoundedCornerShape(10.dp))
                    )
                }
            }
        },
        confirmButton = {}
    )
}
