package cl.mpaz.panol.notificaciones.service;

import cl.mpaz.panol.notificaciones.dto.NotificacionRequest;
import cl.mpaz.panol.notificaciones.dto.NotificacionResponse;
import cl.mpaz.panol.notificaciones.exception.ApiException;
import cl.mpaz.panol.notificaciones.model.Notificacion;
import cl.mpaz.panol.notificaciones.model.TipoNotificacion;
import cl.mpaz.panol.notificaciones.repository.NotificacionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificacionServiceTest {

    private static final Instant AHORA = Instant.parse("2026-09-01T15:00:00Z");

    @Mock
    NotificacionRepository repo;
    private NotificacionService service;

    @BeforeEach
    void preparar() {
        service = new NotificacionService(repo, Clock.fixed(AHORA, ZoneOffset.UTC));
    }

    private NotificacionRequest request(Instant programadaPara) {
        return new NotificacionRequest("12345678-5", 10L, TipoNotificacion.RECORDATORIO_DEVOLUCION,
                "Recordatorio", "Devuelve los equipos", programadaPara);
    }

    private Notificacion notificacion(Long id, String rut, boolean leida) {
        return Notificacion.builder().id(id).destinatarioRut(rut).tipo(TipoNotificacion.SOLICITUD_ACEPTADA)
                .titulo("t").mensaje("m").creadaEn(AHORA).enviadaEn(AHORA).leida(leida).build();
    }

    @Test
    void sinHoraProgramadaSeEnviaDeInmediato() {
        when(repo.save(any(Notificacion.class))).thenAnswer(i -> i.getArgument(0));

        NotificacionResponse r = service.crear(request(null));

        assertEquals(AHORA, r.enviadaEn());
        assertFalse(r.leida());
    }

    @Test
    void conHoraFuturaQuedaPendiente() {
        when(repo.save(any(Notificacion.class))).thenAnswer(i -> i.getArgument(0));

        NotificacionResponse r = service.crear(request(AHORA.plus(Duration.ofMinutes(45))));

        assertNull(r.enviadaEn());
        assertEquals(AHORA.plus(Duration.ofMinutes(45)), r.programadaPara());
    }

    @Test
    void enviarPendientesMarcaComoEnviadasLasQueYaVencieron() {
        Notificacion vencida = Notificacion.builder().id(1L).destinatarioRut("12345678-5")
                .tipo(TipoNotificacion.RECORDATORIO_DEVOLUCION).titulo("t").mensaje("m")
                .creadaEn(AHORA.minusSeconds(3600)).programadaPara(AHORA.minusSeconds(60)).build();
        when(repo.findByEnviadaEnIsNullAndProgramadaParaLessThanEqual(AHORA)).thenReturn(List.of(vencida));

        int enviadas = service.enviarPendientes();

        assertEquals(1, enviadas);
        assertEquals(AHORA, vencida.getEnviadaEn());
        verify(repo).saveAll(List.of(vencida));
    }

    @Test
    void enviarPendientesSinNadaVencidoNoHaceNada() {
        when(repo.findByEnviadaEnIsNullAndProgramadaParaLessThanEqual(AHORA)).thenReturn(List.of());

        assertEquals(0, service.enviarPendientes());
    }

    @Test
    void misNotificacionesPuedeFiltrarSoloLasNoLeidas() {
        when(repo.findByDestinatarioRutAndEnviadaEnIsNotNullOrderByEnviadaEnDescIdDesc("12345678-5"))
                .thenReturn(List.of(notificacion(1L, "12345678-5", true), notificacion(2L, "12345678-5", false)));

        assertEquals(2, service.misNotificaciones("12345678-5", false).size());
        assertEquals(1, service.misNotificaciones("12345678-5", true).size());
    }

    @Test
    void sinRutNoSePuedeConsultar() {
        ApiException e = assertThrows(ApiException.class, () -> service.misNotificaciones(null, false));
        assertEquals(HttpStatus.UNAUTHORIZED, e.getStatus());
    }

    @Test
    void marcarLeidaSoloFuncionaConLasPropias() {
        when(repo.findById(1L)).thenReturn(Optional.of(notificacion(1L, "22222222-2", false)));

        ApiException e = assertThrows(ApiException.class, () -> service.marcarLeida("12345678-5", 1L));

        assertEquals(HttpStatus.NOT_FOUND, e.getStatus());
    }

    @Test
    void marcarLeidaActualizaLaNotificacion() {
        Notificacion n = notificacion(1L, "12345678-5", false);
        when(repo.findById(1L)).thenReturn(Optional.of(n));
        when(repo.save(n)).thenReturn(n);

        assertTrue(service.marcarLeida("12345678-5", 1L).leida());
    }

    @Test
    void elLogCompletoEsSoloParaAdmin() {
        assertThrows(ApiException.class, () -> service.log("SOLICITANTE"));
        when(repo.findAllByOrderByCreadaEnDescIdDesc()).thenReturn(List.of(notificacion(1L, "12345678-5", false)));
        assertEquals(1, service.log("ADMIN").size());
    }
}
