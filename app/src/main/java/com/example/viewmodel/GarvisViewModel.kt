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

  // Default to true for Algerian Arabic Darija
  private val _isArabic = MutableStateFlow(true)
  val isArabic: StateFlow<Boolean> = _isArabic.asStateFlow()

  private val _messages = MutableStateFlow<List<TranscriptMessage>>(emptyList())
  val messages: StateFlow<List<TranscriptMessage>> = _messages.asStateFlow()

  private val _memoryInfo = MutableStateFlow(deviceManager.getMemoryTelemetry())
  val memoryInfo: StateFlow<MemoryInfoData> = _memoryInfo.asStateFlow()

  private val _showDiagnosticsDialog = MutableStateFlow(false)
  val showDiagnosticsDialog: StateFlow<Boolean> = _showDiagnosticsDialog.asStateFlow()

  private val _showWorldRadarDialog = MutableStateFlow(false)
  val showWorldRadarDialog: StateFlow<Boolean> = _showWorldRadarDialog.asStateFlow()

  private var currentJob: Job? = null

  init {
    val timeStr = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
    val initialMsg = TranscriptMessage(
      sender = "GARVIS",
      text = "أهلاً بيك يا خويا العزيز! جارفيز الجزائري معاك. مفاعل القوس الأحمر واجد 100%. أنا صاحبك لي يعرف كلش ويحل التمارين ويوريلك واش صاري فالعالم. واش نديرو درك؟",
      timestamp = timeStr,
      isLiveTyping = true
    )
    _messages.value = listOf(initialMsg)

    viewModelScope.launch {
      ttsManager.isSpeaking.collect { speaking ->
        if (speaking) {
          _status.value = GarvisStatus.SPEAKING
        } else if (_status.value == GarvisStatus.SPEAKING) {
          _status.value = GarvisStatus.STANDBY
        }
      }
    }

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
    val confirmation = if (_isArabic.value)
      "راني نهدر بالدارجة الجزائرية معاك يا خويا."
    else
      "Switched to English interface mode, Sir."
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
          addTranscriptMessage("GARVIS", "ما سمعتكش مليح يا خويا: $error")
        }
      )
    }
  }

  fun processUserPrompt(prompt: String) {
    if (prompt.isBlank()) return
    deviceManager.triggerHaptic()

    addTranscriptMessage("USER", prompt, isLiveTyping = false)

    val lower = prompt.lowercase().trim()
    val isAr = true // Primary Algerian mode

    // 1. Torch / Flashlight command in Darija & English
    if (lower.contains("flashlight") || lower.contains("torch") || lower.contains("مصباح") || lower.contains("فلاش") || lower.contains("ضو")) {
      val isNowOn = deviceManager.toggleFlashlight()
      val reply = if (isNowOn) "شعلتلك الفلاش يا خويا، هاك ضوي بيه!" else "طفيت الفلاش يا خويا."
      respondAsGarvis(reply, isAr)
      return
    }

    // 2. World radar command in Darija
    if (lower.contains("عالم") || lower.contains("صاري") || lower.contains("جديد") || lower.contains("اخبار") || lower.contains("أخبار") || lower.contains("دنيا")) {
      _showWorldRadarDialog.value = true
      val reply = "هاك واش صاري فالعالم يا خويا! فتحتلك رادار الأحداث العالمية والتكنولوجيا باش تشوف كلش بعينيك."
      respondAsGarvis(reply, isAr)
      return
    }

    // 3. Diagnostics command in Darija
    if (lower.contains("diagnostic") || lower.contains("فحص") || lower.contains("تيليفون") || lower.contains("حالة")) {
      _showDiagnosticsDialog.value = true
      val reply = "جاري فحص كامل دوائر المفاعل ومكونات التيليفون يا صاحبي... كلش راهو سليم وممتاز!"
      respondAsGarvis(reply, isAr)
      return
    }

    // 4. Exercise / Homework solver command in Darija
    if (lower.contains("تمرين") || lower.contains("تمارين") || lower.contains("واجب") || lower.contains("فرض") || lower.contains("مسألة")) {
      currentJob?.cancel()
      currentJob = viewModelScope.launch {
        _status.value = GarvisStatus.PROCESSING
        val homeworkPrompt = "أنا صاحبك ونحتاجك تحلي هذا التمرين وتشرحهولي بالتفصيل وبخطوات واضحة وبأسلوب جزائري ذكي ومبسط: $prompt"
        val history = _messages.value.takeLast(4).map { it.sender to it.text }
        val response = aiService.askGarvis(homeworkPrompt, history)
        respondAsGarvis(response.text, isAr)
      }
      return
    }

    // 5. Alarm command
    if (lower.contains("alarm") || lower.contains("منبه") || lower.contains("صحيحي") || lower.contains("نبهني")) {
      deviceManager.openAlarmOrTimer()
      val reply = "فتحتلك المنبه يا خويا باش تريكلو واش تحب."
      respondAsGarvis(reply, isAr)
      return
    }

    // 6. Calendar command
    if (lower.contains("calendar") || lower.contains("تقويم") || lower.contains("جدول") || lower.contains("تاريخ")) {
      deviceManager.openCalendar()
      val reply = "فتحتلك التقويم باش تشوف كامل مواعيدك يا خويا."
      respondAsGarvis(reply, isAr)
      return
    }

    // General question -> Gemini AI Service
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
    ttsManager.speak(text, isArabic = true)
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
    val msg = if (newState) "شعلت المصباح يا خويا." else "طفيت المصباح يا خويا."
    addTranscriptMessage("GARVIS", msg, isLiveTyping = false)
    ttsManager.speak(msg, isArabic = true)
  }

  fun runDiagnostics() {
    deviceManager.triggerHaptic()
    _showDiagnosticsDialog.value = true
    val msg = "راني درت فحص شامل للتيليفون والمفاعل يا خويا، كلش راهو يمشي مية بالمية."
    addTranscriptMessage("GARVIS", msg, isLiveTyping = false)
    ttsManager.speak(msg, isArabic = true)
  }

  fun openWorldRadar() {
    deviceManager.triggerHaptic()
    _showWorldRadarDialog.value = true
    val msg = "رادار العالم راهو مفتوح يا خويا، شوف واش كاين جديد فالتكنولوجيا والعلوم والرياضة!"
    addTranscriptMessage("GARVIS", msg, isLiveTyping = false)
    ttsManager.speak(msg, isArabic = true)
  }

  fun dismissWorldRadar() {
    _showWorldRadarDialog.value = false
  }

  fun triggerSolveExerciseMode() {
    deviceManager.triggerHaptic()
    val msg = "أعطيني التمرين تاعك درك نحلوه خطوة بخطوة بالتبسيط يا صاحبي! اكتبو ولا قولهولي بالصوت."
    addTranscriptMessage("GARVIS", msg, isLiveTyping = true)
    ttsManager.speak(msg, isArabic = true)
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
