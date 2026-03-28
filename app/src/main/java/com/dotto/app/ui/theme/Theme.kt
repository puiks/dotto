package com.dotto.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = DottoPrimary,
    onPrimary = DottoOnPrimary,
    background = DottoBackground,
    surface = DottoSurface,
    onSurface = DottoOnSurface,
    onSurfaceVariant = DottoOnSurfaceVariant
)

@Composable
fun DottoTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = DottoTypography,
        content = content
    )
}
