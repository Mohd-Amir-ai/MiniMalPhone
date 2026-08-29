package com.minimalphone.launcher.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.minimalphone.launcher.domain.usage.AppUsageItem
import com.minimalphone.launcher.domain.usage.DailyUsageSummary
import com.minimalphone.launcher.domain.usage.UsageStatsHelper
import com.minimalphone.launcher.theme.MidGray
import com.minimalphone.launcher.theme.OffWhite
import com.minimalphone.launcher.theme.PureBlack
import com.minimalphone.launcher.theme.PureWhite
import kotlinx.coroutines.delay

@Composable
fun UsageScreen(
    usageStatsHelper: UsageStatsHelper,
    onLaunchApp: (String) -> Unit = {},
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var hasPermission by remember { mutableStateOf(usageStatsHelper.hasUsagePermission()) }
    var usageSummary by remember { mutableStateOf(DailyUsageSummary(0L, emptyList())) }

    // Periodic refresh
    LaunchedEffect(Unit) {
        while (true) {
            hasPermission = usageStatsHelper.hasUsagePermission()
            if (hasPermission) {
                usageSummary = usageStatsHelper.getDailyUsageSummary()
            }
            delay(15_000L)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(PureBlack)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp, vertical = 14.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = PureWhite
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                Text(
                    text = "APPLICATION USAGE",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp,
                    color = PureWhite
                )

                Spacer(modifier = Modifier.weight(1f))
                Spacer(modifier = Modifier.size(36.dp))
            }

            Spacer(modifier = Modifier.height(28.dp))

            if (!hasPermission) {
                // Permission Request Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF141517))
                            .border(1.dp, Color(0xFF24262A), RoundedCornerShape(12.dp))
                            .padding(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.HourglassEmpty,
                            contentDescription = "Usage Access",
                            tint = PureWhite,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Usage Access Required",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = PureWhite
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Grant permission to view your daily screen time and monitor app usage.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MidGray,
                            lineHeight = 20.sp
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Button(
                            onClick = { usageStatsHelper.openUsageSettings() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PureWhite,
                                contentColor = PureBlack
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth().height(46.dp)
                        ) {
                            Text(
                                text = "Grant Usage Access →",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = PureBlack
                            )
                        }
                    }
                }
            } else {
                // Big text in top middle: Today's Screen Time
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = usageSummary.formattedTotalTime,
                        fontSize = 68.sp,
                        lineHeight = 72.sp,
                        fontWeight = FontWeight.Light,
                        letterSpacing = (-2).sp,
                        color = PureWhite
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "TODAY'S SCREEN TIME",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 2.sp,
                        color = MidGray
                    )
                }

                Spacer(modifier = Modifier.height(36.dp))

                // Section Title
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "MOST USED APPS",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp,
                        color = MidGray
                    )
                    Text(
                        text = "${usageSummary.topApps.size} apps active",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF6E7179)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Ranked App List
                val maxDuration = usageSummary.topApps.firstOrNull()?.totalTimeInForegroundMs ?: 1L

                LazyColumn(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (usageSummary.topApps.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 40.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No app activity recorded today",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MidGray
                                )
                            }
                        }
                    } else {
                        itemsIndexed(
                            items = usageSummary.topApps,
                            key = { _, item -> item.packageName }
                        ) { index, app ->
                            AppUsageRow(
                                rank = index + 1,
                                app = app,
                                maxDuration = maxDuration,
                                onClick = { onLaunchApp(app.packageName) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AppUsageRow(
    rank: Int,
    app: AppUsageItem,
    maxDuration: Long,
    onClick: () -> Unit
) {
    val proportion = if (maxDuration > 0) {
        (app.totalTimeInForegroundMs.toFloat() / maxDuration.toFloat()).coerceIn(0.05f, 1f)
    } else 0.05f

    val animatedProgress by animateFloatAsState(targetValue = proportion, label = "usageBar")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF141517))
            .border(1.dp, Color(0xFF222428), RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "$rank",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF6E7179),
                    modifier = Modifier.width(20.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = app.appName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = PureWhite
                )
            }

            Text(
                text = app.formattedDuration,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
                color = OffWhite
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Monochrome relative usage bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(CircleShape)
                .background(Color(0xFF222428))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedProgress)
                    .fillMaxHeight()
                    .clip(CircleShape)
                    .background(OffWhite)
            )
        }
    }
}
