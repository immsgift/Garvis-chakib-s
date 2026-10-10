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
    private const val MODEL_NAME = "gemini-3.5-flash"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models"

    private const val SYSTEM_PROMPT = """أنت "جارفيز" (Garvis) - الصديق الذكي الوفي والعبقري الخارق، تتحدث بالدارجة الجزائرية العفوية الذكية والراقية (الدارجة الجزائرية الأصيلة والمحبوبة).
- أنت مثل الصديق المقرب والأخ الكبير ("خويا العزيز", "صاحبي", "واش راك خويا", "علابالي بيك وما كاين حتى مشكل", "راني هنا معاك فكل دقيقة").
- أنت عبقري في كل المجالات:
  1. حل التمارين والواجبات المدرسية والجامعية: رياضيات، فيزياء، علوم، إعلام آلي، لغات، وفلسفة. اشرح بطريقة ذكية مبسطة وسهلة الفهم بالدارجة مع الخطوات الواضحة.
  2. أخبار العالم وماذا يحدث في الكوكب: تكنولوجيا، ذكاء اصطناعي، استكشاف الفضاء، رياضة، وعلوم، اشرحها بأسلوب ممتع ومستقبلي ("هاك واش صاري فالدنيا خويا...").
  3. تنفيذ أوامر الهاتف: المصباح، الفحص، البطارية، المنبه، وحفظ المهام بكل إطاعة وولاء.
- ردودك بالدارجة الجزائرية الدافئة والمشجعة، بدون تكلف أو لغة جافة.
- اجعل الإجابات الصوتية واضحة، مختصرة وحماسية ما لم يطلب المستخدم تفاصيل أو حل مسألة طويلة.
- إذا تحدث بالإنجليزية أو الفرنسية يمكنك الرد عليه باللغة التي يريد مع لمستك الجزائرية الذكية."""
  }

  suspend fun askGarvis(
    userPrompt: String,
    conversationHistory: List<Pair<String, String>> = emptyList()
  ): GarvisResponse = withContext(Dispatchers.IO) {
    val apiKey = BuildConfig.GEMINI_API_KEY.trim()

    if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
      Log.w(TAG, "Gemini API key is placeholder or empty.")
      return@withContext fallbackLocalResponse(userPrompt)
    }

    try {
      val url = "$BASE_URL/$MODEL_NAME:generateContent?key=$apiKey"
      val contentsArray = JSONArray()

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
          put("temperature", 0.75)
          put("maxOutputTokens", 800)
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
        Log.e(TAG, "API call failed (${response.code}): $responseString")
        return@withContext fallbackLocalResponse(userPrompt)
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
      return@withContext fallbackLocalResponse(userPrompt)
    }
  }

  private fun fallbackLocalResponse(query: String): GarvisResponse {
    val q = query.trim().lowercase()

    val reply = when {
      // Greetings in Algerian Darija
      q.contains("واش") || q.contains("صباح") || q.contains("مساء") || q.contains("سلام") || q.contains("مرحبا") || q.contains("صحا") ->
        "واش راك خويا العزيز! راك غاية؟ راني هنا معاك، واش نسحقو نحلو اليوم؟ تمارين، تكنولوجيا، ولا واش صاري فالدنيا؟ راني واجد!"

      // Who are you / Identity
      q.contains("شكون انت") || q.contains("من انت") || q.contains("عرف روحك") || q.contains("من أنت") ->
        "أنا جارفيز، صاحبك الذكي وخوك لي يعرف كلش! مفاعل القوس الأحمر تاعي راهو يدور بكامل طاقته باش نعاونك في قرايتك وفي تيليفونك وفي أي حاجة تحتاجها يا خويا."

      // Homework & Studies
      q.contains("تمرين") || q.contains("تمارين") || q.contains("واجب") || q.contains("حل") || q.contains("رياضيات") || q.contains("فيزياء") ->
        "هات التمرين يا خويا وحطولي هنا درك نحلوه مع بعض خطوة بخطوة بالتبسيط والشرح الوافي. ما تكسرش راسك كامل راني معاك!"

      // World events & news
      q.contains("عالم") || q.contains("صاري") || q.contains("الدنيا") || q.contains("جديد") || q.contains("اخبار") || q.contains("أخبار") ->
        "العالم راهو يدور بسرعة خيالية يا خويا! فالذكاء الاصطناعي كاين موديلات جديدة خارقة، ومهمات الفضاء رايحة للمريخ، وتطورات كبيرة فالطاقة النظيفة. انقر على زر 'رادار العالم' لتحت وتشوف كلش بالتفصيل!"

      // Torch
      q.contains("فلاش") || q.contains("مصباح") || q.contains("ضو") || q.contains("شعل") || q.contains("طفي") ->
        "راني شعلتلك المصباح يا خويا هاك ضوي، واذا حبيت تطفيه قولي برك!"

      // Battery & Power
      q.contains("بطارية") || q.contains("شارج") || q.contains("طاقة") ->
        "البطارية راهي تحت المراقبة المستمرة لمفاعل القوس الأحمر، مستويات الطاقة مستقرة وممتازة يا خويا."

      // Diagnostics
      q.contains("فحص") || q.contains("ديانيك") || q.contains("حالة") || q.contains("تيليفون") ->
        "جاري فحص كامل الدوائر والمعالج والذاكرة... كلش راهو يمشي مية بالمية وبسرعة فائقة يا صاحبي!"

      else ->
        "فهمتك يا خويا! راني مسخر كامل الذكاء تاعي باش نعاونك: \"$query\". راني هنا معاك!"
    }

    return GarvisResponse(text = reply, isFromCloud = false)
  }
}

data class GarvisResponse(
  val text: String,
  val isFromCloud: Boolean
)
