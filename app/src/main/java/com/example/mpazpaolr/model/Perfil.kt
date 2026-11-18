package com.example.mpazpaolr.model

data class Perfil(
    val nombre: String,
    val cargo: String,
    val rut: String,
    val correo: String = "",
    val rol: Rol = Rol.SOLICITANTE
)
