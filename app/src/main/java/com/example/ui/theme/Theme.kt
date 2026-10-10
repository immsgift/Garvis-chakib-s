package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val GarvisColorScheme = darkColorScheme(
  primary = GarvisCyan,
  onPrimary = GarvisDarkBackground,
  primaryContainer = GarvisCyanDark,
  onPrimaryContainer = GarvisCyanSoft,

  secondary = GarvisBlue,
  onSecondary = GarvisDarkBackground,
  secondaryContainer = GarvisBlueDark,
  onSecondaryContainer = GarvisCyanSoft,

  tertiary = GarvisAmber,
  onTertiary = GarvisDarkBackground,
  tertiaryContainer = Color(0xFF332500),
  onTertiaryContainer = GarvisAmber,

  background = GarvisDarkBackground,
  onBackground = GarvisTextPrimary,

  surface = GarvisDarkSurface,
  onSurface = GarvisTextPrimary,
  surfaceVariant = GarvisSurfaceVariant,
  onSurfaceVariant = GarvisTextSecondary,

  outline = GarvisSurfaceBorder,
  outlineVariant = GarvisSurfaceBorderBright,
  error = GarvisRed,
  onError = GarvisDarkBackground
)

@Composable
fun GarvisTheme(
  content: @Composable () -> Unit
) {
  MaterialTheme(
    colorScheme = GarvisColorScheme,
    typography = Typography,
    content = content
  )
}
