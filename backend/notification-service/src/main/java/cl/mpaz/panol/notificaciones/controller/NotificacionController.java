package cl.mpaz.panol.notificaciones.controller;

import cl.mpaz.panol.notificaciones.dto.NotificacionRequest;
import cl.mpaz.panol.notificaciones.dto.NotificacionResponse;
import cl.mpaz.panol.notificaciones.service.NotificacionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class NotificacionController {

    private final NotificacionService service;

    public NotificacionController(NotificacionService service) {
        this.service = service;
    }

    /** Notificaciones del usuario autenticado (el gateway entrega su RUT en X-User-Rut). */
    @GetMapping("/notificaciones")
    public List<NotificacionResponse> mias(@RequestHeader(value = "X-User-Rut", required = false) String rut,
                                           @RequestParam(defaultValue = "false") boolean soloNoLeidas) {
        return service.misNotificaciones(rut, soloNoLeidas);
    }

    @PatchMapping("/notificaciones/{id}/leida")
    public NotificacionResponse marcarLeida(@RequestHeader(value = "X-User-Rut", required = false) String rut,
                                            @PathVariable Long id) {
        return service.marcarLeida(rut, id);
    }

    @GetMapping("/notificaciones/log")
    public List<NotificacionResponse> log(@RequestHeader(value = "X-User-Rol", required = false) String rol) {
        return service.log(rol);
    }

    /** Uso interno: lo invoca el request-service. No está expuesto por el gateway. */
    @PostMapping("/internal/notificaciones")
    @ResponseStatus(HttpStatus.CREATED)
    public NotificacionResponse crear(@Valid @RequestBody NotificacionRequest request) {
        return service.crear(request);
    }
}
