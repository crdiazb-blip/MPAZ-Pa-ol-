package com.example.mpazpaolr.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.mpazpaolr.model.EstadoSolicitud

@Entity(tableName = "solicitudes")
data class SolicitudEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val sala: String,
    val fecha: String,
    val horaInicio: String,
    val horaDevolucion: String,
    val estado: EstadoSolicitud,
    val comentarioGestion: String? = null,
    val creadaEn: Long,
    val entregadaEn: Long? = null,
    val devueltaEn: Long? = null
)
