package com.minimalphone.launcher.ui.components

import android.view.HapticFeedbackConstants
import android.view.SoundEffectConstants
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

/**
 * SOTA Wheel Tumbler Time Picker matching reference image.
 * Features 3 snapping columns (Hour, Minute, AM/PM) with center selection highlight,
 * smooth 3D gradient fading, and satisfying mechanical sound & haptic tick feedback.
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
    val minutes = (0..55 step 5).toList()
    val periods = listOf("AM", "PM")

    val itemHeight = 44.dp
    val totalHeight = itemHeight * 3 // Exactly 132.dp, zero overflow

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(totalHeight)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF141518))
            .border(1.dp, Color(0xFF282B30), RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center
    ) {
        // Center selection highlight pill spanning all 3 tumblers
        Box(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .height(itemHeight)
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0x35FFFFFF))
        )

        Row(
            modifier = Modifier
                .fillMaxWidth(0.88f)
                .height(totalHeight),
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

        // Top & bottom gradient fading masks for realistic 3D tumbler illusion
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(itemHeight)
                .align(Alignment.TopCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFF141518), Color.Transparent)
                    )
                )
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(itemHeight)
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color(0xFF141518))
                    )
                )
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun WheelColumn(
    items: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val itemHeight = 44.dp
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = selectedIndex)
    val snapFlingBehavior = rememberSnapFlingBehavior(lazyListState = listState)
    val scope = rememberCoroutineScope()
    val view = LocalView.current

    // Trigger haptic & mechanical sound click when center item changes
    LaunchedEffect(listState) {
        snapshotFlow { listState.firstVisibleItemIndex }
            .distinctUntilChanged()
            .collect { index ->
                if (index in items.indices && index != selectedIndex) {
                    try {
                        view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                        view.playSoundEffect(SoundEffectConstants.CLICK)
                    } catch (e: Exception) {
                        // ignore if unsupported
                    }
                    onSelect(index)
                }
            }
    }

    // Synchronize programmatic selection changes
    LaunchedEffect(selectedIndex) {
        if (listState.firstVisibleItemIndex != selectedIndex && selectedIndex in items.indices) {
            listState.animateScrollToItem(selectedIndex)
        }
    }

    LazyColumn(
        state = listState,
        flingBehavior = snapFlingBehavior,
        modifier = modifier.height(itemHeight * 3),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top spacing buffer so index 0 lands directly in center highlight
        item { Box(modifier = Modifier.height(itemHeight)) }

        itemsIndexed(items) { index, item ->
            val isSelected = index == selectedIndex
            Box(
                modifier = Modifier
                    .height(itemHeight)
                    .width(68.dp)
                    .clickable {
                        onSelect(index)
                        scope.launch {
                            listState.animateScrollToItem(index)
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = item,
                    fontSize = if (isSelected) 22.sp else 16.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = if (isSelected) Color.White else Color(0x66FFFFFF)
                )
            }
        }

        // Bottom spacing buffer so last index lands directly in center highlight
        item { Box(modifier = Modifier.height(itemHeight)) }
    }
}
