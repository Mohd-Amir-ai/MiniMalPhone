package com.minimalphone.launcher.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
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
import com.minimalphone.launcher.model.AppInfo
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
    apps: List<AppInfo>,
    onLaunchApp: (AppInfo) -> Unit,
    onToggleFavorite: (AppInfo) -> Unit,
    onToggleDistraction: (AppInfo) -> Unit,
    onToggleHide: (AppInfo) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }

    val filteredApps = remember(searchQuery, apps) {
        apps.filter { !it.isHidden }
            .filter { it.label.contains(searchQuery, ignoreCase = true) }
            .sortedBy { it.label.lowercase() }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Black)
            .padding(horizontal = 24.dp, vertical = 24.dp)
    ) {
        // Search bar
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
            modifier = Modifier.fillMaxWidth()
        )

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
                    onLaunch = { onLaunchApp(app) },
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
    app: AppInfo,
    onLaunch: () -> Unit,
    onToggleFavorite: () -> Unit,
    onToggleDistraction: () -> Unit,
    onToggleHide: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(6.dp))
                .combinedClickable(
                    onClick = onLaunch,
                    onLongClick = { showMenu = true }
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
