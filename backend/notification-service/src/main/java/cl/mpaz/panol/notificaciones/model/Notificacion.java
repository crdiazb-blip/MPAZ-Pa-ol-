package cl.mpaz.panol.notificaciones.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

/**
 * Notificación interna simulada. No se envían correos reales ni se usan credenciales:
 * "enviada" significa que quedó disponible para el usuario dentro de la aplicación.
 */
@Entity
@Table(name = "notificaciones", indexes = {
        @Index(name = "idx_notif_destinatario", columnList = "destinatario_rut"),
        @Index(name = "idx_notif_pendientes", columnList = "enviada_en, programada_para")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notificacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "destinatario_rut", nullable = false, length = 12)
    private String destinatarioRut;

    @Column(name = "solicitud_id")
    private Long solicitudId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TipoNotificacion tipo;

    @Column(nullable = false, length = 120)
    private String titulo;

    @Column(nullable = false, length = 500)
    private String mensaje;

    @Column(name = "creada_en", nullable = false)
    private Instant creadaEn;

    /** Si es null se envía de inmediato; si no, un job la envía cuando llegue la hora. */
    @Column(name = "programada_para")
    private Instant programadaPara;

    /** Indicador del log de notificación: null = aún pendiente de envío. */
    @Column(name = "enviada_en")
    private Instant enviadaEn;

    @Column(nullable = false)
    private boolean leida;
}
