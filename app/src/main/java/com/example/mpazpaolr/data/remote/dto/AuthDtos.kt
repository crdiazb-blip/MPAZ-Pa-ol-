package com.example.mpazpaolr.data.remote.dto

data class LoginRequestDto(
    val rut: String,
    val password: String
)

data class UsuarioDto(
    val id: Long,
    val rut: String,
    val nombre: String,
    val cargo: String,
    val correo: String,
    val rol: String
)

data class LoginResponseDto(
    val token: String,
    val expiraEn: String,
    val usuario: UsuarioDto
)
