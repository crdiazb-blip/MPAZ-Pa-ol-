package com.example.mpazpaolr

import com.example.mpazpaolr.data.security.PasswordHasher
import com.example.mpazpaolr.model.FechaIso
import com.example.mpazpaolr.model.Rol
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AutenticacionTest {

    @Test
    fun hash_conLaMismaSalEsDeterminista() {
        val sal = PasswordHasher.nuevaSal()
        assertEquals(PasswordHasher.hash("1234", sal), PasswordHasher.hash("1234", sal))
    }

    @Test
    fun hash_conOtraSalEsDistinto() {
        assertNotEquals(PasswordHasher.hash("1234", "aaaa"), PasswordHasher.hash("1234", "bbbb"))
    }

    @Test
    fun hash_noContieneLaContrasenaEnTextoPlano() {
        assertFalse(PasswordHasher.hash("admin123", "sal").contains("admin123"))
    }

    @Test
    fun coincide_aceptaLaClaveCorrectaYRechazaLaIncorrecta() {
        val sal = PasswordHasher.nuevaSal()
        val hash = PasswordHasher.hash("admin123", sal)
        assertTrue(PasswordHasher.coincide("admin123", sal, hash))
        assertFalse(PasswordHasher.coincide("admin124", sal, hash))
    }

    @Test
    fun nuevaSal_esDistintaEnCadaLlamada() {
        assertNotEquals(PasswordHasher.nuevaSal(), PasswordHasher.nuevaSal())
    }

    @Test
    fun fechaIso_convierteInstantUtcAMilisegundos() {
        assertEquals(1_788_264_000_000L, FechaIso.aMillis("2026-09-01T12:00:00Z"))
    }

    @Test
    fun fechaIso_ignoraLaParteFraccionaria() {
        assertEquals(FechaIso.aMillis("2026-09-01T12:00:00Z"), FechaIso.aMillis("2026-09-01T12:00:00.123456Z"))
    }

    @Test
    fun fechaIso_conTextoInvalidoDevuelveNull() {
        assertNull(FechaIso.aMillis(null))
        assertNull(FechaIso.aMillis(""))
        assertNull(FechaIso.aMillis("no es una fecha"))
    }

    @Test
    fun rol_desdeApiInterpretaElTextoDelServidor() {
        assertEquals(Rol.ADMIN, Rol.desdeApi("ADMIN"))
        assertEquals(Rol.SOLICITANTE, Rol.desdeApi("solicitante"))
        assertEquals(Rol.SOLICITANTE, Rol.desdeApi("desconocido"))
    }
}
