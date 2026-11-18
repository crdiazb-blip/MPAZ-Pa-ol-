package cl.mpaz.panol.inventario.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/** Cantidad de un equipo a reservar o liberar (uso interno del request-service). */
public record ItemStock(
        @NotNull Long equipoId,
        @Min(value = 1, message = "La cantidad debe ser al menos 1") int cantidad) {
}
