package com.minimalphone.launcher.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val MonochromeColorScheme = darkColorScheme(
    primary = PureWhite,
    onPrimary = Black,
    primaryContainer = DarkCard,
    onPrimaryContainer = PureWhite,
    secondary = ChalkWhite,
    onSecondary = Black,
    background = Black,
    onBackground = ChalkWhite,
    surface = Black,
    onSurface = ChalkWhite,
    surfaceVariant = DarkSurface,
    onSurfaceVariant = LightGray
)

@Composable
fun MiniMalTheme(
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Black.toArgb()
            window.navigationBarColor = Black.toArgb()
            val insetsController = WindowCompat.getInsetsController(window, view)
            insetsController.isAppearanceLightStatusBars = false
            insetsController.isAppearanceLightNavigationBars = false
        }
    }

    MaterialTheme(
        colorScheme = MonochromeColorScheme,
        typography = Typography,
        content = content
    )
}
