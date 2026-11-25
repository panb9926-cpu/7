package com.example.ui.theme

import android.os.Build
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DvdDarkColorScheme =
  darkColorScheme(
    primary = ElectricCyan,
    onPrimary = ObsidianVoid,
    primaryContainer = CrtSurfaceElevated,
    onPrimaryContainer = TextPrimary,
    secondary = HotMagenta,
    onSecondary = ObsidianVoid,
    secondaryContainer = CrtSurfaceElevated,
    onSecondaryContainer = TextPrimary,
    tertiary = AcidLime,
    onTertiary = ObsidianVoid,
    background = ObsidianVoid,
    onBackground = TextPrimary,
    surface = CrtSurface,
    onSurface = TextPrimary,
    surfaceVariant = CrtSurfaceElevated,
    onSurfaceVariant = TextSecondary,
    outline = CrtOutline,
  )

@Composable
fun DvdBounceTheme(
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme =
    if (dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
      dynamicDarkColorScheme(LocalContext.current)
    } else {
      DvdDarkColorScheme
    }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
