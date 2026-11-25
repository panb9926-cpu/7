package com.example.dream

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.os.SystemClock
import android.service.dreams.DreamService
import android.view.View
import com.example.data.PreferencesRepository
import com.example.model.BouncePalette
import com.example.model.ScreensaverConfig
import com.example.physics.BounceBody
import com.example.physics.DvdPhysicsEngine
import com.example.ui.DvdLogoRenderer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class DvdDreamService : DreamService() {

  private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
  private var dreamView: DvdDreamView? = null

  override fun onAttachedToWindow() {
    super.onAttachedToWindow()
    isInteractive = false
    isFullscreen = true
    isScreenBright = false

    val view = DvdDreamView(this)
    dreamView = view
    setContentView(view)

    serviceScope.launch {
      val config =
        runCatching { PreferencesRepository(applicationContext).configFlow.first() }
          .getOrDefault(ScreensaverConfig())
      view.applyConfig(config)
    }
  }

  override fun onDreamingStarted() {
    super.onDreamingStarted()
    dreamView?.startLoop()
  }

  override fun onDreamingStopped() {
    dreamView?.stopLoop()
    super.onDreamingStopped()
  }

  override fun onDetachedFromWindow() {
    dreamView?.stopLoop()
    serviceScope.cancel()
    dreamView = null
    super.onDetachedFromWindow()
  }
}

private class DvdDreamView(context: Context) : View(context) {

  private val paint =
    Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }

  private var config = ScreensaverConfig()
  private var running = false
  private var lastFrameMs = 0L

  private var logoWidth = 0f
  private var logoHeight = 0f
  private var cachedPath: Path? = null

  private var body =
    BounceBody(
      x = 48f,
      y = 64f,
      vx = 180f,
      vy = 150f,
      colorIndex = 0,
    )

  private val frameRunnable =
    object : Runnable {
      override fun run() {
        if (!running) return
        val now = SystemClock.uptimeMillis()
        val dt = ((now - lastFrameMs).coerceIn(1L, 50L)) / 1000f
        lastFrameMs = now
        stepPhysics(dt)
        invalidate()
        postOnAnimation(this)
      }
    }

  fun applyConfig(newConfig: ScreensaverConfig) {
    config = newConfig
    rebuildGeometry()
  }

  fun startLoop() {
    if (running) return
    running = true
    lastFrameMs = SystemClock.uptimeMillis()
    postOnAnimation(frameRunnable)
  }

  fun stopLoop() {
    running = false
    removeCallbacks(frameRunnable)
  }

  override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
    super.onSizeChanged(w, h, oldw, oldh)
    rebuildGeometry()
    body = DvdPhysicsEngine.clampIntoBounds(body, w.toFloat(), h.toFloat(), logoWidth, logoHeight)
  }

  private fun rebuildGeometry() {
    val density = resources.displayMetrics.density
    val minDim = minOf(width.takeIf { it > 0 } ?: 1080, height.takeIf { it > 0 } ?: 1920).toFloat()
    val baseW = (minDim * 0.28f).coerceIn(112f * density, 220f * density)
    logoWidth = baseW * config.logoScale
    logoHeight = logoWidth / DvdLogoRenderer.ASPECT_RATIO
    cachedPath = DvdLogoRenderer.createScaledAndroidPath(logoWidth, logoHeight)

    val baseSpeed = 165f * density
    val signX = if (body.vx >= 0f) 1f else -1f
    val signY = if (body.vy >= 0f) 1f else -1f
    body = body.copy(vx = signX * baseSpeed, vy = signY * baseSpeed * 0.86f)
  }

  private fun stepPhysics(dt: Float) {
    val w = width.toFloat()
    val h = height.toFloat()
    if (w <= 0f || h <= 0f || logoWidth <= 0f || logoHeight <= 0f) return

    val paletteColors = config.palette.colors
    val tolerance = 6f * resources.displayMetrics.density
    val result =
      DvdPhysicsEngine.step(
        body = body,
        deltaSeconds = dt,
        speedMultiplier = config.speedMultiplier,
        boundsWidth = w,
        boundsHeight = h,
        logoWidth = logoWidth,
        logoHeight = logoHeight,
        paletteSize = paletteColors.size,
        cornerTolerancePx = tolerance,
      )
    body = result.body
  }

  override fun onDraw(canvas: Canvas) {
    canvas.drawColor(Color.BLACK)
    val path = cachedPath ?: return
    val colors = config.palette.colors.ifEmpty { BouncePalette.CLASSIC_NEON.colors }
    val namedColor = colors[body.colorIndex % colors.size]
    DvdLogoRenderer.drawOnAndroidCanvas(
      canvas = canvas,
      x = body.x,
      y = body.y,
      logoWidth = logoWidth,
      logoHeight = logoHeight,
      scaledPath = path,
      color = namedColor.color,
      paint = paint,
    )
  }
}
