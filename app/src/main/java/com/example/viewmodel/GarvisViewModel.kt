package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.GarvisAiService
import com.example.device.DeviceManager
import com.example.device.MemoryInfoData
import com.example.ui.components.GarvisStatus
import com.example.ui.components.TranscriptMessage
import com.example.voice.GarvisSpeechManager
import com.example.voice.GarvisTtsManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class GarvisViewModel(application: Application) : AndroidViewModel(application) {

  private val aiService = GarvisAiService()
  val ttsManager = GarvisTtsManager(application)
  val speechManager = GarvisSpeechManager(application)
  val deviceManager = DeviceManager(application)

  private val _status = MutableStateFlow(GarvisStatus.STANDBY)
  val status: StateFlow<GarvisStatus> = _status.asStateFlow()

  private val _isArabic = MutableStateFlow(false)
  val isArabic: StateFlow<Boolean> = _isArabic.asStateFlow()

  private val _messages = MutableStateFlow<List<TranscriptMessage>>(emptyList())
  val messages: StateFlow<List<TranscriptMessage>> = _messages.asStateFlow()

  private val _memoryInfo = MutableStateFlow(deviceManager.getMemoryTelemetry())
  val memoryInfo: StateFlow<MemoryInfoData> = _memoryInfo.asStateFlow()

  private val _showDiagnosticsDialog = MutableStateFlow(false)
  val showDiagnosticsDialog: StateFlow<Boolean> = _showDiagnosticsDialog.asStateFlow()

  private var currentJob: Job? = null

  init {
    // Initial welcome message
    val timeStr = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
    val initialMsg = TranscriptMessage(
      sender = "GARVIS",
      text = "Garvis AI systems initialized. Arc Reactor at 100% output. Standing by for your directives, Sir.",
      timestamp = timeStr,
      isLiveTyping = true
    )
    _messages.value = listOf(initialMsg)

    // Listen to TTS speaking changes
    viewModelScope.launch {
      ttsManager.isSpeaking.collect { speaking ->
        if (speaking) {
          _status.value = GarvisStatus.SPEAKING
        } else if (_status.value == GarvisStatus.SPEAKING) {
          _status.value = GarvisStatus.STANDBY
        }
      }
    }

    // Periodic telemetry update
    viewModelScope.launch {
      while (true) {
        delay(4000)
        _memoryInfo.value = deviceManager.getMemoryTelemetry()
        deviceManager.updateBatteryStatus()
      }
    }
  }

  fun toggleLanguage() {
    _isArabic.value = !_isArabic.value
    deviceManager.triggerHaptic()
    val langStr = if (_isArabic.value) "Arabic" else "English"
    val confirmation = if (_isArabic.value)
      "تم تحويل بروتوكول اللغة إلى العربية يا سيدي."
    else
      "Language matrix updated to English, Sir."
    addTranscriptMessage("GARVIS", confirmation, isLiveTyping = true)
    ttsManager.speak(confirmation, isArabic = _isArabic.value)
  }

  fun toggleListening() {
    deviceManager.triggerHaptic()
    if (speechManager.isListening.value) {
      speechManager.stopListening()
      _status.value = GarvisStatus.STANDBY
    } else {
      ttsManager.stop()
      _status.value = GarvisStatus.LISTENING
      speechManager.startListening(
        isArabic = _isArabic.value,
        onResult = { recognizedText ->
          processUserPrompt(recognizedText)
        },
        onError = { error ->
          _status.value = GarvisStatus.STANDBY
          addTranscriptMessage("GARVIS", "Audio sensor note: $error, Sir.")
        }
      )
    }
  }

  fun processUserPrompt(prompt: String) {
    if (prompt.isBlank()) return
    deviceManager.triggerHaptic()

    val timeStr = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
    addTranscriptMessage("USER", prompt, isLiveTyping = false)

    // Check for native hardware triggers first
    val lower = prompt.lowercase().trim()
    val isAr = prompt.any { it in '\u0600'..'\u06FF' }

    if (lower.contains("flashlight") || lower.contains("torch") || lower.contains("مصباح") || lower.contains("فلاش")) {
      val isNowOn = deviceManager.toggleFlashlight()
      val reply = if (isAr) {
        if (isNowOn) "تم تشغيل المصباح يا سيدي." else "تم إيقاف المصباح يا سيدي."
      } else {
        if (isNowOn) "Photonic torch engaged, Sir." else "Torch deactivated, Sir."
      }
      respondAsGarvis(reply, isAr)
      return
    }

    if (lower.contains("diagnostic") || lower.contains("فحص") || lower.contains("تشخيص")) {
      _showDiagnosticsDialog.value = true
      val reply = if (isAr)
        "جاري تشغيل فحص تشخيصي شامل لكافة الأنظمة والمفاعل يا سيدي."
      else
        "Initiating full diagnostic sweep of all subsystems and Arc Reactor core, Sir."
      respondAsGarvis(reply, isAr)
      return
    }

    if (lower.contains("alarm") || lower.contains("منبه")) {
      deviceManager.openAlarmOrTimer()
      val reply = if (isAr) "تم فتح تطبيق المنبه يا سيدي." else "Accessing system alarm subroutines, Sir."
      respondAsGarvis(reply, isAr)
      return
    }

    if (lower.contains("calendar") || lower.contains("تقويم") || lower.contains("جدول")) {
      deviceManager.openCalendar()
      val reply = if (isAr) "تم فتح التقويم يا سيدي." else "Displaying chronological calendar grid, Sir."
      respondAsGarvis(reply, isAr)
      return
    }

    // Otherwise, send to Gemini AI service
    currentJob?.cancel()
    currentJob = viewModelScope.launch {
      _status.value = GarvisStatus.PROCESSING
      val history = _messages.value.takeLast(6).map { it.sender to it.text }
      val response = aiService.askGarvis(prompt, history)
      respondAsGarvis(response.text, isAr)
    }
  }

  private fun respondAsGarvis(text: String, isArabicInput: Boolean) {
    addTranscriptMessage("GARVIS", text, isLiveTyping = true)
    ttsManager.speak(text, isArabic = isArabicInput || _isArabic.value)
  }

  fun reSpeakMessage(text: String) {
    deviceManager.triggerHaptic()
    ttsManager.stop()
    ttsManager.speak(text, isArabic = _isArabic.value)
  }

  fun toggleMute() {
    deviceManager.triggerHaptic()
    ttsManager.toggleMute()
  }

  fun toggleTorch() {
    val newState = deviceManager.toggleFlashlight()
    val msg = if (newState) "Torch activated, Sir." else "Torch deactivated, Sir."
    addTranscriptMessage("GARVIS", msg, isLiveTyping = false)
    ttsManager.speak(msg, isArabic = _isArabic.value)
  }

  fun runDiagnostics() {
    deviceManager.triggerHaptic()
    _showDiagnosticsDialog.value = true
    val msg = "Diagnostic sweep in progress. Subsystems nominal, Sir."
    addTranscriptMessage("GARVIS", msg, isLiveTyping = false)
    ttsManager.speak(msg, isArabic = _isArabic.value)
  }

  fun openAlarm() {
    deviceManager.openAlarmOrTimer()
  }

  fun openCalendar() {
    deviceManager.openCalendar()
  }

  fun dismissDiagnostics() {
    _showDiagnosticsDialog.value = false
  }

  fun clearTranscript() {
    deviceManager.triggerHaptic()
    _messages.value = emptyList()
  }

  private fun addTranscriptMessage(sender: String, text: String, isLiveTyping: Boolean = false) {
    val timeStr = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
    val newMsg = TranscriptMessage(
      sender = sender,
      text = text,
      timestamp = timeStr,
      isLiveTyping = isLiveTyping
    )
    _messages.value = _messages.value + newMsg
  }

  override fun onCleared() {
    super.onCleared()
    ttsManager.shutdown()
    speechManager.destroy()
  }
}
