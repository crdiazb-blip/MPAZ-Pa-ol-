package cl.mpaz.panol.notificaciones.controller;

import cl.mpaz.panol.notificaciones.dto.NotificacionResponse;
import cl.mpaz.panol.notificaciones.model.TipoNotificacion;
import cl.mpaz.panol.notificaciones.service.NotificacionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(NotificacionController.class)
class NotificacionControllerTest {

    @Autowired
    MockMvc mvc;
    @MockitoBean
    NotificacionService service;

    private NotificacionResponse respuesta() {
        return new NotificacionResponse(1L, "12345678-5", 10L, TipoNotificacion.SOLICITUD_ACEPTADA, "Aceptada",
                "Tu solicitud fue aceptada", Instant.parse("2026-09-01T15:00:00Z"), null,
                Instant.parse("2026-09-01T15:00:00Z"), false);
    }

    @Test
    void listaLasNotificacionesDelUsuario() throws Exception {
        when(service.misNotificaciones("12345678-5", false)).thenReturn(List.of(respuesta()));

        mvc.perform(get("/notificaciones").header("X-User-Rut", "12345678-5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].tipo").value("SOLICITUD_ACEPTADA"));
    }

    @Test
    void crearInternaRetorna201() throws Exception {
        when(service.crear(any())).thenReturn(respuesta());

        mvc.perform(post("/internal/notificaciones").contentType(MediaType.APPLICATION_JSON).content("""
                        {"destinatarioRut":"12345678-5","solicitudId":10,"tipo":"SOLICITUD_ACEPTADA",
                         "titulo":"Aceptada","mensaje":"Tu solicitud fue aceptada"}"""))
                .andExpect(status().isCreated());
    }

    @Test
    void crearSinMensajeRetorna400() throws Exception {
        mvc.perform(post("/internal/notificaciones").contentType(MediaType.APPLICATION_JSON).content("""
                        {"destinatarioRut":"12345678-5","tipo":"SOLICITUD_ACEPTADA","titulo":"x"}"""))
                .andExpect(status().isBadRequest());
    }
}
