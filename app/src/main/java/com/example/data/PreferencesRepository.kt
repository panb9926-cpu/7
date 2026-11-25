package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.model.BouncePalette
import com.example.model.ScreensaverConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "dvd_bounce_prefs")

data class LifetimeStats(val totalBounces: Int = 0, val totalCornerHits: Int = 0)

class PreferencesRepository(private val context: Context) {

  private object Keys {
    val SPEED = floatPreferencesKey("speed_multiplier")
    val LOGO_SCALE = floatPreferencesKey("logo_scale")
    val PALETTE = stringPreferencesKey("palette")
    val NEON_GLOW = booleanPreferencesKey("neon_glow")
    val CRT_SCANLINES = booleanPreferencesKey("crt_scanlines")
    val PURE_OLED_BLACK = booleanPreferencesKey("pure_oled_black")
    val IMMERSIVE_MODE = booleanPreferencesKey("immersive_mode")
    val KEEP_SCREEN_ON = booleanPreferencesKey("keep_screen_on")
    val CORNER_HAPTICS = booleanPreferencesKey("corner_haptics")
    val TOTAL_BOUNCES = intPreferencesKey("total_bounces")
    val TOTAL_CORNERS = intPreferencesKey("total_corners")
  }

  val configFlow: Flow<ScreensaverConfig> =
    context.dataStore.data.map { prefs ->
      val paletteName = prefs[Keys.PALETTE] ?: BouncePalette.CLASSIC_NEON.name
      val palette =
        BouncePalette.entries.firstOrNull { it.name == paletteName } ?: BouncePalette.CLASSIC_NEON
      ScreensaverConfig(
        speedMultiplier = (prefs[Keys.SPEED] ?: 1.0f).coerceIn(0.4f, 3.0f),
        logoScale = (prefs[Keys.LOGO_SCALE] ?: 1.0f).coerceIn(0.55f, 1.65f),
        palette = palette,
        neonGlowEnabled = prefs[Keys.NEON_GLOW] ?: true,
        crtScanlinesEnabled = prefs[Keys.CRT_SCANLINES] ?: true,
        pureOledBlack = prefs[Keys.PURE_OLED_BLACK] ?: false,
        immersiveMode = prefs[Keys.IMMERSIVE_MODE] ?: true,
        keepScreenOn = prefs[Keys.KEEP_SCREEN_ON] ?: true,
        cornerHapticsEnabled = prefs[Keys.CORNER_HAPTICS] ?: true,
      )
    }

  val lifetimeStatsFlow: Flow<LifetimeStats> =
    context.dataStore.data.map { prefs ->
      LifetimeStats(
        totalBounces = prefs[Keys.TOTAL_BOUNCES] ?: 0,
        totalCornerHits = prefs[Keys.TOTAL_CORNERS] ?: 0,
      )
    }

  suspend fun updateConfig(transform: (ScreensaverConfig) -> ScreensaverConfig) {
    context.dataStore.edit { prefs ->
      val currentPalette =
        BouncePalette.entries.firstOrNull { it.name == prefs[Keys.PALETTE] }
          ?: BouncePalette.CLASSIC_NEON
      val current =
        ScreensaverConfig(
          speedMultiplier = (prefs[Keys.SPEED] ?: 1.0f).coerceIn(0.4f, 3.0f),
          logoScale = (prefs[Keys.LOGO_SCALE] ?: 1.0f).coerceIn(0.55f, 1.65f),
          palette = currentPalette,
          neonGlowEnabled = prefs[Keys.NEON_GLOW] ?: true,
          crtScanlinesEnabled = prefs[Keys.CRT_SCANLINES] ?: true,
          pureOledBlack = prefs[Keys.PURE_OLED_BLACK] ?: false,
          immersiveMode = prefs[Keys.IMMERSIVE_MODE] ?: true,
          keepScreenOn = prefs[Keys.KEEP_SCREEN_ON] ?: true,
          cornerHapticsEnabled = prefs[Keys.CORNER_HAPTICS] ?: true,
        )
      val updated = transform(current)
      prefs[Keys.SPEED] = updated.speedMultiplier
      prefs[Keys.LOGO_SCALE] = updated.logoScale
      prefs[Keys.PALETTE] = updated.palette.name
      prefs[Keys.NEON_GLOW] = updated.neonGlowEnabled
      prefs[Keys.CRT_SCANLINES] = updated.crtScanlinesEnabled
      prefs[Keys.PURE_OLED_BLACK] = updated.pureOledBlack
      prefs[Keys.IMMERSIVE_MODE] = updated.immersiveMode
      prefs[Keys.KEEP_SCREEN_ON] = updated.keepScreenOn
      prefs[Keys.CORNER_HAPTICS] = updated.cornerHapticsEnabled
    }
  }

  suspend fun incrementStats(bounces: Int, corners: Int) {
    if (bounces <= 0 && corners <= 0) return
    context.dataStore.edit { prefs ->
      prefs[Keys.TOTAL_BOUNCES] = (prefs[Keys.TOTAL_BOUNCES] ?: 0) + bounces
      prefs[Keys.TOTAL_CORNERS] = (prefs[Keys.TOTAL_CORNERS] ?: 0) + corners
    }
  }

  suspend fun resetLifetimeStats() {
    context.dataStore.edit { prefs ->
      prefs[Keys.TOTAL_BOUNCES] = 0
      prefs[Keys.TOTAL_CORNERS] = 0
    }
  }
}
