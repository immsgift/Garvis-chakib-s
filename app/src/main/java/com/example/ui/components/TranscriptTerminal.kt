package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.GarvisCyan
import com.example.ui.theme.GarvisCyanSoft
import com.example.ui.theme.GarvisGreen
import com.example.ui.theme.GarvisSurfaceBorder
import com.example.ui.theme.GarvisSurfaceVariant
import com.example.ui.theme.GarvisTextMuted
import com.example.ui.theme.GarvisTextPrimary
import com.example.ui.theme.GarvisTextSecondary
import kotlinx.coroutines.delay

data class TranscriptMessage(
  val id: String = java.util.UUID.randomUUID().toString(),
  val sender: String, // "USER" or "GARVIS"
  val text: String,
  val timestamp: String,
  val isLiveTyping: Boolean = false
)

@Composable
fun TranscriptTerminal(
  messages: List<TranscriptMessage>,
  partialSpeech: String,
  isListening: Boolean,
  onReSpeak: (String) -> Unit,
  onClearHistory: () -> Unit,
  modifier: Modifier = Modifier
) {
  val listState = rememberLazyListState()
  val context = LocalContext.current

  // Auto-scroll to bottom when new messages arrive
  LaunchedEffect(messages.size, partialSpeech) {
    if (messages.isNotEmpty()) {
      listState.animateScrollToItem(messages.size - 1)
    }
  }

  Column(
    modifier = modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(12.dp))
      .background(Color(0xFF070B14).copy(alpha = 0.95f))
      .border(1.dp, GarvisSurfaceBorder, RoundedCornerShape(12.dp))
  ) {
    // Terminal Top Header Bar
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .background(Color(0xFF0D1628))
        .padding(horizontal = 12.dp, vertical = 8.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
          modifier = Modifier
            .size(8.dp)
            .background(GarvisCyan, RoundedCornerShape(2.dp))
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
          text = "NEURAL LOG // TRANSCRIPT STREAM",
          color = GarvisCyan,
          fontSize = 10.sp,
          fontWeight = FontWeight.Bold,
          fontFamily = FontFamily.Monospace,
          letterSpacing = 1.sp
        )
      }

      Row(verticalAlignment = Alignment.CenterVertically) {
        if (messages.isNotEmpty()) {
          Icon(
            imageVector = Icons.Default.DeleteSweep,
            contentDescription = "Clear Log",
            tint = GarvisTextMuted,
            modifier = Modifier
              .size(16.dp)
              .testTag("clear_transcript_button")
              .clickable { onClearHistory() }
          )
        }
      }
    }

    // Scrollable message list
    LazyColumn(
      state = listState,
      modifier = Modifier
        .fillMaxSize()
        .padding(10.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      items(messages, key = { it.id }) { msg ->
        TerminalMessageCard(
          message = msg,
          onReSpeak = { onReSpeak(msg.text) },
          onCopy = {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            clipboard.setPrimaryClip(ClipData.newPlainText("Garvis Log", msg.text))
            Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
          }
        )
      }

      // Live voice recognition subtitle chip
      if (isListening && partialSpeech.isNotBlank()) {
        item {
          LivePartialCard(partialSpeech = partialSpeech)
        }
      }
    }
  }
}

@Composable
private fun TerminalMessageCard(
  message: TranscriptMessage,
  onReSpeak: () -> Unit,
  onCopy: () -> Unit
) {
  val isGarvis = message.sender == "GARVIS"
  val tagColor = if (isGarvis) GarvisCyan else GarvisGreen
  val label = if (isGarvis) "GARVIS // CORE" else "DIRECTIVE // USER"

  // Typewriter effect for live typing Garvis message
  var displayedText by remember(message.id) { mutableStateOf(if (message.isLiveTyping) "" else message.text) }
  var isTypingDone by remember(message.id) { mutableStateOf(!message.isLiveTyping) }

  LaunchedEffect(message.id, message.isLiveTyping) {
    if (message.isLiveTyping) {
      val full = message.text
      var currentLen = 0
      while (currentLen <= full.length) {
        displayedText = full.take(currentLen)
        currentLen += 2
        delay(18)
      }
      displayedText = full
      isTypingDone = true
    } else {
      displayedText = message.text
      isTypingDone = true
    }
  }

  Column(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(8.dp))
      .background(if (isGarvis) Color(0xFF0A1424) else Color(0xFF0F1B24))
      .border(
        1.dp,
        if (isGarvis) GarvisCyan.copy(alpha = 0.35f) else GarvisGreen.copy(alpha = 0.25f),
        RoundedCornerShape(8.dp)
      )
      .padding(10.dp)
  ) {
    // Header with tag & timestamp & action buttons
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
          text = label,
          color = tagColor,
          fontSize = 9.sp,
          fontWeight = FontWeight.Bold,
          fontFamily = FontFamily.Monospace,
          letterSpacing = 0.8.sp
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
          text = "[${message.timestamp}]",
          color = GarvisTextMuted,
          fontSize = 8.sp,
          fontFamily = FontFamily.Monospace
        )
      }

      Row(verticalAlignment = Alignment.CenterVertically) {
        if (isGarvis) {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.VolumeUp,
            contentDescription = "Speak Again",
            tint = GarvisCyan.copy(alpha = 0.8f),
            modifier = Modifier
              .size(15.dp)
              .clickable { onReSpeak() }
          )
          Spacer(modifier = Modifier.width(8.dp))
        }

        Icon(
          imageVector = Icons.Default.ContentCopy,
          contentDescription = "Copy text",
          tint = GarvisTextMuted,
          modifier = Modifier
            .size(13.dp)
            .clickable { onCopy() }
        )
      }
    }

    Spacer(modifier = Modifier.height(6.dp))

    // Message body text
    Text(
      text = displayedText + if (!isTypingDone) " █" else "",
      color = if (isGarvis) GarvisTextPrimary else GarvisCyanSoft,
      fontSize = 13.sp,
      lineHeight = 19.sp,
      fontFamily = FontFamily.Monospace
    )
  }
}

@Composable
private fun LivePartialCard(partialSpeech: String) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(8.dp))
      .background(Color(0xFF132238))
      .border(1.dp, GarvisCyan.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
      .padding(8.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Text(
      text = "LISTENING: ",
      color = GarvisCyan,
      fontSize = 10.sp,
      fontWeight = FontWeight.Bold,
      fontFamily = FontFamily.Monospace
    )
    Text(
      text = "\"$partialSpeech\"",
      color = Color.White,
      fontSize = 12.sp,
      fontFamily = FontFamily.Monospace
    )
  }
}
