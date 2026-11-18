package cl.mpaz.panol.auditoria.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "bitacora", indexes = {
        @Index(name = "idx_bitacora_solicitud", columnList = "solicitud_id"),
        @Index(name = "idx_bitacora_solicitante", columnList = "solicitante_rut")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegistroBitacora {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "solicitud_id", nullable = false)
    private Long solicitudId;

    /** Funcionario dueño de la solicitud (permite que cada uno vea solo su historial). */
    @Column(name = "solicitante_rut", nullable = false, length = 12)
    private String solicitanteRut;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Hito hito;

    /** Quien ejecutó la acción (el solicitante o el encargado de TI). */
    @Column(name = "actor_rut", nullable = false, length = 12)
    private String actorRut;

    @Column(name = "actor_nombre", nullable = false, length = 120)
    private String actorNombre;

    @Column(length = 500)
    private String detalle;

    @Column(nullable = false)
    private Instant fecha;
}
