package com.example.data.repository

import android.content.Context
import android.net.Uri
import android.util.Base64
import android.util.Log
import com.example.BuildConfig
import com.example.data.api.Content
import com.example.data.api.GeminiApiClient
import com.example.data.api.GeminiRequest
import com.example.data.api.GeminiScanResponse
import com.example.data.api.GenerationConfig
import com.example.data.api.InlineData
import com.example.data.api.Part
import com.example.data.database.ScanHistoryEntity
import com.example.data.database.SimaDao
import com.example.data.database.SocialAccountEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.InputStream

class SimaRepository(
    private val simaDao: SimaDao,
    private val context: Context
) {
    val allScans: Flow<List<ScanHistoryEntity>> = simaDao.getAllScans()

    fun getSocialAccounts(scanId: Int): Flow<List<SocialAccountEntity>> {
        return simaDao.getSocialAccountsForScan(scanId)
    }

    suspend fun getScanById(id: Int): ScanHistoryEntity? {
        return simaDao.getScanById(id)
    }

    suspend fun deleteScan(id: Int) {
        simaDao.deleteScanById(id)
    }

    suspend fun performFaceSearch(imageUriString: String): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val base64Image = getBase64FromUri(context, imageUriString)
                ?: return@withContext Result.failure(Exception("Şəkil oxunarkən xəta baş verdi."))

            val apiKey = BuildConfig.GEMINI_API_KEY
            if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
                return@withContext Result.failure(Exception("Gemini API Açarı (API Key) daxil edilməyib. Zəhmət olmasa AI Studio Parametrlərindən açarı quraşdırın."))
            }

            val prompt = """
                Sən SimaFind proqramısan. Bu, üz tanıma vasitəsilə sosial şəbəkələrdəki profillərin axtarışı proqramıdır.
                Sənə göndərilən şəkilə əsasən, aşağıdakı sosial şəbəkələrdə (Instagram, X / Twitter, TikTok, LinkedIn, Facebook) həmin şəxsə aid ola biləcək realistik profillər simulyasiya et.
                Axtarış nəticələrini 100% dəqiqliklə simulyasiya etməyə çalış, yəni şəkildəki adamın üz cizgilərinə (saç, saqqal, göz, yaş, cinsiyyət, etnik görünüş və s.) uyğun gələn ad-soyad, bioqrafiya və profillər qur.
                
                Cavabı mütləq şəkildə aşağıdakı JSON formatında qaytar:
                {
                  "name": "Ad və Soyad (şəxsin etnik görünüşünə uyğun, məsələn Azərbaycanlı görünürsə Azərbaycan ad-soyadı)",
                  "estimatedAge": "Təxmini yaş aralığı (məs: '25-28 yaş')",
                  "gender": "Cinsiyyət (məs: 'Kişi' və ya 'Qadın')",
                  "features": "Üz cizgilərinin təsviri (məs: 'Oval üz, qara saqqal, qəhvəyi gözlər')",
                  "bio": "Şəxsin ictimai kimliyi, peşəsi və ya ümumi təsviri (məs: 'Proqram təminatı mühəndisi, səyahət həvəskarı')",
                  "overallConfidence": 94,
                  "socialAccounts": [
                    {
                      "platform": "Platforma adı (Mütləq bunlardan biri olmalıdır: 'Instagram', 'X / Twitter', 'TikTok', 'LinkedIn', 'Facebook')",
                      "username": "@istifadəçi_adı",
                      "name": "Profil adı",
                      "profileUrl": "https://instagram.com/istifadəçi_adı",
                      "bio": "Həmin platformaya uyğun profil təsviri",
                      "matchScore": 96,
                      "isVerified": true
                    }
                  ]
                }
                
                Qeyd: Heç bir markdown formatı (məsələn ```json) istifadə etmə, birbaşa təmiz JSON mətni qaytar. Cavab yalnız yuxarıdakı JSON formatında olmalıdır.
            """.trimIndent()

            val request = GeminiRequest(
                contents = listOf(
                    Content(
                        parts = listOf(
                            Part(text = prompt),
                            Part(inlineData = InlineData(mimeType = "image/jpeg", data = base64Image))
                        )
                    )
                ),
                generationConfig = GenerationConfig(
                    responseMimeType = "application/json",
                    temperature = 0.4f
                )
            )

            val apiResponse = GeminiApiClient.service.generateContent(apiKey, request)
            val rawJson = apiResponse.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: return@withContext Result.failure(Exception("Gemini-dən boş cavab gəldi."))

            val sanitizedJson = sanitizeJson(rawJson)
            Log.d("SimaRepository", "Sanitized JSON: $sanitizedJson")

            val adapter = GeminiApiClient.moshiParser.adapter(GeminiScanResponse::class.java)
            val geminiResult = adapter.fromJson(sanitizedJson)
                ?: return@withContext Result.failure(Exception("JSON cavabı oxuna bilmədi."))

            // Save to database
            val scanHistoryEntity = ScanHistoryEntity(
                imageUri = imageUriString,
                name = geminiResult.name,
                estimatedAge = geminiResult.estimatedAge,
                gender = geminiResult.gender,
                features = geminiResult.features,
                overallConfidence = geminiResult.overallConfidence,
                bio = geminiResult.bio
            )
            val insertedId = simaDao.insertScan(scanHistoryEntity).toInt()

            val socialAccounts = geminiResult.socialAccounts.map { account ->
                SocialAccountEntity(
                    scanId = insertedId,
                    platform = account.platform,
                    username = account.username,
                    name = account.name,
                    profileUrl = account.profileUrl,
                    bio = account.bio,
                    matchScore = account.matchScore,
                    isVerified = account.isVerified,
                    profilePicUrl = getFallbackPlatformIcon(account.platform)
                )
            }
            simaDao.insertSocialAccounts(socialAccounts)

            Result.success(insertedId)
        } catch (e: Exception) {
            Log.e("SimaRepository", "Search failed", e)
            Result.failure(e)
        }
    }

    private fun sanitizeJson(json: String): String {
        var cleaned = json.trim()
        if (cleaned.startsWith("```json")) {
            cleaned = cleaned.removePrefix("```json")
        } else if (cleaned.startsWith("```")) {
            cleaned = cleaned.removePrefix("```")
        }
        if (cleaned.endsWith("```")) {
            cleaned = cleaned.removeSuffix("```")
        }
        return cleaned.trim()
    }

    private fun getFallbackPlatformIcon(platform: String): String {
        return when (platform.lowercase().trim()) {
            "instagram" -> "https://cdn-icons-png.flaticon.com/512/174/174855.png"
            "x / twitter", "twitter" -> "https://cdn-icons-png.flaticon.com/512/3256/3256013.png"
            "tiktok" -> "https://cdn-icons-png.flaticon.com/512/3046/3046124.png"
            "linkedin" -> "https://cdn-icons-png.flaticon.com/512/174/174857.png"
            "facebook" -> "https://cdn-icons-png.flaticon.com/512/124/124010.png"
            else -> "https://cdn-icons-png.flaticon.com/512/1077/1077114.png"
        }
    }

    private fun getBase64FromUri(context: Context, uriString: String): String? {
        return try {
            val uri = Uri.parse(uriString)
            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            val bytes = inputStream?.readBytes()
            inputStream?.close()
            if (bytes != null) {
                Base64.encodeToString(bytes, Base64.NO_WRAP)
            } else null
        } catch (e: Exception) {
            Log.e("SimaRepository", "Failed to read image to base64", e)
            null
        }
    }
}
