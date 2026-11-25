package com.example

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.DvdScreensaverScreen
import com.example.ui.ScreensaverViewModel
import com.example.ui.theme.DvdBounceTheme

class MainActivity : ComponentActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge(
      statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
      navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
    )
    WindowCompat.setDecorFitsSystemWindows(window, false)

    setContent {
      val screensaverViewModel: ScreensaverViewModel = viewModel()
      val config by screensaverViewModel.config.collectAsStateWithLifecycle()
      val uiState by screensaverViewModel.uiState.collectAsStateWithLifecycle()
      val lifetimeStats by screensaverViewModel.lifetimeStats.collectAsStateWithLifecycle()

      LaunchedEffect(config.keepScreenOn) {
        if (config.keepScreenOn) {
          window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
          window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
      }

      LaunchedEffect(config.immersiveMode, uiState.isHudVisible, uiState.isSettingsOpen) {
        val controller = WindowInsetsControllerCompat(window, window.decorView)
        controller.systemBarsBehavior =
          WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        val hideBars = config.immersiveMode && !uiState.isHudVisible && !uiState.isSettingsOpen
        if (hideBars) {
          controller.hide(WindowInsetsCompat.Type.systemBars())
        } else {
          controller.show(WindowInsetsCompat.Type.systemBars())
        }
      }

      DvdBounceTheme {
        DvdScreensaverScreen(
          config = config,
          uiState = uiState,
          lifetimeStats = lifetimeStats,
          viewModel = screensaverViewModel,
        )
      }
    }
  }
}
