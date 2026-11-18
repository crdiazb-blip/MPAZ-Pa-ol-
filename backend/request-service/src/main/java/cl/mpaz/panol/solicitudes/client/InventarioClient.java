package cl.mpaz.panol.solicitudes.client;

import cl.mpaz.panol.solicitudes.exception.ApiException;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.List;

/** Cliente REST del inventory-service. Los errores del inventario se propagan con su mensaje. */
@Component
public class InventarioClient {

    private final RestClient rest;
    private final ObjectMapper mapper;

    public InventarioClient(@Qualifier("inventarioRest") RestClient rest, ObjectMapper mapper) {
        this.rest = rest;
        this.mapper = mapper;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Equipo(Long id, String nombre, int stockDisponible, boolean reservable) {
    }

    public record Item(Long equipoId, int cantidad) {
    }

    public Equipo obtener(Long equipoId) {
        return ejecutar(() -> rest.get().uri("/equipos/{id}", equipoId).retrieve().body(Equipo.class));
    }

    /** Descuenta el stock de todos los items o falla sin descontar nada (409 si no alcanza). */
    public void reservar(List<Item> items) {
        ejecutar(() -> {
            rest.post().uri("/internal/equipos/reservar").body(items).retrieve().toBodilessEntity();
            return null;
        });
    }

    public void liberar(List<Item> items) {
        ejecutar(() -> {
            rest.post().uri("/internal/equipos/liberar").body(items).retrieve().toBodilessEntity();
            return null;
        });
    }

    private <T> T ejecutar(java.util.function.Supplier<T> llamada) {
        try {
            return llamada.get();
        } catch (RestClientResponseException e) {
            HttpStatusCode status = e.getStatusCode();
            throw new ApiException(status.is5xxServerError() ? HttpStatus.BAD_GATEWAY : HttpStatus.valueOf(status.value()),
                    extraerMensaje(e));
        } catch (ResourceAccessException e) {
            throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE, "El servicio de inventario no responde.");
        }
    }

    private String extraerMensaje(RestClientResponseException e) {
        try {
            JsonNode nodo = mapper.readTree(e.getResponseBodyAsString());
            if (nodo.hasNonNull("message")) {
                return nodo.get("message").asText();
            }
        } catch (Exception ignorado) {
            // se usa el mensaje genérico
        }
        return "Error al consultar el inventario.";
    }
}
