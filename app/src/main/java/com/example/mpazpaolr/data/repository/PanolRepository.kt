package com.example.mpazpaolr.data.repository

import android.content.Context
import androidx.room.withTransaction
import com.example.mpazpaolr.data.database.AppDatabase
import com.example.mpazpaolr.data.local.entity.EquipoEntity
import com.example.mpazpaolr.data.local.entity.SolicitudConItems
import com.example.mpazpaolr.data.local.entity.SolicitudEntity
import com.example.mpazpaolr.data.local.entity.SolicitudItemEntity
import com.example.mpazpaolr.model.Categoria
import com.example.mpazpaolr.model.CarritoItem
import com.example.mpazpaolr.model.EstadoEquipo
import com.example.mpazpaolr.model.EstadoSolicitud
import kotlinx.coroutines.flow.Flow

class PanolRepository private constructor(private val db: AppDatabase) {

    private val dao = db.panolDao()

    val equipos: Flow<List<EquipoEntity>> = dao.observarEquipos()
    val solicitudes: Flow<List<SolicitudConItems>> = dao.observarSolicitudes()

    suspend fun cargarDatosIniciales() {
        if (dao.contarEquipos() == 0) dao.insertarEquipos(catalogoFicticio())
        if (dao.contarSolicitudes() == 0) cargarSolicitudesDeEjemplo()
    }

    suspend fun crearSolicitud(
        sala: String,
        fecha: String,
        horaInicio: String,
        horaDevolucion: String,
        items: List<CarritoItem>
    ) {
        db.withTransaction {
            val id = dao.insertarSolicitud(
                SolicitudEntity(
                    sala = sala.trim(),
                    fecha = fecha.trim(),
                    horaInicio = horaInicio.trim(),
                    horaDevolucion = horaDevolucion.trim(),
                    estado = EstadoSolicitud.PENDIENTE,
                    creadaEn = System.currentTimeMillis()
                )
            ).toInt()
            dao.insertarItems(
                items.map {
                    SolicitudItemEntity(
                        solicitudId = id,
                        equipoId = it.equipoId,
                        nombreEquipo = it.nombre,
                        cantidad = it.cantidad
                    )
                }
            )
        }
    }

    private suspend fun cargarSolicitudesDeEjemplo() {
        val ahora = System.currentTimeMillis()
        val ejemplos = listOf(
            SolicitudEntity(
                sala = "Sala 4° Básico A", fecha = "01/09/2026",
                horaInicio = "09:00", horaDevolucion = "10:30",
                estado = EstadoSolicitud.RECHAZADA,
                comentarioGestion = "El proyector está en mantenimiento esa semana.",
                creadaEn = ahora - 86_400_000L
            ),
            SolicitudEntity(
                sala = "Laboratorio de Ciencias", fecha = "02/09/2026",
                horaInicio = "11:00", horaDevolucion = "12:30",
                estado = EstadoSolicitud.ACEPTADA,
                creadaEn = ahora - 43_200_000L
            )
        )
        ejemplos.forEachIndexed { indice, solicitud ->
            val id = dao.insertarSolicitud(solicitud).toInt()
            val (equipoId, nombre) = if (indice == 0) 3 to "Proyector Epson X41" else 6 to "Kit LEGO WeDo 2.0"
            dao.insertarItems(listOf(SolicitudItemEntity(solicitudId = id, equipoId = equipoId, nombreEquipo = nombre, cantidad = 1)))
        }
    }

    private fun catalogoFicticio() = listOf(
        EquipoEntity(nombre = "Notebook Lenovo V15", marca = "Lenovo", modelo = "V15", categoria = Categoria.NOTEBOOK, stockTotal = 10, stockDisponible = 7, estado = EstadoEquipo.DISPONIBLE, ubicacion = "Pañol - Estante A"),
        EquipoEntity(nombre = "Computador de escritorio", marca = "HP", modelo = "ProDesk 400", categoria = Categoria.COMPUTADOR, stockTotal = 4, stockDisponible = 4, estado = EstadoEquipo.DISPONIBLE, ubicacion = "Pañol - Estante B"),
        EquipoEntity(nombre = "Proyector Epson X41", marca = "Epson", modelo = "X41", categoria = Categoria.PROYECTOR, stockTotal = 3, stockDisponible = 0, estado = EstadoEquipo.MANTENIMIENTO, ubicacion = "Pañol - Estante C"),
        EquipoEntity(nombre = "Parlante portátil", marca = "JBL", modelo = "Partybox 100", categoria = Categoria.PARLANTE, stockTotal = 2, stockDisponible = 2, estado = EstadoEquipo.DISPONIBLE, ubicacion = "Pañol - Estante C"),
        EquipoEntity(nombre = "Alargador 5 metros", marca = "Steren", modelo = "AL-5M", categoria = Categoria.ALARGADOR, stockTotal = 12, stockDisponible = 9, estado = EstadoEquipo.DISPONIBLE, ubicacion = "Pañol - Cajón 1"),
        EquipoEntity(nombre = "Kit LEGO WeDo 2.0", marca = "LEGO", modelo = "WeDo 2.0", categoria = Categoria.ROBOTICA, stockTotal = 6, stockDisponible = 5, estado = EstadoEquipo.DISPONIBLE, ubicacion = "Pañol - Estante D"),
        EquipoEntity(nombre = "Placa Arduino UNO", marca = "Arduino", modelo = "UNO R3", categoria = Categoria.ROBOTICA, stockTotal = 8, stockDisponible = 8, estado = EstadoEquipo.DISPONIBLE, ubicacion = "Pañol - Estante D"),
        EquipoEntity(nombre = "Placa ESP32", marca = "Espressif", modelo = "DevKit V1", categoria = Categoria.ROBOTICA, stockTotal = 5, stockDisponible = 1, estado = EstadoEquipo.DISPONIBLE, ubicacion = "Pañol - Estante D"),
        EquipoEntity(nombre = "Switch de red 24 puertos", marca = "TP-Link", modelo = "TL-SG1024", categoria = Categoria.RED, stockTotal = 1, stockDisponible = 0, estado = EstadoEquipo.PRESTADO, ubicacion = "Sala de servidores (fija)"),
        EquipoEntity(nombre = "Router WiFi", marca = "Mikrotik", modelo = "hAP ac2", categoria = Categoria.RED, stockTotal = 1, stockDisponible = 0, estado = EstadoEquipo.FUERA_DE_SERVICIO, ubicacion = "Biblioteca (fija)")
    )

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
