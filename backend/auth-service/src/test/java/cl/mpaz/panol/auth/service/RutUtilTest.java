package cl.mpaz.panol.auth.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RutUtilTest {

    @Test
    void normalizaPuntosYGuion() {
        assertEquals("12345678-5", RutUtil.normalizar("12.345.678-5"));
        assertEquals("12345678-5", RutUtil.normalizar("123456785"));
    }

    @Test
    void aceptaRutConDigitoVerificadorCorrecto() {
        assertTrue(RutUtil.esValido("12.345.678-5"));
        assertTrue(RutUtil.esValido("11111111-1"));
        assertTrue(RutUtil.esValido("22222222-2"));
    }

    @Test
    void rechazaDigitoVerificadorIncorrecto() {
        assertFalse(RutUtil.esValido("12.345.678-9"));
    }

    @Test
    void rechazaTextoVacioONulo() {
        assertFalse(RutUtil.esValido(""));
        assertFalse(RutUtil.esValido(null));
    }
}
