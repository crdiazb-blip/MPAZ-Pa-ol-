package com.example.mpazpaolr

import com.example.mpazpaolr.model.Validaciones
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

/** Validaciones de los formularios del perfil Administrador. */
class ValidacionesInventarioTest {

    @Test
    fun textoObligatorio_rechazaVacioYEspacios() {
        assertNotNull(Validaciones.textoObligatorio("", "el nombre"))
        assertNotNull(Validaciones.textoObligatorio("   ", "el nombre"))
        assertNull(Validaciones.textoObligatorio("Proyector Epson", "el nombre"))
    }

    @Test
    fun textoObligatorio_respetaElLargoMaximo() {
        assertNotNull(Validaciones.textoObligatorio("a".repeat(81), "la marca", 80))
        assertNull(Validaciones.textoObligatorio("a".repeat(80), "la marca", 80))
    }

    @Test
    fun stock_aceptaEnterosEnRango() {
        assertNull(Validaciones.stock("0"))
        assertNull(Validaciones.stock("25"))
        assertNull(Validaciones.stock(Validaciones.STOCK_MAXIMO.toString()))
    }

    @Test
    fun stock_rechazaVacioNegativoTextoYExceso() {
        assertNotNull(Validaciones.stock(""))
        assertNotNull(Validaciones.stock("-3"))
        assertNotNull(Validaciones.stock("abc"))
        assertNotNull(Validaciones.stock("2.5"))
        assertNotNull(Validaciones.stock((Validaciones.STOCK_MAXIMO + 1).toString()))
    }

    @Test
    fun categoria_debeSerUnaDeLaLista() {
        assertNull(Validaciones.categoria("Notebook"))
        assertNull(Validaciones.categoria("Equipamiento de red"))
        assertNotNull(Validaciones.categoria(""))
        assertNotNull(Validaciones.categoria("Cosas varias"))
    }

    @Test
    fun justificacionRechazo_esObligatoriaYConLargoMinimo() {
        assertNotNull(Validaciones.justificacionRechazo(""))
        assertNotNull(Validaciones.justificacionRechazo("   "))
        assertNotNull(Validaciones.justificacionRechazo("no"))
        assertNull(Validaciones.justificacionRechazo("Equipo en mantenimiento"))
    }

    @Test
    fun justificacionRechazo_tieneLargoMaximo() {
        assertNotNull(Validaciones.justificacionRechazo("x".repeat(501)))
        assertNull(Validaciones.justificacionRechazo("x".repeat(500)))
    }
}
