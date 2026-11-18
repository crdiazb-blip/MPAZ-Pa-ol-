package cl.mpaz.panol.notificaciones.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** Revisa cada 30 segundos si hay recordatorios de devolución que ya deben enviarse. */
@Component
public class RecordatorioScheduler {

    private final NotificacionService service;

    public RecordatorioScheduler(NotificacionService service) {
        this.service = service;
    }

    @Scheduled(fixedDelayString = "${notificaciones.intervalo-ms:30000}", initialDelay = 15000)
    public void enviarPendientes() {
        service.enviarPendientes();
    }
}
