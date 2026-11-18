package cl.mpaz.panol.solicitudes.repository;

import cl.mpaz.panol.solicitudes.model.EstadoSolicitud;
import cl.mpaz.panol.solicitudes.model.Solicitud;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SolicitudRepository extends JpaRepository<Solicitud, Long> {

    List<Solicitud> findAllByOrderByCreadaEnDesc();

    List<Solicitud> findByEstadoOrderByCreadaEnDesc(EstadoSolicitud estado);

    List<Solicitud> findBySolicitanteRutOrderByCreadaEnDesc(String solicitanteRut);

    List<Solicitud> findBySolicitanteRutAndEstadoOrderByCreadaEnDesc(String solicitanteRut, EstadoSolicitud estado);
}
