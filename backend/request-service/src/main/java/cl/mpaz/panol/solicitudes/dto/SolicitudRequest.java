package cl.mpaz.panol.solicitudes.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;

/** fecha en formato yyyy-MM-dd; horas en formato HH:mm. */
public record SolicitudRequest(
        @NotBlank(message = "La sala es obligatoria") @Size(max = 80) String sala,
        @NotBlank(message = "La fecha es obligatoria")
        @Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}", message = "La fecha debe tener formato yyyy-MM-dd") String fecha,
        @NotBlank(message = "La hora de inicio es obligatoria")
        @Pattern(regexp = "\\d{2}:\\d{2}", message = "La hora de inicio debe tener formato HH:mm") String horaInicio,
        @NotBlank(message = "La hora de devolución es obligatoria")
        @Pattern(regexp = "\\d{2}:\\d{2}", message = "La hora de devolución debe tener formato HH:mm") String horaDevolucion,
        @NotEmpty(message = "El carrito no puede estar vacío") List<@Valid ItemRequest> items
) {
}
