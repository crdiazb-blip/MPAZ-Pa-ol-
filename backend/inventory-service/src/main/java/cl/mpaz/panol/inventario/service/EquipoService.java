package cl.mpaz.panol.inventario.service;

import cl.mpaz.panol.inventario.dto.EquipoRequest;
import cl.mpaz.panol.inventario.dto.EquipoResponse;
import cl.mpaz.panol.inventario.dto.ItemStock;
import cl.mpaz.panol.inventario.exception.ApiException;
import cl.mpaz.panol.inventario.model.Categoria;
import cl.mpaz.panol.inventario.model.Equipo;
import cl.mpaz.panol.inventario.model.EstadoEquipo;
import cl.mpaz.panol.inventario.repository.EquipoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class EquipoService {

    private final EquipoRepository repo;

    public EquipoService(EquipoRepository repo) {
        this.repo = repo;
    }

    @Transactional(readOnly = true)
    public List<EquipoResponse> listar(Categoria categoria, boolean soloDisponibles) {
        return repo.findByActivoTrueOrderByNombreAsc().stream()
                .filter(e -> categoria == null || e.getCategoria() == categoria)
                .filter(e -> !soloDisponibles || e.isReservable())
                .map(EquipoResponse::de)
                .toList();
    }

    @Transactional(readOnly = true)
    public EquipoResponse obtener(Long id) {
        return EquipoResponse.de(buscar(id));
    }

    @Transactional
    public EquipoResponse crear(EquipoRequest req) {
        int total = req.stockTotal();
        int disponible = req.stockDisponible() != null ? req.stockDisponible() : total;
        if (disponible > total) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "El stock disponible no puede superar el stock total.");
        }
        Equipo equipo = Equipo.builder()
                .nombre(req.nombre().trim())
                .marca(req.marca().trim())
                .modelo(req.modelo().trim())
                .descripcion(req.descripcion())
                .categoria(req.categoria())
                .stockTotal(total)
                .stockDisponible(disponible)
                .estado(req.estado() != null ? req.estado() : EstadoEquipo.DISPONIBLE)
                .ubicacion(req.ubicacion().trim())
                .activo(true)
                .build();
        ajustarEstadoPorStock(equipo);
        return EquipoResponse.de(repo.save(equipo));
    }

    @Transactional
    public EquipoResponse actualizar(Long id, EquipoRequest req) {
        Equipo e = buscar(id);
        e.setNombre(req.nombre().trim());
        e.setMarca(req.marca().trim());
        e.setModelo(req.modelo().trim());
        e.setDescripcion(req.descripcion());
        e.setCategoria(req.categoria());
        e.setUbicacion(req.ubicacion().trim());
        if (req.estado() != null) {
            e.setEstado(req.estado());
        }
        aplicarStockTotal(e, req.stockTotal());
        ajustarEstadoPorStock(e);
        return EquipoResponse.de(repo.save(e));
    }

    @Transactional
    public EquipoResponse cambiarEstado(Long id, EstadoEquipo estado) {
        Equipo e = buscar(id);
        e.setEstado(estado);
        return EquipoResponse.de(repo.save(e));
    }

    @Transactional
    public EquipoResponse cambiarStock(Long id, int nuevoTotal) {
        Equipo e = buscar(id);
        aplicarStockTotal(e, nuevoTotal);
        ajustarEstadoPorStock(e);
        return EquipoResponse.de(repo.save(e));
    }

    /** Baja lógica: el equipo deja de aparecer en el catálogo pero se conserva su historial. */
    @Transactional
    public void darDeBaja(Long id) {
        Equipo e = buscar(id);
        if (e.getStockDisponible() < e.getStockTotal()) {
            throw new ApiException(HttpStatus.CONFLICT,
                    "No se puede dar de baja: hay unidades de " + e.getNombre() + " en préstamo o reservadas.");
        }
        e.setActivo(false);
        e.setEstado(EstadoEquipo.FUERA_DE_SERVICIO);
        repo.save(e);
    }

    /** Descuenta stock de varios equipos en una sola transacción: o se reserva todo o nada. */
    @Transactional
    public void reservar(List<ItemStock> items) {
        Map<Long, Integer> cantidades = agrupar(items);
        for (Equipo e : bloquear(cantidades)) {
            int cantidad = cantidades.get(e.getId());
            if (!e.isActivo() || e.getEstado() == EstadoEquipo.MANTENIMIENTO
                    || e.getEstado() == EstadoEquipo.FUERA_DE_SERVICIO) {
                throw new ApiException(HttpStatus.CONFLICT, e.getNombre() + " no está disponible para préstamo.");
            }
            if (cantidad > e.getStockDisponible()) {
                throw new ApiException(HttpStatus.CONFLICT,
                        "Stock insuficiente de " + e.getNombre() + ": quedan " + e.getStockDisponible() + ".");
            }
            e.setStockDisponible(e.getStockDisponible() - cantidad);
            ajustarEstadoPorStock(e);
            repo.save(e);
        }
    }

    /** Devuelve stock al inventario (devolución o cancelación de una reserva). */
    @Transactional
    public void liberar(List<ItemStock> items) {
        Map<Long, Integer> cantidades = agrupar(items);
        for (Equipo e : bloquear(cantidades)) {
            int nuevo = Math.min(e.getStockTotal(), e.getStockDisponible() + cantidades.get(e.getId()));
            e.setStockDisponible(nuevo);
            ajustarEstadoPorStock(e);
            repo.save(e);
        }
    }

    private Equipo buscar(Long id) {
        return repo.findByIdAndActivoTrue(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Equipo " + id + " no encontrado."));
    }

    private Map<Long, Integer> agrupar(List<ItemStock> items) {
        if (items == null || items.isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Debe indicar al menos un equipo.");
        }
        Map<Long, Integer> cantidades = new LinkedHashMap<>();
        for (ItemStock item : items) {
            if (item.cantidad() < 1) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "La cantidad debe ser al menos 1.");
            }
            cantidades.merge(item.equipoId(), item.cantidad(), Integer::sum);
        }
        return cantidades;
    }

    private List<Equipo> bloquear(Map<Long, Integer> cantidades) {
        List<Equipo> equipos = repo.bloquearPorIds(cantidades.keySet());
        if (equipos.size() != cantidades.size()) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Alguno de los equipos solicitados no existe.");
        }
        return equipos;
    }

    private void aplicarStockTotal(Equipo e, int nuevoTotal) {
        int nuevoDisponible = e.getStockDisponible() + (nuevoTotal - e.getStockTotal());
        if (nuevoDisponible < 0) {
            throw new ApiException(HttpStatus.CONFLICT,
                    "No se puede reducir el stock: hay " + (e.getStockTotal() - e.getStockDisponible())
                            + " unidades en préstamo.");
        }
        e.setStockTotal(nuevoTotal);
        e.setStockDisponible(nuevoDisponible);
    }

    /** Sin unidades libres el equipo pasa a RESERVADO; al recuperar stock vuelve a DISPONIBLE. */
    private void ajustarEstadoPorStock(Equipo e) {
        if (e.getStockDisponible() == 0 && e.getEstado() == EstadoEquipo.DISPONIBLE) {
            e.setEstado(EstadoEquipo.RESERVADO);
        } else if (e.getStockDisponible() > 0 && e.getEstado() == EstadoEquipo.RESERVADO) {
            e.setEstado(EstadoEquipo.DISPONIBLE);
        }
    }
}
