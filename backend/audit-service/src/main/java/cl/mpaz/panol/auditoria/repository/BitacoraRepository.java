package cl.mpaz.panol.auditoria.repository;

import cl.mpaz.panol.auditoria.model.RegistroBitacora;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BitacoraRepository extends JpaRepository<RegistroBitacora, Long> {

    List<RegistroBitacora> findAllByOrderByFechaDescIdDesc();

    List<RegistroBitacora> findBySolicitudIdOrderByFechaAscIdAsc(Long solicitudId);

    List<RegistroBitacora> findBySolicitanteRutOrderByFechaDescIdDesc(String solicitanteRut);

    List<RegistroBitacora> findBySolicitudIdAndSolicitanteRutOrderByFechaAscIdAsc(Long solicitudId, String solicitanteRut);
}
