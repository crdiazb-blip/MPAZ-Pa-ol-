package com.example.mpazpaolr.data.repository

import android.content.Context
import com.example.mpazpaolr.data.database.AppDatabase
import com.example.mpazpaolr.data.local.entity.EquipoEntity
import com.example.mpazpaolr.data.remote.ApiResult
import com.example.mpazpaolr.data.remote.RetrofitClient
import com.example.mpazpaolr.data.remote.aEntity
import com.example.mpazpaolr.data.remote.dto.EquipoRequestDto
import com.example.mpazpaolr.data.remote.dto.EstadoRequestDto
import com.example.mpazpaolr.data.remote.llamarApi
import com.example.mpazpaolr.model.EstadoEquipo
import kotlinx.coroutines.flow.Flow

/**
 * Gestión del inventario para el perfil Administrador. Cada cambio se envía al inventory-service
 * y, si el servidor lo acepta, se refleja de inmediato en la copia local (Room).
 */
class InventarioRepository private constructor(private val db: AppDatabase) {

    private val dao = db.panolDao()

    val equipos: Flow<List<EquipoEntity>> = dao.observarEquipos()

    suspend fun crearEquipo(peticion: EquipoRequestDto): ApiResult<Unit> {
        return when (val r = llamarApi { RetrofitClient.equipoApi.crear(peticion) }) {
            is ApiResult.Ok -> {
                dao.insertarEquipo(r.valor.aEntity())
                ApiResult.Ok(Unit)
            }
            is ApiResult.Error -> r
            is ApiResult.SinConexion -> r
        }
    }

    suspend fun actualizarEquipo(id: Int, peticion: EquipoRequestDto): ApiResult<Unit> {
        return when (val r = llamarApi { RetrofitClient.equipoApi.actualizar(id, peticion) }) {
            is ApiResult.Ok -> {
                dao.insertarEquipo(r.valor.aEntity())
                ApiResult.Ok(Unit)
            }
            is ApiResult.Error -> r
            is ApiResult.SinConexion -> r
        }
    }

    suspend fun cambiarEstado(id: Int, estado: EstadoEquipo): ApiResult<Unit> {
        return when (val r = llamarApi { RetrofitClient.equipoApi.cambiarEstado(id, EstadoRequestDto(estado.name)) }) {
            is ApiResult.Ok -> {
                dao.insertarEquipo(r.valor.aEntity())
                ApiResult.Ok(Unit)
            }
            is ApiResult.Error -> r
            is ApiResult.SinConexion -> r
        }
    }

    /** Baja lógica: el equipo se retira del inventario (el servidor impide hacerlo si hay unidades prestadas). */
    suspend fun darDeBaja(id: Int): ApiResult<Unit> {
        return when (val r = llamarApi { RetrofitClient.equipoApi.darDeBaja(id) }) {
            is ApiResult.Ok -> {
                dao.borrarEquipo(id)
                ApiResult.Ok(Unit)
            }
            is ApiResult.Error -> r
            is ApiResult.SinConexion -> r
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: InventarioRepository? = null

        fun getRepository(context: Context): InventarioRepository {
            return INSTANCE ?: synchronized(this) {
                InventarioRepository(AppDatabase.getDatabase(context)).also { INSTANCE = it }
            }
        }
    }
}
