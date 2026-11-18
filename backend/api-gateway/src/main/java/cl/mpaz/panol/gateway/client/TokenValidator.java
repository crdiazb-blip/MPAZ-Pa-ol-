package cl.mpaz.panol.gateway.client;

import cl.mpaz.panol.gateway.config.RutaResolver;
import cl.mpaz.panol.gateway.exception.GatewayException;
import cl.mpaz.panol.gateway.model.UsuarioToken;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

/** Valida cada token consultando al auth-service (así un logout invalida la sesión al instante). */
@Component
public class TokenValidator {

    private final RestClient rest;
    private final RutaResolver rutas;

    public TokenValidator(RestClient rest, RutaResolver rutas) {
        this.rest = rest;
        this.rutas = rutas;
    }

    public UsuarioToken validar(String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ") || authorization.length() < 8) {
            throw new GatewayException(HttpStatus.UNAUTHORIZED, "Falta el token de autenticación.");
        }
        String base = rutas.getRutas().get("auth");
        try {
            UsuarioToken usuario = rest.get().uri(base + "/auth/validate")
                    .header(HttpHeaders.AUTHORIZATION, authorization)
                    .retrieve().body(UsuarioToken.class);
            if (usuario == null || usuario.rut() == null || usuario.rol() == null) {
                throw new GatewayException(HttpStatus.UNAUTHORIZED, "Token inválido.");
            }
            return usuario;
        } catch (RestClientResponseException e) {
            if (e.getStatusCode().value() == 401) {
                throw new GatewayException(HttpStatus.UNAUTHORIZED, "Sesión inválida o expirada. Inicia sesión nuevamente.");
            }
            throw new GatewayException(HttpStatus.BAD_GATEWAY, "El servicio de autenticación no respondió correctamente.");
        } catch (ResourceAccessException e) {
            throw new GatewayException(HttpStatus.SERVICE_UNAVAILABLE, "El servicio de autenticación no está disponible.");
        }
    }
}
