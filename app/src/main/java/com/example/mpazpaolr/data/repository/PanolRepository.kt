package com.example.mpazpaolr.data.repository

import android.content.Context
import androidx.room.withTransaction
import com.example.mpazpaolr.data.database.AppDatabase
import com.example.mpazpaolr.data.local.entity.EquipoEntity
import com.example.mpazpaolr.data.local.entity.SolicitudConItems
import com.example.mpazpaolr.data.remote.ApiResult
import com.example.mpazpaolr.data.remote.RetrofitClient
import com.example.mpazpaolr.data.remote.aEntity
import com.example.mpazpaolr.data.remote.dto.ItemSolicitudRequestDto
import com.example.mpazpaolr.data.remote.dto.NotificacionDto
import com.example.mpazpaolr.data.remote.dto.SolicitudDto
import com.example.mpazpaolr.data.remote.dto.SolicitudRequestDto
import com.example.mpazpaolr.data.remote.itemsAEntity
import com.example.mpazpaolr.data.remote.llamarApi
import com.example.mpazpaolr.model.CarritoItem
import com.example.mpazpaolr.model.FormatoFecha
import kotlinx.coroutines.flow.Flow

/**
 * Fuente única de datos de equipos y solicitudes. La UI siempre lee de Room (funciona sin conexión);
 * las funciones "sincronizar" y las operaciones llaman a los microservicios y actualizan esa copia local.
 */
class PanolRepository private constructor(private val db: AppDatabase) {

    private val dao = db.panolDao()

    val equipos: Flow<List<EquipoEntity>> = dao.observarEquipos()
    val solicitudes: Flow<List<SolicitudConItems>> = dao.observarSolicitudes()

    /** Descarga el catálogo del inventory-service y reemplaza la copia local. */
    suspend fun sincronizarEquipos(): ApiResult<Unit> {
        return when (val resultado = llamarApi { RetrofitClient.equipoApi.listar() }) {
            is ApiResult.Ok -> {
                db.withTransaction {
                    dao.borrarEquipos()
                    dao.insertarEquipos(resultado.valor.map { it.aEntity() })
                }
                ApiResult.Ok(Unit)
            }
            is ApiResult.Error -> resultado
            is ApiResult.SinConexion -> resultado
        }
    }

    /** Descarga las solicitudes del usuario (o todas, si es administrador) y reemplaza la copia local. */
    suspend fun sincronizarSolicitudes(): ApiResult<Unit> {
        return when (val resultado = llamarApi { RetrofitClient.solicitudApi.listar() }) {
            is ApiResult.Ok -> {
                db.withTransaction {
                    dao.borrarItems()
                    dao.borrarSolicitudes()
                    dao.insertarSolicitudes(resultado.valor.map { it.aEntity() })
                    dao.insertarItems(resultado.valor.flatMap { it.itemsAEntity() })
                }
                ApiResult.Ok(Unit)
            }
            is ApiResult.Error -> resultado
            is ApiResult.SinConexion -> resultado
        }
    }

    /** Envía la solicitud al request-service. El servidor vuelve a validar la regla de 3 horas y el stock. */
    suspend fun crearSolicitud(
        sala: String,
        fecha: String,
        horaInicio: String,
        horaDevolucion: String,
        items: List<CarritoItem>
    ): ApiResult<SolicitudDto> {
        val peticion = SolicitudRequestDto(
            sala = sala.trim(),
            fecha = FormatoFecha.displayAIso(fecha),
            horaInicio = horaInicio.trim(),
            horaDevolucion = horaDevolucion.trim(),
            items = items.map { ItemSolicitudRequestDto(it.equipoId, it.cantidad) }
        )
        val resultado = llamarApi { RetrofitClient.solicitudApi.crear(peticion) }
        if (resultado is ApiResult.Ok) guardarSolicitud(resultado.valor)
        return resultado
    }

    /** Guarda (o reemplaza) una solicitud y sus equipos en la copia local. */
    suspend fun guardarSolicitud(dto: SolicitudDto) {
        db.withTransaction {
            dao.borrarItemsDeSolicitud(dto.id)
            dao.insertarSolicitud(dto.aEntity())
            dao.insertarItems(dto.itemsAEntity())
        }
    }

    /** Al cerrar sesión se borran las solicitudes guardadas para no dejar datos de otra persona en el teléfono. */
    suspend fun limpiarSolicitudes() {
        db.withTransaction {
            dao.borrarItems()
            dao.borrarSolicitudes()
        }
    }

    suspend fun notificaciones(): ApiResult<List<NotificacionDto>> =
        llamarApi { RetrofitClient.notificacionApi.listar() }

    suspend fun marcarNotificacionLeida(id: Long): ApiResult<NotificacionDto> =
        llamarApi { RetrofitClient.notificacionApi.marcarLeida(id) }

    companion object {
        @Volatile
        private var INSTANCE: PanolRepository? = null

        fun getRepository(context: Context): PanolRepository {
            return INSTANCE ?: synchronized(this) {
                PanolRepository(AppDatabase.getDatabase(context)).also { INSTANCE = it }
            }
        }
    }
}
