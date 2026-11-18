package com.example.mpazpaolr.data.remote.dto

data class RegistroBitacoraDto(
    val id: Long,
    val solicitudId: Long,
    val solicitanteRut: String,
    val hito: String,
    val actorRut: String,
    val actorNombre: String,
    val detalle: String?,
    val fecha: String
)

data class NotificacionDto(
    val id: Long,
    val solicitudId: Long?,
    val tipo: String,
    val titulo: String,
    val mensaje: String,
    val enviadaEn: String?,
    val leida: Boolean
)

/** Cuerpo de error estándar de todos los microservicios. */
data class ErrorDto(
    val status: Int?,
    val error: String?,
    val message: String?
)
