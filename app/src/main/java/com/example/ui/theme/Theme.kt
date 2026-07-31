package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
  darkColorScheme(
    primary = AccentBlue,
    background = BackgroundDark,
    surface = SurfaceDark,
    surfaceVariant = SurfaceVariantDark,
    onPrimary = PrimaryTextDark,
    onBackground = PrimaryTextDark,
    onSurface = PrimaryTextDark,
    onSurfaceVariant = SecondaryTextDark
  )

private val LightColorScheme =
  lightColorScheme(
    primary = AccentBlue,
    background = BackgroundLight,
    surface = SurfaceLight,
    surfaceVariant = SurfaceVariantLight,
    onPrimary = PrimaryTextLight,
    onBackground = PrimaryTextLight,
    onSurface = PrimaryTextLight,
    onSurfaceVariant = SecondaryTextLight
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true, // Dark mode first
  dynamicColor: Boolean = false, // Disable dynamic to keep brand colors
  content: @Composable () -> Unit,
) {
  val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
