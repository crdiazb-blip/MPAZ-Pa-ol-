package com.example.mpazpaolr.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Sesión activa. Solo existe una fila (id = 1); se borra al cerrar sesión. */
@Entity(tableName = "sesion")
data class SesionEntity(
    @PrimaryKey
    val id: Int = 1,
    val rut: String,
    /** Token JWT del servidor; null cuando se entró sin conexión con datos locales. */
    val token: String?,
    val expiraEn: Long
)
