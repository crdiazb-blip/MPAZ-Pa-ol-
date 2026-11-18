package cl.mpaz.panol.solicitudes.model;

/**
 * Ciclo de vida del ticket:
 * PENDIENTE -> ACEPTADA | RECHAZADA ; ACEPTADA -> ENTREGADA -> DEVUELTA -> FINALIZADA.
 */
public enum EstadoSolicitud {
    PENDIENTE,
    ACEPTADA,
    RECHAZADA,
    ENTREGADA,
    DEVUELTA,
    FINALIZADA
}
