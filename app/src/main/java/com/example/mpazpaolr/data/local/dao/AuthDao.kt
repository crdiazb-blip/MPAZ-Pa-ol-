package com.example.mpazpaolr.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.mpazpaolr.data.local.entity.SesionEntity
import com.example.mpazpaolr.data.local.entity.UsuarioLocalEntity

@Dao
interface AuthDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun guardarUsuario(usuario: UsuarioLocalEntity)

    @Query("SELECT * FROM usuarios_locales WHERE rut = :rut")
    suspend fun buscarUsuario(rut: String): UsuarioLocalEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun guardarSesion(sesion: SesionEntity)

    @Query("SELECT * FROM sesion WHERE id = 1")
    suspend fun obtenerSesion(): SesionEntity?

    @Query("DELETE FROM sesion")
    suspend fun borrarSesion()
}
