package cl.mpaz.panol.inventario.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "equipos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Equipo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String nombre;

    @Column(nullable = false, length = 80)
    private String marca;

    @Column(nullable = false, length = 80)
    private String modelo;

    @Column(length = 300)
    private String descripcion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Categoria categoria;

    @Column(name = "stock_total", nullable = false)
    private int stockTotal;

    @Column(name = "stock_disponible", nullable = false)
    private int stockDisponible;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 25)
    private EstadoEquipo estado;

    /** Ubicación de resguardo en el pañol o ubicación fija ficticia de switches y routers. */
    @Column(nullable = false, length = 120)
    private String ubicacion;

    /** false = dado de baja (retirado del inventario). */
    @Column(nullable = false)
    @Builder.Default
    private boolean activo = true;

    public boolean isReservable() {
        return activo && estado == EstadoEquipo.DISPONIBLE && stockDisponible > 0;
    }
}
