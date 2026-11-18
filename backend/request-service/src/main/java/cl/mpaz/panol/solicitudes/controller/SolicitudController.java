package cl.mpaz.panol.solicitudes.controller;

import cl.mpaz.panol.solicitudes.dto.RechazoRequest;
import cl.mpaz.panol.solicitudes.dto.SolicitudRequest;
import cl.mpaz.panol.solicitudes.dto.SolicitudResponse;
import cl.mpaz.panol.solicitudes.model.EstadoSolicitud;
import cl.mpaz.panol.solicitudes.security.UsuarioActual;
import cl.mpaz.panol.solicitudes.service.SolicitudService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/solicitudes")
public class SolicitudController {

    private final SolicitudService service;

    public SolicitudController(SolicitudService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SolicitudResponse crear(@RequestHeader HttpHeaders headers, @Valid @RequestBody SolicitudRequest request) {
        return service.crear(UsuarioActual.de(headers), request);
    }

    /** El administrador ve todas las solicitudes; el solicitante, solo las suyas. */
    @GetMapping
    public List<SolicitudResponse> listar(@RequestHeader HttpHeaders headers,
                                          @RequestParam(required = false) EstadoSolicitud estado) {
        return service.listar(UsuarioActual.de(headers), estado);
    }

    @GetMapping("/{id}")
    public SolicitudResponse obtener(@RequestHeader HttpHeaders headers, @PathVariable Long id) {
        return service.obtener(UsuarioActual.de(headers), id);
    }

    @PatchMapping("/{id}/aceptar")
    public SolicitudResponse aceptar(@RequestHeader HttpHeaders headers, @PathVariable Long id) {
        return service.aceptar(UsuarioActual.de(headers), id);
    }

    @PatchMapping("/{id}/rechazar")
    public SolicitudResponse rechazar(@RequestHeader HttpHeaders headers, @PathVariable Long id,
                                      @Valid @RequestBody RechazoRequest request) {
        return service.rechazar(UsuarioActual.de(headers), id, request.comentario());
    }

    @PatchMapping("/{id}/entregar")
    public SolicitudResponse entregar(@RequestHeader HttpHeaders headers, @PathVariable Long id) {
        return service.entregar(UsuarioActual.de(headers), id);
    }

    @PatchMapping("/{id}/devolver")
    public SolicitudResponse devolver(@RequestHeader HttpHeaders headers, @PathVariable Long id) {
        return service.devolver(UsuarioActual.de(headers), id);
    }

    @PatchMapping("/{id}/finalizar")
    public SolicitudResponse finalizar(@RequestHeader HttpHeaders headers, @PathVariable Long id) {
        return service.finalizar(UsuarioActual.de(headers), id);
    }
}
