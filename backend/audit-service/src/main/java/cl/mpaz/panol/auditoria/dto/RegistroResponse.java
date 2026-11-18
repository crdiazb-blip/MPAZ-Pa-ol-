package cl.mpaz.panol.auditoria.dto;

import cl.mpaz.panol.auditoria.model.Hito;
import cl.mpaz.panol.auditoria.model.RegistroBitacora;

import java.time.Instant;

public record RegistroResponse(
        Long id,
        Long solicitudId,
        String solicitanteRut,
        Hito hito,
        String actorRut,
        String actorNombre,
        String detalle,
        Instant fecha
) {

    public static RegistroResponse de(RegistroBitacora r) {
        return new RegistroResponse(r.getId(), r.getSolicitudId(), r.getSolicitanteRut(), r.getHito(),
                r.getActorRut(), r.getActorNombre(), r.getDetalle(), r.getFecha());
    }
}
