package cl.mpaz.panol.auth.controller;

import cl.mpaz.panol.auth.dto.LoginResponse;
import cl.mpaz.panol.auth.dto.UsuarioDto;
import cl.mpaz.panol.auth.exception.ApiException;
import cl.mpaz.panol.auth.model.Rol;
import cl.mpaz.panol.auth.repository.UsuarioRepository;
import cl.mpaz.panol.auth.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({AuthController.class, UsuarioController.class})
class AuthControllerTest {

    @Autowired
    MockMvc mvc;
    @MockitoBean
    AuthService authService;
    @MockitoBean
    UsuarioRepository usuarios;

    @Test
    void loginValidoRetorna200ConToken() throws Exception {
        UsuarioDto u = new UsuarioDto(1L, "11111111-1", "Admin", "TI", "a@mpaz.test", Rol.ADMIN);
        when(authService.login(any())).thenReturn(new LoginResponse("tok", Instant.now(), u));

        mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"rut\":\"11111111-1\",\"password\":\"admin123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("tok"))
                .andExpect(jsonPath("$.usuario.rol").value("ADMIN"));
    }

    @Test
    void loginSinCamposRetorna400() throws Exception {
        mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"rut\":\"\",\"password\":\"\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void loginIncorrectoRetorna401() throws Exception {
        when(authService.login(any())).thenThrow(new ApiException(HttpStatus.UNAUTHORIZED, "RUT o contraseña incorrectos."));

        mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"rut\":\"11111111-1\",\"password\":\"x\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void validateSinTokenRetorna401() throws Exception {
        mvc.perform(get("/auth/validate")).andExpect(status().isUnauthorized());
    }

    @Test
    void logoutConTokenRetorna204() throws Exception {
        mvc.perform(post("/auth/logout").header("Authorization", "Bearer abc"))
                .andExpect(status().isNoContent());
    }

    @Test
    void listarUsuariosComoSolicitanteRetorna403() throws Exception {
        mvc.perform(get("/usuarios").header("X-User-Rol", "SOLICITANTE"))
                .andExpect(status().isForbidden());
    }

    @Test
    void listarUsuariosComoAdminRetorna200() throws Exception {
        mvc.perform(get("/usuarios").header("X-User-Rol", "ADMIN"))
                .andExpect(status().isOk());
    }
}
