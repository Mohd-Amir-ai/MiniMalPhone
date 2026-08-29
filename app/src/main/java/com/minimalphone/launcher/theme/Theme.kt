package com.minimalphone.launcher.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val PaperDarkColorScheme = darkColorScheme(
    primary = PaperChalkWhite,
    onPrimary = PaperDarkBackground,
    primaryContainer = PaperDarkCard,
    onPrimaryContainer = PaperChalkWhite,
    secondary = PaperPencilGray,
    onSecondary = PaperDarkBackground,
    background = PaperDarkBackground,
    onBackground = PaperChalkWhite,
    surface = PaperDarkBackground,
    onSurface = PaperChalkWhite,
    surfaceVariant = PaperDarkSurface,
    onSurfaceVariant = PaperPencilGray,
    outline = PaperHairlineBorder
)

@Composable
fun MiniMalTheme(
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = PaperDarkBackground.toArgb()
            window.navigationBarColor = PaperDarkBackground.toArgb()
            val insetsController = WindowCompat.getInsetsController(window, view)
            insetsController.isAppearanceLightStatusBars = false
            insetsController.isAppearanceLightNavigationBars = false
        }
    }

    MaterialTheme(
        colorScheme = PaperDarkColorScheme,
        typography = Typography,
        content = content
    )
}
