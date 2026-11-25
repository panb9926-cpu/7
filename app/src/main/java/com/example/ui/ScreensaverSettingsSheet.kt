package com.example.ui

import android.content.Intent
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Contrast
import androidx.compose.material.icons.outlined.Fullscreen
import androidx.compose.material.icons.outlined.GridOn
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.RestartAlt
import androidx.compose.material.icons.outlined.Tv
import androidx.compose.material.icons.outlined.Vibration
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.LifetimeStats
import com.example.model.BouncePalette
import com.example.model.ScreensaverConfig
import com.example.ui.theme.CrtOutline
import com.example.ui.theme.CrtSurface
import com.example.ui.theme.CrtSurfaceElevated
import java.util.Locale
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreensaverSettingsSheet(
  config: ScreensaverConfig,
  lifetimeStats: LifetimeStats,
  accentColor: Color,
  onDismiss: () -> Unit,
  onSpeedChange: (Float) -> Unit,
  onLogoScaleChange: (Float) -> Unit,
  onPaletteSelect: (BouncePalette) -> Unit,
  onNeonGlowChange: (Boolean) -> Unit,
  onCrtScanlinesChange: (Boolean) -> Unit,
  onPureOledBlackChange: (Boolean) -> Unit,
  onImmersiveModeChange: (Boolean) -> Unit,
  onKeepScreenOnChange: (Boolean) -> Unit,
  onCornerHapticsChange: (Boolean) -> Unit,
  onResetStats: () -> Unit,
) {
  BackHandler(onBack = onDismiss)
  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
  val context = LocalContext.current

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = sheetState,
    containerColor = CrtSurface,
    contentColor = MaterialTheme.colorScheme.onSurface,
    tonalElevation = 8.dp,
  ) {
    Column(
      modifier =
        Modifier
          .fillMaxWidth()
          .widthIn(max = 560.dp)
          .align(Alignment.CenterHorizontally)
          .verticalScroll(rememberScrollState())
          .navigationBarsPadding()
          .padding(horizontal = 24.dp)
          .padding(bottom = 24.dp),
      verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = stringResource(R.string.settings_title),
            style = MaterialTheme.typography.headlineSmall,
          )
          Text(
            text = stringResource(R.string.settings_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
        IconButton(
          onClick = onDismiss,
          modifier = Modifier.minimumInteractiveComponentSize().testTag("close_settings_button"),
        ) {
          Icon(
            imageVector = Icons.Outlined.Close,
            contentDescription = stringResource(R.string.cd_settings),
          )
        }
      }

      // Speed & Logo Scale Sliders
      Surface(
        shape = RoundedCornerShape(18.dp),
        color = CrtSurfaceElevated,
        border = BorderStroke(1.dp, CrtOutline.copy(alpha = 0.6f)),
      ) {
        Column(
          modifier = Modifier.padding(16.dp),
          verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
          SliderControlRow(
            label = stringResource(R.string.settings_speed_label),
            valueText = String.format(Locale.US, "%.1fx", config.speedMultiplier),
            value = config.speedMultiplier,
            valueRange = 0.4f..3.0f,
            accentColor = accentColor,
            testTag = "speed_slider",
            onValueChange = { raw ->
              val snapped = (raw * 10f).roundToInt() / 10f
              onSpeedChange(snapped)
            },
          )

          HorizontalDivider(color = CrtOutline.copy(alpha = 0.4f))

          SliderControlRow(
            label = stringResource(R.string.settings_size_label),
            valueText = String.format(Locale.US, "%.1fx", config.logoScale),
            value = config.logoScale,
            valueRange = 0.6f..1.6f,
            accentColor = accentColor,
            testTag = "size_slider",
            onValueChange = { raw ->
              val snapped = (raw * 10f).roundToInt() / 10f
              onLogoScaleChange(snapped)
            },
          )
        }
      }

      // Palette Picker
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
          text = stringResource(R.string.settings_palette_label),
          style = MaterialTheme.typography.titleMedium,
        )
        Row(
          modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
          horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
          BouncePalette.entries.forEach { palette ->
            val selected = palette == config.palette
            Surface(
              shape = RoundedCornerShape(14.dp),
              color = if (selected) accentColor.copy(alpha = 0.16f) else CrtSurfaceElevated,
              border =
                BorderStroke(
                  width = if (selected) 1.5.dp else 1.dp,
                  color = if (selected) accentColor else CrtOutline.copy(alpha = 0.6f),
                ),
              modifier =
                Modifier
                  .minimumInteractiveComponentSize()
                  .clip(RoundedCornerShape(14.dp))
                  .clickable { onPaletteSelect(palette) }
                  .testTag("palette_option_${palette.name.lowercase()}"),
            ) {
              Column(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
              ) {
                Text(
                  text = palette.displayName,
                  style = MaterialTheme.typography.labelLarge,
                  color =
                    if (selected) accentColor else MaterialTheme.colorScheme.onSurface,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                  palette.colors.take(5).forEach { namedColor ->
                    Box(
                      modifier =
                        Modifier
                          .size(10.dp)
                          .clip(CircleShape)
                          .background(namedColor.color)
                          .border(0.5.dp, Color.White.copy(alpha = 0.25f), CircleShape),
                    )
                  }
                }
              }
            }
          }
        }
      }

      // Effects & Behavior Toggles
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
          text = stringResource(R.string.settings_effects_label),
          style = MaterialTheme.typography.titleMedium,
        )
        Surface(
          shape = RoundedCornerShape(18.dp),
          color = CrtSurfaceElevated,
          border = BorderStroke(1.dp, CrtOutline.copy(alpha = 0.6f)),
        ) {
          Column(modifier = Modifier.padding(vertical = 4.dp)) {
            ToggleRow(
              icon = Icons.Outlined.AutoAwesome,
              title = stringResource(R.string.toggle_neon_glow),
              subtitle = stringResource(R.string.toggle_neon_glow_desc),
              checked = config.neonGlowEnabled,
              accentColor = accentColor,
              testTag = "toggle_neon_glow",
              onCheckedChange = onNeonGlowChange,
            )
            HorizontalDivider(
              modifier = Modifier.padding(horizontal = 16.dp),
              color = CrtOutline.copy(alpha = 0.35f),
            )
            ToggleRow(
              icon = Icons.Outlined.GridOn,
              title = stringResource(R.string.toggle_crt_scanlines),
              subtitle = stringResource(R.string.toggle_crt_scanlines_desc),
              checked = config.crtScanlinesEnabled,
              accentColor = accentColor,
              testTag = "toggle_crt_scanlines",
              onCheckedChange = onCrtScanlinesChange,
            )
            HorizontalDivider(
              modifier = Modifier.padding(horizontal = 16.dp),
              color = CrtOutline.copy(alpha = 0.35f),
            )
            ToggleRow(
              icon = Icons.Outlined.Contrast,
              title = stringResource(R.string.toggle_oled_black),
              subtitle = stringResource(R.string.toggle_oled_black_desc),
              checked = config.pureOledBlack,
              accentColor = accentColor,
              testTag = "toggle_oled_black",
              onCheckedChange = onPureOledBlackChange,
            )
            HorizontalDivider(
              modifier = Modifier.padding(horizontal = 16.dp),
              color = CrtOutline.copy(alpha = 0.35f),
            )
            ToggleRow(
              icon = Icons.Outlined.Fullscreen,
              title = stringResource(R.string.toggle_immersive),
              subtitle = stringResource(R.string.toggle_immersive_desc),
              checked = config.immersiveMode,
              accentColor = accentColor,
              testTag = "toggle_immersive",
              onCheckedChange = onImmersiveModeChange,
            )
            HorizontalDivider(
              modifier = Modifier.padding(horizontal = 16.dp),
              color = CrtOutline.copy(alpha = 0.35f),
            )
            ToggleRow(
              icon = Icons.Outlined.LightMode,
              title = stringResource(R.string.toggle_keep_screen_on),
              subtitle = stringResource(R.string.toggle_keep_screen_on_desc),
              checked = config.keepScreenOn,
              accentColor = accentColor,
              testTag = "toggle_keep_screen_on",
              onCheckedChange = onKeepScreenOnChange,
            )
            HorizontalDivider(
              modifier = Modifier.padding(horizontal = 16.dp),
              color = CrtOutline.copy(alpha = 0.35f),
            )
            ToggleRow(
              icon = Icons.Outlined.Vibration,
              title = stringResource(R.string.toggle_corner_haptics),
              subtitle = stringResource(R.string.toggle_corner_haptics_desc),
              checked = config.cornerHapticsEnabled,
              accentColor = accentColor,
              testTag = "toggle_corner_haptics",
              onCheckedChange = onCornerHapticsChange,
            )
          }
        }
      }

      // Lifetime Stats + Reset
      Surface(
        shape = RoundedCornerShape(18.dp),
        color = CrtSurfaceElevated,
        border = BorderStroke(1.dp, CrtOutline.copy(alpha = 0.6f)),
      ) {
        Row(
          modifier = Modifier.fillMaxWidth().padding(16.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
              text = stringResource(R.string.stats_lifetime_title),
              style = MaterialTheme.typography.titleMedium,
            )
            Text(
              text =
                "${lifetimeStats.totalBounces} bounces  •  ${lifetimeStats.totalCornerHits} corners",
              style = MaterialTheme.typography.labelMedium,
              color = accentColor,
            )
          }
          TextButton(
            onClick = onResetStats,
            modifier = Modifier.minimumInteractiveComponentSize().testTag("reset_stats_button"),
          ) {
            Icon(
              imageVector = Icons.Outlined.RestartAlt,
              contentDescription = null,
              modifier = Modifier.size(18.dp),
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = stringResource(R.string.stats_reset))
          }
        }
      }

      // Android System Screensaver Shortcut
      OutlinedButton(
        onClick = {
          val intent = Intent(Settings.ACTION_DREAM_SETTINGS)
          val resolved = intent.resolveActivity(context.packageManager) != null
          if (resolved) {
            context.startActivity(intent)
          } else {
            Toast.makeText(
                context,
                context.getString(R.string.system_screensaver_unavailable),
                Toast.LENGTH_SHORT,
              )
              .show()
          }
        },
        modifier =
          Modifier.fillMaxWidth().height(50.dp).testTag("system_screensaver_button"),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, CrtOutline),
      ) {
        Icon(
          imageVector = Icons.Outlined.Tv,
          contentDescription = null,
          tint = accentColor,
          modifier = Modifier.size(20.dp),
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = stringResource(R.string.system_screensaver_button),
          style = MaterialTheme.typography.labelLarge,
          color = MaterialTheme.colorScheme.onSurface,
        )
      }
    }
  }
}

