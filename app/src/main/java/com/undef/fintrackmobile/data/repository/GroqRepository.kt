package com.undef.fintrackmobile.data.repository

import android.util.Base64
import com.squareup.moshi.Moshi
import com.undef.fintrackmobile.data.network.GroqApiService
import com.undef.fintrackmobile.data.network.dto.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GroqRepository(
    private val apiService: GroqApiService,
    private val moshi: Moshi
) {
    suspend fun parseTicket(imageBytes: ByteArray): Result<TicketParsedResponse> = withContext(Dispatchers.IO) {
        try {
            val base64Image = Base64.encodeToString(imageBytes, Base64.NO_WRAP)
            val imageUrl = "data:image/jpeg;base64,$base64Image"

            val prompt = """
                Extract the details from this supermarket ticket. 
                Return a JSON object with the following fields:
                - supermarket: String (name of the store)
                - date: String (format yyyy-MM-dd, if not present use current date)
                - products: Array of objects with:
                    - name: String
                    - quantity: Integer
                    - price: Double (unit price)
                    - discount: Double (optional, default 0.0)
                Return ONLY the JSON object.
            """.trimIndent()

            val request = GroqRequest(
                model = "llama-3.2-11b-vision-preview",
                messages = listOf(
                    GroqMessage(
                        role = "user",
                        content = listOf(
                            GroqContent(type = "text", text = prompt),
                            GroqContent(type = "image_url", imageUrl = GroqImageUrl(url = imageUrl))
                        )
                    )
                ),
                responseFormat = GroqResponseFormat()
            )

            val response = apiService.getChatCompletion(request)
            val jsonContent = response.choices.firstOrNull()?.message?.content ?: return@withContext Result.failure(Exception("Empty response from AI"))
            
            val adapter = moshi.adapter(TicketParsedResponse::class.java)
            val parsed = adapter.fromJson(jsonContent) ?: return@withContext Result.failure(Exception("Failed to parse AI JSON"))
            
            Result.success(parsed)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
