package com.github.junhee8649.cleancalendar.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = TdsLightPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD6E4FF),
    onPrimaryContainer = TdsLightOnBackground,
    secondary = TdsSaturday,
    onSecondary = Color.White,
    tertiary = TdsGreen,
    onTertiary = Color.White,
    background = TdsLightBackground,
    onBackground = TdsLightOnBackground,
    surface = TdsLightSurface,
    onSurface = TdsLightOnBackground,
    surfaceVariant = TdsLightSurfaceVariant,
    onSurfaceVariant = TdsLightOnSurfaceVariant,
    outlineVariant = TdsLightOutlineVariant,
    error = TdsRed,
    onError = Color.White,
)

private val DarkColorScheme = darkColorScheme(
    primary = TdsBlue,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF003399),
    onPrimaryContainer = TdsDarkOnBackground,
    secondary = TdsSaturday,
    onSecondary = Color.White,
    tertiary = TdsGreen,
    onTertiary = Color.White,
    background = TdsDarkBackground,
    onBackground = TdsDarkOnBackground,
    surface = TdsDarkSurface,
    onSurface = TdsDarkOnBackground,
    surfaceVariant = TdsDarkSurfaceVariant,
    onSurfaceVariant = TdsLightOnSurfaceVariant,
    outlineVariant = TdsDarkOutlineVariant,
    error = TdsRed,
    onError = Color.White,
)

@Composable
fun CleanCalendarTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
