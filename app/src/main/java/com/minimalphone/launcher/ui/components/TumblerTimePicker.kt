package com.minimalphone.launcher.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

/**
 * High-fidelity iOS/Cupertino style wheel tumbler time picker matching user's photo.
 * Features 3 scrollable columns (Hour, Minute, AM/PM) with a center highlight bar.
 */
@Composable
fun TumblerTimePicker(
    selectedHour: Int,
    selectedMinute: Int,
    selectedPeriod: String, // "AM" or "PM"
    onTimeChange: (hour: Int, minute: Int, period: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val hours = (1..12).toList()
    val minutes = (0..55 step 5).toList() // Clean 5-min intervals or 0..59
    val periods = listOf("AM", "PM")

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(180.dp)
            .background(Color(0xFF151619)),
        contentAlignment = Alignment.Center
    ) {
        // Center selection highlight pill spanning all 3 tumblers (matching photo)
        Box(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .height(44.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0x30FFFFFF))
        )

        Row(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .height(180.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 1. Hour Tumbler
            WheelColumn(
                items = hours.map { it.toString() },
                selectedIndex = hours.indexOf(selectedHour).coerceAtLeast(0),
                onSelect = { idx ->
                    onTimeChange(hours[idx], selectedMinute, selectedPeriod)
                },
                modifier = Modifier.weight(1f)
            )

            // 2. Minute Tumbler
            WheelColumn(
                items = minutes.map { String.format("%02d", it) },
                selectedIndex = minutes.indexOf(selectedMinute).coerceAtLeast(0),
                onSelect = { idx ->
                    onTimeChange(selectedHour, minutes[idx], selectedPeriod)
                },
                modifier = Modifier.weight(1f)
            )

            // 3. AM / PM Tumbler
            WheelColumn(
                items = periods,
                selectedIndex = periods.indexOf(selectedPeriod).coerceAtLeast(0),
                onSelect = { idx ->
                    onTimeChange(selectedHour, selectedMinute, periods[idx])
                },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun WheelColumn(
    items: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    LaunchedEffect(selectedIndex) {
        if (selectedIndex in items.indices) {
            listState.animateScrollToItem((selectedIndex - 1).coerceAtLeast(0))
        }
    }

    LazyColumn(
        state = listState,
        modifier = modifier.height(180.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top spacing buffer
        item { Box(modifier = Modifier.height(45.dp)) }

        itemsIndexed(items) { index, item ->
            val isSelected = index == selectedIndex
            Box(
                modifier = Modifier
                    .height(44.dp)
                    .width(64.dp)
                    .clickable {
                        onSelect(index)
                        scope.launch {
                            listState.animateScrollToItem((index - 1).coerceAtLeast(0))
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = item,
                    fontSize = if (isSelected) 24.sp else 18.sp,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (isSelected) Color.White else Color(0x66FFFFFF)
                )
            }
        }

        // Bottom spacing buffer
        item { Box(modifier = Modifier.height(45.dp)) }
    }
}
