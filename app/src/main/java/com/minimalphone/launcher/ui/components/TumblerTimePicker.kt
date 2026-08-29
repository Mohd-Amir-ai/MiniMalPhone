package com.minimalphone.launcher.ui.components

import androidx.compose.animation.core.Animatable
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
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.minimalphone.launcher.core.system.HapticHelper
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

/**
 * High-performance, buttery smooth SOTA wheel tumbler.
 * Fully decoupled columns: touching or scrolling minutes will NEVER cause
 * hours or periods to move or jitter!
 * Features physical vibration motor tick feedback and native mechanical sound.
 */
@Composable
fun TumblerTimePicker(
    selectedHour: Int,
    selectedMinute: Int,
    selectedPeriod: String, // "AM" or "PM"
    onTimeChange: (hour: Int, minute: Int, period: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val hours = remember { (1..12).toList() }
    val minutes = remember { (0..59).toList() }
    val periods = remember { listOf("AM", "PM") }

    // Internal state isolated to avoid sibling recomposition jumps
    var currentHour by remember { mutableIntStateOf(selectedHour) }
    var currentMinute by remember { mutableIntStateOf(selectedMinute) }
    var currentPeriod by remember { androidx.compose.runtime.mutableStateOf(selectedPeriod) }

    val itemHeight = 44.dp
    val totalHeight = itemHeight * 3 // Exactly 132.dp

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(totalHeight)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF141518))
            .border(1.dp, Color(0xFF2B2D33), RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center
    ) {
        // Center selection highlight pill across all 3 tumblers
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
            // 1. Hour Column (Completely decoupled)
            IndependentWheelColumn(
                items = hours.map { it.toString() },
                initialIndex = hours.indexOf(currentHour).coerceAtLeast(0),
                onIndexSettled = { idx ->
                    val newHour = hours[idx]
                    if (newHour != currentHour) {
                        currentHour = newHour
                        onTimeChange(currentHour, currentMinute, currentPeriod)
                    }
                },
                modifier = Modifier.weight(1f)
            )

            // 2. Minute Column (Completely decoupled)
            IndependentWheelColumn(
                items = minutes.map { String.format("%02d", it) },
                initialIndex = minutes.indexOf(currentMinute).coerceAtLeast(0),
                onIndexSettled = { idx ->
                    val newMinute = minutes[idx]
                    if (newMinute != currentMinute) {
                        currentMinute = newMinute
                        onTimeChange(currentHour, currentMinute, currentPeriod)
                    }
                },
                modifier = Modifier.weight(1f)
            )

            // 3. AM / PM Column (Completely decoupled)
            IndependentWheelColumn(
                items = periods,
                initialIndex = periods.indexOf(currentPeriod).coerceAtLeast(0),
                onIndexSettled = { idx ->
                    val newPeriod = periods[idx]
                    if (newPeriod != currentPeriod) {
                        currentPeriod = newPeriod
                        onTimeChange(currentHour, currentMinute, currentPeriod)
                    }
                },
                modifier = Modifier.weight(1f)
            )
        }

        // Top & bottom gradient fading masks for realistic 3D cylindrical tumbler effect
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
private fun IndependentWheelColumn(
    items: List<String>,
    initialIndex: Int,
    onIndexSettled: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val itemHeight = 44.dp
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = initialIndex)
    val snapFlingBehavior = rememberSnapFlingBehavior(lazyListState = listState)
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val view = LocalView.current

    var selectedIndex by remember { mutableIntStateOf(initialIndex) }

    // Detect center item change dynamically during scrolling
    LaunchedEffect(listState) {
        snapshotFlow {
            // Center element index is firstVisibleItemIndex when top buffer is exactly 1 itemHeight
            val firstIndex = listState.firstVisibleItemIndex
            val offset = listState.firstVisibleItemScrollOffset
            if (offset > 22) firstIndex + 1 else firstIndex
        }
        .distinctUntilChanged()
        .collect { rawIndex ->
            val clamped = rawIndex.coerceIn(0, items.size - 1)
            if (clamped != selectedIndex) {
                selectedIndex = clamped
                // Trigger real physical vibration motor pulse + sound click!
                HapticHelper.triggerScrollTick(context, view)
                onIndexSettled(clamped)
            }
        }
    }

    LazyColumn(
        state = listState,
        flingBehavior = snapFlingBehavior,
        modifier = modifier.height(itemHeight * 3),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top buffer so index 0 centers in the highlight pill
        item { Box(modifier = Modifier.height(itemHeight)) }

        itemsIndexed(items) { index, item ->
            val isSelected = index == selectedIndex
            Box(
                modifier = Modifier
                    .height(itemHeight)
                    .width(64.dp)
                    .clickable {
                        scope.launch {
                            listState.animateScrollToItem(index)
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = item,
                    fontSize = if (isSelected) 24.sp else 16.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = if (isSelected) Color.White else Color(0x55FFFFFF),
                    modifier = Modifier.graphicsLayer {
                        scaleX = if (isSelected) 1.15f else 0.85f
                        scaleY = if (isSelected) 1.15f else 0.85f
                    }
                )
            }
        }

        // Bottom buffer so last index centers in the highlight pill
        item { Box(modifier = Modifier.height(itemHeight)) }
    }
}
