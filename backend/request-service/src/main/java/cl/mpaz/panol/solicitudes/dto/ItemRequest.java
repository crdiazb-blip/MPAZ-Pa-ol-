package cl.mpaz.panol.solicitudes.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record ItemRequest(
        @NotNull(message = "El equipo es obligatorio") Long equipoId,
        @Min(value = 1, message = "La cantidad debe ser al menos 1") int cantidad
) {
}
