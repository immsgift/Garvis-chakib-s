package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.ArcReactorView
import com.example.ui.components.AudioWaveformBar
import com.example.ui.components.DiagnosticsDialog
import com.example.ui.components.GarvisStatus
import com.example.ui.components.HudTelemetryBar
import com.example.ui.components.QuickActionPanel
import com.example.ui.components.TranscriptTerminal
import com.example.ui.components.WorldPulseDialog
import com.example.ui.theme.GarvisDarkBackground
import com.example.ui.theme.GarvisGold
import com.example.ui.theme.GarvisGreen
import com.example.ui.theme.GarvisRed
import com.example.ui.theme.GarvisRedBright
import com.example.ui.theme.GarvisRedDark
import com.example.ui.theme.GarvisSurfaceBorder
import com.example.ui.theme.GarvisSurfaceVariant
import com.example.ui.theme.GarvisTextMuted
import com.example.ui.theme.GarvisTextPrimary
import com.example.ui.theme.GarvisTheme
import com.example.viewmodel.GarvisViewModel

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      GarvisTheme {
        GarvisAppScreen()
      }
    }
  }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun GarvisAppScreen(
  vm: GarvisViewModel = viewModel()
) {
  val context = LocalContext.current
  val keyboardController = LocalSoftwareKeyboardController.current

  val status by vm.status.collectAsState()
  val isArabic by vm.isArabic.collectAsState()
  val messages by vm.messages.collectAsState()
  val memoryInfo by vm.memoryInfo.collectAsState()
  val batteryLevel by vm.deviceManager.batteryLevel.collectAsState()
  val isCharging by vm.deviceManager.isCharging.collectAsState()
  val isFlashlightOn by vm.deviceManager.isFlashlightOn.collectAsState()
  val isMuted by vm.ttsManager.isMuted.collectAsState()
  val rmsAudioLevel by vm.speechManager.rmsDb.collectAsState()
  val isListening by vm.speechManager.isListening.collectAsState()
  val partialSpeech by vm.speechManager.partialText.collectAsState()
  val showDiagnostics by vm.showDiagnosticsDialog.collectAsState()
  val showWorldRadar by vm.showWorldRadarDialog.collectAsState()

  var inputText by remember { mutableStateOf("") }

  val audioPermissionLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestPermission()
  ) { isGranted ->
    if (isGranted) {
      vm.toggleListening()
    } else {
      Toast.makeText(
        context,
        "اعطينا إذن الميكروفون باش نقدر نسمعك يا خويا",
        Toast.LENGTH_LONG
      ).show()
    }
  }

  val onMicClick: () -> Unit = {
    val hasAudio = ContextCompat.checkSelfPermission(
      context,
      Manifest.permission.RECORD_AUDIO
    ) == PackageManager.PERMISSION_GRANTED
    if (hasAudio) {
      vm.toggleListening()
    } else {
      audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
    }
  }

  // Diagnostics Dialog
  if (showDiagnostics) {
    DiagnosticsDialog(onDismiss = { vm.dismissDiagnostics() })
  }

  // World Events Radar Dialog
  if (showWorldRadar) {
    WorldPulseDialog(
      onDismiss = { vm.dismissWorldRadar() },
      onSpeakEvent = { eventText ->
        vm.reSpeakMessage(eventText)
      }
    )
  }

  Scaffold(
    modifier = Modifier.fillMaxSize(),
    containerColor = GarvisDarkBackground,
    contentWindowInsets = WindowInsets(0, 0, 0, 0)
  ) { paddingValues ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .background(
          brush = Brush.verticalGradient(
            colors = listOf(
              Color(0xFF17080B),
              Color(0xFF0F0608),
              Color(0xFF0A0405)
            )
          )
        )
        .padding(paddingValues)
        .statusBarsPadding()
        .navigationBarsPadding(),
      contentAlignment = Alignment.TopCenter
    ) {
      Column(
        modifier = Modifier
          .fillMaxSize()
          .widthIn(max = 680.dp)
          .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        // 1. Top HUD Telemetry Bar
        HudTelemetryBar(
          status = status,
          batteryLevel = batteryLevel,
          isCharging = isCharging,
          memoryInfo = memoryInfo,
          isArabic = isArabic,
          onToggleLanguage = { vm.toggleLanguage() }
        )

        Spacer(modifier = Modifier.height(6.dp))

        // 2. Central Red Arc Reactor
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .weight(1f),
          contentAlignment = Alignment.Center
        ) {
          Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
          ) {
            ArcReactorView(
              status = status,
              rmsAudioLevel = rmsAudioLevel,
              onReactorClick = onMicClick
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Reactor Status Subtext in Algerian Darija
            val statusHint = when (status) {
              GarvisStatus.LISTENING -> "● راني نسمع فيك مليح... اهدر يا خويا!"
              GarvisStatus.PROCESSING -> "⟳ راني نخمم ونحلل فالأمر تاعك..."
              GarvisStatus.SPEAKING -> "▲ راني نجاوب فيك بالصوت يا صاحبي..."
              else -> "انقر على مفاعل القوس الأحمر باش تهدر معايا"
            }
            Text(
              text = statusHint,
              color = when (status) {
                GarvisStatus.LISTENING -> GarvisGreen
                GarvisStatus.PROCESSING -> GarvisGold
                else -> GarvisRedBright
              },
              fontSize = 10.sp,
              fontWeight = FontWeight.Bold,
              fontFamily = FontFamily.Monospace,
              letterSpacing = 0.8.sp
            )

            Spacer(modifier = Modifier.height(2.dp))

            // Audio Waveform Visualizer
            AudioWaveformBar(
              status = status,
              rmsAudioLevel = rmsAudioLevel
            )

            // Quick Directive Suggestion Chips in Algerian Darija
            val quickPrompts = listOf(
              "واش صاري فالعالم؟ 🌍",
              "حل هاد التمرين 📚",
              "شعل الفلاش 🔦",
              "دير فحص شامل 🛡️",
              "شكون انت يا جارفيز؟ 🤖"
            )

            FlowRow(
              modifier = Modifier.padding(top = 2.dp),
              horizontalArrangement = Arrangement.Center
            ) {
              quickPrompts.forEach { prompt ->
                Box(
                  modifier = Modifier
                    .padding(horizontal = 4.dp, vertical = 2.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFF260D12))
                    .border(1.dp, GarvisRed.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                    .clickable { vm.processUserPrompt(prompt) }
                    .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                  Text(
                    text = prompt,
                    color = GarvisGold,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                  )
                }
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // 3. Quick Action Command Center (Torch, World Radar, Homework, Diagnostics, Mute)
        QuickActionPanel(
          isFlashlightOn = isFlashlightOn,
          onToggleFlashlight = { vm.toggleTorch() },
          onOpenWorldRadar = { vm.openWorldRadar() },
          onSolveExercise = { vm.triggerSolveExerciseMode() },
          onRunDiagnostics = { vm.runDiagnostics() },
          isMuted = isMuted,
          onToggleMute = { vm.toggleMute() }
        )

        Spacer(modifier = Modifier.height(6.dp))

        // 4. Scrollable Transcript Terminal
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .height(175.dp)
        ) {
          TranscriptTerminal(
            messages = messages,
            partialSpeech = partialSpeech,
            isListening = isListening,
            onReSpeak = { vm.reSpeakMessage(it) },
            onClearHistory = { vm.clearTranscript() }
          )
        }

        Spacer(modifier = Modifier.height(6.dp))

        // 5. Directive Input Bar (Text + Voice Mic Button)
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 2.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          OutlinedTextField(
            value = inputText,
            onValueChange = { inputText = it },
            placeholder = {
              Text(
                text = "اهدر ولا اكتبلي واش تحب يا خويا…",
                color = GarvisTextMuted,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace
              )
            },
            modifier = Modifier
              .weight(1f)
              .testTag("directive_input_field"),
            shape = RoundedCornerShape(10.dp),
            colors = OutlinedTextFieldDefaults.colors(
              focusedContainerColor = Color(0xFF1A0A0E),
              unfocusedContainerColor = Color(0xFF14070A),
              focusedBorderColor = GarvisRed,
              unfocusedBorderColor = GarvisSurfaceBorder,
              focusedTextColor = GarvisTextPrimary,
              unfocusedTextColor = GarvisTextPrimary,
              cursorColor = GarvisRed
            ),
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
            keyboardActions = KeyboardActions(
              onSend = {
                if (inputText.isNotBlank()) {
                  val text = inputText
                  inputText = ""
                  keyboardController?.hide()
                  vm.processUserPrompt(text)
                }
              }
            )
          )

          Spacer(modifier = Modifier.width(6.dp))

          // Send Button
          if (inputText.isNotBlank()) {
            IconButton(
              onClick = {
                val text = inputText
                inputText = ""
                keyboardController?.hide()
                vm.processUserPrompt(text)
              },
              modifier = Modifier
                .size(46.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(GarvisRedDark)
                .border(1.dp, GarvisRed, RoundedCornerShape(10.dp))
                .testTag("send_directive_button")
            ) {
              Icon(
                imageVector = Icons.AutoMirrored.Filled.Send,
                contentDescription = "Send",
                tint = GarvisGold,
                modifier = Modifier.size(20.dp)
              )
            }
          } else {
            // Glowing Red Mic Button
            val infiniteTransition = rememberInfiniteTransition(label = "MicPulse")
            val micGlow by infiniteTransition.animateFloat(
              initialValue = 0.5f,
              targetValue = 1f,
              animationSpec = infiniteRepeatable(
                animation = tween(600, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
              ),
              label = "micGlow"
            )

            IconButton(
              onClick = onMicClick,
              modifier = Modifier
                .size(46.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(if (isListening) GarvisRedDark else GarvisSurfaceVariant)
                .border(
                  width = if (isListening) 2.dp else 1.dp,
                  color = if (isListening) GarvisRed.copy(alpha = micGlow) else GarvisRed.copy(alpha = 0.6f),
                  shape = RoundedCornerShape(10.dp)
                )
                .testTag("mic_button")
            ) {
              Icon(
                imageVector = if (isListening) Icons.Default.MicOff else Icons.Default.Mic,
                contentDescription = if (isListening) "Stop Listening" else "Start Listening",
                tint = if (isListening) GarvisGold else GarvisRedBright,
                modifier = Modifier.size(22.dp)
              )
            }
          }
        }
      }
    }
  }
}
