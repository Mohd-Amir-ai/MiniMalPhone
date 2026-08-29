package com.minimalphone.launcher

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.minimalphone.launcher.core.crash.CrashReporter
import com.minimalphone.launcher.core.crash.NoOpCrashReporter
import com.minimalphone.launcher.data.apps.AppRepositoryImpl
import com.minimalphone.launcher.data.economy.EconomyEngineImpl
import com.minimalphone.launcher.data.local.LocalPreferencesStore
import com.minimalphone.launcher.data.local.LocalTaskDataSourceImpl
import com.minimalphone.launcher.domain.apps.AppModel
import com.minimalphone.launcher.domain.apps.AppRepository
import com.minimalphone.launcher.domain.apps.EssentialAppType
import com.minimalphone.launcher.domain.economy.EconomyEngine
import com.minimalphone.launcher.domain.economy.EconomyEvent
import com.minimalphone.launcher.domain.friction.FrictionIntervention
import com.minimalphone.launcher.domain.friction.interventions.BreathingIntervention
import com.minimalphone.launcher.domain.productivity.EventItem
import com.minimalphone.launcher.domain.productivity.TaskDataSource
import com.minimalphone.launcher.domain.productivity.TaskItem
import com.minimalphone.launcher.theme.MiniMalTheme
import com.minimalphone.launcher.ui.AppDrawerScreen
import com.minimalphone.launcher.ui.FrictionHostScreen
import com.minimalphone.launcher.ui.HomeScreen
import com.minimalphone.launcher.ui.TasksScreen
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    // Extensible Core Contracts
    private lateinit var crashReporter: CrashReporter
    private lateinit var prefsStore: LocalPreferencesStore
    private lateinit var taskDataSource: TaskDataSource
    private lateinit var economyEngine: EconomyEngine
    private lateinit var appRepository: AppRepository
    private lateinit var activeFriction: FrictionIntervention

    @OptIn(ExperimentalFoundationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize Core Crash Reporter (ready for Sentry / Firebase)
        crashReporter = NoOpCrashReporter().apply { initialize() }
        crashReporter.logBreadcrumb("Lifecycle", "MainActivity onCreate")

        // Initialize Data & Domain dependencies
        prefsStore = LocalPreferencesStore(this)
        taskDataSource = LocalTaskDataSourceImpl(prefsStore)
        economyEngine = EconomyEngineImpl(prefsStore, crashReporter)
        appRepository = AppRepositoryImpl(this, prefsStore, crashReporter)

        // Active pluggable friction intervention
        activeFriction = BreathingIntervention(countdownSeconds = 5, defaultCost = 10)

        setContent {
            MiniMalTheme {
                MainContent()
            }
        }
    }

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    private fun MainContent() {
        val scope = rememberCoroutineScope()
        // Page 0: Tasks, Page 1: Home (default), Page 2: App Drawer
        val pagerState = rememberPagerState(initialPage = 1, pageCount = { 3 })

        val allApps = remember { mutableStateListOf<AppModel>() }
        val tasks = remember { mutableStateListOf<TaskItem>() }
        val events = remember {
            mutableStateListOf(
                EventItem(id = 1L, title = "Make a post on X", timeFormatted = "2:30 PM"),
                EventItem(id = 2L, title = "Study book", timeFormatted = "3:00 PM")
            )
        }

        var focusCredits by remember { mutableIntStateOf(economyEngine.getCurrentBalance()) }
        var batteryPct by remember { mutableIntStateOf(-1) }
        var frictionTargetApp by remember { mutableStateOf<AppModel?>(null) }

        fun reloadData() {
            scope.launch {
                tasks.clear()
                tasks.addAll(taskDataSource.getTasks())
                focusCredits = economyEngine.getCurrentBalance()
            }
        }

        fun reloadApps() {
            scope.launch {
                val apps = appRepository.getInstalledApps()
                allApps.clear()
                allApps.addAll(apps)
            }
        }

        LaunchedEffect(Unit) {
            reloadData()
            reloadApps()
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
            onDispose {
                unregisterReceiver(receiver)
            }
        }

        // Back button: return to Home or dismiss friction
        BackHandler(enabled = frictionTargetApp != null || pagerState.currentPage != 1) {
            if (frictionTargetApp != null) {
                frictionTargetApp = null
            } else if (pagerState.currentPage != 1) {
                scope.launch { pagerState.animateScrollToPage(1) }
            }
        }

        fun handleAppClick(app: AppModel) {
            if (app.isDistraction) {
                frictionTargetApp = app
            } else {
                appRepository.launchApp(app.packageName)
            }
        }

        if (frictionTargetApp != null) {
            val target = frictionTargetApp!!
            FrictionHostScreen(
                intervention = activeFriction,
                appName = target.label,
                creditCost = activeFriction.defaultCost,
                userCredits = focusCredits,
                onProceed = {
                    val success = economyEngine.processEvent(
                        EconomyEvent.AppFrictionBypassed(
                            packageName = target.packageName,
                            costAmount = activeFriction.defaultCost
                        )
                    )
                    if (success) {
                        focusCredits = economyEngine.getCurrentBalance()
                        frictionTargetApp = null
                        appRepository.launchApp(target.packageName)
                    } else {
                        Toast.makeText(this@MainActivity, "Not enough Focus Credits!", Toast.LENGTH_SHORT).show()
                    }
                },
                onCancel = {
                    frictionTargetApp = null
                }
            )
        } else {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                when (page) {
                    0 -> TasksScreen(
                        tasks = tasks,
                        focusCredits = focusCredits,
                        onToggleTask = { task ->
                            scope.launch {
                                val nextStatus = !task.isCompleted
                                taskDataSource.updateTaskStatus(task.id, nextStatus)
                                if (nextStatus) {
                                    economyEngine.processEvent(EconomyEvent.TaskCompleted(task.id, task.rewardPoints))
                                    Toast.makeText(this@MainActivity, "+${task.rewardPoints} Focus Credits earned!", Toast.LENGTH_SHORT).show()
                                } else {
                                    economyEngine.processEvent(EconomyEvent.TaskUncompleted(task.id, task.rewardPoints))
                                }
                                reloadData()
                            }
                        },
                        onAddTask = { title ->
                            scope.launch {
                                taskDataSource.addTask(title = title)
                                reloadData()
                            }
                        },
                        onDeleteTask = { id ->
                            scope.launch {
                                taskDataSource.deleteTask(id)
                                reloadData()
                            }
                        }
                    )
                    1 -> HomeScreen(
                        events = events,
                        focusCredits = focusCredits,
                        batteryPct = batteryPct,
                        onLaunchEssential = { type ->
                            appRepository.launchEssentialApp(type)
                        },
                        onToggleEvent = { event ->
                            val index = events.indexOfFirst { it.id == event.id }
                            if (index != -1) {
                                events[index] = event.copy(isCompleted = !event.isCompleted)
                            }
                        },
                        onNavigateToTasks = {
                            scope.launch { pagerState.animateScrollToPage(0) }
                        },
                        onNavigateToDrawer = {
                            scope.launch { pagerState.animateScrollToPage(2) }
                        }
                    )
                    2 -> AppDrawerScreen(
                        apps = allApps,
                        onLaunchApp = { handleAppClick(it) },
                        onToggleFavorite = { app ->
                            scope.launch {
                                appRepository.toggleFavorite(app.packageName)
                                reloadApps()
                            }
                        },
                        onToggleDistraction = { app ->
                            scope.launch {
                                appRepository.toggleDistraction(app.packageName)
                                reloadApps()
                            }
                        },
                        onToggleHide = { app ->
                            scope.launch {
                                appRepository.toggleHidden(app.packageName)
                                reloadApps()
                            }
                        }
                    )
                }
            }
        }
    }
}
