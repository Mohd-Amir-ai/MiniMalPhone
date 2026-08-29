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
import com.minimalphone.launcher.data.PreferencesManager
import com.minimalphone.launcher.model.AppInfo
import com.minimalphone.launcher.model.TaskItem
import com.minimalphone.launcher.theme.MiniMalTheme
import com.minimalphone.launcher.ui.AppDrawerScreen
import com.minimalphone.launcher.ui.FrictionScreen
import com.minimalphone.launcher.ui.HomeScreen
import com.minimalphone.launcher.ui.TasksScreen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {

    private lateinit var prefs: PreferencesManager

    @OptIn(ExperimentalFoundationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefs = PreferencesManager(this)

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

        val allApps = remember { mutableStateListOf<AppInfo>() }
        val tasks = remember { mutableStateListOf<TaskItem>() }
        var focusCredits by remember { mutableIntStateOf(prefs.focusCredits) }
        var batteryPct by remember { mutableIntStateOf(-1) }
        var frictionTargetApp by remember { mutableStateOf<AppInfo?>(null) }

        // Load tasks and initial apps
        fun reloadData() {
            tasks.clear()
            tasks.addAll(prefs.getTasks())
            focusCredits = prefs.focusCredits
        }

        fun reloadApps() {
            scope.launch(Dispatchers.IO) {
                val pm = packageManager
                val intent = Intent(Intent.ACTION_MAIN, null).apply {
                    addCategory(Intent.CATEGORY_LAUNCHER)
                }
                val resolveInfos = pm.queryIntentActivities(intent, 0)
                val favorites = prefs.getFavorites()
                val hidden = prefs.getHiddenApps()
                val distractions = prefs.getDistractionApps()

                val appList = resolveInfos
                    .filter { it.activityInfo.packageName != packageName }
                    .map { resolveInfo ->
                        val pkg = resolveInfo.activityInfo.packageName
                        val label = resolveInfo.loadLabel(pm).toString()
                        AppInfo(
                            label = label,
                            packageName = pkg,
                            isFavorite = favorites.contains(pkg),
                            isHidden = hidden.contains(pkg),
                            isDistraction = distractions.contains(pkg)
                        )
                    }
                    .sortedBy { it.label.lowercase() }

                withContext(Dispatchers.Main) {
                    allApps.clear()
                    allApps.addAll(appList)

                    // Seed default favorites if completely empty (e.g. first run)
                    if (favorites.isEmpty() && appList.isNotEmpty()) {
                        val candidates = appList.filter {
                            val lower = it.label.lowercase()
                            lower.contains("phone") || lower.contains("message") ||
                                lower.contains("camera") || lower.contains("browser") || lower.contains("chrome")
                        }.take(4)
                        candidates.forEach { prefs.toggleFavorite(it.packageName) }
                        reloadApps()
                    }
                }
            }
        }

        LaunchedEffect(Unit) {
            reloadData()
            reloadApps()
        }

        // Battery listener
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

        // Back button handling: Return to Home if on another page or dismiss friction
        BackHandler(enabled = frictionTargetApp != null || pagerState.currentPage != 1) {
            if (frictionTargetApp != null) {
                frictionTargetApp = null
            } else if (pagerState.currentPage != 1) {
                scope.launch { pagerState.animateScrollToPage(1) }
            }
        }

        fun launchActualApp(pkg: String) {
            try {
                val launchIntent = packageManager.getLaunchIntentForPackage(pkg)
                if (launchIntent != null) {
                    startActivity(launchIntent)
                } else {
                    Toast.makeText(this@MainActivity, "Cannot launch app", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@MainActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }

        fun handleAppClick(app: AppInfo) {
            if (app.isDistraction) {
                frictionTargetApp = app
            } else {
                launchActualApp(app.packageName)
            }
        }

        val favoriteApps = allApps.filter { it.isFavorite && !it.isHidden }

        if (frictionTargetApp != null) {
            val target = frictionTargetApp!!
            FrictionScreen(
                appName = target.label,
                creditCost = 10,
                userCredits = focusCredits,
                onProceed = {
                    if (prefs.spendCredits(10)) {
                        focusCredits = prefs.focusCredits
                        frictionTargetApp = null
                        launchActualApp(target.packageName)
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
                            val newCompleted = !task.isCompleted
                            prefs.updateTaskCompletion(task.id, newCompleted)
                            if (newCompleted) {
                                prefs.addCredits(task.creditReward)
                                Toast.makeText(this@MainActivity, "+${task.creditReward} Focus Credits earned!", Toast.LENGTH_SHORT).show()
                            } else {
                                prefs.spendCredits(task.creditReward)
                            }
                            reloadData()
                        },
                        onAddTask = { title ->
                            val newTask = TaskItem(title = title)
                            prefs.saveTask(newTask)
                            reloadData()
                        },
                        onDeleteTask = { id ->
                            prefs.deleteTask(id)
                            reloadData()
                        }
                    )
                    1 -> HomeScreen(
                        favoriteApps = favoriteApps,
                        tasks = tasks,
                        focusCredits = focusCredits,
                        batteryPct = batteryPct,
                        onLaunchApp = { handleAppClick(it) },
                        onToggleTask = { task ->
                            val newCompleted = !task.isCompleted
                            prefs.updateTaskCompletion(task.id, newCompleted)
                            if (newCompleted) {
                                prefs.addCredits(task.creditReward)
                                Toast.makeText(this@MainActivity, "+${task.creditReward} Focus Credits earned!", Toast.LENGTH_SHORT).show()
                            } else {
                                prefs.spendCredits(task.creditReward)
                            }
                            reloadData()
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
                            prefs.toggleFavorite(app.packageName)
                            reloadApps()
                        },
                        onToggleDistraction = { app ->
                            prefs.toggleDistraction(app.packageName)
                            reloadApps()
                        },
                        onToggleHide = { app ->
                            prefs.toggleHidden(app.packageName)
                            reloadApps()
                        }
                    )
                }
            }
        }
    }
}
