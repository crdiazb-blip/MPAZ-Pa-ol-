package com.example.mpazpaolr.data.database

import androidx.room.TypeConverter
import com.example.mpazpaolr.model.Categoria
import com.example.mpazpaolr.model.EstadoEquipo
import com.example.mpazpaolr.model.EstadoSolicitud
import com.example.mpazpaolr.model.Rol

class Converters {
    @TypeConverter
    fun categoriaToString(valor: Categoria): String = valor.name

    @TypeConverter
    fun stringToCategoria(valor: String): Categoria = Categoria.valueOf(valor)

    @TypeConverter
    fun estadoEquipoToString(valor: EstadoEquipo): String = valor.name

    @TypeConverter
    fun stringToEstadoEquipo(valor: String): EstadoEquipo = EstadoEquipo.valueOf(valor)

    @TypeConverter
    fun estadoSolicitudToString(valor: EstadoSolicitud): String = valor.name

    @TypeConverter
    fun stringToEstadoSolicitud(valor: String): EstadoSolicitud = EstadoSolicitud.valueOf(valor)

    @TypeConverter
    fun rolToString(valor: Rol): String = valor.name

    @TypeConverter
    fun stringToRol(valor: String): Rol = Rol.valueOf(valor)
}
