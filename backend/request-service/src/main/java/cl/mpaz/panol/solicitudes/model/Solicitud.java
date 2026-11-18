package cl.mpaz.panol.solicitudes.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "solicitudes", indexes = {
        @Index(name = "idx_solicitudes_rut", columnList = "solicitante_rut"),
        @Index(name = "idx_solicitudes_estado", columnList = "estado")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Solicitud {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "solicitante_rut", nullable = false, length = 12)
    private String solicitanteRut;

    @Column(name = "solicitante_nombre", nullable = false, length = 120)
    private String solicitanteNombre;

    @Column(name = "solicitante_cargo", length = 60)
    private String solicitanteCargo;

    @Column(name = "solicitante_correo", length = 120)
    private String solicitanteCorreo;

    @Column(nullable = false, length = 80)
    private String sala;

    @Column(nullable = false)
    private LocalDate fecha;

    @Column(name = "hora_inicio", nullable = false)
    private LocalTime horaInicio;

    @Column(name = "hora_devolucion", nullable = false)
    private LocalTime horaDevolucion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoSolicitud estado;

    /** Justificación escrita obligatoria cuando el encargado de TI rechaza la solicitud. */
    @Column(name = "comentario_gestion", length = 500)
    private String comentarioGestion;

    @Column(name = "creada_en", nullable = false)
    private Instant creadaEn;

    @Column(name = "aceptada_en")
    private Instant aceptadaEn;

    @Column(name = "entregada_en")
    private Instant entregadaEn;

    @Column(name = "devuelta_en")
    private Instant devueltaEn;

    @OneToMany(mappedBy = "solicitud", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @Builder.Default
    private List<SolicitudItem> items = new ArrayList<>();

    public void agregarItem(SolicitudItem item) {
        item.setSolicitud(this);
        items.add(item);
    }
}
