package com.example.mpazpaolr.model

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
