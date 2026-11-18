package com.example.mpazpaolr.data.remote.dto

data class EquipoDto(
    val id: Int,
    val nombre: String,
    val marca: String,
    val modelo: String,
    val descripcion: String?,
    val categoria: String,
    val stockTotal: Int,
    val stockDisponible: Int,
    val estado: String,
    val ubicacion: String,
    val reservable: Boolean
)

data class EquipoRequestDto(
    val nombre: String,
    val marca: String,
    val modelo: String,
    val descripcion: String? = null,
    val categoria: String,
    val stockTotal: Int,
    val stockDisponible: Int? = null,
    val estado: String? = null,
    val ubicacion: String
)

data class EstadoRequestDto(val estado: String)

data class StockRequestDto(val stockTotal: Int)
