package com.example.mpazpaolr.model

data class LoginModel(
    val rut: String = "",
    val password: String = "",
    val rutError: String? = null,
    val passwordError: String? = null,
    val errorMessage: String? = null
)
