package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.VolumeMute
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.FlashlightOff
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.GarvisGold
import com.example.ui.theme.GarvisRed
import com.example.ui.theme.GarvisRedBright
import com.example.ui.theme.GarvisSurfaceBorder
import com.example.ui.theme.GarvisSurfaceVariant
import com.example.ui.theme.GarvisTextPrimary
import com.example.ui.theme.GarvisTextSecondary

@Composable
fun QuickActionPanel(
  isFlashlightOn: Boolean,
  onToggleFlashlight: () -> Unit,
  onOpenWorldRadar: () -> Unit,
  onSolveExercise: () -> Unit,
  onRunDiagnostics: () -> Unit,
  isMuted: Boolean,
  onToggleMute: () -> Unit,
  modifier: Modifier = Modifier
) {
  Column(
    modifier = modifier
      .fillMaxWidth()
      .background(Color(0xFF14070A).copy(alpha = 0.9f))
      .border(1.dp, GarvisSurfaceBorder, RoundedCornerShape(12.dp))
      .padding(8.dp)
  ) {
    Text(
      text = "لوحة أوامر الصديق الذكي // STARK DZ COMMAND",
      color = GarvisRedBright,
      fontSize = 9.sp,
      fontWeight = FontWeight.Bold,
      fontFamily = FontFamily.Monospace,
      letterSpacing = 0.8.sp,
      modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
    )

    Spacer(modifier = Modifier.height(4.dp))

    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      // 1. Torch / Flashlight
      QuickActionButton(
        icon = if (isFlashlightOn) Icons.Default.FlashlightOn else Icons.Default.FlashlightOff,
        label = if (isFlashlightOn) "المصباح: شاعل" else "المصباح",
        isActive = isFlashlightOn,
        activeColor = GarvisGold,
        testTag = "torch_toggle_button",
        onClick = onToggleFlashlight,
        modifier = Modifier.weight(1f)
      )

      // 2. World Pulse / What's happening in the world
      QuickActionButton(
        icon = Icons.Default.Public,
        label = "رادار العالم",
        isActive = false,
        activeColor = GarvisRedBright,
        testTag = "world_radar_button",
        onClick = onOpenWorldRadar,
        modifier = Modifier.weight(1f)
      )

      // 3. Homework Solver
      QuickActionButton(
        icon = Icons.AutoMirrored.Filled.MenuBook,
        label = "حل التمارين",
        isActive = false,
        activeColor = GarvisGold,
        testTag = "solve_exercise_button",
        onClick = onSolveExercise,
        modifier = Modifier.weight(1f)
      )

      // 4. System Diagnostics
      QuickActionButton(
        icon = Icons.Default.Shield,
        label = "فحص شامل",
        isActive = false,
        activeColor = GarvisRed,
        testTag = "diagnostics_button",
        onClick = onRunDiagnostics,
        modifier = Modifier.weight(1f)
      )

      // 5. Audio Mute
      QuickActionButton(
        icon = if (isMuted) Icons.AutoMirrored.Filled.VolumeMute else Icons.AutoMirrored.Filled.VolumeUp,
        label = if (isMuted) "صامت" else "صوت",
        isActive = !isMuted,
        activeColor = GarvisRedBright,
        testTag = "mute_toggle_button",
        onClick = onToggleMute,
        modifier = Modifier.weight(1f)
      )
    }
  }
}

@Composable
private fun QuickActionButton(
  icon: ImageVector,
  label: String,
  isActive: Boolean,
  activeColor: Color,
  testTag: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  val borderColor = if (isActive) activeColor else GarvisSurfaceBorder
  val tintColor = if (isActive) activeColor else GarvisTextSecondary
  val bgColor = if (isActive) activeColor.copy(alpha = 0.2f) else GarvisSurfaceVariant

  Column(
    modifier = modifier
      .padding(horizontal = 2.5.dp)
      .clip(RoundedCornerShape(8.dp))
      .background(bgColor)
      .border(1.dp, borderColor, RoundedCornerShape(8.dp))
      .clickable { onClick() }
      .padding(vertical = 8.dp, horizontal = 2.dp)
      .testTag(testTag),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center
  ) {
    Icon(
      imageVector = icon,
      contentDescription = label,
      tint = tintColor,
      modifier = Modifier.size(18.dp)
    )
    Spacer(modifier = Modifier.height(4.dp))
    Text(
      text = label,
      color = if (isActive) activeColor else GarvisTextPrimary,
      fontSize = 8.sp,
      fontWeight = FontWeight.Bold,
      fontFamily = FontFamily.Monospace,
      maxLines = 1
    )
  }
}
