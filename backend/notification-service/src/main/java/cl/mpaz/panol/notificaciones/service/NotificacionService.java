package cl.mpaz.panol.notificaciones.service;

import cl.mpaz.panol.notificaciones.dto.NotificacionRequest;
import cl.mpaz.panol.notificaciones.dto.NotificacionResponse;
import cl.mpaz.panol.notificaciones.exception.ApiException;
import cl.mpaz.panol.notificaciones.model.Notificacion;
import cl.mpaz.panol.notificaciones.repository.NotificacionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.List;

@Service
public class NotificacionService {

    private static final Logger log = LoggerFactory.getLogger(NotificacionService.class);

    private final NotificacionRepository repo;
    private final Clock reloj;

    public NotificacionService(NotificacionRepository repo, Clock reloj) {
        this.repo = repo;
        this.reloj = reloj;
    }

    @Transactional
    public NotificacionResponse crear(NotificacionRequest req) {
        Instant ahora = Instant.now(reloj);
        boolean inmediata = req.programadaPara() == null || !req.programadaPara().isAfter(ahora);
        Notificacion n = Notificacion.builder()
                .destinatarioRut(req.destinatarioRut())
                .solicitudId(req.solicitudId())
                .tipo(req.tipo())
                .titulo(req.titulo())
                .mensaje(req.mensaje())
                .creadaEn(ahora)
                .programadaPara(req.programadaPara())
                .enviadaEn(inmediata ? ahora : null)
                .leida(false)
                .build();
        return NotificacionResponse.de(repo.save(n));
    }

    /** Notificaciones ya enviadas al usuario autenticado. */
    @Transactional(readOnly = true)
    public List<NotificacionResponse> misNotificaciones(String rut, boolean soloNoLeidas) {
        exigirRut(rut);
        return repo.findByDestinatarioRutAndEnviadaEnIsNotNullOrderByEnviadaEnDescIdDesc(rut).stream()
                .filter(n -> !soloNoLeidas || !n.isLeida())
                .map(NotificacionResponse::de)
                .toList();
    }

    @Transactional
    public NotificacionResponse marcarLeida(String rut, Long id) {
        exigirRut(rut);
        Notificacion n = repo.findById(id)
                .filter(x -> x.getDestinatarioRut().equals(rut))
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Notificación " + id + " no encontrada."));
        n.setLeida(true);
        return NotificacionResponse.de(repo.save(n));
    }

    /** Log completo (enviadas y pendientes), solo para el administrador. */
    @Transactional(readOnly = true)
    public List<NotificacionResponse> log(String rol) {
        if (!"ADMIN".equals(rol)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "El log de notificaciones es solo para el Administrador.");
        }
        return repo.findAllByOrderByCreadaEnDescIdDesc().stream().map(NotificacionResponse::de).toList();
    }

    /** Envía (simulado) las notificaciones programadas cuya hora ya llegó. Retorna cuántas envió. */
    @Transactional
    public int enviarPendientes() {
        Instant ahora = Instant.now(reloj);
        List<Notificacion> pendientes = repo.findByEnviadaEnIsNullAndProgramadaParaLessThanEqual(ahora);
        for (Notificacion n : pendientes) {
            n.setEnviadaEn(ahora);
            log.info("[SIMULADO] Notificación '{}' enviada internamente a {} (solicitud {})",
                    n.getTipo(), n.getDestinatarioRut(), n.getSolicitudId());
        }
        repo.saveAll(pendientes);
        return pendientes.size();
    }

    private void exigirRut(String rut) {
        if (rut == null || rut.isBlank()) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "No se pudo identificar al usuario.");
        }
    }
}
