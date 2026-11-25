package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.LifetimeStats
import com.example.data.PreferencesRepository
import com.example.model.BouncePalette
import com.example.model.ScreensaverConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ScreensaverUiState(
  val isPaused: Boolean = false,
  val isHudVisible: Boolean = true,
  val isSettingsOpen: Boolean = false,
  val sessionBounces: Int = 0,
  val sessionCorners: Int = 0,
  val currentColorIndex: Int = 0,
  val nudgeSignal: Int = 0,
  val cornerBannerVisible: Boolean = false,
)

class ScreensaverViewModel(application: Application) : AndroidViewModel(application) {
  private val repository = PreferencesRepository(application.applicationContext)

  val config: StateFlow<ScreensaverConfig> =
    repository.configFlow.stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5_000),
      initialValue = ScreensaverConfig(),
    )

  val lifetimeStats: StateFlow<LifetimeStats> =
    repository.lifetimeStatsFlow.stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5_000),
      initialValue = LifetimeStats(),
    )

  private val _uiState = MutableStateFlow(ScreensaverUiState())
  val uiState: StateFlow<ScreensaverUiState> = _uiState.asStateFlow()

  private var pendingBounces = 0
  private var pendingCorners = 0

  fun toggleHud() {
    _uiState.update { state -> state.copy(isHudVisible = !state.isHudVisible) }
  }

  fun hideHud() {
    _uiState.update { state ->
      if (!state.isSettingsOpen) state.copy(isHudVisible = false) else state
    }
  }

  fun togglePause() {
    _uiState.update { it.copy(isPaused = !it.isPaused) }
  }

  fun openSettings() {
    _uiState.update { it.copy(isSettingsOpen = true, isHudVisible = true) }
  }

  fun closeSettings() {
    _uiState.update { it.copy(isSettingsOpen = false) }
  }

  fun triggerNudge() {
    _uiState.update { it.copy(nudgeSignal = it.nudgeSignal + 1, isPaused = false) }
  }

  fun cyclePalette() {
    viewModelScope.launch {
      repository.updateConfig { current -> current.copy(palette = current.palette.next()) }
    }
  }

  fun selectPalette(palette: BouncePalette) {
    viewModelScope.launch {
      repository.updateConfig { current -> current.copy(palette = palette) }
    }
  }

  fun setSpeedMultiplier(speed: Float) {
    viewModelScope.launch {
      repository.updateConfig { current -> current.copy(speedMultiplier = speed.coerceIn(0.4f, 3.0f)) }
    }
  }

  fun setLogoScale(scale: Float) {
    viewModelScope.launch {
      repository.updateConfig { current -> current.copy(logoScale = scale.coerceIn(0.55f, 1.65f)) }
    }
  }

  fun setNeonGlow(enabled: Boolean) {
    viewModelScope.launch {
      repository.updateConfig { it.copy(neonGlowEnabled = enabled) }
    }
  }

  fun setCrtScanlines(enabled: Boolean) {
    viewModelScope.launch {
      repository.updateConfig { it.copy(crtScanlinesEnabled = enabled) }
    }
  }

  fun setPureOledBlack(enabled: Boolean) {
    viewModelScope.launch {
      repository.updateConfig { it.copy(pureOledBlack = enabled) }
    }
  }

  fun setImmersiveMode(enabled: Boolean) {
    viewModelScope.launch {
      repository.updateConfig { it.copy(immersiveMode = enabled) }
    }
  }

  fun setKeepScreenOn(enabled: Boolean) {
    viewModelScope.launch {
      repository.updateConfig { it.copy(keepScreenOn = enabled) }
    }
  }

  fun setCornerHaptics(enabled: Boolean) {
    viewModelScope.launch {
      repository.updateConfig { it.copy(cornerHapticsEnabled = enabled) }
    }
  }

  fun onCollision(newColorIndex: Int, isCorner: Boolean) {
    _uiState.update { state ->
      state.copy(
        sessionBounces = state.sessionBounces + 1,
        sessionCorners = if (isCorner) state.sessionCorners + 1 else state.sessionCorners,
        currentColorIndex = newColorIndex,
        cornerBannerVisible = isCorner || state.cornerBannerVisible,
      )
    }

    pendingBounces += 1
    if (isCorner) pendingCorners += 1

    if (isCorner || pendingBounces >= 5) {
      flushPendingStats()
    }
  }

  fun dismissCornerBanner() {
    _uiState.update { it.copy(cornerBannerVisible = false) }
  }

  fun resetStats() {
    pendingBounces = 0
    pendingCorners = 0
    _uiState.update { it.copy(sessionBounces = 0, sessionCorners = 0) }
    viewModelScope.launch { repository.resetLifetimeStats() }
  }

  fun flushPendingStats() {
    val bouncesToSave = pendingBounces
    val cornersToSave = pendingCorners
    if (bouncesToSave <= 0 && cornersToSave <= 0) return
    pendingBounces = 0
    pendingCorners = 0
    viewModelScope.launch { repository.incrementStats(bouncesToSave, cornersToSave) }
  }

  override fun onCleared() {
    super.onCleared()
    flushPendingStats()
  }
}
