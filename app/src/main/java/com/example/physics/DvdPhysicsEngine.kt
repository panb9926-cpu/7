package com.example.physics

import kotlin.math.abs
import kotlin.random.Random

data class BounceBody(
  val x: Float,
  val y: Float,
  val vx: Float,
  val vy: Float,
  val colorIndex: Int,
)

data class StepResult(
  val body: BounceBody,
  val hitHorizontalWall: Boolean,
  val hitVerticalWall: Boolean,
  val hitCorner: Boolean,
  val impactX: Float,
  val impactY: Float,
) {
  val hitAnyWall: Boolean
    get() = hitHorizontalWall || hitVerticalWall
}

object DvdPhysicsEngine {

  /**
   * Advances the DVD logo by [deltaSeconds] inside [boundsWidth] x [boundsHeight] for a logo of
   * size [logoWidth] x [logoHeight], shifting to a distinct color index on every wall collision.
   */
  fun step(
    body: BounceBody,
    deltaSeconds: Float,
    speedMultiplier: Float,
    boundsWidth: Float,
    boundsHeight: Float,
    logoWidth: Float,
    logoHeight: Float,
    paletteSize: Int,
    cornerTolerancePx: Float,
    random: Random = Random.Default,
  ): StepResult {
    if (boundsWidth <= 0f || boundsHeight <= 0f || logoWidth <= 0f || logoHeight <= 0f) {
      return StepResult(
        body = body,
        hitHorizontalWall = false,
        hitVerticalWall = false,
        hitCorner = false,
        impactX = body.x,
        impactY = body.y,
      )
    }

    val dt = deltaSeconds.coerceIn(0f, 0.05f)
    val maxX = (boundsWidth - logoWidth).coerceAtLeast(0f)
    val maxY = (boundsHeight - logoHeight).coerceAtLeast(0f)

    var nextX = body.x + body.vx * speedMultiplier * dt
    var nextY = body.y + body.vy * speedMultiplier * dt
    var nextVx = body.vx
    var nextVy = body.vy

    var hitX = false
    var hitY = false

    if (maxX > 0f) {
      if (nextX <= 0f) {
        nextX = -nextX.coerceAtMost(maxX)
        nextVx = abs(body.vx)
        hitX = true
      } else if (nextX >= maxX) {
        val overshoot = nextX - maxX
        nextX = (maxX - overshoot).coerceAtLeast(0f)
        nextVx = -abs(body.vx)
        hitX = true
      }
    } else {
      nextX = 0f
    }

    if (maxY > 0f) {
      if (nextY <= 0f) {
        nextY = -nextY.coerceAtMost(maxY)
        nextVy = abs(body.vy)
        hitY = true
      } else if (nextY >= maxY) {
        val overshoot = nextY - maxY
        nextY = (maxY - overshoot).coerceAtLeast(0f)
        nextVy = -abs(body.vy)
        hitY = true
      }
    } else {
      nextY = 0f
    }

    val nearHorizontalEdge = nextX <= cornerTolerancePx || (maxX - nextX) <= cornerTolerancePx
    val nearVerticalEdge = nextY <= cornerTolerancePx || (maxY - nextY) <= cornerTolerancePx
    val isCorner =
      (hitX && hitY) || ((hitX || hitY) && nearHorizontalEdge && nearVerticalEdge)

    val nextColorIndex =
      if ((hitX || hitY) && paletteSize > 1) {
        pickNextColorIndex(body.colorIndex, paletteSize, random)
      } else {
        body.colorIndex.coerceIn(0, (paletteSize - 1).coerceAtLeast(0))
      }

    val impactX =
      when {
        hitX && nextVx > 0f -> 0f
        hitX && nextVx < 0f -> boundsWidth
        else -> (nextX + logoWidth * 0.5f).coerceIn(0f, boundsWidth)
      }

    val impactY =
      when {
        hitY && nextVy > 0f -> 0f
        hitY && nextVy < 0f -> boundsHeight
        else -> (nextY + logoHeight * 0.5f).coerceIn(0f, boundsHeight)
      }

    return StepResult(
      body = BounceBody(x = nextX, y = nextY, vx = nextVx, vy = nextVy, colorIndex = nextColorIndex),
      hitHorizontalWall = hitX,
      hitVerticalWall = hitY,
      hitCorner = isCorner,
      impactX = impactX,
      impactY = impactY,
    )
  }

  fun clampIntoBounds(
    body: BounceBody,
    boundsWidth: Float,
    boundsHeight: Float,
    logoWidth: Float,
    logoHeight: Float,
  ): BounceBody {
    val maxX = (boundsWidth - logoWidth).coerceAtLeast(0f)
    val maxY = (boundsHeight - logoHeight).coerceAtLeast(0f)
    return body.copy(x = body.x.coerceIn(0f, maxX), y = body.y.coerceIn(0f, maxY))
  }

  fun pickNextColorIndex(currentIndex: Int, paletteSize: Int, random: Random = Random.Default): Int {
    if (paletteSize <= 1) return 0
    val offset = 1 + random.nextInt(paletteSize - 1)
    return ((currentIndex % paletteSize) + paletteSize + offset) % paletteSize
  }
}
