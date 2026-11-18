package cl.mpaz.panol.inventario.repository;

import cl.mpaz.panol.inventario.model.Equipo;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface EquipoRepository extends JpaRepository<Equipo, Long> {

    List<Equipo> findByActivoTrueOrderByNombreAsc();

    Optional<Equipo> findByIdAndActivoTrue(Long id);

    /** Bloquea las filas para que dos reservas simultaneas no descuenten el mismo stock. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from Equipo e where e.id in :ids order by e.id")
    List<Equipo> bloquearPorIds(@Param("ids") Collection<Long> ids);
}
