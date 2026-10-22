package com.example.mpazpaolr.data.repository

import com.example.mpazpaolr.model.Perfil

// Esta versión no tiene base de datos de usuarios: existe un único perfil solicitante ficticio.
object AuthRepository {

    private const val CORREO_DEMO = "docente@mpaz.cl"
    private const val CLAVE_DEMO = "1234"

    private val perfilDemo = Perfil(
        nombre = "Docente Demo",
        cargo = "Docente",
        correo = CORREO_DEMO
    )

    var perfilActivo: Perfil? = null
        private set

    fun iniciarSesion(correo: String, clave: String): Boolean {
        val ok = correo.trim().equals(CORREO_DEMO, ignoreCase = true) && clave == CLAVE_DEMO
        perfilActivo = if (ok) perfilDemo else null
        return ok
    }

    fun cerrarSesion() {
        perfilActivo = null
    }
}
