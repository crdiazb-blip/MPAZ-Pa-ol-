package cl.mpaz.panol.inventario.controller;

import cl.mpaz.panol.inventario.dto.EquipoRequest;
import cl.mpaz.panol.inventario.dto.EquipoResponse;
import cl.mpaz.panol.inventario.dto.EstadoRequest;
import cl.mpaz.panol.inventario.dto.StockRequest;
import cl.mpaz.panol.inventario.model.Categoria;
import cl.mpaz.panol.inventario.service.EquipoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/equipos")
public class EquipoController {

    private final EquipoService service;

    public EquipoController(EquipoService service) {
        this.service = service;
    }

    @GetMapping
    public List<EquipoResponse> listar(@RequestParam(required = false) Categoria categoria,
                                       @RequestParam(defaultValue = "false") boolean soloDisponibles) {
        return service.listar(categoria, soloDisponibles);
    }

    @GetMapping("/{id}")
    public EquipoResponse obtener(@PathVariable Long id) {
        return service.obtener(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EquipoResponse crear(@RequestHeader(value = Acceso.HEADER_ROL, required = false) String rol,
                                @Valid @RequestBody EquipoRequest request) {
        Acceso.exigirAdmin(rol);
        return service.crear(request);
    }

    @PutMapping("/{id}")
    public EquipoResponse actualizar(@RequestHeader(value = Acceso.HEADER_ROL, required = false) String rol,
                                     @PathVariable Long id, @Valid @RequestBody EquipoRequest request) {
        Acceso.exigirAdmin(rol);
        return service.actualizar(id, request);
    }

    @PatchMapping("/{id}/estado")
    public EquipoResponse cambiarEstado(@RequestHeader(value = Acceso.HEADER_ROL, required = false) String rol,
                                        @PathVariable Long id, @Valid @RequestBody EstadoRequest request) {
        Acceso.exigirAdmin(rol);
        return service.cambiarEstado(id, request.estado());
    }

    @PatchMapping("/{id}/stock")
    public EquipoResponse cambiarStock(@RequestHeader(value = Acceso.HEADER_ROL, required = false) String rol,
                                       @PathVariable Long id, @Valid @RequestBody StockRequest request) {
        Acceso.exigirAdmin(rol);
        return service.cambiarStock(id, request.stockTotal());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void darDeBaja(@RequestHeader(value = Acceso.HEADER_ROL, required = false) String rol,
                          @PathVariable Long id) {
        Acceso.exigirAdmin(rol);
        service.darDeBaja(id);
    }
}
