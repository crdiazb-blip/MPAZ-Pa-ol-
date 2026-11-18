package cl.mpaz.panol.solicitudes.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/** Registra los hitos de la solicitud en el audit-service. Si falla, no interrumpe el flujo principal. */
@Component
public class AuditoriaClient {

    private static final Logger log = LoggerFactory.getLogger(AuditoriaClient.class);

    private final RestClient rest;

    public AuditoriaClient(@Qualifier("auditoriaRest") RestClient rest) {
        this.rest = rest;
    }

    public record Registro(Long solicitudId, String solicitanteRut, String hito, String actorRut,
                           String actorNombre, String detalle) {
    }

    public void registrar(Registro registro) {
        try {
            rest.post().uri("/internal/bitacora").body(registro).retrieve().toBodilessEntity();
        } catch (Exception e) {
            log.warn("No se pudo registrar el hito {} de la solicitud {}: {}",
                    registro.hito(), registro.solicitudId(), e.getMessage());
        }
    }
}
