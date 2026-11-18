package cl.mpaz.panol.auditoria.service;

import cl.mpaz.panol.auditoria.dto.RegistroRequest;
import cl.mpaz.panol.auditoria.dto.RegistroResponse;
import cl.mpaz.panol.auditoria.exception.ApiException;
import cl.mpaz.panol.auditoria.model.Hito;
import cl.mpaz.panol.auditoria.model.RegistroBitacora;
import cl.mpaz.panol.auditoria.repository.BitacoraRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BitacoraServiceTest {

    private static final Instant AHORA = Instant.parse("2026-09-01T12:00:00Z");

    @Mock
    BitacoraRepository repo;
    private BitacoraService service;

    @BeforeEach
    void preparar() {
        service = new BitacoraService(repo, Clock.fixed(AHORA, ZoneOffset.UTC));
    }

    private RegistroBitacora registro(Hito hito) {
        return RegistroBitacora.builder().id(1L).solicitudId(5L).solicitanteRut("12345678-5")
                .hito(hito).actorRut("11111111-1").actorNombre("Encargado TI").fecha(AHORA).build();
    }

    @Test
    void registrarGuardaElHitoConLaFechaActual() {
        when(repo.save(any(RegistroBitacora.class))).thenAnswer(i -> i.getArgument(0));

        RegistroResponse r = service.registrar(new RegistroRequest(5L, "12345678-5", Hito.SOLICITADO,
                "12345678-5", "Docente Demo", "Solicitud creada"));

        ArgumentCaptor<RegistroBitacora> captor = ArgumentCaptor.forClass(RegistroBitacora.class);
        verify(repo).save(captor.capture());
        assertEquals(AHORA, captor.getValue().getFecha());
        assertEquals(Hito.SOLICITADO, r.hito());
        assertEquals("Solicitud creada", r.detalle());
    }

    @Test
    void adminVeTodoElHistorial() {
        when(repo.findAllByOrderByFechaDescIdDesc()).thenReturn(List.of(registro(Hito.APROBADO)));

        List<RegistroResponse> lista = service.listar("ADMIN", "11111111-1", null);

        assertEquals(1, lista.size());
        verify(repo, never()).findBySolicitanteRutOrderByFechaDescIdDesc(any());
    }

    @Test
    void adminPuedeFiltrarPorSolicitud() {
        when(repo.findBySolicitudIdOrderByFechaAscIdAsc(5L)).thenReturn(List.of(registro(Hito.ENTREGADO)));

        assertEquals(1, service.listar("ADMIN", "11111111-1", 5L).size());
    }

    @Test
    void solicitanteSoloVeSusPropiosRegistros() {
        when(repo.findBySolicitanteRutOrderByFechaDescIdDesc("12345678-5"))
                .thenReturn(List.of(registro(Hito.SOLICITADO)));

        List<RegistroResponse> lista = service.listar("SOLICITANTE", "12345678-5", null);

        assertEquals(1, lista.size());
        verify(repo, never()).findAllByOrderByFechaDescIdDesc();
    }

    @Test
    void solicitanteNoPuedeVerLaSolicitudDeOtro() {
        when(repo.findBySolicitudIdAndSolicitanteRutOrderByFechaAscIdAsc(9L, "12345678-5"))
                .thenReturn(List.of());

        assertTrue(service.listar("SOLICITANTE", "12345678-5", 9L).isEmpty());
    }

    @Test
    void sinRutYSinSerAdminEsProhibido() {
        assertThrows(ApiException.class, () -> service.listar("SOLICITANTE", null, null));
    }
}
