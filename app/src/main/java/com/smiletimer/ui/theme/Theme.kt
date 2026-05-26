package com.smiletimer.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary        = ButtonStart,
    secondary      = ButtonPause,
    tertiary       = ButtonReset,
    background     = BackgroundDark,
    surface        = SurfaceDark,
    surfaceVariant = SurfaceVariant,
    onBackground   = TextPrimary,
    onSurface      = TextPrimary,
    onPrimary      = BackgroundDark,
)

@Composable
fun SmileTimerTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
