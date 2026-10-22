package com.example.mpazpaolr.model

data class LoginModel(
    val email: String = "",
    val password: String = "",
    val emailError: String? = null,
    val passwordError: String? = null,
    val errorMessage: String? = null
)
