package com.minimalphone.launcher

import android.app.role.RoleManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.BatteryManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.WindowCompat
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
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.compose.runtime.collectAsState
import com.minimalphone.launcher.core.crash.CrashReporter
import com.minimalphone.launcher.core.crash.NoOpCrashReporter
import com.minimalphone.launcher.core.system.MonochromeModeHelper
import com.minimalphone.launcher.core.system.NetworkStatusMonitor
import com.minimalphone.launcher.core.system.ScreenStateReceiver
import com.minimalphone.launcher.core.wallpaper.WallpaperHelper
import com.minimalphone.launcher.data.apps.AppRepositoryImpl
import com.minimalphone.launcher.data.economy.EconomyEngineImpl
import com.minimalphone.launcher.data.local.LocalPreferencesStore
import com.minimalphone.launcher.data.local.LocalTaskDataSourceImpl
import com.minimalphone.launcher.data.weather.WeatherRepository
import com.minimalphone.launcher.domain.apps.AppModel
import com.minimalphone.launcher.domain.apps.AppRepository
import com.minimalphone.launcher.domain.apps.EssentialAppType
import com.minimalphone.launcher.domain.economy.EconomyEngine
import com.minimalphone.launcher.domain.economy.EconomyEvent
import com.minimalphone.launcher.domain.friction.FrictionIntervention
import com.minimalphone.launcher.domain.friction.interventions.BreathingIntervention
import com.minimalphone.launcher.domain.productivity.TaskDataSource
import com.minimalphone.launcher.domain.productivity.TaskItem
import com.minimalphone.launcher.domain.usage.UsageStatsHelper
import com.minimalphone.launcher.theme.MiniMalTheme
import com.minimalphone.launcher.ui.AppDrawerScreen
import com.minimalphone.launcher.ui.FrictionHostScreen
import com.minimalphone.launcher.ui.HomeScreen
import com.minimalphone.launcher.ui.OnboardingScreen
import com.minimalphone.launcher.ui.SettingsScreen
import com.minimalphone.launcher.ui.TasksScreen
import com.minimalphone.launcher.ui.UsageScreen
import com.minimalphone.launcher.ui.WeatherScreen
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    // Extensible Core Contracts
    private lateinit var crashReporter: CrashReporter
    private lateinit var prefsStore: LocalPreferencesStore
    private lateinit var taskDataSource: TaskDataSource
    private lateinit var economyEngine: EconomyEngine
    private lateinit var appRepository: AppRepository
    private lateinit var usageStatsHelper: UsageStatsHelper
    private lateinit var activeFriction: FrictionIntervention
    private lateinit var roleRequestLauncher: ActivityResultLauncher<Intent>

    private var isDefaultHomeState by mutableStateOf(false)
    private lateinit var networkMonitor: NetworkStatusMonitor
    private lateinit var weatherRepository: WeatherRepository
    private var screenReceiver: ScreenStateReceiver? = null

    @OptIn(ExperimentalFoundationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Immersive Edge-to-Edge: Hide black status bar strip completely, swipe down to reveal
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        window.navigationBarColor = android.graphics.Color.TRANSPARENT
        WindowCompat.getInsetsController(window, window.decorView)?.apply {
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            hide(WindowInsetsCompat.Type.statusBars())
        }

        // Initialize Core Crash Reporter
        crashReporter = NoOpCrashReporter().apply { initialize() }
        crashReporter.logBreadcrumb("Lifecycle", "MainActivity onCreate")

        // Initialize Data & Domain dependencies
        prefsStore = LocalPreferencesStore(this)
        taskDataSource = LocalTaskDataSourceImpl(prefsStore)
        economyEngine = EconomyEngineImpl(prefsStore, crashReporter)
        appRepository = AppRepositoryImpl(this, prefsStore, crashReporter)
        usageStatsHelper = UsageStatsHelper(this)
        networkMonitor = NetworkStatusMonitor(this)
        weatherRepository = WeatherRepository(prefsStore)

        // Active pluggable friction intervention
        activeFriction = BreathingIntervention(
            countdownSeconds = prefsStore.frictionCountdownSeconds,
            defaultCost = prefsStore.frictionCost
        )

        // Register default home launcher role request
        roleRequestLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            isDefaultHomeState = isDefaultHomeLauncher()
            updateLockScreenReceiver()
        }

        // If first launch is already done, apply wallpaper & grayscale
        if (prefsStore.isFirstLaunchCompleted) {
            Thread {
                if (prefsStore.isPitchBlackWallpaper) {
                    WallpaperHelper.applyPitchBlackWallpaper(this)
                } else {
                    WallpaperHelper.applyDuneWallpaper(this)
                }
            }.start()

            if (prefsStore.isMonochromeEnabled && isDefaultHomeLauncher()) {
                MonochromeModeHelper.enableMonochrome(this)
            }
            updateLockScreenReceiver()
        }

        setContent {
            MiniMalTheme {
                MainContent()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        isDefaultHomeState = isDefaultHomeLauncher()
        if (isDefaultHomeState) {
            if (prefsStore.isMonochromeEnabled) {
                MonochromeModeHelper.enableMonochrome(this)
            }
        } else {
            // If user switched away from MiniMalPhone, safely disable hardware monochrome!
            MonochromeModeHelper.disableMonochrome(this)
        }
        updateLockScreenReceiver()
        WindowCompat.getInsetsController(window, window.decorView)?.apply {
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            hide(WindowInsetsCompat.Type.statusBars())
        }
    }

    private fun updateLockScreenReceiver() {
        if (prefsStore.isCustomLockScreenEnabled && isDefaultHomeLauncher()) {
            if (screenReceiver == null) {
                try {
                    val filter = IntentFilter(Intent.ACTION_SCREEN_OFF)
                    screenReceiver = ScreenStateReceiver()
                    registerReceiver(screenReceiver, filter)
                } catch (ignored: Exception) {}
            }
        } else {
            screenReceiver?.let {
                try { unregisterReceiver(it) } catch (e: Exception) {}
                screenReceiver = null
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        screenReceiver?.let {
            try { unregisterReceiver(it) } catch (e: Exception) {}
        }
    }

    private fun isDefaultHomeLauncher(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val roleManager = getSystemService(RoleManager::class.java)
            roleManager?.isRoleHeld(RoleManager.ROLE_HOME) == true
        } else {
            val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
            val resolveInfo = packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY)
            resolveInfo?.activityInfo?.packageName == packageName
        }
    }

    private fun requestSetDefaultLauncher() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val roleManager = getSystemService(RoleManager::class.java)
                if (roleManager != null && roleManager.isRoleAvailable(RoleManager.ROLE_HOME)) {
                    val intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_HOME)
                    roleRequestLauncher.launch(intent)
                    return
                }
            }
            val intent = Intent(Settings.ACTION_HOME_SETTINGS)
            startActivity(intent)
        } catch (e: Exception) {
            crashReporter.logException(e, "Failed to launch home role request")
        }
    }

    private fun triggerWallpaperApply() {
        Thread {
            val success = WallpaperHelper.applyDuneWallpaper(this)
            runOnUiThread {
                if (success) {
                    Toast.makeText(this, "Wallpaper applied to Home & Lock Screen!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Could not set wallpaper", Toast.LENGTH_SHORT).show()
                }
            }
        }.start()
    }

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    private fun MainContent() {
        val scope = rememberCoroutineScope()
        // Page 0: Weather, Page 1: Tasks, Page 2: Home (default), Page 3: App Drawer, Page 4: Usage
        val pagerState = rememberPagerState(initialPage = 2, pageCount = { 5 })

        val allApps = remember { mutableStateListOf<AppModel>() }
        val tasks = remember { mutableStateListOf<TaskItem>() }

        var focusCredits by remember { mutableIntStateOf(economyEngine.getCurrentBalance()) }
        var batteryPct by remember { mutableIntStateOf(-1) }
        val networkStatus by networkMonitor.status.collectAsState()
        var frictionTargetApp by remember { mutableStateOf<AppModel?>(null) }
        var isOnboarding by remember { mutableStateOf(!prefsStore.isFirstLaunchCompleted) }
        var isSettingsOpen by remember { mutableStateOf(false) }

        val activeIntervention = remember(prefsStore.frictionCountdownSeconds, prefsStore.frictionCost) {
            BreathingIntervention(
                countdownSeconds = prefsStore.frictionCountdownSeconds,
                defaultCost = prefsStore.frictionCost
            )
        }

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
            isDefaultHomeState = isDefaultHomeLauncher()
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

        // Back button: return to Home (Page 2), dismiss settings, or dismiss friction
        BackHandler(enabled = isSettingsOpen || frictionTargetApp != null || (!isOnboarding && pagerState.currentPage != 2)) {
            if (isSettingsOpen) {
                isSettingsOpen = false
                reloadData()
                updateLockScreenReceiver()
            } else if (frictionTargetApp != null) {
                frictionTargetApp = null
            } else if (pagerState.currentPage != 2) {
                scope.launch { pagerState.animateScrollToPage(2) }
            }
        }

        fun handleAppClick(app: AppModel) {
            if (prefsStore.isMonochromeEnabled && isDefaultHomeState) {
                MonochromeModeHelper.enableMonochrome(this@MainActivity)
            }
            if (app.isDistraction) {
                frictionTargetApp = app
            } else {
                appRepository.launchApp(app.packageName)
            }
        }

        if (isOnboarding) {
            OnboardingScreen(
                prefsStore = prefsStore,
                apps = allApps,
                isDefaultLauncher = isDefaultHomeState,
                onRequestSetDefaultLauncher = { requestSetDefaultLauncher() },
                onToggleDistraction = { app ->
                    scope.launch {
                        appRepository.toggleDistraction(app.packageName)
                        reloadApps()
                    }
                },
                onCompleteOnboarding = {
                    isOnboarding = false
                    reloadData()
                    reloadApps()
                    updateLockScreenReceiver()
                    if (prefsStore.isMonochromeEnabled && isDefaultHomeState) {
                        MonochromeModeHelper.enableMonochrome(this@MainActivity)
                    }
                }
            )
        } else if (isSettingsOpen) {
            SettingsScreen(
                prefsStore = prefsStore,
                economyEngine = economyEngine,
                apps = allApps,
                onToggleDistractionApp = { app ->
                    scope.launch {
                        appRepository.toggleDistraction(app.packageName)
                        reloadApps()
                    }
                },
                onNavigateBack = {
                    isSettingsOpen = false
                    reloadData()
                    updateLockScreenReceiver()
                }
            )
        } else if (frictionTargetApp != null) {
            val target = frictionTargetApp!!
            FrictionHostScreen(
                intervention = activeIntervention,
                appName = target.label,
                creditCost = activeIntervention.defaultCost,
                userCredits = focusCredits,
                onProceed = {
                    val success = economyEngine.processEvent(
                        EconomyEvent.AppFrictionBypassed(
                            packageName = target.packageName,
                            costAmount = activeIntervention.defaultCost
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
                    0 -> WeatherScreen(
                        weatherRepository = weatherRepository,
                        onNavigateBack = {
                            scope.launch { pagerState.animateScrollToPage(1) }
                        }
                    )
                    1 -> TasksScreen(
                        tasks = tasks,
                        focusCredits = focusCredits,
                        onToggleTask = { task ->
                            scope.launch {
                                val nextStatus = !task.isCompleted
                                taskDataSource.updateTaskStatus(task.id, nextStatus)
                                if (nextStatus) {
                                    economyEngine.processEvent(EconomyEvent.TaskCompleted(task.id, task.rewardPoints))
                                    Toast.makeText(this@MainActivity, "+${task.rewardPoints} points earned!", Toast.LENGTH_SHORT).show()
                                } else {
                                    economyEngine.processEvent(EconomyEvent.TaskUncompleted(task.id, task.rewardPoints))
                                }
                                reloadData()
                            }
                        },
                        onSaveTask = { task ->
                            scope.launch {
                                taskDataSource.saveTask(task)
                                reloadData()
                            }
                        },
                        onSwapTasks = { taskA, taskB ->
                            scope.launch {
                                taskDataSource.swapTaskTimingsAndPositions(taskA.id, taskB.id)
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
                    2 -> HomeScreen(
                        tasks = tasks,
                        focusCredits = focusCredits,
                        batteryPct = batteryPct,
                        networkStatus = networkStatus,
                        isDefaultLauncher = isDefaultHomeState,
                        is24Hour = prefsStore.is24HourFormat,
                        onLaunchEssential = { type ->
                            if (prefsStore.isMonochromeEnabled && isDefaultHomeState) {
                                MonochromeModeHelper.enableMonochrome(this@MainActivity)
                            }
                            appRepository.launchEssentialApp(type)
                        },
                        onToggleTask = { task ->
                            scope.launch {
                                val nextStatus = !task.isCompleted
                                taskDataSource.updateTaskStatus(task.id, nextStatus)
                                if (nextStatus) {
                                    economyEngine.processEvent(EconomyEvent.TaskCompleted(task.id, task.rewardPoints))
                                    Toast.makeText(this@MainActivity, "+${task.rewardPoints} points earned!", Toast.LENGTH_SHORT).show()
                                } else {
                                    economyEngine.processEvent(EconomyEvent.TaskUncompleted(task.id, task.rewardPoints))
                                }
                                reloadData()
                            }
                        },
                        onRequestSetDefaultLauncher = {
                            requestSetDefaultLauncher()
                        },
                        onApplyDeviceWallpaper = {
                            triggerWallpaperApply()
                        },
                        onNavigateToTasks = {
                            scope.launch { pagerState.animateScrollToPage(1) }
                        },
                        onNavigateToDrawer = {
                            scope.launch { pagerState.animateScrollToPage(3) }
                        },
                        onOpenSettings = {
                            isSettingsOpen = true
                        }
                    )
                    3 -> AppDrawerScreen(
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
                        },
                        onOpenSettings = {
                            isSettingsOpen = true
                        }
                    )
                    4 -> UsageScreen(
                        usageStatsHelper = usageStatsHelper,
                        onLaunchApp = { packageName ->
                            if (prefsStore.isMonochromeEnabled && isDefaultHomeState) {
                                MonochromeModeHelper.enableMonochrome(this@MainActivity)
                            }
                            appRepository.launchApp(packageName)
                        },
                        onNavigateBack = {
                            scope.launch { pagerState.animateScrollToPage(3) }
                        }
                    )
                }
            }
        }
    }
}
