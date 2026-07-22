package com.undef.fintrackmobile.data.repository

import android.util.Log
import android.util.Base64
import com.squareup.moshi.Moshi
import com.undef.fintrackmobile.data.network.GroqApiService
import com.undef.fintrackmobile.data.network.dto.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException

class GroqRepository(
    private val apiService: GroqApiService,
    private val moshi: Moshi,
) {
    suspend fun parseTicket(imageBytes: ByteArray): Result<TicketParsedResponse> = withContext(Dispatchers.IO) {
        val base64Image = Base64.encodeToString(imageBytes, Base64.NO_WRAP)
        val imageUrl = "data:image/jpeg;base64,$base64Image"

        val prompt = """
        Extract from receipt:
        1. supermarket (name)
        2. date (yyyy-MM-dd)
        3. total (sum)
        4. products (list: name, quantity, price, discount)
        
        RETURN ONLY JSON.
        """.trimIndent()

        // Modelo de visión soportado por Groq
        val modelName = "qwen/qwen3.6-27b"

        try {
            Log.d("GroqRepo", "Using model: $modelName")

            val request = GroqRequest(
                model = modelName,
                messages = listOf(
                    GroqMessage(
                        role = "system",
                        content = "You are a professional OCR. You only output valid JSON. No preamble. No thinking. No markdown. Just '{...}'."
                    ),
                    GroqMessage(
                        role = "user",
                        content = listOf(
                            GroqContent(type = "text", text = prompt),
                            GroqContent(type = "image_url", imageUrl = GroqImageUrl(url = imageUrl))
                        )
                    )
                ),
                temperature = 0.0,
                maxTokens = 4096,
                responseFormat = GroqResponseFormat(type = "json_object")
            )

            val response = apiService.getChatCompletion(request)
            val rawContent = response.choices.firstOrNull()?.message?.content
                ?: throw Exception("No content received from model")

            Log.d("GroqRepo", "Received raw response: $rawContent")

            // Extracción manual del JSON (por si viene con texto/markdown alrededor)
            val startIndex = rawContent.indexOf('{')
            val endIndex = rawContent.lastIndexOf('}')

            if (startIndex == -1 || endIndex == -1) {
                Log.e("GroqRepo", "Full raw response (first 2000 chars): ${rawContent.take(2000)}")
                throw Exception("El modelo no devolvió un JSON válido (faltan llaves). Asegúrate de que el modelo $modelName sea un modelo de visión y tenga suficientes tokens.")
            }

            val cleanJsonContent = rawContent.substring(startIndex, endIndex + 1)

            // Sanitizado defensivo: saca comas colgantes antes de } o ]
            // (implementado a mano, sin regex, para evitar problemas de escapado)
            val sanitizedJsonContent = removeTrailingCommas(cleanJsonContent)

            Log.d("GroqRepo", "Sanitized JSON: $sanitizedJsonContent")

            // .lenient() para tolerar caracteres de control sin escapar y otras
            // pequeñas inconsistencias que a veces meten los modelos.
            val adapter = moshi.adapter(TicketParsedResponse::class.java).lenient()
            val parsed = adapter.fromJson(sanitizedJsonContent)
                ?: throw Exception("Error al parsear el JSON extraído")

            return@withContext Result.success(parsed)

        } catch (e: Exception) {
            Log.w("GroqRepo", "Model $modelName failed: ${e.message}")
            if (e is HttpException) {
                val errorBody = e.response()?.errorBody()?.string()
                Result.failure(Exception("Groq Error (HTTP ${e.code()}): $errorBody"))
            } else {
                Result.failure(e)
            }
        }
    }

    /**
     * Saca comas colgantes antes de '}' o ']' (ej: {"a":1,} -> {"a":1}).
     * Implementado con un scan manual de caracteres para no depender de regex.
     */
    private fun removeTrailingCommas(json: String): String {
        val result = StringBuilder()
        var i = 0
        while (i < json.length) {
            val c = json[i]
            if (c == ',') {
                var j = i + 1
                while (j < json.length && json[j].isWhitespace()) j++
                if (j < json.length && (json[j] == '}' || json[j] == ']')) {
                    i++
                    continue
                }
            }
            result.append(c)
            i++
        }
        return result.toString()
    }
}
