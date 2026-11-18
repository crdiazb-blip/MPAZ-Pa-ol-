package cl.mpaz.panol.auth.dto;

import cl.mpaz.panol.auth.model.Rol;
import cl.mpaz.panol.auth.model.Usuario;

public record UsuarioDto(Long id, String rut, String nombre, String cargo, String correo, Rol rol) {

    public static UsuarioDto de(Usuario u) {
        return new UsuarioDto(u.getId(), u.getRut(), u.getNombre(), u.getCargo(), u.getCorreo(), u.getRol());
    }
}
