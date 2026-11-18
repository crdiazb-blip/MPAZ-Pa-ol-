package cl.mpaz.panol.solicitudes.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RechazoRequest(
        @NotBlank(message = "La justificación del rechazo es obligatoria")
        @Size(min = 5, max = 500, message = "La justificación debe tener entre 5 y 500 caracteres") String comentario
) {
}
