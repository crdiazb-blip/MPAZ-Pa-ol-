package cl.mpaz.panol.auditoria.controller;

import cl.mpaz.panol.auditoria.dto.RegistroResponse;
import cl.mpaz.panol.auditoria.model.Hito;
import cl.mpaz.panol.auditoria.service.BitacoraService;
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

@WebMvcTest(BitacoraController.class)
class BitacoraControllerTest {

    @Autowired
    MockMvc mvc;
    @MockitoBean
    BitacoraService service;

    private RegistroResponse respuesta() {
        return new RegistroResponse(1L, 5L, "12345678-5", Hito.APROBADO, "11111111-1",
                "Encargado TI", null, Instant.parse("2026-09-01T12:00:00Z"));
    }

    @Test
    void listarEntregaLosRegistros() throws Exception {
        when(service.listar("ADMIN", "11111111-1", 5L)).thenReturn(List.of(respuesta()));

        mvc.perform(get("/bitacora?solicitudId=5").header("X-User-Rol", "ADMIN").header("X-User-Rut", "11111111-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].hito").value("APROBADO"));
    }

    @Test
    void registrarInternoRetorna201() throws Exception {
        when(service.registrar(any())).thenReturn(respuesta());

        mvc.perform(post("/internal/bitacora").contentType(MediaType.APPLICATION_JSON).content("""
                        {"solicitudId":5,"solicitanteRut":"12345678-5","hito":"APROBADO",
                         "actorRut":"11111111-1","actorNombre":"Encargado TI"}"""))
                .andExpect(status().isCreated());
    }

    @Test
    void registrarSinHitoRetorna400() throws Exception {
        mvc.perform(post("/internal/bitacora").contentType(MediaType.APPLICATION_JSON).content("""
                        {"solicitudId":5,"solicitanteRut":"12345678-5","actorRut":"1","actorNombre":"x"}"""))
                .andExpect(status().isBadRequest());
    }
}
