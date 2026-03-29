package com.gymtrack.app.presentation.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Dark Color Scheme
private val DarkColorScheme = darkColorScheme(
    primary = ElectricBlue,
    onPrimary = Color.White,
    primaryContainer = DeepBlue,
    onPrimaryContainer = NeonCyan,
    secondary = ElectricPurple,
    onSecondary = Color.White,
    secondaryContainer = ElectricPurple.copy(alpha = 0.3f),
    onSecondaryContainer = NeonPurple,
    tertiary = EnergeticOrange,
    onTertiary = Color.White,
    tertiaryContainer = EnergeticOrange.copy(alpha = 0.3f),
    onTertiaryContainer = NeonOrange,
    error = ErrorRed,
    onError = Color.White,
    errorContainer = ErrorRed.copy(alpha = 0.3f),
    onErrorContainer = ErrorRed,
    background = DarkBackground,
    onBackground = TextPrimaryDark,
    surface = DarkSurface,
    onSurface = TextPrimaryDark,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = TextSecondaryDark,
    outline = DarkCard,
    outlineVariant = GlassBorder,
    inverseSurface = LightSurface,
    inverseOnSurface = TextPrimaryLight,
    inversePrimary = DeepBlue,
    surfaceTint = ElectricBlue,
    scrim = Color.Black.copy(alpha = 0.5f)
)

// Light Color Scheme  - Enhanced for professional appearance
private val LightColorScheme = lightColorScheme(
    primary = LightPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFB2EBF2),
    onPrimaryContainer = LightPrimaryVariant,
    secondary = LightSecondary,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE1BEE7),
    onSecondaryContainer = Color(0xFF4A148C),
    tertiary = LightAccent,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFFE0B2),
    onTertiaryContainer = Color(0xFFBF360C),
    error = ErrorRed,
    onError = Color.White,
    errorContainer = Color(0xFFFFCDD2),
    onErrorContainer = Color(0xFFB71C1C),
    background = LightBackground,
    onBackground = TextPrimaryLight,
    surface = LightSurface,
    onSurface = TextPrimaryLight,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = TextSecondaryLight,
    outline = Color(0xFFDEE2E6),
    outlineVariant = Color(0xFFE9ECEF),
    inverseSurface = DarkSurface,
    inverseOnSurface = TextPrimaryDark,
    inversePrimary = ElectricBlue,
    surfaceTint = LightPrimary,
    scrim = Color.Black.copy(alpha = 0.5f)
)

// Gradient Brushes
val OnboardingGradient = Brush.horizontalGradient(
    colors = listOf(GradientStart, GradientMid, GradientEnd)
)

val OnboardingGradientVertical = Brush.verticalGradient(
    colors = listOf(GradientStart, GradientMid, GradientEnd)
)

val CardGradient = Brush.verticalGradient(
    colors = listOf(
        DarkSurface.copy(alpha = 0.9f),
        DarkSurface
    )
)

val GlowGradient = Brush.radialGradient(
    colors = listOf(
        ElectricBlue.copy(alpha = 0.4f),
        ElectricBlue.copy(alpha = 0.1f),
        Color.Transparent
    )
)

val PurpleGradient = Brush.horizontalGradient(
    colors = listOf(GradientPurpleStart, GradientPurpleEnd)
)

val NeonGradient = Brush.horizontalGradient(
    colors = listOf(ElectricBlue, NeonCyan, ElectricPurple)
)

// Glass Effect Brush
val GlassBrush = Brush.verticalGradient(
    colors = listOf(
        GlassBackgroundDark.copy(alpha = 0.3f),
        GlassBackgroundDark.copy(alpha = 0.1f)
    )
)

val GlassBorderBrush = Brush.linearGradient(
    colors = listOf(
        GlassBorder.copy(alpha = 0.5f),
        GlassBorder.copy(alpha = 0.2f)
    )
)

@Composable
fun GymTrackTheme(
    darkTheme: Boolean = isSystemInDarkTheme(), // Use system preference by default
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Color.Transparent.toArgb()
            window.navigationBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = false
                isAppearanceLightNavigationBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
