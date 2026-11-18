package cl.mpaz.panol.auth.dto;

import java.time.Instant;

public record LoginResponse(String token, Instant expiraEn, UsuarioDto usuario) {
}
