package com.example.inspirationmushroom.ai

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class AnalysisResult(
    val emotion: String,
    val keywords: List<String>,
    val summary: String,
    val category: String
) {
    companion object {
        private val json = Json {
            ignoreUnknownKeys = true
            isLenient = true
        }

        fun fromJson(jsonString: String?): AnalysisResult? {
            return try {
                jsonString?.let { json.decodeFromString<AnalysisResult>(it) }
            } catch (e: Exception) {
                null
            }
        }
    }
}
