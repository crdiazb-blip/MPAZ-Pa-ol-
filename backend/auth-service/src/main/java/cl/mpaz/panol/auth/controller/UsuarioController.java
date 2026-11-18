package cl.mpaz.panol.auth.controller;

import cl.mpaz.panol.auth.dto.UsuarioDto;
import cl.mpaz.panol.auth.exception.ApiException;
import cl.mpaz.panol.auth.model.Rol;
import cl.mpaz.panol.auth.repository.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/usuarios")
public class UsuarioController {

    private final UsuarioRepository usuarios;

    public UsuarioController(UsuarioRepository usuarios) {
        this.usuarios = usuarios;
    }

    /** Solo el perfil ADMIN puede listar usuarios. El rol lo agrega el gateway. */
    @GetMapping
    public List<UsuarioDto> listar(@RequestHeader(value = "X-User-Rol", required = false) String rol) {
        if (!Rol.ADMIN.name().equals(rol)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Solo el administrador puede listar usuarios.");
        }
        return usuarios.findAll().stream().map(UsuarioDto::de).toList();
    }
}
