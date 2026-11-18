package cl.mpaz.panol.auth.service;

import cl.mpaz.panol.auth.model.Rol;
import cl.mpaz.panol.auth.model.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

@Service
public class JwtService {

    private final SecretKey clave;
    private final long expiracionMinutos;

    public JwtService(@Value("${jwt.secret}") String secreto,
                      @Value("${jwt.expiration-minutes:480}") long expiracionMinutos) {
        if (secreto == null || secreto.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalStateException("JWT_SECRET debe tener al menos 32 caracteres");
        }
        this.clave = Keys.hmacShaKeyFor(secreto.getBytes(StandardCharsets.UTF_8));
        this.expiracionMinutos = expiracionMinutos;
    }

    public record Emitido(String token, Instant expiraEn) {
    }

    public record Datos(String jti, String rut, Long usuarioId, Rol rol, Instant expiraEn) {
    }

    public Emitido generar(Usuario usuario) {
        Instant ahora = Instant.now();
        Instant expira = ahora.plusSeconds(expiracionMinutos * 60);
        String token = Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject(usuario.getRut())
                .claim("uid", usuario.getId())
                .claim("rol", usuario.getRol().name())
                .issuedAt(Date.from(ahora))
                .expiration(Date.from(expira))
                .signWith(clave)
                .compact();
        return new Emitido(token, expira);
    }

    /** Devuelve los datos del token si la firma y la expiración son válidas. */
    public Optional<Datos> leer(String token) {
        try {
            Claims c = Jwts.parser().verifyWith(clave).build().parseSignedClaims(token).getPayload();
            return Optional.of(new Datos(
                    c.getId(),
                    c.getSubject(),
                    c.get("uid", Number.class).longValue(),
                    Rol.valueOf(c.get("rol", String.class)),
                    c.getExpiration().toInstant()));
        } catch (JwtException | IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}
