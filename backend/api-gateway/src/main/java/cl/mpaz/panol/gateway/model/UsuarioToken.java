package cl.mpaz.panol.gateway.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** Usuario devuelto por el auth-service al validar un token. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record UsuarioToken(Long id, String rut, String nombre, String cargo, String correo, String rol) {
}
