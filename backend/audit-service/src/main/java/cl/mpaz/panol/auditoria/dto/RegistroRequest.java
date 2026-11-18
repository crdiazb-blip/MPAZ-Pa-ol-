package cl.mpaz.panol.auditoria.dto;

import cl.mpaz.panol.auditoria.model.Hito;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RegistroRequest(
        @NotNull(message = "La solicitud es obligatoria") Long solicitudId,
        @NotBlank(message = "El solicitante es obligatorio") String solicitanteRut,
        @NotNull(message = "El hito es obligatorio") Hito hito,
        @NotBlank(message = "El actor es obligatorio") String actorRut,
        @NotBlank(message = "El nombre del actor es obligatorio") String actorNombre,
        @Size(max = 500) String detalle
) {
}
