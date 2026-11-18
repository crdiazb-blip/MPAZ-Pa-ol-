package cl.mpaz.panol.inventario.dto;

import cl.mpaz.panol.inventario.model.Categoria;
import cl.mpaz.panol.inventario.model.EstadoEquipo;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record EquipoRequest(
        @NotBlank(message = "El nombre es obligatorio") @Size(max = 120) String nombre,
        @NotBlank(message = "La marca es obligatoria") @Size(max = 80) String marca,
        @NotBlank(message = "El modelo es obligatorio") @Size(max = 80) String modelo,
        @Size(max = 300) String descripcion,
        @NotNull(message = "La categoría es obligatoria") Categoria categoria,
        @NotNull(message = "El stock total es obligatorio") @Min(value = 0, message = "El stock no puede ser negativo") Integer stockTotal,
        @Min(value = 0, message = "El stock disponible no puede ser negativo") Integer stockDisponible,
        EstadoEquipo estado,
        @NotBlank(message = "La ubicación es obligatoria") @Size(max = 120) String ubicacion
) {
}
