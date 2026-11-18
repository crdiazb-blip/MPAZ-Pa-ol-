package com.example.mpazpaolr

import com.example.mpazpaolr.data.remote.ApiErrores
import com.example.mpazpaolr.data.remote.aEntity
import com.example.mpazpaolr.data.remote.dto.EquipoDto
import com.example.mpazpaolr.data.remote.dto.SolicitudDto
import com.example.mpazpaolr.data.remote.dto.SolicitudItemDto
import com.example.mpazpaolr.data.remote.itemsAEntity
import com.example.mpazpaolr.model.Categoria
import com.example.mpazpaolr.model.EstadoEquipo
import com.example.mpazpaolr.model.EstadoSolicitud
import com.example.mpazpaolr.model.FormatoFecha
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** Conversión de las respuestas de los microservicios a las entidades guardadas en Room. */
class MapeoServidorTest {

    private fun equipoDto(categoria: String = "NOTEBOOK", estado: String = "DISPONIBLE", stock: Int = 4) = EquipoDto(
        id = 7, nombre = "Notebook Lenovo", marca = "Lenovo", modelo = "V15", descripcion = null,
        categoria = categoria, stockTotal = 10, stockDisponible = stock, estado = estado,
        ubicacion = "Pañol - Estante A", reservable = true
    )

    private fun solicitudDto(estado: String = "ACEPTADA") = SolicitudDto(
        id = 12, solicitanteRut = "12345678-5", solicitanteNombre = "Docente Demo", solicitanteCargo = "Docente",
        solicitanteCorreo = "d@mpaz.test", sala = "Sala 1", fecha = "2026-09-02", horaInicio = "08:00",
        horaDevolucion = "09:30", estado = estado, comentarioGestion = null,
        creadaEn = "2026-09-01T12:00:00.123Z", aceptadaEn = "2026-09-01T12:05:00Z",
        entregadaEn = null, devueltaEn = null,
        items = listOf(SolicitudItemDto(7, "Notebook Lenovo", 2), SolicitudItemDto(9, "Alargador", 1))
    )

    @Test
    fun equipo_seConvierteConSuCategoriaYEstado() {
        val e = equipoDto().aEntity()
        assertEquals(7, e.id)
        assertEquals(Categoria.NOTEBOOK, e.categoria)
        assertEquals(EstadoEquipo.DISPONIBLE, e.estado)
        assertTrue(e.reservable)
    }

    @Test
    fun equipo_conCategoriaDesconocidaPasaAOtra() {
        assertEquals(Categoria.OTRA, equipoDto(categoria = "INVENTADA").aEntity().categoria)
    }

    @Test
    fun equipo_sinStockNoEsReservable() {
        assertFalse(equipoDto(stock = 0).aEntity().reservable)
    }

    @Test
    fun equipo_enMantenimientoNoEsReservable() {
        assertFalse(equipoDto(estado = "MANTENIMIENTO").aEntity().reservable)
    }

    @Test
    fun solicitud_seConvierteConFechaLocalYMarcasDeTiempo() {
        val s = solicitudDto().aEntity()
        assertEquals(12, s.id)
        assertEquals("02/09/2026", s.fecha)
        assertEquals(EstadoSolicitud.ACEPTADA, s.estado)
        assertEquals(1_788_264_000_000L, s.creadaEn)
        assertEquals(1_788_264_300_000L, s.aceptadaEn)
        assertNull(s.entregadaEn)
    }

    @Test
    fun solicitud_generaUnItemPorCadaEquipo() {
        val items = solicitudDto().itemsAEntity()
        assertEquals(2, items.size)
        assertEquals(12, items[0].solicitudId)
        assertEquals("Notebook Lenovo", items[0].nombreEquipo)
        assertEquals(2, items[0].cantidad)
    }

    @Test
    fun formatoFecha_conviertePorAmbosLados() {
        assertEquals("2026-09-02", FormatoFecha.displayAIso("02/09/2026"))
        assertEquals("02/09/2026", FormatoFecha.isoADisplay("2026-09-02"))
    }

    @Test
    fun formatoFecha_conTextoSinFormatoLoDejaIgual() {
        assertEquals("hoy", FormatoFecha.displayAIso("hoy"))
        assertEquals("-", FormatoFecha.millisADisplay(null))
    }

    @Test
    fun errorDelServidor_seMuestraConSuMensaje() {
        val cuerpo = "{\"status\":400,\"error\":\"Bad Request\",\"message\":\"Debes reservar con al menos 3 horas de anticipación.\"}"
        assertEquals(
            "Debes reservar con al menos 3 horas de anticipación.",
            ApiErrores.mensajeDe(cuerpo, "Error")
        )
    }

    @Test
    fun errorSinCuerpoUsaElMensajePorDefecto() {
        assertEquals("Error", ApiErrores.mensajeDe(null, "Error"))
        assertEquals("Error", ApiErrores.mensajeDe("no es json", "Error"))
    }
}
