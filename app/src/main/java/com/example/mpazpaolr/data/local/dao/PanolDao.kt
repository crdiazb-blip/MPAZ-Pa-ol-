package com.example.mpazpaolr.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.mpazpaolr.data.local.entity.EquipoEntity
import com.example.mpazpaolr.data.local.entity.SolicitudConItems
import com.example.mpazpaolr.data.local.entity.SolicitudEntity
import com.example.mpazpaolr.data.local.entity.SolicitudItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PanolDao {

    // ---- Equipos (caché del catálogo) ----

    @Query("SELECT * FROM equipos ORDER BY nombre")
    fun observarEquipos(): Flow<List<EquipoEntity>>

    @Query("SELECT COUNT(*) FROM equipos")
    suspend fun contarEquipos(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarEquipos(equipos: List<EquipoEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarEquipo(equipo: EquipoEntity)

    @Query("DELETE FROM equipos")
    suspend fun borrarEquipos()

    @Query("DELETE FROM equipos WHERE id = :id")
    suspend fun borrarEquipo(id: Int)

    // ---- Solicitudes (caché de las solicitudes del usuario) ----

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarSolicitud(solicitud: SolicitudEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarSolicitudes(solicitudes: List<SolicitudEntity>)

    @Insert
    suspend fun insertarItems(items: List<SolicitudItemEntity>)

    @Transaction
    @Query("SELECT * FROM solicitudes ORDER BY creadaEn DESC")
    fun observarSolicitudes(): Flow<List<SolicitudConItems>>

    @Query("SELECT COUNT(*) FROM solicitudes")
    suspend fun contarSolicitudes(): Int

    @Query("DELETE FROM solicitud_items WHERE solicitudId = :solicitudId")
    suspend fun borrarItemsDeSolicitud(solicitudId: Int)

    @Query("DELETE FROM solicitud_items")
    suspend fun borrarItems()

    @Query("DELETE FROM solicitudes")
    suspend fun borrarSolicitudes()
}
