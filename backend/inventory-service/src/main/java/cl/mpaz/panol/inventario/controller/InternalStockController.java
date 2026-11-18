package cl.mpaz.panol.inventario.controller;

import cl.mpaz.panol.inventario.dto.ItemStock;
import cl.mpaz.panol.inventario.service.EquipoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Endpoints de uso interno entre microservicios (request-service -> inventory-service).
 * El api-gateway no enruta /internal, por lo que no son accesibles desde la app.
 */
@RestController
@RequestMapping("/internal/equipos")
public class InternalStockController {

    private final EquipoService service;

    public InternalStockController(EquipoService service) {
        this.service = service;
    }

    @PostMapping("/reservar")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void reservar(@RequestBody List<@Valid ItemStock> items) {
        service.reservar(items);
    }

    @PostMapping("/liberar")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void liberar(@RequestBody List<@Valid ItemStock> items) {
        service.liberar(items);
    }
}
