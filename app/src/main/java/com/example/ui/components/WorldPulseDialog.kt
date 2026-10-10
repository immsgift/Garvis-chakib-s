package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.GarvisGold
import com.example.ui.theme.GarvisRed
import com.example.ui.theme.GarvisRedBright
import com.example.ui.theme.GarvisRedSoft
import com.example.ui.theme.GarvisSurfaceBorder
import com.example.ui.theme.GarvisSurfaceVariant
import com.example.ui.theme.GarvisTextMuted
import com.example.ui.theme.GarvisTextPrimary
import com.example.ui.theme.GarvisTextSecondary

data class WorldEventItem(
  val id: String,
  val category: String,
  val icon: ImageVector,
  val title: String,
  val summary: String,
  val timeTag: String
)

@Composable
fun WorldPulseDialog(
  onDismiss: () -> Unit,
  onSpeakEvent: (String) -> Unit
) {
  val events = remember {
    listOf(
      WorldEventItem(
        id = "1",
        category = "الذكاء الاصطناعي والتكنولوجيا",
        icon = Icons.Default.Public,
        title = "ثورة النماذج العصبية الخارقة في العالم",
        summary = "قفزات عملاقة في نماذج الاستدلال والتفكير الرياضي المتقدم، وروبوتات بشرية تبدأ العمل في المصانع الذكية.",
        timeTag = "مباشر // 2026"
      ),
      WorldEventItem(
        id = "2",
        category = "استكشاف الفضاء والعلوم",
        icon = Icons.Default.RocketLaunch,
        title = "محطة قمرية جديدة والتحضير لرحلات المريخ",
        summary = "وكالات الفضاء العالمية تنجز اختبارات محركات الدفع الأيوني للوصول إلى المريخ في وقت قياسي.",
        timeTag = "مدار الأرض"
      ),
      WorldEventItem(
        id = "3",
        category = "الطاقة والابتكار الأخضر",
        icon = Icons.Default.Science,
        title = "اندماج نووي نظيف ومفاعلات طاقة متجددة",
        summary = "تحقيق رقم قياسي جديد في إنتاج الطاقة النظيفة الخالية من الانبعاثات عبر مفاعلات التوكاماك المغناطيسية.",
        timeTag = "طاقة المستقبل"
      ),
      WorldEventItem(
        id = "4",
        category = "الرياضة والشباب",
        icon = Icons.Default.SportsSoccer,
        title = "تحضيرات كبرى لبطولات كرة القدم العالمية",
        summary = "تألق كبير للمواهب الرياضية الشابة في الدوريات الكبرى وتطوير تقنيات التسلل الآلي بالذكاء الاصطناعي.",
        timeTag = "عالم الرياضة"
      )
    )
  }

  val infiniteTransition = rememberInfiniteTransition(label = "RadarSweep")
  val sweepAngle by infiniteTransition.animateFloat(
    initialValue = 0f,
    targetValue = 360f,
    animationSpec = infiniteRepeatable(
      animation = tween(3000, easing = LinearEasing),
      repeatMode = RepeatMode.Restart
    ),
    label = "sweepAngle"
  )

  Dialog(onDismissRequest = onDismiss) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(16.dp))
        .background(Color(0xFF0F070A))
        .border(1.5.dp, GarvisRed, RoundedCornerShape(16.dp))
        .padding(16.dp)
        .testTag("world_pulse_dialog")
    ) {
      // Header with Radar Scanner
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          // Mini Animated Holographic Radar
          Box(
            modifier = Modifier
              .size(38.dp)
              .clip(CircleShape)
              .background(Color(0xFF200B10))
              .border(1.dp, GarvisRed, CircleShape),
            contentAlignment = Alignment.Center
          ) {
            Canvas(modifier = Modifier.size(34.dp)) {
              val center = Offset(size.width / 2f, size.height / 2f)
              val radius = size.minDimension / 2f
              drawCircle(
                color = GarvisRed.copy(alpha = 0.3f),
                radius = radius * 0.5f,
                style = Stroke(width = 1f)
              )
              rotate(sweepAngle, pivot = center) {
                drawLine(
                  brush = Brush.horizontalGradient(listOf(GarvisRed, Color.Transparent)),
                  start = center,
                  end = Offset(center.x + radius, center.y),
                  strokeWidth = 2.dp.toPx()
                )
              }
            }
          }

          Spacer(modifier = Modifier.width(10.dp))

          Column {
            Text(
              text = "رادار العالم // واش صاري فالدنيا",
              color = GarvisRedBright,
              fontSize = 13.sp,
              fontWeight = FontWeight.Bold,
              fontFamily = FontFamily.Monospace
            )
            Text(
              text = "بث مباشر للأحداث العالمية والتكنولوجية",
              color = GarvisTextMuted,
              fontSize = 9.sp,
              fontFamily = FontFamily.Monospace
            )
          }
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

      Spacer(modifier = Modifier.height(12.dp))

      // Events Feed
      LazyColumn(
        modifier = Modifier
          .fillMaxWidth()
          .height(300.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        items(events, key = { it.id }) { item ->
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(10.dp))
              .background(GarvisSurfaceVariant)
              .border(1.dp, GarvisSurfaceBorder, RoundedCornerShape(10.dp))
              .padding(10.dp)
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                  imageVector = item.icon,
                  contentDescription = null,
                  tint = GarvisGold,
                  modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = item.category,
                  color = GarvisGold,
                  fontSize = 10.sp,
                  fontWeight = FontWeight.Bold,
                  fontFamily = FontFamily.Monospace
                )
              }

              Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                  text = item.timeTag,
                  color = GarvisRedSoft,
                  fontSize = 8.sp,
                  fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                  imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                  contentDescription = "قراءة صوتية",
                  tint = GarvisRedBright,
                  modifier = Modifier
                    .size(16.dp)
                    .clickable { onSpeakEvent("${item.title}. ${item.summary}") }
                )
              }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
              text = item.title,
              color = GarvisTextPrimary,
              fontSize = 12.sp,
              fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
              text = item.summary,
              color = GarvisTextSecondary,
              fontSize = 11.sp,
              lineHeight = 16.sp
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      Button(
        onClick = onDismiss,
        colors = ButtonDefaults.buttonColors(
          containerColor = GarvisRed,
          contentColor = Color.White
        ),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
          .fillMaxWidth()
          .height(40.dp)
      ) {
        Text(
          text = "إغلاق الرادار",
          fontSize = 12.sp,
          fontWeight = FontWeight.Bold,
          fontFamily = FontFamily.Monospace
        )
      }
    }
  }
}
