package com.example.mpazpaolr.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.mpazpaolr.model.Perfil
import com.example.mpazpaolr.model.Rol

/** Usuario que ya inició sesión en este teléfono (permite volver a entrar sin conexión). */
@Entity(tableName = "usuarios_locales")
data class UsuarioLocalEntity(
    /** RUT normalizado (solo dígitos y K). */
    @PrimaryKey
    val rut: String,
    val nombre: String,
    val cargo: String,
    val correo: String,
    val rol: Rol,
    val passwordHash: String,
    val sal: String
) {
    fun aPerfil() = Perfil(nombre = nombre, cargo = cargo, rut = rut, correo = correo, rol = rol)
}
