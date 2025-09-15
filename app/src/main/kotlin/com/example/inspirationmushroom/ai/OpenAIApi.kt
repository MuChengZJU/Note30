package com.example.inspirationmushroom.ai

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface OpenAIApi {
    @GET("v1/models")
    suspend fun getModels(
        @Header("Authorization") apiKey: String
    ): Response<ModelsResponse>

    @POST("v1/chat/completions")
    suspend fun getChatCompletions(
        @Header("Authorization") apiKey: String,
        @Body request: ChatRequest
    ): Response<ChatResponse>
}
