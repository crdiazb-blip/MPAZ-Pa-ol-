package com.example.mpazpaolr.model

/** Estado del formulario de alta y edición de equipos (perfil Administrador). */
data class EquipoFormModel(
    /** null = equipo nuevo; con valor = se está editando ese equipo. */
    val editandoId: Int? = null,
    val nombre: String = "",
    val marca: String = "",
    val modelo: String = "",
    val descripcion: String = "",
    val categoria: String = "",
    val stockTotal: String = "",
    val ubicacion: String = "",
    val estado: String = EstadoEquipo.DISPONIBLE.label,
    val nombreError: String? = null,
    val marcaError: String? = null,
    val modeloError: String? = null,
    val categoriaError: String? = null,
    val stockError: String? = null,
    val ubicacionError: String? = null,
    val errorMessage: String? = null,
    val guardando: Boolean = false
)
