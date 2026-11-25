package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Shuffle
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.R
import com.example.data.LifetimeStats
import com.example.model.ImpactRipple
import com.example.model.ScreensaverConfig
import com.example.physics.BounceBody
import com.example.physics.DvdPhysicsEngine
import com.example.ui.DvdLogoRenderer.drawDvdLogo
import com.example.ui.theme.CrtMidnight
import com.example.ui.theme.CrtOutline
import com.example.ui.theme.CrtSurface
import com.example.ui.theme.ObsidianVoid
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random
import kotlinx.coroutines.delay

@Composable
fun DvdScreensaverScreen(
  config: ScreensaverConfig,
  uiState: ScreensaverUiState,
  lifetimeStats: LifetimeStats,
  viewModel: ScreensaverViewModel,
) {
  val density = LocalDensity.current
  val haptics = LocalHapticFeedback.current
  val lifecycleOwner = LocalLifecycleOwner.current

  DisposableEffect(lifecycleOwner) {
    val observer = LifecycleEventObserver { _, event ->
      if (event == Lifecycle.Event.ON_PAUSE || event == Lifecycle.Event.ON_STOP) {
        viewModel.flushPendingStats()
      }
    }
    lifecycleOwner.lifecycle.addObserver(observer)
    onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
  }

  // Auto-hide HUD after 4.5s of inactivity when settings are closed
  var interactionNonce by remember { mutableLongStateOf(0L) }
  LaunchedEffect(uiState.isHudVisible, uiState.isSettingsOpen, interactionNonce) {
    if (uiState.isHudVisible && !uiState.isSettingsOpen) {
      delay(4_500L)
      viewModel.hideHud()
    }
  }

  // Auto-dismiss corner celebration banner
  LaunchedEffect(uiState.cornerBannerVisible) {
    if (uiState.cornerBannerVisible) {
      delay(1_800L)
      viewModel.dismissCornerBanner()
    }
  }

  val paletteColors = config.palette.colors
  val activeNamedColor = paletteColors[uiState.currentColorIndex % paletteColors.size]
  val activeColor = activeNamedColor.color

  BoxWithConstraints(
    modifier =
      Modifier
        .fillMaxSize()
        .background(if (config.pureOledBlack) ObsidianVoid else CrtMidnight)
        .clickable(
          interactionSource = remember { MutableInteractionSource() },
          indication = null,
        ) {
          interactionNonce += 1L
          viewModel.toggleHud()
        }
        .semantics { contentDescription = "Bouncing DVD logo screensaver canvas" }
        .testTag("screensaver_canvas"),
  ) {
    val boundsWidthPx = with(density) { maxWidth.toPx() }
    val boundsHeightPx = with(density) { maxHeight.toPx() }

    val baseLogoWidthPx =
      with(density) {
        val minDimPx = minOf(boundsWidthPx, boundsHeightPx)
        (minDimPx * 0.28f).coerceIn(112.dp.toPx(), 210.dp.toPx())
      }
    val logoWidthPx = baseLogoWidthPx * config.logoScale
    val logoHeightPx = logoWidthPx / DvdLogoRenderer.ASPECT_RATIO

    val cachedLogoPath =
      remember(logoWidthPx, logoHeightPx) {
        DvdLogoRenderer.createScaledComposePath(logoWidthPx, logoHeightPx)
      }

    val baseSpeedPx = with(density) { 170.dp.toPx() }
    val cornerTolerancePx = with(density) { 7.dp.toPx() }

    var body by remember {
      mutableStateOf(
        BounceBody(
          x = (boundsWidthPx - logoWidthPx).coerceAtLeast(0f) * 0.22f,
          y = (boundsHeightPx - logoHeightPx).coerceAtLeast(0f) * 0.32f,
          vx = baseSpeedPx,
          vy = baseSpeedPx * 0.87f,
          colorIndex = 0,
        ),
      )
    }

    val ripples = remember { mutableStateListOf<ImpactRipple>() }
    var nextRippleId by remember { mutableLongStateOf(1L) }

    // Keep logo clamped safely if window size or logo scale changes
    LaunchedEffect(boundsWidthPx, boundsHeightPx, logoWidthPx, logoHeightPx) {
      body =
        DvdPhysicsEngine.clampIntoBounds(
          body = body,
          boundsWidth = boundsWidthPx,
          boundsHeight = boundsHeightPx,
          logoWidth = logoWidthPx,
          logoHeight = logoHeightPx,
        )
    }

    // Respond to "Nudge / Randomize Angle" button
    LaunchedEffect(uiState.nudgeSignal) {
      if (uiState.nudgeSignal > 0) {
        val angleDeg = listOf(34f, 41f, 49f, 56f, 128f, 139f, 218f, 229f, 312f, 324f).random()
        val angleRad = Math.toRadians(angleDeg.toDouble())
        val speedMagnitude = baseSpeedPx * 1.32f
        body =
          body.copy(
            vx = (cos(angleRad) * speedMagnitude).toFloat(),
            vy = (sin(angleRad) * speedMagnitude).toFloat(),
          )
      }
    }

    // 60/120fps physics & ripple loop
    LaunchedEffect(
      uiState.isPaused,
      config.speedMultiplier,
      config.palette,
      boundsWidthPx,
      boundsHeightPx,
      logoWidthPx,
      logoHeightPx,
    ) {
      var lastFrameNanos = 0L
      while (true) {
        withFrameNanos { frameNanos ->
          if (lastFrameNanos == 0L) {
            lastFrameNanos = frameNanos
            return@withFrameNanos
          }
          val dt = ((frameNanos - lastFrameNanos) / 1_000_000_000f).coerceIn(0f, 0.05f)
          lastFrameNanos = frameNanos

          if (!uiState.isPaused) {
            val step =
              DvdPhysicsEngine.step(
                body = body,
                deltaSeconds = dt,
                speedMultiplier = config.speedMultiplier,
                boundsWidth = boundsWidthPx,
                boundsHeight = boundsHeightPx,
                logoWidth = logoWidthPx,
                logoHeight = logoHeightPx,
                paletteSize = paletteColors.size,
                cornerTolerancePx = cornerTolerancePx,
                random = Random,
              )
            body = step.body

            if (step.hitAnyWall) {
              val hitColor = paletteColors[step.body.colorIndex % paletteColors.size].color
              viewModel.onCollision(step.body.colorIndex, step.hitCorner)
              ripples.add(
                ImpactRipple(
                  id = nextRippleId++,
                  x = step.impactX,
                  y = step.impactY,
                  color = hitColor,
                  isCorner = step.hitCorner,
                  progress = 0f,
                ),
              )
              if (config.cornerHapticsEnabled) {
                haptics.performHapticFeedback(
                  if (step.hitCorner) HapticFeedbackType.LongPress
                  else HapticFeedbackType.TextHandleMove,
                )
              }
            }
          }

          if (ripples.isNotEmpty()) {
            val iter = ripples.listIterator()
            while (iter.hasNext()) {
              val ripple = iter.next()
              val speed = if (ripple.isCorner) 1.4f else 2.6f
              val nextProgress = ripple.progress + dt * speed
              if (nextProgress >= 1f) {
                iter.remove()
              } else {
                iter.set(ripple.copy(progress = nextProgress))
              }
            }
          }
        }
      }
    }

    val logoColor = paletteColors[body.colorIndex % paletteColors.size].color

    // Main Canvas: Background Vignette, Scanlines, Ripples, and DVD Logo
    Canvas(modifier = Modifier.fillMaxSize()) {
      if (!config.pureOledBlack) {
        drawRect(
          brush =
            Brush.radialGradient(
              colors = listOf(Color(0xFF0E1322), CrtMidnight, ObsidianVoid),
              center = Offset(size.width * 0.5f, size.height * 0.5f),
              radius = maxOf(size.width, size.height) * 0.78f,
            ),
        )
      }

      // Impact ripples on wall/corner collision
      ripples.forEach { ripple ->
        val maxRadius = if (ripple.isCorner) 160.dp.toPx() else 52.dp.toPx()
        val radius = maxRadius * ripple.progress
        val alpha = (1f - ripple.progress) * (if (ripple.isCorner) 0.7f else 0.35f)
        drawCircle(
          color = ripple.color.copy(alpha = alpha),
          radius = radius,
          center = Offset(ripple.x, ripple.y),
          style = Stroke(width = if (ripple.isCorner) 3.dp.toPx() else 1.5.dp.toPx()),
        )
      }

      drawDvdLogo(
        x = body.x,
        y = body.y,
        logoWidth = logoWidthPx,
        logoHeight = logoHeightPx,
        cachedPath = cachedLogoPath,
        color = logoColor,
        neonGlowEnabled = config.neonGlowEnabled,
      )

      if (config.crtScanlinesEnabled) {
        val spacing = 4.dp.toPx()
        var yLine = 0f
        val lineColor = Color.Black.copy(alpha = 0.20f)
        while (yLine < size.height) {
          drawLine(
            color = lineColor,
            start = Offset(0f, yLine),
            end = Offset(size.width, yLine),
            strokeWidth = 1.dp.toPx(),
          )
          yLine += spacing
        }
      }
    }

    // Corner Hit Celebration Banner
    AnimatedVisibility(
      visible = uiState.cornerBannerVisible,
      enter =
        fadeIn() +
          scaleIn(
            initialScale = 0.75f,
            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
          ),
      exit = fadeOut() + scaleOut(targetScale = 0.9f),
      modifier = Modifier.align(Alignment.Center),
    ) {
      Surface(
        shape = RoundedCornerShape(999.dp),
        color = CrtSurface.copy(alpha = 0.88f),
        border = BorderStroke(1.5.dp, activeColor),
        shadowElevation = 12.dp,
      ) {
        Text(
          text = stringResource(R.string.corner_hit_banner),
          style = MaterialTheme.typography.headlineSmall,
          color = activeColor,
          modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
        )
      }
    }

    // Top Floating Telemetry HUD
    AnimatedVisibility(
      visible = uiState.isHudVisible,
      enter = fadeIn() + slideInVertically { -it / 2 },
      exit = fadeOut() + slideOutVertically { -it / 2 },
      modifier = Modifier.align(Alignment.TopCenter).statusBarsPadding().padding(top = 12.dp),
    ) {
      Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(horizontal = 16.dp).widthIn(max = 560.dp),
      ) {
        Surface(
          shape = RoundedCornerShape(20.dp),
          color = CrtSurface.copy(alpha = 0.82f),
          border = BorderStroke(1.dp, CrtOutline.copy(alpha = 0.8f)),
          tonalElevation = 6.dp,
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(18.dp),
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
              Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(activeColor))
              Text(
                text = activeNamedColor.name,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface,
              )
            }

            HudMetricPill(
              label = stringResource(R.string.hud_bounces_label),
              value = uiState.sessionBounces.toString(),
              accentColor = activeColor,
              testTag = "bounce_counter",
            )

            HudMetricPill(
              label = stringResource(R.string.hud_corners_label),
              value = uiState.sessionCorners.toString(),
              accentColor = activeColor,
              testTag = "corner_counter",
            )
          }
        }

        Text(
          text = stringResource(R.string.hud_tap_hint),
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f),
        )
      }
    }

    // Bottom Floating Control Dock
    AnimatedVisibility(
      visible = uiState.isHudVisible,
      enter = fadeIn() + slideInVertically { it / 2 },
      exit = fadeOut() + slideOutVertically { it / 2 },
      modifier =
        Modifier.align(Alignment.BottomCenter).navigationBarsPadding().padding(bottom = 16.dp),
    ) {
      Surface(
        shape = RoundedCornerShape(28.dp),
        color = CrtSurface.copy(alpha = 0.88f),
        border = BorderStroke(1.dp, CrtOutline.copy(alpha = 0.85f)),
        tonalElevation = 8.dp,
        modifier = Modifier.padding(horizontal = 16.dp).widthIn(max = 560.dp),
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
          horizontalArrangement = Arrangement.spacedBy(6.dp),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          IconButton(
            onClick = {
              interactionNonce += 1L
              viewModel.togglePause()
            },
            modifier =
              Modifier.minimumInteractiveComponentSize().testTag("play_pause_button"),
          ) {
            Icon(
              imageVector = if (uiState.isPaused) Icons.Filled.PlayArrow else Icons.Filled.Pause,
              contentDescription =
                stringResource(if (uiState.isPaused) R.string.cd_play else R.string.cd_pause),
              tint = activeColor,
            )
          }

          IconButton(
            onClick = {
              interactionNonce += 1L
              viewModel.triggerNudge()
            },
            modifier = Modifier.minimumInteractiveComponentSize().testTag("nudge_button"),
          ) {
            Icon(
              imageVector = Icons.Outlined.Shuffle,
              contentDescription = stringResource(R.string.cd_nudge),
              tint = MaterialTheme.colorScheme.onSurface,
            )
          }

          Surface(
            shape = RoundedCornerShape(18.dp),
            color = activeColor.copy(alpha = 0.14f),
            border = BorderStroke(1.dp, activeColor.copy(alpha = 0.45f)),
            modifier =
              Modifier
                .minimumInteractiveComponentSize()
                .clip(RoundedCornerShape(18.dp))
                .clickable {
                  interactionNonce += 1L
                  viewModel.cyclePalette()
                }
                .testTag("palette_button")
                .semantics { contentDescription = "Cycle color palette" },
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
              Icon(
                imageVector = Icons.Outlined.Palette,
                contentDescription = null,
                tint = activeColor,
                modifier = Modifier.size(18.dp),
              )
              Text(
                text = config.palette.displayName,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface,
              )
            }
          }

          Spacer(modifier = Modifier.width(2.dp))

          IconButton(
            onClick = {
              interactionNonce += 1L
              viewModel.openSettings()
            },
            modifier = Modifier.minimumInteractiveComponentSize().testTag("settings_button"),
          ) {
            Icon(
              imageVector = Icons.Outlined.Tune,
              contentDescription = stringResource(R.string.cd_settings),
              tint = MaterialTheme.colorScheme.onSurface,
            )
          }
        }
      }
    }

    if (uiState.isSettingsOpen) {
      ScreensaverSettingsSheet(
        config = config,
        lifetimeStats = lifetimeStats,
        accentColor = activeColor,
        onDismiss = viewModel::closeSettings,
        onSpeedChange = viewModel::setSpeedMultiplier,
        onLogoScaleChange = viewModel::setLogoScale,
        onPaletteSelect = viewModel::selectPalette,
        onNeonGlowChange = viewModel::setNeonGlow,
        onCrtScanlinesChange = viewModel::setCrtScanlines,
        onPureOledBlackChange = viewModel::setPureOledBlack,
        onImmersiveModeChange = viewModel::setImmersiveMode,
        onKeepScreenOnChange = viewModel::setKeepScreenOn,
        onCornerHapticsChange = viewModel::setCornerHaptics,
        onResetStats = viewModel::resetStats,
      )
    }
  }
}

@Composable
private fun HudMetricPill(
  label: String,
  value: String,
  accentColor: Color,
  testTag: String,
) {
  Column(horizontalAlignment = Alignment.CenterHorizontally) {
    Text(
      text = label,
      style = MaterialTheme.typography.labelSmall,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    Text(
      text = value,
      style = MaterialTheme.typography.labelMedium,
      fontWeight = FontWeight.Bold,
      color = accentColor,
      modifier = Modifier.testTag(testTag),
    )
  }
}
