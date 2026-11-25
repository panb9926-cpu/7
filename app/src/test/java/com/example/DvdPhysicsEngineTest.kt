package com.example

import com.example.physics.BounceBody
import com.example.physics.DvdPhysicsEngine
import kotlin.random.Random
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DvdPhysicsEngineTest {

  @Test
  fun noCollision_keepsColorAndVelocity() {
    val start = BounceBody(x = 100f, y = 100f, vx = 200f, vy = 150f, colorIndex = 2)
    val result =
      DvdPhysicsEngine.step(
        body = start,
        deltaSeconds = 0.016f,
        speedMultiplier = 1f,
        boundsWidth = 1000f,
        boundsHeight = 1000f,
        logoWidth = 100f,
        logoHeight = 50f,
        paletteSize = 6,
        cornerTolerancePx = 8f,
        random = Random(42),
      )

    assertFalse(result.hitAnyWall)
    assertEquals(2, result.body.colorIndex)
    assertTrue(result.body.vx > 0f)
    assertTrue(result.body.vy > 0f)
  }

  @Test
  fun rightWallCollision_reversesVxAndChangesColor() {
    val start = BounceBody(x = 895f, y = 200f, vx = 200f, vy = 100f, colorIndex = 3)
    val result =
      DvdPhysicsEngine.step(
        body = start,
        deltaSeconds = 0.05f,
        speedMultiplier = 1f,
        boundsWidth = 1000f,
        boundsHeight = 1000f,
        logoWidth = 100f,
        logoHeight = 50f,
        paletteSize = 6,
        cornerTolerancePx = 8f,
        random = Random(7),
      )

    assertTrue(result.hitHorizontalWall)
    assertFalse(result.hitVerticalWall)
    assertFalse(result.hitCorner)
    assertTrue(result.body.vx < 0f)
    assertTrue(result.body.vy > 0f)
    assertNotEquals(3, result.body.colorIndex)
    assertTrue(result.body.x in 0f..900f)
  }

  @Test
  fun cornerCollision_triggersCornerFlagAndReversesBothAxes() {
    val start = BounceBody(x = 4f, y = 4f, vx = -300f, vy = -300f, colorIndex = 0)
    val result =
      DvdPhysicsEngine.step(
        body = start,
        deltaSeconds = 0.02f,
        speedMultiplier = 1f,
        boundsWidth = 800f,
        boundsHeight = 1200f,
        logoWidth = 120f,
        logoHeight = 60f,
        paletteSize = 8,
        cornerTolerancePx = 8f,
        random = Random(1),
      )

    assertTrue(result.hitHorizontalWall)
    assertTrue(result.hitVerticalWall)
    assertTrue(result.hitCorner)
    assertTrue(result.body.vx > 0f)
    assertTrue(result.body.vy > 0f)
    assertNotEquals(0, result.body.colorIndex)
  }

  @Test
  fun clampIntoBounds_keepsLogoInsideResizedViewport() {
    val body = BounceBody(x = 950f, y = 1800f, vx = 100f, vy = 100f, colorIndex = 1)
    val clamped =
      DvdPhysicsEngine.clampIntoBounds(
        body = body,
        boundsWidth = 600f,
        boundsHeight = 800f,
        logoWidth = 120f,
        logoHeight = 60f,
      )

    assertEquals(480f, clamped.x)
    assertEquals(740f, clamped.y)
  }
}