@Composable
private fun SliderControlRow(
  label: String,
  valueText: String,
  value: Float,
  valueRange: ClosedFloatingPointRange<Float>,
  accentColor: Color,
  testTag: String,
  onValueChange: (Float) -> Unit,
) {
  Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Text(text = label, style = MaterialTheme.typography.titleMedium)
      Text(
        text = valueText,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Bold,
        color = accentColor,
      )
    }
    Slider(
      value = value,
      onValueChange = onValueChange,
      valueRange = valueRange,
      modifier = Modifier.fillMaxWidth().testTag(testTag),
      colors =
        SliderDefaults.colors(
          thumbColor = accentColor,
          activeTrackColor = accentColor,
          inactiveTrackColor = CrtOutline,
        ),
    )
  }
}

@Composable
private fun ToggleRow(
  icon: ImageVector,
  title: String,
  subtitle: String,
  checked: Boolean,
  accentColor: Color,
  testTag: String,
  onCheckedChange: (Boolean) -> Unit,
) {
  Row(
    modifier =
      Modifier
        .fillMaxWidth()
        .minimumInteractiveComponentSize()
        .clickable { onCheckedChange(!checked) }
        .padding(horizontal = 16.dp, vertical = 10.dp),
    horizontalArrangement = Arrangement.spacedBy(12.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Icon(
      imageVector = icon,
      contentDescription = null,
      tint = if (checked) accentColor else MaterialTheme.colorScheme.onSurfaceVariant,
      modifier = Modifier.size(22.dp),
    )
    Column(modifier = Modifier.weight(1f)) {
      Text(text = title, style = MaterialTheme.typography.bodyLarge)
      Text(
        text = subtitle,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
    }
    Switch(
      checked = checked,
      onCheckedChange = onCheckedChange,
      modifier = Modifier.testTag(testTag),
      colors =
        SwitchDefaults.colors(
          checkedThumbColor = Color.Black,
          checkedTrackColor = accentColor,
          uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
          uncheckedTrackColor = CrtSurface,
          uncheckedBorderColor = CrtOutline,
        ),
    )
  }
}
