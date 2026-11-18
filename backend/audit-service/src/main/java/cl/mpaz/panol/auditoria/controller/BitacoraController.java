package cl.mpaz.panol.auditoria.controller;

import cl.mpaz.panol.auditoria.dto.RegistroRequest;
import cl.mpaz.panol.auditoria.dto.RegistroResponse;
import cl.mpaz.panol.auditoria.service.BitacoraService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class BitacoraController {

    private final BitacoraService service;

    public BitacoraController(BitacoraService service) {
        this.service = service;
    }

    /** Consulta de la bitácora. Rol y RUT los entrega el api-gateway tras validar el token. */
    @GetMapping("/bitacora")
    public List<RegistroResponse> listar(
            @RequestHeader(value = "X-User-Rol", required = false) String rol,
            @RequestHeader(value = "X-User-Rut", required = false) String rut,
            @RequestParam(required = false) Long solicitudId) {
        return service.listar(rol, rut, solicitudId);
    }

    /** Uso interno: lo invoca el request-service en cada hito. No está expuesto por el gateway. */
    @PostMapping("/internal/bitacora")
    @ResponseStatus(HttpStatus.CREATED)
    public RegistroResponse registrar(@Valid @RequestBody RegistroRequest request) {
        return service.registrar(request);
    }
}
