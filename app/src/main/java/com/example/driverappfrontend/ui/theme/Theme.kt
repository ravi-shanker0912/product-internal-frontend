package com.example.driverappfrontend.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = BlueLight,
    secondary = TealLight,
    tertiary = AmberLight
)

private val LightColorScheme = lightColorScheme(
    primary = BlueDark,
    secondary = TealDark,
    tertiary = AmberDark
)

data class ExtendedColors(
    val success: Color,
    val warning: Color,
    val info: Color
)

private val DarkExtendedColors = ExtendedColors(
    success = SuccessLight,
    warning = WarningLightTone,
    info = InfoLightTone
)

private val LightExtendedColors = ExtendedColors(
    success = SuccessDark,
    warning = WarningDarkTone,
    info = InfoDarkTone
)

private val LocalExtendedColors = staticCompositionLocalOf { LightExtendedColors }

object AppTheme {
    val extendedColors: ExtendedColors
        @Composable get() = LocalExtendedColors.current
}

@Composable
fun DriverAppFrontendTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // App's own brand colors, not the device wallpaper's Material You palette.
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
    val extendedColors = if (darkTheme) DarkExtendedColors else LightExtendedColors

    CompositionLocalProvider(LocalExtendedColors provides extendedColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
