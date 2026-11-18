package cl.mpaz.panol.solicitudes.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.Instant;

/** Crea notificaciones internas simuladas en el notification-service (sin correos reales). */
@Component
public class NotificacionesClient {

    private static final Logger log = LoggerFactory.getLogger(NotificacionesClient.class);

    private final RestClient rest;

    public NotificacionesClient(@Qualifier("notificacionesRest") RestClient rest) {
        this.rest = rest;
    }

    /** programadaPara = null significa "enviar ahora". */
    public record Notificacion(String destinatarioRut, Long solicitudId, String tipo, String titulo,
                               String mensaje, Instant programadaPara) {
    }

    public void enviar(Notificacion notificacion) {
        try {
            rest.post().uri("/internal/notificaciones").body(notificacion).retrieve().toBodilessEntity();
        } catch (Exception e) {
            log.warn("No se pudo crear la notificación {} de la solicitud {}: {}",
                    notificacion.tipo(), notificacion.solicitudId(), e.getMessage());
        }
    }
}
