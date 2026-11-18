package com.example.mpazpaolr.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.mpazpaolr.model.EstadoSolicitud

/** Copia local de una solicitud del request-service (el id es el del servidor). */
@Entity(tableName = "solicitudes")
data class SolicitudEntity(
    @PrimaryKey
    val id: Int,
    val solicitanteRut: String,
    val solicitanteNombre: String,
    val solicitanteCargo: String? = null,
    val sala: String,
    /** Fecha de uso en formato dd/MM/yyyy. */
    val fecha: String,
    val horaInicio: String,
    val horaDevolucion: String,
    val estado: EstadoSolicitud,
    val comentarioGestion: String? = null,
    val creadaEn: Long,
    val aceptadaEn: Long? = null,
    val entregadaEn: Long? = null,
    val devueltaEn: Long? = null
)
