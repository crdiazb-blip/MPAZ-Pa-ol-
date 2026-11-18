package cl.mpaz.panol.inventario.service;

import cl.mpaz.panol.inventario.dto.EquipoRequest;
import cl.mpaz.panol.inventario.dto.EquipoResponse;
import cl.mpaz.panol.inventario.dto.ItemStock;
import cl.mpaz.panol.inventario.exception.ApiException;
import cl.mpaz.panol.inventario.model.Categoria;
import cl.mpaz.panol.inventario.model.Equipo;
import cl.mpaz.panol.inventario.model.EstadoEquipo;
import cl.mpaz.panol.inventario.repository.EquipoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EquipoServiceTest {

    @Mock
    EquipoRepository repo;
    @InjectMocks
    EquipoService service;

    private Equipo notebook;
    private Equipo proyector;

    @BeforeEach
    void preparar() {
        notebook = Equipo.builder().id(1L).nombre("Notebook").marca("Lenovo").modelo("V15")
                .categoria(Categoria.NOTEBOOK).stockTotal(10).stockDisponible(7)
                .estado(EstadoEquipo.DISPONIBLE).ubicacion("Estante A").activo(true).build();
        proyector = Equipo.builder().id(2L).nombre("Proyector").marca("Epson").modelo("X41")
                .categoria(Categoria.PROYECTOR).stockTotal(2).stockDisponible(2)
                .estado(EstadoEquipo.DISPONIBLE).ubicacion("Estante C").activo(true).build();
    }

    private EquipoRequest request(Integer total, Integer disponible) {
        return new EquipoRequest("Parlante", "JBL", "Box", null, Categoria.PARLANTE, total, disponible,
                null, "Estante C");
    }

    @Test
    void crearSinStockDisponibleLoIgualaAlTotal() {
        when(repo.save(any(Equipo.class))).thenAnswer(i -> i.getArgument(0));

        EquipoResponse r = service.crear(request(5, null));

        assertEquals(5, r.stockDisponible());
        assertEquals(EstadoEquipo.DISPONIBLE, r.estado());
        assertTrue(r.reservable());
    }

    @Test
    void crearConDisponibleMayorAlTotalFalla() {
        ApiException e = assertThrows(ApiException.class, () -> service.crear(request(3, 5)));
        assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
    }

    @Test
    void crearSinUnidadesDisponiblesLoMarcaReservado() {
        when(repo.save(any(Equipo.class))).thenAnswer(i -> i.getArgument(0));

        EquipoResponse r = service.crear(request(3, 0));

        assertEquals(EstadoEquipo.RESERVADO, r.estado());
        assertFalse(r.reservable());
    }

    @Test
    void listarSoloDisponiblesExcluyeLosNoReservables() {
        notebook.setStockDisponible(0);
        notebook.setEstado(EstadoEquipo.RESERVADO);
        when(repo.findByActivoTrueOrderByNombreAsc()).thenReturn(List.of(notebook, proyector));

        List<EquipoResponse> lista = service.listar(null, true);

        assertEquals(1, lista.size());
        assertEquals("Proyector", lista.get(0).nombre());
    }

    @Test
    void listarFiltraPorCategoria() {
        when(repo.findByActivoTrueOrderByNombreAsc()).thenReturn(List.of(notebook, proyector));

        List<EquipoResponse> lista = service.listar(Categoria.PROYECTOR, false);

        assertEquals(1, lista.size());
        assertEquals(Categoria.PROYECTOR, lista.get(0).categoria());
    }

    @Test
    void obtenerInexistenteRetorna404() {
        when(repo.findByIdAndActivoTrue(99L)).thenReturn(Optional.empty());

        ApiException e = assertThrows(ApiException.class, () -> service.obtener(99L));

        assertEquals(HttpStatus.NOT_FOUND, e.getStatus());
    }

    @Test
    void cambiarStockAumentaElDisponibleEnLaMismaCantidad() {
        when(repo.findByIdAndActivoTrue(1L)).thenReturn(Optional.of(notebook));
        when(repo.save(any(Equipo.class))).thenAnswer(i -> i.getArgument(0));

        EquipoResponse r = service.cambiarStock(1L, 15);

        assertEquals(15, r.stockTotal());
        assertEquals(12, r.stockDisponible());
    }

    @Test
    void cambiarStockNoPuedeDejarMenosQueLoPrestado() {
        when(repo.findByIdAndActivoTrue(1L)).thenReturn(Optional.of(notebook));

        ApiException e = assertThrows(ApiException.class, () -> service.cambiarStock(1L, 2));

        assertEquals(HttpStatus.CONFLICT, e.getStatus());
    }

    @Test
    void darDeBajaConUnidadesPrestadasFalla() {
        when(repo.findByIdAndActivoTrue(1L)).thenReturn(Optional.of(notebook));

        assertThrows(ApiException.class, () -> service.darDeBaja(1L));
        verify(repo, never()).save(any());
    }

    @Test
    void darDeBajaSinPrestamosDesactivaElEquipo() {
        when(repo.findByIdAndActivoTrue(2L)).thenReturn(Optional.of(proyector));

        service.darDeBaja(2L);

        assertFalse(proyector.isActivo());
        assertEquals(EstadoEquipo.FUERA_DE_SERVICIO, proyector.getEstado());
    }

    @Test
    void reservarDescuentaElStockDisponible() {
        when(repo.bloquearPorIds(any())).thenReturn(List.of(notebook));

        service.reservar(List.of(new ItemStock(1L, 3)));

        assertEquals(4, notebook.getStockDisponible());
    }

    @Test
    void reservarTodoElStockMarcaElEquipoComoReservado() {
        when(repo.bloquearPorIds(any())).thenReturn(List.of(proyector));

        service.reservar(List.of(new ItemStock(2L, 2)));

        assertEquals(0, proyector.getStockDisponible());
        assertEquals(EstadoEquipo.RESERVADO, proyector.getEstado());
    }

    @Test
    void reservarMasDeLoDisponibleRetorna409() {
        when(repo.bloquearPorIds(any())).thenReturn(List.of(notebook));

        ApiException e = assertThrows(ApiException.class,
                () -> service.reservar(List.of(new ItemStock(1L, 8))));

        assertEquals(HttpStatus.CONFLICT, e.getStatus());
        assertEquals(7, notebook.getStockDisponible());
    }

    @Test
    void reservarEquipoEnMantenimientoRetorna409() {
        proyector.setEstado(EstadoEquipo.MANTENIMIENTO);
        when(repo.bloquearPorIds(any())).thenReturn(List.of(proyector));

        assertThrows(ApiException.class, () -> service.reservar(List.of(new ItemStock(2L, 1))));
    }

    @Test
    void reservarSumaCantidadesRepetidasDelMismoEquipo() {
        when(repo.bloquearPorIds(any())).thenReturn(List.of(notebook));

        service.reservar(List.of(new ItemStock(1L, 2), new ItemStock(1L, 3)));

        assertEquals(2, notebook.getStockDisponible());
    }

    @Test
    void reservarEquipoInexistenteRetorna404() {
        when(repo.bloquearPorIds(any())).thenReturn(List.of(notebook));

        ApiException e = assertThrows(ApiException.class,
                () -> service.reservar(List.of(new ItemStock(1L, 1), new ItemStock(50L, 1))));

        assertEquals(HttpStatus.NOT_FOUND, e.getStatus());
    }

    @Test
    void liberarNoSuperaElStockTotalYRestauraElEstado() {
        proyector.setStockDisponible(0);
        proyector.setEstado(EstadoEquipo.RESERVADO);
        when(repo.bloquearPorIds(any())).thenReturn(List.of(proyector));

        service.liberar(List.of(new ItemStock(2L, 5)));

        assertEquals(2, proyector.getStockDisponible());
        assertEquals(EstadoEquipo.DISPONIBLE, proyector.getEstado());
    }
}
