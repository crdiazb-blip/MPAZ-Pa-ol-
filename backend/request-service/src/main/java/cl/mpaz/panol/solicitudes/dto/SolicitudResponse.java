package cl.mpaz.panol.solicitudes.dto;

import cl.mpaz.panol.solicitudes.model.EstadoSolicitud;
import cl.mpaz.panol.solicitudes.model.Solicitud;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.List;

public record SolicitudResponse(
        Long id,
        String solicitanteRut,
        String solicitanteNombre,
        String solicitanteCargo,
        String solicitanteCorreo,
        String sala,
        String fecha,
        String horaInicio,
        String horaDevolucion,
        EstadoSolicitud estado,
        String comentarioGestion,
        Instant creadaEn,
        Instant aceptadaEn,
        Instant entregadaEn,
        Instant devueltaEn,
        List<Item> items
) {

    private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("HH:mm");

    public record Item(Long equipoId, String nombreEquipo, int cantidad) {
    }

    public static SolicitudResponse de(Solicitud s) {
        return new SolicitudResponse(
                s.getId(), s.getSolicitanteRut(), s.getSolicitanteNombre(), s.getSolicitanteCargo(),
                s.getSolicitanteCorreo(), s.getSala(), s.getFecha().toString(),
                s.getHoraInicio().format(HORA), s.getHoraDevolucion().format(HORA),
                s.getEstado(), s.getComentarioGestion(), s.getCreadaEn(), s.getAceptadaEn(),
                s.getEntregadaEn(), s.getDevueltaEn(),
                s.getItems().stream().map(i -> new Item(i.getEquipoId(), i.getNombreEquipo(), i.getCantidad())).toList());
    }
}
