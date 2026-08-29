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
import com.minimalphone.launcher.domain.apps.EssentialAppType
import com.minimalphone.launcher.domain.productivity.EventItem
import com.minimalphone.launcher.theme.PaperAccentDot
import com.minimalphone.launcher.theme.PaperChalkWhite
import com.minimalphone.launcher.theme.PaperDarkBackground
import com.minimalphone.launcher.theme.PaperDarkCard
import com.minimalphone.launcher.theme.PaperHairlineBorder
import com.minimalphone.launcher.theme.PaperMutedInk
import com.minimalphone.launcher.theme.PaperPencilGray
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
    var currentTime by remember { mutableStateOf("") }
    var currentDate by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
        val dateFormat = SimpleDateFormat("EEEE, MMMM d", Locale.getDefault())
        while (true) {
            val now = Date()
            currentTime = timeFormat.format(now)
            currentDate = dateFormat.format(now)
            delay(1000L)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(PaperDarkBackground)
            .padding(horizontal = 28.dp, vertical = 34.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // TOP SECTION: Aligned to the Top-Right (Time, Day/Date, Reward Points)
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.End
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End
            ) {
                if (batteryPct >= 0) {
                    Text(
                        text = "$batteryPct%",
                        style = MaterialTheme.typography.labelSmall,
                        color = PaperMutedInk,
                        modifier = Modifier.padding(end = 10.dp)
                    )
                }

                // Focus Reward Points Pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(PaperDarkCard)
                        .border(1.dp, PaperHairlineBorder, RoundedCornerShape(4.dp))
                        .clickable { onNavigateToTasks() }
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "$focusCredits pts",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium,
                        color = PaperChalkWhite
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Large Minimal Time
            Text(
                text = currentTime.ifEmpty { "--:--" },
                style = MaterialTheme.typography.displayLarge,
                fontWeight = FontWeight.Light,
                letterSpacing = (-1).sp,
                color = PaperChalkWhite
            )

            // Day and Date
            Text(
                text = currentDate.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                letterSpacing = 2.sp,
                color = PaperPencilGray
            )
        }

        // MIDDLE SECTION: Upcoming Events (Paper Display Card)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
        ) {
            Text(
                text = "UPCOMING SCHEDULE",
                style = MaterialTheme.typography.labelSmall,
                letterSpacing = 2.5.sp,
                color = PaperPencilGray
            )

            Spacer(modifier = Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(PaperDarkCard)
                    .border(1.dp, PaperHairlineBorder, RoundedCornerShape(6.dp))
                    .padding(horizontal = 18.dp, vertical = 18.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
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
                                // Tactile paper dot / indicator
                                Box(
                                    modifier = Modifier
                                        .size(7.dp)
                                        .clip(CircleShape)
                                        .background(if (event.isCompleted) PaperMutedInk else PaperAccentDot)
                                )

                                Spacer(modifier = Modifier.width(14.dp))

                                Text(
                                    text = event.title,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Normal,
                                    color = if (event.isCompleted) PaperMutedInk else PaperChalkWhite
                                )
                            }

                            Text(
                                text = event.timeFormatted,
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (event.isCompleted) PaperMutedInk else PaperPencilGray
                            )
                        }
                    }
                }
            }
        }

        // BOTTOM SECTION: 4 Essential Apps (Phone, Google Search, Messaging, Camera)
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "ESSENTIALS",
                style = MaterialTheme.typography.labelSmall,
                letterSpacing = 2.5.sp,
                color = PaperPencilGray
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 1. Phone
                Text(
                    text = "phone",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Light,
                    color = PaperChalkWhite,
                    modifier = Modifier
                        .clickable { onLaunchEssential(EssentialAppType.PHONE) }
                        .padding(vertical = 8.dp)
                )

                // 2. Google / Search
                Text(
                    text = "google",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Light,
                    color = PaperChalkWhite,
                    modifier = Modifier
                        .clickable { onLaunchEssential(EssentialAppType.SEARCH) }
                        .padding(vertical = 8.dp)
                )

                // 3. Messages
                Text(
                    text = "messages",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Light,
                    color = PaperChalkWhite,
                    modifier = Modifier
                        .clickable { onLaunchEssential(EssentialAppType.MESSAGES) }
                        .padding(vertical = 8.dp)
                )

                // 4. Camera
                Text(
                    text = "camera",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Light,
                    color = PaperChalkWhite,
                    modifier = Modifier
                        .clickable { onLaunchEssential(EssentialAppType.CAMERA) }
                        .padding(vertical = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Minimal Navigation Footnotes
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "← Daily Tasks",
                    style = MaterialTheme.typography.labelSmall,
                    color = PaperMutedInk,
                    modifier = Modifier
                        .clickable { onNavigateToTasks() }
                        .padding(vertical = 4.dp)
                )

                Text(
                    text = "All Apps →",
                    style = MaterialTheme.typography.labelSmall,
                    color = PaperMutedInk,
                    modifier = Modifier
                        .clickable { onNavigateToDrawer() }
                        .padding(vertical = 4.dp)
                )
            }
        }
    }
}
