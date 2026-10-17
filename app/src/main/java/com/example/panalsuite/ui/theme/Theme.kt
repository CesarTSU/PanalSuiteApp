package com.example.panalsuite.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

private val DarkColorScheme = darkColorScheme(
    primary = Violet300,
    onPrimary = Ink950,
    primaryContainer = Violet700,
    onPrimaryContainer = Violet100,
    secondary = Sky300,
    onSecondary = Ink950,
    secondaryContainer = Ink700,
    onSecondaryContainer = Sky100,
    tertiary = Pink300,
    onTertiary = Ink950,
    tertiaryContainer = Ink700,
    onTertiaryContainer = Pink100,
    background = Ink950,
    onBackground = Gray100,
    surface = Ink900,
    onSurface = Gray100,
    surfaceVariant = Ink800,
    onSurfaceVariant = Gray400,
    surfaceTint = Violet300,
    surfaceContainerLowest = Ink950,
    surfaceContainerLow = Ink850,
    surfaceContainer = Ink800,
    surfaceContainerHigh = Ink700,
    surfaceContainerHighest = Ink600,
    outline = Gray500,
    outlineVariant = Ink600,
    error = Color(0xFFFF8A8F),
)

private val LightColorScheme = lightColorScheme(
    primary = Violet500,
    onPrimary = Color.White,
    primaryContainer = Violet100,
    onPrimaryContainer = Violet700,
    secondary = Blue700,
    onSecondary = Color.White,
    secondaryContainer = Sky100,
    onSecondaryContainer = Blue700,
    tertiary = Pink500,
    onTertiary = Color.White,
    tertiaryContainer = Pink100,
    onTertiaryContainer = Color(0xFF7A1C4B),
    background = Mist200,
    onBackground = Ink900,
    surface = Mist200,
    onSurface = Ink900,
    surfaceVariant = Mist300,
    onSurfaceVariant = Gray500,
    surfaceTint = Violet500,
    surfaceContainerLowest = Mist100,
    surfaceContainerLow = Mist100,
    surfaceContainer = Mist300,
    surfaceContainerHigh = Mist300,
    surfaceContainerHighest = Mist400,
    outline = Gray400,
    outlineVariant = Mist500,
    error = CriticalColor,
)

private val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(14.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(28.dp),
)

@Composable
fun PanalSuiteTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = AppShapes,
        content = content
    )
}

