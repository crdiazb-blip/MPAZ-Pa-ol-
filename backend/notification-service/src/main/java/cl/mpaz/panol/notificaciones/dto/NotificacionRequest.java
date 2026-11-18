package cl.mpaz.panol.notificaciones.dto;

import cl.mpaz.panol.notificaciones.model.TipoNotificacion;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;

public record NotificacionRequest(
        @NotBlank(message = "El destinatario es obligatorio") String destinatarioRut,
        Long solicitudId,
        @NotNull(message = "El tipo es obligatorio") TipoNotificacion tipo,
        @NotBlank(message = "El título es obligatorio") @Size(max = 120) String titulo,
        @NotBlank(message = "El mensaje es obligatorio") @Size(max = 500) String mensaje,
        Instant programadaPara
) {
}
