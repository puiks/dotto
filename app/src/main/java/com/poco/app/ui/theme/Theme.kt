package com.poco.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = PocoPrimary,
    onPrimary = PocoOnPrimary,
    background = PocoBackground,
    surface = PocoSurface,
    onSurface = PocoOnSurface,
    onSurfaceVariant = PocoOnSurfaceVariant
)

@Composable
fun PocoTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = PocoTypography,
        content = content
    )
}
