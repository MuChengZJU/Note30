package com.example.inspirationmushroom.ai

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ChatRequest(
    val model: String,
    val messages: List<Message>,
    @SerialName("response_format") val responseFormat: ResponseFormat? = null // For JSON mode
)

@Serializable
data class Message(
    val role: String, // "system", "user"
    val content: String
)

@Serializable
data class ResponseFormat(
    val type: String = "json_object"
)

@Serializable
data class ChatResponse(
    val choices: List<Choice>
)

@Serializable
data class Choice(
    val message: Message
)

@Serializable
data class ModelsResponse(
    val data: List<ModelInfo>
)

@Serializable
data class ModelInfo(
    val id: String,
    val owned_by: String? = null
)
