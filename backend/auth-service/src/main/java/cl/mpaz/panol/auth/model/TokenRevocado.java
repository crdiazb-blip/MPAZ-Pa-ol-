package cl.mpaz.panol.auth.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

/** Token JWT invalidado por un cierre de sesión (logout). */
@Entity
@Table(name = "tokens_revocados")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TokenRevocado {

    @Id
    @Column(length = 64)
    private String jti;

    @Column(name = "expira_en", nullable = false)
    private Instant expiraEn;
}
