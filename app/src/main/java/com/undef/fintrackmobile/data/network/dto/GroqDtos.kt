package com.undef.fintrackmobile.data.network.dto

import com.squareup.moshi.Json

data class GroqRequest(
    val model: String,
    val messages: List<GroqMessage>,
    val temperature: Double = 0.0,
    @Json(name = "response_format") val responseFormat: GroqResponseFormat? = null
)

data class GroqResponseFormat(
    val type: String = "json_object"
)

data class GroqMessage(
    val role: String,
    val content: Any // Can be String or List<GroqContent>
)

data class GroqContent(
    val type: String,
    val text: String? = null,
    @Json(name = "image_url") val imageUrl: GroqImageUrl? = null
)

data class GroqImageUrl(
    val url: String
)

data class GroqChatResponse(
    val choices: List<GroqChoice>
)

data class GroqChoice(
    val message: GroqChoiceMessage
)

data class GroqChoiceMessage(
    val content: String
)

data class TicketParsedResponse(
    val supermarket: String,
    val date: String?, // yyyy-MM-dd
    val products: List<ParsedProduct>
)

data class ParsedProduct(
    val name: String,
    val quantity: Int,
    val price: Double,
    val discount: Double = 0.0
)
