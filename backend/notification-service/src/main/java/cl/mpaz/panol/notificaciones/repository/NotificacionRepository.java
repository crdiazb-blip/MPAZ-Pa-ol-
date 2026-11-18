package cl.mpaz.panol.notificaciones.repository;

import cl.mpaz.panol.notificaciones.model.Notificacion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;

public interface NotificacionRepository extends JpaRepository<Notificacion, Long> {

    /** Notificaciones ya enviadas a un usuario, la más reciente primero. */
    List<Notificacion> findByDestinatarioRutAndEnviadaEnIsNotNullOrderByEnviadaEnDescIdDesc(String destinatarioRut);

    /** Pendientes cuya hora programada ya llegó. */
    List<Notificacion> findByEnviadaEnIsNullAndProgramadaParaLessThanEqual(Instant limite);

    List<Notificacion> findAllByOrderByCreadaEnDescIdDesc();
}
