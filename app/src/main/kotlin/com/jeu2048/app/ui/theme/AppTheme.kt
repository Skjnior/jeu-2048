package com.jeu2048.app.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF6750A4),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFEADDFF),
    onPrimaryContainer = Color(0xFF21005D),
    background = Color(0xFFF8F5F0),
    onBackground = Color(0xFF1C1B1F),
    surface = Color(0xFFF8F5F0),
    onSurface = Color(0xFF1C1B1F),
    surfaceVariant = Color(0xFFE7E0EC),
    onSurfaceVariant = Color(0xFF49454F),
    outline = Color(0xFF79747E)
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFD0BCFF),
    onPrimary = Color(0xFF381E72),
    primaryContainer = Color(0xFF4F378B),
    onPrimaryContainer = Color(0xFFEADDFF),
    background = Color(0xFF1C1B1F),
    onBackground = Color(0xFFE6E1E5),
    surface = Color(0xFF1C1B1F),
    onSurface = Color(0xFFE6E1E5),
    surfaceVariant = Color(0xFF49454F),
    onSurfaceVariant = Color(0xFFCAC4D0),
    outline = Color(0xFF938F99)
)

private val ColorfulColorScheme = darkColorScheme(
    primary = Color(0xFF6C5CE7),
    onPrimary = Color.White,
    primaryContainer = Color(0xFF2D2D44),
    onPrimaryContainer = Color(0xFFE0E0E0),
    background = Color(0xFF0F0F1A),
    onBackground = Color(0xFFE6E1E5),
    surface = Color(0xFF2D2D44),
    onSurface = Color(0xFFE6E1E5),
    surfaceVariant = Color(0xFF3D3D5C),
    onSurfaceVariant = Color(0xFFCAC4D0),
    outline = Color(0xFF6C5CE7)
)

@Composable
fun Jeu2048Theme(
    themeIndex: Int = 0,
    content: @Composable () -> Unit
) {
    // 0 = Clair, 1 = Système, 2 = Sombre, 3 = Coloré
    val index = themeIndex.coerceIn(0, 3)
    val colorScheme = when (index) {
        2 -> DarkColorScheme
        3 -> ColorfulColorScheme
        1 -> if (isSystemInDarkTheme()) DarkColorScheme else LightColorScheme
        else -> LightColorScheme
    }
    val darkTheme = colorScheme == DarkColorScheme || colorScheme == ColorfulColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            try {
                val ctx = view.context
                if (ctx is Activity) {
                    ctx.window.statusBarColor = colorScheme.surface.toArgb()
                    WindowCompat.getInsetsController(ctx.window, view).apply {
                        setAppearanceLightStatusBars(!darkTheme)
                    }
                }
            } catch (_: Exception) { }
        }
    }
    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
