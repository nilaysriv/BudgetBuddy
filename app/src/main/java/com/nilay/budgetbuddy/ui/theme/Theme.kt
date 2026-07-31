package com.nilay.budgetbuddy.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
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

enum class AppColorScheme { DYNAMIC, BLUE, GREEN, PURPLE, ORANGE, RED }

private fun staticLightScheme(primary: Color, onPrimary: Color, primaryContainer: Color, onPrimaryContainer: Color): ColorScheme = lightColorScheme(
    primary = primary,
    onPrimary = onPrimary,
    primaryContainer = primaryContainer,
    onPrimaryContainer = onPrimaryContainer,
    secondary = OneUiSlate,
    tertiary = OneUiGreen,
    error = OneUiRed,
    background = OneUiBackgroundLight,
    onBackground = Color(0xFF1A1C1E),
    surface = OneUiSurfaceLight,
    onSurface = Color(0xFF1A1C1E),
    surfaceVariant = OneUiSurfaceVariantLight,
    onSurfaceVariant = Color(0xFF43474E),
    outline = OneUiOutlineLight
)

private fun staticDarkScheme(primary: Color, onPrimary: Color, primaryContainer: Color, onPrimaryContainer: Color): ColorScheme = darkColorScheme(
    primary = primary,
    onPrimary = onPrimary,
    primaryContainer = primaryContainer,
    onPrimaryContainer = onPrimaryContainer,
    secondary = OneUiSlateLight,
    tertiary = OneUiGreenLight,
    error = OneUiRedLight,
    background = OneUiBackgroundDark,
    onBackground = Color(0xFFE3E2E6),
    surface = OneUiSurfaceDark,
    onSurface = Color(0xFFE3E2E6),
    surfaceVariant = OneUiSurfaceVariantDark,
    onSurfaceVariant = Color(0xFFC4C6D0),
    outline = OneUiOutlineDark
)

private fun lightSchemeFor(scheme: AppColorScheme): ColorScheme = when (scheme) {
    AppColorScheme.GREEN -> staticLightScheme(GreenAccent, Color.White, GreenContainerLight, OnGreenContainerLight)
    AppColorScheme.PURPLE -> staticLightScheme(PurpleAccent, Color.White, PurpleContainerLight, OnPurpleContainerLight)
    AppColorScheme.ORANGE -> staticLightScheme(OrangeAccent, Color.White, OrangeContainerLight, OnOrangeContainerLight)
    AppColorScheme.RED -> staticLightScheme(RedAccent, Color.White, RedContainerLight, OnRedContainerLight)
    AppColorScheme.BLUE, AppColorScheme.DYNAMIC -> staticLightScheme(OneUiBlue, Color.White, OneUiBlueContainerLight, OneUiOnBlueContainerLight)
}

private fun darkSchemeFor(scheme: AppColorScheme): ColorScheme = when (scheme) {
    AppColorScheme.GREEN -> staticDarkScheme(GreenAccentLight, Color(0xFF00391F), GreenContainerDark, OnGreenContainerDark)
    AppColorScheme.PURPLE -> staticDarkScheme(PurpleAccentLight, Color(0xFF3A0086), PurpleContainerDark, OnPurpleContainerDark)
    AppColorScheme.ORANGE -> staticDarkScheme(OrangeAccentLight, Color(0xFF562000), OrangeContainerDark, OnOrangeContainerDark)
    AppColorScheme.RED -> staticDarkScheme(RedAccentLight, Color(0xFF690003), RedContainerDark, OnRedContainerDark)
    AppColorScheme.BLUE, AppColorScheme.DYNAMIC -> staticDarkScheme(OneUiBlueLight, Color(0xFF00306B), OneUiBlueContainerDark, OneUiOnBlueContainerDark)
}

// One UI leans on noticeably larger, softer corner radii than stock Material.
val OneUiShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp)
)

// Fully-rounded shape for pills: buttons, switches, chips, text field capsules.
val PillShape = RoundedCornerShape(50)

@Composable
fun BudgetBuddyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+, and only applies when colorScheme is DYNAMIC
    dynamicColor: Boolean = true,
    colorScheme: AppColorScheme = AppColorScheme.DYNAMIC,
    content: @Composable () -> Unit
) {
    val scheme = when {
        colorScheme == AppColorScheme.DYNAMIC && dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> darkSchemeFor(colorScheme)
        else -> lightSchemeFor(colorScheme)
    }

    MaterialTheme(
        colorScheme = scheme,
        typography = Typography,
        shapes = OneUiShapes,
        content = content
    )
}
