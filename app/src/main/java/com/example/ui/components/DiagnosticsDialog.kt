package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.GarvisAmber
import com.example.ui.theme.GarvisCyan
import com.example.ui.theme.GarvisGreen
import com.example.ui.theme.GarvisSurfaceBorder
import com.example.ui.theme.GarvisTextMuted
import com.example.ui.theme.GarvisTextPrimary
import com.example.ui.theme.GarvisTextSecondary
import kotlinx.coroutines.delay

data class DiagnosticStep(
  val title: String,
  val detail: String,
  var isDone: Boolean = false
)

@Composable
fun DiagnosticsDialog(
  onDismiss: () -> Unit
) {
  val steps = remember {
    mutableStateListOf(
      DiagnosticStep("ARC REACTOR CORE", "Flux: 100% // Palladium containment intact"),
      DiagnosticStep("NEURAL INTELLIGENCE MATRIX", "Gemini 3.5 Flash pipeline ready // 4ms latency"),
      DiagnosticStep("PHOTONIC ILLUMINATION EMITTER", "Camera torch hardware synchronized"),
      DiagnosticStep("ENERGY TELEMETRY BUS", "Battery cell voltage nominal"),
      DiagnosticStep("AUDIO & SPEECH TRANSLATOR", "Dual-stream English / Arabic engines calibrated")
    )
  }

  var progress by remember { mutableFloatStateOf(0f) }
  val animatedProgress by animateFloatAsState(
    targetValue = progress,
    animationSpec = tween(durationMillis = 300, easing = LinearEasing),
    label = "progressAnim"
  )

  LaunchedEffect(Unit) {
    for (i in steps.indices) {
      delay(400)
      steps[i] = steps[i].copy(isDone = true)
      progress = (i + 1).toFloat() / steps.size
    }
  }

  Dialog(onDismissRequest = onDismiss) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(16.dp))
        .background(Color(0xFF090E1A))
        .border(1.5.dp, GarvisCyan, RoundedCornerShape(16.dp))
        .padding(18.dp)
        .testTag("diagnostics_dialog")
    ) {
      // Title Bar
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = "STARK SYSTEM AUDIT",
            color = GarvisCyan,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 1.sp
          )
          Text(
            text = "PROTOCOL MARK-VII DIAGNOSTICS",
            color = GarvisTextMuted,
            fontSize = 9.sp,
            fontFamily = FontFamily.Monospace
          )
        }
        IconButton(
          onClick = onDismiss,
          modifier = Modifier.size(28.dp)
        ) {
          Icon(
            imageVector = Icons.Default.Close,
            contentDescription = "Close",
            tint = GarvisTextSecondary
          )
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Progress bar
      LinearProgressIndicator(
        progress = { animatedProgress },
        modifier = Modifier
          .fillMaxWidth()
          .height(6.dp)
          .clip(RoundedCornerShape(3.dp)),
        color = if (progress >= 1f) GarvisGreen else GarvisCyan,
        trackColor = Color(0xFF142033),
      )

      Spacer(modifier = Modifier.height(14.dp))

      // Diagnostic Items
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        steps.forEach { step ->
          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = if (step.isDone) Icons.Default.CheckCircle else Icons.Default.Warning,
              contentDescription = null,
              tint = if (step.isDone) GarvisGreen else GarvisAmber,
              modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
              Text(
                text = step.title,
                color = if (step.isDone) GarvisTextPrimary else GarvisTextSecondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
              )
              Text(
                text = step.detail,
                color = GarvisTextMuted,
                fontSize = 9.sp,
                fontFamily = FontFamily.Monospace
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(18.dp))

      // Bottom Status & Close Button
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = if (progress >= 1f) "ALL SYSTEMS NOMINAL" else "SCANNING SUBSYSTEMS...",
          color = if (progress >= 1f) GarvisGreen else GarvisCyan,
          fontSize = 10.sp,
          fontWeight = FontWeight.Bold,
          fontFamily = FontFamily.Monospace
        )

        Button(
          onClick = onDismiss,
          colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF11223A),
            contentColor = GarvisCyan
          ),
          shape = RoundedCornerShape(8.dp),
          modifier = Modifier.height(36.dp)
        ) {
          Text(
            text = "CONFIRM",
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold
          )
        }
      }
    }
  }
}
