package cl.mpaz.panol.gateway.controller;

import cl.mpaz.panol.gateway.client.DownstreamClient;
import cl.mpaz.panol.gateway.client.TokenValidator;
import cl.mpaz.panol.gateway.config.RutaResolver;
import cl.mpaz.panol.gateway.exception.GatewayException;
import cl.mpaz.panol.gateway.model.UsuarioToken;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.net.URI;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProxyController.class)
@Import(RutaResolver.class)
class ProxyControllerTest {

    @Autowired
    MockMvc mvc;
    @MockitoBean
    TokenValidator validador;
    @MockitoBean
    DownstreamClient downstream;

    @BeforeEach
    void preparar() {
        when(downstream.enviar(any(), any(), any(), any())).thenReturn(
                ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body("[]".getBytes(StandardCharsets.UTF_8)));
    }

    private UsuarioToken admin() {
        return new UsuarioToken(1L, "11111111-1", "María Peña", "Encargado TI", "ti@mpaz.test", "ADMIN");
    }

    @Test
    void loginEsPublicoYNoValidaToken() throws Exception {
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"rut\":\"1\",\"password\":\"x\"}"))
                .andExpect(status().isOk());

        verifyNoInteractions(validador);
        ArgumentCaptor<URI> uri = ArgumentCaptor.forClass(URI.class);
        verify(downstream).enviar(eq(HttpMethod.POST), uri.capture(), any(), any());
        assertEquals("http://auth-service:8081/auth/login", uri.getValue().toString());
    }

    @Test
    void sinTokenRetorna401() throws Exception {
        when(validador.validar(null)).thenThrow(new GatewayException(HttpStatus.UNAUTHORIZED, "Falta el token"));

        mvc.perform(get("/api/equipos")).andExpect(status().isUnauthorized());

        verifyNoInteractions(downstream);
    }

    @Test
    void conTokenValidoReenviaConLaIdentidadDelUsuario() throws Exception {
        when(validador.validar("Bearer abc")).thenReturn(admin());

        mvc.perform(get("/api/equipos?soloDisponibles=true").header("Authorization", "Bearer abc"))
                .andExpect(status().isOk())
                .andExpect(content().string("[]"));

        ArgumentCaptor<URI> uri = ArgumentCaptor.forClass(URI.class);
        ArgumentCaptor<HttpHeaders> headers = ArgumentCaptor.forClass(HttpHeaders.class);
        verify(downstream).enviar(eq(HttpMethod.GET), uri.capture(), headers.capture(), any());
        assertEquals("http://inventory-service:8082/equipos?soloDisponibles=true", uri.getValue().toString());
        assertEquals("ADMIN", headers.getValue().getFirst("X-User-Rol"));
        assertEquals("11111111-1", headers.getValue().getFirst("X-User-Rut"));
        assertEquals("Mar%C3%ADa+Pe%C3%B1a", headers.getValue().getFirst("X-User-Nombre"));
        assertNull(headers.getValue().getFirst("Authorization"));
    }

    @Test
    void descartaLasCabecerasDeIdentidadEnviadasPorElCliente() throws Exception {
        when(validador.validar("Bearer abc"))
                .thenReturn(new UsuarioToken(2L, "12345678-5", "Docente", "Docente", "d@mpaz.test", "SOLICITANTE"));

        mvc.perform(get("/api/equipos").header("Authorization", "Bearer abc").header("X-User-Rol", "ADMIN"))
                .andExpect(status().isOk());

        ArgumentCaptor<HttpHeaders> headers = ArgumentCaptor.forClass(HttpHeaders.class);
        verify(downstream).enviar(any(), any(), headers.capture(), any());
        assertEquals("SOLICITANTE", headers.getValue().getFirst("X-User-Rol"));
    }

    @Test
    void reenviaElPatchConSuCuerpo() throws Exception {
        when(validador.validar("Bearer abc")).thenReturn(admin());

        mvc.perform(patch("/api/solicitudes/5/rechazar").header("Authorization", "Bearer abc")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"comentario\":\"Sin stock\"}"))
                .andExpect(status().isOk());

        ArgumentCaptor<byte[]> cuerpo = ArgumentCaptor.forClass(byte[].class);
        verify(downstream).enviar(eq(HttpMethod.PATCH), any(), any(), cuerpo.capture());
        assertEquals("{\"comentario\":\"Sin stock\"}", new String(cuerpo.getValue(), StandardCharsets.UTF_8));
    }

    @Test
    void elLogoutReenviaElTokenAlServicioDeAutenticacion() throws Exception {
        when(validador.validar("Bearer abc")).thenReturn(admin());

        mvc.perform(post("/api/auth/logout").header("Authorization", "Bearer abc")).andExpect(status().isOk());

        ArgumentCaptor<HttpHeaders> headers = ArgumentCaptor.forClass(HttpHeaders.class);
        verify(downstream).enviar(eq(HttpMethod.POST), any(), headers.capture(), any());
        assertEquals("Bearer abc", headers.getValue().getFirst("Authorization"));
    }

    @Test
    void losEndpointsInternosNoSeExponen() throws Exception {
        mvc.perform(post("/api/internal/equipos/reservar").header("Authorization", "Bearer abc"))
                .andExpect(status().isNotFound());

        verifyNoInteractions(downstream);
    }

    @Test
    void esPublicaSoloElLogin() {
        assertTrue(ProxyController.esPublica(HttpMethod.POST, "/auth/login"));
        assertFalse(ProxyController.esPublica(HttpMethod.GET, "/auth/login"));
        assertFalse(ProxyController.esPublica(HttpMethod.POST, "/auth/logout"));
        assertFalse(ProxyController.esPublica(HttpMethod.GET, "/equipos"));
    }
}
