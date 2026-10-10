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
import androidx.compose.material.icons.automirrored.filled.VolumeMute
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.FlashlightOff
import androidx.compose.material.icons.filled.FlashlightOn
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
import com.example.ui.theme.GarvisAmber
import com.example.ui.theme.GarvisCyan
import com.example.ui.theme.GarvisSurfaceBorder
import com.example.ui.theme.GarvisSurfaceVariant
import com.example.ui.theme.GarvisTextPrimary
import com.example.ui.theme.GarvisTextSecondary

@Composable
fun QuickActionPanel(
  isFlashlightOn: Boolean,
  onToggleFlashlight: () -> Unit,
  onRunDiagnostics: () -> Unit,
  onOpenAlarm: () -> Unit,
  onOpenCalendar: () -> Unit,
  isMuted: Boolean,
  onToggleMute: () -> Unit,
  modifier: Modifier = Modifier
) {
  Column(
    modifier = modifier
      .fillMaxWidth()
      .background(Color(0xFF0C1424).copy(alpha = 0.85f))
      .border(1.dp, GarvisSurfaceBorder, RoundedCornerShape(12.dp))
      .padding(8.dp)
  ) {
    Text(
      text = "DIRECTIVE COMMAND CENTER",
      color = GarvisCyan.copy(alpha = 0.8f),
      fontSize = 9.sp,
      fontWeight = FontWeight.Bold,
      fontFamily = FontFamily.Monospace,
      letterSpacing = 1.sp,
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
        label = if (isFlashlightOn) "TORCH: ON" else "TORCH: OFF",
        isActive = isFlashlightOn,
        activeColor = GarvisAmber,
        testTag = "torch_toggle_button",
        onClick = onToggleFlashlight,
        modifier = Modifier.weight(1f)
      )

      // 2. Diagnostics Sweep
      QuickActionButton(
        icon = Icons.Default.Shield,
        label = "DIAGNOSTICS",
        isActive = false,
        activeColor = GarvisCyan,
        testTag = "diagnostics_button",
        onClick = onRunDiagnostics,
        modifier = Modifier.weight(1f)
      )

      // 3. Alarm / Timer
      QuickActionButton(
        icon = Icons.Default.Alarm,
        label = "ALARM",
        isActive = false,
        activeColor = GarvisCyan,
        testTag = "alarm_button",
        onClick = onOpenAlarm,
        modifier = Modifier.weight(1f)
      )

      // 4. Calendar
      QuickActionButton(
        icon = Icons.Default.CalendarMonth,
        label = "CALENDAR",
        isActive = false,
        activeColor = GarvisCyan,
        testTag = "calendar_button",
        onClick = onOpenCalendar,
        modifier = Modifier.weight(1f)
      )

      // 5. Audio Mute
      QuickActionButton(
        icon = if (isMuted) Icons.AutoMirrored.Filled.VolumeMute else Icons.AutoMirrored.Filled.VolumeUp,
        label = if (isMuted) "MUTED" else "VOICE",
        isActive = !isMuted,
        activeColor = GarvisCyan,
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
  val bgColor = if (isActive) activeColor.copy(alpha = 0.15f) else GarvisSurfaceVariant

  Column(
    modifier = modifier
      .padding(horizontal = 3.dp)
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
