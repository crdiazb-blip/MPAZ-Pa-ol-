package com.example.mpazpaolr.data.remote

import com.example.mpazpaolr.data.local.entity.EquipoEntity
import com.example.mpazpaolr.data.local.entity.SolicitudEntity
import com.example.mpazpaolr.data.local.entity.SolicitudItemEntity
import com.example.mpazpaolr.data.remote.dto.EquipoDto
import com.example.mpazpaolr.data.remote.dto.SolicitudDto
import com.example.mpazpaolr.model.Categoria
import com.example.mpazpaolr.model.EstadoEquipo
import com.example.mpazpaolr.model.EstadoSolicitud
import com.example.mpazpaolr.model.FechaIso
import com.example.mpazpaolr.model.FormatoFecha

/** Convierte las respuestas del servidor (DTO) en entidades de la base de datos local (Room). */

fun EquipoDto.aEntity() = EquipoEntity(
    id = id,
    nombre = nombre,
    marca = marca,
    modelo = modelo,
    descripcion = descripcion,
    categoria = Categoria.entries.firstOrNull { it.name == categoria } ?: Categoria.OTRA,
    stockTotal = stockTotal,
    stockDisponible = stockDisponible,
    estado = EstadoEquipo.entries.firstOrNull { it.name == estado } ?: EstadoEquipo.FUERA_DE_SERVICIO,
    ubicacion = ubicacion
)

fun SolicitudDto.aEntity() = SolicitudEntity(
    id = id,
    solicitanteRut = solicitanteRut,
    solicitanteNombre = solicitanteNombre,
    solicitanteCargo = solicitanteCargo,
    sala = sala,
    fecha = FormatoFecha.isoADisplay(fecha),
    horaInicio = horaInicio,
    horaDevolucion = horaDevolucion,
    estado = EstadoSolicitud.entries.firstOrNull { it.name == estado } ?: EstadoSolicitud.PENDIENTE,
    comentarioGestion = comentarioGestion,
    creadaEn = FechaIso.aMillis(creadaEn) ?: System.currentTimeMillis(),
    aceptadaEn = FechaIso.aMillis(aceptadaEn),
    entregadaEn = FechaIso.aMillis(entregadaEn),
    devueltaEn = FechaIso.aMillis(devueltaEn)
)

fun SolicitudDto.itemsAEntity() = items.map {
    SolicitudItemEntity(solicitudId = id, equipoId = it.equipoId, nombreEquipo = it.nombreEquipo, cantidad = it.cantidad)
}
