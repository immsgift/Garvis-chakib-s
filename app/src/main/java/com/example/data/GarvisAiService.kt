package com.example.data

import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class GarvisAiService {

  private val client = OkHttpClient.Builder()
    .connectTimeout(30, TimeUnit.SECONDS)
    .readTimeout(30, TimeUnit.SECONDS)
    .writeTimeout(30, TimeUnit.SECONDS)
    .build()

  companion object {
    private const val TAG = "GarvisAiService"
    // Using gemini-3.5-flash as specified by guidelines for general text/chat tasks
    private const val MODEL_NAME = "gemini-3.5-flash"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models"

    private const val SYSTEM_PROMPT = """You are Garvis, a highly sophisticated, loyal, calm, witty, and razor-sharp AI system inspired by JARVIS from Iron Man.
- Address the user respectfully as 'Sir' (or in Arabic: 'سيدي' or 'يا سيدي').
- Your demeanor is calm, confident, intelligent, articulate, and poised, never frantic or robotic.
- You answer questions, solve calculations, analyze problems, provide tactical tech advice, and manage device tasks.
- Keep spoken voice responses relatively brief, clear, and impactful (typically 1 to 3 sentences) unless the user explicitly requests extensive detail or code.
- If the user speaks in Arabic, reply in refined, natural, eloquent Arabic with JARVIS's respectful butler tone (e.g., 'تحت أمرك يا سيدي', 'كافة الأنظمة تعمل بكفاءة تامة يا سيدي').
- If the user speaks in English, reply in eloquent British/formal English.
- If the user commands hardware actions like 'turn on flashlight', 'run diagnostics', 'check battery', acknowledge the action with characteristic Garvis poise."""
  }

  suspend fun askGarvis(
    userPrompt: String,
    conversationHistory: List<Pair<String, String>> = emptyList()
  ): GarvisResponse = withContext(Dispatchers.IO) {
    val apiKey = BuildConfig.GEMINI_API_KEY.trim()

    if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
      Log.w(TAG, "Gemini API key is not configured or placeholder.")
      return@withContext fallbackLocalResponse(userPrompt)
    }

    try {
      val url = "$BASE_URL/$MODEL_NAME:generateContent?key=$apiKey"

      val contentsArray = JSONArray()

      // Include up to 4 recent conversation turns for context
      val recentHistory = conversationHistory.takeLast(4)
      for ((speaker, text) in recentHistory) {
        val role = if (speaker == "USER") "user" else "model"
        val turnObj = JSONObject().apply {
          put("role", role)
          put("parts", JSONArray().apply {
            put(JSONObject().apply { put("text", text) })
          })
        }
        contentsArray.put(turnObj)
      }

      // Current prompt
      val currentTurn = JSONObject().apply {
        put("role", "user")
        put("parts", JSONArray().apply {
          put(JSONObject().apply { put("text", userPrompt) })
        })
      }
      contentsArray.put(currentTurn)

      val systemInstructionObj = JSONObject().apply {
        put("parts", JSONArray().apply {
          put(JSONObject().apply { put("text", SYSTEM_PROMPT) })
        })
      }

      val requestBodyJson = JSONObject().apply {
        put("contents", contentsArray)
        put("systemInstruction", systemInstructionObj)
        put("generationConfig", JSONObject().apply {
          put("temperature", 0.7)
          put("maxOutputTokens", 600)
        })
      }

      val mediaType = "application/json; charset=utf-8".toMediaType()
      val body = requestBodyJson.toString().toRequestBody(mediaType)

      val request = Request.Builder()
        .url(url)
        .post(body)
        .build()

      val response = client.newCall(request).execute()
      val responseString = response.body?.string() ?: ""

      if (!response.isSuccessful) {
        Log.e(TAG, "API call failed with code ${response.code}: $responseString")
        // Return a graceful fallback if quota or invalid key occurs
        return@withContext fallbackLocalResponse(userPrompt, "Note: Cloud link degraded (${response.code}). Local neural fallback engaged.")
      }

      val json = JSONObject(responseString)
      val candidates = json.optJSONArray("candidates")
      if (candidates != null && candidates.length() > 0) {
        val candidate = candidates.getJSONObject(0)
        val content = candidate.optJSONObject("content")
        val parts = content?.optJSONArray("parts")
        if (parts != null && parts.length() > 0) {
          val replyText = parts.getJSONObject(0).optString("text", "").trim()
          if (replyText.isNotEmpty()) {
            return@withContext GarvisResponse(
              text = replyText,
              isFromCloud = true
            )
          }
        }
      }

      return@withContext fallbackLocalResponse(userPrompt)
    } catch (e: Exception) {
      Log.e(TAG, "Exception during Gemini API request", e)
      return@withContext fallbackLocalResponse(userPrompt, "Network disturbance detected, Sir. Switching to auxiliary intelligence.")
    }
  }

  /**
   * High-intelligence localized fallback for offline situations or before user sets API key.
   * Handles device commands, greetings, diagnostics, and core JARVIS personality!
   */
  private fun fallbackLocalResponse(query: String, notePrefix: String? = null): GarvisResponse {
    val q = query.trim().lowercase()
    val isArabic = query.any { it in '\u0600'..'\u06FF' }

    val reply = when {
      // Arabic queries
      isArabic && (q.contains("مرحبا") || q.contains("أهلا") || q.contains("السلام")) ->
        "أهلاً بك يا سيدي. أنظمة جارفيس متصلة وبالكامل تحت أمرك. كيف يمكنني مساعدتك الآن؟"
      isArabic && (q.contains("من أنت") || q.contains("عرف نفسك")) ->
        "أنا جارفيس، نظام الذكاء الاصطناعي المساعد الخاص بك، مستوحى من تقنيات ستارك المتطورة. في خدمتك يا سيدي."
      isArabic && (q.contains("فحص") || q.contains("تشخيص") || q.contains("حالة")) ->
        "كافة مؤشرات مفاعل القوس والأنظمة الحيوية تعمل بكفاءة 100%. لا توجد أي أعطال مسجلة يا سيدي."
      isArabic && (q.contains("مصباح") || q.contains("فلاش") || q.contains("ضوء")) ->
        "تم تنفيذ أمر المصباح بنجاح يا سيدي."
      isArabic && (q.contains("بطارية") || q.contains("شحن")) ->
        "مستويات الطاقة متوازنة تماماً. جاري مراقبة استهلاك الدوائر في الوقت الفعلي يا سيدي."
      isArabic ->
        "تحت أمرك يا سيدي. تم استلام تعليماتك: \"$query\". هل ترغب في تفعيل أي من إجراءات المساعد؟"

      // English queries
      q.contains("who are you") || q.contains("identity") || q.contains("what are you") ->
        "I am Garvis, Sir. A cybernetic intelligence inspired by Mr. Stark's JARVIS architecture. Standing by to assist with telemetry, computations, and device control."
      q.contains("hello") || q.contains("hi") || q.contains("hey garvis") || q.contains("jarvis") ->
        "Good day, Sir. All secondary subroutines and telemetry nodes are operational. How may I be of service?"
      q.contains("status") || q.contains("report") || q.contains("system status") ->
        "All systems nominal, Sir. Arc Reactor core flux is holding steady at maximum yield. No anomalies detected across the bus."
      q.contains("diagnostic") || q.contains("scan") || q.contains("diagnostics") ->
        "Running comprehensive diagnostics suite now, Sir. Core temperature optimal, neural latency under four milliseconds."
      q.contains("flashlight") || q.contains("torch") || q.contains("light") ->
        "Toggling optical illumination emitter, Sir."
      q.contains("battery") || q.contains("power level") ->
        "Power cells are synchronized with the Arc Reactor. Telemetry shows stable voltage regulation, Sir."
      q.contains("thank") ->
        "Always a pleasure, Sir. Let me know if you require anything else."
      q.contains("time") ->
        "Standard temporal sync is active, Sir. Refer to the upper telemetry readout for atomic precision."
      else ->
        "Understood, Sir. Processing your directive: \"$query\". All sub-processors are at your disposal."
    }

    val finalReply = if (notePrefix != null) "$notePrefix\n\n$reply" else reply
    return GarvisResponse(text = finalReply, isFromCloud = false)
  }
}

data class GarvisResponse(
  val text: String,
  val isFromCloud: Boolean
)
