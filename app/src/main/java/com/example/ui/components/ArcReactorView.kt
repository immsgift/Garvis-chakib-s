package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.ui.theme.GarvisGold
import com.example.ui.theme.GarvisRed
import com.example.ui.theme.GarvisRedBright
import com.example.ui.theme.GarvisRedDark
import com.example.ui.theme.GarvisRedGlow
import com.example.ui.theme.GarvisRedSoft
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

enum class GarvisStatus {
  ONLINE,
  STANDBY,
  LISTENING,
  PROCESSING,
  SPEAKING
}

@Composable
fun ArcReactorView(
  status: GarvisStatus,
  rmsAudioLevel: Float, // 0.0 to 1.0
  onReactorClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  val infiniteTransition = rememberInfiniteTransition(label = "ArcReactorTransitions")

  // Outer ring rotation speed depending on state
  val outerRotationDuration = when (status) {
    GarvisStatus.PROCESSING -> 3000
    GarvisStatus.LISTENING -> 4500
    GarvisStatus.SPEAKING -> 5500
    else -> 11000
  }
  val outerRotation = infiniteTransition.animateFloat(
    initialValue = 0f,
    targetValue = 360f,
    animationSpec = infiniteRepeatable(
      animation = tween(outerRotationDuration, easing = LinearEasing),
      repeatMode = RepeatMode.Restart
    ),
    label = "outerRotation"
  )

  // Inner ring counter-rotation
  val innerRotationDuration = when (status) {
    GarvisStatus.PROCESSING -> 1800
    GarvisStatus.LISTENING -> 3500
    GarvisStatus.SPEAKING -> 4000
    else -> 8000
  }
  val innerRotation = infiniteTransition.animateFloat(
    initialValue = 360f,
    targetValue = 0f,
    animationSpec = infiniteRepeatable(
      animation = tween(innerRotationDuration, easing = LinearEasing),
      repeatMode = RepeatMode.Restart
    ),
    label = "innerRotation"
  )

  // Pulsing glow animation
  val pulseDuration = when (status) {
    GarvisStatus.SPEAKING -> 750
    GarvisStatus.PROCESSING -> 500
    GarvisStatus.LISTENING -> 900
    else -> 2000
  }
  val pulse = infiniteTransition.animateFloat(
    initialValue = 0.85f,
    targetValue = 1.18f,
    animationSpec = infiniteRepeatable(
      animation = tween(pulseDuration, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "pulse"
  )

  // Energy flux jitter for processing/speaking
  val fluxJitter = infiniteTransition.animateFloat(
    initialValue = 0.92f,
    targetValue = 1.08f,
    animationSpec = infiniteRepeatable(
      animation = tween(110, easing = LinearEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "fluxJitter"
  )

  Box(
    modifier = modifier
      .size(260.dp)
      .testTag("arc_reactor_button")
      .clickable(
        interactionSource = remember { MutableInteractionSource() },
        indication = null,
        onClick = onReactorClick
      ),
    contentAlignment = Alignment.Center
  ) {
    Canvas(modifier = Modifier.fillMaxSize()) {
      val center = Offset(size.width / 2f, size.height / 2f)
      val radius = size.minDimension / 2f * 0.92f

      // Audio reactivity boost
      val audioBoost = if (status == GarvisStatus.LISTENING || status == GarvisStatus.SPEAKING) {
        rmsAudioLevel * 0.4f
      } else {
        0f
      }

      val effectivePulse = (pulse.value + audioBoost) * if (status == GarvisStatus.PROCESSING) fluxJitter.value else 1f

      // 1. Outermost Radial Background Glow (Blazing Crimson Red / Gold)
      val glowColor = when (status) {
        GarvisStatus.LISTENING -> GarvisRedBright
        GarvisStatus.PROCESSING -> GarvisGold
        GarvisStatus.SPEAKING -> GarvisRed
        else -> GarvisRed
      }
      drawCircle(
        brush = Brush.radialGradient(
          colors = listOf(
            glowColor.copy(alpha = 0.42f * effectivePulse.coerceIn(0.6f, 1.4f)),
            glowColor.copy(alpha = 0.15f),
            Color.Transparent
          ),
          center = center,
          radius = radius * 1.3f
        ),
        radius = radius * 1.3f,
        center = center
      )

      // 2. Outermost Calibration Ring with Red & Gold Tick Marks
      drawCircle(
        color = GarvisRed.copy(alpha = 0.35f),
        radius = radius * 0.98f,
        center = center,
        style = Stroke(width = 1.5.dp.toPx())
      )
      drawTicks(
        center = center,
        radius = radius * 0.98f,
        tickCount = 36,
        tickLength = 5.dp.toPx(),
        strokeWidth = 1.dp.toPx(),
        color = GarvisGold.copy(alpha = 0.55f)
      )

      // 3. Rotating Outer Segmented Stator Housing
      rotate(outerRotation.value, pivot = center) {
        // Outer stator boundary
        drawCircle(
          color = GarvisRed.copy(alpha = 0.75f),
          radius = radius * 0.85f,
          center = center,
          style = Stroke(width = 2.dp.toPx())
        )

        // 10 Inductor Coils around circumference
        val coilCount = 10
        val coilRadius = radius * 0.85f
        val coilWidth = 14.dp.toPx()
        val coilHeight = 10.dp.toPx()

        for (i in 0 until coilCount) {
          val angleDeg = (i * 36f)
          val angleRad = Math.toRadians(angleDeg.toDouble())
          val coilCenter = Offset(
            x = center.x + (coilRadius * cos(angleRad)).toFloat(),
            y = center.y + (coilRadius * sin(angleRad)).toFloat()
          )

          rotate(angleDeg + 90f, pivot = coilCenter) {
            // Inductor block in Red / Gold
            val blockColor = if (i % 2 == 0) GarvisRed else GarvisGold
            drawRect(
              color = blockColor,
              topLeft = Offset(coilCenter.x - coilWidth / 2f, coilCenter.y - coilHeight / 2f),
              size = androidx.compose.ui.geometry.Size(coilWidth, coilHeight)
            )
            // Inner wire wrap line
            drawLine(
              color = Color.White.copy(alpha = 0.95f),
              start = Offset(coilCenter.x - coilWidth / 2f, coilCenter.y),
              end = Offset(coilCenter.x + coilWidth / 2f, coilCenter.y),
              strokeWidth = 2.dp.toPx()
            )
          }
        }
      }

      // 4. Middle Counter-Rotating Track
      rotate(innerRotation.value, pivot = center) {
        drawCircle(
          color = GarvisGold.copy(alpha = 0.65f),
          radius = radius * 0.62f,
          center = center,
          style = Stroke(
            width = 3.dp.toPx(),
            cap = StrokeCap.Round
          )
        )
        // Dashed inner tick arcs
        drawTicks(
          center = center,
          radius = radius * 0.62f,
          tickCount = 20,
          tickLength = 6.dp.toPx(),
          strokeWidth = 1.5.dp.toPx(),
          color = GarvisRed.copy(alpha = 0.8f)
        )
      }

      // 5. Stationary Crosshairs
      val crossLen = radius * 0.72f
      drawLine(
        color = GarvisRed.copy(alpha = 0.4f),
        start = Offset(center.x - crossLen, center.y),
        end = Offset(center.x - radius * 0.45f, center.y),
        strokeWidth = 1.dp.toPx()
      )
      drawLine(
        color = GarvisRed.copy(alpha = 0.4f),
        start = Offset(center.x + radius * 0.45f, center.y),
        end = Offset(center.x + crossLen, center.y),
        strokeWidth = 1.dp.toPx()
      )
      drawLine(
        color = GarvisRed.copy(alpha = 0.4f),
        start = Offset(center.x, center.y - crossLen),
        end = Offset(center.x, center.y - radius * 0.45f),
        strokeWidth = 1.dp.toPx()
      )
      drawLine(
        color = GarvisRed.copy(alpha = 0.4f),
        start = Offset(center.x, center.y + radius * 0.45f),
        end = Offset(center.x, center.y + crossLen),
        strokeWidth = 1.dp.toPx()
      )

      // 6. Inner Core Housing Ring with intense Crimson Red Pulse
      val coreRadius = radius * 0.42f * effectivePulse.coerceIn(0.85f, 1.25f)
      drawCircle(
        brush = Brush.radialGradient(
          colors = listOf(
            Color.White,
            GarvisRedSoft,
            GarvisRed,
            GarvisRedDark
          ),
          center = center,
          radius = coreRadius
        ),
        radius = coreRadius,
        center = center
      )

      // 7. Nanotech Triangular Core Structure
      rotate(-outerRotation.value * 0.5f, pivot = center) {
        val triRadius = coreRadius * 0.68f
        val trianglePath = Path().apply {
          val p1 = Offset(
            center.x + (triRadius * cos(-PI / 2)).toFloat(),
            center.y + (triRadius * sin(-PI / 2)).toFloat()
          )
          val p2 = Offset(
            center.x + (triRadius * cos(PI / 6)).toFloat(),
            center.y + (triRadius * sin(PI / 6)).toFloat()
          )
          val p3 = Offset(
            center.x + (triRadius * cos(5 * PI / 6)).toFloat(),
            center.y + (triRadius * sin(5 * PI / 6)).toFloat()
          )
          moveTo(p1.x, p1.y)
          lineTo(p2.x, p2.y)
          lineTo(p3.x, p3.y)
          close()
        }
        drawPath(
          path = trianglePath,
          color = Color(0xFF14070A).copy(alpha = 0.88f)
        )
        drawPath(
          path = trianglePath,
          color = if (status == GarvisStatus.PROCESSING) GarvisGold else GarvisRedBright,
          style = Stroke(width = 2.dp.toPx())
        )
      }

      // 8. Luminescent Central Core Hotspot (Blinding clean hot white)
      drawCircle(
        color = Color.White,
        radius = coreRadius * 0.28f,
        center = center
      )
    }
  }
}

private fun DrawScope.drawTicks(
  center: Offset,
  radius: Float,
  tickCount: Int,
  tickLength: Float,
  strokeWidth: Float,
  color: Color
) {
  val step = (2 * PI / tickCount).toFloat()
  for (i in 0 until tickCount) {
    val angle = i * step
    val startX = center.x + (radius - tickLength) * cos(angle)
    val startY = center.y + (radius - tickLength) * sin(angle)
    val endX = center.x + radius * cos(angle)
    val endY = center.y + radius * sin(angle)
    drawLine(
      color = color,
      start = Offset(startX, startY),
      end = Offset(endX, endY),
      strokeWidth = strokeWidth
    )
  }
}
