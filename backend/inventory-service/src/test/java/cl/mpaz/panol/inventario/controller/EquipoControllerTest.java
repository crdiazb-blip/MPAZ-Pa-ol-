package cl.mpaz.panol.inventario.controller;

import cl.mpaz.panol.inventario.dto.EquipoResponse;
import cl.mpaz.panol.inventario.model.Categoria;
import cl.mpaz.panol.inventario.model.EstadoEquipo;
import cl.mpaz.panol.inventario.service.EquipoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({EquipoController.class, InternalStockController.class})
class EquipoControllerTest {

    private static final String JSON_VALIDO = """
            {"nombre":"Parlante","marca":"JBL","modelo":"Box","categoria":"PARLANTE",
             "stockTotal":4,"ubicacion":"Estante C"}""";

    @Autowired
    MockMvc mvc;
    @MockitoBean
    EquipoService service;

    private EquipoResponse respuesta() {
        return new EquipoResponse(1L, "Parlante", "JBL", "Box", null, Categoria.PARLANTE, 4, 4,
                EstadoEquipo.DISPONIBLE, "Estante C", true);
    }

    @Test
    void cualquierUsuarioPuedeListarElCatalogo() throws Exception {
        when(service.listar(null, false)).thenReturn(List.of(respuesta()));

        mvc.perform(get("/equipos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre").value("Parlante"))
                .andExpect(jsonPath("$[0].reservable").value(true));
    }

    @Test
    void crearComoSolicitanteRetorna403() throws Exception {
        mvc.perform(post("/equipos").header("X-User-Rol", "SOLICITANTE")
                        .contentType(MediaType.APPLICATION_JSON).content(JSON_VALIDO))
                .andExpect(status().isForbidden());
    }

    @Test
    void crearSinRolRetorna403() throws Exception {
        mvc.perform(post("/equipos").contentType(MediaType.APPLICATION_JSON).content(JSON_VALIDO))
                .andExpect(status().isForbidden());
    }

    @Test
    void crearComoAdminRetorna201() throws Exception {
        when(service.crear(any())).thenReturn(respuesta());

        mvc.perform(post("/equipos").header("X-User-Rol", "ADMIN")
                        .contentType(MediaType.APPLICATION_JSON).content(JSON_VALIDO))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void crearConDatosInvalidosRetorna400() throws Exception {
        mvc.perform(post("/equipos").header("X-User-Rol", "ADMIN")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"\",\"stockTotal\":-1}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void darDeBajaComoSolicitanteRetorna403() throws Exception {
        mvc.perform(delete("/equipos/1").header("X-User-Rol", "SOLICITANTE"))
                .andExpect(status().isForbidden());
    }

    @Test
    void darDeBajaComoAdminRetorna204() throws Exception {
        mvc.perform(delete("/equipos/1").header("X-User-Rol", "ADMIN"))
                .andExpect(status().isNoContent());
        verify(service).darDeBaja(1L);
    }

    @Test
    void cambiarEstadoComoAdmin() throws Exception {
        when(service.cambiarEstado(1L, EstadoEquipo.MANTENIMIENTO)).thenReturn(respuesta());

        mvc.perform(patch("/equipos/1/estado").header("X-User-Rol", "ADMIN")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"estado\":\"MANTENIMIENTO\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void reservarInternoRetorna204() throws Exception {
        mvc.perform(post("/internal/equipos/reservar").contentType(MediaType.APPLICATION_JSON)
                        .content("[{\"equipoId\":1,\"cantidad\":2}]"))
                .andExpect(status().isNoContent());
    }
}
