package com.example.mpazpaolr.data.remote.api

import com.example.mpazpaolr.data.remote.dto.LoginRequestDto
import com.example.mpazpaolr.data.remote.dto.LoginResponseDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {

    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequestDto): LoginResponseDto

    @POST("api/auth/logout")
    suspend fun logout(): Response<Unit>
}
