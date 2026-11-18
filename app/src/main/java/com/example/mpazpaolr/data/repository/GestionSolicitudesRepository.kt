package com.example.mpazpaolr.data.repository

import android.content.Context
import com.example.mpazpaolr.data.remote.ApiResult
import com.example.mpazpaolr.data.remote.RetrofitClient
import com.example.mpazpaolr.data.remote.dto.RechazoRequestDto
import com.example.mpazpaolr.data.remote.dto.RegistroBitacoraDto
import com.example.mpazpaolr.data.remote.dto.SolicitudDto
import com.example.mpazpaolr.data.remote.llamarApi

/**
 * Consola de auditoría del perfil Administrador: acepta o rechaza solicitudes y registra la entrega
 * y la devolución. Cada acción se ejecuta en el request-service y, si el servidor la acepta,
 * se actualiza la copia local de la solicitud (y el stock del catálogo cuando corresponde).
 */
class GestionSolicitudesRepository private constructor(private val panol: PanolRepository) {

    val solicitudes = panol.solicitudes

    suspend fun sincronizar(): ApiResult<Unit> {
        val equipos = panol.sincronizarEquipos()
        val solicitudes = panol.sincronizarSolicitudes()
        return if (solicitudes is ApiResult.Ok) equipos else solicitudes
    }

    suspend fun aceptar(id: Int): ApiResult<Unit> =
        ejecutar(actualizaStock = true) { RetrofitClient.solicitudApi.aceptar(id) }

    suspend fun rechazar(id: Int, comentario: String): ApiResult<Unit> =
        ejecutar { RetrofitClient.solicitudApi.rechazar(id, RechazoRequestDto(comentario.trim())) }

    suspend fun entregar(id: Int): ApiResult<Unit> =
        ejecutar { RetrofitClient.solicitudApi.entregar(id) }

    suspend fun devolver(id: Int): ApiResult<Unit> =
        ejecutar(actualizaStock = true) { RetrofitClient.solicitudApi.devolver(id) }

    suspend fun finalizar(id: Int): ApiResult<Unit> =
        ejecutar { RetrofitClient.solicitudApi.finalizar(id) }

    /** Historial de hitos (Solicitado, Aprobado, Entregado, Devuelto...) guardado en el audit-service. */
    suspend fun bitacora(solicitudId: Int): ApiResult<List<RegistroBitacoraDto>> =
        llamarApi { RetrofitClient.bitacoraApi.listar(solicitudId) }

    private suspend fun ejecutar(
        actualizaStock: Boolean = false,
        llamada: suspend () -> SolicitudDto
    ): ApiResult<Unit> {
        return when (val r = llamarApi { llamada() }) {
            is ApiResult.Ok -> {
                panol.guardarSolicitud(r.valor)
                if (actualizaStock) panol.sincronizarEquipos()
                ApiResult.Ok(Unit)
            }
            is ApiResult.Error -> r
            is ApiResult.SinConexion -> r
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: GestionSolicitudesRepository? = null

        fun getRepository(context: Context): GestionSolicitudesRepository {
            return INSTANCE ?: synchronized(this) {
                GestionSolicitudesRepository(PanolRepository.getRepository(context)).also { INSTANCE = it }
            }
        }
    }
}
