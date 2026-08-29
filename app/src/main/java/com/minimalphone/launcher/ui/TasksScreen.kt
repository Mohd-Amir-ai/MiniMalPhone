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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
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
import com.minimalphone.launcher.ui.components.TaskEditorModal

@Composable
fun TasksScreen(
    tasks: List<TaskItem>,
    focusCredits: Int,
    onToggleTask: (TaskItem) -> Unit,
    onSaveTask: (TaskItem) -> Unit,
    onSwapTasks: (TaskItem, TaskItem) -> Unit,
    onDeleteTask: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var showEditorModal by remember { mutableStateOf(false) }
    var taskToEdit by remember { mutableStateOf<TaskItem?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(PureBlack)
            .padding(horizontal = 24.dp, vertical = 28.dp)
    ) {
        // Top Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "TASKS & SCHEDULES",
                    style = MaterialTheme.typography.labelSmall,
                    letterSpacing = 2.sp,
                    color = MidGray
                )
                Text(
                    text = "Focus Ledger",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = PureWhite
                )
            }

            // Circular Add Button
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(DarkCard)
                    .border(1.dp, DarkGray600, CircleShape)
                    .clickable {
                        taskToEdit = null
                        showEditorModal = true
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Task",
                    tint = PureWhite,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Balance Banner
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(DarkCard)
                .border(1.dp, DarkGray600, RoundedCornerShape(10.dp))
                .padding(horizontal = 18.dp, vertical = 14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Text(
                        text = "POINTS EARNED SO FAR",
                        style = MaterialTheme.typography.labelSmall,
                        letterSpacing = 1.5.sp,
                        color = MidGray
                    )
                    Text(
                        text = "$focusCredits pts",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = PureWhite
                    )
                }

                Text(
                    text = "+10 to +50 per task",
                    style = MaterialTheme.typography.labelSmall,
                    color = LightGray
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "TODAY'S PRIORITIES (${tasks.size})",
            style = MaterialTheme.typography.labelSmall,
            letterSpacing = 2.sp,
            color = MidGray
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (tasks.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No schedules yet.\nTap + above to plan your day.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MidGray,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                itemsIndexed(tasks, key = { _, item -> item.id }) { index, task ->
                    TaskCard(
                        task = task,
                        canMoveUp = index > 0,
                        canMoveDown = index < tasks.size - 1,
                        onToggle = { onToggleTask(task) },
                        onEdit = {
                            taskToEdit = task
                            showEditorModal = true
                        },
                        onMoveUp = {
                            if (index > 0) {
                                onSwapTasks(task, tasks[index - 1])
                            }
                        },
                        onMoveDown = {
                            if (index < tasks.size - 1) {
                                onSwapTasks(task, tasks[index + 1])
                            }
                        },
                        onDelete = { onDeleteTask(task.id) }
                    )
                }
            }
        }
    }

    if (showEditorModal) {
        TaskEditorModal(
            initialTask = taskToEdit,
            onDismiss = { showEditorModal = false },
            onSave = { saved ->
                onSaveTask(saved)
                showEditorModal = false
            }
        )
    }
}

@Composable
private fun TaskCard(
    task: TaskItem,
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    onToggle: () -> Unit,
    onEdit: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onDelete: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(if (task.isCompleted) Color(0xFF141416) else DarkCard)
            .border(1.dp, if (task.isCompleted) Color(0xFF222224) else DarkGray600, RoundedCornerShape(10.dp))
            .padding(14.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Checkbox + Title
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (task.isCompleted) PureWhite else Color(0xFF222429))
                            .border(1.dp, if (task.isCompleted) PureWhite else DarkGray600, RoundedCornerShape(4.dp))
                            .clickable { onToggle() },
                        contentAlignment = Alignment.Center
                    ) {
                        if (task.isCompleted) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Done",
                                tint = PureBlack,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Text(
                        text = task.title,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                        color = if (task.isCompleted) MidGray else PureWhite
                    )
                }

                // Edit & Delete icons
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onEdit, modifier = Modifier.size(28.dp)) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = LightGray,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Delete",
                            tint = MidGray,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Badges Row: Scheduled Time, Difficulty Badge, and Reorder Swapping Arrows
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Time Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFF222428))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "🕒 ${task.scheduledTime}",
                            style = MaterialTheme.typography.labelSmall,
                            color = LightGray
                        )
                    }

                    // Difficulty & Reward Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFF2A2D33))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "${task.difficulty.label} • +${task.rewardPoints} pts",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = PureWhite
                        )
                    }
                }

                // Reorder Swap Buttons (When moving X to Y position, timings are swapped!)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (canMoveUp) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF222428))
                                .clickable { onMoveUp() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowUpward,
                                contentDescription = "Swap Earlier",
                                tint = LightGray,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }

                    if (canMoveDown) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF222428))
                                .clickable { onMoveDown() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowDownward,
                                contentDescription = "Swap Later",
                                tint = LightGray,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
