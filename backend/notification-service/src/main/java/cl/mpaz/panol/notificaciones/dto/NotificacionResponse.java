package cl.mpaz.panol.notificaciones.dto;

import cl.mpaz.panol.notificaciones.model.Notificacion;
import cl.mpaz.panol.notificaciones.model.TipoNotificacion;

import java.time.Instant;

public record NotificacionResponse(
        Long id,
        String destinatarioRut,
        Long solicitudId,
        TipoNotificacion tipo,
        String titulo,
        String mensaje,
        Instant creadaEn,
        Instant programadaPara,
        Instant enviadaEn,
        boolean leida
) {

    public static NotificacionResponse de(Notificacion n) {
        return new NotificacionResponse(n.getId(), n.getDestinatarioRut(), n.getSolicitudId(), n.getTipo(),
                n.getTitulo(), n.getMensaje(), n.getCreadaEn(), n.getProgramadaPara(), n.getEnviadaEn(), n.isLeida());
    }
}
