package com.example.mpazpaolr

import com.example.mpazpaolr.model.Validaciones
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class ValidacionesTest {

    private val ahora = Validaciones.parsear("10/09/2026", "10:00")!!

    @Test
    fun correoInvalido_devuelveError() {
        assertNotNull(Validaciones.email("sin-arroba"))
        assertNull(Validaciones.email("docente@mpaz.cl"))
    }

    @Test
    fun rut_validaDigitoVerificador() {
        assertNull(Validaciones.rut("12.345.678-5"))
        assertNull(Validaciones.rut("123456785"))
        assertNotNull(Validaciones.rut("12.345.678-9"))
        assertNotNull(Validaciones.rut(""))
        assertNotNull(Validaciones.rut("abc"))
    }

    @Test
    fun passwordCorta_devuelveError() {
        assertNotNull(Validaciones.password("123"))
        assertNull(Validaciones.password("1234"))
    }

    @Test
    fun reservaConMenosDeTresHoras_sebloquea() {
        assertNotNull(Validaciones.anticipacion("10/09/2026", "12:59", ahora))
    }

    @Test
    fun reservaConTresHoras_sePermite() {
        assertNull(Validaciones.anticipacion("10/09/2026", "13:00", ahora))
    }

    @Test
    fun devolucionAnteriorAlInicio_devuelveError() {
        assertNotNull(Validaciones.rangoHoras("10:00", "09:00"))
        assertEquals(null, Validaciones.rangoHoras("10:00", "11:00"))
    }

    @Test
    fun fechaInexistente_devuelveError() {
        assertNotNull(Validaciones.fecha("31/02/2026"))
        assertNull(Validaciones.fecha("15/09/2026"))
    }
}
