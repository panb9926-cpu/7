package com.example.model

import androidx.compose.ui.graphics.Color

data class NamedColor(val name: String, val color: Color)

enum class BouncePalette(val displayName: String, val colors: List<NamedColor>) {
  CLASSIC_NEON(
    displayName = "Classic Neon",
    colors =
      listOf(
        NamedColor("Electric Cyan", Color(0xFF00E5FF)),
        NamedColor("Hot Magenta", Color(0xFFFF2E93)),
        NamedColor("Acid Lime", Color(0xFFAAFF00)),
        NamedColor("Solar Amber", Color(0xFFFFB300)),
        NamedColor("Laser Violet", Color(0xFFB366FF)),
        NamedColor("Coral Red", Color(0xFFFF4D4D)),
        NamedColor("Cobalt Blue", Color(0xFF3D7BFF)),
        NamedColor("Mint Glow", Color(0xFF00FFA3)),
      ),
  ),
  VAPORWAVE(
    displayName = "Vaporwave",
    colors =
      listOf(
        NamedColor("Miami Pink", Color(0xFFFF71CE)),
        NamedColor("Cyber Cyan", Color(0xFF01CDFE)),
        NamedColor("Sunset Peach", Color(0xFFFFB86C)),
        NamedColor("Dream Purple", Color(0xFFB967FF)),
        NamedColor("Mint Soda", Color(0xFF05FFA1)),
        NamedColor("Lemon Fizz", Color(0xFFFFFB96)),
      ),
  ),
  CRT_PHOSPHOR(
    displayName = "CRT Phosphor",
    colors =
      listOf(
        NamedColor("P1 Green", Color(0xFF33FF66)),
        NamedColor("P3 Amber", Color(0xFFFFB000)),
        NamedColor("P4 White", Color(0xFFEAF4FF)),
        NamedColor("P31 Aqua", Color(0xFF28FFD8)),
        NamedColor("P22 Red", Color(0xFFFF5E4D)),
      ),
  ),
  PASTEL_POP(
    displayName = "Pastel Pop",
    colors =
      listOf(
        NamedColor("Sky Mist", Color(0xFF7CE8FF)),
        NamedColor("Bubblegum", Color(0xFFFF9CE3)),
        NamedColor("Buttercup", Color(0xFFFFE57F)),
        NamedColor("Lavender", Color(0xFFC5A3FF)),
        NamedColor("Seafoam", Color(0xFF8CFFDA)),
        NamedColor("Apricot", Color(0xFFFFAE8A)),
      ),
  );

  fun next(): BouncePalette {
    val all = entries
    return all[(ordinal + 1) % all.size]
  }
}

data class ScreensaverConfig(
  val speedMultiplier: Float = 1.0f,
  val logoScale: Float = 1.0f,
  val palette: BouncePalette = BouncePalette.CLASSIC_NEON,
  val neonGlowEnabled: Boolean = true,
  val crtScanlinesEnabled: Boolean = true,
  val pureOledBlack: Boolean = false,
  val immersiveMode: Boolean = true,
  val keepScreenOn: Boolean = true,
  val cornerHapticsEnabled: Boolean = true,
)

data class ImpactRipple(
  val id: Long,
  val x: Float,
  val y: Float,
  val color: Color,
  val isCorner: Boolean,
  val progress: Float = 0f,
)
