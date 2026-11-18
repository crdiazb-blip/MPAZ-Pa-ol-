package cl.mpaz.panol.auditoria.service;

import cl.mpaz.panol.auditoria.dto.RegistroRequest;
import cl.mpaz.panol.auditoria.dto.RegistroResponse;
import cl.mpaz.panol.auditoria.exception.ApiException;
import cl.mpaz.panol.auditoria.model.RegistroBitacora;
import cl.mpaz.panol.auditoria.repository.BitacoraRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.List;

@Service
public class BitacoraService {

    private final BitacoraRepository repo;
    private final Clock reloj;

    @Autowired
    public BitacoraService(BitacoraRepository repo) {
        this(repo, Clock.systemUTC());
    }

    BitacoraService(BitacoraRepository repo, Clock reloj) {
        this.repo = repo;
        this.reloj = reloj;
    }

    @Transactional
    public RegistroResponse registrar(RegistroRequest req) {
        RegistroBitacora registro = RegistroBitacora.builder()
                .solicitudId(req.solicitudId())
                .solicitanteRut(req.solicitanteRut())
                .hito(req.hito())
                .actorRut(req.actorRut())
                .actorNombre(req.actorNombre())
                .detalle(req.detalle())
                .fecha(Instant.now(reloj))
                .build();
        return RegistroResponse.de(repo.save(registro));
    }

    /**
     * El administrador (Encargado TI) audita todo el historial; un solicitante
     * solo ve los registros de sus propias solicitudes.
     */
    @Transactional(readOnly = true)
    public List<RegistroResponse> listar(String rol, String rut, Long solicitudId) {
        boolean admin = "ADMIN".equals(rol);
        if (!admin && (rut == null || rut.isBlank())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "No se pudo identificar al usuario.");
        }
        List<RegistroBitacora> registros;
        if (admin) {
            registros = solicitudId != null
                    ? repo.findBySolicitudIdOrderByFechaAscIdAsc(solicitudId)
                    : repo.findAllByOrderByFechaDescIdDesc();
        } else {
            registros = solicitudId != null
                    ? repo.findBySolicitudIdAndSolicitanteRutOrderByFechaAscIdAsc(solicitudId, rut)
                    : repo.findBySolicitanteRutOrderByFechaDescIdDesc(rut);
        }
        return registros.stream().map(RegistroResponse::de).toList();
    }
}
