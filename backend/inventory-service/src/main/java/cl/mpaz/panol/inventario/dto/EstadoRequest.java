package cl.mpaz.panol.inventario.dto;

import cl.mpaz.panol.inventario.model.EstadoEquipo;
import jakarta.validation.constraints.NotNull;

public record EstadoRequest(@NotNull(message = "El estado es obligatorio") EstadoEquipo estado) {
}
