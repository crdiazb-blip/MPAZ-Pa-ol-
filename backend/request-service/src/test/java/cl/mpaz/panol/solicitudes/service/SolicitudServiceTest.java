package cl.mpaz.panol.solicitudes.service;

import cl.mpaz.panol.solicitudes.client.AuditoriaClient;
import cl.mpaz.panol.solicitudes.client.InventarioClient;
import cl.mpaz.panol.solicitudes.client.NotificacionesClient;
import cl.mpaz.panol.solicitudes.dto.ItemRequest;
import cl.mpaz.panol.solicitudes.dto.SolicitudRequest;
import cl.mpaz.panol.solicitudes.dto.SolicitudResponse;
import cl.mpaz.panol.solicitudes.exception.ApiException;
import cl.mpaz.panol.solicitudes.model.EstadoSolicitud;
import cl.mpaz.panol.solicitudes.model.Solicitud;
import cl.mpaz.panol.solicitudes.model.SolicitudItem;
import cl.mpaz.panol.solicitudes.repository.SolicitudRepository;
import cl.mpaz.panol.solicitudes.security.UsuarioActual;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.time.*;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SolicitudServiceTest {

    private static final ZoneId ZONA = ZoneId.of("America/Santiago");
    /** "Ahora" de las pruebas: 1 de septiembre de 2026, 12:00 hora de Chile. */
    private static final Instant AHORA = ZonedDateTime.of(2026, 9, 1, 12, 0, 0, 0, ZONA).toInstant();

    @Mock
    SolicitudRepository repo;
    @Mock
    InventarioClient inventario;
    @Mock
    AuditoriaClient auditoria;
    @Mock
    NotificacionesClient notificaciones;

    private SolicitudService service;
    private final UsuarioActual docente =
            new UsuarioActual("12345678-5", "Docente Demo", "Docente", "d@mpaz.test", "SOLICITANTE");
    private final UsuarioActual admin =
            new UsuarioActual("11111111-1", "Encargado TI Demo", "Encargado TI", "ti@mpaz.test", "ADMIN");

    @BeforeEach
    void preparar() {
        service = new SolicitudService(repo, inventario, auditoria, notificaciones,
                Clock.fixed(AHORA, ZONA), 3);
    }

    // ---------------------------------------------------------------- crear

    private SolicitudRequest request(String fecha, String inicio, String devolucion, ItemRequest... items) {
        return new SolicitudRequest("Sala 4° Básico A", fecha, inicio, devolucion, List.of(items));
    }

    private void equipoDisponible(long id, String nombre, int stock) {
        when(inventario.obtener(id)).thenReturn(new InventarioClient.Equipo(id, nombre, stock, true));
    }

    private void guardarDevuelveLoMismo() {
        when(repo.save(any(Solicitud.class))).thenAnswer(i -> {
            Solicitud s = i.getArgument(0);
            if (s.getId() == null) {
                s.setId(10L);
            }
            return s;
        });
    }

    @Test
    void creaSolicitudConExactamenteTresHorasDeAnticipacion() {
        equipoDisponible(1, "Notebook", 5);
        guardarDevuelveLoMismo();

        SolicitudResponse r = service.crear(docente, request("2026-09-01", "15:00", "16:00", new ItemRequest(1L, 2)));

        assertEquals(EstadoSolicitud.PENDIENTE, r.estado());
        assertEquals("15:00", r.horaInicio());
        assertEquals(1, r.items().size());
        assertEquals("Notebook", r.items().get(0).nombreEquipo());
        verify(auditoria).registrar(argThat(reg -> reg.hito().equals("SOLICITADO") && reg.solicitudId() == 10L));
    }

    @Test
    void rechazaSolicitudConMenosDeTresHorasDeAnticipacion() {
        ApiException e = assertThrows(ApiException.class,
                () -> service.crear(docente, request("2026-09-01", "14:59", "16:00", new ItemRequest(1L, 1))));

        assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
        assertTrue(e.getMessage().contains("3 horas"));
        verifyNoInteractions(inventario);
        verify(repo, never()).save(any());
    }

    @Test
    void rechazaSolicitudConFechaPasada() {
        assertThrows(ApiException.class,
                () -> service.crear(docente, request("2026-08-31", "10:00", "11:00", new ItemRequest(1L, 1))));
    }

    @Test
    void aceptaSolicitudParaOtroDiaConAnticipacionSuficiente() {
        equipoDisponible(1, "Notebook", 5);
        guardarDevuelveLoMismo();

        assertDoesNotThrow(() ->
                service.crear(docente, request("2026-09-02", "08:00", "09:00", new ItemRequest(1L, 1))));
    }

    @Test
    void rechazaHoraDeDevolucionAnteriorOIgualALaDeInicio() {
        ApiException e = assertThrows(ApiException.class,
                () -> service.crear(docente, request("2026-09-02", "10:00", "10:00", new ItemRequest(1L, 1))));
        assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
    }

    @Test
    void rechazaHoraInexistente() {
        assertThrows(ApiException.class,
                () -> service.crear(docente, request("2026-09-02", "25:00", "26:00", new ItemRequest(1L, 1))));
    }

    @Test
    void rechazaFechaInexistente() {
        assertThrows(ApiException.class,
                () -> service.crear(docente, request("2026-02-31", "10:00", "11:00", new ItemRequest(1L, 1))));
    }

    @Test
    void rechazaCantidadMayorAlStockDisponible() {
        equipoDisponible(1, "Notebook", 2);

        ApiException e = assertThrows(ApiException.class,
                () -> service.crear(docente, request("2026-09-02", "08:00", "09:00", new ItemRequest(1L, 3))));

        assertEquals(HttpStatus.CONFLICT, e.getStatus());
        verify(repo, never()).save(any());
    }

    @Test
    void rechazaEquipoNoReservable() {
        when(inventario.obtener(3L)).thenReturn(new InventarioClient.Equipo(3L, "Proyector", 0, false));

        ApiException e = assertThrows(ApiException.class,
                () -> service.crear(docente, request("2026-09-02", "08:00", "09:00", new ItemRequest(3L, 1))));

        assertEquals(HttpStatus.CONFLICT, e.getStatus());
    }

    @Test
    void sumaCantidadesDelMismoEquipoRepetidoEnElCarrito() {
        equipoDisponible(1, "Notebook", 5);
        guardarDevuelveLoMismo();

        SolicitudResponse r = service.crear(docente, request("2026-09-02", "08:00", "09:00",
                new ItemRequest(1L, 2), new ItemRequest(1L, 1)));

        assertEquals(1, r.items().size());
        assertEquals(3, r.items().get(0).cantidad());
    }

    @Test
    void elAdministradorNoPuedeCrearSolicitudes() {
        ApiException e = assertThrows(ApiException.class,
                () -> service.crear(admin, request("2026-09-02", "08:00", "09:00", new ItemRequest(1L, 1))));
        assertEquals(HttpStatus.FORBIDDEN, e.getStatus());
    }

    // ---------------------------------------------------------------- flujo del administrador

    private Solicitud solicitud(EstadoSolicitud estado) {
        Solicitud s = Solicitud.builder().id(10L).solicitanteRut("12345678-5").solicitanteNombre("Docente Demo")
                .sala("Sala 1").fecha(LocalDate.of(2026, 9, 1)).horaInicio(LocalTime.of(15, 0))
                .horaDevolucion(LocalTime.of(17, 0)).estado(estado).creadaEn(AHORA).build();
        s.agregarItem(SolicitudItem.builder().equipoId(1L).nombreEquipo("Notebook").cantidad(2).build());
        return s;
    }

    private void existe(Solicitud s) {
        when(repo.findById(s.getId())).thenReturn(Optional.of(s));
    }

    @Test
    void aceptarDescuentaElStockYRegistraElHito() {
        Solicitud s = solicitud(EstadoSolicitud.PENDIENTE);
        existe(s);

        SolicitudResponse r = service.aceptar(admin, 10L);

        assertEquals(EstadoSolicitud.ACEPTADA, r.estado());
        assertNotNull(r.aceptadaEn());
        verify(inventario).reservar(List.of(new InventarioClient.Item(1L, 2)));
        verify(auditoria).registrar(argThat(reg -> reg.hito().equals("APROBADO")));
        verify(notificaciones).enviar(argThat(n -> n.tipo().equals("SOLICITUD_ACEPTADA")));
    }

    @Test
    void aceptarFallaSiElInventarioNoTieneStockYNoCambiaElEstado() {
        Solicitud s = solicitud(EstadoSolicitud.PENDIENTE);
        existe(s);
        doThrow(new ApiException(HttpStatus.CONFLICT, "Stock insuficiente de Notebook"))
                .when(inventario).reservar(anyList());

        ApiException e = assertThrows(ApiException.class, () -> service.aceptar(admin, 10L));

        assertEquals(HttpStatus.CONFLICT, e.getStatus());
        assertEquals(EstadoSolicitud.PENDIENTE, s.getEstado());
        verify(repo, never()).save(any());
        verifyNoInteractions(auditoria);
    }

    @Test
    void noSePuedeAceptarDosVeces() {
        existe(solicitud(EstadoSolicitud.ACEPTADA));

        ApiException e = assertThrows(ApiException.class, () -> service.aceptar(admin, 10L));

        assertEquals(HttpStatus.CONFLICT, e.getStatus());
        verifyNoInteractions(inventario);
    }

    @Test
    void unSolicitanteNoPuedeAceptar() {
        ApiException e = assertThrows(ApiException.class, () -> service.aceptar(docente, 10L));
        assertEquals(HttpStatus.FORBIDDEN, e.getStatus());
    }

    @Test
    void rechazarExigeJustificacion() {
        ApiException e = assertThrows(ApiException.class, () -> service.rechazar(admin, 10L, "   "));
        assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
        verifyNoInteractions(repo);
    }

    @Test
    void rechazarGuardaLaJustificacionYNoTocaElStock() {
        Solicitud s = solicitud(EstadoSolicitud.PENDIENTE);
        existe(s);

        SolicitudResponse r = service.rechazar(admin, 10L, "El proyector está en mantenimiento");

        assertEquals(EstadoSolicitud.RECHAZADA, r.estado());
        assertEquals("El proyector está en mantenimiento", r.comentarioGestion());
        verifyNoInteractions(inventario);
        verify(auditoria).registrar(argThat(reg -> reg.hito().equals("RECHAZADO")
                && reg.detalle().contains("mantenimiento")));
    }

    @Test
    void entregarRegistraLaFechaYProgramaElRecordatorioDeDevolucion() {
        Solicitud s = solicitud(EstadoSolicitud.ACEPTADA);
        existe(s);

        SolicitudResponse r = service.entregar(admin, 10L);

        assertEquals(EstadoSolicitud.ENTREGADA, r.estado());
        assertEquals(AHORA, r.entregadaEn());
        ArgumentCaptor<NotificacionesClient.Notificacion> captor =
                ArgumentCaptor.forClass(NotificacionesClient.Notificacion.class);
        verify(notificaciones, times(2)).enviar(captor.capture());
        NotificacionesClient.Notificacion recordatorio = captor.getAllValues().stream()
                .filter(n -> n.tipo().equals("RECORDATORIO_DEVOLUCION")).findFirst().orElseThrow();
        Instant esperado = ZonedDateTime.of(2026, 9, 1, 16, 45, 0, 0, ZONA).toInstant();
        assertEquals(esperado, recordatorio.programadaPara());
    }

    @Test
    void soloSePuedeEntregarUnaSolicitudAceptada() {
        existe(solicitud(EstadoSolicitud.PENDIENTE));

        ApiException e = assertThrows(ApiException.class, () -> service.entregar(admin, 10L));

        assertEquals(HttpStatus.CONFLICT, e.getStatus());
    }

    @Test
    void devolverLiberaElStockYRegistraLaRecepcion() {
        Solicitud s = solicitud(EstadoSolicitud.ENTREGADA);
        existe(s);

        SolicitudResponse r = service.devolver(admin, 10L);

        assertEquals(EstadoSolicitud.DEVUELTA, r.estado());
        assertNotNull(r.devueltaEn());
        verify(inventario).liberar(List.of(new InventarioClient.Item(1L, 2)));
        verify(auditoria).registrar(argThat(reg -> reg.hito().equals("DEVUELTO")));
    }

    @Test
    void finalizarSoloDesdeDevuelta() {
        existe(solicitud(EstadoSolicitud.ENTREGADA));
        assertThrows(ApiException.class, () -> service.finalizar(admin, 10L));
    }

    @Test
    void finalizarCierraElTicket() {
        existe(solicitud(EstadoSolicitud.DEVUELTA));
        assertEquals(EstadoSolicitud.FINALIZADA, service.finalizar(admin, 10L).estado());
    }

    @Test
    void solicitudInexistenteRetorna404() {
        when(repo.findById(99L)).thenReturn(Optional.empty());
        ApiException e = assertThrows(ApiException.class, () -> service.aceptar(admin, 99L));
        assertEquals(HttpStatus.NOT_FOUND, e.getStatus());
    }

    // ---------------------------------------------------------------- consultas

    @Test
    void elSolicitanteListaSoloSusSolicitudes() {
        when(repo.findBySolicitanteRutOrderByCreadaEnDesc("12345678-5"))
                .thenReturn(List.of(solicitud(EstadoSolicitud.PENDIENTE)));

        assertEquals(1, service.listar(docente, null).size());
        verify(repo, never()).findAllByOrderByCreadaEnDesc();
    }

    @Test
    void elAdministradorListaTodasYPuedeFiltrarPorEstado() {
        when(repo.findByEstadoOrderByCreadaEnDesc(EstadoSolicitud.PENDIENTE))
                .thenReturn(List.of(solicitud(EstadoSolicitud.PENDIENTE)));

        assertEquals(1, service.listar(admin, EstadoSolicitud.PENDIENTE).size());
    }

    @Test
    void unSolicitanteNoPuedeVerLaSolicitudDeOtro() {
        Solicitud ajena = solicitud(EstadoSolicitud.PENDIENTE);
        ajena.setSolicitanteRut("22222222-2");
        existe(ajena);

        ApiException e = assertThrows(ApiException.class, () -> service.obtener(docente, 10L));

        assertEquals(HttpStatus.FORBIDDEN, e.getStatus());
    }

    @Test
    void elAdministradorPuedeVerCualquierSolicitud() {
        existe(solicitud(EstadoSolicitud.PENDIENTE));
        assertEquals(10L, service.obtener(admin, 10L).id());
    }
}
