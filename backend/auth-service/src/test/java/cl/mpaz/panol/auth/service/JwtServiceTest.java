package cl.mpaz.panol.auth.service;

import cl.mpaz.panol.auth.model.Rol;
import cl.mpaz.panol.auth.model.Usuario;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private static final String SECRETO = "una-clave-de-prueba-con-mas-de-32-caracteres!!";

    private final JwtService jwt = new JwtService(SECRETO, 60);

    private Usuario admin() {
        return Usuario.builder().id(7L).rut("11111111-1").nombre("Admin").cargo("TI")
                .correo("a@mpaz.test").passwordHash("x").rol(Rol.ADMIN).build();
    }

    @Test
    void tokenGeneradoSePuedeLeer() {
        JwtService.Emitido emitido = jwt.generar(admin());

        JwtService.Datos datos = jwt.leer(emitido.token()).orElseThrow();

        assertEquals("11111111-1", datos.rut());
        assertEquals(7L, datos.usuarioId());
        assertEquals(Rol.ADMIN, datos.rol());
        assertNotNull(datos.jti());
    }

    @Test
    void tokenAlteradoEsRechazado() {
        String token = jwt.generar(admin()).token();
        assertTrue(jwt.leer(token + "x").isEmpty());
    }

    @Test
    void tokenFirmadoConOtraClaveEsRechazado() {
        JwtService otro = new JwtService("otra-clave-distinta-con-mas-de-32-caracteres!!", 60);
        assertTrue(jwt.leer(otro.generar(admin()).token()).isEmpty());
    }

    @Test
    void tokenExpiradoEsRechazado() {
        JwtService corto = new JwtService(SECRETO, -1);
        assertTrue(jwt.leer(corto.generar(admin()).token()).isEmpty());
    }

    @Test
    void claveCortaNoEsPermitida() {
        assertThrows(IllegalStateException.class, () -> new JwtService("corta", 60));
    }
}
