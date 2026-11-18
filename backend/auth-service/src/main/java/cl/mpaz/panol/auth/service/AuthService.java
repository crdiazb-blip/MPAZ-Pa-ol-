package cl.mpaz.panol.auth.service;

import cl.mpaz.panol.auth.dto.LoginRequest;
import cl.mpaz.panol.auth.dto.LoginResponse;
import cl.mpaz.panol.auth.dto.UsuarioDto;
import cl.mpaz.panol.auth.exception.ApiException;
import cl.mpaz.panol.auth.model.TokenRevocado;
import cl.mpaz.panol.auth.model.Usuario;
import cl.mpaz.panol.auth.repository.TokenRevocadoRepository;
import cl.mpaz.panol.auth.repository.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class AuthService {

    private static final String CREDENCIALES_INVALIDAS = "RUT o contraseña incorrectos.";

    private final UsuarioRepository usuarios;
    private final TokenRevocadoRepository revocados;
    private final PasswordEncoder encoder;
    private final JwtService jwt;

    public AuthService(UsuarioRepository usuarios, TokenRevocadoRepository revocados,
                       PasswordEncoder encoder, JwtService jwt) {
        this.usuarios = usuarios;
        this.revocados = revocados;
        this.encoder = encoder;
        this.jwt = jwt;
    }

    public LoginResponse login(LoginRequest req) {
        String rut = RutUtil.normalizar(req.rut());
        Usuario usuario = usuarios.findByRut(rut)
                .filter(Usuario::isActivo)
                .filter(u -> encoder.matches(req.password(), u.getPasswordHash()))
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, CREDENCIALES_INVALIDAS));
        JwtService.Emitido emitido = jwt.generar(usuario);
        return new LoginResponse(emitido.token(), emitido.expiraEn(), UsuarioDto.de(usuario));
    }

    /** Valida firma, expiración y que el token no haya sido cerrado con logout. */
    @Transactional(readOnly = true)
    public UsuarioDto validar(String token) {
        JwtService.Datos datos = jwt.leer(token)
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Token inválido o expirado."));
        if (revocados.existsById(datos.jti())) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "La sesión fue cerrada.");
        }
        return usuarios.findByRut(datos.rut())
                .filter(Usuario::isActivo)
                .map(UsuarioDto::de)
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Usuario inactivo o inexistente."));
    }

    @Transactional
    public void logout(String token) {
        JwtService.Datos datos = jwt.leer(token)
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Token inválido o expirado."));
        revocados.deleteByExpiraEnBefore(Instant.now());
        if (!revocados.existsById(datos.jti())) {
            revocados.save(new TokenRevocado(datos.jti(), datos.expiraEn()));
        }
    }
}
