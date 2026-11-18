package com.example.mpazpaolr.data.remote.api

import com.example.mpazpaolr.data.remote.dto.NotificacionDto
import com.example.mpazpaolr.data.remote.dto.RegistroBitacoraDto
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.Path
import retrofit2.http.Query

interface BitacoraApi {

    @GET("api/bitacora")
    suspend fun listar(@Query("solicitudId") solicitudId: Int? = null): List<RegistroBitacoraDto>
}

interface NotificacionApi {

    @GET("api/notificaciones")
    suspend fun listar(): List<NotificacionDto>

    @PATCH("api/notificaciones/{id}/leida")
    suspend fun marcarLeida(@Path("id") id: Long): NotificacionDto
}
