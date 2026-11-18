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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class SolicitudService {

    private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("HH:mm");
    private static final long MINUTOS_AVISO_DEVOLUCION = 15;

    private final SolicitudRepository repo;
    private final InventarioClient inventario;
    private final AuditoriaClient auditoria;
    private final NotificacionesClient notificaciones;
    private final Clock reloj;
    private final int anticipacionHoras;

    public SolicitudService(SolicitudRepository repo, InventarioClient inventario, AuditoriaClient auditoria,
                            NotificacionesClient notificaciones, Clock reloj,
                            @Value("${panol.anticipacion-horas:3}") int anticipacionHoras) {
        this.repo = repo;
        this.inventario = inventario;
        this.auditoria = auditoria;
        this.notificaciones = notificaciones;
        this.reloj = reloj;
        this.anticipacionHoras = anticipacionHoras;
    }

    // ---------------------------------------------------------------- Solicitante

    @Transactional
    public SolicitudResponse crear(UsuarioActual usuario, SolicitudRequest req) {
        if (!usuario.esSolicitante()) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Solo el perfil Solicitante puede crear solicitudes.");
        }
        LocalDate fecha = parsearFecha(req.fecha());
        LocalTime inicio = parsearHora(req.horaInicio(), "inicio");
        LocalTime devolucion = parsearHora(req.horaDevolucion(), "devolución");
        validarHorarios(fecha, inicio, devolucion);
        Map<Long, Integer> cantidades = agruparItems(req.items());

        Solicitud solicitud = Solicitud.builder()
                .solicitanteRut(usuario.rut())
                .solicitanteNombre(usuario.nombre() != null ? usuario.nombre() : usuario.rut())
                .solicitanteCargo(usuario.cargo())
                .solicitanteCorreo(usuario.correo())
                .sala(req.sala().trim())
                .fecha(fecha)
                .horaInicio(inicio)
                .horaDevolucion(devolucion)
                .estado(EstadoSolicitud.PENDIENTE)
                .creadaEn(reloj.instant())
                .build();

        cantidades.forEach((equipoId, cantidad) -> {
            InventarioClient.Equipo equipo = inventario.obtener(equipoId);
            if (!equipo.reservable()) {
                throw new ApiException(HttpStatus.CONFLICT, equipo.nombre() + " no está disponible.");
            }
            if (cantidad > equipo.stockDisponible()) {
                throw new ApiException(HttpStatus.CONFLICT,
                        "Solo quedan " + equipo.stockDisponible() + " unidades de " + equipo.nombre() + ".");
            }
            solicitud.agregarItem(SolicitudItem.builder()
                    .equipoId(equipoId).nombreEquipo(equipo.nombre()).cantidad(cantidad).build());
        });

        Solicitud guardada = repo.save(solicitud);
        registrar(guardada, "SOLICITADO", usuario, "Solicitud creada para " + guardada.getSala());
        return SolicitudResponse.de(guardada);
    }

    @Transactional(readOnly = true)
    public List<SolicitudResponse> listar(UsuarioActual usuario, EstadoSolicitud estado) {
        List<Solicitud> solicitudes;
        if (usuario.esAdmin()) {
            solicitudes = estado == null ? repo.findAllByOrderByCreadaEnDesc()
                    : repo.findByEstadoOrderByCreadaEnDesc(estado);
        } else {
            solicitudes = estado == null ? repo.findBySolicitanteRutOrderByCreadaEnDesc(usuario.rut())
                    : repo.findBySolicitanteRutAndEstadoOrderByCreadaEnDesc(usuario.rut(), estado);
        }
        return solicitudes.stream().map(SolicitudResponse::de).toList();
    }

    @Transactional(readOnly = true)
    public SolicitudResponse obtener(UsuarioActual usuario, Long id) {
        Solicitud s = cargar(id);
        if (!usuario.esAdmin() && !s.getSolicitanteRut().equals(usuario.rut())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "No puedes ver solicitudes de otros funcionarios.");
        }
        return SolicitudResponse.de(s);
    }

    // ---------------------------------------------------------------- Administrador

    /** Acepta la solicitud y descuenta de inmediato el stock disponible del inventario. */
    @Transactional
    public SolicitudResponse aceptar(UsuarioActual usuario, Long id) {
        Solicitud s = prepararTransicion(usuario, id, EstadoSolicitud.PENDIENTE, "aceptar");
        List<InventarioClient.Item> items = itemsInventario(s);
        inventario.reservar(items);
        try {
            s.setEstado(EstadoSolicitud.ACEPTADA);
            s.setAceptadaEn(reloj.instant());
            repo.save(s);
        } catch (RuntimeException e) {
            inventario.liberar(items);
            throw e;
        }
        registrar(s, "APROBADO", usuario, "Solicitud aceptada");
        notificar(s, "SOLICITUD_ACEPTADA", "Solicitud aceptada",
                "Tu solicitud #" + s.getId() + " fue aceptada. Retira los equipos a las " + s.getHoraInicio().format(HORA) + ".", null);
        return SolicitudResponse.de(s);
    }

    @Transactional
    public SolicitudResponse rechazar(UsuarioActual usuario, Long id, String comentario) {
        if (comentario == null || comentario.isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "La justificación del rechazo es obligatoria.");
        }
        Solicitud s = prepararTransicion(usuario, id, EstadoSolicitud.PENDIENTE, "rechazar");
        s.setEstado(EstadoSolicitud.RECHAZADA);
        s.setComentarioGestion(comentario.trim());
        repo.save(s);
        registrar(s, "RECHAZADO", usuario, comentario.trim());
        notificar(s, "SOLICITUD_RECHAZADA", "Solicitud rechazada",
                "Tu solicitud #" + s.getId() + " fue rechazada: " + comentario.trim(), null);
        return SolicitudResponse.de(s);
    }

    /** Deja constancia de la fecha y hora exacta en que los equipos salen del pañol. */
    @Transactional
    public SolicitudResponse entregar(UsuarioActual usuario, Long id) {
        Solicitud s = prepararTransicion(usuario, id, EstadoSolicitud.ACEPTADA, "entregar");
        s.setEstado(EstadoSolicitud.ENTREGADA);
        s.setEntregadaEn(reloj.instant());
        repo.save(s);
        registrar(s, "ENTREGADO", usuario, "Equipos entregados a " + s.getSolicitanteNombre());
        notificar(s, "EQUIPOS_ENTREGADOS", "Equipos entregados",
                "Recibiste los equipos de la solicitud #" + s.getId() + ". Devuélvelos a las "
                        + s.getHoraDevolucion().format(HORA) + ".", null);
        programarRecordatorioDevolucion(s);
        return SolicitudResponse.de(s);
    }

    /** Recepción conforme: devuelve el stock al inventario. */
    @Transactional
    public SolicitudResponse devolver(UsuarioActual usuario, Long id) {
        Solicitud s = prepararTransicion(usuario, id, EstadoSolicitud.ENTREGADA, "registrar la devolución de");
        inventario.liberar(itemsInventario(s));
        s.setEstado(EstadoSolicitud.DEVUELTA);
        s.setDevueltaEn(reloj.instant());
        repo.save(s);
        registrar(s, "DEVUELTO", usuario, "Equipos recibidos conforme");
        notificar(s, "EQUIPOS_DEVUELTOS", "Devolución registrada",
                "Se registró la devolución de la solicitud #" + s.getId() + ". ¡Gracias!", null);
        return SolicitudResponse.de(s);
    }

    @Transactional
    public SolicitudResponse finalizar(UsuarioActual usuario, Long id) {
        Solicitud s = prepararTransicion(usuario, id, EstadoSolicitud.DEVUELTA, "finalizar");
        s.setEstado(EstadoSolicitud.FINALIZADA);
        repo.save(s);
        registrar(s, "FINALIZADO", usuario, "Solicitud finalizada");
        return SolicitudResponse.de(s);
    }

    // ---------------------------------------------------------------- Reglas

    /**
     * Regla de anticipación crítica: el uso debe comenzar al menos N horas (3 por defecto)
     * después del momento de la solicitud. Se valida en el servidor, además de en la app.
     */
    void validarHorarios(LocalDate fecha, LocalTime inicio, LocalTime devolucion) {
        if (!devolucion.isAfter(inicio)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "La hora de devolución debe ser posterior a la hora de inicio.");
        }
        Instant usoDesde = LocalDateTime.of(fecha, inicio).atZone(reloj.getZone()).toInstant();
        Instant minimo = reloj.instant().plus(anticipacionHoras, ChronoUnit.HOURS);
        if (usoDesde.isBefore(minimo)) {
            throw new ApiException(HttpStatus.BAD_REQUEST,
                    "Debes reservar con al menos " + anticipacionHoras + " horas de anticipación.");
        }
    }

    private Solicitud prepararTransicion(UsuarioActual usuario, Long id, EstadoSolicitud esperado, String accion) {
        if (!usuario.esAdmin()) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Esta acción es solo para el perfil Administrador.");
        }
        Solicitud s = cargar(id);
        if (s.getEstado() != esperado) {
            throw new ApiException(HttpStatus.CONFLICT,
                    "No se puede " + accion + " una solicitud en estado " + s.getEstado() + ".");
        }
        return s;
    }

    private Solicitud cargar(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Solicitud " + id + " no encontrada."));
    }

    private Map<Long, Integer> agruparItems(List<ItemRequest> items) {
        if (items == null || items.isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "El carrito no puede estar vacío.");
        }
        Map<Long, Integer> cantidades = new LinkedHashMap<>();
        for (ItemRequest item : items) {
            if (item.cantidad() < 1) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "La cantidad debe ser al menos 1.");
            }
            cantidades.merge(item.equipoId(), item.cantidad(), Integer::sum);
        }
        return cantidades;
    }

    private List<InventarioClient.Item> itemsInventario(Solicitud s) {
        return s.getItems().stream().map(i -> new InventarioClient.Item(i.getEquipoId(), i.getCantidad())).toList();
    }

    private LocalDate parsearFecha(String valor) {
        try {
            return LocalDate.parse(valor);
        } catch (DateTimeParseException e) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "La fecha no es válida (usa yyyy-MM-dd).");
        }
    }

    private LocalTime parsearHora(String valor, String nombre) {
        try {
            return LocalTime.parse(valor, HORA);
        } catch (DateTimeParseException e) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "La hora de " + nombre + " no es válida (usa HH:mm).");
        }
    }

    // ---------------------------------------------------------------- Integraciones (no bloqueantes)

    private void registrar(Solicitud s, String hito, UsuarioActual actor, String detalle) {
        auditoria.registrar(new AuditoriaClient.Registro(s.getId(), s.getSolicitanteRut(), hito,
                actor.rut(), actor.nombre() != null ? actor.nombre() : actor.rut(), detalle));
    }

    private void notificar(Solicitud s, String tipo, String titulo, String mensaje, Instant programadaPara) {
        notificaciones.enviar(new NotificacionesClient.Notificacion(
                s.getSolicitanteRut(), s.getId(), tipo, titulo, mensaje, programadaPara));
    }

    private void programarRecordatorioDevolucion(Solicitud s) {
        Instant devolucion = LocalDateTime.of(s.getFecha(), s.getHoraDevolucion()).atZone(reloj.getZone()).toInstant();
        Instant aviso = devolucion.minus(MINUTOS_AVISO_DEVOLUCION, ChronoUnit.MINUTES);
        if (aviso.isAfter(reloj.instant())) {
            notificar(s, "RECORDATORIO_DEVOLUCION", "Recordatorio de devolución",
                    "En " + MINUTOS_AVISO_DEVOLUCION + " minutos (" + s.getHoraDevolucion().format(HORA)
                            + ") debes devolver los equipos de la solicitud #" + s.getId() + ".", aviso);
        }
    }
}
