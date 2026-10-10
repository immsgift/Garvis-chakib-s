package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
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
import com.example.device.MemoryInfoData
import com.example.ui.theme.GarvisGold
import com.example.ui.theme.GarvisGreen
import com.example.ui.theme.GarvisRed
import com.example.ui.theme.GarvisRedBright
import com.example.ui.theme.GarvisSurfaceBorder
import com.example.ui.theme.GarvisSurfaceVariant
import com.example.ui.theme.GarvisTextMuted
import com.example.ui.theme.GarvisTextPrimary
import com.example.ui.theme.GarvisTextSecondary
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HudTelemetryBar(
  status: GarvisStatus,
  batteryLevel: Int,
  isCharging: Boolean,
  memoryInfo: MemoryInfoData,
  isArabic: Boolean,
  onToggleLanguage: () -> Unit,
  modifier: Modifier = Modifier
) {
  var currentTime by remember { mutableStateOf("") }

  LaunchedEffect(Unit) {
    val formatter = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    while (true) {
      currentTime = formatter.format(Date())
      delay(1000)
    }
  }

  val infiniteTransition = rememberInfiniteTransition(label = "LedBlink")
  val ledAlpha by infiniteTransition.animateFloat(
    initialValue = 0.35f,
    targetValue = 1f,
    animationSpec = infiniteRepeatable(
      animation = tween(
        durationMillis = when (status) {
          GarvisStatus.PROCESSING -> 300
          GarvisStatus.LISTENING -> 450
          GarvisStatus.SPEAKING -> 600
          else -> 1200
        },
        easing = FastOutSlowInEasing
      ),
      repeatMode = RepeatMode.Reverse
    ),
    label = "ledAlpha"
  )

  Column(
    modifier = modifier
      .fillMaxWidth()
      .background(Color(0xFF14070A).copy(alpha = 0.95f))
      .border(1.dp, GarvisSurfaceBorder, RoundedCornerShape(12.dp))
      .padding(horizontal = 14.dp, vertical = 10.dp)
  ) {
    // Upper Row: Clock, Status Indicator, Language Toggle
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      // Left: Real-time clock & Algerian Garvis tag
      Row(verticalAlignment = Alignment.CenterVertically) {
        Column {
          Text(
            text = "GARVIS DZ // $currentTime",
            color = GarvisRedBright,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 1.sp
          )
          Text(
            text = "صاحبك الذكي // أنظمة المفاعل 100%",
            color = GarvisTextMuted,
            fontSize = 9.sp,
            fontFamily = FontFamily.Monospace
          )
        }
      }

      // Center: Status Badge with blinking red LED
      val (statusColor, statusLabel) = when (status) {
        GarvisStatus.ONLINE -> Pair(GarvisGreen, if (isArabic) "متصل" else "ONLINE")
        GarvisStatus.STANDBY -> Pair(GarvisGold, if (isArabic) "جاهز" else "STANDBY")
        GarvisStatus.LISTENING -> Pair(GarvisRedBright, if (isArabic) "يسمع" else "LISTENING")
        GarvisStatus.PROCESSING -> Pair(GarvisGold, if (isArabic) "يحلل" else "PROCESSING")
        GarvisStatus.SPEAKING -> Pair(GarvisRed, if (isArabic) "يهدر" else "SPEAKING")
      }

      Row(
        modifier = Modifier
          .clip(RoundedCornerShape(6.dp))
          .background(GarvisSurfaceVariant)
          .border(1.dp, statusColor.copy(alpha = 0.7f), RoundedCornerShape(6.dp))
          .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Box(
          modifier = Modifier
            .size(7.dp)
            .clip(CircleShape)
            .background(statusColor.copy(alpha = ledAlpha))
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
          text = statusLabel,
          color = statusColor,
          fontSize = 10.sp,
          fontWeight = FontWeight.ExtraBold,
          fontFamily = FontFamily.Monospace,
          letterSpacing = 1.2.sp
        )
      }

      // Right: Language Switcher (DZ الدارجة / EN)
      Row(
        modifier = Modifier
          .testTag("language_toggle_button")
          .clip(RoundedCornerShape(6.dp))
          .background(Color(0xFF260D12))
          .border(1.dp, GarvisRed.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
          .clickable { onToggleLanguage() }
          .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Icon(
          imageVector = Icons.Default.Translate,
          contentDescription = "Language Switch",
          tint = GarvisGold,
          modifier = Modifier.size(13.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
          text = if (isArabic) "دارجة DZ" else "EN",
          color = GarvisTextPrimary,
          fontSize = 11.sp,
          fontWeight = FontWeight.Bold,
          fontFamily = FontFamily.Monospace
        )
      }
    }

    Spacer(modifier = Modifier.height(8.dp))

    // Lower Row: Hardware Telemetry Bars (Battery & Memory)
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      // Memory / RAM gauge
      Row(
        modifier = Modifier.weight(1f),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "الرام RAM",
          color = GarvisTextSecondary,
          fontSize = 9.sp,
          fontFamily = FontFamily.Monospace,
          fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.width(6.dp))
        Box(
          modifier = Modifier
            .weight(1f)
            .height(5.dp)
            .clip(RoundedCornerShape(3.dp))
            .background(Color(0xFF241016))
        ) {
          Box(
            modifier = Modifier
              .fillMaxWidth(fraction = (memoryInfo.percentage / 100f).coerceIn(0.1f, 1f))
              .height(5.dp)
              .clip(RoundedCornerShape(3.dp))
              .background(if (memoryInfo.percentage > 85) GarvisGold else GarvisRed)
          )
        }
        Spacer(modifier = Modifier.width(6.dp))
        Text(
          text = "${memoryInfo.usedMb}MB",
          color = GarvisTextMuted,
          fontSize = 9.sp,
          fontFamily = FontFamily.Monospace
        )
      }

      Spacer(modifier = Modifier.width(16.dp))

      // Battery level telemetry
      Row(verticalAlignment = Alignment.CenterVertically) {
        val batteryIcon = when {
          isCharging -> Icons.Default.BatteryChargingFull
          batteryLevel < 20 -> Icons.Default.BatteryAlert
          else -> Icons.Default.BatteryFull
        }
        val batteryColor = when {
          isCharging -> GarvisGreen
          batteryLevel < 20 -> GarvisGold
          else -> GarvisRedBright
        }

        Icon(
          imageVector = batteryIcon,
          contentDescription = "Battery Status",
          tint = batteryColor,
          modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
          text = "البطارية $batteryLevel%",
          color = batteryColor,
          fontSize = 10.sp,
          fontFamily = FontFamily.Monospace,
          fontWeight = FontWeight.Bold
        )
      }
    }
  }
}
