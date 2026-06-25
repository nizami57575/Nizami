package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val GlassColorScheme = darkColorScheme(
  primary = GlassPrimary,
  secondary = GlassSecondary,
  tertiary = GlassTertiary,
  background = GlassBackground,
  surface = GlassSurface,
  onBackground = GlassOnBackground,
  onSurface = GlassOnSurface,
  surfaceVariant = GlassSurfaceVariant,
  onSurfaceVariant = GlassOnSurfaceVariant,
  primaryContainer = GlassPrimaryContainer,
  onPrimaryContainer = GlassOnPrimaryContainer,
  error = GlassError
)

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true,
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  MaterialTheme(colorScheme = GlassColorScheme, typography = Typography, content = content)
}
