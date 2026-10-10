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
import com.example.ui.theme.GarvisAmber
import com.example.ui.theme.GarvisCyan
import com.example.ui.theme.GarvisCyanGlow
import com.example.ui.theme.GarvisCyanSoft
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
    GarvisStatus.LISTENING -> 5000
    GarvisStatus.SPEAKING -> 6000
    else -> 12000
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
    GarvisStatus.PROCESSING -> 2000
    GarvisStatus.LISTENING -> 4000
    GarvisStatus.SPEAKING -> 4500
    else -> 9000
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
    GarvisStatus.SPEAKING -> 800
    GarvisStatus.PROCESSING -> 600
    GarvisStatus.LISTENING -> 1000
    else -> 2200
  }
  val pulse = infiniteTransition.animateFloat(
    initialValue = 0.85f,
    targetValue = 1.15f,
    animationSpec = infiniteRepeatable(
      animation = tween(pulseDuration, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "pulse"
  )

  // Energy flux jitter for processing/speaking
  val fluxJitter = infiniteTransition.animateFloat(
    initialValue = 0.9f,
    targetValue = 1.1f,
    animationSpec = infiniteRepeatable(
      animation = tween(120, easing = LinearEasing),
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
        rmsAudioLevel * 0.35f
      } else {
        0f
      }

      val effectivePulse = (pulse.value + audioBoost) * if (status == GarvisStatus.PROCESSING) fluxJitter.value else 1f

      // 1. Outermost Radial Background Glow
      val glowColor = when (status) {
        GarvisStatus.LISTENING -> GarvisCyan
        GarvisStatus.PROCESSING -> GarvisAmber
        GarvisStatus.SPEAKING -> GarvisCyanSoft
        else -> GarvisCyan
      }
      drawCircle(
        brush = Brush.radialGradient(
          colors = listOf(
            glowColor.copy(alpha = 0.35f * effectivePulse.coerceIn(0.6f, 1.4f)),
            glowColor.copy(alpha = 0.12f),
            Color.Transparent
          ),
          center = center,
          radius = radius * 1.25f
        ),
        radius = radius * 1.25f,
        center = center
      )

      // 2. Outermost Calibration Ring with Tick Marks
      drawCircle(
        color = GarvisCyan.copy(alpha = 0.25f),
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
        color = GarvisCyan.copy(alpha = 0.4f)
      )

      // 3. Rotating Outer Segmented Stator Housing
      rotate(outerRotation.value, pivot = center) {
        // Outer stator boundary
        drawCircle(
          color = GarvisCyan.copy(alpha = 0.6f),
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
            // Inductor block
            drawRect(
              color = if (status == GarvisStatus.PROCESSING && i % 2 == 0) GarvisAmber else GarvisCyan,
              topLeft = Offset(coilCenter.x - coilWidth / 2f, coilCenter.y - coilHeight / 2f),
              size = androidx.compose.ui.geometry.Size(coilWidth, coilHeight)
            )
            // Inner wire wrap line
            drawLine(
              color = Color.White.copy(alpha = 0.9f),
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
          color = GarvisCyan.copy(alpha = 0.5f),
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
          color = GarvisCyan.copy(alpha = 0.7f)
        )
      }

      // 5. Stationary Crosshair Radians
      val crossLen = radius * 0.72f
      drawLine(
        color = GarvisCyan.copy(alpha = 0.35f),
        start = Offset(center.x - crossLen, center.y),
        end = Offset(center.x - radius * 0.45f, center.y),
        strokeWidth = 1.dp.toPx()
      )
      drawLine(
        color = GarvisCyan.copy(alpha = 0.35f),
        start = Offset(center.x + radius * 0.45f, center.y),
        end = Offset(center.x + crossLen, center.y),
        strokeWidth = 1.dp.toPx()
      )
      drawLine(
        color = GarvisCyan.copy(alpha = 0.35f),
        start = Offset(center.x, center.y - crossLen),
        end = Offset(center.x, center.y - radius * 0.45f),
        strokeWidth = 1.dp.toPx()
      )
      drawLine(
        color = GarvisCyan.copy(alpha = 0.35f),
        start = Offset(center.x, center.y + radius * 0.45f),
        end = Offset(center.x, center.y + crossLen),
        strokeWidth = 1.dp.toPx()
      )

      // 6. Inner Core Housing Ring with intense pulse
      val coreRadius = radius * 0.42f * effectivePulse.coerceIn(0.85f, 1.25f)
      drawCircle(
        brush = Brush.radialGradient(
          colors = listOf(
            Color.White,
            GarvisCyanSoft,
            GarvisCyan,
            GarvisCyanGlow
          ),
          center = center,
          radius = coreRadius
        ),
        radius = coreRadius,
        center = center
      )

      // 7. Vibranium Triangular / Hexagonal Core Structure
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
          color = Color(0xFF0A0E17).copy(alpha = 0.85f)
        )
        drawPath(
          path = trianglePath,
          color = if (status == GarvisStatus.PROCESSING) GarvisAmber else GarvisCyan,
          style = Stroke(width = 2.dp.toPx())
        )
      }

      // 8. Luminescent Central Core Hotspot (Blinding clean white-cyan)
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
