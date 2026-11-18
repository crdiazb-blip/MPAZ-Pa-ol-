package cl.mpaz.panol.solicitudes.controller;

import cl.mpaz.panol.solicitudes.dto.SolicitudResponse;
import cl.mpaz.panol.solicitudes.exception.ApiException;
import cl.mpaz.panol.solicitudes.model.EstadoSolicitud;
import cl.mpaz.panol.solicitudes.security.UsuarioActual;
import cl.mpaz.panol.solicitudes.service.SolicitudService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SolicitudController.class)
class SolicitudControllerTest {

    private static final String BODY = """
            {"sala":"Sala 1","fecha":"2026-09-02","horaInicio":"08:00","horaDevolucion":"09:00",
             "items":[{"equipoId":1,"cantidad":2}]}""";

    @Autowired
    MockMvc mvc;
    @MockitoBean
    SolicitudService service;

    private SolicitudResponse respuesta(EstadoSolicitud estado) {
        return new SolicitudResponse(10L, "12345678-5", "Docente Demo", "Docente", "d@mpaz.test", "Sala 1",
                "2026-09-02", "08:00", "09:00", estado, null, null, null, null, null, List.of());
    }

    @Test
    void sinCabecerasDeUsuarioRetorna401() throws Exception {
        mvc.perform(get("/solicitudes")).andExpect(status().isUnauthorized());
    }

    @Test
    void crearEntregaLosDatosDelUsuarioDecodificados() throws Exception {
        when(service.crear(any(), any())).thenReturn(respuesta(EstadoSolicitud.PENDIENTE));

        mvc.perform(post("/solicitudes")
                        .header("X-User-Rut", "12345678-5")
                        .header("X-User-Rol", "SOLICITANTE")
                        .header("X-User-Nombre", "Mar%C3%ADa+Pe%C3%B1a")
                        .contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado").value("PENDIENTE"));

        ArgumentCaptor<UsuarioActual> captor = ArgumentCaptor.forClass(UsuarioActual.class);
        verify(service).crear(captor.capture(), any());
        assertEquals("María Peña", captor.getValue().nombre());
    }

    @Test
    void crearConCarritoVacioRetorna400() throws Exception {
        mvc.perform(post("/solicitudes")
                        .header("X-User-Rut", "12345678-5").header("X-User-Rol", "SOLICITANTE")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sala\":\"Sala 1\",\"fecha\":\"2026-09-02\",\"horaInicio\":\"08:00\","
                                + "\"horaDevolucion\":\"09:00\",\"items\":[]}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void crearConFormatoDeFechaInvalidoRetorna400() throws Exception {
        mvc.perform(post("/solicitudes")
                        .header("X-User-Rut", "12345678-5").header("X-User-Rol", "SOLICITANTE")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(BODY.replace("2026-09-02", "02/09/2026")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void elErrorDeNegocioDelServicioSeTraduceAHttp() throws Exception {
        when(service.crear(any(), any()))
                .thenThrow(new ApiException(HttpStatus.BAD_REQUEST, "Debes reservar con al menos 3 horas de anticipación."));

        mvc.perform(post("/solicitudes")
                        .header("X-User-Rut", "12345678-5").header("X-User-Rol", "SOLICITANTE")
                        .contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Debes reservar con al menos 3 horas de anticipación."));
    }

    @Test
    void rechazarSinComentarioRetorna400() throws Exception {
        mvc.perform(patch("/solicitudes/10/rechazar")
                        .header("X-User-Rut", "11111111-1").header("X-User-Rol", "ADMIN")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"comentario\":\"\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void rechazarConComentarioLlamaAlServicio() throws Exception {
        when(service.rechazar(any(), eq(10L), eq("No hay stock suficiente"))).thenReturn(respuesta(EstadoSolicitud.RECHAZADA));

        mvc.perform(patch("/solicitudes/10/rechazar")
                        .header("X-User-Rut", "11111111-1").header("X-User-Rol", "ADMIN")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"comentario\":\"No hay stock suficiente\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("RECHAZADA"));
    }

    @Test
    void aceptarLlamaAlServicio() throws Exception {
        when(service.aceptar(any(), eq(10L))).thenReturn(respuesta(EstadoSolicitud.ACEPTADA));

        mvc.perform(patch("/solicitudes/10/aceptar")
                        .header("X-User-Rut", "11111111-1").header("X-User-Rol", "ADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("ACEPTADA"));
    }
}
