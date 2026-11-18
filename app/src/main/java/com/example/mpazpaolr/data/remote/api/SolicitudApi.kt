package com.example.mpazpaolr.data.remote.api

import com.example.mpazpaolr.data.remote.dto.RechazoRequestDto
import com.example.mpazpaolr.data.remote.dto.SolicitudDto
import com.example.mpazpaolr.data.remote.dto.SolicitudRequestDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

interface SolicitudApi {

    @POST("api/solicitudes")
    suspend fun crear(@Body request: SolicitudRequestDto): SolicitudDto

    /** El administrador recibe todas; el solicitante, solo las suyas. */
    @GET("api/solicitudes")
    suspend fun listar(): List<SolicitudDto>

    @PATCH("api/solicitudes/{id}/aceptar")
    suspend fun aceptar(@Path("id") id: Int): SolicitudDto

    @PATCH("api/solicitudes/{id}/rechazar")
    suspend fun rechazar(@Path("id") id: Int, @Body request: RechazoRequestDto): SolicitudDto

    @PATCH("api/solicitudes/{id}/entregar")
    suspend fun entregar(@Path("id") id: Int): SolicitudDto

    @PATCH("api/solicitudes/{id}/devolver")
    suspend fun devolver(@Path("id") id: Int): SolicitudDto

    @PATCH("api/solicitudes/{id}/finalizar")
    suspend fun finalizar(@Path("id") id: Int): SolicitudDto
}
