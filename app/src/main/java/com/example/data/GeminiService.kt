package com.example.data

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object GeminiService {
    private val client = OkHttpClient.Builder()
        .connectTimeout(45, TimeUnit.SECONDS)
        .readTimeout(45, TimeUnit.SECONDS)
        .build()

    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/"

    suspend fun generateContent(
        model: String,
        prompt: String,
        systemInstruction: String? = null,
        thinking: Boolean = false,
        useSearch: Boolean = false,
        useMaps: Boolean = false
    ): String = withContext(Dispatchers.IO) {
        val apiKey = com.example.BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext "Error: Gemini API Key is missing. Please enter your real API key in the Secrets panel in AI Studio."
        }

        val modelName = when (model) {
            "gemini-3.1-pro-preview" -> "gemini-1.5-pro-latest" // fallback to stable or use raw
            "gemini-3.5-flash" -> "gemini-1.5-flash"
            "gemini-3.1-flash-lite" -> "gemini-1.5-flash"
            else -> model
        }

        val url = "$BASE_URL$modelName:generateContent?key=$apiKey"

        val rootJson = JSONObject()
        val contentsArray = JSONArray()
        val contentObj = JSONObject()
        contentObj.put("role", "user")
        val partsArray = JSONArray()
        val partObj = JSONObject()
        partObj.put("text", prompt)
        partsArray.put(partObj)
        contentObj.put("parts", partsArray)
        contentsArray.put(contentObj)
        rootJson.put("contents", contentsArray)

        // Add systemInstruction if specified
        if (systemInstruction != null) {
            val systemInstructionObj = JSONObject()
            val systemPartsArray = JSONArray()
            val systemPartObj = JSONObject()
            systemPartObj.put("text", systemInstruction)
            systemPartsArray.put(systemPartObj)
            systemInstructionObj.put("parts", systemPartsArray)
            rootJson.put("systemInstruction", systemInstructionObj)
        }

        // Add generationConfig / thinkingConfig if thinking is enabled
        if (thinking) {
            val generationConfig = JSONObject()
            // In gemini-3.1-pro-preview or gemini-2.0-flash-thinking, thinkingLevel/thinkingBudget can be configured.
            val thinkingConfig = JSONObject()
            thinkingConfig.put("thinkingBudget", 2048)
            generationConfig.put("thinkingConfig", thinkingConfig)
            rootJson.put("generationConfig", generationConfig)
        }

        // Add tools for search or maps grounding if specified
        if (useSearch || useMaps) {
            val toolsArray = JSONArray()
            val toolObj = JSONObject()
            
            // Structure: { "googleSearch": {} }
            val googleSearchObj = JSONObject()
            toolObj.put("googleSearch", googleSearchObj)
            toolsArray.put(toolObj)
            
            rootJson.put("tools", toolsArray)
        }

        val mediaType = "application/json; charset=utf-8".toMediaType()
        val requestBody = rootJson.toString().toRequestBody(mediaType)

        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    val bodyText = response.body?.string() ?: ""
                    return@withContext "API Call Failed: Code ${response.code}\n$bodyText"
                }
                val bodyString = response.body?.string() ?: return@withContext "Empty response"
                val resObj = JSONObject(bodyString)
                val candidates = resObj.optJSONArray("candidates")
                if (candidates != null && candidates.length() > 0) {
                    val firstCandidate = candidates.getJSONObject(0)
                    val content = firstCandidate.optJSONObject("content")
                    if (content != null) {
                        val parts = content.optJSONArray("parts")
                        if (parts != null && parts.length() > 0) {
                            val sb = StringBuilder()
                            for (i in 0 until parts.length()) {
                                val part = parts.getJSONObject(i)
                                val txt = part.optString("text")
                                if (txt.isNotEmpty()) {
                                    sb.append(txt)
                                }
                            }
                            return@withContext sb.toString()
                        }
                    }
                }
                return@withContext "Could not parse response content. Body:\n$bodyString"
            }
        } catch (e: Exception) {
            return@withContext "Error calling Gemini: ${e.message ?: "Unknown error"}"
        }
    }
}
