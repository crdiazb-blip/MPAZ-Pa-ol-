package cl.mpaz.panol.auth.service;

import cl.mpaz.panol.auth.dto.LoginRequest;
import cl.mpaz.panol.auth.dto.LoginResponse;
import cl.mpaz.panol.auth.dto.UsuarioDto;
import cl.mpaz.panol.auth.exception.ApiException;
import cl.mpaz.panol.auth.model.Rol;
import cl.mpaz.panol.auth.model.TokenRevocado;
import cl.mpaz.panol.auth.model.Usuario;
import cl.mpaz.panol.auth.repository.TokenRevocadoRepository;
import cl.mpaz.panol.auth.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    UsuarioRepository usuarios;
    @Mock
    TokenRevocadoRepository revocados;

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(4);
    private final JwtService jwt = new JwtService("una-clave-de-prueba-con-mas-de-32-caracteres!!", 60);
    private AuthService service;
    private Usuario docente;

    @BeforeEach
    void preparar() {
        service = new AuthService(usuarios, revocados, encoder, jwt);
        docente = Usuario.builder().id(2L).rut("12345678-5").nombre("Docente Demo").cargo("Docente")
                .correo("d@mpaz.test").passwordHash(encoder.encode("1234")).rol(Rol.SOLICITANTE).activo(true).build();
    }

    @Test
    void loginCorrectoEntregaTokenYPerfil() {
        when(usuarios.findByRut("12345678-5")).thenReturn(Optional.of(docente));

        LoginResponse r = service.login(new LoginRequest("12.345.678-5", "1234"));

        assertNotNull(r.token());
        assertEquals(Rol.SOLICITANTE, r.usuario().rol());
        assertEquals("Docente Demo", r.usuario().nombre());
    }

    @Test
    void loginConClaveIncorrectaRetorna401() {
        when(usuarios.findByRut("12345678-5")).thenReturn(Optional.of(docente));

        ApiException e = assertThrows(ApiException.class,
                () -> service.login(new LoginRequest("12345678-5", "mala")));

        assertEquals(HttpStatus.UNAUTHORIZED, e.getStatus());
    }

    @Test
    void loginConUsuarioInexistenteRetorna401() {
        when(usuarios.findByRut(any())).thenReturn(Optional.empty());

        ApiException e = assertThrows(ApiException.class,
                () -> service.login(new LoginRequest("99999999-9", "1234")));

        assertEquals(HttpStatus.UNAUTHORIZED, e.getStatus());
    }

    @Test
    void loginDeUsuarioInactivoRetorna401() {
        docente.setActivo(false);
        when(usuarios.findByRut("12345678-5")).thenReturn(Optional.of(docente));

        assertThrows(ApiException.class, () -> service.login(new LoginRequest("12345678-5", "1234")));
    }

    @Test
    void validarTokenVigenteDevuelveElUsuario() {
        when(usuarios.findByRut("12345678-5")).thenReturn(Optional.of(docente));
        String token = service.login(new LoginRequest("12345678-5", "1234")).token();
        when(revocados.existsById(any())).thenReturn(false);

        UsuarioDto u = service.validar(token);

        assertEquals("12345678-5", u.rut());
    }

    @Test
    void validarTokenCerradoConLogoutRetorna401() {
        when(usuarios.findByRut("12345678-5")).thenReturn(Optional.of(docente));
        String token = service.login(new LoginRequest("12345678-5", "1234")).token();
        when(revocados.existsById(any())).thenReturn(true);

        ApiException e = assertThrows(ApiException.class, () -> service.validar(token));

        assertEquals(HttpStatus.UNAUTHORIZED, e.getStatus());
    }

    @Test
    void logoutGuardaElTokenComoRevocado() {
        when(usuarios.findByRut("12345678-5")).thenReturn(Optional.of(docente));
        String token = service.login(new LoginRequest("12345678-5", "1234")).token();
        when(revocados.existsById(any())).thenReturn(false);

        service.logout(token);

        verify(revocados).save(any(TokenRevocado.class));
    }

    @Test
    void validarTokenBasuraRetorna401() {
        assertThrows(ApiException.class, () -> service.validar("no-es-un-token"));
    }
}
