package com.example.mpazpaolr.data.remote.api

import com.example.mpazpaolr.data.remote.dto.EquipoDto
import com.example.mpazpaolr.data.remote.dto.EquipoRequestDto
import com.example.mpazpaolr.data.remote.dto.EstadoRequestDto
import com.example.mpazpaolr.data.remote.dto.StockRequestDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface EquipoApi {

    @GET("api/equipos")
    suspend fun listar(): List<EquipoDto>

    @POST("api/equipos")
    suspend fun crear(@Body request: EquipoRequestDto): EquipoDto

    @PUT("api/equipos/{id}")
    suspend fun actualizar(@Path("id") id: Int, @Body request: EquipoRequestDto): EquipoDto

    @PATCH("api/equipos/{id}/estado")
    suspend fun cambiarEstado(@Path("id") id: Int, @Body request: EstadoRequestDto): EquipoDto

    @PATCH("api/equipos/{id}/stock")
    suspend fun cambiarStock(@Path("id") id: Int, @Body request: StockRequestDto): EquipoDto

    @DELETE("api/equipos/{id}")
    suspend fun darDeBaja(@Path("id") id: Int): Response<Unit>
}
