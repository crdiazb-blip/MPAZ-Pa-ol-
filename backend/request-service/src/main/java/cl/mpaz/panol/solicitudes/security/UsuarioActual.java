package cl.mpaz.panol.solicitudes.security;

import cl.mpaz.panol.solicitudes.exception.ApiException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

/**
 * Usuario autenticado. El api-gateway valida el token y entrega estos datos en cabeceras
 * (nombre, cargo y correo van codificados en URL para admitir tildes y la letra ñ).
 */
public record UsuarioActual(String rut, String nombre, String cargo, String correo, String rol) {

    public static final String RUT = "X-User-Rut";
    public static final String NOMBRE = "X-User-Nombre";
    public static final String CARGO = "X-User-Cargo";
    public static final String CORREO = "X-User-Correo";
    public static final String ROL = "X-User-Rol";

    public static UsuarioActual de(HttpHeaders headers) {
        String rut = headers.getFirst(RUT);
        String rol = headers.getFirst(ROL);
        if (rut == null || rut.isBlank() || rol == null || rol.isBlank()) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "No se pudo identificar al usuario.");
        }
        return new UsuarioActual(rut, decodificar(headers.getFirst(NOMBRE)), decodificar(headers.getFirst(CARGO)),
                decodificar(headers.getFirst(CORREO)), rol);
    }

    public boolean esAdmin() {
        return "ADMIN".equals(rol);
    }

    public boolean esSolicitante() {
        return "SOLICITANTE".equals(rol);
    }

    private static String decodificar(String valor) {
        return valor == null ? null : URLDecoder.decode(valor, StandardCharsets.UTF_8);
    }
}
