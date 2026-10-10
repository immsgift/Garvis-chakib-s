package com.example.voice

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.speech.tts.Voice
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

class GarvisTtsManager(private val context: Context) : TextToSpeech.OnInitListener {

  private var tts: TextToSpeech? = null
  private var isInitialized = false

  private val _isSpeaking = MutableStateFlow(false)
  val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

  private val _isMuted = MutableStateFlow(false)
  val isMuted: StateFlow<Boolean> = _isMuted.asStateFlow()

  private var onSpeechDoneCallback: (() -> Unit)? = null

  companion object {
    private const val TAG = "GarvisTtsManager"
    private const val DEFAULT_PITCH = 0.85f // Deep, calm JARVIS-like pitch
    private const val DEFAULT_RATE = 1.0f
  }

  init {
    tts = TextToSpeech(context.applicationContext, this)
  }

  override fun onInit(status: Int) {
    if (status == TextToSpeech.SUCCESS) {
      isInitialized = true
      tts?.let { engine ->
        engine.setPitch(DEFAULT_PITCH)
        engine.setSpeechRate(DEFAULT_RATE)

        // Try UK or US English as default
        val result = engine.setLanguage(Locale.UK)
        if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
          engine.setLanguage(Locale.US)
        }

        // Try to pick a male sounding voice if available
        try {
          val voices = engine.voices
          if (!voices.isNullOrEmpty()) {
            val maleVoice = voices.find { voice ->
              val name = voice.name.lowercase()
              (name.contains("male") || name.contains("en-gb") || name.contains("en_gb")) &&
                  !voice.isNetworkConnectionRequired
            }
            if (maleVoice != null) {
              engine.voice = maleVoice
            }
          }
        } catch (e: Exception) {
          Log.w(TAG, "Voice selection fallback", e)
        }

        engine.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
          override fun onStart(utteranceId: String?) {
            _isSpeaking.value = true
          }

          override fun onDone(utteranceId: String?) {
            _isSpeaking.value = false
            onSpeechDoneCallback?.invoke()
          }

          @Deprecated("Deprecated in Java")
          override fun onError(utteranceId: String?) {
            _isSpeaking.value = false
          }

          override fun onError(utteranceId: String?, errorCode: Int) {
            _isSpeaking.value = false
          }
        })
      }
      Log.d(TAG, "TTS initialized successfully")
    } else {
      Log.e(TAG, "TTS initialization failed: $status")
    }
  }

  fun toggleMute(): Boolean {
    val newMute = !_isMuted.value
    _isMuted.value = newMute
    if (newMute) {
      stop()
    }
    return newMute
  }

  fun speak(text: String, isArabic: Boolean = false, onDone: (() -> Unit)? = null) {
    if (_isMuted.value) {
      onDone?.invoke()
      return
    }

    if (!isInitialized || tts == null) {
      Log.w(TAG, "TTS not yet initialized")
      onDone?.invoke()
      return
    }

    onSpeechDoneCallback = onDone

    // Clean text for natural speech (remove markdown asterisks, hashes, backticks, URLs)
    val cleanedText = cleanTextForSpeech(text)
    if (cleanedText.isBlank()) {
      onDone?.invoke()
      return
    }

    tts?.let { engine ->
      // Set language according to context
      if (isArabic || cleanedText.any { it in '\u0600'..'\u06FF' }) {
        val arLocale = Locale.forLanguageTag("ar")
        engine.setLanguage(arLocale)
        engine.setPitch(0.92f)
      } else {
        engine.setLanguage(Locale.UK)
        engine.setPitch(DEFAULT_PITCH)
      }
      engine.setSpeechRate(DEFAULT_RATE)

      val utteranceId = "garvis_${System.currentTimeMillis()}"
      engine.speak(cleanedText, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
    }
  }

  fun stop() {
    tts?.stop()
    _isSpeaking.value = false
  }

  fun shutdown() {
    stop()
    tts?.shutdown()
    tts = null
  }

  private fun cleanTextForSpeech(input: String): String {
    return input
      .replace(Regex("[*#`_~>|\\[\\]]"), " ")
      .replace(Regex("http[s]?://\\S+"), "")
      .replace(Regex("\\s+"), " ")
      .trim()
  }
}
