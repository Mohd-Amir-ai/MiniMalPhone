package com.minimalphone.launcher.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.minimalphone.launcher.domain.apps.AppModel
import com.minimalphone.launcher.theme.AccentBorder
import com.minimalphone.launcher.theme.Black
import com.minimalphone.launcher.theme.ChalkWhite
import com.minimalphone.launcher.theme.DarkCard
import com.minimalphone.launcher.theme.LightGray
import com.minimalphone.launcher.theme.MidGray
import com.minimalphone.launcher.theme.PureWhite

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppDrawerScreen(
    apps: List<AppModel>,
    onLaunchApp: (AppModel) -> Unit,
    onToggleFavorite: (AppModel) -> Unit,
    onToggleDistraction: (AppModel) -> Unit,
    onToggleHide: (AppModel) -> Unit,
    onOpenSettings: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }

    val settingsVirtualApp = remember {
        AppModel(
            label = "MiniMalPhone Settings",
            packageName = "com.minimalphone.launcher.settings"
        )
    }

    val filteredApps = remember(searchQuery, apps) {
        val list = apps.filter { !it.isHidden }.toMutableList()
        list.add(settingsVirtualApp)
        list.filter { it.label.contains(searchQuery, ignoreCase = true) }
            .sortedBy { it.label.lowercase() }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Black)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Type to search…", color = MidGray) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = MidGray
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear",
                                tint = LightGray
                            )
                        }
                    }
                },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = ChalkWhite,
                    unfocusedTextColor = ChalkWhite,
                    focusedBorderColor = PureWhite,
                    unfocusedBorderColor = AccentBorder
                ),
                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.width(10.dp))

            IconButton(
                onClick = onOpenSettings,
                modifier = Modifier
                    .size(54.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(DarkCard)
                    .border(1.dp, AccentBorder, RoundedCornerShape(8.dp))
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "MiniMalPhone Settings",
                    tint = ChalkWhite,
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "${filteredApps.size} APPS",
            style = MaterialTheme.typography.labelSmall,
            letterSpacing = 2.sp,
            color = MidGray
        )

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(filteredApps, key = { it.packageName }) { app ->
                AppListItem(
                    app = app,
                    onLaunch = {
                        if (app.packageName == "com.minimalphone.launcher.settings") {
                            onOpenSettings()
                        } else {
                            onLaunchApp(app)
                        }
                    },
                    onToggleFavorite = { onToggleFavorite(app) },
                    onToggleDistraction = { onToggleDistraction(app) },
                    onToggleHide = { onToggleHide(app) }
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun AppListItem(
    app: AppModel,
    onLaunch: () -> Unit,
    onToggleFavorite: () -> Unit,
    onToggleDistraction: () -> Unit,
    onToggleHide: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    val isSettings = app.packageName == "com.minimalphone.launcher.settings"

    Box(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(6.dp))
                .combinedClickable(
                    onClick = onLaunch,
                    onLongClick = { if (!isSettings) showMenu = true }
                )
                .padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = app.label,
                style = MaterialTheme.typography.bodyLarge,
                color = ChalkWhite
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (app.isFavorite) {
                    Text(
                        text = "★",
                        style = MaterialTheme.typography.bodyMedium,
                        color = PureWhite,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                }
                if (app.isDistraction) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(AccentBorder)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "FRICTION",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 9.sp,
                            color = LightGray
                        )
                    }
                }
            }
        }

        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu = false },
            modifier = Modifier.background(DarkCard)
        ) {
            DropdownMenuItem(
                text = {
                    Text(
                        if (app.isFavorite) "Remove from Home" else "Pin to Home",
                        color = ChalkWhite
                    )
                },
                onClick = {
                    onToggleFavorite()
                    showMenu = false
                }
            )
            DropdownMenuItem(
                text = {
                    Text(
                        if (app.isDistraction) "Remove Distraction Friction" else "Mark as Distraction App",
                        color = ChalkWhite
                    )
                },
                onClick = {
                    onToggleDistraction()
                    showMenu = false
                }
            )
            DropdownMenuItem(
                text = {
                    Text("Hide from Drawer", color = LightGray)
                },
                onClick = {
                    onToggleHide()
                    showMenu = false
                }
            )
        }
    }
}
