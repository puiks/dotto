package com.dotto.app.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
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

private val DarkColorScheme = darkColorScheme(
    primary = DottoDarkPrimary,
    onPrimary = DottoDarkOnPrimary,
    background = DottoDarkBackground,
    surface = DottoDarkSurface,
    onSurface = DottoDarkOnSurface,
    onSurfaceVariant = DottoDarkOnSurfaceVariant
)

@Composable
fun DottoTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = DottoTypography,
        content = content
    )
}
