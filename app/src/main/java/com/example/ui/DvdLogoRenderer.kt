package com.example.ui

import android.graphics.Canvas as AndroidCanvas
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path as AndroidPath
import android.graphics.RectF
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.asComposePath
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.toArgb

object DvdLogoRenderer {
  const val BASE_WIDTH = 220f
  const val BASE_HEIGHT = 104f
  const val ASPECT_RATIO = BASE_WIDTH / BASE_HEIGHT

  private fun buildNormalizedAndroidPath(): AndroidPath {
    val lettersPath = AndroidPath().apply { fillType = AndroidPath.FillType.EVEN_ODD }

    // Left 'D'
    lettersPath.moveTo(20f, 8f)
    lettersPath.lineTo(46f, 8f)
    lettersPath.cubicTo(74f, 8f, 74f, 60f, 46f, 60f)
    lettersPath.lineTo(20f, 60f)
    lettersPath.close()

    // Left 'D' inner cutout
    lettersPath.moveTo(35f, 20f)
    lettersPath.lineTo(45f, 20f)
    lettersPath.cubicTo(58f, 20f, 58f, 48f, 45f, 48f)
    lettersPath.lineTo(35f, 48f)
    lettersPath.close()

    // Center 'V'
    lettersPath.moveTo(72f, 8f)
    lettersPath.lineTo(89f, 8f)
    lettersPath.lineTo(105f, 43f)
    lettersPath.lineTo(121f, 8f)
    lettersPath.lineTo(138f, 8f)
    lettersPath.lineTo(113f, 60f)
    lettersPath.lineTo(97f, 60f)
    lettersPath.close()

    // Right 'D'
    lettersPath.moveTo(138f, 8f)
    lettersPath.lineTo(164f, 8f)
    lettersPath.cubicTo(192f, 8f, 192f, 60f, 164f, 60f)
    lettersPath.lineTo(138f, 60f)
    lettersPath.close()

    // Right 'D' inner cutout
    lettersPath.moveTo(153f, 20f)
    lettersPath.lineTo(163f, 20f)
    lettersPath.cubicTo(176f, 20f, 176f, 48f, 163f, 48f)
    lettersPath.lineTo(153f, 48f)
    lettersPath.close()

    // Apply classic italic slant around the vertical midpoint of the letters
    val slantMatrix = Matrix().apply { setSkew(-0.28f, 0f, 110f, 34f) }
    lettersPath.transform(slantMatrix)

    val discPath = AndroidPath().apply { fillType = AndroidPath.FillType.EVEN_ODD }
    discPath.addOval(RectF(12f, 67f, 208f, 99f), AndroidPath.Direction.CW)
    discPath.addOval(RectF(86f, 78f, 134f, 88f), AndroidPath.Direction.CW)

    val combined = AndroidPath().apply { fillType = AndroidPath.FillType.EVEN_ODD }
    combined.addPath(lettersPath)
    combined.addPath(discPath)
    return combined
  }

  fun createScaledComposePath(targetWidth: Float, targetHeight: Float): Path {
    val androidPath = buildNormalizedAndroidPath()
    val scaleMatrix =
      Matrix().apply { setScale(targetWidth / BASE_WIDTH, targetHeight / BASE_HEIGHT) }
    androidPath.transform(scaleMatrix)
    return androidPath.asComposePath().apply { fillType = PathFillType.EvenOdd }
  }

  fun createScaledAndroidPath(targetWidth: Float, targetHeight: Float): AndroidPath {
    val androidPath = buildNormalizedAndroidPath()
    val scaleMatrix =
      Matrix().apply { setScale(targetWidth / BASE_WIDTH, targetHeight / BASE_HEIGHT) }
    androidPath.transform(scaleMatrix)
    return androidPath
  }

  fun DrawScope.drawDvdLogo(
    x: Float,
    y: Float,
    logoWidth: Float,
    logoHeight: Float,
    cachedPath: Path,
    color: Color,
    neonGlowEnabled: Boolean,
  ) {
    if (neonGlowEnabled) {
      val center = Offset(x + logoWidth * 0.5f, y + logoHeight * 0.5f)
      val glowRadius = logoWidth * 0.85f
      drawCircle(
        brush =
          Brush.radialGradient(
            colors = listOf(color.copy(alpha = 0.28f), color.copy(alpha = 0.08f), Color.Transparent),
            center = center,
            radius = glowRadius,
          ),
        radius = glowRadius,
        center = center,
      )
    }

    translate(left = x, top = y) {
      if (neonGlowEnabled) {
        drawPath(path = cachedPath, color = color.copy(alpha = 0.24f))
      }
      drawPath(path = cachedPath, color = color)
    }
  }

  fun drawOnAndroidCanvas(
    canvas: AndroidCanvas,
    x: Float,
    y: Float,
    logoWidth: Float,
    logoHeight: Float,
    scaledPath: AndroidPath,
    color: Color,
    paint: Paint,
  ) {
    paint.color = color.toArgb()
    canvas.save()
    canvas.translate(x, y)
    canvas.drawPath(scaledPath, paint)
    canvas.restore()
  }
}
