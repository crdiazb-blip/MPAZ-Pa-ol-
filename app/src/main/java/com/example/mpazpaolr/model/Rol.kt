package com.example.mpazpaolr.model

/** Perfiles de usuario de la aplicación. Cada uno tiene su propia navegación y funciones. */
enum class Rol(val label: String) {
    ADMIN("Administrador (Encargado TI)"),
    SOLICITANTE("Solicitante");

    companion object {
        fun desdeApi(valor: String): Rol =
            entries.firstOrNull { it.name.equals(valor, ignoreCase = true) } ?: SOLICITANTE
    }
}
