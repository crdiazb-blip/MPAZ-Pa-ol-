package com.example.mpazpaolr.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.mpazpaolr.model.Categoria
import com.example.mpazpaolr.model.EstadoEquipo

/** Copia local del catálogo del inventory-service (el id es el del servidor). */
@Entity(tableName = "equipos")
data class EquipoEntity(
    @PrimaryKey
    val id: Int,
    val nombre: String,
    val marca: String,
    val modelo: String,
    val descripcion: String? = null,
    val categoria: Categoria,
    val stockTotal: Int,
    val stockDisponible: Int,
    val estado: EstadoEquipo,
    val ubicacion: String
) {
    val reservable: Boolean
        get() = estado == EstadoEquipo.DISPONIBLE && stockDisponible > 0
}
