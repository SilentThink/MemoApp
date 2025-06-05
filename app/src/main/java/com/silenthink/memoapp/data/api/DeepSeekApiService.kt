package com.silenthink.memoapp.data.api

import com.silenthink.memoapp.data.model.DeepSeekRequest
import com.silenthink.memoapp.data.model.DeepSeekResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface DeepSeekApiService {
    
    @POST("chat/completions")
    suspend fun chatCompletion(
        @Header("Authorization") authorization: String,
        @Header("Content-Type") contentType: String = "application/json",
        @Body request: DeepSeekRequest
    ): Response<DeepSeekResponse>
} 