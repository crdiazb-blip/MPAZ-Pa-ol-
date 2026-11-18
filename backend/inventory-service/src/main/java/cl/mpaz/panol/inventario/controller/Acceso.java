package cl.mpaz.panol.inventario.controller;

import cl.mpaz.panol.inventario.exception.ApiException;
import org.springframework.http.HttpStatus;

/** El api-gateway valida el token y entrega el rol del usuario en la cabecera X-User-Rol. */
final class Acceso {

    static final String HEADER_ROL = "X-User-Rol";

    private Acceso() {
    }

    static void exigirAdmin(String rol) {
        if (!"ADMIN".equals(rol)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Esta acción es solo para el perfil Administrador.");
        }
    }
}
