package com.example.mpazpaolr.model

val SALAS_DISPONIBLES = listOf(
    "Sala 1", "Sala 2", "Sala 3", "Sala 4", "Sala 5", "Sala 6",
    "Laboratorio", "Gimnasio"
)

data class SolicitudModel(
    val sala: String = "",
    val fecha: String = "",
    val horaInicio: String = "",
    val horaDevolucion: String = "",
    val salaError: String? = null,
    val fechaError: String? = null,
    val horaInicioError: String? = null,
    val horaDevolucionError: String? = null,
    val errorMessage: String? = null
)
