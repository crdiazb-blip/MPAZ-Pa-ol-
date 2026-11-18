package com.example.mpazpaolr.data.remote.dto

data class ItemSolicitudRequestDto(
    val equipoId: Int,
    val cantidad: Int
)

/** fecha en formato yyyy-MM-dd; horas en HH:mm. */
data class SolicitudRequestDto(
    val sala: String,
    val fecha: String,
    val horaInicio: String,
    val horaDevolucion: String,
    val items: List<ItemSolicitudRequestDto>
)

data class RechazoRequestDto(val comentario: String)

data class SolicitudItemDto(
    val equipoId: Int,
    val nombreEquipo: String,
    val cantidad: Int
)

data class SolicitudDto(
    val id: Int,
    val solicitanteRut: String,
    val solicitanteNombre: String,
    val solicitanteCargo: String?,
    val solicitanteCorreo: String?,
    val sala: String,
    val fecha: String,
    val horaInicio: String,
    val horaDevolucion: String,
    val estado: String,
    val comentarioGestion: String?,
    val creadaEn: String,
    val aceptadaEn: String?,
    val entregadaEn: String?,
    val devueltaEn: String?,
    val items: List<SolicitudItemDto>
)
