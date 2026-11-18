package cl.mpaz.panol.solicitudes.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "solicitud_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SolicitudItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "solicitud_id", nullable = false)
    private Solicitud solicitud;

    @Column(name = "equipo_id", nullable = false)
    private Long equipoId;

    @Column(name = "nombre_equipo", nullable = false, length = 120)
    private String nombreEquipo;

    @Column(nullable = false)
    private int cantidad;
}
