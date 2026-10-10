package com.example.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class GarvisSpeechManager(private val context: Context) {

  private var speechRecognizer: SpeechRecognizer? = null

  private val _isListening = MutableStateFlow(false)
  val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

  private val _rmsDb = MutableStateFlow(0f)
  val rmsDb: StateFlow<Float> = _rmsDb.asStateFlow()

  private val _partialText = MutableStateFlow("")
  val partialText: StateFlow<String> = _partialText.asStateFlow()

  private var onResultCallback: ((String) -> Unit)? = null
  private var onErrorCallback: ((String) -> Unit)? = null

  companion object {
    private const val TAG = "GarvisSpeechManager"
  }

  fun isAvailable(): Boolean {
    return SpeechRecognizer.isRecognitionAvailable(context)
  }

  fun startListening(
    isArabic: Boolean = false,
    onResult: (String) -> Unit,
    onError: ((String) -> Unit)? = null
  ) {
    if (_isListening.value) {
      stopListening()
      return
    }

    if (!isAvailable()) {
      onError?.invoke("Speech recognition is not available on this device.")
      return
    }

    onResultCallback = onResult
    onErrorCallback = onError
    _partialText.value = ""

    try {
      speechRecognizer?.destroy()
      speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
        setRecognitionListener(object : RecognitionListener {
          override fun onReadyForSpeech(params: Bundle?) {
            _isListening.value = true
          }

          override fun onBeginningOfSpeech() {}

          override fun onRmsChanged(rmsdB: Float) {
            // Normalize roughly from -2 to 10 dB into 0.0 to 1.0 range
            val normalized = ((rmsdB + 2f) / 12f).coerceIn(0f, 1f)
            _rmsDb.value = normalized
          }

          override fun onBufferReceived(buffer: ByteArray?) {}

          override fun onEndOfSpeech() {
            _isListening.value = false
            _rmsDb.value = 0f
          }

          override fun onError(error: Int) {
            _isListening.value = false
            _rmsDb.value = 0f
            val errorMsg = when (error) {
              SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
              SpeechRecognizer.ERROR_CLIENT -> "Client side error"
              SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Audio permission required"
              SpeechRecognizer.ERROR_NETWORK -> "Network connection error"
              SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout"
              SpeechRecognizer.ERROR_NO_MATCH -> "No speech recognized"
              SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Recognition service busy"
              SpeechRecognizer.ERROR_SERVER -> "Server error"
              SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech input detected"
              else -> "Speech recognition error: $error"
            }
            Log.w(TAG, "SpeechRecognizer error: $errorMsg")
            onErrorCallback?.invoke(errorMsg)
          }

          override fun onResults(results: Bundle?) {
            _isListening.value = false
            _rmsDb.value = 0f
            val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            val speech = matches?.firstOrNull() ?: ""
            _partialText.value = ""
            if (speech.isNotBlank()) {
              onResultCallback?.invoke(speech)
            }
          }

          override fun onPartialResults(partialResults: Bundle?) {
            val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            val partial = matches?.firstOrNull() ?: ""
            _partialText.value = partial
          }

          override fun onEvent(eventType: Int, params: Bundle?) {}
        })
      }

      val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
        if (isArabic) {
          putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ar-SA")
          putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "ar-SA")
        } else {
          putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US")
          putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "en-US")
        }
      }

      speechRecognizer?.startListening(intent)
      _isListening.value = true
    } catch (e: Exception) {
      Log.e(TAG, "Failed to start speech recognition", e)
      _isListening.value = false
      onError?.invoke("Failed to start speech recognition: ${e.message}")
    }
  }

  fun stopListening() {
    try {
      speechRecognizer?.stopListening()
    } catch (e: Exception) {
      Log.w(TAG, "Error stopping speech recognition", e)
    }
    _isListening.value = false
    _rmsDb.value = 0f
  }

  fun destroy() {
    try {
      speechRecognizer?.destroy()
      speechRecognizer = null
    } catch (e: Exception) {
      Log.w(TAG, "Error destroying speech recognizer", e)
    }
  }
}
