package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.example.ui.theme.GarvisGold
import com.example.ui.theme.GarvisRed
import com.example.ui.theme.GarvisRedBright
import kotlin.math.sin

@Composable
fun AudioWaveformBar(
  status: GarvisStatus,
  rmsAudioLevel: Float,
  modifier: Modifier = Modifier
) {
  val barCount = 24
  val infiniteTransition = rememberInfiniteTransition(label = "WaveformAnim")

  val phase by infiniteTransition.animateFloat(
    initialValue = 0f,
    targetValue = (2 * Math.PI).toFloat(),
    animationSpec = infiniteRepeatable(
      animation = tween(1200, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Restart
    ),
    label = "wavePhase"
  )

  Row(
    modifier = modifier
      .fillMaxWidth()
      .height(26.dp),
    horizontalArrangement = Arrangement.Center,
    verticalAlignment = Alignment.CenterVertically
  ) {
    for (i in 0 until barCount) {
      val normalizedIndex = i.toFloat() / barCount
      val baseHeight = when (status) {
        GarvisStatus.LISTENING -> {
          val sineVal = (sin(phase + normalizedIndex * 4f) + 1f) / 2f
          (4f + (sineVal * 18f * (0.3f + rmsAudioLevel * 0.7f))).coerceIn(4f, 26f)
        }
        GarvisStatus.SPEAKING -> {
          val sineVal = (sin(phase * 1.5f + normalizedIndex * 6f) + 1f) / 2f
          (5f + (sineVal * 20f)).coerceIn(5f, 26f)
        }
        GarvisStatus.PROCESSING -> {
          val sineVal = (sin(phase * 2f + normalizedIndex * 8f) + 1f) / 2f
          (4f + (sineVal * 16f)).coerceIn(4f, 24f)
        }
        else -> {
          val sineVal = (sin(phase * 0.5f + normalizedIndex * 3f) + 1f) / 2f
          (3f + (sineVal * 5f)).coerceIn(3f, 8f)
        }
      }

      val barColor = when (status) {
        GarvisStatus.PROCESSING -> GarvisGold
        GarvisStatus.LISTENING -> GarvisRedBright
        GarvisStatus.SPEAKING -> GarvisRed
        else -> GarvisRed.copy(alpha = 0.5f)
      }

      Box(
        modifier = Modifier
          .padding(horizontal = 1.5.dp)
          .width(3.dp)
          .height(baseHeight.dp)
          .clip(RoundedCornerShape(1.5.dp))
          .background(barColor)
      )
    }
  }
}
