package com.example.mpazpaolr.data.repository

import com.example.mpazpaolr.model.Perfil
import com.example.mpazpaolr.model.Validaciones

object AuthRepository {

    const val RUT_DEMO = "12.345.678-5"
    const val CLAVE_DEMO = "1234"

    private val perfilDemo = Perfil(
        nombre = "Docente Demo",
        cargo = "Docente",
        rut = RUT_DEMO
    )

    var perfilActivo: Perfil? = null
        private set

    fun iniciarSesion(rut: String, clave: String): Boolean {
        val ok = Validaciones.normalizarRut(rut) == Validaciones.normalizarRut(RUT_DEMO) &&
            clave == CLAVE_DEMO
        perfilActivo = if (ok) perfilDemo else null
        return ok
    }

    fun cerrarSesion() {
        perfilActivo = null
    }
}
