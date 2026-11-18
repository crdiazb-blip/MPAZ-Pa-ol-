package cl.mpaz.panol.auditoria.model;

/** Hitos del ciclo de vida de una solicitud: Solicitado -> Aprobado/Rechazado -> Entregado -> Devuelto. */
public enum Hito {
    SOLICITADO,
    APROBADO,
    RECHAZADO,
    ENTREGADO,
    DEVUELTO,
    FINALIZADO
}
