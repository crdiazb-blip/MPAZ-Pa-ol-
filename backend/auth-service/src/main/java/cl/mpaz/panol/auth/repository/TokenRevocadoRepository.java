package cl.mpaz.panol.auth.repository;

import cl.mpaz.panol.auth.model.TokenRevocado;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;

public interface TokenRevocadoRepository extends JpaRepository<TokenRevocado, String> {

    void deleteByExpiraEnBefore(Instant limite);
}
