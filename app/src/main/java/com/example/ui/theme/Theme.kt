package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val GarvisCrimsonColorScheme = darkColorScheme(
  primary = GarvisRed,
  onPrimary = GarvisDarkBackground,
  primaryContainer = GarvisRedDark,
  onPrimaryContainer = GarvisRedSoft,

  secondary = GarvisGold,
  onSecondary = GarvisDarkBackground,
  secondaryContainer = GarvisGoldDark,
  onSecondaryContainer = GarvisGold,

  tertiary = GarvisRedBright,
  onTertiary = GarvisDarkBackground,
  tertiaryContainer = GarvisRedDark,
  onTertiaryContainer = GarvisRedSoft,

  background = GarvisDarkBackground,
  onBackground = GarvisTextPrimary,

  surface = GarvisDarkSurface,
  onSurface = GarvisTextPrimary,
  surfaceVariant = GarvisSurfaceVariant,
  onSurfaceVariant = GarvisTextSecondary,

  outline = GarvisSurfaceBorder,
  outlineVariant = GarvisSurfaceBorderBright,
  error = Color(0xFFFF5252),
  onError = GarvisDarkBackground
)

@Composable
fun GarvisTheme(
  content: @Composable () -> Unit
) {
  MaterialTheme(
    colorScheme = GarvisCrimsonColorScheme,
    typography = Typography,
    content = content
  )
}
