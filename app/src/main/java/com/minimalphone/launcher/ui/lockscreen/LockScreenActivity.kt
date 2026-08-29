package com.minimalphone.launcher.ui.lockscreen

import android.app.KeyguardManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.SignalCellularAlt
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.minimalphone.launcher.core.system.NetworkStatusMonitor
import com.minimalphone.launcher.data.local.LocalPreferencesStore
import com.minimalphone.launcher.data.local.LocalTaskDataSourceImpl
import com.minimalphone.launcher.domain.productivity.TaskItem
import com.minimalphone.launcher.theme.MiniMalTheme
import com.minimalphone.launcher.ui.components.MonochromeWallpaper
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class LockScreenActivity : ComponentActivity() {

    private lateinit var networkMonitor: NetworkStatusMonitor
    private lateinit var taskDataSource: LocalTaskDataSourceImpl

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Allow display over secure Samsung keyguard when phone wakes up
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
            )
        }

        // Full edge-to-edge immersive: hide system status bar, pull down to reveal
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        window.navigationBarColor = android.graphics.Color.TRANSPARENT
        WindowCompat.getInsetsController(window, window.decorView)?.apply {
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            hide(WindowInsetsCompat.Type.statusBars())
        }

        val prefsStore = LocalPreferencesStore(this)
        taskDataSource = LocalTaskDataSourceImpl(prefsStore)
        networkMonitor = NetworkStatusMonitor(this)

        setContent {
            MiniMalTheme {
                LockScreenContent(
                    networkMonitor = networkMonitor,
                    onUnlockRequested = { unlockDevice() }
                )
            }
        }
    }

    private fun unlockDevice() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val keyguardManager = getSystemService(KeyguardManager::class.java)
            keyguardManager?.requestDismissKeyguard(this, null)
        }
        finish()
    }

    @Composable
    private fun LockScreenContent(
        networkMonitor: NetworkStatusMonitor,
        onUnlockRequested: () -> Unit
    ) {
        val networkState by networkMonitor.status.collectAsState()
        val tasks = remember { mutableStateListOf<TaskItem>() }

        var hourString by remember { mutableStateOf("") }
        var minuteString by remember { mutableStateOf("") }
        var dateString by remember { mutableStateOf("") }
        var batteryPct by remember { mutableIntStateOf(-1) }

        LaunchedEffect(Unit) {
            tasks.clear()
            tasks.addAll(taskDataSource.getTasks())

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

        // Battery state receiver
        DisposableEffect(Unit) {
            val receiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context?, intent: Intent?) {
                    val level = intent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
                    val scale = intent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
                    if (level >= 0 && scale > 0) {
                        batteryPct = (level * 100) / scale
                    }
                }
            }
            registerReceiver(receiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
            onDispose { unregisterReceiver(receiver) }
        }

        fun isWithinNext30Minutes(timeStr: String): Boolean {
            return try {
                val sdf = SimpleDateFormat("h:mm a", Locale.getDefault())
                val parsed = sdf.parse(timeStr.trim()) ?: return false
                val calTask = Calendar.getInstance().apply { time = parsed }
                val taskMins = calTask.get(Calendar.HOUR_OF_DAY) * 60 + calTask.get(Calendar.MINUTE)
                val calNow = Calendar.getInstance()
                val nowMins = calNow.get(Calendar.HOUR_OF_DAY) * 60 + calNow.get(Calendar.MINUTE)
                val diff = taskMins - nowMins
                diff in 0..30
            } catch (e: Exception) {
                false
            }
        }

        val next30MinTasks = tasks.filter { !it.isCompleted && isWithinNext30Minutes(it.scheduledTime) }

        val topPrimaryTextColor = Color(0xFF16181C)
        val topSecondaryTextColor = Color(0xFF484C54)

        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectVerticalDragGestures { _, dragAmount ->
                        // Swipe up gesture (negative dragAmount) unlocks
                        if (dragAmount < -30f) {
                            onUnlockRequested()
                        }
                    }
                }
        ) {
            // Procedural Layered Monochrome Wallpaper
            MonochromeWallpaper()

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .padding(horizontal = 26.dp, vertical = 14.dp)
            ) {
                // Top Right: Real working Wi-Fi, Cellular, Battery
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (networkState.isWifiConnected) {
                        Icon(
                            imageVector = Icons.Default.Wifi,
                            contentDescription = "Wi-Fi Connected",
                            tint = topPrimaryTextColor,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }

                    if (networkState.isCellularConnected) {
                        Icon(
                            imageVector = Icons.Default.SignalCellularAlt,
                            contentDescription = "Cellular Connected",
                            tint = topPrimaryTextColor,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
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

                Spacer(modifier = Modifier.height(16.dp))

                // Stacked Clock Widget (identical to Home Screen)
                Column(horizontalAlignment = Alignment.Start) {
                    Text(
                        text = hourString.ifEmpty { "16" },
                        fontSize = 78.sp,
                        lineHeight = 76.sp,
                        fontWeight = FontWeight.Normal,
                        letterSpacing = (-2).sp,
                        color = topPrimaryTextColor
                    )
                    Text(
                        text = minuteString.ifEmpty { "42" },
                        fontSize = 78.sp,
                        lineHeight = 76.sp,
                        fontWeight = FontWeight.Normal,
                        letterSpacing = (-2).sp,
                        color = topPrimaryTextColor
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = dateString.ifEmpty { "Sat 29 August" },
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = topSecondaryTextColor
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Upcoming Schedules (Next 30 Minutes)
                if (next30MinTasks.isNotEmpty()) {
                    Text(
                        text = "Upcoming schedules (next 30m)",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = topPrimaryTextColor
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        next30MinTasks.forEach { task ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(topPrimaryTextColor)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = task.title,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = topPrimaryTextColor
                                    )
                                }

                                Text(
                                    text = task.scheduledTime,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Normal,
                                    color = topSecondaryTextColor
                                )
                            }
                        }
                    }
                }

                // NO BOTTOM APP BAR! Empty space pushes unlock hint to bottom
                Spacer(modifier = Modifier.weight(1f))

                // Clean Minimal Unlock Area (Swipe up to unlock)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Swipe up to unlock",
                        tint = Color(0x99FFFFFF),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Swipe up to unlock",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0x99FFFFFF),
                        letterSpacing = 1.sp
                    )
                }
            }
        }
    }
}
