package com.osssh.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Dark = darkColorScheme(
    primary = Color(0xFF7DD3FC),
    surface = Color(0xFF0B1220),
    background = Color(0xFF070C16),
    onBackground = Color(0xFFE2E8F0),
)

private val Light = lightColorScheme(primary = Color(0xFF0369A1))

@Composable
fun OSSSHTheme(dark: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = if (dark) Dark else Light, content = content)
}
