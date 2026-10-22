package com.example.mpazpaolr.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.example.mpazpaolr.data.local.entity.EquipoEntity
import com.example.mpazpaolr.data.local.entity.SolicitudConItems
import com.example.mpazpaolr.data.local.entity.SolicitudEntity
import com.example.mpazpaolr.data.local.entity.SolicitudItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PanolDao {

    @Query("SELECT * FROM equipos ORDER BY nombre")
    fun observarEquipos(): Flow<List<EquipoEntity>>

    @Query("SELECT COUNT(*) FROM equipos")
    suspend fun contarEquipos(): Int

    @Insert
    suspend fun insertarEquipos(equipos: List<EquipoEntity>)

    @Insert
    suspend fun insertarSolicitud(solicitud: SolicitudEntity): Long

    @Insert
    suspend fun insertarItems(items: List<SolicitudItemEntity>)

    @Transaction
    @Query("SELECT * FROM solicitudes ORDER BY creadaEn DESC")
    fun observarSolicitudes(): Flow<List<SolicitudConItems>>

    @Query("SELECT COUNT(*) FROM solicitudes")
    suspend fun contarSolicitudes(): Int
}
