package cl.mpaz.panol.inventario.dto;

import cl.mpaz.panol.inventario.model.Categoria;
import cl.mpaz.panol.inventario.model.Equipo;
import cl.mpaz.panol.inventario.model.EstadoEquipo;

public record EquipoResponse(
        Long id,
        String nombre,
        String marca,
        String modelo,
        String descripcion,
        Categoria categoria,
        int stockTotal,
        int stockDisponible,
        EstadoEquipo estado,
        String ubicacion,
        boolean reservable
) {

    public static EquipoResponse de(Equipo e) {
        return new EquipoResponse(e.getId(), e.getNombre(), e.getMarca(), e.getModelo(), e.getDescripcion(),
                e.getCategoria(), e.getStockTotal(), e.getStockDisponible(), e.getEstado(), e.getUbicacion(),
                e.isReservable());
    }
}
