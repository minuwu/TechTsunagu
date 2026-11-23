package com.techtsunagu.wakatta.core.design.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val LightColorScheme = lightColorScheme(
    primary = NagasakiBlue,
    onPrimary = Color.White,
    primaryContainer = NagasakiBlueContainer,
    onPrimaryContainer = OnNagasakiBlueContainer,
    secondary = Teal,
    onSecondary = Color.White,
    secondaryContainer = Surface2Light,
    onSecondaryContainer = Navy,
    tertiary = Navy,
    onTertiary = Color.White,
    tertiaryContainer = Surface1Light,
    onTertiaryContainer = Navy,
    error = ErrorRed,
    onError = Color.White,
    errorContainer = ErrorContainerRed,
    onErrorContainer = Color(0xFF680003),
    background = SurfaceLight,
    onBackground = Navy,
    surface = SurfaceLight,
    onSurface = Navy,
    surfaceVariant = Surface1Light,
    onSurfaceVariant = Color(0xFF4A5A78),
    surfaceContainer = CardBgLight,
    surfaceContainerHigh = Surface2Light,
    surfaceContainerHighest = Surface3Light,
    outline = OutlineMuted,
    outlineVariant = OutlineBorder,
)

val DarkColorScheme = darkColorScheme(
    primary = DarkPrimary,
    onPrimary = OnNagasakiBlueContainer,
    primaryContainer = DarkPrimaryContainer,
    onPrimaryContainer = DarkOnPrimaryContainer,
    secondary = Color(0xFF5AC2C7),
    onSecondary = Color(0xFF003739),
    secondaryContainer = Color(0xFF1B4E51),
    onSecondaryContainer = Color(0xFFBBEBED),
    tertiary = DarkOnPrimaryContainer,
    onTertiary = Navy,
    tertiaryContainer = DarkOutlineBorder,
    onTertiaryContainer = DarkOnPrimaryContainer,
    error = DarkError,
    onError = Color(0xFF601410),
    errorContainer = DarkErrorContainer,
    onErrorContainer = Color(0xFFF9DEDC),
    background = DarkBg,
    onBackground = SurfaceLight,
    surface = DarkSurface,
    onSurface = SurfaceLight,
    surfaceVariant = DarkSurface1,
    onSurfaceVariant = Color(0xFFA5B7D1),
    surfaceContainer = DarkSurface1,
    surfaceContainerHigh = DarkSurface2,
    surfaceContainerHighest = DarkSurface3,
    outline = DarkOutline,
    outlineVariant = DarkOutlineBorder,
)

@Composable
fun WakattaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = WakattaTypography,
        shapes = WakattaShapes,
        content = content,
    )
}
